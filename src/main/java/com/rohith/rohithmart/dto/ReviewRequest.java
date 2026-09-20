package com.rohith.rohithmart.dto;

import java.util.Objects;

/**
 * Request DTO for submitting a product review.
 */
public class ReviewRequest {
    private Long productId;
    private Integer rating;
    private String comment;

    public ReviewRequest() {
    }

    public ReviewRequest(Long productId, Integer rating, String comment) {
        this.productId = productId;
        this.rating = rating;
        this.comment = comment;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReviewRequest that = (ReviewRequest) o;
        return Objects.equals(productId, that.productId) &&
                Objects.equals(rating, that.rating) &&
                Objects.equals(comment, that.comment);
    }

    @Override
    public int hashCode() {
        return Objects.hash(productId, rating, comment);
    }

    @Override
    public String toString() {
        return "ReviewRequest{" +
                "productId=" + productId +
                ", rating=" + rating +
                ", comment='" + comment + '\'' +
                '}';
    }
}
