package com.bt.rsqe.projectengine.web.quoteoptionorders.ecrfsheet;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.IOException;
import java.util.List;

import static junit.framework.Assert.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.core.Is.*;

public class ECRFSheetModelBuilderTest {
    private static Workbook importTestWorkBook;
    private static Workbook importXLSXWorkBook;
    private static Workbook importTestWorkBookError;
    private static Workbook importTestWorkBookNoControlSheet;
    private static Workbook importTestWorkBookDuplicateIds;
    private static Workbook importTestOnlyWithControlSheet;
    private static Workbook importTest4GWorkBook;

    private static ECRFSheetModelBuilder ecrfSheetModelBuilder;
    private static final String PRODUCT_CODE_IMPORTABLE = "S0308545";
    private static final String PRODUCT_CODE_BULK = "S0338235";
    private static final String PRODUCT_CODE_NOT_IMPORTABLE = "notImportableSCode";
    private static final String ROOT_PRODUCT_S_CODE = "S0308545";

    private static final String CHILD_PRODUCT_S_CODE = "S0308469";
    @Rule
    public ExpectedException exception = ExpectedException.none();

    @BeforeClass
    public static void before() throws IOException, InvalidFormatException {
        ecrfSheetModelBuilder = new ECRFSheetModelBuilder();
        importTestWorkBook = WorkbookFactory.create(ECRFSheetModelBuilderTest.class.getResourceAsStream("occ_test_upload.xls"));
        importTestWorkBookError = WorkbookFactory.create(ECRFSheetModelBuilderTest.class.getResourceAsStream("occ_test_missing_sheet.xls"));
        importTestWorkBookNoControlSheet = WorkbookFactory.create(ECRFSheetModelBuilderTest.class.getResourceAsStream("occ_test_no_control_sheet.xls"));
        importTestWorkBookDuplicateIds = WorkbookFactory.create(ECRFSheetModelBuilderTest.class.getResourceAsStream("occ_test_duplicate_ids.xls"));
        importXLSXWorkBook = WorkbookFactory.create(ECRFSheetModelBuilderTest.class.getResourceAsStream("occ_test_upload.xlsx"));
        importTest4GWorkBook = WorkbookFactory.create(ECRFSheetModelBuilderTest.class.getResourceAsStream("4g_test_upload.xls"));
        importTestOnlyWithControlSheet = WorkbookFactory.create(ECRFSheetModelBuilderTest.class.getResourceAsStream("only_control_sheet.xls"));
    }

    @Test
    public void shouldThrowExceptionForUnknownSCode() throws Exception {
        exception.expect(ECRFImportException.class);
        exception.expectMessage(ECRFImportException.controlSheetDoesNotContainSCode);
        ecrfSheetModelBuilder.build(PRODUCT_CODE_NOT_IMPORTABLE, importTestWorkBook);
    }

    @Test
    public void shouldImportECRFSheetForValidSCode() throws Exception {
        ECRFWorkBook workBook = ecrfSheetModelBuilder.build(PRODUCT_CODE_IMPORTABLE, importTestWorkBook);
        assertNotNull(workBook);
        assertEquals(workBook.getSheets().size(), 2);
        assertEquals(workBook.getControlSheet().get("Connect Acceleration Site"), "S0308545");
    }

    @Test
    public void shouldGetECRFModelSheetAttribute() {
        ECRFWorkBook workBook = ecrfSheetModelBuilder.build(PRODUCT_CODE_IMPORTABLE, importTestWorkBook);
        assertNotNull(workBook);
        assertEquals(workBook.getSheetByProductCode(ROOT_PRODUCT_S_CODE).getRow(0).getAttributeByName("ID").getName(), "ID");
        assertEquals(workBook.getSheetByProductCode(ROOT_PRODUCT_S_CODE).getRow(0).getAttributeByName("ID").getValue(), "12345");
        assertEquals(workBook.getSheetByProductCode(CHILD_PRODUCT_S_CODE).getRow(0).getAttributeByName("PARENT PRODUCT ID").getName(), "PARENT PRODUCT ID");
        assertEquals(workBook.getSheetByProductCode(CHILD_PRODUCT_S_CODE).getRow(0).getAttributeByName("PARENT PRODUCT ID").getValue(), "12345");
        assertEquals(workBook.getSheetByProductCode(CHILD_PRODUCT_S_CODE).getRow(0).getAttributeByName("PORTING DATE").getValue(), "2015/01/01");
    }

    @Test
    public void shouldThrowIfNoSheetForSCode() {
        exception.expect(ECRFImportException.class);
        exception.expectMessage(String.format(ECRFImportException.workSheetNotFound, "notImportableSCode"));
        ECRFWorkBook workBook = ecrfSheetModelBuilder.build(PRODUCT_CODE_IMPORTABLE, importTestWorkBook);
        assertNotNull(workBook);
        workBook.getSheetByProductCode(PRODUCT_CODE_NOT_IMPORTABLE);
    }

    @Test
    public void shouldThrowExceptionForMissingAttribute() {
        exception.expect(ECRFImportException.class);
        exception.expectMessage(String.format(ECRFImportException.attributeNotFoundInWorkSheet, "NOT IN SHEET", "Connect Acceleration Site"));
        ECRFWorkBook workBook = ecrfSheetModelBuilder.build(PRODUCT_CODE_IMPORTABLE, importTestWorkBook);
        assertNotNull(workBook);
        workBook.getSheetByProductCode(ROOT_PRODUCT_S_CODE).getRow(0).getAttributeByName("NOT IN SHEET");
    }

    @Test
    public void shouldGetAttributes() {
        ECRFWorkBook workBook = ecrfSheetModelBuilder.build(PRODUCT_CODE_IMPORTABLE, importTestWorkBook);
        assertEquals(workBook.getSheetByProductCode(ROOT_PRODUCT_S_CODE).getRow(0).getAttributes().size(), 13);
        assertEquals(workBook.getSheetByProductCode(CHILD_PRODUCT_S_CODE).getRow(0).getAttributes().size(), 5);
        List<ECRFSheetModelAttribute> attributeList = workBook.getSheetByProductCode(ROOT_PRODUCT_S_CODE).getRow(0).getAttributes();
        for (ECRFSheetModelAttribute attribute : attributeList) {
            assertNotSame(attribute.getName(), "ID");
        }
        attributeList = workBook.getSheetByProductCode(CHILD_PRODUCT_S_CODE).getRow(0).getAttributes();
        for (ECRFSheetModelAttribute attribute : attributeList) {
            assertNotSame(attribute.getName(), "ID");
            assertNotSame(attribute.getName(), "PARENT PRODUCT ID");
        }
    }

    @Test
    public void shouldLoadAttributesBasedOnTypes() throws Exception {
        final ECRFSheet ecrfSheet = new ECRFSheet();
        ecrfSheet.setSheetTypeStrategy(SheetTypeStrategy.Parent);
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet();

        final Row headerRow = sheet.createRow(0);
        headerRow.createCell(0, Cell.CELL_TYPE_STRING).setCellValue("ID");

        final Row secondHeaderRow = sheet.createRow(0);
        secondHeaderRow.createCell(0, Cell.CELL_TYPE_STRING).setCellValue("ID");
        secondHeaderRow.createCell(1, Cell.CELL_TYPE_STRING).setCellValue("BLANK");
        secondHeaderRow.createCell(2, Cell.CELL_TYPE_STRING).setCellValue("BOOLEAN");
        secondHeaderRow.createCell(3, Cell.CELL_TYPE_STRING).setCellValue("STRING");
        secondHeaderRow.createCell(4, Cell.CELL_TYPE_STRING).setCellValue("NUMERIC");
        secondHeaderRow.createCell(5, Cell.CELL_TYPE_STRING).setCellValue("ERROR");
        secondHeaderRow.createCell(6, Cell.CELL_TYPE_STRING).setCellValue("FORMULA");

        final Row firstRow = sheet.createRow(2);
        firstRow.createCell(0, Cell.CELL_TYPE_STRING).setCellValue("ID1");
        firstRow.createCell(1, Cell.CELL_TYPE_BLANK).setCellValue("");
        firstRow.createCell(2, Cell.CELL_TYPE_BOOLEAN).setCellValue(true);
        firstRow.createCell(3, Cell.CELL_TYPE_STRING).setCellValue("STRING");
        firstRow.createCell(4, Cell.CELL_TYPE_NUMERIC).setCellValue(100.55);
        firstRow.createCell(5, Cell.CELL_TYPE_ERROR).setCellValue("ERROR");
        final Cell formulaCell = firstRow.createCell(6, Cell.CELL_TYPE_FORMULA);
        formulaCell.setCellFormula("E3");
        final FormulaEvaluator formulaEvaluator = workbook.getCreationHelper().createFormulaEvaluator();
        formulaEvaluator.evaluateFormulaCell(formulaCell);

        ecrfSheet.buildRowsFromSheet(sheet);
        final List<ECRFSheetModelRow> rows = ecrfSheet.getRows();
        assertThat(rows.size(), is(1));
        final ECRFSheetModelRow row = rows.get(0);
        assertThat(row.getAttributeByName("BLANK").getValue(), is(""));
        assertThat(row.getAttributeByName("BOOLEAN").getValue(), is("TRUE"));
        assertThat(row.getAttributeByName("STRING").getValue(), is("STRING"));
        assertThat(row.getAttributeByName("NUMERIC").getValue(), is("100.55"));
        assertThat(row.getAttributeByName("ERROR").getValue(), is("ERROR"));
        assertThat(row.getAttributeByName("FORMULA").getValue(), is("100.55"));
    }

    @Test
    public void shouldThrowExceptionForMissingWorksheetSpecifiedOnControlSheet() {
        exception.expect(ECRFImportException.class);
        exception.expectMessage(String.format(ECRFImportException.workSheetNotFound, "NotInHere"));
        ecrfSheetModelBuilder.build(PRODUCT_CODE_IMPORTABLE, importTestWorkBookError);
    }

    @Test
    public void shouldThrowExceptionForMissingControlSheetInWorkBook() {
        exception.expect(ECRFImportException.class);
        exception.expectMessage(String.format(ECRFImportException.controlSheetNotFound));
        ecrfSheetModelBuilder.build(PRODUCT_CODE_IMPORTABLE, importTestWorkBookNoControlSheet);
    }

    @Test
    public void shouldThrowExceptionForDuplicateParentIds() {
        exception.expect(ECRFImportException.class);
        exception.expectMessage(String.format(ECRFImportException.duplicateParentIdsFoundInSheet, "123457", "Steelhead"));
        ecrfSheetModelBuilder.build(PRODUCT_CODE_IMPORTABLE, importTestWorkBookDuplicateIds);
    }

    @Test
    public void shouldImportXLSXECRFSheet() throws Exception {
        ECRFWorkBook workBook = ecrfSheetModelBuilder.build(PRODUCT_CODE_IMPORTABLE, importXLSXWorkBook);
        assertNotNull(workBook);
        assertEquals(workBook.getSheets().size(), 2);
        assertEquals(workBook.getControlSheet().get("Connect Acceleration Site"), "S0308545");
    }

    @Test
    public void shouldImportWorkBookWithHasRelatedToFlag() throws Exception {
        ECRFWorkBook workBook = ecrfSheetModelBuilder.build(PRODUCT_CODE_BULK, importTest4GWorkBook);
        assertNotNull(workBook);
        assertTrue(workBook.hasRelatedToSheet());
    }

    @Test
    public void shouldFetchTheSheetFromWorkBookWithSheetIndex() throws Exception {
        ECRFWorkBook workBook = ecrfSheetModelBuilder.build(PRODUCT_CODE_BULK, importTest4GWorkBook);
        assertNotNull(workBook);
        assertNotNull(workBook.getSheets().get(1));
        assertNotNull(workBook.getSheetBySheetIndex(1));
    }

    @Test
    public void shouldSetSheetInformationBasedOnSheetStrategyAsParent() throws Exception {
        ECRFWorkBook workBook = ecrfSheetModelBuilder.build(PRODUCT_CODE_BULK, importTest4GWorkBook);
        assertNotNull(workBook);
        ECRFSheet parentTestSheet = workBook.getSheets().get(1);
        assertNotNull(parentTestSheet.isParentSheet());
        assertTrue(parentTestSheet.isParentSheet());
        ECRFSheetModelRow testRow = parentTestSheet.getRows().get(0);
        assertNotNull(testRow.getRowId());
        assertNull(testRow.getParentId());
        assertNull(testRow.getOwnerId());
        assertNull(testRow.getRelatedToId());
        assertNull(testRow.getRelationshipName());
    }

    @Test
    public void shouldSetSheetInformationBasedOnSheetStrategyAsChild() throws Exception {
        ECRFWorkBook workBook = ecrfSheetModelBuilder.build(PRODUCT_CODE_BULK, importTest4GWorkBook);
        assertNotNull(workBook);
        ECRFSheet childTestSheet = workBook.getSheets().get(2);
        assertNotNull(childTestSheet.isChildSheet());
        assertTrue(childTestSheet.isChildSheet());
        ECRFSheetModelRow testRow = childTestSheet.getRows().get(0);
        assertNotNull(testRow.getRowId());
        assertNotNull(testRow.getParentId());
        assertNull(testRow.getOwnerId());
        assertNull(testRow.getRelatedToId());
        assertNull(testRow.getRelationshipName());
    }

    @Test
    public void shouldSetSheetInformationBasedOnSheetStrategyAsRelated() throws Exception {
        ECRFWorkBook workBook = ecrfSheetModelBuilder.build(PRODUCT_CODE_BULK, importTest4GWorkBook);
        assertNotNull(workBook);
        ECRFSheet relatedTestSheet = workBook.getSheets().get(0);
        assertNotNull(relatedTestSheet.isRelatedProductSheet());
        assertTrue(relatedTestSheet.isRelatedProductSheet());
        ECRFSheetModelRow testRow = relatedTestSheet.getRows().get(0);
        assertNotNull(testRow.getRowId());
        assertNull(testRow.getParentId());
        assertNotNull(testRow.getOwnerId());
        assertNotNull(testRow.getRelatedToId());
        assertNotNull(testRow.getRelationshipName());
    }

    @Test
    public void shouldSetControlSheetIndex() throws Exception {
        ECRFWorkBook workBook = ecrfSheetModelBuilder.build(PRODUCT_CODE_BULK, importTestOnlyWithControlSheet);
        assertNotNull(workBook);
        assertNotNull(workBook.getControlSheet());
        assertThat(workBook.getControlSheetIndex(), is(3));
        assertThat(workBook.getSheets().size(), is(1));
        assertThat(workBook.getNonProductSheets().size(), is(2));
        assertTrue(workBook.getSheetBySheetIndex(4).isParentSheet());
    }
}
