package com.bt.rsqe.customerinventory.service.extenders;

import com.bt.rsqe.customerinventory.service.client.domain.CIFAsset;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetChoosableCandidate;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetCreatableCandidate;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetOfferingRelationshipDetail;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetRelationshipCardinality;
import com.bt.rsqe.customerinventory.service.client.domain.UnloadedExtensionAccessException;
import com.bt.rsqe.customerinventory.service.client.resource.CIFAssetExtension;
import com.bt.rsqe.customerinventory.service.evaluators.CIFAssetCharacteristicEvaluatorFactory;
import com.bt.rsqe.customerinventory.service.externals.PmrHelper;
import com.bt.rsqe.customerinventory.service.orchestrators.CIFAssetOrchestrator;
import com.bt.rsqe.customerinventory.service.providers.ChoosableCandidateProvider;
import com.bt.rsqe.customerinventory.service.providers.CreatableCandidateProvider;
import com.bt.rsqe.domain.StencilId;
import com.bt.rsqe.domain.product.ProductOffering;
import com.bt.rsqe.domain.product.SimpleProductOfferingType;
import com.bt.rsqe.domain.product.extensions.Expression;
import com.bt.rsqe.domain.product.extensions.RuleDefaulting;
import com.bt.rsqe.domain.product.extensions.StructuredRule;
import com.bt.rsqe.domain.product.parameters.ProductIdentifier;
import com.bt.rsqe.domain.product.parameters.RelatedProductIdentifier;
import com.bt.rsqe.domain.product.parameters.RelationshipGroup;
import com.bt.rsqe.domain.product.parameters.RelationshipGroupName;
import com.bt.rsqe.domain.product.parameters.RelationshipName;
import com.bt.rsqe.domain.product.parameters.RelationshipType;
import com.bt.rsqe.domain.product.parameters.ResolvesTo;
import com.bt.rsqe.domain.product.parameters.SalesRelationship;
import com.bt.rsqe.util.TestWithRules;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.bt.rsqe.customerinventory.service.client.domain.CIFAssetRelationshipCardinality.*;
import static com.bt.rsqe.customerinventory.service.client.fixtures.CIFAssetFixture.*;
import static com.bt.rsqe.customerinventory.service.client.resource.CIFAssetExtension.*;
import static com.bt.rsqe.domain.product.extensions.ExpressionExpectedResultType.Integer;
import static com.bt.rsqe.domain.product.parameters.RelationshipType.*;
import static com.google.common.collect.Lists.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.*;

public class SalesRelationshipExtenderTest extends TestWithRules {
    public static final String ROOT_PRODUCT_ID = "prodId";
    private final CreatableCandidateProvider creatableCandidateProvider = mock(CreatableCandidateProvider.class);
    private final CIFAssetOrchestrator cifAssetOrchestrator = mock(CIFAssetOrchestrator.class);
    private final CIFAssetCharacteristicEvaluatorFactory cifAssetCharacteristicEvaluatorFactory = mock(CIFAssetCharacteristicEvaluatorFactory.class);
    private CIFAssetRelationshipCardinality minCardinality = NO_CARDINALITY;
    private CIFAssetRelationshipCardinality maxCardinality = NO_CARDINALITY;
    private CIFAssetRelationshipCardinality defaultCardinality = NO_CARDINALITY;
    private PmrHelper pmrHelper = mock(PmrHelper.class);
    private final ProductOffering productOffering = mock(ProductOffering.class);
    private final ProductOffering productOfferingNotUpdatable = mock(ProductOffering.class);
    private final ChoosableCandidateProvider choosableCandidateProvider = mock(ChoosableCandidateProvider.class);
    private final SalesRelationshipExtender salesRelationshipExtender = new SalesRelationshipExtender(creatableCandidateProvider,
            choosableCandidateProvider,
            cifAssetOrchestrator,
            cifAssetCharacteristicEvaluatorFactory, pmrHelper);

    @Before
    public void setUp() throws Exception {
        when(pmrHelper.getProductOffering(anyString())).thenReturn(productOfferingNotUpdatable);
        when(pmrHelper.getProductOffering(ROOT_PRODUCT_ID)).thenReturn(productOffering);
    }

    @Test
    public void shouldNotExtendAssetWithSalesRelationshipDetailWhenNotRequested() {
        expectException(UnloadedExtensionAccessException.class, "Cannot get sales relationships for this asset. Customer inventory service should be called with the ProductOfferingRelationshipDetail flag.");

        CIFAsset cifAsset = aCIFAsset().build();
        ProductOffering productOffering = mock(ProductOffering.class);
        salesRelationshipExtender.extend(new ArrayList<CIFAssetExtension>(), cifAsset, productOffering);

        cifAsset.getRelationshipDefinitions();
    }

    @Test
    public void shouldExtendAssetWithSalesRelationshipDetailWhenRequested() {
        CIFAsset cifAsset = aCIFAsset().build();
        ProductOffering productOffering = mock(ProductOffering.class);
        List<String> linkedIdentifiers = newArrayList("linked1", "linked2");
        SalesRelationship salesRelationship = mockSalesRelationship("relationshipName", ROOT_PRODUCT_ID, "groupName", linkedIdentifiers, false, Child, ResolvesTo.Any);
        when(productOffering.getSalesRelationships()).thenReturn(newArrayList(salesRelationship));

        salesRelationshipExtender.extend(newArrayList(ProductOfferingRelationshipDetail), cifAsset, productOffering);

        final List<CIFAssetOfferingRelationshipDetail> salesRelationships = cifAsset.getRelationshipDefinitions();
        assertThat(salesRelationships.size(), is(1));

        assertThat(salesRelationships.get(0), is(new CIFAssetOfferingRelationshipDetail(minCardinality, maxCardinality, defaultCardinality, "relationshipName", Child, "stencilId", ROOT_PRODUCT_ID,
                "groupName", new ArrayList<String>(), false, ResolvesTo.Any, false)));
    }

    @Test
    public void shouldAddLinkedIdentifiersForFeatureSpec() {
        CIFAsset cifAsset = aCIFAsset().build();
        ProductOffering productOffering = mock(ProductOffering.class);
        List<String> linkedIdentifiers = newArrayList("linked1", "linked2");
        SalesRelationship salesRelationship = mockSalesRelationship("relationshipName", ROOT_PRODUCT_ID, "groupName", linkedIdentifiers, true, Child, ResolvesTo.Any);
        when(productOffering.getSalesRelationships()).thenReturn(newArrayList(salesRelationship));

        salesRelationshipExtender.extend(newArrayList(ProductOfferingRelationshipDetail), cifAsset, productOffering);

        final List<CIFAssetOfferingRelationshipDetail> salesRelationships = cifAsset.getRelationshipDefinitions();
        assertThat(salesRelationships.size(), is(1));
        assertThat(salesRelationships.get(0), is(new CIFAssetOfferingRelationshipDetail(minCardinality, maxCardinality, defaultCardinality, "relationshipName", Child, "stencilId", ROOT_PRODUCT_ID,
                "groupName", linkedIdentifiers, false, ResolvesTo.Any, false)));
    }

    @Test
    public void shouldGroupSalesRelationshipsByName() {
        CIFAsset cifAsset = aCIFAsset().build();
        ProductOffering productOffering = mock(ProductOffering.class);
        List<String> linkedIdentifiers1 = newArrayList("linked1", "linked2");
        List<String> linkedIdentifiers2 = newArrayList("linked3", "linked4");
        SalesRelationship salesRelationship1 = mockSalesRelationship("relationshipName1", "prodId1", "groupName", linkedIdentifiers1, false, Child, ResolvesTo.Any);
        SalesRelationship salesRelationship2 = mockSalesRelationship("relationshipName1", "prodId1", "groupName", linkedIdentifiers1, false, Child, ResolvesTo.Any);
        SalesRelationship salesRelationship3 = mockSalesRelationship("relationshipName2", "prodId2", "groupName", linkedIdentifiers1, true, RelatedTo, ResolvesTo.Any);
        SalesRelationship salesRelationship4 = mockSalesRelationship("relationshipName2", "prodId2", "groupName", linkedIdentifiers2, true, RelatedTo, ResolvesTo.Any);
        when(productOffering.getSalesRelationships()).thenReturn(newArrayList(salesRelationship1, salesRelationship2,
                salesRelationship3, salesRelationship4));

        salesRelationshipExtender.extend(newArrayList(ProductOfferingRelationshipDetail), cifAsset, productOffering);

        final List<CIFAssetOfferingRelationshipDetail> salesRelationships = cifAsset.getRelationshipDefinitions();
        assertThat(salesRelationships.size(), is(2));
        assertThat(salesRelationships.get(0), is(new CIFAssetOfferingRelationshipDetail(minCardinality, maxCardinality, defaultCardinality, "relationshipName1", Child, "stencilId", "prodId1",
                "groupName", new ArrayList<String>(), false, ResolvesTo.Any, false)));
        List<String> allLinkedIdentifiers = newArrayList(linkedIdentifiers1);
        allLinkedIdentifiers.addAll(linkedIdentifiers2);
        assertThat(salesRelationships.get(1), is(new CIFAssetOfferingRelationshipDetail(minCardinality, maxCardinality, defaultCardinality, "relationshipName2", RelatedTo, "stencilId", "prodId2",
                "groupName", allLinkedIdentifiers, false, ResolvesTo.Any, false)));
    }

    @Test
    public void shouldLoadCreatableCandidatesWhenRequested() {
        CIFAsset cifAsset = aCIFAsset().build();
        ProductOffering productOffering = mock(ProductOffering.class);
        SalesRelationship salesRelationship1 = mockSalesRelationship("relationshipName1", "prodId1", "groupName", new ArrayList<String>(), false, Child, ResolvesTo.Any);
        when(productOffering.getSalesRelationships()).thenReturn(newArrayList(salesRelationship1));
        when(creatableCandidateProvider.getCreatableCandidates(eq(cifAsset), any(ProductIdentifier.class), eq(newArrayList(salesRelationship1)), any(SimpleProductOfferingType.class))).thenReturn(newArrayList(new CIFAssetCreatableCandidate()));

        salesRelationshipExtender.extend(newArrayList(RelationshipCreatableCandidates), cifAsset, productOffering);

        assertThat(cifAsset.getRelationshipDefinitions().get(0).getCreatableCandidates().get(0), is(new CIFAssetCreatableCandidate()));
    }

    @Test
    public void shouldLoadAutoCreatableCandidatesWhenRequested() {
        CIFAsset cifAsset = aCIFAsset().build();
        ProductOffering productOffering = mock(ProductOffering.class);
        SalesRelationship salesRelationship1 = mockSalesRelationship("relationshipName1", "prodId1", "groupName", new ArrayList<String>(), false, Child, ResolvesTo.Any);
        when(productOffering.getSalesRelationships()).thenReturn(newArrayList(salesRelationship1));
        when(creatableCandidateProvider.getAutoCreatableCandidates(eq(cifAsset), any(ProductIdentifier.class), eq(newArrayList(salesRelationship1)), any(Integer.class), any(SimpleProductOfferingType.class))).thenReturn(newArrayList(new CIFAssetCreatableCandidate()));

        salesRelationshipExtender.extend(newArrayList(AutoCreatableCandidates), cifAsset, productOffering);

        assertThat(cifAsset.getRelationshipDefinitions().get(0).getAutoCreatableCandidates().get(0), is(new CIFAssetCreatableCandidate()));
    }

    @Test
    public void shouldNotLoadAutoCreatableCandidatesWhenMultipleRelationsAvailableForANameAndNoneOfThemAreDefaultingRule() {
        StructuredRule structuredRule = mock(StructuredRule.class);
        List<StructuredRule> rules = newArrayList(structuredRule);
        CIFAsset cifAsset = aCIFAsset().build();
        cifAsset.loadProductRules(rules);
        when(structuredRule.isDefaultingRule()).thenReturn(false);
        ProductOffering productOffering = mock(ProductOffering.class);
        SalesRelationship salesRelationship1 = mockSalesRelationship("relationshipName1", "prodId1", "groupName", new ArrayList<String>(), false, Child, ResolvesTo.Any);
        SalesRelationship salesRelationship2 = mockSalesRelationship("relationshipName1", "prodId2", "groupName", new ArrayList<String>(), false, Child, ResolvesTo.Any);
        when(productOffering.getSalesRelationships()).thenReturn(newArrayList(salesRelationship1, salesRelationship2));
        when(creatableCandidateProvider.getAutoCreatableCandidates(eq(cifAsset), any(ProductIdentifier.class), eq(newArrayList(salesRelationship1)), any(Integer.class), any(SimpleProductOfferingType.class))).thenReturn(newArrayList(new CIFAssetCreatableCandidate()));

        salesRelationshipExtender.extend(newArrayList(AutoCreatableCandidates), cifAsset, productOffering);

        assertThat(cifAsset.getRelationshipDefinitions().get(0).getAutoCreatableCandidates().isEmpty(), is(true));
    }

    @Test
    public void shouldLoadAutoCreatableCandidatesWhenMultipleRelationsAvailableWithDefaultingRuleSpecified() {
        RuleDefaulting structuredRule = mock(RuleDefaulting.class);
        List<StructuredRule> rules = newArrayList((StructuredRule) structuredRule);
        CIFAsset cifAsset = aCIFAsset().build();
        cifAsset.loadProductRules(rules);
        when(structuredRule.getRelationshipName()).thenReturn("relationshipName1");
        when(structuredRule.isDefaultingRule()).thenReturn(true);
        ProductOffering productOffering = mock(ProductOffering.class);
        SalesRelationship salesRelationship1 = mockSalesRelationship("relationshipName1", "prodId1", "groupName", new ArrayList<String>(), false, Child, ResolvesTo.Any);
        SalesRelationship salesRelationship2 = mockSalesRelationship("relationshipName1", "prodId2", "groupName", new ArrayList<String>(), false, Child, ResolvesTo.Any);
        when(productOffering.getSalesRelationships()).thenReturn(newArrayList(salesRelationship1, salesRelationship2));
        when(creatableCandidateProvider.getAutoCreatableCandidates(any(CIFAsset.class), any(ProductIdentifier.class), anyList(), any(Integer.class), any(SimpleProductOfferingType.class))).thenReturn(newArrayList(new CIFAssetCreatableCandidate()));

        salesRelationshipExtender.extend(newArrayList(AutoCreatableCandidates), cifAsset, productOffering);

        assertThat(cifAsset.getRelationshipDefinitions().get(0).getAutoCreatableCandidates().isEmpty(), is(false));
    }

    @Test
    public void shouldLoadAutoCreatableCandidatesAfterFilteringRelationshipsWithDefaultCardinality() {
        RuleDefaulting structuredRule = mock(RuleDefaulting.class);
        List<StructuredRule> rules = newArrayList((StructuredRule) structuredRule);
        CIFAsset cifAsset = aCIFAsset().build();
        cifAsset.loadProductRules(rules);
        when(structuredRule.getRelationshipName()).thenReturn("relationshipName1");
        when(structuredRule.isDefaultingRule()).thenReturn(false);
        ProductOffering productOffering = mock(ProductOffering.class);
        SalesRelationship salesRelationship1 = mockSalesRelationship("relationshipName1", "prodId1", "groupName", new ArrayList<String>(), false, Child, 0, 0, 0, null, null, null, ResolvesTo.Any );
        SalesRelationship salesRelationship2 = mockSalesRelationship("relationshipName1", "prodId2", "groupName", new ArrayList<String>(), false, Child, 0, 0, 3, null, null, null, ResolvesTo.Any );
        when(productOffering.getSalesRelationships()).thenReturn(newArrayList(salesRelationship1, salesRelationship2));
        when(creatableCandidateProvider.getAutoCreatableCandidates(any(CIFAsset.class), any(ProductIdentifier.class), anyList(), eq(3), any(SimpleProductOfferingType.class))).thenReturn(newArrayList(new CIFAssetCreatableCandidate()));

        salesRelationshipExtender.extend(newArrayList(AutoCreatableCandidates), cifAsset, productOffering);

        assertThat(cifAsset.getRelationshipDefinitions().get(0).getAutoCreatableCandidates().get(0), is(new CIFAssetCreatableCandidate()));
    }

    @Test
    public void shouldLoadChoosableCandidatesWhenRequested() {
        CIFAsset cifAsset = aCIFAsset().build();
        ProductOffering productOffering = mock(ProductOffering.class);
        SalesRelationship salesRelationship1 = mockSalesRelationship("relationshipName1", "prodId1", "groupName", new ArrayList<String>(), false, Child, ResolvesTo.Any);
        when(productOffering.getSalesRelationships()).thenReturn(newArrayList(salesRelationship1));
        when(choosableCandidateProvider.getChoosableCandidates(cifAsset, newArrayList(salesRelationship1))).thenReturn(newArrayList(new CIFAssetChoosableCandidate()));

        salesRelationshipExtender.extend(newArrayList(RelationshipChoosableCandidates), cifAsset, productOffering);

        assertThat(cifAsset.getRelationshipDefinitions().get(0).getChoosableCandidates().get(0), is(new CIFAssetChoosableCandidate()));
    }

    @Test
    public void shouldLoadAutoChoosableCandidatesWhenRequested() {
        CIFAsset cifAsset = aCIFAsset().build();
        ProductOffering productOffering = mock(ProductOffering.class);
        SalesRelationship salesRelationship1 = mockSalesRelationship("relationshipName1", "prodId1", "groupName", new ArrayList<String>(), false, Child, ResolvesTo.Any);
        when(productOffering.getSalesRelationships()).thenReturn(newArrayList(salesRelationship1));
        when(choosableCandidateProvider.getAutoChoosableCandidates(cifAsset, newArrayList(salesRelationship1), false, CIFAssetRelationshipCardinality.NO_CARDINALITY)).thenReturn(newArrayList(new CIFAssetChoosableCandidate()));

        salesRelationshipExtender.extend(newArrayList(AutoChoosableCandidates), cifAsset, productOffering);

        assertThat(cifAsset.getRelationshipDefinitions().get(0).getAutoChoosableCandidates().get(0), is(new CIFAssetChoosableCandidate()));
    }

    @Test
    public void shouldGetCardinalityDetailsWithNoExpression() {
        CIFAsset cifAsset = aCIFAsset().build();
        ProductOffering productOffering = mock(ProductOffering.class);
        SalesRelationship salesRelationship1 = mockSalesRelationship("relationshipName1", "prodId1", "groupName",
                new ArrayList<String>(), false, Child, 1, 2, 3, null, null, null, ResolvesTo.Any);
        when(productOffering.getSalesRelationships()).thenReturn(newArrayList(salesRelationship1));

        salesRelationshipExtender.extend(newArrayList(RelationshipCardinality), cifAsset, productOffering);

        CIFAssetOfferingRelationshipDetail cifAssetOfferingRelationshipDetail = cifAsset.getRelationshipDefinitions().get(0);
        assertThat(cifAssetOfferingRelationshipDetail.getMinCardinality(), is(new CIFAssetRelationshipCardinality(1)));
        assertThat(cifAssetOfferingRelationshipDetail.getMaxCardinality(), is(new CIFAssetRelationshipCardinality(2)));
        assertThat(cifAssetOfferingRelationshipDetail.getDefaultCardinality(), is(new CIFAssetRelationshipCardinality(3)));
    }

    @Test
    public void shouldGetCardinalityFromExpression() {
        CIFAsset cifAsset = aCIFAsset().build();
        ProductOffering productOffering = mock(ProductOffering.class);
        SalesRelationship salesRelationship1 = mockSalesRelationship("relationshipName1", "prodId1", "groupName",
                new ArrayList<String>(), false, Child, 1, 2, 3,
                new Expression("3", Integer),
                new Expression("4", Integer),
                new Expression("5", Integer), ResolvesTo.Any);
        when(productOffering.getSalesRelationships()).thenReturn(newArrayList(salesRelationship1));

        salesRelationshipExtender.extend(newArrayList(RelationshipCardinality), cifAsset, productOffering);

        CIFAssetOfferingRelationshipDetail cifAssetOfferingRelationshipDetail = cifAsset.getRelationshipDefinitions().get(0);
        assertThat(cifAssetOfferingRelationshipDetail.getMinCardinality(), is(new CIFAssetRelationshipCardinality(3)));
        assertThat(cifAssetOfferingRelationshipDetail.getMaxCardinality(), is(new CIFAssetRelationshipCardinality(4)));
        assertThat(cifAssetOfferingRelationshipDetail.getDefaultCardinality(), is(new CIFAssetRelationshipCardinality(5)));
    }

    @Test
    public void shouldGetNoCardinalityWhenExpressionReturnsEmpty() {
        CIFAsset cifAsset = aCIFAsset().build();
        ProductOffering productOffering = mock(ProductOffering.class);
        SalesRelationship salesRelationship1 = mockSalesRelationship("relationshipName1", "prodId1", "groupName",
                new ArrayList<String>(), false, Child, 1, 2, 3,
                new Expression("''", Integer),
                new Expression("''", Integer),
                new Expression("''", Integer), ResolvesTo.Any);
        when(productOffering.getSalesRelationships()).thenReturn(newArrayList(salesRelationship1));

        salesRelationshipExtender.extend(newArrayList(RelationshipCardinality), cifAsset, productOffering);

        CIFAssetOfferingRelationshipDetail cifAssetOfferingRelationshipDetail = cifAsset.getRelationshipDefinitions().get(0);
        assertThat(cifAssetOfferingRelationshipDetail.getMinCardinality(), is(NO_CARDINALITY));
        assertThat(cifAssetOfferingRelationshipDetail.getMaxCardinality(), is(NO_CARDINALITY));
        assertThat(cifAssetOfferingRelationshipDetail.getDefaultCardinality(), is(NO_CARDINALITY));
    }

    @Test
    public void shouldGetNoCardinalityWhenExpressionHasSyntaxError() {
        CIFAsset cifAsset = aCIFAsset().build();
        ProductOffering productOffering = mock(ProductOffering.class);
        SalesRelationship salesRelationship1 = mockSalesRelationship("relationshipName1", "prodId1", "groupName",
                new ArrayList<String>(), false, Child, 1, 2, 3,
                new Expression("2134-=3321", Integer),
                new Expression("2134-=3321", Integer),
                new Expression("2134-=3321", Integer), ResolvesTo.Any);
        when(productOffering.getSalesRelationships()).thenReturn(newArrayList(salesRelationship1));

        salesRelationshipExtender.extend(newArrayList(RelationshipCardinality), cifAsset, productOffering);

        CIFAssetOfferingRelationshipDetail cifAssetOfferingRelationshipDetail = cifAsset.getRelationshipDefinitions().get(0);
        assertThat(cifAssetOfferingRelationshipDetail.getMinCardinality(), is(NO_CARDINALITY));
        assertThat(cifAssetOfferingRelationshipDetail.getMaxCardinality(), is(NO_CARDINALITY));
        assertThat(cifAssetOfferingRelationshipDetail.getDefaultCardinality(), is(NO_CARDINALITY));
    }

    @Test
    public void shouldNotHaveUpdatableStencilWhenMaximumCardinalityIsNotOne() {
        CIFAsset cifAsset = aCIFAsset().build();
        when(productOfferingNotUpdatable.isStencilUpdatable()).thenReturn(true);
        SalesRelationship salesRelationship1 = mockSalesRelationship("relationshipName1", "prodId1", "groupName",
                new ArrayList<String>(), false, Child, 1, 2,
                3, null, null, new Expression("5", Integer), ResolvesTo.Any);
        when(productOffering.getSalesRelationships()).thenReturn(newArrayList(salesRelationship1));

        salesRelationshipExtender.extend(newArrayList(RelationshipCardinality), cifAsset, productOffering);

        CIFAssetOfferingRelationshipDetail cifAssetOfferingRelationshipDetail = cifAsset.getRelationshipDefinitions().get(0);
        assertThat(cifAssetOfferingRelationshipDetail.isStencilUpdatable(), is(false));
    }

    @Test
    public void shouldNotHaveUpdatableStencilForChildRelationship() {
        CIFAsset cifAsset = aCIFAsset().build();
        when(productOfferingNotUpdatable.isStencilUpdatable()).thenReturn(true);
        SalesRelationship salesRelationship1 = mockSalesRelationship("relationshipName1", "prodId1", "groupName",
                new ArrayList<String>(), false, RelatedTo, 1, 1,
                3, null, null, new Expression("5", Integer), ResolvesTo.Any);
        when(productOffering.getSalesRelationships()).thenReturn(newArrayList(salesRelationship1));

        salesRelationshipExtender.extend(newArrayList(RelationshipCardinality), cifAsset, productOffering);

        CIFAssetOfferingRelationshipDetail cifAssetOfferingRelationshipDetail = cifAsset.getRelationshipDefinitions().get(0);
        assertThat(cifAssetOfferingRelationshipDetail.isStencilUpdatable(), is(false));
    }

    @Test
    public void shouldGetResolvesToNewOnlyFromRelationship() {
        CIFAsset cifAsset = aCIFAsset().build();
        when(productOfferingNotUpdatable.isStencilUpdatable()).thenReturn(true);
        SalesRelationship salesRelationship1 = mockSalesRelationship("relationshipName1", "prodId1", "groupName",
                new ArrayList<String>(), false, RelatedTo, 1, 1,
                3, null, null, new Expression("5", Integer), ResolvesTo.NewOnly);
        when(productOffering.getSalesRelationships()).thenReturn(newArrayList(salesRelationship1));

        salesRelationshipExtender.extend(newArrayList(RelationshipCardinality), cifAsset, productOffering);

        CIFAssetOfferingRelationshipDetail cifAssetOfferingRelationshipDetail = cifAsset.getRelationshipDefinitions().get(0);
        assertThat(cifAssetOfferingRelationshipDetail.getResolvesTo(), is(ResolvesTo.NewOnly));
    }

    private SalesRelationship mockSalesRelationship(String relationshipName, String rootProductId, String groupName,
                                                    List<String> linkedIdentifiers, boolean isFeatureSpec,
                                                    RelationshipType relationshipType, ResolvesTo resolvesTo) {
        return mockSalesRelationship(relationshipName, rootProductId, groupName,
                linkedIdentifiers, isFeatureSpec, relationshipType,
                0, 0, 3, null, null, new Expression("5", Integer), resolvesTo);
    }

    private SalesRelationship mockSalesRelationship(String relationshipName, String rootProductId, String groupName,
                                                    List<String> linkedIdentifiers, boolean isFeatureSpec,
                                                    RelationshipType relationshipType,
                                                    int minCardinality, int maxCardinality, int defaultCardinality,
                                                    Expression minCardinalityExpression,
                                                    Expression maxCardinalityExpression,
                                                    Expression defaultExpression, ResolvesTo resolvesTo) {
        SalesRelationship salesRelationship = mock(SalesRelationship.class);
        final ProductIdentifier productIdentifier = new ProductIdentifier(rootProductId, "v1");
        when(salesRelationship.getRootProductIdentifier()).thenReturn(productIdentifier);
        when(salesRelationship.getRelatedProductIdentifier()).thenReturn(new RelatedProductIdentifier(productIdentifier, StencilId.latestVersionFor("stencilId")));
        when(salesRelationship.getGroup()).thenReturn(RelationshipGroup.newInstance(RelationshipGroupName.newInstance(groupName)));
        Set<ProductIdentifier> linkedIdentifierSet = new HashSet<ProductIdentifier>();
        for (String linkedIdentifier : linkedIdentifiers) {
            linkedIdentifierSet.add(new ProductIdentifier(linkedIdentifier, "v1"));
        }
        when(salesRelationship.getLinkedIdentifiers()).thenReturn(linkedIdentifierSet);
        when(salesRelationship.isTargetAFeatureSpecification()).thenReturn(isFeatureSpec);
        when(salesRelationship.getRelationshipName()).thenReturn(RelationshipName.newInstance(relationshipName));
        when(salesRelationship.getType()).thenReturn(relationshipType);
        when(salesRelationship.getMinimum()).thenReturn(minCardinality);
        when(salesRelationship.getMaximum()).thenReturn(maxCardinality);
        when(salesRelationship.getDefault()).thenReturn(defaultCardinality);
        when(salesRelationship.getMinimumExpression()).thenReturn(minCardinalityExpression);
        when(salesRelationship.getMaximumExpression()).thenReturn(maxCardinalityExpression);
        when(salesRelationship.getDefaultExpression()).thenReturn(defaultExpression);
        when(salesRelationship.getResolvesToValue()).thenReturn(resolvesTo);
        return salesRelationship;
    }
}