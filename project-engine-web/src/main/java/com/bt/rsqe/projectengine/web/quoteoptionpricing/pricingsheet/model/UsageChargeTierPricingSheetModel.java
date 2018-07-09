package com.bt.rsqe.projectengine.web.quoteoptionpricing.pricingsheet.model;

import com.bt.rsqe.Money;
import com.bt.rsqe.domain.project.Price;
import com.bt.rsqe.enums.PriceCategory;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Iterables;

import java.util.Collection;

import static com.bt.rsqe.utils.Strings.leaveOnlyDigits;

public class UsageChargeTierPricingSheetModel extends PricingSheetPriceModel implements Comparable<UsageChargeTierPricingSheetModel> {
    private String classifier;
    private Price minCharge;
    private Price fixedCharge;
    private Price chargeRate;

    public UsageChargeTierPricingSheetModel(PricingSheetPriceModel parent,
                                            String classifier,
                                            Collection<Price> prices) {
        super(parent.getPmfId(),
              parent.getOneTimePrice(),
              parent.getRentalPrice(),
              parent.getUsagePrice(),
              String.valueOf(parent.getContractTerm()),
              parent.getPriceType(),
              parent.getOwningInstance(),
              parent.getAsIsInstance());
        this.classifier = classifier;
        this.minCharge = getPriceByCategory(prices, PriceCategory.MIN_CHARGE);
        this.fixedCharge = getPriceByCategory(prices, PriceCategory.FIXED_CHARGE);
        this.chargeRate = getPriceByCategory(prices, PriceCategory.CHARGE_RATE);
    }

    @Override
    public Number getNonRecurringEupPrice() {
        return null; // usage charges don't have this price
    }

    @Override
    public Number getRecurringEupPrice() {
        return null; // usage charges don't have this price
    }

    @Override
    public Number getMinCharge() {
        return minCharge == null ? null : Money.from(minCharge.getDiscountedPrice()).toBigDecimal();
    }

    @Override
    public Number getFixedCharge() {
        return fixedCharge == null ? null : Money.from(fixedCharge.getDiscountedPrice()).toBigDecimal();
    }

    @Override
    public Number getChargeRate() {
        return chargeRate == null ? null : chargeRate.getDiscountedPrice();
    }

    @Override
    public String getDescription() {
        return super.getDescription() + " - " + classifier;
    }

    private Price getPriceByCategory(Collection<Price> prices, final PriceCategory category) {
        Optional<Price> price = Iterables.tryFind(prices, new Predicate<Price>() {
            @Override
            public boolean apply(Price input) {
                return category.equals(PriceCategory.forLabel(input.getCategory()));
            }
        });

        if(price.isPresent()) {
            return price.get();
        } else {
            return null;
        }
    }

    @Override
    public int compareTo(UsageChargeTierPricingSheetModel o) {
        int t1 = Integer.parseInt(leaveOnlyDigits(classifier));
        int t2 = Integer.parseInt(leaveOnlyDigits(o.classifier));
        return ComparisonChain.start().compare(t1, t2).result();
    }
}
