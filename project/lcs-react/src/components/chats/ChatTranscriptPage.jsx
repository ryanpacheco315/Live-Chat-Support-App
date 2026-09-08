import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import { getChat, getChatMessages } from "../../api/chats";
import ChatMessageBubble from "./ChatMessageBubble";

function ChatTranscriptPage() {
    const { id } = useParams();
    const chatId = Number(id);

    const [chat, setChat] = useState(null);
    const [messages, setMessages] = useState([]);
    const [accessDenied, setAccessDenied] = useState(false);

    useEffect(() => {
        let isCancelled = false;

        async function loadTranscript() {
            const [chatResult, messagesResult] = await Promise.all([
                getChat(chatId),
                getChatMessages(chatId),
            ]);

            if (isCancelled) return;

            if (chatResult.ok && messagesResult.ok) {
                setChat(chatResult.payload);
                setMessages(messagesResult.payload);
            } else {
                setAccessDenied(true);
            }
        }
        loadTranscript();

        return () => {
            isCancelled = true;
        };
    }, [chatId]);

    if (accessDenied) {
        return <p>You don&apos;t have access to this chat.</p>;
    }

    if (!chat) {
        return null;
    }

    return (
        <div>
            <h4>Chat Transcript</h4>
            <p className="text-muted">
                {chat.problem.category} &mdash; {chat.status === "CLOSED_SOLVED" ? "Solved" : "Unsolved"}
            </p>
            <div className="border rounded p-3">
                {messages.map((message) => (
                    <ChatMessageBubble key={message.id} message={message} />
                ))}
            </div>
        </div>
    );
}

export default ChatTranscriptPage;
