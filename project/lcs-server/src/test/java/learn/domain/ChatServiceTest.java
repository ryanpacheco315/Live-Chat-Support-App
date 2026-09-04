package learn.domain;

import learn.data.ChatRepository;
import learn.data.DataAccessException;
import learn.data.MessageRepository;
import learn.data.ProblemRepository;
import learn.data.TestDataHelper;
import learn.data.TimeRecordRepository;
import learn.models.Chat;
import learn.models.ChatStatus;
import learn.models.Message;
import learn.models.Problem;
import learn.models.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class ChatServiceTest {

    @Autowired
    ChatService service;

    @MockBean
    ChatRepository chatRepository;

    @MockBean
    ProblemRepository problemRepository;

    @MockBean
    TimeRecordRepository timeRecordRepository;

    @MockBean
    MessageRepository messageRepository;

    @MockBean
    SimpMessagingTemplate messagingTemplate;

    @Test
    void failsWhenCategoryMissing() throws DataAccessException {
        Problem toCreate = TestDataHelper.problemToCreate();
        toCreate.setCategory(null);

        Result<Chat> actual = service.startChat(TestDataHelper.existingClient(), toCreate);

        assertEquals(ResultType.INVALID, actual.getType());
        assertTrue(actual.getErrorMessages().contains("Problem `category` is required."));
        verify(problemRepository, never()).create(any());
    }

    @Test
    void failsWhenDescriptionBlank() throws DataAccessException {
        Problem toCreate = TestDataHelper.problemToCreate();
        toCreate.setDescription("");

        Result<Chat> actual = service.startChat(TestDataHelper.existingClient(), toCreate);

        assertEquals(ResultType.INVALID, actual.getType());
        assertTrue(actual.getErrorMessages().contains("Problem `description` is required."));
    }

    @Test
    void failsWhenIdAlreadySet() throws DataAccessException {
        Problem toCreate = TestDataHelper.problemToCreate();
        toCreate.setId(5);

        Result<Chat> actual = service.startChat(TestDataHelper.existingClient(), toCreate);

        assertEquals(ResultType.INVALID, actual.getType());
        assertTrue(actual.getErrorMessages().contains("Problem `id` should not be set."));
    }

    @Test
    void happyPath() throws DataAccessException {
        Problem toCreate = TestDataHelper.problemToCreate();
        when(problemRepository.create(any(Problem.class))).thenReturn(TestDataHelper.problemAfterCreate());
        when(timeRecordRepository.create(any())).thenReturn(TestDataHelper.timeRecordAfterCreate());

        Chat expectedChat = new Chat(4, TestDataHelper.existingClient(), null, ChatStatus.WAITING,
                TestDataHelper.problemAfterCreate(), TestDataHelper.timeRecordAfterCreate());

        when(chatRepository.create(any(Chat.class))).thenReturn(expectedChat);

        Result<Chat> actual = service.startChat(TestDataHelper.existingClient(), toCreate);

        assertTrue(actual.isSuccess());
        assertEquals(ChatStatus.WAITING, actual.getPayload().getStatus());
        assertNull(actual.getPayload().getAgent());
        assertEquals(TestDataHelper.existingClient(), actual.getPayload().getClient());
        verify(messagingTemplate).convertAndSend(eq("/topic/queue"), any(Object.class));
    }

    @Test
    void shouldFindById() throws DataAccessException {
        when(chatRepository.findById(1)).thenReturn(TestDataHelper.existingActiveChat());

        Chat actual = service.findById(1);

        assertEquals(TestDataHelper.existingActiveChat(), actual);
    }

    @Test
    void shouldFindWaiting() throws DataAccessException {
        when(chatRepository.findWaiting()).thenReturn(List.of(TestDataHelper.existingWaitingChat()));

        List<Chat> actual = service.findWaiting();

        assertEquals(List.of(TestDataHelper.existingWaitingChat()), actual);
    }

    @Test
    void claimHappyPath() throws DataAccessException {
        User agent = TestDataHelper.existingAgent();
        when(chatRepository.claim(2, agent.getId())).thenReturn(true);
        Chat claimedChat = new Chat(2, TestDataHelper.existingClient(), agent, ChatStatus.ACTIVE,
                TestDataHelper.existingProblem2(), TestDataHelper.existingTimeRecord2());
        when(chatRepository.findById(2)).thenReturn(claimedChat);
        when(messageRepository.create(any(Message.class))).thenReturn(TestDataHelper.systemMessageToCreate());

        Result<Chat> actual = service.claim(2, agent);

        assertTrue(actual.isSuccess());
        assertEquals(ChatStatus.ACTIVE, actual.getPayload().getStatus());
        assertEquals(agent, actual.getPayload().getAgent());
        verify(messageRepository).create(argThat(message ->
                message.getChatId() == 2 && message.getSender() == null));
        verify(messagingTemplate).convertAndSend(eq("/topic/queue"), any(Object.class));
        verify(messagingTemplate).convertAndSend(eq("/topic/chat/2"), any(Object.class));
    }

    @Test
    void claimFailsWhenAlreadyClaimed() throws DataAccessException {
        User agent = TestDataHelper.existingAgent();
        when(chatRepository.claim(1, agent.getId())).thenReturn(false);
        when(chatRepository.findById(1)).thenReturn(TestDataHelper.existingActiveChat());

        Result<Chat> actual = service.claim(1, agent);

        assertEquals(ResultType.CONFLICT, actual.getType());
        assertTrue(actual.getErrorMessages().contains("Chat 1 has already been claimed."));
    }

    @Test
    void claimFailsWhenNotFound() throws DataAccessException {
        User agent = TestDataHelper.existingAgent();
        when(chatRepository.claim(999, agent.getId())).thenReturn(false);
        when(chatRepository.findById(999)).thenReturn(null);

        Result<Chat> actual = service.claim(999, agent);

        assertEquals(ResultType.NOT_FOUND, actual.getType());
        assertTrue(actual.getErrorMessages().contains("Chat 999 was not found."));
    }

    @Test
    void closeByClientProducesUnsolved() throws DataAccessException {
        User client = TestDataHelper.existingClient();
        when(chatRepository.findById(1)).thenReturn(TestDataHelper.existingActiveChat());
        Chat closedChat = new Chat(1, client, TestDataHelper.existingAgent(), ChatStatus.CLOSED_UNSOLVED,
                TestDataHelper.existingProblem1(), TestDataHelper.existingTimeRecord1());
        when(chatRepository.close(1, ChatStatus.CLOSED_UNSOLVED)).thenReturn(true);
        when(messageRepository.create(any(Message.class))).thenReturn(TestDataHelper.systemMessageToCreate());

        when(chatRepository.findById(1))
                .thenReturn(TestDataHelper.existingActiveChat())
                .thenReturn(closedChat);

        Result<Chat> actual = service.close(1, client);

        assertTrue(actual.isSuccess());
        assertEquals(ChatStatus.CLOSED_UNSOLVED, actual.getPayload().getStatus());
        verify(chatRepository).close(1, ChatStatus.CLOSED_UNSOLVED);
        verify(timeRecordRepository).update(argThat(tr -> tr.getClosedAt() != null));
        verify(messageRepository).create(argThat(message ->
                message.getChatId() == 1 && message.getSender() == null));
        verify(messagingTemplate, times(2)).convertAndSend(eq("/topic/chat/1"), any(Object.class));
    }

    @Test
    void closeByAgentProducesSolved() throws DataAccessException {
        User agent = TestDataHelper.existingAgent();
        Chat closedChat = new Chat(1, TestDataHelper.existingClient(), agent, ChatStatus.CLOSED_SOLVED,
                TestDataHelper.existingProblem1(), TestDataHelper.existingTimeRecord1());
        when(chatRepository.close(1, ChatStatus.CLOSED_SOLVED)).thenReturn(true);
        when(messageRepository.create(any(Message.class))).thenReturn(TestDataHelper.systemMessageToCreate());
        when(chatRepository.findById(1))
                .thenReturn(TestDataHelper.existingActiveChat())
                .thenReturn(closedChat);

        Result<Chat> actual = service.close(1, agent);

        assertTrue(actual.isSuccess());
        assertEquals(ChatStatus.CLOSED_SOLVED, actual.getPayload().getStatus());
        verify(chatRepository).close(1, ChatStatus.CLOSED_SOLVED);
    }

    @Test
    void closeFailsWhenNotFound() throws DataAccessException {
        when(chatRepository.findById(999)).thenReturn(null);

        Result<Chat> actual = service.close(999, TestDataHelper.existingClient());

        assertEquals(ResultType.NOT_FOUND, actual.getType());
        assertTrue(actual.getErrorMessages().contains("Chat 999 was not found."));
        verify(chatRepository, never()).close(anyInt(), any());
    }

    @Test
    void closeFailsWhenNotActive() throws DataAccessException {
        when(chatRepository.findById(2)).thenReturn(TestDataHelper.existingWaitingChat());

        Result<Chat> actual = service.close(2, TestDataHelper.existingClient());

        assertEquals(ResultType.INVALID, actual.getType());
        assertTrue(actual.getErrorMessages().contains("Chat 2 is not active."));
        verify(chatRepository, never()).close(anyInt(), any());
    }

    @Test
    void closeFailsWhenNotParticipant() throws DataAccessException {
        when(chatRepository.findById(1)).thenReturn(TestDataHelper.existingActiveChat());

        Result<Chat> actual = service.close(1, TestDataHelper.existingAdmin());

        assertEquals(ResultType.INVALID, actual.getType());
        assertTrue(actual.getErrorMessages().contains("You are not a participant in this chat."));
        verify(chatRepository, never()).close(anyInt(), any());
    }
}
