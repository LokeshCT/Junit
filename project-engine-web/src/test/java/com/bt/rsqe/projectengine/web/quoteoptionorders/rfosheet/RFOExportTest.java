package com.bt.rsqe.projectengine.web.quoteoptionorders.rfosheet;

import com.bt.rsqe.customerinventory.client.CustomerInventoryClientManager;
import com.bt.rsqe.customerinventory.client.CustomerInventoryStubClientManagerFactory;
import com.bt.rsqe.customerinventory.client.ProductInstanceClient;
import com.bt.rsqe.customerinventory.fixtures.LineItemIdFixture;
import com.bt.rsqe.customerinventory.parameter.CustomerId;
import com.bt.rsqe.customerinventory.parameter.LengthConstrainingProductInstanceId;
import com.bt.rsqe.customerinventory.parameter.LineItemId;
import com.bt.rsqe.customerinventory.parameter.ProductCode;
import com.bt.rsqe.customerinventory.parameter.ProductInstanceVersion;
import com.bt.rsqe.customerinventory.parameter.ProductVersion;
import com.bt.rsqe.customerinventory.parameter.ProjectId;
import com.bt.rsqe.customerinventory.parameter.QuoteOptionId;
import com.bt.rsqe.customerinventory.parameter.RandomSiteId;
import com.bt.rsqe.customerinventory.parameter.SiteId;
import com.bt.rsqe.customerrecord.BillingAccountDTO;
import com.bt.rsqe.customerrecord.SiteDTO;
import com.bt.rsqe.domain.ProductCategoryMigration;
import com.bt.rsqe.domain.StencilId;
import com.bt.rsqe.domain.bom.fixtures.ProductOfferingFixture;
import com.bt.rsqe.domain.product.Attribute;
import com.bt.rsqe.domain.product.ContributesToCharacteristicUpdater;
import com.bt.rsqe.domain.product.DefaultProductInstanceFixture;
import com.bt.rsqe.domain.product.InstanceCharacteristic;
import com.bt.rsqe.domain.product.InstanceTreeScenario;
import com.bt.rsqe.domain.product.parameters.ProductIdentifier;
import com.bt.rsqe.domain.project.InstanceCharacteristicNotFound;
import com.bt.rsqe.domain.project.ProductInstance;
import com.bt.rsqe.domain.project.ProductInstanceFactory;
import com.bt.rsqe.domain.project.StubCountryResolver;
import com.bt.rsqe.enums.AssetType;
import com.bt.rsqe.pmr.client.PmrClient;
import com.bt.rsqe.pmr.client.PmrMocker;
import com.bt.rsqe.projectengine.migration.QuoteMigrationDetailsProvider;
import com.bt.rsqe.projectengine.web.model.LineItemModel;
import com.bt.rsqe.utils.countries.Countries;
import com.google.common.base.Optional;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.bt.rsqe.projectengine.web.quoteoptionorders.rfosheet.RFOSheetModel.*;
import static com.google.common.collect.Lists.*;
import static java.util.Arrays.asList;
import static org.apache.poi.ss.usermodel.Cell.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@Ignore("The tests in this class are flaky for some reason and require further investigation.")
public class RFOExportTest {
    private CustomerInventoryClientManager cifClientManager;
    private ProductInstanceFactory productInstanceFactory;
    private ProductInstanceClient futureProductInstanceClient;
    private QuoteMigrationDetailsProvider migrationDetailsProvider;
    private static final String SHEET_NAME = "Onevoice";

    private LineItemId lineItemId;
    private SiteId siteId;
    private SiteDTO siteInfo;
    private RFOSheetModel rfoSheetModel;
    private DateTime signedOnDate;
    private String billingId;
    private String productName;
    private CustomerId customerId;
    private QuoteOptionId quoteOptionId;
    private static final String scode = "S0205086";
    private static final String version = "A.1";
    private ProjectId projectId;
    private String contractTerm = "24";
    private ContributesToCharacteristicUpdater contributesToCharacteristicUpdater;

    @Before
    public void setup() throws Exception {
        PmrClient pmr = mock(PmrClient.class);
        PmrMocker.returnForProduct(pmr, ProductOfferingFixture.aProductOffering().withProductIdentifier(new ProductIdentifier(scode, version)).build());

        cifClientManager = CustomerInventoryStubClientManagerFactory.getClientManager(pmr);
        productInstanceFactory = ProductInstanceFactory.getProductInstanceFactory(pmr, cifClientManager, StubCountryResolver.resolveTo(Countries.byIsoStatic("GB")));
        futureProductInstanceClient = cifClientManager.getProductInstanceClient();
        migrationDetailsProvider = mock(QuoteMigrationDetailsProvider.class);
        contributesToCharacteristicUpdater = mock(ContributesToCharacteristicUpdater.class);

        lineItemId = LineItemIdFixture.aUniqueLineItemId().build();
        siteId = new RandomSiteId();
        siteInfo = new SiteDTO(siteId.value(), "Site 1");
        rfoSheetModel = new RFOSheetModel(futureProductInstanceClient, SHEET_NAME, scode, migrationDetailsProvider, contributesToCharacteristicUpdater);
        signedOnDate = new DateTime();
        billingId = "billing id";
        productName = "product name";
        customerId = new CustomerId("customerId");
        quoteOptionId = new QuoteOptionId("quoteOptionId");
        projectId = new ProjectId("projectId");
        when(migrationDetailsProvider.conditionalFor(Mockito.any(ProductInstance.class))).thenCallRealMethod();
        when(migrationDetailsProvider.isMigrationQuote(projectId.value(), quoteOptionId.value())).thenReturn(Optional.<Boolean>absent());
        when(migrationDetailsProvider.getMigrationDetailsForProductCode(Mockito.any(String.class))).thenReturn(Optional.<ProductCategoryMigration>absent());
    }

    @Test
    public void shouldCreateExportRFOModelForOneLineItemIdWhereNoUnderlyingProductConfigurationStored() throws Exception {
        ProductInstance onevoice = productInstanceFactory.newFutureProductInstance(new LengthConstrainingProductInstanceId(),
                                                                                   ProductInstanceVersion.DEFAULT_VALUE, lineItemId,
                                                                                   new ProductCode(scode),
                                                                                   new ProductVersion(version), siteId, StencilId.NIL, customerId, null, quoteOptionId, AssetType.REAL,
                                                                                   projectId, contractTerm, null, null, null,null,null, null,null,null);

        CustomerInventoryStubClientManagerFactory.TOBE_ASSET_HOLDER.add(onevoice);

        rfoSheetModel.add(new MockLineItemBuilder(lineItemId, siteInfo).build(), scode);
        List<RFORowModel> rfoExportModel = rfoSheetModel.getRFOExportModel();
        assertThat(rfoExportModel.size(), is(1));

        Map<String, String> rfoRowModel = rfoExportModel.get(0).getAttributes();
        assertRFOAttributes(onevoice, rfoRowModel);
        assertThat(rfoRowModel.get(SITE_ID_HEADER), is(siteInfo.bfgSiteID));
        assertThat(rfoRowModel.get(LINE_ITEM_ID_HEADER), is(lineItemId.value()));
        assertThat(rfoRowModel.get(SITE_NAME_HEADER), is(siteInfo.name));
    }

    @Test
    public void shouldCreateExportRFOModelForOneLineItemIdWithConfiguredAttributes() throws Exception {
        ProductInstance onevoice = DefaultProductInstanceFixture.aProductInstance()
                                                                .withProductIdentifier(new ProductIdentifier(scode, version))
                                                                .withLineItemId(lineItemId.value())
                                                                .withSiteId(siteId.value())
                                                                .withProjectId(projectId.value())
                                                                .withQuoteOptionId(quoteOptionId.value())
                                                                .build();

        CustomerInventoryStubClientManagerFactory.TOBE_ASSET_HOLDER.add(onevoice);

        rfoSheetModel.add(new MockLineItemBuilder(lineItemId, siteInfo).build(), scode);
        List<RFORowModel> rfoExportModel = rfoSheetModel.getRFOExportModel();
        assertThat(rfoExportModel.size(), is(1));

        testRecursively(rfoExportModel.get(0), onevoice);
    }

    private void testRecursively(RFORowModel rfoExportModel, ProductInstance onevoice) {
        assertAttributesRecursively(rfoExportModel.getAttributes(), onevoice.whatReadyForOrderAttributesShouldIConfigureForScenario(InstanceTreeScenario.PROVIDE));

        for (ProductInstance productInstance : onevoice.getChildren()) {
            Optional<RFORowModel> child = rfoExportModel.getChild(productInstance.getProductOffering().getProductIdentifier().getProductId(), productInstance.getProductInstanceId());
            if (!child.isPresent()) {
                fail("Not exported in sheet:" + productInstance);
            }
            testRecursively(child.get(), productInstance);
        }
    }

    private void assertAttributesRecursively(Map<String, String> rfoRowModel, List<Attribute> attributes) {
        for (Attribute attribute : attributes) {
            String value = rfoRowModel.get(attribute.getName().getName());
            assertNotNull("Value is null", value);
            assertThat(value, is(attribute.getName() + "-value"));
        }
    }

    @Test
    public void shouldCreateExportOneVoiceRFOModelForMultipleLines() throws Exception {
        ProductInstance site1 = productInstanceFactory.newFutureProductInstance(new LengthConstrainingProductInstanceId(),
                                                                                ProductInstanceVersion.DEFAULT_VALUE, lineItemId,
                                                                                new ProductCode("S0205086"),
                                                                                new ProductVersion(version), siteId, StencilId.NIL, customerId, null, quoteOptionId,
                                                                                AssetType.REAL,
                                                                                projectId, contractTerm, null, null, null,null,null, null,null,null);
        CustomerInventoryStubClientManagerFactory.TOBE_ASSET_HOLDER.add(site1);

        LineItemId lineItemId2 = LineItemIdFixture.aUniqueLineItemId().build();
        SiteId siteId2 = new RandomSiteId();
        SiteDTO siteInfo2 = new SiteDTO(siteId2.value(), "Site 2");
        ProductInstance site2 = productInstanceFactory.newFutureProductInstance(new LengthConstrainingProductInstanceId(),
                                                                                ProductInstanceVersion.DEFAULT_VALUE, lineItemId2,
                                                                                new ProductCode("S0205086"),
                                                                                new ProductVersion(version), siteId2, StencilId.NIL, customerId, null, quoteOptionId,
                                                                                AssetType.REAL,
                                                                                projectId, contractTerm, null, null, null,null,null, null,null,null);
        CustomerInventoryStubClientManagerFactory.TOBE_ASSET_HOLDER.add(site2);

        rfoSheetModel.add(new MockLineItemBuilder(lineItemId, siteInfo).build(), scode);
        rfoSheetModel.add(new MockLineItemBuilder(lineItemId2, siteInfo2).build(), scode);
        List<RFORowModel> rfoExportModel = rfoSheetModel.getRFOExportModel();
        assertThat(rfoExportModel.size(), is(2));

        for (RFORowModel row : rfoExportModel) {
            Map<String, String> attributes = row.getAttributes();
            assertRFOAttributes(site1, attributes);
        }
        assertThat(rfoExportModel.get(0).getAttributes().get(SITE_ID_HEADER), is(siteInfo.bfgSiteID));
        assertThat(rfoExportModel.get(0).getAttributes().get(LINE_ITEM_ID_HEADER), is(lineItemId.value()));
        assertThat(rfoExportModel.get(0).getAttributes().get(SITE_NAME_HEADER), is(siteInfo.name));
        assertThat(rfoExportModel.get(1).getAttributes().get(SITE_ID_HEADER), is(siteInfo2.bfgSiteID));
        assertThat(rfoExportModel.get(1).getAttributes().get(LINE_ITEM_ID_HEADER), is(lineItemId2.value()));
        assertThat(rfoExportModel.get(1).getAttributes().get(SITE_NAME_HEADER), is(siteInfo2.name));
    }

    private void assertRFOAttributes(ProductInstance onevoice, Map<String, String> rfoRowModel) throws InstanceCharacteristicNotFound {
        for (InstanceCharacteristic instanceCharacteristic : onevoice.getReadyForOrderInstanceCharacteristics(InstanceTreeScenario.PROVIDE)) {
            String value = rfoRowModel.get(instanceCharacteristic.getName());
            assertNotNull("Value is null", value);
            assertThat(value, is(""));
        }
    }

    @Test
    public void shouldExportToSpreadsheet() throws Exception {
        ProductInstance onevoice = productInstanceFactory.newFutureProductInstance(new LengthConstrainingProductInstanceId(),
                                                                                   ProductInstanceVersion.DEFAULT_VALUE, lineItemId,
                                                                                   new ProductCode(scode),
                                                                                   new ProductVersion(version), siteId, StencilId.NIL, customerId, null, quoteOptionId,
                                                                                   AssetType.REAL,
                                                                                   projectId, contractTerm, null, null, null,null,null, null,null,null);

        CustomerInventoryStubClientManagerFactory.TOBE_ASSET_HOLDER.add(onevoice);
        rfoSheetModel.add(new MockLineItemBuilder(lineItemId, siteInfo).build(), scode);

        final BillingAccountDTO billingAccount = new BillingAccountDTO(UUID.randomUUID().toString(), UUID.randomUUID().toString(), "USD");
        final OrderSheetModel orderModel = new OrderSheetModel(
            newArrayList(new MockLineItemBuilder("id", siteInfo).build()),
            newArrayList(billingAccount),
            signedOnDate, null,null, "");

        final Workbook workbook = new ExportExcelMarshaller(asList(rfoSheetModel), orderModel, new OrderSheetColumnManager(projectId.value(), quoteOptionId.value(), migrationDetailsProvider)).marshall();

        final Sheet oneVoiceSheet = workbook.getSheet(SHEET_NAME);
        assertThat(oneVoiceSheet.getLastRowNum(), is(2));

        final Row rowHeaders = oneVoiceSheet.getRow(1);//Headers
        final Row rowData = oneVoiceSheet.getRow(2);

        final Map<String, String> attributes = rfoSheetModel.getRFOExportModel().get(0).getAttributes();
        int cellNum = 0;
        for (Map.Entry<String, String> entry : attributes.entrySet()) {
            assertThat(rowHeaders.getCell(cellNum).getStringCellValue(), is(entry.getKey()));
            assertThat(rowData.getCell(cellNum).getStringCellValue(), is(entry.getValue()));
            cellNum++;
        }

        final Sheet orderSheet = workbook.getSheet(OrderSheetMarshaller.SHEET_NAME);
        assertThat(orderSheet.getLastRowNum(), is(orderModel.rows().size()));

        final Row orderDetailsHeader = orderSheet.getRow(0);
        final Row orderDetailsData = orderSheet.getRow(1);


        final OrderSheetMarshaller.Column[] columns = OrderSheetMarshaller.Column.values();

        for (OrderSheetMarshaller.Column column : columns) {
            assertThat(orderDetailsHeader.getCell(column.column).getStringCellValue(),
                       is(column.header));
        }

        assertThat(orderDetailsData.getCell(OrderSheetMarshaller.Column.SITE_ID.column).getStringCellValue(),
                   is(orderModel.rows().get(0).siteId()));
        assertThat(orderDetailsData.getCell(OrderSheetMarshaller.Column.SITE_NAME.column).getStringCellValue(),
                   is(orderModel.rows().get(0).siteName()));
    }

    @Test
    public void shouldHandleLargeColumnIndexs() throws Exception {
        ProductInstance onevoice = productInstanceFactory.newFutureProductInstance(new LengthConstrainingProductInstanceId(),
                                                                                   ProductInstanceVersion.DEFAULT_VALUE, lineItemId,
                                                                                   new ProductCode(scode),
                                                                                   new ProductVersion(version), siteId, StencilId.NIL, customerId, null, quoteOptionId,
                                                                                   AssetType.REAL,
                                                                                   projectId, contractTerm, null, null, null,null,null, null,null,null);

        CustomerInventoryStubClientManagerFactory.TOBE_ASSET_HOLDER.add(onevoice);
        rfoSheetModel.add(new MockLineItemBuilder(lineItemId, siteInfo).build(), scode);

        final BillingAccountDTO billingAccount = new BillingAccountDTO(UUID.randomUUID().toString(), UUID.randomUUID().toString(), "USD");
        final OrderSheetModel orderModel = new OrderSheetModel(
            newArrayList(new MockLineItemBuilder("id", siteInfo).build()),
            newArrayList(billingAccount),
            signedOnDate,null, null, "");

        final XSSFWorkbook workbook = new ExportExcelMarshaller(asList(rfoSheetModel), orderModel, new OrderSheetColumnManager(projectId.value(), quoteOptionId.value(), migrationDetailsProvider)).marshall();

        final XSSFSheet oneVoiceSheet = workbook.getSheet(SHEET_NAME);

        XSSFRow row = oneVoiceSheet.createRow(0);
        Cell cell = row.createCell(1000, CELL_TYPE_STRING);
        assertNotNull(cell);
    }

    @Test
    public void check(){
        Pattern p= Pattern.compile("[0-9]{12}$");
        Matcher m = p.matcher("123456789123");
        assertTrue(m.find());

    }

    private class MockLineItemBuilder {
        private LineItemId lineItemId;
        private SiteDTO siteInfo;

        public MockLineItemBuilder(LineItemId lineItemId, SiteDTO siteInfo) {
            this.lineItemId = lineItemId;
            this.siteInfo = siteInfo;
        }

        public MockLineItemBuilder(String lineItemId, SiteDTO siteInfo) {
            this.lineItemId = new LineItemId(lineItemId);
            this.siteInfo = siteInfo;
        }

        public LineItemModel build() {
            final LineItemModel model = mock(LineItemModel.class);
            when(model.getId()).thenReturn(lineItemId.value());
            when(model.getLineItemId()).thenReturn(lineItemId);
            when(model.getSite()).thenReturn(siteInfo);
            when(model.getBillingId()).thenReturn(billingId);
            when(model.getProductName()).thenReturn(productName);
            return model;
        }
    }
}
