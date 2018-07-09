package com.bt.rsqe.projectengine.web.quoteoptiondetails;

import com.bt.rsqe.AttachmentDTOFixture;
import com.bt.rsqe.container.ApplicationConfig;
import com.bt.rsqe.customerrecord.CustomerResource;
import com.bt.rsqe.customerrecord.SiteDTO;
import com.bt.rsqe.domain.AttachmentDTO;
import com.bt.rsqe.domain.QuoteOptionItemStatus;
import com.bt.rsqe.dto.NoteDto;
import com.bt.rsqe.emppal.attachmentresource.AttachmentManager;
import com.bt.rsqe.emppal.attachmentresource.EmpPalClientResources;
import com.bt.rsqe.emppal.attachmentresource.client.EmpPalFacadeClientConfig;
import com.bt.rsqe.expedio.project.ExpedioProjectResource;
import com.bt.rsqe.fixtures.CalendarFixture;
import com.bt.rsqe.inlife.client.ApplicationCapabilityProvider;
import com.bt.rsqe.inlife.client.ApplicationPropertyResourceClient;
import com.bt.rsqe.inlife.client.dto.ApplicationProperty;
import com.bt.rsqe.projectengine.AttachmentViewDTO;
import com.bt.rsqe.projectengine.QuoteOptionDTO;
import com.bt.rsqe.projectengine.web.facades.LineItemFacade;
import com.bt.rsqe.projectengine.web.facades.ProductIdentifierFacade;
import com.bt.rsqe.projectengine.web.facades.QuoteOptionFacade;
import com.bt.rsqe.projectengine.web.facades.UserFacade;
import com.bt.rsqe.projectengine.web.model.LineItemModel;
import com.bt.rsqe.projectengine.web.quoteoptionpricing.PriceSuppressStrategy;
import com.bt.rsqe.projectengine.web.uri.UriFactory;
import com.bt.rsqe.projectengine.web.view.NotesView;
import com.bt.rsqe.projectengine.web.view.Products;
import com.bt.rsqe.projectengine.web.view.QuoteOptionDetailsDTO;
import com.bt.rsqe.projectengine.web.view.QuoteOptionDetailsView;
import com.bt.rsqe.projectengine.web.view.filtering.PaginatedAttachmentDialogFilter;
import com.bt.rsqe.projectengine.web.view.filtering.PaginatedFilter;
import com.bt.rsqe.projectengine.web.view.filtering.PaginatedFilterResult;
import com.bt.rsqe.projectengine.web.view.pagination.DefaultPagination;
import com.bt.rsqe.projectengine.web.view.pagination.Pagination;
import com.bt.rsqe.projectengine.web.view.sorting.PaginatedSort;
import com.bt.rsqe.projectengine.web.view.sorting.PaginatedSortResult;
import com.bt.rsqe.security.UserDTO;
import com.bt.rsqe.security.UserType;
import com.bt.rsqe.utils.RSQEMockery;
import com.bt.rsqe.web.rest.ProxyAwareRestRequestBuilder;
import com.bt.rsqe.web.rest.RestResource;
import com.bt.rsqe.web.rest.dto.types.JaxbDateTime;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.jmock.Mockery;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;

import java.io.IOException;
import java.net.URI;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.bt.rsqe.expedio.fixtures.ProjectDTOFixture.*;
import static com.google.common.collect.Lists.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class QuoteOptionDetailsOrchestratorTest {
    private static final String CUSTOMER_ID = "1234";
    private static final String CONTRACT_ID = "contractId";
    private static final String PROJECT_ID = "projectId";
    private static final String QUOTE_OPTION_ID = "quoteOptionId";
    private static final String CATEGORY_ID = "Sales";
    private static final String BID_MANAGER_CATEGORY_ID = "BidManager";
    private static final String COST_DOCUMENT_CATEGORY_ID = "CostDocument";
    private static final String DOCUMENT_ID = "documentId";
    private static final String PARENT_PATH = "parentPath";
    private static final String FILE_NAME = "fileName.txt";
    private static final String FILE_NAME_WITH_PATH = "parentPath/fileName.txt";

    public static final String CURRENCY = "USD";
    private QuoteOptionDetailsOrchestrator quoteOptionDetailsOrchestrator;
    private ProductIdentifierFacade productIdentifierFacade;
    private LineItemFacade lineItemFacade;
    private UserFacade userFacade;
    private QuoteOptionFacade quoteOptionFacade;
    private ExpedioProjectResource projectResource;
    private UriFactory mockUriFactory;
    private PaginatedFilter paginatedFilter;
    private PaginatedSort paginatedSort;
    private PaginatedFilterResult paginatedFilterResult = mock(PaginatedFilterResult.class);
    private PaginatedSortResult paginatedSortResult = mock(PaginatedSortResult.class);
    private EmpPalFacadeClientConfig empPalFacadeClientConfig = mock(EmpPalFacadeClientConfig.class);

    private EmpPalClientResources empPalClientResources;
    private Pagination pagination = new DefaultPagination(1, 0, 10);

    private CustomerResource customerResource;
    private URI uri;
    private Mockery mockery;
    private RestResource restResource;
    private AttachmentManager attachmentManager;
    private ApplicationCapabilityProvider capabilityProvider;
    private ApplicationPropertyResourceClient applicationPropertyResourceClient;
    final byte[] fileContent = {1, 2, 3};

    @Before
    public void before() throws Exception {
        lineItemFacade = mock(LineItemFacade.class);
        userFacade = mock(UserFacade.class);
        productIdentifierFacade = mock(ProductIdentifierFacade.class);
        quoteOptionFacade = mock(QuoteOptionFacade.class);
        projectResource = mock(ExpedioProjectResource.class);
        empPalClientResources = mock(EmpPalClientResources.class);
        ApplicationConfig applicationConfig = mock(ApplicationConfig.class);
        when(empPalFacadeClientConfig.getApplicationConfig()).thenReturn(applicationConfig);

        final QuoteOptionDTO quoteOptionDTO = new QuoteOptionDTO();
        quoteOptionDTO.currency = CURRENCY;
        when(quoteOptionFacade.get(PROJECT_ID, QUOTE_OPTION_ID)).thenReturn(quoteOptionDTO);
        mockUriFactory = mock(UriFactory.class);
        paginatedFilter = mock(PaginatedFilter.class);
        paginatedSort = mock(PaginatedSort.class);
        mockery = new RSQEMockery();
        restResource = mockery.mock(RestResource.class);
        attachmentManager = mock(AttachmentManager.class);
        uri = new URI("uri");
        customerResource = new CustomerResource(new ProxyAwareRestRequestBuilder(uri) {
            @Override
            protected RestResource build(URI uri) {
                QuoteOptionDetailsOrchestratorTest.this.uri = uri;

                return restResource;
            }
        }, uri, "secret");
        capabilityProvider = mock(ApplicationCapabilityProvider.class);
        when(capabilityProvider.isFunctionalityEnabled(ApplicationCapabilityProvider.Capability.ALLOW_COPY_OPTION_ITEMS, false, null)).thenReturn(true);
        applicationPropertyResourceClient = mock(ApplicationPropertyResourceClient.class);
        when(applicationPropertyResourceClient.getApplicationProperty("maxConfigurableLineItems", "30", QUOTE_OPTION_ID)).thenReturn(new ApplicationProperty("maxConfigurableLineItems", "20"));
        quoteOptionDetailsOrchestrator = new QuoteOptionDetailsOrchestrator(lineItemFacade, mockUriFactory,
                                                                            productIdentifierFacade,
                                                                            userFacade,
                                                                            quoteOptionFacade,
                                                                            projectResource,
                                                                            attachmentManager,
                                                                            capabilityProvider,
                                                                            applicationPropertyResourceClient);
    }

    @Test
    public void shouldAddNoLineItemsGivenThereAreNoLineItems() throws Exception {
        final ArrayList<LineItemModel> lineItemModels = new ArrayList<LineItemModel>();
        PaginatedFilterResult filterResult = mock(PaginatedFilterResult.class);
        when(lineItemFacade.fetchLineItems(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID, null, true, PriceSuppressStrategy.None)).thenReturn(lineItemModels);
        when(paginatedFilter.applyTo(lineItemModels)).thenReturn(paginatedFilterResult);
        when(paginatedFilterResult.getItems()).thenReturn(Lists.newArrayList());
        when(paginatedSortResult.getPaginatedFilterResultItems()).thenReturn(filterResult);
        when(paginatedSortResult.getPaginatedFilterResultItems().getItems()).thenReturn(Lists.newArrayList());
        when(paginatedSort.applyTo(Matchers.<PaginatedFilterResult>any())).thenReturn(paginatedSortResult);

        final QuoteOptionDetailsDTO quoteOptionDetailsDTO = quoteOptionDetailsOrchestrator.buildJsonResponse(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID,
                                                                                                             QUOTE_OPTION_ID,
                                                                                                             paginatedFilter, paginatedSort, "", "");
        assertThat(quoteOptionDetailsDTO.getItems().size(), is(0));
    }

    @Test
    public void shouldReturnPaginatedLineItems() throws Exception {
        LineItemModel lineItem1 = mock(LineItemModel.class);
        LineItemModel lineItem2 = mock(LineItemModel.class);
        PaginatedFilterResult filterResult = mock(PaginatedFilterResult.class);

        final ArrayList<LineItemModel> lineItemModels = newArrayList(lineItem1, lineItem2);
        when(lineItemFacade.fetchVisibleLineItems(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID, null, true, PriceSuppressStrategy.None)).thenReturn(lineItemModels);
        when(paginatedFilter.applyTo(lineItemModels)).thenReturn(paginatedFilterResult);
        when(paginatedSort.applyTo(Matchers.<PaginatedFilterResult>any())).thenReturn(paginatedSortResult);
        when(paginatedFilterResult.getItems()).thenReturn(Lists.newArrayList(lineItem2));
        when(paginatedSortResult.getPaginatedFilterResultItems()).thenReturn(filterResult);
        when(paginatedSortResult.getPaginatedFilterResultItems().getItems()).thenReturn(Lists.newArrayList(lineItem2));
        when(lineItem1.getStatus()).thenReturn(QuoteOptionItemStatus.INITIALIZING.getDescription());
        when(lineItem2.getStatus()).thenReturn(QuoteOptionItemStatus.INITIALIZING.getDescription());
        when(lineItem1.getSite()).thenReturn(SiteDTO.CUSTOMER_OWNED);
        when(lineItem2.getSite()).thenReturn(SiteDTO.CUSTOMER_OWNED);

        QuoteOptionDetailsDTO quoteOptionDetailsDTO = quoteOptionDetailsOrchestrator.buildJsonResponse(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID, paginatedFilter, paginatedSort, "", "");

        assertThat(quoteOptionDetailsDTO.pageNumber, is(0));
        assertThat(quoteOptionDetailsDTO.totalDisplayRecords, is(0));
        assertThat(quoteOptionDetailsDTO.totalRecords, is(2));
    }

    @Test
    public void shouldGetProductListFromProductFacade() throws Exception {

        final Products products = new Products();

        when(projectResource.getProject(PROJECT_ID)).thenReturn(aProjectDTO().build());
        when(productIdentifierFacade.getAllSellableProducts()).thenReturn(products);

        final QuoteOptionDetailsView view = quoteOptionDetailsOrchestrator.buildView(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID);

        assertThat(view.getProducts(), is(products));
    }

    @Test
    public void shouldGetCurrencyFromQuoteOption() throws Exception {
        when(projectResource.getProject(PROJECT_ID)).thenReturn(aProjectDTO().build());
        when(productIdentifierFacade.getAllSellableProducts()).thenReturn(new Products());

        final QuoteOptionDetailsView view = quoteOptionDetailsOrchestrator.buildView(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID);

        assertThat(view.getCurrency(), is(CURRENCY));
    }

    @Test
    public void shouldGetMaxConfigurableLineItems() throws Exception {
        when(projectResource.getProject(PROJECT_ID)).thenReturn(aProjectDTO().build());
        when(productIdentifierFacade.getAllSellableProducts()).thenReturn(new Products());

        final QuoteOptionDetailsView view = quoteOptionDetailsOrchestrator.buildView(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID);

        assertThat(view.getMaxConfigurableLineItems(), is(20));
    }

    @Test
    public void shouldGetRemoveLineItemAllowedValue() throws Exception {
        when(capabilityProvider.isFunctionalityEnabled(ApplicationCapabilityProvider.Capability.REMOVE_LINE_ITEM_ALLOWED, true, Optional.of(QUOTE_OPTION_ID))).thenReturn(true);

        when(projectResource.getProject(PROJECT_ID)).thenReturn(aProjectDTO().build());
        when(productIdentifierFacade.getAllSellableProducts()).thenReturn(new Products());

        final QuoteOptionDetailsView view = quoteOptionDetailsOrchestrator.buildView(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID);

        assertThat(view.getRemoveLineItemAllowed(), is("true"));
    }

    @Test
    public void shouldBuildNoteView() throws Exception {
        List<NoteDto> notes = newArrayList(new NoteDto("note text", null, "createdBy"));

        when(userFacade.findUser("createdBy")).thenReturn(new UserDTO("forename", "surname", "", UserType.DIRECT, "02890232333","loginname","1234"));

        NotesView notesView = quoteOptionDetailsOrchestrator.buildNoteView(notes);
        final NotesView.NoteView actual = notesView.getNotes().get(0);
        assertThat(actual.getCreatedBy(), is("forename surname"));
        assertThat(actual.getNoteText(), is("note text"));
        assertThat(notesView.getLastCreatedNoteText(), is("note text"));
    }

    @Test
    public void shouldLoadCostAttachmentList() throws IOException, InvalidFormatException, ParseException {
        setExpectations();
        PaginatedAttachmentDialogFilter paginatedAttachmentDialogFilter = new PaginatedAttachmentDialogFilter(pagination);
        AttachmentViewDTO attachmentViewDTO = quoteOptionDetailsOrchestrator.loadAttachmentTable(CUSTOMER_ID, PROJECT_ID, BID_MANAGER_CATEGORY_ID, paginatedAttachmentDialogFilter
        );
        assertThat(attachmentViewDTO.itemDTOs.size(), is(1));
        String category = attachmentViewDTO.getItemDTOs().get(0).getUploadAppliesTo();
        String fileName = attachmentViewDTO.getItemDTOs().get(0).getUploadFileName();
        assertThat(category, is(COST_DOCUMENT_CATEGORY_ID));
        assertThat(fileName, is(FILE_NAME));
    }

    private void setExpectations() {
        List<AttachmentDTO> attachmentdtos = newArrayList();
        AttachmentDTO attachmentDTO = AttachmentDTOFixture.attachmentDTO()
                                                          .withDocumentId("documentId1")
                                                          .withDocumentTitle("documentTitle")
                                                          .withFileContent(new byte[]{1, 2, 3})
                                                          .withFileName("fileName.txt")
            .withCreatedDate(JaxbDateTime.valueOf(new DateTime(new Date())))
                                                          .withParentPath("SampleSalesOrganization/1-5000/1234/Sales/projectId")
                                                          .build();
        attachmentdtos.add(attachmentDTO);
        when(attachmentManager.getAttachmentParentPath(CATEGORY_ID, CUSTOMER_ID, PROJECT_ID)).thenReturn(PARENT_PATH);
        when(attachmentManager.getAttachmentParentPath(BID_MANAGER_CATEGORY_ID, CUSTOMER_ID, PROJECT_ID)).thenReturn(PARENT_PATH);
        when(attachmentManager.loadAttachments(CATEGORY_ID, CUSTOMER_ID, PROJECT_ID, PARENT_PATH)).thenReturn(attachmentdtos);
        when(attachmentManager.loadAttachments(BID_MANAGER_CATEGORY_ID, CUSTOMER_ID, PROJECT_ID, PARENT_PATH)).thenReturn(attachmentdtos);
        when(attachmentManager.downloadAttachment(DOCUMENT_ID,CATEGORY_ID,CUSTOMER_ID,PROJECT_ID, StringUtils.EMPTY, StringUtils.EMPTY)).thenReturn(attachmentDTO);
        when(attachmentManager.acceptedFileSize(3)).thenReturn(true);
        quoteOptionDetailsOrchestrator = new QuoteOptionDetailsOrchestrator(lineItemFacade, mockUriFactory,
                                                                            productIdentifierFacade,
                                                                            userFacade,
                                                                            quoteOptionFacade,
                                                                            projectResource,
                                                                            attachmentManager, null, null);
        PaginatedFilterResult result = mock(PaginatedFilterResult.class);
        when(result.getItems()).thenReturn(Lists.newArrayList(attachmentDTO));
        when(result.getPageNumber()).thenReturn(1);
        when(result.getFilteredSize()).thenReturn(1);
        when(result.getTotalRecords()).thenReturn(1);
        when(paginatedFilter.applyTo(Matchers.<List>any())).thenReturn(result);

    }

    @Test
    public void shouldUploadAttachmentSuccessfully() throws IOException, InvalidFormatException {
        setExpectations();
        quoteOptionDetailsOrchestrator.uploadAttachment(CUSTOMER_ID,PROJECT_ID,CATEGORY_ID,
                                                                            "fileName.txt",new byte[]{1, 2, 3}
        );
        verify(attachmentManager).uploadAttachment(CUSTOMER_ID,PROJECT_ID,CATEGORY_ID,
                                                                            "fileName.txt",new byte[]{1, 2, 3}, StringUtils.EMPTY);
    }

    @Test
    public void shouldDownloadAttachmentSuccessfully() throws IOException, InvalidFormatException {
        setExpectations();
        AttachmentDTO expectedAttachmentDTO = AttachmentDTOFixture.attachmentDTO()
                                                                  .withDocumentId("documentId")
                                                                  .withDocumentTitle("documentTitle")
                                                                  .withFileContent(new byte[]{1, 2, 3})
                                                                  .withFileName("fileName")
                                                                  .withParentPath("parentPath")
                                                                  .build();

        AttachmentDTO attachmentResultDTO = quoteOptionDetailsOrchestrator.downloadAttachment(DOCUMENT_ID,
                                                                                              CATEGORY_ID,
                                                                                              CUSTOMER_ID,
                                                                                              PROJECT_ID
        );
        assertThat(attachmentResultDTO.getFileContent(), is(expectedAttachmentDTO.getFileContent()));
    }

    @Test
    public void shouldDeleteAttachmentSuccessfully() throws IOException, InvalidFormatException {
        setExpectations();
        quoteOptionDetailsOrchestrator.deleteAttachment(DOCUMENT_ID,
                                                        CATEGORY_ID,
                                                        CUSTOMER_ID,
                                                        PROJECT_ID
        );
        verify(attachmentManager).deleteAttachment(DOCUMENT_ID,CATEGORY_ID,CUSTOMER_ID, PROJECT_ID, "");
    }

    @Test
    public void shouldReturnErrorMsgIfFileSizeIsGreaterThan10MB() throws IOException, InvalidFormatException {
        setExpectations();
        final String response = quoteOptionDetailsOrchestrator.uploadAttachment(CUSTOMER_ID,PROJECT_ID,CATEGORY_ID,
                                                                            "fileName.txt",new byte[149323776]
        );
        assertThat(response,is(QuoteOptionDetailsOrchestrator.FILE_SIZE_VALIDATION_MESSAGE));

    }

    @Test
    public void shouldGetAttachmentUrl() throws Exception {
        when(projectResource.getProject(PROJECT_ID)).thenReturn(aProjectDTO().build());
        when(productIdentifierFacade.getAllSellableProducts()).thenReturn(new Products());

        final QuoteOptionDetailsView view = quoteOptionDetailsOrchestrator.buildView(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID);

        assertThat(view.getAttachmentUrl(), is("/rsqe/customers/1234/contracts/contractId/projects/projectId/quote-options/quoteOptionId/attachments/form?isCostAttachmentDialog=false"));
    }

    @Test
    public void shouldLoadAttachmentList() throws IOException, InvalidFormatException {
        setExpectations();
        AttachmentViewDTO attachmentViewDTO = quoteOptionDetailsOrchestrator.loadAttachmentTable(CUSTOMER_ID, PROJECT_ID, CATEGORY_ID, paginatedFilter
        );
        assertThat(attachmentViewDTO.itemDTOs.size(), is(1));
    }

    @Test
    public void shouldSortAttachmentsByUploadedDate() {
        DateTime date1 = new DateTime(CalendarFixture.aCalendar().day(1).month(CalendarFixture.Month.JAN).year(2011).get().getTime());
        DateTime date2 = new DateTime(CalendarFixture.aCalendar().day(1).month(CalendarFixture.Month.JAN).year(2012).get().getTime());
        DateTime date3 = new DateTime(CalendarFixture.aCalendar().day(1).month(CalendarFixture.Month.JAN).year(2013).get().getTime());
        DateTime date4 = new DateTime(CalendarFixture.aCalendar().day(1).month(CalendarFixture.Month.JAN).year(2014).get().getTime());
        DateTime date5 = new DateTime(CalendarFixture.aCalendar().day(1).month(CalendarFixture.Month.JAN).year(2015).get().getTime());

        AttachmentDTO attachmentDTO3 = AttachmentDTOFixture.attachmentDTO()
                                                          .withDocumentId("")
                                                          .withDocumentTitle("fileName.txt")
                                                          .withFileContent(fileContent)
                                                          .withFileName("fileName3.txt")
                                                          .withParentPath(PARENT_PATH)
                                                          .withCreatedDate(JaxbDateTime.valueOf(date3))
                                                          .build();
        AttachmentDTO attachmentDTO1 = AttachmentDTOFixture.attachmentDTO()
                                                          .withDocumentId("")
                                                          .withDocumentTitle("fileName.txt")
                                                          .withFileContent(fileContent)
                                                          .withFileName("fileName1.txt")
                                                          .withParentPath(PARENT_PATH)
                                                          .withCreatedDate(JaxbDateTime.valueOf(date1))
                                                          .build();
        AttachmentDTO attachmentDTO5 = AttachmentDTOFixture.attachmentDTO()
                                                          .withDocumentId("")
                                                          .withDocumentTitle("fileName.txt")
                                                          .withFileContent(fileContent)
                                                          .withFileName("fileName5.txt")
                                                          .withParentPath(PARENT_PATH)
                                                          .withCreatedDate(JaxbDateTime.valueOf(date5))
                                                          .build();
        AttachmentDTO attachmentDTO2 = AttachmentDTOFixture.attachmentDTO()
                                                          .withDocumentId("")
                                                          .withDocumentTitle("fileName.txt")
                                                          .withFileContent(fileContent)
                                                          .withFileName("fileName2.txt")
                                                          .withParentPath(PARENT_PATH)
                                                          .withCreatedDate(JaxbDateTime.valueOf(date2))
                                                          .build();
        AttachmentDTO attachmentDTO4 = AttachmentDTOFixture.attachmentDTO()
                                                          .withDocumentId("")
                                                          .withDocumentTitle("fileName.txt")
                                                          .withFileContent(fileContent)
                                                          .withFileName("fileName4.txt")
                                                          .withParentPath(PARENT_PATH)
                                                          .withCreatedDate(JaxbDateTime.valueOf(date4))
                                                          .build();
        List<AttachmentDTO> attachmentDTOList = newArrayList(attachmentDTO3, attachmentDTO1, attachmentDTO5, attachmentDTO2, attachmentDTO4);

        List<AttachmentViewDTO.ItemRowDTO> attachmentItemRowDtos = quoteOptionDetailsOrchestrator.buildCostAttachmentView(attachmentDTOList);
        assertThat(attachmentItemRowDtos.size(), is(5));
        assertTrue(attachmentItemRowDtos.get(0).getUploadDate().contains(String.valueOf(attachmentDTO5.getCreatedDate().getYear())));
        assertTrue(attachmentItemRowDtos.get(1).getUploadDate().contains(String.valueOf(attachmentDTO4.getCreatedDate().getYear())));
        assertTrue(attachmentItemRowDtos.get(2).getUploadDate().contains(String.valueOf(attachmentDTO3.getCreatedDate().getYear())));
        assertTrue(attachmentItemRowDtos.get(3).getUploadDate().contains(String.valueOf(attachmentDTO2.getCreatedDate().getYear())));
        assertTrue(attachmentItemRowDtos.get(4).getUploadDate().contains(String.valueOf(attachmentDTO1.getCreatedDate().getYear())));
    }
}
