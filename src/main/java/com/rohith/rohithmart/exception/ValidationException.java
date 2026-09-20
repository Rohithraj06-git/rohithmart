package com.rohith.rohithmart.exception;

/**
 * Exception thrown when input validation fails (HTTP 400).
 */
public class ValidationException extends AppException {
    public ValidationException(String message) {
        super(message, 400);
    }
}
