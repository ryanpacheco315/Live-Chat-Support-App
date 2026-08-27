package learn.models;

import java.time.LocalDateTime;
import java.util.Objects;

public class Message {
    private int id;
    private int chatId;
    private User sender;
    private String body;
    private LocalDateTime createdAt;

    public Message() {
    }

    public Message(int chatId, User sender, String body, LocalDateTime createdAt) {
        this(0, chatId, sender, body, createdAt);
    }

    public Message(int id, int chatId, User sender, String body, LocalDateTime createdAt) {
        this.id = id;
        this.chatId = chatId;
        this.sender = sender;
        this.body = body;
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getChatId() {
        return chatId;
    }

    public void setChatId(int chatId) {
        this.chatId = chatId;
    }

    public User getSender() {
        return sender;
    }

    public void setSender(User sender) {
        this.sender = sender;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Message message = (Message) o;
        return id == message.id
                && chatId == message.chatId
                && Objects.equals(sender, message.sender)
                && Objects.equals(body, message.body)
                && Objects.equals(createdAt, message.createdAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, chatId, sender, body, createdAt);
    }
}
