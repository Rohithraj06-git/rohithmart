package com.rohith.rohithmart.dto;

import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * Data Transfer Object for Cart Items with product metadata and calculated subtotals.
 * Extends canonical CartItemResponse for backward compatibility.
 */
public class CartItemDTO extends CartItemResponse {

    public CartItemDTO() {
        super();
    }

    public CartItemDTO(Long id, Long productId, String productName, String productImageUrl,
                       BigDecimal unitPrice, Integer quantity, BigDecimal subtotal, Timestamp createdAt) {
        super(id, productId, productName, productImageUrl, unitPrice, quantity, subtotal, createdAt);
    }
}
