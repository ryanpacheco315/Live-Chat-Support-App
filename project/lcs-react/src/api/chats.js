import { api } from "./client";

export function startChat(problem) {
    return api.post("/chats", problem);
}

export function getWaitingChats() {
    return api.get("/chats/waiting");
}

export function getAllChats(username) {
    const query = username ? `?username=${encodeURIComponent(username)}` : "";
    return api.get(`/chats${query}`);
}

export function searchChats(q) {
    return api.get(`/chats/search?q=${encodeURIComponent(q)}`);
}

export function getMyChatHistory() {
    return api.get("/chats/mine");
}

export function getChat(id) {
    return api.get(`/chats/${id}`);
}

export function getSimilarChats(id) {
    return api.get(`/chats/${id}/similar`);
}

export function resolveSelfServe(id) {
    return api.post(`/chats/${id}/resolve`);
}

export function claimChat(id) {
    return api.post(`/chats/${id}/claim`);
}

export function getChatMessages(id) {
    return api.get(`/chats/${id}/messages`);
}

export function closeChat(id) {
    return api.post(`/chats/${id}/close`);
}
