package com.rohith.rohithmart.dto;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Objects;

/**
 * Response DTO representing a product listing.
 */
public class ProductResponse {
    private Long id;
    private Long sellerId;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer stockQty;
    private String category;
    private String imageUrl;
    private Timestamp createdAt;
    private Double averageRating = 0.0;
    private Integer reviewCount = 0;

    public ProductResponse() {
    }

    public ProductResponse(Long id, Long sellerId, String name, String description, BigDecimal price,
                           Integer stockQty, String category, String imageUrl, Timestamp createdAt) {
        this.id = id;
        this.sellerId = sellerId;
        this.name = name;
        this.description = description;
        this.price = price;
        this.stockQty = stockQty;
        this.category = category;
        this.imageUrl = imageUrl;
        this.createdAt = createdAt;
    }

    protected ProductResponse(Builder builder) {
        this.id = builder.id;
        this.sellerId = builder.sellerId;
        this.name = builder.name;
        this.description = builder.description;
        this.price = builder.price;
        this.stockQty = builder.stockQty;
        this.category = builder.category;
        this.imageUrl = builder.imageUrl;
        this.createdAt = builder.createdAt;
        this.averageRating = builder.averageRating != null ? builder.averageRating : 0.0;
        this.reviewCount = builder.reviewCount != null ? builder.reviewCount : 0;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSellerId() {
        return sellerId;
    }

    public void setSellerId(Long sellerId) {
        this.sellerId = sellerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getStockQty() {
        return stockQty;
    }

    public void setStockQty(Integer stockQty) {
        this.stockQty = stockQty;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Double getAverageRating() {
        return averageRating != null ? averageRating : 0.0;
    }

    public void setAverageRating(Double averageRating) {
        this.averageRating = averageRating;
    }

    public Integer getReviewCount() {
        return reviewCount != null ? reviewCount : 0;
    }

    public void setReviewCount(Integer reviewCount) {
        this.reviewCount = reviewCount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProductResponse that = (ProductResponse) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "ProductResponse{" +
                "id=" + id +
                ", sellerId=" + sellerId +
                ", name='" + name + '\'' +
                ", price=" + price +
                ", stockQty=" + stockQty +
                ", category='" + category + '\'' +
                ", createdAt=" + createdAt +
                ", averageRating=" + averageRating +
                ", reviewCount=" + reviewCount +
                '}';
    }

    public static class Builder {
        private Long id;
        private Long sellerId;
        private String name;
        private String description;
        private BigDecimal price;
        private Integer stockQty;
        private String category;
        private String imageUrl;
        private Timestamp createdAt;
        private Double averageRating = 0.0;
        private Integer reviewCount = 0;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder sellerId(Long sellerId) {
            this.sellerId = sellerId;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder price(BigDecimal price) {
            this.price = price;
            return this;
        }

        public Builder stockQty(Integer stockQty) {
            this.stockQty = stockQty;
            return this;
        }

        public Builder category(String category) {
            this.category = category;
            return this;
        }

        public Builder imageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
            return this;
        }

        public Builder createdAt(Timestamp createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder averageRating(Double averageRating) {
            this.averageRating = averageRating;
            return this;
        }

        public Builder reviewCount(Integer reviewCount) {
            this.reviewCount = reviewCount;
            return this;
        }

        public ProductResponse build() {
            return new ProductResponse(this);
        }
    }
}
