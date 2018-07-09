package com.bt.rsqe.projectengine.web.view.filtering.pricing;

import com.bt.rsqe.projectengine.web.model.LineItemModel;
import com.bt.rsqe.projectengine.web.view.filtering.Filters.Filter;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * The Global Search Filter will determine if the given {@link LineItemModel} should be filtered out of the result set or not.
 * Any model that contains a field matching the searchTerms this class is built with will be kept.
 * Any that do not will be filtered out.
 * Note: Only fields that are visible on the front end are searched.
 */
public class GlobalSearchFilter implements Filter<LineItemModel>
{
    public static final Logger LOGGER = LoggerFactory.getLogger(GlobalSearchFilter.class);

    private final Collection<String> searchTerms;

    // The number of fields on the Pricing Tab's priceLines table that can be searched using the search box.
    private static final int SEARCHABLE_FIELDS              = 7;

    // The column names of the Pricing Tabs priceLine tables that should be searchable.
    private static final String SITE_COLUMN_NAME            = "site";
    private static final String SITE_ADDRESS_COLUMN_NAME    = "siteAddress";
    private static final String PRODUCT_COLUMN_NAME         = "productName";
    private static final String DISPLAY_NAME                = "displayName";
    private static final String SUMMARY_COLUMN_NAME         = "summary";
    private static final String DISCOUNT_STATUS_COLUMN_NAME = "discountStatus";
    private static final String STATUS_COLUMN_NAME          = "status";

    /**
     * Build a Product Filter with the given product name.
     * @param searchTerms The product names to build this filter with.
     */
    GlobalSearchFilter(Collection<String> searchTerms)
    {
        LOGGER.debug("Building Global Search Filter with search terms = {}", searchTerms);
        this.searchTerms = searchTerms;
    }

    /**
     * Determines if the given model should be filtered out of the result set or not.
     * This determination is based on whether the model contains a field with a value matching the searchTerms field that this filter
     * was built with. If they match they model will be kept, if they do not it will be filtered out.
     * @param model The model to apply this filter to.
     * @return True if model has a visible field containing one of the search terms. False otherwise.
     */
    @Override
    public boolean apply(LineItemModel model)
    {
        LOGGER.debug("Applying Global Search Filter to Line Item Model = {}", model);
        Map<String, String> fields = buildColumnValueMap(model);

        for (String field : fields.values())
        {
            // If any value returns true. Short circuit this method. No need to search the remaining fields.
            if (containsSearchTerm(field))
            {
                LOGGER.debug("Model included one or more search term = {}", searchTerms);
                return true;
            }
        }
        LOGGER.debug("Model did not include any of the search terms = {}", searchTerms);
        return false;
    }

    /**
     * Iterates over each of the search terms this class was built with, and returns true if the search term exists in the given field.
     * Ignores the case of both the models fields and the search terms.
     * @param field The field to search for the searchTerms this class was built with.
     * @return True if the given field contains any of the search terms, false otherwise.
     */
    private boolean containsSearchTerm(String field)
    {
        for (String searchTerm: searchTerms)
        {
            // If any value returns true. Short circuit this method. No need to search through the remaining search terms.
            if (field.toLowerCase().contains(searchTerm.toLowerCase()))
            {
                LOGGER.debug("Field: '{}' contains searchTerm = '{}'", field, searchTerm);
                return true;
            }
        }
        LOGGER.debug("Field: '{}' does not contain any of the searchTerms = '{}'", field, searchTerms);
        return false;
    }

    // TODO: Add the numeric columns
    // TODO: Should we have the offer column here as well? If so where to get the data displayed on page?
    /**
     * Builds a map of the column names and the cell values for the current model.
     * Any values in this map will be searchable. I.e. if the value against the following keys are also in the searchTerms field the
     * filter will return true for the current model.
     *
     * PriceLine Table Columns  ::  Searchable (Y/N)
     *  Site                             Y
     *  Site Address                     Y
     *  Product                          Y
     *  Summary                          Y
     *  Discount Status                  Y
     *  Offer                            N
     *  Description                      Y
     *  Status                           Y
     *
     * One Time Price - RRP
     *  Gross                            N
     *  Discount                         N
     *  Net                              N
     *
     * Recurring Price - RRP
     *  Gross                            N
     *  Discount                         N
     *  Net                              N
     *
     * @param model The model to build a map from.
     * @return A map in which the keys are column names from the priceLines table and the values are the cell values.
     */
    private static Map<String, String> buildColumnValueMap(LineItemModel model)
    {
        // Retrieve values to assign to keys.
        String site           = model.getSite().getSiteName();
        String siteAddress    = getSiteAddress(model);
        String productName    = model.getProductName();
        String displayName    = model.getDisplayName();
        String summary        = model.getSummary();
        String discountStatus = model.getDiscountStatus();
        String pricingStatus  = model.getPricingStatusOfTree().getSubStatus();

        Map<String, String> result = new HashMap<>(SEARCHABLE_FIELDS);

        result.put(SITE_COLUMN_NAME,            site);
        result.put(SITE_ADDRESS_COLUMN_NAME,    siteAddress);
        result.put(PRODUCT_COLUMN_NAME,         productName);
        result.put(DISPLAY_NAME,                displayName);
        result.put(SUMMARY_COLUMN_NAME,         summary);
        result.put(STATUS_COLUMN_NAME,          discountStatus);
        result.put(DISCOUNT_STATUS_COLUMN_NAME, pricingStatus);

        LOGGER.debug("Returning HashMap of column names and cell values = {}", result);
        return result;
    }

    /**
     * Construct the Site/Mini Address used on the UI.
     * The site address is the city and country concatenated separated by a comma.
     * @param model The model to build the Site Address for.
     * @return The Site Address for this model.
     */
    private static String getSiteAddress(LineItemModel model)
    {
        String city = model.getSite().getCity();
        String country = model.getSite().getCountry();
        String result = city + ", " + country;
        LOGGER.debug("Returning Site Address = {}", result);
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