import { useState } from "react";
import { claimChat } from "../../api/chats";

function WaitingChatRow({ chat, onClaimed }) {
    const [error, setError] = useState(null);

    async function handleClaim() {
        const result = await claimChat(chat.id);

        if (result.ok) {
            onClaimed(chat.id);
        } else {
            setError(result.payload?.[0] ?? "Could not claim this chat.");
        }
    }

    return (
        <tr>
            <td>{chat.client.fullName}</td>
            <td>{chat.problem.category}</td>
            <td>{chat.problem.description}</td>
            <td>
                <button className="btn btn-primary" onClick={handleClaim}>
                    Claim
                </button>
                {error && <div className="text-danger">{error}</div>}
            </td>
        </tr>
    );
}

export default WaitingChatRow;
