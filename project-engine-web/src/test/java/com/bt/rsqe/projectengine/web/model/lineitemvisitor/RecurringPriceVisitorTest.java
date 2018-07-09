package com.bt.rsqe.projectengine.web.model.lineitemvisitor;

import com.bt.rsqe.domain.product.fixtures.ProductChargingSchemeFixture;
import com.bt.rsqe.enums.PriceCategory;
import com.bt.rsqe.enums.PriceType;
import com.bt.rsqe.Money;
import com.bt.rsqe.Percentage;
import com.bt.rsqe.projectengine.web.model.LineItemModel;
import com.bt.rsqe.projectengine.web.model.PriceModel;
import com.bt.rsqe.projectengine.web.model.lineitemvisitor.totalpricesvisitors.PriceVisitor;
import com.bt.rsqe.projectengine.web.model.lineitemvisitor.totalpricesvisitors.RecurringPriceVisitor;
import com.bt.rsqe.utils.RSQEMockery;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.core.Is.*;

public class RecurringPriceVisitorTest {

    private final Mockery context = new RSQEMockery();
    private PriceModel satisfied = context.mock(PriceModel.class);
    private PriceModel dissatisfied = context.mock(PriceModel.class);
    private LineItemModel tenMonth = context.mock(LineItemModel.class);
    private LineItemModel hundredMonth = context.mock(LineItemModel.class);
    private LineItemModel unparseableContractTerm = context.mock(LineItemModel.class);
    private PriceVisitor visitor = new RecurringPriceVisitor(PriceCategory.CHARGE_PRICE);

    @Before
    public void before() {
        context.checking(new Expectations() {{
            allowing(dissatisfied).isSatisfiedBy(PriceType.RECURRING, PriceCategory.CHARGE_PRICE);
            will(returnValue(false));
            allowing(satisfied).isSatisfiedBy(PriceType.RECURRING, PriceCategory.CHARGE_PRICE);
            will(returnValue(true));
            allowing(satisfied).getScheme();
            will(returnValue(ProductChargingSchemeFixture.aChargingScheme().build()));
            allowing(satisfied).getValue();
            will(returnValue(Money.from("2")));
            allowing(satisfied).getNetValue();
            will(returnValue(Money.from("1")));
            allowing(tenMonth).getContractTerm();
            will(returnValue("10"));
            allowing(hundredMonth).getContractTerm();
            will(returnValue("100"));
            allowing(unparseableContractTerm).getContractTerm();
            will(returnValue("fred"));
        }});
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowIfNoContractTermPresent() throws Exception {
        visitor.visit(satisfied);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowIfUnparseableContractTerm() throws Exception {
        visitor.visit(unparseableContractTerm);
    }

    @Test
    public void shouldOnlyIncludeRecurring() throws Exception {
        visitor.visit(tenMonth);
        visitor.visit(satisfied);
        visitor.visit(dissatisfied);
        assertThat(visitor.getGross(), is(Money.from("20")));
        assertThat(visitor.getNet(), is(Money.from("10")));
        assertThat(visitor.getDiscount(), is(Percentage.from("50")));
    }

    @Test
    public void shouldSumDifferentContractTerms() throws Exception {
        visitor.visit(tenMonth);
        visitor.visit(satisfied);
        visitor.visit(hundredMonth);
        visitor.visit(satisfied);
        assertThat(visitor.getGross(), is(Money.from("220")));
        assertThat(visitor.getNet(), is(Money.from("110")));
        assertThat(visitor.getDiscount(), is(Percentage.from("50")));
    }

}
