package com.bt.rsqe.customerinventory.service.extenders;

import com.bt.rsqe.customerinventory.service.client.domain.CIFAsset;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetOfferingDetail;
import com.bt.rsqe.customerinventory.service.client.resource.CIFAssetExtension;
import com.bt.rsqe.domain.product.ProductOffering;
import com.bt.rsqe.domain.product.extensions.StructuredRule;
import com.bt.rsqe.domain.product.parameters.ProductIdentifier;
import com.google.common.base.Strings;

import java.util.List;

import static com.bt.rsqe.customerinventory.service.client.resource.CIFAssetExtension.*;

public class ProductOfferingExtender {
    public void extend(List<CIFAssetExtension> cifAssetExtensions, CIFAsset cifAsset, ProductOffering productOffering) {
        if(ProductOfferingDetail.isInList(cifAssetExtensions)) {
            extendProductOfferingDetail(cifAsset, productOffering);
        }

        if(ProductRules.isInList(cifAssetExtensions)){
            extendProductRules(cifAsset, productOffering);
        }
    }

    private void extendProductRules(CIFAsset cifAsset, ProductOffering productOffering) {
        List<StructuredRule> rules = productOffering.getRules();
        cifAsset.loadProductRules(rules);
    }

    private void extendProductOfferingDetail(CIFAsset cifAsset, ProductOffering productOffering) {
        final String productOfferingName = productOffering.getProductIdentifier().getProductName();
        final String productOfferingDisplayName = !Strings.isNullOrEmpty(productOffering.getProductIdentifier().getDisplayName()) ? productOffering.getProductIdentifier().getDisplayName() : productOfferingName;
        final String productGroupName = productOffering.getProductGroupName().value();
        final ProductIdentifier legacyIdentifier = productOffering.getLegacyIdentifier();
        final String legacyId = legacyIdentifier == null ? "" : legacyIdentifier.getProductId();
        final boolean bearer = productOffering.isBearer();
        final boolean apeFlag = productOffering.hasApeFlag();
        final String proposition = productOffering.getProposition();
        final boolean cpe = productOffering.isCpe();

        cifAsset.loadOfferingDetail(new CIFAssetOfferingDetail(productOfferingName, productOfferingDisplayName, productGroupName, legacyId, bearer, apeFlag,
                                                               proposition, cpe, productOffering.isAvailable(), productOffering.getSimpleProductOfferingType()));
    }
}
