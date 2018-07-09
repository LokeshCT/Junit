package com.bt.rsqe.projectengine.web.view.sorting;

import com.bt.rsqe.projectengine.web.model.LineItemModel;
import com.bt.rsqe.projectengine.web.view.filtering.PaginatedFilterResult;
import com.google.common.collect.ComparisonChain;

import java.util.*;

import static com.bt.rsqe.domain.project.PricingStatus.NOT_APPLICABLE;
import static com.bt.rsqe.projectengine.LineItemValidationResultDTO.Status.PENDING;

public class PaginatedDetailsTabViewSort implements PaginatedSort<LineItemModel> {

    private SortValues sortValues;

    public PaginatedDetailsTabViewSort(SortValues sortValues) {
        this.sortValues = sortValues;
    }

    @Override
    public PaginatedSortResult<LineItemModel> applyTo(PaginatedFilterResult<LineItemModel> paginatedFilterResult) {
        final List<LineItemModel> sortedItems = sortedItems(paginatedFilterResult.getItems());
        paginatedFilterResult.setItems(sortedItems);
        return new PaginatedSortResult<LineItemModel>(paginatedFilterResult);
    }

    public List<LineItemModel> sortedItems(List<LineItemModel> lineItemModels) {
        final int sortColumnIndex = Integer.parseInt(sortValues.getValue(sortValues.getSortColumnIndexString()));
        final int sortDirection = sortValues.getValue(sortValues.getSortDirectionString()).equals("asc") ? 1 : -1;

        Collections.sort(lineItemModels, new Comparator<LineItemModel>(){
            @Override
            public int compare(LineItemModel l1, LineItemModel l2) {
                switch(sortColumnIndex){
                    case 2:
                        return l1.getDisplayName().compareTo(l2.getDisplayName()) * sortDirection;
                    case 0:
                    case 1:
                        return sortBySiteAndProduct(l1, l2, sortDirection);
                    case 3:
                        return l1.getSummary().compareTo(l2.getSummary()) * sortDirection;
                    case 4:
                        return l1.getAction().compareTo(l2.getAction()) * sortDirection;
                    case 5:
                        return l1.getContractTerm().compareTo(l2.getContractTerm()) * sortDirection;
                    case 6:
                        return l1.getOfferName().compareTo(l2.getOfferName()) * sortDirection;
                    case 7:
                        return l1.getStatus().compareTo(l2.getStatus()) * sortDirection;
                    case 8:
                        return l1.getDiscountStatus().compareTo(l2.getDiscountStatus()) * sortDirection;
                    case 9:
                        String l1priceStatus = l1.isInitialised() ? l1.getPricingStatusOfTree().getDescription() : NOT_APPLICABLE.getDescription();
                        String l2priceStatus = l2.isInitialised() ? l2.getPricingStatusOfTree().getDescription() : NOT_APPLICABLE.getDescription();
                        return l1priceStatus.compareTo(l2priceStatus) * sortDirection;
                    case 10:
                        return l1.getOrderStatus().compareTo(l2.getOrderStatus()) * sortDirection;
                    case 11:
                        String l1validation = l1.isInitialised() ? l1.getValidity() : PENDING.name();
                        String l2validation = l2.isInitialised() ? l2.getValidity() : PENDING.name();
                        return l1validation.compareTo(l2validation) * sortDirection;
                }
                return 0;
            }
        });

        return lineItemModels;
    }

    private int sortBySiteAndProduct(LineItemModel l1, LineItemModel l2, int sortDirection) {
        String leftDisplayName = sortDirection == 1 ? l1.getDisplayName() : l2.getDisplayName();
        String rightDisplayName = sortDirection == 1 ? l2.getDisplayName() : l1.getDisplayName();

        return ComparisonChain.start()
                              .compare(l1.getSite().name, l2.getSite().name)
                              .compare(leftDisplayName, rightDisplayName, String.CASE_INSENSITIVE_ORDER)
                              .result() * sortDirection;
    }
}
