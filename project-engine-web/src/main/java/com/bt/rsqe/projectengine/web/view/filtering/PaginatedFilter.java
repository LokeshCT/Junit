package com.bt.rsqe.projectengine.web.view.filtering;

import java.util.List;

public interface PaginatedFilter<T> {
    PaginatedFilterResult applyTo(List<T> items);
}
