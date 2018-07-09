package com.bt.rsqe.customerinventory.service.extenders;

import com.bt.rsqe.customerinventory.service.client.domain.CIFAsset;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetCharacteristic;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetRelationship;
import com.bt.rsqe.customerinventory.service.client.resource.CIFAssetExtension;
import com.bt.rsqe.customerinventory.service.externals.PmrHelper;
import com.bt.rsqe.domain.product.ProductOffering;

import java.util.List;

import static com.bt.rsqe.customerinventory.service.client.resource.CIFAssetExtension.*;
import static com.google.common.collect.Lists.newArrayList;

public class CIFAssetExtender {
    private final PmrHelper pmrHelper;
    private final CharacteristicExtender characteristicExtender;
    private final ProductOfferingExtender productOfferingExtender;
    private final QuoteOptionDetailExtender quoteOptionDetailExtender;
    private final StencilDetailExtender stencilDetailExtender;
    private final SiteDetailExtender siteDetailExtender;
    private final ValidationExtender validationExtender;
    private final AsIsAssetExtender asIsAssetExtender;
    private final SalesRelationshipExtender salesRelationshipExtender;
    private final AccessDetailExtender accessDetailExtender;
    private final SpecialBidExtender specialBidExtender;
    private final ActionExtender actionExtender;
    private final JourneySpecificDetailExtender journeySpecificDetailExtender;
    private final ProductCategoryExtender categoryExtender;

    public CIFAssetExtender(PmrHelper pmrHelper, CharacteristicExtender characteristicExtender,
                            ProductOfferingExtender productOfferingExtender, QuoteOptionDetailExtender quoteOptionDetailExtender,
                            StencilDetailExtender stencilDetailExtender, SiteDetailExtender siteDetailExtender,
                            ValidationExtender validationExtender, AsIsAssetExtender asIsAssetExtender,
                            SalesRelationshipExtender salesRelationshipExtender, AccessDetailExtender accessDetailExtender,
                            SpecialBidExtender specialBidExtender, ActionExtender actionExtender,
                            JourneySpecificDetailExtender journeySpecificDetailExtender, ProductCategoryExtender categoryExtender) {
        this.pmrHelper = pmrHelper;
        this.characteristicExtender = characteristicExtender;
        this.productOfferingExtender = productOfferingExtender;
        this.quoteOptionDetailExtender = quoteOptionDetailExtender;
        this.stencilDetailExtender = stencilDetailExtender;
        this.siteDetailExtender = siteDetailExtender;
        this.validationExtender = validationExtender;
        this.asIsAssetExtender = asIsAssetExtender;
        this.salesRelationshipExtender = salesRelationshipExtender;
        this.accessDetailExtender = accessDetailExtender;
        this.specialBidExtender = specialBidExtender;
        this.actionExtender = actionExtender;
        this.journeySpecificDetailExtender = journeySpecificDetailExtender;
        this.categoryExtender = categoryExtender;
    }

    public void extend(CIFAsset cifAsset, List<CIFAssetExtension> cifAssetExtensions) {
        extend(cifAsset, null, null, cifAssetExtensions);
    }

    public void extend(CIFAsset cifAsset, String userToken, String loginName, List<CIFAssetExtension> cifAssetExtensions) {
        extendAsset(cifAsset, userToken, loginName, cifAssetExtensions, cifAsset.getCharacteristics());
    }

    public void extend(CIFAsset cifAsset, CIFAssetCharacteristic cifAssetCharacteristic, List<CIFAssetExtension> cifAssetExtensions) {
        extendAsset(cifAsset, null, null, cifAssetExtensions, newArrayList(cifAssetCharacteristic));
    }

    private void extendAsset(CIFAsset cifAsset, String userToken, String loginName, List<CIFAssetExtension> cifAssetExtensions, Iterable<CIFAssetCharacteristic> characteristics) {
        ProductOffering productOffering = null;
        if(ProductOfferingDetail.isInList(cifAssetExtensions)) {
            productOffering = pmrHelper.getProductOffering(cifAsset);
        }

        productOfferingExtender.extend(cifAssetExtensions, cifAsset, productOffering);
        quoteOptionDetailExtender.extend(cifAssetExtensions, cifAsset);
        characteristicExtender.extend(cifAssetExtensions, cifAsset, productOffering, characteristics);
        stencilDetailExtender.extend(cifAssetExtensions, cifAsset);
        siteDetailExtender.extend(cifAssetExtensions, cifAsset);
        asIsAssetExtender.extend(cifAssetExtensions, cifAsset);
        validationExtender.extend(cifAssetExtensions, cifAsset);
        accessDetailExtender.extend(cifAssetExtensions, cifAsset);
        salesRelationshipExtender.extend(cifAssetExtensions, cifAsset, productOffering);
        categoryExtender.extend(cifAssetExtensions, cifAsset);
        specialBidExtender.extend(cifAssetExtensions, cifAsset, userToken, loginName);
        actionExtender.extend(cifAssetExtensions, cifAsset);
        journeySpecificDetailExtender.extend(cifAssetExtensions, cifAsset);

        if(Relationships.isInList(cifAssetExtensions)) {
            for (CIFAssetRelationship cifAssetRelationship : cifAsset.getRelationships()) {
                extend(cifAssetRelationship.getRelated(), userToken, loginName, cifAssetExtensions);
            }
        }
    }
}
