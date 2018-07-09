package com.bt.rsqe.projectengine.web.view.sorting;

import com.bt.rsqe.projectengine.web.model.LineItemModel;
import com.bt.rsqe.projectengine.web.view.filtering.PaginatedFilterResult;

public class PaginatedSortResult<T> {

    private PaginatedFilterResult<LineItemModel> paginatedFilterResult;

    public PaginatedSortResult(PaginatedFilterResult<LineItemModel> paginatedFilterResult) {
        this.paginatedFilterResult = paginatedFilterResult;
    }

    public PaginatedFilterResult<LineItemModel> getPaginatedFilterResultItems() {
        return paginatedFilterResult;
    }
}
