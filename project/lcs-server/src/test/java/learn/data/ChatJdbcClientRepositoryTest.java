package learn.data;

import learn.models.Chat;
import learn.models.ChatStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.simple.JdbcClient;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class ChatJdbcClientRepositoryTest {

    @Autowired
    private ChatJdbcClientRepository repository;

    @Autowired
    private JdbcClient jdbcClient;

    @BeforeEach
    void setup() {
        jdbcClient.sql("call set_known_good_state();").update();
    }

    @Test
    void shouldFindById() throws DataAccessException {
        Chat actual = repository.findById(1);

        assertEquals(TestDataHelper.existingActiveChat(), actual);
    }

    @Test
    void shouldNotFindByIdWhenMissing() throws DataAccessException {
        assertNull(repository.findById(999));
    }

    @Test
    void shouldFindWaiting() throws DataAccessException {
        List<Chat> actual = repository.findWaiting();

        assertEquals(List.of(TestDataHelper.existingWaitingChat()), actual);
    }

    @Test
    void shouldCreate() throws DataAccessException {
        Chat toCreate = new Chat(TestDataHelper.existingClient(), null, ChatStatus.WAITING,
                TestDataHelper.existingProblem1(), TestDataHelper.existingTimeRecord1());

        Chat actual = repository.create(toCreate);

        assertEquals(3, actual.getId());
        assertEquals(actual, repository.findById(3));
    }

    @Test
    void shouldClaim() throws DataAccessException {
        assertTrue(repository.claim(2, 2));

        Chat claimed = repository.findById(2);
        assertEquals(ChatStatus.ACTIVE, claimed.getStatus());
        assertEquals(2, claimed.getAgent().getId());
    }

    @Test
    void shouldNotClaimWhenAlreadyClaimed() throws DataAccessException {
        assertFalse(repository.claim(1, 2));
    }

    @Test
    void shouldClose() throws DataAccessException {
        assertTrue(repository.close(1, ChatStatus.CLOSED_SOLVED));

        assertEquals(ChatStatus.CLOSED_SOLVED, repository.findById(1).getStatus());
    }
}
