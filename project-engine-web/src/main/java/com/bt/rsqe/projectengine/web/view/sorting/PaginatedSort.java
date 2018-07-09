package com.bt.rsqe.projectengine.web.view.sorting;

import com.bt.rsqe.projectengine.web.view.filtering.PaginatedFilterResult;

public interface PaginatedSort<T> {
    PaginatedSortResult<T> applyTo(PaginatedFilterResult<T> items);
}
