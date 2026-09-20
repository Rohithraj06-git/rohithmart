package com.rohith.rohithmart.service;

import com.rohith.rohithmart.dao.CartItemDAO;
import com.rohith.rohithmart.dao.OrderDAO;
import com.rohith.rohithmart.dao.ProductDAO;
import com.rohith.rohithmart.dto.OrderDTO;
import com.rohith.rohithmart.exception.ForbiddenException;
import com.rohith.rohithmart.exception.OrderException;
import com.rohith.rohithmart.exception.ResourceNotFoundException;
import com.rohith.rohithmart.exception.ValidationException;
import com.rohith.rohithmart.model.CartItem;
import com.rohith.rohithmart.model.Order;
import com.rohith.rohithmart.model.OrderStatus;
import com.rohith.rohithmart.model.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderService Unit Tests (Mockito)")
class OrderServiceTest {

    @Mock
    private OrderDAO orderDAO;

    @Mock
    private ProductDAO productDAO;

    @Mock
    private CartItemDAO cartItemDAO;

    private OrderService orderService;

    @BeforeEach
    void setUp() {
        orderService = new OrderServiceImpl(orderDAO, productDAO, cartItemDAO);
    }

    @Test
    @DisplayName("checkout: fails when cart is empty")
    void testCheckoutEmptyCart() throws Exception {
        when(cartItemDAO.findByUserId(1L)).thenReturn(Collections.emptyList());
        assertThrows(ValidationException.class, () -> orderService.checkout(1L));
        verify(orderDAO, never()).createOrder(any(), any());
    }

    @Test
    @DisplayName("checkout: fails when product is no longer available")
    void testCheckoutProductMissing() throws Exception {
        CartItem item = new CartItem(1L, 1L, 999L, 1, Timestamp.from(Instant.now()));
        when(cartItemDAO.findByUserId(1L)).thenReturn(List.of(item));
        when(productDAO.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ValidationException.class, () -> orderService.checkout(1L));
        verify(orderDAO, never()).createOrder(any(), any());
    }

    @Test
    @DisplayName("checkout: fails when product stock is insufficient")
    void testCheckoutInsufficientStock() throws Exception {
        CartItem item = new CartItem(1L, 1L, 10L, 5, Timestamp.from(Instant.now()));
        Product product = new Product(10L, 2L, "Monitor", "4K", new BigDecimal("300.00"), 2, "Electronics", null, null);

        when(cartItemDAO.findByUserId(1L)).thenReturn(List.of(item));
        when(productDAO.findById(10L)).thenReturn(Optional.of(product));

        assertThrows(ValidationException.class, () -> orderService.checkout(1L));
        verify(orderDAO, never()).createOrder(any(), any());
    }

    @Test
    @DisplayName("getOrderById: buyer cannot view another buyer's order")
    void testGetOrderByIdAuthorization() throws Exception {
        Order orderOfUser2 = new Order(100L, 2L, OrderStatus.CONFIRMED, new BigDecimal("50.00"), Timestamp.from(Instant.now()));
        when(orderDAO.findById(100L)).thenReturn(Optional.of(orderOfUser2));

        // User 1 cannot view order 100
        assertThrows(ForbiddenException.class, () -> orderService.getOrderById(100L, 1L, false));

        // Admin can view order 100
        when(orderDAO.findItemsByOrderId(100L)).thenReturn(Collections.emptyList());
        OrderDTO adminView = orderService.getOrderById(100L, 999L, true);
        assertNotNull(adminView);
        assertEquals(100L, adminView.getId());
    }

    @Test
    @DisplayName("getSellerOrders: retrieves orders relevant to seller")
    void testGetSellerOrders() throws Exception {
        Order o = new Order(200L, 1L, OrderStatus.CONFIRMED, new BigDecimal("120.00"), Timestamp.from(Instant.now()));
        when(orderDAO.findOrdersBySellerId(2L)).thenReturn(List.of(o));
        when(orderDAO.findItemsByOrderId(200L)).thenReturn(Collections.emptyList());

        List<OrderDTO> sellerOrders = orderService.getSellerOrders(2L);
        assertEquals(1, sellerOrders.size());
        assertEquals(200L, sellerOrders.get(0).getId());
    }

    @Test
    @DisplayName("updateOrderStatus: fails on delivered or cancelled orders")
    void testUpdateOrderStatusTerminalStates() throws Exception {
        Order delivered = new Order(300L, 1L, OrderStatus.DELIVERED, new BigDecimal("75.00"), Timestamp.from(Instant.now()));
        when(orderDAO.findById(300L)).thenReturn(Optional.of(delivered));

        assertThrows(OrderException.class, () -> orderService.updateOrderStatus(300L, OrderStatus.CANCELLED));
        verify(orderDAO, never()).updateStatus(anyLong(), any());

        Order cancelled = new Order(301L, 1L, OrderStatus.CANCELLED, new BigDecimal("75.00"), Timestamp.from(Instant.now()));
        when(orderDAO.findById(301L)).thenReturn(Optional.of(cancelled));

        assertThrows(OrderException.class, () -> orderService.updateOrderStatus(301L, OrderStatus.CONFIRMED));
    }

    @Test
    @DisplayName("cancelOrder: buyer cannot cancel other's order or already shipped/delivered order")
    void testCancelOrderRules() throws Exception {
        // 1. Cannot cancel other buyer's order
        Order order = new Order(400L, 2L, OrderStatus.PENDING, new BigDecimal("30.00"), Timestamp.from(Instant.now()));
        when(orderDAO.findById(400L)).thenReturn(Optional.of(order));

        assertThrows(ForbiddenException.class, () -> orderService.cancelOrder(400L, 1L));

        // 2. Cannot cancel SHIPPED order
        Order shipped = new Order(401L, 1L, OrderStatus.SHIPPED, new BigDecimal("30.00"), Timestamp.from(Instant.now()));
        when(orderDAO.findById(401L)).thenReturn(Optional.of(shipped));

        assertThrows(OrderException.class, () -> orderService.cancelOrder(401L, 1L));

        // 3. Successfully cancel PENDING order
        Order pending = new Order(402L, 1L, OrderStatus.PENDING, new BigDecimal("30.00"), Timestamp.from(Instant.now()));
        when(orderDAO.findById(402L)).thenReturn(Optional.of(pending));
        when(orderDAO.cancelOrder(402L)).thenReturn(true);

        assertDoesNotThrow(() -> orderService.cancelOrder(402L, 1L));
        verify(orderDAO).cancelOrder(402L);
    }
}
