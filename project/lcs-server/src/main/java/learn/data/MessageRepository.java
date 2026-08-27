package learn.data;

import learn.models.Message;

import java.util.List;

public interface MessageRepository {
    List<Message> findByChatId(int chatId) throws DataAccessException;

    Message create(Message message) throws DataAccessException;
}
