package com.bt.rsqe.customerinventory.service.updates;

import com.bt.rsqe.customerinventory.service.client.domain.CIFAsset;
import com.bt.rsqe.customerinventory.service.client.domain.updates.InvalidatePriceRequest;
import com.bt.rsqe.customerinventory.service.externals.PmrHelper;
import com.bt.rsqe.domain.product.Attribute;
import com.bt.rsqe.domain.product.ConfigurationPhase;
import com.bt.rsqe.domain.product.ProductOffering;
import com.bt.rsqe.domain.project.PricingStatus;
import com.google.common.base.Optional;

import java.util.List;

import static com.bt.rsqe.customerinventory.service.client.domain.updates.InvalidatePriceRequest.ChangeType.*;
import static com.bt.rsqe.domain.product.ConfigurationPhase.PRE_CREDIT_VET;
import static com.bt.rsqe.domain.project.PricingStatus.*;
import static com.google.common.collect.Lists.*;

public class InvalidatePriceRequestBuilder {
    private final PmrHelper pmrHelper;
    private final List<PricingStatus> ignorablePricingStatus = newArrayList(NOT_PRICED, PricingStatus.NOT_APPLICABLE);

    public InvalidatePriceRequestBuilder(PmrHelper pmrHelper) {
        this.pmrHelper = pmrHelper;
    }

    public Optional<InvalidatePriceRequest> invalidatePriceForRelationshipChange(CIFAsset cifAsset) {
        if (!ignorablePricingStatus.contains(cifAsset.getPricingStatus())) {
            InvalidatePriceRequest request = new InvalidatePriceRequest(cifAsset.getLineItemId(),
                    cifAsset.getQuoteOptionItemDetail().getLockVersion(),
                    cifAsset.getAssetKey(),
                    RelationshipChange);

            return Optional.of(request);
        }
        return Optional.absent();
    }

    public Optional<InvalidatePriceRequest> invalidatePriceForStencilChange(CIFAsset cifAsset) {
        if (NOT_PRICED != cifAsset.getPricingStatus()) {
            return Optional.of(new InvalidatePriceRequest(cifAsset.getLineItemId(), cifAsset.getQuoteOptionItemDetail().getLockVersion(), cifAsset.getAssetKey(), StencilChange));
        }
        return Optional.absent();
    }

    public Optional<InvalidatePriceRequest> invalidatePriceForCharacteristicChanges(CIFAsset cifAsset, List<String> characteristics) {
        if (!ignorablePricingStatus.contains(cifAsset.getPricingStatus()) && hasRfqAttribute(cifAsset, characteristics)) {
            return Optional.of(new InvalidatePriceRequest(cifAsset.getLineItemId(), cifAsset.getQuoteOptionItemDetail().getLockVersion(), cifAsset.getAssetKey(), PriceAffectingChange));
        }
        return Optional.absent();
    }

    private boolean hasRfqAttribute(CIFAsset cifAsset, List<String> attributeNames) {
        ProductOffering productOffering = pmrHelper.getProductOffering(cifAsset);
        for (String attributeName : attributeNames) {
            for (Attribute productOfferingAttribute : productOffering.getAttributes()) {
                if(productOfferingAttribute.getName().getName().equals(attributeName)) {
                    if(productOfferingAttribute.isAttributeRequiredForPhase(PRE_CREDIT_VET))  {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
