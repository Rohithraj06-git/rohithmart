package com.rohith.rohithmart.exception;

/**
 * Exception thrown when a database or persistence operation encounters an internal failure (HTTP 500).
 */
public class DatabaseException extends AppException {

    public DatabaseException(String message) {
        super(message, 500, "DATABASE_ERROR");
    }

    public DatabaseException(String message, Throwable cause) {
        super(message, cause, 500, "DATABASE_ERROR");
    }
}
