package com.bt.rsqe.projectengine.web;

import com.bt.rsqe.projectengine.ProjectDTO;
import com.bt.rsqe.projectengine.ProjectResource;
import com.bt.rsqe.projectengine.QuoteOptionDTO;
import com.bt.rsqe.projectengine.QuoteOptionResource;
import com.bt.rsqe.projectengine.web.quoteoption.QuoteOptionBcmResourceHandlerTest;
import com.bt.rsqe.projectengine.web.quoteoption.validation.BillAccountCurrencyValidator;
import com.bt.rsqe.projectengine.web.quoteoption.validation.QuoteOptionDependencyValidator;
import com.bt.rsqe.projectengine.web.quoteoptionorders.rfosheet.ExcelWorkbook;
import com.bt.rsqe.projectengine.web.quoteoptionorders.rfosheet.OrderRFOSheetOrchestrator;
import com.bt.rsqe.projectengine.web.quoteoptionorders.rfosheet.RFOImportException;
import com.bt.rsqe.utils.JSONSerializer;
import com.bt.rsqe.utils.RSQEMockery;
import com.bt.rsqe.web.AjaxResponseDTO;
import com.bt.rsqe.web.Presenter;
import com.google.common.collect.Sets;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.hamcrest.core.Is;
import org.jmock.Expectations;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.io.IOException;

import static com.google.common.collect.Sets.newHashSet;
import static junit.framework.Assert.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;

public class OrderRFOResourceHandlerTest {

    private static final String CUSTOMER_ID = "customerId";
    private static final String CONTRACT_ID = "contractId";
    private static final String PROJECT_ID = "projectId";
    private static final String QUOTE_OPTION_ID = "quoteOptionId";
    private static final String ORDER_ID = "orderId";
    private static final String PROJECT_NAME = "PROJECTNAME";
    RSQEMockery context = new RSQEMockery();
    private OrderRFOResourceHandler resourceHandler;
    private Presenter presenter;
    private OrderRFOSheetOrchestrator orderRFOSheetOrchestrator;
    private ProjectResource projectResource;
    private QuoteOptionResource quoteOptionResource;
    private ProjectDTO projectDTO;
    private QuoteOptionDependencyValidator quoteOptionDependencyValidator;

    @Before
    public void before() {
        presenter = context.mock(Presenter.class);
        orderRFOSheetOrchestrator = context.mock(OrderRFOSheetOrchestrator.class);
        projectResource = mock(ProjectResource.class);
        quoteOptionResource = mock(QuoteOptionResource.class);
        quoteOptionDependencyValidator = mock(QuoteOptionDependencyValidator.class);
        resourceHandler = new OrderRFOResourceHandler(presenter, orderRFOSheetOrchestrator, projectResource, quoteOptionDependencyValidator);
        projectDTO = new ProjectDTO(PROJECT_ID, PROJECT_NAME, CUSTOMER_ID, CONTRACT_ID);
    }

    @After
    public void tearDown() {
        context.assertIsSatisfied();
    }

    @Test
    public void shouldReturnInternalServerErrorWhenRFOExportValidationFails() throws Exception {
        //Given
        when(projectResource.quoteOptionResource(PROJECT_ID)).thenReturn(quoteOptionResource);
        when(quoteOptionResource.get(QUOTE_OPTION_ID)).thenReturn(QuoteOptionDTOFixture.aQuoteOptionDTO().withId(QUOTE_OPTION_ID).withCurrency("USD").build());
        when(quoteOptionDependencyValidator.validate(eq(CUSTOMER_ID), any(BillAccountCurrencyValidator.class))).thenReturn(newHashSet("Error1", "Error2"));

        //When
        final Response response = resourceHandler.canRFOSheetBeExported(CUSTOMER_ID, PROJECT_ID, QUOTE_OPTION_ID);

        //Then
        assertThat(response.getStatus(), Is.is(500));
        final String error = (String) response.getEntity();
        assertTrue(error.contains("Error1"));
        assertTrue(error.contains("Error2"));
    }

    @Test
    public void shouldReturnOKWhenRFOExportValidationPasses() throws Exception {
        //Given
        when(projectResource.quoteOptionResource(PROJECT_ID)).thenReturn(quoteOptionResource);
        when(quoteOptionResource.get(QUOTE_OPTION_ID)).thenReturn(QuoteOptionDTOFixture.aQuoteOptionDTO().withId(QUOTE_OPTION_ID).withCurrency("USD").build());
        when(quoteOptionDependencyValidator.validate(eq(CUSTOMER_ID), any(BillAccountCurrencyValidator.class))).thenReturn(Sets.<String>newHashSet());

        //When
        final Response response = resourceHandler.canRFOSheetBeExported(CUSTOMER_ID, PROJECT_ID, QUOTE_OPTION_ID);

        //Then
        assertThat(response.getStatus(), Is.is(200));
    }

    @Test
    public void shouldReturnExcelWorkBook() throws Exception {
        final String sheetName = "SQE_PROJECTNAME_QuoteName_RFOSheet.xlsx";
        final XSSFWorkbook hssfWorkbook = new XSSFWorkbook();
        context.checking(new Expectations() {{
            oneOf(orderRFOSheetOrchestrator).buildRFOExportExcelSheet(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID, ORDER_ID);

            will(returnValue(new ExcelWorkbook(hssfWorkbook, sheetName)));
        }});
        when(projectResource.get(PROJECT_ID)).thenReturn(projectDTO);
        when(projectResource.quoteOptionResource(PROJECT_ID)).thenReturn(quoteOptionResource);
        when(quoteOptionResource.get(QUOTE_OPTION_ID)).thenReturn(QuoteOptionDTO.newInstance("QUOTE_OPTION_ID", "QuoteName", "GBP", "contractTerm", "createdBy"));
        final Response response = resourceHandler.getRFOExportSheet(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID, ORDER_ID);
        final MultivaluedMap<String,Object> responseMetadata = response.getMetadata();
        assertThat(String.valueOf(responseMetadata.getFirst("Content-Disposition")),Is.is("attachment; filename=" + sheetName));
    }

    @Test
    public void shouldImportExcelWorkBook() throws IOException {
        context.checking(new Expectations() {{
            oneOf(orderRFOSheetOrchestrator).importRfo(with(any(String.class)), with(any(String.class)), with(any(String.class)), with(any(String.class)), with(any(String.class)), with(any(XSSFWorkbook.class)));
        }});
        Response response = resourceHandler.post(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID, ORDER_ID, QuoteOptionBcmResourceHandlerTest.class.getResourceAsStream("importedbcm.xlsx"));
        AjaxResponseDTO responseDto = JSONSerializer.getInstance().deSerialize(response.getEntity().toString(), AjaxResponseDTO.class);
        assertTrue(responseDto.successful());
    }

    @Test
    public void shouldReturnAjaxResponseDTOIfAnyRuntimeException() throws IOException {
        context.checking(new Expectations() {{
            oneOf(orderRFOSheetOrchestrator).importRfo(with(any(String.class)), with(any(String.class)), with(any(String.class)), with(any(String.class)), with(any(String.class)), with(any(XSSFWorkbook.class)));
            will(throwException(new RuntimeException("ex message")));
        }});

        Response response = resourceHandler.post(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID, ORDER_ID, QuoteOptionBcmResourceHandlerTest.class.getResourceAsStream("importedbcm.xlsx"));
        AjaxResponseDTO responseDto = JSONSerializer.getInstance().deSerialize(response.getEntity().toString(), AjaxResponseDTO.class);
        assertFalse(responseDto.successful());
        assertThat(responseDto.errors(), Is.is("Error : ex message"));
    }

    @Test
    public void shouldReturnAjaxResponseDTOIfThereIsRFOImportException() throws IOException {
         context.checking(new Expectations() {{
            oneOf(orderRFOSheetOrchestrator).importRfo(with(any(String.class)), with(any(String.class)), with(any(String.class)), with(any(String.class)), with(any(String.class)), with(any(XSSFWorkbook.class)));
            will(throwException(new RFOImportException("ex message")));
        }});

        Response response = resourceHandler.post(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID, ORDER_ID, QuoteOptionBcmResourceHandlerTest.class.getResourceAsStream("importedbcm.xlsx"));
        AjaxResponseDTO responseDto = JSONSerializer.getInstance().deSerialize(response.getEntity().toString(), AjaxResponseDTO.class);
        assertFalse(responseDto.successful());
        assertThat(responseDto.errors(), Is.is("ex message"));
    }
}
