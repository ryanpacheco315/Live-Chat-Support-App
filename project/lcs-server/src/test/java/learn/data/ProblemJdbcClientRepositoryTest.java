package learn.data;

import learn.models.Problem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.simple.JdbcClient;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class ProblemJdbcClientRepositoryTest {

    @Autowired
    private ProblemJdbcClientRepository repository;

    @Autowired
    private JdbcClient jdbcClient;

    @BeforeEach
    void setup() {
        jdbcClient.sql("call set_known_good_state();").update();
    }

    @Test
    void shouldFindById() throws DataAccessException {
        Problem actual = repository.findById(1);

        assertEquals(TestDataHelper.existingProblem1(), actual);
    }

    @Test
    void shouldNotFindByIdWhenMissing() throws DataAccessException {
        assertNull(repository.findById(999));
    }

    @Test
    void shouldCreate() throws DataAccessException {
        Problem actual = repository.create(TestDataHelper.problemToCreate());

        assertEquals(TestDataHelper.problemAfterCreate(), actual);
        assertEquals(actual, repository.findById(3));
    }
}
