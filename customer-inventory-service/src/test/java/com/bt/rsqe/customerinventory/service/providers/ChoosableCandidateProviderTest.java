package com.bt.rsqe.customerinventory.service.providers;

import com.bt.rsqe.customerinventory.service.client.domain.CIFAsset;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetChoosableCandidate;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetKey;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetOfferingDetail;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetQuoteOptionItemDetail;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetRelationshipCardinality;
import com.bt.rsqe.customerinventory.service.client.resource.CIFAssetExtension;
import com.bt.rsqe.customerinventory.service.evaluators.CIFAssetCharacteristicEvaluatorFactory;
import com.bt.rsqe.customerinventory.service.extenders.reservedattributes.StencilReservedAttributesHelper;
import com.bt.rsqe.customerinventory.service.orchestrators.CIFAssetOrchestrator;
import com.bt.rsqe.domain.AssetKey;
import com.bt.rsqe.domain.product.extensions.FilterRuleExecutionPoint;
import com.bt.rsqe.domain.product.extensions.RuleFilter;
import com.bt.rsqe.domain.product.extensions.ValidationErrorType;
import com.bt.rsqe.domain.product.parameters.ConsumerCardinality;
import com.bt.rsqe.domain.product.parameters.ProductCategoryCode;
import com.bt.rsqe.domain.product.parameters.ProductIdentifier;
import com.bt.rsqe.domain.product.parameters.RelationshipName;
import com.bt.rsqe.domain.product.parameters.RelationshipType;
import com.bt.rsqe.domain.product.parameters.SalesRelationship;
import com.bt.rsqe.enums.AssetVersionStatus;
import com.bt.rsqe.expressionevaluator.ContextualEvaluatorMap;
import com.google.common.base.Optional;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.List;

import static com.bt.rsqe.customerinventory.service.client.fixtures.CIFAssetFixture.aCIFAsset;
import static com.bt.rsqe.customerinventory.service.client.resource.CIFAssetExtension.*;
import static com.bt.rsqe.enums.AssetVersionStatus.*;
import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class ChoosableCandidateProviderTest {
    private static final boolean MIGRATION = true;
    private static final boolean NON_MIGRATION = false;
    private static final boolean AVAILABLE = true;
    private static final boolean UNAVAILABLE = false;
    public static final String RELATIONSHIP_NAME = "relationshipName";
    private final ArrayList<AssetVersionStatus> consumerStatus = newArrayList(CUSTOMER_ACCEPTED, PROVISIONING, IN_SERVICE);
    private final CIFAssetOrchestrator cifAssetOrchestrator = mock(CIFAssetOrchestrator.class);
    private final CIFAssetCharacteristicEvaluatorFactory evaluatorFactory = mock(CIFAssetCharacteristicEvaluatorFactory.class);
    private final StencilReservedAttributesHelper stencilReservedAttributesHelper = new StencilReservedAttributesHelper();
    private final ChoosableCandidateProvider provider = new ChoosableCandidateProvider(cifAssetOrchestrator, evaluatorFactory, stencilReservedAttributesHelper);
    private final RuleFilter filterRule = mock(RuleFilter.class);
    private CIFAsset baseAsset = aCIFAsset().withQuoteOptionId("QuoteOption1")
                                            .withRule(filterRule).build();

    @Test
    public void shouldGetNoCandidatesWhenNoRelationshipsPassedIn() {
        final List<CIFAssetChoosableCandidate> candidates = provider.getChoosableCandidates(baseAsset, new ArrayList<SalesRelationship>());

        assertThat(candidates, is(((List<CIFAssetChoosableCandidate>) new ArrayList<CIFAssetChoosableCandidate>())));
    }

    @Test
    public void shouldGetOnlyInServiceAndProvisioningAndSameQuoteAssets() {
        mockExtendAsset(newArrayList(ProductOfferingDetail, QuoteOptionItemDetail, ProductRules), baseAsset);
        SalesRelationship mockRelationship = mockRelationship(new ConsumerCardinality(0, 0, false), 1, false);

        AssetKey inServiceAssetKey = new AssetKey("inService", 1);
        AssetKey inServiceLatestAssetKey = new AssetKey("inService", 2);
        AssetKey provisioningAssetKey = new AssetKey("provisioning", 1);
        AssetKey sameQuoteAssetKey = new AssetKey("sameQuote", 1);
        AssetKey unavailableAssetKey = new AssetKey("unavailable", 1);
        final CIFAsset inServiceAsset = aCIFAsset().withID(inServiceAssetKey.getAssetId()).withVersion(inServiceAssetKey.getAssetVersion())
                                                   .withQuoteOptionId("QuoteOption3")
                                                   .withAssetVersionStatus(AssetVersionStatus.IN_SERVICE)
                                                   .with(new CIFAssetQuoteOptionItemDetail(null, 0, MIGRATION, false, null, null, true,
                                                                                           null, null, null, "quoteOptionName", true, ProductCategoryCode.NIL, null, false)).build();
        final CIFAsset provisioningAsset = aCIFAsset().withID(provisioningAssetKey.getAssetId()).withVersion(provisioningAssetKey.getAssetVersion())
                                                      .withQuoteOptionId("QuoteOption4")
                                                      .withAssetVersionStatus(AssetVersionStatus.PROVISIONING)
                                                      .with(new CIFAssetQuoteOptionItemDetail(null, 0, MIGRATION, false, null, null, true,
                                                                                              null, null, null, "quoteOptionName", true, ProductCategoryCode.NIL, null, false)).build();
        final CIFAsset sameQuoteAsset = aCIFAsset().withID(sameQuoteAssetKey.getAssetId()).withVersion(sameQuoteAssetKey.getAssetVersion())
                                                   .withQuoteOptionId("QuoteOption1")
                                                   .withAssetVersionStatus(AssetVersionStatus.DRAFT)
                                                   .with(new CIFAssetQuoteOptionItemDetail(null, 0, MIGRATION, false, null, null, true,
                                                                                           null, null, null, "quoteOptionName", true, ProductCategoryCode.NIL, null, false)).build();
        final CIFAsset inServiceLatestAsset = aCIFAsset().withID(inServiceLatestAssetKey.getAssetId()).withVersion(inServiceLatestAssetKey.getAssetVersion())
                                                         .withQuoteOptionId("QuoteOption3")
                                                         .withAssetVersionStatus(AssetVersionStatus.IN_SERVICE)
                                                         .with(new CIFAssetQuoteOptionItemDetail(null, 0, MIGRATION, false, null, null, true,
                                                                                                 null, null, null, "quoteOptionName", true, ProductCategoryCode.NIL, null, false)).build();
        final CIFAsset unavailableAsset = aCIFAsset().withID(unavailableAssetKey.getAssetId()).withVersion(unavailableAssetKey.getAssetVersion())
                                                     .withQuoteOptionId("QuoteOption3")
                                                     .withAssetVersionStatus(AssetVersionStatus.IN_SERVICE)
                                                     .with(new CIFAssetQuoteOptionItemDetail(null, 0, NON_MIGRATION, false, null, null, true,
                                                                                             null, null, null, "quoteOptionName", true, ProductCategoryCode.NIL, null, false))
                                                     .with(new CIFAssetOfferingDetail("", "", "",  "", false, false, "", false, UNAVAILABLE, null))
                                                     .build();

        when(cifAssetOrchestrator.eligibleExistingCandidates(baseAsset.getCustomerId(), baseAsset.getContractId(),
                mockRelationship.getProductIdentifier().getProductId(),
                newArrayList(ProductOfferingDetail, ProductRules, QuoteOptionItemDetail), baseAsset.getQuoteOptionId(), baseAsset.getSiteId(), false))
            .thenReturn(newArrayList(inServiceAsset, inServiceLatestAsset, provisioningAsset, sameQuoteAsset, unavailableAsset));

        final List<CIFAssetChoosableCandidate> candidates = provider.getChoosableCandidates(baseAsset, newArrayList(mockRelationship));

        CIFAssetChoosableCandidate expectedCandidate1 = new CIFAssetChoosableCandidate(inServiceLatestAssetKey, "PRODUCT_CODE", "");
        CIFAssetChoosableCandidate expectedCandidate2 = new CIFAssetChoosableCandidate(provisioningAssetKey, "PRODUCT_CODE", "");
        CIFAssetChoosableCandidate expectedCandidate3 = new CIFAssetChoosableCandidate(sameQuoteAssetKey, "PRODUCT_CODE", "");
        final List<CIFAssetChoosableCandidate> cifAssetChoosableCandidates = newArrayList(expectedCandidate1, expectedCandidate2,
                                                                                          expectedCandidate3);
        assertThat(candidates, is(cifAssetChoosableCandidates));
    }

    @Test
    public void shouldFilterAssetsWhichAlreadyFillTheirConsumerCardinality() {
        mockExtendAsset(newArrayList(ProductOfferingDetail, QuoteOptionItemDetail, ProductRules), baseAsset);
        SalesRelationship mockRelationship = mockRelationship(new ConsumerCardinality(0, 2, true), 1, false);

        AssetKey sameQuoteAssetKey = new AssetKey("sameQuote", 1);
        AssetKey inServiceAssetKey = new AssetKey("inService", 1);
        final CIFAsset sameQuoteAsset = aCIFAsset().withID(sameQuoteAssetKey.getAssetId()).withVersion(sameQuoteAssetKey.getAssetVersion())
                                                   .withQuoteOptionId("QuoteOption1")
                                                   .withAssetVersionStatus(AssetVersionStatus.DRAFT)
                                                   .with(new CIFAssetQuoteOptionItemDetail(null, 0, MIGRATION, false, null, null, true,
                                                                                           null, null, null, "quoteOptionName", true, ProductCategoryCode.NIL, null, false)).build();
        final CIFAsset inServiceAsset = aCIFAsset().withID(inServiceAssetKey.getAssetId()).withVersion(inServiceAssetKey.getAssetVersion())
                                                   .withQuoteOptionId("QuoteOption3")
                                                   .withAssetVersionStatus(AssetVersionStatus.IN_SERVICE)
                                                   .with(new CIFAssetQuoteOptionItemDetail(null, 0, MIGRATION, false, null, null, true,
                                                                                           null, null, null, "quoteOptionName", true, ProductCategoryCode.NIL, null, false)).build();
        when(cifAssetOrchestrator.eligibleExistingCandidates(baseAsset.getCustomerId(), baseAsset.getContractId(),
                                            mockRelationship.getProductIdentifier().getProductId(),
                                            newArrayList(ProductOfferingDetail, ProductRules, QuoteOptionItemDetail), baseAsset.getQuoteOptionId(), baseAsset.getSiteId(), false))
            .thenReturn(newArrayList(sameQuoteAsset, inServiceAsset));
        // Returning two owners for same quote asset so the consumer cardinality is met
        when(cifAssetOrchestrator.getOwnerAssets(new CIFAssetKey(sameQuoteAssetKey), consumerStatus, Optional.<String>absent(), Optional.<String>absent())).thenReturn(newArrayList(aCIFAsset().build(),
                                                                                                                              aCIFAsset().build()));
        // Returning one owner for in service asset so consumer cardinality is not met
        when(cifAssetOrchestrator.getOwnerAssets(new CIFAssetKey(inServiceAssetKey), consumerStatus, Optional.<String>absent(), Optional.<String>absent())).thenReturn(newArrayList(aCIFAsset().build()));

        final List<CIFAssetChoosableCandidate> candidates = provider.getChoosableCandidates(baseAsset, newArrayList(mockRelationship));

        CIFAssetChoosableCandidate expectedCandidate1 = new CIFAssetChoosableCandidate(inServiceAssetKey, "PRODUCT_CODE", "");
        final List<CIFAssetChoosableCandidate> cifAssetChoosableCandidates = newArrayList(expectedCandidate1);
        assertThat(candidates, is(cifAssetChoosableCandidates));
    }

    @Test
    public void shouldFilterAssetsByRules() {
        mockExtendAsset(newArrayList(ProductOfferingDetail, QuoteOptionItemDetail, ProductRules), baseAsset);
        SalesRelationship mockRelationship = mockRelationship(new ConsumerCardinality(0, 2, false), 1, false);

        AssetKey sameQuoteAssetKey = new AssetKey("sameQuote", 1);
        AssetKey inServiceAssetKey = new AssetKey("inService", 1);
        when(filterRule.getExecutionPoint()).thenReturn(FilterRuleExecutionPoint.FilterCandidateInstances);
        when(filterRule.getRelationshipName()).thenReturn(RELATIONSHIP_NAME);
        when(filterRule.isFilterRule()).thenReturn(true);
        final CIFAsset sameQuoteAsset = aCIFAsset().withID(sameQuoteAssetKey.getAssetId()).withVersion(sameQuoteAssetKey.getAssetVersion())
                                                   .withQuoteOptionId("QuoteOption1")
                                                   .withAssetVersionStatus(AssetVersionStatus.DRAFT)
                                                   .with(new CIFAssetQuoteOptionItemDetail(null, 0, MIGRATION, false, null, null, true,
                                                                                           null, null, null, "quoteOptionName", true, ProductCategoryCode.NIL, null, false)).build();
        final CIFAsset inServiceAsset = aCIFAsset().withID(inServiceAssetKey.getAssetId()).withVersion(inServiceAssetKey.getAssetVersion())
                                                   .withQuoteOptionId("QuoteOption3")
                                                   .withCharacteristic("STENCIL", "s0001")
                                                   .withAssetVersionStatus(AssetVersionStatus.IN_SERVICE)
                                                   .with(new CIFAssetQuoteOptionItemDetail(null, 0, MIGRATION, false, null, null, true,
                                                                                           null, null, null, "quoteOptionName", true, ProductCategoryCode.NIL, null, false)).build();
        when(filterRule.getSatisfaction(anyListOf(ContextualEvaluatorMap.class), eq(RELATIONSHIP_NAME))).thenReturn(ValidationErrorType.Error)
                                                                                                 .thenReturn(ValidationErrorType.Satisfied);
        when(cifAssetOrchestrator.eligibleExistingCandidates(baseAsset.getCustomerId(), baseAsset.getContractId(),
                                            mockRelationship.getProductIdentifier().getProductId(),
                                            newArrayList(ProductOfferingDetail, ProductRules, QuoteOptionItemDetail), baseAsset.getQuoteOptionId(), baseAsset.getSiteId(), false))
            .thenReturn(newArrayList(sameQuoteAsset, inServiceAsset));

        final List<CIFAssetChoosableCandidate> candidates = provider.getChoosableCandidates(baseAsset, newArrayList(mockRelationship));

        CIFAssetChoosableCandidate expectedCandidate1 = new CIFAssetChoosableCandidate(inServiceAssetKey, "PRODUCT_CODE", "s0001");
        final List<CIFAssetChoosableCandidate> cifAssetChoosableCandidates = newArrayList(expectedCandidate1);
        assertThat(candidates, is(cifAssetChoosableCandidates));
    }

    @Test
    public void shouldNotReturnChoosableCandidatesWhenDefaultCardinalityIsNotSpecified() {

        CIFAsset baseAsset = aCIFAsset().withQuoteOptionId("QuoteOption1").withRule(filterRule).withRelationships(0).build();

        mockExtendAsset(newArrayList(ProductOfferingDetail, QuoteOptionItemDetail), baseAsset);
        SalesRelationship mockRelationship = mockRelationship(new ConsumerCardinality(0, 2, false), 0, false);

        AssetKey sameQuoteAssetKey = new AssetKey("sameQuote", 1);
        AssetKey inServiceAssetKey = new AssetKey("inService", 1);
        when(filterRule.getExecutionPoint()).thenReturn(FilterRuleExecutionPoint.FilterCandidateInstances);
        when(filterRule.getRelationshipName()).thenReturn(RELATIONSHIP_NAME);
        when(filterRule.isFilterRule()).thenReturn(true);
        final CIFAsset sameQuoteAsset = aCIFAsset().withID(sameQuoteAssetKey.getAssetId()).withVersion(sameQuoteAssetKey.getAssetVersion())
                                                   .withQuoteOptionId("QuoteOption1")
                                                   .withAssetVersionStatus(AssetVersionStatus.DRAFT)
                                                   .with(new CIFAssetQuoteOptionItemDetail(null, 0, MIGRATION, false, null, null, true,
                                                                                           null, null, null, "quoteOptionName", true, ProductCategoryCode.NIL, null, false)).build();
        final CIFAsset inServiceAsset = aCIFAsset().withID(inServiceAssetKey.getAssetId()).withVersion(inServiceAssetKey.getAssetVersion())
                                                   .withQuoteOptionId("QuoteOption3")
                                                   .withCharacteristic("STENCIL", "s0001")
                                                   .withAssetVersionStatus(AssetVersionStatus.IN_SERVICE)
                                                   .with(new CIFAssetQuoteOptionItemDetail(null, 0, MIGRATION, false, null, null, true,
                                                                                           null, null, null, "quoteOptionName", true, ProductCategoryCode.NIL, null, false)).build();
        when(filterRule.getSatisfaction(anyListOf(ContextualEvaluatorMap.class), eq(RELATIONSHIP_NAME))).thenReturn(ValidationErrorType.Error)
                                                                                                        .thenReturn(ValidationErrorType.Satisfied);
        when(cifAssetOrchestrator.getAssets(baseAsset.getCustomerId(), baseAsset.getContractId(),
                                            mockRelationship.getProductIdentifier().getProductId(),
                                            newArrayList(ProductOfferingDetail, ProductRules, QuoteOptionItemDetail)))
            .thenReturn(newArrayList(sameQuoteAsset, inServiceAsset));

        final List<CIFAssetChoosableCandidate> candidates = provider.getAutoChoosableCandidates(baseAsset, newArrayList(mockRelationship), false, CIFAssetRelationshipCardinality.NO_CARDINALITY);

        assertThat(candidates.isEmpty(), is(true));
    }

    @Test
    public void shouldNotReturnChoosableCandidatesWhenDefaultCardinalityIsSpecifiedButAssetAddedAlready() {

        SalesRelationship mockRelationship = mockRelationship(new ConsumerCardinality(0, 2, false), 1, false);

        AssetKey sameQuoteAssetKey = new AssetKey("sameQuote", 1);
        AssetKey inServiceAssetKey = new AssetKey("inService", 1);
        when(filterRule.getExecutionPoint()).thenReturn(FilterRuleExecutionPoint.FilterCandidateInstances);
        when(filterRule.getRelationshipName()).thenReturn(RELATIONSHIP_NAME);
        when(filterRule.isFilterRule()).thenReturn(true);
        final CIFAsset sameQuoteAsset = aCIFAsset().withID(sameQuoteAssetKey.getAssetId()).withVersion(sameQuoteAssetKey.getAssetVersion())
                                                   .withQuoteOptionId("QuoteOption1")
                                                   .withAssetVersionStatus(AssetVersionStatus.DRAFT)
                                                   .with(new CIFAssetQuoteOptionItemDetail(null, 0, MIGRATION, false, null, null, true,
                                                                                           null, null, null, "quoteOptionName", true, ProductCategoryCode.NIL, null, false)).build();
        final CIFAsset inServiceAsset = aCIFAsset().withID(inServiceAssetKey.getAssetId()).withVersion(inServiceAssetKey.getAssetVersion())
                                                   .withQuoteOptionId("QuoteOption3")
                                                   .withCharacteristic("STENCIL", "s0001")
                                                   .withAssetVersionStatus(AssetVersionStatus.IN_SERVICE)
                                                   .with(new CIFAssetQuoteOptionItemDetail(null, 0, MIGRATION, false, null, null, true,
                                                                                           null, null, null, "quoteOptionName", true, ProductCategoryCode.NIL, null, false)).build();
        when(filterRule.getSatisfaction(anyListOf(ContextualEvaluatorMap.class), eq(RELATIONSHIP_NAME))).thenReturn(ValidationErrorType.Error)
                                                                                                        .thenReturn(ValidationErrorType.Satisfied);

        CIFAsset baseAsset = aCIFAsset().withQuoteOptionId("QuoteOption1").withRule(filterRule).withRelationship(sameQuoteAsset, RELATIONSHIP_NAME, RelationshipType.RelatedTo).build();

        mockExtendAsset(newArrayList(ProductOfferingDetail, QuoteOptionItemDetail, ProductRules), baseAsset);

        when(cifAssetOrchestrator.getAssets(baseAsset.getCustomerId(), baseAsset.getContractId(),
                                            mockRelationship.getProductIdentifier().getProductId(),
                                            newArrayList(ProductOfferingDetail, ProductRules, QuoteOptionItemDetail)))
            .thenReturn(newArrayList(sameQuoteAsset, inServiceAsset));

        final List<CIFAssetChoosableCandidate> candidates = provider.getAutoChoosableCandidates(baseAsset, newArrayList(mockRelationship), false, CIFAssetRelationshipCardinality.NO_CARDINALITY);

        assertThat(candidates.isEmpty(), is(true));
    }

    @Test
    public void shouldReturnChoosableCandidatesWhenDefaultCardinalityIsSpecifiedAndAssetYetToBeAdded() {

        SalesRelationship mockRelationship = mockRelationship(new ConsumerCardinality(0, 2, false), 1, false);

        AssetKey sameQuoteAssetKey = new AssetKey("sameQuote", 1);
        AssetKey inServiceAssetKey = new AssetKey("inService", 1);
        when(filterRule.getExecutionPoint()).thenReturn(FilterRuleExecutionPoint.FilterCandidateInstances);
        when(filterRule.getRelationshipName()).thenReturn(RELATIONSHIP_NAME);
        when(filterRule.isFilterRule()).thenReturn(true);
        final CIFAsset sameQuoteAsset = aCIFAsset().withID(sameQuoteAssetKey.getAssetId()).withVersion(sameQuoteAssetKey.getAssetVersion())
                                                   .withQuoteOptionId("QuoteOption1")
                                                   .withAssetVersionStatus(AssetVersionStatus.DRAFT)
                                                   .with(new CIFAssetQuoteOptionItemDetail(null, 0, MIGRATION, false, null, null, true,
                                                                                           null, null, null, "quoteOptionName", true, ProductCategoryCode.NIL, null, false)).build();
        final CIFAsset inServiceAsset = aCIFAsset().withID(inServiceAssetKey.getAssetId()).withVersion(inServiceAssetKey.getAssetVersion())
                                                   .withQuoteOptionId("QuoteOption3")
                                                   .withCharacteristic("STENCIL", "s0001")
                                                   .withAssetVersionStatus(AssetVersionStatus.IN_SERVICE)
                                                   .with(new CIFAssetQuoteOptionItemDetail(null, 0, MIGRATION, false, null, null, true,
                                                                                           null, null, null, "quoteOptionName", true, ProductCategoryCode.NIL, null, false)).build();
        when(filterRule.getSatisfaction(anyListOf(ContextualEvaluatorMap.class), eq(RELATIONSHIP_NAME))).thenReturn(ValidationErrorType.Error)
                                                                                                        .thenReturn(ValidationErrorType.Satisfied);

        CIFAsset baseAsset = aCIFAsset().withQuoteOptionId("QuoteOption1").withRule(filterRule).withRelationships(0).build();

        mockExtendAsset(newArrayList(ProductOfferingDetail, QuoteOptionItemDetail, ProductRules), baseAsset);

        when(cifAssetOrchestrator.eligibleExistingCandidates(baseAsset.getCustomerId(), baseAsset.getContractId(),
                                            mockRelationship.getProductIdentifier().getProductId(),
                                            newArrayList(ProductOfferingDetail, ProductRules, QuoteOptionItemDetail), baseAsset.getQuoteOptionId(), baseAsset.getSiteId(), false))
            .thenReturn(newArrayList(sameQuoteAsset, inServiceAsset));

        final List<CIFAssetChoosableCandidate> candidates = provider.getAutoChoosableCandidates(baseAsset, newArrayList(mockRelationship), false, CIFAssetRelationshipCardinality.NO_CARDINALITY);

        CIFAssetChoosableCandidate expectedCandidate1 = new CIFAssetChoosableCandidate(inServiceAssetKey, "PRODUCT_CODE", "s0001");
        final List<CIFAssetChoosableCandidate> cifAssetChoosableCandidates = newArrayList(expectedCandidate1);
        assertThat(candidates, is(cifAssetChoosableCandidates));
    }

    @Test
    public void shouldNotReturnChoosableCandidatesWhenDefaultCardinalityIsSpecifiedAndAssetYetToBeAddedButStencilNotSet() {

        SalesRelationship mockRelationship = mockRelationship(new ConsumerCardinality(0, 2, false), 1, false);

        AssetKey sameQuoteAssetKey = new AssetKey("sameQuote", 1);
        AssetKey inServiceAssetKey = new AssetKey("inService", 1);
        when(filterRule.getExecutionPoint()).thenReturn(FilterRuleExecutionPoint.FilterCandidateInstances);
        when(filterRule.getRelationshipName()).thenReturn(RELATIONSHIP_NAME);
        when(filterRule.isFilterRule()).thenReturn(true);
        final CIFAsset sameQuoteAsset = aCIFAsset().withID(sameQuoteAssetKey.getAssetId()).withVersion(sameQuoteAssetKey.getAssetVersion())
                                                   .withQuoteOptionId("QuoteOption1")
                                                   .withAssetVersionStatus(AssetVersionStatus.DRAFT)
                                                   .with(new CIFAssetQuoteOptionItemDetail(null, 0, MIGRATION, false, null, null, true,
                                                                                           null, null, null, "quoteOptionName", true, ProductCategoryCode.NIL, null, false)).build();
        final CIFAsset inServiceAsset = aCIFAsset().withID(inServiceAssetKey.getAssetId()).withVersion(inServiceAssetKey.getAssetVersion())
                                                   .withQuoteOptionId("QuoteOption3")
                                                   .withCharacteristic("STENCIL", "s0001")
                                                   .withAssetVersionStatus(AssetVersionStatus.IN_SERVICE)
                                                   .with(new CIFAssetQuoteOptionItemDetail(null, 0, MIGRATION, false, null, null, true,
                                                                                           null, null, null, "quoteOptionName", true, ProductCategoryCode.NIL, null, false)).build();
        when(filterRule.getSatisfaction(anyListOf(ContextualEvaluatorMap.class), eq(RELATIONSHIP_NAME))).thenReturn(ValidationErrorType.Error)
                                                                                                        .thenReturn(ValidationErrorType.Satisfied);

        CIFAsset baseAsset = aCIFAsset().withQuoteOptionId("QuoteOption1").withRule(filterRule).withRelationships(0).build();

        mockExtendAsset(newArrayList(ProductOfferingDetail, QuoteOptionItemDetail), baseAsset);

        when(cifAssetOrchestrator.getAssets(baseAsset.getCustomerId(), baseAsset.getContractId(),
                                            mockRelationship.getProductIdentifier().getProductId(),
                                            newArrayList(ProductOfferingDetail, ProductRules, QuoteOptionItemDetail)))
            .thenReturn(newArrayList(sameQuoteAsset, inServiceAsset));

        final List<CIFAssetChoosableCandidate> candidates = provider.getAutoChoosableCandidates(baseAsset, newArrayList(mockRelationship), true, CIFAssetRelationshipCardinality.NO_CARDINALITY);

        assertThat(candidates.isEmpty(), is(true));
    }

    @Test
    public void shouldReturnChoosableCandidatesWhenDefaultCardinalityIsSpecifiedAndAssetYetToBeAddedAndStencilIsSet() {

        SalesRelationship mockRelationship = mockRelationship(new ConsumerCardinality(0, 2, false), 1, true);

        AssetKey sameQuoteAssetKey = new AssetKey("sameQuote", 1);
        AssetKey inServiceAssetKey = new AssetKey("inService", 1);
        when(filterRule.getExecutionPoint()).thenReturn(FilterRuleExecutionPoint.FilterCandidateInstances);
        when(filterRule.getRelationshipName()).thenReturn(RELATIONSHIP_NAME);
        when(filterRule.isFilterRule()).thenReturn(true);
        final CIFAsset sameQuoteAsset = aCIFAsset().withID(sameQuoteAssetKey.getAssetId()).withVersion(sameQuoteAssetKey.getAssetVersion())
                                                   .withQuoteOptionId("QuoteOption1")
                                                   .withAssetVersionStatus(AssetVersionStatus.DRAFT)
                                                   .with(new CIFAssetQuoteOptionItemDetail(null, 0, MIGRATION, false, null, null, true,
                                                                                           null, null, null, "quoteOptionName", true, ProductCategoryCode.NIL, null, false)).build();
        final CIFAsset inServiceAsset = aCIFAsset().withID(inServiceAssetKey.getAssetId()).withVersion(inServiceAssetKey.getAssetVersion())
                                                   .withQuoteOptionId("QuoteOption3")
                                                   .withCharacteristic("STENCIL", "s0001")
                                                   .withAssetVersionStatus(AssetVersionStatus.IN_SERVICE)
                                                   .with(new CIFAssetQuoteOptionItemDetail(null, 0, MIGRATION, false, null, null, true,
                                                                                           null, null, null, "quoteOptionName", true, ProductCategoryCode.NIL, null, false)).build();
        when(filterRule.getSatisfaction(anyListOf(ContextualEvaluatorMap.class), eq(RELATIONSHIP_NAME))).thenReturn(ValidationErrorType.Error)
                                                                                                        .thenReturn(ValidationErrorType.Satisfied);

        CIFAsset baseAsset = aCIFAsset().withQuoteOptionId("QuoteOption1").withRule(filterRule).withRelationships(0).build();

        mockExtendAsset(newArrayList(ProductOfferingDetail, QuoteOptionItemDetail, ProductRules), baseAsset);

        when(cifAssetOrchestrator.eligibleExistingCandidates(baseAsset.getCustomerId(), baseAsset.getContractId(),
                mockRelationship.getProductIdentifier().getProductId(),
                newArrayList(ProductOfferingDetail, ProductRules, QuoteOptionItemDetail), baseAsset.getQuoteOptionId(), baseAsset.getSiteId(), false))
            .thenReturn(newArrayList(sameQuoteAsset, inServiceAsset));

        final List<CIFAssetChoosableCandidate> candidates = provider.getAutoChoosableCandidates(baseAsset, newArrayList(mockRelationship), true, CIFAssetRelationshipCardinality.NO_CARDINALITY);

        CIFAssetChoosableCandidate expectedCandidate1 = new CIFAssetChoosableCandidate(inServiceAssetKey, "PRODUCT_CODE", "s0001");
        final List<CIFAssetChoosableCandidate> cifAssetChoosableCandidates = newArrayList(expectedCandidate1);
        assertThat(candidates, is(cifAssetChoosableCandidates));
    }

    private void mockExtendAsset(ArrayList<CIFAssetExtension> expectedExtensions, CIFAsset baseAsset) {
        Answer<CIFAsset> extendAnswer = new Answer<CIFAsset>() {
            @Override
            public CIFAsset answer(InvocationOnMock invocation) throws Throwable {
                final CIFAsset baseAsset = (CIFAsset) invocation.getArguments()[0];

                baseAsset.loadQuoteOptionItemDetail(new CIFAssetQuoteOptionItemDetail(null, 0, MIGRATION, false, null, null, true,
                                                                                      null, null, null, "quoteOptionName", true, ProductCategoryCode.NIL, null, false));
                baseAsset.loadOfferingDetail(new CIFAssetOfferingDetail(null, null, null, null, false, false, null, false, AVAILABLE, null));

                return baseAsset;
            }
        };
        when(cifAssetOrchestrator.extendAsset(baseAsset, expectedExtensions)).thenAnswer(extendAnswer);
    }

    private SalesRelationship mockRelationship(ConsumerCardinality consumerCardinality, int defaultCardinality, boolean hasStencilSet) {
        SalesRelationship mockRelationship = mock(SalesRelationship.class);
        when(mockRelationship.getProductIdentifier()).thenReturn(new ProductIdentifier("relatedCode", "V1"));
        when(mockRelationship.getConsumerCardinality()).thenReturn(consumerCardinality);
        when(mockRelationship.getRelationshipName()).thenReturn(RelationshipName.newInstance(RELATIONSHIP_NAME));
        when(mockRelationship.getDefault()).thenReturn(defaultCardinality);
        when(mockRelationship.hasStencilSet()).thenReturn(hasStencilSet);
        when(mockRelationship.getType()).thenReturn(RelationshipType.RelatedTo);
        return mockRelationship;
    }
}
