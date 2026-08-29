package learn.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import learn.data.TestDataHelper;
import learn.domain.ChatService;
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
}
