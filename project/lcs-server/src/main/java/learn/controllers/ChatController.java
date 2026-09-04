package learn.controllers;

import learn.data.DataAccessException;
import learn.domain.ChatService;
import learn.domain.MessageService;
import learn.domain.Result;
import learn.domain.UserService;
import learn.dtos.ChatResponse;
import learn.dtos.MessageResponse;
import learn.models.Chat;
import learn.models.Problem;
import learn.models.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chats")
public class ChatController {
    private final ChatService chatService;
    private final MessageService messageService;
    private final UserService userService;

    public ChatController(ChatService chatService, MessageService messageService, UserService userService) {
        this.chatService = chatService;
        this.messageService = messageService;
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<?> startChat(@RequestBody Problem problem) throws DataAccessException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        Result<User> clientResult = userService.findByUsername(authentication.getName());
        if (!clientResult.isSuccess()) {
            return ErrorResponse.build(clientResult);
        }

        Result<Chat> result = chatService.startChat(clientResult.getPayload(), problem);
        if (!result.isSuccess()) {
            return ErrorResponse.build(result);
        }
        return new ResponseEntity<>(ChatResponse.fromChat(result.getPayload()), HttpStatus.CREATED);
    }

    @GetMapping("/waiting")
    public List<ChatResponse> findWaiting() throws DataAccessException {
        return chatService.findWaiting().stream().map(ChatResponse::fromChat).toList();
    }

    @PostMapping("/{id}/claim")
    public ResponseEntity<?> claim(@PathVariable int id) throws DataAccessException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        Result<User> agentResult = userService.findByUsername(authentication.getName());
        if (!agentResult.isSuccess()) {
            return ErrorResponse.build(agentResult);
        }

        Result<Chat> result = chatService.claim(id, agentResult.getPayload());
        if (!result.isSuccess()) {
            return ErrorResponse.build(result);
        }
        return ResponseEntity.ok(ChatResponse.fromChat(result.getPayload()));
    }

    @PostMapping("/{id}/close")
    public ResponseEntity<?> close(@PathVariable int id) throws DataAccessException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        Result<User> requesterResult = userService.findByUsername(authentication.getName());
        if (!requesterResult.isSuccess()) {
            return ErrorResponse.build(requesterResult);
        }

        Result<Chat> result = chatService.close(id, requesterResult.getPayload());
        if (!result.isSuccess()) {
            return ErrorResponse.build(result);
        }
        return ResponseEntity.ok(ChatResponse.fromChat(result.getPayload()));
    }

    @GetMapping("/{id}/messages")
    public ResponseEntity<?> findMessages(@PathVariable int id) throws DataAccessException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        Chat chat = chatService.findById(id);
        if (chat == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        String username = authentication.getName();
        boolean isParticipant = chat.getClient().getUsername().equals(username)
                || (chat.getAgent() != null && chat.getAgent().getUsername().equals(username));
        if (!isParticipant) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        List<MessageResponse> messages = messageService.findByChatId(id).stream()
                .map(MessageResponse::fromMessage)
                .toList();
        return ResponseEntity.ok(messages);
    }
}
