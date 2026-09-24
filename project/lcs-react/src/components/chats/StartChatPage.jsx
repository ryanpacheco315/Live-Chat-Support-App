import { useRef, useState } from "react";
import { useNavigate } from "react-router-dom";
import { startChat } from "../../api/chats";
import ChatMessageBubble from "./ChatMessageBubble";

const CATEGORIES = ["HARDWARE", "SOFTWARE", "OTHER"];

function StartChatPage({ user }) {
    const navigate = useNavigate();
    const nextId = useRef(2);
    const clientSender = { fullName: user?.fullName ?? "You", role: "CLIENT" };

    const [step, setStep] = useState("category");
    const [problem, setProblem] = useState({ category: "", subcategory: "", description: "" });
    const [input, setInput] = useState("");
    const [error, setError] = useState(null);
    const [log, setLog] = useState([
        { id: "bot-0", sender: null, body: "Hi! I just need a few quick details before connecting you with an agent." },
        { id: "bot-1", sender: null, body: "What type of issue are you having?" },
    ]);

    function appendLog(entries) {
        setLog((current) => [
            ...current,
            ...entries.map((entry) => {
                nextId.current += 1;
                return { id: `local-${nextId.current}`, ...entry };
            }),
        ]);
    }

    function pickCategory(category) {
        setProblem((current) => ({ ...current, category }));
        appendLog([
            { sender: clientSender, body: category },
            { sender: null, body: "Got it. Is there a more specific subcategory? Type one, or just hit send to skip." },
        ]);
        setStep("subcategory");
    }

    async function handleSend(event) {
        event.preventDefault();
        const text = input.trim();

        if (step === "subcategory") {
            setProblem((current) => ({ ...current, subcategory: text }));
            appendLog([
                { sender: clientSender, body: text || "(skipped)" },
                { sender: null, body: "Last thing — describe the problem in a bit more detail." },
            ]);
            setInput("");
            setStep("description");
            return;
        }

        if (step === "description") {
            if (!text) return;
            appendLog([{ sender: clientSender, body: text }]);
            setInput("");
            setStep("submitting");
            setError(null);

            const result = await startChat({ ...problem, description: text });
            if (result.ok) {
                navigate(`/chat/${result.payload.id}`);
            } else {
                setError(result.payload?.[0] ?? "Something went wrong. Please try again.");
                appendLog([{ sender: null, body: "Something went wrong on our end — want to try describing it again?" }]);
                setStep("description");
            }
        }
    }

    return (
        <div className="p-3">
            <h4 className="mb-3">Start a Chat</h4>

            <div className="border rounded p-4 mb-0" style={{ height: "60vh", overflowY: "auto" }}>
                {log.map((message) => (
                    <ChatMessageBubble key={message.id} message={message} />
                ))}
            </div>

            {step === "category" && (
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

            {(step === "subcategory" || step === "description") && (
                <form className="d-flex pt-3" onSubmit={handleSend}>
                    <input
                        className="form-control me-2"
                        type="text"
                        value={input}
                        onChange={(event) => setInput(event.target.value)}
                        placeholder={step === "subcategory" ? "Subcategory (optional)..." : "Describe the problem..."}
                    />
                    <button className="btn btn-primary" type="submit">
                        Send
                    </button>
                </form>
            )}

            {step === "submitting" && <p className="text-muted pt-3">Connecting you to the queue...</p>}
            {error && <div className="text-danger pt-2">{error}</div>}
        </div>
    );
}

export default StartChatPage;
