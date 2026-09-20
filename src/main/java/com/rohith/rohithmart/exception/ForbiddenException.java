package com.rohith.rohithmart.exception;

/**
 * Exception thrown when authenticated user lacks permissions (HTTP 403).
 */
public class ForbiddenException extends AppException {
    public ForbiddenException(String message) {
        super(message, 403);
    }
}
