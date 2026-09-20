package com.rohith.rohithmart.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Model Classes Unit Tests")
class ModelTest {

    @Test
    @DisplayName("User entity: properties, equals, hashCode, and masked toString()")
    void testUserEntity() {
        Timestamp now = Timestamp.from(Instant.now());
        User user1 = new User(1L, "Alice Buyer", "alice@example.com", "secretHash123", Role.BUYER, now);
        User user2 = new User(1L, "Alice Buyer", "alice@example.com", "secretHash123", Role.BUYER, now);
        User user3 = new User(2L, "Bob Seller", "bob@example.com", "otherHash456", Role.SELLER, now);

        assertEquals(1L, user1.getId());
        assertEquals("Alice Buyer", user1.getName());
        assertEquals("alice@example.com", user1.getEmail());
        assertEquals("secretHash123", user1.getPasswordHash());
        assertEquals(Role.BUYER, user1.getRole());
        assertEquals(now, user1.getCreatedAt());

        // Equals & HashCode
        assertEquals(user1, user2);
        assertEquals(user1.hashCode(), user2.hashCode());
        assertNotEquals(user1, user3);

        // Security: toString() must NEVER leak passwordHash
        String str = user1.toString();
        assertFalse(str.contains("secretHash123"), "toString() must not expose password hash");
        assertTrue(str.contains("[PROTECTED]"), "toString() should mask password hash");
    }

    @Test
    @DisplayName("Product entity: properties, equals, hashCode, and toString()")
    void testProductEntity() {
        Timestamp now = Timestamp.from(Instant.now());
        Product p1 = new Product(10L, 2L, "Wireless Mouse", "Ergonomic mouse", new BigDecimal("29.99"), 100, "Electronics", "mouse.jpg", now);
        Product p2 = new Product(10L, 2L, "Wireless Mouse", "Ergonomic mouse", new BigDecimal("29.99"), 100, "Electronics", "mouse.jpg", now);
        Product p3 = new Product(20L, 2L, "Keyboard", "Mechanical", new BigDecimal("79.99"), 50, "Electronics", "kb.jpg", now);

        assertEquals(10L, p1.getId());
        assertEquals(2L, p1.getSellerId());
        assertEquals("Wireless Mouse", p1.getName());
        assertEquals("Ergonomic mouse", p1.getDescription());
        assertEquals(new BigDecimal("29.99"), p1.getPrice());
        assertEquals(100, p1.getStockQty());
        assertEquals("Electronics", p1.getCategory());
        assertEquals("mouse.jpg", p1.getImageUrl());
        assertEquals(now, p1.getCreatedAt());

        assertEquals(p1, p2);
        assertEquals(p1.hashCode(), p2.hashCode());
        assertNotEquals(p1, p3);
        assertTrue(p1.toString().contains("Wireless Mouse"));
    }

    @Test
    @DisplayName("Order entity: properties, equals, hashCode, and toString()")
    void testOrderEntity() {
        Timestamp now = Timestamp.from(Instant.now());
        Order o1 = new Order(100L, 1L, OrderStatus.CONFIRMED, new BigDecimal("149.99"), now);
        Order o2 = new Order(100L, 1L, OrderStatus.CONFIRMED, new BigDecimal("149.99"), now);
        Order o3 = new Order(101L, 1L, OrderStatus.PENDING, new BigDecimal("50.00"), now);

        assertEquals(100L, o1.getId());
        assertEquals(1L, o1.getBuyerId());
        assertEquals(OrderStatus.CONFIRMED, o1.getStatus());
        assertEquals(new BigDecimal("149.99"), o1.getTotalAmount());
        assertEquals(now, o1.getCreatedAt());

        assertEquals(o1, o2);
        assertEquals(o1.hashCode(), o2.hashCode());
        assertNotEquals(o1, o3);
        assertTrue(o1.toString().contains("CONFIRMED"));
    }

    @Test
    @DisplayName("OrderItem entity: properties, equals, hashCode, and toString()")
    void testOrderItemEntity() {
        Timestamp now = Timestamp.from(Instant.now());
        OrderItem oi1 = new OrderItem(500L, 100L, 10L, 2, new BigDecimal("29.99"), now);
        OrderItem oi2 = new OrderItem(500L, 100L, 10L, 2, new BigDecimal("29.99"), now);
        OrderItem oi3 = new OrderItem(501L, 100L, 20L, 1, new BigDecimal("79.99"), now);

        assertEquals(500L, oi1.getId());
        assertEquals(100L, oi1.getOrderId());
        assertEquals(10L, oi1.getProductId());
        assertEquals(2, oi1.getQuantity());
        assertEquals(new BigDecimal("29.99"), oi1.getUnitPrice());
        assertEquals(now, oi1.getCreatedAt());

        assertEquals(oi1, oi2);
        assertEquals(oi1.hashCode(), oi2.hashCode());
        assertNotEquals(oi1, oi3);
        assertTrue(oi1.toString().contains("500"));
    }

    @Test
    @DisplayName("CartItem entity: properties, equals, hashCode, and toString()")
    void testCartItemEntity() {
        Timestamp now = Timestamp.from(Instant.now());
        CartItem c1 = new CartItem(300L, 1L, 10L, 3, now);
        CartItem c2 = new CartItem(300L, 1L, 10L, 3, now);
        CartItem c3 = new CartItem(301L, 1L, 20L, 1, now);

        assertEquals(300L, c1.getId());
        assertEquals(1L, c1.getUserId());
        assertEquals(10L, c1.getProductId());
        assertEquals(3, c1.getQuantity());
        assertEquals(now, c1.getCreatedAt());

        assertEquals(c1, c2);
        assertEquals(c1.hashCode(), c2.hashCode());
        assertNotEquals(c1, c3);
        assertTrue(c1.toString().contains("300"));
    }

    @Test
    @DisplayName("Review entity: properties, equals, hashCode, and toString()")
    void testReviewEntity() {
        Timestamp now = Timestamp.from(Instant.now());
        Review r1 = new Review(400L, 10L, 1L, 5, "Outstanding product!", now);
        Review r2 = new Review(400L, 10L, 1L, 5, "Outstanding product!", now);
        Review r3 = new Review(401L, 10L, 2L, 4, "Good quality", now);

        assertEquals(400L, r1.getId());
        assertEquals(10L, r1.getProductId());
        assertEquals(1L, r1.getUserId());
        assertEquals(5, r1.getRating());
        assertEquals("Outstanding product!", r1.getComment());
        assertEquals(now, r1.getCreatedAt());

        assertEquals(r1, r2);
        assertEquals(r1.hashCode(), r2.hashCode());
        assertNotEquals(r1, r3);
        assertTrue(r1.toString().contains("Outstanding product!"));
    }
}
