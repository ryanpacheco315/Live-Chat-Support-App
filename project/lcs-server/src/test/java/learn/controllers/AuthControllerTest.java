package learn.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import learn.domain.Result;
import learn.domain.ResultType;
import learn.domain.UserService;
import learn.models.Role;
import learn.models.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    UserService userService;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldSignUp() throws Exception {
        User toCreate = new User("Dana Client", "dana", "rawpassword1", Role.CLIENT);
        User created = new User(4, "Dana Client", "dana", "hashed", Role.CLIENT);
        Result<User> result = new Result<>();
        result.setPayload(created);
        when(userService.create(any(User.class))).thenReturn(result);

        mvc.perform(post("/api/auth/signup")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(toCreate)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("dana"))
                .andExpect(jsonPath("$.role").value("CLIENT"));
    }

    @Test
    void shouldNotSignUpWhenInvalid() throws Exception {
        Result<User> result = new Result<>();
        result.addErrorMessage("User `password` must be at least 8 characters.", ResultType.INVALID);
        when(userService.create(any(User.class))).thenReturn(result);

        mvc.perform(post("/api/auth/signup")
                        .contentType("application/json")
                        .content("{\"fullName\":\"Dana\",\"username\":\"dana\",\"password\":\"short\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldLogin() throws Exception {
        Authentication authentication = new UsernamePasswordAuthenticationToken("bob", "password");
        Result<Authentication> authResult = new Result<>();
        authResult.setPayload(authentication);
        when(userService.authenticate("bob", "password")).thenReturn(authResult);

        Result<User> result = new Result<>();
        result.setPayload(new User(2, "Bob Agent", "bob", "hashed", Role.AGENT));
        when(userService.findByUsername("bob")).thenReturn(result);

        mvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content("{\"username\":\"bob\",\"password\":\"password\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("AGENT"));
    }

    @Test
    void shouldNotLoginWithBadCredentials() throws Exception {
        Result<Authentication> authResult = new Result<>();
        authResult.addErrorMessage("Invalid username or password.", ResultType.INVALID);
        when(userService.authenticate("bob", "wrong")).thenReturn(authResult);

        mvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content("{\"username\":\"bob\",\"password\":\"wrong\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldGetCurrentUser() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "bob", "password", List.of(new SimpleGrantedAuthority("ROLE_AGENT")))
        );

        Result<User> result = new Result<>();
        result.setPayload(new User(2, "Bob Agent", "bob", "hashed", Role.AGENT));
        when(userService.findByUsername("bob")).thenReturn(result);

        mvc.perform(get("/api/auth/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("bob"));
    }

    @Test
    void shouldRejectMeWhenNotAuthenticated() throws Exception {
        mvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());
    }
}
