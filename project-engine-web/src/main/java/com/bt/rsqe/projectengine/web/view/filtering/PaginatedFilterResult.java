package com.bt.rsqe.projectengine.web.view.filtering;

import java.util.List;

public class PaginatedFilterResult<T> {
    private int filteredSize;
    private List<T> items;
    private int totalRecords;
    private int pageNumber;

    public PaginatedFilterResult(int filteredSize, List<T> items, int totalRecords, int pageNumber) {
        this.filteredSize = filteredSize;
        this.items = items;
        this.totalRecords = totalRecords;
        this.pageNumber = pageNumber;
    }

    public int getFilteredSize() {
        return filteredSize;
    }

    public List<T> getItems() {
        return items;
    }

    public int getTotalRecords() {
        return totalRecords;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public void setItems(List<T> items) {
        this.items = items;
    }
}
