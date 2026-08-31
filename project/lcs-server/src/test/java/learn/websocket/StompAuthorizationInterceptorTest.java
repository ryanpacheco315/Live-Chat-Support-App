package learn.websocket;

import learn.data.ChatRepository;
import learn.data.DataAccessException;
import learn.data.TestDataHelper;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.security.Principal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class StompAuthorizationInterceptorTest {

    ChatRepository chatRepository = mock(ChatRepository.class);
    StompAuthorizationInterceptor interceptor = new StompAuthorizationInterceptor(chatRepository);

    private Message<byte[]> subscribeMessage(String destination, Principal principal) {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);
        accessor.setDestination(destination);
        if (principal != null) {
            accessor.setUser(principal);
        }
        accessor.setLeaveMutable(true);
        return MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
    }

    private Authentication authAs(String username, String role) {
        return new UsernamePasswordAuthenticationToken(username, "password",
                List.of(new SimpleGrantedAuthority("ROLE_" + role)));
    }

    @Test
    void shouldRejectSubscribeWithNoPrincipal() {
        Message<byte[]> message = subscribeMessage("/topic/queue", null);

        assertThrows(AccessDeniedException.class, () -> interceptor.preSend(message, null));
    }

    @Test
    void shouldAllowAgentToSubscribeToQueue() {
        Message<byte[]> message = subscribeMessage("/topic/queue", authAs("bob", "AGENT"));

        assertDoesNotThrow(() -> interceptor.preSend(message, null));
    }

    @Test
    void shouldRejectClientSubscribingToQueue() {
        Message<byte[]> message = subscribeMessage("/topic/queue", authAs("alice", "CLIENT"));

        assertThrows(AccessDeniedException.class, () -> interceptor.preSend(message, null));
    }

    @Test
    void shouldAllowParticipantToSubscribeToChatTopic() throws DataAccessException {
        when(chatRepository.findById(1)).thenReturn(TestDataHelper.existingActiveChat());
        Message<byte[]> message = subscribeMessage("/topic/chat/1", authAs("alice", "CLIENT"));

        assertDoesNotThrow(() -> interceptor.preSend(message, null));
    }

    @Test
    void shouldRejectNonParticipantSubscribingToChatTopic() throws DataAccessException {
        when(chatRepository.findById(1)).thenReturn(TestDataHelper.existingActiveChat());
        Message<byte[]> message = subscribeMessage("/topic/chat/1", authAs("carol", "ADMIN"));

        assertThrows(AccessDeniedException.class, () -> interceptor.preSend(message, null));
    }

    @Test
    void shouldRejectSubscribingToNonexistentChat() throws DataAccessException {
        when(chatRepository.findById(999)).thenReturn(null);
        Message<byte[]> message = subscribeMessage("/topic/chat/999", authAs("alice", "CLIENT"));

        assertThrows(AccessDeniedException.class, () -> interceptor.preSend(message, null));
    }

    @Test
    void shouldAllowNonSubscribeCommandsThrough() {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SEND);
        accessor.setLeaveMutable(true);
        Message<byte[]> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        assertDoesNotThrow(() -> interceptor.preSend(message, null));
    }
}
