package com.bt.rsqe.projectengine.web.view.filtering.pricing;

import com.bt.rsqe.projectengine.web.model.LineItemModel;
import com.bt.rsqe.projectengine.web.view.filtering.Filters.Filter;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

/**
 * The Product Filter will determine if the given {@link LineItemModel} should be filtered out of the result set or not.
 * Any model whose Product Name or Display Name matches the productName that this filter is built with will be kept.
 * Any that do not will be filtered out.
 */
public class ProductFilter implements Filter<LineItemModel>
{
    public static final Logger LOGGER = LoggerFactory.getLogger(ProductFilter.class);

    private final Collection<String> productNames;

    /**
     * Build a Product Filter with the given product name.
     * @param productNames The product names to build this filter with.
     */
    ProductFilter(Collection<String> productNames)
    {
        LOGGER.debug("Building Product Filter with product names = {}", productNames);
        this.productNames = productNames;
    }

    /**
     * Determines if the given model should be filtered out of the result set or not.
     * This determination is based on whether the model's Product Name or Display Name fields are present in the productNames field
     * that this filter was built with. If they match they model will be kept, if they do not it will be filtered out.
     * @param model The model to apply this filter to.
     * @return True if the given model's Product Name or Display Name matches the productName field of this class. False otherwise.
     */
    @Override
    public boolean apply(LineItemModel model)
    {
        String displayName = model.getDisplayName();
        String productName = model.getProductName();
        LOGGER.debug("Applying Product Filter to Line Item Model Display Name = {}, Product Name = {}", displayName, productName);

        boolean result = productNames.contains(productName) || productNames.contains(displayName);
        LOGGER.debug("Model was filtered: {}", result);
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