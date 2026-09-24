package learn.rag;

import learn.data.ChatEmbeddingRepository;
import learn.data.ChatRepository;
import learn.data.DataAccessException;
import learn.data.MessageRepository;
import learn.domain.Result;
import learn.domain.ResultType;
import learn.models.Chat;
import learn.models.ChatEmbedding;
import learn.models.ChatStatus;
import learn.models.Message;
import learn.models.User;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ChatEmbeddingService {
    private static final int DEFAULT_SEARCH_LIMIT = 5;

    private final ChatEmbeddingRepository chatEmbeddingRepository;
    private final MessageRepository messageRepository;
    private final ChatRepository chatRepository;
    private final EmbeddingClient embeddingClient;
    private final SimilaritySearchService similaritySearchService;

    public ChatEmbeddingService(ChatEmbeddingRepository chatEmbeddingRepository,
                                 MessageRepository messageRepository,
                                 ChatRepository chatRepository,
                                 EmbeddingClient embeddingClient,
                                 SimilaritySearchService similaritySearchService) {
        this.chatEmbeddingRepository = chatEmbeddingRepository;
        this.messageRepository = messageRepository;
        this.chatRepository = chatRepository;
        this.embeddingClient = embeddingClient;
        this.similaritySearchService = similaritySearchService;
    }

    public void embedChat(Chat chat) throws DataAccessException {
        List<Message> transcript = messageRepository.findByChatId(chat.getId());
        String content = buildContent(chat, transcript);
        List<Double> vector = embeddingClient.embed(content);
        chatEmbeddingRepository.create(new ChatEmbedding(chat.getId(), content, vector, LocalDateTime.now()));
    }

    public Result<List<Chat>> search(String query) throws DataAccessException {
        return search(query, DEFAULT_SEARCH_LIMIT);
    }

    public Result<List<Chat>> search(String query, int limit) throws DataAccessException {
        Result<List<Chat>> result = new Result<>();

        if (query == null || query.isBlank()) {
            result.addErrorMessage("Search text is required.", ResultType.INVALID);
            return result;
        }

        List<Double> queryEmbedding;
        try {
            queryEmbedding = embeddingClient.embed(query);
        } catch (Exception ex) {
            result.addErrorMessage("Search is temporarily unavailable.", ResultType.INVALID);
            return result;
        }

        List<ChatEmbedding> topMatches = findTopMatches(queryEmbedding, limit);

        List<Chat> chats = new ArrayList<>();
        for (ChatEmbedding match : topMatches) {
            Chat chat = chatRepository.findById(match.getChatId());
            if (chat != null) {
                chats.add(chat);
            }
        }

        result.setPayload(chats);
        return result;
    }

    /**
     * Client-facing self-serve check: embeds the chat's own problem (no query text needed —
     * the requester's chat already IS the query) and returns similar already-resolved chats.
     * Scoped to the chat's own client, and only while it's still WAITING — this isn't a
     * general-purpose search endpoint, it's "does my ticket look like something already solved."
     */
    public Result<List<ChatEmbedding>> findSimilarForChat(int chatId, User requester) throws DataAccessException {
        Result<List<ChatEmbedding>> result = new Result<>();

        Chat chat = chatRepository.findById(chatId);
        if (chat == null) {
            result.addErrorMessage("Chat %s was not found.", ResultType.NOT_FOUND, chatId);
            return result;
        }

        if (chat.getClient().getId() != requester.getId()) {
            result.addErrorMessage("You are not a participant in this chat.", ResultType.INVALID);
            return result;
        }

        if (chat.getStatus() != ChatStatus.WAITING) {
            result.addErrorMessage("Chat %s is not waiting for an agent.", ResultType.INVALID, chatId);
            return result;
        }

        List<Double> queryEmbedding;
        try {
            queryEmbedding = embeddingClient.embed(buildProblemContent(chat));
        } catch (Exception ex) {
            result.addErrorMessage("Search is temporarily unavailable.", ResultType.INVALID);
            return result;
        }

        List<ChatEmbedding> topMatches = findTopMatches(queryEmbedding, DEFAULT_SEARCH_LIMIT);

        messageRepository.create(new Message(chatId, null,
                "Client checked for a self-serve solution.", LocalDateTime.now()));

        result.setPayload(topMatches);
        return result;
    }

    private List<ChatEmbedding> findTopMatches(List<Double> queryEmbedding, int limit) throws DataAccessException {
        List<ChatEmbedding> candidates = chatEmbeddingRepository.findAll();
        return similaritySearchService.findMostSimilar(queryEmbedding, candidates, limit);
    }

    private String buildProblemContent(Chat chat) {
        StringBuilder content = new StringBuilder();
        content.append(chat.getProblem().getCategory());
        if (chat.getProblem().getSubcategory() != null && !chat.getProblem().getSubcategory().isBlank()) {
            content.append(" / ").append(chat.getProblem().getSubcategory());
        }
        content.append(": ").append(chat.getProblem().getDescription());
        return content.toString();
    }

    private String buildContent(Chat chat, List<Message> transcript) {
        StringBuilder content = new StringBuilder(buildProblemContent(chat));

        String lastRealMessage = transcript.stream()
                .filter(message -> message.getSender() != null)
                .reduce((first, second) -> second)
                .map(Message::getBody)
                .orElse(null);

        if (lastRealMessage != null) {
            content.append(" Resolution: ").append(lastRealMessage);
        }

        return content.toString();
    }
}
