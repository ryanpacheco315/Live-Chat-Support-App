package learn.controllers;

import learn.data.DataAccessException;
import learn.domain.Result;
import learn.domain.UserService;
import learn.dtos.UserResponse;
import learn.models.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final UserService userService;

    public AdminController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/agents")
    public ResponseEntity<?> createAgent(@RequestBody User user) throws DataAccessException {
        Result<User> result = userService.createAgent(user);
        if (!result.isSuccess()) {
            return ErrorResponse.build(result);
        }
        return new ResponseEntity<>(UserResponse.fromUser(result.getPayload()), HttpStatus.CREATED);
    }
}
