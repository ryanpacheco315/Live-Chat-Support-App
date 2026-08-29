package learn.data;

import learn.data.mappers.ChatMapper;
import learn.models.Chat;
import learn.models.ChatStatus;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ChatJdbcClientRepository implements ChatRepository {
    private final JdbcClient jdbcClient;

    public ChatJdbcClientRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    private static final String BASE_SELECT = """
            select
                c.id, c.status,
                client.id as client_id, client.full_name as client_full_name,
                client.username as client_username, client.password as client_password,
                client.role as client_role,
                agent.id as agent_id, agent.full_name as agent_full_name,
                agent.username as agent_username, agent.password as agent_password,
                agent.role as agent_role,
                p.id as problem_id, p.category as problem_category,
                p.subcategory as problem_subcategory, p.description as problem_description,
                t.id as time_id, t.created_at as time_created_at, t.closed_at as time_closed_at
            from chat c
            join user client on c.client_id = client.id
            left join user agent on c.agent_id = agent.id
            left join problem p on c.problem_id = p.id
            left join time_record t on c.time_id = t.id
            """;

    @Override
    public Chat findById(int id) throws DataAccessException {
        return jdbcClient.sql(BASE_SELECT + " where c.id = ?")
                .param(id)
                .query(new ChatMapper())
                .optional().orElse(null);
    }

    @Override
    public List<Chat> findWaiting() throws DataAccessException {
        return jdbcClient.sql(BASE_SELECT + " where c.status = 'WAITING'")
                .query(new ChatMapper())
                .list();
    }

    @Override
    public Chat create(Chat chat) throws DataAccessException {
        final String sql = """
                insert into chat (client_id, agent_id, status, problem_id, time_id)
                values (:client_id, :agent_id, :status, :problem_id, :time_id);
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        int rowsAffected = jdbcClient.sql(sql)
                .param("client_id", chat.getClient().getId())
                .param("agent_id", chat.getAgent() != null ? chat.getAgent().getId() : null)
                .param("status", chat.getStatus().name())
                .param("problem_id", chat.getProblem() != null ? chat.getProblem().getId() : null)
                .param("time_id", chat.getTimeRecord() != null ? chat.getTimeRecord().getId() : null)
                .update(keyHolder, "id");

        if (rowsAffected == 0) {
            return null;
        }

        chat.setId(keyHolder.getKey().intValue());
        return chat;
    }

    @Override
    public boolean claim(int chatId, int agentId) throws DataAccessException {
        final String sql = """
                update chat set
                status = 'ACTIVE',
                agent_id = :agent_id
                where id = :id and status = 'WAITING';
                """;

        return jdbcClient.sql(sql)
                .param("agent_id", agentId)
                .param("id", chatId)
                .update() > 0;
    }

    @Override
    public boolean close(int chatId, ChatStatus finalStatus) throws DataAccessException {
        final String sql = "update chat set status = :status where id = :id;";

        return jdbcClient.sql(sql)
                .param("status", finalStatus.name())
                .param("id", chatId)
                .update() > 0;
    }
}
