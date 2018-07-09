package com.bt.rsqe.projectengine.web.quoteoptionorders.rfosheet;

import com.bt.rsqe.customerinventory.client.ProductInstanceClient;
import com.bt.rsqe.customerinventory.dto.AssetDTO;
import com.bt.rsqe.customerinventory.fixtures.AssetDTOFixture;
import com.bt.rsqe.customerinventory.parameter.LengthConstrainingProductInstanceId;
import com.bt.rsqe.customerinventory.parameter.LineItemId;
import com.bt.rsqe.customerinventory.parameter.RandomSiteId;
import com.bt.rsqe.customerrecord.SiteDTO;
import com.bt.rsqe.domain.ProductCategoryMigration;
import com.bt.rsqe.domain.bom.fixtures.AttributeFixture;
import com.bt.rsqe.domain.bom.fixtures.ProductOfferingFixture;
import com.bt.rsqe.domain.bom.parameters.ProductInstanceId;
import com.bt.rsqe.domain.product.ContributesToCharacteristicUpdater;
import com.bt.rsqe.domain.product.InstanceCharacteristic;
import com.bt.rsqe.domain.product.InstanceCharacteristicValue;
import com.bt.rsqe.domain.product.InstanceTreeScenario;
import com.bt.rsqe.domain.product.SimpleProductOfferingType;
import com.bt.rsqe.domain.product.constraints.AttributeValue;
import com.bt.rsqe.domain.product.parameters.ProductIdentifier;
import com.bt.rsqe.domain.project.DefaultProductInstance;
import com.bt.rsqe.domain.project.ProductInstance;
import com.bt.rsqe.productinstancemerge.MergeResult;
import com.bt.rsqe.productinstancemerge.changetracker.ChangeTracker;
import com.bt.rsqe.projectengine.migration.QuoteMigrationDetailsProvider;
import com.bt.rsqe.projectengine.web.model.LineItemModel;
import com.bt.rsqe.projectengine.web.quoteoptionorders.rfosheet.bfgcontactsattributes.BFGContactAttribute;
import com.bt.rsqe.projectengine.web.quoteoptionorders.rfosheet.bfgcontactsattributes.BFGContactsStrategy;
import com.bt.rsqe.projectengine.web.quoteoptionorders.rfosheet.bfgcontactsattributes.BFGContactsStrategyFactory;
import com.bt.rsqe.projectengine.web.quoteoptionorders.rfosheet.bfgcontactsattributes.ServiceProductsBFGContactsStrategy;
import com.google.common.base.Optional;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.List;

import static com.bt.rsqe.domain.product.InstanceTreeScenario.PROVIDE;
import static com.bt.rsqe.enums.ProductCodes.*;
import static com.bt.rsqe.productinstancemerge.ChangeType.*;
import static com.bt.rsqe.projectengine.web.fixtures.OrderSheetRowFixture.*;
import static com.bt.rsqe.projectengine.web.quoteoptionorders.rfosheet.RFOSheetMarshaller.Column.*;
import static com.google.common.collect.Lists.*;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.core.Is.*;
import static org.hamcrest.core.IsNot.*;
import static org.hamcrest.core.IsNull.*;
import static org.mockito.Mockito.*;


public class ExportExcelMarshallerTest {
    private RFOSheetModel rfoSheetModel;
    private OrderSheetModel orderSheetModel;
    final String child2Scode = "childScode2";
    final String child1Scode = "childScode1";
    final String grandChildScode = "grandChildScode";
    final String projectId = "projectId";
    final String quoteOptionId = "quoteOptionId";
    private ChangeTracker tracker;
    private QuoteMigrationDetailsProvider migrationDetailsProvider;
    private ContributesToCharacteristicUpdater contributesToCharacteristicUpdater;

    @Before
    public void before() {
        rfoSheetModel = mock(RFOSheetModel.class);
        orderSheetModel = mock(OrderSheetModel.class);
        tracker = mock(ChangeTracker.class);
        migrationDetailsProvider = mock(QuoteMigrationDetailsProvider.class);
        contributesToCharacteristicUpdater = mock(ContributesToCharacteristicUpdater.class);
        when(migrationDetailsProvider.isMigrationQuote(projectId, quoteOptionId)).thenReturn(Optional.of(false));
    }

    @Test
    public void shouldCreateExcelWithRFOHeader() throws Exception {
        RFOSheetModel.RFORowModel rfoRowModel = createRecursiveRFORowModel();
        when(rfoSheetModel.getRFOExportModel()).thenReturn(Arrays.asList(rfoRowModel));
        when(rfoSheetModel.sheetName()).thenReturn("Onevoice");
        when(rfoSheetModel.getsCode()).thenReturn("rootScode");

        when(orderSheetModel.rows()).thenReturn(asList(anOrderSheetRow().build()));
        when(orderSheetModel.billingIds()).thenReturn(asList("1", "2"));

        final Workbook workbook = new ExportExcelMarshaller(asList(rfoSheetModel), orderSheetModel, new OrderSheetColumnManager(projectId, quoteOptionId, migrationDetailsProvider)).marshall();
        assertThat(workbook, is(not(nullValue())));
        final Sheet oneVoiceSheet = workbook.getSheet("Onevoice");

        assertThat(oneVoiceSheet.getRow(0).getCell(0).getStringCellValue(), is("rootScode"));
        assertThat(oneVoiceSheet.getRow(0).getCell(1).getStringCellValue(), is("rootScode"));
        assertThat(oneVoiceSheet.getRow(0).getCell(2).getStringCellValue(), is("rootScode"));
        assertThat(oneVoiceSheet.getRow(0).getCell(3).getStringCellValue(), is("rootScode"));
        assertThat(oneVoiceSheet.getRow(0).getCell(4).getStringCellValue(), is("rootScode"));

        assertThat(oneVoiceSheet.getRow(0).getCell(5).getStringCellValue(), is(child1Scode));
        assertThat(oneVoiceSheet.getRow(0).getCell(6).getStringCellValue(), is(child1Scode));
        assertThat(oneVoiceSheet.getRow(0).getCell(7).getStringCellValue(), is(child1Scode));
        assertThat(oneVoiceSheet.getRow(0).getCell(8).getStringCellValue(), is(child1Scode));

        assertThat(oneVoiceSheet.getRow(0).getCell(9).getStringCellValue(), is(child2Scode));
        assertThat(oneVoiceSheet.getRow(0).getCell(10).getStringCellValue(), is(child2Scode));

        assertThat(oneVoiceSheet.getRow(1).getCell(0).getStringCellValue(), is(LINE_ITEM_ID.header));
        assertThat(oneVoiceSheet.getRow(1).getCell(1).getStringCellValue(), is(SITE_ID.header));
        assertThat(oneVoiceSheet.getRow(1).getCell(2).getStringCellValue(), is(SITE_NAME.header));
        assertThat(oneVoiceSheet.getRow(1).getCell(3).getStringCellValue(), is("Summary"));
        assertThat(oneVoiceSheet.getRow(1).getCell(4).getStringCellValue(), is("Product Type"));

        assertThat(oneVoiceSheet.getRow(1).getCell(5).getStringCellValue(), is(PRODUCT_INSTANCE_ID.header));
        assertThat(oneVoiceSheet.isColumnHidden(5), is(true));
        assertThat(oneVoiceSheet.getRow(1).getCell(6).getStringCellValue(), is(PRODUCT_NAME.header));
        assertThat(oneVoiceSheet.getRow(1).getCell(7).getStringCellValue(), is("Contact Name"));

        assertThat(oneVoiceSheet.getRow(1).getCell(8).getStringCellValue(), is("Duplex"));
        assertThat(oneVoiceSheet.getRow(1).getCell(9).getStringCellValue(), is(PRODUCT_INSTANCE_ID.header));
        assertThat(oneVoiceSheet.isColumnHidden(9), is(true));
        assertThat(oneVoiceSheet.getRow(1).getCell(10).getStringCellValue(), is("Product Name"));
        assertThat(oneVoiceSheet.getRow(1).getCell(11).getStringCellValue(), is("Contact Name"));
        assertThat(oneVoiceSheet.getRow(1).getCell(12).getStringCellValue(), is("Duplex"));

        assertThat(oneVoiceSheet.getRow(2).getCell(0).getStringCellValue(), is("lineItemId"));
        assertThat(oneVoiceSheet.getRow(2).getCell(1).getStringCellValue(), is("siteId"));
        assertThat(oneVoiceSheet.getRow(2).getCell(2).getStringCellValue(), is("siteName"));
        assertThat(oneVoiceSheet.getRow(2).getCell(3).getStringCellValue(), is("summary"));
        assertThat(oneVoiceSheet.getRow(2).getCell(4).getStringCellValue(), is("default product type"));

        assertThat(oneVoiceSheet.getRow(2).getCell(5).getStringCellValue(), is("productInstanceIdOne"));
        assertThat(oneVoiceSheet.getRow(2).getCell(6).getStringCellValue(), is("Steelhead1"));
        assertThat(oneVoiceSheet.getRow(2).getCell(7).getStringCellValue(), is("default Contact1"));

        assertThat(oneVoiceSheet.getRow(3).getCell(9).getStringCellValue(), is("productInstanceIdTwo"));
        assertThat(oneVoiceSheet.getRow(3).getCell(10).getStringCellValue(), is("Steelhead2"));
        assertThat(oneVoiceSheet.getRow(3).getCell(11).getStringCellValue(), is("default Contact2"));

        assertThat(oneVoiceSheet.isColumnHidden(OrderSheetMarshaller.Column.LINE_ITEM_ID.column), is(true));

        final Sheet orderDetailsSheet = workbook.getSheet(OrderSheetMarshaller.SHEET_NAME);
       assertThat(oneVoiceSheet.getRow(0).getZeroHeight(), is(true));

        final Row header = orderDetailsSheet.getRow(0);
        assertThat(header.getCell(OrderSheetMarshaller.Column.SITE_ID.column).getStringCellValue(), is(OrderSheetMarshaller.Column.SITE_ID.header));
    }

    @Test
    public void shouldCreateExcelWithRecursive() throws Exception {
        RFOSheetModel.RFORowModel rfoRowModel = createRecursiveRFORowModelWithThreeLevels();
        when(rfoSheetModel.getRFOExportModel()).thenReturn(Arrays.asList(rfoRowModel));
        when(rfoSheetModel.sheetName()).thenReturn("Onevoice");
        when(rfoSheetModel.getsCode()).thenReturn("rootScode");

        when(orderSheetModel.rows()).thenReturn(asList(anOrderSheetRow().build()));
        when(orderSheetModel.billingIds()).thenReturn(asList("1", "2", "3", "4"));

        final Workbook workbook = new ExportExcelMarshaller(asList(rfoSheetModel), orderSheetModel, new OrderSheetColumnManager(projectId, quoteOptionId, migrationDetailsProvider)).marshall();
        assertThat(workbook, is(not(nullValue())));
        final Sheet oneVoiceSheet = workbook.getSheet("Onevoice");

        assertThat(oneVoiceSheet.getRow(0).getCell(0).getStringCellValue(), is("rootScode"));
        assertThat(oneVoiceSheet.getRow(0).getCell(1).getStringCellValue(), is("rootScode"));
        assertThat(oneVoiceSheet.getRow(0).getCell(2).getStringCellValue(), is("rootScode"));
        assertThat(oneVoiceSheet.getRow(0).getCell(3).getStringCellValue(), is("rootScode"));
        assertThat(oneVoiceSheet.getRow(0).getCell(4).getStringCellValue(), is("rootScode"));

        assertThat(oneVoiceSheet.getRow(0).getCell(5).getStringCellValue(), is(child1Scode));
        assertThat(oneVoiceSheet.getRow(0).getCell(6).getStringCellValue(), is(child1Scode));
        assertThat(oneVoiceSheet.getRow(0).getCell(7).getStringCellValue(), is(child1Scode));

        assertThat(oneVoiceSheet.getRow(0).getCell(8).getStringCellValue(), is(child2Scode));
        assertThat(oneVoiceSheet.getRow(0).getCell(9).getStringCellValue(), is(child2Scode));
        assertThat(oneVoiceSheet.getRow(0).getCell(10).getStringCellValue(), is(child2Scode));

        assertThat(oneVoiceSheet.getRow(0).getCell(11).getStringCellValue(), is(grandChildScode));
        assertThat(oneVoiceSheet.getRow(0).getCell(12).getStringCellValue(), is(grandChildScode));
        assertThat(oneVoiceSheet.getRow(0).getCell(13).getStringCellValue(), is(grandChildScode));

        assertThat(oneVoiceSheet.getRow(1).getCell(0).getStringCellValue(), is(LINE_ITEM_ID.header));
        assertThat(oneVoiceSheet.getRow(1).getCell(1).getStringCellValue(), is(SITE_ID.header));
        assertThat(oneVoiceSheet.getRow(1).getCell(2).getStringCellValue(), is(SITE_NAME.header));
        assertThat(oneVoiceSheet.getRow(1).getCell(3).getStringCellValue(), is("Summary"));
        assertThat(oneVoiceSheet.getRow(1).getCell(4).getStringCellValue(), is("Product Type"));

        assertThat(oneVoiceSheet.getRow(1).getCell(5).getStringCellValue(), is(PRODUCT_INSTANCE_ID.header));
        assertThat(oneVoiceSheet.getRow(1).getCell(6).getStringCellValue(), is(PRODUCT_NAME.header));
        assertThat(oneVoiceSheet.getRow(1).getCell(7).getStringCellValue(), is("Contact Name"));

        assertThat(oneVoiceSheet.getRow(1).getCell(8).getStringCellValue(), is(PRODUCT_INSTANCE_ID.header));
        assertThat(oneVoiceSheet.getRow(1).getCell(9).getStringCellValue(), is(PRODUCT_NAME.header));
        assertThat(oneVoiceSheet.getRow(1).getCell(10).getStringCellValue(), is("Contact Name"));

        assertThat(oneVoiceSheet.getRow(1).getCell(11).getStringCellValue(), is(PRODUCT_INSTANCE_ID.header));
        assertThat(oneVoiceSheet.getRow(1).getCell(12).getStringCellValue(), is(PRODUCT_NAME.header));
        assertThat(oneVoiceSheet.getRow(1).getCell(13).getStringCellValue(), is("grand child name"));

        assertThat(oneVoiceSheet.getRow(2).getCell(0).getStringCellValue(), is("lineItemId"));
        assertThat(oneVoiceSheet.getRow(2).getCell(1).getStringCellValue(), is("siteId"));
        assertThat(oneVoiceSheet.getRow(2).getCell(2).getStringCellValue(), is("siteName"));
        assertThat(oneVoiceSheet.getRow(2).getCell(3).getStringCellValue(), is("summary"));
        assertThat(oneVoiceSheet.getRow(2).getCell(4).getStringCellValue(), is("default product type"));

        assertThat(oneVoiceSheet.getRow(2).getCell(5).getStringCellValue(), is("productInstanceIdOne"));
        assertThat(oneVoiceSheet.getRow(2).getCell(6).getStringCellValue(), is(""));
        assertThat(oneVoiceSheet.getRow(2).getCell(7).getStringCellValue(), is("default Contact1"));

        assertThat(oneVoiceSheet.getRow(2).getCell(11).getStringCellValue(), is("productInstanceIdGrandChildOne"));
        assertThat(oneVoiceSheet.getRow(2).getCell(12).getStringCellValue(), is(""));
        assertThat(oneVoiceSheet.getRow(2).getCell(13).getStringCellValue(), is("value1"));

        assertThat(oneVoiceSheet.getRow(3).getCell(11).getStringCellValue(), is("productInstanceIdGrandChildTwo"));
        assertThat(oneVoiceSheet.getRow(3).getCell(12).getStringCellValue(), is(""));
        assertThat(oneVoiceSheet.getRow(3).getCell(13).getStringCellValue(), is("value2"));

        assertThat(oneVoiceSheet.getRow(4).getCell(11).getStringCellValue(), is("productInstanceIdGrandChildThree"));
        assertThat(oneVoiceSheet.getRow(4).getCell(12).getStringCellValue(), is(""));
        assertThat(oneVoiceSheet.getRow(4).getCell(13).getStringCellValue(), is("value3"));

        assertThat(oneVoiceSheet.getRow(5).getCell(8).getStringCellValue(), is("productInstanceIdTwo"));
        assertThat(oneVoiceSheet.getRow(5).getCell(9).getStringCellValue(), is(""));
        assertThat(oneVoiceSheet.getRow(5).getCell(10).getStringCellValue(), is("default Contact2"));

        final Sheet orderDetailsSheet = workbook.getSheet(OrderSheetMarshaller.SHEET_NAME);
        final Row header = orderDetailsSheet.getRow(0);
        assertThat(header.getCell(OrderSheetMarshaller.Column.SITE_ID.column).getStringCellValue(), is(OrderSheetMarshaller.Column.SITE_ID.header));
        assertThat(orderDetailsSheet.isColumnHidden(OrderSheetMarshaller.Column.LINE_ITEM_ID.column), is(true));
    }

    private RFOSheetModel.RFORowModel createRecursiveRFORowModel() {
        RFOSheetModel.RFORowModel child1 = new RFOSheetModel.RFORowModel(new ProductInstanceId("productInstanceIdOne"), "Steelhead1", child1Scode);
        child1.addAttribute("Contact Name", "default Contact1");
        child1.addAttribute("Duplex", "full duplex");
        child1.addAllowedValues("Duplex", newArrayList("half duplex", "full duplex"));

        RFOSheetModel.RFORowModel child2 = new RFOSheetModel.RFORowModel(new ProductInstanceId("productInstanceIdTwo"), "Steelhead2", child2Scode);
        child2.addAttribute("Contact Name", "default Contact2");
        child2.addAttribute("Duplex", "full duplex");
        child2.addAllowedValues("Duplex", newArrayList("half duplex", "full duplex"));

        RFOSheetModel.RFORowModel root = new RFOSheetModel.RFORowModel(new LineItemId("lineItemId"), "siteId", "siteName", "rootScode", "summary");
        root.addAttribute("Product Type", "default product type");

        root.addChild(child1Scode, child1);
        root.addChild(child2Scode, child2);
        return root;

    }

    private RFOSheetModel.RFORowModel createRecursiveRFORowModelWithThreeLevels() {
        RFOSheetModel.RFORowModel child1 = new RFOSheetModel.RFORowModel(new ProductInstanceId("productInstanceIdOne"), "", child1Scode);
        child1.addAttribute("Contact Name", "default Contact1");

        RFOSheetModel.RFORowModel grandChild1 = new RFOSheetModel.RFORowModel(new ProductInstanceId("productInstanceIdGrandChildOne"), "", grandChildScode);
        grandChild1.addAttribute("grand child name", "value1");

        RFOSheetModel.RFORowModel grandChild2 = new RFOSheetModel.RFORowModel(new ProductInstanceId("productInstanceIdGrandChildTwo"), "", grandChildScode);
        grandChild2.addAttribute("grand child name", "value2");

        RFOSheetModel.RFORowModel grandChild3 = new RFOSheetModel.RFORowModel(new ProductInstanceId("productInstanceIdGrandChildThree"), "", grandChildScode);
        grandChild3.addAttribute("grand child name", "value3");

        RFOSheetModel.RFORowModel child2 = new RFOSheetModel.RFORowModel(new ProductInstanceId("productInstanceIdTwo"), "", child2Scode);
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

    @Test
    public void shouldCreateExcelWithAdditionalHeadersForChannelAnalystAndChannelConsultant() throws Exception {
        RFOSheetModel.RFORowModel rfoRowModel = createRowModelWithAdditionalAttributes(true, true);
        when(rfoSheetModel.getRFOExportModel()).thenReturn(Arrays.asList(rfoRowModel));
        String sheetName = "ConnectAcceleration";
        when(rfoSheetModel.sheetName()).thenReturn(sheetName);
        when(rfoSheetModel.getsCode()).thenReturn(ConnectAccelerationService.productCode());

        when(orderSheetModel.rows()).thenReturn(asList(anOrderSheetRow().build()));
        when(orderSheetModel.billingIds()).thenReturn(asList("1", "2", "3", "4"));

        final Workbook workbook = new ExportExcelMarshaller(asList(rfoSheetModel), orderSheetModel, new OrderSheetColumnManager(projectId, quoteOptionId, migrationDetailsProvider)).marshall();
        assertThat(workbook, is(not(nullValue())));
        final Sheet caSheet = workbook.getSheet(sheetName);

        assertThat(caSheet.getRow(1).getCell(3).getStringCellValue(), is("Summary"));
        assertThat(caSheet.getRow(1).getCell(4).getStringCellValue(), is("Channel Consultant First Name (M)"));
        assertThat(caSheet.getRow(1).getCell(5).getStringCellValue(), is("Channel Consultant Last Name (M)"));
        assertThat(caSheet.getRow(1).getCell(6).getStringCellValue(), is("Channel Consultant Job Title (M)"));
        assertThat(caSheet.getRow(1).getCell(7).getStringCellValue(), is("Channel Consultant Phone Number (M)"));
        assertThat(caSheet.getRow(1).getCell(8).getStringCellValue(), is("Channel Consultant User Name EIN (M)"));
        assertThat(caSheet.getRow(1).getCell(9).getStringCellValue(), is("Channel Consultant Email Address (M)"));

        assertThat(caSheet.getRow(1).getCell(10).getStringCellValue(), is("Channel Analyst First Name (M)"));
        assertThat(caSheet.getRow(1).getCell(11).getStringCellValue(), is("Channel Analyst Last Name (M)"));
        assertThat(caSheet.getRow(1).getCell(12).getStringCellValue(), is("Channel Analyst Job Title (M)"));
        assertThat(caSheet.getRow(1).getCell(13).getStringCellValue(), is("Channel Analyst Phone Number (M)"));
        assertThat(caSheet.getRow(1).getCell(14).getStringCellValue(), is("Channel Analyst User Name EIN (M)"));
        assertThat(caSheet.getRow(1).getCell(15).getStringCellValue(), is("Channel Analyst Email Address (M)"));

    }

    @Test
    public void shouldCreateExcelWithAdditionalHeadersForChannelAnalystWhenCentralAnalystIsNotRequired() throws Exception {
        RFOSheetModel.RFORowModel rfoRowModel = createRowModelWithAdditionalAttributes(false, true);
        when(rfoSheetModel.getRFOExportModel()).thenReturn(Arrays.asList(rfoRowModel));
        String sheetName = "ConnectAcceleration";
        when(rfoSheetModel.sheetName()).thenReturn(sheetName);
        when(rfoSheetModel.getsCode()).thenReturn(ConnectAccelerationService.productCode());

        when(orderSheetModel.rows()).thenReturn(asList(anOrderSheetRow().build()));
        when(orderSheetModel.billingIds()).thenReturn(asList("1", "2", "3", "4"));

        final Workbook workbook = new ExportExcelMarshaller(asList(rfoSheetModel), orderSheetModel, new OrderSheetColumnManager(projectId, quoteOptionId, migrationDetailsProvider)).marshall();
        assertThat(workbook, is(not(nullValue())));
        final Sheet caSheet = workbook.getSheet(sheetName);

        assertThat(caSheet.getRow(1).getCell(3).getStringCellValue(), is("Summary"));
        assertThat(caSheet.getRow(1).getCell(4).getStringCellValue(), is("Channel Analyst First Name (M)"));
        assertThat(caSheet.getRow(1).getCell(5).getStringCellValue(), is("Channel Analyst Last Name (M)"));
        assertThat(caSheet.getRow(1).getCell(6).getStringCellValue(), is("Channel Analyst Job Title (M)"));
        assertThat(caSheet.getRow(1).getCell(7).getStringCellValue(), is("Channel Analyst Phone Number (M)"));
        assertThat(caSheet.getRow(1).getCell(8).getStringCellValue(), is("Channel Analyst User Name EIN (M)"));
        assertThat(caSheet.getRow(1).getCell(9).getStringCellValue(), is("Channel Analyst Email Address (M)"));
    }

    @Test
    public void shouldCreateExcelWithAdditionalHeadersForChannelConsultantWhenCentralConsultantIsNotRequired() throws Exception {
        RFOSheetModel.RFORowModel rfoRowModel = createRowModelWithAdditionalAttributes(true, false);
        when(rfoSheetModel.getRFOExportModel()).thenReturn(Arrays.asList(rfoRowModel));
        String sheetName = "ConnectAcceleration";
        when(rfoSheetModel.sheetName()).thenReturn(sheetName);
        when(rfoSheetModel.getsCode()).thenReturn(ConnectAccelerationService.productCode());

        when(orderSheetModel.rows()).thenReturn(asList(anOrderSheetRow().build()));
        when(orderSheetModel.billingIds()).thenReturn(asList("1", "2", "3", "4"));

        final Workbook workbook = new ExportExcelMarshaller(asList(rfoSheetModel), orderSheetModel, new OrderSheetColumnManager(projectId, quoteOptionId, migrationDetailsProvider)).marshall();
        assertThat(workbook, is(not(nullValue())));
        final Sheet caSheet = workbook.getSheet(sheetName);

        assertThat(caSheet.getRow(1).getCell(3).getStringCellValue(), is("Summary"));
        assertThat(caSheet.getRow(1).getCell(4).getStringCellValue(), is("Channel Consultant First Name (M)"));
        assertThat(caSheet.getRow(1).getCell(5).getStringCellValue(), is("Channel Consultant Last Name (M)"));
        assertThat(caSheet.getRow(1).getCell(6).getStringCellValue(), is("Channel Consultant Job Title (M)"));
        assertThat(caSheet.getRow(1).getCell(7).getStringCellValue(), is("Channel Consultant Phone Number (M)"));
        assertThat(caSheet.getRow(1).getCell(8).getStringCellValue(), is("Channel Consultant User Name EIN (M)"));
        assertThat(caSheet.getRow(1).getCell(9).getStringCellValue(), is("Channel Consultant Email Address (M)"));
    }

    @Test
    public void shouldNotCreateExcelWithAdditionalHeadersForChannelConsultantAndChannelAnalyst() throws Exception {
        RFOSheetModel.RFORowModel rfoRowModel = createRowModelWithAdditionalAttributes(false, false);
        when(rfoSheetModel.getRFOExportModel()).thenReturn(Arrays.asList(rfoRowModel));
        String sheetName = "ConnectAcceleration";
        when(rfoSheetModel.sheetName()).thenReturn(sheetName);
        when(rfoSheetModel.getsCode()).thenReturn(ConnectAccelerationService.productCode());

        when(orderSheetModel.rows()).thenReturn(asList(anOrderSheetRow().build()));
        when(orderSheetModel.billingIds()).thenReturn(asList("1", "2", "3", "4"));

        final Workbook workbook = new ExportExcelMarshaller(asList(rfoSheetModel), orderSheetModel, new OrderSheetColumnManager(projectId, quoteOptionId, migrationDetailsProvider)).marshall();
        assertThat(workbook, is(not(nullValue())));
        final Sheet caSheet = workbook.getSheet(sheetName);
        assertThat(caSheet.getRow(1).getCell(4).getStringCellValue(), not("Channel Consultant First Name (M)"));
        assertThat(caSheet.getRow(1).getCell(4).getStringCellValue(), not("Channel Analyst First Name (M)"));
    }

    @Test
    public void shouldUseHeadersFromEachExportModel() throws Exception {
        RFOSheetModel.RFORowModel root1 = new RFOSheetModel.RFORowModel(new LineItemId("lineItemId"), "siteId", "siteName", "rootScode", "summary");
        root1.addAttribute("Product Type1", "default product type1");

        RFOSheetModel.RFORowModel root2 = new RFOSheetModel.RFORowModel(new LineItemId("lineItemId"), "siteId", "siteName", "rootScode", "summary");
        root2.addAttribute("Product Type2", "default product type2");

        when(rfoSheetModel.getRFOExportModel()).thenReturn(Arrays.asList(root1, root2));
        when(rfoSheetModel.sheetName()).thenReturn("Onevoice");
        when(rfoSheetModel.getsCode()).thenReturn("rootScode");
        when(orderSheetModel.billingIds()).thenReturn(asList("1", "2", "3", "4"));

        final Workbook workbook = new ExportExcelMarshaller(asList(rfoSheetModel), orderSheetModel, new OrderSheetColumnManager(projectId, quoteOptionId, migrationDetailsProvider)).marshall();
        assertThat(workbook, is(not(nullValue())));
        final Sheet oneVoiceSheet = workbook.getSheet("Onevoice");

        assertThat(oneVoiceSheet.getRow(1).getCell(3).getStringCellValue(), is("Summary"));
        assertThat(oneVoiceSheet.getRow(1).getCell(4).getStringCellValue(), is("Product Type1"));
        assertThat(oneVoiceSheet.getRow(1).getCell(5).getStringCellValue(), is("Product Type2"));
    }

    private RFOSheetModel.RFORowModel createRowModelWithAdditionalAttributes(boolean addConsultant, boolean addAnalyst) throws Exception {
        String vcmc = "CMC-VE";
        String licenseSupport = "License Support";
        String child1Scode = "child1";
        String child2Scode = "child2";
        String child3Scode = "child3";

        RFOSheetModel.RFORowModel child1 = new RFOSheetModel.RFORowModel(new ProductInstanceId("productInstanceIdOne"), "", vcmc);
        child1.addAttribute("Contact Name", "default Contact1");

        RFOSheetModel.RFORowModel grandChild1 = new RFOSheetModel.RFORowModel(new ProductInstanceId("productInstanceIdGrandChildOne"), "", child1Scode);
        grandChild1.addAttribute("grand child name", "value1");

        RFOSheetModel.RFORowModel grandChild2 = new RFOSheetModel.RFORowModel(new ProductInstanceId("productInstanceIdGrandChildTwo"), "", child2Scode);
        grandChild2.addAttribute("grand child name", "value2");

        RFOSheetModel.RFORowModel grandChild3 = new RFOSheetModel.RFORowModel(new ProductInstanceId("productInstanceIdGrandChildThree"), "", child3Scode);
        grandChild3.addAttribute("grand child name", "value3");

        RFOSheetModel.RFORowModel child2 = new RFOSheetModel.RFORowModel(new ProductInstanceId("productInstanceIdTwo"), "", licenseSupport);
        child2.addAttribute("Contact Name", "default Contact2");

        child1.addChild(child1Scode, grandChild1);
        child1.addChild(child2Scode, grandChild2);
        child1.addChild(child3Scode, grandChild3);

        RFOSheetModel.RFORowModel root = createRowModelForRootProduct(ConnectAccelerationService.productCode(), addConsultant, addAnalyst);

        root.addChild(vcmc, child1);
        root.addChild(licenseSupport, child2);
        return root;

    }

    private RFOSheetModel.RFORowModel createRowModelForRootProduct(String sCode, final boolean addConsultant, final boolean addAnalyst) throws Exception {
        LineItemId lineItemId = new LineItemId("someLineItemId");

        ProductInstanceClient productInstanceClient = mock(ProductInstanceClient.class);
        LineItemModel lineItemModel = mock(LineItemModel.class);
        ProductInstance productInstance = mock(DefaultProductInstance.class);

        InstanceCharacteristic centralConsultantAttribute = new InstanceCharacteristic(AttributeFixture.anAttribute()
                                                                                                       .called(ServiceProductsBFGContactsStrategy.CENTRAL_CONSULTANT_REQUIRED)
                                                                                                       .withAllowedValues(AttributeValue.newInstance("No"))
                                                                                                       .build(), new InstanceCharacteristic.ValueChangeListener() {
                                                                                                           @Override
                                                                                                           public void valueChanged(InstanceCharacteristic instanceCharacteristic, InstanceCharacteristicValue oldValue, InstanceCharacteristicValue newValue) {
                                                                                                               // null impl - do nothing
                                                                                                           }
                                                                                                       });

        InstanceCharacteristic centralAnalystAttribute = new InstanceCharacteristic(AttributeFixture.anAttribute()
                                                                                                    .called(ServiceProductsBFGContactsStrategy.CENTRAL_ANALYST_REQUIRED)
                                                                                                    .withAllowedValues(AttributeValue.newInstance("No"))
                                                                                                    .build(), new InstanceCharacteristic.ValueChangeListener() {
                                                                                                        @Override
                                                                                                        public void valueChanged(InstanceCharacteristic instanceCharacteristic, InstanceCharacteristicValue oldValue, InstanceCharacteristicValue newValue) {
                                                                                                            // null impl - do nothing
                                                                                                        }
                                                                                                    });
        when(lineItemModel.getSite()).thenReturn(new SiteDTO(new RandomSiteId().value(), "someSiteName"));
        when(lineItemModel.getLineItemId()).thenReturn(lineItemId);
        when(productInstance.getProductOffering()).thenReturn(new ProductOfferingFixture().withSimpleProductOfferingType(SimpleProductOfferingType.CentralService).withProductIdentifier(sCode).build());
        when(productInstance.getProductIdentifier()).thenReturn(new ProductIdentifier(sCode, "versionNumber"));
        when(productInstanceClient.get(lineItemId)).thenReturn(productInstance);

        final AssetDTO asset = AssetDTOFixture.anAsset().build();
        when(lineItemModel.getRootInstance()).thenReturn(asset);
        when(productInstance.getProductInstanceId()).thenReturn(new ProductInstanceId("someId"));
        when(productInstanceClient.convertAssetToLightweightInstance(asset)).thenReturn(productInstance);
        when(productInstanceClient.getSourceAssetDTO(productInstance.getProductInstanceId().getValue())).thenReturn(Optional.<AssetDTO>absent());
        MergeResult mergeResult = new MergeResult(newArrayList(productInstance), tracker);
        when(productInstanceClient.getMergeResult(productInstance, null, PROVIDE)).thenReturn(mergeResult);
        when(tracker.changeFor(productInstance)).thenReturn(ADD);
        when(productInstanceClient.getAssetsDiff(anyString(), anyLong(), anyLong(), any(InstanceTreeScenario.class))).thenReturn(mergeResult);
        when(productInstanceClient.getSourceAsset(any(LengthConstrainingProductInstanceId.class))).thenReturn(Optional.<ProductInstance>absent());
        if (addConsultant) {
            when(productInstance.getInstanceCharacteristic(ServiceProductsBFGContactsStrategy.CENTRAL_CONSULTANT_REQUIRED)).thenReturn(centralConsultantAttribute);
        }

        if (addAnalyst) {
            when(productInstance.getInstanceCharacteristic(ServiceProductsBFGContactsStrategy.CENTRAL_ANALYST_REQUIRED)).thenReturn(centralAnalystAttribute);
        }

        ProductInstanceClient futureProductInstanceClient = mock(ProductInstanceClient.class);
        when(futureProductInstanceClient.get(lineItemModel.getLineItemId())).thenReturn(productInstance);

        BFGContactsStrategyFactory bfgContactsStrategyFactory = mock(BFGContactsStrategyFactory.class);
        BFGContactsStrategy caServiceBFGContactsStrategy = mock(ServiceProductsBFGContactsStrategy.class);
        List<BFGContactAttribute> bfgContactAttributes = newArrayList();

        if (addConsultant) {
            bfgContactAttributes.add(new BFGContactAttribute("Channel Consultant First Name", "Value1"));
            bfgContactAttributes.add(new BFGContactAttribute("Channel Consultant Last Name", "Value2"));
            bfgContactAttributes.add(new BFGContactAttribute("Channel Consultant Job Title", "Value3"));
            bfgContactAttributes.add(new BFGContactAttribute("Channel Consultant Phone Number", "Value4"));
            bfgContactAttributes.add(new BFGContactAttribute("Channel Consultant User Name EIN", "Value4"));
            bfgContactAttributes.add(new BFGContactAttribute("Channel Consultant Email Address", "Value4"));
        }

        if (addAnalyst) {
            bfgContactAttributes.add(new BFGContactAttribute("Channel Analyst First Name", "Value1"));
            bfgContactAttributes.add(new BFGContactAttribute("Channel Analyst Last Name", "Value2"));
            bfgContactAttributes.add(new BFGContactAttribute("Channel Analyst Job Title", "Value3"));
            bfgContactAttributes.add(new BFGContactAttribute("Channel Analyst Phone Number", "Value4"));
            bfgContactAttributes.add(new BFGContactAttribute("Channel Analyst User Name EIN", "Value4"));
            bfgContactAttributes.add(new BFGContactAttribute("Channel Analyst Email Address", "Value4"));
        }

        when(caServiceBFGContactsStrategy.getBFGContactsAttributes(productInstance)).thenReturn(bfgContactAttributes);
        when(bfgContactsStrategyFactory.getStrategyFor(productInstance.getSimpleProductOfferingType())).thenReturn((Optional) Optional.of(caServiceBFGContactsStrategy));
        when(migrationDetailsProvider.conditionalFor(Mockito.any(ProductInstance.class))).thenCallRealMethod();
        when(migrationDetailsProvider.isMigrationQuote(Mockito.any(String.class), Mockito.any(String.class))).thenReturn(Optional.<Boolean>absent());
        when(migrationDetailsProvider.getMigrationDetailsForProductCode(Mockito.any(String.class))).thenReturn(Optional.<ProductCategoryMigration>absent());

        RFOSheetModel rfoSheetModel = new RFOSheetModel(bfgContactsStrategyFactory, productInstanceClient, "ConnectAcceleration", ConnectAccelerationService.productCode(), migrationDetailsProvider, contributesToCharacteristicUpdater);
        rfoSheetModel.add(lineItemModel, sCode);
        return rfoSheetModel.getRFOExportModel().get(0);
    }
}
