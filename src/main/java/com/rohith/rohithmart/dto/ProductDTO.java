package com.rohith.rohithmart.dto;

import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * Data Transfer Object for Product listings and responses.
 */
public class ProductDTO {
    private Long id;
    private Long sellerId;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer stockQty;
    private String category;
    private String imageUrl;
    private Timestamp createdAt;

    public ProductDTO() {
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

    public static class Builder {
        private final ProductDTO dto = new ProductDTO();

        public Builder id(Long id) {
            dto.setId(id);
            return this;
        }

        public Builder sellerId(Long sellerId) {
            dto.setSellerId(sellerId);
            return this;
        }

        public Builder name(String name) {
            dto.setName(name);
            return this;
        }

        public Builder description(String description) {
            dto.setDescription(description);
            return this;
        }

        public Builder price(BigDecimal price) {
            dto.setPrice(price);
            return this;
        }

        public Builder stockQty(Integer stockQty) {
            dto.setStockQty(stockQty);
            return this;
        }

        public Builder category(String category) {
            dto.setCategory(category);
            return this;
        }

        public Builder imageUrl(String imageUrl) {
            dto.setImageUrl(imageUrl);
            return this;
        }

        public Builder createdAt(Timestamp createdAt) {
            dto.setCreatedAt(createdAt);
            return this;
        }

        public ProductDTO build() {
            return dto;
        }
    }
}
