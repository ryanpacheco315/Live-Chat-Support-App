package learn.controllers;

import learn.data.DataAccessException;
import learn.data.TestDataHelper;
import learn.domain.MessageService;
import learn.domain.Result;
import learn.domain.ResultType;
import learn.domain.UserService;
import learn.dtos.MessageResponse;
import learn.dtos.SendMessageRequest;
import learn.models.Message;
import learn.models.User;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.security.Principal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class ChatMessageControllerTest {

    MessageService messageService = mock(MessageService.class);
    UserService userService = mock(UserService.class);
    SimpMessagingTemplate messagingTemplate = mock(SimpMessagingTemplate.class);
    ChatMessageController controller = new ChatMessageController(messageService, userService, messagingTemplate);

    private Principal principalNamed(String username) {
        return () -> username;
    }

    @Test
    void shouldBroadcastOnSuccess() throws DataAccessException {
        Result<User> userResult = new Result<>();
        userResult.setPayload(TestDataHelper.existingClient());
        when(userService.findByUsername("alice")).thenReturn(userResult);

        Result<Message> messageResult = new Result<>();
        messageResult.setPayload(TestDataHelper.messageAfterCreate());
        when(messageService.sendMessage(1, TestDataHelper.existingClient(), "hello")).thenReturn(messageResult);

        controller.send(1, new SendMessageRequest("hello"), principalNamed("alice"));

        verify(messagingTemplate).convertAndSend(eq("/topic/chat/1"), any(MessageResponse.class));
    }

    @Test
    void shouldNotBroadcastWhenSendFails() throws DataAccessException {
        Result<User> userResult = new Result<>();
        userResult.setPayload(TestDataHelper.existingClient());
        when(userService.findByUsername("alice")).thenReturn(userResult);

        Result<Message> messageResult = new Result<>();
        messageResult.addErrorMessage("Chat 1 is not active.", ResultType.INVALID);
        when(messageService.sendMessage(1, TestDataHelper.existingClient(), "hello")).thenReturn(messageResult);

        controller.send(1, new SendMessageRequest("hello"), principalNamed("alice"));

        verifyNoInteractions(messagingTemplate);
    }

    @Test
    void shouldNotBroadcastWhenPrincipalMissing() throws DataAccessException {
        controller.send(1, new SendMessageRequest("hello"), null);

        verifyNoInteractions(messageService, messagingTemplate);
    }

    @Test
    void shouldNotBroadcastWhenSenderNotFound() throws DataAccessException {
        Result<User> userResult = new Result<>();
        userResult.addErrorMessage("User not found", ResultType.NOT_FOUND);
        when(userService.findByUsername("ghost")).thenReturn(userResult);

        controller.send(1, new SendMessageRequest("hello"), principalNamed("ghost"));

        verifyNoInteractions(messageService, messagingTemplate);
    }
}
