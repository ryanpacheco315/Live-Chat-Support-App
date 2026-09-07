function ChatRow({ chat }) {
    return (
        <tr>
            <td>{chat.client.username}</td>
            <td>{chat.agent ? chat.agent.username : "—"}</td>
            <td>{chat.status}</td>
            <td>{chat.problem.category}</td>
            <td>{chat.problem.description}</td>
        </tr>
    );
}

export default ChatRow;
