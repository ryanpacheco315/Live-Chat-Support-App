package learn.rag;

import learn.data.ChatEmbeddingRepository;
import learn.data.DataAccessException;
import learn.data.MessageRepository;
import learn.data.TestDataHelper;
import learn.models.Chat;
import learn.models.ChatEmbedding;
import learn.models.Message;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
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
    EmbeddingClient embeddingClient;

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
}
