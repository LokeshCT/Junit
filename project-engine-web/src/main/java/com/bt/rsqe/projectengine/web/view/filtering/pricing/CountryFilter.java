package com.bt.rsqe.projectengine.web.view.filtering.pricing;

import com.bt.rsqe.projectengine.web.model.LineItemModel;
import com.bt.rsqe.projectengine.web.view.filtering.Filters.Filter;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

/**
 * The Country Filter will determine if the given {@link LineItemModel} should be filtered out of the result set or not.
 * Any model whose Country Name matches the country that this filter is built with will be kept.
 * Any that do not will be filtered out.
 */
public class CountryFilter implements Filter<LineItemModel>
{
    public static final Logger LOGGER = LoggerFactory.getLogger(CountryFilter.class);

    private final Collection<String> countries;

    /**
     * Build a Country Filter with the given country name.
     * @param countries The country names to build this filter with.
     */
    CountryFilter(Collection<String> countries)
    {
        LOGGER.debug("Building Country Filter with country names = {}", countries);
        this.countries = countries;
    }

    /**
     * Determines if the given model should be filtered out of the result set or not.
     * This determination is based on whether the model's country field is in the countries field that this filter was built with.
     * If they match they model will be kept, if they do not it will be filtered out.
     * @param model The model to apply this filter to.
     * @return True if the given model's country matches the country field of this class. False otherwise.
     */
    @Override
    public boolean apply(LineItemModel model)
    {
        String country = model.getSite().country;
        LOGGER.debug("Applying Country Filter to country = {}", country);
        boolean result = countries.contains(country);
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