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

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        if ("/review".equals(pathInfo)) {
            javax.servlet.http.HttpSession session = req.getSession(false);
            com.rohith.rohithmart.model.User user = (session != null) ? (com.rohith.rohithmart.model.User) session.getAttribute("user") : null;
            if (user == null) {
                res.sendRedirect(req.getContextPath() + "/auth/login");
                return;
            }
            String pId = req.getParameter("productId");
            try {
                Long productId = Long.parseLong(pId);
                int rating = Integer.parseInt(req.getParameter("rating"));
                String comment = req.getParameter("comment");
                reviewService.addReview(user.getId(), productId, rating, comment);
                session.setAttribute("flashSuccess", "Thank you! Your verified review has been published.");
                res.sendRedirect(req.getContextPath() + "/products/detail?id=" + productId);
            } catch (Exception e) {
                handleException(req, res, e);
                res.sendRedirect(req.getContextPath() + "/products/detail?id=" + (pId != null ? pId : ""));
            }
        } else {
            res.sendRedirect(req.getContextPath() + "/products");
        }
    }

    private void showCatalog(HttpServletRequest req, HttpServletResponse res) throws Exception {
        String category = req.getParameter("category");
        String keyword = req.getParameter("q");
        if (keyword == null || keyword.isEmpty()) {
            keyword = req.getParameter("keyword");
        }
        String sortBy = req.getParameter("sortBy");
        if (sortBy == null || sortBy.isEmpty()) {
            sortBy = req.getParameter("sort");
        }
        String minPriceStr = req.getParameter("minPrice");
        String maxPriceStr = req.getParameter("maxPrice");
        String ratingStr = req.getParameter("rating");
        if (ratingStr == null || ratingStr.isEmpty()) {
            ratingStr = req.getParameter("minRating");
        }
        String inStockStr = req.getParameter("inStock");
        String pageStr = req.getParameter("page");
        String sizeStr = req.getParameter("size");

        int page = 1;
        int size = 12;
        if (pageStr != null && !pageStr.trim().isEmpty()) {
            try { page = Integer.parseInt(pageStr.trim()); } catch (NumberFormatException ignored) {}
        }
        if (sizeStr != null && !sizeStr.trim().isEmpty()) {
            try { size = Integer.parseInt(sizeStr.trim()); } catch (NumberFormatException ignored) {}
        }

        java.math.BigDecimal minPrice = null;
        if (minPriceStr != null && !minPriceStr.trim().isEmpty()) {
            try { minPrice = new java.math.BigDecimal(minPriceStr.trim()); } catch (Exception ignored) {}
        }
        java.math.BigDecimal maxPrice = null;
        if (maxPriceStr != null && !maxPriceStr.trim().isEmpty()) {
            try { maxPrice = new java.math.BigDecimal(maxPriceStr.trim()); } catch (Exception ignored) {}
        }
        Double minRating = null;
        if (ratingStr != null && !ratingStr.trim().isEmpty()) {
            try { minRating = Double.parseDouble(ratingStr.trim()); } catch (Exception ignored) {}
        }
        Boolean inStock = null;
        if (inStockStr != null && !inStockStr.trim().isEmpty()) {
            inStock = Boolean.parseBoolean(inStockStr.trim()) || "1".equals(inStockStr) || "on".equalsIgnoreCase(inStockStr);
        }

        com.rohith.rohithmart.dto.ProductFilter filter = com.rohith.rohithmart.dto.ProductFilter.builder()
                .category(category)
                .keyword(keyword)
                .sortBy(sortBy)
                .minPrice(minPrice)
                .maxPrice(maxPrice)
                .minRating(minRating)
                .inStockOnly(inStock)
                .page(page)
                .size(size)
                .build();

        com.rohith.rohithmart.dto.PagedResponse<ProductDTO> paged = productService.searchProductsWithFilters(filter);

        req.setAttribute("products", paged.getData());
        req.setAttribute("currentPage", paged.getCurrentPage());
        req.setAttribute("totalPages", paged.getTotalPages());
        req.setAttribute("totalResults", paged.getTotalResults());
        req.setAttribute("pageSize", paged.getPageSize());
        req.setAttribute("filter", filter);
        req.setAttribute("categories", List.of("Electronics", "Fashion", "Home & Living", "Books"));
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
