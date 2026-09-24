package learn.rag;

import learn.data.ChatEmbeddingRepository;
import learn.data.DataAccessException;
import learn.data.MessageRepository;
import learn.models.Chat;
import learn.models.ChatEmbedding;
import learn.models.Message;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ChatEmbeddingService {
    private final ChatEmbeddingRepository chatEmbeddingRepository;
    private final MessageRepository messageRepository;
    private final EmbeddingClient embeddingClient;

    public ChatEmbeddingService(ChatEmbeddingRepository chatEmbeddingRepository,
                                 MessageRepository messageRepository,
                                 EmbeddingClient embeddingClient) {
        this.chatEmbeddingRepository = chatEmbeddingRepository;
        this.messageRepository = messageRepository;
        this.embeddingClient = embeddingClient;
    }

    public void embedChat(Chat chat) throws DataAccessException {
        List<Message> transcript = messageRepository.findByChatId(chat.getId());
        String content = buildContent(chat, transcript);
        List<Double> vector = embeddingClient.embed(content);
        chatEmbeddingRepository.create(new ChatEmbedding(chat.getId(), content, vector, LocalDateTime.now()));
    }

    private String buildContent(Chat chat, List<Message> transcript) {
        StringBuilder content = new StringBuilder();
        content.append(chat.getProblem().getCategory());
        if (chat.getProblem().getSubcategory() != null && !chat.getProblem().getSubcategory().isBlank()) {
            content.append(" / ").append(chat.getProblem().getSubcategory());
        }
        content.append(": ").append(chat.getProblem().getDescription());

        String lastRealMessage = transcript.stream()
                .filter(message -> message.getSender() != null)
                .reduce((first, second) -> second)
                .map(Message::getBody)
                .orElse(null);

        if (lastRealMessage != null) {
            content.append(" Resolution: ").append(lastRealMessage);
        }

        return content.toString();
    }
}
