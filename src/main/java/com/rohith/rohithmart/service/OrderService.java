package com.rohith.rohithmart.service;

import com.rohith.rohithmart.dto.OrderDTO;
import com.rohith.rohithmart.exception.AppException;
import com.rohith.rohithmart.model.OrderStatus;

import java.util.List;

/**
 * Service interface for order operations, transactions, and status workflows.
 */
public interface OrderService {

    /**
     * Executes atomic checkout transaction:
     * Validates cart, stock, creates order header, order items, deducts stock, and clears cart.
     *
     * @param userId buyer user ID
     * @return created OrderDTO
     * @throws AppException if cart empty or stock insufficient
     */
    OrderDTO checkout(Long userId) throws AppException;

    /**
     * Retrieves an order by ID.
     *
     * @param orderId order ID
     * @param userId requesting user ID
     * @param isAdmin admin privilege flag
     * @return OrderDTO
     * @throws AppException if not found or unauthorized
     */
    OrderDTO getOrderById(Long orderId, Long userId, boolean isAdmin) throws AppException;

    /**
     * Retrieves all orders placed by a buyer.
     *
     * @param buyerId buyer user ID
     * @return list of OrderDTOs
     * @throws AppException on failure
     */
    List<OrderDTO> getBuyerOrders(Long buyerId) throws AppException;

    /**
     * Updates status of an order.
     *
     * @param orderId order ID
     * @param newStatus target status
     * @throws AppException on failure
     */
    void updateOrderStatus(Long orderId, OrderStatus newStatus) throws AppException;

    /**
     * Cancels a pending order.
     *
     * @param orderId order ID
     * @param userId buyer user ID
     * @throws AppException if order not found or not in PENDING status
     */
    void cancelOrder(Long orderId, Long userId) throws AppException;

    /**
     * Retrieves all platform orders for administrator view.
     *
     * @return list of all OrderDTOs
     * @throws AppException on failure
     */
    List<OrderDTO> getAllOrders() throws AppException;

    /**
     * Total order count.
     *
     * @return order count
     * @throws AppException on failure
     */
    int getTotalOrderCount() throws AppException;
}
