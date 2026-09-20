package com.rohith.rohithmart.exception;

/**
 * Exception thrown when a resource already exists or state conflicts (HTTP 409).
 */
public class ConflictException extends AppException {

    public ConflictException(String message) {
        super(message, 409, "CONFLICT");
    }

    public ConflictException(String message, String errorCode) {
        super(message, 409, errorCode);
    }
}
