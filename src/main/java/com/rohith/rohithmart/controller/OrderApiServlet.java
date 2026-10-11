package com.rohith.rohithmart.controller;

import com.rohith.rohithmart.dto.OrderDTO;
import com.rohith.rohithmart.exception.ValidationException;
import com.rohith.rohithmart.model.Role;
import com.rohith.rohithmart.model.User;
import com.rohith.rohithmart.service.OrderService;
import com.rohith.rohithmart.service.ServiceFactory;
import com.rohith.rohithmart.util.ValidationUtil;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * REST API Controller for order queries, transactional checkout, and cancellations.
 *
 * Endpoints:
 * - GET    /api/orders                    (list current buyer's order history)
 * - GET    /api/orders/{id}               (get order details by ID)
 * - POST   /api/orders                    (execute transactional checkout from cart)
 * - POST   /api/orders/checkout           (execute transactional checkout from cart)
 * - POST   /api/orders/{id}/cancel        (cancel a pending order)
 */
@WebServlet(name = "OrderApiServlet", urlPatterns = {"/api/orders", "/api/orders/*"})
public class OrderApiServlet extends BaseServlet {

    private final OrderService orderService;

    public OrderApiServlet() {
        this(ServiceFactory.getOrderService());
    }

    public OrderApiServlet(OrderService orderService) {
        this.orderService = orderService;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        try {
            User user = requireAuthenticatedUser(req);
            Long orderId = parseIdFromPath(req.getPathInfo());

            if (orderId != null) {
                // GET /api/orders/{id}
                OrderDTO order = orderService.getOrderById(orderId, user.getId(), user.getRole() == Role.ADMIN);
                sendJsonResponse(res, HttpServletResponse.SC_OK, order);
            } else {
                // GET /api/orders
                List<OrderDTO> orders = orderService.getBuyerOrders(user.getId());
                sendJsonResponse(res, HttpServletResponse.SC_OK, orders);
            }
        } catch (Exception e) {
            try {
                handleException(req, res, e);
            } catch (Exception ex) {
                logger.error("Unhandled exception in OrderApiServlet doGet", ex);
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        try {
            User user = requireAuthenticatedUser(req);
            String pathInfo = req.getPathInfo();

            if (pathInfo == null || "/".equals(pathInfo) || "/checkout".equals(pathInfo)) {
                // POST /api/orders or /api/orders/checkout -> execute atomic checkout
                OrderDTO createdOrder = orderService.checkout(user.getId());
                sendJsonResponse(res, HttpServletResponse.SC_CREATED, "Order placed successfully", createdOrder);
            } else if (pathInfo.endsWith("/cancel")) {
                // POST /api/orders/{id}/cancel
                Long orderId = parseCancelIdFromPath(pathInfo);
                orderService.cancelOrder(orderId, user.getId());
                sendJsonResponse(res, HttpServletResponse.SC_OK, "Order cancelled successfully", null);
            } else {
                sendJsonError(res, HttpServletResponse.SC_NOT_FOUND, "NOT_FOUND", "Unknown order action: " + pathInfo);
            }
        } catch (Exception e) {
            try {
                handleException(req, res, e);
            } catch (Exception ex) {
                logger.error("Unhandled exception in OrderApiServlet doPost", ex);
            }
        }
    }

    private Long parseIdFromPath(String pathInfo) throws ValidationException {
        if (pathInfo == null || pathInfo.equals("/") || pathInfo.isEmpty()) {
            return null;
        }
        String clean = pathInfo.startsWith("/") ? pathInfo.substring(1) : pathInfo;
        if (clean.equals("checkout")) {
            return null;
        }
        if (clean.contains("/")) {
            clean = clean.substring(0, clean.indexOf("/"));
        }
        return ValidationUtil.validateId(clean, "orderId");
    }

    private Long parseCancelIdFromPath(String pathInfo) throws ValidationException {
        // format: /{id}/cancel
        String clean = pathInfo.startsWith("/") ? pathInfo.substring(1) : pathInfo;
        if (clean.endsWith("/cancel")) {
            clean = clean.substring(0, clean.length() - "/cancel".length());
        }
        return ValidationUtil.validateId(clean, "orderId");
    }
}
