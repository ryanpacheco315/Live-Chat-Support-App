package learn.dtos;

public class QueueUpdate {
    private String type;
    private int chatId;
    private ChatResponse chat;

    public static QueueUpdate added(ChatResponse chat) {
        QueueUpdate update = new QueueUpdate();
        update.type = "ADDED";
        update.chatId = chat.getId();
        update.chat = chat;
        return update;
    }

    public static QueueUpdate claimed(int chatId) {
        QueueUpdate update = new QueueUpdate();
        update.type = "CLAIMED";
        update.chatId = chatId;
        return update;
    }

    public String getType() {
        return type;
    }

    public int getChatId() {
        return chatId;
    }

    public ChatResponse getChat() {
        return chat;
    }
}
