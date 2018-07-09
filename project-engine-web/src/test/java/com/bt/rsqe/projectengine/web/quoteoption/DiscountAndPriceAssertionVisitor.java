package com.bt.rsqe.projectengine.web.quoteoption;

import com.bt.rsqe.Money;
import com.bt.rsqe.Percentage;
import com.bt.rsqe.enums.PriceCategory;
import com.bt.rsqe.enums.PriceType;
import com.bt.rsqe.projectengine.web.model.LineItemModel;
import com.bt.rsqe.projectengine.web.model.OneVoicePriceTariff;
import com.bt.rsqe.projectengine.web.model.PriceLineModel;
import com.bt.rsqe.projectengine.web.model.PriceModel;
import com.bt.rsqe.projectengine.web.model.lineitemvisitor.AbstractLineItemVisitor;

import java.util.List;

import static com.google.common.collect.Lists.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.*;
import static org.junit.Assert.*;

public class DiscountAndPriceAssertionVisitor extends AbstractLineItemVisitor{

    private List<Assertion> assertions = newArrayList();
    private List<Assertion> satisfiedAssertions = newArrayList();
    private long currentPpsrId;
    PriceType priceType;

    private DiscountAndPriceAssertionVisitor() {
    }

    public void visit(LineItemModel lineItem) {

    }

    @Override
    public void visit(PriceLineModel priceLine) {
        currentPpsrId = priceLine.getPpsrId();
        try{
            OneVoicePriceTariff oneVoicePriceTariff = OneVoicePriceTariff.forId(currentPpsrId);
            priceType = oneVoicePriceTariff.priceType();
        }catch(IllegalArgumentException ex){
           priceType = priceLine.getPriceType();
        }
    }

    @Override
    public void visit(PriceModel priceModel) {
        for (Assertion assertion : assertions) {
            if (priceModel.isSatisfiedBy(priceType, assertion.category) && currentPpsrId == assertion.ppsrId ) {
                assertion.checkSatisfaction(priceModel);
            }
        }
    }

    @Override
    public String toString() {
        return "OneTimeDiscountAssertionVisitor{" +
               "assertions=" + assertions +
               '}';
    }

    public void verfifyAllAssertionsSatisfied() {
        for (Assertion assertion : assertions) {
            if (!satisfiedAssertions.contains(assertion)) {
                fail(String.format("assertion: %s not satified", assertion));
            }
        }
    }

    public void reset() {
        satisfiedAssertions.clear();
    }

    public static class Builder {
        private DiscountAndPriceAssertionVisitor assertionVisitor;

        public Builder() {
            assertionVisitor = new DiscountAndPriceAssertionVisitor();
        }

        public Builder withDiscount(long ppsrId, PriceCategory category, Percentage discount) {
            assertionVisitor.assertions.add(assertionVisitor.new DiscountAssertion(ppsrId, category, discount));
            return this;
        }

        public Builder withPrice(OneVoicePriceTariff ppsrId, PriceCategory category, Money price) {
            assertionVisitor.assertions.add(assertionVisitor.new PriceAssertion(ppsrId.ppsrId(), category, price));
            return this;
        }

        public DiscountAndPriceAssertionVisitor build() {
            return assertionVisitor;
        }
    }

    private abstract class Assertion {
        final long ppsrId;
        final PriceCategory category;

        protected Assertion(long ppsrId, PriceCategory category) {
            this.ppsrId = ppsrId;
            this.category = category;
        }

        abstract void checkSatisfaction(PriceModel priceModel);
    }

    private class DiscountAssertion extends Assertion {
        private final Percentage discount;

        private DiscountAssertion(long ppsrId, PriceCategory category, Percentage discount) {
            super(ppsrId, category);
            this.discount = discount;
        }

        @Override
        public String toString() {
            return "Assertion{" +
                   "ppsrId=" + ppsrId +
                   ", category=" + category +
                   ", discount=" + discount +
                   '}';
        }

        public void checkSatisfaction(PriceModel priceModel) {
            assertThat(priceModel.getDiscountPercentage(), is(discount));
            satisfiedAssertions.add(this);
        }
    }

    private class PriceAssertion extends Assertion {
        private final Money price;

        private PriceAssertion(long ppsrId, PriceCategory category, Money price) {
            super(ppsrId, category);
            this.price = price;
        }

        @Override
        public String toString() {
            return "Assertion{" +
                   "ppsrId=" + ppsrId +
                   ", category=" + category +
                   ", price=" + price +
                   '}';
        }

        public void checkSatisfaction(PriceModel priceModel) {
            assertThat(priceModel.getValue(), is(price));
            satisfiedAssertions.add(this);
        }
    }
}
