package learn.data.mappers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import learn.models.ChatEmbedding;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class ChatEmbeddingMapper implements RowMapper<ChatEmbedding> {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public ChatEmbedding mapRow(ResultSet rs, int rowNum) throws SQLException {
        List<Double> embedding;
        try {
            embedding = MAPPER.readValue(rs.getString("embedding"), new TypeReference<List<Double>>() {
            });
        } catch (Exception ex) {
            throw new SQLException("Could not parse stored embedding JSON.", ex);
        }

        return new ChatEmbedding(
                rs.getInt("id"),
                rs.getInt("chat_id"),
                rs.getString("content"),
                embedding,
                rs.getTimestamp("created_at").toLocalDateTime()
        );
    }
}
