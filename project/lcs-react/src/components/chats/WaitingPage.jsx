import { useEffect } from "react";
import { Navigate, useLocation, useNavigate } from "react-router-dom";
import { createStompClient } from "../../api/stomp";

function WaitingPage() {
    const location = useLocation();
    const navigate = useNavigate();
    const chat = location.state?.chat;

    useEffect(() => {
        if (!chat) return;

        // A WAITING chat can't receive any message except the "agent joined" system
        // one (the backend rejects sends to a chat that isn't ACTIVE yet), so any
        // message arriving here means it's time to move into the live chat room.
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

    // No GET-by-id endpoint exists to re-fetch this on a refresh, so if the chat
    // wasn't handed off via navigation state, there's nothing to show here.
    if (!chat) {
        return <Navigate to="/" />;
    }

    return (
        <div>
            <h1>You&apos;re in the queue</h1>
            <p>An agent will be with you shortly.</p>
            <p>Category: {chat.problem.category}</p>
            {chat.problem.subcategory && <p>Subcategory: {chat.problem.subcategory}</p>}
            <p>Description: {chat.problem.description}</p>
        </div>
    );
}

export default WaitingPage;
