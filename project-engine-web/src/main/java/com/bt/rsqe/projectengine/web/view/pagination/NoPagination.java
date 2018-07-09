package com.bt.rsqe.projectengine.web.view.pagination;

import java.util.List;

public class NoPagination implements Pagination{
    @Override
    public int getPageNumber() {
        return 1;
    }

    @Override
    public <T> List<T> paginate(List<T> items) {
        return items;
    }
}
