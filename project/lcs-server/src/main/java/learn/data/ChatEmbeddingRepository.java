package learn.data;

import learn.models.ChatEmbedding;

import java.util.List;

public interface ChatEmbeddingRepository {
    ChatEmbedding findByChatId(int chatId) throws DataAccessException;

    List<ChatEmbedding> findAll() throws DataAccessException;

    ChatEmbedding create(ChatEmbedding chatEmbedding) throws DataAccessException;
}
