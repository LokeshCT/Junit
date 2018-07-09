package com.bt.rsqe.projectengine.web;

import com.bt.rsqe.CallbackConditional;
import com.bt.rsqe.customerinventory.client.CustomerInventoryClientManager;
import com.bt.rsqe.customerinventory.client.CustomerInventoryStubClientManagerFactory;
import com.bt.rsqe.customerinventory.client.ProductInstanceClient;
import com.bt.rsqe.customerinventory.parameter.LineItemId;
import com.bt.rsqe.domain.QuoteOptionItemStatus;
import com.bt.rsqe.domain.bom.fixtures.ProductOfferingFixture;
import com.bt.rsqe.domain.product.DefaultProductInstanceFixture;
import com.bt.rsqe.domain.product.parameters.ProductCategoryCode;
import com.bt.rsqe.domain.product.parameters.ProductIdentifier;
import com.bt.rsqe.domain.project.ProductInstanceFactory;
import com.bt.rsqe.domain.project.StubCountryResolver;
import com.bt.rsqe.enums.ProductCodes;
import com.bt.rsqe.fixtures.UserDTOFixture;
import com.bt.rsqe.inlife.client.ApplicationCapabilityProvider;
import com.bt.rsqe.pmr.client.PmrClient;
import com.bt.rsqe.pmr.client.PmrMocker;
import com.bt.rsqe.pricing.PriceClientResponse;
import com.bt.rsqe.projectengine.ImportErrorLogResource;
import com.bt.rsqe.projectengine.ImportProductErrorLogDTO;
import com.bt.rsqe.projectengine.ImportProductLogItemResource;
import com.bt.rsqe.projectengine.ImportProductStatusLogDTO;
import com.bt.rsqe.projectengine.LineItemValidationResultDTO;
import com.bt.rsqe.projectengine.ProjectEngineClientResources;
import com.bt.rsqe.projectengine.ProjectResource;
import com.bt.rsqe.projectengine.QuoteOptionDTO;
import com.bt.rsqe.projectengine.QuoteOptionItemDTOFixture;
import com.bt.rsqe.projectengine.QuoteOptionItemResource;
import com.bt.rsqe.projectengine.QuoteOptionResource;
import com.bt.rsqe.projectengine.web.facades.ExpedioServicesFacade;
import com.bt.rsqe.projectengine.web.facades.FlattenedProductStructure;
import com.bt.rsqe.projectengine.web.facades.FutureProductInstanceFacade;
import com.bt.rsqe.projectengine.web.quoteoptiondetails.QuoteOptionDetailsOrchestrator;
import com.bt.rsqe.projectengine.web.quoteoptionorders.ecrfsheet.ECRFSheetOrchestrator;
import com.bt.rsqe.projectengine.web.quoteoptionorders.ecrfsheet.ECRFSheetOrchestratorTest;
import com.bt.rsqe.projectengine.web.tpe.TpeStatusManager;
import com.bt.rsqe.projectengine.web.view.QuoteOptionDetailsDTO;
import com.bt.rsqe.projectengine.web.view.filtering.PaginatedFilter;
import com.bt.rsqe.projectengine.web.view.sorting.PaginatedSort;
import com.bt.rsqe.security.UserContextManager;
import com.bt.rsqe.security.UserDTO;
import com.bt.rsqe.utils.RSQEMockery;
import com.bt.rsqe.utils.countries.Countries;
import com.bt.rsqe.web.Presenter;
import com.google.common.base.Optional;
import org.apache.poi.ss.usermodel.Workbook;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.hamcrest.core.Is;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.List;
import java.util.Set;

import static com.bt.rsqe.matchers.ReflectionEqualsMatcher.*;
import static com.bt.rsqe.matchers.ResponseMatcher.*;
import static com.bt.rsqe.security.UserContextBuilder.*;
import static com.google.common.collect.Lists.*;
import static com.google.common.collect.Sets.*;
import static org.junit.Assert.*;
import static org.mockito.Matchers.*;

public class LineItemResourceHandlerTest {

    private static final String CUSTOMER_ID = "cust";
    private static final String CONTRACT_ID = "contract";
    private static final String CONTRACT_TERM = "12";
    private static final String PROJECT_ID = "proj";
    private static final String QUOTE_OPTION_ID = "quote";
    private static final String PRODUCT_CODE = "aProductCode";
    private LineItemResourceHandler lineItemResourceHandler;
    private Mockery mockery;
    private QuoteOptionDetailsOrchestrator quoteOptionDetailsOrchestrator;
    private QuoteOptionItemResource quoteOptionItemResource;
    private static final String LINE_ITEM_ID = "line-item-id";
    private ProjectEngineClientResources clientResources;
    private static PmrClient pmr;
    private static CustomerInventoryClientManager cifClientManager;
    private static ProductInstanceFactory productInstanceFactory;
    private static FutureProductInstanceFacade futureProductInstanceFacade;
    private static ECRFSheetOrchestrator ecrfSheetOrchestrator;
    private static ExpedioServicesFacade expedioServicesFacade;
    private UriInfo uriInfo;
    private ProjectResource projectResource;
    private QuoteOptionResource quoteOptionResource;
    private ImportProductLogItemResource importProductLogItemResource;
    private ImportProductLogItemResource importProductLogItemResource2;
    private ImportErrorLogResource importErrorLogResource;
    private static PriceHandlerService priceHandlerService;
    private static ProductInstanceClient productInstanceClient;
    private static ApplicationCapabilityProvider capabilityProvider;
    private UserDTO userDTO;
    private TpeStatusManager tpeStatusManager;

    private static final FlattenedProductStructure STUB_ONE_VOICE_STRUCTURE = new FlattenedProductStructure(null, null) {
        @Override public String getRootProductCode() { return ProductCodes.Onevoice.productCode(); }
        @Override public void markAll() { /*no-op*/ }
    };

    private static final FlattenedProductStructure STUB_STRUCTURE = new FlattenedProductStructure(null, null) {
        @Override
        public String getRootProductCode() {
            return ProductCodes.Invalid.productCode();
        }

        @Override
        public void markAll() { /*no-op*/ }
    };


    @BeforeClass
    public static void beforeClass() throws Exception {
        pmr = PmrMocker.getMockedInstance();
        cifClientManager = CustomerInventoryStubClientManagerFactory.getClientManager(pmr);
        productInstanceFactory = ProductInstanceFactory.getProductInstanceFactory(pmr, cifClientManager, StubCountryResolver.resolveTo(Countries.byIsoStatic("GB")));
        futureProductInstanceFacade = new FutureProductInstanceFacade(cifClientManager.getProductInstanceClient(), productInstanceFactory);
    }

    @Before
    public void setUp() throws Exception {
        mockery = new RSQEMockery();
        tpeStatusManager = mockery.mock(TpeStatusManager.class);
        quoteOptionDetailsOrchestrator = mockery.mock(QuoteOptionDetailsOrchestrator.class);
        quoteOptionItemResource = mockery.mock(QuoteOptionItemResource.class);
        clientResources = mockery.mock(ProjectEngineClientResources.class);
        uriInfo = mockery.mock(UriInfo.class);
        capabilityProvider = mockery.mock(ApplicationCapabilityProvider.class);
        ecrfSheetOrchestrator = mockery.mock(ECRFSheetOrchestrator.class);
        projectResource = mockery.mock(ProjectResource.class);
        quoteOptionResource = mockery.mock(QuoteOptionResource.class);
        importProductLogItemResource = mockery.mock(ImportProductLogItemResource.class);
        importProductLogItemResource2 = mockery.mock(ImportProductLogItemResource.class);
        importErrorLogResource = mockery.mock(ImportErrorLogResource.class);
        productInstanceClient = mockery.mock(ProductInstanceClient.class);
        expedioServicesFacade = mockery.mock(ExpedioServicesFacade.class);
        priceHandlerService = mockery.mock(PriceHandlerService.class);
        mockery.checking(new Expectations() {{
            allowing(clientResources).quoteOptionItemResource(PROJECT_ID, QUOTE_OPTION_ID);
            will(returnValue(quoteOptionItemResource));
        }});
        UserContextManager.setCurrent(aDirectUserContext().withLoginName("john").build());
        userDTO = UserDTOFixture.anUser().build();
        lineItemResourceHandler = new LineItemResourceHandler(new Presenter(), quoteOptionDetailsOrchestrator, clientResources,
                                                              futureProductInstanceFacade, ecrfSheetOrchestrator, projectResource, productInstanceClient, expedioServicesFacade, priceHandlerService, tpeStatusManager, capabilityProvider);
    }

    @Test
    public void shouldDelegateValidationAndReturnTheResult() throws Exception {
        final FutureProductInstanceFacade futureProductInstanceFacade = mockery.mock(FutureProductInstanceFacade.class);
        LineItemResourceHandler lineItemResourceHandler = new LineItemResourceHandler(new Presenter(), quoteOptionDetailsOrchestrator, clientResources,
                                                                                      futureProductInstanceFacade, ecrfSheetOrchestrator,
                                                                                      projectResource, productInstanceClient, expedioServicesFacade, priceHandlerService,tpeStatusManager, capabilityProvider);

        mockery.checking(new Expectations() {{
            oneOf(futureProductInstanceFacade).buildFullFlattenedRelationshipStructure(new LineItemId(LINE_ITEM_ID));
            will(returnValue(STUB_STRUCTURE));
            oneOf(quoteOptionItemResource).validate(LINE_ITEM_ID);
            will(returnValue(LineItemValidationResultDTO.valid()));
        }});
        final Response response = lineItemResourceHandler.validate(PROJECT_ID, QUOTE_OPTION_ID, LINE_ITEM_ID);
        assertThat(response, aResponse().withStatusOK().withEntity(reflectionEquals(LineItemValidationResultDTO.valid())));
        mockery.assertIsSatisfied();
    }

    @Test
    public void shouldPersistChangesToOneVoiceProductsBeforeValidation() throws Exception {
        final FutureProductInstanceFacade futureProductInstanceFacade = mockery.mock(FutureProductInstanceFacade.class);
        LineItemResourceHandler lineItemResourceHandler = new LineItemResourceHandler(new Presenter(), quoteOptionDetailsOrchestrator,
                                                                                      clientResources, futureProductInstanceFacade,
                                                                                      ecrfSheetOrchestrator, projectResource, productInstanceClient, expedioServicesFacade, priceHandlerService, tpeStatusManager, capabilityProvider);

        mockery.checking(new Expectations() {{
            oneOf(futureProductInstanceFacade).buildFullFlattenedRelationshipStructure(new LineItemId(LINE_ITEM_ID));
            will(returnValue(STUB_ONE_VOICE_STRUCTURE));
            oneOf(futureProductInstanceFacade).saveProductInstance(with(any(FlattenedProductStructure.class)));
            oneOf(quoteOptionItemResource).validate(LINE_ITEM_ID);
            will(returnValue(LineItemValidationResultDTO.valid()));
        }});
        final Response response = lineItemResourceHandler.validate(PROJECT_ID, QUOTE_OPTION_ID, LINE_ITEM_ID);
        assertThat(response, aResponse().withStatusOK().withEntity(reflectionEquals(LineItemValidationResultDTO.valid())));
        mockery.assertIsSatisfied();
    }

    @Test
    public void testGetQuoteOptionLineItems() throws Exception {
        final QuoteOptionDetailsDTO result = new QuoteOptionDetailsDTO();
        mockery.checking(new Expectations() {{
            allowing(capabilityProvider).isFunctionalityEnabled(with(equal(ApplicationCapabilityProvider.Capability.FILTER_CEASED_BFG_SITES)), with(equal(true)), with(equal(Optional.of(QUOTE_OPTION_ID))));
            will(returnValue(false));
            allowing(quoteOptionDetailsOrchestrator).buildJsonResponse(with(equal(CUSTOMER_ID)), with(equal(CONTRACT_ID)), with(equal(PROJECT_ID)),
                                                                       with(equal(QUOTE_OPTION_ID)), with(any(PaginatedFilter.class)), with(any(PaginatedSort.class)),  with(any(String.class)), with(any(String.class)));
            will(returnValue(result));
            oneOf(uriInfo).getQueryParameters(true);
            will(returnValue(new MultivaluedHashMap()));
        }});
        final Response response = lineItemResourceHandler.getQuoteOptionLineItems(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID, 1, 10, 2, null, uriInfo);
        assertThat(response, aResponse().withStatusOK().withEntity(result));
        mockery.assertIsSatisfied();
    }

    @Test
    public void shouldCallECRFSheetOrchestratorWhenImportingFile() throws Exception {
        mockery.checking(new Expectations() {{
            ImportProductStatusLogDTO importProductInitiatedStatusLogDTO = new ImportProductStatusLogDTO(QUOTE_OPTION_ID, LINE_ITEM_ID, ImportStatus.Initiated.toString(), "sCode", "BATMAN",
                                                                                                         "occ_test_upload.xls");
            ImportProductStatusLogDTO importProductSuccessStatusLogDTO = new ImportProductStatusLogDTO(QUOTE_OPTION_ID, LINE_ITEM_ID, ImportStatus.Success.toString(), "sCode", "BATMAN",
                                                                                                       "occ_test_upload.xls");
            one(projectResource).quoteOptionResource(PROJECT_ID);
            will(returnValue(quoteOptionResource));

            one(quoteOptionResource).get(QUOTE_OPTION_ID);
            will(returnValue(QuoteOptionDTOFixture.aQuoteOptionDTO().withCreatedBy("BATMAN").withMigrationQuote(false).build()));

            one(priceHandlerService).processLineItemsForPricing(with(any(Set.class)), with(any(String.class)), with(any(String.class)), with(any(String.class)), with(any(Boolean.class)), with(any(String.class)));
            will(returnValue(anyMapOf(String.class, PriceClientResponse.class)));

            one(quoteOptionResource).importProductLogItemResource(QUOTE_OPTION_ID);
            will(returnValue(importProductLogItemResource));

            one(importProductLogItemResource).put(importProductInitiatedStatusLogDTO);

            one(quoteOptionResource).importProductLogItemResource(QUOTE_OPTION_ID);
            will(returnValue(importProductLogItemResource2));

            one(importProductLogItemResource2).put(importProductSuccessStatusLogDTO);

            one(quoteOptionResource).getProductImportStatusForQuote(QUOTE_OPTION_ID);
            will(returnValue(importProductInitiatedStatusLogDTO));

            atLeast(2).of(quoteOptionResource).importErrorLogResource(QUOTE_OPTION_ID);
            will(returnValue(importErrorLogResource));

            oneOf(importErrorLogResource).put(anyListOf(ImportProductErrorLogDTO.class));

            oneOf(importErrorLogResource).delete("sCode");

            one(productInstanceClient).get(new LineItemId(LINE_ITEM_ID));
            will(returnValue(DefaultProductInstanceFixture.aProductInstance().withLineItemId(LINE_ITEM_ID).withProductOffering(ProductOfferingFixture.aProductOffering().withProductIdentifier(new ProductIdentifier("sCode", "version"))).build()));

            oneOf(ecrfSheetOrchestrator).importUsingLineItem(with(CUSTOMER_ID),
                                                             with(CONTRACT_ID),
                                                             with(PROJECT_ID),
                                                             with(QUOTE_OPTION_ID),
                                                             with(LINE_ITEM_ID),
                                                             with(any(Workbook.class)),
                                                             with(any(ImportResults.class)),
                                                             with(any(String.class)),
                                                             with(any(Boolean.class)), with(any(ProductCategoryCode.class)));
            will(returnValue(newHashSet(LINE_ITEM_ID)));

            oneOf(expedioServicesFacade).getUserDetails(with("john"));
            UserDTO userDTO = UserDTOFixture.anUser().build();
            will(returnValue(userDTO));

            one(quoteOptionResource).importProductLogItemResource(QUOTE_OPTION_ID);
            will(returnValue(importProductLogItemResource));

            oneOf(importProductLogItemResource).sendEmail(with("sCode"), with(userDTO));
        }});
        final Response response = lineItemResourceHandler.importUsingLineItem(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID, LINE_ITEM_ID,
                                                                              ECRFSheetOrchestratorTest.class.getResourceAsStream("occ_test_upload.xls"),
                                                                              FormDataContentDisposition
                                                                                  .name("occ_test_upload.xls")
                                                                                  .fileName("occ_test_upload.xls").build());
        assertThat(response, aResponse().withStatusOK());
        mockery.assertIsSatisfied();
    }

    @Test
    public void shouldCallECRFSheetOrchestratorWhenBulkImportingFile() throws Exception {
        mockery.checking(new Expectations() {{

            ImportProductStatusLogDTO importProductInitiatedStatusLogDTO = new ImportProductStatusLogDTO(QUOTE_OPTION_ID, null, ImportStatus.Initiated.toString(), "aProductCode", "BATMAN",
                                                                                                         "occ_test_upload.xls");
            ImportProductStatusLogDTO importProductSuccessStatusLogDTO = new ImportProductStatusLogDTO(QUOTE_OPTION_ID, null, ImportStatus.Success.toString(), "aProductCode", "BATMAN",
                                                                                                       "occ_test_upload.xls");

            one(projectResource).quoteOptionResource(PROJECT_ID);
            will(returnValue(quoteOptionResource));

            one(quoteOptionResource).get(QUOTE_OPTION_ID);
            will(returnValue(QuoteOptionDTOFixture.aQuoteOptionDTO().withCreatedBy("BATMAN").withContractTerm(CONTRACT_TERM).build()));

            one(quoteOptionResource).importProductLogItemResource(QUOTE_OPTION_ID);
            will(returnValue(importProductLogItemResource));

            one(priceHandlerService).processLineItemsForPricing(with(any(Set.class)), with(any(String.class)), with(any(String.class)), with(any(String.class)), with(any(Boolean.class)), with(any(String.class)));
            will(returnValue(anyMapOf(String.class, PriceClientResponse.class)));


            one(importProductLogItemResource).put(importProductInitiatedStatusLogDTO);

            one(quoteOptionResource).put(with(any(QuoteOptionDTO.class)));

            one(quoteOptionResource).importProductLogItemResource(QUOTE_OPTION_ID);
            will(returnValue(importProductLogItemResource2));

            one(importProductLogItemResource2).put(importProductSuccessStatusLogDTO);

            one(quoteOptionResource).getProductImportStatusForQuote(QUOTE_OPTION_ID);
            will(returnValue(importProductInitiatedStatusLogDTO));

            atLeast(2).of(quoteOptionResource).importErrorLogResource(QUOTE_OPTION_ID);
            will(returnValue(importErrorLogResource));

            oneOf(importErrorLogResource).put(anyListOf(ImportProductErrorLogDTO.class));

            oneOf(importErrorLogResource).delete(PRODUCT_CODE);

            oneOf(ecrfSheetOrchestrator).importUsingProduct(with(CUSTOMER_ID),
                                                            with(CONTRACT_ID),
                                                            with(CONTRACT_TERM),
                                                            with(PROJECT_ID),
                                                            with(QUOTE_OPTION_ID),
                                                            with(any(Workbook.class)),
                                                            with(PRODUCT_CODE),
                                                            with(any(ImportResults.class)),
                                                            with(any(Boolean.class)), with(any(ProductCategoryCode.class)));
            will(returnValue(newHashSet(LINE_ITEM_ID)));

            oneOf(expedioServicesFacade).getUserDetails(with("john"));
            will(returnValue(userDTO));

            one(quoteOptionResource).importProductLogItemResource(QUOTE_OPTION_ID);
            will(returnValue(importProductLogItemResource));

            oneOf(importProductLogItemResource).sendEmail(with("aProductCode"), with(userDTO));

        }});
        final Response response = lineItemResourceHandler.importUsingProduct(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID, PRODUCT_CODE, "Add",
                                                                             ECRFSheetOrchestratorTest.class.getResourceAsStream("occ_test_upload.xls"),
                                                                             FormDataContentDisposition
                                                                                 .name("occ_test_upload.xls")
                                                                                 .fileName("occ_test_upload.xls").build(), "H123");
        assertThat(response, aResponse().withStatusOK());
        mockery.assertIsSatisfied();
    }

    @Test
    public void shouldCallECRFSheetOrchestratorWhenBulkImportingFileAndHandleException() throws Exception {
        mockery.checking(new Expectations() {{

            ImportProductStatusLogDTO importProductInitiatedStatusLogDTO = new ImportProductStatusLogDTO(QUOTE_OPTION_ID, null, ImportStatus.Initiated.toString(), "aProductCode", "BATMAN",
                                                                                                         "occ_test_upload.xls");
            ImportProductStatusLogDTO importProductSuccessStatusLogDTO = new ImportProductStatusLogDTO(QUOTE_OPTION_ID, null, ImportStatus.Failed.toString(), "aProductCode", "BATMAN",
                                                                                                       "occ_test_upload.xls");

            one(projectResource).quoteOptionResource(PROJECT_ID);
            will(returnValue(quoteOptionResource));

            one(quoteOptionResource).get(QUOTE_OPTION_ID);
            will(returnValue(QuoteOptionDTOFixture.aQuoteOptionDTO().withCreatedBy("BATMAN").withContractTerm(CONTRACT_TERM).build()));

            one(priceHandlerService).processLineItemsForPricing(with(any(Set.class)), with(any(String.class)), with(any(String.class)), with(any(String.class)), with(any(Boolean.class)), with(any(String.class)));
            will(returnValue(anyMapOf(String.class, PriceClientResponse.class)));

            one(quoteOptionResource).importProductLogItemResource(QUOTE_OPTION_ID);
            will(returnValue(importProductLogItemResource));

            one(importProductLogItemResource).put(importProductInitiatedStatusLogDTO);

            one(quoteOptionResource).put(with(any(QuoteOptionDTO.class)));


            one(quoteOptionResource).importProductLogItemResource(QUOTE_OPTION_ID);
            will(returnValue(importProductLogItemResource2));

            one(importProductLogItemResource2).put(importProductSuccessStatusLogDTO);

            one(quoteOptionResource).getProductImportStatusForQuote(QUOTE_OPTION_ID);
            will(returnValue(importProductInitiatedStatusLogDTO));

            atLeast(2).of(quoteOptionResource).importErrorLogResource(QUOTE_OPTION_ID);
            will(returnValue(importErrorLogResource));

            one(importErrorLogResource).put(with(any(List.class)));

            oneOf(importErrorLogResource).delete(PRODUCT_CODE);
            ImportResults importResults = new ImportResults();
            oneOf(ecrfSheetOrchestrator).importUsingProduct(with(CUSTOMER_ID),
                                                            with(CONTRACT_ID),
                                                            with(CONTRACT_TERM),
                                                            with(PROJECT_ID),
                                                            with(QUOTE_OPTION_ID),
                                                            with(any(Workbook.class)),
                                                            with(PRODUCT_CODE),
                                                            with(importResults),
                                                            with(any(Boolean.class)),
                                                            with(any(ProductCategoryCode.class)));
            will(returnValue(newHashSet()));

            oneOf(expedioServicesFacade).getUserDetails(with("john"));
            will(returnValue(userDTO));

            one(quoteOptionResource).importProductLogItemResource(QUOTE_OPTION_ID);
            will(returnValue(importProductLogItemResource));

            oneOf(importProductLogItemResource).sendEmail(with("aProductCode"), with(any(UserDTO.class)));
        }});
        final Response response = lineItemResourceHandler.importUsingProduct(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID, PRODUCT_CODE, "Add",
                                                                             ECRFSheetOrchestratorTest.class.getResourceAsStream("occ_test_upload.xls"),
                                                                             FormDataContentDisposition
                                                                                 .name("occ_test_upload.xls")
                                                                                 .fileName("occ_test_upload.xls").build(), "H123");
        assertThat(response, aResponse().withStatusOK());
        mockery.assertIsSatisfied();
    }

    @Test
    public void shouldCallECRFSheetOrchestratorWhenImportingFileAndHandleException() throws Exception {
        mockery.checking(new Expectations() {{

            ImportProductStatusLogDTO importProductInitiatedStatusLogDTO = new ImportProductStatusLogDTO(QUOTE_OPTION_ID, LINE_ITEM_ID, ImportStatus.Initiated.toString(), "sCode", "BATMAN",
                                                                                                         "occ_test_upload.xls");
            ImportProductStatusLogDTO importProductSuccessStatusLogDTO = new ImportProductStatusLogDTO(QUOTE_OPTION_ID, LINE_ITEM_ID, ImportStatus.Failed.toString(), "sCode", "BATMAN",
                                                                                                       "occ_test_upload.xls");

            one(projectResource).quoteOptionResource(PROJECT_ID);
            will(returnValue(quoteOptionResource));

            one(quoteOptionResource).get(QUOTE_OPTION_ID);
            will(returnValue(QuoteOptionDTOFixture.aQuoteOptionDTO().withCreatedBy("BATMAN").withMigrationQuote(false).build()));

            one(productInstanceClient).get(new LineItemId(LINE_ITEM_ID));
            will(returnValue(DefaultProductInstanceFixture.aProductInstance().withLineItemId(LINE_ITEM_ID).withProductOffering(ProductOfferingFixture.aProductOffering().withProductIdentifier(new ProductIdentifier("sCode", "version"))).build()));

            one(priceHandlerService).processLineItemsForPricing(with(any(Set.class)), with(any(String.class)), with(any(String.class)), with(any(String.class)), with(any(Boolean.class)), with(any(String.class)));
            will(returnValue(anyMapOf(String.class, PriceClientResponse.class)));

            one(quoteOptionResource).importProductLogItemResource(QUOTE_OPTION_ID);
            will(returnValue(importProductLogItemResource));

            one(importProductLogItemResource).put(importProductInitiatedStatusLogDTO);

            one(quoteOptionResource).importProductLogItemResource(QUOTE_OPTION_ID);
            will(returnValue(importProductLogItemResource2));

            one(importProductLogItemResource2).put(importProductSuccessStatusLogDTO);

            one(quoteOptionResource).getProductImportStatusForQuote(QUOTE_OPTION_ID);
            will(returnValue(importProductInitiatedStatusLogDTO));

            atLeast(2).of(quoteOptionResource).importErrorLogResource(QUOTE_OPTION_ID);
            will(returnValue(importErrorLogResource));

            oneOf(importErrorLogResource).put(with(any(List.class)));

            oneOf(importErrorLogResource).delete("sCode");

            oneOf(ecrfSheetOrchestrator).importUsingLineItem(with(CUSTOMER_ID),
                                                             with(CONTRACT_ID),
                                                             with(PROJECT_ID),
                                                             with(QUOTE_OPTION_ID),
                                                             with(LINE_ITEM_ID),
                                                             with(any(Workbook.class)),
                                                             with(any(ImportResults.class)),
                                                             with(any(String.class)),
                                                             with(any(Boolean.class)),
                                                             with(any(ProductCategoryCode.class)));

            will(returnValue(newHashSet()));

            oneOf(expedioServicesFacade).getUserDetails(with("john"));
            will(returnValue(userDTO));

            one(quoteOptionResource).importProductLogItemResource(QUOTE_OPTION_ID);
            will(returnValue(importProductLogItemResource));

            oneOf(importProductLogItemResource).sendEmail(with("sCode"), with(any(UserDTO.class)));

        }});
        final Response response = lineItemResourceHandler.importUsingLineItem(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID, LINE_ITEM_ID,
                                                                              ECRFSheetOrchestratorTest.class.getResourceAsStream("occ_test_upload.xls"),
                                                                              FormDataContentDisposition
                                                                                  .name("occ_test_upload.xls")
                                                                                  .fileName("occ_test_upload.xls").build());
        assertThat(response, aResponse().withStatusOK());
        mockery.assertIsSatisfied();
    }

    @Test
    public void shouldCallECRFSheetOrchestratorWhenImportingXLSXFile() throws Exception {
        mockery.checking(new Expectations() {{
            ImportProductStatusLogDTO importProductInitiatedStatusLogDTO = new ImportProductStatusLogDTO(QUOTE_OPTION_ID, LINE_ITEM_ID, ImportStatus.Initiated.toString(), "sCode", "BATMAN",
                                                                                                         "occ_test_upload.xls");
            ImportProductStatusLogDTO importProductSuccessStatusLogDTO = new ImportProductStatusLogDTO(QUOTE_OPTION_ID, LINE_ITEM_ID, ImportStatus.Success.toString(), "sCode", "BATMAN",
                                                                                                       "occ_test_upload.xls");
            one(projectResource).quoteOptionResource(PROJECT_ID);
            will(returnValue(quoteOptionResource));

            one(quoteOptionResource).get(QUOTE_OPTION_ID);
            will(returnValue(QuoteOptionDTOFixture.aQuoteOptionDTO().withCreatedBy("BATMAN").withMigrationQuote(false).build()));

            one(priceHandlerService).processLineItemsForPricing(with(any(Set.class)), with(any(String.class)), with(any(String.class)), with(any(String.class)), with(any(Boolean.class)), with(any(String.class)));
            will(returnValue(anyMapOf(String.class, PriceClientResponse.class)));

            one(quoteOptionResource).importProductLogItemResource(QUOTE_OPTION_ID);
            will(returnValue(importProductLogItemResource));

            one(expedioServicesFacade).getUserDetails(UserContextManager.getCurrent().getLoginName());
            will(returnValue(userDTO));

            one(importProductLogItemResource).put(importProductInitiatedStatusLogDTO);

            one(quoteOptionResource).importProductLogItemResource(QUOTE_OPTION_ID);
            will(returnValue(importProductLogItemResource2));

            one(importProductLogItemResource2).put(importProductSuccessStatusLogDTO);

            one(quoteOptionResource).getProductImportStatusForQuote(QUOTE_OPTION_ID);
            will(returnValue(importProductInitiatedStatusLogDTO));

            atLeast(2).of(quoteOptionResource).importErrorLogResource(QUOTE_OPTION_ID);
            will(returnValue(importErrorLogResource));

            oneOf(importErrorLogResource).put(with(any(List.class)));
            oneOf(importErrorLogResource).delete("sCode");

            one(productInstanceClient).get(new LineItemId(LINE_ITEM_ID));
            will(returnValue(DefaultProductInstanceFixture.aProductInstance().withLineItemId(LINE_ITEM_ID).withProductOffering(ProductOfferingFixture.aProductOffering().withProductIdentifier(new ProductIdentifier("sCode", "version"))).build()));

            oneOf(ecrfSheetOrchestrator).importUsingLineItem(with(CUSTOMER_ID),
                                                             with(CONTRACT_ID),
                                                             with(PROJECT_ID),
                                                             with(QUOTE_OPTION_ID),
                                                             with(LINE_ITEM_ID),
                                                             with(any(Workbook.class)),
                                                             with(any(ImportResults.class)),
                                                             with(any(String.class)),
                                                             with(any(Boolean.class)), with(any(ProductCategoryCode.class)));

            will(returnValue(newHashSet(LINE_ITEM_ID)));

            one(quoteOptionResource).importProductLogItemResource(QUOTE_OPTION_ID);
            will(returnValue(importProductLogItemResource));

            oneOf(importProductLogItemResource).sendEmail(with("sCode"), with(any(UserDTO.class)));


        }});
        final Response response = lineItemResourceHandler.importUsingLineItem(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID, LINE_ITEM_ID,
                                                                              ECRFSheetOrchestratorTest.class.getResourceAsStream("occ_test_upload.xlsx"),
                                                                              FormDataContentDisposition
                                                                                  .name("occ_test_upload.xlsx")
                                                                                  .fileName("occ_test_upload.xlsx").build());
        assertThat(response, aResponse().withStatusOK());
        mockery.assertIsSatisfied();
    }

    @Test
    public void shouldRemoveLineItem() throws Exception {
        mockery.checking(new Expectations() {{
            oneOf(projectResource).quoteOptionResource(PROJECT_ID);
            will(returnValue(quoteOptionResource));
            oneOf(quoteOptionResource).quoteOptionItemResource(QUOTE_OPTION_ID);
            will(returnValue(quoteOptionItemResource));
            oneOf(quoteOptionItemResource).get(LINE_ITEM_ID);
            will(returnValue(QuoteOptionItemDTOFixture.aQuoteOptionItemDTO().withStatus(QuoteOptionItemStatus.DRAFT).build()));
            oneOf(productInstanceClient).removeLineItemFromInventory(with(new LineItemId(LINE_ITEM_ID)), with(any(CallbackConditional.class)));
            will(returnValue(newArrayList(new LineItemId(LINE_ITEM_ID))));
            oneOf(quoteOptionItemResource).delete(LINE_ITEM_ID);
        }});

        Response response = lineItemResourceHandler.removeLineItemFromQuoteOption(PROJECT_ID, QUOTE_OPTION_ID, LINE_ITEM_ID);
        assertThat(response.getStatus(), Is.is(200));
    }

    @Test
    public void shouldReturnInternalServerErrorWhenLineItemCanNotBeRemoved() throws Exception {
        mockery.checking(new Expectations() {{
            oneOf(projectResource).quoteOptionResource(PROJECT_ID);
            will(returnValue(quoteOptionResource));
            oneOf(quoteOptionResource).quoteOptionItemResource(QUOTE_OPTION_ID);
            will(returnValue(quoteOptionItemResource));
            oneOf(quoteOptionItemResource).get(LINE_ITEM_ID);
            will(returnValue(QuoteOptionItemDTOFixture.aQuoteOptionItemDTO().withStatus(QuoteOptionItemStatus.DRAFT).build()));
            oneOf(productInstanceClient).removeLineItemFromInventory(with(new LineItemId(LINE_ITEM_ID)), with(any(CallbackConditional.class)));
            will(throwException(new IllegalStateException()));
        }});

        Response response = lineItemResourceHandler.removeLineItemFromQuoteOption(PROJECT_ID, QUOTE_OPTION_ID, LINE_ITEM_ID);
        assertThat(response.getStatus(), Is.is(500));
    }

    @Test
    public void shouldReturnInternalServerErrorWhenLineItemIsLocked() throws Exception {
        mockery.checking(new Expectations() {{
            oneOf(projectResource).quoteOptionResource(PROJECT_ID);
            will(returnValue(quoteOptionResource));
            oneOf(quoteOptionResource).quoteOptionItemResource(QUOTE_OPTION_ID);
            will(returnValue(quoteOptionItemResource));
            oneOf(quoteOptionItemResource).get(LINE_ITEM_ID);
            will(returnValue(QuoteOptionItemDTOFixture.aQuoteOptionItemDTO().withStatus(QuoteOptionItemStatus.ORDER_CREATED).build()));
        }});

        Response response = lineItemResourceHandler.removeLineItemFromQuoteOption(PROJECT_ID, QUOTE_OPTION_ID, LINE_ITEM_ID);
        assertThat(response.getStatus(), Is.is(500));
    }

    @Test
    public void shouldReturnInternalServerErrorWhenLineItemStatusIsNull() throws Exception {
        mockery.checking(new Expectations() {{
            oneOf(projectResource).quoteOptionResource(PROJECT_ID);
            will(returnValue(quoteOptionResource));
            oneOf(quoteOptionResource).quoteOptionItemResource(QUOTE_OPTION_ID);
            will(returnValue(quoteOptionItemResource));
            oneOf(quoteOptionItemResource).get(LINE_ITEM_ID);
            will(returnValue(QuoteOptionItemDTOFixture.aQuoteOptionItemDTO().withStatus(null).build()));
        }});

        Response response = lineItemResourceHandler.removeLineItemFromQuoteOption(PROJECT_ID, QUOTE_OPTION_ID, LINE_ITEM_ID);
        assertThat(response.getStatus(), Is.is(500));
    }

    @Test
    public void shouldReturnInternalServerErrorWhenLineItemStatusIsOffered() throws Exception {
        mockery.checking(new Expectations() {{
            oneOf(projectResource).quoteOptionResource(PROJECT_ID);
            will(returnValue(quoteOptionResource));
            oneOf(quoteOptionResource).quoteOptionItemResource(QUOTE_OPTION_ID);
            will(returnValue(quoteOptionItemResource));
            oneOf(quoteOptionItemResource).get(LINE_ITEM_ID);
            will(returnValue(QuoteOptionItemDTOFixture.aQuoteOptionItemDTO().withStatus(QuoteOptionItemStatus.OFFERED).build()));
        }});

        Response response = lineItemResourceHandler.removeLineItemFromQuoteOption(PROJECT_ID, QUOTE_OPTION_ID, LINE_ITEM_ID);
        assertThat(response.getStatus(), Is.is(500));
    }

    @Test
    public void shouldReturnInternalServerErrorForIFCLineItem() throws Exception {
        mockery.checking(new Expectations() {{
            oneOf(projectResource).quoteOptionResource(PROJECT_ID);
            will(returnValue(quoteOptionResource));
            oneOf(quoteOptionResource).quoteOptionItemResource(QUOTE_OPTION_ID);
            will(returnValue(quoteOptionItemResource));
            oneOf(quoteOptionItemResource).get(LINE_ITEM_ID);
            will(returnValue(QuoteOptionItemDTOFixture.aQuoteOptionItemDTO().withStatus(QuoteOptionItemStatus.DRAFT).withIFC(true).build()));
        }});

        Response response = lineItemResourceHandler.removeLineItemFromQuoteOption(PROJECT_ID, QUOTE_OPTION_ID, LINE_ITEM_ID);
        assertThat(response.getStatus(), Is.is(500));
    }
}
