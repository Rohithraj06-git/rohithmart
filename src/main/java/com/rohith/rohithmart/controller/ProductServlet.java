package com.rohith.rohithmart.controller;

import com.rohith.rohithmart.dto.ProductDTO;
import com.rohith.rohithmart.dto.ReviewDTO;
import com.rohith.rohithmart.service.ProductService;
import com.rohith.rohithmart.service.ReviewService;
import com.rohith.rohithmart.service.ServiceFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * Controller for product catalog, search, filtering, sorting, and detail views.
 */
@WebServlet(name = "ProductServlet", urlPatterns = {"/products", "/products/*"})
public class ProductServlet extends BaseServlet {

    private final ProductService productService = ServiceFactory.getProductService();
    private final ReviewService reviewService = ServiceFactory.getReviewService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();

        try {
            if ("/detail".equals(pathInfo)) {
                showDetail(req, res);
            } else {
                showCatalog(req, res);
            }
        } catch (Exception e) {
            handleException(req, res, e);
        }
    }

    private void showCatalog(HttpServletRequest req, HttpServletResponse res) throws Exception {
        String category = req.getParameter("category");
        String keyword = req.getParameter("q");
        String sortBy = req.getParameter("sortBy");

        List<ProductDTO> products = productService.searchProducts(category, keyword, sortBy, 1, 50);
        req.setAttribute("products", products);
        forward(req, res, "buyer/catalog.jsp");
    }

    private void showDetail(HttpServletRequest req, HttpServletResponse res) throws Exception {
        String idParam = req.getParameter("id");
        if (idParam == null) {
            res.sendRedirect(req.getContextPath() + "/products");
            return;
        }

        Long id = Long.parseLong(idParam);
        ProductDTO product = productService.getProductById(id);
        List<ReviewDTO> reviews = reviewService.getReviewsByProduct(id);

        req.setAttribute("product", product);
        req.setAttribute("reviews", reviews);
        forward(req, res, "buyer/product-detail.jsp");
    }
}
