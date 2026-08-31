package learn.data;

import learn.models.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.simple.JdbcClient;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class MessageJdbcClientRepositoryTest {

    @Autowired
    private MessageJdbcClientRepository repository;

    @Autowired
    private JdbcClient jdbcClient;

    @BeforeEach
    void setup() {
        jdbcClient.sql("call set_known_good_state();").update();
    }

    @Test
    void shouldFindByChatId() throws DataAccessException {
        List<Message> actual = repository.findByChatId(1);

        assertEquals(List.of(TestDataHelper.existingClientMessage(), TestDataHelper.existingAgentMessage()), actual);
    }

    @Test
    void shouldFindNoneForChatWithNoMessages() throws DataAccessException {
        assertTrue(repository.findByChatId(2).isEmpty());
    }

    @Test
    void shouldCreate() throws DataAccessException {
        Message actual = repository.create(TestDataHelper.messageToCreate());

        assertEquals(TestDataHelper.messageAfterCreate(), actual);
        assertEquals(List.of(TestDataHelper.existingClientMessage(), TestDataHelper.existingAgentMessage(), actual),
                repository.findByChatId(1));
    }

    @Test
    void shouldCreateSystemMessageWithNullSender() throws DataAccessException {
        Message actual = repository.create(TestDataHelper.systemMessageToCreate());

        assertNull(actual.getSender());
        assertEquals(actual, repository.findByChatId(2).get(0));
    }
}
