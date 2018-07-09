package com.bt.rsqe.customerinventory.service.updates;

import com.bt.rsqe.customerinventory.service.client.domain.CIFAsset;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetChoosableCandidate;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetCreatableCandidate;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetKey;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetRelationshipCardinality;
import com.bt.rsqe.customerinventory.service.client.domain.updates.AutoDefaultRelationshipsRequest;
import com.bt.rsqe.customerinventory.service.client.domain.updates.AutoDefaultRelationshipsResponse;
import com.bt.rsqe.customerinventory.service.client.domain.updates.CIFAssetUpdateRequest;
import com.bt.rsqe.customerinventory.service.client.domain.updates.ChooseRelationshipRequest;
import com.bt.rsqe.customerinventory.service.client.domain.updates.CreateRelationshipRequest;
import com.bt.rsqe.customerinventory.service.client.domain.updates.UpdateRequestSource;
import com.bt.rsqe.customerinventory.service.client.resource.CIFAssetExtension;
import com.bt.rsqe.customerinventory.service.evaluators.CIFAssetCandidateEvaluator;
import com.bt.rsqe.customerinventory.service.evaluators.CIFAssetCharacteristicEvaluatorFactory;
import com.bt.rsqe.customerinventory.service.evaluators.CIFAssetEvaluator;
import com.bt.rsqe.customerinventory.service.externals.PmrHelper;
import com.bt.rsqe.customerinventory.service.orchestrators.CIFAssetOrchestrator;
import com.bt.rsqe.domain.AssetKey;
import com.bt.rsqe.domain.product.ProductOffering;
import com.bt.rsqe.domain.product.extensions.RuleDefaulting;
import com.bt.rsqe.domain.product.extensions.StructuredRule;
import com.bt.rsqe.domain.product.parameters.RelationshipType;
import com.bt.rsqe.domain.product.parameters.ResolvesTo;
import com.bt.rsqe.expressionevaluator.ContextualEvaluatorMap;
import com.bt.rsqe.expressionevaluator.SingleValueEvaluator;
import com.bt.rsqe.expressionevaluator.expr.SyntaxException;
import com.bt.rsqe.matchers.ReflectionEqualsMatcher;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.List;

import static com.bt.rsqe.customerinventory.service.client.fixtures.CIFAssetFixture.*;
import static com.bt.rsqe.customerinventory.service.client.resource.CIFAssetExtension.*;
import static com.bt.rsqe.domain.bom.fixtures.ProductOfferingFixture.*;
import static com.bt.rsqe.domain.bom.fixtures.SalesRelationshipFixture.*;
import static com.google.common.collect.Lists.*;
import static org.hamcrest.core.Is.*;
import static org.hamcrest.core.IsInstanceOf.*;
import static org.junit.Assert.*;
import static org.junit.matchers.JUnitMatchers.*;
import static org.mockito.Mockito.*;

public class AutoDefaultRelationshipUpdaterTest {
    private final AssetKey assetKey = new AssetKey("assetId", 1);
    private final AssetKey choosableAssetKey = new AssetKey("chooseAssetId", 1);
    private final ArrayList<CIFAssetExtension> requiredExtensions = newArrayList(ProductOfferingRelationshipDetail,
                                                                                 Relationships,
                                                                                 RelationshipCardinality,
                                                                                 AutoCreatableCandidates,
                                                                                 AutoChoosableCandidates);
    private final CIFAssetKey cifAssetKey = new CIFAssetKey(assetKey, requiredExtensions);
    private final AutoDefaultRelationshipsRequest request = new AutoDefaultRelationshipsRequest(assetKey, "lineItemId", 1, "aProductCode");
    private final CIFAssetOrchestrator cifAssetOrchestrator = mock(CIFAssetOrchestrator.class);
    private final PmrHelper pmrHelper = mock(PmrHelper.class);
    private final CIFAssetCharacteristicEvaluatorFactory evaluatorFactory = mock(CIFAssetCharacteristicEvaluatorFactory.class);
    private final AutoDefaultRelationshipUpdater updater = new AutoDefaultRelationshipUpdater(cifAssetOrchestrator, evaluatorFactory, pmrHelper);

    @Captor
    private ArgumentCaptor<List<ContextualEvaluatorMap>> expectedContextualEvaluators;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        Answer<CIFAsset> extendAssetAnswer = new Answer<CIFAsset>() {
            @Override
            public CIFAsset answer(InvocationOnMock invocation) throws Throwable {
                return (CIFAsset)invocation.getArguments()[0];
            }
        };
        when(cifAssetOrchestrator.forceExtendAsset(any(CIFAsset.class), anyListOf(CIFAssetExtension.class))).thenAnswer(extendAssetAnswer);
        ProductOffering productOffering = aProductOffering().withSalesRelationship(aSalesRelationship().withRelationName("relation1")
                                                                                                       .withProductIdentifier("prodId")).build();
        when(pmrHelper.getProductOffering("aProductCode")).thenReturn(productOffering);
    }

    @Test
    public void shouldNotProduceAnyDependantUpdatesForRelationshipWithExistingRelationshipsEqualToDefaultCardinality() {
        CIFAsset relatedAsset = aCIFAsset().build();
        CIFAsset cifAsset = aCIFAsset().withRelationshipDefinition("relation1", RelationshipType.Child, "prodId", "groupName",
                                                                   new ArrayList<String>(),
                                                                   new CIFAssetRelationshipCardinality(1),
                                                                   new CIFAssetRelationshipCardinality(1),
                                                                   new CIFAssetRelationshipCardinality(1),
                                                                   "stencilId",
                                                                   new ArrayList<CIFAssetCreatableCandidate>(),
                                                                   new ArrayList<CIFAssetChoosableCandidate>(),
                                                                   ResolvesTo.Any, false)
                                       .withNoRules()
                                       .withRelationship(relatedAsset, "relation1", RelationshipType.Child).build();
        when(cifAssetOrchestrator.getAsset(cifAssetKey)).thenReturn(cifAsset);

        final AutoDefaultRelationshipsResponse response = updater.performUpdate(request);

        assertThat(response.getDependantUpdates(), is((List<CIFAssetUpdateRequest>) new ArrayList<CIFAssetUpdateRequest>()));
    }

    @Test
    public void shouldNotProduceAnyDependantUpdatesForRelationshipWithExistingRelationshipsGreaterThanDefaultCardinality() {
        CIFAsset relatedAsset1 = aCIFAsset().build();
        CIFAsset relatedAsset2 = aCIFAsset().build();
        CIFAsset cifAsset = aCIFAsset().withRelationshipDefinitionAndDefaultCandidates("relation1", RelationshipType.Child, "prodId", "groupName",
                new ArrayList<String>(),
                new CIFAssetRelationshipCardinality(1),
                new CIFAssetRelationshipCardinality(1),
                new CIFAssetRelationshipCardinality(1),
                "stencilId",
                new ArrayList<CIFAssetCreatableCandidate>(),
                new ArrayList<CIFAssetChoosableCandidate>(),
                ResolvesTo.Any, false)
                                       .withRelationship(relatedAsset1, "relation1", RelationshipType.Child)
                                       .withRelationship(relatedAsset2, "relation1", RelationshipType.Child)
                                       .withNoRules()
                                       .build();
        when(cifAssetOrchestrator.getAsset(cifAssetKey)).thenReturn(cifAsset);

        final AutoDefaultRelationshipsResponse response = updater.performUpdate(request);

        assertThat(response.getDependantUpdates(), is((List<CIFAssetUpdateRequest>) new ArrayList<CIFAssetUpdateRequest>()));
    }

    @Test
    public void shouldNotProduceCreateRelationshipRequestForRelationshipWithFewerThanDefaultCardinalityButNoPossibleCreatableOrChoosableCandidate() {
        CIFAsset cifAsset = aCIFAsset().withRelationshipDefinitionAndDefaultCandidates("relation1", RelationshipType.Child, "prodId", "groupName",
                new ArrayList<String>(),
                new CIFAssetRelationshipCardinality(1),
                new CIFAssetRelationshipCardinality(1),
                new CIFAssetRelationshipCardinality(1),
                "stencilId",
                new ArrayList<CIFAssetCreatableCandidate>(),
                new ArrayList<CIFAssetChoosableCandidate>(),
                ResolvesTo.Any, false)
                                       .withNoRules()
                                       .withRelationships(0)
                                       .build();
        when(cifAssetOrchestrator.getAsset(cifAssetKey)).thenReturn(cifAsset);

        final AutoDefaultRelationshipsResponse response = updater.performUpdate(request);
        assertThat(response.getDependantUpdates(), is((List<CIFAssetUpdateRequest>) new ArrayList<CIFAssetUpdateRequest>()));
    }

    @Test
    public void shouldNotProduceCreateRelationshipRequestForRelationshipWithFewerThanDefaultCardinalityButResolvesToExistingOnlyAndNoChoosableCandidates() {
        CIFAssetCreatableCandidate candidate = new CIFAssetCreatableCandidate("p0001", "V01", "s0001", "", "", false);
        CIFAsset cifAsset = aCIFAsset().withRelationshipDefinitionAndDefaultCandidates("relation1", RelationshipType.Child, "prodId", "groupName",
                                                                                       new ArrayList<String>(),
                                                                                       new CIFAssetRelationshipCardinality(1),
                                                                                       new CIFAssetRelationshipCardinality(1),
                                                                                       new CIFAssetRelationshipCardinality(1),
                                                                                       "stencilId",
                                                                                       newArrayList(candidate),
                                                                                       new ArrayList<CIFAssetChoosableCandidate>(),
                                                                                       ResolvesTo.ExistingOnly, false)
                                       .withNoRules()
                                       .withRelationships(0)
                                       .build();
        when(cifAssetOrchestrator.getAsset(cifAssetKey)).thenReturn(cifAsset);

        final AutoDefaultRelationshipsResponse response = updater.performUpdate(request);
        assertThat(response.getDependantUpdates(), is((List<CIFAssetUpdateRequest>) new ArrayList<CIFAssetUpdateRequest>()));
    }

    @Test
    public void shouldProduceCreateRelationshipRequestForRelationshipWithFewerThanDefaultCardinalityAndOnePossibleCreatableCandidate() {
        CIFAssetCreatableCandidate candidate = new CIFAssetCreatableCandidate("p0001", "V01", "s0001", "", "", false);
        CIFAsset cifAsset = aCIFAsset().withRelationshipDefinitionAndDefaultCandidates("relation1", RelationshipType.Child, "prodId", "groupName",
                                                                                       new ArrayList<String>(),
                                                                                       new CIFAssetRelationshipCardinality(1),
                                                                                       new CIFAssetRelationshipCardinality(1),
                                                                                       new CIFAssetRelationshipCardinality(2),
                                                                                       "stencilId",
                                                                                       newArrayList(candidate),
                                                                                       new ArrayList<CIFAssetChoosableCandidate>(),
                                                                                       ResolvesTo.Any, false)
                                       .withNoRules()
                                       .withRelationships(0)
                                       .build();
        when(cifAssetOrchestrator.getAsset(cifAssetKey)).thenReturn(cifAsset);

        final AutoDefaultRelationshipsResponse response = updater.performUpdate(request);

        CIFAssetUpdateRequest expectedRequest = new CreateRelationshipRequest("", assetKey, "relation1", "p0001", "s0001", "", "",
                                                                                  request.getLineItemId(), request.getLockVersion());
        assertThat(response.getDependantUpdates(), hasItem(ReflectionEqualsMatcher.reflectionEquals(expectedRequest, "clientIdentifier")));
    }

    @Test
    public void shouldNotProduceCreateRelationshipRequestForRelationshipWhenRelationIsAStencillableOneButNoStencilIdAvailable() {
    //This scenario would occur for bearer product, though its stencillable product,
    // the stencil Id would be available after getAccess call.
        CIFAssetCreatableCandidate candidate = new CIFAssetCreatableCandidate("p0001", "V01", "s0001", "", "", false);
        CIFAsset cifAsset = aCIFAsset().withRelationshipDefinitionAndDefaultCandidates("relation1", RelationshipType.Child, "prodId", "groupName",
                                                                                       new ArrayList<String>(),
                                                                                       new CIFAssetRelationshipCardinality(1),
                                                                                       new CIFAssetRelationshipCardinality(1),
                                                                                       new CIFAssetRelationshipCardinality(2),
                                                                                       null,
                                                                                       newArrayList(candidate),
                                                                                       new ArrayList<CIFAssetChoosableCandidate>(),
                                                                                       ResolvesTo.Any, true)
                                       .withNoRules()
                                       .withRelationships(0)
                                       .build();
        when(cifAssetOrchestrator.getAsset(cifAssetKey)).thenReturn(cifAsset);

        final AutoDefaultRelationshipsResponse response = updater.performUpdate(request);

        assertThat(response.getDependantUpdates().isEmpty(), is(true));
    }

    @Test
    public void shouldProduceChooseRelationshipRequestForRelationshipWithFewerThanDefaultCardinalityAndNoCreatableCandidatesAndOneChoosableCandidate() {
        CIFAssetChoosableCandidate candidate = new CIFAssetChoosableCandidate(choosableAssetKey, "p0001", "s0001");
        CIFAsset cifAsset = aCIFAsset().withRelationshipDefinitionAndDefaultCandidates("relation1", RelationshipType.RelatedTo, "prodId", "groupName",
                                                                                       new ArrayList<String>(),
                                                                                       new CIFAssetRelationshipCardinality(1),
                                                                                       new CIFAssetRelationshipCardinality(1),
                                                                                       new CIFAssetRelationshipCardinality(2),
                                                                                       "stencilId",
                                                                                       new ArrayList<CIFAssetCreatableCandidate>(),
                                                                                       newArrayList(candidate),
                                                                                       ResolvesTo.Any, false)
                                       .withNoRules()
                                       .withRelationships(0)
                                       .build();
        when(cifAssetOrchestrator.getAsset(cifAssetKey)).thenReturn(cifAsset);

        final AutoDefaultRelationshipsResponse response = updater.performUpdate(request);

        ChooseRelationshipRequest expectedRequest = new ChooseRelationshipRequest(assetKey, choosableAssetKey, "relation1",
                                                                                  request.getLineItemId(), request.getLockVersion(), UpdateRequestSource.AutoDefault);
        assertThat(response.getDependantUpdates(), hasItem((CIFAssetUpdateRequest) expectedRequest));
    }

    @Test
    public void shouldNotProduceChooseRelationshipRequestForResolvesToNewOnlyRelationshipWithFewerThanDefaultCardinalityAndNoCreatableCandidatesAndOneChoosableCandidate() {
        CIFAssetChoosableCandidate candidate = new CIFAssetChoosableCandidate(choosableAssetKey, "p0001", "s0001");
        CIFAsset cifAsset = aCIFAsset().withRelationshipDefinitionAndDefaultCandidates("relation1", RelationshipType.Child, "prodId", "groupName",
                                                                                       new ArrayList<String>(),
                                                                                       new CIFAssetRelationshipCardinality(1),
                                                                                       new CIFAssetRelationshipCardinality(1),
                                                                                       new CIFAssetRelationshipCardinality(2),
                                                                                       "stencilId",
                                                                                       new ArrayList<CIFAssetCreatableCandidate>(),
                                                                                       newArrayList(candidate),
                                                                                       ResolvesTo.NewOnly, false)
                                       .withNoRules()
                                       .withRelationships(0)
                                       .build();
        when(cifAssetOrchestrator.getAsset(cifAssetKey)).thenReturn(cifAsset);

        final AutoDefaultRelationshipsResponse response = updater.performUpdate(request);

        assertThat(response.getDependantUpdates(), is((List<CIFAssetUpdateRequest>) new ArrayList<CIFAssetUpdateRequest>()));
    }

    @Test
    public void shouldNotReadAssetWithExtensionsWhenThereAreNoSalesRelationships() {
        //Given
        when(pmrHelper.getProductOffering("aProductCode")).thenReturn(aProductOffering().build());

        //When
        updater.performUpdate(request);

        //Then
        verify(cifAssetOrchestrator, times(0)).getAsset(any(CIFAssetKey.class));
    }

    @Test
    @SuppressWarnings("unchecked") // Suppressing the warning for thenThrow(SyntaxException.class)
    public void shouldFilterCandidatesUsingDefaultingRule() throws SyntaxException {
        CIFAssetChoosableCandidate choosableCandidate = new CIFAssetChoosableCandidate(choosableAssetKey, "p0001", "s0001");
        CIFAssetCreatableCandidate creatableCandidate = new CIFAssetCreatableCandidate("p0001", "V01", "s0001", "", "", false);
        RuleDefaulting defaultingRule = mock(RuleDefaulting.class);
        when(defaultingRule.isDefaultingRule()).thenReturn(true);
        when(defaultingRule.getRelationshipName()).thenReturn("relation1");

        when(defaultingRule.evaluateCandidate(anyListOf(ContextualEvaluatorMap.class))).thenReturn(true)
                                                                                       .thenThrow(SyntaxException.class)
                                                                                       .thenReturn(false);
        StructuredRule nonDefaultingRule = mock(StructuredRule.class);
        when(nonDefaultingRule.isDefaultingRule()).thenReturn(false);
        CIFAsset cifAsset = aCIFAsset().withRelationshipDefinitionAndDefaultCandidates("relation1", RelationshipType.RelatedTo, "prodId", "groupName",
                                                                                       new ArrayList<String>(),
                                                                                       new CIFAssetRelationshipCardinality(1),
                                                                                       new CIFAssetRelationshipCardinality(1),
                                                                                       new CIFAssetRelationshipCardinality(1),
                                                                                       "stencilId",
                                                                                       newArrayList(creatableCandidate, creatableCandidate),
                                                                                       newArrayList(choosableCandidate),
                                                                                       ResolvesTo.Any, false)
                                       .withRule(defaultingRule).withRule(nonDefaultingRule)
                                       .withRelationships(0)
                                       .build();
        when(cifAssetOrchestrator.getAsset(cifAssetKey)).thenReturn(cifAsset);
        CIFAsset someOtherAsset = aCIFAsset().build();
        final CIFAsset parentAsset = aCIFAsset().withRelationship(someOtherAsset, "relation1", RelationshipType.Child)
                                                .withRelationship(cifAsset, "relation1", RelationshipType.Child).build();
        when(cifAssetOrchestrator.getParentAsset(new CIFAssetKey(cifAsset.getAssetKey(), newArrayList(Relationships))))
            .thenReturn(parentAsset);

        final AutoDefaultRelationshipsResponse response = updater.performUpdate(request);

        CIFAssetUpdateRequest expectedRequest = new CreateRelationshipRequest("", assetKey, "relation1", "p0001", "s0001", "", "",
                                                                                  request.getLineItemId(), request.getLockVersion());
        assertThat(response.getDependantUpdates(), hasItem(ReflectionEqualsMatcher.reflectionEquals(expectedRequest, "clientIdentifier")));

        verify(defaultingRule, times(3)).evaluateCandidate(expectedContextualEvaluators.capture());
        final List<ContextualEvaluatorMap> value = expectedContextualEvaluators.getValue();
        assertThat(value.get(0).getEvaluatorIdentifier(), is(RuleDefaulting.CANDIDATE_IDENTIFIER));
        assertThat(value.get(0).getContextualEvaluator(), instanceOf(CIFAssetCandidateEvaluator.class));
        assertThat(value.get(1).getEvaluatorIdentifier(), is(RuleDefaulting.PARENT_IDENTIFIER));
        assertThat(value.get(1).getContextualEvaluator(), instanceOf(CIFAssetEvaluator.class));
        assertThat(value.get(2).getEvaluatorIdentifier(), is(RuleDefaulting.GRAND_PARENT_IDENTIFIER));
        assertThat(value.get(2).getContextualEvaluator(), instanceOf(CIFAssetEvaluator.class));
        assertThat(value.get(3).getEvaluatorIdentifier(), is(RuleDefaulting.GRAND_PARENT_RELATION_NAME_IDENTIFIER));
        assertThat(value.get(3).getContextualEvaluator(), instanceOf(SingleValueEvaluator.class));
    }

    @Test
    public void shouldProduceCreateRelationshipRequestsForRelationshipWhenDefaultCardinalityForMoreCandidates() {
        CIFAssetCreatableCandidate candidate = new CIFAssetCreatableCandidate("p0001", "V01", "s0001", "", "", false);
        CIFAsset cifAsset = aCIFAsset().withRelationshipDefinitionAndDefaultCandidates("relation1", RelationshipType.Child, "prodId", "groupName",
                new ArrayList<String>(),
                new CIFAssetRelationshipCardinality(2),
                new CIFAssetRelationshipCardinality(2),
                new CIFAssetRelationshipCardinality(2),
                "stencilId",
                newArrayList(candidate),
                new ArrayList<CIFAssetChoosableCandidate>(),
                ResolvesTo.Any, false)
                .withNoRules()
                .withRelationships(0)
                .build();
        when(cifAssetOrchestrator.getAsset(cifAssetKey)).thenReturn(cifAsset);

        final AutoDefaultRelationshipsResponse response = updater.performUpdate(request);

        CreateRelationshipRequest expectedRequest = new CreateRelationshipRequest("", assetKey, "relation1", "p0001", "s0001", "", "",
                request.getLineItemId(), request.getLockVersion());

        assertThat(response.getDependantUpdates().size(), is(2));
        assertThat((CreateRelationshipRequest) response.getDependantUpdates().get(0), is(ReflectionEqualsMatcher.reflectionEquals(expectedRequest, "clientIdentifier")));
        assertThat((CreateRelationshipRequest) response.getDependantUpdates().get(1), is(ReflectionEqualsMatcher.reflectionEquals(expectedRequest, "clientIdentifier")));
    }

}