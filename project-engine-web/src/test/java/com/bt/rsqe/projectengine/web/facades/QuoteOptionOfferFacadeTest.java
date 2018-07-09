package com.bt.rsqe.projectengine.web.facades;

import com.bt.rsqe.domain.project.OfferStatus;
import com.bt.rsqe.projectengine.OfferDTO;
import com.bt.rsqe.projectengine.OfferResource;
import com.bt.rsqe.projectengine.ProjectResource;
import com.bt.rsqe.projectengine.QuoteOptionItemDTO;
import com.bt.rsqe.projectengine.QuoteOptionResource;
import com.bt.rsqe.projectengine.web.model.OfferDetailsModel;
import com.bt.rsqe.projectengine.web.model.modelfactory.OfferDetailsModelFactory;
import com.bt.rsqe.utils.RSQEMockery;
import org.jmock.Expectations;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.google.common.collect.Lists.*;
import static java.util.Arrays.asList;

@RunWith(JMock.class)
public class QuoteOptionOfferFacadeTest {

    private static final String OFFER_CREATED_DATE = "2011-01-01T12:30:55.000+00:00";
    private static final String OFFER_STATUS = OfferStatus.ACTIVE.name();
    private static final String QUOTE_OPTION_ID = "quoteOptionId";
    private static final String PROJECT_ID = "projectId";
    private static final String OFFER_NAME = "name";
    private static final String OFFER_ID = "id1";

    private OfferResource offerResource;
    private QuoteOptionResource quoteOptionResource;
    private ProjectResource projectResource;
    private QuoteOptionOfferFacade offerFacade;
    private OfferDTO offerDTO;

    private JUnit4Mockery context = new RSQEMockery();
    private OfferDetailsModelFactory offerDetailsModelFactory;

    @Before
    public void setUp() {
        offerDTO = OfferDTO.create(OFFER_ID, OFFER_NAME, OFFER_CREATED_DATE, OFFER_STATUS, new ArrayList<QuoteOptionItemDTO>(),OFFER_NAME+"-COR");
        offerResource = context.mock(OfferResource.class);
        quoteOptionResource = context.mock(QuoteOptionResource.class);
        projectResource = context.mock(ProjectResource.class);
        offerDetailsModelFactory = context.mock(OfferDetailsModelFactory.class);
        offerFacade = new QuoteOptionOfferFacade(projectResource, offerDetailsModelFactory);

        context.checking(new Expectations() {{
            allowing(projectResource).quoteOptionResource(PROJECT_ID);
            will(returnValue(quoteOptionResource));
            allowing(quoteOptionResource).quoteOptionOfferResource(QUOTE_OPTION_ID);
            will(returnValue(offerResource));
        }});
    }

    @Test
    public void shouldCreateAnOffer() throws Exception {

        context.checking(new Expectations() {{
            oneOf(offerResource).post(with(any(OfferDTO.class)));
            will(returnValue(offerDTO));
        }});

        offerFacade.createOffer(PROJECT_ID, QUOTE_OPTION_ID, OFFER_NAME, asList("12", "23"), OFFER_NAME+"-COR");
        context.assertIsSatisfied();
    }

    @Test
    public void shouldGetOfferDetailsFromResource() throws Exception {

        context.checking(new Expectations() {{
            oneOf(offerResource).get(offerDTO.id);
            will(returnValue(offerDTO));
            ignoring(offerDetailsModelFactory);
        }});

        offerFacade.getOfferDetails(PROJECT_ID, QUOTE_OPTION_ID, offerDTO.id);
        context.assertIsSatisfied();
    }

    @Test
    public void shouldApproveOffer() {
        context.checking(new Expectations() {{
            oneOf(offerResource).approve("blah-offer-id");
        }});

        offerFacade.approve(PROJECT_ID, QUOTE_OPTION_ID, "blah-offer-id");
    }

    @Test
    public void shouldRejectOffer() {
        context.checking(new Expectations() {{
            oneOf(offerResource).reject("blah-offer-id");
        }});
        offerFacade.reject(PROJECT_ID, QUOTE_OPTION_ID, "blah-offer-id");
    }

    @Test
    public void shouldReturnSortableModels() throws Exception {
        context.checking(new Expectations() {{
            allowing(offerResource).get();
            will(returnValue(newArrayList(offerDTO, offerDTO, offerDTO)));
            exactly(3).of(offerDetailsModelFactory).create(offerDTO);
            will(returnValue(context.mock(OfferDetailsModel.class)));
        }});
        final List<OfferDetailsModel> models = offerFacade.get(PROJECT_ID, QUOTE_OPTION_ID);
        Collections.sort(models, new Comparator<OfferDetailsModel>() {
            @Override
            public int compare(OfferDetailsModel o1, OfferDetailsModel o2) {
                return 0;
            }
        });
        context.assertIsSatisfied();
    }

    @Test
    public void shouldCancelOfferApproval() {
        context.checking(new Expectations() {{
            oneOf(offerResource).cancelOfferApproval("blah-offer-id");
        }});
        offerFacade.cancelOfferApproval(PROJECT_ID, QUOTE_OPTION_ID, "blah-offer-id");
    }
}
