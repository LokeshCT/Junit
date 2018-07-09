package com.bt.rsqe.projectengine.web.quoteoptionpricing.pricingsheet.model;

import com.bt.rsqe.customerrecord.SiteDTO;
import com.bt.rsqe.domain.project.ProductInstance;
import com.bt.rsqe.pricing.PricingClient;
import com.bt.rsqe.productinstancemerge.MergeResult;
import com.bt.rsqe.projectengine.CaveatResource;
import com.bt.rsqe.projectengine.QuoteOptionItemDTO;
import com.google.common.base.Optional;

public class PricingSheetProductModelFactory {

    public static PricingSheetProductModel create(SiteDTO site, ProductInstance productInstance, QuoteOptionItemDTO quoteOptionItem, MergeResult mergeResult, CaveatResource caveatResource, PricingClient pricingClient, Optional<ProductInstance> asIs) {
        return new PricingSheetProductModel(site, productInstance, quoteOptionItem, mergeResult, caveatResource, pricingClient, asIs);
    }
}
