package com.bt.rsqe.projectengine.web.view.filtering;

import com.bt.rsqe.domain.QuoteOptionItemStatus;
import com.bt.rsqe.projectengine.web.model.LineItemModel;

import java.util.List;

public class DetailsTabViewFilter {

    protected static final String EXCLUDE_FAILED = "excludeFailed";
    private Filters<LineItemModel> filters = new Filters<LineItemModel>();

    public DetailsTabViewFilter(FilterValues filterValues) {
        if (!filterValues.getValue(EXCLUDE_FAILED).isEmpty()) {
            filters.add(new ExcludeFailedFilter(filterValues.getValue(EXCLUDE_FAILED)));
        }
        if(!filterValues.getValue(LineItemGlobalSearchFilter.GLOBAL_SEARCH_FILTER).isEmpty()) {
            filters.add(new LineItemGlobalSearchFilter(filterValues.getValue(LineItemGlobalSearchFilter.GLOBAL_SEARCH_FILTER)));
        }
    }

    public List<LineItemModel> filter(List<LineItemModel> lineItemModels) {
        return filters.apply(lineItemModels);
    }

    private class ExcludeFailedFilter implements  Filters.Filter<LineItemModel> {

        private boolean excludeFailed;

        public ExcludeFailedFilter(String filter) {
            if ("YES".equals(filter.toUpperCase())) {
                excludeFailed = true;
            }
        }

        @Override
        public boolean apply(LineItemModel lineItemModel) {
            if (excludeFailed && lineItemModel.getLineItemStatus() == QuoteOptionItemStatus.FAILED) {
                return false;
            } else {
                return true;
            }
        }
    }
}
