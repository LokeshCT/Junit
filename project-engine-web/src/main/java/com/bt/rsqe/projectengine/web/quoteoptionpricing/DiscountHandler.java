package com.bt.rsqe.projectengine.web.quoteoptionpricing;

import com.bt.rsqe.customerinventory.client.ProductInstanceClient;
import com.bt.rsqe.customerinventory.parameter.LineItemId;
import com.bt.rsqe.customerinventory.parameter.PriceLineStatus;
import com.bt.rsqe.domain.project.PricingStatus;
import com.bt.rsqe.pricing.AutoPriceAggregator;
import com.bt.rsqe.projectengine.ProjectResource;
import com.bt.rsqe.projectengine.QuoteOptionItemResource;
import com.bt.rsqe.projectengine.web.PriceHandlerService;
import com.bt.rsqe.projectengine.web.facades.FutureAssetPricesFacade;
import com.bt.rsqe.projectengine.web.facades.QuoteOptionFacade;
import com.bt.rsqe.projectengine.web.model.DiscountDelta;
import com.bt.rsqe.projectengine.web.model.FutureAssetPricesModel;
import com.bt.rsqe.projectengine.web.view.QuoteOptionCostDiscountDeltas;
import com.bt.rsqe.projectengine.web.view.QuoteOptionPricingDeltas;
import com.bt.rsqe.security.UserContextManager;
import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.common.collect.LinkedHashMultimap;
import com.google.gson.Gson;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.collect.Lists.*;
import static com.google.common.collect.Maps.*;
import static com.google.common.collect.Sets.*;

@Path("/rsqe/customers/{customerId}/projects/{projectId}/quote-options/{quoteOptionId}/discounts")
public class DiscountHandler {
    private static final String PROJECT_ID = "projectId";
    private static final String QUOTE_OPTION_ID = "quoteOptionId";
    private static final String CUSTOMER_ID = "customerId";
    private FutureAssetPricesFacade futureAssetPricesFacade;
    private QuoteOptionFacade quoteOptionFacade;
    private AutoPriceAggregator autoPriceAggregator;
    private ProjectResource projectResource;
    private PriceHandlerService priceHandlerService;
    private ProductInstanceClient instanceClient;

    public DiscountHandler(FutureAssetPricesFacade futureAssetPricesFacade,
                           QuoteOptionFacade quoteOptionFacade,
                           AutoPriceAggregator autoPriceAggregator,
                           ProjectResource projectResource,
                           PriceHandlerService priceHandlerService,
                           ProductInstanceClient instanceClient) {
        this.futureAssetPricesFacade = futureAssetPricesFacade;
        this.quoteOptionFacade = quoteOptionFacade;
        this.autoPriceAggregator = autoPriceAggregator;
        this.projectResource = projectResource;
        this.priceHandlerService = priceHandlerService;
        this.instanceClient = instanceClient;
    }


    @POST
    @Path("usage")
    @Produces(MediaType.APPLICATION_JSON)
    public Response applyUsageDiscounts(@PathParam(CUSTOMER_ID) String customerId,
                                        @PathParam(PROJECT_ID) String projectId,
                                        @PathParam(QUOTE_OPTION_ID) String quoteOptionId,
                                        String deltas) {
        QuoteOptionPricingDeltas pricingDeltas = new Gson().fromJson(deltas, QuoteOptionPricingDeltas.class);

        Set<LineItemId> affectedLineItems = newHashSet();
        for(QuoteOptionPricingDeltas.QuoteOptionPricingDelta delta : pricingDeltas.getQuoteOptionPricingDeltas()) {
            affectedLineItems.add(new LineItemId(delta.getLineItemId()));
        }

        final List<FutureAssetPricesModel> lineItems = futureAssetPricesFacade.getForLineItems(customerId,
                                                                                               projectId,
                                                                                               quoteOptionId,
                                                                                               newArrayList(affectedLineItems));

        for(FutureAssetPricesModel lineItem : lineItems) {
            List<QuoteOptionPricingDeltas.QuoteOptionPricingDelta> currentDeltas = QuoteOptionPricingDeltas.filterByLineItem(pricingDeltas, lineItem.getLineItemId());
            // modified the signature to accommodate new changes for charging scheme discounts
            lineItem.applyUsageDiscounts(currentDeltas,lineItem,futureAssetPricesFacade);
            futureAssetPricesFacade.save(lineItem);
        }

        return Response.ok().build();
    }

    @POST
    @Path("cost")
    @Produces(MediaType.APPLICATION_JSON)
    public Response applyCostDiscounts(@PathParam(CUSTOMER_ID) String customerId,
                                       @PathParam(PROJECT_ID) String projectId,
                                       @PathParam(QUOTE_OPTION_ID) String quoteOptionId,
                                       String deltas) {
        // parse cost JSON to usable object.
        QuoteOptionCostDiscountDeltas costDeltas = new Gson().fromJson(deltas, QuoteOptionCostDiscountDeltas.class);

        // get unique list of Line Item ID's being updated.
        Set<LineItemId> lineItemIds = newHashSet();
        for(QuoteOptionCostDiscountDeltas.QuoteOptionCostDiscountDelta costDelta : costDeltas.getQuoteOptionCostDeltas()) {
            lineItemIds.add(new LineItemId(costDelta.getLineItemId()));
        }

        // apply discount to line items and save back to CIF
        final List<FutureAssetPricesModel> lineItems = futureAssetPricesFacade.getForLineItems(customerId, projectId, quoteOptionId, newArrayList(lineItemIds));
        Set<LineItemId> lineItemsToReprice = newHashSet();
        Set<ManualPrice> addGrossPrice = newHashSet();
        for (FutureAssetPricesModel lineItem : lineItems) {
            Map<String, DiscountDelta> discounts = newHashMap();
            boolean lineItemNeedsPriced = false;

            for(QuoteOptionCostDiscountDeltas.QuoteOptionCostDiscountDelta costDelta : costDeltas.getQuoteOptionCostDeltas()) {
                if(costDelta.getLineItemId().equals(lineItem.getLineItemId())) {
                    boolean oneTimeDiscountUpdated = costDelta.getOneTimeDiscount().isDiscountUpdated();
                    final Optional<String> vendorDiscountRef = !Strings.isNullOrEmpty(costDelta.getVendorDiscountRef()) ? Optional.of(costDelta.getVendorDiscountRef()) : Optional.<String>absent();
                    final PriceLineStatus priceLineStatus = costDelta.isManualPricing() ? PriceLineStatus.FIRM : PriceLineStatus.REPRICING ;
                    discounts.put(costDelta.getOneTimeDiscount().getPriceLineId(),
                                  new DiscountDelta(oneTimeDiscountUpdated ? Optional.of(new BigDecimal(costDelta.getOneTimeDiscount().getDiscount())) : Optional.<BigDecimal>absent(),
                                                    vendorDiscountRef,
                                                    oneTimeDiscountUpdated ? Optional.of(priceLineStatus) : Optional.<PriceLineStatus>absent()));

                    boolean recurringDiscountUpdated = costDelta.getRecurringDiscount().isDiscountUpdated();
                    discounts.put(costDelta.getRecurringDiscount().getPriceLineId(),
                                  new DiscountDelta(recurringDiscountUpdated ? Optional.of(new BigDecimal(costDelta.getRecurringDiscount().getDiscount())) : Optional.<BigDecimal>absent(),
                                                    vendorDiscountRef,
                                                    recurringDiscountUpdated ? Optional.of(priceLineStatus) : Optional.<PriceLineStatus>absent()));

                    if (costDelta.isGrossAdded()) {
                        addGrossPrice.add(new ManualPrice(costDelta.getOneTimeDiscount().getPriceLineId(),
                                new BigDecimal(costDelta.getOneTimeDiscount().getGrossValue()),
                                "oneTime",
                                costDelta.getDescription()));

                        addGrossPrice.add(new ManualPrice(costDelta.getRecurringDiscount().getPriceLineId(),
                                new BigDecimal(costDelta.getRecurringDiscount().getGrossValue()),
                                "recurring",
                                costDelta.getDescription()));
                    }

                    if (!lineItemNeedsPriced && !costDelta.isManualPricing()) {
                        lineItemNeedsPriced = oneTimeDiscountUpdated || recurringDiscountUpdated;
                    }
                }
            }

            if(!addGrossPrice.isEmpty()){
                futureAssetPricesFacade.updatePricingStatus(lineItem, PricingStatus.FIRM);
                lineItem.applyGrossPriceUpdate(addGrossPrice);
            }

            lineItem.applyDiscountDeltas(discounts);
            if (lineItemNeedsPriced) {
                futureAssetPricesFacade.updatePricingStatus(lineItem, PricingStatus.REPRICING);
                lineItemsToReprice.add(new LineItemId(lineItem.getLineItemId()));
            }

            refreshLineItemLockVersion(lineItem);
            futureAssetPricesFacade.save(lineItem);

            if (!lineItemsToReprice.isEmpty()) {
                priceHandlerService.processLineItemsForPricing(lineItemsToReprice,
                        customerId,
                        projectId,
                        quoteOptionId,
                        UserContextManager.getCurrent().getPermissions().indirectUser,
                        UserContextManager.getCurrent().getRsqeToken());
            }
        }

        return Response.ok().build();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response addDiscounts(@PathParam(CUSTOMER_ID) String customerId,
                                 @PathParam(PROJECT_ID) String projectId,
                                 @PathParam(QUOTE_OPTION_ID) String quoteOptionId,
                                 String discountJson) {

        JSONObject data;
        try {
            data = parseJsonData(discountJson);
        } catch (JSONException e) {
            return Response.status(400).entity("{ error: 'Can not parse JSON', message: '" + discountJson + "'").build();
        }
        final Map<LineItemId, Map<String, BigDecimal>> lineItemDiscounts;
        final LinkedHashMultimap<LineItemId, ManualPrice> lineItemGrossPrice;

        try {
            lineItemDiscounts = parseDiscountMap(data.get("discount").toString());
            lineItemGrossPrice = parseGrossMap(data.get("gross").toString());
        } catch (JSONException e) {
            return Response.status(400).entity("{ error: 'Can not parse JSON', message: '" + discountJson + "'").build();
        }

        final List<FutureAssetPricesModel> lineItems = futureAssetPricesFacade.getForLineItems(customerId, projectId, quoteOptionId, new ArrayList<LineItemId>(lineItemDiscounts.keySet()));
        for (FutureAssetPricesModel lineItem : lineItems) {
            if (lineItems.size() > 1) {   // adding this check to refresh the lock version as lineItem lock version is getting changed for 2nd line item QC48130.
                refreshLineItemLockVersion(lineItem);
            }
            lineItem.applyGrossPriceUpdate(lineItemGrossPrice.get(new LineItemId(lineItem.getLineItemId())));
            lineItem.applyDiscount(lineItemDiscounts.get(new LineItemId(lineItem.getLineItemId())));
            futureAssetPricesFacade.save(lineItem);
            QuoteOptionItemResource quoteOptionItemResource = getQuoteOptionItemResource(projectId, quoteOptionId);
            autoPriceAggregator.aggregatePricesOf(quoteOptionItemResource.get(lineItem.getLineItemId()).contractDTO.priceBooks.get(0), new LineItemId(lineItem.getLineItemId()));
        }
        quoteOptionFacade.putDiscountRequest(projectId, quoteOptionId);
        return Response.status(204).build();
    }

    private void refreshLineItemLockVersion(FutureAssetPricesModel lineItem) {
        int currentLockVersion = instanceClient.getCurrentLockVersion(new LineItemId(lineItem.getLineItemId()));
        lineItem.getPricesDTO().setLockVersion(currentLockVersion);
    }

    private QuoteOptionItemResource getQuoteOptionItemResource(String projectId, String quoteOptionId) {
        return projectResource.quoteOptionResource(projectId).quoteOptionItemResource(quoteOptionId);
    }

    private JSONObject parseJsonData(String json) throws JSONException {
        return new JSONObject(json);
    }

    private LinkedHashMultimap<LineItemId, ManualPrice> parseGrossMap(String grossJson) throws JSONException {
        JSONArray jsonArray = new JSONArray(grossJson);
        LinkedHashMultimap<LineItemId, ManualPrice> lineItemIds = LinkedHashMultimap.create();
        if(jsonArray.length() > 0) {
            for(int i=0 ; i< jsonArray.length(); i++) {
                JSONObject manualPrices = (JSONObject) jsonArray.get(i);
                lineItemIds.put(new LineItemId(manualPrices.getString("lineItemId")), new ManualPrice(manualPrices.getString("id"),
                                                                                                      new BigDecimal(manualPrices.getString("gross")),
                                                                                                      manualPrices.getString("type"),
                                                                                                      manualPrices.getString("productDescription")
                ));
            }
        }
        return lineItemIds;
    }

    private Map<LineItemId, Map<String, BigDecimal>> parseDiscountMap(String discountJson) throws JSONException {
        JSONObject jsonObject = new JSONObject(discountJson);
        Map<LineItemId, Map<String, BigDecimal>> lineItemIds = new HashMap<LineItemId, Map<String, BigDecimal>>();
        final Iterator keys = jsonObject.keys();
        while (keys.hasNext()) {
            final String lineItemId = (String) keys.next();
            final JSONObject discounts = jsonObject.getJSONObject(lineItemId);
            final HashMap<String, BigDecimal> discountMap = new HashMap<String, BigDecimal>();
            final Iterator discountIterator = discounts.keys();
            while (discountIterator.hasNext()) {
                String priceLineId = (String) discountIterator.next();
                discountMap.put(priceLineId.replace("id_", ""), new BigDecimal(discounts.getString(priceLineId)));
            }

            lineItemIds.put(new LineItemId(lineItemId), discountMap);
        }
        return lineItemIds;
    }
}
