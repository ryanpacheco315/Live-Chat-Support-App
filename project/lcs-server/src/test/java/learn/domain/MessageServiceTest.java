package learn.domain;

import learn.data.ChatRepository;
import learn.data.DataAccessException;
import learn.data.MessageRepository;
import learn.data.TestDataHelper;
import learn.models.Message;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class MessageServiceTest {

    @Autowired
    MessageService service;

    @MockBean
    MessageRepository messageRepository;

    @MockBean
    ChatRepository chatRepository;

    @Test
    void shouldFindByChatId() throws DataAccessException {
        when(messageRepository.findByChatId(1)).thenReturn(
                List.of(TestDataHelper.existingClientMessage(), TestDataHelper.existingAgentMessage()));

        List<Message> actual = service.findByChatId(1);

        assertEquals(List.of(TestDataHelper.existingClientMessage(), TestDataHelper.existingAgentMessage()), actual);
    }

    @Test
    void sendFailsWhenChatNotFound() throws DataAccessException {
        when(chatRepository.findById(999)).thenReturn(null);

        Result<Message> actual = service.sendMessage(999, TestDataHelper.existingClient(), "hello");

        assertEquals(ResultType.NOT_FOUND, actual.getType());
        verify(messageRepository, never()).create(any());
    }

    @Test
    void sendFailsWhenChatNotActive() throws DataAccessException {
        when(chatRepository.findById(2)).thenReturn(TestDataHelper.existingWaitingChat());

        Result<Message> actual = service.sendMessage(2, TestDataHelper.existingClient(), "hello");

        assertEquals(ResultType.INVALID, actual.getType());
        assertTrue(actual.getErrorMessages().contains("Chat 2 is not active."));
    }

    @Test
    void sendFailsWhenSenderNotParticipant() throws DataAccessException {
        when(chatRepository.findById(1)).thenReturn(TestDataHelper.existingActiveChat());

        Result<Message> actual = service.sendMessage(1, TestDataHelper.existingAdmin(), "hello");

        assertEquals(ResultType.INVALID, actual.getType());
        assertTrue(actual.getErrorMessages().contains("You are not a participant in this chat."));
    }

    @Test
    void sendFailsWhenBodyBlank() throws DataAccessException {
        when(chatRepository.findById(1)).thenReturn(TestDataHelper.existingActiveChat());

        Result<Message> actual = service.sendMessage(1, TestDataHelper.existingClient(), "  ");

        assertEquals(ResultType.INVALID, actual.getType());
        assertTrue(actual.getErrorMessages().contains("Message `body` is required."));
    }

    @Test
    void sendHappyPathAllowsClient() throws DataAccessException {
        when(chatRepository.findById(1)).thenReturn(TestDataHelper.existingActiveChat());
        when(messageRepository.create(any(Message.class))).thenReturn(TestDataHelper.messageAfterCreate());

        Result<Message> actual = service.sendMessage(1, TestDataHelper.existingClient(), "hello");

        assertTrue(actual.isSuccess());
    }

    @Test
    void sendHappyPathAllowsAgent() throws DataAccessException {
        when(chatRepository.findById(1)).thenReturn(TestDataHelper.existingActiveChat());
        when(messageRepository.create(any(Message.class))).thenReturn(TestDataHelper.messageAfterCreate());

        Result<Message> actual = service.sendMessage(1, TestDataHelper.existingAgent(), "hello");

        assertTrue(actual.isSuccess());
    }
}
