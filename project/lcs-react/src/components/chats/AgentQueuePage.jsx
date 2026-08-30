import { useEffect, useState } from "react";
import WaitingChatRow from "./WaitingChatRow";
import { getWaitingChats } from "../../api/chats";

function AgentQueuePage() {
    const [chats, setChats] = useState([]);

    async function loadWaitingChats() {
        const result = await getWaitingChats();
        if (result.ok) {
            setChats(result.payload);
        }
    }

    useEffect(() => {
        let isCancelled = false;

        async function loadInitialChats() {
            const result = await getWaitingChats();
            if (!isCancelled && result.ok) {
                setChats(result.payload);
            }
        }

        loadInitialChats();

        return () => {
            isCancelled = true;
        };
    }, []);

    function handleClaimed(chatId) {
        setChats(chats.filter((chat) => chat.id !== chatId));
    }

    return (
        <>
            <div className="d-flex justify-content-between align-items-center">
                <h4>Live Chats</h4>
                {/* Websockets will list the waiting chats.
                    For now, refreshing the page will update the list.*/}
                <button className="btn btn-secondary mb-2" onClick={loadWaitingChats}>
                    Refresh
                </button>
            </div>

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
        </>
    );
}

export default AgentQueuePage;
