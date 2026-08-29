import { Outlet } from "react-router-dom";
import Nav from "./Nav";

function Layout({ user, setUser }) {
    return (
        <div className="container">
            <header className="mb-3">
                <Nav user={user} setUser={setUser} />
            </header>
            <main>
                <Outlet />
            </main>
        </div>
    );
}

export default Layout;
