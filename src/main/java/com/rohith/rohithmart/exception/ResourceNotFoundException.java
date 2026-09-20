package com.rohith.rohithmart.exception;

/**
 * Exception thrown when a requested resource is not found (HTTP 404).
 */
public class ResourceNotFoundException extends AppException {
    public ResourceNotFoundException(String message) {
        super(message, 404);
    }
}
