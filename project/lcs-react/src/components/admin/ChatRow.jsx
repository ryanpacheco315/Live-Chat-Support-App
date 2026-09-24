const STATUS_BADGE = {
    INTAKE: "secondary",
    WAITING: "warning",
    ACTIVE: "primary",
    CLOSED_SOLVED: "success",
    CLOSED_UNSOLVED: "secondary",
};

function ChatRow({ chat }) {
    return (
        <tr>
            <td>{chat.client.username}</td>
            <td>{chat.agent ? chat.agent.username : "—"}</td>
            <td>
                <span className={`badge text-bg-${STATUS_BADGE[chat.status] ?? "secondary"}`}>
                    {chat.status}
                </span>
            </td>
            <td>{chat.problem.category}</td>
            <td>{chat.problem.description}</td>
        </tr>
    );
}

export default ChatRow;
