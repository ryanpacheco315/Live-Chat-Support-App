import { useEffect, useState } from "react";
import { useLocation, useNavigate, useParams } from "react-router-dom";
import { createStompClient } from "../../api/stomp";
import { getChat } from "../../api/chats";

function WaitingPage() {
    const { id } = useParams();
    const chatId = Number(id);
    const location = useLocation();
    const navigate = useNavigate();

    const [chat, setChat] = useState(location.state?.chat ?? null);
    const [notFound, setNotFound] = useState(false);

    useEffect(() => {
        if (chat) return;

        let isCancelled = false;

        async function loadChat() {
            const result = await getChat(chatId);
            if (isCancelled) return;
            if (result.ok) {
                setChat(result.payload);
            } else {
                setNotFound(true);
            }
        }
        loadChat();

        return () => {
            isCancelled = true;
        };
    }, [chat, chatId]);

    useEffect(() => {
        if (chat && chat.status !== "WAITING") {
            navigate(`/chat/${chat.id}`, { replace: true });
        }
    }, [chat, navigate]);

    useEffect(() => {
        if (!chat || chat.status !== "WAITING") return;

        const stompClient = createStompClient();

        stompClient.onConnect = () => {
            stompClient.subscribe(`/topic/chat/${chat.id}`, () => {
                navigate(`/chat/${chat.id}`);
            });
        };

        stompClient.activate();

        return () => {
            stompClient.deactivate();
        };
    }, [chat, navigate]);

    if (notFound) {
        return <p>This chat could not be found.</p>;
    }

    if (!chat || chat.status !== "WAITING") {
        return null;
    }

    return (
        <div>
            <h1>You are in the queue</h1>
            <p>An agent will be with you shortly.</p>
            <p>Category: {chat.problem.category}</p>
            {chat.problem.subcategory && <p>Subcategory: {chat.problem.subcategory}</p>}
            <p>Description: {chat.problem.description}</p>
        </div>
    );
}

export default WaitingPage;
