package learn.dtos;

import learn.models.ChatEmbedding;

public class SimilarChatResponse {
    private int chatId;
    private String summary;

    public static SimilarChatResponse fromChatEmbedding(ChatEmbedding chatEmbedding) {
        SimilarChatResponse response = new SimilarChatResponse();
        response.chatId = chatEmbedding.getChatId();
        response.summary = chatEmbedding.getContent();
        return response;
    }

    public int getChatId() {
        return chatId;
    }

    public String getSummary() {
        return summary;
    }
}
