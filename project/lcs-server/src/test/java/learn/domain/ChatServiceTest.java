package learn.domain;

import learn.data.ChatRepository;
import learn.data.DataAccessException;
import learn.data.ProblemRepository;
import learn.data.TestDataHelper;
import learn.data.TimeRecordRepository;
import learn.models.Chat;
import learn.models.ChatStatus;
import learn.models.Problem;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

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
    }
}
