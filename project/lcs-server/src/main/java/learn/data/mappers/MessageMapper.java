package learn.data.mappers;

import learn.models.Message;
import learn.models.Role;
import learn.models.User;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class MessageMapper implements RowMapper<Message> {

    @Override
    public Message mapRow(ResultSet rs, int rowNum) throws SQLException {
        User sender = null;
        int senderId = rs.getInt("sender_id");
        if (!rs.wasNull()) {
            sender = new User(
                    senderId,
                    rs.getString("sender_full_name"),
                    rs.getString("sender_username"),
                    rs.getString("sender_password"),
                    Role.valueOf(rs.getString("sender_role"))
            );
        }

        return new Message(
                rs.getInt("id"),
                rs.getInt("chat_id"),
                sender,
                rs.getString("body"),
                rs.getTimestamp("created_at").toLocalDateTime()
        );
    }
}
