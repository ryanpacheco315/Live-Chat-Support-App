package learn.data;

import learn.data.mappers.MessageMapper;
import learn.models.Message;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class MessageJdbcClientRepository implements MessageRepository {
    private final JdbcClient jdbcClient;

    public MessageJdbcClientRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    private static final String BASE_SELECT = """
            select
                m.id, m.chat_id, m.body, m.created_at,
                s.id as sender_id, s.full_name as sender_full_name,
                s.username as sender_username, s.password as sender_password,
                s.role as sender_role
            from message m
            left join user s on m.sender_id = s.id
            """;

    @Override
    public List<Message> findByChatId(int chatId) throws DataAccessException {
        return jdbcClient.sql(BASE_SELECT + " where m.chat_id = ? order by m.created_at, m.id")
                .param(chatId)
                .query(new MessageMapper())
                .list();
    }

    @Override
    public Message create(Message message) throws DataAccessException {
        final String sql = """
                insert into message (chat_id, sender_id, body, created_at)
                values (:chat_id, :sender_id, :body, :created_at);
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        int rowsAffected = jdbcClient.sql(sql)
                .param("chat_id", message.getChatId())
                .param("sender_id", message.getSender() != null ? message.getSender().getId() : null)
                .param("body", message.getBody())
                .param("created_at", message.getCreatedAt())
                .update(keyHolder, "id");

        if (rowsAffected == 0) {
            return null;
        }

        message.setId(keyHolder.getKey().intValue());
        return message;
    }
}
