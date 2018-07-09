package com.bt.rsqe.projectengine.web.view;

import com.bt.rsqe.projectengine.web.view.filtering.PaginatedFilterResult;
import com.google.common.collect.ComparisonChain;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class QuoteOptionUsagePricingDTO extends BaseQuoteOptionPricingDTO {
    @XmlElement
    public List<UsageProduct> products;

    public QuoteOptionUsagePricingDTO() {
        super();
    }

    public QuoteOptionUsagePricingDTO(List<UsageProduct> products, PaginatedFilterResult filterResult) {
        super(filterResult);
        this.products = products;
    }

    public static class UsageProduct {
        @XmlElement
        public String productName;
        @XmlElement
        public String pricingModel;
        @XmlElement
        public String lineItemId;
        @XmlElement
        public String summary;

        @XmlElement
        public List<UsagePriceLine> priceLines;
    }

    public static class UsagePriceLine {
        @XmlElement
        public String description;
        @XmlElement
        public List<UsageItemRowDTO> tiers;
    }

    public static class UsageItemRowDTO implements Comparable<UsageItemRowDTO> {
        @XmlElement
        public String lineItemId;
        @XmlElement
        public String priceLineId;
        @XmlElement
        public String product;
        @XmlElement
        public String description;
        @XmlElement
        public String tier;
        @XmlElement
        public String tierDescription;
        @XmlElement
        public String pricingModel;
        @XmlElement
        public String summary;
        @XmlElement
        public PriceLineDTO minCharge;
        @XmlElement
        public PriceLineDTO fixedCharge;
        @XmlElement
        public PriceLineDTO chargeRate;

        @Override
        public int compareTo(UsageItemRowDTO o) {
            int t1 = Integer.parseInt(tier.replaceAll("\\D+",""));
            int t2 = Integer.parseInt(o.tier.replaceAll("\\D+",""));
            return ComparisonChain.start().compare(t1, t2).result();
        }
    }
}
