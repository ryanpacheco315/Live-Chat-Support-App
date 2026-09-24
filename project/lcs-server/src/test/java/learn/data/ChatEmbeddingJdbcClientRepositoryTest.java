package learn.data;

import learn.models.ChatEmbedding;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.simple.JdbcClient;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class ChatEmbeddingJdbcClientRepositoryTest {

    @Autowired
    private ChatEmbeddingJdbcClientRepository repository;

    @Autowired
    private JdbcClient jdbcClient;

    @BeforeEach
    void setup() {
        jdbcClient.sql("call set_known_good_state();").update();
    }

    @Test
    void shouldFindByChatId() throws DataAccessException {
        ChatEmbedding actual = repository.findByChatId(1);

        assertEquals(TestDataHelper.existingChatEmbedding1(), actual);
    }

    @Test
    void shouldNotFindByChatIdWhenMissing() throws DataAccessException {
        assertNull(repository.findByChatId(999));
    }

    @Test
    void shouldFindAll() throws DataAccessException {
        List<ChatEmbedding> actual = repository.findAll();

        assertEquals(List.of(TestDataHelper.existingChatEmbedding1()), actual);
    }

    @Test
    void shouldCreate() throws DataAccessException {
        ChatEmbedding actual = repository.create(TestDataHelper.chatEmbeddingToCreate());

        assertEquals(TestDataHelper.chatEmbeddingAfterCreate(), actual);
        assertEquals(actual, repository.findByChatId(2));
        assertEquals(2, repository.findAll().size());
    }
}
