package com.bt.rsqe.projectengine.web.quoteoptionpricing.pricingsheet.model;

import com.bt.rsqe.customerrecord.SiteDTO;
import com.bt.rsqe.domain.project.ProductInstance;
import com.bt.rsqe.pricing.PricingClient;
import com.bt.rsqe.productinstancemerge.MergeResult;
import com.bt.rsqe.projectengine.CaveatResource;
import com.bt.rsqe.projectengine.QuoteOptionItemDTO;
import com.google.common.base.Optional;

public class PricingSheetProductModelFixture {

    public static Builder aPricingSheetProductModel() {
        return new Builder();
    }

    public static class Builder {
        public ProductInstance productInstance;
        public SiteDTO site;
        public QuoteOptionItemDTO quoteOptionItem;
        public MergeResult mergeResult;
        private CaveatResource caveatResource;
        private PricingClient pricingClient;

        public Builder withProductInstance(ProductInstance productInstance) {
            this.productInstance = productInstance;
            return this;
        }

        public Builder withSiteDTO(SiteDTO site) {
            this.site = site;
            return this;
        }

        public Builder withQuoteOptionItem(QuoteOptionItemDTO quoteOptionItem) {
            this.quoteOptionItem = quoteOptionItem;
            return this;
        }

        public Builder withMergeResult(MergeResult mergeResult) {
            this.mergeResult = mergeResult;
            return this;
        }

        public Builder withCaveatResource(CaveatResource caveatResource) {
            this.caveatResource = caveatResource;
            return this;
        }

        public Builder withPricingClient(PricingClient pricingClient){
            this.pricingClient = pricingClient;
            return this;
        }

        public PricingSheetProductModel build() {
            return new PricingSheetProductModel(site,
                                                productInstance, quoteOptionItem, mergeResult, caveatResource, pricingClient, Optional.<ProductInstance>absent());
        }
    }

}
