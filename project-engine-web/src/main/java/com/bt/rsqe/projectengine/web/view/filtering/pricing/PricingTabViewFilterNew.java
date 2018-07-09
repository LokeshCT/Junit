package com.bt.rsqe.projectengine.web.view.filtering.pricing;

import com.bt.rsqe.projectengine.web.model.LineItemModel;
import com.bt.rsqe.projectengine.web.view.filtering.FilterValues;
import com.bt.rsqe.projectengine.web.view.filtering.Filters;
import com.bt.rsqe.projectengine.web.view.filtering.Filters.Filter;
import com.bt.rsqe.projectengine.web.view.filtering.PricingTabViewFilter;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

// TODO: Ensure unit tests exist for all methods of this class and it's sub classes.
/**
 * This class defines a list of filters used on the Pricing Tab.
 *
 * Implemented in this class are three {@link Filter} classes that filter {@link LineItemModel}s under the following conditions;
 *  GlobalSearchFilter   - Models searchable fields contain any of the search terms.
 *  ProductFilter        - Model has any of the of the given Product Names or Display Names.
 *  CountryFilter        - Model has any of the of the given Country Names.
 *  VendorDiscountFilter - Model has a given Vendor Discount Reference.
 *
 * The filters returns true to indicate that the given {@link LineItemModel} should be kept. I.e. It should NOT be filtered out.
 *
 * Note: This class is intended as a complete replacement for the {@link PricingTabViewFilter} class.
 */
public class PricingTabViewFilterNew
{
    public static final Logger LOGGER = LoggerFactory.getLogger(PricingTabViewFilterNew.class);

    private static final String GLOBAL_SEARCH          = "globalSearch";
    private static final String PRODUCT_COLUMN         = "product";
    private static final String COUNTRY_COLUMN         = "country";
    private static final String VENDOR_DISCOUNT_COLUMN = "vendorDiscount";

    // Regular expression to split on commas. Expression ignores any number of spaces to either side of the comma.
    private static final String REGEX_SPLIT_COMMA      = "\\s*,\\s*";
    private static final Pattern COMMA_PATTERN         = Pattern.compile(REGEX_SPLIT_COMMA);

    // Regular expression to split on double ampersands (&&). Expression ignores any number of spaces to either side of the ampersand.
    private static final String REGEX_SPLIT_AMPERSAND  = "\\s*&&\\s*";

    // The drop down boxes when empty will contain a null value. This null will be sent via the URL params as a null String.
    private static final String NULL_STRING            = "null";
    private static final Pattern AMPERSAND_PATTERN     = Pattern.compile(REGEX_SPLIT_AMPERSAND);

    private final Filters<LineItemModel> filters       = new Filters<>();
    private final FilterValues filterValues;

    /**
     * Builds a list of filters for the Pricing Tab.
     * Filters are only added to the filters field if they are non empty.
     * @param filterValues The values to add as filters.
     */
    public PricingTabViewFilterNew(FilterValues filterValues)
    {
        LOGGER.info("Determining which filters to add to the Quote Options Pricing Tab.");
        this.filterValues = filterValues;

        // Retrieve filter values.
        String global   = getFilterValues(GLOBAL_SEARCH);
        String product  = getFilterValues(PRODUCT_COLUMN);
        String country  = getFilterValues(COUNTRY_COLUMN);
        String discount = getFilterValues(VENDOR_DISCOUNT_COLUMN);

        // Append filters for any non-empty values.
        addGlobalFilter(splitOnPattern(global,   AMPERSAND_PATTERN));
        addProductFilter(splitOnPattern(product, COMMA_PATTERN));
        addCountryFilter(splitOnPattern(country, COMMA_PATTERN));

        if(isNotNullOrEmpty(discount))
        {
            LOGGER.debug("Added Vendor Discount filter.");
            filters.add(new VendorDiscountFilter(discount));
        }
    }

    /**
     * Gets the value input by the user in the given filter.
     * @param filter The filter to return the value(s) of.
     * @return The values associated to the given filter name.
     */
    public final String getFilterValues(String filter)
    {
        String result = this.filterValues.getValue(filter);
        LOGGER.debug("Returning search value: '{}' for filter: '{}'", result, filter);
        return result;
    }

    /**
     * Add a {@link GlobalSearchFilter} if the given list of search terms is non empty.
     * @param globalSearch The Collection of search terms to create a {@link GlobalSearchFilter} with.
     */
    public final void addGlobalFilter(Collection<String> globalSearch)
    {
        if (!globalSearch.isEmpty())
        {
            LOGGER.info("Global Search Filter Added.");
            filters.add(new GlobalSearchFilter(globalSearch));
        }
    }

    /**
     * Add a {@link ProductFilter} if the given list of products is non empty.
     * @param products The Collection of products to create a {@link ProductFilter} with.
     */
    public final void addProductFilter(Collection<String> products)
    {
        if (!products.isEmpty())
        {
            LOGGER.info("Product Filter Added.");
            filters.add(new ProductFilter(products));
        }
    }

    /**
     * Add a {@link CountryFilter} if the given list of countries is non empty.
     * @param countries The Collection of countries to create a {@link CountryFilter} with.
     */
    public final void addCountryFilter(Collection<String> countries)
    {
        if (!countries.isEmpty())
        {
            LOGGER.info("Country Filter Added.");
            filters.add(new CountryFilter(countries));
        }
    }

    /**
     * Split the given value on the given pattern.
     * @param value The value to split.
     * @param pattern The pattern to split the value on.
     * @return A Collection of Strings the value was split into, or an empty Collection if the split had no results.
     */
    public static Collection<String> splitOnPattern(String value, Pattern pattern)
    {
        LOGGER.debug("Attempting to split value = {} with pattern = {}", value, pattern);
        List<String> values = Collections.emptyList();
        if (isNotNullOrEmpty(value))
        {
            values = Arrays.asList(pattern.split(value));
        }
        LOGGER.debug("Returning split values = {}", values);
        return values;
    }

    /**
     * Filters the given models.
     * When called the models will be filtered based on the type of Filter to which they belong.
     * @param models The models to filter.
     * @return The models after filtering.
     */
    public List<LineItemModel> filter(List<LineItemModel> models)
    {
        List<LineItemModel> result = filters.apply(models);
        LOGGER.debug("Returning filtered Line Item Models = {}", result);
        return result;
    }

    /**
     * Determines if a String is NOT empty or equal to NULL_STRING.
     * @param value The value to test.
     * @return True if value is NOT empty or equal to NULL_STRING.
     */
    private static boolean isNotNullOrEmpty(String value)
    {
        boolean result = !isNullOrEmpty(value);
        LOGGER.debug("isNotNullOrEmpty check result is {}", result);
        return result;
    }

    /**
     * Determines if a String is empty or is a String that equals NULL_STRING.
     * @param value The value to test.
     * @return True if the String is empty or equals the NULL_STRING value.
     */
    public static boolean isNullOrEmpty(String value)
    {
        boolean result = value.isEmpty() || value.equals(NULL_STRING);
        LOGGER.debug("isNullOrEmpty check result is {}", result);
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