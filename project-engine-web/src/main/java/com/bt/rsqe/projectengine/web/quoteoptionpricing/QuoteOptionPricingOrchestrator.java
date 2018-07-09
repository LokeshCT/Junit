package com.bt.rsqe.projectengine.web.quoteoptionpricing;

import com.bt.rsqe.dto.BidManagerCommentsDTO;
import com.bt.rsqe.enums.PriceType;
import com.bt.rsqe.projectengine.QuoteOptionDTO;
import com.bt.rsqe.projectengine.web.facades.BidManagerCommentsFacade;
import com.bt.rsqe.projectengine.web.facades.LineItemFacade;
import com.bt.rsqe.projectengine.web.facades.ProductIdentifierFacade;
import com.bt.rsqe.projectengine.web.facades.QuoteOptionFacade;
import com.bt.rsqe.projectengine.web.facades.SiteFacade;
import com.bt.rsqe.projectengine.web.model.LineItemModel;
import com.bt.rsqe.projectengine.web.model.PriceLineModel;
import com.bt.rsqe.projectengine.web.model.lineitemvisitor.AbstractLineItemVisitor;
import com.bt.rsqe.projectengine.web.model.lineitemvisitor.PricingDTOItemRowVisitor;
import com.bt.rsqe.projectengine.web.model.lineitemvisitor.UsagePricingDTOItemRowVisitor;
import com.bt.rsqe.projectengine.web.uri.UriFactoryImpl;
import com.bt.rsqe.projectengine.web.view.Products;
import com.bt.rsqe.projectengine.web.view.QuoteOptionPricingDTO;
import com.bt.rsqe.projectengine.web.view.QuoteOptionUsagePricingDTO;
import com.bt.rsqe.projectengine.web.view.filtering.PaginatedFilter;
import com.bt.rsqe.projectengine.web.view.filtering.PaginatedFilterResult;
import com.bt.rsqe.projectengine.web.view.filtering.PricingTabView;
import com.bt.rsqe.projectengine.web.view.filtering.PricingTabViewNew;
import com.bt.rsqe.web.Presenter;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import org.apache.commons.collections.CollectionUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.*;
import static java.util.Collections.*;

public class QuoteOptionPricingOrchestrator
{
    private static final Logger LOGGER = LoggerFactory.getLogger(QuoteOptionPricingOrchestrator.class);

    private static final String CONNECT_ACCELERATION_SITE_PRODUCT       = "Connect Acceleration Site";
    private static final String CONNECT_ACCELERATION_SERVICE_PRODUCT    = "Connect Acceleration service";
    private static final String MODIFY_ACTION                           = "Modify";
    private static final String CEASE_ACTION                            = "Cease";

    private ProductIdentifierFacade productIdentifierFacade;
    private LineItemFacade lineItemFacade;
    private QuoteOptionFacade quoteOptionFacade;
    private SiteFacade siteFacade;
    private BidManagerCommentsFacade bidManagerCommentsFacade;

    public QuoteOptionPricingOrchestrator(ProductIdentifierFacade productIdentifierFacade, LineItemFacade lineItemFacade, QuoteOptionFacade quoteOptionFacade, SiteFacade siteFacade, BidManagerCommentsFacade bidManagerCommentsFacade) {
        this.productIdentifierFacade = productIdentifierFacade;
        this.lineItemFacade = lineItemFacade;
        this.quoteOptionFacade = quoteOptionFacade;
        this.siteFacade = siteFacade;
        this.bidManagerCommentsFacade = bidManagerCommentsFacade;
    }

    public QuoteOptionPricingDTO buildStandardResponse(String customerId,
                                                       String contractId,
                                                       String projectId,
                                                       String quoteOptionId,
                                                       PaginatedFilter paginatedFilter,
                                                       PriceSuppressStrategy priceSuppressStrategy) {
        final PaginatedFilterResult<LineItemModel> filterResult = getFilteredLineItems(customerId,
                                                                                       contractId,
                                                                                       projectId,
                                                                                       quoteOptionId,
                                                                                       paginatedFilter,
                                                                                       priceSuppressStrategy);

        List<QuoteOptionPricingDTO.ItemRowDTO> items = new ArrayList<QuoteOptionPricingDTO.ItemRowDTO>();
        acceptLineItems(filterResult, new PricingDTOItemRowVisitor(items));
        return new QuoteOptionPricingDTO(items, filterResult);
    }

    public QuoteOptionUsagePricingDTO buildUsageResponse(String customerId,
                                                         String contractId,
                                                         String projectId,
                                                         String quoteOptionId,
                                                         PaginatedFilter paginatedFilter,
                                                         PriceSuppressStrategy priceSuppressStrategy) {
        final PaginatedFilterResult<LineItemModel> filterResult = getFilteredLineItems(customerId,
                                                                                       contractId,
                                                                                       projectId,
                                                                                       quoteOptionId,
                                                                                       paginatedFilter,
                                                                                       priceSuppressStrategy);

        List<QuoteOptionUsagePricingDTO.UsageProduct> products = newArrayList();
        acceptLineItems(filterResult, new UsagePricingDTOItemRowVisitor(products));
        removeProductsWithNoPriceLines(products);
        return new QuoteOptionUsagePricingDTO(products, filterResult);
    }

    public List<LineItemModel> getLineItemModels(String customerId, String contractId, String projectId, String quoteOptionId) {
        return  lineItemFacade.fetchLineItems(customerId, contractId, projectId, quoteOptionId, PriceSuppressStrategy.None);
    }

    // TODO: Remove once pricing design implementation is complete.
    public PricingTabView getPricingTabView(String customerId, String contractId, String projectId, String quoteOptionId) {
        final Products products = productIdentifierFacade.getAllSellableProducts();
        final QuoteOptionDTO quoteOptionDTO = quoteOptionFacade.get(projectId, quoteOptionId);
        List<LineItemModel> lineItemModels = getLineItemModels(customerId, contractId, projectId, quoteOptionId);
        final boolean costDiscountApplicable = isCostDiscountApplicable(lineItemModels);
        ObjectMapper mapper = new ObjectMapper();
        Map<String, String> isManualModify = new HashMap<String, String>();
        for (LineItemModel lineItemModel : lineItemModels) {
            if ((lineItemModel.getProductName().equals("Connect Acceleration Site")
                 || lineItemModel.getProductName().equals("Connect Acceleration service"))
                && (lineItemModel.getAction().equals("Modify") || lineItemModel.getAction().equals("Cease"))) {
                isManualModify.put(lineItemModel.getLineItemId().toString(), lineItemModel.getAction());
            }

        }

        final boolean priceLinesLocked = quoteOptionHasLockedPriceLines(lineItemModels);
        final String currency = quoteOptionDTO.currency;
        final String activityId = quoteOptionDTO.activityId;
        String jsonObject = "";
        try {
           jsonObject = mapper.defaultPrettyPrintingWriter().writeValueAsString(isManualModify);
        } catch (IOException e) {
            e.printStackTrace();  //TODO: Auto-generated
        }
        List<BidManagerCommentsDTO> commentsAndCaveats = bidManagerCommentsFacade.getBidManagerComments(projectId, quoteOptionId);
        String costAttachmentUrl = UriFactoryImpl.attachmentDialogForm(customerId, contractId, projectId, quoteOptionId, true).toString();

        return new PricingTabView(products.getAllNames(),
                                  currency,
                                  siteFacade.getCountries(customerId, projectId),
                                  UriFactoryImpl.exportPricingSheet(customerId, contractId, projectId, quoteOptionId, "").toString(),
                                  UriFactoryImpl.pricingActionsUri(customerId, contractId, projectId, quoteOptionId).toString(),
                                  priceLinesLocked,
                                  activityId == null ? hasDiscountableLineItems(lineItemModels) : false,
                                                                  jsonObject,
                                  UriFactoryImpl.quoteOptionBcm(customerId, contractId, projectId, quoteOptionId).toString(),
                                  costAttachmentUrl, quoteOptionDTO, commentsAndCaveats,costDiscountApplicable);
    }

    private boolean isCostDiscountApplicable(List<LineItemModel> lineItemModels)
    {
        for(LineItemModel lineItemModel : lineItemModels) {
            for(PriceLineModel priceLineModel : lineItemModel.getFutureAssetPricesModel().getDeepFlattenedPriceLines()) {
                if (priceLineModel.isDiscountApplicable(priceLineModel.getPriceLineDTO(PriceType.RECURRING), priceLineModel.getScheme())) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean quoteOptionHasLockedPriceLines(List<LineItemModel> lineItemModels) {
        boolean quoteOptionContainsLockedPriceLines = false;
        for (LineItemModel lineItemModel : lineItemModels) {
            if (lineItemModel.priceLinesCanBeUnLocked()) {
                quoteOptionContainsLockedPriceLines = true;
                break;
            }
        }
        return quoteOptionContainsLockedPriceLines;
    }

    private static boolean hasDiscountableLineItems(List<LineItemModel> lineItemModels) {
        for (LineItemModel lineItemModel : lineItemModels) {
            if (!lineItemModel.isReadOnly()) {
                return true;
            }
        }
        return false;
    }

    private PaginatedFilterResult<LineItemModel> getFilteredLineItems(String customerId,
                                                                      String contractId,
                                                                      String projectId,
                                                                      String quoteOptionId,
                                                                      PaginatedFilter paginatedFilter,
                                                                      PriceSuppressStrategy priceSuppressStrategy) {
        final List<LineItemModel> lineItemModels = lineItemFacade.fetchLineItems(customerId, contractId, projectId, quoteOptionId, priceSuppressStrategy);
        final PaginatedFilterResult<LineItemModel> filterResult = paginatedFilter.applyTo(newArrayList(Iterables.filter(lineItemModels, new Predicate<LineItemModel>() {
            @Override
            public boolean apply(LineItemModel input) {
                return input.isProductVisibleOnlineSummary();
            }
        })));

        return filterResult;
    }

    private void acceptLineItems(PaginatedFilterResult<LineItemModel> filterResult, AbstractLineItemVisitor visitor) {
        List<LineItemModel> lineItemModelList = filterResult.getItems();
        Comparator<LineItemModel> lineItemsOrder = new Comparator<LineItemModel>() {
            @Override
            public int compare(LineItemModel o1, LineItemModel o2) {
                int productNameComparator = o1.getProductName().compareTo(o2.getProductName());
                if(productNameComparator == 0){
                    return o1.getSite().bfgSiteID.compareTo(o2.getSite().bfgSiteID);
                }else {
                    return productNameComparator;
                }
            }
        };
        sort(lineItemModelList,lineItemsOrder);
        for (LineItemModel lineItemModel : lineItemModelList) {
            if(lineItemModel.isLineItemHasPriceLines()){
            lineItemModel.accept(visitor);
            }
        }
    }

    private void removeProductsWithNoPriceLines(List<QuoteOptionUsagePricingDTO.UsageProduct> products) {
        Iterator<QuoteOptionUsagePricingDTO.UsageProduct> productIterator = products.iterator();
        while(productIterator.hasNext()) {
            QuoteOptionUsagePricingDTO.UsageProduct product = productIterator.next();
            if(product.priceLines.isEmpty()) {
                productIterator.remove();
            }
        }
    }

    // FIXME: CHANGE Products values to be only root products.
    /**
     * Collects the data used by the Pricing Tab into a {@link PricingTabViewNew} POJO which can then be rendered by {@link Presenter}.
     * This is intended as a replacement for {@see getPricingTabView and @see PricingTabView} which implement the features of the pricing
     * redesign.
     * @param customerId ID of the Customer.
     * @param contractId ID of the Customer's contract.
     * @param projectId ID of the project.
     * @param quoteOptionId ID of the quote option.
     * @return A {@link PricingTabViewNew} that can be rendered to display the Pricing Tab.
     */
    public PricingTabViewNew getPricingTabViewNew(String customerId, String contractId, String projectId, String quoteOptionId)
    {
        LOGGER.info("Retrieving Pricing Tab View.");
        LOGGER.debug("getPricingTabViewNew method called with parameters; Customer ID = {}, Contract ID = {}, Project ID = {}, Quote Option ID = {}",
                customerId, contractId, projectId, quoteOptionId);

        QuoteOptionDTO quoteOptionDTO                   = quoteOptionFacade.get(projectId, quoteOptionId);
        List<LineItemModel> lineItems                   = getLineItemModels(customerId, contractId, projectId, quoteOptionId);
        List<BidManagerCommentsDTO> commentsAndCaveats  = bidManagerCommentsFacade.getBidManagerComments(projectId, quoteOptionId);

        PricingTabViewNew view =
                new PricingTabViewNew(generatePriceLineProductList(lineItems),
                                      quoteOptionDTO.currency,
                                      generatePriceLineCountriesList(lineItems),
                                      UriFactoryImpl.exportPricingSheet(customerId, contractId, projectId, quoteOptionId, "").toString(),
                                      UriFactoryImpl.pricingActionsUri(customerId, contractId, projectId, quoteOptionId).toString(),
                                      quoteOptionHasLockedPriceLines(lineItems),
                                      isRequestDiscountAllowed(quoteOptionDTO.activityId, lineItems),
                                      generateIsManualModify(lineItems),
                                      UriFactoryImpl.quoteOptionBcm(customerId,
                                                                    contractId,
                                                                    projectId,
                                                                    quoteOptionId).toString(),
                                      UriFactoryImpl.attachmentDialogForm(customerId, contractId, projectId, quoteOptionId, true).toString(),
                                      quoteOptionDTO,
                                      commentsAndCaveats,
                                      isCostDiscountApplicable(lineItems));

        LOGGER.debug("Returning Pricing Tab View = {}", view);
        return view;
    }

    /**
     * Generates a list of unique product display names for the given line items.
     * Intended for use with the Pricing Tabs filter.
     * @param lineItems The line items to generate the the product name list from.
     * @return A list of distinct product display names belonging to the given price lines, or an empty list if the input was null or empty.
     */
    public static Collection<String> generatePriceLineProductList(Collection<LineItemModel> lineItems)
    {
        LOGGER.info("Generating list of price line product display names for display on pricing tab.");

        // Handle null or empty inputs.
        if (CollectionUtils.isEmpty(lineItems))
        {
            LOGGER.info("Input was empty or null. Returning empty result.");
            return emptyList();
        }

        // Using a Set here to ensure distinct values only.
        Collection<String> productNames = new HashSet<>(lineItems.size());
        for (LineItemModel lineItem : lineItems)
        {
            if (lineItem.isInFrontCatlogueProduct())
            {
                String productName = lineItem.getDisplayName();
                productNames.add(productName);
                LOGGER.debug("Product = {}. Is Front Catalog Product and has been added to list of Products to display in Product drop down.", productName);
            }
        }

        LOGGER.debug("Returning list of price line product names = {}", productNames);
        return productNames;
    }

    /**
     * Generates a list of unique countries names for the given line items.
     * Intended for use with the Pricing Tabs filter.
     * @param lineItems The line items to generate the the countries list from.
     * @return A list of distinct country names belonging to the given price lines, or an empty list if the input was null or empty.
     */
    public static Collection<String> generatePriceLineCountriesList(Collection<LineItemModel> lineItems)
    {
        LOGGER.info("Generating list of price line country names for display on pricing tab.");

        // Handle null or empty inputs.
        if (CollectionUtils.isEmpty(lineItems))
        {
            LOGGER.info("Input was empty or null. Returning empty result.");
            return emptyList();
        }

        // Using a Set here to ensure distinct values only.
        Collection<String> countryNames = new HashSet<>(lineItems.size());
        for (LineItemModel lineItem : lineItems)
        {
            String countryName = lineItem.getSite().getCountry();
            countryNames.add(countryName);
        }

        LOGGER.debug("Returning list of price line country names = {}", countryNames);
        return countryNames;
    }

    // FIXME: Remove this product specific code.
    /**
     * Checks the line items for the Connect Acceleration Site and Connect Acceleration Service products, if found and assigned the modify or cease
     * actions. The ID and the action of the line item are stored into a map for return. The map is serialized before returning.
     * @param lineItems The line items to check for the product/actions.
     * @return A map of line Item ID -> Line Item Action serialized into a String.
     */
    public static String generateIsManualModify(Collection<LineItemModel> lineItems)
    {
        LOGGER.debug("Checking line items for Connect Acceleration Site and Connect Acceleration Service products, with modify or cease actions.");
        Map<String, String> isManualModify = new HashMap<>(1);

        // Handle null or empty inputs.
        if (CollectionUtils.isEmpty(lineItems))
        {
            LOGGER.info("Input was empty or null. Returning empty result.");
            return serializeString(isManualModify);
        }

        for (LineItemModel lineItemModel : lineItems)
        {
            // Only update the line items if the line item is a specific product/action combination.
            if (isConnectAccelerationModifyOrCease(lineItemModel))
            {
                isManualModify.put(lineItemModel.getLineItemId().toString(), lineItemModel.getAction());
            }
        }

        // Serialize and return.
        String isManualModifyString = serializeString(isManualModify);
        LOGGER.debug("Returning isManualModify as serialized String = {}", isManualModifyString);
        return isManualModifyString;
    }

    /**
     * Determines if the request discount is allowed for this quote.
     * @param activityId The activity ID of the the Quote.
     * @param lineItemModels The Line items belonging to this quote.
     * @return True if the discount request is allowed, false otherwise.
     */
    public static boolean isRequestDiscountAllowed(String activityId, List<LineItemModel> lineItemModels)
    {
        return activityId == null && !hasDiscountableLineItems(lineItemModels);
    }

    // FIXME: Remove this product specific code.
    /**
     * Determines if the given line item's product name equals either CONNECT_ACCELERATION_SITE_PRODUCT or CONNECT_ACCELERATION_SERVICE_PRODUCT,
     * and it's action field equals the MODIFY_ACTION or CEASE_ACTION.
     * @param lineItem The line item to check.
     * @return True if the line item's product product name equals either CONNECT_ACCELERATION_SITE_PRODUCT or CONNECT_ACCELERATION_SERVICE_PRODUCT,
     *         and it's action field equals the MODIFY_ACTION or CEASE_ACTION. False otherwise.
     */
    public static boolean isConnectAccelerationModifyOrCease(LineItemModel lineItem)
    {
        return isConnectAcceleration(lineItem) && isModifyOrCease(lineItem);
    }

    // FIXME: Remove this product specific code.
    /**
     * Determine if the given line item's product name is either of CONNECT_ACCELERATION_SITE_PRODUCT or CONNECT_ACCELERATION_SERVICE_PRODUCT.
     * @param lineItem The line item to check.
     * @return True if the given line item's product name is CONNECT_ACCELERATION_SITE_PRODUCT or CONNECT_ACCELERATION_SERVICE_PRODUCT. False otherwise.
     */
    public static boolean isConnectAcceleration(LineItemModel lineItem)
    {
        return CONNECT_ACCELERATION_SITE_PRODUCT.equals(lineItem.getProductName()) || CONNECT_ACCELERATION_SERVICE_PRODUCT.equals(lineItem.getProductName());
    }

    // FIXME: Remove this product specific code.
    /**
     * Determine if the given line item's action is equal to either MODIFY_ACTION or CEASE_ACTION.
     * @param lineItem The line item to check.
     * @return True if the given line item's action is equal to either MODIFY_ACTION or CEASE_ACTION.
     */
    public static boolean isModifyOrCease(LineItemModel lineItem)
    {
        return MODIFY_ACTION.equals(lineItem.getAction()) || CEASE_ACTION.equals(lineItem.getAction());
    }

    /**
     * Takes a map and serializes it into a JSON String.
     * @param map A map of two Strings that should be concatenated together.
     *            Example:
     *            Input: Map<id1, Modify>
     *            Output:
     *            {
     *              "id3" : "Modify"
     *            }
     * @return The map serialized into a JSON String.
     */
    public static String serializeString(Map<String, String> map)
    {
        LOGGER.debug("Serializing Map = {} to JSON String.", map);

        ObjectMapper mapper = new ObjectMapper();
        ObjectWriter writer = mapper.writerWithDefaultPrettyPrinter();

        // Attempt to write the value as a String.
        String json = "";
        try
        {
            json = writer.writeValueAsString(map);
        }
        catch (IOException e)
        {
            LOGGER.debug("IOException caught whilst attempting to serialize map = {}.", map, e);
        }

        LOGGER.debug("Returning JSON String = {}", json);
        return json;
    }

}