package learn.domain;

import learn.data.DataAccessException;
import learn.data.UserRepository;
import learn.models.Role;
import learn.models.User;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    public static final int MIN_PASSWORD_LENGTH = 8;

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    public UserService(UserRepository repository, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
    }

    public Result<Authentication> authenticate(String username, String password) {
        Result<Authentication> result = new Result<>();
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password));
            result.setPayload(authentication);
        } catch (BadCredentialsException ex) {
            result.addErrorMessage("Invalid username or password.", ResultType.INVALID);
        }
        return result;
    }

    public Result<User> findByUsername(String username) throws DataAccessException {
        Result<User> result = new Result<>();
        User found = repository.findByUsername(username);

        if (found == null) {
            result.addErrorMessage("User not found", ResultType.NOT_FOUND);
        } else {
            result.setPayload(found);
        }

        return result;
    }

    public Result<User> create(User user) throws DataAccessException {
        Result<User> result = new Result<>();
        validate(user, result);

        if (!result.isSuccess())
            return result;

        if (repository.findByUsername(user.getUsername()) != null) {
            result.addErrorMessage("Username `%s` is already taken.", ResultType.INVALID, user.getUsername());
            return result;
        }

        user.setRole(Role.CLIENT);
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        User created = repository.create(user);
        result.setPayload(created);
        return result;
    }

    private void validate(User user, Result<User> result) {
        if (user == null) {
            result.addErrorMessage("User cannot be null.", ResultType.INVALID);
            return;
        }

        if (user.getId() > 0) {
            result.addErrorMessage("User `id` should not be set.", ResultType.INVALID);
        }

        if (user.getFullName() == null || user.getFullName().isBlank()) {
            result.addErrorMessage("User `fullName` is required.", ResultType.INVALID);
        }

        if (user.getUsername() == null || user.getUsername().isBlank()) {
            result.addErrorMessage("User `username` is required.", ResultType.INVALID);
        }

        if (user.getPassword() == null || user.getPassword().length() < MIN_PASSWORD_LENGTH) {
            result.addErrorMessage("User `password` must be at least %s characters.",
                    ResultType.INVALID, MIN_PASSWORD_LENGTH);
        }
    }
}
