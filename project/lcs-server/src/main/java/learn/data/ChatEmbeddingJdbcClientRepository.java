package learn.data;

import com.fasterxml.jackson.databind.ObjectMapper;
import learn.data.mappers.ChatEmbeddingMapper;
import learn.models.ChatEmbedding;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ChatEmbeddingJdbcClientRepository implements ChatEmbeddingRepository {
    private final JdbcClient jdbcClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ChatEmbeddingJdbcClientRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    private static final String BASE_SELECT = "select id, chat_id, content, embedding, created_at from chat_embedding";

    @Override
    public ChatEmbedding findByChatId(int chatId) throws DataAccessException {
        return jdbcClient.sql(BASE_SELECT + " where chat_id = ?")
                .param(chatId)
                .query(new ChatEmbeddingMapper())
                .optional().orElse(null);
    }

    @Override
    public List<ChatEmbedding> findAll() throws DataAccessException {
        return jdbcClient.sql(BASE_SELECT).query(new ChatEmbeddingMapper()).list();
    }

    @Override
    public ChatEmbedding create(ChatEmbedding chatEmbedding) throws DataAccessException {
        final String sql = """
                insert into chat_embedding (chat_id, content, embedding, created_at)
                values (:chat_id, :content, :embedding, :created_at);
                """;

        String embeddingJson;
        try {
            embeddingJson = objectMapper.writeValueAsString(chatEmbedding.getEmbedding());
        } catch (Exception ex) {
            throw new DataAccessException("Could not serialize embedding to JSON.", ex);
        }

        KeyHolder keyHolder = new GeneratedKeyHolder();

        int rowsAffected = jdbcClient.sql(sql)
                .param("chat_id", chatEmbedding.getChatId())
                .param("content", chatEmbedding.getContent())
                .param("embedding", embeddingJson)
                .param("created_at", chatEmbedding.getCreatedAt())
                .update(keyHolder, "id");

        if (rowsAffected == 0) {
            return null;
        }

        chatEmbedding.setId(keyHolder.getKey().intValue());
        return chatEmbedding;
    }
}
