package com.bt.rsqe.projectengine.web;


import com.bt.rsqe.client.Pmr;
import com.bt.rsqe.customerinventory.client.ProductInstanceAssetValidator;
import com.bt.rsqe.customerinventory.client.ProductInstanceClient;
import com.bt.rsqe.customerinventory.client.ScopePricing;
import com.bt.rsqe.customerinventory.client.ScopePricingItem;
import com.bt.rsqe.customerinventory.client.ScopePricingItemError;
import com.bt.rsqe.customerinventory.client.ScopePricingStatus;
import com.bt.rsqe.customerinventory.dto.AssetCharacteristicDTO;
import com.bt.rsqe.customerinventory.dto.AssetDTO;
import com.bt.rsqe.customerinventory.parameter.CharacteristicName;
import com.bt.rsqe.customerinventory.parameter.LineItemId;
import com.bt.rsqe.domain.AbstractNotificationEvent;
import com.bt.rsqe.domain.AssetKey;
import com.bt.rsqe.domain.Notification;
import com.bt.rsqe.domain.bom.parameters.ProductSCode;
import com.bt.rsqe.domain.product.AssetProcessType;
import com.bt.rsqe.domain.project.PricingStatus;
import com.bt.rsqe.domain.project.ProductInstance;
import com.bt.rsqe.enums.AssetVersionStatus;
import com.bt.rsqe.inlife.client.ApplicationCapabilityProvider;
import com.bt.rsqe.logging.Log;
import com.bt.rsqe.logging.LogFactory;
import com.bt.rsqe.logging.LogLevel;
import com.bt.rsqe.pc.client.ConfiguratorSpecialBidClient;
import com.bt.rsqe.pricing.PriceClientResponse;
import com.bt.rsqe.projectengine.migration.QuoteMigrationDetailsProvider;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.collect.Lists.*;
import static com.google.common.collect.Maps.*;
import static com.google.common.collect.Sets.*;

public class ScopePricingTask implements Runnable {

    private final Logger logger = LogFactory.createDefaultLogger(Logger.class);
    private String lineItems;
    private String customerId;
    private String projectId;
    private String quoteOptionId;
    private boolean indirectUser;
    private PriceHandlerService priceHandlerService;
    private ProductInstanceClient futureProductInstanceClient;
    private ProductInstanceAssetValidator productInstanceValidator;
    private ConfiguratorSpecialBidClient configuratorSpecialBidClient;
    private String userToken;
    private QuoteMigrationDetailsProvider quoteMigrationDetailsProvider;
    private Pmr pmr;
    private ApplicationCapabilityProvider applicationCapabilityProvider;

    public ScopePricingTask(String lineItems, String customerId, String projectId, String quoteOptionId, boolean indirectUser,
                            PriceHandlerService priceHandlerService, ProductInstanceClient futureProductInstanceClient, ProductInstanceAssetValidator productInstanceValidator,
                            ConfiguratorSpecialBidClient configuratorSpecialBidClient, String userToken, QuoteMigrationDetailsProvider quoteMigrationDetailsProvider, Pmr pmr,
                            ApplicationCapabilityProvider applicationCapabilityProvider) {
        this.lineItems = lineItems;
        this.customerId = customerId;
        this.projectId = projectId;
        this.quoteOptionId = quoteOptionId;
        this.indirectUser = indirectUser;
        this.priceHandlerService = priceHandlerService;
        this.futureProductInstanceClient = futureProductInstanceClient;
        this.productInstanceValidator = productInstanceValidator;
        this.configuratorSpecialBidClient = configuratorSpecialBidClient;
        this.userToken = userToken;
        this.quoteMigrationDetailsProvider = quoteMigrationDetailsProvider;
        this.pmr = pmr;
        this.applicationCapabilityProvider = applicationCapabilityProvider;
    }

    @Override
    public final void run() {
            ScopePricing scopePricing = futureProductInstanceClient.getScopePricing(lineItems);
        try {
            final List<ScopePricingItemError> scopePricingItemErrorMap = newArrayList();
            String filteredLinesItems = filterLineItemsByValidProducts(lineItems, scopePricingItemErrorMap);
            Map<String, PriceClientResponse> priceClientResponseMap = newHashMap();
            if (!filteredLinesItems.equals("")) {
                priceClientResponseMap = priceHandlerService.processLineItemsForPricing(filteredLinesItems, customerId,
                                                                                                                         projectId, quoteOptionId,
                                                                                                                         indirectUser, userToken);
            }
            populateRecurringNonRecurringValues(priceClientResponseMap);
            final List<String> lineItemIds = newArrayList(lineItems.split(","));
            for (final String lineItemId : lineItemIds) {
                final List<String> nonSellableLineItemsIOwn = getNonSellableLineItemsOwnedBy(lineItemId, lineItemIds);  //TODO: do it recursively
                final List<ScopePricingItemError> pricingItemErrors = newArrayList();
                List<PriceClientResponse> priceClientResponseList = newArrayList(Iterables.filter(priceClientResponseMap.values(), new Predicate<PriceClientResponse>() {
                    @Override
                    public boolean apply(PriceClientResponse input) {
                        final boolean applicable = input.getLineItemId().equals(lineItemId) || nonSellableLineItemsIOwn.contains(input.getLineItemId());
                        if(applicable && !input.getRequestStatus().getPricingErrors().isEmpty()) {
                            pricingItemErrors.add(new ScopePricingItemError(lineItemId, input.getRequestStatus().getPricingErrors().get(0).getMessage()));
                        }
                        return applicable;
                    }
                }));
                addICBErrors(lineItemId, pricingItemErrors, priceClientResponseList);

                if(!pricingItemErrors.isEmpty()) {
                    scopePricing.addItem(new ScopePricingItem(lineItemId,
                                                              PricingStatus.NOT_PRICED.getDescription(),
                                                              scopePricing.getLineItems(),
                                                              pricingItemErrors));
                } else if(priceClientResponseList.isEmpty()) {
                    final ArrayList<ScopePricingItemError> errors = newArrayList(new ScopePricingItemError(lineItemId, "The Line Item is invalid so can not be priced"));
                    errors.addAll(newArrayList(Iterables.filter(scopePricingItemErrorMap, new Predicate<ScopePricingItemError>() {
                        @Override
                        public boolean apply(@Nullable ScopePricingItemError input) {
                            return lineItemId.equals(input.getLineItemId());
                        }
                    })));

                    scopePricing.addItem(new ScopePricingItem(lineItemId,
                                                              PricingStatus.NOT_PRICED.getDescription(),
                                                              scopePricing.getLineItems(),
                                                              errors));
                } else {
                    scopePricing.addItem(new ScopePricingItem(lineItemId,
                                                              getPricingStatus(lineItemId, priceClientResponseList),
                                                              scopePricing.getLineItems()));
                }
            }

            scopePricing.setStatus(ScopePricingStatus.COMPLETE);
            futureProductInstanceClient.updateScopePricing(scopePricing);
         } catch (Exception ex) {
            logger.errorWhilePricing(ExceptionUtils.getFullStackTrace(ex));
            scopePricing.setStatus(ScopePricingStatus.ERROR);
            scopePricing.setError(ex.getMessage());
            futureProductInstanceClient.updateScopePricing(scopePricing);
        }
    }
    private void populateRecurringNonRecurringValues(Map<String, PriceClientResponse> priceClientResponseMap){
        List<PriceClientResponse> priceClientResponseList = newArrayList(Iterables.filter(priceClientResponseMap.values(), new Predicate<PriceClientResponse>() {
            @Override
            public boolean apply(@Nullable PriceClientResponse input) {
                return PricingStatus.FIRM.name().equalsIgnoreCase(input.getPriceStatus());
            }
        }));
        final String nonRecurringCharges = "NON RECURRING CHARGES";
        final String recurringCharges = "RECURRING CHARGES";
        final CharacteristicName nonRecurringCharacteristic = new CharacteristicName(nonRecurringCharges);
        final CharacteristicName recurringCharacteristic = new CharacteristicName(recurringCharges);

        for(PriceClientResponse priceClientResponse:priceClientResponseList){
            boolean modified = false;
            String productInstanceId = priceClientResponse.getProductInstanceId();
            AssetDTO assetDTO = futureProductInstanceClient.getAssetDtoByAssetKey(new AssetKey(productInstanceId, priceClientResponse.getProductVersion()));
            if(assetDTO.hasCharacteristic(nonRecurringCharacteristic)){
              AssetCharacteristicDTO assetCharacteristicDTO = assetDTO.getCharacteristic(nonRecurringCharges);
                assetCharacteristicDTO.setValue(priceClientResponse.getOneTimePrice());
                modified = true;
            }
            if(assetDTO.hasCharacteristic(recurringCharacteristic))  {
                AssetCharacteristicDTO assetCharacteristicDTO = assetDTO.getCharacteristic(recurringCharges);
                assetCharacteristicDTO.setValue(priceClientResponse.getRecurringPrice());
                modified = true;
            }
            if (modified) {
                futureProductInstanceClient.putAsset(assetDTO);
            }
        }
    }
    private List<String> getNonSellableLineItemsOwnedBy(final String lineItemId, final List<String> lineItemsBeingPriced) {
        return newArrayList(Iterables.filter(getRelatedToItemsOfRelatedAssets(lineItemId), new Predicate<String>() {
            @Override
            public boolean apply(String input) {
                return !lineItemsBeingPriced.contains(input);
            }
        }));
    }

    private List<String> getRelatedToItemsOfRelatedAssets(String lineItemId) {
        List<String> relatedItems = futureProductInstanceClient.getRelatedToLineItemIdsOwnedByLineItemId(lineItemId);
        Set<String> relatedItemList = newHashSet();
        relatedItemList.addAll(relatedItems);

        for (String relatedItem : relatedItems) {
            if (relatedItem != null && !relatedItem.equals(lineItemId)) {
                List<String> items = futureProductInstanceClient.getRelatedToLineItemIdsOwnedByLineItemId(relatedItem);
                if (items != null && !items.isEmpty()) {
                    relatedItemList.addAll(items);
                }
            }
        }
        return newArrayList(relatedItemList);
    }

    private String getPricingStatus(String lineItemId, List<PriceClientResponse> priceClientResponseList) {
        // get the minimum ranked pricing status for all the price client responses...
        int rank = PricingStatus.getByDescription(priceClientResponseList.get(0).getPriceStatus()).rank();
        for(PriceClientResponse priceClientResponse : priceClientResponseList) {
            rank = Math.min(rank, PricingStatus.getByDescription(priceClientResponse.getPriceStatus()).rank());
        }

        final PricingStatus pricingStatus = PricingStatus.rankOf(rank);

        // if status is not priced then check for any firm price lines.  If any price lines are firm then return 'Partially Priced'
        if(PricingStatus.NOT_PRICED.equals(pricingStatus)) {
            AssetDTO root = futureProductInstanceClient.getAssetDTO(new LineItemId(lineItemId));
            for(AssetDTO asset : root.flattenMeAndMyChildren().values()) {
                if(asset.anyPriceLinesAreFirm()) {
                    return PricingStatus.PARTIALLY_PRICED;
                }
                for(AssetDTO related : asset.getRelatedToAssets()) {
                    if (!pmr.productOffering(ProductSCode.newInstance(related.getProductCode())).get().isInFrontCatalogue) {
                        if(related.anyPriceLinesAreFirm()) {
                            return PricingStatus.PARTIALLY_PRICED;
                        }
                    }
                }
            }
        }

        return pricingStatus.getDescription();
    }

    private void addICBErrors(String lineItemId, List<ScopePricingItemError> pricingItemErrors, List<PriceClientResponse> priceClientResponseList) {
        for(PriceClientResponse priceClientResponse : priceClientResponseList) {
            try {
                if(PricingStatus.getByDescription(priceClientResponse.getPriceStatus()).isIcb()) {
                    pricingItemErrors.add(new ScopePricingItemError(lineItemId, "ICB Asset must be priced on the Bulk Configuration Page using the ICB Pricing Buttons"));
                    return;
                }
            } catch (IllegalArgumentException e) {
                // keep calm as we only care if the status is ICB.
            }
        }
    }

    private String filterLineItemsByValidProducts(String lineItemsToFilter, List<ScopePricingItemError> scopePricingItemErrorMap) {
        List<String> lineItemsToPrice = newArrayList();
        for (String lineItemId : lineItemsToFilter.split(",")) {
            ProductInstance productInstance = futureProductInstanceClient.get(new LineItemId(lineItemId));
            Notification notification = productInstanceValidator.validateAsset(productInstance, isMigrationQuote(productInstance));
            List<AbstractNotificationEvent> errors = getErrorsPreventingPricing(notification, productInstance.getQuoteOptionId());
            if (errors.isEmpty()) {
                if (validateSpecialBidAttributes(productInstance)) {
                    lineItemsToPrice.add(lineItemId);
                } else {
                    scopePricingItemErrorMap.add(new ScopePricingItemError(lineItemId, "Special Bid Attributes Invalid"));
                }
            } else {
                for (AbstractNotificationEvent errorNotificationEvent : errors) {
                    scopePricingItemErrorMap.add(new ScopePricingItemError(lineItemId, errorNotificationEvent.getMessage()));
                }
            }
        }
        return StringUtils.join(lineItemsToPrice, ",");
    }

    private List<AbstractNotificationEvent> getErrorsPreventingPricing(Notification notification, String quoteOptionId) {
        if(applicationCapabilityProvider.isFunctionalityEnabled(ApplicationCapabilityProvider.Capability.IGNORE_RELATION_VALIDATIONS_WHEN_PRICING, false, Optional.fromNullable(quoteOptionId))) {
            // remove relationship errors from the notification as they do not prevent pricing from occurring...
            return newArrayList(Iterables.filter(notification.getErrorEvents(), new Predicate<AbstractNotificationEvent>() {
                @Override
                public boolean apply(AbstractNotificationEvent input) {
                    return !AbstractNotificationEvent.EventType.RELATIONSHIP.equals(input.getType());
                }
            }));
        } else {
            return notification.getErrorEvents();
        }
    }

    private boolean isMigrationQuote(ProductInstance productInstance) {
        return quoteMigrationDetailsProvider.conditionalFor(productInstance)
                                       .isMigrationQuote()
                                       .check();
    }

    private boolean validateSpecialBidAttributes(ProductInstance productInstance) {
        if (productInstance.isSpecialBid()) {
            boolean isMovesToSpecialBidProduct = AssetProcessType.MOVE.value().equals(productInstance.getAssetProcessType())
                                                 && AssetVersionStatus.DRAFT.equals(productInstance.getAssetVersionStatus());
            if (isMovesToSpecialBidProduct) {
                return true;
            }
            return configuratorSpecialBidClient.validateSpecialBidAttributes(productInstance.getLineItemId(), productInstance.getProductInstanceId().getValue(),
                                                                          productInstance.getProductInstanceVersion());
        }
        return true;
    }


    interface Logger {
        @Log(level = LogLevel.ERROR, format = "Scope Pricing Task - Error While getting prices message:\n %s")
        void errorWhilePricing(String error);
    }
}