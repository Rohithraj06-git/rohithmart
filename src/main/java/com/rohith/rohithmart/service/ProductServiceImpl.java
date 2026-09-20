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
import java.util.List;
import java.util.Optional;

/**
 * Implementation of ProductService with business logic and validation.
 */
public class ProductServiceImpl implements ProductService {

    private static final Logger logger = LoggerFactory.getLogger(ProductServiceImpl.class);
    private final ProductDAO productDAO;

    public ProductServiceImpl() {
        this(DAOFactory.getProductDAO());
    }

    public ProductServiceImpl(ProductDAO productDAO) {
        this.productDAO = productDAO;
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
            List<ProductDTO> dtos = new ArrayList<>(list.size());
            for (Product p : list) {
                dtos.add(toDTO(p));
            }
            return dtos;
        } catch (SQLException e) {
            logger.error("Error searching products", e);
            throw new AppException("Failed to retrieve products", e, 500);
        }
    }

    @Override
    public List<ProductDTO> getProductsBySeller(Long sellerId) throws AppException {
        if (sellerId == null || sellerId <= 0) {
            throw new ValidationException("Invalid seller ID");
        }

        try {
            List<Product> list = productDAO.findBySellerId(sellerId);
            List<ProductDTO> dtos = new ArrayList<>(list.size());
            for (Product p : list) {
                dtos.add(toDTO(p));
            }
            return dtos;
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
    public ProductDTO updateProduct(ProductDTO dto, Long sellerId) throws AppException {
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
            if (!existing.getSellerId().equals(sellerId)) {
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
        if (!ValidationUtil.isNotEmpty(dto.getName())) {
            throw new ValidationException("Product name cannot be blank");
        }
        if (dto.getPrice() == null || dto.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Price must be a positive value");
        }
        if (dto.getStockQty() == null || dto.getStockQty() < 0) {
            throw new ValidationException("Stock quantity cannot be negative");
        }
        if (!ValidationUtil.isNotEmpty(dto.getCategory())) {
            throw new ValidationException("Category is required");
        }
    }

    private ProductDTO toDTO(Product p) {
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
                .build();
    }
}
