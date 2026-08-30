import { api } from "./client";

export function startChat(problem) {
    return api.post("/chats", problem);
}

export function getWaitingChats() {
    return api.get("/chats/waiting");
}

export function claimChat(id) {
    return api.post(`/chats/${id}/claim`);
}
