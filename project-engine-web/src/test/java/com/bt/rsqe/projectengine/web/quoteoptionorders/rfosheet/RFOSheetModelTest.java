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
import com.bt.rsqe.domain.bom.fixtures.BehaviourFixture;
import com.bt.rsqe.domain.bom.fixtures.BehaviourStrategyFixture;
import com.bt.rsqe.domain.bom.fixtures.ProductOfferingFixture;
import com.bt.rsqe.domain.bom.fixtures.SalesRelationshipFixture;
import com.bt.rsqe.domain.product.AttributeName;
import com.bt.rsqe.domain.product.Behaviour;
import com.bt.rsqe.domain.product.BehaviourStrategy;
import com.bt.rsqe.domain.product.ConfigurationPhase;
import com.bt.rsqe.domain.product.ContributesToCharacteristicUpdater;
import com.bt.rsqe.domain.product.DefaultProductInstanceFixture;
import com.bt.rsqe.domain.product.InstanceCharacteristic;
import com.bt.rsqe.domain.product.InstanceTreeScenario;
import com.bt.rsqe.domain.product.LeadToCashPhase;
import com.bt.rsqe.domain.product.LifeTime;
import com.bt.rsqe.domain.product.SimpleProductOfferingType;
import com.bt.rsqe.domain.product.VisibleInSummary;
import com.bt.rsqe.domain.product.Writability;
import com.bt.rsqe.domain.product.constraints.AttributeRequiredConstraint;
import com.bt.rsqe.domain.product.constraints.Constraint;
import com.bt.rsqe.domain.product.parameters.ProductIdentifier;
import com.bt.rsqe.domain.project.ProductInstance;
import com.bt.rsqe.domain.project.ProductInstanceStatus;
import com.bt.rsqe.productinstancemerge.MergeResult;
import com.bt.rsqe.productinstancemerge.changetracker.ChangeTracker;
import com.bt.rsqe.projectengine.migration.QuoteMigrationDetailsProvider;
import com.bt.rsqe.projectengine.web.model.LineItemModel;
import com.google.common.base.Optional;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import static com.bt.rsqe.domain.product.InstanceTreeScenario.PROVIDE;
import static com.bt.rsqe.domain.product.LifeTime.*;
import static com.bt.rsqe.domain.product.Writability.*;
import static com.bt.rsqe.domain.product.parameters.RelationshipType.*;
import static com.bt.rsqe.productinstancemerge.ChangeType.*;
import static com.google.common.collect.Lists.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.*;

public class RFOSheetModelTest {
    RFOSheetModel rfoSheetModel;
    @Mock
    @SuppressWarnings("unused")
    private ProductInstanceClient productInstanceClient;
    @Mock
    @SuppressWarnings("unused")
    private LineItemModel lineItemModel;
    @Mock
    @SuppressWarnings("unused")
    private ChangeTracker tracker;
    @Mock
    private QuoteMigrationDetailsProvider migrationDetailsProvider;
    @Mock
    private ContributesToCharacteristicUpdater contributesToCharacteristicUpdater;

    private String sCode;
    private SiteDTO siteDTO;
    private LineItemId lineItemId;
    private MergeResult mergeResult;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        sCode = "S0308469";
        rfoSheetModel = new RFOSheetModel(productInstanceClient, "sheetName", sCode, migrationDetailsProvider, contributesToCharacteristicUpdater);
        String siteName = "siteName";
        lineItemId = new LineItemId("lineItemId");
        siteDTO = new SiteDTO(new RandomSiteId().value(), siteName);
        when(migrationDetailsProvider.conditionalFor(Mockito.any(ProductInstance.class))).thenCallRealMethod();
        when(migrationDetailsProvider.isMigrationQuote(Mockito.any(String.class), Mockito.any(String.class))).thenReturn(Optional.<Boolean>absent());
        when(migrationDetailsProvider.getMigrationDetailsForProductCode(Mockito.any(String.class))).thenReturn(Optional.<ProductCategoryMigration>absent());
        when(productInstanceClient.getSourceAssetDTO(Mockito.any(String.class))).thenReturn(Optional.<AssetDTO>absent());
    }

    private void seedRootAsset(ProductInstance toReturn) {
        final AssetDTO asset = AssetDTOFixture.anAsset().build();
        when(lineItemModel.getRootInstance()).thenReturn(asset);
        when(productInstanceClient.convertAssetToLightweightInstance(asset)).thenReturn(toReturn);
        when(productInstanceClient.getMergeResult(toReturn, null, PROVIDE)).thenReturn(mergeResult);
    }

    @Test
    public void shouldTestForStencilledProductName() throws Exception {
        ProductInstance productInstance = DefaultProductInstanceFixture.aProductInstance()
                                                                       .withProductOffering(ProductOfferingFixture.aProductOffering()
                                                                                                                  .withSimpleProductOfferingType(SimpleProductOfferingType.Package)
                                                                                                                  .withProductIdentifier(new ProductIdentifier("S0308454", "Connection Acceleration Site"))
                                                                                                                  .withSalesRelationship(SalesRelationshipFixture.aSalesRelationship()
                                                                                                                                                                 .withProductIdentifier(sCode, "Steelhead")
                                                                                                                                                                 .withRelationType(Child))).build();

        ProductInstance childProductInstance = DefaultProductInstanceFixture.aProductInstance()
                                                                            .withProductOffering(ProductOfferingFixture
                                                                                                     .aStencilableProductOfferingFullyConfiguredInstanceCharacteristic()
                                                                                                     .withSimpleProductOfferingType(SimpleProductOfferingType.FOI)

                                                                                                     .withProductIdentifier(sCode)
                                                                            ).build();
        mergeResult = new MergeResult(newArrayList(productInstance, childProductInstance), tracker);
        when(tracker.changeFor(productInstance)).thenReturn(ADD);
        when(tracker.changeFor(childProductInstance)).thenReturn(UPDATE);
        when(productInstanceClient.getAssetsDiff(anyString(), anyLong(), anyLong(), any(InstanceTreeScenario.class))).thenReturn(mergeResult);
        when(productInstanceClient.getSourceAsset(any(LengthConstrainingProductInstanceId.class))).thenReturn(Optional.<ProductInstance>absent());
        productInstance.addChildProductInstance(childProductInstance, Child);
        when(lineItemModel.getSite()).thenReturn(siteDTO);
        when(lineItemModel.getLineItemId()).thenReturn(lineItemId);
        seedRootAsset(productInstance);
        rfoSheetModel.add(lineItemModel, sCode);

        assertThat(rfoSheetModel.getRFOExportModel().get(0).getChildren(sCode).get(0).getAttribute(RFOSheetModel.PRODUCT_NAME_HEADER), is("steelhead"));
    }

    @Test
    public void shouldTestForHiddenRFOAttributeNotExported() {
        ProductInstance productInstance = DefaultProductInstanceFixture.aProductInstance()
                                                                       .withProductOffering(ProductOfferingFixture.aProductOffering()
                                                                                                                  .withSimpleProductOfferingType(SimpleProductOfferingType.VAS)
                                                                                                                  .withProductIdentifier(new ProductIdentifier("S0308496", "Riverbed Professional Services"))

                                                                       ).withRFOAttributeValue("Contract Id", "54321", false, false)
                                                                       .withRFOAttributeValue("NonHiddenRFO", "someValue", false, false).build();
        when(lineItemModel.getSite()).thenReturn(siteDTO);
        when(lineItemModel.getLineItemId()).thenReturn(lineItemId);
        mergeResult = new MergeResult(newArrayList(productInstance), tracker);
        when(tracker.changeFor(productInstance)).thenReturn(ADD);
        when(productInstanceClient.getAssetsDiff(anyString(), anyLong(), anyLong(), any(InstanceTreeScenario.class))).thenReturn(mergeResult);
        when(productInstanceClient.getSourceAsset(any(LengthConstrainingProductInstanceId.class))).thenReturn(Optional.<ProductInstance>absent());
        seedRootAsset(productInstance);
        rfoSheetModel.add(lineItemModel, sCode);
        assertThat(rfoSheetModel.getRFOExportModel().get(0).getAttribute("Contract Id (O)"), is(nullValue()));
        assertThat(rfoSheetModel.getRFOExportModel().get(0).getAttribute("NonHiddenRFO (M)"), is("someValue"));
    }

    @Test(expected = RFOImportException.class)
    public void shouldThrowErrorIfProductValidationFailed(){
        BehaviourStrategy behaviourStrategy = BehaviourStrategyFixture.aBehaviourStrategy()
                                                                      .withLifeTime(LifeTime.Asset)
                                                                      .withVisible(true)
                                                                      .withOptional(false)
                                                                      .withWritability(Writability.Updatable)
                                                                      .withIsVisibleInSummary(VisibleInSummary.No).build();

        Behaviour behaviour = BehaviourFixture.aBehaviour().withBehaviourStrategy(behaviourStrategy)
                                                        .withDisplayName("CONTRACT ID")
                                                        .forConfigurationPhase(ConfigurationPhase.POST_CREDIT_VET)
                                                        .forLeadToCashPhase(LeadToCashPhase.REQUIRED_FOR_ORDER).build();
        Constraint<InstanceCharacteristic> contraint = new AttributeRequiredConstraint(newArrayList(behaviour));
        ProductInstance productInstance = DefaultProductInstanceFixture.aProductInstance().withProductInstanceId("assetId")
                                                                       .withProductInstanceVersion(1L)
                                                                       .withStatus(ProductInstanceStatus.LIVE)
                                                                       .withProductOffering(ProductOfferingFixture.aProductOffering()
                                                                                                                  .withSimpleProductOfferingType(SimpleProductOfferingType.VAS)
                                                                                                                  .withAttribute(AttributeFixture.anAttribute().withBehaviours(behaviour)
                                                                                                                                                 .called(new AttributeName("CONTRACT ID")).withConstraint(contraint)
                                                                                                                                                 .build())
                                                                                                                  .withProductIdentifier(new ProductIdentifier("S0308496", "Riverbed Professional Services")))
                                                                       .build();
        setupRFOSheetModelForUpdatedProductInstance(productInstance);

        rfoSheetModel.update();

    }

    @Test
    @Ignore("switch of story- 1748")
    public void shouldGetOnlyCeaseAttributesWhenItsACeaseJourney() {
        ProductInstance productInstance = DefaultProductInstanceFixture.aProductInstance().withStatus(ProductInstanceStatus.CEASED)
                                                                       .withProductOffering(ProductOfferingFixture.aProductOffering()
                                                                                                                  .withProductIdentifier(new ProductIdentifier("S0308496", "Riverbed Professional Services"))

                                                                       ).withRFOAttributeValue("Contract Id", "54321", AddOrder, Updatable, false, false)
                                                                       .withRFOAttributeValue("NonHiddenRFO", "someValue", AddOrder, Updatable, false, false)
                                                                       .withRFOAttributeValue("modifyAttribute", "modifyValue", AddOrder, Updatable, false, false)
                                                                       .withRFOAttributeValue("NonHiddenRFO", "someValue", AddOrder, Updatable, false, false)
                                                                       .withRFOAttributeValue("ceaseAttribute", "ceaseValue", AddAndDeleteOrder, Updatable, false, false).build();
        when(lineItemModel.getSite()).thenReturn(siteDTO);
        when(lineItemModel.getLineItemId()).thenReturn(lineItemId);
        when(productInstanceClient.get(lineItemId)).thenReturn(productInstance);
        mergeResult = new MergeResult(newArrayList(productInstance), tracker);
        when(tracker.changeFor(productInstance)).thenReturn(DELETE);
        when(productInstanceClient.getAssetsDiff(anyString(), anyLong(), anyLong(), any(InstanceTreeScenario.class))).thenReturn(mergeResult);
        when(productInstanceClient.getSourceAsset(any(LengthConstrainingProductInstanceId.class))).thenReturn(Optional.<ProductInstance>absent());
        rfoSheetModel.add(lineItemModel, sCode);

        assertNull(rfoSheetModel.getRFOExportModel().get(0).getAttribute("Contract Id (M)"));
        assertNull(rfoSheetModel.getRFOExportModel().get(0).getAttribute("NonHiddenRFO (M)"));
        assertNull(rfoSheetModel.getRFOExportModel().get(0).getAttribute("modifyAttribute (M)"));
        assertNotNull(rfoSheetModel.getRFOExportModel().get(0).getAttribute("ceaseAttribute (M)"));

    }

    @Test
    @Ignore("switch of story- 1748")
    public void shouldGetOnlyModifyAttributesWhenItsAModifyJourney() {
        ProductInstance productInstance = DefaultProductInstanceFixture.aProductInstance().withStatus(ProductInstanceStatus.LIVE)
                                                                       .withProductOffering(ProductOfferingFixture.aProductOffering()
                                                                                                                  .withProductIdentifier(new ProductIdentifier("S0308496", "Riverbed Professional Services"))

                                                                       ).withRFOAttributeValue("Contract Id", "54321", AddOrder, Updatable, false, false)
                                                                       .withRFOAttributeValue("NonHiddenRFO", "someValue", AddOrder, Updatable, false, false)
                                                                       .withRFOAttributeValue("modifyAttribute", "modifyValue", Asset, Updatable, false, false)
                                                                       .withRFOAttributeValue("NonHiddenRFO", "someValue", AddOrder, Updatable, false, false)
                                                                       .withRFOAttributeValue("ceaseAttribute", "ceaseValue", AddAndDeleteOrder, Updatable, false, false).build();
        when(lineItemModel.getSite()).thenReturn(siteDTO);
        when(lineItemModel.getLineItemId()).thenReturn(lineItemId);
        when(productInstanceClient.get(lineItemId)).thenReturn(productInstance);
        mergeResult = new MergeResult(newArrayList(productInstance), tracker);
        when(tracker.changeFor(productInstance)).thenReturn(UPDATE);
        when(productInstanceClient.getAssetsDiff(anyString(), anyLong(), anyLong(), any(InstanceTreeScenario.class))).thenReturn(mergeResult);
        when(productInstanceClient.getSourceAsset(any(LengthConstrainingProductInstanceId.class))).thenReturn(Optional.<ProductInstance>absent());
        rfoSheetModel.add(lineItemModel, sCode);
        assertNull(rfoSheetModel.getRFOExportModel().get(0).getAttribute("Contract Id (M)"));
        assertNull(rfoSheetModel.getRFOExportModel().get(0).getAttribute("NonHiddenRFO (M)"));
        assertNotNull(rfoSheetModel.getRFOExportModel().get(0).getAttribute("modifyAttribute (M)"));
        assertNull(rfoSheetModel.getRFOExportModel().get(0).getAttribute("ceaseAttribute (M)"));
    }

    @Test
    public void shouldLockedColumnsWhenItsAttributeIsReadOnly() {
        ProductInstance productInstance = DefaultProductInstanceFixture.aProductInstance().withStatus(ProductInstanceStatus.LIVE)
                                                                       .withProductOffering(ProductOfferingFixture.aProductOffering()
                                                                                                                  .withSimpleProductOfferingType(SimpleProductOfferingType.NetworkNode)
                                                                                                                  .withProductIdentifier(new ProductIdentifier("S0308496", "Riverbed Professional Services"))

                                                                       ).withRFOAttributeValue("UpdatableRFO", "54321", AddOrder, Updatable, false, false)
                                                                       .withRFOAttributeValue("ConstantRFO", "54321", AddOrder, Constant, false, false)
                                                                       .withRFOAttributeValue("FixedRFO", "someValue", AddOrder, Fixed, false, false).build();
        when(lineItemModel.getSite()).thenReturn(siteDTO);
        when(lineItemModel.getLineItemId()).thenReturn(lineItemId);

        mergeResult = new MergeResult(newArrayList(productInstance), tracker);
        when(tracker.changeFor(productInstance)).thenReturn(ADD);
        when(productInstanceClient.getAssetsDiff(anyString(), anyLong(), anyLong(), any(InstanceTreeScenario.class))).thenReturn(mergeResult);
        when(productInstanceClient.getSourceAsset(any(LengthConstrainingProductInstanceId.class))).thenReturn(Optional.<ProductInstance>absent());
        seedRootAsset(productInstance);
        rfoSheetModel.add(lineItemModel, sCode);
        final RFOSheetModel.RFORowModel model = rfoSheetModel.getRFOExportModel().get(0);
        assertNotNull(model.getAttribute("UpdatableRFO (M)"));
        assertNotNull(model.getAttribute("ConstantRFO (M)"));
        assertNotNull(model.getAttribute("FixedRFO (M)"));
        assertTrue(model.getLockedColumns("ConstantRFO (M)"));
        assertFalse(model.getLockedColumns("UpdatableRFO"));
        assertFalse(model.getLockedColumns("FixedRFO"));
    }

    @Test
    public void shouldGreyedOutColumnsIfAttributesMarkedAsConstant() {
        ProductInstance productInstance = DefaultProductInstanceFixture.aProductInstance().withStatus(ProductInstanceStatus.LIVE)
                                                                       .withProductOffering(ProductOfferingFixture.aProductOffering()
                                                                                                                  .withSimpleProductOfferingType(SimpleProductOfferingType.NetworkNode)
                                                                                                                  .withProductIdentifier(new ProductIdentifier("S0308496", "Riverbed Professional Services"))

                                                                       ).withRFOAttributeValue("UpdatableRFO", "54321", AddOrder, Updatable, false, false)
                                                                       .withRFOAttributeValue("ConstantRFO", "54321", AddOrder, Constant, false, false)
                                                                       .withRFOAttributeValue("FixedRFO", "someValue", AddOrder, Fixed, false, false).build();
        when(lineItemModel.getSite()).thenReturn(siteDTO);
        when(lineItemModel.getLineItemId()).thenReturn(lineItemId);

        mergeResult = new MergeResult(newArrayList(productInstance), tracker);
        when(tracker.changeFor(productInstance)).thenReturn(ADD);
        when(productInstanceClient.getAssetsDiff(anyString(), anyLong(), anyLong(), any(InstanceTreeScenario.class))).thenReturn(mergeResult);
        when(productInstanceClient.getSourceAsset(any(LengthConstrainingProductInstanceId.class))).thenReturn(Optional.<ProductInstance>absent());
        seedRootAsset(productInstance);
        rfoSheetModel.add(lineItemModel, sCode);

        final RFOSheetModel.RFORowModel model = rfoSheetModel.getRFOExportModel().get(0);

        assertTrue(model.getGrayOutColumns("ConstantRFO (M)"));
        assertFalse(model.getGrayOutColumns("FixedRFO (M))"));
    }

    @Test
    public void shouldUseDisplayNameForHeader() throws Exception {
        ProductInstance productInstance = DefaultProductInstanceFixture.aProductInstance()
                                                                       .withStatus(ProductInstanceStatus.LIVE)
                                                                       .withProductOffering(ProductOfferingFixture.aProductOffering()
                                                                                                                  .withSimpleProductOfferingType(SimpleProductOfferingType.NetworkNode)
                                                                                                                  .withAttribute(AttributeFixture.anRfoAttribute()
                                                                                                                                                 .called(new AttributeName("CONTRACT ID", "Contract ID Display Name"))
                                                                                                                                                 .build())
                                                                                                                  .withProductIdentifier(new ProductIdentifier("S0308496", "Riverbed Professional Services")))
                                                                       .withAttributeValue("CONTRACT ID", "someValue")
                                                                       .build();

        setupRFOSheetModelForUpdatedProductInstance(productInstance);

        rfoSheetModel.update();

        assertThat(rfoSheetModel.getRFOExportModel().get(0).getAttribute("Contract ID Display Name (M)"), is("someValue"));
    }

    @Test
    public void shouldUseAttributeNameForHeaderWhenDisplayNameIsNotAvailable() throws Exception {
        ProductInstance productInstance = DefaultProductInstanceFixture.aProductInstance()
                                                                       .withStatus(ProductInstanceStatus.LIVE)
                                                                       .withProductOffering(ProductOfferingFixture.aProductOffering()
                                                                                                                  .withSimpleProductOfferingType(SimpleProductOfferingType.NetworkNode)
                                                                                                                  .withAttribute(AttributeFixture.anRfoAttribute()
                                                                                                                                                 .called(new AttributeName("CONTRACT ID"))
                                                                                                                                                 .build())
                                                                                                                  .withProductIdentifier(new ProductIdentifier("S0308496", "Riverbed Professional Services")))
                                                                       .withAttributeValue("CONTRACT ID", "someValue")
                                                                       .build();

        setupRFOSheetModelForUpdatedProductInstance(productInstance);

        rfoSheetModel.update();

        assertThat(rfoSheetModel.getRFOExportModel().get(0).getAttribute("CONTRACT ID (M)"), is("someValue"));
    }

    @Test
    public void shouldExportServiceDeliveryAttributesIfAssetIsOnAMigrationQuoteAndExistingBFGInventoryIsSupported() {
        ProductInstance productInstance = DefaultProductInstanceFixture.aProductInstance()
                                                                       .withProductOffering(ProductOfferingFixture.aProductOffering()
                                                                                                                  .withSimpleProductOfferingType(SimpleProductOfferingType.NetworkNode)
                                                                                                                  .withProductIdentifier(new ProductIdentifier("S0308496", "Riverbed Professional Services")))
                                                                       .withRFOAttributeValue("IsServiceDelivery Hidden Optional", "value1", false, false, ConfigurationPhase.SERVICE_DELIVERY)
                                                                       .withRFOAttributeValue("NotServiceDelivery NotHidden Optional", "value2", true, false, ConfigurationPhase.POST_CREDIT_VET).build();
        when(lineItemModel.getSite()).thenReturn(siteDTO);
        when(lineItemModel.getLineItemId()).thenReturn(lineItemId);

        mergeResult = new MergeResult(newArrayList(productInstance), tracker);
        when(tracker.changeFor(productInstance)).thenReturn(ADD);
        when(productInstanceClient.getAssetsDiff(anyString(), anyLong(), anyLong(), any(InstanceTreeScenario.class))).thenReturn(mergeResult);
        when(productInstanceClient.getSourceAsset(any(LengthConstrainingProductInstanceId.class))).thenReturn(Optional.<ProductInstance>absent());
        when(migrationDetailsProvider.conditionalFor(productInstance)).thenCallRealMethod();
        when(migrationDetailsProvider.isMigrationQuote(productInstance.getProjectId(), productInstance.getQuoteOptionId())).thenReturn(com.google.common.base.Optional.of(true));
        when(migrationDetailsProvider.getMigrationDetailsForProductCode(productInstance.getProductIdentifier().getProductId())).thenReturn(com.google.common.base.Optional.of(new ProductCategoryMigration(true, true, false)));
        seedRootAsset(productInstance);
        rfoSheetModel.add(lineItemModel, sCode);
        assertThat(rfoSheetModel.getRFOExportModel().get(0).getAttribute("IsServiceDelivery Hidden Optional (M)"), is("value1"));
        assertThat(rfoSheetModel.getRFOExportModel().get(0).getAttribute("NotServiceDelivery NotHidden Optional (O)"), is("value2"));
        assertThat(rfoSheetModel.getRFOExportModel().get(0).getGrayOutColumns("IsServiceDelivery Hidden Optional (M)"), is(false));
        assertThat(rfoSheetModel.getRFOExportModel().get(0).getGrayOutColumns("NotServiceDelivery NotHidden Optional (O)"), is(false));
    }

    @Test
    public void shouldNotExportServiceDeliveryAttributesIfAssetIsNotOnAMigrationQuoteAndAttributesAreHidden() {
        ProductInstance productInstance = DefaultProductInstanceFixture.aProductInstance()
                                                                       .withProductOffering(ProductOfferingFixture.aProductOffering()
                                                                                                                  .withSimpleProductOfferingType(SimpleProductOfferingType.NetworkNode)
                                                                                                                  .withProductIdentifier(new ProductIdentifier("S0308496", "Riverbed Professional Services")))
                                                                       .withRFOAttributeValue("Attribute Hidden Optional1", "value1", true, true, ConfigurationPhase.SERVICE_DELIVERY)
                                                                       .withRFOAttributeValue("Attribute Hidden Optional2", "value2", true, true, ConfigurationPhase.POST_CREDIT_VET).build();
        when(lineItemModel.getSite()).thenReturn(siteDTO);
        when(lineItemModel.getLineItemId()).thenReturn(lineItemId);
        when(productInstanceClient.get(lineItemId)).thenReturn(productInstance);
        mergeResult = new MergeResult(newArrayList(productInstance), tracker);
        when(tracker.changeFor(productInstance)).thenReturn(ADD);
        when(productInstanceClient.getAssetsDiff(anyString(), anyLong(), anyLong(), any(InstanceTreeScenario.class))).thenReturn(mergeResult);
        when(productInstanceClient.getSourceAsset(any(LengthConstrainingProductInstanceId.class))).thenReturn(Optional.<ProductInstance>absent());
        seedRootAsset(productInstance);
        rfoSheetModel.add(lineItemModel, sCode);
        assertThat(rfoSheetModel.getRFOExportModel().get(0).getAttribute("Attribute Hidden Optional1 (O)"), is(nullValue()));
        assertThat(rfoSheetModel.getRFOExportModel().get(0).getAttribute("Attribute Hidden Optional2 (O)"), is(nullValue()));
    }

    @Test
    public void shouldExportServiceDeliveryAttributesIfAssetIsOnAMigrationQuoteButExistingBFGInventoryIsNotSupported() {
        ProductInstance productInstance = DefaultProductInstanceFixture.aProductInstance()
                                                                       .withProductOffering(ProductOfferingFixture.aProductOffering()
                                                                                                                  .withSimpleProductOfferingType(SimpleProductOfferingType.NetworkNode)
                                                                                                                  .withProductIdentifier(new ProductIdentifier("code", "Riverbed Professional Services")))
                                                                       .withRFOAttributeValue("Attribute Hidden Optional1", "value1", true, true, ConfigurationPhase.SERVICE_DELIVERY)
                                                                       .withRFOAttributeValue("Attribute Hidden Optional2", "value2", true, true, ConfigurationPhase.POST_CREDIT_VET).build();
        when(lineItemModel.getSite()).thenReturn(siteDTO);
        when(lineItemModel.getLineItemId()).thenReturn(lineItemId);

        mergeResult = new MergeResult(newArrayList(productInstance), tracker);
        when(tracker.changeFor(productInstance)).thenReturn(ADD);
        when(productInstanceClient.getAssetsDiff(anyString(), anyLong(), anyLong(), any(InstanceTreeScenario.class))).thenReturn(mergeResult);
        when(productInstanceClient.getSourceAsset(any(LengthConstrainingProductInstanceId.class))).thenReturn(Optional.<ProductInstance>absent());
        when(migrationDetailsProvider.conditionalFor(productInstance)).thenCallRealMethod();
        when(migrationDetailsProvider.isMigrationQuote(productInstance.getProjectId(), productInstance.getQuoteOptionId())).thenReturn(com.google.common.base.Optional.of(true));
        seedRootAsset(productInstance);
        rfoSheetModel.add(lineItemModel, sCode);
        assertThat(rfoSheetModel.getRFOExportModel().get(0).getAttribute("Attribute Hidden Optional1 (O)"), is("value1"));
        assertThat(rfoSheetModel.getRFOExportModel().get(0).getAttribute("Attribute Hidden Optional2 (O)"), is(nullValue()));
    }

    private void setupRFOSheetModelForUpdatedProductInstance(ProductInstance productInstance) {
        when(lineItemModel.getSite()).thenReturn(siteDTO);
        when(lineItemModel.getLineItemId()).thenReturn(lineItemId);
        when(productInstanceClient.get(lineItemId)).thenReturn(productInstance);
        mergeResult = new MergeResult(newArrayList(productInstance), tracker);
        when(tracker.changeFor(productInstance)).thenReturn(UPDATE);
        when(productInstanceClient.getAssetsDiff(anyString(), anyLong(), anyLong(), any(InstanceTreeScenario.class))).thenReturn(mergeResult);
        when(productInstanceClient.getSourceAsset(any(LengthConstrainingProductInstanceId.class))).thenReturn(Optional.<ProductInstance>absent());
        when(productInstanceClient.getByAssetKey(productInstance.getKey())).thenReturn(productInstance);
        seedRootAsset(productInstance);
        rfoSheetModel.add(lineItemModel, sCode);
    }
}
