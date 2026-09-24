import { useEffect, useState } from "react";
import PastChatCard from "./PastChatCard";
import { getMyChatHistory } from "../../api/chats";

function PastChatsPage() {
    const [chats, setChats] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        let isCancelled = false;

        async function loadHistory() {
            const result = await getMyChatHistory();
            if (isCancelled) return;
            if (result.ok) {
                setChats(result.payload);
            }
            setLoading(false);
        }
        loadHistory();

        return () => {
            isCancelled = true;
        };
    }, []);

    return (
        <div>
            <h4>Past Chats</h4>
            {loading && (
                <div className="text-center py-5">
                    <div className="spinner-border text-primary" role="status">
                        <span className="visually-hidden">Loading...</span>
                    </div>
                </div>
            )}
            {!loading && chats.length === 0 && (
                <p className="text-muted">
                    <i className="bi bi-inbox me-2" aria-hidden="true" />
                    You have no past chats yet.
                </p>
            )}
            {chats.map((chat) => (
                <PastChatCard key={chat.id} chat={chat} />
            ))}
        </div>
    );
}

export default PastChatsPage;
