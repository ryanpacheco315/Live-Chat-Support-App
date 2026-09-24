import { useEffect, useState } from "react";
import ChatRow from "./ChatRow";
import { getAllChats, searchChats } from "../../api/chats";

function AllChatsPage() {
    const [chats, setChats] = useState([]);
    const [username, setUsername] = useState("");
    const [searchQuery, setSearchQuery] = useState("");
    const [searchError, setSearchError] = useState(null);
    const [searching, setSearching] = useState(false);

    async function loadChats(filterUsername) {
        const result = await getAllChats(filterUsername);
        if (result.ok) {
            setChats(result.payload);
        }
    }

    async function handleSearch(event) {
        event.preventDefault();
        if (!searchQuery.trim()) return;

        setSearching(true);
        setSearchError(null);
        const result = await searchChats(searchQuery);
        setSearching(false);

        if (result.ok) {
            setChats(result.payload);
        } else {
            setSearchError(result.payload?.[0] ?? "Search is currently unavailable.");
        }
    }

    function handleSearchClear() {
        setSearchQuery("");
        setSearchError(null);
        loadChats();
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

            <form className="d-flex mb-2" onSubmit={handleSearch}>
                <input
                    className="form-control me-2"
                    type="text"
                    placeholder="Search past chats by meaning (e.g. wifi connection problem)"
                    value={searchQuery}
                    onChange={(event) => setSearchQuery(event.target.value)}
                />
                <button className="btn btn-primary me-2" type="submit" disabled={searching}>
                    {searching ? "Searching..." : "Search"}
                </button>
                <button className="btn btn-secondary" type="button" onClick={handleSearchClear}>
                    Clear
                </button>
            </form>
            {searchError && <div className="alert alert-danger">{searchError}</div>}

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
