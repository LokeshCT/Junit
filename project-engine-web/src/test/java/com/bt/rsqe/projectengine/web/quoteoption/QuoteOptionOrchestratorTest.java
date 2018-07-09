package com.bt.rsqe.projectengine.web.quoteoption;


import com.bt.rsqe.customerrecord.CustomerDTO;
import com.bt.rsqe.domain.QuoteOptionItemStatus;
import com.bt.rsqe.projectengine.ProjectIdDTO;
import com.bt.rsqe.projectengine.web.quoteoption.validation.QuoteOptionsBillAccountCurrencyValidator;
import com.bt.rsqe.projectengine.web.uri.UriFactoryImpl;
import com.bt.rsqe.projectengine.web.view.CustomerProjectQuoteOptionsTab;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Set;

import static com.bt.rsqe.projectengine.web.QuoteOptionDTOFixture.*;
import static com.google.common.collect.Lists.*;
import static org.hamcrest.core.Is.*;
import static org.junit.Assert.*;
import static org.junit.internal.matchers.IsCollectionContaining.*;
import static org.mockito.Mockito.*;

public class QuoteOptionOrchestratorTest extends QuoteOptionOrchestratorBaseTest {
    public ProjectIdDTO projectIdDTO;

    @Before
    public void setUp(){
        projectIdDTO = new ProjectIdDTO(newArrayList(PROJECT_ID));
    }

    @Test
    public void shouldBuildEmptyViewWhenNoItems() throws Exception {
        final CustomerProjectQuoteOptionsTab result = quoteOptionOrchestrator.buildResponse(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, projectIdDTO);

        assertThat(result.getQuoteOptions().size(), is(0));
    }

    @Test
    public void shouldReturnDTOWithOneQuoteOption() throws Exception {
        when(quoteOptionFacade.getAll(PROJECT_ID)).thenReturn(Lists.newArrayList(quoteOptionDTO));

        final CustomerProjectQuoteOptionsTab result = quoteOptionOrchestrator.buildResponse(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, projectIdDTO);
        assertThat(result.getQuoteOptions().size(), is(1));
    }

    @Test
    public void shouldReturnDtoWithCorrectProperties() throws Exception {
        when(customerFacade.getByToken(CUSTOMER_ID, TOKEN)).thenReturn(new CustomerDTO("custId", "custName", ""));

        final CustomerProjectQuoteOptionsTab result = quoteOptionOrchestrator.buildResponse(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, projectIdDTO);

        assertThat(result.getCustomerName(), is("custName"));
        assertThat(result.getQuoteOptionDialogUri(), is(UriFactoryImpl.quoteOptionDialog(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID).toString()));
        assertThat(result.getNotesDialogUri(), is("/rsqe/customers/customerId/contracts/contractId/projects/projectId/quote-options/notes"));
        assertThat(result.getDeleteQuoteUri(), is(is(UriFactoryImpl.deleteQuoteOptionUri(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID).toString())));
    }


    @Test
    public void shouldOrderQuoteOptionsByDateCreatedDescending() {
        final QuoteOptionOrchestratorTestFixture fixture =
            new QuoteOptionOrchestratorTestFixture()
                .withQuoteOption(aQuoteOptionDTO().withId("id1").withCreationDate("2011-01-01T12:30:55.000+00:00").withStatus(QuoteOptionItemStatus.DRAFT).build())
                .withQuoteOption(aQuoteOptionDTO().withId("id2").withCreationDate("2011-03-01T12:30:55.000+00:00").withStatus(QuoteOptionItemStatus.DRAFT).build())
                .withQuoteOption(aQuoteOptionDTO().withId("id3").withCreationDate("2011-03-01T12:30:59.000+00:00").withStatus(QuoteOptionItemStatus.DRAFT).build())
                .withQuoteOption(aQuoteOptionDTO().withId("id4").withCreationDate("2011-04-07T13:30:55.000+00:00").withStatus(QuoteOptionItemStatus.DRAFT).build());

        when(customerFacade.getByToken(CUSTOMER_ID, TOKEN)).thenReturn(new CustomerDTO());
        Mockito.when(quoteOptionFacade.getAll(PROJECT_ID)).thenReturn(fixture.build());

        final CustomerProjectQuoteOptionsTab result = quoteOptionOrchestrator.buildResponse(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, projectIdDTO);
        final List<CustomerProjectQuoteOptionsTab.QuoteOptionRow> quoteOptions = result.getQuoteOptions();
        assertThat(quoteOptions.size(), is(4));
        assertThat(quoteOptions.get(0).getId(), is("id4"));
        assertThat(quoteOptions.get(1).getId(), is("id3"));
        assertThat(quoteOptions.get(2).getId(), is("id2"));
        assertThat(quoteOptions.get(3).getId(), is("id1"));

    }

    @Test
    public void shouldCallQuoteOptionValidator() {
        quoteOptionOrchestrator.buildResponse(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, projectIdDTO);

        verify(validator).validate(eq(CUSTOMER_ID), any(QuoteOptionsBillAccountCurrencyValidator.class));
    }

    @Test
    public void shouldAddValidatorMessagesToView() {
        when(validator.validate(eq(CUSTOMER_ID), any(QuoteOptionsBillAccountCurrencyValidator.class))).thenReturn(Sets.newHashSet("This is a message", "This is another message"));

        CustomerProjectQuoteOptionsTab result = quoteOptionOrchestrator.buildResponse(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, projectIdDTO);

        Set<String> messages = result.getValidationMessages();
        assertThat(messages, hasItem("This is a message"));
        assertThat(messages, hasItem("This is another message"));
    }

    @Test
    public void shouldAddLaunchQuoteUriToView() {
        when(validator.validate(CUSTOMER_ID, billAccountCurrencyValidator)).thenReturn(Sets.newHashSet("This is a message", "This is another message"));

        CustomerProjectQuoteOptionsTab result = quoteOptionOrchestrator.buildResponse(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, projectIdDTO);

        assertNotNull(result.getProductConfiguratorUriFactory());
        assertThat(result.getProductConfiguratorUriFactory().getQuoteLaunchUri(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID), is("/rsqe/customers/customerId/contracts/contractId/projects/projectId"));
    }

    @Test
    public void shouldAddCustomerIdToView() {
        when(validator.validate(CUSTOMER_ID, billAccountCurrencyValidator)).thenReturn(Sets.newHashSet("This is a message", "This is another message"));

        CustomerProjectQuoteOptionsTab result = quoteOptionOrchestrator.buildResponse(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, projectIdDTO);

        assertNotNull(result.getProductConfiguratorUriFactory());
        assertThat(result.getCustomerId(), is(CUSTOMER_ID));
    }

    @Test
    public void shouldAddContractIdToView() {
        when(validator.validate(CUSTOMER_ID, billAccountCurrencyValidator)).thenReturn(Sets.newHashSet("This is a message", "This is another message"));

        CustomerProjectQuoteOptionsTab result = quoteOptionOrchestrator.buildResponse(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, projectIdDTO);

        assertNotNull(result.getProductConfiguratorUriFactory());
        assertThat(result.getContractId(), is(CONTRACT_ID));
    }

    @Test
    public void shouldAddProjectIdToView() {
        when(validator.validate(CUSTOMER_ID, billAccountCurrencyValidator)).thenReturn(Sets.newHashSet("This is a message", "This is another message"));

        CustomerProjectQuoteOptionsTab result = quoteOptionOrchestrator.buildResponse(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, projectIdDTO);

        assertNotNull(result.getProductConfiguratorUriFactory());
        assertThat(result.getProjectId(), is(PROJECT_ID));
    }

    @Test
    public void shouldAddListOfQuotesForCustomerToView() {
        when(validator.validate(CUSTOMER_ID, billAccountCurrencyValidator)).thenReturn(Sets.newHashSet("This is a message", "This is another message"));

        ProjectIdDTO projectIdList = new ProjectIdDTO(newArrayList(PROJECT_ID,"project2","project3"));
        CustomerProjectQuoteOptionsTab result = quoteOptionOrchestrator.buildResponse(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, projectIdList);

        assertNotNull(result.getProductConfiguratorUriFactory());
        assertThat(result.getProjectId(), is(PROJECT_ID));
        assertThat(result.getExpedioQuoteId().size(), is(3));
        assertTrue(result.getExpedioQuoteId().contains(PROJECT_ID));
        assertTrue(result.getExpedioQuoteId().contains("project2"));
        assertTrue(result.getExpedioQuoteId().contains("project3"));
    }
}
