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
        <nav className="navbar navbar-expand navbar-gray border-bottom px-3 py-2">
            <div className="d-flex align-items-center w-100">
                <Link className="navbar-brand" to="/">
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
                            Past Chats
                        </NavLink>
                    )}
                    {user && user.role === "CLIENT" && (
                        <NavLink to="/start-chat" className={navButtonClass("success")}>
                            Start a Chat
                        </NavLink>
                    )}
                    {user && user.role === "AGENT" && (
                        <NavLink to="/queue" className={navButtonClass("warning")}>
                            Live Chats
                        </NavLink>
                    )}
                    {user && user.role === "ADMIN" && (
                        <>
                            <NavLink to="/admin/create-agent" className={navButtonClass("secondary")}>
                                Create Agent
                            </NavLink>
                            <NavLink to="/admin/chats" className={navButtonClass("dark")}>
                                All Chats
                            </NavLink>
                        </>
                    )}
                    {user && (
                        <button className="btn btn-sm btn-danger" onClick={handleLogOut}>
                            Log Out
                        </button>
                    )}
                </div>
            </div>
        </nav>
    );
}

export default Nav;
