package com.bt.rsqe.projectengine.web.quoteoptionorders.rfosheet;

import com.bt.rsqe.customerinventory.client.ProductInstanceClient;
import com.bt.rsqe.customerinventory.driver.CustomerInventoryDriverManager;
import com.bt.rsqe.customerinventory.driver.CustomerProductCategoryContactsDriver;
import com.bt.rsqe.customerinventory.dto.AssetDTO;
import com.bt.rsqe.customerinventory.fixtures.AssetDTOFixture;
import com.bt.rsqe.customerinventory.parameter.CustomerId;
import com.bt.rsqe.customerinventory.parameter.LengthConstrainingProductInstanceId;
import com.bt.rsqe.customerinventory.parameter.LineItemId;
import com.bt.rsqe.customerinventory.parameter.ProductCode;
import com.bt.rsqe.customerrecord.CustomerResource;
import com.bt.rsqe.customerrecord.ExpedioClientResources;
import com.bt.rsqe.customerrecord.SiteDTO;
import com.bt.rsqe.customerrecord.SiteResource;
import com.bt.rsqe.domain.AssetKey;
import com.bt.rsqe.domain.Notification;
import com.bt.rsqe.domain.ProductCategoryMigration;
import com.bt.rsqe.domain.bom.fixtures.ProductOfferingFixture;
import com.bt.rsqe.domain.bom.fixtures.SalesRelationshipFixture;
import com.bt.rsqe.domain.product.Association;
import com.bt.rsqe.domain.product.ConfigurationPhase;
import com.bt.rsqe.domain.product.ContributesToCharacteristicUpdater;
import com.bt.rsqe.domain.product.DefaultProductInstanceFixture;
import com.bt.rsqe.domain.product.InstanceTreeScenario;
import com.bt.rsqe.domain.product.LifeTime;
import com.bt.rsqe.domain.product.LocalAssociation;
import com.bt.rsqe.domain.product.ProductOffering;
import com.bt.rsqe.domain.product.ProductSalesRelationshipInstance;
import com.bt.rsqe.domain.product.SimpleProductOfferingType;
import com.bt.rsqe.domain.product.Writability;
import com.bt.rsqe.domain.product.parameters.ProductIdentifier;
import com.bt.rsqe.domain.product.parameters.RelationshipType;
import com.bt.rsqe.domain.project.InstanceCharacteristicNotFound;
import com.bt.rsqe.domain.project.ProductInstance;
import com.bt.rsqe.enums.ProductCodes;
import com.bt.rsqe.expedio.contact.BFGContactCreationFailureException;
import com.bt.rsqe.expressionevaluator.NonCharacteristicExpressions;
import com.bt.rsqe.factory.ServiceLocator;
import com.bt.rsqe.fixtures.CalendarFixture;
import com.bt.rsqe.pmr.client.PmrClient;
import com.bt.rsqe.pmr.client.PmrMocker;
import com.bt.rsqe.productinstancemerge.MergeResult;
import com.bt.rsqe.productinstancemerge.changetracker.ChangeTracker;
import com.bt.rsqe.projectengine.QuoteOptionItemResource;
import com.bt.rsqe.projectengine.RfoUpdateDTO;
import com.bt.rsqe.projectengine.migration.QuoteMigrationDetailsProvider;
import com.bt.rsqe.projectengine.web.facades.BfgContactsFacade;
import com.bt.rsqe.projectengine.web.facades.QuoteOptionOrderFacade;
import com.bt.rsqe.projectengine.web.model.LineItemModel;
import com.bt.rsqe.projectengine.web.quoteoptionorders.rfosheet.bfgcontactsattributes.BFGContactAttribute;
import com.bt.rsqe.projectengine.web.quoteoptionorders.rfosheet.bfgcontactsattributes.BFGContactsStrategy;
import com.bt.rsqe.projectengine.web.quoteoptionorders.rfosheet.bfgcontactsattributes.BFGContactsStrategyFactory;
import com.bt.rsqe.projectengine.web.quoteoptionorders.rfosheet.bfgcontactsattributes.ServiceProductsBFGContactsStrategy;
import com.bt.rsqe.web.rest.dto.types.JaxbDateTime;
import com.google.common.base.Optional;
import org.apache.commons.lang.time.DateUtils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.joda.time.format.DateTimeFormat;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import static com.bt.rsqe.domain.product.LifeTime.*;
import static com.bt.rsqe.domain.product.Writability.*;
import static com.bt.rsqe.enums.ProductCodes.*;
import static com.bt.rsqe.productinstancemerge.ChangeType.*;
import static com.bt.rsqe.projectengine.web.quoteoptionorders.rfosheet.OrderSheetMarshaller.Column.*;
import static com.bt.rsqe.projectengine.web.quoteoptionorders.rfosheet.RFOSheetModel.*;
import static com.bt.rsqe.projectengine.web.quoteoptionorders.rfosheet.bfgcontactsattributes.CAServiceBFGContactsAttributes.*;
import static com.bt.rsqe.projectengine.web.quoteoptionorders.rfosheet.bfgcontactsattributes.ServiceProductsBFGContactsStrategy.*;
import static com.google.common.collect.Lists.*;
import static com.google.common.collect.Sets.*;
import static org.hamcrest.core.Is.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class RFOUpdaterTest extends WorkbookTest {
    @Mock
    @SuppressWarnings("unused")
    private QuoteOptionOrderFacade orderFacade;
    @Mock
    @SuppressWarnings("unused")
    private QuoteOptionItemResource quoteOptionItemResource;
    @Mock
    @SuppressWarnings("unused")
    private ProductInstanceClient futureProductInstanceClient;
    @Mock
    @SuppressWarnings("unused")
    private LineItemModel lineItemModel;
    @Mock
    @SuppressWarnings("unused")
    private OrderSheetDemarshaller ordersheetDemarsheller;
    @Mock
    @SuppressWarnings("unused")
    private CustomerInventoryDriverManager customerInventoryDriverManager;
    @Mock
    @SuppressWarnings("unused")
    private CustomerResource customerResource;
    @Mock
    @SuppressWarnings("unused")
    private SiteResource siteResource;
    @Mock
    @SuppressWarnings("unused")
    private CustomerProductCategoryContactsDriver customerProductCategoryContactsDriver;
    @Mock
    @SuppressWarnings("unused")
    private ExpedioClientResources expedioClientResources;
    @Mock
    @SuppressWarnings("unused")
    private PmrClient pmrClient;
    @Mock
    @SuppressWarnings("unused")
    private Notification notification;
    @Mock
    @SuppressWarnings("unused")
    private ChangeTracker tracker;
    @Mock
    private QuoteMigrationDetailsProvider migrationDetailsProvider;
    @Mock
    private ProductInstanceClient productInstanceClient;
    @Mock
    private ContributesToCharacteristicUpdater contributesToCharacteristicUpdater;

    private SiteDTO siteDTO;
    private RFOSheetModel rfoSheetModel;
    private RFOUpdater rfoUpdater;
    private String childScode1;
    private String rootScode;
    private String contactName;
    private String productType;
    private String serviceDelivery;
    private ProductCodes connectAccelerationSteelhead;
    private ProductCodes connectAcceleration;
    private ProductCodes connectAccelerationService;
    private ProductInstance rootProductInstance;
    private ProductInstance steelHeadOne;
    private ProductInstance steelHeadTwo;
    private String productInstanceIdOne;
    private String productInstanceIdTwo;
    private String rootLineItemId;
    private final Long CUSTOMER_ID = 7368365322L;
    private final Long BFG_SITE_ID = 234235L;
    private final Long BFG_ADDRESS_ID = 43463L;
    private BfgContactsFacade bfgContactsFacade;
    private String contractId;
    private ProductInstance ipConnectGatewayConfiguration;
    private String relatedProductInstanceId;
    private MergeResult mergeResult;

    @Before
    public void setUp() throws BFGContactCreationFailureException {
        MockitoAnnotations.initMocks(this);
        pmrClient = PmrMocker.getMockedInstance(true);
        when(productInstanceClient.getAssetDTO(any(LineItemId.class))).thenReturn(AssetDTOFixture.anAsset().build());
        siteDTO = new SiteDTO(BFG_SITE_ID.toString(), "site-A");
        siteDTO.addressId = BFG_ADDRESS_ID.toString();

        rfoSheetModel = new RFOSheetModel(futureProductInstanceClient, "Sheet Name", rootScode, migrationDetailsProvider, contributesToCharacteristicUpdater);
        rfoUpdater = new RFOUpdater(orderFacade, productInstanceClient, contributesToCharacteristicUpdater, pmrClient);
        childScode1 = ProductCodes.ConnectAccelerationSteelhead.productCode();
        rootScode = ProductCodes.ConnectAccelerationSite.productCode();

        rootLineItemId = "rootLineItemId";
        productInstanceIdOne = "productInstanceIdOne";
        productInstanceIdTwo = "productInstanceIdTwo";

        contactName = "Contact Name";
        productType = "Product Type";
        serviceDelivery = "isServiceDeliveryInMigrationOrder";
        contractId  = "CONTRACT ID";

        connectAccelerationSteelhead = ProductCodes.ConnectAccelerationSteelhead;
        connectAcceleration = ProductCodes.ConnectAccelerationSite;
        connectAccelerationService = ProductCodes.ConnectAccelerationService;
        Optional<ProductIdentifier> productCategoryCode = Optional.of(new ProductIdentifier("H0300521", connectAccelerationService.productName(), "1.0"));

        when(expedioClientResources.getCustomerResource()).thenReturn(customerResource);
        when(customerInventoryDriverManager.getCustomerProductCategoryContactsDriver(any(CustomerId.class), any(ProductCode.class), anyString())).thenReturn(customerProductCategoryContactsDriver);
        when(customerResource.siteResource(any(String.class))).thenReturn(siteResource);
        when(siteResource.getCentralSite(any(String.class))).thenReturn(siteDTO);
        when(pmrClient.getProductHCode(connectAccelerationService.productCode())).thenReturn(productCategoryCode);

        bfgContactsFacade = new BfgContactsFacade(expedioClientResources, pmrClient, customerInventoryDriverManager);
        ServiceLocator.serviceLocatorInstance().unRegisterAll();
        ServiceLocator.serviceLocatorInstance().register(BfgContactsFacade.class, bfgContactsFacade);
        relatedProductInstanceId = "relatedProductInstanceId";

        when(migrationDetailsProvider.conditionalFor(Mockito.any(ProductInstance.class))).thenCallRealMethod();
        when(migrationDetailsProvider.isMigrationQuote(Mockito.any(String.class), Mockito.any(String.class))).thenReturn(Optional.<Boolean>absent());
        when(migrationDetailsProvider.getMigrationDetailsForProductCode(Mockito.any(String.class))).thenReturn(Optional.<ProductCategoryMigration>absent());

    }

    @Test
    public void shouldUpdateProductInstanceFromRFOSheet() throws InstanceCharacteristicNotFound {
        createProductInstanceTree();
        final XSSFWorkbook workBook = createWorkBookFor(connectAcceleration);

        when(lineItemModel.getLineItemId()).thenReturn(new LineItemId("Line Item Id"));
        when(lineItemModel.getSite()).thenReturn(siteDTO);
        when(lineItemModel.getSummary()).thenReturn("summary");
        final AssetDTO asset = AssetDTOFixture.anAsset().build();
        when(lineItemModel.getRootInstance()).thenReturn(asset);
        when(futureProductInstanceClient.convertAssetToLightweightInstance(asset)).thenReturn(rootProductInstance);
        when(futureProductInstanceClient.getSourceAssetDTO(rootProductInstance.getProductInstanceId().getValue())).thenReturn(Optional.<AssetDTO>absent());
        mergeResult = new MergeResult(newArrayList(rootProductInstance), tracker);
        when(tracker.changeFor(rootProductInstance)).thenReturn(ADD);
        when(tracker.changeFor(steelHeadOne)).thenReturn(ADD);
        when(futureProductInstanceClient.getMergeResult(rootProductInstance, null, InstanceTreeScenario.PROVIDE)).thenReturn(mergeResult);
        when(futureProductInstanceClient.getByAssetKey(any(AssetKey.class))).thenReturn(rootProductInstance);
        when(futureProductInstanceClient.getAssetsDiff(anyString(), anyLong(), anyLong(), any(InstanceTreeScenario.class))).thenReturn(mergeResult);
        when(futureProductInstanceClient.getSourceAsset(new LengthConstrainingProductInstanceId(rootProductInstance.getProductInstanceId().getValue()))).thenReturn(Optional.<ProductInstance>absent());

        rfoSheetModel.add(lineItemModel, rootScode);
        Map<String, RFOSheetModel> rfoSheetModel = new HashMap<String, RFOSheetModel>();
        rfoSheetModel.put(connectAcceleration.productCode(), this.rfoSheetModel);
        rfoUpdater.updateProductInstanceAndOrderDetails("projectId", "quoteOptionId", "orderId", rfoSheetModel, ordersheetDemarsheller, workBook, contractId);

        assertThat(steelHeadOne.getInstanceCharacteristic(contactName).getValue().toString(), is("updated contact 1"));
        //assertThat(steelHeadTwo.getInstanceCharacteristic(contactName).getValue().toString(), is("updated contact 2"));
    }

    @Test
    public void shouldUpdateProductInstanceWithServiceDeliveryAttributeValuesFromRFOSheet() throws InstanceCharacteristicNotFound {
        createProductInstanceTree();
        final XSSFWorkbook workBook = createWorkBookFor(connectAcceleration);

        when(lineItemModel.getLineItemId()).thenReturn(new LineItemId("Line Item Id"));
        when(lineItemModel.getSite()).thenReturn(siteDTO);
        when(lineItemModel.getSummary()).thenReturn("summary");

        final AssetDTO asset = AssetDTOFixture.anAsset().build();
        when(lineItemModel.getRootInstance()).thenReturn(asset);
        when(futureProductInstanceClient.convertAssetToLightweightInstance(asset)).thenReturn(rootProductInstance);
        when(futureProductInstanceClient.getSourceAssetDTO(rootProductInstance.getProductInstanceId().getValue())).thenReturn(Optional.<AssetDTO>absent());
        mergeResult = new MergeResult(newArrayList(rootProductInstance), tracker);
        when(tracker.changeFor(rootProductInstance)).thenReturn(ADD);
        when(tracker.changeFor(steelHeadOne)).thenReturn(ADD);
        when(futureProductInstanceClient.getMergeResult(rootProductInstance, null, InstanceTreeScenario.PROVIDE)).thenReturn(mergeResult);
        when(futureProductInstanceClient.getAssetsDiff(anyString(), anyLong(), anyLong(), any(InstanceTreeScenario.class))).thenReturn(mergeResult);
        when(futureProductInstanceClient.getSourceAsset(any(LengthConstrainingProductInstanceId.class))).thenReturn(Optional.<ProductInstance>absent());
        when(migrationDetailsProvider.conditionalFor(any(ProductInstance.class))).thenCallRealMethod();
        when(migrationDetailsProvider.isMigrationQuote(Matchers.<String>any(String.class), Matchers.<String>any(String.class))).thenReturn(com.google.common.base.Optional.of(true));
        when(migrationDetailsProvider.getMigrationDetailsForProductCode(rootProductInstance.getProductIdentifier().getProductId())).thenReturn(Optional.of(new ProductCategoryMigration(true, true,false)));
        rfoSheetModel.add(lineItemModel, rootScode);
        when(futureProductInstanceClient.getByAssetKey(any(AssetKey.class))).thenReturn(rootProductInstance);
        Map<String, RFOSheetModel> rfoSheetModel = new HashMap<String, RFOSheetModel>();
        rfoSheetModel.put(connectAcceleration.productCode(), this.rfoSheetModel);
        rfoUpdater.updateProductInstanceAndOrderDetails("projectId", "quoteOptionId", "orderId", rfoSheetModel, ordersheetDemarsheller, workBook, contractId);

        assertThat(rootProductInstance.getInstanceCharacteristic(serviceDelivery).getValue().toString(), is("cell value"));
    }

    @Test
    public void shouldUpdateOrderRFOSheet() throws InstanceCharacteristicNotFound {
        when(productInstanceClient.get(new LineItemId("lineItemId1"))).thenReturn(DefaultProductInstanceFixture.aProductInstance().build());
        when(productInstanceClient.get(new LineItemId("lineItemId2"))).thenReturn(DefaultProductInstanceFixture.aProductInstance().build());

        XSSFWorkbook workbook = new XSSFWorkbook();

        Map<String, RFOSheetModel> rfoSheetModel = new HashMap<String, RFOSheetModel>();
        rfoSheetModel.put(connectAcceleration.productCode(), this.rfoSheetModel);
        final Sheet sheet = workbook.createSheet(OrderSheetMarshaller.SHEET_NAME);
        sheet.setColumnHidden(OrderSheetMarshaller.Column.INITIAL_BILLING_START_DATE.column, true);
        addOrderDetailsSheetRow(sheet, 1, "lineItemId1", "siteId1", "siteName1", "blah product1", "2015-OCT-20", "2015-OCT-20", null, "1 - A1");
        addOrderDetailsSheetRow(sheet, 2, "lineItemId2", "siteId2", "siteName2", "blah product2", "2015-OCT-20", "2015-OCT-20", null, "2 - A2");

        String projectId = "projectId";
        String quoteOptionId = "quoteOptionId";
        String orderId = "orderId";
        rfoUpdater.updateProductInstanceAndOrderDetails(projectId, quoteOptionId, orderId, rfoSheetModel,
                                                        new OrderSheetDemarshaller(workbook), workbook, contractId);

        ArgumentCaptor<RfoUpdateDTO> rfoDtoCaptor = ArgumentCaptor.forClass(RfoUpdateDTO.class);
        verify(orderFacade).updateWithRfo(eq(projectId), eq(quoteOptionId), eq(orderId), rfoDtoCaptor.capture());

        assertThat(rfoDtoCaptor.getValue().itemBillings.size(), is(2));
        assertThat(rfoDtoCaptor.getValue().itemBillings.get(0).getBillingId(), is("1"));
        assertThat(rfoDtoCaptor.getValue().itemBillings.get(0).getLineItemId(), is("lineItemId1"));
        assertThat(rfoDtoCaptor.getValue().itemBillings.get(0).getCustomerRequiredDate(),
                   is(JaxbDateTime.valueOf(DateTimeFormat.forPattern("yyyy-MMM-dd").parseDateTime("2015-OCT-20"))));
    }

    @Test
    public void shouldThrowRFOExceptionIfCustomerRequiredDateIsMissing() throws InstanceCharacteristicNotFound {
        expectException(RFOImportException.class, "RFO mandatory values are missing for Customer Required Date");

        XSSFWorkbook workbook = new XSSFWorkbook();

        Map<String, RFOSheetModel> rfoSheetModel = new HashMap<String, RFOSheetModel>();
        rfoSheetModel.put(connectAcceleration.productCode(), this.rfoSheetModel);
        final Sheet sheet = workbook.createSheet(OrderSheetMarshaller.SHEET_NAME);
        addOrderDetailsSheetRow(sheet, 1, "lineItemId1", "siteId1", "siteName1", "blah product1", "2015-Nov-11", null, null, "1 - A1");
        addOrderDetailsSheetRow(sheet, 2, "lineItemId2", "siteId2", "siteName2", "blah product2", "2015-Nov-11", null, null, "2 - A2");

        String projectId = "projectId";
        String quoteOptionId = "quoteOptionId";
        String orderId = "orderId";
        rfoUpdater.updateProductInstanceAndOrderDetails(projectId, quoteOptionId, orderId, rfoSheetModel,
                                                        new OrderSheetDemarshaller(workbook), workbook, contractId);
    }

    @Test
    public void shouldThrowRFOExceptionIfBothCustomerRequiredDateAndContractStartDateAreMissing() throws Exception {
        expectException(RFOImportException.class, "RFO mandatory values are missing for Customer Required Date and Contract Start Date");

        XSSFWorkbook workbook = new XSSFWorkbook();

        Map<String, RFOSheetModel> rfoSheetModel = new HashMap<String, RFOSheetModel>();
        rfoSheetModel.put(connectAcceleration.productCode(), this.rfoSheetModel);
        final Sheet sheet = workbook.createSheet(OrderSheetMarshaller.SHEET_NAME);
        addOrderDetailsSheetRow(sheet, 1, "lineItemId1", "siteId1", "siteName1", "blah product1", "2015-Nov-11", null, null, "1 - A1");
        addOrderDetailsSheetRow(sheet, 2, "lineItemId2", "siteId2", "siteName2", "blah product2", "2015-Nov-11", null, null, "2 - A2");

        String projectId = "projectId";
        String quoteOptionId = "quoteOptionId";
        String orderId = "orderId";
        when(migrationDetailsProvider.isMigrationQuote(projectId, quoteOptionId)).thenReturn(Optional.of(true));

        rfoUpdater.updateProductInstanceAndOrderDetails(projectId, quoteOptionId, orderId, rfoSheetModel,
                                                        new OrderSheetDemarshaller(workbook), workbook, contractId);
    }

    @Test
    public void shouldInvokeBFGContactCreationForCAServiceWithUserDataFromRFOModel() throws InstanceCharacteristicNotFound, BFGContactCreationFailureException {
        String caServiceProductCode = connectAccelerationService.productCode();
        String caServiceProductName = connectAccelerationService.productName();

        final ProductIdentifier rootProductIdentifier = new ProductIdentifier(caServiceProductCode, caServiceProductName, "1.0");

        final ProductOfferingFixture rootProductOfferingFixture = ProductOfferingFixture.aProductOffering()
                                                                                        .withSimpleProductOfferingType(SimpleProductOfferingType.CentralService)
                                                                                        .withProductIdentifier(rootProductIdentifier)
                                                                                        .withRFOAttribute(productType, AddOrder, Updatable, true, false)
                                                                                        .withRFQAttribute(ServiceProductsBFGContactsStrategy.CENTRAL_CONSULTANT_REQUIRED)
                                                                                        .withRFQAttribute(ServiceProductsBFGContactsStrategy.CENTRAL_ANALYST_REQUIRED);

        rootProductInstance = DefaultProductInstanceFixture.aProductInstance()
                                                           .withCustomerId(CUSTOMER_ID.toString())
                                                           .withProductIdentifier(caServiceProductCode, caServiceProductName)
                                                           .withProductOffering(rootProductOfferingFixture)
                                                           .withLineItemId(rootLineItemId)
                                                           .withAttributeValue(ServiceProductsBFGContactsStrategy.CENTRAL_CONSULTANT_REQUIRED, "NO")
                                                           .withAttributeValue(ServiceProductsBFGContactsStrategy.CENTRAL_ANALYST_REQUIRED, "NO")
                                                           .build();

        final XSSFWorkbook workBook = createBFGContactsWorkBookFor(connectAccelerationService);

        BFGContactsStrategyFactory bfgContactsStrategyFactory = mock(BFGContactsStrategyFactory.class);
        BFGContactsStrategy caServiceBFGContactsStrategy = mock(ServiceProductsBFGContactsStrategy.class);
        when(bfgContactsStrategyFactory.getStrategyFor(rootProductInstance.getSimpleProductOfferingType())).thenReturn((Optional) Optional.of(caServiceBFGContactsStrategy));
        when(caServiceBFGContactsStrategy.getBFGContactsAttributes(rootProductInstance)).thenReturn(new ArrayList<BFGContactAttribute>());

        when(lineItemModel.getLineItemId()).thenReturn(new LineItemId("Line Item Id"));
        when(lineItemModel.getSite()).thenReturn(siteDTO);
        when(lineItemModel.getSummary()).thenReturn("summary");
        final AssetDTO asset = AssetDTOFixture.anAsset().build();
        when(lineItemModel.getRootInstance()).thenReturn(asset);
        when(futureProductInstanceClient.convertAssetToLightweightInstance(asset)).thenReturn(rootProductInstance);
        when(futureProductInstanceClient.getSourceAssetDTO(rootProductInstance.getProductInstanceId().getValue())).thenReturn(Optional.<AssetDTO>absent());
        mergeResult = new MergeResult(newArrayList(rootProductInstance), tracker);
        when(tracker.changeFor(rootProductInstance)).thenReturn(ADD);
        when(tracker.changeFor(steelHeadOne)).thenReturn(ADD);
        when(futureProductInstanceClient.getMergeResult(rootProductInstance, null, InstanceTreeScenario.PROVIDE)).thenReturn(mergeResult);
        when(futureProductInstanceClient.getAssetsDiff(anyString(), anyLong(), anyLong(), any(InstanceTreeScenario.class))).thenReturn(mergeResult);
        when(futureProductInstanceClient.getSourceAsset(any(LengthConstrainingProductInstanceId.class))).thenReturn(Optional.<ProductInstance>absent());
        when(futureProductInstanceClient.getByAssetKey(any(AssetKey.class))).thenReturn(rootProductInstance);
        rfoSheetModel = new RFOSheetModel(bfgContactsStrategyFactory, futureProductInstanceClient, "Sheet Name", rootScode, migrationDetailsProvider, contributesToCharacteristicUpdater);

        this.rfoSheetModel.add(lineItemModel, caServiceProductCode);
        Map<String, RFOSheetModel> expectedRfoSheetModel = new HashMap<String, RFOSheetModel>();
        expectedRfoSheetModel.put(caServiceProductCode, this.rfoSheetModel);

        rfoUpdater.updateProductInstanceAndOrderDetails("projectId", "quoteOptionId", "orderId", expectedRfoSheetModel,
                                                        ordersheetDemarsheller, workBook, contractId);

        verify(caServiceBFGContactsStrategy).createAndPersistBFGContactID(Matchers.<RFORowModel>any());
    }

    @Test
    public void shouldUpdateInitialBillingStartDateOnRootInstanceWhenPresent() throws Exception {
        XSSFWorkbook workbook = new XSSFWorkbook();

        Map<String, RFOSheetModel> rfoSheetModel = new HashMap<String, RFOSheetModel>();
        rfoSheetModel.put(connectAcceleration.productCode(), this.rfoSheetModel);
        final Sheet sheet = workbook.createSheet(OrderSheetMarshaller.SHEET_NAME);
        final Date initialBillingStartDate = CalendarFixture.aCalendar().day(1).month(CalendarFixture.Month.JAN).year(2014).get().getTime();
        addOrderDetailsSheetRow(sheet, 1, "aLineItemId", "siteId1", "siteName1", "blah product1", "2015-Nov-11", "2015-Nov-11", initialBillingStartDate, "1 - A1");

        String projectId = "projectId";
        String quoteOptionId = "quoteOptionId";
        String orderId = "orderId";
        when(migrationDetailsProvider.isMigrationQuote(projectId, quoteOptionId)).thenReturn(Optional.of(true));

        when(productInstanceClient.getAssetDTO(new LineItemId("aLineItemId"))).thenReturn(AssetDTOFixture.anAsset().build());
        ArgumentCaptor<AssetDTO> instanceCaptor = ArgumentCaptor.forClass(AssetDTO.class);

        rfoUpdater.updateProductInstanceAndOrderDetails(projectId,
                                                        quoteOptionId,
                                                        orderId,
                                                        rfoSheetModel,
                                                        new OrderSheetDemarshaller(workbook),
                                                        workbook,
                                                        contractId);

        verify(productInstanceClient).putAsset(instanceCaptor.capture());
        assertThat(DateUtils.isSameDay(initialBillingStartDate, instanceCaptor.getValue().detail().getInitialBillingStartDate()), is(true));
    }

    @Test
    public void shouldUpdateExistingInitialBillingStartDateOnRootInstanceWhenPresent() throws Exception {
        XSSFWorkbook workbook = new XSSFWorkbook();

        Map<String, RFOSheetModel> rfoSheetModel = new HashMap<String, RFOSheetModel>();
        rfoSheetModel.put(connectAcceleration.productCode(), this.rfoSheetModel);
        final Sheet sheet = workbook.createSheet(OrderSheetMarshaller.SHEET_NAME);
        final Date initialBillingStartDate = CalendarFixture.aCalendar().day(1).month(CalendarFixture.Month.JAN).year(2014).get().getTime();
        addOrderDetailsSheetRow(sheet, 1, "aLineItemId", "siteId1", "siteName1", "blah product1", "2015-Nov-11", "2015-Nov-11", initialBillingStartDate, "1 - A1");

        String projectId = "projectId";
        String quoteOptionId = "quoteOptionId";
        String orderId = "orderId";
        when(migrationDetailsProvider.isMigrationQuote(projectId, quoteOptionId)).thenReturn(Optional.of(true));

        when(productInstanceClient.getAssetDTO(new LineItemId("aLineItemId"))).thenReturn(AssetDTOFixture.anAsset().withInitialBillingStartDate(Calendar.getInstance().getTime()).build());
        ArgumentCaptor<AssetDTO> instanceCaptor = ArgumentCaptor.forClass(AssetDTO.class);

        rfoUpdater.updateProductInstanceAndOrderDetails(projectId,
                                                        quoteOptionId,
                                                        orderId,
                                                        rfoSheetModel,
                                                        new OrderSheetDemarshaller(workbook),
                                                        workbook,
                                                        contractId);

        verify(productInstanceClient).putAsset(instanceCaptor.capture());
        assertThat(DateUtils.isSameDay(initialBillingStartDate, instanceCaptor.getValue().detail().getInitialBillingStartDate()), is(true));
    }

    @Test
    public void shouldUpdateBillingIdAndTriggerContributedToRules() {
       XSSFWorkbook workbook = new XSSFWorkbook();

        Map<String, RFOSheetModel> rfoSheetModel = new HashMap<String, RFOSheetModel>();
        rfoSheetModel.put(connectAcceleration.productCode(), this.rfoSheetModel);
        final Sheet sheet = workbook.createSheet(OrderSheetMarshaller.SHEET_NAME);
        sheet.setColumnHidden(OrderSheetMarshaller.Column.INITIAL_BILLING_START_DATE.column, true);
        addOrderDetailsSheetRow(sheet, 1, "aLineItemId", "siteId1", "siteName1", "blah product1", "2015-Nov-11", "2015-Nov-11", null, "1 - A1");

        String projectId = "projectId";
        String quoteOptionId = "quoteOptionId";
        String orderId = "orderId";
        when(migrationDetailsProvider.isMigrationQuote(projectId, quoteOptionId)).thenReturn(Optional.of(true));

        when(productInstanceClient.getAssetDTO(new LineItemId("aLineItemId"))).thenReturn(AssetDTOFixture.anAsset().withProductCode(new ProductCode("aProductCode")).build());
        final ProductOffering offering = spy(ProductOfferingFixture.aProductOffering().withProductIdentifier("aProductCode").build());
        final Set<Association> associations = newHashSet((Association)new LocalAssociation("BillingId", Association.AssociationType.ATTRIBUTE_SOURCE));
        doReturn(associations).when(offering).getAttributeAssociations(NonCharacteristicExpressions.BillingId.name());
        PmrMocker.returnForProduct(pmrClient, offering);

        rfoUpdater.updateProductInstanceAndOrderDetails(projectId,
                                                        quoteOptionId,
                                                        orderId,
                                                        rfoSheetModel,
                                                        new OrderSheetDemarshaller(workbook),
                                                        workbook,
                                                        contractId);
        verify(productInstanceClient, times(1)).get(new LineItemId("aLineItemId"));
        verify(contributesToCharacteristicUpdater, times(1)).update(any(ProductInstance.class), eq(associations));
    }

    @Test
    public void shouldNotTriggerContributedToRulesWhenNoMatchingAttributeAssociationsExist() {
       XSSFWorkbook workbook = new XSSFWorkbook();

        Map<String, RFOSheetModel> rfoSheetModel = new HashMap<String, RFOSheetModel>();
        rfoSheetModel.put(connectAcceleration.productCode(), this.rfoSheetModel);
        final Sheet sheet = workbook.createSheet(OrderSheetMarshaller.SHEET_NAME);
        sheet.setColumnHidden(OrderSheetMarshaller.Column.INITIAL_BILLING_START_DATE.column, true);
        final Date initialBillingStartDate = CalendarFixture.aCalendar().day(1).month(CalendarFixture.Month.JAN).year(2014).get().getTime();
        addOrderDetailsSheetRow(sheet, 1, "aLineItemId", "siteId1", "siteName1", "blah product1", "2015-Nov-11", "2015-Nov-11", null, "1 - A1");

        String projectId = "projectId";
        String quoteOptionId = "quoteOptionId";
        String orderId = "orderId";
        when(migrationDetailsProvider.isMigrationQuote(projectId, quoteOptionId)).thenReturn(Optional.of(true));

        rfoUpdater.updateProductInstanceAndOrderDetails(projectId,
                                                        quoteOptionId,
                                                        orderId,
                                                        rfoSheetModel,
                                                        new OrderSheetDemarshaller(workbook),
                                                        workbook,
                                                        contractId);

        verify(contributesToCharacteristicUpdater, never()).update(any(ProductInstance.class), eq(NonCharacteristicExpressions.BillingId.name()));
    }

    private ProductInstance createProductInstanceTree() {
        final ProductIdentifier steelHeadProductIdentifier = new ProductIdentifier(connectAccelerationSteelhead.productCode(),
                                                                                   connectAccelerationSteelhead.productName(), "1.0");
        final SalesRelationshipFixture steelHeadSalesRelationshipFixture = SalesRelationshipFixture.aSalesRelationship()
                                                                                                   .withRelationType(RelationshipType.Child)
                                                                                                   .withProductIdentifier(steelHeadProductIdentifier);
        final ProductOfferingFixture steelHeadProductOfferingFixture = ProductOfferingFixture.aProductOffering()
                                                                                             .withSimpleProductOfferingType(SimpleProductOfferingType.NetworkService)
                                                                                             .withSalesRelationship(steelHeadSalesRelationshipFixture)
                                                                                             .withProductIdentifier(steelHeadProductIdentifier)
                                                                                             .withRFOAttribute(contactName, AddOrder, Updatable, true, false);


        final ProductIdentifier rootProductIdentifier = new ProductIdentifier(connectAcceleration.productCode(), connectAcceleration.productName(), "1.0");
        final ProductOfferingFixture rootProductOfferingFixture = ProductOfferingFixture.aProductOffering()
                                                                                        .withSimpleProductOfferingType(SimpleProductOfferingType.Package)
                                                                                        .withProductIdentifier(rootProductIdentifier)
                                                                                        .withRFOAttribute(productType, AddOrder, Updatable, true, false)
                                                                                        .withRFQAttribute(ServiceProductsBFGContactsStrategy.CENTRAL_CONSULTANT_REQUIRED)
                                                                                        .withRFQAttribute(ServiceProductsBFGContactsStrategy.CENTRAL_ANALYST_REQUIRED)
                                                                                        .withRFOAttribute(serviceDelivery, LifeTime.AddOrder, Writability.Updatable, false, false, ConfigurationPhase.SERVICE_DELIVERY);

        rootProductInstance = DefaultProductInstanceFixture.aProductInstance()
                                                           .withProductInstanceId("assetId")
                                                           .withProductInstanceVersion(1L)
                                                           .withCustomerId(CUSTOMER_ID.toString())
                                                           .withProductIdentifier(connectAcceleration.productCode(), connectAcceleration.productName())
                                                           .withProductOffering(rootProductOfferingFixture)
                                                           .withLineItemId(rootLineItemId)
                                                           .withAttributeValue(ServiceProductsBFGContactsStrategy.CENTRAL_CONSULTANT_REQUIRED, "NO")
                                                           .withAttributeValue(ServiceProductsBFGContactsStrategy.CENTRAL_ANALYST_REQUIRED, "NO")
                                                           .withRFOAttributeValue(contractId, "contractID_54321", true, true)
                                                           .build();

        steelHeadOne = DefaultProductInstanceFixture.aProductInstance()
                                                    .withProductIdentifier(steelHeadProductIdentifier)
                                                    .withProductOffering(steelHeadProductOfferingFixture)
                                                    .withProductInstanceId(productInstanceIdOne)
                                                    .build();

        steelHeadTwo = DefaultProductInstanceFixture.aProductInstance()
                                                    .withProductIdentifier(steelHeadProductIdentifier)
                                                    .withProductOffering(steelHeadProductOfferingFixture)
                                                    .withProductInstanceId(productInstanceIdTwo)
                                                    .build();

        configureIpConnectGatewayForSteelHead();
        rootProductInstance.addRelationship(new ProductSalesRelationshipInstance(steelHeadSalesRelationshipFixture.build(), steelHeadOne));
        //rootProductInstance.addRelationship(new ProductSalesRelationshipInstance(steelHeadSalesRelationshipFixture.build(), steelHeadTwo));
        return rootProductInstance;
    }

    private void configureIpConnectGatewayForSteelHead() {
        final ProductIdentifier ipConnectGatewayConfigurationProductIdentifier =
                                                    new ProductIdentifier(IPConnectGatewayConfiguration.productCode(),
                                                                          IPConnectGatewayConfiguration.productName(), "1.0");

        final SalesRelationshipFixture ipConnectGatewayConfigurationSalesRelationshipFixture = SalesRelationshipFixture.aSalesRelationship()
                                                                                                   .withRelationType(RelationshipType.RelatedTo)
                                                                                                   .withProductIdentifier(ipConnectGatewayConfigurationProductIdentifier);

        final ProductOfferingFixture ipConnectGatewayConfigurationProductOfferingFixture = ProductOfferingFixture.aProductOffering()
                                                                                             .withSalesRelationship(ipConnectGatewayConfigurationSalesRelationshipFixture)
                                                                                             .withProductIdentifier(ipConnectGatewayConfigurationProductIdentifier)
                                                                                             .withRFOAttribute(contactName);

        ipConnectGatewayConfiguration = DefaultProductInstanceFixture.aProductInstance()
                                                    .withProductIdentifier(ipConnectGatewayConfigurationProductIdentifier)
                                                    .withProductOffering(ipConnectGatewayConfigurationProductOfferingFixture)
                                                    .withProductInstanceId(relatedProductInstanceId)
                                                    .withRFOAttributeValue(contractId, "contractID_54321", false, true)
                                                    .build();

        steelHeadOne.addRelationship(new ProductSalesRelationshipInstance(ipConnectGatewayConfigurationSalesRelationshipFixture.build(), ipConnectGatewayConfiguration));
    }

    private XSSFWorkbook createWorkBookFor(ProductCodes rootProduct) {
        XSSFWorkbook workbook = new XSSFWorkbook();
        final Sheet sheet = workbook.createSheet(rootProduct.productName());
        final Row row = sheet.createRow(0);

        String rootProductCode = rootProduct.productCode();
        row.createCell(0).setCellValue(rootProductCode);
        row.createCell(1).setCellValue(rootProductCode);
        row.createCell(2).setCellValue(rootProductCode);
        row.createCell(3).setCellValue(rootProductCode);
        row.createCell(4).setCellValue(rootProductCode);
        row.createCell(5).setCellValue(rootProductCode);
        row.createCell(6).setCellValue(childScode1);
        row.createCell(7).setCellValue(childScode1);
        row.createCell(8).setCellValue(childScode1);

        final Row row1 = sheet.createRow(1);
        row1.createCell(LINE_ITEM_ID.column).setCellValue(LINE_ITEM_ID.header);
        row1.createCell(SITE_ID.column).setCellValue(SITE_ID.header);
        row1.createCell(SITE_NAME.column).setCellValue(SITE_NAME.header);
        row1.createCell(SUMMARY.column).setCellValue(SUMMARY.header);
        row1.createCell(4).setCellValue(productType.concat(" (O)"));
        row1.createCell(5).setCellValue(serviceDelivery.concat(" (M)"));
        row1.createCell(6).setCellValue(PRODUCT_INSTANCE_ID_HEADER);
        row1.createCell(7).setCellValue(PRODUCT_NAME_HEADER);
        row1.createCell(8).setCellValue(contactName.concat(" (O)"));

        Row row2 = sheet.createRow(2);
        row2.createCell(0).setCellValue(rootLineItemId);
        row2.createCell(1).setCellValue("siteId");
        row2.createCell(2).setCellValue("siteName");
        row2.createCell(3).setCellValue("summary");
        row2.createCell(4).setCellValue("updated product type");
        row2.createCell(5).setCellValue("cell value");
        row2.createCell(6).setCellValue(productInstanceIdOne);
        row2.createCell(7).setCellValue("childProductName1");
        row2.createCell(8).setCellValue("updated contact 1");

        Row row3 = sheet.createRow(3);
        row3.createCell(6).setCellValue(productInstanceIdTwo);
        row3.createCell(7).setCellValue("childProductName2");
        row3.createCell(8).setCellValue("updated contact 2");

        return workbook;
    }

    private XSSFWorkbook createBFGContactsWorkBookFor(ProductCodes rootProduct) {
        XSSFWorkbook workbook = new XSSFWorkbook();
        final Sheet sheet = workbook.createSheet(rootProduct.productName());
        final Row row = sheet.createRow(0);

        String rootProductCode = rootProduct.productCode();
        row.createCell(0).setCellValue(rootProductCode);
        row.createCell(1).setCellValue(rootProductCode);
        row.createCell(2).setCellValue(rootProductCode);
        row.createCell(3).setCellValue(rootProductCode);
        row.createCell(4).setCellValue(rootProductCode);
        row.createCell(5).setCellValue(childScode1);
        row.createCell(6).setCellValue(childScode1);

        final Row row1 = sheet.createRow(1);
        row1.createCell(LINE_ITEM_ID.column).setCellValue(LINE_ITEM_ID.header);
        row1.createCell(SITE_ID.column).setCellValue(SITE_ID.header);
        row1.createCell(SITE_NAME.column).setCellValue(SITE_NAME.header);
        row1.createCell(SUMMARY.column).setCellValue(SUMMARY.header);
        row1.createCell(4).setCellValue(productType.concat(" (O)"));
        row1.createCell(5).setCellValue(CONTACT_FIRST_NAME.columnName(CHANNEL_CONSULTANT));
        row1.createCell(6).setCellValue(CONTACT_LAST_NAME.columnName(CHANNEL_CONSULTANT));
        row1.createCell(7).setCellValue(CONTACT_JOB_TITLE.columnName(CHANNEL_CONSULTANT));
        row1.createCell(8).setCellValue(CONTACT_PHONE_NUMBER.columnName(CHANNEL_CONSULTANT));
        row1.createCell(9).setCellValue(CONTACT_USER_NAME_EIN.columnName(CHANNEL_CONSULTANT));
        row1.createCell(10).setCellValue(CONTACT_EMAIL_ADDRESS.columnName(CHANNEL_CONSULTANT));
        row1.createCell(11).setCellValue(CONTACT_FIRST_NAME.columnName(CHANNEL_ANALYST));

        final Row row2 = sheet.createRow(2);
        row2.createCell(0).setCellValue(rootLineItemId);
        row2.createCell(1).setCellValue("siteId");
        row2.createCell(2).setCellValue("siteName");
        row2.createCell(3).setCellValue("summary");
        row2.createCell(4).setCellValue("updated product type");
        row2.createCell(5).setCellValue("ChannelConsultantFirstName");
        row2.createCell(6).setCellValue("ChannelConsultantLastName");
        row2.createCell(7).setCellValue("ChannelConsultantJobTitle");
        row2.createCell(8).setCellValue("ChannelConsultantPhoneNumber");
        row2.createCell(9).setCellValue("ChannelConsultantUserEin");
        row2.createCell(10).setCellValue("ChannelConsultantEmailAddress");
        row2.createCell(11).setCellValue("ChannelAnalystFirstName");

        return workbook;
    }

    @Test
    public void shouldUpdateContractIDForAllRootProductsFromRFOSheet() throws InstanceCharacteristicNotFound, IOException {
        createProductInstanceTree();
        final XSSFWorkbook workBook = createWorkBookFor(connectAcceleration);

        when(lineItemModel.getLineItemId()).thenReturn(new LineItemId("Line Item Id"));
        when(lineItemModel.getSite()).thenReturn(siteDTO);
        when(lineItemModel.getSummary()).thenReturn("summary");
        final AssetDTO asset = AssetDTOFixture.anAsset().build();
        when(lineItemModel.getRootInstance()).thenReturn(asset);
        when(futureProductInstanceClient.convertAssetToLightweightInstance(asset)).thenReturn(rootProductInstance);
        when(futureProductInstanceClient.getSourceAssetDTO(rootProductInstance.getProductInstanceId().getValue())).thenReturn(Optional.<AssetDTO>absent());
        mergeResult = new MergeResult(newArrayList(rootProductInstance), tracker);
        when(tracker.changeFor(rootProductInstance)).thenReturn(ADD);
        when(futureProductInstanceClient.getMergeResult(rootProductInstance, null, InstanceTreeScenario.PROVIDE)).thenReturn(mergeResult);
        when(futureProductInstanceClient.getAssetsDiff(anyString(), anyLong(), anyLong(), any(InstanceTreeScenario.class))).thenReturn(mergeResult);
        when(futureProductInstanceClient.getSourceAsset(any(LengthConstrainingProductInstanceId.class))).thenReturn(Optional.<ProductInstance>absent());
        when(futureProductInstanceClient.getByAssetKey(any(AssetKey.class))).thenReturn(rootProductInstance);
        rfoSheetModel.add(lineItemModel, rootScode);
        Map<String, RFOSheetModel> rfoSheetModel = new HashMap<String, RFOSheetModel>();
        rfoSheetModel.put(connectAcceleration.productCode(), this.rfoSheetModel);

        String expedioContractID = "expedioContractID";
        rfoUpdater.updateProductInstanceAndOrderDetails("projectId", "quoteOptionId", "orderId", rfoSheetModel,
                                                        ordersheetDemarsheller, workBook, expedioContractID);

        assertThat(rootProductInstance.getInstanceCharacteristic(contractId).getValue().toString(), is(expedioContractID));
        assertThat(ipConnectGatewayConfiguration.getInstanceCharacteristic(contractId).getValue().toString(), is(expedioContractID));
    }

}
