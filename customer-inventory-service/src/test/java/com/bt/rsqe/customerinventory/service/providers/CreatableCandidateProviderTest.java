package com.bt.rsqe.customerinventory.service.providers;

import com.bt.rsqe.customerinventory.service.client.domain.CIFAsset;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetCreatableCandidate;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetRelationshipCardinality;
import com.bt.rsqe.customerinventory.service.evaluators.CIFAssetCharacteristicEvaluatorFactory;
import com.bt.rsqe.customerinventory.service.externals.PmrHelper;
import com.bt.rsqe.customerinventory.service.orchestrators.CIFAssetOrchestrator;
import com.bt.rsqe.domain.StencilCode;
import com.bt.rsqe.domain.StencilId;
import com.bt.rsqe.domain.bom.parameters.ProductName;
import com.bt.rsqe.domain.product.ProductOffering;
import com.bt.rsqe.domain.product.SimpleProductOfferingType;
import com.bt.rsqe.domain.product.extensions.FilterRuleExecutionPoint;
import com.bt.rsqe.domain.product.extensions.RuleFilter;
import com.bt.rsqe.domain.product.extensions.ValidationErrorType;
import com.bt.rsqe.domain.product.parameters.ProductIdentifier;
import com.bt.rsqe.domain.product.parameters.RelatedProductIdentifier;
import com.bt.rsqe.domain.product.parameters.RelationshipName;
import com.bt.rsqe.domain.product.parameters.RelationshipType;
import com.bt.rsqe.domain.product.parameters.ResolvesTo;
import com.bt.rsqe.domain.product.parameters.SalesRelationship;
import com.bt.rsqe.expressionevaluator.ContextualEvaluatorMap;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static com.bt.rsqe.customerinventory.service.client.fixtures.CIFAssetFixture.*;
import static com.bt.rsqe.domain.product.parameters.RelationshipType.*;
import static com.bt.rsqe.domain.product.parameters.ResolvesTo.*;
import static com.google.common.collect.Lists.*;
import static com.google.common.collect.Sets.*;
import static org.apache.commons.lang.StringUtils.*;
import static org.hamcrest.core.Is.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class CreatableCandidateProviderTest {
    private static final String RELATIONSHIP_NAME = "relationshipName";
    private final PmrHelper pmrHelper = mock(PmrHelper.class);
    private final RuleFilter filterRule = mock(RuleFilter.class);
    private final CIFAsset baseAsset = aCIFAsset().withRule(filterRule).build();
    private final CIFAssetOrchestrator cifAssetOrchestrator = mock(CIFAssetOrchestrator.class);
    private final CIFAssetCharacteristicEvaluatorFactory evaluatorFactory = mock(CIFAssetCharacteristicEvaluatorFactory.class);

    @Test
    public void shouldReturnNoCreatableCandidatesWhenNoRelationships() {
        final List<SalesRelationship> salesRelationships = new ArrayList<SalesRelationship>();
        final CreatableCandidateProvider creatableCandidateProvider = new CreatableCandidateProvider(pmrHelper, cifAssetOrchestrator, evaluatorFactory);
        when(pmrHelper.creatableCandidates(any(ProductIdentifier.class), any(RelationshipName.class), any(Set.class), any(SimpleProductOfferingType.class))).thenReturn(Collections.<ProductIdentifier>emptyList());

        final List<CIFAssetCreatableCandidate> creatableCandidates = creatableCandidateProvider.getCreatableCandidates(baseAsset,
                new ProductIdentifier("aProductCode", "A.1"),
                salesRelationships, SimpleProductOfferingType.Package);

        final List<CIFAssetCreatableCandidate> expectedCandidates = new ArrayList<CIFAssetCreatableCandidate>();
        assertThat(creatableCandidates, is(expectedCandidates));
    }

    @Test
    public void shouldReturnCreatableCandidatesForEachRelationship() {
        ProductOffering mockOffering1 = mock(ProductOffering.class);
        when(mockOffering1.getVisibleInSummaryText(true)).thenReturn("description1");
        when(mockOffering1.requiresSiteSelection()).thenReturn(true);
        when(pmrHelper.getProductOffering("productCode1", "stencilCode1")).thenReturn(mockOffering1);

        ProductOffering mockOffering2 = mock(ProductOffering.class);
        when(mockOffering2.getVisibleInSummaryText(true)).thenReturn("description2");
        when(mockOffering2.requiresSiteSelection()).thenReturn(false);
        when(pmrHelper.getProductOffering("productCode2", "stencilCode2")).thenReturn(mockOffering2);

        SalesRelationship salesRelationship1 = mockSalesRelationship("productCode1", "productVersion1", "stencilCode1", "stencilName1", false, Child, true, 0, Any);
        SalesRelationship salesRelationship2 = mockSalesRelationship("productCode2", "productVersion2", "stencilCode2", "stencilName2", false, Child, true, 0, Any);
        final List<SalesRelationship> salesRelationships = newArrayList(salesRelationship1, salesRelationship2);
        final CreatableCandidateProvider creatableCandidateProvider = new CreatableCandidateProvider(pmrHelper, cifAssetOrchestrator, evaluatorFactory);
        when(pmrHelper.creatableCandidates(any(ProductIdentifier.class), any(RelationshipName.class), any(Set.class), any(SimpleProductOfferingType.class))).thenReturn(Collections.<ProductIdentifier>emptyList());


        final List<CIFAssetCreatableCandidate> creatableCandidates = creatableCandidateProvider.getCreatableCandidates(baseAsset, new ProductIdentifier("aProductCode", "A.1"), salesRelationships, SimpleProductOfferingType.Package);

        final List<CIFAssetCreatableCandidate> expectedCandidates = newArrayList(new CIFAssetCreatableCandidate("productCode1",
                        "productVersion1",
                        "stencilCode1",
                        "stencilName1",
                        "description1",
                        true),
                new CIFAssetCreatableCandidate("productCode2",
                        "productVersion2",
                        "stencilCode2",
                        "stencilName2",
                        "description2",
                        false));
        assertThat(creatableCandidates, is(expectedCandidates));
    }

    @Test
    public void shouldReturnAutoCreatableCandidatesWhenRelationshipHasDefaultCardinalitySpecifiedAndAssetYetToBeAdded() {
        ProductOffering mockOffering1 = mock(ProductOffering.class);
        when(mockOffering1.getVisibleInSummaryText(true)).thenReturn("description1");
        when(mockOffering1.requiresSiteSelection()).thenReturn(true);
        when(pmrHelper.getProductOffering("productCode1", "stencilCode1")).thenReturn(mockOffering1);

        SalesRelationship salesRelationship1 = mockSalesRelationship("productCode1", "productVersion1", "stencilCode1", "stencilName1", false, Child, true, 0, Any);
        final List<SalesRelationship> salesRelationships = newArrayList(salesRelationship1);
        final CreatableCandidateProvider creatableCandidateProvider = new CreatableCandidateProvider(pmrHelper, cifAssetOrchestrator, evaluatorFactory);
        when(pmrHelper.creatableCandidates(any(ProductIdentifier.class), any(RelationshipName.class), any(Set.class), any(SimpleProductOfferingType.class))).thenReturn(Collections.<ProductIdentifier>emptyList());

        CIFAsset baseAsset = aCIFAsset().withRule(filterRule).withRelationships(0).build();


        final List<CIFAssetCreatableCandidate> creatableCandidates = creatableCandidateProvider.getAutoCreatableCandidates(baseAsset,
                new ProductIdentifier("aProductCode", "A.1"),
                salesRelationships, 1, SimpleProductOfferingType.Package);

        final List<CIFAssetCreatableCandidate> expectedCandidates = newArrayList(new CIFAssetCreatableCandidate("productCode1",
                "productVersion1",
                "stencilCode1",
                "stencilName1",
                "description1",
                true));
        assertThat(creatableCandidates, is(expectedCandidates));
    }

    @Test
    public void shouldReturnAutoCreatableCandidatesWhenRelationshipIsRelatedToAndResolvesToAsNewOnly() {
        ProductOffering mockOffering1 = mock(ProductOffering.class);
        when(mockOffering1.getVisibleInSummaryText(true)).thenReturn("description1");
        when(mockOffering1.requiresSiteSelection()).thenReturn(true);
        when(pmrHelper.getProductOffering("productCode1", "stencilCode1")).thenReturn(mockOffering1);

        SalesRelationship salesRelationship1 = mockSalesRelationship("productCode1", "productVersion1", "stencilCode1", "stencilName1", false, RelatedTo, true, 1, NewOnly);
        final List<SalesRelationship> salesRelationships = newArrayList(salesRelationship1);
        final CreatableCandidateProvider creatableCandidateProvider = new CreatableCandidateProvider(pmrHelper, cifAssetOrchestrator, evaluatorFactory);
        when(pmrHelper.creatableCandidates(any(ProductIdentifier.class), any(RelationshipName.class), any(Set.class), any(SimpleProductOfferingType.class))).thenReturn(newArrayList(new ProductIdentifier("productCode1", "A.1")));

        CIFAsset baseAsset = aCIFAsset().withRule(filterRule).withRelationships(0).build();


        final List<CIFAssetCreatableCandidate> creatableCandidates = creatableCandidateProvider.getAutoCreatableCandidates(baseAsset,
                new ProductIdentifier("aProductCode", "A.1"),
                salesRelationships, 1, SimpleProductOfferingType.Package);

        final List<CIFAssetCreatableCandidate> expectedCandidates = newArrayList(new CIFAssetCreatableCandidate("productCode1",
                "productVersion1",
                "stencilCode1",
                "stencilName1",
                "description1",
                true));
        assertThat(creatableCandidates, is(expectedCandidates));
    }

    @Test
    public void shouldNotReturnAutoCreatableCandidatesWhenRelationshipHasNoDefaultCardinalitySpecified() {
        ProductOffering mockOffering1 = mock(ProductOffering.class);
        when(mockOffering1.getVisibleInSummaryText(true)).thenReturn("description1");
        when(mockOffering1.requiresSiteSelection()).thenReturn(true);
        when(pmrHelper.getProductOffering("productCode1", "stencilCode1")).thenReturn(mockOffering1);

        SalesRelationship salesRelationship1 = mockSalesRelationship("productCode1", "productVersion1", "stencilCode1", "stencilName1", false, Child, true, 0, Any);
        final List<SalesRelationship> salesRelationships = newArrayList(salesRelationship1);
        final CreatableCandidateProvider creatableCandidateProvider = new CreatableCandidateProvider(pmrHelper, cifAssetOrchestrator, evaluatorFactory);
        when(pmrHelper.creatableCandidates(any(ProductIdentifier.class), any(RelationshipName.class), any(Set.class), any(SimpleProductOfferingType.class))).thenReturn(Collections.<ProductIdentifier>emptyList());

        CIFAsset baseAsset = aCIFAsset().withRule(filterRule).withRelationships(0).build();


        final List<CIFAssetCreatableCandidate> creatableCandidates = creatableCandidateProvider.getAutoCreatableCandidates(baseAsset,
                new ProductIdentifier("aProductCode", "A.1"),
                salesRelationships, CIFAssetRelationshipCardinality.NO_CARDINALITY.getCardinality(), SimpleProductOfferingType.Package);
        assertThat(creatableCandidates.isEmpty(), is(true));
    }

    @Test
    public void shouldNotReturnAutoCreatableCandidatesWhenRelationshipHasDefaultCardinalitySpecifiedAndAssetAlreadyBeAdded() {
        ProductOffering mockOffering1 = mock(ProductOffering.class);
        when(mockOffering1.getVisibleInSummaryText(true)).thenReturn("description1");
        when(mockOffering1.requiresSiteSelection()).thenReturn(true);
        when(pmrHelper.getProductOffering("productCode1", "stencilCode1")).thenReturn(mockOffering1);

        SalesRelationship salesRelationship1 = mockSalesRelationship("productCode1", "productVersion1", "stencilCode1", "stencilName1", false, Child, true, 1, Any);
        final List<SalesRelationship> salesRelationships = newArrayList(salesRelationship1);
        final CreatableCandidateProvider creatableCandidateProvider = new CreatableCandidateProvider(pmrHelper, cifAssetOrchestrator, evaluatorFactory);
        when(pmrHelper.creatableCandidates(any(ProductIdentifier.class), any(RelationshipName.class), any(Set.class), any(SimpleProductOfferingType.class))).thenReturn(Collections.<ProductIdentifier>emptyList());


        CIFAsset relatedAsset = aCIFAsset().build();
        CIFAsset baseAsset = aCIFAsset().withRule(filterRule).withRelationship(relatedAsset, RELATIONSHIP_NAME, RelatedTo).build();

        final List<CIFAssetCreatableCandidate> creatableCandidates = creatableCandidateProvider.getAutoCreatableCandidates(baseAsset,
                new ProductIdentifier("aProductCode", "A.1"),
                salesRelationships, CIFAssetRelationshipCardinality.NO_CARDINALITY.getCardinality(), SimpleProductOfferingType.Package);

        assertThat(creatableCandidates.isEmpty(), is(true));
    }


    @Test
    public void shouldNotAddCandidatesForFeatureSpecs() {
        ProductOffering mockOffering1 = mock(ProductOffering.class);
        when(mockOffering1.getVisibleInSummaryText(true)).thenReturn("description1");
        when(mockOffering1.requiresSiteSelection()).thenReturn(true);
        when(pmrHelper.getProductOffering("productCode1", "stencilCode1")).thenReturn(mockOffering1);

        ProductOffering mockOffering2 = mock(ProductOffering.class);
        when(mockOffering2.getVisibleInSummaryText(true)).thenReturn("description2");
        when(mockOffering2.requiresSiteSelection()).thenReturn(false);
        when(pmrHelper.getProductOffering("productCode2", "stencilCode2")).thenReturn(mockOffering2);

        SalesRelationship salesRelationship1 = mockSalesRelationship("productCode1", "productVersion1", "stencilCode1", "stencilName1", false, Child, true, 0, Any);
        SalesRelationship salesRelationship2 = mockSalesRelationship("productCode2", "productVersion2", "stencilCode2", "stencilName2", true, Child, true, 0, Any);
        final List<SalesRelationship> salesRelationships = newArrayList(salesRelationship1, salesRelationship2);

        when(pmrHelper.creatableCandidates(any(ProductIdentifier.class), any(RelationshipName.class), any(Set.class), any(SimpleProductOfferingType.class))).thenReturn(Collections.<ProductIdentifier>emptyList());

        final CreatableCandidateProvider creatableCandidateProvider = new CreatableCandidateProvider(pmrHelper, cifAssetOrchestrator, evaluatorFactory);
        final List<CIFAssetCreatableCandidate> creatableCandidates = creatableCandidateProvider.getCreatableCandidates(baseAsset, new ProductIdentifier("aProductCode", "A.1"), salesRelationships, SimpleProductOfferingType.Package);

        final List<CIFAssetCreatableCandidate> expectedCandidates = newArrayList(new CIFAssetCreatableCandidate("productCode1",
                "productVersion1",
                "stencilCode1",
                "stencilName1",
                "description1",
                true));

        assertThat(creatableCandidates, is(expectedCandidates));
    }

    @Test
    public void shouldNotAddCandidatesForRelatedToRelationship() {
        ProductOffering mockOffering1 = mock(ProductOffering.class);
        when(mockOffering1.getVisibleInSummaryText(true)).thenReturn("description1");
        when(mockOffering1.requiresSiteSelection()).thenReturn(true);
        when(pmrHelper.getProductOffering("productCode1", "stencilCode1")).thenReturn(mockOffering1);

        ProductOffering mockOffering2 = mock(ProductOffering.class);
        when(mockOffering2.getVisibleInSummaryText(true)).thenReturn("description2");
        when(mockOffering2.requiresSiteSelection()).thenReturn(false);
        when(pmrHelper.getProductOffering("productCode2", "stencilCode2")).thenReturn(mockOffering2);
        when(pmrHelper.creatableCandidates(any(ProductIdentifier.class), any(RelationshipName.class), any(Set.class), any(SimpleProductOfferingType.class))).thenReturn(Collections.<ProductIdentifier>emptyList());


        SalesRelationship salesRelationship1 = mockSalesRelationship("productCode1", "productVersion1", "stencilCode1", "stencilName1", false, Child, true, 0, Any);
        SalesRelationship salesRelationship2 = mockSalesRelationship("productCode2", "productVersion2", "stencilCode2", "stencilName2", true, RelatedTo, true, 0, Any);
        final List<SalesRelationship> salesRelationships = newArrayList(salesRelationship1, salesRelationship2);

        final CreatableCandidateProvider creatableCandidateProvider = new CreatableCandidateProvider(pmrHelper, cifAssetOrchestrator, evaluatorFactory);
        final List<CIFAssetCreatableCandidate> creatableCandidates = creatableCandidateProvider.getCreatableCandidates(baseAsset, new ProductIdentifier("aProductCode", "A.1"), salesRelationships, SimpleProductOfferingType.Package);

        final List<CIFAssetCreatableCandidate> expectedCandidates = newArrayList(new CIFAssetCreatableCandidate("productCode1",
                "productVersion1",
                "stencilCode1",
                "stencilName1",
                "description1",
                true));

        assertThat(creatableCandidates, is(expectedCandidates));
    }

    @Test
    public void shouldAddCandidatesForRelatedToRelationshipWhenItsACreatableRelation() {
        ProductOffering mockOffering1 = mock(ProductOffering.class);
        when(mockOffering1.getVisibleInSummaryText(true)).thenReturn("description1");
        when(mockOffering1.requiresSiteSelection()).thenReturn(true);
        when(pmrHelper.getProductOffering("productCode1", "stencilCode1")).thenReturn(mockOffering1);

        ProductOffering mockOffering2 = mock(ProductOffering.class);
        when(mockOffering2.getVisibleInSummaryText(true)).thenReturn("description2");
        when(mockOffering2.requiresSiteSelection()).thenReturn(false);
        when(pmrHelper.getProductOffering("productCode2", null)).thenReturn(mockOffering2);
        when(pmrHelper.creatableCandidates(new ProductIdentifier("aProductCode", "A.1"),
                RelationshipName.newInstance(RELATIONSHIP_NAME),
                newHashSet(new ProductIdentifier("productCode2", "productVersion2")), SimpleProductOfferingType.Package))
                .thenReturn(newArrayList(new ProductIdentifier("productCode2", "productVersion2")));


        SalesRelationship salesRelationship1 = mockSalesRelationship("productCode1", "productVersion1", "stencilCode1", "stencilName1", false, Child, true, 0, Any);
        SalesRelationship salesRelationship2 = mockSalesRelationship("productCode2", "productVersion2", null, null, false, RelatedTo, false, 0, Any);
        final List<SalesRelationship> salesRelationships = newArrayList(salesRelationship1, salesRelationship2);

        final CreatableCandidateProvider creatableCandidateProvider = new CreatableCandidateProvider(pmrHelper, cifAssetOrchestrator, evaluatorFactory);
        final List<CIFAssetCreatableCandidate> creatableCandidates = creatableCandidateProvider.getCreatableCandidates(baseAsset, new ProductIdentifier("aProductCode", "A.1"), salesRelationships, SimpleProductOfferingType.Package);

        final List<CIFAssetCreatableCandidate> expectedCandidates = newArrayList(new CIFAssetCreatableCandidate("productCode1",
                        "productVersion1",
                        "stencilCode1",
                        "stencilName1",
                        "description1",
                        true),
                new CIFAssetCreatableCandidate("productCode2",
                        "productVersion2",
                        null,
                        null,
                        "description2",
                        false));

        assertThat(creatableCandidates, is(expectedCandidates));
    }

    @Test
    public void shouldNotAddCandidatesForRelatedToRelationshipWhenItsACreatableRelationButIsVisible() {
        ProductOffering mockOffering2 = mock(ProductOffering.class);
        when(mockOffering2.getVisibleInSummaryText(true)).thenReturn("description2");
        when(mockOffering2.requiresSiteSelection()).thenReturn(false);
        when(pmrHelper.getProductOffering("productCode2", "stencilCode2")).thenReturn(mockOffering2);
        when(pmrHelper.creatableCandidates(new ProductIdentifier("aProductCode", "A.1"),
                RelationshipName.newInstance(RELATIONSHIP_NAME),
                newHashSet(new ProductIdentifier("productCode2", "productVersion2")), SimpleProductOfferingType.Package))
                .thenReturn(newArrayList(new ProductIdentifier("productCode2", "productVersion2")));


        SalesRelationship salesRelationship2 = mockSalesRelationship("productCode2", "productVersion2", "stencilCode2", "stencilName2", false, RelatedTo, true, 0, Any);
        final List<SalesRelationship> salesRelationships = newArrayList(salesRelationship2);

        final CreatableCandidateProvider creatableCandidateProvider = new CreatableCandidateProvider(pmrHelper, cifAssetOrchestrator, evaluatorFactory);
        final List<CIFAssetCreatableCandidate> creatableCandidates = creatableCandidateProvider.getCreatableCandidates(baseAsset, new ProductIdentifier("aProductCode", "A.1"), salesRelationships, SimpleProductOfferingType.Package);

        assertThat(creatableCandidates.isEmpty(), is(true));
    }

    @Test
    public void shouldNotAddCandidatesForRelatedToRelationshipWhenItsACreatableRelationAlsoStecilSet() {
        ProductOffering mockOffering2 = mock(ProductOffering.class);
        when(mockOffering2.getVisibleInSummaryText(true)).thenReturn("description2");
        when(mockOffering2.requiresSiteSelection()).thenReturn(false);
        when(pmrHelper.getProductOffering("productCode2", "stencilCode2")).thenReturn(mockOffering2);
        when(pmrHelper.creatableCandidates(new ProductIdentifier("aProductCode", "A.1"),
                RelationshipName.newInstance(RELATIONSHIP_NAME),
                newHashSet(new ProductIdentifier("productCode2", "productVersion2")), SimpleProductOfferingType.Package))
                .thenReturn(newArrayList(new ProductIdentifier("productCode2", "productVersion2")));


        SalesRelationship salesRelationship2 = mockSalesRelationship("productCode2", "productVersion2", "stencilCode2", "stencilName2", false, RelatedTo, false, 0, Any);
        final List<SalesRelationship> salesRelationships = newArrayList(salesRelationship2);

        final CreatableCandidateProvider creatableCandidateProvider = new CreatableCandidateProvider(pmrHelper, cifAssetOrchestrator, evaluatorFactory);
        final List<CIFAssetCreatableCandidate> creatableCandidates = creatableCandidateProvider.getCreatableCandidates(baseAsset, new ProductIdentifier("aProductCode", "A.1"), salesRelationships, SimpleProductOfferingType.Package);

        assertThat(creatableCandidates.isEmpty(), is(true));
    }


    @Test
    public void shouldNotReturnCandidatesFilteredOutByRules() {
        when(filterRule.isFilterRule()).thenReturn(true);
        when(filterRule.getExecutionPoint()).thenReturn(FilterRuleExecutionPoint.FilterCandidateProducts);
        when(filterRule.getRelationshipName()).thenReturn(RELATIONSHIP_NAME);
        when(filterRule.getSatisfaction(anyListOf(ContextualEvaluatorMap.class), eq(RELATIONSHIP_NAME)))
                .thenReturn(ValidationErrorType.Satisfied)
                .thenReturn(ValidationErrorType.Error);

        ProductOffering mockOffering1 = mock(ProductOffering.class);
        when(mockOffering1.getVisibleInSummaryText(true)).thenReturn("description1");
        when(mockOffering1.requiresSiteSelection()).thenReturn(true);
        when(pmrHelper.getProductOffering("productCode1", "stencilCode1")).thenReturn(mockOffering1);

        ProductOffering mockOffering2 = mock(ProductOffering.class);
        when(mockOffering2.getVisibleInSummaryText(true)).thenReturn("description2");
        when(mockOffering2.requiresSiteSelection()).thenReturn(false);
        when(pmrHelper.getProductOffering("productCode2", "stencilCode2")).thenReturn(mockOffering2);
        when(pmrHelper.creatableCandidates(any(ProductIdentifier.class), any(RelationshipName.class), any(Set.class), any(SimpleProductOfferingType.class))).thenReturn(Collections.<ProductIdentifier>emptyList());


        SalesRelationship salesRelationship1 = mockSalesRelationship("productCode1", "productVersion1", "stencilCode1", "stencilName1", false, Child, true, 0, Any);
        SalesRelationship salesRelationship2 = mockSalesRelationship("productCode2", "productVersion2", "stencilCode2", "stencilName2", false, Child, true, 0, Any);
        final List<SalesRelationship> salesRelationships = newArrayList(salesRelationship1, salesRelationship2);
        final CreatableCandidateProvider creatableCandidateProvider = new CreatableCandidateProvider(pmrHelper, cifAssetOrchestrator, evaluatorFactory);

        final List<CIFAssetCreatableCandidate> creatableCandidates = creatableCandidateProvider.getCreatableCandidates(baseAsset, new ProductIdentifier("aProductCode", "A.1"), salesRelationships, SimpleProductOfferingType.Package);

        final List<CIFAssetCreatableCandidate> expectedCandidates = newArrayList(new CIFAssetCreatableCandidate("productCode1",
                "productVersion1",
                "stencilCode1",
                "stencilName1",
                "description1",
                true));
        assertThat(creatableCandidates, is(expectedCandidates));
    }

    private SalesRelationship mockSalesRelationship(String productCode, String productVersion, String stencilCode,
                                                    String stencilName, Boolean featureSpec, RelationshipType type, boolean isViewable, int defaultCardinality, ResolvesTo resolvesTo) {
        SalesRelationship mockRelationship = mock(SalesRelationship.class);
        RelatedProductIdentifier mockRelatedIdentifier = mock(RelatedProductIdentifier.class);
        ProductIdentifier mockIdentifier = mock(ProductIdentifier.class);
        StencilId mockStencilId = mock(StencilId.class);

        when(mockIdentifier.getProductId()).thenReturn(productCode);
        when(mockIdentifier.getVersionNumber()).thenReturn(productVersion);

        when(mockStencilId.getCCode()).thenReturn(StencilCode.newInstance(stencilCode));
        when(mockStencilId.getProductName()).thenReturn(ProductName.newInstance(stencilName));

        when(mockRelatedIdentifier.getStencilId()).thenReturn(mockStencilId);
        when(mockRelatedIdentifier.getProductIdentifier()).thenReturn(mockIdentifier);

        when(mockRelationship.getRelatedProductIdentifier()).thenReturn(mockRelatedIdentifier);
        when(mockRelationship.isTargetAFeatureSpecification()).thenReturn(featureSpec);
        when(mockRelationship.getType()).thenReturn(type);
        when(mockRelationship.getRelationshipName()).thenReturn(RelationshipName.newInstance(RELATIONSHIP_NAME));
        when(mockRelationship.isRelatedToRelationship()).thenReturn(RelatedTo.equals(type));
        when(mockRelationship.hasStencilSet()).thenReturn(isNotEmpty(stencilCode));

        when(mockRelationship.getLinkedIdentifiers()).thenReturn(newHashSet(new ProductIdentifier(productCode, productVersion)));
        when(mockRelationship.isViewable()).thenReturn(isViewable);
        when(mockRelationship.getResolvesToValue()).thenReturn(resolvesTo);


        when(mockRelationship.getDefault()).thenReturn(defaultCardinality);
        return mockRelationship;
    }
}