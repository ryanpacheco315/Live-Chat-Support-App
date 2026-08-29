package learn.data.mappers;

import learn.models.TimeRecord;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class TimeRecordMapper implements RowMapper<TimeRecord> {

    @Override
    public TimeRecord mapRow(ResultSet rs, int rowNum) throws SQLException {
        Timestamp closedAt = rs.getTimestamp("closed_at");
        return new TimeRecord(
                rs.getInt("id"),
                rs.getTimestamp("created_at").toLocalDateTime(),
                closedAt != null ? closedAt.toLocalDateTime() : null
        );
    }
}
