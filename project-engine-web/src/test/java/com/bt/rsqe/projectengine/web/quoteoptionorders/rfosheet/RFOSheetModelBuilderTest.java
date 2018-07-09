package com.bt.rsqe.projectengine.web.quoteoptionorders.rfosheet;

import com.bt.rsqe.customerinventory.client.ProductInstanceClient;
import com.bt.rsqe.customerinventory.dto.AssetDTO;
import com.bt.rsqe.customerinventory.fixtures.AssetDTOFixture;
import com.bt.rsqe.customerinventory.parameter.LengthConstrainingProductInstanceId;
import com.bt.rsqe.customerinventory.parameter.LineItemId;
import com.bt.rsqe.customerrecord.SiteDTO;
import com.bt.rsqe.domain.ProductCategoryMigration;
import com.bt.rsqe.domain.bom.parameters.ProductInstanceId;
import com.bt.rsqe.domain.product.Attribute;
import com.bt.rsqe.domain.product.AttributeName;
import com.bt.rsqe.domain.product.ContributesToCharacteristicUpdater;
import com.bt.rsqe.domain.product.InstanceCharacteristic;
import com.bt.rsqe.domain.product.InstanceTreeScenario;
import com.bt.rsqe.domain.product.ProductOffering;
import com.bt.rsqe.domain.product.SimpleProductOfferingType;
import com.bt.rsqe.domain.product.constraints.AllowedValuesProvider;
import com.bt.rsqe.domain.product.constraints.AttributeValue;
import com.bt.rsqe.domain.product.extensions.RuleValidationExpression;
import com.bt.rsqe.domain.product.extensions.StructuredRule;
import com.bt.rsqe.domain.product.parameters.ProductIdentifier;
import com.bt.rsqe.domain.project.InstanceCharacteristicNotFound;
import com.bt.rsqe.domain.project.ProductInstance;
import com.bt.rsqe.expressionevaluator.ContextualEvaluatorMap;
import com.bt.rsqe.productinstancemerge.MergeResult;
import com.bt.rsqe.productinstancemerge.changetracker.ChangeTracker;
import com.bt.rsqe.projectengine.migration.QuoteMigrationDetailsProvider;
import com.bt.rsqe.projectengine.web.model.LineItemModel;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.bt.rsqe.domain.product.InstanceTreeScenario.*;
import static com.bt.rsqe.productinstancemerge.ChangeType.*;
import static com.bt.rsqe.utils.Uuid.*;
import static com.google.common.collect.Lists.*;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.anyListOf;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.*;

public class RFOSheetModelBuilderTest {
    public static final String ATTR1 = "Attr1";
    public static final String ATTR1_M = ATTR1 + " (M)";
    public static final String ATTR1_O = ATTR1 + " (O)";
    public static final String S_CODE_3 = "scode3";
    public static final String S_CODE_4 = "scode4";
    public static final String ERROR_TEXT = "error text";
    private static final String ATTR2 = "Atrr2";
    public static final String ATTR2_M = ATTR2 + " (M)";
    public static final String ATTR2_O = ATTR2 + " (O)";
    @MockitoAnnotations.Mock
    @SuppressWarnings("unused")
    private ProductInstanceClient futureProductInstanceClient;
    @Mock
    @SuppressWarnings("unused")
    private ChangeTracker tracker;
    private RFOSheetModelBuilder rfoSheetModelBuilder;
    @Mock
    private QuoteMigrationDetailsProvider migrationDetailsProvider;
    @Mock
    private ContributesToCharacteristicUpdater contributesToCharacteristicUpdater;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        rfoSheetModelBuilder = new RFOSheetModelBuilder(futureProductInstanceClient, migrationDetailsProvider, contributesToCharacteristicUpdater);
    }

    @Test
    public void shouldBuildRFOSheetModelBuilderForDifferentProducts() throws InstanceCharacteristicNotFound {
        Map<String, RFOSheetModel> rfoSheets = rfoSheetModelBuilder.build(asList(mockLineItem("scode1"), mockLineItem("scode1"), mockLineItem("scode2")));
        assertThat(rfoSheets.keySet(), hasItems("scode1", "scode2"));
        assertThat(rfoSheets.get("scode1").getRFOExportModel().size(), is(2));
        assertThat(rfoSheets.get("scode1").sheetName(), is("name for scode1"));
        assertThat(rfoSheets.get("scode2").getRFOExportModel().size(), is(1));
        assertThat(rfoSheets.get("scode2").sheetName(), is("name for scode2"));
    }

    @Test
    public void shouldBuildRFOSheetModelBuilderForSiteAgnosticProducts() throws InstanceCharacteristicNotFound {
        Map<String, RFOSheetModel> rfoSheets = rfoSheetModelBuilder.build(asList(siteAgnosticMockLineItem("scode1"), siteAgnosticMockLineItem("scode1"), siteAgnosticMockLineItem("scode2")));
        assertThat(rfoSheets.keySet(), hasItems("scode1", "scode2"));
        final List<RFOSheetModel.RFORowModel> sheetForSCode1 = rfoSheets.get("scode1").getRFOExportModel();
        assertThat(sheetForSCode1.size(), is(2));
        assertThat(rfoSheets.get("scode1").sheetName(), is("name for scode1"));
        assertThat(rfoSheets.get("scode2").getRFOExportModel().size(), is(1));
        assertThat(rfoSheets.get("scode2").sheetName(), is("name for scode2"));
        for (RFOSheetModel rfoSheetModel : rfoSheets.values()) {
            for (RFOSheetModel.RFORowModel rfoRowModel : rfoSheetModel.getRFOExportModel()) {
                assertThat(rfoRowModel.getAttributes(), org.hamcrest.Matchers.hasEntry(RFOSheetMarshaller.Column.SITE_NAME.header, SiteDTO.CUSTOMER_OWNED.name));
            }
        }
    }

    @Test
    public void shouldBuildRFOSheetWithDefaultedAttributes() throws InstanceCharacteristicNotFound {
        InstanceDefinition instance1Child1 = new InstanceDefinition(S_CODE_4, newArrayList(new AttributeDefinition(ATTR1, "attrVal3", true, Optional.<AllowedValuesProvider>absent())));
        InstanceDefinition instance1 = new InstanceDefinition(S_CODE_3, newArrayList(new AttributeDefinition(ATTR1, "attrVal1", true, Optional.<AllowedValuesProvider>absent())),
                                                              newArrayList(instance1Child1));
        InstanceDefinition instance2Child1 = new InstanceDefinition(S_CODE_4, newArrayList(new AttributeDefinition(ATTR1, "attrVal4", false, Optional.<AllowedValuesProvider>absent())));
        InstanceDefinition instance2 = new InstanceDefinition(S_CODE_3, newArrayList(new AttributeDefinition(ATTR1, "attrVal2", false, Optional.<AllowedValuesProvider>absent())),
                                                              newArrayList(instance2Child1));

        final Map<String, RFOSheetModel> rfoSheets = rfoSheetModelBuilder.build(newArrayList(siteAgnosticMockLineItem(instance1),
                                                                                             siteAgnosticMockLineItem(instance2)));

        assertThat(rfoSheets.size(), is(1));

        final RFOSheetModel rfoSheetModel = rfoSheets.get(S_CODE_3);
        final List<RFOSheetModel.RFORowModel> rowModelList = rfoSheetModel.getRFOExportModel();
        assertThat(rowModelList.size(), is(2));

        final RFOSheetModel.RFORowModel row1 = rowModelList.get(0);
        final RFOSheetModel.RFORowModel row2 = rowModelList.get(1);

        assertAttributeEditable(row1, ATTR1_O, "attrVal1");
        assertAttributeUneditable(row1, ATTR1_M, "");
        assertAttributeEditable(row1.getChildren(S_CODE_4).get(0), ATTR1_O, "attrVal3");
        assertAttributeUneditable(row1.getChildren(S_CODE_4).get(0), ATTR1_M, "");

        assertAttributeEditable(row2, ATTR1_M, "attrVal2");
        assertAttributeUneditable(row2, ATTR1_O, "");
        assertAttributeEditable(row2.getChildren(S_CODE_4).get(0), ATTR1_M, "attrVal4");
        assertAttributeUneditable(row2.getChildren(S_CODE_4).get(0), ATTR1_O, "");
    }

    @Test
    public void shouldBuildRFOSheetWithAttributeWithAllowedValues() throws InstanceCharacteristicNotFound {
        AllowedValuesProvider allowedValuesProvider1 = mock(AllowedValuesProvider.class);
        when(allowedValuesProvider1.getAllowedValues()).thenReturn(newArrayList(AttributeValue.newInstance("Val1"), AttributeValue.newInstance("Val2")));
        when(allowedValuesProvider1.getAllowUserOverride()).thenReturn(false);
        AllowedValuesProvider allowedValuesProvider2 = mock(AllowedValuesProvider.class);
        when(allowedValuesProvider2.getAllowedValues()).thenReturn(newArrayList(AttributeValue.newInstance("Val3"), AttributeValue.newInstance("Val4")));
        when(allowedValuesProvider2.getAllowUserOverride()).thenReturn(true);

        InstanceDefinition instance1Child1 = new InstanceDefinition(S_CODE_4, newArrayList(new AttributeDefinition(ATTR2, "attrVal3", true, Optional.<AllowedValuesProvider>absent())));
        InstanceDefinition instance1 = new InstanceDefinition(S_CODE_3, newArrayList(new AttributeDefinition(ATTR1, "attrVal1", true, Optional.of(allowedValuesProvider1))),
                                                              newArrayList(instance1Child1));
        InstanceDefinition instance2Child1 = new InstanceDefinition(S_CODE_4, newArrayList(new AttributeDefinition(ATTR2, "attrVal4", false, Optional.<AllowedValuesProvider>absent())));
        InstanceDefinition instance2 = new InstanceDefinition(S_CODE_3, newArrayList(new AttributeDefinition(ATTR1, "attrVal2", false,Optional.of(allowedValuesProvider2))),
                                                              newArrayList(instance2Child1));

        final Map<String, RFOSheetModel> rfoSheets = rfoSheetModelBuilder.build(newArrayList(siteAgnosticMockLineItem(instance1),
                                                                                             siteAgnosticMockLineItem(instance2)));

        assertThat(rfoSheets.size(), is(1));

        final RFOSheetModel rfoSheetModel = rfoSheets.get(S_CODE_3);
        final List<RFOSheetModel.RFORowModel> rowModelList = rfoSheetModel.getRFOExportModel();
        assertThat(rowModelList.size(), is(2));

        final RFOSheetModel.RFORowModel row1 = rowModelList.get(0);
        final RFOSheetModel.RFORowModel row2 = rowModelList.get(1);

        assertAttributeEditable(row1, ATTR1_O, "attrVal1");
        assertAttributeUneditable(row1, ATTR1_M, "");
        assertThat(row1.getAllowedValues(ATTR1_O), hasItems("Val1", "Val2"));
        assertThat(row1.getAllowOverride(ATTR1_O), is(false));
        assertThat(row1.getAllowOverride(ATTR1_M), is(false));
        assertAttributeEditable(row1.getChildren(S_CODE_4).get(0), ATTR2_O, "attrVal3");
        assertAttributeUneditable(row1.getChildren(S_CODE_4).get(0), ATTR2_M, "");

        assertAttributeEditable(row2, ATTR1_M, "attrVal2");
        assertAttributeUneditable(row2, ATTR1_O, "");
        assertThat(row2.getAllowedValues(ATTR1_M), hasItems("Val3", "Val4"));
        assertThat(row2.getAllowOverride(ATTR1_M), is(true));
        assertAttributeEditable(row2.getChildren(S_CODE_4).get(0), ATTR2_M, "attrVal4");
        assertAttributeUneditable(row2.getChildren(S_CODE_4).get(0), ATTR2_O, "");
    }

    @Test
    public void shouldBuildRFOSheetWithConditionalAttributes() throws InstanceCharacteristicNotFound {
        InstanceDefinition instance1Child1 = new InstanceDefinition(S_CODE_4, newArrayList(new AttributeDefinition(ATTR1, "attrVal3", true, Optional.<AllowedValuesProvider>absent())));
        InstanceDefinition instance1 = new InstanceDefinition(S_CODE_3, newArrayList(new AttributeDefinition(ATTR1, "attrVal1", true, Optional.<AllowedValuesProvider>absent())),
                                                              newArrayList(instance1Child1), true);

        final Map<String, RFOSheetModel> rfoSheets = rfoSheetModelBuilder.build(newArrayList(siteAgnosticMockLineItem(instance1)));

        assertThat(rfoSheets.size(), is(1));

        final RFOSheetModel rfoSheetModel = rfoSheets.get(S_CODE_3);
        final List<RFOSheetModel.RFORowModel> rowModelList = rfoSheetModel.getRFOExportModel();
        assertThat(rowModelList.size(), is(1));

        final RFOSheetModel.RFORowModel row1 = rowModelList.get(0);
        assertTrue(row1.getConditionalAttributes(ATTR1_O).get(0).equals(ERROR_TEXT));
    }

    private void assertAttributeEditable(RFOSheetModel.RFORowModel row, String attrName, String value) {
        assertThat(row.getAttributes(), hasEntry(attrName, value));
        assertFalse(row.getGrayOutColumns(attrName));
        assertFalse(row.getLockedColumns(attrName));
    }

    private void assertAttributeUneditable(RFOSheetModel.RFORowModel row, String attrName, String value) {
        assertThat(row.getAttributes(), hasEntry(attrName, value));
        assertTrue(row.getGrayOutColumns(attrName));
        assertTrue(row.getLockedColumns(attrName));
    }

    private LineItemModel siteAgnosticMockLineItem(InstanceDefinition instance) throws InstanceCharacteristicNotFound {
        return mockLineItem(SiteDTO.CUSTOMER_OWNED, instance);
    }

    private LineItemModel siteAgnosticMockLineItem(String sCode) throws InstanceCharacteristicNotFound {
        return mockLineItem(SiteDTO.CUSTOMER_OWNED, new InstanceDefinition(sCode, new ArrayList<AttributeDefinition>()));
    }

    private LineItemModel mockLineItem(String sCode) throws InstanceCharacteristicNotFound {
        return mockLineItem(new InstanceDefinition(sCode, new ArrayList<AttributeDefinition>()));
    }

    private LineItemModel mockLineItem(InstanceDefinition instance) throws InstanceCharacteristicNotFound {
        return mockLineItem(new SiteDTO() {{
            bfgSiteID = "SITE_ID";
            name = "SITE_NAME";
        }}, instance);
    }

    private LineItemModel mockLineItem(SiteDTO siteDTO, InstanceDefinition instance) throws InstanceCharacteristicNotFound {
        final LineItemId lineItemId = new LineItemId(randomUuid());

        LineItemModel lineItemModel = mock(LineItemModel.class);
        when(lineItemModel.getProductSCode()).thenReturn(instance.getSCode());
        when(lineItemModel.getLineItemId()).thenReturn(lineItemId);
        when(lineItemModel.getProductName()).thenReturn("name for " + instance.getSCode());
        when(lineItemModel.getSite()).thenReturn(siteDTO);

        final ProductInstance productInstance = instance.getProductInstance();

        final AssetDTO asset = AssetDTOFixture.anAsset().build();
        when(lineItemModel.getRootInstance()).thenReturn(asset);
        when(futureProductInstanceClient.convertAssetToLightweightInstance(asset)).thenReturn(productInstance);
        when(futureProductInstanceClient.getSourceAssetDTO(productInstance.getProductInstanceId().getValue())).thenReturn(Optional.<AssetDTO>absent());

        when(futureProductInstanceClient.get(lineItemId)).thenReturn(productInstance);
        when(futureProductInstanceClient.getSourceAsset(any(LengthConstrainingProductInstanceId.class))).thenReturn(Optional.<ProductInstance>absent());
        when(migrationDetailsProvider.conditionalFor(Mockito.any(ProductInstance.class))).thenCallRealMethod();
        when(migrationDetailsProvider.isMigrationQuote(Mockito.any(String.class), Mockito.any(String.class))).thenReturn(Optional.<Boolean>absent());
        when(migrationDetailsProvider.getMigrationDetailsForProductCode(Mockito.any(String.class))).thenReturn(Optional.<ProductCategoryMigration>absent());

        return lineItemModel;
    }

    class InstanceDefinition {
        String sCode;
        List<AttributeDefinition> attributes;
        List<InstanceDefinition> children;
        boolean optional = false;

        public InstanceDefinition(String sCode, List<AttributeDefinition> attributes, List<InstanceDefinition> children) {
            this.sCode = sCode;
            this.attributes = attributes;
            this.children = children;
        }

        public InstanceDefinition(String sCode, List<AttributeDefinition> attributes, List<InstanceDefinition> children, boolean optional) {
            this(sCode, attributes, children);
            this.optional = optional;
        }

        public InstanceDefinition(String sCode, List<AttributeDefinition> attributes) {
            this(sCode, attributes, new ArrayList<InstanceDefinition>());
        }

        List<InstanceDefinition> getChildren() {
            return children;
        }

        List<AttributeDefinition> getAttributes() {
            return attributes;
        }

        public String getSCode() {
            return sCode;
        }

        public ProductInstance getProductInstance() throws InstanceCharacteristicNotFound {
            final ProductInstance productInstance = mock(ProductInstance.class);
            final ProductOffering productOffering = mock(ProductOffering.class);
            final RuleValidationExpression ruleValidationExpression = mock(RuleValidationExpression.class);
            final ProductInstanceId productInstanceId = new ProductInstanceId(randomUuid());
            List<StructuredRule> structuredRuleList = newArrayList();
            structuredRuleList.add(ruleValidationExpression);
            when(ruleValidationExpression.isValidationRule()).thenCallRealMethod();

            for (AttributeDefinition attributeDefinition : getAttributes()) {
                final InstanceCharacteristic characteristic = attributeDefinition.getCharacteristic();
                when(productInstance.getInstanceCharacteristic(new AttributeName(attributeDefinition.getName()))).thenReturn(characteristic);
            }

            List<Attribute> attributes = transform(getAttributes(), new Function<AttributeDefinition, Attribute>() {
                @Override
                public Attribute apply(AttributeDefinition attributeDefinition) {
                    return attributeDefinition.getAttribute();
                }
            });
            when(productInstance.whatReadyForOrderAttributesShouldIConfigureForScenario(any(InstanceTreeScenario.class))).thenReturn(attributes);

            Set<ProductInstance> childInstances = new HashSet<ProductInstance>(transform(getChildren(), new Function<InstanceDefinition, ProductInstance>(){
                @Override
                public ProductInstance apply(InstanceDefinition instanceDefinition){
                    try {
                        return instanceDefinition.getProductInstance();
                    } catch (InstanceCharacteristicNotFound instanceCharacteristicNotFound) {
                        return null;
                    }
                }
            }));
            when(productInstance.getChildren()).thenReturn(childInstances);

            when(productInstance.getProductInstanceId()).thenReturn(productInstanceId);
            when(productInstance.getProductIdentifier()).thenReturn(new ProductIdentifier(getSCode(), "1"));
            when(productInstance.getSimpleProductOfferingType()).thenReturn(SimpleProductOfferingType.NetworkNode);
            MergeResult mergeResult = new MergeResult(newArrayList(productInstance), tracker);
            when(futureProductInstanceClient.getMergeResult(productInstance, null, PROVIDE)).thenReturn(mergeResult);
            when(tracker.changeFor(productInstance)).thenReturn(ADD);

            when(productInstance.getProductOffering()).thenReturn(productOffering);
            when(productInstance.getParentOptional()).thenReturn(Optional.<ProductInstance>absent());
            if (optional) {
                when(productOffering.getRules()).thenReturn(structuredRuleList);
                when(ruleValidationExpression.getNonFilterContributingAttributes()).thenReturn(newArrayList(ATTR1));
                when(ruleValidationExpression.getErrorText(anyListOf(ContextualEvaluatorMap.class))).thenReturn(ERROR_TEXT);
            } else {
                when(productOffering.getRules()).thenReturn(Lists.<StructuredRule>newArrayList());
            }

            return productInstance;
        }
    }

    class AttributeDefinition {
        String name = "";
        String value = "";
        boolean optional = true;
        private final Optional<AllowedValuesProvider> allowedValues;

        String getName() {
            return name;
        }

        AttributeDefinition(String name, String value, boolean optional, Optional<AllowedValuesProvider> allowedValues) {
            this.name = name;
            this.value = value;
            this.optional = optional;
            this.allowedValues = allowedValues;
        }

        public Attribute getAttribute() {
            Attribute attribute = mock(Attribute.class);
            when(attribute.getName()).thenReturn(new AttributeName(name));
            when(attribute.isRequiredForOrderPhase(InstanceTreeScenario.PROVIDE)).thenReturn(!optional);
            return attribute;
        }

        public InstanceCharacteristic getCharacteristic() {
            InstanceCharacteristic characteristic = mock(InstanceCharacteristic.class);
            when(characteristic.getAllowedValuesProvider()).thenReturn(allowedValues);
            when(characteristic.getStringValue()).thenReturn(value);
            return characteristic;
        }
    }
}
