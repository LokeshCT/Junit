package com.bt.rsqe.projectengine.web.quoteoption;

import com.bt.rsqe.projectengine.ProjectIdDTO;
import com.bt.rsqe.projectengine.web.uri.UriFactoryImpl;
import com.bt.rsqe.projectengine.web.view.CustomerProjectQuoteOptionsTab;
import com.bt.rsqe.web.LocalDateTimeFormatter;
import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;

import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.core.Is.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class QuoteOptionOrchestratorPropertiesTest extends QuoteOptionOrchestratorBaseTest {

    private CustomerProjectQuoteOptionsTab.QuoteOptionRow quoteOption;
    private ProjectIdDTO projectIdDTO;

    @Before
    public void setup() {
        projectIdDTO = new ProjectIdDTO(newArrayList(PROJECT_ID));
    }

    @Test
    public void shouldSetId() {
        populateQuoteOptionFromResponse();
        assertThat(quoteOption.getId(), is(quoteOptionDTO.id));
    }

    @Test
    public void shouldSetName() {
        populateQuoteOptionFromResponse();
        assertThat(quoteOption.getName(), is(quoteOptionDTO.name));
    }

    @Test
    public void shouldSetCurrency() {
        populateQuoteOptionFromResponse();
        assertThat(quoteOption.getCurrency(), is(quoteOptionDTO.currency));
    }

    @Test
    public void shouldSetFriendlyId() {
        populateQuoteOptionFromResponse();
        assertThat(quoteOption.getFriendlyId(), is(quoteOptionDTO.friendlyQuoteId));
    }

    @Test
    public void shouldSetCreatedBy() {
        populateQuoteOptionFromResponse();
        assertThat(quoteOption.getCreatedBy(), is(quoteOptionDTO.createdBy));
    }

    @Test
    public void shouldSetCreationDate() {
        populateQuoteOptionFromResponse();
        assertThat(quoteOption.getCreationDate(), is(new LocalDateTimeFormatter(quoteOptionDTO.creationDate).format()));
    }

    @Test
    public void shouldSetStatus() {
        populateQuoteOptionFromResponse();
        assertThat(quoteOption.getStatus(), is(quoteOptionDTO.getStatus().getDescription()));
    }

    @Test
    public void shouldSetIfCStatus() {
        populateQuoteOptionFromResponse();
        assertThat(quoteOption.isIfcPending(), is(quoteOptionDTO.ifcPending));
    }

    @Test
    public void shouldSetDiscountApprovalRequested() {
        populateQuoteOptionFromResponse();
        assertThat(quoteOption.isDiscountApprovalRequested(), is(quoteOptionDTO.discountApprovalRequested));
    }

    @Test
    public void shouldNotHaveQuoteOptionItems() {
        populateQuoteOptionFromResponse();
        assertThat(quoteOption.isEditAllowed(), is(quoteOptionDTO.isEditAllowed));
    }

    @Test
    public void shouldNotHaveQuoteOptionNotes() {
        populateQuoteOptionFromResponse();
        assertThat(quoteOption.isHasQuoteOptionNotes(), is(quoteOptionDTO.hasQuoteOptionNotes));
    }

    @Test
    public void shouldSetUri() {
        populateQuoteOptionFromResponse();
        assertThat(quoteOption.getUri(), is(UriFactoryImpl.quoteOption(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, quoteOptionDTO.id).toString()));
    }

    @Test
    public void shouldSetBcmUri() {
        populateQuoteOptionFromResponse();
        assertThat(quoteOption.getBcmUri(), is(UriFactoryImpl.quoteOptionBcm(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, quoteOptionDTO.id).toString()));
    }


    @Test
    public void shouldGetBcmApproveUri() throws Exception {
        populateQuoteOptionFromResponse();
        assertThat(quoteOption.getBcmApproveUri(), is(UriFactoryImpl.quoteOptionBcm(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, quoteOptionDTO.id).toString() + "/approve-discounts"));
    }

    @Test
    public void shouldSetMigrationQuote() {
        populateQuoteOptionFromResponse();
        assertThat(quoteOption.getMigrationQuote(), is(quoteOptionDTO.migrationQuote));
    }

    @Test
    public void shouldSetMigrationQuoteOfFalse() {
        quoteOptionDTO.setMigrationQuote(false);
        when(quoteOptionFacade.getAll(PROJECT_ID)).thenReturn(Lists.newArrayList(quoteOptionDTO));
        CustomerProjectQuoteOptionsTab result = quoteOptionOrchestrator.buildResponse(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, projectIdDTO);
        quoteOption = result.getQuoteOptions().get(0);
        assertThat(quoteOption.getMigrationQuote(), is(quoteOptionDTO.getMigrationQuote()));
    }

    @Test
    public void shouldSetMigrationQuoteOfFalseWhenDtoMigrationQuoteIsNull() {
        quoteOptionDTO.setMigrationQuote(null);
        when(quoteOptionFacade.getAll(PROJECT_ID)).thenReturn(Lists.newArrayList(quoteOptionDTO));
        CustomerProjectQuoteOptionsTab result = quoteOptionOrchestrator.buildResponse(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, projectIdDTO);
        quoteOption = result.getQuoteOptions().get(0);
        quoteOptionDTO.setMigrationQuote(false);
        assertThat(quoteOption.getMigrationQuote(), is(quoteOptionDTO.getMigrationQuote()));
    }

    private void populateQuoteOptionFromResponse() {
        when(quoteOptionFacade.getAll(PROJECT_ID)).thenReturn(Lists.newArrayList(quoteOptionDTO));
        CustomerProjectQuoteOptionsTab result = quoteOptionOrchestrator.buildResponse(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, projectIdDTO);
        quoteOption = result.getQuoteOptions().get(0);
    }
}