package com.rohith.rohithmart.dto;

import java.util.Objects;

/**
 * Data Transfer Object encapsulating validated pagination parameters.
 */
public class PaginationParams {
    public static final int DEFAULT_PAGE = 1;
    public static final int DEFAULT_PAGE_SIZE = 10;
    public static final int MAX_PAGE_SIZE = 100;

    private int page;
    private int pageSize;
    private int offset;

    public PaginationParams() {
        this(DEFAULT_PAGE, DEFAULT_PAGE_SIZE);
    }

    public PaginationParams(int page, int pageSize) {
        this.page = Math.max(1, page);
        this.pageSize = (pageSize <= 0) ? DEFAULT_PAGE_SIZE : Math.min(pageSize, MAX_PAGE_SIZE);
        this.offset = (this.page - 1) * this.pageSize;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = Math.max(1, page);
        this.offset = (this.page - 1) * this.pageSize;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = (pageSize <= 0) ? DEFAULT_PAGE_SIZE : Math.min(pageSize, MAX_PAGE_SIZE);
        this.offset = (this.page - 1) * this.pageSize;
    }

    public int getOffset() {
        return offset;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PaginationParams that = (PaginationParams) o;
        return page == that.page && pageSize == that.pageSize && offset == that.offset;
    }

    @Override
    public int hashCode() {
        return Objects.hash(page, pageSize, offset);
    }

    @Override
    public String toString() {
        return "PaginationParams{" +
                "page=" + page +
                ", pageSize=" + pageSize +
                ", offset=" + offset +
                '}';
    }
}
