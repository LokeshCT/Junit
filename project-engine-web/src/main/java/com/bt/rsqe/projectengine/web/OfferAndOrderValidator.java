package com.bt.rsqe.projectengine.web;

import com.bt.rsqe.client.Pmr;
import com.bt.rsqe.customerinventory.client.ProductInstanceClient;
import com.bt.rsqe.customerinventory.dto.ExternalIdentifierDTO;
import com.bt.rsqe.customerinventory.parameter.LineItemId;
import com.bt.rsqe.domain.product.ExternalIdentifier;
import com.bt.rsqe.domain.product.parameters.ProductIdentifier;
import com.bt.rsqe.domain.project.PricingStatus;
import com.bt.rsqe.domain.project.ProductInstance;
import com.bt.rsqe.enums.AssetVersionStatus;
import com.bt.rsqe.projectengine.LineItemDiscountStatus;
import com.bt.rsqe.projectengine.ProjectResource;
import com.bt.rsqe.projectengine.QuoteOptionItemDTO;
import com.bt.rsqe.projectengine.QuoteOptionItemResource;
import com.bt.rsqe.projectengine.web.facades.LineItemFacade;
import com.bt.rsqe.projectengine.web.model.LineItemModel;
import com.bt.rsqe.projectengine.web.quoteoptionpricing.PriceSuppressStrategy;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static com.google.common.collect.Lists.transform;

public class OfferAndOrderValidator {


    private ProductInstanceClient productInstanceClient;
    private ProjectResource projectResource;
    private LineItemFacade lineItemFacade;
    private Pmr pmr;

    public OfferAndOrderValidator(ProductInstanceClient productInstanceClient, ProjectResource projectResource, LineItemFacade lineItemFacade, Pmr pmr) {
        this.productInstanceClient = productInstanceClient;
        this.projectResource = projectResource;
        this.lineItemFacade = lineItemFacade;
        this.pmr = pmr;
    }

    public OfferAndOrderValidationResult anyRelatedToHavingInvalidAssetStatus(List<String> lineItemIds){
        boolean isWrongStatus = false;
        for(String lineItemId: lineItemIds){
        Set<ProductInstance> relatedToInstances = productInstanceClient.get(new LineItemId(lineItemId)).getRelatedToInstances();
        isWrongStatus = Iterables.tryFind(relatedToInstances, new Predicate<ProductInstance>() {
            @Override
            public boolean apply(ProductInstance instance) {
                return !(instance.getAssetVersionStatus().equals(AssetVersionStatus.CUSTOMER_ACCEPTED) ||
                         instance.getAssetVersionStatus().equals(AssetVersionStatus.PROVISIONING) ||
                         instance.getAssetVersionStatus().equals(AssetVersionStatus.IN_SERVICE));
            }
        }).isPresent();
        }
        return new OfferAndOrderValidationResult(!isWrongStatus,"The status of related asset is invalid.");
    }

    public OfferAndOrderValidationResult anyLineItemHavingInvalidDiscountStatus(List<String> lineItemIds, String projectId, String quoteOptionId){
        boolean isWrongStatus = false;
        QuoteOptionItemResource quoteOptionItemResource = projectResource.quoteOptionResource(projectId).quoteOptionItemResource(quoteOptionId);
        for(String lineItemId: lineItemIds){
            QuoteOptionItemDTO itemDTO = quoteOptionItemResource.get(lineItemId);
            if(!isWrongStatus){
             if(!(LineItemDiscountStatus.APPROVED.equals(itemDTO.discountStatus) ||
                LineItemDiscountStatus.NOT_APPLICABLE.equals(itemDTO.discountStatus)) ){
                isWrongStatus = true;
             }
            }
        }
        return new OfferAndOrderValidationResult(!isWrongStatus,"The discount status is invalid.");
    }

    public OfferAndOrderValidationResult anyLineItemsWithPricingStatus(String projectId, String quoteOptionId,String customerId,
                                                                       String contractId,List<String> lineItems, List<PricingStatus> statuses){
       boolean isValid = true;
        List<LineItemModel> lineItemModels = lineItemFacade.fetchLineItems(customerId, contractId, projectId, quoteOptionId, PriceSuppressStrategy.None);
        for(LineItemModel model:lineItemModels){
            if(lineItems.contains(model.getId()) && !statuses.contains(model.getPricingStatusOfTree())){
               isValid = false;
            }
        }

        return new OfferAndOrderValidationResult(isValid,"The Pricing status is invalid.");
    }

    public OfferAndOrderValidationResult proxyAssetConfigurationStatus(List<String> lineItems){
        boolean isValid = true;
        List<String> proxyAssetProducts = transform(pmr.getProxyAssetTypeProducts(), toProductCode());
        for(String lineItemId: lineItems){
            ProductInstance productInstance = productInstanceClient.get(new LineItemId(lineItemId));
            if(proxyAssetProducts.contains(productInstance.getProductIdentifier().getProductId())){
                ExternalIdentifierDTO externalIdentifierDTO = productInstanceClient.loadProxyAssetDetails(productInstance.getProductInstanceId().getValue(), productInstance.getProductInstanceVersion().toString());
                if(Strings.isNullOrEmpty(externalIdentifierDTO.getValue())){
                    isValid = false;
                    break;
                }
            }
        }

        return new OfferAndOrderValidationResult(isValid,"Proxy asset is not configured completely.");
    }

    private static Function<ProductIdentifier, String> toProductCode() {
        return new Function<ProductIdentifier, String>() {
            @Override
            public String apply(ProductIdentifier input) {
                return input.getProductId();
            }
        };
    }
}
