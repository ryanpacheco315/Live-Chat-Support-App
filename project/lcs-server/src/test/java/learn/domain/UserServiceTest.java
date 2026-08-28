package learn.domain;

import learn.data.DataAccessException;
import learn.data.TestDataHelper;
import learn.data.UserRepository;
import learn.models.Role;
import learn.models.User;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class UserServiceTest {

    @Autowired
    UserService service;

    @Autowired
    PasswordEncoder passwordEncoder;

    @MockBean
    UserRepository repository;

    @Nested
    class Authenticate {
        @Test
        void happyPath() throws DataAccessException {
            User existing = TestDataHelper.existingAgent();
            existing.setPassword(passwordEncoder.encode("password"));
            when(repository.findByUsername("bob")).thenReturn(existing);

            Result<Authentication> actual = service.authenticate("bob", "password");

            assertTrue(actual.isSuccess());
            assertEquals("bob", actual.getPayload().getName());
        }

        @Test
        void failsWithWrongPassword() throws DataAccessException {
            User existing = TestDataHelper.existingAgent();
            existing.setPassword(passwordEncoder.encode("password"));
            when(repository.findByUsername("bob")).thenReturn(existing);

            Result<Authentication> actual = service.authenticate("bob", "wrong");

            assertEquals(ResultType.INVALID, actual.getType());
            assertTrue(actual.getErrorMessages().contains("Invalid username or password."));
        }

        @Test
        void failsWhenUserDoesNotExist() throws DataAccessException {
            when(repository.findByUsername("nobody")).thenReturn(null);

            Result<Authentication> actual = service.authenticate("nobody", "whatever");

            assertEquals(ResultType.INVALID, actual.getType());
        }
    }

    @Nested
    class FindByUsername {
        @Test
        void happyPath() throws DataAccessException {
            when(repository.findByUsername("bob")).thenReturn(TestDataHelper.existingAgent());

            Result<User> actual = service.findByUsername("bob");

            assertTrue(actual.isSuccess());
            assertEquals(TestDataHelper.existingAgent(), actual.getPayload());
        }

        @Test
        void failsWhenNotFound() throws DataAccessException {
            when(repository.findByUsername("nobody")).thenReturn(null);

            Result<User> actual = service.findByUsername("nobody");

            assertEquals(ResultType.NOT_FOUND, actual.getType());
        }
    }

    @Nested
    class Create {
        @Test
        void failsWhenIdIsSet() throws DataAccessException {
            User toCreate = TestDataHelper.userToCreate();
            toCreate.setId(99);

            Result<User> actual = service.create(toCreate);

            assertEquals(ResultType.INVALID, actual.getType());
            assertTrue(actual.getErrorMessages().contains("User `id` should not be set."));
        }

        @Test
        void failsWhenFullNameBlank() throws DataAccessException {
            User toCreate = TestDataHelper.userToCreate();
            toCreate.setFullName("");

            Result<User> actual = service.create(toCreate);

            assertEquals(ResultType.INVALID, actual.getType());
            assertTrue(actual.getErrorMessages().contains("User `fullName` is required."));
        }

        @Test
        void failsWhenUsernameBlank() throws DataAccessException {
            User toCreate = TestDataHelper.userToCreate();
            toCreate.setUsername("");

            Result<User> actual = service.create(toCreate);

            assertEquals(ResultType.INVALID, actual.getType());
            assertTrue(actual.getErrorMessages().contains("User `username` is required."));
        }

        @Test
        void failsWhenPasswordTooShort() throws DataAccessException {
            User toCreate = TestDataHelper.userToCreate();
            toCreate.setPassword("short");

            Result<User> actual = service.create(toCreate);

            assertEquals(ResultType.INVALID, actual.getType());
            assertTrue(actual.getErrorMessages().contains(
                    String.format("User `password` must be at least %s characters.", UserService.MIN_PASSWORD_LENGTH)));
        }

        @Test
        void failsWhenUsernameTaken() throws DataAccessException {
            User toCreate = TestDataHelper.userToCreate();
            when(repository.findByUsername(toCreate.getUsername())).thenReturn(TestDataHelper.existingClient());

            Result<User> actual = service.create(toCreate);

            assertEquals(ResultType.INVALID, actual.getType());
            verify(repository, never()).create(any());
        }

        @Test
        void forcesClientRoleRegardlessOfInput() throws DataAccessException {
            User toCreate = TestDataHelper.userToCreate();
            toCreate.setRole(Role.ADMIN);
            when(repository.create(any(User.class))).thenReturn(TestDataHelper.userAfterCreate());

            Result<User> actual = service.create(toCreate);

            assertTrue(actual.isSuccess());
            assertEquals(Role.CLIENT, actual.getPayload().getRole());
        }

        @Test
        void happyPathHashesPassword() throws DataAccessException {
            User toCreate = TestDataHelper.userToCreate();
            String rawPassword = toCreate.getPassword();
            User created = TestDataHelper.userAfterCreate();
            created.setPassword(passwordEncoder.encode(rawPassword));
            when(repository.create(any(User.class))).thenReturn(created);

            Result<User> actual = service.create(toCreate);

            assertTrue(actual.isSuccess());
            assertNotEquals(rawPassword, actual.getPayload().getPassword());
            assertTrue(passwordEncoder.matches(rawPassword, actual.getPayload().getPassword()));
        }
    }
}