package com.rohith.rohithmart.service;

import com.rohith.rohithmart.dao.DAOFactory;
import com.rohith.rohithmart.dao.ReviewDAO;
import com.rohith.rohithmart.dao.UserDAO;
import com.rohith.rohithmart.dto.ReviewDTO;
import com.rohith.rohithmart.exception.AppException;
import com.rohith.rohithmart.exception.ValidationException;
import com.rohith.rohithmart.model.Review;
import com.rohith.rohithmart.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of ReviewService.
 */
public class ReviewServiceImpl implements ReviewService {

    private static final Logger logger = LoggerFactory.getLogger(ReviewServiceImpl.class);
    private final ReviewDAO reviewDAO;
    private final UserDAO userDAO;

    public ReviewServiceImpl() {
        this(DAOFactory.getReviewDAO(), DAOFactory.getUserDAO());
    }

    public ReviewServiceImpl(ReviewDAO reviewDAO, UserDAO userDAO) {
        this.reviewDAO = reviewDAO;
        this.userDAO = userDAO;
    }

    @Override
    public List<ReviewDTO> getReviewsByProduct(Long productId) throws AppException {
        if (productId == null || productId <= 0) {
            throw new ValidationException("Invalid product ID");
        }

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
        if (productId == null || productId <= 0) {
            throw new ValidationException("Product ID is required");
        }
        if (userId == null || userId <= 0) {
            throw new ValidationException("User ID is required");
        }
        if (rating < 1 || rating > 5) {
            throw new ValidationException("Rating must be an integer between 1 and 5");
        }

        Review r = new Review();
        r.setProductId(productId);
        r.setUserId(userId);
        r.setRating(rating);
        r.setComment(comment != null ? comment.trim() : "");

        try {
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
            return dto;
        } catch (SQLException e) {
            logger.error("Failed to persist review", e);
            throw new AppException("Failed to submit review", e, 500);
        }
    }
}
