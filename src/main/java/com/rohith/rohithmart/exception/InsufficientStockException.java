package com.rohith.rohithmart.exception;

/**
 * Exception thrown when a requested product quantity exceeds available inventory (HTTP 400).
 */
public class InsufficientStockException extends AppException {

    private final Long productId;
    private final int requestedQuantity;
    private final int availableStock;

    public InsufficientStockException(Long productId, int requestedQuantity, int availableStock) {
        super(String.format("Insufficient stock for product ID %d. Requested: %d, Available: %d",
                productId, requestedQuantity, availableStock), 400, "INSUFFICIENT_STOCK");
        this.productId = productId;
        this.requestedQuantity = requestedQuantity;
        this.availableStock = availableStock;
    }

    public InsufficientStockException(String message) {
        super(message, 400, "INSUFFICIENT_STOCK");
        this.productId = null;
        this.requestedQuantity = 0;
        this.availableStock = 0;
    }

    public Long getProductId() {
        return productId;
    }

    public int getRequestedQuantity() {
        return requestedQuantity;
    }

    public int getAvailableStock() {
        return availableStock;
    }
}
