import { useEffect, useState } from "react";
import PastChatCard from "./PastChatCard";
import { getMyChatHistory } from "../../api/chats";

function PastChatsPage() {
    const [chats, setChats] = useState([]);

    useEffect(() => {
        let isCancelled = false;

        async function loadHistory() {
            const result = await getMyChatHistory();
            if (!isCancelled && result.ok) {
                setChats(result.payload);
            }
        }
        loadHistory();

        return () => {
            isCancelled = true;
        };
    }, []);

    return (
        <div>
            <h4>Past Chats</h4>
            {chats.length === 0 && <p>You have no past chats yet.</p>}
            {chats.map((chat) => (
                <PastChatCard key={chat.id} chat={chat} />
            ))}
        </div>
    );
}

export default PastChatsPage;
