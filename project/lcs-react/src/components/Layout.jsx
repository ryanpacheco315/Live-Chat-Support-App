import { Outlet } from "react-router-dom";
import Nav from "./Nav";

function Layout({ user }) {
    return (
        <div className="container">
            <header className="mb-3">
                <Nav user={user} />
            </header>
            <main>
                <Outlet />
            </main>
        </div>
    );
}

export default Layout;
