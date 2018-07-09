package com.bt.rsqe.projectengine.web.model.lineitemvisitor;

import com.bt.rsqe.domain.product.fixtures.ProductChargingSchemeFixture;
import com.bt.rsqe.enums.PriceCategory;
import com.bt.rsqe.enums.PriceType;
import com.bt.rsqe.Money;
import com.bt.rsqe.Percentage;
import com.bt.rsqe.projectengine.web.model.LineItemModel;
import com.bt.rsqe.projectengine.web.model.PriceModel;
import com.bt.rsqe.projectengine.web.model.lineitemvisitor.totalpricesvisitors.OneTimePriceVisitor;
import com.bt.rsqe.projectengine.web.model.lineitemvisitor.totalpricesvisitors.PriceVisitor;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.core.Is.*;
import static org.mockito.Mockito.*;

public class OneTimePriceVisitorTest {

    private PriceModel satisfied = mock(PriceModel.class);
    private PriceModel dissatisfied = mock(PriceModel.class);
    private LineItemModel notSuperseded = mock(LineItemModel.class);
    private PriceVisitor visitor = new OneTimePriceVisitor(PriceCategory.CHARGE_PRICE);

    @Before
    public void before() {
        when(satisfied.isSatisfiedBy(PriceType.ONE_TIME, PriceCategory.CHARGE_PRICE)).thenReturn(true);
        when(satisfied.getValue()).thenReturn(Money.from("2"));
        when(satisfied.getNetValue()).thenReturn(Money.from("1"));
        when(satisfied.getScheme()).thenReturn(ProductChargingSchemeFixture.aChargingScheme().build());
        when(dissatisfied.isSatisfiedBy(PriceType.ONE_TIME, PriceCategory.CHARGE_PRICE)).thenReturn(false);
    }

    @Test
    public void shouldOnlyIncludeOneTime() throws Exception {
        visitor.visit(notSuperseded);
        visitor.visit(satisfied);
        visitor.visit(dissatisfied);
        assertThat(visitor.getGross(), is(Money.from("2")));
        assertThat(visitor.getNet(), is(Money.from("1")));
        assertThat(visitor.getDiscount(), is(Percentage.from("50")));
    }

    @Test
    public void shouldSumMultiplePrices() throws Exception {
        visitor.visit(notSuperseded);
        visitor.visit(satisfied);
        visitor.visit(satisfied);
        assertThat(visitor.getGross(), is(Money.from("4")));
        assertThat(visitor.getNet(), is(Money.from("2")));
        assertThat(visitor.getDiscount(), is(Percentage.from("50")));
    }


}
