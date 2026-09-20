package com.rohith.rohithmart.service;

import com.rohith.rohithmart.dto.ReviewDTO;
import com.rohith.rohithmart.exception.AppException;

import java.util.List;

/**
 * Service interface for product reviews and star ratings.
 */
public interface ReviewService {

    /**
     * Retrieves all reviews submitted for a product.
     *
     * @param productId product ID
     * @return list of ReviewDTOs
     * @throws AppException on failure
     */
    List<ReviewDTO> getReviewsByProduct(Long productId) throws AppException;

    /**
     * Submits a customer review and rating on a product.
     *
     * @param productId product ID
     * @param userId buyer user ID
     * @param rating 1 to 5 star rating
     * @param comment optional text comment
     * @return created ReviewDTO
     * @throws AppException if validation fails
     */
    ReviewDTO addReview(Long productId, Long userId, int rating, String comment) throws AppException;

    /**
     * Calculates the average star rating for a product.
     *
     * @param productId product ID
     * @return average star rating
     * @throws AppException on failure
     */
    double getAverageRating(Long productId) throws AppException;

    /**
     * Checks if a user has already reviewed a given product.
     *
     * @param userId user ID
     * @param productId product ID
     * @return true if user has reviewed product, false otherwise
     * @throws AppException on failure
     */
    boolean hasUserReviewed(Long userId, Long productId) throws AppException;
}
