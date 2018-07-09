package com.bt.rsqe.projectengine.web.quoteoptionpricing.pricingsheet.model;

import com.bt.rsqe.customerrecord.SiteDTO;
import com.bt.rsqe.domain.product.ProductOffering;
import com.bt.rsqe.domain.project.ProductInstance;
import com.bt.rsqe.pricing.PricingClient;
import com.bt.rsqe.productinstancemerge.MergeResult;
import com.bt.rsqe.projectengine.QuoteOptionItemDTO;
import com.bt.rsqe.projectengine.web.quoteoptionpricing.PriceSuppressStrategy;
import com.google.common.base.Optional;

import java.util.List;

public class PricingSheetContractProduct extends AbstractPricingSheetProductModel {
    public PricingSheetContractProduct(SiteDTO site, QuoteOptionItemDTO quoteOptionItem, MergeResult mergeResult, ProductInstance productInstance, PricingClient pricingClient, Optional<ProductInstance> asIs) {
        super(site, productInstance, quoteOptionItem, mergeResult, pricingClient, asIs);
    }

    public String getPlanName() {
        return getInstanceCharacteristic(ProductOffering.PLAN_NAME);
    }

    public String getCallCommitment() {
        return getInstanceCharacteristic(ProductOffering.CALL_COMMITMENT);
    }

    @Override
    public List<PricingSheetPriceModel> getAllDetailSheetPriceLines() {
        return getAllPriceLines(PriceSuppressStrategy.DetailedSheet);
    }

    @Override
    public List<PricingSheetPriceModel> getAllDetailSheetPriceLines(String priceType) {
        return filterPriceLineForAction(getAllChildPriceLines(PriceSuppressStrategy.DetailedSheet), priceType);
    }
}
