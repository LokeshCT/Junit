package com.bt.rsqe.projectengine.web.view;

import com.bt.rsqe.enums.PriceCategory;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;

import javax.annotation.Nullable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.math.BigDecimal;
import java.util.List;

import static com.google.common.collect.Lists.*;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class QuoteOptionPricingDeltas {
    private List<QuoteOptionPricingDelta> quoteOptionPricingDeltas = newArrayList();

    public QuoteOptionPricingDeltas() {
        // JAXB
    }

    public List<QuoteOptionPricingDelta> getQuoteOptionPricingDeltas() {
        return quoteOptionPricingDeltas;
    }

    @XmlRootElement
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class QuoteOptionPricingDelta {
        public QuoteOptionPricingDelta() {
            // JAXB
        }

        public QuoteOptionPricingDelta(String lineItemId,
                                       String priceLineId,
                                       String classifier,
                                       String minChargeDiscount,
                                       String fixedChargeDiscount,
                                       String chargeRateDiscount) {
            this.lineItemId = lineItemId;
            this.priceLineId = priceLineId;
            this.classifier = classifier;
            this.minChargeDiscount = minChargeDiscount;
            this.fixedChargeDiscount = fixedChargeDiscount;
            this.chargeRateDiscount = chargeRateDiscount;
        }

        private String lineItemId;
        private String priceLineId;
        private String classifier;
        private String minChargeDiscount;
        private String fixedChargeDiscount;
        private String chargeRateDiscount;

        public String getLineItemId() {
            return lineItemId;
        }

        public String getPriceLineId() {
            return priceLineId;
        }

        public String getClassifier() {
            return classifier;
        }

        public BigDecimal getChargeDiscount(PriceCategory priceCategory) {
            switch(priceCategory) {
                case FIXED_CHARGE: return getDiscountOrNull(fixedChargeDiscount);
                case MIN_CHARGE: return getDiscountOrNull(minChargeDiscount);
                case CHARGE_RATE: return getDiscountOrNull(chargeRateDiscount);
                default: return null;
            }
        }

        private BigDecimal getDiscountOrNull(String discount) {
            return !Strings.isNullOrEmpty(discount) ? new BigDecimal(discount) : null;
        }
    }

    public static List<QuoteOptionPricingDelta> filterByLineItem(QuoteOptionPricingDeltas deltas, final String lineItemId) {
        return newArrayList(Iterables.filter(deltas.getQuoteOptionPricingDeltas(), new Predicate<QuoteOptionPricingDelta>() {
            @Override
            public boolean apply(QuoteOptionPricingDelta input) {
                return lineItemId.equals(input.getLineItemId());
            }
        }));
    }
}
