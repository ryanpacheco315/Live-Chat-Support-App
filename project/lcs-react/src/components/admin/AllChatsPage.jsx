import { useEffect, useState } from "react";
import ChatRow from "./ChatRow";
import { getAllChats } from "../../api/chats";

function AllChatsPage() {
    const [chats, setChats] = useState([]);
    const [username, setUsername] = useState("");

    async function loadChats(filterUsername) {
        const result = await getAllChats(filterUsername);
        if (result.ok) {
            setChats(result.payload);
        }
    }

    useEffect(() => {
        let isCancelled = false;

        async function loadInitialChats() {
            const result = await getAllChats();
            if (!isCancelled && result.ok) {
                setChats(result.payload);
            }
        }

        loadInitialChats();

        return () => {
            isCancelled = true;
        };
    }, []);

    function handleUsernameChange(event) {
        setUsername(event.target.value);
    }

    function handleFilter(event) {
        event.preventDefault();
        loadChats(username);
    }

    function handleClear() {
        setUsername("");
        loadChats();
    }

    return (
        <>
            <h4>All Chats</h4>
            <form className="d-flex mb-3" onSubmit={handleFilter}>
                <input
                    className="form-control me-2"
                    type="text"
                    placeholder="Filter by username"
                    value={username}
                    onChange={handleUsernameChange}
                />
                <button className="btn btn-primary me-2" type="submit">
                    Filter
                </button>
                <button className="btn btn-secondary" type="button" onClick={handleClear}>
                    Clear
                </button>
            </form>

            <table className="table table-striped">
                <thead>
                    <tr>
                        <th>Client</th>
                        <th>Agent</th>
                        <th>Status</th>
                        <th>Category</th>
                        <th>Description</th>
                    </tr>
                </thead>
                <tbody>
                    {chats.map((chat) => (
                        <ChatRow key={chat.id} chat={chat} />
                    ))}
                </tbody>
            </table>
        </>
    );
}

export default AllChatsPage;
