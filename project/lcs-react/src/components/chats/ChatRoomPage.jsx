import { useEffect, useRef, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { createStompClient } from "../../api/stomp";
import { closeChat, getChatMessages } from "../../api/chats";
import ChatMessageBubble from "./ChatMessageBubble";

function ChatRoomPage({ user }) {
    const { id } = useParams();
    const chatId = Number(id);
    const navigate = useNavigate();

    const [messages, setMessages] = useState([]);
    const [accessDenied, setAccessDenied] = useState(false);
    const [closed, setClosed] = useState(false);
    const [closeError, setCloseError] = useState(null);
    const [body, setBody] = useState("");
    const clientRef = useRef(null);

    function addMessage(message) {
        setMessages((current) =>
            current.some((existing) => existing.id === message.id) ? current : [...current, message]
        );
    }

    useEffect(() => {
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
                // Two different shapes ride this same topic: a chat message has a
                // `body`, a close notice is a ChatResponse and has a `status` --
                // that's the signal to lock the other side out and route them away.
                if (payload.status) {
                    setClosed(true);
                } else {
                    addMessage(payload);
                }
            });
        };

        stompClient.activate();

        return () => {
            isCancelled = true;
            stompClient.deactivate();
        };
    }, [chatId]);

    useEffect(() => {
        if (!closed) return;

        const destination = user?.role === "AGENT" ? "/queue" : "/";
        const timeoutId = setTimeout(() => navigate(destination), 1500);
        return () => clearTimeout(timeoutId);
    }, [closed, user, navigate]);

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

    if (accessDenied) {
        return <p>You don&apos;t have access to this chat.</p>;
    }

    return (
        <div>
            <div className="d-flex justify-content-between align-items-center mb-2">
                <h4>Live Chat</h4>
                <div>
                    <button className="btn btn-danger" onClick={handleClose} disabled={closed}>
                        Close Chat
                    </button>
                    {closeError && <div className="text-danger">{closeError}</div>}
                </div>
            </div>

            {closed && (
                <div className="alert alert-secondary">
                    This chat has been closed. Returning you shortly...
                </div>
            )}

            <div className="border rounded p-3 mb-3" style={{ minHeight: "300px" }}>
                {messages.map((message) => (
                    <ChatMessageBubble key={message.id} message={message} />
                ))}
            </div>
            <form className="d-flex" onSubmit={handleSend}>
                <input
                    className="form-control me-2"
                    type="text"
                    value={body}
                    onChange={handleChange}
                    placeholder="Type a message..."
                    disabled={closed}
                />
                <button className="btn btn-primary" type="submit" disabled={closed}>
                    Send
                </button>
            </form>
        </div>
    );
}

export default ChatRoomPage;
