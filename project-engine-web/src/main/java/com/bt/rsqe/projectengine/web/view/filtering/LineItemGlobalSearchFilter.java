package com.bt.rsqe.projectengine.web.view.filtering;

import com.bt.rsqe.projectengine.web.model.LineItemModel;
import com.google.common.base.Strings;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

public class LineItemGlobalSearchFilter implements Filters.Filter<LineItemModel> {
    public static final String GLOBAL_SEARCH_FILTER = "globalSearch";

    private List<String> criteria;
    private boolean hasCriteria;

    public LineItemGlobalSearchFilter(String criteria) {
        this.hasCriteria = !Strings.isNullOrEmpty(criteria);
        if(hasCriteria) {
            this.criteria = newArrayList(criteria.split("&&"));
        }
    }

    @Override
    public boolean apply(LineItemModel lineItemModel) {
        if(!hasCriteria) {
            return true;
        }

        for(String criteriaPiece : criteria) {
            boolean match = match(lineItemModel.getSite().getSiteName(), criteriaPiece)
                                || match(lineItemModel.getProductName(), criteriaPiece)
                                || match(lineItemModel.getPricingStatusOfTree().getDescription(), criteriaPiece)
                                || match(lineItemModel.getSummary(), criteriaPiece);
            if(!match) {
                return false;
            }
        }

        return true;
    }

    private boolean match(String modelData, String criteria) {
        return null != modelData && modelData.toUpperCase().contains(criteria.trim().toUpperCase());
    }
}
