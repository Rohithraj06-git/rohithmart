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
     * Creates a new review on a product.
     *
     * @param review review entity
     * @return created review
     * @throws SQLException on database failure
     */
    Review create(Review review) throws SQLException;
}
