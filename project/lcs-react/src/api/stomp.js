import { Client } from "@stomp/stompjs";

const WS_URL = `${window.location.protocol === "https:" ? "wss:" : "ws:"}//${window.location.host}/ws`;

export function createStompClient() {
    return new Client({
        brokerURL: WS_URL,
        reconnectDelay: 5000,
    });
}
