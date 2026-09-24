import { Link } from "react-router-dom";

function PastChatCard({ chat }) {
    const isSolved = chat.status === "CLOSED_SOLVED";

    return (
        <div className="card mb-3">
            <div className="card-body">
                <div className="d-flex align-items-center justify-content-between mb-2">
                    <h5 className="card-title mb-0">{chat.problem.category}</h5>
                    <span className={`badge text-bg-${isSolved ? "success" : "secondary"}`}>
                        {isSolved ? "Solved" : "Unsolved"}
                    </span>
                </div>
                <p className="card-text">{chat.problem.description}</p>
                <p className="card-text small text-muted">
                    Client: {chat.client.fullName} &middot; Agent:{" "}
                    {chat.agent ? chat.agent.fullName : "—"}
                </p>
                <Link to={`/past-chats/${chat.id}`} className="btn btn-primary btn-sm">
                    <i className="bi bi-file-text me-1" aria-hidden="true" />
                    View Transcript
                </Link>
            </div>
        </div>
    );
}

export default PastChatCard;
