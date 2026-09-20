package com.rohith.rohithmart.service;

import com.rohith.rohithmart.dao.DAOFactory;
import com.rohith.rohithmart.dao.ProductDAO;
import com.rohith.rohithmart.dao.ReviewDAO;
import com.rohith.rohithmart.dao.UserDAO;
import com.rohith.rohithmart.dto.ReviewDTO;
import com.rohith.rohithmart.exception.AppException;
import com.rohith.rohithmart.exception.ConflictException;
import com.rohith.rohithmart.exception.ResourceNotFoundException;
import com.rohith.rohithmart.model.Review;
import com.rohith.rohithmart.model.User;
import com.rohith.rohithmart.util.ValidationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of ReviewService with eligibility validation and duplicate prevention.
 */
public class ReviewServiceImpl implements ReviewService {

    private static final Logger logger = LoggerFactory.getLogger(ReviewServiceImpl.class);
    private final ReviewDAO reviewDAO;
    private final UserDAO userDAO;
    private final ProductDAO productDAO;

    public ReviewServiceImpl() {
        this(DAOFactory.getReviewDAO(), DAOFactory.getUserDAO(), DAOFactory.getProductDAO());
    }

    public ReviewServiceImpl(ReviewDAO reviewDAO, UserDAO userDAO) {
        this(reviewDAO, userDAO, DAOFactory.getProductDAO());
    }

    public ReviewServiceImpl(ReviewDAO reviewDAO, UserDAO userDAO, ProductDAO productDAO) {
        this.reviewDAO = reviewDAO;
        this.userDAO = userDAO;
        this.productDAO = productDAO;
    }

    @Override
    public List<ReviewDTO> getReviewsByProduct(Long productId) throws AppException {
        ValidationUtil.validateId(productId, "productId");

        try {
            List<Review> list = reviewDAO.findByProductId(productId);
            List<ReviewDTO> dtos = new ArrayList<>(list.size());
            for (Review r : list) {
                ReviewDTO dto = new ReviewDTO();
                dto.setId(r.getId());
                dto.setProductId(r.getProductId());
                dto.setUserId(r.getUserId());
                dto.setRating(r.getRating());
                dto.setComment(r.getComment());
                dto.setCreatedAt(r.getCreatedAt());

                Optional<User> uOpt = userDAO.findById(r.getUserId());
                dto.setUserName(uOpt.map(User::getName).orElse("Verified Buyer"));
                dtos.add(dto);
            }
            return dtos;
        } catch (SQLException e) {
            logger.error("Failed to load reviews for product {}", productId, e);
            throw new AppException("Failed to load product reviews", e, 500);
        }
    }

    @Override
    public ReviewDTO addReview(Long productId, Long userId, int rating, String comment) throws AppException {
        ValidationUtil.validateId(productId, "productId");
        ValidationUtil.validateId(userId, "userId");
        ValidationUtil.validateRating(rating);
        String validatedComment = ValidationUtil.validateComment(comment);

        try {
            // 1. Verify product exists
            if (productDAO.findById(productId).isEmpty()) {
                throw new ResourceNotFoundException("Product not found with ID: " + productId);
            }

            // 2. Prevent duplicate reviews by same user
            if (reviewDAO.hasUserReviewed(userId, productId)) {
                throw new ConflictException("You have already reviewed this product");
            }

            Review r = new Review();
            r.setProductId(productId);
            r.setUserId(userId);
            r.setRating(rating);
            r.setComment(validatedComment);

            Review created = reviewDAO.create(r);
            ReviewDTO dto = new ReviewDTO();
            dto.setId(created.getId());
            dto.setProductId(created.getProductId());
            dto.setUserId(created.getUserId());
            dto.setRating(created.getRating());
            dto.setComment(created.getComment());
            dto.setCreatedAt(created.getCreatedAt());

            Optional<User> uOpt = userDAO.findById(userId);
            dto.setUserName(uOpt.map(User::getName).orElse("Verified Buyer"));
            logger.info("Review added for product ID {} by user ID {}", productId, userId);
            return dto;
        } catch (SQLException e) {
            logger.error("Failed to persist review", e);
            throw new AppException("Failed to submit review", e, 500);
        }
    }

    @Override
    public double getAverageRating(Long productId) throws AppException {
        ValidationUtil.validateId(productId, "productId");
        try {
            return reviewDAO.calculateAverageRating(productId);
        } catch (SQLException e) {
            logger.error("Failed to calculate average rating for product {}", productId, e);
            throw new AppException("Failed to calculate average rating", e, 500);
        }
    }

    @Override
    public boolean hasUserReviewed(Long userId, Long productId) throws AppException {
        ValidationUtil.validateId(userId, "userId");
        ValidationUtil.validateId(productId, "productId");
        try {
            return reviewDAO.hasUserReviewed(userId, productId);
        } catch (SQLException e) {
            logger.error("Failed to check existing review", e);
            throw new AppException("Failed to verify review status", e, 500);
        }
    }
}
