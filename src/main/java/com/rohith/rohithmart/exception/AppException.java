package com.rohith.rohithmart.exception;

import java.util.Collections;
import java.util.Map;

/**
 * Base checked exception for the RohithMart application.
 * Carries an HTTP status code, standardized error code, and optional field-level validation errors.
 */
public class AppException extends Exception {
    private final int statusCode;
    private final String errorCode;
    private final Map<String, String> fieldErrors;

    public AppException(String message, int statusCode) {
        this(message, statusCode, "ERROR", Collections.emptyMap());
    }

    public AppException(String message, int statusCode, String errorCode) {
        this(message, statusCode, errorCode, Collections.emptyMap());
    }

    public AppException(String message, int statusCode, String errorCode, Map<String, String> fieldErrors) {
        super(message);
        this.statusCode = statusCode;
        this.errorCode = errorCode;
        this.fieldErrors = fieldErrors != null ? Collections.unmodifiableMap(fieldErrors) : Collections.emptyMap();
    }

    public AppException(String message, Throwable cause, int statusCode) {
        this(message, cause, statusCode, "INTERNAL_ERROR");
    }

    public AppException(String message, Throwable cause, int statusCode, String errorCode) {
        super(message, cause);
        this.statusCode = statusCode;
        this.errorCode = errorCode;
        this.fieldErrors = Collections.emptyMap();
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public Map<String, String> getFieldErrors() {
        return fieldErrors;
    }
}
