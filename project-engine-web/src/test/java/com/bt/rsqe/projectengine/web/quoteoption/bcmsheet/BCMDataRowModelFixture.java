package com.bt.rsqe.projectengine.web.quoteoption.bcmsheet;

import com.bt.rsqe.customerrecord.SiteDTO;
import com.bt.rsqe.domain.project.ProductInstance;
import com.bt.rsqe.projectengine.QuoteOptionItemDTO;

import java.util.List;

public class BCMDataRowModelFixture {

    public static Builder aBCMRowModelFixture() {
        return new Builder();
    }

    public static class Builder{

        public  QuoteOptionItemDTO quoteOptionItem;
        public SiteDTO site;
        public List<ProductInstance> childProducts;
        public BCMPriceModel aggregatedPriceLine;
        public List<BCMPriceModel> costLines;
        public ProductInstance rootProductInstance;

        public Builder withQuoteOptionItem(QuoteOptionItemDTO quoteOptionItem) {
            this.quoteOptionItem = quoteOptionItem;
            return this;
        }

        public Builder withSite(SiteDTO site){
            this.site = site;
            return this;
        }

        public Builder withRootProductInstance(ProductInstance productModel){
            this.rootProductInstance = productModel;
            return  this;
        }


        public Builder withChildProducts(List<ProductInstance> childProducts){
           this.childProducts = childProducts;
            return  this;
        }

        public Builder withAggregatedPriceLine(BCMPriceModel aggregatedPriceLine){
            this.aggregatedPriceLine = aggregatedPriceLine;
            return  this;
        }

        public Builder withCostLines(List<BCMPriceModel> costLines){
            this.costLines = costLines;
            return  this;
        }

        public BCMDataRowModel build(){
            return new BCMDataRowModel(rootProductInstance,quoteOptionItem,site,childProducts,aggregatedPriceLine,costLines);
        }
    }


}
