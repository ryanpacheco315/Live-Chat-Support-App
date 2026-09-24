import { useEffect, useState } from "react";
import ChatRow from "./ChatRow";
import { backfillEmbeddings, getAllChats, searchChats } from "../../api/chats";

function AllChatsPage() {
    const [chats, setChats] = useState([]);
    const [username, setUsername] = useState("");
    const [searchQuery, setSearchQuery] = useState("");
    const [searchError, setSearchError] = useState(null);
    const [searching, setSearching] = useState(false);
    const [backfillResult, setBackfillResult] = useState(null);
    const [backfillError, setBackfillError] = useState(null);
    const [backfilling, setBackfilling] = useState(false);

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

    async function handleBackfill() {
        setBackfilling(true);
        setBackfillError(null);
        setBackfillResult(null);
        const result = await backfillEmbeddings();
        setBackfilling(false);

        if (result.ok) {
            setBackfillResult(result.payload);
        } else {
            setBackfillError(result.payload?.[0] ?? "Backfill is currently unavailable.");
        }
    }

    return (
        <>
            <div className="d-flex justify-content-between align-items-center mb-3">
                <h4 className="mb-0">All Chats</h4>
                <button
                    className="btn btn-outline-primary btn-sm"
                    type="button"
                    onClick={handleBackfill}
                    disabled={backfilling}
                    title="Generate embeddings for any solved chat that doesn't have one yet (new data, or older chats closed before this feature existed)"
                >
                    {backfilling ? (
                        <>
                            <span className="spinner-border spinner-border-sm me-1" aria-hidden="true" />
                            Backfilling...
                        </>
                    ) : (
                        <>
                            <i className="bi bi-arrow-repeat me-1" aria-hidden="true" />
                            Backfill Embeddings
                        </>
                    )}
                </button>
            </div>

            {backfillResult && (
                <div className="alert alert-success">
                    <i className="bi bi-check-circle me-1" aria-hidden="true" />
                    Embedded {backfillResult.embedded} chat{backfillResult.embedded === 1 ? "" : "s"}.
                    {backfillResult.failed > 0 && ` ${backfillResult.failed} failed — try again in a moment.`}
                </div>
            )}
            {backfillError && <div className="alert alert-danger">{backfillError}</div>}
            <form className="d-flex mb-3" onSubmit={handleFilter}>
                <input
                    className="form-control me-2"
                    type="text"
                    placeholder="Filter by username"
                    value={username}
                    onChange={handleUsernameChange}
                />
                <button className="btn btn-primary me-2" type="submit">
                    <i className="bi bi-funnel me-1" aria-hidden="true" />
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
                    {searching ? (
                        <>
                            <span className="spinner-border spinner-border-sm me-1" aria-hidden="true" />
                            Searching...
                        </>
                    ) : (
                        <>
                            <i className="bi bi-search me-1" aria-hidden="true" />
                            Search
                        </>
                    )}
                </button>
                <button className="btn btn-secondary" type="button" onClick={handleSearchClear}>
                    Clear
                </button>
            </form>
            {searchError && <div className="alert alert-danger">{searchError}</div>}

            {chats.length === 0 ? (
                <p className="text-muted">
                    <i className="bi bi-inbox me-2" aria-hidden="true" />
                    No chats found.
                </p>
            ) : (
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
            )}
        </>
    );
}

export default AllChatsPage;
