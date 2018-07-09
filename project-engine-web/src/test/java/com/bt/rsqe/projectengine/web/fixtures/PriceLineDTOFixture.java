package com.bt.rsqe.projectengine.web.fixtures;

import com.bt.rsqe.customerinventory.dto.PriceDTO;
import com.bt.rsqe.customerinventory.dto.PriceLineDTO;
import com.bt.rsqe.customerinventory.parameter.PmfId;
import com.bt.rsqe.customerinventory.parameter.PpsrId;
import com.bt.rsqe.customerinventory.parameter.PriceLineId;
import com.bt.rsqe.customerinventory.parameter.PriceLineStatus;
import com.bt.rsqe.enums.PriceCategory;
import com.bt.rsqe.enums.PriceType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static com.bt.rsqe.projectengine.web.fixtures.PriceDTOFixture.*;

public class PriceLineDTOFixture {

    public static Builder aPriceLineDTO() {
        return new Builder();
    }

    public static class Builder {
        private Map<PriceCategory, PriceDTOFixture.Builder> priceDtoFixtures = new HashMap<PriceCategory, PriceDTOFixture.Builder>();
        private PriceType priceType = PriceType.UNSPECIFIED;
        private PriceLineStatus priceLineStatus = PriceLineStatus.NOT_PRICED;
        private long ppsrId = new Random().nextLong();
        private String priceLineId;
        private String description;
        private String chargingScheme;
        private String tariffType;
        private String pmfId= "";
        private String vendorDiscountRef;
        private String userEntered;

        public PriceLineDTO build() {
            final ArrayList<PriceDTO> prices = new ArrayList<PriceDTO>();
            for (PriceDTOFixture.Builder builder : priceDtoFixtures.values()) {
                prices.add(builder.build());
            }

            return new PriceLineDTO(new PriceLineId(priceLineId),
                                    null,
                                    new PpsrId(ppsrId),
                                    priceType,
                                    null,
                                    priceLineStatus,
                                    description,
                                    prices,
                                    new PmfId(pmfId)
                , null, null, null, null, null, null, null, tariffType, chargingScheme, null, null, null, null, null, null, vendorDiscountRef, userEntered, null);
        }

        public PriceLineDTOFixture.Builder withPrice(PriceCategory priceCategory, double price) {
            return withPrice(priceCategory, price, null);
        }

        public PriceLineDTOFixture.Builder withPrice(PriceCategory priceCategory, double price, String classifier) {
            PriceDTOFixture.Builder builder = priceDtoFixtureFor(priceCategory, classifier);
            builder.withPrice(price);
            return this;
        }

        public PriceLineDTOFixture.Builder withChargePrice(double price) {
            withPrice(PriceCategory.CHARGE_PRICE, price);
            return this;
        }

        public Builder withVendorDiscountRef(String vendorDiscountRef) {
            this.vendorDiscountRef = vendorDiscountRef;
            return this;
        }

        public PriceLineDTOFixture.Builder withEupPrice(double price) {
            withPrice(PriceCategory.END_USER_PRICE, price);
            return this;
        }

        public PriceLineDTOFixture.Builder withDiscount(PriceCategory priceCategory, double discount) {
            PriceDTOFixture.Builder builder = priceDtoFixtureFor(priceCategory, null);
            builder.withDiscount(discount);
            return this;
        }

        public PriceLineDTOFixture.Builder withChargePriceDiscount(double discount) {
            withDiscount(PriceCategory.CHARGE_PRICE, discount);
            return this;
        }

        public PriceLineDTOFixture.Builder withEupPriceDiscount(double discount) {
            withDiscount(PriceCategory.END_USER_PRICE, discount);
            return this;
        }

        public PriceLineDTOFixture.Builder withPtpPriceDiscount(double discount) {
            withDiscount(PriceCategory.PRICE_TO_PARTNER, discount);
            return this;
        }

        public Builder with(PriceType priceType) {
            this.priceType = priceType;
            return this;
        }

        public Builder with(PriceLineStatus status) {
            this.priceLineStatus = status;
            return this;
        }

        public Builder withPpsrId(long ppsrId) {
            this.ppsrId = ppsrId;
            return this;
        }

        public Builder withId(String id) {
            this.priceLineId = id;
            return this;
        }

        public Builder withDescription(String description) {
            this.description = description;
            return this;
        }

        public Builder withChargingScheme(String chargingScheme) {
            this.chargingScheme = chargingScheme;
            return this;
        }

        public Builder withTariffType(String tariffType) {
            this.tariffType = tariffType;
            return this;
        }

        public Builder withPmfId(String pmfId) {
            this.pmfId = pmfId;
            return this;
        }

        public Builder withUserEntered(String userEntered) {
            this.userEntered = userEntered;
            return this;
        }

        private PriceDTOFixture.Builder priceDtoFixtureFor(PriceCategory priceCategory, String classifier) {
            PriceDTOFixture.Builder builder = priceDtoFixtures.get(priceCategory);
            if (builder == null) {
                builder = aPriceDTO().withCategory(priceCategory).withClassifier(classifier);
                priceDtoFixtures.put(priceCategory, builder);
            }
            return builder;
        }
    }

}
