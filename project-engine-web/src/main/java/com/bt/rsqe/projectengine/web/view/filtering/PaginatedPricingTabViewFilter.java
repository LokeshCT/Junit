package com.bt.rsqe.projectengine.web.view.filtering;

import com.bt.rsqe.projectengine.web.model.LineItemModel;
import com.bt.rsqe.projectengine.web.view.pagination.Pagination;

import java.util.ArrayList;
import java.util.List;

// TODO: Remove once new pricing filter is complete.
public class PaginatedPricingTabViewFilter extends PricingTabViewFilter implements PaginatedFilter<LineItemModel> {

    private Pagination pagination;

    public PaginatedPricingTabViewFilter(FilterValues filterValues, Pagination pagination) {
        super(filterValues);
        this.pagination = pagination;
    }

    @Override
    public PaginatedFilterResult<LineItemModel> applyTo(List<LineItemModel> lineItemModels) {
        List<LineItemModel> pricedModels = new ArrayList<LineItemModel>();
        for (LineItemModel priceModel : lineItemModels) {
            pricedModels.add(priceModel);
        }
        final List<LineItemModel> filteredItems = super.filter(pricedModels);
        return new PaginatedFilterResult<LineItemModel>(filteredItems.size(),
                                                        pagination.paginate(filteredItems),
                                                        lineItemModels.size(),
                                                        pagination.getPageNumber());
    }
}
