package com.rohith.rohithmart.dto;

import java.math.BigDecimal;

/**
 * Filter parameters for searching and querying products with pagination.
 */
public class ProductFilter {
    private String category;
    private String keyword;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private Boolean inStockOnly;
    private Long sellerId;
    private String sortBy; // "price_asc", "price_desc", "name_asc", "name_desc", "newest"
    private int page = 1;
    private int size = 20;

    public ProductFilter() {
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public BigDecimal getMinPrice() {
        return minPrice;
    }

    public void setMinPrice(BigDecimal minPrice) {
        this.minPrice = minPrice;
    }

    public BigDecimal getMaxPrice() {
        return maxPrice;
    }

    public void setMaxPrice(BigDecimal maxPrice) {
        this.maxPrice = maxPrice;
    }

    public Boolean getInStockOnly() {
        return inStockOnly;
    }

    public void setInStockOnly(Boolean inStockOnly) {
        this.inStockOnly = inStockOnly;
    }

    public Long getSellerId() {
        return sellerId;
    }

    public void setSellerId(Long sellerId) {
        this.sellerId = sellerId;
    }

    public String getSortBy() {
        return sortBy;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    public int getPage() {
        return Math.max(1, page);
    }

    public void setPage(int page) {
        this.page = Math.max(1, page);
    }

    public int getSize() {
        return size > 0 ? Math.min(size, 100) : 20;
    }

    public void setSize(int size) {
        this.size = size > 0 ? Math.min(size, 100) : 20;
    }

    public int getOffset() {
        return (getPage() - 1) * getSize();
    }

    public static class Builder {
        private final ProductFilter filter = new ProductFilter();

        public Builder category(String category) {
            filter.setCategory(category);
            return this;
        }

        public Builder keyword(String keyword) {
            filter.setKeyword(keyword);
            return this;
        }

        public Builder minPrice(BigDecimal minPrice) {
            filter.setMinPrice(minPrice);
            return this;
        }

        public Builder maxPrice(BigDecimal maxPrice) {
            filter.setMaxPrice(maxPrice);
            return this;
        }

        public Builder inStockOnly(Boolean inStockOnly) {
            filter.setInStockOnly(inStockOnly);
            return this;
        }

        public Builder sellerId(Long sellerId) {
            filter.setSellerId(sellerId);
            return this;
        }

        public Builder sortBy(String sortBy) {
            filter.setSortBy(sortBy);
            return this;
        }

        public Builder page(int page) {
            filter.setPage(page);
            return this;
        }

        public Builder size(int size) {
            filter.setSize(size);
            return this;
        }

        public ProductFilter build() {
            return filter;
        }
    }
}
