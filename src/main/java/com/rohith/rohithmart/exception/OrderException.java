package com.rohith.rohithmart.exception;

/**
 * Exception thrown when order processing, checkout, or cancellation fails (HTTP 400).
 */
public class OrderException extends AppException {

    public OrderException(String message) {
        super(message, 400, "ORDER_ERROR");
    }

    public OrderException(String message, String errorCode) {
        super(message, 400, errorCode);
    }

    public OrderException(String message, Throwable cause) {
        super(message, cause, 400, "ORDER_ERROR");
    }
}
