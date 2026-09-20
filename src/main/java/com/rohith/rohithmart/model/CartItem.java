package com.rohith.rohithmart.model;

import java.sql.Timestamp;
import java.util.Objects;

/**
 * Represents an item in a buyer's persistent shopping cart.
 */
public class CartItem {
    private Long id;
    private Long userId;
    private Long productId;
    private Integer quantity;
    private Timestamp createdAt;

    public CartItem() {
    }

    public CartItem(Long id, Long userId, Long productId, Integer quantity, Timestamp createdAt) {
        this.id = id;
        this.userId = userId;
        this.productId = productId;
        this.quantity = quantity;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CartItem cartItem = (CartItem) o;
        return Objects.equals(id, cartItem.id) ||
                (Objects.equals(userId, cartItem.userId) && Objects.equals(productId, cartItem.productId));
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, userId, productId);
    }

    @Override
    public String toString() {
        return "CartItem{" +
                "id=" + id +
                ", userId=" + userId +
                ", productId=" + productId +
                ", quantity=" + quantity +
                '}';
    }
}
