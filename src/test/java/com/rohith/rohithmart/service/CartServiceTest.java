package com.rohith.rohithmart.service;

import com.rohith.rohithmart.dao.CartItemDAO;
import com.rohith.rohithmart.dao.ProductDAO;
import com.rohith.rohithmart.dto.CartItemDTO;
import com.rohith.rohithmart.exception.ForbiddenException;
import com.rohith.rohithmart.exception.InsufficientStockException;
import com.rohith.rohithmart.exception.ResourceNotFoundException;
import com.rohith.rohithmart.exception.ValidationException;
import com.rohith.rohithmart.model.CartItem;
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
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CartService Unit Tests (Mockito)")
class CartServiceTest {

    @Mock
    private CartItemDAO cartItemDAO;

    @Mock
    private ProductDAO productDAO;

    private CartService cartService;

    @BeforeEach
    void setUp() {
        cartService = new CartServiceImpl(cartItemDAO, productDAO);
    }

    @Test
    @DisplayName("addToCart: success when product exists and stock sufficient")
    void testAddToCartSuccess() throws Exception {
        Product p = new Product(10L, 2L, "USB Cable", "Type-C", new BigDecimal("9.99"), 20, "Accessories", null, null);
        when(productDAO.findById(10L)).thenReturn(Optional.of(p));
        when(cartItemDAO.findByUserAndProduct(1L, 10L)).thenReturn(Optional.empty());

        cartService.addToCart(1L, 10L, 2);
        verify(cartItemDAO).create(any(CartItem.class));
    }

    @Test
    @DisplayName("addToCart: fails when product not found")
    void testAddToCartProductNotFound() throws Exception {
        when(productDAO.findById(999L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> cartService.addToCart(1L, 999L, 1));
    }

    @Test
    @DisplayName("addToCart: fails when requested quantity exceeds available stock")
    void testAddToCartExceedsStock() throws Exception {
        Product p = new Product(10L, 2L, "Limited Item", "Rare", new BigDecimal("100.00"), 3, "Collectibles", null, null);
        when(productDAO.findById(10L)).thenReturn(Optional.of(p));
        when(cartItemDAO.findByUserAndProduct(1L, 10L)).thenReturn(Optional.empty());

        assertThrows(ValidationException.class, () -> cartService.addToCart(1L, 10L, 5));
        verify(cartItemDAO, never()).create(any());
    }

    @Test
    @DisplayName("updateQuantity: prevents updating another user's cart item")
    void testUpdateQuantityOwnership() throws Exception {
        CartItem itemOfUser2 = new CartItem(50L, 2L, 10L, 1, Timestamp.from(Instant.now()));
        when(cartItemDAO.findById(50L)).thenReturn(Optional.of(itemOfUser2));

        // User 1 attempts to update User 2's cart item -> Forbidden
        assertThrows(ForbiddenException.class, () -> cartService.updateQuantity(1L, 50L, 3));
        verify(cartItemDAO, never()).updateQuantity(anyLong(), anyInt());
    }

    @Test
    @DisplayName("updateQuantity: throws InsufficientStockException if update exceeds available stock")
    void testUpdateQuantityStockCheck() throws Exception {
        CartItem item = new CartItem(50L, 1L, 10L, 1, Timestamp.from(Instant.now()));
        Product prod = new Product(10L, 2L, "Pen", "Gel", new BigDecimal("1.50"), 2, "Office", null, null);

        when(cartItemDAO.findById(50L)).thenReturn(Optional.of(item));
        when(productDAO.findById(10L)).thenReturn(Optional.of(prod));

        assertThrows(InsufficientStockException.class, () -> cartService.updateQuantity(1L, 50L, 10));
    }

    @Test
    @DisplayName("calculateTotal: strictly calculates on server and does not trust client totals")
    void testCalculateTotalOnServer() {
        CartItemDTO item1 = new CartItemDTO();
        item1.setUnitPrice(new BigDecimal("15.00"));
        item1.setQuantity(2);
        item1.setSubtotal(new BigDecimal("30.00"));

        CartItemDTO item2 = new CartItemDTO();
        item2.setUnitPrice(new BigDecimal("25.50"));
        item2.setQuantity(1);
        item2.setSubtotal(new BigDecimal("25.50"));

        BigDecimal total = cartService.calculateTotal(List.of(item1, item2));
        assertEquals(new BigDecimal("55.50"), total);
    }

    @Test
    @DisplayName("clearCart: clears items for given user")
    void testClearCart() throws Exception {
        cartService.clearCart(1L);
        verify(cartItemDAO).clearCart(1L);
    }
}
