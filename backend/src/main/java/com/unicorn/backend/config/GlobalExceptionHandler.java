package com.unicorn.backend.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Map<String, String>> handleAuthenticationException(AuthenticationException e) {
        Map<String, String> response = new HashMap<>();
        response.put("error", "Authentication Failed");
        response.put("message", e.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(com.unicorn.backend.exception.TokenRefreshException.class)
    public ResponseEntity<Map<String, String>> handleTokenRefreshException(
            com.unicorn.backend.exception.TokenRefreshException e) {
        Map<String, String> response = new HashMap<>();
        response.put("error", "RefreshToken Error");
        response.put("message", e.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    @ExceptionHandler(com.unicorn.backend.exception.UserSuspendedException.class)
    public ResponseEntity<com.unicorn.backend.auth.LoginResponse> handleUserSuspendedException(
            com.unicorn.backend.exception.UserSuspendedException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getLoginResponse());
    }

    @ExceptionHandler(com.unicorn.backend.exception.UserNotVerifiedException.class)
    public ResponseEntity<Map<String, String>> handleUserNotVerifiedException(
            com.unicorn.backend.exception.UserNotVerifiedException e) {
        Map<String, String> response = new HashMap<>();
        response.put("error", "EMAIL_NOT_VERIFIED");
        response.put("errorCode", "EMAIL_NOT_VERIFIED");
        response.put("message", "Please verify your email address before logging in");
        response.put("email", e.getEmail());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGenericException(Exception e) {
        // Log the error to console so we can debug it
        e.printStackTrace();

        Map<String, String> response = new HashMap<>();
        response.put("error", "Internal Server Error");
        response.put("message", e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
