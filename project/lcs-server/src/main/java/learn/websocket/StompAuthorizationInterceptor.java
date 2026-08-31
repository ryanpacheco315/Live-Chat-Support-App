package learn.websocket;

import learn.data.ChatRepository;
import learn.data.DataAccessException;
import learn.models.Chat;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.security.Principal;

@Component
public class StompAuthorizationInterceptor implements ChannelInterceptor {
    private final ChatRepository chatRepository;

    public StompAuthorizationInterceptor(ChatRepository chatRepository) {
        this.chatRepository = chatRepository;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            String destination = accessor.getDestination();
            Principal principal = accessor.getUser();

            if (destination == null || principal == null) {
                throw new AccessDeniedException("Not authorized for this subscription.");
            }

            if (destination.equals("/topic/queue")) {
                if (!hasAgentRole(principal)) {
                    throw new AccessDeniedException("Only agents may subscribe to the queue.");
                }
            } else if (destination.startsWith("/topic/chat/")) {
                if (!isParticipant(destination, principal.getName())) {
                    throw new AccessDeniedException("Not a participant in this chat.");
                }
            }
        }

        return message;
    }

    private boolean hasAgentRole(Principal principal) {
        if (principal instanceof Authentication authentication) {
            return authentication.getAuthorities().stream()
                    .anyMatch(authority -> authority.getAuthority().equals("ROLE_AGENT"));
        }
        return false;
    }

    private boolean isParticipant(String destination, String username) {
        int chatId;
        try {
            chatId = Integer.parseInt(destination.substring(destination.lastIndexOf('/') + 1));
        } catch (NumberFormatException ex) {
            return false;
        }

        try {
            Chat chat = chatRepository.findById(chatId);
            if (chat == null) {
                return false;
            }
            boolean isClient = chat.getClient().getUsername().equals(username);
            boolean isAgent = chat.getAgent() != null && chat.getAgent().getUsername().equals(username);
            return isClient || isAgent;
        } catch (DataAccessException ex) {
            return false;
        }
    }
}
