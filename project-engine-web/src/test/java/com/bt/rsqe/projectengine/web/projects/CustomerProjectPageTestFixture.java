package com.bt.rsqe.projectengine.web.projects;

import com.bt.rsqe.ProjectEngineWebEnvironmentTestConfig;
import com.bt.rsqe.configuration.ConfigurationProvider;
import com.bt.rsqe.customerrecord.CustomerDTO;
import com.bt.rsqe.customerrecord.CustomerResource;
import com.bt.rsqe.customerrecord.ExpedioServicesResource;
import com.bt.rsqe.customerrecord.UserResource;
import com.bt.rsqe.domain.project.LineItemAction;
import com.bt.rsqe.fixtures.UserDTOFixture;
import com.bt.rsqe.expedio.project.ExpedioProjectResource;
import com.bt.rsqe.inlife.client.ApplicationPropertyResourceClient;
import com.bt.rsqe.projectengine.ProjectDTO;
import com.bt.rsqe.projectengine.ProjectResource;
import com.bt.rsqe.projectengine.QuoteOptionDTO;
import com.bt.rsqe.projectengine.QuoteOptionItemDTO;
import com.bt.rsqe.projectengine.QuoteOptionItemDTOFixture;
import com.bt.rsqe.domain.QuoteOptionItemStatus;
import com.bt.rsqe.projectengine.web.BreadCrumbFactory;
import com.bt.rsqe.projectengine.web.CustomerProjectResourceHandler;
import com.bt.rsqe.projectengine.web.QuoteOptionResourceHandler;
import com.bt.rsqe.projectengine.web.facades.CustomerFacade;
import com.bt.rsqe.projectengine.web.facades.ExpedioServicesFacade;
import com.bt.rsqe.projectengine.web.facades.QuoteOptionFacade;
import com.bt.rsqe.projectengine.web.facades.UserFacade;
import com.bt.rsqe.projectengine.web.quoteoption.QuoteOptionOrchestrator;
import com.bt.rsqe.projectengine.web.quoteoption.validation.QuoteOptionDependency;
import com.bt.rsqe.projectengine.web.quoteoption.validation.QuoteOptionDependencyValidator;
import com.bt.rsqe.projectengine.web.resourcestubs.ProjectResourceStub;
import com.bt.rsqe.projectengine.web.resourcestubs.QuoteOptionItemResourceStub;
import com.bt.rsqe.projectengine.web.resourcestubs.QuoteOptionResourceStub;
import com.bt.rsqe.projectengine.web.uri.UriFactory;
import com.bt.rsqe.tpe.config.TemplateSelectionGuideConfig;
import com.bt.rsqe.tpe.config.TpeConfig;
import com.bt.rsqe.utils.Environment;
import com.bt.rsqe.web.Presenter;

import java.io.IOException;
import java.net.URI;

import static com.bt.rsqe.expedio.fixtures.ProjectDTOFixture.*;
import static org.mockito.Mockito.*;

final class CustomerProjectPageTestFixture {
    static final String PROJECT_ID = "projectId";
    static final String CUSTOMER_ID = "7789";
    static final String CONTRACT_ID = "1234";
    static final String CUSTOMER_NAME = "Customer Name";
    static final String PROJECT_URI_PATH = "http://localhost:%d/rsqe/customers/%s/projects/%s";
    static final String DEFAULT_QUOTE_OPTION_NAME = "QuoteOptionName";
    static final String DEFAULT_QUOTE_CURRENCY = "USD";
    static final String DEFAULT_QUOTE_TERM = "48";
    static final String CREATED_BY = "createdBy";
    static final String QUOTE_OPTION_ID = "52B9EDA9-C4D0-4AD1-F3FA-FF85496C6A8B";
    static final String PROJECT_NAME = "Default Project";
    static final String NEW_QUOTE_OPTION_NAME = "New Quote Option";
    static final String NEW_QUOTE_OPTION_CURRENCY = "USD";
    static final String NEW_QUOTE_OPTION_CURRENCY_VALUE = "USD";
    static final String NEW_QUOTE_OPTION_TERM = "24";

    private CustomerProjectPageTest test;
    private ProjectResource projects;
    private CustomerResource customers;
    private ExpedioServicesFacade expedioServicesFacade;

    CustomerProjectPageTestFixture(CustomerProjectPageTest test) {
        this.test = test;
        withPopulatedCustomerResource();
        withPopulatedExpedioProject();
    }

    public CustomerProjectPage build() throws IOException {

        TpeConfig tpeConfig = new TpeConfig() {
            @Override
            public String getServiceEndpoint() {
                return String.format("%s://%s:%s/rsqe/codebase-stub/tpe", "http", "localhost", "9985");
            }

            @Override
            public TemplateSelectionGuideConfig getTemplateSelectionGuideConfig() {
                return null;
            }
        };
        test.givenApplicationStart(

            new CustomerProjectResourceHandler(new Presenter(), projects,
                                               new QuoteOptionOrchestrator(new QuoteOptionFacade(projects),
                                                                           new CustomerFacade(customers),
                                                                           new QuoteOptionDependencyValidator(new QuoteOptionDependency[] {}), mock(UriFactory.class), projects),
                                               ConfigurationProvider.provide(ProjectEngineWebEnvironmentTestConfig.class, Environment.env()).getProjectEngineWebConfig(),
                                               expedioServicesFacade,
                                               mock(CustomerResource.class),mock(ApplicationPropertyResourceClient.class), null, null, null, null),
            new QuoteOptionResourceHandler(
                null,
                new Presenter(),
                projects,
                null,
                null,
                BreadCrumbFactory.getInstance(projects),
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null));

        return CustomerProjectPage.navigateToCustomerProjectPage(test.browser(), CUSTOMER_ID, PROJECT_ID, "BID_MGR");
    }

    public CustomerProjectPageTestFixture withPopulatedProjectResource() {
        createProjectWithQuoteOptionResource();
        return this;
    }

    private void withPopulatedExpedioProject() {
        ExpedioProjectResource expedioProjectResource = new ExpedioProjectResource(URI.create(""), null) {
            @Override
            public com.bt.rsqe.expedio.project.ProjectDTO getProject(final String projectId) {
                return aProjectDTO().build();
            }
        };
        CustomerResource customerResource = new CustomerResource(URI.create(""), null);
        ExpedioServicesResource expedioServicesResource = new ExpedioServicesResource(URI.create(""), null);
        UserResource userResource = mock(UserResource.class);
        when(userResource.findUser(anyString())).thenReturn(new UserDTOFixture()
                                                                .withLoginName("aLoginId")
                                                                .withForeName("someForeName")
                                                                .withSurName("someSurName")
                                                                .withEmailId("abc@bt.com")
                                                                .withPhoneNumber("012345678")
                                                                .withEIN("6012345678")
                                                                .build());
        expedioServicesFacade = new ExpedioServicesFacade(expedioServicesResource, expedioProjectResource, new UserFacade(userResource));
    }

    public CustomerProjectPageTestFixture withPopulatedLineItemResource() {
        QuoteOptionItemDTO quoteOptionItemDTO = QuoteOptionItemDTOFixture.aQuoteOptionItemDTO().withSCode("Name").withAction(LineItemAction.PROVIDE.name())
                                                                         .withStatus(QuoteOptionItemStatus.DRAFT).build();

        QuoteOptionItemResourceStub quoteOptionItemResourceStub = createProjectWithQuoteOptionResource().quoteOptionItemResource(QUOTE_OPTION_ID);
        quoteOptionItemResourceStub.with(quoteOptionItemDTO);
        return this;
    }

    public CustomerProjectPageTestFixture withPopulatedCustomerResource() {
        customers =
            new CustomerResource(URI.create(""), null) {
                @Override
                public CustomerDTO get(String customerId, String contractId) {
                    return new CustomerDTO("customer id", "Customer Name From Expedio", "sales channel");
                }

                @Override
                public CustomerDTO getByToken(String customerId, String token) {
                    return new CustomerDTO("customer id", "Customer Name From Expedio", "sales channel");
                }
            };
        return this;
    }

    private QuoteOptionResourceStub createProjectWithQuoteOptionResource() {
        ProjectDTO projectDTO = new ProjectDTO(PROJECT_ID, PROJECT_NAME, CUSTOMER_ID, CONTRACT_ID);
        projects = new ProjectResourceStub().with(projectDTO);
        QuoteOptionDTO quoteOptionDTO = QuoteOptionDTO.newInstance(QUOTE_OPTION_ID,
                                                                   DEFAULT_QUOTE_OPTION_NAME,
                                                                   DEFAULT_QUOTE_CURRENCY,
                                                                   DEFAULT_QUOTE_TERM,
                                                                   CREATED_BY);
        QuoteOptionResourceStub quoteOptionResourceStub = (QuoteOptionResourceStub) projects.quoteOptionResource(projectDTO.id);
        quoteOptionResourceStub.with(quoteOptionDTO);
        return quoteOptionResourceStub;
    }
}
