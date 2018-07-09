package com.bt.rsqe.projectengine.web.quoteoption.pricing;


import com.bt.rsqe.customerinventory.dto.PriceDTO;
import com.bt.rsqe.enums.PriceCategory;
import com.bt.rsqe.enums.PriceType;
import com.bt.rsqe.enums.Currency;
import com.bt.rsqe.Money;
import com.bt.rsqe.projectengine.web.model.PriceModel;
import org.junit.Test;

import java.math.BigDecimal;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.core.Is.*;

public class PriceModelTest {

    @Test
    public void shouldReturnZeroNetValueWhenPriceIsNull() {
        assertThatNetValueIsCorrect(null, null, Money.ZERO);
    }

    @Test
    public void shouldReturnZeroNetValueWhenPriceIsNullAndDiscountIsNotNull() {
        assertThatNetValueIsCorrect(null, new BigDecimal("3"), Money.ZERO);
    }

    @Test
    public void shouldReturnPriceAsNetValueWhenDiscountPercentIsNull() {
         assertThatNetValueIsCorrect(new BigDecimal("3"), null, Money.from("3"));
    }

    @Test
    public void shouldReturnPriceAsNetValueWhenDiscountPercentIsZero() {
         assertThatNetValueIsCorrect(new BigDecimal("3"), BigDecimal.ZERO, Money.from("3"));
    }

    @Test
    public void shouldReturnDiscountedPriceWhenDiscountSet() {
         assertThatNetValueIsCorrect(new BigDecimal("12"), new BigDecimal("50"), Money.from("6"));
    }

    @Test
    public void shouldReturnZeroValueWhenPriceIsNull() {
        assertThatValueIsCorrect(null, Money.ZERO);
    }

    @Test
    public void shouldReturnPrice() {
         assertThatValueIsCorrect(Money.from("12"), Money.from("12"));
    }

    @Test
    public void shouldReturnType() throws Exception {
        final PriceModel priceModel = new PriceModel(new PriceDTO(null, null), PriceType.RECURRING, null);
        assertThat(priceModel.getType(), is(PriceType.RECURRING));
    }

    private void assertThatValueIsCorrect(Money actualPrice, Money expectedPrice) {
        PriceModel priceModel = new PriceModel(new PriceDTO(PriceCategory.CHARGE_PRICE, Currency.GBP, "1.0", actualPrice == null ? null : actualPrice.toBigDecimal(), null), PriceType.ONE_TIME, null);
        Money value = priceModel.getValue();
        assertThat(value, is(expectedPrice));
    }

    private void assertThatNetValueIsCorrect(BigDecimal price, BigDecimal discountPercentage, Money expected) {
        PriceModel priceModel = new PriceModel(new PriceDTO(PriceCategory.CHARGE_PRICE, Currency.GBP, "1.0", price, discountPercentage), PriceType.ONE_TIME, null);
        assertThat(priceModel.getNetValue(), is(expected));
    }
}
