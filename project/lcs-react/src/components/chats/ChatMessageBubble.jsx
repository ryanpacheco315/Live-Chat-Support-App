function ChatMessageBubble({ message }) {
    if (!message.sender) {
        return <p className="text-center text-muted fst-italic">{message.body}</p>;
    }

    const isClient = message.sender.role === "CLIENT";

    return (
        <div className={`d-flex mb-2 ${isClient ? "justify-content-end" : "justify-content-start"}`}>
            <div
                className={`p-2 rounded ${isClient ? "bg-primary text-white" : "bg-light"}`}
                style={{ maxWidth: "70%" }}
            >
                <div className="small fw-bold">{message.sender.fullName}</div>
                <div>{message.body}</div>
            </div>
        </div>
    );
}

export default ChatMessageBubble;
