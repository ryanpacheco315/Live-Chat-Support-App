import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import WaitingChatRow from "./WaitingChatRow";
import { getWaitingChats } from "../../api/chats";
import { createStompClient } from "../../api/stomp";

function AgentQueuePage() {
    const navigate = useNavigate();
    const [chats, setChats] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        let isCancelled = false;

        async function loadInitialChats() {
            const result = await getWaitingChats();
            if (isCancelled) return;
            if (result.ok) {
                setChats(result.payload);
            }
            setLoading(false);
        }

        loadInitialChats();

        const stompClient = createStompClient();

        stompClient.onConnect = () => {
            stompClient.subscribe("/topic/queue", (frame) => {
                const update = JSON.parse(frame.body);

                if (update.type === "ADDED") {
                    setChats((current) =>
                        current.some((chat) => chat.id === update.chat.id)
                            ? current
                            : [...current, update.chat]
                    );
                } else if (update.type === "CLAIMED" || update.type === "REMOVED") {
                    setChats((current) => current.filter((chat) => chat.id !== update.chatId));
                }
            });
        };

        stompClient.activate();

        return () => {
            isCancelled = true;
            stompClient.deactivate();
        };
    }, []);

    function handleClaimed(chatId) {
        navigate(`/chat/${chatId}`);
    }

    return (
        <>
            <h4>Live Chats</h4>

            {loading && (
                <div className="text-center py-5">
                    <div className="spinner-border text-primary" role="status">
                        <span className="visually-hidden">Loading...</span>
                    </div>
                </div>
            )}

            {!loading && chats.length === 0 && (
                <p className="text-muted">
                    <i className="bi bi-cup-hot me-2" aria-hidden="true" />
                    No one&apos;s waiting right now.
                </p>
            )}

            {!loading && chats.length > 0 && (
                <table className="table table-striped">
                    <thead>
                        <tr>
                            <th>Client</th>
                            <th>Category</th>
                            <th>Description</th>
                            <th>Claim</th>
                        </tr>
                    </thead>
                    <tbody>
                        {chats.map((chat) => (
                            <WaitingChatRow key={chat.id} chat={chat} onClaimed={handleClaimed} />
                        ))}
                    </tbody>
                </table>
            )}
        </>
    );
}

export default AgentQueuePage;
