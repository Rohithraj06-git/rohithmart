package com.rohith.rohithmart.controller;

import com.google.gson.JsonObject;
import com.rohith.rohithmart.dto.OrderDTO;
import com.rohith.rohithmart.dto.ProductDTO;
import com.rohith.rohithmart.exception.AuthorizationException;
import com.rohith.rohithmart.exception.ValidationException;
import com.rohith.rohithmart.model.OrderStatus;
import com.rohith.rohithmart.model.Role;
import com.rohith.rohithmart.model.User;
import com.rohith.rohithmart.service.OrderService;
import com.rohith.rohithmart.service.ProductService;
import com.rohith.rohithmart.service.ServiceFactory;
import com.rohith.rohithmart.util.ValidationUtil;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * REST API Controller for seller management operations:
 * - GET  /api/seller/products           (list products owned by seller)
 * - GET  /api/seller/orders             (list orders containing seller items)
 * - POST /api/seller/orders/{id}/status (update status of an order item/order)
 * - PUT  /api/seller/orders/{id}/status (update status of an order item/order)
 */
@WebServlet(name = "SellerApiServlet", urlPatterns = {"/api/seller/*"})
public class SellerApiServlet extends BaseServlet {

    private final ProductService productService;
    private final OrderService orderService;

    public SellerApiServlet() {
        this(ServiceFactory.getProductService(), ServiceFactory.getOrderService());
    }

    public SellerApiServlet(ProductService productService, OrderService orderService) {
        this.productService = productService;
        this.orderService = orderService;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        try {
            User user = requireSellerUser(req);
            String pathInfo = req.getPathInfo();

            if ("/products".equals(pathInfo)) {
                List<ProductDTO> products = productService.getProductsBySeller(user.getId());
                sendJsonResponse(res, HttpServletResponse.SC_OK, products);
            } else if ("/orders".equals(pathInfo)) {
                List<OrderDTO> orders = orderService.getSellerOrders(user.getId());
                sendJsonResponse(res, HttpServletResponse.SC_OK, orders);
            } else {
                sendJsonError(res, HttpServletResponse.SC_NOT_FOUND, "NOT_FOUND", "Endpoint not found: " + pathInfo);
            }
        } catch (Exception e) {
            try {
                handleException(req, res, e);
            } catch (Exception ex) {
                logger.error("Unhandled exception in SellerApiServlet doGet", ex);
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        handleStatusUpdate(req, res);
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse res) throws IOException {
        handleStatusUpdate(req, res);
    }

    private void handleStatusUpdate(HttpServletRequest req, HttpServletResponse res) throws IOException {
        try {
            User user = requireSellerUser(req);
            String pathInfo = req.getPathInfo();

            if (pathInfo != null && pathInfo.contains("/orders/") && pathInfo.endsWith("/status")) {
                Long orderId = parseOrderIdFromStatusPath(pathInfo);
                String statusStr = null;

                if (req.getContentType() != null && req.getContentType().contains("application/json")) {
                    JsonObject json = parseJson(req, JsonObject.class);
                    if (json != null && json.has("status")) {
                        statusStr = json.get("status").getAsString();
                    }
                } else {
                    statusStr = req.getParameter("status");
                }

                if (!ValidationUtil.isNotEmpty(statusStr)) {
                    throw new ValidationException("Order status is required", "status", "status cannot be blank");
                }

                OrderStatus newStatus;
                try {
                    newStatus = OrderStatus.valueOf(statusStr.trim().toUpperCase());
                } catch (IllegalArgumentException e) {
                    throw new ValidationException("Invalid order status: " + statusStr, "status", "Invalid status value");
                }

                orderService.updateOrderStatus(orderId, newStatus, user.getId(), user.getRole() == Role.ADMIN);
                sendJsonResponse(res, HttpServletResponse.SC_OK, "Order status updated successfully", null);
            } else {
                sendJsonError(res, HttpServletResponse.SC_NOT_FOUND, "NOT_FOUND", "Endpoint not found: " + pathInfo);
            }
        } catch (Exception e) {
            try {
                handleException(req, res, e);
            } catch (Exception ex) {
                logger.error("Unhandled exception in SellerApiServlet status update", ex);
            }
        }
    }

    private User requireSellerUser(HttpServletRequest req) throws Exception {
        User user = requireAuthenticatedUser(req);
        if (user.getRole() != Role.SELLER && user.getRole() != Role.ADMIN) {
            throw new AuthorizationException("Only sellers and administrators can access seller endpoints");
        }
        return user;
    }

    private Long parseOrderIdFromStatusPath(String pathInfo) throws ValidationException {
        // e.g. /orders/12/status
        String prefix = "/orders/";
        int start = pathInfo.indexOf(prefix);
        if (start == -1) throw new ValidationException("Invalid order status URL");
        String after = pathInfo.substring(start + prefix.length());
        int end = after.indexOf("/status");
        if (end == -1) throw new ValidationException("Invalid order status URL");
        String idStr = after.substring(0, end);
        return ValidationUtil.validateId(idStr, "orderId");
    }
}
