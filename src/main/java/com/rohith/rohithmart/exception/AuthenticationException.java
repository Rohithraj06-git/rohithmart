package com.rohith.rohithmart.exception;

/**
 * Exception thrown when authentication fails or user is unauthenticated (HTTP 401).
 */
public class AuthenticationException extends AppException {
    public AuthenticationException(String message) {
        super(message, 401);
    }
}
