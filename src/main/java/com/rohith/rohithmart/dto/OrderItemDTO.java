package com.rohith.rohithmart.dto;

import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * Data Transfer Object for Order Items.
 * Extends OrderItemResponse for full backward compatibility.
 */
public class OrderItemDTO extends OrderItemResponse {
    public OrderItemDTO() {
        super();
    }

    public OrderItemDTO(Long id, Long orderId, Long productId, String productName,
                        Integer quantity, BigDecimal unitPrice, BigDecimal subtotal, Timestamp createdAt) {
        super(id, orderId, productId, productName, quantity, unitPrice, subtotal, createdAt);
    }
}
