package com.rohith.rohithmart.controller;

import com.rohith.rohithmart.dto.PagedResponse;
import com.rohith.rohithmart.dto.ProductDTO;
import com.rohith.rohithmart.dto.ProductFilter;
import com.rohith.rohithmart.exception.AppException;
import com.rohith.rohithmart.exception.AuthorizationException;
import com.rohith.rohithmart.exception.ValidationException;
import com.rohith.rohithmart.model.Role;
import com.rohith.rohithmart.model.User;
import com.rohith.rohithmart.service.ProductService;
import com.rohith.rohithmart.service.ServiceFactory;
import com.rohith.rohithmart.util.ValidationUtil;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;

/**
 * REST API Controller for Product catalog search, filtering, detail, and seller/admin product management.
 *
 * Endpoints:
 * - GET    /api/products                 (search, filter, sort, pagination)
 * - GET    /api/products/{id}            (product detail by ID)
 * - POST   /api/products                 (create new product listing - SELLER/ADMIN)
 * - PUT    /api/products/{id}            (update product listing - SELLER/ADMIN)
 * - DELETE /api/products/{id}            (delete product listing - SELLER/ADMIN)
 */
@WebServlet(name = "ProductApiServlet", urlPatterns = {"/api/products", "/api/products/*"})
public class ProductApiServlet extends BaseServlet {

    private final ProductService productService;

    public ProductApiServlet() {
        this(ServiceFactory.getProductService());
    }

    public ProductApiServlet(ProductService productService) {
        this.productService = productService;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        try {
            Long productId = parseIdFromPath(req.getPathInfo());
            if (productId != null) {
                // GET /api/products/{id}
                ProductDTO product = productService.getProductById(productId);
                sendJsonResponse(res, HttpServletResponse.SC_OK, product);
            } else {
                // GET /api/products (with search, category, sort, price, and pagination filters)
                ProductFilter filter = buildFilterFromRequest(req);
                PagedResponse<ProductDTO> pagedResponse = productService.searchProductsWithFilters(filter);
                sendJsonResponse(res, HttpServletResponse.SC_OK, pagedResponse);
            }
        } catch (Exception e) {
            try {
                handleException(req, res, e);
            } catch (Exception ex) {
                logger.error("Unhandled exception in ProductApiServlet doGet", ex);
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        try {
            User user = requireAuthenticatedUser(req);
            if (user.getRole() != Role.SELLER && user.getRole() != Role.ADMIN) {
                throw new AuthorizationException("Only sellers or administrators can create products");
            }

            ProductDTO dto = parseJson(req, ProductDTO.class);
            if (dto == null) {
                throw new ValidationException("Product payload is required");
            }

            ProductDTO created = productService.createProduct(dto, user.getId());
            sendJsonResponse(res, HttpServletResponse.SC_CREATED, "Product created successfully", created);
        } catch (Exception e) {
            try {
                handleException(req, res, e);
            } catch (Exception ex) {
                logger.error("Unhandled exception in ProductApiServlet doPost", ex);
            }
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse res) throws IOException {
        try {
            User user = requireAuthenticatedUser(req);
            if (user.getRole() != Role.SELLER && user.getRole() != Role.ADMIN) {
                throw new AuthorizationException("Only sellers or administrators can update products");
            }

            Long productId = parseIdFromPath(req.getPathInfo());
            if (productId == null) {
                throw new ValidationException("Product ID is required in URL path");
            }

            ProductDTO dto = parseJson(req, ProductDTO.class);
            if (dto == null) {
                throw new ValidationException("Product payload is required");
            }
            dto.setId(productId);

            ProductDTO updated = productService.updateProduct(dto, user.getId(), user.getRole() == Role.ADMIN);
            sendJsonResponse(res, HttpServletResponse.SC_OK, "Product updated successfully", updated);
        } catch (Exception e) {
            try {
                handleException(req, res, e);
            } catch (Exception ex) {
                logger.error("Unhandled exception in ProductApiServlet doPut", ex);
            }
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse res) throws IOException {
        try {
            User user = requireAuthenticatedUser(req);
            if (user.getRole() != Role.SELLER && user.getRole() != Role.ADMIN) {
                throw new AuthorizationException("Only sellers or administrators can delete products");
            }

            Long productId = parseIdFromPath(req.getPathInfo());
            if (productId == null) {
                throw new ValidationException("Product ID is required in URL path");
            }

            productService.deleteProduct(productId, user.getId(), user.getRole() == Role.ADMIN);
            sendJsonResponse(res, HttpServletResponse.SC_OK, "Product deleted successfully", null);
        } catch (Exception e) {
            try {
                handleException(req, res, e);
            } catch (Exception ex) {
                logger.error("Unhandled exception in ProductApiServlet doDelete", ex);
            }
        }
    }

    private ProductFilter buildFilterFromRequest(HttpServletRequest req) {
        String pageStr = req.getParameter("page");
        String sizeStr = req.getParameter("size");
        String category = req.getParameter("category");
        String keyword = req.getParameter("keyword");
        if (keyword == null || keyword.isEmpty()) {
            keyword = req.getParameter("q");
        }
        String sortBy = req.getParameter("sort");
        if (sortBy == null || sortBy.isEmpty()) {
            sortBy = req.getParameter("sortBy");
        }
        String minPriceStr = req.getParameter("minPrice");
        String maxPriceStr = req.getParameter("maxPrice");
        String inStockStr = req.getParameter("inStock");

        int page = 1;
        int size = 20;
        if (ValidationUtil.isNotEmpty(pageStr)) {
            try {
                page = Integer.parseInt(pageStr.trim());
            } catch (NumberFormatException ignored) {}
        }
        if (ValidationUtil.isNotEmpty(sizeStr)) {
            try {
                size = Integer.parseInt(sizeStr.trim());
            } catch (NumberFormatException ignored) {}
        }

        BigDecimal minPrice = null;
        if (ValidationUtil.isNotEmpty(minPriceStr)) {
            try {
                minPrice = new BigDecimal(minPriceStr.trim());
            } catch (Exception ignored) {}
        }

        BigDecimal maxPrice = null;
        if (ValidationUtil.isNotEmpty(maxPriceStr)) {
            try {
                maxPrice = new BigDecimal(maxPriceStr.trim());
            } catch (Exception ignored) {}
        }

        Boolean inStock = null;
        if (ValidationUtil.isNotEmpty(inStockStr)) {
            inStock = Boolean.parseBoolean(inStockStr.trim());
        }

        String ratingStr = req.getParameter("rating");
        if (ratingStr == null || ratingStr.isEmpty()) {
            ratingStr = req.getParameter("minRating");
        }
        Double minRating = null;
        if (ValidationUtil.isNotEmpty(ratingStr)) {
            try {
                minRating = Double.parseDouble(ratingStr.trim());
            } catch (Exception ignored) {}
        }

        return ProductFilter.builder()
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
    }

    private Long parseIdFromPath(String pathInfo) throws ValidationException {
        if (pathInfo == null || pathInfo.equals("/") || pathInfo.isEmpty()) {
            return null;
        }
        String clean = pathInfo.startsWith("/") ? pathInfo.substring(1) : pathInfo;
        // In case there are sub-paths, take the first segment
        if (clean.contains("/")) {
            clean = clean.substring(0, clean.indexOf("/"));
        }
        if (clean.isEmpty()) {
            return null;
        }
        return ValidationUtil.validateId(clean, "productId");
    }
}
