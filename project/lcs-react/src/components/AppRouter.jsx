import { createBrowserRouter, RouterProvider } from "react-router-dom";
import { useEffect, useState } from "react";
import Layout from "./Layout";
import Home from "./Home";
import LoginPage from "./auth/LoginPage";
import SignupPage from "./auth/SignupPage";
import { getCurrentUser } from "../api/auth";

function AppRouter() {
    const [user, setUser] = useState(null);

    useEffect(() => {
        let isCancelled = false;

        async function loadCurrentUser() {
            const result = await getCurrentUser();
            if (!isCancelled) {
                setUser(result.ok ? result.payload : null);
            }
        }

        loadCurrentUser();

        return () => {
            isCancelled = true;
        };
    }, []);

    const routes = [
        {
            path: "/",
            element: <Layout user={user} />,
            children: [
                { path: "/", element: <Home /> },
                { path: "/login", element: <LoginPage /> },
                { path: "/signup", element: <SignupPage /> },
            ],
        },
    ];

    const router = createBrowserRouter(routes);

    return <RouterProvider router={router} />;
}

export default AppRouter;
