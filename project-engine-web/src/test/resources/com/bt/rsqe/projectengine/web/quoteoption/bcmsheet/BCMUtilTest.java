package com.bt.rsqe.projectengine.web.quoteoption.bcmsheet;


import org.junit.Test;

import java.math.BigDecimal;

import static junit.framework.Assert.assertNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class BCMUtilTest  {

    @Test
    public void testChangeDiscountToDecimalAndRoundWithNull() throws Exception {
        String discount=null;
        assertNull(BCMUtil.changeDiscountToDecimalAndRound(discount));

    }

    @Test
    public void testChangeDiscountToDecimalAndRoundWithoutNull() throws Exception {
        String discount="12.35124";
        assertThat(BCMUtil.changeDiscountToDecimalAndRound(discount), is(new BigDecimal(discount).movePointLeft(2)));
    }

    @Test
    public void testGetPriceInStrWithNull() throws Exception {
        BigDecimal price =null;
        assertThat(BCMUtil.getPriceInStr(price), is(""));
    }

    @Test
    public void testGetPriceInStrWithoutNull() throws Exception {
        BigDecimal price =BigDecimal.ZERO;
        assertThat(BCMUtil.getPriceInStr(price),is("0.00"));

        price = new BigDecimal("44.5678");
        assertThat(BCMUtil.getPriceInStr(price),is("44.57"));
    }
}
