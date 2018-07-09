package com.bt.rsqe.projectengine.web.model.lineitemvisitor;

import com.bt.rsqe.domain.project.PricingStatus;
import com.bt.rsqe.projectengine.LineItemDiscountStatus;
import com.bt.rsqe.projectengine.web.model.FutureAssetPricesModel;
import com.bt.rsqe.projectengine.web.model.LineItemModel;
import com.bt.rsqe.projectengine.web.model.PriceLineModel;
import com.bt.rsqe.projectengine.web.view.QuoteOptionPricingDTO;
import com.bt.rsqe.security.UserContext;
import com.bt.rsqe.security.UserContextBuilder;
import com.bt.rsqe.security.UserContextManager;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

import static com.bt.rsqe.enums.PriceType.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.core.Is.*;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

public class PricingDTOItemRowVisitorTest {

    private static final String SITE_NAME = "siteName";
    private final static String PRODUCT_NAME = "productName";
    private static final String DESCRIPTION = "DESCRIPTION";
    private static final String STATUS = "STATUS";
    private static final String LINE_ITEM_ID = "ID";
    private QuoteOptionPricingDTO.PriceLineDTO oneTime;
    private QuoteOptionPricingDTO.PriceLineDTO recurring;
    private ArrayList<QuoteOptionPricingDTO.ItemRowDTO> list;
    private LineItemVisitor visitor;
    private LineItemModel lineItem;
    private PriceLineModel priceLine;
    private FutureAssetPricesModel futureAssetPricesModel;

    @Before
    public void setUp() throws Exception {
        oneTime = new QuoteOptionPricingDTO.PriceLineDTO();
        recurring = new QuoteOptionPricingDTO.PriceLineDTO();
        list = new ArrayList<QuoteOptionPricingDTO.ItemRowDTO>();
        visitor = new PricingDTOItemRowVisitor(list);
        lineItem = mock(LineItemModel.class);
        priceLine = mock(PriceLineModel.class);
        futureAssetPricesModel = mock(FutureAssetPricesModel.class);
    }

    @Test
    public void shouldGetDetailsFromPriceLineModel() throws Exception {
        when(priceLine.getDescription()).thenReturn(DESCRIPTION);
        when(priceLine.getStatus()).thenReturn(STATUS);
        when(priceLine.getOneTimeDto()).thenReturn(oneTime);
        when(priceLine.getRecurringDto()).thenReturn(recurring);

        visitor.visit(lineItem);
        visitor.visit(futureAssetPricesModel, 0);
        visitor.visit(priceLine);

        final QuoteOptionPricingDTO.ItemRowDTO itemRowDTO = list.get(0);
        assertThat(itemRowDTO.description, is(DESCRIPTION));
        assertThat(itemRowDTO.status, is(STATUS));
        assertThat(itemRowDTO.aggregateRow, is(false));
        assertThat(itemRowDTO.oneTime, is(oneTime));
        assertThat(itemRowDTO.recurring, is(recurring));
    }

    @Test
    public void shouldGetDetailsFromPriceLineModelWithUserEnteredIfPriceLineIsUserEntered() throws Exception {
        when(priceLine.getDescription()).thenReturn(DESCRIPTION);
        when(priceLine.getStatus()).thenReturn(STATUS);
        when(priceLine.getOneTimeDto()).thenReturn(oneTime);
        when(priceLine.getRecurringDto()).thenReturn(recurring);
        when(priceLine.getUserEntered()).thenReturn("Y");
        when(priceLine.isManualPricing()).thenReturn(true);



        visitor.visit(lineItem);
        visitor.visit(futureAssetPricesModel, 0);
        visitor.visit(priceLine);

        final QuoteOptionPricingDTO.ItemRowDTO itemRowDTO = list.get(0);
        assertThat(itemRowDTO.description, is(DESCRIPTION));
        assertThat(itemRowDTO.aggregateRow, is(false));
        assertThat(itemRowDTO.oneTime, is(oneTime));
        assertThat(itemRowDTO.recurring, is(recurring));
        assertThat(itemRowDTO.status, is("STATUS (User Entered)"));
        assertTrue(itemRowDTO.isManualPricing);
    }

    @Test
    public void shouldGetDetailsFromLineItem() throws Exception {
        when(lineItem.isReadOnly()).thenReturn(true);
        when(lineItem.getId()).thenReturn(LINE_ITEM_ID);

        visitor.visit(lineItem);
        visitor.visit(futureAssetPricesModel, 0);
        visitor.visit(priceLine);

        final QuoteOptionPricingDTO.ItemRowDTO itemRowDTO = list.get(0);
        assertThat(itemRowDTO.readOnly, is(true));
        assertThat(itemRowDTO.lineItemId, is(LINE_ITEM_ID));
    }

    @Test
    public void shouldGetDetailsFromFutureAssetPricesModel() throws Exception {
        when(futureAssetPricesModel.getDisplayName()).thenReturn(PRODUCT_NAME);
        when(futureAssetPricesModel.getSiteName()).thenReturn(SITE_NAME);

        visitor.visit(lineItem);
        visitor.visit(futureAssetPricesModel, 1);
        visitor.visit(priceLine);

        final QuoteOptionPricingDTO.ItemRowDTO itemRowDTO = list.get(0);
        assertThat(itemRowDTO.product, is(PRODUCT_NAME));
        assertThat(itemRowDTO.site, is(SITE_NAME));
        assertThat(itemRowDTO.groupingLevel, is(1));
    }

    @Test
    public void shouldGetDetailsFromLineItemModel() throws Exception {
        when(lineItem.getSummary()).thenReturn("summary");

        visitor.visit(lineItem);
        visitor.visit(futureAssetPricesModel, 1);
        visitor.visit(priceLine);

        final QuoteOptionPricingDTO.ItemRowDTO itemRowDTO = list.get(0);
        assertThat(itemRowDTO.summary, is("summary"));
    }


    @Test
    public void shouldAddAggregateRowIfThereAreNoPriceLinesAndGroupingLevelIsNot0() throws Exception {
        when(futureAssetPricesModel.hasNoPriceLines()).thenReturn(true);


        visitor.visit(lineItem);
        visitor.visit(futureAssetPricesModel, 1);

        assertThat(list.isEmpty(), is(true));
    }

    @Test
    public void shouldNotAddAggregateRowIfThereArePriceLinesAndGroupingLevelIsNot0() throws Exception {
        when(futureAssetPricesModel.hasNoPriceLines()).thenReturn(false);

        visitor.visit(lineItem);
        visitor.visit(futureAssetPricesModel, 1);

        assertThat(list.isEmpty(), is(true));
    }

    @Test
    public void shouldAddAggregateRowIfThereAreNoPriceLinesAndGroupingLevelIs0() throws Exception {
        when(lineItem.isReadOnly()).thenReturn(true);
        when(futureAssetPricesModel.hasNoPriceLines()).thenReturn(true);
        when(lineItem.isForIfc()).thenReturn(true);
        when(lineItem.isPricingStatusOfTreeApplicableForOnPricingTab()).thenReturn(true);

        visitor.visit(lineItem);
        visitor.visit(futureAssetPricesModel, 0);

        final QuoteOptionPricingDTO.ItemRowDTO itemRowDTO = list.get(0);
        assertThat(itemRowDTO.aggregateRow, is(true));
        assertThat(itemRowDTO.forIfc, is(true));
        assertThat(itemRowDTO.readOnly, is(true));
    }

    @Test
    public void shouldSkipUsageBasePriceLine() throws Exception {
        when(priceLine.getPriceType()).thenReturn(USAGE_BASED);
        when(futureAssetPricesModel.hasNoPriceLines()).thenReturn(true);

        visitor.visit(lineItem);
        visitor.visit(futureAssetPricesModel, 1);
        visitor.visit(priceLine);

        assertThat(list.isEmpty(), is(true));
    }

    @Test
    public void shouldReturnReadOnlyWhenLineItemOrPriceLineIsReadOnly() {
        when(lineItem.isReadOnly()).thenReturn(false);
        when(priceLine.isCustomerAggregatedPrice()).thenReturn(true);
        when(futureAssetPricesModel.hasNoPriceLines()).thenReturn(false);

        visitor.visit(lineItem);
        visitor.visit(futureAssetPricesModel, 0);
        visitor.visit(priceLine);

        final QuoteOptionPricingDTO.ItemRowDTO itemRowDTO = list.get(0);
        assertThat(itemRowDTO.readOnly, is(true));
    }


    @Test
    public void shouldNotReturnReadOnlyWhenLineItemDiscountStatusIsApprovalRequestedForBidManager() {
        final UserContext userContext = UserContextBuilder.aDirectUserContext().withDiscountAccess().build();
        UserContextManager.setCurrent(userContext);

        when(lineItem.isReadOnly()).thenReturn(true);
        when(lineItem.getDiscountStatus()).thenReturn(LineItemDiscountStatus.APPROVAL_REQUESTED.getDescription());
        when(priceLine.isCustomerAggregatedPrice()).thenReturn(false);
        when(futureAssetPricesModel.hasNoPriceLines()).thenReturn(false);

        visitor.visit(lineItem);
        visitor.visit(futureAssetPricesModel, 0);
        visitor.visit(priceLine);

        final QuoteOptionPricingDTO.ItemRowDTO itemRowDTO = list.get(0);
        assertThat(itemRowDTO.readOnly, is(false));
    }

    @Test
    public void shouldReturnReadOnlyWhenLineItemDiscountStatusIsApprovalRequestedForSalesUser() {
        final UserContext userContext = UserContextBuilder.aDirectUserContext().build();
        UserContextManager.setCurrent(userContext);

        when(lineItem.isReadOnly()).thenReturn(true);
        when(lineItem.getDiscountStatus()).thenReturn(LineItemDiscountStatus.APPROVAL_REQUESTED.getDescription());
        when(priceLine.isCustomerAggregatedPrice()).thenReturn(false);
        when(futureAssetPricesModel.hasNoPriceLines()).thenReturn(false);

        visitor.visit(lineItem);
        visitor.visit(futureAssetPricesModel, 0);
        visitor.visit(priceLine);

        final QuoteOptionPricingDTO.ItemRowDTO itemRowDTO = list.get(0);
        assertThat(itemRowDTO.readOnly, is(true));
    }

    @Test
    public void shouldReturnReadOnlyWhenPriceLineIsCustomerAggregatedPrice() {
        final UserContext userContext = UserContextBuilder.aDirectUserContext().withDiscountAccess().build();
        UserContextManager.setCurrent(userContext);

        when(lineItem.isReadOnly()).thenReturn(false);
        when(lineItem.getDiscountStatus()).thenReturn(LineItemDiscountStatus.APPROVAL_REQUESTED.getDescription());
        when(priceLine.isCustomerAggregatedPrice()).thenReturn(true);
        when(futureAssetPricesModel.hasNoPriceLines()).thenReturn(false);

        visitor.visit(lineItem);
        visitor.visit(futureAssetPricesModel, 0);
        visitor.visit(priceLine);

        final QuoteOptionPricingDTO.ItemRowDTO itemRowDTO = list.get(0);
        assertThat(itemRowDTO.readOnly, is(true));
    }
    @Test
    public void shouldNotAddRowIfForNotApplicablePriceLineItems() {
        final UserContext userContext = UserContextBuilder.aDirectUserContext().withDiscountAccess().build();
        UserContextManager.setCurrent(userContext);
        when(lineItem.isReadOnly()).thenReturn(false);
        when(lineItem.getPricingStatusOfTree()).thenReturn(PricingStatus.NOT_APPLICABLE);
        when(priceLine.isCustomerAggregatedPrice()).thenReturn(true);
        when(futureAssetPricesModel.hasNoPriceLines()).thenReturn(true);

        visitor.visit(lineItem);
        visitor.visit(futureAssetPricesModel, 0);
        assertThat(list.size(), is(0));
    }

}
