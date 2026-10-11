package com.rohith.rohithmart.controller;

import com.google.gson.JsonObject;
import com.rohith.rohithmart.dto.CartItemDTO;
import com.rohith.rohithmart.dto.CartItemRequest;
import com.rohith.rohithmart.exception.ValidationException;
import com.rohith.rohithmart.model.User;
import com.rohith.rohithmart.service.CartService;
import com.rohith.rohithmart.service.ServiceFactory;
import com.rohith.rohithmart.util.ValidationUtil;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * REST API Controller for shopping cart operations.
 *
 * Endpoints:
 * - GET    /api/cart                  (view user cart, totals, and item count)
 * - POST   /api/cart/add              (add product to cart)
 * - POST   /api/cart/update           (update item quantity)
 * - POST   /api/cart/remove           (remove item from cart)
 * - DELETE /api/cart                  (clear cart)
 * - DELETE /api/cart/remove           (remove item from cart)
 * - PUT    /api/cart/update           (update item quantity)
 */
@WebServlet(name = "CartApiServlet", urlPatterns = {"/api/cart", "/api/cart/*"})
public class CartApiServlet extends BaseServlet {

    private final CartService cartService;

    public CartApiServlet() {
        this(ServiceFactory.getCartService());
    }

    public CartApiServlet(CartService cartService) {
        this.cartService = cartService;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        try {
            User user = requireAuthenticatedUser(req);
            List<CartItemDTO> items = cartService.getCartItems(user.getId());
            BigDecimal total = cartService.calculateTotal(items);

            Map<String, Object> cartData = new LinkedHashMap<>();
            cartData.put("items", items);
            cartData.put("total", total);
            cartData.put("itemCount", items.size());

            sendJsonResponse(res, HttpServletResponse.SC_OK, cartData);
        } catch (Exception e) {
            try {
                handleException(req, res, e);
            } catch (Exception ex) {
                logger.error("Unhandled exception in CartApiServlet doGet", ex);
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        try {
            User user = requireAuthenticatedUser(req);
            String pathInfo = req.getPathInfo();

            if (pathInfo == null || "/".equals(pathInfo) || "/add".equals(pathInfo)) {
                handleAdd(req, res, user);
            } else if ("/update".equals(pathInfo)) {
                handleUpdate(req, res, user);
            } else if ("/remove".equals(pathInfo)) {
                handleRemove(req, res, user);
            } else if ("/clear".equals(pathInfo)) {
                handleClear(req, res, user);
            } else {
                sendJsonError(res, HttpServletResponse.SC_NOT_FOUND, "NOT_FOUND", "Unknown cart action: " + pathInfo);
            }
        } catch (Exception e) {
            try {
                handleException(req, res, e);
            } catch (Exception ex) {
                logger.error("Unhandled exception in CartApiServlet doPost", ex);
            }
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse res) throws IOException {
        try {
            User user = requireAuthenticatedUser(req);
            handleUpdate(req, res, user);
        } catch (Exception e) {
            try {
                handleException(req, res, e);
            } catch (Exception ex) {
                logger.error("Unhandled exception in CartApiServlet doPut", ex);
            }
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse res) throws IOException {
        try {
            User user = requireAuthenticatedUser(req);
            String pathInfo = req.getPathInfo();

            if (pathInfo != null && ("/remove".equals(pathInfo) || pathInfo.length() > 1)) {
                handleRemove(req, res, user);
            } else {
                handleClear(req, res, user);
            }
        } catch (Exception e) {
            try {
                handleException(req, res, e);
            } catch (Exception ex) {
                logger.error("Unhandled exception in CartApiServlet doDelete", ex);
            }
        }
    }

    private void handleAdd(HttpServletRequest req, HttpServletResponse res, User user) throws Exception {
        Long productId = null;
        Integer quantity = 1;

        // Try reading JSON body if available
        if (req.getContentType() != null && req.getContentType().contains("application/json")) {
            JsonObject json = parseJson(req, JsonObject.class);
            if (json != null) {
                if (json.has("productId")) productId = json.get("productId").getAsLong();
                if (json.has("quantity")) quantity = json.get("quantity").getAsInt();
            }
        } else {
            String pIdStr = req.getParameter("productId");
            String qStr = req.getParameter("quantity");
            if (ValidationUtil.isNotEmpty(pIdStr)) {
                productId = Long.parseLong(pIdStr.trim());
            }
            if (ValidationUtil.isNotEmpty(qStr)) {
                quantity = Integer.parseInt(qStr.trim());
            }
        }

        if (productId == null) {
            throw new ValidationException("Product ID is required", "productId", "productId cannot be null");
        }

        cartService.addToCart(user.getId(), productId, quantity);
        sendJsonResponse(res, HttpServletResponse.SC_CREATED, "Product added to cart successfully", buildCartData(user.getId()));
    }

    private void handleUpdate(HttpServletRequest req, HttpServletResponse res, User user) throws Exception {
        Long cartItemId = null;
        Integer quantity = null;

        if (req.getContentType() != null && req.getContentType().contains("application/json")) {
            JsonObject json = parseJson(req, JsonObject.class);
            if (json != null) {
                if (json.has("cartItemId")) cartItemId = json.get("cartItemId").getAsLong();
                else if (json.has("id")) cartItemId = json.get("id").getAsLong();
                if (json.has("quantity")) quantity = json.get("quantity").getAsInt();
            }
        } else {
            String cIdStr = req.getParameter("cartItemId");
            if (cIdStr == null) cIdStr = req.getParameter("id");
            String qStr = req.getParameter("quantity");
            if (ValidationUtil.isNotEmpty(cIdStr)) {
                cartItemId = Long.parseLong(cIdStr.trim());
            }
            if (ValidationUtil.isNotEmpty(qStr)) {
                quantity = Integer.parseInt(qStr.trim());
            }
        }

        if (cartItemId == null) {
            // Check path info
            cartItemId = parseIdFromPath(req.getPathInfo());
        }

        if (cartItemId == null) {
            throw new ValidationException("Cart item ID is required", "cartItemId", "cartItemId cannot be null");
        }
        if (quantity == null) {
            throw new ValidationException("Quantity is required", "quantity", "quantity cannot be null");
        }

        cartService.updateQuantity(user.getId(), cartItemId, quantity);
        sendJsonResponse(res, HttpServletResponse.SC_OK, "Cart quantity updated successfully", buildCartData(user.getId()));
    }

    private void handleRemove(HttpServletRequest req, HttpServletResponse res, User user) throws Exception {
        Long cartItemId = null;

        if (req.getContentType() != null && req.getContentType().contains("application/json")) {
            JsonObject json = parseJson(req, JsonObject.class);
            if (json != null) {
                if (json.has("cartItemId")) cartItemId = json.get("cartItemId").getAsLong();
                else if (json.has("id")) cartItemId = json.get("id").getAsLong();
            }
        } else {
            String cIdStr = req.getParameter("cartItemId");
            if (cIdStr == null) cIdStr = req.getParameter("id");
            if (ValidationUtil.isNotEmpty(cIdStr)) {
                cartItemId = Long.parseLong(cIdStr.trim());
            }
        }

        if (cartItemId == null) {
            cartItemId = parseIdFromPath(req.getPathInfo());
        }

        if (cartItemId == null) {
            throw new ValidationException("Cart item ID is required to remove item", "cartItemId", "cartItemId cannot be null");
        }

        cartService.removeFromCart(user.getId(), cartItemId);
        sendJsonResponse(res, HttpServletResponse.SC_OK, "Cart item removed successfully", buildCartData(user.getId()));
    }

    private void handleClear(HttpServletRequest req, HttpServletResponse res, User user) throws Exception {
        cartService.clearCart(user.getId());
        sendJsonResponse(res, HttpServletResponse.SC_OK, "Cart cleared successfully", buildCartData(user.getId()));
    }

    private Map<String, Object> buildCartData(Long userId) throws Exception {
        List<CartItemDTO> items = cartService.getCartItems(userId);
        BigDecimal total = cartService.calculateTotal(items);
        Map<String, Object> cartData = new LinkedHashMap<>();
        cartData.put("items", items);
        cartData.put("total", total);
        cartData.put("itemCount", items.size());
        return cartData;
    }

    private Long parseIdFromPath(String pathInfo) {
        if (pathInfo == null || pathInfo.equals("/") || pathInfo.isEmpty()) {
            return null;
        }
        String clean = pathInfo.startsWith("/") ? pathInfo.substring(1) : pathInfo;
        if (clean.equals("update") || clean.equals("remove") || clean.equals("add") || clean.equals("clear")) {
            return null;
        }
        if (clean.contains("/")) {
            clean = clean.substring(0, clean.indexOf("/"));
        }
        try {
            return Long.parseLong(clean);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
