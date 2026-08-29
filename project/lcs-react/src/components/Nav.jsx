import { Link, NavLink } from "react-router-dom";

function Nav({ user }) {
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
                        <li className="nav-item nav-link">Welcome, {user.fullName}</li>
                    )}
                </ul>
            </div>
        </nav>
    );
}

export default Nav;
