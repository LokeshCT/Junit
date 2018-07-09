package com.bt.rsqe.projectengine.web;

import com.bt.rsqe.projectengine.QuoteOptionDTO;
import com.bt.rsqe.projectengine.web.facades.ProductIdentifierFacade;
import com.bt.rsqe.projectengine.web.facades.QuoteOptionFacade;
import com.bt.rsqe.projectengine.web.uri.UriFactory;
import com.bt.rsqe.projectengine.web.view.Products;
import com.bt.rsqe.utils.RSQEMockery;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.Response;

import static com.bt.rsqe.matchers.ResponseMatcher.*;
import static javax.ws.rs.core.Response.Status.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.core.Is.*;

public class QuoteOptionDialogsResourceHandlerTest {

    private Mockery context;

    protected static final String CUSTOMER_ID = "customerId";
    protected static final String PROJECT_ID = "projectId";
    protected static final String QUOTE_OPTION_ID = "quoteOptionId";

    private QuoteOptionDialogsResourceHandler resourceHandler;

    private ProductIdentifierFacade productIdentifierFacade;
    private QuoteOptionFacade quoteOptionFacade;
    private UriFactory uriFactory;

    @Before
    public void setUp() {
        context = new RSQEMockery();

        productIdentifierFacade = context.mock(ProductIdentifierFacade.class);
        quoteOptionFacade = context.mock(QuoteOptionFacade.class);
        uriFactory = context.mock(UriFactory.class);
        resourceHandler = new QuoteOptionDialogsResourceHandler(uriFactory, productIdentifierFacade, quoteOptionFacade);
    }

    @Test
    public void shouldReturnCreateTemplatePage() throws Exception {
        context.checking(new Expectations() {{
            ignoring(uriFactory);
            oneOf(quoteOptionFacade).get(PROJECT_ID, QUOTE_OPTION_ID);
            will(returnValue(QuoteOptionDTO.newInstance("", "", "GBP", "", "")));
            oneOf(productIdentifierFacade).getAllSellableProducts();
            will(returnValue(new Products()));
        }});

        final Response response = resourceHandler.getBulkTemplateDialog(CUSTOMER_ID, PROJECT_ID, QUOTE_OPTION_ID);
        assertThat(response, is(aResponse().withStatus(OK)));
        context.assertIsSatisfied();
    }

}
