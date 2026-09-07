import { api } from "./client";

export function createAgent(user) {
    return api.post("/admin/agents", user);
}
