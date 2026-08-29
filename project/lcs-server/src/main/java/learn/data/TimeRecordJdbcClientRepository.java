package learn.data;

import learn.data.mappers.TimeRecordMapper;
import learn.models.TimeRecord;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class TimeRecordJdbcClientRepository implements TimeRecordRepository {
    private final JdbcClient jdbcClient;

    public TimeRecordJdbcClientRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    private static final String BASE_SELECT = "select id, created_at, closed_at from time_record";

    @Override
    public TimeRecord findById(int id) throws DataAccessException {
        return jdbcClient.sql(BASE_SELECT + " where id = ?")
                .param(id)
                .query(new TimeRecordMapper())
                .optional().orElse(null);
    }

    @Override
    public TimeRecord create(TimeRecord timeRecord) throws DataAccessException {
        final String sql = """
                insert into time_record (created_at, closed_at)
                values (:created_at, :closed_at);
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        int rowsAffected = jdbcClient.sql(sql)
                .param("created_at", timeRecord.getCreatedAt())
                .param("closed_at", timeRecord.getClosedAt())
                .update(keyHolder, "id");

        if (rowsAffected == 0) {
            return null;
        }

        timeRecord.setId(keyHolder.getKey().intValue());
        return timeRecord;
    }

    @Override
    public boolean update(TimeRecord timeRecord) throws DataAccessException {
        final String sql = """
                update time_record set
                created_at = :created_at,
                closed_at = :closed_at
                where id = :id;
                """;

        return jdbcClient.sql(sql)
                .param("created_at", timeRecord.getCreatedAt())
                .param("closed_at", timeRecord.getClosedAt())
                .param("id", timeRecord.getId())
                .update() > 0;
    }
}
