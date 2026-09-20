package com.rohith.rohithmart.service;

import com.rohith.rohithmart.dto.CartItemDTO;
import com.rohith.rohithmart.exception.AppException;

import java.math.BigDecimal;
import java.util.List;

/**
 * Service interface for shopping cart operations.
 */
public interface CartService {

    /**
     * Retrieves cart items for a buyer with product details and subtotals.
     *
     * @param userId buyer user ID
     * @return list of CartItemDTOs
     * @throws AppException on failure
     */
    List<CartItemDTO> getCartItems(Long userId) throws AppException;

    /**
     * Adds an item to the buyer's cart or increments quantity if already present.
     * Validates stock quantity against product inventory.
     *
     * @param userId buyer user ID
     * @param productId product ID
     * @param quantity requested quantity
     * @throws AppException if product not found or insufficient stock
     */
    void addToCart(Long userId, Long productId, int quantity) throws AppException;

    /**
     * Updates the quantity of an item in the cart.
     *
     * @param userId buyer user ID
     * @param cartItemId cart item ID
     * @param quantity updated quantity
     * @throws AppException if item not found or exceeds stock
     */
    void updateQuantity(Long userId, Long cartItemId, int quantity) throws AppException;

    /**
     * Removes an item from the cart.
     *
     * @param userId buyer user ID
     * @param cartItemId cart item ID
     * @throws AppException on failure
     */
    void removeFromCart(Long userId, Long cartItemId) throws AppException;

    /**
     * Clears all items from the buyer's cart.
     *
     * @param userId buyer user ID
     * @throws AppException on failure
     */
    void clearCart(Long userId) throws AppException;

    /**
     * Calculates the total sum for a list of cart items on the server.
     * Never trusts client-provided totals.
     *
     * @param items list of cart items
     * @return total monetary amount
     */
    BigDecimal calculateTotal(List<CartItemDTO> items);
}
