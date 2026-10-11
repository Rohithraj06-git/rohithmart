package com.rohith.rohithmart.service;

import com.rohith.rohithmart.dao.DAOFactory;
import com.rohith.rohithmart.dao.ProductDAO;
import com.rohith.rohithmart.dto.ProductDTO;
import com.rohith.rohithmart.exception.AppException;
import com.rohith.rohithmart.exception.ForbiddenException;
import com.rohith.rohithmart.exception.ResourceNotFoundException;
import com.rohith.rohithmart.exception.ValidationException;
import com.rohith.rohithmart.model.Product;
import com.rohith.rohithmart.util.ValidationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Implementation of ProductService with business logic and validation.
 */
public class ProductServiceImpl implements ProductService {

    private static final Logger logger = LoggerFactory.getLogger(ProductServiceImpl.class);
    private final ProductDAO productDAO;
    private final com.rohith.rohithmart.dao.ReviewDAO reviewDAO;

    public ProductServiceImpl() {
        this(DAOFactory.getProductDAO(), DAOFactory.getReviewDAO());
    }

    public ProductServiceImpl(ProductDAO productDAO) {
        this(productDAO, DAOFactory.getReviewDAO());
    }

    public ProductServiceImpl(ProductDAO productDAO, com.rohith.rohithmart.dao.ReviewDAO reviewDAO) {
        this.productDAO = productDAO;
        this.reviewDAO = reviewDAO;
    }

    @Override
    public ProductDTO getProductById(Long id) throws AppException {
        if (id == null || id <= 0) {
            throw new ValidationException("Invalid product ID");
        }

        try {
            Optional<Product> opt = productDAO.findById(id);
            if (opt.isEmpty()) {
                throw new ResourceNotFoundException("Product not found with ID: " + id);
            }
            return toDTO(opt.get());
        } catch (SQLException e) {
            logger.error("Error retrieving product ID {}", id, e);
            throw new AppException("Failed to load product", e, 500);
        }
    }

    @Override
    public List<ProductDTO> searchProducts(String category, String keyword, String sortBy, int page, int pageSize) throws AppException {
        int pageNum = Math.max(1, page);
        int size = (pageSize > 0 && pageSize <= 100) ? pageSize : 20;
        int offset = (pageNum - 1) * size;

        try {
            List<Product> list = productDAO.search(category, keyword, sortBy, size, offset);
            return toDTOList(list);
        } catch (SQLException e) {
            logger.error("Error searching products", e);
            throw new AppException("Failed to retrieve products", e, 500);
        }
    }

    @Override
    public com.rohith.rohithmart.dto.PagedResponse<ProductDTO> searchProductsWithFilters(com.rohith.rohithmart.dto.ProductFilter filter) throws AppException {
        com.rohith.rohithmart.dto.ProductFilter safeFilter = filter != null ? filter : com.rohith.rohithmart.dto.ProductFilter.builder().build();
        try {
            List<Product> list = productDAO.findWithFilters(safeFilter);
            int totalResults = productDAO.countFiltered(safeFilter);

            List<ProductDTO> dtos = toDTOList(list);
            return com.rohith.rohithmart.dto.PagedResponse.of(dtos, safeFilter.getPage(), safeFilter.getSize(), totalResults);
        } catch (SQLException e) {
            logger.error("Error searching products with filters", e);
            throw new AppException("Failed to retrieve filtered products", e, 500);
        }
    }

    @Override
    public List<ProductDTO> getProductsByCategory(String category) throws AppException {
        ValidationUtil.validateNotBlank(category, "category");
        try {
            List<Product> list = productDAO.findByCategory(category);
            return toDTOList(list);
        } catch (SQLException e) {
            logger.error("Error retrieving products by category {}", category, e);
            throw new AppException("Failed to load products by category", e, 500);
        }
    }

    @Override
    public List<ProductDTO> getProductsBySeller(Long sellerId) throws AppException {
        if (sellerId == null || sellerId <= 0) {
            throw new ValidationException("Invalid seller ID");
        }

        try {
            List<Product> list = productDAO.findBySellerId(sellerId);
            return toDTOList(list);
        } catch (SQLException e) {
            logger.error("Error retrieving seller products for seller {}", sellerId, e);
            throw new AppException("Failed to load seller products", e, 500);
        }
    }

    @Override
    public ProductDTO createProduct(ProductDTO dto, Long sellerId) throws AppException {
        validateProduct(dto);
        if (sellerId == null || sellerId <= 0) {
            throw new ValidationException("Valid seller ID is required");
        }

        Product product = new Product();
        product.setSellerId(sellerId);
        product.setName(dto.getName().trim());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setStockQty(dto.getStockQty());
        product.setCategory(dto.getCategory().trim());
        product.setImageUrl(dto.getImageUrl());

        try {
            Product created = productDAO.create(product);
            return toDTO(created);
        } catch (SQLException e) {
            logger.error("Failed to create product listing", e);
            throw new AppException("Database error creating product listing", e, 500);
        }
    }

    @Override
    public ProductDTO updateProduct(ProductDTO dto, Long userId, boolean isAdmin) throws AppException {
        if (dto == null || dto.getId() == null) {
            throw new ValidationException("Product ID is required for update");
        }
        validateProduct(dto);

        try {
            Optional<Product> opt = productDAO.findById(dto.getId());
            if (opt.isEmpty()) {
                throw new ResourceNotFoundException("Product not found");
            }
            Product existing = opt.get();
            if (!isAdmin && !existing.getSellerId().equals(userId)) {
                throw new ForbiddenException("You are not authorized to update this listing");
            }

            existing.setName(dto.getName().trim());
            existing.setDescription(dto.getDescription());
            existing.setPrice(dto.getPrice());
            existing.setStockQty(dto.getStockQty());
            existing.setCategory(dto.getCategory().trim());
            existing.setImageUrl(dto.getImageUrl());

            productDAO.update(existing);
            return toDTO(existing);
        } catch (SQLException e) {
            logger.error("Database error updating product ID {}", dto.getId(), e);
            throw new AppException("Failed to update product listing", e, 500);
        }
    }

    @Override
    public ProductDTO updateProduct(ProductDTO dto, Long sellerId) throws AppException {
        return updateProduct(dto, sellerId, false);
    }

    @Override
    public void deleteProduct(Long productId, Long userId, boolean isAdmin) throws AppException {
        if (productId == null || productId <= 0) {
            throw new ValidationException("Invalid product ID");
        }

        try {
            Optional<Product> opt = productDAO.findById(productId);
            if (opt.isEmpty()) {
                throw new ResourceNotFoundException("Product not found");
            }
            Product existing = opt.get();
            if (!isAdmin && !existing.getSellerId().equals(userId)) {
                throw new ForbiddenException("You do not have permission to delete this product");
            }

            productDAO.delete(productId);
            logger.info("Product ID {} deleted by user ID {}", productId, userId);
        } catch (SQLException e) {
            logger.error("Database error deleting product ID {}", productId, e);
            throw new AppException("Failed to delete product", e, 500);
        }
    }

    @Override
    public int getTotalProductCount() throws AppException {
        try {
            return productDAO.countAll();
        } catch (SQLException e) {
            logger.error("Failed to count products", e);
            throw new AppException("Error reading product count", e, 500);
        }
    }

    private void validateProduct(ProductDTO dto) throws ValidationException {
        if (dto == null) {
            throw new ValidationException("Product data is required");
        }
        ValidationUtil.validateProductName(dto.getName());
        ValidationUtil.validatePrice(dto.getPrice());
        ValidationUtil.validateStockQuantity(dto.getStockQty());
        ValidationUtil.validateNotBlank(dto.getCategory(), "category");
        ValidationUtil.validateDescription(dto.getDescription());
    }

    private List<ProductDTO> toDTOList(List<Product> products) {
        if (products == null || products.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> pids = products.stream().map(Product::getId).filter(Objects::nonNull).toList();
        Map<Long, double[]> statsMap = Collections.emptyMap();
        if (reviewDAO != null && !pids.isEmpty()) {
            try {
                statsMap = reviewDAO.getRatingStatsForProducts(pids);
            } catch (Exception e) {
                logger.warn("Could not batch load ratings for products", e);
            }
        }

        List<ProductDTO> dtos = new ArrayList<>(products.size());
        for (Product p : products) {
            double[] stats = statsMap.get(p.getId());
            double avg = stats != null ? stats[0] : 0.0;
            int count = stats != null ? (int) stats[1] : 0;
            dtos.add(toDTO(p, avg, count));
        }
        return dtos;
    }

    private ProductDTO toDTO(Product p) {
        double avgRating = 0.0;
        int reviewCount = 0;
        if (reviewDAO != null && p.getId() != null) {
            try {
                avgRating = reviewDAO.calculateAverageRating(p.getId());
                reviewCount = reviewDAO.countByProductId(p.getId());
            } catch (Exception e) {
                logger.warn("Could not calculate ratings for product {}", p.getId(), e);
            }
        }
        return toDTO(p, avgRating, reviewCount);
    }

    private ProductDTO toDTO(Product p, double avgRating, int reviewCount) {
        return ProductDTO.builder()
                .id(p.getId())
                .sellerId(p.getSellerId())
                .name(p.getName())
                .description(p.getDescription())
                .price(p.getPrice())
                .stockQty(p.getStockQty())
                .category(p.getCategory())
                .imageUrl(p.getImageUrl())
                .createdAt(p.getCreatedAt())
                .averageRating(avgRating)
                .reviewCount(reviewCount)
                .build();
    }
}
