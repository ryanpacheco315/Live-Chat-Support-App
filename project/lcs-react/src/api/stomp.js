import { Client } from "@stomp/stompjs";

const WS_URL = "ws://localhost:8080/ws";

export function createStompClient() {
    return new Client({
        brokerURL: WS_URL,
        reconnectDelay: 5000,
    });
}
