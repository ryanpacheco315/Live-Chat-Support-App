package learn.models;

import java.time.LocalDateTime;
import java.util.Objects;

public class TimeRecord {
    private int id;
    private LocalDateTime createdAt;
    private LocalDateTime closedAt;

    public TimeRecord() {
    }

    public TimeRecord(LocalDateTime createdAt, LocalDateTime closedAt) {
        this(0, createdAt, closedAt);
    }

    public TimeRecord(int id, LocalDateTime createdAt, LocalDateTime closedAt) {
        this.id = id;
        this.createdAt = createdAt;
        this.closedAt = closedAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getClosedAt() {
        return closedAt;
    }

    public void setClosedAt(LocalDateTime closedAt) {
        this.closedAt = closedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        TimeRecord that = (TimeRecord) o;
        return id == that.id
                && Objects.equals(createdAt, that.createdAt)
                && Objects.equals(closedAt, that.closedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, createdAt, closedAt);
    }
}
