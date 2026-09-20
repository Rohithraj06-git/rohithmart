package com.rohith.rohithmart.exception;

/**
 * Exception thrown when a requested resource is not found (HTTP 404).
 */
public class ResourceNotFoundException extends AppException {

    public ResourceNotFoundException(String message) {
        super(message, 404, "RESOURCE_NOT_FOUND");
    }

    public ResourceNotFoundException(String message, String errorCode) {
        super(message, 404, errorCode);
    }
}
