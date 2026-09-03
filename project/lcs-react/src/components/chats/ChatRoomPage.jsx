import { useEffect, useRef, useState } from "react";
import { useParams } from "react-router-dom";
import { createStompClient } from "../../api/stomp";
import { getChatMessages } from "../../api/chats";
import ChatMessageBubble from "./ChatMessageBubble";

function ChatRoomPage() {
    const { id } = useParams();
    const chatId = Number(id);

    const [messages, setMessages] = useState([]);
    const [accessDenied, setAccessDenied] = useState(false);
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
                addMessage(JSON.parse(frame.body));
            });
        };

        stompClient.activate();

        return () => {
            isCancelled = true;
            stompClient.deactivate();
        };
    }, [chatId]);

    function handleChange(event) {
        setBody(event.target.value);
    }

    function handleSend(event) {
        event.preventDefault();
        if (!body.trim() || !clientRef.current?.connected) {
            return;
        }

        clientRef.current.publish({
            destination: `/app/chat/${chatId}/send`,
            body: JSON.stringify({ body }),
        });
        setBody("");
    }

    if (accessDenied) {
        return <p>You do not have access to this chat.</p>;
    }

    return (
        <div>
            <h4>Live Chat</h4>
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
                />
                <button className="btn btn-primary" type="submit">
                    Send
                </button>
            </form>
        </div>
    );
}

export default ChatRoomPage;
