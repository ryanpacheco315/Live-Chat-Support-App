import { Link, NavLink, useNavigate } from "react-router-dom";
import { logout } from "../api/auth";

function navButtonClass(color) {
    return ({ isActive }) =>
        `btn btn-sm me-2 ${isActive ? `btn-outline-${color} nav-btn-active` : `btn-${color}`}`;
}

function Nav({ user, setUser }) {
    const navigate = useNavigate();

    async function handleLogOut() {
        await logout();
        setUser(null);
        navigate("/");
    }

    return (
        <nav className="navbar navbar-expand navbar-gray navbar-dark px-3 py-2">
            <div className="d-flex align-items-center w-100">
                <Link className="navbar-brand d-flex align-items-center" to="/">
                    <i className="bi bi-chat-dots-fill me-2" aria-hidden="true" />
                    Live Chat Support
                </Link>
                {user && <span className="navbar-text me-3">Welcome, {user.fullName}</span>}

                <div className="d-flex align-items-center ms-auto">
                    {!user && (
                        <>
                            <NavLink to="/login" className={navButtonClass("primary")}>
                                Log In
                            </NavLink>
                            <NavLink to="/signup" className={navButtonClass("secondary")}>
                                Sign Up
                            </NavLink>
                        </>
                    )}
                    {user && (user.role === "CLIENT" || user.role === "AGENT") && (
                        <NavLink to="/past-chats" className={navButtonClass("info")}>
                            <i className="bi bi-clock-history me-1" aria-hidden="true" />
                            Past Chats
                        </NavLink>
                    )}
                    {user && user.role === "CLIENT" && (
                        <NavLink to="/chat/new" className={navButtonClass("success")}>
                            <i className="bi bi-plus-circle me-1" aria-hidden="true" />
                            Start a Chat
                        </NavLink>
                    )}
                    {user && user.role === "AGENT" && (
                        <NavLink to="/queue" className={navButtonClass("warning")}>
                            <i className="bi bi-headset me-1" aria-hidden="true" />
                            Live Chats
                        </NavLink>
                    )}
                    {user && user.role === "ADMIN" && (
                        <>
                            <NavLink to="/admin/create-agent" className={navButtonClass("secondary")}>
                                <i className="bi bi-person-plus me-1" aria-hidden="true" />
                                Create Agent
                            </NavLink>
                            <NavLink to="/admin/chats" className={navButtonClass("dark")}>
                                <i className="bi bi-list-ul me-1" aria-hidden="true" />
                                All Chats
                            </NavLink>
                        </>
                    )}
                    {user && (
                        <button className="btn btn-sm btn-danger" onClick={handleLogOut}>
                            <i className="bi bi-box-arrow-right me-1" aria-hidden="true" />
                            Log Out
                        </button>
                    )}
                </div>
            </div>
        </nav>
    );
}

export default Nav;
