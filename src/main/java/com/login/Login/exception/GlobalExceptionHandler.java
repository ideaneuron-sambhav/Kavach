package com.login.Login.exception;

import com.login.Login.dto.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Response<Object>> handleRuntimeException(RuntimeException ex) {
        String message = ex.getMessage() != null ? ex.getMessage() : "Unexpected error";

        if (message.toLowerCase().contains("expired")) {
            return buildError(HttpStatus.UNAUTHORIZED, "Token expired");
        } else if (message.toLowerCase().contains("invalid")) {
            return buildError(HttpStatus.UNAUTHORIZED, "Invalid token");
        } else if (message.toLowerCase().contains("access denied")) {
            return buildError(HttpStatus.FORBIDDEN, "Access denied");
        } else if (message.toLowerCase().contains("not found")) {
            return buildError(HttpStatus.NOT_FOUND, message);
        } else if (message.toLowerCase().contains("already exists")) {
            return buildError(HttpStatus.CONFLICT, message);
        }

        return buildError(HttpStatus.BAD_REQUEST, message);
    }

    // Handle specific "user not found" case
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Response<Object>> handleUserNotFound(UserNotFoundException ex) {
        return buildError(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    // Handle invalid credentials (custom)
    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<Response<Object>> handleInvalidCredentials(InvalidCredentialsException ex) {
        return buildError(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    // Handle access denied (custom)
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Response<Object>> handleAccessDenied(AccessDeniedException ex) {
        return buildError(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    // Catch any other exception (fallback)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Response<Object>> handleException(Exception ex) {
        return buildError(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong: " + ex.getMessage());
    }
    @ExceptionHandler(TokenBlacklistedException.class)
    public ResponseEntity<?> handleTokenBlacklisted(TokenBlacklistedException ex) {
        return buildError(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    // Handle validation errors (password length, email format, etc.)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Response<Object>> handleValidationException(MethodArgumentNotValidException ex) {
        // Collect all error messages into a single string
        StringBuilder errorMessage = new StringBuilder();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errorMessage.append(error.getField())
                    .append(": ")
                    .append(error.getDefaultMessage())
                    .append("; ");
        }

        // Remove trailing "; " if present
        if (errorMessage.length() > 2) {
            errorMessage.setLength(errorMessage.length() - 2);
        }

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Response.builder()
                        .data(null)
                        .httpStatusCode(HttpStatus.BAD_REQUEST.value())
                        .message(errorMessage.toString())
                        .build());
    }

    // Helper method to build consistent response
    private ResponseEntity<Response<Object>> buildError(HttpStatus status, String message) {
        return ResponseEntity
                .status(status)
                .body(Response.builder()
                        .data(null)
                        .httpStatusCode(status.value())
                        .message(message)
                        .build());
    }
}
