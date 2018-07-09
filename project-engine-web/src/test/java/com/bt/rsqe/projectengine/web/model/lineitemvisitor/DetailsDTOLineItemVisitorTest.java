package com.bt.rsqe.projectengine.web.model.lineitemvisitor;

import com.bt.rsqe.customerrecord.SiteDTO;
import com.bt.rsqe.domain.project.PricingStatus;
import com.bt.rsqe.expedio.fixtures.ProjectDTOFixture;
import com.bt.rsqe.expedio.project.ProjectDTO;
import com.bt.rsqe.projectengine.web.model.LineItemModel;
import com.bt.rsqe.projectengine.web.view.QuoteOptionDetailsDTO;
import com.bt.rsqe.utils.RSQEMockery;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

import static com.bt.rsqe.expedio.fixtures.SiteDTOFixture.*;
import static com.google.common.collect.Lists.*;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class DetailsDTOLineItemVisitorTest {

    private Mockery context = new RSQEMockery();
    private ArrayList<QuoteOptionDetailsDTO.LineItem> lineItemList;
    //private DetailsDTOLineItemVisitor visitor;
    private LineItemVisitor visitor;

    @Before
    public void before() {
        lineItemList = newArrayList();
        visitor = new DetailsDTOLineItemVisitor(lineItemList);
    }

    @Test
    public void shouldBuildResponseWithOrderStatuses() {

        visitor.visit(lineItemModel("LineItemOrderStatus.ON_HOLD"));
        visitor.visit(lineItemModel("LineItemOrderStatus.ORDERED"));
        assertThat(lineItemList, hasItems(withOrderStatus("LineItemOrderStatus.ON_HOLD"), withOrderStatus("LineItemOrderStatus.ORDERED")));
    }

    @Test
    public void shouldAddLineItemWithSiteInformationGivenLineItemHasSite() throws Exception {
        final LineItemModel lineItemModel = context.mock(LineItemModel.class);
        context.checking(new Expectations() {{
            oneOf(lineItemModel).hasSite();
            will(returnValue(true));
            allowing(lineItemModel).getSite();
            will(returnValue(aSiteDTO().withBuilding("building").withCity("city").withCountry("country").withName("site 1").build()));
            oneOf(lineItemModel).getPricingStatusOfTree();
            will(returnValue(PricingStatus.FIRM));
            allowing(lineItemModel).isInitialised();
            will(returnValue(true));
            ignoring(lineItemModel); // ignore the rest because we don't care
        }});
        visitor.visit(lineItemModel);
        assertThat(lineItemList.get(0).getSiteName(), is("site 1"));
    }

    @Test
    public void shouldAddLineItemWithSiteInformationGivenLineItemHasNoSite() throws Exception {
        final LineItemModel lineItemModel = context.mock(LineItemModel.class);
        context.checking(new Expectations() {{
            allowing(lineItemModel).getSite();
            will(returnValue(SiteDTO.CUSTOMER_OWNED));
            allowing(lineItemModel).getPricingStatusOfTree();
            will(returnValue(PricingStatus.NOT_PRICED));
            allowing(lineItemModel).isInitialised();
            will(returnValue(true));
            ignoring(lineItemModel); // ignore the rest because we don't care
        }});

        visitor.visit(lineItemModel);
        assertThat(lineItemList.get(0).getSiteName(), is(SiteDTO.CUSTOMER_OWNED.name));
    }

    @Test
    public void shouldAddLineItemWithLineItemDetails() throws Exception {
        final LineItemModel lineItemModel = lineItemModel("LineItemOrderStatus.ORDERED");
        visitor.visit(lineItemModel);

        QuoteOptionDetailsDTO.LineItem lineItem = lineItemList.get(0);
        assertThat(lineItem.getProductSCode(), is("S123"));
        assertThat(lineItem.getName(), is("product"));
        assertThat(lineItem.getId(), is("id"));
        assertThat(lineItem.getContractTerm(), is("contractTerm"));
        assertThat(lineItem.getAction(), is("Modify"));
        assertThat(lineItem.getStatus(), is("status"));
        assertThat(lineItem.getDiscountStatus(), is("discountStatus"));
        assertThat(lineItem.getConfigureUrl(), is("configureUrl"));
        assertThat(lineItem.getOfferDetailsUrl(), is("offerDetailsLink"));
        assertThat(lineItem.getErrorMessage(), is("error message"));
        assertThat(lineItem.getValidity(), is("Valid"));
        assertThat(lineItem.getIfcAction(), is("Amend"));
        assertThat(lineItem.getSummary(), is("summary"));
        assertTrue(lineItem.isConfigurable());
        assertThat(lineItem.getPricingStatus(), is(PricingStatus.BUDGETARY.getDescription()));
        assertThat(lineItem.getIsImportable(), is(false));
    }

    @Test
    public void shouldGiveAPricingStatusOfPartiallyPricedWhenPricingStatusIsNotPricedButSomePriceLinesAreFirm() throws Exception {
        final LineItemModel lineItemModel = lineItemModel("LineItemOrderStatus.ORDERED", PricingStatus.NOT_PRICED, true);
        visitor.visit(lineItemModel);

        QuoteOptionDetailsDTO.LineItem lineItem = lineItemList.get(0);
        assertThat(lineItem.getPricingStatus(), is("Partially Priced"));
    }

    private LineItemModel lineItemModel(final String lineItemOrderStatus) {
        return lineItemModel(lineItemOrderStatus, PricingStatus.BUDGETARY, false);
    }

    private LineItemModel lineItemModel(final String lineItemOrderStatus, final PricingStatus pricingStatus, final boolean haveFirmPriceLines) {
        final LineItemModel lineItemModel = context.mock(LineItemModel.class);
        final ProjectDTO projectDTO = ProjectDTOFixture.aProjectDTO().build();
        context.checking(new Expectations() {{
            oneOf(lineItemModel).getProjectDTO();
            will(returnValue(projectDTO));
            oneOf(lineItemModel).getProductName();
            will(returnValue("product"));
            oneOf(lineItemModel).getDisplayName();
            will(returnValue("product"));
            oneOf(lineItemModel).getProductSCode();
            will(returnValue("S123"));
            oneOf(lineItemModel).getId();
            will(returnValue("id"));
            oneOf(lineItemModel).getContractTerm();
            will(returnValue("contractTerm"));
            oneOf(lineItemModel).getAction();
            will(returnValue("Modify"));
            oneOf(lineItemModel).getStatus();
            will(returnValue("status"));
            oneOf(lineItemModel).getDiscountStatus();
            will(returnValue("discountStatus"));
            oneOf(lineItemModel).getPricingStatusOfTree();
            will(returnValue(pricingStatus));
            oneOf(lineItemModel).getConfigureUrl(projectDTO);
            will(returnValue("configureUrl"));
            oneOf(lineItemModel).getOfferDetailsUrl();
            will(returnValue("offerDetailsLink"));
            oneOf(lineItemModel).getErrorMessage();
            will(returnValue("error message"));
            oneOf(lineItemModel).getValidity();
            will(returnValue("Valid"));
            oneOf(lineItemModel).getOrderStatus();
            will(returnValue(lineItemOrderStatus));
            allowing(lineItemModel).getIfcAction();
            will(returnValue("Amend"));
            allowing(lineItemModel).isInitialised();
            will(returnValue(true));
            allowing(lineItemModel).getSummary();
            will(returnValue("summary"));
            allowing(lineItemModel).getIsImportable();
            will(returnValue(false));
            if(haveFirmPriceLines) {
                allowing(lineItemModel).anyAssetsAreFirm();
                will(returnValue(true));
            }
            ignoring(lineItemModel); // ignore the rest because we don't care
        }});
        return lineItemModel;
    }

    private Matcher<QuoteOptionDetailsDTO.LineItem> withOrderStatus(final String orderStatus) {

        return new TypeSafeMatcher<QuoteOptionDetailsDTO.LineItem>() {

            @Override
            public boolean matchesSafely(QuoteOptionDetailsDTO.LineItem lineItem) {
                return lineItem.getOrderStatus().equals(orderStatus);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("item with order status: ");
                description.appendText(orderStatus);
            }
        };
    }

}
