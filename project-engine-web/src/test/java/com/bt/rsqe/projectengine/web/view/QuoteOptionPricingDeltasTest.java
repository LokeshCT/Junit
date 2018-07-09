package com.bt.rsqe.projectengine.web.view;

import com.bt.rsqe.enums.PriceCategory;
import com.google.gson.Gson;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;

public class QuoteOptionPricingDeltasTest {
    private static final String PRICING_DELTA = "{lineItemId: '%s', minChargeDiscount:'1', fixedChargeDiscount:'2', chargeRateDiscount:'3'}";
    private static final String PRICING_DELTAS = "{quoteOptionPricingDeltas : ["+String.format(PRICING_DELTA, "L1")+","+String.format(PRICING_DELTA, "L2")+"]}";

    @Test
    public void shouldGetChargeDiscountForUsagePriceCategory() throws Exception {
        QuoteOptionPricingDeltas.QuoteOptionPricingDelta delta = new Gson().fromJson(PRICING_DELTA, QuoteOptionPricingDeltas.QuoteOptionPricingDelta.class);

        assertThat(delta.getChargeDiscount(PriceCategory.MIN_CHARGE), is(new BigDecimal("1")));
        assertThat(delta.getChargeDiscount(PriceCategory.FIXED_CHARGE), is(new BigDecimal("2")));
        assertThat(delta.getChargeDiscount(PriceCategory.CHARGE_RATE), is(new BigDecimal("3")));
    }

    @Test
    public void shouldReturnNullDiscountForInvalidCategory() throws Exception {
        QuoteOptionPricingDeltas.QuoteOptionPricingDelta delta = new Gson().fromJson(PRICING_DELTA, QuoteOptionPricingDeltas.QuoteOptionPricingDelta.class);
        assertThat(delta.getChargeDiscount(PriceCategory.END_USER_PRICE), is(nullValue()));
    }

    @Test
    public void shouldFilterPricingDeltasByLineItemId() throws Exception {
        QuoteOptionPricingDeltas pricingDeltas = new Gson().fromJson(PRICING_DELTAS, QuoteOptionPricingDeltas.class);
        List<QuoteOptionPricingDeltas.QuoteOptionPricingDelta> filteredDeltas = QuoteOptionPricingDeltas.filterByLineItem(pricingDeltas, "L1");
        assertThat(filteredDeltas.size(), is(1));
        assertThat(filteredDeltas.get(0).getLineItemId(), is("L1"));
    }
}
