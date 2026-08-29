package learn.data.mappers;

import learn.models.*;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class ChatMapper implements RowMapper<Chat> {

    @Override
    public Chat mapRow(ResultSet rs, int rowNum) throws SQLException {
        User client = new User(
                rs.getInt("client_id"),
                rs.getString("client_full_name"),
                rs.getString("client_username"),
                rs.getString("client_password"),
                Role.valueOf(rs.getString("client_role"))
        );

        User agent = null;
        int agentId = rs.getInt("agent_id");
        if (!rs.wasNull()) {
            agent = new User(
                    agentId,
                    rs.getString("agent_full_name"),
                    rs.getString("agent_username"),
                    rs.getString("agent_password"),
                    Role.valueOf(rs.getString("agent_role"))
            );
        }

        Problem problem = null;
        int problemId = rs.getInt("problem_id");
        if (!rs.wasNull()) {
            problem = new Problem(
                    problemId,
                    ProblemCategory.valueOf(rs.getString("problem_category")),
                    rs.getString("problem_subcategory"),
                    rs.getString("problem_description")
            );
        }

        TimeRecord timeRecord = null;
        int timeId = rs.getInt("time_id");
        if (!rs.wasNull()) {
            Timestamp closedAt = rs.getTimestamp("time_closed_at");
            timeRecord = new TimeRecord(
                    timeId,
                    rs.getTimestamp("time_created_at").toLocalDateTime(),
                    closedAt != null ? closedAt.toLocalDateTime() : null
            );
        }

        return new Chat(
                rs.getInt("id"),
                client,
                agent,
                ChatStatus.valueOf(rs.getString("status")),
                problem,
                timeRecord
        );
    }
}
