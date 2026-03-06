package com.example.util;

import java.util.List;

public class PaginationResult<T> {
    private List<T> items;
    private int currentPage;
    private int pageSize;
    private long totalItems;
    private int totalPages;

    public PaginationResult(List<T> items, int currentPage, int pageSize, long totalItems) {
        this.items = items;
        this.currentPage = currentPage;
        this.pageSize = pageSize;
        this.totalItems = totalItems;
        this.totalPages = (int) Math.ceil((double) totalItems / pageSize);
    }

    public List<T> getItems() { return items; }
    public void setItems(List<T> items) { this.items = items; }

    public int getCurrentPage() { return currentPage; }
    public void setCurrentPage(int currentPage) { this.currentPage = currentPage; }

    public int getPageSize() { return pageSize; }
    public void setPageSize(int pageSize) { this.pageSize = pageSize; }

    public long getTotalItems() { return totalItems; }
    public void setTotalItems(long totalItems) { this.totalItems = totalItems; }

    public int getTotalPages() { return totalPages; }
    public void setTotalPages(int totalPages) { this.totalPages = totalPages; }

    public boolean hasNext() {
        return currentPage < totalPages;
    }

    public boolean hasPrevious() {
        return currentPage > 1;
    }

    public int getNextPage() {
        return hasNext() ? currentPage + 1 : currentPage;
    }

    public int getPreviousPage() {
        return hasPrevious() ? currentPage - 1 : currentPage;
    }

    @Override
    public String toString() {
        return "PaginationResult{" +
                "currentPage=" + currentPage +
                ", pageSize=" + pageSize +
                ", totalItems=" + totalItems +
                ", totalPages=" + totalPages +
                ", items=" + items.size() +
                '}';
    }
}