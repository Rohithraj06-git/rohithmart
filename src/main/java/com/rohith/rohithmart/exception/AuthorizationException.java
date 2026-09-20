package com.rohith.rohithmart.exception;

/**
 * Exception thrown when an authenticated user lacks permissions to access a resource (HTTP 403).
 */
public class AuthorizationException extends AppException {

    public AuthorizationException(String message) {
        super(message, 403, "ACCESS_DENIED");
    }

    public AuthorizationException(String message, String errorCode) {
        super(message, 403, errorCode);
    }
}
