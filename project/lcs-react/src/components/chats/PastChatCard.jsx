import { Link } from "react-router-dom";

function PastChatCard({ chat }) {
    const isSolved = chat.status === "CLOSED_SOLVED";

    return (
        <div className="card mb-3">
            <div className="card-body">
                <h5 className="card-title">{chat.problem.category}</h5>
                <h6 className="card-subtitle mb-2 text-muted">{isSolved ? "Solved" : "Unsolved"}</h6>
                <p className="card-text">{chat.problem.description}</p>
                <p className="card-text small text-muted">
                    Client: {chat.client.fullName} &middot; Agent:{" "}
                    {chat.agent ? chat.agent.fullName : "—"}
                </p>
                <Link to={`/past-chats/${chat.id}`} className="btn btn-primary">
                    View Transcript
                </Link>
            </div>
        </div>
    );
}

export default PastChatCard;
