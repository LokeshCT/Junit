package com.bt.rsqe.projectengine.web.quoteoption;

import com.bt.rsqe.customerrecord.CustomerDTO;
import com.bt.rsqe.domain.QuoteOptionItemStatus;
import com.bt.rsqe.projectengine.ProjectResource;
import com.bt.rsqe.projectengine.QuoteOptionDTO;
import com.bt.rsqe.projectengine.QuoteOptionResource;
import com.bt.rsqe.projectengine.web.facades.CustomerFacade;
import com.bt.rsqe.projectengine.web.facades.QuoteOptionFacade;
import com.bt.rsqe.projectengine.web.quoteoption.validation.BillAccountCurrencyValidator;
import com.bt.rsqe.projectengine.web.quoteoption.validation.QuoteOptionDependencyValidator;
import com.bt.rsqe.projectengine.web.uri.UriFactory;
import com.bt.rsqe.projectengine.web.uri.UriFactoryImpl;
import com.bt.rsqe.security.UserContext;
import com.bt.rsqe.security.UserContextManager;
import org.jmock.Mockery;
import org.junit.Before;

import java.util.ArrayList;
import java.util.HashSet;

import static com.bt.rsqe.projectengine.web.QuoteOptionDTOFixture.*;
import static com.bt.rsqe.security.UserContextBuilder.*;
import static com.google.common.collect.Lists.*;
import static org.mockito.Mockito.*;

public abstract class QuoteOptionOrchestratorBaseTest {
    protected final static String CUSTOMER_ID = "customerId";
    protected final static String CONTRACT_ID = "contractId";
    protected final static String PROJECT_ID = "projectId";
    public static final String TOKEN = "aToken";

    protected QuoteOptionOrchestrator quoteOptionOrchestrator;
    protected Mockery context;
    protected QuoteOptionFacade quoteOptionFacade;
    protected CustomerFacade customerFacade;
    protected ProjectResource projectResource;

    protected QuoteOptionDependencyValidator validator;
    protected BillAccountCurrencyValidator billAccountCurrencyValidator = mock(BillAccountCurrencyValidator.class);

    protected static final QuoteOptionDTO quoteOptionDTO;
    private UriFactory productConfiguratorUriFactory;

    static {
        quoteOptionDTO = aQuoteOptionDTO().withId("id")
                                          .withFriendlyQuoteId("friendlyId")
                                          .withName("name")
                                          .withCurrency("GBP")
                                          .withCreationDate("2011-01-01T12:30:55.000+00:00")
                                          .withCreatedBy("admin")
                                          .isDiscountApprovalRequested()
                                          .isIfcPending()
                                          .withStatus(QuoteOptionItemStatus.DRAFT)
                                          .withContractTerm("term")
                                          .withMigrationQuote(true)
                                          .withDeleteStatus(false).build();
    }

    @Before
    public void before() {

        UserContext userContext = aDirectUserContext().withToken(TOKEN).build();
        UserContextManager.setCurrent(userContext);

        quoteOptionFacade = mock(QuoteOptionFacade.class);
        customerFacade = mock(CustomerFacade.class);
        validator = mock(QuoteOptionDependencyValidator.class);
        productConfiguratorUriFactory = new UriFactoryImpl(null);
        ProjectResource projectResource = mock(ProjectResource.class);
        QuoteOptionResource quoteOptionResource = mock(QuoteOptionResource.class);

        when(customerFacade.getByToken(CUSTOMER_ID, TOKEN)).thenReturn(new CustomerDTO());
        when(quoteOptionFacade.getAll(PROJECT_ID)).thenReturn(new ArrayList<QuoteOptionDTO>());

        when(validator.validate(anyString(), any(BillAccountCurrencyValidator.class))).thenReturn(new HashSet<String>());
        when(projectResource.quoteOptionResource(PROJECT_ID)).thenReturn(quoteOptionResource);
        when(quoteOptionResource.get()).thenReturn(newArrayList(quoteOptionDTO));

        quoteOptionOrchestrator = new QuoteOptionOrchestrator(quoteOptionFacade, customerFacade, validator, productConfiguratorUriFactory, projectResource);
    }
}
