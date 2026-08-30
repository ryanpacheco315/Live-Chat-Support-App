import { api } from "./client";

export function startChat(problem) {
    return api.post("/chats", problem);
}
