package learn.models;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

public class ChatEmbedding {
    private int id;
    private int chatId;
    private String content;
    private List<Double> embedding;
    private LocalDateTime createdAt;

    public ChatEmbedding() {
    }

    public ChatEmbedding(int chatId, String content, List<Double> embedding, LocalDateTime createdAt) {
        this(0, chatId, content, embedding, createdAt);
    }

    public ChatEmbedding(int id, int chatId, String content, List<Double> embedding, LocalDateTime createdAt) {
        this.id = id;
        this.chatId = chatId;
        this.content = content;
        this.embedding = embedding;
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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public List<Double> getEmbedding() {
        return embedding;
    }

    public void setEmbedding(List<Double> embedding) {
        this.embedding = embedding;
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
        ChatEmbedding that = (ChatEmbedding) o;
        return id == that.id
                && chatId == that.chatId
                && Objects.equals(content, that.content)
                && Objects.equals(embedding, that.embedding)
                && Objects.equals(createdAt, that.createdAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, chatId, content, embedding, createdAt);
    }
}
