package com.rohith.rohithmart.dto;

import java.sql.Timestamp;

/**
 * Data Transfer Object for Product Reviews and Star Ratings.
 * Extends ReviewResponse for full backward compatibility.
 */
public class ReviewDTO extends ReviewResponse {
    public ReviewDTO() {
        super();
    }

    public ReviewDTO(Long id, Long productId, Long userId, String userName,
                     Integer rating, String comment, Timestamp createdAt) {
        super(id, productId, userId, userName, rating, comment, createdAt);
    }
}
