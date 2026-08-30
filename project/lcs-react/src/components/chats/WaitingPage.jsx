import { Navigate, useLocation } from "react-router-dom";

function WaitingPage() {
    const location = useLocation();
    const chat = location.state?.chat;

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
