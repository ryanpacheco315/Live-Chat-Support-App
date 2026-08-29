import { api } from "./client";

export function getCurrentUser() {
    return api.get("/auth/me");
}

export function login(username, password) {
    return api.post("/auth/login", { username, password });
}

export function signup(user) {
    return api.post("/auth/signup", user);
}

export function logout() {
    return api.post("/auth/logout");
}
