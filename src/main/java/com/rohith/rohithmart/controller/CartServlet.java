package com.rohith.rohithmart.controller;

import com.rohith.rohithmart.dto.CartItemDTO;
import com.rohith.rohithmart.model.User;
import com.rohith.rohithmart.service.CartService;
import com.rohith.rohithmart.service.ServiceFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

/**
 * Controller for shopping cart views and modifications.
 */
@WebServlet(name = "CartServlet", urlPatterns = {"/cart", "/cart/*"})
public class CartServlet extends BaseServlet {

    private final CartService cartService = ServiceFactory.getCartService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;

        if (user == null) {
            res.sendRedirect(req.getContextPath() + "/auth/login");
            return;
        }

        try {
            List<CartItemDTO> items = cartService.getCartItems(user.getId());
            BigDecimal total = cartService.calculateTotal(items);

            req.setAttribute("cartItems", items);
            req.setAttribute("cartTotal", total);
            forward(req, res, "buyer/cart.jsp");
        } catch (Exception e) {
            handleException(req, res, e);
            forward(req, res, "buyer/cart.jsp");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;

        if (user == null) {
            res.sendRedirect(req.getContextPath() + "/auth/login");
            return;
        }

        String pathInfo = req.getPathInfo();
        try {
            if ("/add".equals(pathInfo)) {
                Long productId = Long.parseLong(req.getParameter("productId"));
                int quantity = Integer.parseInt(req.getParameter("quantity"));
                cartService.addToCart(user.getId(), productId, quantity);
                session.setAttribute("flashSuccess", "Product added to cart!");
            } else if ("/remove".equals(pathInfo)) {
                Long cartItemId = Long.parseLong(req.getParameter("cartItemId"));
                cartService.removeFromCart(user.getId(), cartItemId);
                session.setAttribute("flashSuccess", "Item removed from cart");
            }
            res.sendRedirect(req.getContextPath() + "/cart");
        } catch (Exception e) {
            handleException(req, res, e);
            res.sendRedirect(req.getContextPath() + "/cart");
        }
    }
}
