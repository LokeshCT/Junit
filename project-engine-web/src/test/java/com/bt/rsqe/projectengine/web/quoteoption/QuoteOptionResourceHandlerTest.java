package com.bt.rsqe.projectengine.web.quoteoption;

import com.bt.rsqe.cookie.JsonCookie;
import com.bt.rsqe.cookie.LineItemFilterCookie;
import com.bt.rsqe.customerinventory.client.ProductInstanceClient;
import com.bt.rsqe.customerrecord.CustomerResource;
import com.bt.rsqe.customerrecord.ExpedioClientResources;
import com.bt.rsqe.customerrecord.UserResource;
import com.bt.rsqe.domain.DateFormats;
import com.bt.rsqe.inlife.client.ApplicationCapabilityProvider;
import com.bt.rsqe.projectengine.ImportProductLogItemResource;
import com.bt.rsqe.projectengine.ProjectDTO;
import com.bt.rsqe.projectengine.ProjectResource;
import com.bt.rsqe.projectengine.QuoteOptionDTO;
import com.bt.rsqe.projectengine.QuoteOptionItemCloneDTO;
import com.bt.rsqe.projectengine.QuoteOptionResource;
import com.bt.rsqe.projectengine.web.QuoteOptionDTOFixture;
import com.bt.rsqe.projectengine.web.QuoteOptionResourceHandler;
import com.bt.rsqe.projectengine.web.facades.ExpedioServicesFacade;
import com.bt.rsqe.projectengine.web.facades.QuoteOptionNoteFacade;
import com.bt.rsqe.projectengine.web.quoteoptiondetails.QuoteOptionDetailsOrchestrator;
import com.bt.rsqe.projectengine.web.quoteoptionpricing.PriceSuppressStrategy;
import com.bt.rsqe.projectengine.web.quoteoptionpricing.PricingSheetOrchestrator;
import com.bt.rsqe.projectengine.web.quoteoptionpricing.QuoteOptionPricingOrchestrator;
import com.bt.rsqe.projectengine.web.quoteoptionpricing.QuoteOptionRevenueOrchestrator;
import com.bt.rsqe.projectengine.web.view.QuoteOptionPricingDTO;
import com.bt.rsqe.projectengine.web.view.QuoteOptionRevenueDTO;
import com.bt.rsqe.projectengine.web.view.QuoteOptionUsagePricingDTO;
import com.bt.rsqe.projectengine.web.view.filtering.PaginatedFilter;
import com.bt.rsqe.projectengine.web.view.pagination.Pagination;
import com.bt.rsqe.security.UserContext;
import com.bt.rsqe.security.UserContextManager;
import com.bt.rsqe.security.UserDTO;
import com.bt.rsqe.security.UserType;
import com.bt.rsqe.tpe.config.TemplateSelectionGuideConfig;
import com.bt.rsqe.tpe.config.TpeConfig;
import com.bt.rsqe.utils.JSONSerializer;
import com.bt.rsqe.utils.RSQEMockery;
import com.bt.rsqe.web.AjaxResponseDTO;
import com.bt.rsqe.web.Presenter;
import com.bt.rsqe.web.rest.exception.BadRequestException;
import com.google.common.base.Optional;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.List;

import static com.bt.rsqe.expedio.fixtures.ProjectDTOFixture.*;
import static com.bt.rsqe.matchers.ReflectionEqualsMatcher.*;
import static com.bt.rsqe.matchers.ResponseMatcher.*;
import static com.bt.rsqe.security.UserContextBuilder.*;
import static com.google.common.collect.Lists.*;
import static javax.ws.rs.core.Response.Status.*;
import static junit.framework.Assert.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.core.Is.*;
import static org.mockito.Mockito.*;

@RunWith(JMock.class)
public class QuoteOptionResourceHandlerTest {
    private static final String PROJECT_ID = "PROJECT_ID";
    private static final String QUOTE_OPTION_ID = "QUOTE_OPTION_ID";
    private static final String FRIENDLY_QUOTE_OPTION_ID = "FRIENDLY_QUOTE_OPTION_ID";
    private static final String CUSTOMER_ID = "customerId";
    private static final String CONTRACT_ID = "contractId";
    private static final String PROJECT_NAME = "PROJECTNAME";
    private static final String FILE_NAME = "importedbcm.xls";
    private static final String OFFER_ID = "offerId";

    private final Mockery context = new RSQEMockery();

    private QuoteOptionNoteFacade mockQuoteOptionNoteFacade = context.mock(QuoteOptionNoteFacade.class);
    private ApplicationCapabilityProvider applicationCapabilityProvider = context.mock(ApplicationCapabilityProvider.class);
    private ProductInstanceClient productInstanceClient = context.mock(ProductInstanceClient.class);
    private PricingSheetOrchestrator mockPricingSheetOrchestrator = context.mock(PricingSheetOrchestrator.class);
    private QuoteOptionPricingSummaryOrchestrator mockQuoteOptionPricingSummaryOrchestrator = context.mock(QuoteOptionPricingSummaryOrchestrator.class);
    private QuoteOptionBulkUploadOrchestrator mockQuoteOptionBulkUploadOrchestrator = context.mock(QuoteOptionBulkUploadOrchestrator.class);
    private QuoteOptionResourceHandler quoteOptionResourceHandler;
    private QuoteOptionDetailsOrchestrator mockDetailsOrchestrator = context.mock(QuoteOptionDetailsOrchestrator.class);
    private ProjectResource projectResource = context.mock(ProjectResource.class);
    private CustomerResource customerResource = context.mock(CustomerResource.class);
    private ExpedioServicesFacade expedioServicesFacade = context.mock(ExpedioServicesFacade.class);
    private QuoteOptionResource quoteOptionResource = context.mock(QuoteOptionResource.class);
    private QuoteOptionDTO quoteOptionDTO = context.mock(QuoteOptionDTO.class);
    private ImportProductLogItemResource importProductLogItemResource = context.mock(ImportProductLogItemResource.class);
    private JSONSerializer mockJsonSerializer = context.mock(JSONSerializer.class);
    private QuoteOptionRevenueOrchestrator mockRevenueOrchestrator = context.mock(QuoteOptionRevenueOrchestrator.class);
    private Pagination pagination = context.mock(Pagination.class);
    private ProjectDTO projectDTO;
    private com.bt.rsqe.expedio.project.ProjectDTO expedioProjectDto;
    private ExpedioClientResources expedioClientResources = context.mock(ExpedioClientResources.class);
    private UserDTO userDto;
    private UserResource userResource = context.mock(UserResource.class);
    private QuoteOptionPricingOrchestrator pricingOrchestrator = context.mock(QuoteOptionPricingOrchestrator.class);
    private String submitWebMetricsUri= "/web-metrics";
    private final String helpLinkUri = "";

    @Before
    public void before() {
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

        quoteOptionResourceHandler = new QuoteOptionResourceHandler(null,
                                                                    new Presenter(),
                                                                    projectResource,
                                                                    mockDetailsOrchestrator,
                                                                    pricingOrchestrator,
                                                                    null,
                                                                    mockQuoteOptionNoteFacade,
                                                                    mockPricingSheetOrchestrator,
                                                                    mockQuoteOptionPricingSummaryOrchestrator,
                                                                    mockQuoteOptionBulkUploadOrchestrator,
                                                                    mockRevenueOrchestrator,
                                                                    mockJsonSerializer,
                                                                    customerResource,
                                                                    expedioServicesFacade,
                                                                    expedioClientResources,
                                                                    null,
                                                                    submitWebMetricsUri,
                                                                    applicationCapabilityProvider,
                                                                    productInstanceClient,
                                                                    helpLinkUri);
        projectDTO = new ProjectDTO(PROJECT_ID, PROJECT_NAME, CUSTOMER_ID, CONTRACT_ID);
        expedioProjectDto = aProjectDTO().withBidNumber("10").withExpedioRef("ECP100").withSalesRepName("forename surname").withExpiryDate(null).build();
        UserContext userContext = anIndirectUserContext().build();
        UserContextManager.setCurrent(userContext);
        userDto = new UserDTO("forename", "surname", "email", UserType.INDIRECT, "000011112222", "loginName", "ein");
    }

    @Test
    public void shouldCreateNote() throws Exception {
        context.checking(new Expectations() {{
            oneOf(mockQuoteOptionNoteFacade).saveNote(PROJECT_ID, QUOTE_OPTION_ID, "NOTE");
        }});

        quoteOptionResourceHandler.createNote(PROJECT_ID, QUOTE_OPTION_ID, "NOTE");
    }

    @Test
    public void shouldReturnViewWithCorrectValues() throws Exception {
        context.checking(new Expectations() {{
            oneOf(mockDetailsOrchestrator).buildNoteView(with(any(List.class)));
            oneOf(mockQuoteOptionNoteFacade).getNotes(PROJECT_ID, QUOTE_OPTION_ID);
        }});

        quoteOptionResourceHandler.noteForm("", CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID);
    }

    @Test
    public void shouldCalculatePricingSummary() throws Exception {
        context.checking(new Expectations() {{
            oneOf(mockQuoteOptionPricingSummaryOrchestrator).getPricingSummary(PROJECT_ID, QUOTE_OPTION_ID, CUSTOMER_ID, CONTRACT_ID, PriceSuppressStrategy.OFFERS_UI);
            will(returnValue(new QuoteOptionPricingSummaryDTO()));
        }});

        quoteOptionResourceHandler.getPricingSummary(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID, null);
    }

    @Test
    public void shouldOverrideSuppressStrategyWhenFetchingPricingSummary() throws Exception {
        context.checking(new Expectations() {{
            oneOf(mockQuoteOptionPricingSummaryOrchestrator).getPricingSummary(PROJECT_ID, QUOTE_OPTION_ID, CUSTOMER_ID, CONTRACT_ID, PriceSuppressStrategy.UI_COSTS);
            will(returnValue(new QuoteOptionPricingSummaryDTO()));
        }});

        quoteOptionResourceHandler.getPricingSummary(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID, "UI_COSTS");
    }

    @Test
    public void shouldReturnExcelSpreadsheet() throws Exception {
        context.checking(new Expectations() {{
            oneOf(mockPricingSheetOrchestrator).renderPricingSheet(CUSTOMER_ID, PROJECT_ID, QUOTE_OPTION_ID, Optional.<String>absent());
            oneOf(projectResource).get(with(PROJECT_ID));
            will(returnValue(projectDTO));
            oneOf(projectResource).quoteOptionResource(with(PROJECT_ID));
            will(returnValue(quoteOptionResource));
            oneOf(quoteOptionResource).get(with(QUOTE_OPTION_ID));
            will(returnValue(QuoteOptionDTO.newInstance("QUOTE_OPTION_ID", "QuoteName", "GBP", "contractTerm", "createdBy")));

        }});
        quoteOptionResourceHandler.getPricingSheet(CUSTOMER_ID, PROJECT_ID, QUOTE_OPTION_ID, "");
    }

    @Test
    public void shouldReturnOfferExcelSpreadsheet() throws Exception {
        context.checking(new Expectations() {{
            oneOf(mockPricingSheetOrchestrator).renderPricingSheet(CUSTOMER_ID, PROJECT_ID, QUOTE_OPTION_ID, Optional.of(OFFER_ID));
            oneOf(projectResource).get(with(PROJECT_ID));
            will(returnValue(projectDTO));
            oneOf(projectResource).quoteOptionResource(with(PROJECT_ID));
            will(returnValue(quoteOptionResource));
            oneOf(quoteOptionResource).get(with(QUOTE_OPTION_ID));
            will(returnValue(QuoteOptionDTO.newInstance("QUOTE_OPTION_ID", "QuoteName", "GBP", "contractTerm", "createdBy")));

        }});
        quoteOptionResourceHandler.getPricingSheet(CUSTOMER_ID, PROJECT_ID, QUOTE_OPTION_ID, OFFER_ID);
    }

    @Test
    public void shouldReturnCloneQuoteOptionsList() throws Exception {
        context.checking(new Expectations() {{
            oneOf(projectResource).quoteOptionResource(with(PROJECT_ID));
            will(returnValue(quoteOptionResource));
            oneOf(quoteOptionResource).getCloneCandidateFor(QUOTE_OPTION_ID);
            will(returnValue(
                newArrayList(
                    QuoteOptionDTO.newInstance("id1", "friendly-id1", "name1", "GBP", "24", "createdBy", null),
                    QuoteOptionDTO.newInstance("id2", "friendly-id1", "name2", "GBP", "24", "createdBy", null),
                    QuoteOptionDTO.newInstance(QUOTE_OPTION_ID, FRIENDLY_QUOTE_OPTION_ID, "name3", "GBP", "24", "createdBy", null)
                )));
        }});
        assertThat(quoteOptionResourceHandler.getCloneTargetQuoteOptionsList(PROJECT_ID, QUOTE_OPTION_ID).getStatus(), is(OK.getStatusCode()));
    }

    @Test
    public void shouldCloneSelectedQuoteOptionsToTargetQuoteOption() {
        final QuoteOptionItemCloneDTO optionItemCloneDTO = QuoteOptionItemCloneDTO.newInstance("originalQuoteOptionId", "AAA", "BBB");

        context.checking(new Expectations() {{
            exactly(2).of(projectResource).quoteOptionResource(with(PROJECT_ID));
            will(returnValue(quoteOptionResource));
            oneOf(quoteOptionResource).cloneItems(with(QUOTE_OPTION_ID), with(reflectionEquals(optionItemCloneDTO)));
            oneOf(quoteOptionResource).get(QUOTE_OPTION_ID);
            will(returnValue(QuoteOptionDTOFixture.aQuoteOptionDTO().withMigrationQuote(true).build()));
            oneOf(quoteOptionResource).get("originalQuoteOptionId");
            will(returnValue(QuoteOptionDTOFixture.aQuoteOptionDTO().withMigrationQuote(true).build()));
        }});

        assertThat(quoteOptionResourceHandler.cloneQuoteOptionItems(PROJECT_ID, QUOTE_OPTION_ID, "originalQuoteOptionId", "AAA,BBB").getStatus(), is(OK.getStatusCode()));
    }

    @Test
    public void shouldReturnBadRequestWithValidationExceptionWhenOptionItemCloningIsNotAllowed() throws Exception {
        context.checking(new Expectations() {{
            oneOf(projectResource).quoteOptionResource(with(PROJECT_ID));
            will(returnValue(quoteOptionResource));
            oneOf(quoteOptionResource).get(QUOTE_OPTION_ID);
            will(returnValue(QuoteOptionDTOFixture.aQuoteOptionDTO().withMigrationQuote(true).build()));
            oneOf(quoteOptionResource).get("originalQuoteOptionId");
            will(returnValue(QuoteOptionDTOFixture.aQuoteOptionDTO().withMigrationQuote(false).build()));
        }});

        Response response = quoteOptionResourceHandler.cloneQuoteOptionItems(PROJECT_ID, QUOTE_OPTION_ID, "originalQuoteOptionId", "AAA,BBB");
        assertThat(response.getStatus(), is(Response.Status.BAD_REQUEST.getStatusCode()));
        assertThat((String)response.getEntity(), is("This is a migration quote and can only support addition of migration products to the quote."));
    }

    @Test
    public void shouldReturnBadRequestIfDuplicateQuoteOption() throws Exception {
        final BadRequestException badRequestException = new BadRequestException();
        context.checking(new Expectations() {{
            oneOf(projectResource).quoteOptionResource(PROJECT_ID);
            will(returnValue(quoteOptionResource));
            oneOf(expedioClientResources).getUserResource();
            will(returnValue(userResource));
            oneOf(userResource).findUser("indirect");
            will(returnValue(userDto));
            oneOf(expedioServicesFacade).getExpedioProject(PROJECT_ID);
            will(returnValue(expedioProjectDto));
            oneOf(quoteOptionResource).post(with(any(QuoteOptionDTO.class)));
            will(throwException(badRequestException));
        }});

        final Response response = quoteOptionResourceHandler.saveQuoteOption(CUSTOMER_ID, PROJECT_ID, "", "", "name", "term", "currency");
        assertThat(response.getStatus(), is(Response.Status.BAD_REQUEST.getStatusCode()));
    }

    @Test
    public void shouldBulkUploadFiles() throws Exception {
        final String productCode = "DUMMY_PRODUCT_CODE";
        final FormDataMultiPart multiPartFormData = context.mock(FormDataMultiPart.class);
        final AjaxResponseDTO bulkUploadDto = context.mock(AjaxResponseDTO.class);

        context.checking(new Expectations() {{
            oneOf(mockQuoteOptionBulkUploadOrchestrator).upload(productCode, multiPartFormData);
            will(returnValue(bulkUploadDto));
            oneOf(mockJsonSerializer).serialize(bulkUploadDto);
            will(returnValue("serialized string"));
        }});

        Response response = quoteOptionResourceHandler.bulkUpload(productCode, multiPartFormData);

        assertThat(response, is(aResponse().withStatus(OK)));
        assertThat((String) response.getEntity(), is("serialized string"));
    }

    @Test
    public void shouldGetRevenueDetails(){
        final QuoteOptionRevenueDTO revenueDTO = context.mock(QuoteOptionRevenueDTO.class);
        context.checking(new Expectations(){{
            one(mockRevenueOrchestrator).getRevenueFor(with(CUSTOMER_ID),with(CONTRACT_ID),with(PROJECT_ID),with(QUOTE_OPTION_ID), with(any(Pagination.class)));
            will(returnValue(revenueDTO));
        }
        });

        Response response = quoteOptionResourceHandler.getRevenueDetails(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID,1,1,1);
        assertThat(response, is(aResponse().withStatus(OK)));
        assertThat((QuoteOptionRevenueDTO) response.getEntity(), is(revenueDTO));
    }

    @Test
    public void shouldSaveQuoteOption() throws Exception {
        final QuoteOptionDTO quoteOption = QuoteOptionDTO.newInstance(QUOTE_OPTION_ID, "projectId", "name", "currency", "term", "forename surname", "ein");
        final QuoteOptionDTO updatedDto = QuoteOptionDTOFixture
            .aQuoteOptionDTO()
            .withName("quoteOptionName")
            .withCreationDate("2014-01-01T08:00:00.500+01:00")
            .withCurrency("USD")
            .withContractTerm("48")
            .withCreatedBy("forename surname")
            .build();
        final com.bt.rsqe.expedio.project.ProjectDTO updatedProjectDto = expedioProjectDto;
        DateTime updatedDateTime = DateTimeFormat.forPattern(DateFormats.TIMESTAMP_6_DATE_FORMAT).parseDateTime("2014-01-01T08:00:00.500+01:00");
        updatedProjectDto.setModifiedDate(updatedDateTime.toDate());

        context.checking(new Expectations() {{
            oneOf(expedioServicesFacade).getExpedioProject(PROJECT_ID);
            will(returnValue(expedioProjectDto));
            oneOf(expedioClientResources).getUserResource();
            will(returnValue(userResource));
            oneOf(userResource).findUser("indirect");
            will(returnValue(userDto));
            oneOf(projectResource).quoteOptionResource(PROJECT_ID);
            will(returnValue(quoteOptionResource));
            oneOf(quoteOptionResource).put(quoteOption);
        }});

        final Response response = quoteOptionResourceHandler.saveQuoteOption(CUSTOMER_ID, PROJECT_ID, QUOTE_OPTION_ID, "", "name", "term", "currency");
        assertThat(response.getStatus(), is(Response.Status.OK.getStatusCode()));
    }

    @Test
    public void shouldThrowParsingExceptionWhenSavingQuoteOption() throws Exception {
        final QuoteOptionDTO quoteOption = QuoteOptionDTO.newInstance(QUOTE_OPTION_ID, "projectId", "name", "currency", "term", "forename surname", "ein");
        final QuoteOptionDTO updatedDto = QuoteOptionDTOFixture.aQuoteOptionDTO().withName("quoteOptionName").withCreationDate("unparseable date").build();

        context.checking(new Expectations() {{
            oneOf(expedioServicesFacade).getExpedioProject(PROJECT_ID);
            will(returnValue(expedioProjectDto));
            oneOf(expedioClientResources).getUserResource();
            will(returnValue(userResource));
            oneOf(userResource).findUser("indirect");
            will(returnValue(userDto));
            oneOf(projectResource).quoteOptionResource(PROJECT_ID);
            will(returnValue(quoteOptionResource));
            oneOf(quoteOptionResource).put(quoteOption);
        }});

        final Response response = quoteOptionResourceHandler.saveQuoteOption(CUSTOMER_ID, PROJECT_ID, QUOTE_OPTION_ID, "", "name", "term", "currency");
        assertThat(response.getStatus(), is(Response.Status.OK.getStatusCode()));
    }

    @Test
    public void shouldGetUsageChargesForQuoteOption() throws Exception {
        final QuoteOptionUsagePricingDTO quoteOptionUsagePricingDTO = new QuoteOptionUsagePricingDTO();
        final QuoteOptionUsagePricingDTO.UsageProduct product = new QuoteOptionUsagePricingDTO.UsageProduct();
        product.productName = "aProductName";
        quoteOptionUsagePricingDTO.products = newArrayList(product);

        final UriInfo uriInfo = Mockito.mock(UriInfo.class);
        when(uriInfo.getQueryParameters(true)).thenReturn(new MultivaluedHashMap());


        context.checking(new Expectations() {{
            exactly(1).of(pricingOrchestrator).buildUsageResponse(with(CUSTOMER_ID), with(CONTRACT_ID), with(PROJECT_ID), with(QUOTE_OPTION_ID), with(any(PaginatedFilter.class)), with(PriceSuppressStrategy.UI_PRICES));
            will(returnValue(quoteOptionUsagePricingDTO));
        }});

        Response response = quoteOptionResourceHandler.getQuoteOptionPricingTabUsageCharges(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID, 1, 1, 1, uriInfo);
        assertThat((QuoteOptionUsagePricingDTO)response.getEntity(), is(quoteOptionUsagePricingDTO));
    }

    @Test
    public void shouldGetCostsForQuoteOption() throws Exception {
        final QuoteOptionPricingDTO quoteOptionPricingDTO = new QuoteOptionPricingDTO();

        final UriInfo uriInfo = Mockito.mock(UriInfo.class);
        when(uriInfo.getQueryParameters(true)).thenReturn(new MultivaluedHashMap());

        context.checking(new Expectations() {{
            exactly(1).of(pricingOrchestrator).buildStandardResponse(with(CUSTOMER_ID), with(CONTRACT_ID), with(PROJECT_ID), with(QUOTE_OPTION_ID), with(any(PaginatedFilter.class)), with(PriceSuppressStrategy.UI_COSTS));
            will(returnValue(quoteOptionPricingDTO));
        }});

        Response response = quoteOptionResourceHandler.getQuoteOptionPricingTabCostCharges(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID, 1, 1, 1, uriInfo);
        assertThat((QuoteOptionPricingDTO)response.getEntity(), is(quoteOptionPricingDTO));
    }

	@Test
    public void shouldMarkQuoteOptionAsDeleted() throws Exception {
        final QuoteOptionDTO quoteOption = QuoteOptionDTO.newInstance(QUOTE_OPTION_ID, "projectId", "name", "currency", "term", "forename surname", null);
        final QuoteOptionDTO quoteOptionDeleted = QuoteOptionDTO.newInstance(QUOTE_OPTION_ID, "projectId", "name", "currency", "term", "forename surname", null);
        quoteOptionDeleted.setDeleteStatus(true);
        //When
        context.checking(new Expectations() {{
            oneOf(projectResource).quoteOptionResource(PROJECT_ID);
            will(returnValue(quoteOptionResource));
            oneOf(quoteOptionResource).get(QUOTE_OPTION_ID);
            will(returnValue(quoteOption));
            oneOf(quoteOptionResource).put(quoteOptionDeleted);
        }});
        //Then
        final Response response = quoteOptionResourceHandler.deleteQuoteOption(PROJECT_ID, QUOTE_OPTION_ID);
        assertThat(response.getStatus(), is(Response.Status.OK.getStatusCode()));
    }

    @Test
    public void shouldCreateLineItemFilterCookie() throws Exception {
        LineItemFilterCookie lineItemFilterCookie = new LineItemFilterCookie();
        lineItemFilterCookie.getFilters().add(new LineItemFilterCookie.LineItemFilter());
        lineItemFilterCookie.getFilters().get(0).setQuoteOptionId("aQuoteOptionId");
        lineItemFilterCookie.getFilters().get(0).getLineItemIds().add("L1");
        Response response = quoteOptionResourceHandler.filterLineItems(lineItemFilterCookie.toCookie().getValue());

        final List<Object> cookies = response.getMetadata().get("Set-Cookie");
        assertThat(cookies.size(), is(1));

        NewCookie cookie = (NewCookie)cookies.get(0);
        assertThat(cookie.getName(), is(LineItemFilterCookie.ITEM_FILTER_COOKIE));
        final LineItemFilterCookie actualCookie = JsonCookie.fromCookie(cookie.getValue(), LineItemFilterCookie.class);
        assertThat(actualCookie.getFilters().get(0).getQuoteOptionId(), is("aQuoteOptionId"));
        assertThat(actualCookie.getFilters().get(0).getLineItemIds().get(0), is("L1"));
    }

    @Test
    public void shouldExpirePriceLinesWhenLoadingQuoteOptionDetailsTab() throws Exception {
        context.checking(new Expectations() {{
            oneOf(applicationCapabilityProvider).isFunctionalityEnabled(ApplicationCapabilityProvider.Capability.EXPIRE_PRICE_LINES_ENABLED, true, Optional.of(QUOTE_OPTION_ID));
            will(returnValue(true));
            oneOf(productInstanceClient).setExpiredPriceLines(QUOTE_OPTION_ID);
            oneOf(mockDetailsOrchestrator).buildView(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID);
        }});

        quoteOptionResourceHandler.getQuoteOptionDetailsTab(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID);
    }

    @Test
    public void shouldReturnTrueIfQuoteHasFirmPrices() {

        context.checking(new Expectations(){{
            oneOf(productInstanceClient).hasFirmAssets(QUOTE_OPTION_ID);
            will(returnValue(true));
        }});

        Response response = quoteOptionResourceHandler.validateContractTermChange(QUOTE_OPTION_ID);
        assertTrue((Boolean)response.getEntity());
        assertThat(response.getStatus(), is(Response.Status.OK.getStatusCode()));
    }

    @Test
    public void shouldReturnFalseIfQuoteDoesNotContainFirmPrices() {
         context.checking(new Expectations(){{
            oneOf(productInstanceClient).hasFirmAssets(QUOTE_OPTION_ID);
            will(returnValue(false));
        }});

        Response response = quoteOptionResourceHandler.validateContractTermChange(QUOTE_OPTION_ID);
        assertFalse((Boolean) response.getEntity());
        assertThat(response.getStatus(), is(Response.Status.OK.getStatusCode()));
    }
}
