package com.rohith.rohithmart.dto;

import com.rohith.rohithmart.model.OrderStatus;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;

/**
 * Data Transfer Object for Orders including items.
 * Extends OrderResponse for full backward compatibility.
 */
public class OrderDTO extends OrderResponse {
    public OrderDTO() {
        super();
    }

    public OrderDTO(Long id, Long buyerId, OrderStatus status, BigDecimal totalAmount,
                    Timestamp createdAt, List<? extends OrderItemResponse> items) {
        super(id, buyerId, status, totalAmount, createdAt, items);
    }
}
