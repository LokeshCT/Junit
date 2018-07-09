package com.bt.rsqe.projectengine.web.model;

import com.bt.rsqe.customerinventory.parameter.PriceLineStatus;
import com.bt.rsqe.util.Assertions;
import com.google.common.base.Optional;
import org.junit.Test;

import java.math.BigDecimal;

public class DiscountDeltaTest {
    @Test
    public void shouldHaveAWorkingEqualsAndHashCode() throws Exception {
        DiscountDelta d1 = new DiscountDelta(new BigDecimal("1"));
        DiscountDelta d2 = new DiscountDelta(Optional.of(new BigDecimal("1")), Optional.<String>absent(), Optional.<PriceLineStatus>absent());
        DiscountDelta d3 = new DiscountDelta(Optional.of(new BigDecimal("2")), Optional.of("aVendorDiscountRef"), Optional.of(PriceLineStatus.FIRM));
        Assertions.assertThatEqualsAndHashcodeWork(d1, d2, d3);
    }
}
