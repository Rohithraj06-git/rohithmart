package com.rohith.rohithmart.exception;

/**
 * Exception thrown when user is forbidden from performing an operation (HTTP 403).
 */
public class ForbiddenException extends AuthorizationException {

    public ForbiddenException(String message) {
        super(message, "FORBIDDEN");
    }
}
