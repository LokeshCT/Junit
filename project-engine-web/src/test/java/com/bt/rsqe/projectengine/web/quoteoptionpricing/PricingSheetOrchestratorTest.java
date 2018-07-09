package com.bt.rsqe.projectengine.web.quoteoptionpricing;


import com.bt.rsqe.projectengine.web.quoteoptionpricing.pricingsheet.PricingSheetTestDataFixture;
import com.bt.rsqe.projectengine.web.quoteoptionpricing.pricingsheet.model.PricingSheetDataModel;
import com.bt.rsqe.projectengine.web.quoteoptionpricing.pricingsheet.model.PricingSheetDataModelFactory;
import com.bt.rsqe.security.UserContextManager;
import com.bt.rsqe.util.excel.ExcelWithNoHeadersComparator;
import com.google.common.base.Optional;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.Before;
import org.junit.Test;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.bt.rsqe.security.UserContextBuilder.*;
import static com.google.common.collect.Maps.newHashMap;
import static org.hamcrest.core.Is.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;


public class PricingSheetOrchestratorTest {

    private static final String PROJECT_ID = "projectId";
    private static final String QUOTE_OPTION_ID = "quoteOptionId";
    private static final String CUSTOMER_ID = "customerId";

    private PricingSheetOrchestrator orchestrator;
    private PricingSheetDataModelFactory mockPricingSheetDataModelFactory;

    @Before
    public void before() {
        mockPricingSheetDataModelFactory = mock(PricingSheetDataModelFactory.class);
        orchestrator = new PricingSheetOrchestrator(mockPricingSheetDataModelFactory);
    }

    @Test
    public void shouldRenderPricingSheetForDirectUser() throws IOException {
        String expectedFileName = "expected-jxls-output-CA-direct-details.xls";

        //Given
        withDirectUser();
        PricingSheetDataModel pricingSheetDataModel = new PricingSheetTestDataFixture().pricingSheetTestData();
        when(mockPricingSheetDataModelFactory.create(CUSTOMER_ID, PROJECT_ID, QUOTE_OPTION_ID, Optional.<String>absent())).thenReturn(pricingSheetDataModel);
        mockPricingSheetDataModelFactory.isSpecialBidAvailable=false;
        mockPricingSheetDataModelFactory.isAccessAvailable=false;
        //when
        Workbook workbook = orchestrator.renderPricingSheet(CUSTOMER_ID, PROJECT_ID, QUOTE_OPTION_ID, Optional.<String>absent());
        // Uncomment this line to regenerate the expected file from the template (in c:\apps\pricing\) - this is not a good solution the tests become pointless
		write(workbook, expectedFileName);
        //then
        List<String> errors = new ExcelWithNoHeadersComparator((HSSFWorkbook) workbook,
                                                               new HSSFWorkbook(getClass().getResourceAsStream(expectedFileName)),
                                                               true).compare();
        assertThat(errors.size() ,is(0));
    }

    @Test
    public void shouldRenderPricingSheetForSWLicenceGrandchildWithNoPriceLinesDirectUser() throws IOException {
        String expectedFileName = "expected-jxls-output-direct-nonpricable-swlicence.xls";

        //Given
        withDirectUser();
        PricingSheetDataModel pricingSheetDataModel = new PricingSheetTestDataFixture().pricingSheetTestDataForNonPricableGrandChild();
        when(mockPricingSheetDataModelFactory.create(CUSTOMER_ID, PROJECT_ID, QUOTE_OPTION_ID, Optional.<String>absent())).thenReturn(pricingSheetDataModel);
        mockPricingSheetDataModelFactory.isSpecialBidAvailable=false;
        mockPricingSheetDataModelFactory.isAccessAvailable=false;
        //when
        Workbook workbook = orchestrator.renderPricingSheet(CUSTOMER_ID, PROJECT_ID, QUOTE_OPTION_ID, Optional.<String>absent());
        // Uncomment this line to regenerate the expected file from the template (in c:\apps\pricing\) - this is not a good solution the tests become pointless
		write(workbook, expectedFileName);
        
        //then
        List<String> errors = new ExcelWithNoHeadersComparator((HSSFWorkbook) workbook,
                                                               new HSSFWorkbook(getClass().getResourceAsStream(expectedFileName)),
                                                               true).compare();
        assertThat(errors.size() ,is(0));
    }

    @Test
    public void shouldRenderPricingSheetForDirectUserForModifyJourney() throws IOException {
        String expectedFileName = "expected-jxls-output-CA-direct-details-modify-journey.xls";

        //Given
        withDirectUser();
        PricingSheetDataModel pricingSheetDataModel = new PricingSheetTestDataFixture().pricingSheetTestDataForModifyJourney();
        when(mockPricingSheetDataModelFactory.create(CUSTOMER_ID, PROJECT_ID, QUOTE_OPTION_ID, Optional.<String>absent())).thenReturn(pricingSheetDataModel);
        mockPricingSheetDataModelFactory.isSpecialBidAvailable=false;
        //when
        Workbook workbook = orchestrator.renderPricingSheet(CUSTOMER_ID, PROJECT_ID, QUOTE_OPTION_ID, Optional.<String>absent());
        // Uncomment this line to regenerate the expected file from the template (in c:\apps\pricing\) - this is not a good solution the tests become pointless
		write(workbook, expectedFileName);
        //then
        List<String> errors = new ExcelWithNoHeadersComparator((HSSFWorkbook) workbook,
                                                               new HSSFWorkbook(getClass().getResourceAsStream(expectedFileName)),
                                                               true).compare();
        assertThat(errors.size() ,is(0));
    }

    @Test
    public void shouldRenderPricingSheetForInDirectUser() throws IOException {
        String expectedFileName = "expected-jxls-output-CA-indirect-details.xls";

        //Given
        withIndirectUser();
        PricingSheetDataModel pricingSheetDataModel = new PricingSheetTestDataFixture().pricingSheetTestData();
        when(mockPricingSheetDataModelFactory.create(CUSTOMER_ID, PROJECT_ID, QUOTE_OPTION_ID, Optional.<String>absent())).thenReturn(pricingSheetDataModel);
        mockPricingSheetDataModelFactory.isSpecialBidAvailable=false;
        //when
        Workbook workbook = orchestrator.renderPricingSheet(CUSTOMER_ID, PROJECT_ID, QUOTE_OPTION_ID, Optional.<String>absent());
        // Uncomment this line to regenerate the expected file from the template (in c:\apps\pricing\) - this is not a good solution the tests become pointless
		write(workbook, expectedFileName);

        //then
        List<String> errors = new ExcelWithNoHeadersComparator((HSSFWorkbook) workbook,
                                                               new HSSFWorkbook(getClass().getResourceAsStream(expectedFileName)),
                                                               true).compare();
        assertThat(errors.size() ,is(0));
    }

    @Test
    public void shouldRenderPricingSheetForInDirectUserForModifyJourney() throws IOException {
        String expectedFileName = "expected-jxls-output-CA-indirect-details-modify-journey.xls";

        //Given
        withIndirectUser();
        PricingSheetDataModel pricingSheetDataModel = new PricingSheetTestDataFixture().pricingSheetTestDataForModifyJourney();
        when(mockPricingSheetDataModelFactory.create(CUSTOMER_ID, PROJECT_ID, QUOTE_OPTION_ID, Optional.<String>absent())).thenReturn(pricingSheetDataModel);
        mockPricingSheetDataModelFactory.isSpecialBidAvailable=false;
        //when
        Workbook workbook = orchestrator.renderPricingSheet(CUSTOMER_ID, PROJECT_ID, QUOTE_OPTION_ID, Optional.<String>absent());
        // Uncomment this line to regenerate the expected file from the template (in c:\apps\pricing\) - this is not a good solution the tests become pointless
		write(workbook, expectedFileName);

        //then
        List<String> errors = new ExcelWithNoHeadersComparator((HSSFWorkbook) workbook,
                                                               new HSSFWorkbook(getClass().getResourceAsStream(expectedFileName)),
                                                               true).compare();
        assertThat(errors.size() ,is(0));
    }

    @Test
    public void shouldRenderSpecialBidSheetForDirectUser() throws IOException {
        String expectedFileName = "expected-specialbid-direct.xls";

        //Given
        withDirectUser();
        PricingSheetDataModel pricingSheetDataModel = new PricingSheetTestDataFixture().pricingSheetSpecialBidTestData();
        when(mockPricingSheetDataModelFactory.create(CUSTOMER_ID, PROJECT_ID, QUOTE_OPTION_ID, Optional.<String>absent())).thenReturn(pricingSheetDataModel);
        mockPricingSheetDataModelFactory.isSpecialBidAvailable=true;
        //when
        Workbook workbook = orchestrator.renderPricingSheet(CUSTOMER_ID, PROJECT_ID, QUOTE_OPTION_ID, Optional.<String>absent());
        // Uncomment this line to regenerate the expected file from the template (in c:\apps\pricing\) - this is not a good solution the tests become pointless
		write(workbook, expectedFileName);

        //then
        HashMap<Integer, String> skipSheets = new HashMap<Integer, String>();
        skipSheets.put(1, "Summary Pricing Sheet");
        List<String> errors = new ExcelWithNoHeadersComparator((HSSFWorkbook) workbook,
                                                               new HSSFWorkbook(getClass().getResourceAsStream(expectedFileName)),
                                                               new HashMap<Integer, List<Integer>>(), skipSheets, true).compare();
        assertThat(errors.size() ,is(0));
    }

    @Test
    public void shouldRenderSpecialBidSheetForDirectUserForModifyJourney() throws IOException {
        String expectedFileName = "expected-specialbid-direct-modify-journey.xls";

        //Given
        withDirectUser();
        PricingSheetDataModel pricingSheetDataModel = new PricingSheetTestDataFixture().pricingSheetSpecialBidTestDataForModifyJourney();
        when(mockPricingSheetDataModelFactory.create(CUSTOMER_ID, PROJECT_ID, QUOTE_OPTION_ID, Optional.<String>absent())).thenReturn(pricingSheetDataModel);
        mockPricingSheetDataModelFactory.isSpecialBidAvailable=true;

        //when
        Workbook workbook = orchestrator.renderPricingSheet(CUSTOMER_ID, PROJECT_ID, QUOTE_OPTION_ID, Optional.<String>absent());
        // Uncomment this line to regenerate the expected file from the template (in c:\apps\pricing\) - this is not a good solution the tests become pointless
		write(workbook, expectedFileName);
        //then
        HashMap<Integer, String> skipSheets = new HashMap<Integer, String>();
        skipSheets.put(1, "Summary Pricing Sheet");
        List<String> errors = new ExcelWithNoHeadersComparator((HSSFWorkbook) workbook,
                                                               new HSSFWorkbook(getClass().getResourceAsStream(expectedFileName)),
                                                               new HashMap<Integer, List<Integer>>(), skipSheets, true).compare();
        assertThat(errors.size() ,is(0));
    }

    @Test
    public void shouldRenderBothSpecialBidAndStandardProductsForDirectUser() throws IOException {
        String expectedFileName = "expected-specialbid-and-standard-direct.xls";

        withDirectUser();
        PricingSheetDataModel pricingSheetDataModel = new PricingSheetTestDataFixture().pricingSheetTestDataBothSpecialBidAndStandard();
        when(mockPricingSheetDataModelFactory.create(CUSTOMER_ID, PROJECT_ID, QUOTE_OPTION_ID, Optional.<String>absent())).thenReturn(pricingSheetDataModel);
        mockPricingSheetDataModelFactory.isSpecialBidAvailable=true;
        //when
        Workbook workbook = orchestrator.renderPricingSheet(CUSTOMER_ID, PROJECT_ID, QUOTE_OPTION_ID, Optional.<String>absent());
        // Uncomment this line to regenerate the expected file from the template (in c:\apps\pricing\) - this is not a good solution the tests become pointless
		write(workbook, expectedFileName);

        //then
        HashMap<Integer, String> skipSheets = new HashMap<Integer, String>();
        skipSheets.put(1, "Summary Pricing Sheet");
        List<String> errors = new ExcelWithNoHeadersComparator((HSSFWorkbook) workbook,
                                                               new HSSFWorkbook(getClass().getResourceAsStream(expectedFileName)),
                                                               new HashMap<Integer, List<Integer>>(), skipSheets, true).compare();
        assertThat(errors.size() ,is(0));
    }

    @Test
    public void shouldRenderBothSpecialBidAndStandardProductsForDirectUserForModifyJourney() throws IOException {
        String expectedFileName = "expected-specialbid-and-standard-direct-modify-journey.xls";

        withDirectUser();
        PricingSheetDataModel pricingSheetDataModel = new PricingSheetTestDataFixture().pricingSheetTestDataBothSpecialBidAndStandardForModifyJourney();
        when(mockPricingSheetDataModelFactory.create(CUSTOMER_ID, PROJECT_ID, QUOTE_OPTION_ID, Optional.<String>absent())).thenReturn(pricingSheetDataModel);
        mockPricingSheetDataModelFactory.isSpecialBidAvailable=true;
        //when
        Workbook workbook = orchestrator.renderPricingSheet(CUSTOMER_ID, PROJECT_ID, QUOTE_OPTION_ID, Optional.<String>absent());
        // Uncomment this line to regenerate the expected file from the template (in c:\apps\pricing\) - this is not a good solution the tests become pointless
		write(workbook, expectedFileName);

        //then
        HashMap<Integer, String> skipSheets = new HashMap<Integer, String>();
        skipSheets.put(1, "Summary Pricing Sheet");
        List<String> errors = new ExcelWithNoHeadersComparator((HSSFWorkbook) workbook,
                                                               new HSSFWorkbook(getClass().getResourceAsStream(expectedFileName)),
                                                               new HashMap<Integer, List<Integer>>(), skipSheets, true).compare();
        assertThat(errors.size() ,is(0));
    }

    @Test
    public void shouldRenderBothSpecialBidAndStandardProductsForInDirectUser() throws IOException {
        String expectedFileName = "expected-specialbid-and-standard-indirect.xls";

        withIndirectUser();
        PricingSheetDataModel pricingSheetDataModel = new PricingSheetTestDataFixture().pricingSheetTestDataBothSpecialBidAndStandard();
        when(mockPricingSheetDataModelFactory.create(CUSTOMER_ID, PROJECT_ID, QUOTE_OPTION_ID, Optional.<String>absent())).thenReturn(pricingSheetDataModel);
        mockPricingSheetDataModelFactory.isSpecialBidAvailable=true;
        //when
        Workbook workbook = orchestrator.renderPricingSheet(CUSTOMER_ID, PROJECT_ID, QUOTE_OPTION_ID, Optional.<String>absent());
        // Uncomment this line to regenerate the expected file from the template (in c:\apps\pricing\) - this is not a good solution the tests become pointless
       	write(workbook, expectedFileName);

        //then
        List<String> errors = new ExcelWithNoHeadersComparator((HSSFWorkbook) workbook,
                                                               new HSSFWorkbook(getClass().getResourceAsStream(expectedFileName)),
                                                               true).compare();
        assertThat(errors.size() ,is(0));
    }

    @Test
    public void shouldRenderBothSpecialBidAndStandardProductsForInDirectUserForModifyJourney() throws IOException {
        String expectedFileName = "expected-specialbid-and-standard-indirect-modify-journey.xls";

        withIndirectUser();
        PricingSheetDataModel pricingSheetDataModel = new PricingSheetTestDataFixture().pricingSheetTestDataBothSpecialBidAndStandardForModifyJourney();
        when(mockPricingSheetDataModelFactory.create(CUSTOMER_ID, PROJECT_ID, QUOTE_OPTION_ID, Optional.<String>absent())).thenReturn(pricingSheetDataModel);
        mockPricingSheetDataModelFactory.isSpecialBidAvailable=true;
        //when
        Workbook workbook = orchestrator.renderPricingSheet(CUSTOMER_ID, PROJECT_ID, QUOTE_OPTION_ID, Optional.<String>absent());
        // Uncomment this line to regenerate the expected file from the template (in c:\apps\pricing\) - this is not a good solution the tests become pointless
       	write(workbook, expectedFileName);

        //then
        List<String> errors = new ExcelWithNoHeadersComparator((HSSFWorkbook) workbook,
                                                               new HSSFWorkbook(getClass().getResourceAsStream(expectedFileName)),
                                                               true).compare();
        assertThat(errors.size() ,is(0));
    }

    @Test
    public void shouldRenderSpecialBidSheetForInDirectUser() throws IOException {
        String expectedFileName = "expected-specialbid-Indirect.xls";

        //Given
        withDirectUser();
        PricingSheetDataModel pricingSheetDataModel = new PricingSheetTestDataFixture().pricingSheetSpecialBidTestData();
        when(mockPricingSheetDataModelFactory.create(CUSTOMER_ID, PROJECT_ID, QUOTE_OPTION_ID, Optional.<String>absent())).thenReturn(pricingSheetDataModel);
        mockPricingSheetDataModelFactory.isSpecialBidAvailable = true;
        //when
        Workbook workbook = orchestrator.renderPricingSheet(CUSTOMER_ID, PROJECT_ID, QUOTE_OPTION_ID, Optional.<String>absent());
        // Uncomment this line to regenerate the expected file from the template (in c:\apps\pricing\) - this is not a good solution the tests become pointless
		write(workbook, expectedFileName);

        //then
        HashMap<Integer, String> skipSheets = new HashMap<Integer, String>();
        skipSheets.put(1, "Summary Pricing Sheet");
        List<String> errors = new ExcelWithNoHeadersComparator((HSSFWorkbook) workbook,
                                                               new HSSFWorkbook(getClass().getResourceAsStream(expectedFileName)),
                                                               new HashMap<Integer, List<Integer>>(), skipSheets, true).compare();
        assertThat(errors.size() ,is(0));
    }

    @Test
    public void shouldRenderSpecialBidSheetForInDirectUserForModifyJourney() throws IOException {
        String expectedFileName = "expected-specialbid-Indirect-modify-journey.xls";

        //Given
        withDirectUser();
        PricingSheetDataModel pricingSheetDataModel = new PricingSheetTestDataFixture().pricingSheetSpecialBidTestDataForModifyJourney();
        when(mockPricingSheetDataModelFactory.create(CUSTOMER_ID, PROJECT_ID, QUOTE_OPTION_ID, Optional.<String>absent())).thenReturn(pricingSheetDataModel);
        mockPricingSheetDataModelFactory.isSpecialBidAvailable=true;
        //when
        Workbook workbook = orchestrator.renderPricingSheet(CUSTOMER_ID, PROJECT_ID, QUOTE_OPTION_ID, Optional.<String>absent());
        // Uncomment this line to regenerate the expected file from the template (in c:\apps\pricing\) - this is not a good solution the tests become pointless
		write(workbook, expectedFileName);

        //then
        HashMap<Integer, String> skipSheets = new HashMap<Integer, String>();
        skipSheets.put(1, "Summary Pricing Sheet");
        List<String> errors = new ExcelWithNoHeadersComparator((HSSFWorkbook) workbook,
                                                               new HSSFWorkbook(getClass().getResourceAsStream(expectedFileName)),
                                                               new HashMap<Integer, List<Integer>>(), skipSheets, true).compare();
        assertThat(errors.size(), is(0));
    }

    @Test(expected = RuntimeException.class)
    public void shouldThrowRuntimeExceptionIfXlsTransformFail() throws IOException {

        //Given
        withDirectUser();
        when(mockPricingSheetDataModelFactory.create(CUSTOMER_ID, PROJECT_ID, QUOTE_OPTION_ID, Optional.<String>absent())).thenReturn(null);

        //when
        orchestrator.renderPricingSheet(CUSTOMER_ID, PROJECT_ID, QUOTE_OPTION_ID, Optional.<String>absent());
    }

    private void withDirectUser() {
        UserContextManager.setCurrent(aDirectUserContext().build());
    }

    private void withIndirectUser() {
        UserContextManager.setCurrent(anIndirectUserContext().build());
    }

    @Test
    public void
    shouldRenderAccessCaveatsSheetForDirectUser() throws IOException {
        String expectedFileName = "expected-access-product-direct.xls";

        //Given
        withDirectUser();
        PricingSheetDataModel pricingSheetDataModel = new PricingSheetTestDataFixture().pricingSheetAccessCaveatsTestData();
        when(mockPricingSheetDataModelFactory.create(CUSTOMER_ID, PROJECT_ID, QUOTE_OPTION_ID, Optional.<String>absent())).thenReturn(pricingSheetDataModel);
        mockPricingSheetDataModelFactory.isSpecialBidAvailable=false;
        mockPricingSheetDataModelFactory.isAccessAvailable=true;
        //when
        Workbook workbook = orchestrator.renderPricingSheet(CUSTOMER_ID, PROJECT_ID, QUOTE_OPTION_ID, Optional.<String>absent());
        // Uncomment this line to regenerate the expected file from the template (in c:\apps\pricing\) - this is not a good solution the tests become pointless
		write(workbook, expectedFileName);

        //then
        List<String> errors = new ExcelWithNoHeadersComparator((HSSFWorkbook) workbook,
                                                               new HSSFWorkbook(getClass().getResourceAsStream(expectedFileName)),
                                                               true).compare();
        assertThat(errors.size() ,is(0));
    }

    @Test
    public void shouldRenderAccessCaveatsSheetForIndirectUser() throws IOException {
        String expectedFileName = "expected-access-product-indirect.xls";

        //Given
        withIndirectUser();
        PricingSheetDataModel pricingSheetDataModel = new PricingSheetTestDataFixture().pricingSheetAccessCaveatsTestData();
        when(mockPricingSheetDataModelFactory.create(CUSTOMER_ID, PROJECT_ID, QUOTE_OPTION_ID, Optional.<String>absent())).thenReturn(pricingSheetDataModel);
        mockPricingSheetDataModelFactory.isSpecialBidAvailable=false;
        mockPricingSheetDataModelFactory.isAccessAvailable=true;
        //when
        Workbook workbook = orchestrator.renderPricingSheet(CUSTOMER_ID, PROJECT_ID, QUOTE_OPTION_ID, Optional.<String>absent());
        // Uncomment this line to regenerate the expected file from the template (in c:\apps\pricing\) - this is not a good solution the tests become pointless
       	write(workbook, expectedFileName);

        //then
        List<String> errors = new ExcelWithNoHeadersComparator((HSSFWorkbook) workbook,
                                                               new HSSFWorkbook(getClass().getResourceAsStream(expectedFileName)),
                                                               true).compare();
        assertThat(errors.size() ,is(0));
    }

    @Test
    public void shouldRenderPricingCaveatsSheetForDirectUser() throws IOException {
        String expectedFileName = "expected-pricing-caveat-direct.xls";

        //Given
        withDirectUser();
        PricingSheetDataModel pricingSheetDataModel = new PricingSheetTestDataFixture().pricingSheetPricingCaveatsTestData();
        when(mockPricingSheetDataModelFactory.create(CUSTOMER_ID, PROJECT_ID, QUOTE_OPTION_ID, Optional.<String>absent())).thenReturn(pricingSheetDataModel);
        mockPricingSheetDataModelFactory.isSpecialBidAvailable=false;
        mockPricingSheetDataModelFactory.isAccessAvailable=true;
        //when
        Workbook workbook = orchestrator.renderPricingSheet(CUSTOMER_ID, PROJECT_ID, QUOTE_OPTION_ID, Optional.<String>absent());
        // Uncomment this line to regenerate the expected file from the template (in c:\apps\pricing\) - this is not a good solution the tests become pointless
        write(workbook, expectedFileName);

        //then
        List<String> errors = new ExcelWithNoHeadersComparator((HSSFWorkbook) workbook,
                                                               new HSSFWorkbook(getClass().getResourceAsStream(expectedFileName)),
                                                               true).compare();
        assertThat(errors.size() ,is(0));
    }

    @Test
    public void shouldRenderPricingCaveatsSheetForInDirectUser() throws IOException {
        String expectedFileName = "expected-pricing-caveat-indirect.xls";

        //Given
        withDirectUser();
        PricingSheetDataModel pricingSheetDataModel = new PricingSheetTestDataFixture().pricingSheetPricingCaveatsTestData();
        when(mockPricingSheetDataModelFactory.create(CUSTOMER_ID, PROJECT_ID, QUOTE_OPTION_ID, Optional.<String>absent())).thenReturn(pricingSheetDataModel);
        mockPricingSheetDataModelFactory.isSpecialBidAvailable=false;
        mockPricingSheetDataModelFactory.isAccessAvailable=true;
        //when
        Workbook workbook = orchestrator.renderPricingSheet(CUSTOMER_ID, PROJECT_ID, QUOTE_OPTION_ID, Optional.<String>absent());
        // Uncomment this line to regenerate the expected file from the template (in c:\apps\pricing\) - this is not a good solution the tests become pointless
		write(workbook, expectedFileName);

        //then
        List<String> errors = new ExcelWithNoHeadersComparator((HSSFWorkbook) workbook,
                                                               new HSSFWorkbook(getClass().getResourceAsStream(expectedFileName)),
                                                               true).compare();
        assertThat(errors.size() ,is(0));
    }

    @Test
    public void shouldRenderICrAndICgPricingSheetForDirectUser() throws IOException {
        String expectedFileName = "expected-jxls-output-icr-icg-direct-details.xls";

        //Given
        withDirectUser();
        PricingSheetDataModel pricingSheetDataModel = new PricingSheetTestDataFixture().pricingSheetICrICgTestData();
        when(mockPricingSheetDataModelFactory.create(CUSTOMER_ID, PROJECT_ID, QUOTE_OPTION_ID, Optional.<String>absent())).thenReturn(pricingSheetDataModel);
        mockPricingSheetDataModelFactory.isSpecialBidAvailable=false;
        mockPricingSheetDataModelFactory.isAccessAvailable=false;
        //when
        Workbook workbook = orchestrator.renderPricingSheet(CUSTOMER_ID, PROJECT_ID, QUOTE_OPTION_ID, Optional.<String>absent());
        // Uncomment this line to regenerate the expected file from the template (in c:\apps\pricing\) - this is not a good solution the tests become pointless
		write(workbook, expectedFileName);

        //then
        List<String> errors = new ExcelWithNoHeadersComparator((HSSFWorkbook) workbook,
                                                               new HSSFWorkbook(getClass().getResourceAsStream(expectedFileName)),
                                                               true).compare();
        assertThat(errors.size() ,is(0));
    }

    @Test
    public void shouldRenderICrICgPricingSheetForInDirectUser() throws IOException {
        String expectedFileName = "expected-jxls-output-icr-icg-indirect-details.xls";

        //Given
        withIndirectUser();
        PricingSheetDataModel pricingSheetDataModel = new PricingSheetTestDataFixture().pricingSheetICrICgTestData();
        when(mockPricingSheetDataModelFactory.create(CUSTOMER_ID, PROJECT_ID, QUOTE_OPTION_ID, Optional.<String>absent())).thenReturn(pricingSheetDataModel);
        mockPricingSheetDataModelFactory.isSpecialBidAvailable=false;
        mockPricingSheetDataModelFactory.isAccessAvailable=true;
        //when
        Workbook workbook = orchestrator.renderPricingSheet(CUSTOMER_ID, PROJECT_ID, QUOTE_OPTION_ID, Optional.<String>absent());
        // Uncomment this line to regenerate the expected file from the template (in c:\apps\pricing\) - this is not a good solution the tests become pointless
       	write(workbook, expectedFileName);

        //then
        List<String> errors = new ExcelWithNoHeadersComparator((HSSFWorkbook) workbook,
                                                               new HSSFWorkbook(getClass().getResourceAsStream(expectedFileName)),
                                                               true).compare();
        assertThat(errors.size() ,is(0));
    }

    @Test
    public void shouldRenderICrICgPricingSheetForDirectUserForModifyJourney() throws IOException {
        String expectedFileName = "expected-jxls-output-icr-icg-direct-modify-journey.xls";

        //Given
        withDirectUser();
        PricingSheetDataModel pricingSheetDataModel = new PricingSheetTestDataFixture().pricingSheetICrICgTestDataForModifyJourney();
        when(mockPricingSheetDataModelFactory.create(CUSTOMER_ID, PROJECT_ID, QUOTE_OPTION_ID, Optional.<String>absent())).thenReturn(pricingSheetDataModel);
        mockPricingSheetDataModelFactory.isSpecialBidAvailable=false;
        //when
        Workbook workbook = orchestrator.renderPricingSheet(CUSTOMER_ID, PROJECT_ID, QUOTE_OPTION_ID, Optional.<String>absent());
        // Uncomment this line to regenerate the expected file from the template (in c:\apps\pricing\) - this is not a good solution the tests become pointless
		write(workbook, expectedFileName);

        //then
        List<String> errors = new ExcelWithNoHeadersComparator((HSSFWorkbook) workbook,
                                                               new HSSFWorkbook(getClass().getResourceAsStream(expectedFileName)),
                                                               true).compare();
        assertThat(errors.size() ,is(0));
    }

    @Test
    public void shouldRenderICrICgPricingSheetForIndirectUserForModifyJourney() throws IOException {
        String expectedFileName = "expected-jxls-output-icr-icg-indirect-modify-journey.xls";

        //Given
        withIndirectUser();
        PricingSheetDataModel pricingSheetDataModel = new PricingSheetTestDataFixture().pricingSheetICrICgTestDataForModifyJourney();
        when(mockPricingSheetDataModelFactory.create(CUSTOMER_ID, PROJECT_ID, QUOTE_OPTION_ID, Optional.<String>absent())).thenReturn(pricingSheetDataModel);
        mockPricingSheetDataModelFactory.isSpecialBidAvailable=false;
        //when
        Workbook workbook = orchestrator.renderPricingSheet(CUSTOMER_ID, PROJECT_ID, QUOTE_OPTION_ID, Optional.<String>absent());
        // Uncomment this line to regenerate the expected file from the template (in c:\apps\pricing\) - this is not a good solution the tests become pointless
       	write(workbook, expectedFileName);

        //then
        List<String> errors = new ExcelWithNoHeadersComparator((HSSFWorkbook) workbook,
                                                               new HSSFWorkbook(getClass().getResourceAsStream(expectedFileName)),
                                                               true).compare();
        assertThat(errors.size() ,is(0));
    }

    @Test
    public void shouldRenderContractPricingSheetForDirectUser() throws Exception {
        String expectedFileName = "expected-jxls-ouput-contract-product-direct.xls";

        //Given
        withDirectUser();
        PricingSheetDataModel pricingSheetDataModel = new PricingSheetTestDataFixture().pricingSheetContractTestData();
        when(mockPricingSheetDataModelFactory.create(CUSTOMER_ID, PROJECT_ID, QUOTE_OPTION_ID, Optional.<String>absent())).thenReturn(pricingSheetDataModel);
        //when
        Workbook workbook = orchestrator.renderPricingSheet(CUSTOMER_ID, PROJECT_ID, QUOTE_OPTION_ID, Optional.<String>absent());
        // Uncomment this line to regenerate the expected file from the template (in c:\apps\pricing\) - this is not a good solution the tests become pointless
		write(workbook, expectedFileName);

        //then
        List<String> errors = new ExcelWithNoHeadersComparator((HSSFWorkbook) workbook,
                                                               new HSSFWorkbook(getClass().getResourceAsStream(expectedFileName)),
                                                               true).compare();
        assertThat(errors.size() ,is(0));
    }

    @Test
    public void shouldRenderBidManagerCaveatsinPricingSheetForDirectUser() throws Exception {
        String expectedFileName = "expected-jxls-ouput-bid-caveats-product-direct.xls";

        //Given
        withDirectUser();
        PricingSheetDataModel pricingSheetDataModel = new PricingSheetTestDataFixture().pricingSheetBidManagerCaveats();
        when(mockPricingSheetDataModelFactory.create(CUSTOMER_ID, PROJECT_ID, QUOTE_OPTION_ID, Optional.<String>absent())).thenReturn(pricingSheetDataModel);
        //when
        Workbook workbook = orchestrator.renderPricingSheet(CUSTOMER_ID, PROJECT_ID, QUOTE_OPTION_ID, Optional.<String>absent());
        // Uncomment this line to regenerate the expected file from the template (in c:\apps\pricing\) - this is not a good solution the tests become pointless
		write(workbook, expectedFileName);
        int sheetIndex = workbook.getSheetIndex("Bid Manager Caveats");
         Map<Integer, List<Integer>> skipColumnsForSheet = newHashMap();
        skipColumnsForSheet.put(sheetIndex, Arrays.asList(3));
        Map<Integer, String> skipSheets = newHashMap();
        //then
        List<String> errors = new ExcelWithNoHeadersComparator((HSSFWorkbook) workbook,
                                                               new HSSFWorkbook(getClass().getResourceAsStream(expectedFileName)),
                                                               skipColumnsForSheet,skipSheets,true).compare();
        assertThat(errors.size() ,is(0));
    }
    @Test
    public void shouldRenderBidManagerCaveatsinPricingSheetForInDirectUser() throws Exception {
        String expectedFileName = "expected-jxls-ouput-bid-caveats-product-indirect.xls";

        //Given
        withIndirectUser();
        PricingSheetDataModel pricingSheetDataModel = new PricingSheetTestDataFixture().pricingSheetBidManagerCaveats();
        when(mockPricingSheetDataModelFactory.create(CUSTOMER_ID, PROJECT_ID, QUOTE_OPTION_ID, Optional.<String>absent())).thenReturn(pricingSheetDataModel);
        //when               uj
        Workbook workbook = orchestrator.renderPricingSheet(CUSTOMER_ID, PROJECT_ID, QUOTE_OPTION_ID, Optional.<String>absent());
        // Uncomment this line to regenerate the expected file from the template (in c:\apps\pricing\) - this is not a good solution the tests become pointless
		write(workbook, expectedFileName);
        int sheetIndex = workbook.getSheetIndex("Bid Manager Caveats");
         Map<Integer, List<Integer>> skipColumnsForSheet = newHashMap();
        skipColumnsForSheet.put(sheetIndex, Arrays.asList(3));
        Map<Integer, String> skipSheets = newHashMap();
        //then
        List<String> errors = new ExcelWithNoHeadersComparator((HSSFWorkbook) workbook,
                                                               new HSSFWorkbook(getClass().getResourceAsStream(expectedFileName)),
                                                               skipColumnsForSheet,skipSheets,true).compare();
        assertThat(errors.size() ,is(0));
    }

    @SuppressWarnings("unused")
    private void write(Workbook workbook, String fileName) {
        FileOutputStream out;
        try {
            out = new FileOutputStream("C:\\ProgramData\\Generated\\" + fileName);
            workbook.write(out);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
