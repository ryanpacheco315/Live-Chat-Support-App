package learn.dtos;

import learn.models.Chat;
import learn.models.ChatStatus;
import learn.models.Problem;
import learn.models.TimeRecord;

public class ChatResponse {
    private int id;
    private UserResponse client;
    private UserResponse agent;
    private ChatStatus status;
    private Problem problem;
    private TimeRecord timeRecord;

    public static ChatResponse fromChat(Chat chat) {
        ChatResponse response = new ChatResponse();
        response.id = chat.getId();
        response.client = UserResponse.fromUser(chat.getClient());
        response.agent = chat.getAgent() != null ? UserResponse.fromUser(chat.getAgent()) : null;
        response.status = chat.getStatus();
        response.problem = chat.getProblem();
        response.timeRecord = chat.getTimeRecord();
        return response;
    }

    public int getId() {
        return id;
    }

    public UserResponse getClient() {
        return client;
    }

    public UserResponse getAgent() {
        return agent;
    }

    public ChatStatus getStatus() {
        return status;
    }

    public Problem getProblem() {
        return problem;
    }

    public TimeRecord getTimeRecord() {
        return timeRecord;
    }
}
