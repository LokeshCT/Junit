package com.bt.rsqe.projectengine.web.view;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class QuoteOptionCostDiscountDeltas {
    private List<QuoteOptionCostDiscountDelta> quoteOptionCostDeltas = newArrayList();

    public QuoteOptionCostDiscountDeltas() {
        // JAXB
    }

    public List<QuoteOptionCostDiscountDelta> getQuoteOptionCostDeltas() {
        return quoteOptionCostDeltas;
    }

    @XmlRootElement
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class QuoteOptionCostDiscountDelta {
        public QuoteOptionCostDiscountDelta() {
            // JAXB
        }

        private String lineItemId;
        private String description;
        private String vendorDiscountRef;
        private CostDiscount oneTimeDiscount;
        private CostDiscount recurringDiscount;
        private boolean isManualPricing;
        private boolean isGrossAdded;

        public String getLineItemId() {
            return lineItemId;
        }

        public String getDescription() {
            return description;
        }

        public String getVendorDiscountRef() {
            return vendorDiscountRef;
        }

        public CostDiscount getOneTimeDiscount() {
            return oneTimeDiscount;
        }

        public CostDiscount getRecurringDiscount() {
            return recurringDiscount;
        }

        public boolean isManualPricing() { return isManualPricing; };

        public boolean isGrossAdded() { return isGrossAdded; };
    }

    @XmlRootElement
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class CostDiscount {
        public CostDiscount() {
            /* JAXB */
        }

        private String priceLineId;
        private String discount;
        private boolean discountUpdated;
        private String currentDiscount;
        private String grossValue;

        public String getPriceLineId() {
            return priceLineId;
        }

        public String getDiscount() {
            return discount;
        }

        public boolean isDiscountUpdated() {
            return discountUpdated;
        }

        public String getCurrentDiscount() {
            return currentDiscount;
        }

        public String getGrossValue() {
            return grossValue;
        }
    }
}