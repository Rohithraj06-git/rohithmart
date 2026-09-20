package com.rohith.rohithmart.dto;

import com.rohith.rohithmart.model.OrderStatus;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Response DTO representing an order with its line items.
 */
public class OrderResponse {
    protected Long id;
    protected Long buyerId;
    protected OrderStatus status;
    protected BigDecimal totalAmount;
    protected Timestamp createdAt;
    protected List<OrderItemResponse> items = new ArrayList<>();

    public OrderResponse() {
    }

    public OrderResponse(Long id, Long buyerId, OrderStatus status, BigDecimal totalAmount,
                         Timestamp createdAt, List<? extends OrderItemResponse> items) {
        this.id = id;
        this.buyerId = buyerId;
        this.status = status;
        this.totalAmount = totalAmount;
        this.createdAt = createdAt;
        this.items = items != null ? new ArrayList<>(items) : new ArrayList<>();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getBuyerId() {
        return buyerId;
    }

    public void setBuyerId(Long buyerId) {
        this.buyerId = buyerId;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public List<OrderItemResponse> getItems() {
        return items;
    }

    public void setItems(List<? extends OrderItemResponse> items) {
        this.items = items != null ? new ArrayList<>(items) : new ArrayList<>();
    }

    public void addItem(OrderItemResponse item) {
        if (this.items == null) {
            this.items = new ArrayList<>();
        }
        this.items.add(item);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderResponse that = (OrderResponse) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "OrderResponse{" +
                "id=" + id +
                ", buyerId=" + buyerId +
                ", status=" + status +
                ", totalAmount=" + totalAmount +
                ", createdAt=" + createdAt +
                ", itemsCount=" + (items != null ? items.size() : 0) +
                '}';
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private Long buyerId;
        private OrderStatus status;
        private BigDecimal totalAmount;
        private Timestamp createdAt;
        private List<OrderItemResponse> items = new ArrayList<>();

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder buyerId(Long buyerId) {
            this.buyerId = buyerId;
            return this;
        }

        public Builder status(OrderStatus status) {
            this.status = status;
            return this;
        }

        public Builder totalAmount(BigDecimal totalAmount) {
            this.totalAmount = totalAmount;
            return this;
        }

        public Builder createdAt(Timestamp createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder items(List<? extends OrderItemResponse> items) {
            this.items = items != null ? new ArrayList<>(items) : new ArrayList<>();
            return this;
        }

        public Builder addItem(OrderItemResponse item) {
            if (this.items == null) {
                this.items = new ArrayList<>();
            }
            this.items.add(item);
            return this;
        }

        public OrderResponse build() {
            return new OrderResponse(id, buyerId, status, totalAmount, createdAt, items);
        }
    }
}
