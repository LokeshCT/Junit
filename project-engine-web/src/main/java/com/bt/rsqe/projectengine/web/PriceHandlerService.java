package com.bt.rsqe.projectengine.web;

import com.bt.rsqe.customerinventory.client.ProductInstanceClient;
import com.bt.rsqe.customerinventory.dto.AssetDTO;
import com.bt.rsqe.customerinventory.parameter.LengthConstrainingProductInstanceId;
import com.bt.rsqe.customerinventory.parameter.LineItemId;
import com.bt.rsqe.customerinventory.parameter.ProductInstanceVersion;
import com.bt.rsqe.customerrecord.CustomerResource;
import com.bt.rsqe.domain.PriceBookDTO;
import com.bt.rsqe.domain.bom.parameters.ProductInstanceId;
import com.bt.rsqe.domain.product.AttributeName;
import com.bt.rsqe.domain.product.InstanceTreeScenario;
import com.bt.rsqe.domain.product.PriceBooks;
import com.bt.rsqe.domain.product.SimpleProductOfferingType;
import com.bt.rsqe.domain.product.chargingscheme.ProductChargingScheme;
import com.bt.rsqe.domain.product.parameters.ProductCategoryCode;
import com.bt.rsqe.domain.product.parameters.ProductIdentifier;
import com.bt.rsqe.domain.project.PricingStatus;
import com.bt.rsqe.domain.project.ProductInstance;
import com.bt.rsqe.pmr.client.PmrClient;
import com.bt.rsqe.pricing.PriceClientBidManagerAssetResponse;
import com.bt.rsqe.pricing.PriceClientRequest;
import com.bt.rsqe.pricing.PriceClientResponse;
import com.bt.rsqe.pricing.PriceRequestType;
import com.bt.rsqe.pricing.PriceResponse;
import com.bt.rsqe.pricing.PricingClient;
import com.bt.rsqe.pricing.PricingFacadeService;
import com.bt.rsqe.pricing.PricingStatusNADecider;
import com.bt.rsqe.productinstancemerge.ChangeType;
import com.bt.rsqe.productinstancemerge.MergeResult;
import com.bt.rsqe.projectengine.LineItemDiscountStatus;
import com.bt.rsqe.projectengine.ProjectResource;
import com.bt.rsqe.projectengine.QuoteOptionDTO;
import com.bt.rsqe.projectengine.QuoteOptionItemDTO;
import com.bt.rsqe.projectengine.web.facades.SiteFacade;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static com.bt.rsqe.domain.product.ProductOffering.*;
import static com.bt.rsqe.utils.AssertObject.*;
import static com.google.common.base.Strings.*;
import static com.google.common.collect.Lists.*;
import static com.google.common.collect.Maps.*;

public class PriceHandlerService {

    private final PricingClient priceClient;
    private final ProductInstanceClient futureProductInstanceClient;
    private final SiteFacade siteFacade;
    private final PricingFacadeService pricingFacadeService;
    private final PmrClient pmrClient;
    private final CustomerResource customerResource;
    private ProjectResource projectResource;
    private boolean isIndirectUser;
    private String userToken;
    private PricingStatusNADecider pricingStatusNADecider;

    public PriceHandlerService(CustomerResource customerResource, PmrClient pmrClient, PricingFacadeService pricingFacadeService, SiteFacade siteFacade,
                               ProductInstanceClient futureProductInstanceClient, PricingClient priceClient, ProjectResource projectResource, PricingStatusNADecider pricingStatusNADecider) {
        this.customerResource = customerResource;
        this.pmrClient = pmrClient;
        this.pricingFacadeService = pricingFacadeService;
        this.siteFacade = siteFacade;
        this.futureProductInstanceClient = futureProductInstanceClient;
        this.priceClient = priceClient;
        this.projectResource = projectResource;
        this.pricingStatusNADecider = pricingStatusNADecider;
    }


    public Map<String, PriceClientResponse> processLineItemsForPricing(String lineItems, String customerId, String projectId,
                                                                       String quoteOptionId, boolean isIndirectUser, String userToken) {
        return priceLineItems(customerId, projectId, quoteOptionId, Splitter.on(',').split(lineItems), isIndirectUser, userToken);
    }

    public Map<String, PriceClientResponse> processLineItemsForPricing(Set<LineItemId> lineItems, String customerId, String projectId,
                                                                       String quoteOptionId, boolean isIndirectUser, String userToken) {

        return priceLineItems(customerId, projectId, quoteOptionId, Iterables.transform(lineItems, new Function<LineItemId, String>() {
            @Override
            public String apply(LineItemId input) {
                return input.value();
            }
        }), isIndirectUser, userToken);
    }

    private Map<String, PriceClientResponse> priceLineItems(String customerId, String projectId, String quoteOptionId, Iterable<String> lineItemIds, boolean inDirect, String userToken) {

        this.isIndirectUser = inDirect;
        this.userToken = userToken;
        QuoteOptionDTO quoteOptionDTO = getQuoteOptionDTOFromId(quoteOptionId, projectId);
        Map<String, PriceClientResponse> priceClientResponses = newHashMap();
        List<ProductInstance> productInstances = Lists.reverse(buildProductInstancesToBePricedList(lineItemIds, priceClientResponses));
        List<PriceClientRequest> priceClientRequests = buildPriceClientRequestsFromLineProductInstances(productInstances, customerId, projectId, quoteOptionDTO,priceClientResponses);
        Map<String, PriceBookDTO> priceBookForLineItem = getPriceBookFromQuoteOptionLineItems(priceClientRequests);
        List<String> discountApprovedLineItems = getDiscountApprovedLineItems(projectId, quoteOptionId, lineItemIds);
        final PriceResponse priceResponse = pricingFacadeService.fetchPricesAndApplyAggregation(priceClientRequests, priceClientResponses, PriceRequestType.getPrice, priceBookForLineItem, discountApprovedLineItems);
        final Map<String, PriceClientResponse> responsesAfterDecision = pricingStatusNADecider.decide(priceClientRequests, priceResponse, Lists.<PriceClientBidManagerAssetResponse>newArrayList());
        priceClientResponses.putAll(responsesAfterDecision);
        return priceClientResponses;
    }

    public List<String> getDiscountApprovedLineItems(String projectId, String quoteOptionId, final Iterable<String> lineItemIds) {
        ArrayList<String> discountApprovedItems = newArrayList();
        List<QuoteOptionItemDTO> itemDTOs = projectResource.quoteOptionResource(projectId).quoteOptionItemResource(quoteOptionId).get();
        for(QuoteOptionItemDTO item : itemDTOs){
            if(LineItemDiscountStatus.APPROVED.equals(item.discountStatus) && Iterables.contains(lineItemIds,item.getId())){
               discountApprovedItems.add(item.getId());
            }
        }
        return discountApprovedItems;
    }

    private void buildPriceClientResponsesFromProductInstance(Map<String, PriceClientResponse> priceClientResponseMap, ProductInstance productInstance) {
        priceClientResponseMap.put(productInstance.getProductInstanceId().getValue(), generatePriceClientResponse(productInstance));
    }

    private PriceClientResponse generatePriceClientResponse(ProductInstance productInstance) {
        return new PriceClientResponse(productInstance.getProductInstanceId().getValue(),
                                       productInstance.getProductInstanceVersion(),
                                       productInstance.getLineItemId(),
                                       productInstance.getPricingStatus());
    }

    private List<ProductInstance> buildProductInstancesToBePricedList(Iterable<String> lineItemIds, Map<String, PriceClientResponse> priceClientResponses) {
        List<ProductInstance> productInstances = new ArrayList<>();
        for(String lineItemId: lineItemIds) {
            ProductInstance productInstance = futureProductInstanceClient.get(new LineItemId(lineItemId));
            setSiteId(productInstance);
            productInstances.add(productInstance);
            for(ProductInstance child : productInstance.getAllPriceAbleChildren()) {
                setSiteId(child);
                //child.setParent(productInstance);
                productInstances.add(child);
                buildPriceClientResponsesFromProductInstance(priceClientResponses, child);
            }
            checkForRelatedToNonFrontCatalogueInstancesToBePriced(productInstance, priceClientResponses, productInstances);
            buildPriceClientResponsesFromProductInstance(priceClientResponses, productInstance);
        }
        return populateSiteIdToProductInstance(productInstances);

    }

    private List<ProductInstance> populateSiteIdToProductInstance(List<ProductInstance> productInstances) {
        for(ProductInstance productInstance : productInstances){
            setSiteId(productInstance);
        }
        return newArrayList(productInstances);
    }

    private void checkForRelatedToNonFrontCatalogueInstancesToBePriced(ProductInstance productInstance, Map<String, PriceClientResponse> priceClientResponses, List<ProductInstance> productInstances) {
        for (ProductInstance lineItemAsset : productInstance.flattenMeAndMyChildren().values()) {
            for (ProductInstance relatedTo : lineItemAsset.getRelatedToInstances()) {
                if (!relatedTo.getProductOffering().isInFrontCatalogue) {
                    for (ProductInstance flattenedRelatedAsset : relatedTo.flattenMeAndMyChildren().values()) {
                        if (flattenedRelatedAsset.isPriceable()) {
                            productInstances.add(flattenedRelatedAsset);
                            buildPriceClientResponsesFromProductInstance(priceClientResponses, flattenedRelatedAsset);
                        }
                    }
                    for (ProductInstance flattenedRelatedAsset : relatedTo.flattenMeAndMyRelatedInstances().values()) {
                        if (flattenedRelatedAsset.isPriceable()) {
                            productInstances.add(flattenedRelatedAsset);
                            buildPriceClientResponsesFromProductInstance(priceClientResponses, flattenedRelatedAsset);
                        }
                    }

                    //TODO: Remove this after analysis as it seems to be a duplicate logic.
                    /*if(productInstance.isPriceable()) {
                    productInstances.add(productInstance);
                    }*/
                    if (relatedTo.isPriceable()) {
                        productInstances.add(relatedTo);
                        buildPriceClientResponsesFromProductInstance(priceClientResponses, relatedTo);     //Sees this code causes of duplication inclusion , need to check and refactor, but now made the collection to set implementation to avoid duplicates.
                    }
                    checkForRelatedToNonFrontCatalogueInstancesToBePriced(relatedTo, priceClientResponses, productInstances);
                }
            }
        }
    }

    private void setSiteId(ProductInstance productInstance) {
        productInstance.setSiteId(isEmpty(productInstance.getSiteId())
                                      ? customerResource.siteResource(productInstance.getCustomerId())
                                                        .getCentralSite(productInstance.getProjectId())
                                                        .bfgSiteID
                                      : productInstance.getSiteId());
    }

    private QuoteOptionDTO getQuoteOptionDTOFromId(String quoteOptionId, String projectId) {
        List<QuoteOptionDTO> quoteOptionDTOs = projectResource.quoteOptionResource(projectId).get();
        for(QuoteOptionDTO quoteOptionDTO: quoteOptionDTOs) {
            if (quoteOptionDTO.getId().equals(quoteOptionId)) {
                return quoteOptionDTO;
            }
        }
        return null;
    }

    private List<PriceClientRequest> buildPriceClientRequestsFromLineProductInstances(List<ProductInstance> productInstanceList, String customerId, String projectId,
                                                                                      QuoteOptionDTO quoteOptionDTO, Map<String, PriceClientResponse> priceClientResponses) {
        List<PriceClientRequest> priceClientRequests = new ArrayList<PriceClientRequest>();
        for (ProductInstance productInstance : productInstanceList) {
                PriceClientRequest priceClientRequest = generatePriceClientRequest(customerId, projectId, productInstance, productInstance.getLineItemId(), quoteOptionDTO);
                if (isNotNull(priceClientRequest) && isEligibleForPricing(productInstance, priceClientRequest, priceClientResponses)) {
                    priceClientRequests.add(priceClientRequest);
                }
        }
        return priceClientRequests;
    }

    private PriceClientRequest generatePriceClientRequest(String customerId, String projectId, ProductInstance productInstance, String lineItemId, QuoteOptionDTO quoteOptionDTO) {
        String productCategoryCode = getProductCategory(productInstance);
        final QuoteOptionItemDTO quoteOptionItemDTO = projectResource.quoteOptionResource(projectId).quoteOptionItemResource(productInstance.getQuoteOptionId()).get(productInstance.getLineItemId());
        boolean isIFC = quoteOptionItemDTO.isIfc;
        PriceClientRequest priceClientRequest = new PriceClientRequest(customerId,
                                       projectId,
                                       productCategoryCode,
                                       productInstance,
                                       getPriceBookDTOFromLineItem(customerId,quoteOptionItemDTO.getProductCode(),isIndirectUser, quoteOptionItemDTO.getProductCategoryCode()),
                                       isIndirectUser(),
                                       productInstance.getSiteId(),
                                       quoteOptionDTO.getCurrency(),
                                       getChangeType(productInstance, getMergeResult(productInstance)).getValue(),
                                       getSiteSurveyIfApplicable(productInstance),
                                       getMergeResult(productInstance),
                                       isIFC,
                                       null);
        priceClientRequest.setUserToken(userToken);
        return priceClientRequest;
    }

    private String getProductCategory(ProductInstance productInstance) {
        String productCategoryCode = null;

        if (ProductCategoryCode.catCodeSet(productInstance.getProductCategoryCode())) {
          return productInstance.getProductCategoryCode().value();
        }
        else if (productInstance.isSharedProduct()) {
            productCategoryCode = productInstance.getInheritedHCode().get().getProductId();
        } else {
            Optional<ProductIdentifier> identifierOptional = pmrClient.getProductHCode(productInstance.getProductIdentifier().getProductId());
            if(identifierOptional.isPresent()) {
                productCategoryCode = identifierOptional.get().getProductId();
        }
        }

        if(!isEmpty(productCategoryCode)) {
            return productCategoryCode;
        }

        List<String> ownerLineItemIds = futureProductInstanceClient.getRelatedToLineItemIdsWhoOwnLineItemId(productInstance.getLineItemId());
        for(String ownerLineItemId : ownerLineItemIds) {
            AssetDTO owner = futureProductInstanceClient.getAssetDTO(new LineItemId(ownerLineItemId));
            final Optional<ProductIdentifier> productHCode = pmrClient.getProductHCode(owner.getProductCode());
            if(productHCode.isPresent()) {
                return productHCode.get().getProductId();
            }
        }

        throw new RuntimeException(String.format("Product category code is unavailable for product - %s", productInstance.getProductIdentifier().getProductId()));
    }

    private MergeResult getMergeResult(ProductInstance productInstance) {
        MergeResult mergeResult = null;
        final Optional<ProductInstance> optionalAsIsInstance = futureProductInstanceClient.getSourceAsset(new LengthConstrainingProductInstanceId(productInstance.getProductInstanceId().getValue()));
        if (optionalAsIsInstance.isPresent()) {
            mergeResult = findMergeResult(productInstance, optionalAsIsInstance.get());
        }
        return mergeResult;
    }

    private MergeResult findMergeResult(ProductInstance toBeProductInstance, ProductInstance asIsProductInstance) {
        return futureProductInstanceClient.getAssetsDiff(toBeProductInstance.getProductInstanceId().getValue(), toBeProductInstance.getProductInstanceVersion(),
                                                   asIsProductInstance.getProductInstanceVersion(), InstanceTreeScenario.PROVIDE);
    }

    //TODO: Barani : This is a Hack to Fix 11384. Need to Provide a proper fix
    private ChangeType getChangeType(ProductInstance toBeProductInstance, MergeResult mergeResult) {
        ChangeType changeType = ChangeType.ADD;
        if (mergeResult != null) {
            changeType = mergeResult.changeFor(toBeProductInstance);
            SimpleProductOfferingType simpleProductOfferingType = toBeProductInstance.getSimpleProductOfferingType();
            if (changeType.isNoChange() && SimpleProductOfferingType.isPackageType(simpleProductOfferingType)) {
                return ChangeType.UPDATE;
            }
        }
        return changeType;
    }

    private String getSiteSurveyIfApplicable(ProductInstance productInstance) {
        if (productInstance.hasPricingEngineChargingScheme() && productInstance.isSiteInstallable()) {
            try {
                if (productInstance.hasInstanceCharacteristic(new AttributeName(SITE_SURVEY))) {
                    return nullToEmpty((String) productInstance.instanceCharacteristicValueFor(SITE_SURVEY));
                } else {
                    ProductInstance rootProductInstance = futureProductInstanceClient.get(new LineItemId(productInstance.getLineItemId()));
                    return nullToEmpty((String) rootProductInstance.instanceCharacteristicValueFor(SITE_SURVEY));
                }

            } catch (Exception ex) {
                return nullToEmpty(null);
            }
        }
        return nullToEmpty(null);
    }

    private boolean isIndirectUser() {
        return this.isIndirectUser;
    }

    private PriceBookDTO getPriceBookDTOFromLineItem(String customerId, String productCode, boolean indirect, ProductCategoryCode productCategoryCode) {
        String categoryCode = ProductCategoryCode.catCodeSet(productCategoryCode) ? productCategoryCode.value() : pmrClient.getProductHCode(productCode).get().getProductId();

        if(indirect){
            return customerResource.priceBookResource(customerId).defaultPriceBook(categoryCode);
        } else{
            PriceBooks priceBooks = new PriceBooks(pmrClient.getPriceBooks(productCode, categoryCode));
            return new PriceBookDTO(UUID.randomUUID().toString(), null, priceBooks.latestPriceBookForDirectUsers().version, null, null, null);
        }

    }

    private Map<String, PriceBookDTO> getPriceBookFromQuoteOptionLineItems(List<PriceClientRequest> priceClientRequests) {
        Map<String, PriceBookDTO> priceBooks = new HashMap<String, PriceBookDTO>();
        for(PriceClientRequest priceClientRequest: priceClientRequests) {
            priceBooks.put(priceClientRequest.productInstance().getLineItemId(), priceClientRequest.getPriceBook());
        }
        return priceBooks;
    }

    private boolean isEligibleForPricing(ProductInstance productInstance, PriceClientRequest priceClientRequest, Map<String, PriceClientResponse> priceClientResponses) {
        List<ProductChargingScheme> productChargingSchemes = priceClient.filterChargingSchemes(productInstance,
                                                                                               priceClientRequest.getLineItemStatus(),
                                                                                               productInstance.getProductIdentifier().getProductId(),
                                                                                               null,
                                                                                               priceClientRequest.isIFC());
        if(productChargingSchemes.isEmpty()) {
            AssetDTO assetDTO = futureProductInstanceClient.getAssetDtoByAssetKey(new LengthConstrainingProductInstanceId(productInstance.getProductInstanceId().getValue()),
                                                                                  new ProductInstanceVersion(productInstance.getProductInstanceVersion()));
            assetDTO.setPricingStatus(PricingStatus.NOT_APPLICABLE);
            assetDTO.removePriceLines();
            futureProductInstanceClient.putAsset(assetDTO);
            productInstance = futureProductInstanceClient.getLatestProduct(new ProductInstanceId(assetDTO.getId()), assetDTO.getQuoteOptionId());
            populateResponseForNAPricing(priceClientResponses, productInstance);
        }

        return !PricingStatus.NOT_APPLICABLE.equals(productInstance.getPricingStatus());  //For N/A status pricing is not eligible
    }

    private void populateResponseForNAPricing(Map<String, PriceClientResponse> responses, ProductInstance instance) {
        PriceClientResponse response = responses.get(instance.getProductInstanceId().toString());
        response.setPriceStatus(instance.getPricingStatus().getDescription());
        response.setLockVersion(instance.getLineItemLockVersion());
        response.setSpecialBidId(instance.getSpecialBidId());
        response.setSpecialBidUrl(instance.getSpecialBidUrl());
    }
}
