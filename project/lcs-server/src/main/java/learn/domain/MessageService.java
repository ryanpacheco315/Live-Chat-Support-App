package learn.domain;

import learn.data.ChatRepository;
import learn.data.DataAccessException;
import learn.data.MessageRepository;
import learn.models.Chat;
import learn.models.ChatStatus;
import learn.models.Message;
import learn.models.User;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MessageService {
    private final MessageRepository messageRepository;
    private final ChatRepository chatRepository;

    public MessageService(MessageRepository messageRepository, ChatRepository chatRepository) {
        this.messageRepository = messageRepository;
        this.chatRepository = chatRepository;
    }

    public List<Message> findByChatId(int chatId) throws DataAccessException {
        return messageRepository.findByChatId(chatId);
    }

    public Result<Message> sendMessage(int chatId, User sender, String body) throws DataAccessException {
        Result<Message> result = new Result<>();

        Chat chat = chatRepository.findById(chatId);
        if (chat == null) {
            result.addErrorMessage("Chat %s was not found.", ResultType.NOT_FOUND, chatId);
            return result;
        }

        if (chat.getStatus() != ChatStatus.ACTIVE) {
            result.addErrorMessage("Chat %s is not active.", ResultType.INVALID, chatId);
            return result;
        }

        boolean isParticipant = chat.getClient().getId() == sender.getId() || (chat.getAgent() != null && chat.getAgent().getId() == sender.getId());
        if (!isParticipant) {
            result.addErrorMessage("You are not a participant in this chat.", ResultType.INVALID);
            return result;
        }

        if (body == null || body.isBlank()) {
            result.addErrorMessage("Message `body` is required.", ResultType.INVALID);
            return result;
        }

        Message message = new Message(chatId, sender, body, LocalDateTime.now());
        result.setPayload(messageRepository.create(message));
        return result;
    }
}
