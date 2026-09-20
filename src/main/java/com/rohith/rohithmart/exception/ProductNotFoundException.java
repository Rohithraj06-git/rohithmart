package com.rohith.rohithmart.exception;

/**
 * Exception thrown when a specified product cannot be found in the catalog (HTTP 404).
 */
public class ProductNotFoundException extends ResourceNotFoundException {

    private final Long productId;

    public ProductNotFoundException(Long productId) {
        super("Product not found with ID: " + productId, "PRODUCT_NOT_FOUND");
        this.productId = productId;
    }

    public ProductNotFoundException(String message) {
        super(message, "PRODUCT_NOT_FOUND");
        this.productId = null;
    }

    public Long getProductId() {
        return productId;
    }
}
