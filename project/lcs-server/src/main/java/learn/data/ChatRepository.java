package learn.data;

import learn.models.Chat;
import learn.models.ChatStatus;

import java.util.List;

public interface ChatRepository {
    Chat findById(int id) throws DataAccessException;

    List<Chat> findAll(String username) throws DataAccessException;

    List<Chat> findClosedByUsername(String username) throws DataAccessException;

    List<Chat> findWaiting() throws DataAccessException;

    Chat create(Chat chat) throws DataAccessException;

    boolean claim(int chatId, int agentId) throws DataAccessException;

    boolean close(int chatId, ChatStatus finalStatus) throws DataAccessException;
}
