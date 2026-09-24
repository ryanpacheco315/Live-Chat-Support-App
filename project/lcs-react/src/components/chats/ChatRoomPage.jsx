import { useEffect, useRef, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { createStompClient } from "../../api/stomp";
import { closeChat, getChat, getChatMessages, getSimilarChats, resolveSelfServe, startChat } from "../../api/chats";
import ChatMessageBubble from "./ChatMessageBubble";

const CATEGORIES = ["HARDWARE", "SOFTWARE", "OTHER"];
const BOT_SENDER = { fullName: "Support Bot", role: "BOT" };

function ChatRoomPage({ user }) {
    const { id } = useParams();
    const isNew = id === "new";
    const chatId = isNew ? null : Number(id);
    const navigate = useNavigate();

    const clientSender = { fullName: user?.fullName ?? "You", role: "CLIENT" };
    const nextIntakeId = useRef(0);

    const [intakeStep, setIntakeStep] = useState("category");
    const [problem, setProblem] = useState({ category: "", subcategory: "", description: "" });
    const [intakeInput, setIntakeInput] = useState("");
    const [intakeError, setIntakeError] = useState(null);
    const [intakeLog, setIntakeLog] = useState([
        { id: "bot-0", sender: BOT_SENDER, body: "Hi! I just need a few quick details before connecting you with an agent." },
        { id: "bot-1", sender: BOT_SENDER, body: "What type of issue are you having?" },
    ]);

    function appendIntakeLog(entries) {
        setIntakeLog((current) => [
            ...current,
            ...entries.map((entry) => {
                nextIntakeId.current += 1;
                return { id: `local-${nextIntakeId.current}`, ...entry };
            }),
        ]);
    }

    function pickCategory(category) {
        setProblem((current) => ({ ...current, category }));
        appendIntakeLog([
            { sender: clientSender, body: category },
            { sender: BOT_SENDER, body: "Got it. Is there a more specific subcategory? Type one, or just hit send to skip." },
        ]);
        setIntakeStep("subcategory");
    }

    async function handleIntakeSend(event) {
        event.preventDefault();
        const text = intakeInput.trim();

        if (intakeStep === "subcategory") {
            setProblem((current) => ({ ...current, subcategory: text }));
            appendIntakeLog([
                { sender: clientSender, body: text || "(skipped)" },
                { sender: BOT_SENDER, body: "Last thing — describe the problem in a bit more detail." },
            ]);
            setIntakeInput("");
            setIntakeStep("description");
            return;
        }

        if (intakeStep === "description") {
            if (!text) return;
            appendIntakeLog([{ sender: clientSender, body: text }]);
            setIntakeInput("");
            setIntakeStep("submitting");
            setIntakeError(null);
            appendIntakeLog([{ sender: BOT_SENDER, body: "Thanks! Connecting you with an agent now..." }]);

            const result = await startChat({ ...problem, description: text });
            if (result.ok) {
                navigate(`/chat/${result.payload.id}`, { replace: true });
            } else {
                setIntakeError(result.payload?.[0] ?? "Something went wrong. Please try again.");
                appendIntakeLog([{ sender: BOT_SENDER, body: "Something went wrong on our end — want to try describing it again?" }]);
                setIntakeStep("description");
            }
        }
    }

    const [messages, setMessages] = useState([]);
    const [chat, setChat] = useState(null);
    const [accessDenied, setAccessDenied] = useState(false);
    const [closed, setClosed] = useState(false);
    const [closeError, setCloseError] = useState(null);
    const [body, setBody] = useState("");
    const [selfServeResults, setSelfServeResults] = useState(null);
    const [selfServeError, setSelfServeError] = useState(null);
    const [selfServeLoading, setSelfServeLoading] = useState(false);
    const [resolveError, setResolveError] = useState(null);
    const [resolveLoading, setResolveLoading] = useState(false);
    const clientRef = useRef(null);
    const messagesEndRef = useRef(null);

    function addMessage(message) {
        setMessages((current) =>
            current.some((existing) => existing.id === message.id) ? current : [...current, message]
        );
    }

    useEffect(() => {
        if (isNew) return;
        let isCancelled = false;

        async function loadChat() {
            const result = await getChat(chatId);
            if (!isCancelled && result.ok) {
                setChat(result.payload);
            }
        }
        loadChat();

        return () => {
            isCancelled = true;
        };
    }, [isNew, chatId]);

    useEffect(() => {
        if (isNew) return;
        let isCancelled = false;

        async function loadHistory() {
            const result = await getChatMessages(chatId);
            if (isCancelled) return;
            if (result.ok) {
                setMessages(result.payload);
            } else {
                setAccessDenied(true);
            }
        }
        loadHistory();

        const stompClient = createStompClient();
        clientRef.current = stompClient;

        stompClient.onConnect = () => {
            stompClient.subscribe(`/topic/chat/${chatId}`, (frame) => {
                const payload = JSON.parse(frame.body);
                if (payload.status) {
                    setClosed(true);
                } else {
                    addMessage(payload);
                    setChat((current) =>
                        current && current.status === "WAITING" ? { ...current, status: "ACTIVE" } : current
                    );
                }
            });
        };

        stompClient.activate();

        return () => {
            isCancelled = true;
            stompClient.deactivate();
        };
    }, [isNew, chatId]);

    useEffect(() => {
        if (!closed) return;

        const destination = user?.role === "AGENT" ? "/queue" : "/";
        const timeoutId = setTimeout(() => navigate(destination), 1500);
        return () => clearTimeout(timeoutId);
    }, [closed, user, navigate]);

    useEffect(() => {
        messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
    }, [messages, intakeLog]);

    function handleChange(event) {
        setBody(event.target.value);
    }

    function handleSend(event) {
        event.preventDefault();
        if (!body.trim() || !clientRef.current?.connected || closed) {
            return;
        }

        clientRef.current.publish({
            destination: `/app/chat/${chatId}/send`,
            body: JSON.stringify({ body }),
        });
        setBody("");
    }

    async function handleClose() {
        const result = await closeChat(chatId);
        if (result.ok) {
            setClosed(true);
        } else {
            setCloseError(result.payload?.[0] ?? "Could not close this chat.");
        }
    }

    async function handleSelfServeCheck() {
        setSelfServeLoading(true);
        setSelfServeError(null);
        const result = await getSimilarChats(chatId);
        setSelfServeLoading(false);
        if (result.ok) {
            setSelfServeResults(result.payload);
        } else {
            setSelfServeError(result.payload?.[0] ?? "Search is currently unavailable.");
        }
    }

    async function handleSelfServeResolve() {
        setResolveLoading(true);
        setResolveError(null);
        const result = await resolveSelfServe(chatId);
        setResolveLoading(false);
        if (result.ok) {
            setClosed(true);
        } else {
            setResolveError(result.payload?.[0] ?? "Could not close this chat.");
        }
    }

    if (isNew) {
        return (
            <div className="p-3">
                <h4 className="mb-3">Start a Chat</h4>

                <div className="border rounded p-4 mb-0" style={{ height: "60vh", overflowY: "auto" }}>
                    {intakeLog.map((message) => (
                        <ChatMessageBubble key={message.id} message={message} />
                    ))}
                    <div ref={messagesEndRef} />
                </div>

                {intakeStep === "category" && (
                    <div className="d-flex gap-2 pt-3">
                        {CATEGORIES.map((category) => (
                            <button
                                key={category}
                                type="button"
                                className="btn btn-outline-primary"
                                onClick={() => pickCategory(category)}
                            >
                                {category.charAt(0) + category.slice(1).toLowerCase()}
                            </button>
                        ))}
                    </div>
                )}

                {(intakeStep === "subcategory" || intakeStep === "description") && (
                    <form className="d-flex pt-3" onSubmit={handleIntakeSend}>
                        <input
                            className="form-control me-2"
                            type="text"
                            value={intakeInput}
                            onChange={(event) => setIntakeInput(event.target.value)}
                            placeholder={intakeStep === "subcategory" ? "Subcategory (optional)..." : "Describe the problem..."}
                        />
                        <button className="btn btn-primary" type="submit">
                            Send
                        </button>
                    </form>
                )}

                {intakeError && <div className="text-danger pt-2">{intakeError}</div>}
            </div>
        );
    }

    if (accessDenied) {
        return <p>You do not have access to this chat.</p>;
    }

    const waiting = chat && chat.status === "WAITING";

    return (
        <div className="p-3">
            <div className="d-flex justify-content-between align-items-center mb-3">
                <h4>Live Chat</h4>
                <div>
                    <button className="btn btn-danger" onClick={handleClose} disabled={closed || waiting}>
                        Close Chat
                    </button>
                    {closeError && <div className="text-danger">{closeError}</div>}
                </div>
            </div>

            {waiting && (
                <div className="alert alert-secondary">
                    Waiting for an agent to join...
                </div>
            )}

            {waiting && (
                <div className="mb-3">
                    <button
                        className="btn btn-outline-secondary btn-sm"
                        type="button"
                        onClick={handleSelfServeCheck}
                        disabled={selfServeLoading}
                    >
                        {selfServeLoading ? "Checking..." : "Try self-serve help"}
                    </button>

                    {selfServeError && <div className="text-danger mt-2">{selfServeError}</div>}

                    {selfServeResults && selfServeResults.length === 0 && (
                        <p className="text-muted mt-2 mb-0">No similar past chats found yet.</p>
                    )}

                    {selfServeResults && selfServeResults.length > 0 && (
                        <>
                            <ul className="list-group mt-2">
                                {selfServeResults.map((match) => (
                                    <li key={match.chatId} className="list-group-item">
                                        {match.summary}
                                    </li>
                                ))}
                            </ul>

                            <button
                                className="btn btn-success btn-sm mt-2"
                                type="button"
                                onClick={handleSelfServeResolve}
                                disabled={resolveLoading}
                            >
                                {resolveLoading ? "Closing..." : "This solved it — close my ticket"}
                            </button>

                            {resolveError && <div className="text-danger mt-2">{resolveError}</div>}
                        </>
                    )}
                </div>
            )}

            {closed && (
                <div className="alert alert-secondary">
                    This chat has been closed. Returning you shortly...
                </div>
            )}

            <div
                className="border rounded p-4 mb-0"
                style={{ height: "60vh", overflowY: "auto" }}
            >
                {messages.map((message) => (
                    <ChatMessageBubble key={message.id} message={message} />
                ))}
                <div ref={messagesEndRef} />
            </div>
            <form className="d-flex pt-3" onSubmit={handleSend}>
                <input
                    className="form-control me-2"
                    type="text"
                    value={body}
                    onChange={handleChange}
                    placeholder={waiting ? "Waiting for an agent to join..." : "Type a message..."}
                    disabled={closed || waiting}
                />
                <button className="btn btn-primary" type="submit" disabled={closed || waiting}>
                    Send
                </button>
            </form>
        </div>
    );
}

export default ChatRoomPage;
