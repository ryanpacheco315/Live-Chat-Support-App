import { Outlet, useLocation } from "react-router-dom";
import Nav from "./Nav";

function Layout({ user, setUser }) {
    const location = useLocation();
    const inChat = location.pathname.startsWith("/chat/");

    return (
        <div className="container">
            {!inChat && (
                <header className="mb-3">
                    <Nav user={user} setUser={setUser} />
                </header>
            )}
            <main>
                <Outlet />
            </main>
        </div>
    );
}

export default Layout;
