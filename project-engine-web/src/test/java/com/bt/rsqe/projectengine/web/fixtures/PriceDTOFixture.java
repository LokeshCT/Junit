package com.bt.rsqe.projectengine.web.fixtures;

import com.bt.rsqe.customerinventory.dto.PriceDTO;
import com.bt.rsqe.enums.Currency;
import com.bt.rsqe.enums.PriceCategory;

import java.math.BigDecimal;

public class PriceDTOFixture {

    public static Builder aPriceDTO() {
        return new Builder();
    }

    public static class Builder {
        private BigDecimal price, discount;
        private PriceCategory category;
        private String classifier;

        public PriceDTO build() {
            return new PriceDTO(category, Currency.UNSPECIFIED, null, price, discount, classifier, null, null);
        }

        public Builder withPrice(double price) {
            this.price = new BigDecimal(price);
            return this;
        }

        public Builder withDiscount(double discount) {
            this.discount = new BigDecimal(discount);
            return this;
        }

        public Builder withCategory(PriceCategory priceCategory) {
            this.category = priceCategory;
            return this;
        }

        public Builder withClassifier(String classifier) {
            this.classifier = classifier;
            return this;
        }
    }

}
