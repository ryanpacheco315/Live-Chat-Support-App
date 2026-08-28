package learn.data;

import learn.models.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.simple.JdbcClient;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class UserJdbcClientRepositoryTest {

    @Autowired
    private UserJdbcClientRepository repository;

    @Autowired
    private JdbcClient jdbcClient;

    @BeforeEach
    void setup() {
        jdbcClient.sql("call set_known_good_state();").update();
    }

    @Test
    void shouldFindById() throws DataAccessException {
        User actual = repository.findById(1);

        assertEquals(TestDataHelper.existingClient(), actual);
    }

    @Test
    void shouldNotFindByIdWhenMissing() throws DataAccessException {
        assertNull(repository.findById(999));
    }

    @Test
    void shouldFindByUsername() throws DataAccessException {
        User actual = repository.findByUsername("bob");

        assertEquals(TestDataHelper.existingAgent(), actual);
    }

    @Test
    void shouldNotFindByUsernameWhenMissing() throws DataAccessException {
        assertNull(repository.findByUsername("does-not-exist"));
    }

    @Test
    void shouldCreate() throws DataAccessException {
        User actual = repository.create(TestDataHelper.userToCreate());

        assertEquals(TestDataHelper.userAfterCreate(), actual);
        assertEquals(actual, repository.findById(4));
    }
}