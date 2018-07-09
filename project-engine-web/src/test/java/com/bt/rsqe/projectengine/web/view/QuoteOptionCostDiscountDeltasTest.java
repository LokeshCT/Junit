package com.bt.rsqe.projectengine.web.view;

import com.google.gson.Gson;
import org.hamcrest.core.Is;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.is;

public class QuoteOptionCostDiscountDeltasTest {
    @Test
    public void shouldParseJSONStringToQuoteOptionCostDiscountDeltasObject() throws Exception {
        String jsonString = "{" +
                            "\"quoteOptionCostDeltas\": [{" +
                            "\"lineItemId\": \"L1\"," +
                            "\"description\": \"aDescription1\"," +
                            "\"vendorDiscountRef\": \"aVendorDiscountRef1\"," +
                            "\"oneTimeDiscount\": {" +
                            "\"priceLineId\": \"P1\"," +
                            "\"discount\": \"1\"," +
                            "\"grossValue\": \"20\"" +
                            "}," +
                            "\"recurringDiscount\": {" +
                            "\"priceLineId\": \"P2\"," +
                            "\"discount\": \"2\"," +
                            "\"grossValue\": \"20\"" +
                            "}" +
                            "}," +
                            "{" +
                            "\"lineItemId\": \"L2\"," +
                            "\"description\": \"aDescription2\"," +
                            "\"vendorDiscountRef\": \"aVendorDiscountRef2\"," +
                            "\"oneTimeDiscount\": {" +
                            "\"priceLineId\": \"P3\"," +
                            "\"discount\": \"3\"," +
                            "\"grossValue\": \"30\"" +
                "}," +
                            "\"recurringDiscount\": {" +
                            "\"priceLineId\": \"P4\"," +
                            "\"discount\": \"4\"," +
                            "\"grossValue\": \"30\"" +
                "}" +
                            "}]" +
                            "}";

        QuoteOptionCostDiscountDeltas costDeltas = new Gson().fromJson(jsonString, QuoteOptionCostDiscountDeltas.class);

        assertThat(costDeltas.getQuoteOptionCostDeltas().size(), Is.is(2));
        verifyCostDelta(costDeltas.getQuoteOptionCostDeltas().get(0), "L1", "aDescription1", "aVendorDiscountRef1", "P1", "1", "P2", "2", "20");
        verifyCostDelta(costDeltas.getQuoteOptionCostDeltas().get(1), "L2", "aDescription2", "aVendorDiscountRef2", "P3", "3", "P4", "4", "30");
    }

    private void verifyCostDelta(QuoteOptionCostDiscountDeltas.QuoteOptionCostDiscountDelta delta,
                                 String lineItem,
                                 String description,
                                 String vendorDiscountRef,
                                 String oneTimeId,
                                 String oneTimeDiscount,
                                 String recurringId,
                                 String recurringDiscount, String grossValue) {
        assertThat(delta.getLineItemId(), is(lineItem));
        assertThat(delta.getDescription(), is(description));
        assertThat(delta.getVendorDiscountRef(), is(vendorDiscountRef));
        assertThat(delta.getOneTimeDiscount().getPriceLineId(), is(oneTimeId));
        assertThat(delta.getOneTimeDiscount().getDiscount(), is(oneTimeDiscount));
        assertThat(delta.getRecurringDiscount().getPriceLineId(), is(recurringId));
        assertThat(delta.getRecurringDiscount().getDiscount(), is(recurringDiscount));
        assertThat(delta.getOneTimeDiscount().getGrossValue(), is(grossValue));
        assertThat(delta.getRecurringDiscount().getGrossValue(), is(grossValue));
    }
}
