package learn.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import learn.data.DataAccessException;
import learn.domain.Result;
import learn.domain.UserService;
import learn.dtos.LoginRequest;
import learn.dtos.UserResponse;
import learn.models.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody User user) throws DataAccessException {
        Result<User> result = userService.create(user);
        if (!result.isSuccess()) {
            return ErrorResponse.build(result);
        }
        return new ResponseEntity<>(UserResponse.fromUser(result.getPayload()), HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest, HttpServletRequest request)
            throws DataAccessException {
        Result<Authentication> authResult =
                userService.authenticate(loginRequest.getUsername(), loginRequest.getPassword());
        if (!authResult.isSuccess()) {
            return new ResponseEntity<>(authResult.getErrorMessages(), HttpStatus.UNAUTHORIZED);
        }

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authResult.getPayload());
        SecurityContextHolder.setContext(context);

        HttpSession session = request.getSession(true);
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);

        Result<User> result = userService.findByUsername(loginRequest.getUsername());
        return ResponseEntity.ok(UserResponse.fromUser(result.getPayload()));
    }

    @GetMapping("/me")
    public ResponseEntity<?> me() throws DataAccessException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        Result<User> result = userService.findByUsername(authentication.getName());
        if (!result.isSuccess()) {
            return ErrorResponse.build(result);
        }
        return ResponseEntity.ok(UserResponse.fromUser(result.getPayload()));
    }
}
