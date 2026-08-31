package learn.dtos;

import learn.models.Message;

import java.time.LocalDateTime;

public class MessageResponse {
    private int id;
    private int chatId;
    private UserResponse sender;
    private String body;
    private LocalDateTime createdAt;

    public static MessageResponse fromMessage(Message message) {
        MessageResponse response = new MessageResponse();
        response.id = message.getId();
        response.chatId = message.getChatId();
        response.sender = message.getSender() != null ? UserResponse.fromUser(message.getSender()) : null;
        response.body = message.getBody();
        response.createdAt = message.getCreatedAt();
        return response;
    }

    public int getId() {
        return id;
    }

    public int getChatId() {
        return chatId;
    }

    public UserResponse getSender() {
        return sender;
    }

    public String getBody() {
        return body;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
