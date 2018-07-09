package com.bt.rsqe.projectengine.web.view.filtering.pricing;

import com.bt.rsqe.projectengine.web.model.LineItemModel;
import com.bt.rsqe.projectengine.web.view.filtering.FilterValues;
import com.bt.rsqe.projectengine.web.view.filtering.PaginatedFilter;
import com.bt.rsqe.projectengine.web.view.filtering.PaginatedFilterResult;
import com.bt.rsqe.projectengine.web.view.filtering.PaginatedPricingTabViewFilter;
import com.bt.rsqe.projectengine.web.view.pagination.Pagination;
import org.apache.commons.lang.builder.ToStringBuilder;

import java.util.ArrayList;
import java.util.List;

// TODO: Ensure unit tests exist for all methods of this class and it's sub classes.
/**
 * This class adds pagination support to the {@link PricingTabViewFilterNew}.
 *
 * Note: This class is intended as a complete replacement of the existing pricing filter {@link PaginatedPricingTabViewFilter} class.
 */
public class PaginatedPricingTabViewFilterNew extends PricingTabViewFilterNew implements PaginatedFilter<LineItemModel>
{
    private final Pagination pagination;

    /**
     * Build this class using the given {@link Pagination} class to filter the given values.
     * @param filterValues The values to Paginate.
     * @param pagination The class to use to perform the pagination.
     */
    public PaginatedPricingTabViewFilterNew(FilterValues filterValues, Pagination pagination)
    {
        super(filterValues);
        this.pagination = pagination;
    }

    /**
     * Apply pagination filter to given {@link LineItemModel}s.
     * @param items The models to apply pagination to.
     * @return The given models paginated.
     */
    @Override
    public PaginatedFilterResult<LineItemModel> applyTo(List<LineItemModel> items)
    {
        LOGGER.info("Applying pagination filter.");
        LOGGER.debug("Filter applied to models = {}", items);

        List<LineItemModel> pricedModels = new ArrayList<>(items.size());
        for (LineItemModel priceModel : items)
        {
            pricedModels.add(priceModel);
        }

        // Apply the Pricing filter, and return the filtered & paginated result.
        List<LineItemModel> filteredItems = filter(pricedModels);
        PaginatedFilterResult<LineItemModel> result = new PaginatedFilterResult<>(filteredItems.size(),
                                                                                  pagination.paginate(filteredItems),
                                                                                  items.size(),
                                                                                  pagination.getPageNumber());
        LOGGER.debug("Returning paginated result = {}", result);
        return result;
    }

    /**
     * Returns a string representation of this object.
     * @return a string representation of this object.
     */
    @Override
    public String toString()
    {
        String result = ToStringBuilder.reflectionToString(this);
        LOGGER.trace("Returning toString of this object = {}", result);
        return result;
    }
}