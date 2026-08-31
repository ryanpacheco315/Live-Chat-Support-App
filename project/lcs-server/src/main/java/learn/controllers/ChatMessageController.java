package learn.controllers;

import learn.data.DataAccessException;
import learn.domain.MessageService;
import learn.domain.Result;
import learn.domain.UserService;
import learn.dtos.MessageResponse;
import learn.dtos.SendMessageRequest;
import learn.models.Message;
import learn.models.User;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
public class ChatMessageController {
    private final MessageService messageService;
    private final UserService userService;
    private final SimpMessagingTemplate messagingTemplate;

    public ChatMessageController(MessageService messageService, UserService userService, SimpMessagingTemplate messagingTemplate) {
        this.messageService = messageService;
        this.userService = userService;
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/chat/{chatId}/send")
    public void send(@DestinationVariable int chatId, @Payload SendMessageRequest request, Principal principal)
            throws DataAccessException {
        if (principal == null) {
            return;
        }

        Result<User> senderResult = userService.findByUsername(principal.getName());
        if (!senderResult.isSuccess()) {
            return;
        }

        Result<Message> result = messageService.sendMessage(chatId, senderResult.getPayload(), request.getBody());
        if (!result.isSuccess()) {
            return;
        }

        messagingTemplate.convertAndSend("/topic/chat/" + chatId, MessageResponse.fromMessage(result.getPayload()));
    }
}
