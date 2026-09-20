package com.rohith.rohithmart.dao;

import com.rohith.rohithmart.model.OrderItem;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object interface for Order Items.
 */
public interface OrderItemDAO {

    /**
     * Persists an individual order line item within a transaction.
     *
     * @param conn transaction database connection
     * @param item order line item
     * @return created item with generated ID
     * @throws SQLException on database failure
     */
    OrderItem create(Connection conn, OrderItem item) throws SQLException;

    /**
     * Persists an individual order line item.
     *
     * @param item order line item
     * @return created item with generated ID
     * @throws SQLException on database failure
     */
    OrderItem create(OrderItem item) throws SQLException;

    /**
     * Persists multiple order items in a single JDBC batch operation within a transaction.
     *
     * @param conn transaction connection
     * @param items list of items to persist
     * @throws SQLException on database failure
     */
    void createBatch(Connection conn, List<OrderItem> items) throws SQLException;

    /**
     * Finds an order item by its unique ID.
     *
     * @param id order item ID
     * @return Optional containing item if found
     * @throws SQLException on database failure
     */
    Optional<OrderItem> findById(Long id) throws SQLException;

    /**
     * Retrieves all items belonging to a given order ID.
     *
     * @param orderId order ID
     * @return list of order items
     * @throws SQLException on database failure
     */
    List<OrderItem> findByOrderId(Long orderId) throws SQLException;

    /**
     * Retrieves all order items belonging to a given order ID using an existing connection.
     *
     * @param conn database connection
     * @param orderId order ID
     * @return list of order items
     * @throws SQLException on database failure
     */
    List<OrderItem> findByOrderId(Connection conn, Long orderId) throws SQLException;

    /**
     * Retrieves all items ordered for a given seller across orders.
     *
     * @param sellerId seller user ID
     * @return list of order items
     * @throws SQLException on database failure
     */
    List<OrderItem> findBySellerId(Long sellerId) throws SQLException;
}
