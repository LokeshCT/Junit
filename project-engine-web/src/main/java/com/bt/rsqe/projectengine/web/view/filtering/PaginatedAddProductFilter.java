package com.bt.rsqe.projectengine.web.view.filtering;

import com.bt.rsqe.customerrecord.SiteDTO;
import com.bt.rsqe.enums.ProductAction;
import com.bt.rsqe.projectengine.web.view.pagination.Pagination;

import java.util.List;

public class PaginatedAddProductFilter extends AddProductViewFilter implements PaginatedFilter<SiteDTO> {
    private Pagination pagination;

    public PaginatedAddProductFilter(FilterValues filterValues, Pagination pagination, ProductAction action) {
        super(filterValues, action);
        this.pagination = pagination;
    }

    @Override
    public PaginatedFilterResult applyTo(List<SiteDTO> itemRowDTOs) {
        final List<SiteDTO> filteredItems = super.filter(itemRowDTOs);
        return new PaginatedFilterResult<SiteDTO>(filteredItems.size(),
                                                                 pagination.paginate(filteredItems),
                                                                 itemRowDTOs.size(),
                                                                 pagination.getPageNumber());
    }

}


