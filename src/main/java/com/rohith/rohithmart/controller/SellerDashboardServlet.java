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
                showEditForm(req, res);
            } else if ("/orders".equals(pathInfo)) {
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
                handleUpdateOrderStatus(req, res);
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
        req.setAttribute("totalListings", products.size());
        forward(req, res, "seller/dashboard.jsp");
    }

    private void showProducts(HttpServletRequest req, HttpServletResponse res, User user) throws Exception {
        List<ProductDTO> products = productService.getProductsBySeller(user.getId());
        req.setAttribute("products", products);
        forward(req, res, "seller/manage-products.jsp");
    }

    private void showEditForm(HttpServletRequest req, HttpServletResponse res) throws Exception {
        Long id = Long.parseLong(req.getParameter("id"));
        ProductDTO product = productService.getProductById(id);
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

        productService.updateProduct(dto, user.getId());
        req.getSession().setAttribute("flashSuccess", "Product updated successfully!");
        res.sendRedirect(req.getContextPath() + "/seller/products");
    }

    private void handleDeleteProduct(HttpServletRequest req, HttpServletResponse res, User user) throws Exception {
        Long id = Long.parseLong(req.getParameter("id"));
        productService.deleteProduct(id, user.getId(), false);
        req.getSession().setAttribute("flashSuccess", "Product removed successfully!");
        res.sendRedirect(req.getContextPath() + "/seller/products");
    }

    private void handleUpdateOrderStatus(HttpServletRequest req, HttpServletResponse res) throws Exception {
        Long orderId = Long.parseLong(req.getParameter("orderId"));
        OrderStatus status = OrderStatus.valueOf(req.getParameter("status"));
        orderService.updateOrderStatus(orderId, status);
        req.getSession().setAttribute("flashSuccess", "Order status updated to " + status);
        res.sendRedirect(req.getContextPath() + "/seller/orders");
    }
}
