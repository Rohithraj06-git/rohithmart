package com.rohith.rohithmart.dto;

import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * Data Transfer Object for Product listings and responses.
 * Extends canonical ProductResponse for backward compatibility.
 */
public class ProductDTO extends ProductResponse {

    public ProductDTO() {
        super();
    }

    public ProductDTO(Long id, Long sellerId, String name, String description, BigDecimal price,
                      Integer stockQty, String category, String imageUrl, Timestamp createdAt) {
        super(id, sellerId, name, description, price, stockQty, category, imageUrl, createdAt);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends ProductResponse.Builder {
        @Override
        public Builder id(Long id) {
            super.id(id);
            return this;
        }

        @Override
        public Builder sellerId(Long sellerId) {
            super.sellerId(sellerId);
            return this;
        }

        @Override
        public Builder name(String name) {
            super.name(name);
            return this;
        }

        @Override
        public Builder description(String description) {
            super.description(description);
            return this;
        }

        @Override
        public Builder price(BigDecimal price) {
            super.price(price);
            return this;
        }

        @Override
        public Builder stockQty(Integer stockQty) {
            super.stockQty(stockQty);
            return this;
        }

        @Override
        public Builder category(String category) {
            super.category(category);
            return this;
        }

        @Override
        public Builder imageUrl(String imageUrl) {
            super.imageUrl(imageUrl);
            return this;
        }

        @Override
        public Builder createdAt(Timestamp createdAt) {
            super.createdAt(createdAt);
            return this;
        }

        @Override
        public Builder averageRating(Double averageRating) {
            super.averageRating(averageRating);
            return this;
        }

        @Override
        public Builder reviewCount(Integer reviewCount) {
            super.reviewCount(reviewCount);
            return this;
        }

        @Override
        public ProductDTO build() {
            ProductResponse r = super.build();
            ProductDTO dto = new ProductDTO(r.getId(), r.getSellerId(), r.getName(), r.getDescription(),
                    r.getPrice(), r.getStockQty(), r.getCategory(), r.getImageUrl(), r.getCreatedAt());
            dto.setAverageRating(r.getAverageRating());
            dto.setReviewCount(r.getReviewCount());
            return dto;
        }
    }
}
