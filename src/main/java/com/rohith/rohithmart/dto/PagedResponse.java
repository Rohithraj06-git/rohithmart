package com.rohith.rohithmart.dto;

import java.util.Collections;
import java.util.List;

/**
 * Generic response wrapper for paginated API results.
 * Contains page metadata and list of items.
 *
 * @param <T> Item type
 */
public class PagedResponse<T> {
    private int currentPage;
    private int pageSize;
    private long totalResults;
    private int totalPages;
    private List<T> data;

    public PagedResponse() {
        this.data = Collections.emptyList();
    }

    public PagedResponse(int currentPage, int pageSize, long totalResults, int totalPages, List<T> data) {
        this.currentPage = currentPage;
        this.pageSize = pageSize;
        this.totalResults = totalResults;
        this.totalPages = totalPages;
        this.data = data != null ? data : Collections.emptyList();
    }

    public static <T> PagedResponse<T> of(List<T> data, int currentPage, int pageSize, long totalResults) {
        int effectiveSize = pageSize > 0 ? pageSize : 10;
        int totalPages = (int) Math.ceil((double) totalResults / effectiveSize);
        if (totalPages == 0) {
            totalPages = 1;
        }
        return new PagedResponse<>(currentPage, effectiveSize, totalResults, totalPages, data);
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public long getTotalResults() {
        return totalResults;
    }

    public void setTotalResults(long totalResults) {
        this.totalResults = totalResults;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public List<T> getData() {
        return data;
    }

    public void setData(List<T> data) {
        this.data = data;
    }

    public boolean hasNext() {
        return currentPage < totalPages;
    }

    public boolean hasPrevious() {
        return currentPage > 1;
    }
}
