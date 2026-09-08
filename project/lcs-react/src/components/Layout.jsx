import { Outlet, useLocation } from "react-router-dom";
import Nav from "./Nav";

function Layout({ user, setUser }) {
    const location = useLocation();
    const inChat = location.pathname.startsWith("/chat/");

    return (
        <>
            {!inChat && <Nav user={user} setUser={setUser} />}
            <div className="container">
                <main className={inChat ? "" : "py-3"}>
                    <Outlet />
                </main>
            </div>
        </>
    );
}

export default Layout;
