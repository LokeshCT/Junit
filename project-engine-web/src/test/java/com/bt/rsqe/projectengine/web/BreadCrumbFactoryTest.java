package com.bt.rsqe.projectengine.web;

import com.bt.rsqe.projectengine.ProjectDTO;
import com.bt.rsqe.projectengine.ProjectResource;
import com.bt.rsqe.projectengine.QuoteOptionDTO;
import com.bt.rsqe.projectengine.QuoteOptionResource;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.*;
import static org.junit.Assert.*;

@RunWith(JMock.class)
public class BreadCrumbFactoryTest {

    private static final String CUSTOMER_ID = "7789";
    private static final String CONTRACT_ID = "1234";
    private static final String PROJECT_ID = "1";
    private static final String DISPLAY_TEXT = "DISPLAY_TEXT";
    private static final String PROJECT_NAME = "PROJECT_NAME";
    private static final String QUOTE_OPTION_ID = "100";
    private static final String QUOTE_OPTION_NAME = "QUOTE_OPTION_NAME";

    private Mockery context = new JUnit4Mockery() {{
        setImposteriser(ClassImposteriser.INSTANCE);
    }};

    private BreadCrumbFactory breadCrumbFactory;
    private ProjectResource mockProjectResource;
    private ProjectDTO fakeProjectDto;
    private String expectedProjectBreadCrumbURI = "/rsqe/customers/" + CUSTOMER_ID + "/contracts/" + CONTRACT_ID + "/projects/" + PROJECT_ID;

    private String expectedQuoteOptionBreadCrumbURI = expectedProjectBreadCrumbURI + "/quote-options/" + QUOTE_OPTION_ID;
    private QuoteOptionResource mockQuoteOptionResource;

    @Before
    public void setUp() {
        mockProjectResource = context.mock(ProjectResource.class);
        breadCrumbFactory = BreadCrumbFactory.getInstance(mockProjectResource);
        fakeProjectDto = new ProjectDTO(PROJECT_ID, PROJECT_NAME, CUSTOMER_ID, CONTRACT_ID);
    }

    @Test
    public void shouldCreateBreadCrumbsForQuoteOptionResource() throws Exception {
        setUpProjectResourceExpectations();

        final List<BreadCrumb> breadCrumbsForQuoteOptionResource = breadCrumbFactory.createBreadCrumbsForQuoteOptionResource(PROJECT_ID);

        assertNotNull(breadCrumbsForQuoteOptionResource);
        assertThat(breadCrumbsForQuoteOptionResource.size(), is(1));

        assertBreadCrumbForProject(breadCrumbsForQuoteOptionResource, "Quote Options");
    }

    private void assertBreadCrumbForProject(List<BreadCrumb> breadCrumbsForQuoteOptionResource, String displayText) {
        final BreadCrumb breadCrumb = breadCrumbsForQuoteOptionResource.get(0);
        assertThat(breadCrumb.getDisplayText(), is(displayText));
        assertThat(breadCrumb.getUri(), is(expectedProjectBreadCrumbURI));
    }

    @Test
    public void shouldCreateBreadCrumbsForOfferResource() throws Exception {
        setUpQuoteOptionResourceExpectations();

        List<BreadCrumb> breadCrumbs = breadCrumbFactory.createBreadCrumbsForOfferResource(PROJECT_ID, QUOTE_OPTION_ID);

        assertNotNull(breadCrumbs);
        assertThat(breadCrumbs.size(), is(2));

        assertBreadCrumbForProject(breadCrumbs, "Quote Options");
        // Order is important.
        final BreadCrumb breadCrumbQuoteOption = breadCrumbs.get(1);
        assertThat(breadCrumbQuoteOption.getDisplayText(), is("Quote Option Details"));
        assertThat(breadCrumbQuoteOption.getUri(), is(expectedQuoteOptionBreadCrumbURI));

    }

    private void setUpQuoteOptionResourceExpectations() {
        setUpProjectResourceExpectations();

        mockQuoteOptionResource = context.mock(QuoteOptionResource.class);
        final QuoteOptionDTO fakeQuoteOptionDto = QuoteOptionDTO.newInstance(QUOTE_OPTION_ID, QUOTE_OPTION_NAME, null, null, null);

        context.checking(new Expectations() {{
            oneOf(mockProjectResource).quoteOptionResource(PROJECT_ID);
            will(returnValue(mockQuoteOptionResource));

            oneOf(mockQuoteOptionResource).get(QUOTE_OPTION_ID);
            will(returnValue(fakeQuoteOptionDto));

        }});
    }

    private void setUpProjectResourceExpectations() {
        context.checking(new Expectations() {{

            oneOf(mockProjectResource).get(PROJECT_ID);
            will(returnValue(fakeProjectDto));

        }});
    }


}
