package com.rohith.rohithmart.controller;

import com.rohith.rohithmart.dto.OrderDTO;
import com.rohith.rohithmart.dto.ProductDTO;
import com.rohith.rohithmart.dto.UserResponseDTO;
import com.rohith.rohithmart.model.User;
import com.rohith.rohithmart.service.OrderService;
import com.rohith.rohithmart.service.ProductService;
import com.rohith.rohithmart.service.ServiceFactory;
import com.rohith.rohithmart.service.UserService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

/**
 * Thin Controller for Admin platform management: user oversight, product moderation, and order monitoring.
 * Architecture: Servlet -> Service -> DAO
 */
@WebServlet(name = "AdminDashboardServlet", urlPatterns = {"/admin/*"})
public class AdminDashboardServlet extends BaseServlet {

    private final UserService userService = ServiceFactory.getUserService();
    private final ProductService productService = ServiceFactory.getProductService();
    private final OrderService orderService = ServiceFactory.getOrderService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();

        try {
            if (pathInfo == null || "/dashboard".equals(pathInfo)) {
                showDashboard(req, res);
            } else if ("/users".equals(pathInfo)) {
                List<UserResponseDTO> users = userService.getAllUsers();
                req.setAttribute("users", users);
                forward(req, res, "admin/users.jsp");
            } else if ("/listings".equals(pathInfo)) {
                List<ProductDTO> listings = productService.searchProducts(null, null, "newest", 1, 100);
                req.setAttribute("listings", listings);
                forward(req, res, "admin/listings.jsp");
            } else if ("/orders".equals(pathInfo)) {
                List<OrderDTO> orders = orderService.getAllOrders();
                req.setAttribute("allOrders", orders);
                forward(req, res, "admin/orders.jsp");
            } else {
                res.sendRedirect(req.getContextPath() + "/admin/dashboard");
            }
        } catch (Exception e) {
            handleException(req, res, e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        HttpSession session = req.getSession(false);
        User user = (User) session.getAttribute("user");

        try {
            if ("/listings/delete".equals(pathInfo)) {
                Long id = Long.parseLong(req.getParameter("id"));
                productService.deleteProduct(id, user.getId(), true);
                session.setAttribute("flashSuccess", "Product listing removed by admin.");
                res.sendRedirect(req.getContextPath() + "/admin/listings");
            } else {
                res.sendRedirect(req.getContextPath() + "/admin/dashboard");
            }
        } catch (Exception e) {
            handleException(req, res, e);
            res.sendRedirect(req.getContextPath() + "/admin/listings");
        }
    }

    private void showDashboard(HttpServletRequest req, HttpServletResponse res) throws Exception {
        int userCount = userService.getTotalUserCount();
        int listingCount = productService.getTotalProductCount();
        int orderCount = orderService.getTotalOrderCount();

        List<UserResponseDTO> allUsers = userService.getAllUsers();
        long sellerCount = allUsers.stream().filter(u -> com.rohith.rohithmart.model.Role.SELLER == u.getRole()).count();
        long buyerCount = allUsers.stream().filter(u -> com.rohith.rohithmart.model.Role.BUYER == u.getRole()).count();

        List<OrderDTO> allOrders = orderService.getAllOrders();
        java.math.BigDecimal totalRevenue = java.math.BigDecimal.ZERO;
        long deliveredCount = 0;
        long shippedCount = 0;
        long confirmedCount = 0;
        long pendingCount = 0;
        long cancelledCount = 0;

        for (OrderDTO o : allOrders) {
            if (o.getStatus() != com.rohith.rohithmart.model.OrderStatus.CANCELLED && o.getTotalAmount() != null) {
                totalRevenue = totalRevenue.add(o.getTotalAmount());
            }
            if (o.getStatus() == com.rohith.rohithmart.model.OrderStatus.DELIVERED) deliveredCount++;
            else if (o.getStatus() == com.rohith.rohithmart.model.OrderStatus.SHIPPED) shippedCount++;
            else if (o.getStatus() == com.rohith.rohithmart.model.OrderStatus.CONFIRMED) confirmedCount++;
            else if (o.getStatus() == com.rohith.rohithmart.model.OrderStatus.PENDING) pendingCount++;
            else if (o.getStatus() == com.rohith.rohithmart.model.OrderStatus.CANCELLED) cancelledCount++;
        }

        req.setAttribute("userCount", userCount);
        req.setAttribute("sellerCount", sellerCount);
        req.setAttribute("buyerCount", buyerCount);
        req.setAttribute("listingCount", listingCount);
        req.setAttribute("orderCount", orderCount);
        req.setAttribute("totalRevenue", totalRevenue);
        req.setAttribute("deliveredCount", deliveredCount);
        req.setAttribute("shippedCount", shippedCount);
        req.setAttribute("confirmedCount", confirmedCount);
        req.setAttribute("pendingCount", pendingCount);
        req.setAttribute("cancelledCount", cancelledCount);
        req.setAttribute("recentOrders", allOrders.stream().limit(5).toList());
        req.setAttribute("recentUsers", allUsers.stream().limit(5).toList());
        forward(req, res, "admin/dashboard.jsp");
    }
}
