import { createBrowserRouter, RouterProvider } from "react-router-dom";
import { useEffect, useState } from "react";
import Layout from "./Layout";
import Home from "./Home";
import LoginPage from "./auth/LoginPage";
import SignupPage from "./auth/SignupPage";
import StartChatPage from "./chats/StartChatPage";
import WaitingPage from "./chats/WaitingPage";
import AgentQueuePage from "./chats/AgentQueuePage";
import ChatRoomPage from "./chats/ChatRoomPage";
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
            element: <Layout user={user} setUser={setUser} />,
            children: [
                { path: "/", element: <Home /> },
                { path: "/login", element: <LoginPage setUser={setUser} /> },
                { path: "/signup", element: <SignupPage /> },
                { path: "/start-chat", element: <StartChatPage /> },
                { path: "/waiting", element: <WaitingPage /> },
                { path: "/queue", element: <AgentQueuePage /> },
                { path: "/chat/:id", element: <ChatRoomPage /> },
            ],
        },
    ];

    const router = createBrowserRouter(routes);

    return <RouterProvider router={router} />;
}

export default AppRouter;
