package learn.domain;

import learn.data.ChatRepository;
import learn.data.DataAccessException;
import learn.data.MessageRepository;
import learn.data.ProblemRepository;
import learn.data.TimeRecordRepository;
import learn.dtos.ChatResponse;
import learn.dtos.MessageResponse;
import learn.dtos.QueueUpdate;
import learn.models.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ChatService {
    private static final String QUEUE_TOPIC = "/topic/queue";

    private final ChatRepository chatRepository;
    private final ProblemRepository problemRepository;
    private final TimeRecordRepository timeRecordRepository;
    private final MessageRepository messageRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public ChatService(ChatRepository chatRepository, ProblemRepository problemRepository,
                        TimeRecordRepository timeRecordRepository, MessageRepository messageRepository,
                        SimpMessagingTemplate messagingTemplate) {
        this.chatRepository = chatRepository;
        this.problemRepository = problemRepository;
        this.timeRecordRepository = timeRecordRepository;
        this.messageRepository = messageRepository;
        this.messagingTemplate = messagingTemplate;
    }

    @Transactional
    public Result<Chat> startChat(User client, Problem problem) throws DataAccessException {
        Result<Chat> result = new Result<>();
        validate(problem, result);

        if (!result.isSuccess())
            return result;

        Problem createdProblem = problemRepository.create(problem);
        TimeRecord createdTimeRecord = timeRecordRepository.create(new TimeRecord(LocalDateTime.now(), null));

        Chat chat = new Chat(client, null, ChatStatus.WAITING, createdProblem, createdTimeRecord);
        Chat createdChat = chatRepository.create(chat);

        result.setPayload(createdChat);
        messagingTemplate.convertAndSend(QUEUE_TOPIC, QueueUpdate.added(ChatResponse.fromChat(createdChat)));
        return result;
    }

    public Chat findById(int chatId) throws DataAccessException {
        return chatRepository.findById(chatId);
    }

    public List<Chat> findWaiting() throws DataAccessException {
        return chatRepository.findWaiting();
    }

    @Transactional
    public Result<Chat> claim(int chatId, User agent) throws DataAccessException {
        Result<Chat> result = new Result<>();

        boolean claimed = chatRepository.claim(chatId, agent.getId());
        if (!claimed) {
            Chat existing = chatRepository.findById(chatId);
            if (existing == null) {
                result.addErrorMessage("Chat %s was not found.", ResultType.NOT_FOUND, chatId);
            } else {
                result.addErrorMessage("Chat %s has already been claimed.", ResultType.CONFLICT, chatId);
            }
            return result;
        }

        Chat updated = chatRepository.findById(chatId);
        Message systemMessage = messageRepository.create(new Message(chatId, null, "An agent has joined the chat.", LocalDateTime.now()));

        result.setPayload(updated);
        messagingTemplate.convertAndSend(QUEUE_TOPIC, QueueUpdate.claimed(chatId));
        messagingTemplate.convertAndSend("/topic/chat/" + chatId, MessageResponse.fromMessage(systemMessage));
        return result;
    }

    @Transactional
    public Result<Chat> close(int chatId, User requester) throws DataAccessException {
        Result<Chat> result = new Result<>();

        Chat chat = chatRepository.findById(chatId);
        if (chat == null) {
            result.addErrorMessage("Chat %s was not found.", ResultType.NOT_FOUND, chatId);
            return result;
        }

        if (chat.getStatus() != ChatStatus.ACTIVE) {
            result.addErrorMessage("Chat %s is not active.", ResultType.INVALID, chatId);
            return result;
        }

        boolean isClient = chat.getClient().getId() == requester.getId();
        boolean isAgent = chat.getAgent() != null && chat.getAgent().getId() == requester.getId();
        if (!isClient && !isAgent) {
            result.addErrorMessage("You are not a participant in this chat.", ResultType.INVALID);
            return result;
        }

        ChatStatus finalStatus = isClient ? ChatStatus.CLOSED_UNSOLVED : ChatStatus.CLOSED_SOLVED;
        chatRepository.close(chatId, finalStatus);

        TimeRecord timeRecord = chat.getTimeRecord();
        if (timeRecord != null) {
            timeRecord.setClosedAt(LocalDateTime.now());
            timeRecordRepository.update(timeRecord);
        }

        Chat closedChat = chatRepository.findById(chatId);
        String closerLabel = isClient ? "the client" : "the agent";
        Message systemMessage = messageRepository.create(
                new Message(chatId, null, "Chat closed by " + closerLabel + ".", LocalDateTime.now()));

        result.setPayload(closedChat);
        messagingTemplate.convertAndSend("/topic/chat/" + chatId, MessageResponse.fromMessage(systemMessage));
        messagingTemplate.convertAndSend("/topic/chat/" + chatId, ChatResponse.fromChat(closedChat));
        return result;
    }

    private void validate(Problem problem, Result<Chat> result) {
        if (problem == null) {
            result.addErrorMessage("Problem cannot be null.", ResultType.INVALID);
            return;
        }

        if (problem.getId() > 0) {
            result.addErrorMessage("Problem `id` should not be set.", ResultType.INVALID);
        }

        if (problem.getCategory() == null) {
            result.addErrorMessage("Problem `category` is required.", ResultType.INVALID);
        }

        if (problem.getDescription() == null || problem.getDescription().isBlank()) {
            result.addErrorMessage("Problem `description` is required.", ResultType.INVALID);
        }
    }
}
