package com.bt.rsqe.projectengine.web.quoteoptionpricing.pricingsheet.model;

import com.bt.rsqe.customerrecord.SiteDTO;
import com.bt.rsqe.domain.project.ProductInstance;
import com.bt.rsqe.pricing.PricingClient;
import com.bt.rsqe.productinstancemerge.MergeResult;
import com.bt.rsqe.projectengine.QuoteOptionItemDTO;
import com.bt.rsqe.projectengine.web.quoteoptionpricing.PriceSuppressStrategy;
import com.google.common.base.Optional;
import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.bt.rsqe.utils.AssertObject.*;

public class PricingSheetSpecialBidProduct extends AbstractPricingSheetProductModel {

    Map<String, String> attributes;


    public PricingSheetSpecialBidProduct(SiteDTO site, QuoteOptionItemDTO quoteOptionItem, Map<String, String> attributes, MergeResult mergeResult
        , ProductInstance productInstance, PricingClient pricingClient, Optional<ProductInstance> asIs) {
        super(site, productInstance, quoteOptionItem, mergeResult, pricingClient, asIs);
        this.attributes = attributes;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public String getAttributeValueFor(String attributeName) {
        final String value = this.attributes.get(attributeName);
        return isNull(value) ? StringUtils.EMPTY : value;
    }

    public List<PricingSheetPriceModel> getDetailedPriceLines() {
        return getPriceLines(PriceSuppressStrategy.DetailedSheet);
    }

    public List<PricingSheetPriceModel> getDetailedPriceLines(String priceType) {
        return filterPriceLineForAction(getPriceLines(PriceSuppressStrategy.DetailedSheet),priceType);
    }
}
