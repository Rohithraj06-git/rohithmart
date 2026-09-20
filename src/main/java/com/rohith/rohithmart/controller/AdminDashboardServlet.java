package com.rohith.rohithmart.controller;

import com.rohith.rohithmart.dao.DAOFactory;
import com.rohith.rohithmart.dto.OrderDTO;
import com.rohith.rohithmart.dto.ProductDTO;
import com.rohith.rohithmart.model.User;
import com.rohith.rohithmart.service.OrderService;
import com.rohith.rohithmart.service.ProductService;
import com.rohith.rohithmart.service.ServiceFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

/**
 * Controller for Admin platform management: user oversight, product moderation, and order monitoring.
 */
@WebServlet(name = "AdminDashboardServlet", urlPatterns = {"/admin/*"})
public class AdminDashboardServlet extends BaseServlet {

    private final ProductService productService = ServiceFactory.getProductService();
    private final OrderService orderService = ServiceFactory.getOrderService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();

        try {
            if (pathInfo == null || "/dashboard".equals(pathInfo)) {
                showDashboard(req, res);
            } else if ("/users".equals(pathInfo)) {
                List<User> users = DAOFactory.getUserDAO().findAll();
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
        int userCount = DAOFactory.getUserDAO().countAll();
        int listingCount = productService.getTotalProductCount();
        int orderCount = orderService.getTotalOrderCount();

        req.setAttribute("userCount", userCount);
        req.setAttribute("listingCount", listingCount);
        req.setAttribute("orderCount", orderCount);
        forward(req, res, "admin/dashboard.jsp");
    }
}
