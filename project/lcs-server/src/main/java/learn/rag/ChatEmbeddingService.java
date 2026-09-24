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
    private final LlmClient llmClient;

    public ChatEmbeddingService(ChatEmbeddingRepository chatEmbeddingRepository,
                                 MessageRepository messageRepository,
                                 ChatRepository chatRepository,
                                 EmbeddingClient embeddingClient,
                                 SimilaritySearchService similaritySearchService,
                                 LlmClient llmClient) {
        this.chatEmbeddingRepository = chatEmbeddingRepository;
        this.messageRepository = messageRepository;
        this.chatRepository = chatRepository;
        this.embeddingClient = embeddingClient;
        this.similaritySearchService = similaritySearchService;
        this.llmClient = llmClient;
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
     * Embeds the chat's own problem (no query text needed — the requester's chat already IS
     * the query) and returns similar already-resolved chats. Two participants can use this,
     * each only during their own relevant phase of the chat: the client while still WAITING
     * (self-serve — logs a system message so an agent who later claims the chat can see it was
     * tried), and the assigned agent while the chat is ACTIVE (agent-assist — silent, since it's
     * refetched freely from a sidebar rather than being a one-shot client action).
     */
    public Result<List<ChatEmbedding>> findSimilarForChat(int chatId, User requester) throws DataAccessException {
        Result<List<ChatEmbedding>> result = new Result<>();

        Chat chat = chatRepository.findById(chatId);
        if (chat == null) {
            result.addErrorMessage("Chat %s was not found.", ResultType.NOT_FOUND, chatId);
            return result;
        }

        boolean isClient = chat.getClient().getId() == requester.getId();
        boolean isAssignedAgent = chat.getAgent() != null && chat.getAgent().getId() == requester.getId();

        if (!isClient && !isAssignedAgent) {
            result.addErrorMessage("You are not a participant in this chat.", ResultType.INVALID);
            return result;
        }

        if (isClient) {
            if (chat.getStatus() != ChatStatus.WAITING) {
                result.addErrorMessage("Chat %s is not waiting for an agent.", ResultType.INVALID, chatId);
                return result;
            }
        } else {
            if (chat.getStatus() != ChatStatus.ACTIVE) {
                result.addErrorMessage("Chat %s is not active.", ResultType.INVALID, chatId);
                return result;
            }
        }

        List<Double> queryEmbedding;
        try {
            queryEmbedding = embeddingClient.embed(buildProblemContent(chat));
        } catch (Exception ex) {
            result.addErrorMessage("Search is temporarily unavailable.", ResultType.INVALID);
            return result;
        }

        List<ChatEmbedding> topMatches = findTopMatches(queryEmbedding, DEFAULT_SEARCH_LIMIT);

        if (isClient) {
            messageRepository.create(new Message(chatId, null,
                    "Client checked for a self-serve solution.", LocalDateTime.now()));
        }

        result.setPayload(topMatches);
        return result;
    }

    /**
     * Agent-assist generation step: reuses findSimilarForChat's retrieval + permission checks,
     * then turns the matches into one suggested-reply summary instead of a raw list. Skips the
     * LLM call entirely (and returns a null suggestion) when there are no matches — no point
     * paying for a generation call with nothing to summarize.
     */
    public Result<String> suggestReply(int chatId, User agent) throws DataAccessException {
        Result<List<ChatEmbedding>> matches = findSimilarForChat(chatId, agent);
        if (!matches.isSuccess()) {
            return propagateError(matches);
        }

        Result<String> result = new Result<>();
        if (matches.getPayload().isEmpty()) {
            result.setPayload(null);
            return result;
        }

        Chat chat = chatRepository.findById(chatId);
        String prompt = buildSuggestionPrompt(chat, matches.getPayload());

        try {
            result.setPayload(llmClient.generate(prompt));
        } catch (Exception ex) {
            result.addErrorMessage("Suggestion is temporarily unavailable.", ResultType.INVALID);
        }

        return result;
    }

    private <T> Result<T> propagateError(Result<?> source) {
        Result<T> result = new Result<>();
        for (String message : source.getErrorMessages()) {
            result.addErrorMessage(message, source.getType());
        }
        return result;
    }

    private String buildSuggestionPrompt(Chat chat, List<ChatEmbedding> matches) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("A support agent is helping a client with this problem: ")
                .append(buildProblemContent(chat))
                .append(". Here are similar past tickets and how they were resolved:\n");

        int i = 1;
        for (ChatEmbedding match : matches) {
            prompt.append(i++).append(". ").append(match.getContent()).append("\n");
        }

        prompt.append("Write one short suggested reply the agent could send to the client, ")
                .append("summarizing the likely fix based on these past resolutions. ")
                .append("Keep it under 3 sentences, friendly, and actionable.");

        return prompt.toString();
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
