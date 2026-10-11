package com.rohith.rohithmart.controller;

import com.google.gson.JsonObject;
import com.rohith.rohithmart.dto.UserResponseDTO;
import com.rohith.rohithmart.exception.AuthorizationException;
import com.rohith.rohithmart.exception.ValidationException;
import com.rohith.rohithmart.model.Role;
import com.rohith.rohithmart.model.User;
import com.rohith.rohithmart.service.OrderService;
import com.rohith.rohithmart.service.ProductService;
import com.rohith.rohithmart.service.ServiceFactory;
import com.rohith.rohithmart.service.UserService;
import com.rohith.rohithmart.util.ValidationUtil;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * REST API Controller for administrator platform oversight:
 * - GET  /api/admin/users              (list all registered users)
 * - GET  /api/admin/stats              (platform statistics: users, listings, orders)
 * - POST /api/admin/users/{id}/role    (update user role)
 * - PUT  /api/admin/users/{id}/role    (update user role)
 */
@WebServlet(name = "AdminApiServlet", urlPatterns = {"/api/admin/*"})
public class AdminApiServlet extends BaseServlet {

    private final UserService userService;
    private final ProductService productService;
    private final OrderService orderService;

    public AdminApiServlet() {
        this(ServiceFactory.getUserService(), ServiceFactory.getProductService(), ServiceFactory.getOrderService());
    }

    public AdminApiServlet(UserService userService, ProductService productService, OrderService orderService) {
        this.userService = userService;
        this.productService = productService;
        this.orderService = orderService;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        try {
            requireAdminUser(req);
            String pathInfo = req.getPathInfo();

            if ("/users".equals(pathInfo)) {
                List<UserResponseDTO> users = userService.getAllUsers();
                sendJsonResponse(res, HttpServletResponse.SC_OK, users);
            } else if ("/stats".equals(pathInfo)) {
                Map<String, Object> stats = new LinkedHashMap<>();
                stats.put("totalUsers", userService.getTotalUserCount());
                stats.put("totalProducts", productService.getTotalProductCount());
                stats.put("totalOrders", orderService.getTotalOrderCount());
                sendJsonResponse(res, HttpServletResponse.SC_OK, stats);
            } else {
                sendJsonError(res, HttpServletResponse.SC_NOT_FOUND, "NOT_FOUND", "Endpoint not found: " + pathInfo);
            }
        } catch (Exception e) {
            try {
                handleException(req, res, e);
            } catch (Exception ex) {
                logger.error("Unhandled exception in AdminApiServlet doGet", ex);
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        handleRoleUpdate(req, res);
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse res) throws IOException {
        handleRoleUpdate(req, res);
    }

    private void handleRoleUpdate(HttpServletRequest req, HttpServletResponse res) throws IOException {
        try {
            User admin = requireAdminUser(req);
            String pathInfo = req.getPathInfo();

            if (pathInfo != null && pathInfo.contains("/users/") && pathInfo.endsWith("/role")) {
                Long targetUserId = parseUserIdFromRolePath(pathInfo);
                String roleStr = null;

                if (req.getContentType() != null && req.getContentType().contains("application/json")) {
                    JsonObject json = parseJson(req, JsonObject.class);
                    if (json != null && json.has("role")) {
                        roleStr = json.get("role").getAsString();
                    }
                } else {
                    roleStr = req.getParameter("role");
                }

                if (!ValidationUtil.isNotEmpty(roleStr)) {
                    throw new ValidationException("Role is required", "role", "role cannot be blank");
                }

                Role newRole;
                try {
                    newRole = Role.valueOf(roleStr.trim().toUpperCase());
                } catch (IllegalArgumentException e) {
                    throw new ValidationException("Invalid role: " + roleStr, "role", "Invalid role value");
                }

                UserResponseDTO updatedUser = userService.updateUserRole(admin.getId(), targetUserId, newRole);
                sendJsonResponse(res, HttpServletResponse.SC_OK, "User role updated successfully", updatedUser);
            } else {
                sendJsonError(res, HttpServletResponse.SC_NOT_FOUND, "NOT_FOUND", "Endpoint not found: " + pathInfo);
            }
        } catch (Exception e) {
            try {
                handleException(req, res, e);
            } catch (Exception ex) {
                logger.error("Unhandled exception in AdminApiServlet role update", ex);
            }
        }
    }

    private User requireAdminUser(HttpServletRequest req) throws Exception {
        User user = requireAuthenticatedUser(req);
        if (user.getRole() != Role.ADMIN) {
            throw new AuthorizationException("Administrator privileges required");
        }
        return user;
    }

    private Long parseUserIdFromRolePath(String pathInfo) throws ValidationException {
        // e.g. /users/12/role
        String prefix = "/users/";
        int start = pathInfo.indexOf(prefix);
        if (start == -1) throw new ValidationException("Invalid user role URL");
        String after = pathInfo.substring(start + prefix.length());
        int end = after.indexOf("/role");
        if (end == -1) throw new ValidationException("Invalid user role URL");
        String idStr = after.substring(0, end);
        return ValidationUtil.validateId(idStr, "userId");
    }
}
