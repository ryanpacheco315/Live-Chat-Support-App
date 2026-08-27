package learn.controllers;

import learn.domain.Result;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;

public class ErrorResponse {
    private final LocalDateTime timestamp = LocalDateTime.now();
    private final String message;

    public ErrorResponse(String message) {
        this.message = message;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getMessage() {
        return message;
    }

    public static ResponseEntity<Object> build(Result<?> result) {
        HttpStatus status = switch (result.getType()) {
            case NOT_FOUND -> HttpStatus.NOT_FOUND;
            case CONFLICT -> HttpStatus.CONFLICT;
            default -> HttpStatus.BAD_REQUEST;
        };
        return new ResponseEntity<>(result.getErrorMessages(), status);
    }
}
