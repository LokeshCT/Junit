package com.bt.rsqe.projectengine.web;

import com.bt.rsqe.configuration.UrlConfig;
import com.bt.rsqe.customerrecord.CustomerDTO;
import com.bt.rsqe.expedio.fixtures.ProjectDTOFixture;
import com.bt.rsqe.inlife.client.ApplicationPropertyResourceClient;
import com.bt.rsqe.projectengine.ProjectDTO;
import com.bt.rsqe.projectengine.ProjectResource;
import com.bt.rsqe.customerrecord.CustomerResource;
import com.bt.rsqe.projectengine.QuoteOptionContractTerm;
import com.bt.rsqe.projectengine.QuoteOptionCurrency;
import com.bt.rsqe.projectengine.QuoteOptionResource;
import com.bt.rsqe.projectengine.server.ProjectEngineWebConfig;
import com.bt.rsqe.projectengine.web.facades.ExpedioServicesFacade;
import com.bt.rsqe.projectengine.web.quoteoption.QuoteOptionOrchestrator;
import com.bt.rsqe.security.UserContext;
import com.bt.rsqe.security.UserContextManager;
import com.bt.rsqe.web.Presenter;
import com.bt.rsqe.web.View;
import com.bt.rsqe.web.rest.exception.ResourceNotFoundException;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.Response;

import static com.bt.rsqe.matchers.ResponseMatcher.*;
import static com.bt.rsqe.security.UserContextBuilder.aDirectUserContext;
import static javax.ws.rs.core.Response.Status.*;
import static org.hamcrest.core.Is.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class CustomerProjectResourceHandlerTest {

    private ProjectResource projectResource;
    private CustomerResource customerResource;
    private ApplicationPropertyResourceClient  applicationPropertyResourceClient;
    private ProjectEngineWebConfig projectEngineWebConfiguration;
    private Presenter presenter;
    private ProjectDTO projectDTO;
    private CustomerProjectResourceHandler customerProjectResourceHandler;
    private ExpedioServicesFacade expedioServicesFacade;
    private QuoteOptionResource quoteOptionResource;
    private QuoteOptionOrchestrator quoteOptionOrchestrator;
    private CustomerProjectResourceHandler.CustomerProjectResourceHandlerConfig customerProjectResourceHandlerConfig;
    private UrlConfig webMetricsUrlConfig;
    private UrlConfig helpLinkUrlConfig;

    private static final String PROJECT_ID = "projectId";
    private static final String PROJECT_NAME = "projectName";
    private static final String CUSTOMER_ID = "customerId";
    private static final String CONTRACT_ID = "contractId";

    @Before
    public void setUp() throws Exception {
        projectDTO = new ProjectDTO(PROJECT_ID, PROJECT_NAME, CUSTOMER_ID, CONTRACT_ID);
        projectResource = mock(ProjectResource.class);
        projectEngineWebConfiguration = mock(ProjectEngineWebConfig.class);
        webMetricsUrlConfig = mock(UrlConfig.class);
        helpLinkUrlConfig = mock(UrlConfig.class);
        presenter = mock(Presenter.class);
        expedioServicesFacade = mock(ExpedioServicesFacade.class);
        quoteOptionResource = mock(QuoteOptionResource.class);
        customerProjectResourceHandlerConfig = mock(CustomerProjectResourceHandler.CustomerProjectResourceHandlerConfig.class);
        customerResource = mock(CustomerResource.class);
        quoteOptionOrchestrator = mock(QuoteOptionOrchestrator.class);

        UserContext userContext = aDirectUserContext().withToken("aToken").build();
        UserContextManager.setCurrent(userContext);
        when(customerResource.getByToken(CUSTOMER_ID, "aToken")).thenReturn(new CustomerDTO(CUSTOMER_ID, "someName", "BT GERMANY", "123", CONTRACT_ID));

        when(projectResource.get(PROJECT_ID)).thenReturn(projectDTO);
        when(webMetricsUrlConfig.getUrl()).thenReturn("/rsqe/web-metrics");
        when(helpLinkUrlConfig.getUrl()).thenReturn("https://office.bt.com/sites/gs_processes/SitePages/DisplaySystemProcedure.aspx");
        when(projectEngineWebConfiguration.getUrl(ProjectEngineWebConfig.SUBMIT_WEB_METRICS_URI)).thenReturn(webMetricsUrlConfig);
        when(projectEngineWebConfiguration.getUrl(ProjectEngineWebConfig.HELP_LINK_URI)).thenReturn(webMetricsUrlConfig);

        customerProjectResourceHandler = new CustomerProjectResourceHandler(presenter, projectResource, quoteOptionOrchestrator, projectEngineWebConfiguration, expedioServicesFacade, customerResource, applicationPropertyResourceClient, null, null, null, null);
    }

    @Test
    public void shouldGetProjectFromProjectEngine() throws Exception {
        Response response = customerProjectResourceHandler.getProject(CUSTOMER_ID, PROJECT_ID);

        assertThat(response, is(aResponse().withStatus(OK)));
        verify(projectResource).get(PROJECT_ID);
        verify(expedioServicesFacade, never()).getExpedioProject(PROJECT_ID);
    }

    @Test
    public void shouldGetProjectFromExpedio() throws Exception {
        String quoteName = "quoteName";
        String currency = QuoteOptionCurrency.USD.getValue();
        String contractTerm = QuoteOptionContractTerm.SIXTY.getValue();
        String salesRepName = "martin";


        when(presenter.render(any(View.class))).thenReturn("<html/>");
        com.bt.rsqe.expedio.project.ProjectDTO project = ProjectDTOFixture.aProjectDTO()
                                                                          .withProjectId(PROJECT_ID)
                                                                          .withCustomerId(CUSTOMER_ID)
                                                                          .withCurrency(currency)
                                                                          .withContractTerm(contractTerm)
                                                                          .withSalesRepName(salesRepName)
                                                                          .build();

        when(expedioServicesFacade.getExpedioProject(PROJECT_ID)).thenReturn(project);
        when(projectEngineWebConfiguration.getCustomerProjectResourceHandlerConfig()).thenReturn(customerProjectResourceHandlerConfig);
        when(customerProjectResourceHandlerConfig.getDefaultCurrency()).thenReturn(QuoteOptionCurrency.GBP);
        when(customerProjectResourceHandlerConfig.getDefaultContractTerm()).thenReturn(QuoteOptionContractTerm.TWELVE);
        when(customerProjectResourceHandlerConfig.getDefaultQuoteOptionName()).thenReturn(quoteName);
        when(customerProjectResourceHandlerConfig.getDefaultCreatedBy()).thenReturn(salesRepName);
        when(projectResource.quoteOptionResource(PROJECT_ID)).thenReturn(quoteOptionResource);
        when(projectResource.get(PROJECT_ID)).thenThrow(new ResourceNotFoundException()).thenReturn(projectDTO);


        Response response = customerProjectResourceHandler.getProject(CUSTOMER_ID, PROJECT_ID);

        assertThat(response, is(aResponse().withStatus(OK)));

        verify(projectResource, times(2)).get(PROJECT_ID);
        verify(expedioServicesFacade).getExpedioProject(PROJECT_ID);
        verify(quoteOptionResource).post(PROJECT_ID, quoteName, currency, contractTerm, salesRepName);
    }


}