package com.rohith.rohithmart.dao;

import com.rohith.rohithmart.model.Order;
import com.rohith.rohithmart.model.OrderItem;
import com.rohith.rohithmart.model.OrderStatus;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object interface for orders and order items.
 */
public interface OrderDAO {

    /**
     * Persists an order header within a transactional connection.
     *
     * @param conn transaction connection
     * @param order order to insert
     * @return created order with generated ID
     * @throws SQLException on database failure
     */
    Order createOrder(Connection conn, Order order) throws SQLException;

    /**
     * Persists an individual order item within a transactional connection.
     *
     * @param conn transaction connection
     * @param item item to insert
     * @return created item with generated ID
     * @throws SQLException on database failure
     */
    OrderItem createOrderItem(Connection conn, OrderItem item) throws SQLException;

    /**
     * Finds an order by ID.
     *
     * @param id order ID
     * @return Optional containing order if found
     * @throws SQLException on database failure
     */
    Optional<Order> findById(Long id) throws SQLException;

    /**
     * Finds all items for a given order ID.
     *
     * @param orderId order ID
     * @return list of items
     * @throws SQLException on database failure
     */
    List<OrderItem> findItemsByOrderId(Long orderId) throws SQLException;

    /**
     * Retrieves all orders placed by a specific buyer.
     *
     * @param buyerId buyer ID
     * @return list of orders
     * @throws SQLException on database failure
     */
    List<Order> findByBuyerId(Long buyerId) throws SQLException;

    /**
     * Retrieves all orders containing items listed by a specific seller.
     *
     * @param sellerId seller's user ID
     * @return list of orders
     * @throws SQLException on database failure
     */
    List<Order> findOrdersBySellerId(Long sellerId) throws SQLException;

    /**
     * Updates the status of an order.
     *
     * @param orderId order ID
     * @param status new status
     * @return true if updated
     * @throws SQLException on database failure
     */
    boolean updateStatus(Long orderId, OrderStatus status) throws SQLException;

    /**
     * Cancels an order if it is in an allowed cancellable state (PENDING or CONFIRMED).
     *
     * @param orderId order ID
     * @return true if cancelled successfully, false otherwise
     * @throws SQLException on database failure
     */
    boolean cancelOrder(Long orderId) throws SQLException;

    /**
     * Retrieves all platform orders for administrator oversight.
     *
     * @return list of all orders
     * @throws SQLException on database failure
     */
    List<Order> findAll() throws SQLException;

    /**
     * Counts total number of orders on the platform.
     *
     * @return order count
     * @throws SQLException on database failure
     */
    int countAll() throws SQLException;
}
