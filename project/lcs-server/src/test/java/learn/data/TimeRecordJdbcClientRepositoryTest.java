package learn.data;

import learn.models.TimeRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.simple.JdbcClient;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class TimeRecordJdbcClientRepositoryTest {

    @Autowired
    private TimeRecordJdbcClientRepository repository;

    @Autowired
    private JdbcClient jdbcClient;

    @BeforeEach
    void setup() {
        jdbcClient.sql("call set_known_good_state();").update();
    }

    @Test
    void shouldFindById() throws DataAccessException {
        TimeRecord actual = repository.findById(1);

        assertEquals(TestDataHelper.existingTimeRecord1(), actual);
    }

    @Test
    void shouldNotFindByIdWhenMissing() throws DataAccessException {
        assertNull(repository.findById(999));
    }

    @Test
    void shouldCreate() throws DataAccessException {
        TimeRecord actual = repository.create(TestDataHelper.timeRecordToCreate());

        assertEquals(TestDataHelper.timeRecordAfterCreate(), actual);
        assertEquals(actual, repository.findById(3));
    }

    @Test
    void shouldUpdate() throws DataAccessException {
        TimeRecord toUpdate = TestDataHelper.existingTimeRecord1();
        toUpdate.setClosedAt(LocalDateTime.of(2026, 1, 1, 12, 0));

        assertTrue(repository.update(toUpdate));
        assertEquals(toUpdate, repository.findById(1));
    }
}
