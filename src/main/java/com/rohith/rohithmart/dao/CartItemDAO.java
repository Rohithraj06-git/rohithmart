package com.rohith.rohithmart.dao;

import com.rohith.rohithmart.model.CartItem;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object interface for user cart items.
 */
public interface CartItemDAO {

    /**
     * Retrieves all items in a user's cart.
     *
     * @param userId user ID
     * @return list of cart items
     * @throws SQLException on database failure
     */
    List<CartItem> findByUserId(Long userId) throws SQLException;

    /**
     * Finds a specific cart item for a user and product.
     *
     * @param userId user ID
     * @param productId product ID
     * @return Optional containing cart item if found
     * @throws SQLException on database failure
     */
    Optional<CartItem> findByUserAndProduct(Long userId, Long productId) throws SQLException;

    /**
     * Adds an item to the cart.
     *
     * @param item cart item
     * @return created item
     * @throws SQLException on database failure
     */
    CartItem create(CartItem item) throws SQLException;

    /**
     * Updates the quantity of a cart item.
     *
     * @param cartItemId cart item ID
     * @param quantity new quantity
     * @return true if updated
     * @throws SQLException on database failure
     */
    boolean updateQuantity(Long cartItemId, int quantity) throws SQLException;

    /**
     * Removes an item from the cart.
     *
     * @param cartItemId cart item ID
     * @return true if deleted
     * @throws SQLException on database failure
     */
    boolean delete(Long cartItemId) throws SQLException;

    /**
     * Finds a cart item by its unique ID.
     *
     * @param cartItemId cart item ID
     * @return Optional containing cart item if found
     * @throws SQLException on database failure
     */
    Optional<CartItem> findById(Long cartItemId) throws SQLException;

    /**
     * Clears all items from a user's cart.
     *
     * @param userId user ID
     * @return number of deleted items
     * @throws SQLException on database failure
     */
    int clearCart(Long userId) throws SQLException;

    /**
     * Clears all items from a user's cart within a transaction.
     *
     * @param conn transaction connection
     * @param userId user ID
     * @return number of deleted items
     * @throws SQLException on database failure
     */
    int clearCart(Connection conn, Long userId) throws SQLException;
}
