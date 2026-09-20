package com.rohith.rohithmart.controller;

import com.rohith.rohithmart.dto.CartItemDTO;
import com.rohith.rohithmart.dto.OrderDTO;
import com.rohith.rohithmart.model.User;
import com.rohith.rohithmart.service.CartService;
import com.rohith.rohithmart.service.OrderService;
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
 * Controller managing checkout presentation and mock payment confirmation.
 */
@WebServlet(name = "CheckoutServlet", urlPatterns = {"/checkout", "/checkout/*"})
public class CheckoutServlet extends BaseServlet {

    private final CartService cartService = ServiceFactory.getCartService();
    private final OrderService orderService = ServiceFactory.getOrderService();

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
            if (items.isEmpty()) {
                res.sendRedirect(req.getContextPath() + "/cart");
                return;
            }

            BigDecimal total = cartService.calculateTotal(items);
            req.setAttribute("cartItems", items);
            req.setAttribute("cartTotal", total);
            forward(req, res, "buyer/checkout.jsp");
        } catch (Exception e) {
            handleException(req, res, e);
            res.sendRedirect(req.getContextPath() + "/cart");
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
            if ("/place".equals(pathInfo)) {
                OrderDTO order = orderService.checkout(user.getId());
                session.setAttribute("flashSuccess", "Order #" + order.getId() + " placed successfully!");
                res.sendRedirect(req.getContextPath() + "/buyer/orders");
            } else {
                res.sendRedirect(req.getContextPath() + "/checkout");
            }
        } catch (Exception e) {
            handleException(req, res, e);
            session.setAttribute("flashError", e.getMessage());
            res.sendRedirect(req.getContextPath() + "/checkout");
        }
    }
}
