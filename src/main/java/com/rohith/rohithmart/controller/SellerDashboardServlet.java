package com.rohith.rohithmart.controller;

import com.rohith.rohithmart.dto.ProductDTO;
import com.rohith.rohithmart.model.OrderStatus;
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
import java.math.BigDecimal;
import java.util.List;

/**
 * Controller for Seller portal: listings management, product CRUD, and incoming orders.
 */
@WebServlet(name = "SellerDashboardServlet", urlPatterns = {"/seller/*"})
public class SellerDashboardServlet extends BaseServlet {

    private final ProductService productService = ServiceFactory.getProductService();
    private final OrderService orderService = ServiceFactory.getOrderService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        User user = (User) session.getAttribute("user");
        String pathInfo = req.getPathInfo();

        try {
            if (pathInfo == null || "/dashboard".equals(pathInfo)) {
                showDashboard(req, res, user);
            } else if ("/products".equals(pathInfo)) {
                showProducts(req, res, user);
            } else if ("/products/new".equals(pathInfo)) {
                forward(req, res, "seller/product-form.jsp");
            } else if ("/products/edit".equals(pathInfo)) {
                showEditForm(req, res, user);
            } else if ("/orders".equals(pathInfo)) {
                List<com.rohith.rohithmart.dto.OrderDTO> orders = orderService.getSellerOrders(user.getId());
                req.setAttribute("orders", orders);
                forward(req, res, "seller/orders.jsp");
            } else {
                res.sendRedirect(req.getContextPath() + "/seller/dashboard");
            }
        } catch (Exception e) {
            handleException(req, res, e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        User user = (User) session.getAttribute("user");
        String pathInfo = req.getPathInfo();

        try {
            if ("/products/new".equals(pathInfo)) {
                handleCreateProduct(req, res, user);
            } else if ("/products/edit".equals(pathInfo)) {
                handleUpdateProduct(req, res, user);
            } else if ("/products/delete".equals(pathInfo)) {
                handleDeleteProduct(req, res, user);
            } else if ("/orders/status".equals(pathInfo)) {
                handleUpdateOrderStatus(req, res, user);
            } else {
                res.sendRedirect(req.getContextPath() + "/seller/dashboard");
            }
        } catch (Exception e) {
            handleException(req, res, e);
            res.sendRedirect(req.getContextPath() + "/seller/products");
        }
    }

    private void showDashboard(HttpServletRequest req, HttpServletResponse res, User user) throws Exception {
        List<ProductDTO> products = productService.getProductsBySeller(user.getId());
        List<com.rohith.rohithmart.dto.OrderDTO> orders = orderService.getSellerOrders(user.getId());

        int totalProducts = products.size();
        List<ProductDTO> lowStockProducts = products.stream()
                .filter(p -> p.getStockQty() != null && p.getStockQty() <= 5)
                .toList();

        BigDecimal revenue = BigDecimal.ZERO;
        int pendingOrders = 0;
        for (com.rohith.rohithmart.dto.OrderDTO o : orders) {
            if (o.getStatus() != OrderStatus.CANCELLED && o.getTotalAmount() != null) {
                revenue = revenue.add(o.getTotalAmount());
            }
            if (o.getStatus() == OrderStatus.PENDING || o.getStatus() == OrderStatus.CONFIRMED) {
                pendingOrders++;
            }
        }

        req.setAttribute("totalProducts", totalProducts);
        req.setAttribute("totalListings", totalProducts);
        req.setAttribute("lowStockCount", lowStockProducts.size());
        req.setAttribute("lowStockProducts", lowStockProducts);
        req.setAttribute("totalOrders", orders.size());
        req.setAttribute("pendingOrders", pendingOrders);
        req.setAttribute("totalRevenue", revenue);
        req.setAttribute("recentOrders", orders.stream().limit(5).toList());
        req.setAttribute("products", products);
        forward(req, res, "seller/dashboard.jsp");
    }

    private void showProducts(HttpServletRequest req, HttpServletResponse res, User user) throws Exception {
        List<ProductDTO> products = productService.getProductsBySeller(user.getId());
        req.setAttribute("products", products);
        forward(req, res, "seller/manage-products.jsp");
    }

    private void showEditForm(HttpServletRequest req, HttpServletResponse res, User user) throws Exception {
        Long id = Long.parseLong(req.getParameter("id"));
        ProductDTO product = productService.getProductById(id);
        if (user.getRole() != com.rohith.rohithmart.model.Role.ADMIN && !product.getSellerId().equals(user.getId())) {
            throw new com.rohith.rohithmart.exception.ForbiddenException("You are not authorized to edit this listing");
        }
        req.setAttribute("product", product);
        forward(req, res, "seller/product-form.jsp");
    }

    private void handleCreateProduct(HttpServletRequest req, HttpServletResponse res, User user) throws Exception {
        ProductDTO dto = new ProductDTO();
        dto.setName(req.getParameter("name"));
        dto.setDescription(req.getParameter("description"));
        dto.setCategory(req.getParameter("category"));
        dto.setPrice(new BigDecimal(req.getParameter("price")));
        dto.setStockQty(Integer.parseInt(req.getParameter("stockQty")));
        dto.setImageUrl(req.getParameter("imageUrl"));

        productService.createProduct(dto, user.getId());
        req.getSession().setAttribute("flashSuccess", "Product listing created successfully!");
        res.sendRedirect(req.getContextPath() + "/seller/products");
    }

    private void handleUpdateProduct(HttpServletRequest req, HttpServletResponse res, User user) throws Exception {
        ProductDTO dto = new ProductDTO();
        dto.setId(Long.parseLong(req.getParameter("id")));
        dto.setName(req.getParameter("name"));
        dto.setDescription(req.getParameter("description"));
        dto.setCategory(req.getParameter("category"));
        dto.setPrice(new BigDecimal(req.getParameter("price")));
        dto.setStockQty(Integer.parseInt(req.getParameter("stockQty")));
        dto.setImageUrl(req.getParameter("imageUrl"));

        productService.updateProduct(dto, user.getId(), user.getRole() == com.rohith.rohithmart.model.Role.ADMIN);
        req.getSession().setAttribute("flashSuccess", "Product updated successfully!");
        res.sendRedirect(req.getContextPath() + "/seller/products");
    }

    private void handleDeleteProduct(HttpServletRequest req, HttpServletResponse res, User user) throws Exception {
        Long id = Long.parseLong(req.getParameter("id"));
        productService.deleteProduct(id, user.getId(), user.getRole() == com.rohith.rohithmart.model.Role.ADMIN);
        req.getSession().setAttribute("flashSuccess", "Product removed successfully!");
        res.sendRedirect(req.getContextPath() + "/seller/products");
    }

    private void handleUpdateOrderStatus(HttpServletRequest req, HttpServletResponse res, User user) throws Exception {
        Long orderId = Long.parseLong(req.getParameter("orderId"));
        OrderStatus status = OrderStatus.valueOf(req.getParameter("status"));
        orderService.updateOrderStatus(orderId, status, user.getId(), user.getRole() == com.rohith.rohithmart.model.Role.ADMIN);
        req.getSession().setAttribute("flashSuccess", "Order status updated to " + status);
        res.sendRedirect(req.getContextPath() + "/seller/orders");
    }
}
