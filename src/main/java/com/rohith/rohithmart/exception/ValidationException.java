package com.rohith.rohithmart.exception;

import java.util.Collections;
import java.util.Map;

/**
 * Exception thrown when input validation fails (HTTP 400).
 */
public class ValidationException extends AppException {

    public ValidationException(String message) {
        super(message, 400, "VALIDATION_ERROR", Collections.emptyMap());
    }

    public ValidationException(String message, Map<String, String> fieldErrors) {
        super(message, 400, "VALIDATION_ERROR", fieldErrors);
    }

    public ValidationException(String message, String fieldName, String fieldError) {
        super(message, 400, "VALIDATION_ERROR", Map.of(fieldName, fieldError));
    }
}
