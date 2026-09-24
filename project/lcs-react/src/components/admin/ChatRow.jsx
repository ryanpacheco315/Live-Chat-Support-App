import { useNavigate } from "react-router-dom";

const STATUS_BADGE = {
    INTAKE: "secondary",
    WAITING: "warning",
    ACTIVE: "primary",
    CLOSED_SOLVED: "success",
    CLOSED_UNSOLVED: "secondary",
};

function ChatRow({ chat }) {
    const navigate = useNavigate();

    return (
        <tr
            onClick={() => navigate(`/admin/chats/${chat.id}`)}
            style={{ cursor: "pointer" }}
            title="View chat history"
        >
            <td>{chat.client.username}</td>
            <td>{chat.agent ? chat.agent.username : "—"}</td>
            <td>
                <span className={`badge text-bg-${STATUS_BADGE[chat.status] ?? "secondary"}`}>
                    {chat.status}
                </span>
            </td>
            <td>{chat.problem.category}</td>
            <td>{chat.problem.description}</td>
            <td className="text-end text-muted">
                <i className="bi bi-chevron-right" aria-hidden="true" />
            </td>
        </tr>
    );
}

export default ChatRow;
