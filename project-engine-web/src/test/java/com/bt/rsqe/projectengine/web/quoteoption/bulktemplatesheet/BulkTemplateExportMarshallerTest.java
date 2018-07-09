package com.bt.rsqe.projectengine.web.quoteoption.bulktemplatesheet;


import com.bt.rsqe.domain.product.parameters.RelationshipType;
import com.bt.rsqe.excel.ExcelStyler;
import com.bt.rsqe.pmr.client.PmrClient;
import com.bt.rsqe.projectengine.web.quoteoptionorders.rfosheet.ExportExcelMarshaller;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.*;
import static org.hamcrest.core.Is.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class BulkTemplateExportMarshallerTest {

    private final String PRODUCT_CODE = "S01234";
    private final String PRODUCT_NAME= "MBP";
    private BulkTemplateControlSheetModel bulkTemplateControlSheetModel;
    private Map<String, BulkTemplateDetailSheetModel> bulkTemplateDetailSheetModelMap;
    private PmrClient pmrClient;

    @Before
    public void before() {
        pmrClient = mock(PmrClient.class);
        BulkTemplateDetailSheetModelBuilder bulkTemplateDetailSheetModelBuilder = new BulkTemplateDetailSheetModelBuilder(pmrClient);
        List<BulkTemplateProductModel> productModels = newArrayList(new BulkTemplateProductModel(PRODUCT_CODE,PRODUCT_NAME, RelationshipType.NONE.value()));
        bulkTemplateControlSheetModel = new BulkTemplateControlSheetModel(productModels);
        bulkTemplateDetailSheetModelMap= bulkTemplateDetailSheetModelBuilder.build(productModels);

    }

    @Test
    public void shouldBuildBulkTemplateExportSheet(){

        final XSSFWorkbook xssfWorkbook = new ExportExcelMarshaller(bulkTemplateControlSheetModel,bulkTemplateDetailSheetModelMap.values()).marshall();
        assertThat(bulkTemplateControlSheetModel.getRows().size(), is(1));
        assertTrue(bulkTemplateDetailSheetModelMap.containsKey(PRODUCT_CODE));
        assertNotNull(xssfWorkbook);
    }

    @Test
    public void shouldMarshallControlSheet(){

        final XSSFWorkbook workbook = new XSSFWorkbook();
        new BulkTemplateControlSheetMarshaller(bulkTemplateControlSheetModel, workbook, new ExcelStyler(workbook)).marshall();

        assertNotNull(workbook);
        final XSSFSheet sheet = workbook.getSheet(BulkTemplateControlSheetMarshaller.CONTROL_SHEET_NAME);

        assertNotNull(sheet);
        assertThat(sheet.getLastRowNum(), is(bulkTemplateControlSheetModel.getRows().size()));

    }

    @Test
    public void shouldMarshallDetailSheet(){

        final XSSFWorkbook workbook = new XSSFWorkbook();
        BulkTemplateDetailSheetModel bulkTemplateDetailSheetModel = bulkTemplateDetailSheetModelMap.get(PRODUCT_CODE);
        new BulkTemplateDetailSheetMarshaller(bulkTemplateDetailSheetModel, workbook, new ExcelStyler(workbook)).marshall();

        assertNotNull(workbook);
        final XSSFSheet sheet = workbook.getSheet(PRODUCT_NAME);

        assertNotNull(sheet);
        assertThat(sheet.getRow(0).getCell(0).getStringCellValue(),is(BulkTemplateDetailSheetMarshaller.HeaderColumn.ID.columnName));
    }

    @Test
    public void shouldCreateMultipleDetailSheet(){

        final XSSFWorkbook workbook = new XSSFWorkbook();
        BulkTemplateDetailSheetModel bulkTemplateDetailSheetModel = bulkTemplateDetailSheetModelMap.get(PRODUCT_CODE);
        new BulkTemplateDetailSheetMarshaller(bulkTemplateDetailSheetModel, workbook, new ExcelStyler(workbook)).marshall();

        assertNotNull(workbook);
        final XSSFSheet sheet = workbook.getSheet(PRODUCT_NAME);

        assertNotNull(sheet);
        assertThat(sheet.getRow(0).getCell(0).getStringCellValue(),is(BulkTemplateDetailSheetMarshaller.HeaderColumn.ID.columnName));

        new BulkTemplateDetailSheetMarshaller(bulkTemplateDetailSheetModel, workbook, new ExcelStyler(workbook)).marshall();
        assertThat(workbook.getSheetName(1), is(PRODUCT_NAME + 1));

    }

    @Test
    public void shouldCreateDetailSheetWithRestrictedLength(){

        final XSSFWorkbook workbook = new XSSFWorkbook();
        String longSheetName="One Cloud Cisco Contract And Site";
        BulkTemplateProductModel productModel = new BulkTemplateProductModel(PRODUCT_CODE,longSheetName,RelationshipType.Child.value());
        BulkTemplateDetailSheetModel bulkTemplateDetailSheetModel = new BulkTemplateDetailSheetModel(pmrClient,productModel);
        new BulkTemplateDetailSheetMarshaller(bulkTemplateDetailSheetModel, workbook, new ExcelStyler(workbook)).marshall();
        assertNotNull(workbook);
        assertThat(workbook.getSheetName(0),is(longSheetName.substring(0,31)));

    }


}
