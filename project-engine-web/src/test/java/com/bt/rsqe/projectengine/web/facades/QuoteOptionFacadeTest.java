package com.bt.rsqe.projectengine.web.facades;

import com.bt.rsqe.projectengine.ProjectResource;
import com.bt.rsqe.projectengine.QuoteOptionItemResource;
import com.bt.rsqe.projectengine.QuoteOptionResource;
import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Test;

public class QuoteOptionFacadeTest {

    private static final String QUOTE_OPTION_ID = "quoteOptionId";
    private static final String PROJECT_ID = "projectId";

    private JUnit4Mockery context = new JUnit4Mockery() {{
        setImposteriser(ClassImposteriser.INSTANCE);
    }};
    private final ProjectResource projectResource = context.mock(ProjectResource.class);
    private final QuoteOptionResource quoteOptionResource = context.mock(QuoteOptionResource.class);
    private final QuoteOptionItemResource quoteOptionItemResource = context.mock(QuoteOptionItemResource.class);
    private final QuoteOptionFacade quoteOptionFacade = new QuoteOptionFacade(projectResource);

    @Test
    public void shouldGetQuoteOptionDTOsForAGivenProjectId() throws Exception {
        context.checking(new Expectations() {{
            oneOf(projectResource).quoteOptionResource(PROJECT_ID);
            will(returnValue(quoteOptionResource));
            oneOf(quoteOptionResource).get();
        }});

        quoteOptionFacade.getAll(PROJECT_ID);
        context.assertIsSatisfied();
    }

    @Test
    public void shouldGetAQuoteOptionForAGivenProjectIdAndQuoteOptionId() throws Exception {
        context.checking(new Expectations() {{
            oneOf(projectResource).quoteOptionResource(PROJECT_ID);
            will(returnValue(quoteOptionResource));
            oneOf(quoteOptionResource).get(QUOTE_OPTION_ID);
        }});
        quoteOptionFacade.get(PROJECT_ID, QUOTE_OPTION_ID);
        context.assertIsSatisfied();
    }

    @Test
    public void shouldGetAQuoteOptionItemsForAGivenProjectIdAndQuoteOptionId() throws Exception {
        context.checking(new Expectations() {{
            oneOf(projectResource).quoteOptionResource(PROJECT_ID);
            will(returnValue(quoteOptionResource));
            oneOf(quoteOptionResource).quoteOptionItemResource(QUOTE_OPTION_ID);
            will(returnValue(quoteOptionItemResource));
            oneOf(quoteOptionItemResource).get();
        }});
        quoteOptionFacade.getAllQuoteOptionItem(PROJECT_ID, QUOTE_OPTION_ID);
        context.assertIsSatisfied();
    }

    @Test
    public void shouldUnlockPriceLinesForAGivenQuoteOption() throws Exception {
        context.checking(new Expectations() {{
            oneOf(projectResource).quoteOptionResource(PROJECT_ID);
            will(returnValue(quoteOptionResource));
            oneOf(quoteOptionResource).quoteOptionItemResource(QUOTE_OPTION_ID).unlockApprovedPriceLines(QUOTE_OPTION_ID);
        }});

        quoteOptionFacade.unlockApprovedPriceLines(PROJECT_ID, QUOTE_OPTION_ID);
        context.assertIsSatisfied();
    }

    @Test
    public void shouldPutDiscountRequestForGivenProjectIdAndQuoteOptionId() {
        context.checking(new Expectations() {{
            oneOf(projectResource).quoteOptionResource(PROJECT_ID);
            will(returnValue(quoteOptionResource));
            oneOf(quoteOptionResource).discountRequest(QUOTE_OPTION_ID);
        }});

        quoteOptionFacade.putDiscountRequest(PROJECT_ID, QUOTE_OPTION_ID);
        context.assertIsSatisfied();
    }

}
