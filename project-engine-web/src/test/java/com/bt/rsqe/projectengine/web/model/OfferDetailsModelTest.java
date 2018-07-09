package com.bt.rsqe.projectengine.web.model;

import com.bt.rsqe.domain.project.OfferStatus;
import com.bt.rsqe.projectengine.OfferDTO;
import com.bt.rsqe.projectengine.web.model.modelfactory.LineItemModelFactory;
import com.bt.rsqe.utils.RSQEMockery;
import org.jmock.Mockery;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class OfferDetailsModelTest {

    private Mockery context = new RSQEMockery();
    private LineItemModelFactory lineItemModelFactory;
    private OfferDTO offerDTO;
    private OfferDetailsModel model;

    @Before
    public void before() {
        lineItemModelFactory = context.mock(LineItemModelFactory.class);
        offerDTO = new OfferDTO();
        model = new OfferDetailsModel(lineItemModelFactory, offerDTO);
    }

    @Test
    public void shouldGetOfferDetailsModelParameters() throws Exception {
        offerDTO.created = "2011/01/11";
        offerDTO.status = OfferStatus.ACTIVE.name();

        assertThat(model.getCreatedDate(), is("2011/01/11"));
        assertThat(model.isCustomerApprovable(), is(true));
        assertThat(model.isActive(), is(true));
    }

    @Test
    public void shouldGetId() {
        offerDTO.id = "id";
        assertThat(model.getId(), is("id"));
    }

    @Test
    public void shouldReturnApprovedIfOfferStatusIsApproved() {
        offerDTO.status = OfferStatus.APPROVED.name();
        assertThat(model.isApproved(), is(true));
    }
}