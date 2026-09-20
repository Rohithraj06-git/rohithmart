package com.rohith.rohithmart.dao;

import com.rohith.rohithmart.model.Review;

import java.sql.SQLException;
import java.util.List;

/**
 * Data Access Object interface for reviews and ratings.
 */
public interface ReviewDAO {

    /**
     * Finds all reviews submitted for a product.
     *
     * @param productId product ID
     * @return list of reviews
     * @throws SQLException on database failure
     */
    List<Review> findByProductId(Long productId) throws SQLException;

    /**
     * Calculates the average star rating for a given product.
     *
     * @param productId product ID
     * @return average rating rounded or 0.0 if no reviews exist
     * @throws SQLException on database failure
     */
    double calculateAverageRating(Long productId) throws SQLException;

    /**
     * Counts the total number of reviews submitted for a product.
     *
     * @param productId product ID
     * @return review count
     * @throws SQLException on database failure
     */
    int countByProductId(Long productId) throws SQLException;

    /**
     * Checks if a user has already reviewed a product.
     *
     * @param userId user ID
     * @param productId product ID
     * @return true if user has reviewed product, false otherwise
     * @throws SQLException on database failure
     */
    boolean hasUserReviewed(Long userId, Long productId) throws SQLException;

    /**
     * Finds a specific review by user ID and product ID.
     *
     * @param userId user ID
     * @param productId product ID
     * @return Optional containing review if found
     * @throws SQLException on database failure
     */
    java.util.Optional<Review> findByUserAndProductId(Long userId, Long productId) throws SQLException;

    /**
     * Creates a new review on a product.
     *
     * @param review review entity
     * @return created review
     * @throws SQLException on database failure
     */
    Review create(Review review) throws SQLException;
}
