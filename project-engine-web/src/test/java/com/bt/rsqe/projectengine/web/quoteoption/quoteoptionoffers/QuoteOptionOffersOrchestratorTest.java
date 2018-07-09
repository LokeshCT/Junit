package com.bt.rsqe.projectengine.web.quoteoption.quoteoptionoffers;

import com.bt.rsqe.domain.project.PricingStatus;
import com.bt.rsqe.pmr.client.StencilResolverInputKey;
import com.bt.rsqe.projectengine.LineItemDiscountStatus;
import com.bt.rsqe.projectengine.ProjectResource;
import com.bt.rsqe.projectengine.QuoteOptionItemResource;
import com.bt.rsqe.projectengine.QuoteOptionResource;
import com.bt.rsqe.projectengine.web.OfferAndOrderValidationResult;
import com.bt.rsqe.projectengine.web.OfferAndOrderValidator;
import com.bt.rsqe.projectengine.web.facades.QuoteOptionOfferFacade;
import com.bt.rsqe.projectengine.web.model.LineItemModel;
import com.bt.rsqe.projectengine.web.model.OfferDetailsModel;
import com.bt.rsqe.projectengine.web.quoteoptionoffers.QuoteOptionOffersOrchestrator;
import com.bt.rsqe.projectengine.web.quoteoptionpricing.PriceSuppressStrategy;
import com.bt.rsqe.projectengine.web.view.QuoteOptionOffersView;
import com.bt.rsqe.utils.RSQEMockery;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.bt.rsqe.projectengine.QuoteOptionItemDTOFixture.aQuoteOptionItemDTO;
import static com.google.common.collect.Lists.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.core.Is.*;
import static org.junit.Assert.assertFalse;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Mockito.*;

@RunWith(JMock.class)
public class QuoteOptionOffersOrchestratorTest {

    private static final String CUSTOMER_ID = "CUSTOMER";
    private static final String CONTRACT_ID = "CONTRACT";
    private static final String PROJECT_ID = "PROJECT";
    private static final String QUOTE_OPTION_ID = "QUOTE_OPTION";
    private Mockery context = new RSQEMockery();
    private QuoteOptionOfferFacade offerFacade = context.mock(QuoteOptionOfferFacade.class);
    private QuoteOptionOffersOrchestrator orchestrator;
    private OfferAndOrderValidator validator = mock(OfferAndOrderValidator.class);

    @Before
    public void before() {
        orchestrator = new QuoteOptionOffersOrchestrator(offerFacade, validator);
    }

    @Test
    public void shouldPopulateView() throws Exception {
        context.checking(new Expectations() {{
            oneOf(offerFacade).get(PROJECT_ID, QUOTE_OPTION_ID);
            will(returnValue(newArrayList()));
        }});
        final QuoteOptionOffersView view = orchestrator.buildView(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID);
        assertThat(view.getProjectId(), is(PROJECT_ID));
        assertThat(view.getQuoteOptionId(), is(QUOTE_OPTION_ID));
    }

    @Test
    public void shouldPopulateViewWithSortedModels() throws Exception {
        final String first = "first";
        final String second = "second";
        context.checking(new Expectations() {{
            oneOf(offerFacade).get(PROJECT_ID, QUOTE_OPTION_ID);
            final OfferDetailsModel modelFirst = context.mock(OfferDetailsModel.class);
            final OfferDetailsModel modelSecond = context.mock(OfferDetailsModel.class);
            will(returnValue(newArrayList(modelSecond, modelFirst)));
            allowing(modelFirst).getCreatedDate();
            will(returnValue("2011-01-01T12:30:55.000+00:00"));
            allowing(modelFirst).getName();
            will(returnValue(first));
            allowing(modelSecond).getCreatedDate();
            will(returnValue("2010-01-01T12:30:55.000+00:00"));
            allowing(modelSecond).getName();
            will(returnValue(second));
        }});
        final QuoteOptionOffersView view = orchestrator.buildView(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID);
        assertThat(view.getOffers().get(0).getName(), is(first));
        assertThat(view.getOffers().get(1).getName(), is(second));
    }

    @Test
    public void shouldFailValidationForPricingStatus() {
        String aLineItemId = "aLineItemId";
        ArrayList<String> lineItems = newArrayList(aLineItemId);
        when(validator.anyLineItemsWithPricingStatus(eq("aProjectId"), eq("aQuoteOptionId"), eq(CUSTOMER_ID), eq(CONTRACT_ID), eq(lineItems), anyListOf(PricingStatus.class))).thenReturn(new OfferAndOrderValidationResult(false, "The Pricing status is invalid."));
        OfferAndOrderValidationResult validationResult = orchestrator.validateStatusForOfferCreation("aProjectId", "aQuoteOptionId", CUSTOMER_ID, CONTRACT_ID, lineItems);
        assertFalse(validationResult.isValid());
        assertThat(validationResult.getErrorMessage(), is("The Pricing status is invalid."));
    }

    @Test
    public void shouldFailValidationForDiscountStatusOnOfferApproval() {
        QuoteOptionOfferFacade offerFacade = mock(QuoteOptionOfferFacade.class);
        QuoteOptionOffersOrchestrator orchestrator = new QuoteOptionOffersOrchestrator(offerFacade, validator);
        OfferDetailsModel offerDetailsModel = mock(OfferDetailsModel.class);
        when(offerFacade.get(eq("aProjectId"), eq("aQuoteOptionId"))).thenReturn(Arrays.asList(offerDetailsModel));
        when(validator.anyLineItemsWithPricingStatus(eq("aProjectId"),eq("aQuoteOptionId"),eq(CUSTOMER_ID),eq(CONTRACT_ID),anyListOf(String.class),anyListOf(PricingStatus.class))).thenReturn(new OfferAndOrderValidationResult(true, "The Pricing status is invalid."));
        when(validator.anyLineItemHavingInvalidDiscountStatus(anyListOf(String.class), eq("aProjectId"), eq("aQuoteOptionId"))).thenReturn(new OfferAndOrderValidationResult(false, "The Discount status is invalid."));
        OfferAndOrderValidationResult validationResult = orchestrator.validateStatusForOfferApproval("aProjectId", "aQuoteOptionId", CUSTOMER_ID, CONTRACT_ID);
        assertFalse(validationResult.isValid());
        assertThat(validationResult.getErrorMessage(), is("The Discount status is invalid."));
    }

    @Test
    public void shouldFailValidationForPricingStatusOnOfferApproval() {
        QuoteOptionOfferFacade offerFacade = mock(QuoteOptionOfferFacade.class);
        QuoteOptionOffersOrchestrator orchestrator = new QuoteOptionOffersOrchestrator(offerFacade, validator);
        OfferDetailsModel offerDetailsModel = mock(OfferDetailsModel.class);
        when(offerFacade.get(eq("aProjectId"), eq("aQuoteOptionId"))).thenReturn(Arrays.asList(offerDetailsModel));
        when(validator.anyLineItemsWithPricingStatus(eq("aProjectId"),eq("aQuoteOptionId"),eq(CUSTOMER_ID),eq(CONTRACT_ID),anyListOf(String.class),anyListOf(PricingStatus.class))).thenReturn(new OfferAndOrderValidationResult(false, "The Pricing status is invalid."));
        when(validator.anyLineItemHavingInvalidDiscountStatus(anyListOf(String.class), eq("aProjectId"), eq("aQuoteOptionId"))).thenReturn(new OfferAndOrderValidationResult(true, "The Discount status is invalid."));
        OfferAndOrderValidationResult validationResult = orchestrator.validateStatusForOfferApproval("aProjectId", "aQuoteOptionId", CUSTOMER_ID, CONTRACT_ID);
        assertFalse(validationResult.isValid());
        assertThat(validationResult.getErrorMessage(), is("The Pricing status is invalid."));
    }

}
