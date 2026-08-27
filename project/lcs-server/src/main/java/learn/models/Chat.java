package learn.models;

import java.util.Objects;

public class Chat {
    private int id;
    private User client;
    private User agent;
    private ChatStatus status;
    private Problem problem;
    private TimeRecord timeRecord;

    public Chat() {
    }

    public Chat(User client, User agent, ChatStatus status, Problem problem, TimeRecord timeRecord) {
        this(0, client, agent, status, problem, timeRecord);
    }

    public Chat(int id, User client, User agent, ChatStatus status, Problem problem, TimeRecord timeRecord) {
        this.id = id;
        this.client = client;
        this.agent = agent;
        this.status = status;
        this.problem = problem;
        this.timeRecord = timeRecord;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public User getClient() {
        return client;
    }

    public void setClient(User client) {
        this.client = client;
    }

    public User getAgent() {
        return agent;
    }

    public void setAgent(User agent) {
        this.agent = agent;
    }

    public ChatStatus getStatus() {
        return status;
    }

    public void setStatus(ChatStatus status) {
        this.status = status;
    }

    public Problem getProblem() {
        return problem;
    }

    public void setProblem(Problem problem) {
        this.problem = problem;
    }

    public TimeRecord getTimeRecord() {
        return timeRecord;
    }

    public void setTimeRecord(TimeRecord timeRecord) {
        this.timeRecord = timeRecord;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Chat chat = (Chat) o;
        return id == chat.id
                && Objects.equals(client, chat.client)
                && Objects.equals(agent, chat.agent)
                && status == chat.status
                && Objects.equals(problem, chat.problem)
                && Objects.equals(timeRecord, chat.timeRecord);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, client, agent, status, problem, timeRecord);
    }
}
