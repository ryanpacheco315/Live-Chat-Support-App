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
import learn.models.Message;
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
}
