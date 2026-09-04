package learn.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import learn.data.TestDataHelper;
import learn.domain.ChatService;
import learn.domain.MessageService;
import learn.domain.Result;
import learn.domain.ResultType;
import learn.domain.UserService;
import learn.models.Chat;
import learn.models.ChatStatus;
import learn.models.Problem;
import learn.models.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ChatController.class)
@AutoConfigureMockMvc(addFilters = false)
class ChatControllerTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    ChatService chatService;

    @MockBean
    MessageService messageService;

    @MockBean
    UserService userService;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private void authenticateAsAlice() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "alice", "password", List.of(new SimpleGrantedAuthority("ROLE_CLIENT")))
        );
    }

    private void authenticateAsBob() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "bob", "password", List.of(new SimpleGrantedAuthority("ROLE_AGENT")))
        );
    }

    @Test
    void shouldStartChat() throws Exception {
        authenticateAsAlice();

        Result<User> userResult = new Result<>();
        userResult.setPayload(TestDataHelper.existingClient());
        when(userService.findByUsername("alice")).thenReturn(userResult);

        Chat chat = new Chat(4, TestDataHelper.existingClient(), null, ChatStatus.WAITING,
                TestDataHelper.problemAfterCreate(), TestDataHelper.timeRecordAfterCreate());
        Result<Chat> chatResult = new Result<>();
        chatResult.setPayload(chat);
        when(chatService.startChat(eq(TestDataHelper.existingClient()), any(Problem.class))).thenReturn(chatResult);

        mvc.perform(post("/api/chats")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(TestDataHelper.problemToCreate())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("WAITING"))
                .andExpect(jsonPath("$.client.username").value("alice"))
                .andExpect(jsonPath("$.client.password").doesNotExist());
    }

    @Test
    void shouldRejectInvalidProblem() throws Exception {
        authenticateAsAlice();

        Result<User> userResult = new Result<>();
        userResult.setPayload(TestDataHelper.existingClient());
        when(userService.findByUsername("alice")).thenReturn(userResult);

        Result<Chat> chatResult = new Result<>();
        chatResult.addErrorMessage("Problem `description` is required.", ResultType.INVALID);
        when(chatService.startChat(any(User.class), any(Problem.class))).thenReturn(chatResult);

        mvc.perform(post("/api/chats")
                        .contentType("application/json")
                        .content("{\"category\":\"OTHER\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectWhenNotAuthenticated() throws Exception {
        mvc.perform(post("/api/chats")
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldFindWaiting() throws Exception {
        when(chatService.findWaiting()).thenReturn(List.of(TestDataHelper.existingWaitingChat()));

        mvc.perform(get("/api/chats/waiting"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("WAITING"))
                .andExpect(jsonPath("$[0].client.password").doesNotExist());
    }

    @Test
    void shouldClaim() throws Exception {
        authenticateAsBob();

        Result<User> agentResult = new Result<>();
        agentResult.setPayload(TestDataHelper.existingAgent());
        when(userService.findByUsername("bob")).thenReturn(agentResult);

        Chat claimedChat = new Chat(2, TestDataHelper.existingClient(), TestDataHelper.existingAgent(),
                ChatStatus.ACTIVE, TestDataHelper.existingProblem2(), TestDataHelper.existingTimeRecord2());
        Result<Chat> claimResult = new Result<>();
        claimResult.setPayload(claimedChat);
        when(chatService.claim(2, TestDataHelper.existingAgent())).thenReturn(claimResult);

        mvc.perform(post("/api/chats/2/claim"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.agent.username").value("bob"));
    }

    @Test
    void shouldRejectClaimWhenAlreadyClaimed() throws Exception {
        authenticateAsBob();

        Result<User> agentResult = new Result<>();
        agentResult.setPayload(TestDataHelper.existingAgent());
        when(userService.findByUsername("bob")).thenReturn(agentResult);

        Result<Chat> claimResult = new Result<>();
        claimResult.addErrorMessage("Chat 1 has already been claimed.", ResultType.CONFLICT);
        when(chatService.claim(1, TestDataHelper.existingAgent())).thenReturn(claimResult);

        mvc.perform(post("/api/chats/1/claim"))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldRejectClaimWhenNotAuthenticated() throws Exception {
        mvc.perform(post("/api/chats/1/claim"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldFindMessagesForParticipant() throws Exception {
        authenticateAsAlice();
        when(chatService.findById(1)).thenReturn(TestDataHelper.existingActiveChat());
        when(messageService.findByChatId(1)).thenReturn(
                List.of(TestDataHelper.existingClientMessage(), TestDataHelper.existingAgentMessage()));

        mvc.perform(get("/api/chats/1/messages"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].body").value("My laptop will not turn on."))
                .andExpect(jsonPath("$[0].sender.password").doesNotExist());
    }

    @Test
    void shouldRejectMessagesForNonParticipant() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "carol", "password", List.of(new SimpleGrantedAuthority("ROLE_ADMIN")))
        );
        when(chatService.findById(1)).thenReturn(TestDataHelper.existingActiveChat());

        mvc.perform(get("/api/chats/1/messages"))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldRejectMessagesWhenChatNotFound() throws Exception {
        authenticateAsAlice();
        when(chatService.findById(999)).thenReturn(null);

        mvc.perform(get("/api/chats/999/messages"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldRejectMessagesWhenNotAuthenticated() throws Exception {
        mvc.perform(get("/api/chats/1/messages"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldCloseAsClient() throws Exception {
        authenticateAsAlice();
        Result<User> clientResult = new Result<>();
        clientResult.setPayload(TestDataHelper.existingClient());
        when(userService.findByUsername("alice")).thenReturn(clientResult);

        Chat closedChat = new Chat(1, TestDataHelper.existingClient(), TestDataHelper.existingAgent(),
                ChatStatus.CLOSED_UNSOLVED, TestDataHelper.existingProblem1(), TestDataHelper.existingTimeRecord1());
        Result<Chat> closeResult = new Result<>();
        closeResult.setPayload(closedChat);
        when(chatService.close(1, TestDataHelper.existingClient())).thenReturn(closeResult);

        mvc.perform(post("/api/chats/1/close"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CLOSED_UNSOLVED"));
    }

    @Test
    void shouldCloseAsAgent() throws Exception {
        authenticateAsBob();
        Result<User> agentResult = new Result<>();
        agentResult.setPayload(TestDataHelper.existingAgent());
        when(userService.findByUsername("bob")).thenReturn(agentResult);

        Chat closedChat = new Chat(1, TestDataHelper.existingClient(), TestDataHelper.existingAgent(),
                ChatStatus.CLOSED_SOLVED, TestDataHelper.existingProblem1(), TestDataHelper.existingTimeRecord1());
        Result<Chat> closeResult = new Result<>();
        closeResult.setPayload(closedChat);
        when(chatService.close(1, TestDataHelper.existingAgent())).thenReturn(closeResult);

        mvc.perform(post("/api/chats/1/close"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CLOSED_SOLVED"));
    }

    @Test
    void shouldRejectCloseWhenNotParticipant() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "carol", "password", List.of(new SimpleGrantedAuthority("ROLE_ADMIN")))
        );
        Result<User> adminResult = new Result<>();
        adminResult.setPayload(TestDataHelper.existingAdmin());
        when(userService.findByUsername("carol")).thenReturn(adminResult);

        Result<Chat> closeResult = new Result<>();
        closeResult.addErrorMessage("You are not a participant in this chat.", ResultType.INVALID);
        when(chatService.close(1, TestDataHelper.existingAdmin())).thenReturn(closeResult);

        mvc.perform(post("/api/chats/1/close"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectCloseWhenNotAuthenticated() throws Exception {
        mvc.perform(post("/api/chats/1/close"))
                .andExpect(status().isUnauthorized());
    }
}
