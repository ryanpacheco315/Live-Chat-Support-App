package learn.rag;

import learn.data.ChatEmbeddingRepository;
import learn.data.ChatRepository;
import learn.data.DataAccessException;
import learn.data.MessageRepository;
import learn.data.TestDataHelper;
import learn.domain.Result;
import learn.domain.ResultType;
import learn.models.Chat;
import learn.models.ChatEmbedding;
import learn.models.ChatStatus;
import learn.models.Message;
import learn.models.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class ChatEmbeddingServiceTest {

    @Autowired
    ChatEmbeddingService service;

    @MockBean
    ChatEmbeddingRepository chatEmbeddingRepository;

    @MockBean
    MessageRepository messageRepository;

    @MockBean
    ChatRepository chatRepository;

    @MockBean
    EmbeddingClient embeddingClient;

    @MockBean
    SimilaritySearchService similaritySearchService;

    @MockBean
    LlmClient llmClient;

    @Test
    void shouldEmbedChatUsingProblemAndLastMessage() throws DataAccessException {
        Chat chat = TestDataHelper.existingActiveChat();
        Message clientMessage = new Message(1, chat.getId(), chat.getClient(), "My laptop will not turn on.",
                LocalDateTime.of(2026, 1, 1, 9, 1));
        Message resolutionMessage = new Message(2, chat.getId(), chat.getAgent(), "Try holding the power button.",
                LocalDateTime.of(2026, 1, 1, 9, 5));
        when(messageRepository.findByChatId(chat.getId())).thenReturn(List.of(clientMessage, resolutionMessage));
        when(embeddingClient.embed(anyString())).thenReturn(List.of(0.1, 0.2, 0.3));

        service.embedChat(chat);

        verify(embeddingClient).embed(
                "HARDWARE / LAPTOP: Laptop will not turn on. Resolution: Try holding the power button.");
        verify(chatEmbeddingRepository).create(argThat((ChatEmbedding embedding) ->
                embedding.getChatId() == chat.getId() && embedding.getEmbedding().equals(List.of(0.1, 0.2, 0.3))));
    }

    @Test
    void shouldOmitResolutionWhenOnlySystemMessagesExist() throws DataAccessException {
        Chat chat = TestDataHelper.existingActiveChat();
        when(messageRepository.findByChatId(chat.getId()))
                .thenReturn(List.of(new Message(1, chat.getId(), null, "An agent has joined the chat.",
                        LocalDateTime.now())));
        when(embeddingClient.embed(any())).thenReturn(List.of(0.1, 0.2, 0.3));

        service.embedChat(chat);

        verify(embeddingClient).embed("HARDWARE / LAPTOP: Laptop will not turn on.");
    }

    @Test
    void shouldSearchAndReturnMatchingChats() throws DataAccessException {
        List<Double> queryVector = List.of(1.0, 0.0, 0.0);
        ChatEmbedding match = new ChatEmbedding(1, 1, "HARDWARE: wifi issue", queryVector, LocalDateTime.now());
        when(embeddingClient.embed("wifi problem")).thenReturn(queryVector);
        when(chatEmbeddingRepository.findAll()).thenReturn(List.of(match));
        when(similaritySearchService.findMostSimilar(eq(queryVector), anyList(), eq(5)))
                .thenReturn(List.of(match));
        when(chatRepository.findById(1)).thenReturn(TestDataHelper.existingActiveChat());

        Result<List<Chat>> actual = service.search("wifi problem");

        assertTrue(actual.isSuccess());
        assertEquals(List.of(TestDataHelper.existingActiveChat()), actual.getPayload());
    }

    @Test
    void shouldFailWhenQueryBlank() throws DataAccessException {
        Result<List<Chat>> actual = service.search("   ");

        assertEquals(ResultType.INVALID, actual.getType());
        assertTrue(actual.getErrorMessages().contains("Search text is required."));
        verify(embeddingClient, never()).embed(anyString());
    }

    @Test
    void shouldFailGracefullyWhenEmbeddingClientThrows() throws DataAccessException {
        when(embeddingClient.embed(anyString())).thenThrow(new RuntimeException("embeddings API unavailable"));

        Result<List<Chat>> actual = service.search("wifi problem");

        assertEquals(ResultType.INVALID, actual.getType());
        assertTrue(actual.getErrorMessages().contains("Search is temporarily unavailable."));
    }

    @Test
    void shouldFindSimilarForOwnWaitingChat() throws DataAccessException {
        Chat waitingChat = TestDataHelper.existingWaitingChat();
        when(chatRepository.findById(2)).thenReturn(waitingChat);
        when(embeddingClient.embed("SOFTWARE / EMAIL: Cannot log in to email.")).thenReturn(List.of(1.0, 0.0));
        ChatEmbedding match = new ChatEmbedding(1, 1, "similar past issue", List.of(1.0, 0.0), LocalDateTime.now());
        when(chatEmbeddingRepository.findAll()).thenReturn(List.of(match));
        when(similaritySearchService.findMostSimilar(eq(List.of(1.0, 0.0)), anyList(), eq(5)))
                .thenReturn(List.of(match));

        Result<List<ChatEmbedding>> actual = service.findSimilarForChat(2, waitingChat.getClient());

        assertTrue(actual.isSuccess());
        assertEquals(List.of(match), actual.getPayload());
        verify(messageRepository).create(argThat(message ->
                message.getChatId() == 2 && message.getSender() == null
                        && message.getBody().equals("Client checked for a self-serve solution.")));
    }

    @Test
    void shouldRejectSimilarWhenNotTheClient() throws DataAccessException {
        Chat waitingChat = TestDataHelper.existingWaitingChat();
        when(chatRepository.findById(2)).thenReturn(waitingChat);

        Result<List<ChatEmbedding>> actual = service.findSimilarForChat(2, TestDataHelper.existingAgent());

        assertEquals(ResultType.INVALID, actual.getType());
        assertTrue(actual.getErrorMessages().contains("You are not a participant in this chat."));
        verify(embeddingClient, never()).embed(anyString());
    }

    @Test
    void shouldRejectSimilarWhenChatNotWaiting() throws DataAccessException {
        Chat activeChat = TestDataHelper.existingActiveChat();
        when(chatRepository.findById(1)).thenReturn(activeChat);

        Result<List<ChatEmbedding>> actual = service.findSimilarForChat(1, activeChat.getClient());

        assertEquals(ResultType.INVALID, actual.getType());
        assertTrue(actual.getErrorMessages().contains("Chat 1 is not waiting for an agent."));
        verify(embeddingClient, never()).embed(anyString());
    }

    @Test
    void shouldRejectSimilarWhenChatNotFound() throws DataAccessException {
        when(chatRepository.findById(999)).thenReturn(null);

        Result<List<ChatEmbedding>> actual = service.findSimilarForChat(999, TestDataHelper.existingClient());

        assertEquals(ResultType.NOT_FOUND, actual.getType());
        assertTrue(actual.getErrorMessages().contains("Chat 999 was not found."));
    }

    @Test
    void shouldFindSimilarForActiveChatAsAssignedAgent() throws DataAccessException {
        Chat activeChat = TestDataHelper.existingActiveChat();
        when(chatRepository.findById(1)).thenReturn(activeChat);
        when(embeddingClient.embed("HARDWARE / LAPTOP: Laptop will not turn on.")).thenReturn(List.of(1.0, 0.0));
        ChatEmbedding match = new ChatEmbedding(1, 1, "similar past issue", List.of(1.0, 0.0), LocalDateTime.now());
        when(chatEmbeddingRepository.findAll()).thenReturn(List.of(match));
        when(similaritySearchService.findMostSimilar(eq(List.of(1.0, 0.0)), anyList(), eq(5)))
                .thenReturn(List.of(match));

        Result<List<ChatEmbedding>> actual = service.findSimilarForChat(1, activeChat.getAgent());

        assertTrue(actual.isSuccess());
        assertEquals(List.of(match), actual.getPayload());
        verify(messageRepository, never()).create(any());
    }

    @Test
    void shouldRejectSimilarWhenAgentNotAssigned() throws DataAccessException {
        Chat activeChat = TestDataHelper.existingActiveChat();
        when(chatRepository.findById(1)).thenReturn(activeChat);

        Result<List<ChatEmbedding>> actual = service.findSimilarForChat(1, TestDataHelper.existingAdmin());

        assertEquals(ResultType.INVALID, actual.getType());
        assertTrue(actual.getErrorMessages().contains("You are not a participant in this chat."));
        verify(embeddingClient, never()).embed(anyString());
    }

    @Test
    void shouldRejectSimilarForAgentWhenChatNotActive() throws DataAccessException {
        User agent = TestDataHelper.existingAgent();
        Chat closedChat = new Chat(1, TestDataHelper.existingClient(), agent, ChatStatus.CLOSED_SOLVED,
                TestDataHelper.existingProblem1(), TestDataHelper.existingTimeRecord1());
        when(chatRepository.findById(1)).thenReturn(closedChat);

        Result<List<ChatEmbedding>> actual = service.findSimilarForChat(1, agent);

        assertEquals(ResultType.INVALID, actual.getType());
        assertTrue(actual.getErrorMessages().contains("Chat 1 is not active."));
        verify(embeddingClient, never()).embed(anyString());
    }

    @Test
    void shouldSuggestReplyForActiveChat() throws DataAccessException {
        Chat activeChat = TestDataHelper.existingActiveChat();
        when(chatRepository.findById(1)).thenReturn(activeChat);
        when(embeddingClient.embed("HARDWARE / LAPTOP: Laptop will not turn on.")).thenReturn(List.of(1.0, 0.0));
        ChatEmbedding match = new ChatEmbedding(1, 1, "HARDWARE / LAPTOP: same issue. Resolution: reseat the battery.",
                List.of(1.0, 0.0), LocalDateTime.now());
        when(chatEmbeddingRepository.findAll()).thenReturn(List.of(match));
        when(similaritySearchService.findMostSimilar(eq(List.of(1.0, 0.0)), anyList(), eq(5)))
                .thenReturn(List.of(match));
        when(llmClient.generate(anyString())).thenReturn("Try reseating the battery.");

        Result<String> actual = service.suggestReply(1, activeChat.getAgent());

        assertTrue(actual.isSuccess());
        assertEquals("Try reseating the battery.", actual.getPayload());
    }

    @Test
    void shouldReturnNoSuggestionWhenNoMatches() throws DataAccessException {
        Chat activeChat = TestDataHelper.existingActiveChat();
        when(chatRepository.findById(1)).thenReturn(activeChat);
        when(embeddingClient.embed(anyString())).thenReturn(List.of(1.0, 0.0));
        when(chatEmbeddingRepository.findAll()).thenReturn(List.of());
        when(similaritySearchService.findMostSimilar(any(), anyList(), eq(5))).thenReturn(List.of());

        Result<String> actual = service.suggestReply(1, activeChat.getAgent());

        assertTrue(actual.isSuccess());
        assertEquals(null, actual.getPayload());
        verify(llmClient, never()).generate(anyString());
    }

    @Test
    void shouldPropagatePermissionErrorFromSuggestReply() throws DataAccessException {
        Chat activeChat = TestDataHelper.existingActiveChat();
        when(chatRepository.findById(1)).thenReturn(activeChat);

        Result<String> actual = service.suggestReply(1, TestDataHelper.existingAdmin());

        assertEquals(ResultType.INVALID, actual.getType());
        assertTrue(actual.getErrorMessages().contains("You are not a participant in this chat."));
        verify(llmClient, never()).generate(anyString());
    }

    @Test
    void shouldFailGracefullyWhenLlmThrows() throws DataAccessException {
        Chat activeChat = TestDataHelper.existingActiveChat();
        when(chatRepository.findById(1)).thenReturn(activeChat);
        when(embeddingClient.embed(anyString())).thenReturn(List.of(1.0, 0.0));
        ChatEmbedding match = new ChatEmbedding(1, 1, "similar past issue", List.of(1.0, 0.0), LocalDateTime.now());
        when(chatEmbeddingRepository.findAll()).thenReturn(List.of(match));
        when(similaritySearchService.findMostSimilar(any(), anyList(), eq(5))).thenReturn(List.of(match));
        when(llmClient.generate(anyString())).thenThrow(new RuntimeException("LLM API unavailable"));

        Result<String> actual = service.suggestReply(1, activeChat.getAgent());

        assertEquals(ResultType.INVALID, actual.getType());
        assertTrue(actual.getErrorMessages().contains("Suggestion is temporarily unavailable."));
    }
}
