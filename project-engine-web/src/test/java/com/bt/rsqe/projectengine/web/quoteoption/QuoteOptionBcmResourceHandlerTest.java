package com.bt.rsqe.projectengine.web.quoteoption;

import com.bt.rsqe.dto.BidManagerCommentsDTO;
import com.bt.rsqe.expedio.services.CloseBidManagerActivityDTO;
import com.bt.rsqe.projectengine.LineItemDiscountStatus;
import com.bt.rsqe.projectengine.ProjectDTO;
import com.bt.rsqe.projectengine.ProjectResource;
import com.bt.rsqe.projectengine.QuoteOptionDTO;
import com.bt.rsqe.projectengine.QuoteOptionItemDTO;
import com.bt.rsqe.projectengine.QuoteOptionItemResource;
import com.bt.rsqe.projectengine.QuoteOptionResource;
import com.bt.rsqe.projectengine.web.QuoteOptionBcmResourceHandler;
import com.bt.rsqe.projectengine.web.facades.BidManagerCommentsFacade;
import com.bt.rsqe.projectengine.web.facades.ExpedioServicesFacade;
import com.bt.rsqe.projectengine.web.facades.UserFacade;
import com.bt.rsqe.projectengine.web.model.bcmspreadsheet.InvalidExportDataException;
import com.bt.rsqe.projectengine.web.quoteoption.bcmsheet.BCMExportOrchestrator;
import com.bt.rsqe.security.UserContext;
import com.bt.rsqe.security.UserContextManager;
import com.bt.rsqe.security.UserDTO;
import com.bt.rsqe.security.UserType;
import com.bt.rsqe.utils.JSONSerializer;
import com.bt.rsqe.web.AjaxResponseDTO;
import com.bt.rsqe.web.Presenter;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.OfficeXmlFileException;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static com.bt.rsqe.matchers.ResponseMatcher.*;
import static com.bt.rsqe.projectengine.web.QuoteOptionDTOFixture.*;
import static com.google.common.collect.Lists.newArrayList;
import static javax.ws.rs.core.Response.Status.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.*;
import static org.hamcrest.core.IsNull.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class QuoteOptionBcmResourceHandlerTest {
    private static final String PROJECT_ID = "PROJECT_ID";
    private static final String QUOTE_OPTION_ID = "QUOTE_OPTION_ID";
    private static final String CUSTOMER_ID = "customerId";
    private static final String CONTRACT_ID = "CONTRACT_ID";
    private static final String PROJECT_NAME = "PROJECTNAME";
    private static final String APPROVED = "true";
    private QuoteOptionBcmResourceHandler quoteOptionBcmResourceHandler;
    private Presenter mockPresenter;
    private QuoteOptionBcmExportPricingSheetOrchestrator quoteOptionBcmExportPricingSheetOrchestrator;
    private QuoteOptionBcmSheetExportOrchestrator quoteOptionBcmSheetExportOrchestrator;
    private QuoteOptionBcmSheetImportOrchestrator quoteOptionBcmSheetImportOrchestrator;
    private QuoteOptionBcmPricingSheetOrchestrator quoteOptionBcmPricingSheetOrchestrator;
    private BCMExportOrchestrator bcmExportOrchestrator;
    private ExpedioServicesFacade expedioServiceFacade;
    private ProjectResource projectsResource;
    private QuoteOptionResource quoteOptionResource;
    private QuoteOptionDTO quoteOptionDto;
    private ProjectDTO projectDTO;
    private QuoteOptionItemResource quoteOptionItemResource;
    private BidManagerCommentsFacade bidManagerCommentsFacade;
    private UserFacade userFacade;

    @Before
    public void before() {
        quoteOptionBcmExportPricingSheetOrchestrator = mock(QuoteOptionBcmExportPricingSheetOrchestrator.class);
        quoteOptionBcmPricingSheetOrchestrator = mock(QuoteOptionBcmPricingSheetOrchestrator.class);
        expedioServiceFacade = mock(ExpedioServicesFacade.class);
        projectsResource = mock(ProjectResource.class);
        quoteOptionResource = mock(QuoteOptionResource.class);
        mockPresenter = mock(Presenter.class);
        quoteOptionBcmSheetExportOrchestrator=mock(QuoteOptionBcmSheetExportOrchestrator.class);
        quoteOptionBcmSheetImportOrchestrator=mock(QuoteOptionBcmSheetImportOrchestrator.class);
        bcmExportOrchestrator=mock(BCMExportOrchestrator.class);
        bidManagerCommentsFacade=mock(BidManagerCommentsFacade.class);
        userFacade= mock(UserFacade.class);

        quoteOptionBcmResourceHandler = new QuoteOptionBcmResourceHandler(mockPresenter,
                                                                          quoteOptionBcmExportPricingSheetOrchestrator,
                                                                          quoteOptionBcmPricingSheetOrchestrator,
                                                                          projectsResource,
                                                                          expedioServiceFacade,
                                                                          quoteOptionBcmSheetExportOrchestrator,
                                                                          quoteOptionBcmSheetImportOrchestrator,
                                                                          bcmExportOrchestrator,
                                                                          bidManagerCommentsFacade);


        quoteOptionDto = aQuoteOptionDTO().withId("DGH45-DFG6FD-67FDWH-Y65DFH")
            .withName("quote option 1")
            .withCurrency("GBP")
            .withContractTerm("12")
            .withCreationDate(new DateTime().toString())
            .withActivityId("123456").build();

        projectDTO = new ProjectDTO(PROJECT_ID, PROJECT_NAME, CUSTOMER_ID, CONTRACT_ID);
        when(projectsResource.quoteOptionResource(PROJECT_ID)).thenReturn(quoteOptionResource);
        quoteOptionItemResource = mock(QuoteOptionItemResource.class);
        when(quoteOptionResource.quoteOptionItemResource(QUOTE_OPTION_ID)).thenReturn(quoteOptionItemResource);
        when(quoteOptionItemResource.get()).thenReturn(new LinkedList<QuoteOptionItemDTO>());
        when(bidManagerCommentsFacade.getUserFacade()).thenReturn(userFacade);
        UserContext userContext = new UserContext("loginName","token","sellingChannel");
        UserContextManager.setCurrent(userContext);
        UserDTO userDTO = new UserDTO("foreName","surName","mail", UserType.DIRECT,"phone","loginName","ein");
        when(userFacade.findUser("loginName")).thenReturn(userDTO);
    }

    @Test
    public void shouldExportExcelSpreadsheet() throws Exception {
        when(projectsResource.get(PROJECT_ID)).thenReturn(projectDTO);
        when(quoteOptionResource.get(QUOTE_OPTION_ID)).thenReturn(QuoteOptionDTO.newInstance("QUOTE_OPTION_ID", "QuoteName", "GBP", "contractTerm", "createdBy"));
        quoteOptionBcmResourceHandler.getBCMExportSheet(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID,"YES","");
        verify(bcmExportOrchestrator).renderBCMExportSheet(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID, "");
    }

    @Test
    public void shouldImportExcelSpreadsheet() throws Exception {
        final CloseBidManagerActivityDTO closeRequest = new CloseBidManagerActivityDTO("TODO", null, PROJECT_ID, null);
        when(quoteOptionResource.get(QUOTE_OPTION_ID)).thenReturn(quoteOptionDto);

        assertThat(quoteOptionDto.activityId, is(notNullValue()));

        Response response = quoteOptionBcmResourceHandler.importBcmSheet(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID, anInputStream());
        verify(quoteOptionBcmSheetImportOrchestrator).importBCMSheetDetails(eq(CUSTOMER_ID), eq(CONTRACT_ID), eq(PROJECT_ID), eq(QUOTE_OPTION_ID), any(HSSFWorkbook.class));

        assertThat(response, is(aResponse().withStatus(OK)));

        AjaxResponseDTO dto = JSONSerializer.getInstance().deSerialize((String) response.getEntity(), AjaxResponseDTO.class);
        assertTrue(dto.successful());
        assertThat(dto.errors(), is(""));
    }

    @Test
    public void shouldApproveDiscounts() throws Exception {
        when(quoteOptionResource.get(QUOTE_OPTION_ID)).thenReturn(quoteOptionDto);

        Response response = quoteOptionBcmResourceHandler.approveDiscounts(CUSTOMER_ID, PROJECT_ID, QUOTE_OPTION_ID, "", "");

        verify(expedioServiceFacade,times(1)).closeBidManagerDiscountApprovalRequestActivity(Matchers.<CloseBidManagerActivityDTO>any());
        verify(quoteOptionResource).put(quoteOptionDto);
        assertThat(response, is(aResponse().withStatus(OK)));
    }

    @Test
    public void shouldRejectDiscounts() throws Exception {
        List<BidManagerCommentsDTO> bidManagerCommentsDTOList = newArrayList();
        final CloseBidManagerActivityDTO closeRequest = new CloseBidManagerActivityDTO(bidManagerCommentsDTOList, "TODO", "123456", PROJECT_ID);

        when(quoteOptionResource.get(QUOTE_OPTION_ID)).thenReturn(quoteOptionDto);

        assertThat(quoteOptionDto.activityId, is(notNullValue()));
        Response response = quoteOptionBcmResourceHandler.rejectDiscounts(CUSTOMER_ID, PROJECT_ID, QUOTE_OPTION_ID, "RejectComments");
        assertThat(quoteOptionDto.activityId, is(nullValue()));
        assertThat(response, is(aResponse().withStatus(OK)));

        verify(quoteOptionResource).put(quoteOptionDto);
        verify(expedioServiceFacade).closeBidManagerDiscountApprovalRequestActivity(closeRequest);
        verify(quoteOptionBcmPricingSheetOrchestrator).rejectDiscounts(PROJECT_ID, QUOTE_OPTION_ID);

    }

    @Test
    public void shouldReturnErrorIfImportThrowsInvalidDataException() throws Exception {

        doThrow(new InvalidExportDataException("There was an error"))
            .when(quoteOptionBcmSheetImportOrchestrator).importBCMSheetDetails(eq(CUSTOMER_ID), eq(CONTRACT_ID), eq(PROJECT_ID), eq(QUOTE_OPTION_ID), any(HSSFWorkbook.class));
        Response response = quoteOptionBcmResourceHandler.importBcmSheet(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID, anInputStream());

        assertThat(response, is(aResponse().withStatus(OK)));
        AjaxResponseDTO dto = JSONSerializer.getInstance().deSerialize((String) response.getEntity(), AjaxResponseDTO.class);
        assertFalse(dto.successful());
        assertThat(dto.errors(), is("There was an error"));
    }

    @Test
    public void shouldReturnErrorIfRFOSheetUploadedInAnIncorrectVersion() throws Exception {
        doThrow(new OfficeXmlFileException("There was an error"))
            .when(quoteOptionBcmSheetImportOrchestrator).importBCMSheetDetails(eq(CUSTOMER_ID), eq(CONTRACT_ID), eq(PROJECT_ID), eq(QUOTE_OPTION_ID), any(HSSFWorkbook.class));
        Response response = quoteOptionBcmResourceHandler.importBcmSheet(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID, anInputStream());

        assertThat(response, is(aResponse().withStatus(OK)));
        AjaxResponseDTO dto = JSONSerializer.getInstance().deSerialize((String) response.getEntity(), AjaxResponseDTO.class);
        assertFalse(dto.successful());
        assertThat(dto.errors(), is("Please upload the BCM sheet in the same format/version as it was downloaded."));
    }

    @Test
    public void shouldBlockImportIfDisocuntStatusOfNeedsApprovalIsPresentOnQuote() throws Exception {

        QuoteOptionItemDTO quoteOptionItemDTO = new QuoteOptionItemDTO();
        quoteOptionItemDTO.setDiscountStatus(LineItemDiscountStatus.NEEDS_APPROVAL);
        when(quoteOptionItemResource.get()).thenReturn(Arrays.asList(quoteOptionItemDTO));
        Response response = quoteOptionBcmResourceHandler.importBcmSheet(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID, anInputStream());

        assertThat(response, is(aResponse().withStatus(Response.Status.BAD_REQUEST)));
        assertThat(response.getEntity().toString(), is("The discount status is no longer Approval Requested. Please request discount approval before uploading the BCM sheet."));
    }

    @Test
    public void shouldValidateBCMCanBeExportedAndReturnOK() throws Exception {
        doNothing().when(quoteOptionBcmSheetExportOrchestrator).canExportBCMSheet(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID);
        Response response = quoteOptionBcmResourceHandler.validateBCMExportAllowed(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID);
        assertThat(response.getStatus(), is(200));
        verify(quoteOptionBcmSheetExportOrchestrator).canExportBCMSheet(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID);
    }

    @Test
    public void shouldReturnInternalServerErrorWhenBCMExportAllowedValidationFails() throws Exception {
        doThrow(UnsupportedOperationException.class).when(quoteOptionBcmSheetExportOrchestrator).canExportBCMSheet(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID);
        Response response = quoteOptionBcmResourceHandler.validateBCMExportAllowed(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID);
        assertThat(response.getStatus(), is(500));
        verify(quoteOptionBcmSheetExportOrchestrator).canExportBCMSheet(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID);
    }

    private InputStream anInputStream() throws IOException {
        return QuoteOptionBcmResourceHandlerTest.class.getResourceAsStream("importedbcm.xls");
    }


}
