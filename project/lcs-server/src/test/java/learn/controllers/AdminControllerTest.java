package learn.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import learn.data.TestDataHelper;
import learn.domain.Result;
import learn.domain.ResultType;
import learn.domain.UserService;
import learn.models.Role;
import learn.models.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AdminController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminControllerTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    UserService userService;

    @Test
    void shouldCreateAgent() throws Exception {
        User toCreate = new User("Ed Agent", "ed", "rawpassword1", Role.CLIENT);
        User created = new User(5, "Ed Agent", "ed", "hashed", Role.AGENT);
        Result<User> result = new Result<>();
        result.setPayload(created);
        when(userService.createAgent(any(User.class))).thenReturn(result);

        mvc.perform(post("/api/admin/agents")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(toCreate)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("ed"))
                .andExpect(jsonPath("$.role").value("AGENT"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void shouldRejectInvalidAgent() throws Exception {
        Result<User> result = new Result<>();
        result.addErrorMessage("Username `bob` is already taken.", ResultType.INVALID);
        when(userService.createAgent(any(User.class))).thenReturn(result);

        mvc.perform(post("/api/admin/agents")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(TestDataHelper.userToCreate())))
                .andExpect(status().isBadRequest());
    }
}
