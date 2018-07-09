package com.bt.rsqe.projectengine.web.view.pagination;

import java.util.List;

public interface Pagination {
    int getPageNumber();
    <T> List<T> paginate(List<T> itemRowDTOs);
}
