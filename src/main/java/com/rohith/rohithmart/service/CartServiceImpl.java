package com.rohith.rohithmart.service;

import com.rohith.rohithmart.dao.CartItemDAO;
import com.rohith.rohithmart.dao.DAOFactory;
import com.rohith.rohithmart.dao.ProductDAO;
import com.rohith.rohithmart.dto.CartItemDTO;
import com.rohith.rohithmart.exception.AppException;
import com.rohith.rohithmart.exception.ResourceNotFoundException;
import com.rohith.rohithmart.exception.ValidationException;
import com.rohith.rohithmart.model.CartItem;
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
 * Implementation of CartService with server-side stock validation and calculation.
 */
public class CartServiceImpl implements CartService {

    private static final Logger logger = LoggerFactory.getLogger(CartServiceImpl.class);
    private final CartItemDAO cartItemDAO;
    private final ProductDAO productDAO;

    public CartServiceImpl() {
        this(DAOFactory.getCartItemDAO(), DAOFactory.getProductDAO());
    }

    public CartServiceImpl(CartItemDAO cartItemDAO, ProductDAO productDAO) {
        this.cartItemDAO = cartItemDAO;
        this.productDAO = productDAO;
    }

    @Override
    public List<CartItemDTO> getCartItems(Long userId) throws AppException {
        if (userId == null || userId <= 0) {
            throw new ValidationException("Invalid user ID");
        }

        try {
            List<CartItem> items = cartItemDAO.findByUserId(userId);
            List<CartItemDTO> dtos = new ArrayList<>(items.size());

            for (CartItem item : items) {
                Optional<Product> prodOpt = productDAO.findById(item.getProductId());
                if (prodOpt.isPresent()) {
                    Product p = prodOpt.get();
                    CartItemDTO dto = new CartItemDTO();
                    dto.setId(item.getId());
                    dto.setProductId(p.getId());
                    dto.setProductName(p.getName());
                    dto.setProductImageUrl(p.getImageUrl());
                    dto.setUnitPrice(p.getPrice());
                    dto.setQuantity(item.getQuantity());
                    dto.setSubtotal(p.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
                    dtos.add(dto);
                }
            }
            return dtos;
        } catch (SQLException e) {
            logger.error("Failed to fetch cart items for user {}", userId, e);
            throw new AppException("Failed to load cart items", e, 500);
        }
    }

    @Override
    public void addToCart(Long userId, Long productId, int quantity) throws AppException {
        if (userId == null || userId <= 0) {
            throw new ValidationException("User ID is required");
        }
        if (productId == null || productId <= 0) {
            throw new ValidationException("Product ID is required");
        }
        if (quantity <= 0) {
            throw new ValidationException("Quantity must be at least 1");
        }

        try {
            Optional<Product> prodOpt = productDAO.findById(productId);
            if (prodOpt.isEmpty()) {
                throw new ResourceNotFoundException("Product not found");
            }
            Product product = prodOpt.get();

            Optional<CartItem> existingOpt = cartItemDAO.findByUserAndProduct(userId, productId);
            int totalQty = quantity;
            if (existingOpt.isPresent()) {
                totalQty += existingOpt.get().getQuantity();
            }

            if (product.getStockQty() < totalQty) {
                throw new ValidationException("Only " + product.getStockQty() + " items currently available in stock");
            }

            if (existingOpt.isPresent()) {
                cartItemDAO.updateQuantity(existingOpt.get().getId(), totalQty);
            } else {
                CartItem item = new CartItem();
                item.setUserId(userId);
                item.setProductId(productId);
                item.setQuantity(quantity);
                cartItemDAO.create(item);
            }
        } catch (SQLException e) {
            logger.error("Error adding to cart", e);
            throw new AppException("Failed to add item to cart", e, 500);
        }
    }

    @Override
    public void updateQuantity(Long userId, Long cartItemId, int quantity) throws AppException {
        ValidationUtil.validateId(userId, "userId");
        ValidationUtil.validateId(cartItemId, "cartItemId");
        ValidationUtil.validateCartQuantity(quantity);

        try {
            Optional<CartItem> itemOpt = cartItemDAO.findById(cartItemId);
            if (itemOpt.isEmpty()) {
                throw new ResourceNotFoundException("Cart item not found with ID: " + cartItemId);
            }
            CartItem item = itemOpt.get();
            if (!item.getUserId().equals(userId)) {
                throw new com.rohith.rohithmart.exception.ForbiddenException("You are not authorized to update this cart item");
            }

            Optional<Product> prodOpt = productDAO.findById(item.getProductId());
            if (prodOpt.isEmpty()) {
                throw new ResourceNotFoundException("Associated product no longer available");
            }
            Product prod = prodOpt.get();
            if (prod.getStockQty() < quantity) {
                throw new com.rohith.rohithmart.exception.InsufficientStockException(prod.getId(), quantity, prod.getStockQty());
            }

            cartItemDAO.updateQuantity(cartItemId, quantity);
        } catch (SQLException e) {
            logger.error("Error updating cart quantity", e);
            throw new AppException("Failed to update cart quantity", e, 500);
        }
    }

    @Override
    public void removeFromCart(Long userId, Long cartItemId) throws AppException {
        ValidationUtil.validateId(userId, "userId");
        ValidationUtil.validateId(cartItemId, "cartItemId");

        try {
            Optional<CartItem> itemOpt = cartItemDAO.findById(cartItemId);
            if (itemOpt.isEmpty()) {
                throw new ResourceNotFoundException("Cart item not found with ID: " + cartItemId);
            }
            CartItem item = itemOpt.get();
            if (!item.getUserId().equals(userId)) {
                throw new com.rohith.rohithmart.exception.ForbiddenException("You are not authorized to remove this cart item");
            }

            cartItemDAO.delete(cartItemId);
        } catch (SQLException e) {
            logger.error("Error removing cart item", e);
            throw new AppException("Failed to remove item from cart", e, 500);
        }
    }

    @Override
    public void clearCart(Long userId) throws AppException {
        ValidationUtil.validateId(userId, "userId");
        try {
            cartItemDAO.clearCart(userId);
        } catch (SQLException e) {
            logger.error("Error clearing cart for user {}", userId, e);
            throw new AppException("Failed to clear cart", e, 500);
        }
    }

    @Override
    public BigDecimal calculateTotal(List<CartItemDTO> items) {
        BigDecimal total = BigDecimal.ZERO;
        if (items != null) {
            for (CartItemDTO item : items) {
                if (item.getSubtotal() != null) {
                    total = total.add(item.getSubtotal());
                }
            }
        }
        return total;
    }
}
