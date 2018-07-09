package com.bt.rsqe.customerinventory.service.extenders;

import com.bt.rsqe.client.Pmr;
import com.bt.rsqe.customerinventory.parameter.CustomerId;
import com.bt.rsqe.customerinventory.parameter.ProjectId;
import com.bt.rsqe.customerinventory.parameter.SiteId;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAsset;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetCharacteristicValue;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetOfferingDetail;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetStencilDetail;
import com.bt.rsqe.customerinventory.service.client.fixtures.CIFAssetFixture;
import com.bt.rsqe.customerinventory.service.evaluators.CIFAssetCharacteristicEvaluatorFactory;
import com.bt.rsqe.customerinventory.service.extenders.reservedattributes.StencilReservedAttributesHelper;
import com.bt.rsqe.customerinventory.service.externals.PmrHelper;
import com.bt.rsqe.customerinventory.service.orchestrators.CIFAssetOrchestrator;
import com.bt.rsqe.customerinventory.service.repository.CIFAssetJPARepository;
import com.bt.rsqe.domain.AssetKey;
import com.bt.rsqe.domain.ProductOfferingVersion;
import com.bt.rsqe.domain.StencilCode;
import com.bt.rsqe.domain.StencilId;
import com.bt.rsqe.domain.StencilVersion;
import com.bt.rsqe.domain.bom.parameters.ProductName;
import com.bt.rsqe.domain.bom.parameters.ProductSCode;
import com.bt.rsqe.domain.bom.parameters.QrefRequestUniqueId;
import com.bt.rsqe.domain.product.AccessDetail;
import com.bt.rsqe.domain.product.Attribute;
import com.bt.rsqe.domain.product.AttributeName;
import com.bt.rsqe.domain.product.ProductOffering;
import com.bt.rsqe.domain.product.SimpleProductOfferingType;
import com.bt.rsqe.domain.product.constraints.AttributeValue;
import com.bt.rsqe.domain.product.extensions.RuleAttributeSource;
import com.bt.rsqe.domain.product.parameters.ProductIdentifier;
import com.bt.rsqe.domain.product.parameters.RelationshipName;
import com.bt.rsqe.domain.project.CountryResolver;
import com.bt.rsqe.expressionevaluator.ContextualEvaluatorMap;
import com.bt.rsqe.pmr.client.PmrClient;
import com.bt.rsqe.pmr.dto.JourneyBehaviourDTO;
import com.bt.rsqe.utils.countries.Country;
import com.google.common.base.Optional;
import com.google.common.collect.Ordering;
import org.junit.Before;
import org.junit.Test;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import static com.bt.rsqe.domain.product.SimpleProductOfferingType.*;
import static com.bt.rsqe.domain.product.SimpleProductOfferingType.Package;
import static com.bt.rsqe.productinstancemerge.ChangeType.*;
import static com.google.common.collect.Lists.*;
import static com.google.common.collect.Sets.*;
import static org.hamcrest.core.Is.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class PmrHelperTest {
    public static final String STENCIL_COE = "STENCIL_COE";
    public static final String STENCIL_VERSION = "STENCIL_VERSION";
    public static final String PRODUCT_NAME = "PRODUCT_NAME";
    private PmrClient pmr;
    private Pmr.ProductOfferings offerings;
    private ProductOffering expectedOffering;
    private CIFAsset cifAsset;
    private AccessDetail accessDetail;
    private ProductSCode productSCode;
    private ProductOfferingVersion productOfferingVersion;
    private CountryResolver countryResolver;
    private SiteId siteId = new SiteId("1234");
    private ProjectId projectId = new ProjectId("PROJECT1");
    private CustomerId customerId = new CustomerId("CUSTOMER1");
    private Country country = mock(Country.class);
    private StencilId stencilId = StencilId.versioned(StencilCode.newInstance(STENCIL_COE),
            StencilVersion.newInstance(STENCIL_VERSION),
            ProductName.newInstance(PRODUCT_NAME));
    private CIFAssetCharacteristicEvaluatorFactory evaluatorFactory = mock(CIFAssetCharacteristicEvaluatorFactory.class);
    private StencilReservedAttributesHelper stencilReservedAttributesHelper = mock(StencilReservedAttributesHelper.class);
    private CIFAssetStencilDetail stencilDetail = new CIFAssetStencilDetail(STENCIL_COE, STENCIL_VERSION, PRODUCT_NAME, new ArrayList<CIFAssetStencilDetail>());
    private CIFAssetOrchestrator cifAssetOrchestrator = mock(CIFAssetOrchestrator.class);
    private CIFAssetJPARepository cifAssetJPARepository = mock(CIFAssetJPARepository.class);

    @Before
    public void setUp() throws Exception {
        cifAsset = mock(CIFAsset.class);
        expectedOffering = mock(ProductOffering.class);
        pmr = mock(PmrClient.class);
        countryResolver = mock(CountryResolver.class);
        offerings = mock(Pmr.ProductOfferings.class);

        when(cifAsset.getAssetKey()).thenReturn(new AssetKey("AssetId", 3l));
        when(cifAsset.getProductCode()).thenReturn("ProductCode");
        when(cifAsset.getProductVersion()).thenReturn("V2");
        when(stencilReservedAttributesHelper.getStencilDetail(cifAsset)).thenReturn(null);

        accessDetail = new AccessDetail(
                QrefRequestUniqueId.newInstance(
                        cifAsset.getAssetKey().getAssetId(),
                        Long.toString(cifAsset.getAssetKey().getAssetVersion())).value());
        productSCode = ProductSCode.newInstance(cifAsset.getProductCode());
        productOfferingVersion = ProductOfferingVersion.newInstance(cifAsset.getProductVersion());

        when(pmr.productOffering(productSCode)).thenReturn(offerings);
        when(offerings.withAccessDetail(accessDetail)).thenReturn(offerings);
        when(offerings.forOfferingVersion(productOfferingVersion)).thenReturn(offerings);
        when(offerings.get()).thenReturn(expectedOffering);
    }

    private List<CIFAssetCharacteristicValue> sortByValue(List<CIFAssetCharacteristicValue> values) {
        Ordering<CIFAssetCharacteristicValue> ordering = new Ordering<CIFAssetCharacteristicValue>() {
            @Override
            public int compare(@Nullable CIFAssetCharacteristicValue left, @Nullable CIFAssetCharacteristicValue right) {
                return left.getValue().compareTo(right.getValue());
            }
        } ;
        return ordering.sortedCopy(values) ;
    }

    @Test
    public void shouldGetNonSiteAndStencilBasedProduct() {
        PmrHelper pmrHelper = new PmrHelper(pmr, cifAssetOrchestrator, countryResolver, evaluatorFactory, stencilReservedAttributesHelper, cifAssetJPARepository);
        ProductOffering productOffering = pmrHelper.getProductOffering(cifAsset);

        assertThat(productOffering, is(expectedOffering));
        verify(pmr).productOffering(productSCode);
        verify(offerings).forOfferingVersion(productOfferingVersion);
        verify(offerings).withAccessDetail(accessDetail);
    }

    @Test
    public void shouldGetSiteBasedProduct() {
        when(cifAsset.getSiteId()).thenReturn(siteId.value());
        when(cifAsset.getProjectId()).thenReturn(projectId.value());
        when(cifAsset.getCustomerId()).thenReturn(customerId.value());
        when(offerings.forCountry(country)).thenReturn(offerings);
        when(countryResolver.countryForSite(customerId, projectId, siteId)).thenReturn(country);

        PmrHelper pmrHelper = new PmrHelper(pmr, cifAssetOrchestrator, countryResolver, evaluatorFactory, stencilReservedAttributesHelper, cifAssetJPARepository);
        ProductOffering productOffering = pmrHelper.getProductOffering(cifAsset);

        assertThat(productOffering, is(expectedOffering));
        verify(pmr).productOffering(productSCode);
        verify(offerings).forOfferingVersion(productOfferingVersion);
        verify(offerings).withAccessDetail(accessDetail);
        verify(offerings).forCountry(country);
    }

    @Test
    public void shouldGetStencilBasedProduct() {
        when(offerings.withStencil(stencilId)).thenReturn(offerings);
        when(stencilReservedAttributesHelper.getStencilDetail(cifAsset)).thenReturn(stencilDetail);

        PmrHelper pmrHelper = new PmrHelper(pmr, cifAssetOrchestrator, countryResolver, evaluatorFactory, stencilReservedAttributesHelper, cifAssetJPARepository);
        ProductOffering productOffering = pmrHelper.getProductOffering(cifAsset);

        assertThat(productOffering, is(expectedOffering));
        verify(pmr).productOffering(productSCode);
        verify(offerings).forOfferingVersion(productOfferingVersion);
        verify(offerings).withAccessDetail(accessDetail);
        verify(offerings).withStencil(stencilId);
    }

    @Test
    public void shouldNotGetStencilBasedProductWhenStencilCodeIsNull() {
        when(stencilReservedAttributesHelper.getStencilDetail(cifAsset)).thenReturn(new CIFAssetStencilDetail(null, "", "", null));

        PmrHelper pmrHelper = new PmrHelper(pmr, cifAssetOrchestrator, countryResolver, evaluatorFactory, stencilReservedAttributesHelper, cifAssetJPARepository);
        ProductOffering productOffering = pmrHelper.getProductOffering(cifAsset);

        assertThat(productOffering, is(expectedOffering));
        verify(pmr).productOffering(productSCode);
        verify(offerings).forOfferingVersion(productOfferingVersion);
        verify(offerings).withAccessDetail(accessDetail);
        verify(offerings, times(0)).withStencil(any(StencilId.class));
    }

    @Test
    public void shouldGetSiteAndStencilBasedProduct() {
        when(cifAsset.getSiteId()).thenReturn(siteId.value());
        when(cifAsset.getProjectId()).thenReturn(projectId.value());
        when(cifAsset.getCustomerId()).thenReturn(customerId.value());
        when(offerings.forCountry(country)).thenReturn(offerings);
        when(countryResolver.countryForSite(customerId, projectId, siteId)).thenReturn(country);

        when(offerings.withStencil(stencilId)).thenReturn(offerings);
        when(stencilReservedAttributesHelper.getStencilDetail(cifAsset)).thenReturn(stencilDetail);

        PmrHelper pmrHelper = new PmrHelper(pmr, cifAssetOrchestrator, countryResolver, evaluatorFactory, stencilReservedAttributesHelper, cifAssetJPARepository);
        ProductOffering productOffering = pmrHelper.getProductOffering(cifAsset);

        assertThat(productOffering, is(expectedOffering));
        verify(pmr).productOffering(productSCode);
        verify(offerings).forOfferingVersion(productOfferingVersion);
        verify(offerings).withAccessDetail(accessDetail);
        verify(offerings).withStencil(stencilId);
        verify(offerings).forCountry(country);
    }

    @Test
    public void shouldNotGetAllowedValuesWhenThereAreNoConstraintsOrRules() {
        Attribute attribute = mock(Attribute.class);
        when(attribute.getAllowedValuesWithCaptions()).thenReturn(Optional.<List<AttributeValue>>absent());

        PmrHelper pmrHelper = new PmrHelper(pmr, cifAssetOrchestrator, countryResolver, evaluatorFactory, stencilReservedAttributesHelper, cifAssetJPARepository);
        Optional<List<CIFAssetCharacteristicValue>> allowedValues = pmrHelper.getAllowedValues(cifAsset, attribute);

        assertFalse(allowedValues.isPresent());
    }

    @Test
    public void shouldGetNoAllowedValuesWhenThereAreNoAllowedValuesSetInTheAttribute() {
        Attribute attribute = mock(Attribute.class);
        List<AttributeValue> values = new ArrayList<AttributeValue>();
        when(attribute.getAllowedValuesWithCaptions()).thenReturn(Optional.of(values));

        PmrHelper pmrHelper = new PmrHelper(pmr, cifAssetOrchestrator, countryResolver, evaluatorFactory, stencilReservedAttributesHelper, cifAssetJPARepository);
        Optional<List<CIFAssetCharacteristicValue>> allowedValues = pmrHelper.getAllowedValues(cifAsset, attribute);

        assertThat(allowedValues.get(), is((List<CIFAssetCharacteristicValue>) new ArrayList<CIFAssetCharacteristicValue>()));
    }

    @Test
    public void shouldGetPMFBasedAllowedValuesWhenSourceRulesReturnAbsentResults() {
        Attribute attribute = mock(Attribute.class);
        List<AttributeValue> pmfSourcedValues = newArrayList(AttributeValue.newInstance("value1"),
                AttributeValue.newInstance("value2"));
        when(attribute.getAllowedValuesWithCaptions()).thenReturn(Optional.of(pmfSourcedValues));
        RuleAttributeSource attributeSourceRule1 = mock(RuleAttributeSource.class);
        when(attributeSourceRule1.execute(anyListOf(ContextualEvaluatorMap.class))).thenReturn(Optional.<List<String>>absent());
        RuleAttributeSource attributeSourceRule2 = mock(RuleAttributeSource.class);
        when(attributeSourceRule2.execute(anyListOf(ContextualEvaluatorMap.class))).thenReturn(Optional.<List<String>>absent());
        when(attribute.getAttributeSourceRules()).thenReturn(newArrayList(attributeSourceRule1, attributeSourceRule2));

        PmrHelper pmrHelper = new PmrHelper(pmr, cifAssetOrchestrator, countryResolver, evaluatorFactory, stencilReservedAttributesHelper, cifAssetJPARepository);
        Optional<List<CIFAssetCharacteristicValue>> allowedValues = pmrHelper.getAllowedValues(cifAsset, attribute);

        assertThat(allowedValues.get(), is((List<CIFAssetCharacteristicValue>)
                newArrayList(new CIFAssetCharacteristicValue(pmfSourcedValues.get(0).getAsStringValue()),
                        new CIFAssetCharacteristicValue(pmfSourcedValues.get(1).getAsStringValue()))));
    }

    @Test
    public void shouldGetPmfAllowedValuesWhenSourceRulesReturnEmptyValues() {
        Attribute attribute = mock(Attribute.class);
        List<AttributeValue> pmfSourcedValues = newArrayList(AttributeValue.newInstance("value1"),
                AttributeValue.newInstance("value2"));
        List<String> emptyValues = new ArrayList<String>();
        when(attribute.getAllowedValuesWithCaptions()).thenReturn(Optional.of(pmfSourcedValues));
        RuleAttributeSource attributeSourceRule1 = mock(RuleAttributeSource.class);
        when(attributeSourceRule1.execute(anyListOf(ContextualEvaluatorMap.class))).thenReturn(Optional.of(emptyValues));
        RuleAttributeSource attributeSourceRule2 = mock(RuleAttributeSource.class);
        when(attributeSourceRule2.execute(anyListOf(ContextualEvaluatorMap.class))).thenReturn(Optional.of(emptyValues));
        when(attribute.getAttributeSourceRules()).thenReturn(newArrayList(attributeSourceRule1, attributeSourceRule2));

        PmrHelper pmrHelper = new PmrHelper(pmr, cifAssetOrchestrator, countryResolver, evaluatorFactory, stencilReservedAttributesHelper, cifAssetJPARepository);
        Optional<List<CIFAssetCharacteristicValue>> allowedValues = pmrHelper.getAllowedValues(cifAsset, attribute);

        List<CIFAssetCharacteristicValue> expected = newArrayList(new CIFAssetCharacteristicValue(pmfSourcedValues.get(0).getAsStringValue()),
                new CIFAssetCharacteristicValue(pmfSourcedValues.get(1).getAsStringValue())) ;
        assertThat(allowedValues.get(), is(expected));
    }

    @Test
    public void shouldGetSingleRuleAllowedValuesWhenSingleSourceRulesReturnsValues() {
        Attribute attribute = mock(Attribute.class);
        List<AttributeValue> pmfSourcedValues = newArrayList(AttributeValue.newInstance("value1"),
                AttributeValue.newInstance("value2"));
        List<String> values = newArrayList("value3", "value4");
        when(attribute.getAllowedValuesWithCaptions()).thenReturn(Optional.of(pmfSourcedValues));
        RuleAttributeSource attributeSourceRule1 = mock(RuleAttributeSource.class);
        when(attributeSourceRule1.execute(anyListOf(ContextualEvaluatorMap.class))).thenReturn(Optional.of(values));
        RuleAttributeSource attributeSourceRule2 = mock(RuleAttributeSource.class);
        when(attributeSourceRule2.execute(anyListOf(ContextualEvaluatorMap.class))).thenReturn(Optional.<List<String>>absent());
        when(attribute.getAttributeSourceRules()).thenReturn(newArrayList(attributeSourceRule1, attributeSourceRule2));

        PmrHelper pmrHelper = new PmrHelper(pmr, cifAssetOrchestrator, countryResolver, evaluatorFactory, stencilReservedAttributesHelper, cifAssetJPARepository);
        Optional<List<CIFAssetCharacteristicValue>> allowedValues = pmrHelper.getAllowedValues(cifAsset, attribute);

        final List<CIFAssetCharacteristicValue> expectedValues = newArrayList(new CIFAssetCharacteristicValue(values.get(0)),
                new CIFAssetCharacteristicValue(values.get(1)));
        // TODO would be better to use soem of the newer hamcrest matcheers that allow an any order comparison of lists to be done
        assertThat(sortByValue(allowedValues.get()), is(expectedValues));
    }

    @Test
    public void shouldGetSingleRuleAllowedValuesWhenMaxResponseIsConfiguredThoughRuleReturnsMultipleValues() {
        Attribute attribute = mock(Attribute.class);
        List<AttributeValue> pmfSourcedValues = newArrayList(AttributeValue.newInstance("value1"),
                AttributeValue.newInstance("value2"));

        List<String> values = newArrayList("value3", "value4");
        when(attribute.getAllowedValuesWithCaptions()).thenReturn(Optional.of(pmfSourcedValues));
        RuleAttributeSource attributeSourceRule = mock(RuleAttributeSource.class);
        when(attributeSourceRule.execute(anyListOf(ContextualEvaluatorMap.class))).thenReturn(Optional.of(values));
        when(attributeSourceRule.getMaxResponses()).thenReturn(1);
        when(attribute.getAttributeSourceRules()).thenReturn(newArrayList(attributeSourceRule));

        PmrHelper pmrHelper = new PmrHelper(pmr, cifAssetOrchestrator, countryResolver, evaluatorFactory, stencilReservedAttributesHelper, cifAssetJPARepository);
        Optional<List<CIFAssetCharacteristicValue>> allowedValues = pmrHelper.getAllowedValues(cifAsset, attribute);

        final List<CIFAssetCharacteristicValue> expectedValues = newArrayList(new CIFAssetCharacteristicValue("value3"));
        assertThat(allowedValues.get(), is(expectedValues));
    }

    @Test
    public void shouldReturnUniqueValuesWhenAttributeSourceRulesReturnsDuplicate() {
        Attribute attribute = mock(Attribute.class);
        List<AttributeValue> pmfSourcedValues = newArrayList();
        List<String> values = newArrayList("value3");

        when(attribute.getAllowedValuesWithCaptions()).thenReturn(Optional.of(pmfSourcedValues));
        RuleAttributeSource attributeSourceRule1 = mock(RuleAttributeSource.class);
        when(attributeSourceRule1.execute(anyListOf(ContextualEvaluatorMap.class))).thenReturn(Optional.of(values));
        RuleAttributeSource attributeSourceRule2 = mock(RuleAttributeSource.class);
        when(attributeSourceRule2.execute(anyListOf(ContextualEvaluatorMap.class))).thenReturn(Optional.of(values));
        when(attribute.getAttributeSourceRules()).thenReturn(newArrayList(attributeSourceRule1, attributeSourceRule2));

        PmrHelper pmrHelper = new PmrHelper(pmr, cifAssetOrchestrator, countryResolver, evaluatorFactory, stencilReservedAttributesHelper, cifAssetJPARepository);
        Optional<List<CIFAssetCharacteristicValue>> allowedValues = pmrHelper.getAllowedValues(cifAsset, attribute);

        final List<CIFAssetCharacteristicValue> expectedValues = newArrayList(new CIFAssetCharacteristicValue(values.get(0)));
        assertThat(allowedValues.get(), is(expectedValues));
    }


    @Test
    public void shouldGetOfferingBasedOnProductAndStencilCode() {
        StencilId stencilId = StencilId.latestVersionFor(StencilCode.newInstance("stencil"));
        when(offerings.withStencil(stencilId)).thenReturn(offerings);

        PmrHelper pmrHelper = new PmrHelper(pmr, cifAssetOrchestrator, countryResolver, evaluatorFactory, stencilReservedAttributesHelper, cifAssetJPARepository);
        ProductOffering productOffering = pmrHelper.getProductOffering(productSCode.getValue(), "stencil");

        assertThat(productOffering, is(expectedOffering));
        verify(offerings, times(1)).withStencil(stencilId);
    }

    @Test
    public void shouldGetOfferingBasedOnProduct() {
        PmrHelper pmrHelper = new PmrHelper(pmr, cifAssetOrchestrator, countryResolver, evaluatorFactory, stencilReservedAttributesHelper, cifAssetJPARepository);
        ProductOffering productOffering = pmrHelper.getProductOffering(productSCode.getValue());

        assertThat(productOffering, is(expectedOffering));
    }

    @Test
    public void shouldGetJourneyBehaviour() {
        JourneyBehaviourDTO expectedBehaviour = new JourneyBehaviourDTO(true, true, true, false, false, false, true, false);
        when(pmr.getJourneyBehaviour("DELETE", true, true)).thenReturn(expectedBehaviour);

        PmrHelper pmrHelper = new PmrHelper(pmr, cifAssetOrchestrator, countryResolver, evaluatorFactory, stencilReservedAttributesHelper, cifAssetJPARepository);
        final JourneyBehaviourDTO journeyBehaviour = pmrHelper.getJourneyBehaviour(DELETE, true, true);

        assertThat(journeyBehaviour, is(expectedBehaviour));
    }

    @Test
    public void shouldGetContractAssetsFromPmrForCategoryCode() {
        when(cifAsset.getOfferingDetail()).thenReturn(new CIFAssetOfferingDetail("", "", "categoryName", "", false, false, "", false, true, null));
        when(pmr.getProductCodesForCategory("categoryName", newArrayList(CentralService, Package, BundleProduct))).thenReturn(
                newArrayList(new ProductIdentifier("sCode1", "A1"), new ProductIdentifier("sCode2", "A1")));

        final PmrHelper pmrHelper = new PmrHelper(pmr, cifAssetOrchestrator, countryResolver, evaluatorFactory, stencilReservedAttributesHelper, cifAssetJPARepository);
        List<String> productCodes = pmrHelper.getPackageAndContractProductCodesForCategory(cifAsset);

        assertThat(productCodes, is((List<String>) newArrayList("sCode1", "sCode2")));
    }

    @Test
    public void shouldReturnCreatableCandidates() {
        when(pmr.filterProductsCreatableBy(new ProductIdentifier("productCode1", "productVersion1"),
                RelationshipName.newInstance("aName"),
                newArrayList(new ProductIdentifier("productCode2", "productVersion2")), SimpleProductOfferingType.Package))
                .thenReturn(newArrayList(new ProductIdentifier("productCode2", "productVersion2")));

        final PmrHelper pmrHelper = new PmrHelper(pmr, cifAssetOrchestrator, countryResolver, evaluatorFactory, stencilReservedAttributesHelper, cifAssetJPARepository);
        List<ProductIdentifier> productCodes = pmrHelper.creatableCandidates(new ProductIdentifier("productCode1", "productVersion1"),
                RelationshipName.newInstance("aName"),
                newHashSet(new ProductIdentifier("productCode2", "productVersion2")), SimpleProductOfferingType.Package);

        assertThat((ArrayList<ProductIdentifier>) productCodes, is(newArrayList(new ProductIdentifier("productCode2", "productVersion2"))));
    }

    @Test
    public void shouldReturnIsFilterSatisfiedFlagAsTrueWhenNoAttributeSourceRulesAvailable() {
        //Given
        Attribute attribute = mock(Attribute.class);
        when(expectedOffering.getAttribute(new AttributeName("A"))).thenReturn(attribute);
        when(attribute.hasAttributeSourceRule()).thenReturn(false);

        //When
        final PmrHelper pmrHelper = new PmrHelper(pmr, cifAssetOrchestrator, countryResolver, evaluatorFactory, stencilReservedAttributesHelper, cifAssetJPARepository);
        CIFAsset asset = CIFAssetFixture.aCIFAsset().withID("AssetId").withVersion(3L).withProductIdentifier("ProductCode", "V2").withCharacteristic("A", "aValue").withSiteId(null).build();
        assertThat(pmrHelper.isRuleFilterSatisfied(asset, "A"), is(true));
    }

    @Test
    public void shouldReturnIsFilterSatisfiedFlagAsTrueWhenAnyAttributeSourceRuleFilterSatisfied() {
        //Given
        Attribute attribute = mock(Attribute.class);
        RuleAttributeSource ruleAttributeSource = mock(RuleAttributeSource.class);
        when(expectedOffering.getAttribute(new AttributeName("A"))).thenReturn(attribute);
        when(attribute.hasAttributeSourceRule()).thenReturn(true);
        when(attribute.getAttributeSourceRules()).thenReturn(newArrayList(ruleAttributeSource));
        when(ruleAttributeSource.isFilterSatisfied(anyList())).thenReturn(true);

        //When
        final PmrHelper pmrHelper = new PmrHelper(pmr, cifAssetOrchestrator, countryResolver, evaluatorFactory, stencilReservedAttributesHelper, cifAssetJPARepository);
        CIFAsset asset = CIFAssetFixture.aCIFAsset().withID("AssetId").withVersion(3L).withProductIdentifier("ProductCode", "V2").withCharacteristic("A", "aValue").withSiteId(null).build();
        assertThat(pmrHelper.isRuleFilterSatisfied(asset, "A"), is(true));
    }

    @Test
    public void shouldReturnIsFilterSatisfiedFlagAsFalseWhenNoneOfAttributeSourceRuleFilterSatisfied() {
        //Given
        Attribute attribute = mock(Attribute.class);
        RuleAttributeSource ruleAttributeSource = mock(RuleAttributeSource.class);
        RuleAttributeSource anotherRuleAttributeSource = mock(RuleAttributeSource.class);
        when(expectedOffering.getAttribute(new AttributeName("A"))).thenReturn(attribute);
        when(attribute.hasAttributeSourceRule()).thenReturn(true);
        when(attribute.getAttributeSourceRules()).thenReturn(newArrayList(ruleAttributeSource, anotherRuleAttributeSource));
        when(ruleAttributeSource.isFilterSatisfied(anyList())).thenReturn(false);
        when(anotherRuleAttributeSource.isFilterSatisfied(anyList())).thenReturn(false);

        //When
        final PmrHelper pmrHelper = new PmrHelper(pmr, cifAssetOrchestrator, countryResolver, evaluatorFactory, stencilReservedAttributesHelper, cifAssetJPARepository);
        CIFAsset asset = CIFAssetFixture.aCIFAsset().withID("AssetId").withVersion(3L).withProductIdentifier("ProductCode", "V2").withCharacteristic("A", "aValue").withSiteId(null).build();
        assertThat(pmrHelper.isRuleFilterSatisfied(asset, "A"), is(false));
    }
}