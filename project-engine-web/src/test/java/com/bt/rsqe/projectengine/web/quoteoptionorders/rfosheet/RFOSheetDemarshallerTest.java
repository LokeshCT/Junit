package com.bt.rsqe.projectengine.web.quoteoptionorders.rfosheet;

import com.bt.rsqe.customerinventory.client.ProductInstanceClient;
import com.bt.rsqe.customerinventory.parameter.LineItemId;
import com.bt.rsqe.domain.bom.parameters.ProductInstanceId;
import com.bt.rsqe.projectengine.web.model.LineItemModel;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static com.bt.rsqe.projectengine.web.fixtures.OrderSheetRowFixture.anOrderSheetRow;
import static com.bt.rsqe.projectengine.web.quoteoptionorders.rfosheet.RFOSheetMarshaller.Column.*;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class RFOSheetDemarshallerTest {

    private RFOSheetModel rfoSheetModel;
    private ProductInstanceClient futureProductInstanceClient;
    LineItemModel lineItem;
    private OrderSheetModel orderSheetModel;
    final String child2Scode = "childScode2";
    final String child1Scode = "childScode1";
    final String grandChildScode = "grandChildScode";
    private HSSFWorkbook workbook;

    @Before
    public void setUp() throws Exception {
        workbook = new HSSFWorkbook();


    }

    @Before
    public void before() {
        rfoSheetModel = mock(RFOSheetModel.class);
        orderSheetModel = mock(OrderSheetModel.class);
        futureProductInstanceClient = mock(ProductInstanceClient.class);
        lineItem = mock(LineItemModel.class);
    }

    @Test
    public void shouldReturnRFOModel() {
        final Sheet sheet = createProductSheetForSameTypeOfChildren("Onevoice");
        RFOSheetModel.RFORowModel rfoRowModel = createRecursiveRFORowModel();
        when(rfoSheetModel.getRFOExportModel()).thenReturn(Arrays.asList(rfoRowModel));
        when(orderSheetModel.rows()).thenReturn(asList(anOrderSheetRow().build()));
        when(orderSheetModel.billingIds()).thenReturn(asList("1", "2", "3", "4"));
        when(futureProductInstanceClient.get(lineItem.getLineItemId())).thenReturn(null);
        Map<String, RFOSheetModel> map = new HashMap<String, RFOSheetModel>();
        map.put("rootScode", rfoSheetModel);

        RFOSheetModel updatedSheetModel = RFOSheetDemarshaller.updateModelUsingSheet(rfoSheetModel, sheet);

        assertNotNull(updatedSheetModel.getRFOExportModel().get(0));
        assertNotNull(updatedSheetModel.getRFOExportModel().get(0).getChildren(child1Scode).get(0));
        assertThat(updatedSheetModel.getRFOExportModel().get(0).getChildren(child1Scode).get(0).getAttributes().get("Contact Name"), is("new Contact1"));
        assertThat(updatedSheetModel.getRFOExportModel().get(0).getChildren(child1Scode).get(0).getAttributes().get(PRODUCT_NAME.header), is("product name1"));
        assertThat(updatedSheetModel.getRFOExportModel().get(0).getChildren(child1Scode).get(1).getAttributes().get("Contact Name"), is("new Contact2"));
    }

    @Test(expected = RFOImportException.class)
    public void shouldThrowExceptionIfRfoAttributesAreChanged() {
        final Sheet sheet = createProductSheetForSameTypeOfChildren("Onevoice");
        RFOSheetModel.RFORowModel rfoRowModel = createRecursiveRFORowModelWithNewAttributes();
        when(rfoSheetModel.getRFOExportModel()).thenReturn(Arrays.asList(rfoRowModel));
        when(orderSheetModel.rows()).thenReturn(asList(anOrderSheetRow().build()));
        when(orderSheetModel.billingIds()).thenReturn(asList("1", "2", "3", "4"));
        when(futureProductInstanceClient.get(lineItem.getLineItemId())).thenReturn(null);
        Map<String, RFOSheetModel> map = new HashMap<String, RFOSheetModel>();
        map.put("rootScode", rfoSheetModel);

        RFOSheetModel updatedSheetModel = RFOSheetDemarshaller.updateModelUsingSheet(rfoSheetModel, sheet);

        assertNotNull(updatedSheetModel.getRFOExportModel().get(0));
        assertNotNull(updatedSheetModel.getRFOExportModel().get(0).getChildren(child1Scode).get(0));
        assertThat(updatedSheetModel.getRFOExportModel().get(0).getChildren(child1Scode).get(0).getAttributes().get("Contact Name"), is("new Contact1"));
        assertThat(updatedSheetModel.getRFOExportModel().get(0).getChildren(child1Scode).get(0).getAttributes().get(PRODUCT_NAME.header), is("product name1"));
        assertThat(updatedSheetModel.getRFOExportModel().get(0).getChildren(child1Scode).get(1).getAttributes().get("Contact Name"), is("new Contact2"));
    }

    @Test
    public void shouldReturnRFOModelForDifferentChildTypes() {
        final Sheet sheet = createProductSheetForDifferentTypeOfChildren("Onevoice");
        RFOSheetModel.RFORowModel rfoRowModel = createRecursiveRFORowModelWithThreeLevels();
        when(rfoSheetModel.getRFOExportModel()).thenReturn(Arrays.asList(rfoRowModel));
        when(orderSheetModel.rows()).thenReturn(asList(anOrderSheetRow().build()));
        when(orderSheetModel.billingIds()).thenReturn(asList("1", "2", "3", "4"));
        when(futureProductInstanceClient.get(lineItem.getLineItemId())).thenReturn(null);
        Map<String, RFOSheetModel> map = new HashMap<String, RFOSheetModel>();
        map.put("rootScode", rfoSheetModel);

        RFOSheetModel updatedSheetModel = RFOSheetDemarshaller.updateModelUsingSheet(rfoSheetModel, sheet);

        assertNotNull(updatedSheetModel.getRFOExportModel().get(0));
        assertNotNull(updatedSheetModel.getRFOExportModel().get(0).getChildren(child1Scode).get(0));
        assertThat(updatedSheetModel.getRFOExportModel().get(0).getChildren(child1Scode).get(0).getAttributes().get("Contact Name"), is("new Contact1"));
        assertThat(updatedSheetModel.getRFOExportModel().get(0).getChildren(child1Scode).get(0).getAttributes().get(PRODUCT_NAME.header), is("childProductName1"));
        assertThat(updatedSheetModel.getRFOExportModel().get(0).getChildren(child2Scode).get(0).getAttributes().get("Contact Name"), is("new child contact 2"));
    }

    private Sheet createProductSheetForSameTypeOfChildren(String productName) {
        final Sheet sheet = workbook.createSheet(productName);
        createScodeRowForSameTypeOfChildren(sheet);
        createHeaderRowForSameTypeOfChildren(sheet);
        populateValuesForSameTypeOfChildren(sheet);
        return sheet;
    }



    private Sheet createProductSheetForDifferentTypeOfChildren(String productName) {
        final Sheet sheet = workbook.createSheet(productName);
        final Row row = sheet.createRow(0);
        row.createCell(0).setCellValue("rootScode");
        row.createCell(1).setCellValue("rootScode");
        row.createCell(2).setCellValue("rootScode");
        row.createCell(3).setCellValue("rootScode");
        row.createCell(4).setCellValue("rootScode");
        row.createCell(5).setCellValue("childScode1");
        row.createCell(6).setCellValue("childScode1");
        row.createCell(7).setCellValue("childScode1");
        row.createCell(8).setCellValue(child2Scode);
        row.createCell(9).setCellValue(child2Scode);
        row.createCell(10).setCellValue(child2Scode);
        row.createCell(11).setCellValue(grandChildScode);
        row.createCell(12).setCellValue(grandChildScode);
        row.createCell(13).setCellValue(grandChildScode);

        final Row row1 = sheet.createRow(1);
        row1.createCell(LINE_ITEM_ID.column).setCellValue(LINE_ITEM_ID.header);
        row1.createCell(SITE_ID.column).setCellValue(SITE_ID.header);
        row1.createCell(SITE_NAME.column).setCellValue(SITE_NAME.header);
        row1.createCell(SUMMARY.column).setCellValue(SUMMARY.header);
        row1.createCell(4).setCellValue("Product Type");
        row1.createCell(5).setCellValue(PRODUCT_INSTANCE_ID.header);
        row1.createCell(6).setCellValue(PRODUCT_NAME.header);
        row1.createCell(7).setCellValue("Contact Name");

        row1.createCell(11).setCellValue(PRODUCT_INSTANCE_ID.header);
        row1.createCell(12).setCellValue(PRODUCT_NAME.header);
        row1.createCell(13).setCellValue("grand child name");

        row1.createCell(8).setCellValue(PRODUCT_INSTANCE_ID.header);
        row1.createCell(9).setCellValue(PRODUCT_NAME.header);
        row1.createCell(10).setCellValue("Contact Name");

        Row row2 = sheet.createRow(2);
        row2.createCell(0).setCellValue("lineItemId");
        row2.createCell(1).setCellValue("siteId");
        row2.createCell(2).setCellValue("siteName");
        row2.createCell(3).setCellValue("summary");
        row2.createCell(4).setCellValue("default product type");
        row2.createCell(5).setCellValue("productInstanceId1");
        row2.createCell(6).setCellValue("childProductName1");
        row2.createCell(7).setCellValue("new Contact1");

        row2.createCell(11).setCellValue("productInstanceIdGrandChild1");
        row2.createCell(12).setCellValue("grandChildProductName1");
        row2.createCell(13).setCellValue("grand child value 1");


        Row row3 = sheet.createRow(3);
        row3.createCell(11).setCellValue("productInstanceIdGrandChild2");
        row3.createCell(12).setCellValue("grandChildProductName2");
        row3.createCell(13).setCellValue("grand child value 2");

        Row row4 = sheet.createRow(4);
        row4.createCell(11).setCellValue("productInstanceIdGrandChild3");
        row4.createCell(12).setCellValue("grandChildProductName3");
        row4.createCell(13).setCellValue("grand child value 3");

        Row row5 = sheet.createRow(5);
        row5.createCell(8).setCellValue("productInstanceId2");
        row5.createCell(9).setCellValue("childProductName2");
        row5.createCell(10).setCellValue("new child contact 2");


        return sheet;
    }

    private void populateValuesForSameTypeOfChildren(Sheet sheet) {
        Row row2 = sheet.createRow(2);
        row2.createCell(0).setCellValue("lineItemId");
        row2.createCell(1).setCellValue("siteId");
        row2.createCell(2).setCellValue("siteName");
        row2.createCell(3).setCellValue("summary");
        row2.createCell(4).setCellValue("default product type");
        row2.createCell(5).setCellValue("productInstanceId1");
        row2.createCell(6).setCellValue("product name1");
        row2.createCell(7).setCellValue("new Contact1");
        Row row3 = sheet.createRow(3);
        row3.createCell(5).setCellValue("productInstanceId2");
        row3.createCell(6).setCellValue("product name2");
        row3.createCell(7).setCellValue("new Contact2");
    }

    private void createHeaderRowForSameTypeOfChildren(Sheet sheet) {
        final Row row = sheet.createRow(1);
        row.createCell(LINE_ITEM_ID.column).setCellValue(LINE_ITEM_ID.header);
        row.createCell(SITE_ID.column).setCellValue(SITE_ID.header);
        row.createCell(SITE_NAME.column).setCellValue(SITE_NAME.header);
        row.createCell(SUMMARY.column).setCellValue(SUMMARY.header);
        row.createCell(4).setCellValue("Product Type");
        row.createCell(5).setCellValue(PRODUCT_INSTANCE_ID.header);
        row.createCell(6).setCellValue(PRODUCT_NAME.header);
        row.createCell(7).setCellValue("Contact Name");
    }


    private void createScodeRowForSameTypeOfChildren(Sheet sheet) {
        final Row row = sheet.createRow(0);
        row.createCell(0).setCellValue("rootScode");
        row.createCell(1).setCellValue("rootScode");
        row.createCell(2).setCellValue("rootScode");
        row.createCell(3).setCellValue("rootScode");
        row.createCell(4).setCellValue("rootScode");
        row.createCell(5).setCellValue("childScode1");
        row.createCell(6).setCellValue("childScode1");
        row.createCell(7).setCellValue("childScode1");
    }

    private RFOSheetModel.RFORowModel createRecursiveRFORowModel() {
        RFOSheetModel.RFORowModel child1 = new RFOSheetModel.RFORowModel(new ProductInstanceId("productInstanceIdOne"), "", child1Scode);
        child1.addAttribute("Contact Name", "default Contact1");

        RFOSheetModel.RFORowModel child2 = new RFOSheetModel.RFORowModel(new ProductInstanceId("productInstanceIdTwo"), "", child1Scode);
        child2.addAttribute("Contact Name", "default Contact2");

        RFOSheetModel.RFORowModel root = new RFOSheetModel.RFORowModel(new LineItemId("lineItemId"), "siteId", "siteName", "rootScode", "summary");
        root.addAttribute("Product Type", "default product type");

        root.addChild(child1Scode, child1);
        root.addChild(child1Scode, child2);
        return root;

    }

    private RFOSheetModel.RFORowModel createRecursiveRFORowModelWithNewAttributes() {
        RFOSheetModel.RFORowModel child1 = new RFOSheetModel.RFORowModel(new ProductInstanceId("productInstanceIdOne"), "", child1Scode);
        child1.addAttribute("Contact Name", "default Contact1");

        RFOSheetModel.RFORowModel child2 = new RFOSheetModel.RFORowModel(new ProductInstanceId("productInstanceIdTwo"), "", child1Scode);
        child2.addAttribute("New Contact Name", "default Contact2");

        RFOSheetModel.RFORowModel root = new RFOSheetModel.RFORowModel(new LineItemId("lineItemId"), "siteId", "siteName", "rootScode", "summary");
        root.addAttribute("Product Type", "default product type");

        root.addChild(child1Scode, child1);
        root.addChild(child1Scode, child2);
        return root;

    }

    private RFOSheetModel.RFORowModel createRecursiveRFORowModelWithThreeLevels() {
        RFOSheetModel.RFORowModel child1 = new RFOSheetModel.RFORowModel(new ProductInstanceId("productInstanceIdOne"), "childProductName1", child1Scode);
        child1.addAttribute("Contact Name", "default Contact1");

        RFOSheetModel.RFORowModel grandChild1 = new RFOSheetModel.RFORowModel(new ProductInstanceId("productInstanceIdGrandChildOne"), "grandChildProductName1", grandChildScode);
        grandChild1.addAttribute("grand child name", "value1");

        RFOSheetModel.RFORowModel grandChild2 = new RFOSheetModel.RFORowModel(new ProductInstanceId("productInstanceIdGrandChildTwo"), "grandChildProductName2", grandChildScode);
        grandChild2.addAttribute("grand child name", "value2");

        RFOSheetModel.RFORowModel grandChild3 = new RFOSheetModel.RFORowModel(new ProductInstanceId("productInstanceIdGrandChildThree"), "grandChildProductName3", grandChildScode);
        grandChild3.addAttribute("grand child name", "value3");

        RFOSheetModel.RFORowModel child2 = new RFOSheetModel.RFORowModel(new ProductInstanceId("productInstanceIdTwo"), "childProductName2", child2Scode);
        child2.addAttribute("Contact Name", "default Contact2");

        child1.addChild(grandChildScode, grandChild1);
        child1.addChild(grandChildScode, grandChild2);
        child1.addChild(grandChildScode, grandChild3);

        RFOSheetModel.RFORowModel root = new RFOSheetModel.RFORowModel(new LineItemId("lineItemId"), "siteId", "siteName", "rootScode", "summary");
        root.addAttribute("Product Type", "default product type");

        root.addChild(child1Scode, child1);
        root.addChild(child2Scode, child2);
        return root;

    }
}
