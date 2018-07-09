package com.bt.rsqe.projectengine.web.view.filtering;

import com.bt.rsqe.projectengine.web.model.LineItemModel;
import com.bt.rsqe.projectengine.web.view.pagination.Pagination;

import java.util.List;

public class PaginatedDetailsTabViewFilter extends DetailsTabViewFilter implements PaginatedFilter<LineItemModel> {

    private Pagination pagination;

    public PaginatedDetailsTabViewFilter(FilterValues filterValues, Pagination pagination) {
        super(filterValues);
        this.pagination = pagination;
    }

    @Override
    public PaginatedFilterResult<LineItemModel> applyTo(List<LineItemModel> lineItemModels) {
        final List<LineItemModel> filteredItems = super.filter(lineItemModels);
        return new PaginatedFilterResult<LineItemModel>(filteredItems.size(),
                                                             pagination.paginate(filteredItems),
                                                             lineItemModels.size(),
                                                             pagination.getPageNumber());
    }
}
