package com.bt.rsqe.projectengine.web.view.filtering.pricing;

import com.bt.rsqe.projectengine.web.model.LineItemModel;
import com.bt.rsqe.projectengine.web.model.PriceLineModel;
import com.bt.rsqe.projectengine.web.view.filtering.Filters.Filter;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Vendor Discount Filter will determine if the given {@link LineItemModel} should be filtered out of the result set or not.
 * Any model whose Vendor Discount Reference matches the vendorDiscount that this filter is built with will be kept.
 * Any that do not will be filtered out.
 */
public class VendorDiscountFilter implements Filter<LineItemModel>
{
    public static final Logger LOGGER = LoggerFactory.getLogger(VendorDiscountFilter.class);

    private final String vendorDiscount;

    /**
     * Build a Vendor Discount Filter with the given vendor discount.
     * @param vendorDiscount The vendor discount to build this filter with.
     */
    VendorDiscountFilter(String vendorDiscount)
    {
        LOGGER.debug("Building Vendor Discount Filter with vendor Discount = {}", vendorDiscount);
        this.vendorDiscount = vendorDiscount;
    }

    /**
     * Determines if the given model should be filtered out of the result set or not.
     * This determination is based on whether the model's Vendor Discount Reference field matches the vendorDiscount field this filter was built with.
     * If they match they model will be kept, if they do not it will be filtered out.
     * @param model The model to apply this filter to.
     * @return True if the given model's Vendor Discount Reference matches the vendorDiscount field of this class. False otherwise.
     */
    @Override
    public boolean apply(LineItemModel model)
    {
        LOGGER.debug("Applying Vendor Discount Filter to Line Item Model = {}", model);
        for(PriceLineModel priceLineModel : model.getFutureAssetPricesModel().getDeepFlattenedPriceLines())
        {
            if(vendorDiscount.equals(priceLineModel.getPriceLineDTO().getVendorDiscountRef()))
            {
                LOGGER.debug("Model was filtered: true");
                return true;
            }
        }
        LOGGER.debug("Model was filtered: false");
        return false;
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