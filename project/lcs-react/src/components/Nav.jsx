import { Link, NavLink, useNavigate } from "react-router-dom";
import { logout } from "../api/auth";

function Nav({ user, setUser }) {
    const navigate = useNavigate();

    async function handleLogOut() {
        await logout();
        setUser(null);
        navigate("/");
    }

    return (
        <nav className="navbar navbar-expand">
            <div className="d-flex">
                <Link className="navbar-brand" to="/">
                    Live Chat Support
                </Link>
                <ul className="navbar-nav">
                    {!user && (
                        <>
                            <li className="nav-item">
                                <NavLink className="nav-link" to="/login">
                                    Log In
                                </NavLink>
                            </li>
                            <li className="nav-item">
                                <NavLink className="nav-link" to="/signup">
                                    Sign Up
                                </NavLink>
                            </li>
                        </>
                    )}
                    {user && (
                        <>
                            <li className="nav-item nav-link">Welcome, {user.fullName}</li>
                            <li className="nav-item">
                                <button className="nav-link btn btn-link" onClick={handleLogOut}>
                                    Log Out
                                </button>
                            </li>
                        </>
                    )}
                </ul>
            </div>
        </nav>
    );
}

export default Nav;
