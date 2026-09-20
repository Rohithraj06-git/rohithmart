package com.rohith.rohithmart.controller;

import com.rohith.rohithmart.dto.OrderDTO;
import com.rohith.rohithmart.model.User;
import com.rohith.rohithmart.service.OrderService;
import com.rohith.rohithmart.service.ServiceFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

/**
 * Controller for buyer order history and order cancellation.
 */
@WebServlet(name = "BuyerOrderServlet", urlPatterns = {"/buyer/orders", "/orders/cancel"})
public class BuyerOrderServlet extends BaseServlet {

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
            List<OrderDTO> orders = orderService.getBuyerOrders(user.getId());
            req.setAttribute("orders", orders);
            forward(req, res, "buyer/orders.jsp");
        } catch (Exception e) {
            handleException(req, res, e);
            forward(req, res, "buyer/orders.jsp");
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

        try {
            Long orderId = Long.parseLong(req.getParameter("orderId"));
            orderService.cancelOrder(orderId, user.getId());
            session.setAttribute("flashSuccess", "Order #" + orderId + " has been cancelled.");
        } catch (Exception e) {
            handleException(req, res, e);
            session.setAttribute("flashError", e.getMessage());
        }
        res.sendRedirect(req.getContextPath() + "/buyer/orders");
    }
}
