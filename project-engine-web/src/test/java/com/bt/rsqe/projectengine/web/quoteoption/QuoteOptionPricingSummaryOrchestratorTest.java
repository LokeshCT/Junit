package com.bt.rsqe.projectengine.web.quoteoption;

import com.bt.rsqe.enums.PriceCategory;
import com.bt.rsqe.enums.PriceType;
import com.bt.rsqe.Money;
import com.bt.rsqe.Percentage;
import com.bt.rsqe.projectengine.web.facades.LineItemFacade;
import com.bt.rsqe.projectengine.web.model.LineItemModel;
import com.bt.rsqe.projectengine.web.model.lineitemvisitor.CompositeLineItemVisitor;
import com.bt.rsqe.projectengine.web.model.lineitemvisitor.LineItemVisitorFactory;
import com.bt.rsqe.projectengine.web.model.lineitemvisitor.totalpricesvisitors.ChargePricesUsagePricesVisitor;
import com.bt.rsqe.projectengine.web.model.lineitemvisitor.totalpricesvisitors.PriceVisitor;
import com.bt.rsqe.projectengine.web.quoteoptionpricing.PriceSuppressStrategy;
import com.bt.rsqe.utils.RSQEMockery;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.Before;
import org.junit.Test;

import static com.google.common.collect.Lists.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.core.Is.*;

public class QuoteOptionPricingSummaryOrchestratorTest {
    private Mockery context;
    private QuoteOptionPricingSummaryOrchestrator quoteOptionPricingSummaryOrchestrator;
    private LineItemFacade lineItemFacade;
    private static final String PROJECT_ID = "123";
    private static final String QUOTE_OPTION_ID = "quoteOptionId";
    private static final String CUSTOMER_ID = "elvis";
    private static final String CONTRACT_ID = "contactId";
    private LineItemVisitorFactory lineItemVisitorFactory;
    private PriceVisitor oneTimeChargePriceVisitor;
    private PriceVisitor recurringChargePriceVisitor;
    private ChargePricesUsagePricesVisitor chargePriceUsagePricesVisitor;
    private LineItemModel lineItem;
    private CompositeLineItemVisitor compositeLineItemVisitor;

    @Before
    public void before() {
        context = new RSQEMockery();
        lineItemFacade = context.mock(LineItemFacade.class);
        lineItem = context.mock(LineItemModel.class);
        lineItemVisitorFactory = context.mock(LineItemVisitorFactory.class);
        oneTimeChargePriceVisitor = context.mock(PriceVisitor.class);
        recurringChargePriceVisitor = context.mock(PriceVisitor.class);
        chargePriceUsagePricesVisitor = context.mock(ChargePricesUsagePricesVisitor.class);
        compositeLineItemVisitor = context.mock(CompositeLineItemVisitor.class);
        quoteOptionPricingSummaryOrchestrator = new QuoteOptionPricingSummaryOrchestrator(lineItemFacade, lineItemVisitorFactory);
    }

    @Test
    public void shouldGetPricingSummary() throws Exception {
        context.checking(new Expectations() {{
            oneOf(lineItemFacade).fetchLineItems(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID, PriceSuppressStrategy.UI_PRICES);
            will(returnValue(newArrayList(lineItem)));

            oneOf(lineItemVisitorFactory).createPriceVisitor(PriceType.ONE_TIME, PriceCategory.CHARGE_PRICE);
            will(returnValue(oneTimeChargePriceVisitor));

            oneOf(lineItemVisitorFactory).createPriceVisitor(PriceType.RECURRING, PriceCategory.CHARGE_PRICE);
            will(returnValue(recurringChargePriceVisitor));

            oneOf(lineItemVisitorFactory).createUsageVisitor(PriceCategory.CHARGE_PRICE);
            will(returnValue(chargePriceUsagePricesVisitor));

            oneOf(lineItemVisitorFactory).createPricingSummaryVisitor(oneTimeChargePriceVisitor, recurringChargePriceVisitor, chargePriceUsagePricesVisitor);
            will(returnValue(compositeLineItemVisitor));


            oneOf(lineItem).accept(compositeLineItemVisitor);

            oneOf(oneTimeChargePriceVisitor).getGross();
            will(returnValue(mockMoney()));

            oneOf(oneTimeChargePriceVisitor).getNet();
            will(returnValue(mockMoney()));

            oneOf(oneTimeChargePriceVisitor).getDiscount();
            will(returnValue(mockPercentage()));

            oneOf(recurringChargePriceVisitor).getGross();
            will(returnValue(mockMoney()));

            oneOf(recurringChargePriceVisitor).getNet();
            will(returnValue(mockMoney()));

            oneOf(recurringChargePriceVisitor).getDiscount();
            will(returnValue(mockPercentage()));

            oneOf(chargePriceUsagePricesVisitor).getTotalOffNetUsageCharge();
            will(returnValue(mockMoney()));

            oneOf(chargePriceUsagePricesVisitor).getTotalOnNetUsageCharge();
            will(returnValue(mockMoney()));

            oneOf(chargePriceUsagePricesVisitor).getTotalUsageCharge();
            will(returnValue(mockMoney()));
        }});

        quoteOptionPricingSummaryOrchestrator.getPricingSummary(PROJECT_ID, QUOTE_OPTION_ID, CUSTOMER_ID, CONTRACT_ID, PriceSuppressStrategy.UI_PRICES);
        context.assertIsSatisfied();
    }

    private Percentage mockPercentage() {
        return Percentage.from("1");
    }

    private Money mockMoney() {
        return Money.from("1");
    }

    @Test
    public void shouldCalculatePricingSummary() throws Exception {
        context.checking(new Expectations() {{
            allowing(lineItemFacade).fetchLineItems(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID, PriceSuppressStrategy.UI_PRICES);
            will(returnValue(newArrayList(lineItem)));

            allowing(lineItemVisitorFactory).createPriceVisitor(PriceType.ONE_TIME, PriceCategory.CHARGE_PRICE);
            will(returnValue(oneTimeChargePriceVisitor));

            allowing(lineItemVisitorFactory).createPriceVisitor(PriceType.RECURRING, PriceCategory.CHARGE_PRICE);
            will(returnValue(recurringChargePriceVisitor));

            allowing(lineItemVisitorFactory).createUsageVisitor(PriceCategory.CHARGE_PRICE);
            will(returnValue(chargePriceUsagePricesVisitor));

            allowing(lineItemVisitorFactory).createPricingSummaryVisitor(oneTimeChargePriceVisitor, recurringChargePriceVisitor, chargePriceUsagePricesVisitor);
            will(returnValue(compositeLineItemVisitor));


            ignoring(lineItem);

            allowing(oneTimeChargePriceVisitor).getGross();
            will(returnValue(Money.from("20")));

            allowing(oneTimeChargePriceVisitor).getNet();
            will(returnValue(Money.from("18")));

            allowing(oneTimeChargePriceVisitor).getDiscount();
            will(returnValue(Percentage.from("10")));

            allowing(recurringChargePriceVisitor).getGross();
            will(returnValue(Money.from("120")));

            allowing(recurringChargePriceVisitor).getNet();
            will(returnValue(Money.from("108")));

            allowing(recurringChargePriceVisitor).getDiscount();
            will(returnValue(Percentage.from("10")));

            allowing(chargePriceUsagePricesVisitor).getTotalOffNetUsageCharge();
            will(returnValue(Money.from("55")));

            allowing(chargePriceUsagePricesVisitor).getTotalOnNetUsageCharge();
            will(returnValue(Money.from("45")));

            allowing(chargePriceUsagePricesVisitor).getTotalUsageCharge();
            will(returnValue(Money.from("102")));
        }});

        final QuoteOptionPricingSummaryDTO pricingSummaryDTO = quoteOptionPricingSummaryOrchestrator.getPricingSummary(PROJECT_ID, QUOTE_OPTION_ID, CUSTOMER_ID, CONTRACT_ID, PriceSuppressStrategy.UI_PRICES);
        assertThat(pricingSummaryDTO.totalOneTimeGross, is(Money.from("20").toBigDecimal()));
        assertThat(pricingSummaryDTO.totalOneTimeNet, is(Money.from("18").toBigDecimal()));
        assertThat(pricingSummaryDTO.totalOneTimeDiscount, is(Percentage.from("10").toBigDecimal()));
        assertThat(pricingSummaryDTO.totalRecurringGross, is(Money.from("120").toBigDecimal()));
        assertThat(pricingSummaryDTO.totalRecurringNet, is(Money.from("108").toBigDecimal()));
        assertThat(pricingSummaryDTO.totalRecurringDiscount, is(Percentage.from("10").toBigDecimal()));
        assertThat(pricingSummaryDTO.totalOffNetUsage, is(Money.from("55").toBigDecimal()));
        assertThat(pricingSummaryDTO.totalOnNetUsage, is(Money.from("45").toBigDecimal()));
        assertThat(pricingSummaryDTO.totalUsage, is(Money.from("102").toBigDecimal()));
    }
}
