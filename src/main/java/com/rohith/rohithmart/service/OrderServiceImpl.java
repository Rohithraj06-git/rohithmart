package com.rohith.rohithmart.service;

import com.rohith.rohithmart.dao.CartItemDAO;
import com.rohith.rohithmart.dao.DAOFactory;
import com.rohith.rohithmart.dao.OrderDAO;
import com.rohith.rohithmart.dao.ProductDAO;
import com.rohith.rohithmart.dto.OrderDTO;
import com.rohith.rohithmart.dto.OrderItemDTO;
import com.rohith.rohithmart.exception.AppException;
import com.rohith.rohithmart.exception.ForbiddenException;
import com.rohith.rohithmart.exception.ResourceNotFoundException;
import com.rohith.rohithmart.exception.ValidationException;
import com.rohith.rohithmart.model.CartItem;
import com.rohith.rohithmart.model.Order;
import com.rohith.rohithmart.model.OrderItem;
import com.rohith.rohithmart.model.OrderStatus;
import com.rohith.rohithmart.model.Product;
import com.rohith.rohithmart.util.DatabaseUtil;
import com.rohith.rohithmart.util.ValidationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of OrderService with strict transaction isolation and stock consistency.
 */
public class OrderServiceImpl implements OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderServiceImpl.class);
    private final OrderDAO orderDAO;
    private final ProductDAO productDAO;
    private final CartItemDAO cartItemDAO;

    public OrderServiceImpl() {
        this(DAOFactory.getOrderDAO(), DAOFactory.getProductDAO(), DAOFactory.getCartItemDAO());
    }

    public OrderServiceImpl(OrderDAO orderDAO, ProductDAO productDAO, CartItemDAO cartItemDAO) {
        this.orderDAO = orderDAO;
        this.productDAO = productDAO;
        this.cartItemDAO = cartItemDAO;
    }

    @Override
    public OrderDTO checkout(Long userId) throws AppException {
        if (userId == null || userId <= 0) {
            throw new ValidationException("User ID is required for checkout");
        }

        try {
            List<CartItem> cartItems = cartItemDAO.findByUserId(userId);
            if (cartItems == null || cartItems.isEmpty()) {
                throw new ValidationException("Cannot checkout with an empty cart");
            }

            // Calculate total and pre-validate inventory
            BigDecimal total = BigDecimal.ZERO;
            List<Product> products = new ArrayList<>(cartItems.size());
            for (CartItem ci : cartItems) {
                Optional<Product> pOpt = productDAO.findById(ci.getProductId());
                if (pOpt.isEmpty()) {
                    throw new ValidationException("Product with ID " + ci.getProductId() + " is no longer available");
                }
                Product p = pOpt.get();
                if (p.getStockQty() < ci.getQuantity()) {
                    throw new ValidationException("Insufficient stock for product: " + p.getName() + " (Available: " + p.getStockQty() + ")");
                }
                products.add(p);
                total = total.add(p.getPrice().multiply(BigDecimal.valueOf(ci.getQuantity())));
            }

            // Execute Checkout Transaction
            try (Connection conn = DatabaseUtil.getConnection()) {
                conn.setAutoCommit(false);
                try {
                    // 1. Create Order
                    Order order = new Order();
                    order.setBuyerId(userId);
                    order.setStatus(OrderStatus.CONFIRMED);
                    order.setTotalAmount(total);
                    Order createdOrder = orderDAO.createOrder(conn, order);

                    // 2. Create Order Items and Deduct Stock
                    List<OrderItemDTO> itemDTOs = new ArrayList<>();
                    for (int i = 0; i < cartItems.size(); i++) {
                        CartItem ci = cartItems.get(i);
                        Product p = products.get(i);

                        OrderItem oi = new OrderItem();
                        oi.setOrderId(createdOrder.getId());
                        oi.setProductId(p.getId());
                        oi.setQuantity(ci.getQuantity());
                        oi.setUnitPrice(p.getPrice());
                        OrderItem createdItem = orderDAO.createOrderItem(conn, oi);

                        boolean deducted = productDAO.deductStock(conn, p.getId(), ci.getQuantity());
                        if (!deducted) {
                            throw new ValidationException("Failed to reserve stock for: " + p.getName());
                        }

                        OrderItemDTO itemDTO = new OrderItemDTO();
                        itemDTO.setId(createdItem.getId());
                        itemDTO.setProductId(p.getId());
                        itemDTO.setProductName(p.getName());
                        itemDTO.setQuantity(ci.getQuantity());
                        itemDTO.setUnitPrice(p.getPrice());
                        itemDTO.setSubtotal(p.getPrice().multiply(BigDecimal.valueOf(ci.getQuantity())));
                        itemDTOs.add(itemDTO);
                    }

                    // 3. Clear Cart
                    cartItemDAO.clearCart(conn, userId);

                    // Commit Transaction
                    conn.commit();
                    logger.info("Order placed successfully with ID: {} for user: {}", createdOrder.getId(), userId);

                    OrderDTO dto = new OrderDTO();
                    dto.setId(createdOrder.getId());
                    dto.setBuyerId(createdOrder.getBuyerId());
                    dto.setStatus(createdOrder.getStatus());
                    dto.setTotalAmount(createdOrder.getTotalAmount());
                    dto.setCreatedAt(createdOrder.getCreatedAt());
                    dto.setItems(itemDTOs);
                    return dto;

                } catch (Exception e) {
                    conn.rollback();
                    logger.error("Transaction rolled back during checkout for user: {}", userId, e);
                    if (e instanceof AppException) {
                        throw (AppException) e;
                    }
                    throw new AppException("Failed to complete checkout transaction", e, 500);
                } finally {
                    conn.setAutoCommit(true);
                }
            }

        } catch (SQLException e) {
            logger.error("Database connection failure during checkout", e);
            throw new AppException("Checkout failed due to internal error", e, 500);
        }
    }

    @Override
    public OrderDTO getOrderById(Long orderId, Long userId, boolean isAdmin) throws AppException {
        try {
            Optional<Order> opt = orderDAO.findById(orderId);
            if (opt.isEmpty()) {
                throw new ResourceNotFoundException("Order not found with ID: " + orderId);
            }
            Order order = opt.get();
            if (!isAdmin && !order.getBuyerId().equals(userId)) {
                throw new ForbiddenException("You are not authorized to view this order");
            }
            return buildOrderDTO(order);
        } catch (SQLException e) {
            logger.error("Error retrieving order ID {}", orderId, e);
            throw new AppException("Failed to load order", e, 500);
        }
    }

    @Override
    public List<OrderDTO> getBuyerOrders(Long buyerId) throws AppException {
        try {
            List<Order> list = orderDAO.findByBuyerId(buyerId);
            List<OrderDTO> dtos = new ArrayList<>(list.size());
            for (Order o : list) {
                dtos.add(buildOrderDTO(o));
            }
            return dtos;
        } catch (SQLException e) {
            logger.error("Error fetching buyer orders for {}", buyerId, e);
            throw new AppException("Failed to load orders", e, 500);
        }
    }

    @Override
    public List<OrderDTO> getSellerOrders(Long sellerId) throws AppException {
        ValidationUtil.validateId(sellerId, "sellerId");
        try {
            List<Order> list = orderDAO.findOrdersBySellerId(sellerId);
            List<OrderDTO> dtos = new ArrayList<>(list.size());
            for (Order o : list) {
                dtos.add(buildOrderDTO(o));
            }
            return dtos;
        } catch (SQLException e) {
            logger.error("Error fetching seller orders for seller ID {}", sellerId, e);
            throw new AppException("Failed to load seller orders", e, 500);
        }
    }

    @Override
    public void updateOrderStatus(Long orderId, OrderStatus newStatus) throws AppException {
        ValidationUtil.validateId(orderId, "orderId");
        ValidationUtil.validateNotNull(newStatus, "newStatus");

        try {
            Optional<Order> opt = orderDAO.findById(orderId);
            if (opt.isEmpty()) {
                throw new ResourceNotFoundException("Order not found with ID: " + orderId);
            }
            Order current = opt.get();
            if (current.getStatus() == OrderStatus.DELIVERED) {
                throw new com.rohith.rohithmart.exception.OrderException("Cannot change status of a delivered order");
            }
            if (current.getStatus() == OrderStatus.CANCELLED) {
                throw new com.rohith.rohithmart.exception.OrderException("Cannot change status of a cancelled order");
            }
            if (current.getStatus() == newStatus) {
                return;
            }

            orderDAO.updateStatus(orderId, newStatus);
            logger.info("Order ID {} status updated from {} to {}", orderId, current.getStatus(), newStatus);
        } catch (SQLException e) {
            logger.error("Failed to update status for order ID {}", orderId, e);
            throw new AppException("Status update failed", e, 500);
        }
    }

    @Override
    public void cancelOrder(Long orderId, Long userId) throws AppException {
        ValidationUtil.validateId(orderId, "orderId");
        ValidationUtil.validateId(userId, "userId");

        try {
            Optional<Order> opt = orderDAO.findById(orderId);
            if (opt.isEmpty()) {
                throw new ResourceNotFoundException("Order not found with ID: " + orderId);
            }
            Order o = opt.get();
            if (!o.getBuyerId().equals(userId)) {
                throw new ForbiddenException("You cannot cancel another user's order");
            }
            if (o.getStatus() == OrderStatus.SHIPPED || o.getStatus() == OrderStatus.DELIVERED) {
                throw new com.rohith.rohithmart.exception.OrderException("Cannot cancel an order that has already been " + o.getStatus().name().toLowerCase());
            }
            if (o.getStatus() == OrderStatus.CANCELLED) {
                return;
            }

            orderDAO.cancelOrder(orderId);
            logger.info("Order ID {} cancelled by buyer ID {}", orderId, userId);
        } catch (SQLException e) {
            logger.error("Failed to cancel order ID {}", orderId, e);
            throw new AppException("Cancellation failed", e, 500);
        }
    }

    @Override
    public List<OrderDTO> getAllOrders() throws AppException {
        try {
            List<Order> list = orderDAO.findAll();
            List<OrderDTO> dtos = new ArrayList<>(list.size());
            for (Order o : list) {
                dtos.add(buildOrderDTO(o));
            }
            return dtos;
        } catch (SQLException e) {
            logger.error("Error fetching all orders", e);
            throw new AppException("Failed to retrieve platform orders", e, 500);
        }
    }

    @Override
    public int getTotalOrderCount() throws AppException {
        try {
            return orderDAO.countAll();
        } catch (SQLException e) {
            logger.error("Failed to count orders", e);
            throw new AppException("Failed to count orders", e, 500);
        }
    }

    private OrderDTO buildOrderDTO(Order order) throws SQLException {
        OrderDTO dto = new OrderDTO();
        dto.setId(order.getId());
        dto.setBuyerId(order.getBuyerId());
        dto.setStatus(order.getStatus());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setCreatedAt(order.getCreatedAt());

        List<OrderItem> items = orderDAO.findItemsByOrderId(order.getId());
        List<OrderItemDTO> itemDTOs = new ArrayList<>(items.size());
        for (OrderItem oi : items) {
            OrderItemDTO idto = new OrderItemDTO();
            idto.setId(oi.getId());
            idto.setProductId(oi.getProductId());
            Optional<Product> pOpt = productDAO.findById(oi.getProductId());
            idto.setProductName(pOpt.map(Product::getName).orElse("Product #" + oi.getProductId()));
            idto.setQuantity(oi.getQuantity());
            idto.setUnitPrice(oi.getUnitPrice());
            idto.setSubtotal(oi.getUnitPrice().multiply(BigDecimal.valueOf(oi.getQuantity())));
            itemDTOs.add(idto);
        }
        dto.setItems(itemDTOs);
        return dto;
    }
}
