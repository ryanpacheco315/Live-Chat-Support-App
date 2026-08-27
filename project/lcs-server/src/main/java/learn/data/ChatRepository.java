package learn.data;

import learn.models.Chat;
import learn.models.ChatStatus;

import java.util.List;

public interface ChatRepository {
    Chat findById(int id) throws DataAccessException;

    List<Chat> findWaiting() throws DataAccessException;

    Chat create(Chat chat) throws DataAccessException;

    /**
     * Conditional claim: only succeeds if the chat is still WAITING and unclaimed.
     * Returns false (rather than throwing) when another agent already won the race.
     */
    boolean claim(int chatId, int agentId) throws DataAccessException;

    boolean close(int chatId, ChatStatus finalStatus) throws DataAccessException;
}
