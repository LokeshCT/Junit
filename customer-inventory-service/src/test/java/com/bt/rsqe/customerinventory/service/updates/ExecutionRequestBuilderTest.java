package com.bt.rsqe.customerinventory.service.updates;

import com.bt.rsqe.customerinventory.service.client.domain.CIFAsset;
import com.bt.rsqe.customerinventory.service.client.domain.updates.CIFAssetUpdateRequest;
import com.bt.rsqe.customerinventory.service.rules.RelateToRuleExecutor;
import com.bt.rsqe.domain.product.extensions.RuleRelateTo;
import com.bt.rsqe.domain.product.extensions.StructuredRule;
import org.junit.Test;

import java.util.List;

import static com.bt.rsqe.customerinventory.service.client.fixtures.CIFAssetFixture.aCIFAsset;
import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.junit.matchers.JUnitMatchers.hasItem;
import static org.mockito.Mockito.*;

public class ExecutionRequestBuilderTest {
    private RelateToRuleExecutor relateToRuleExecutor = mock(RelateToRuleExecutor.class);

    @Test
    public void shouldNotGenerateAnyRequestsWhenTheAssetOnlyHasNoRules() {
        final CIFAsset cifAsset = aCIFAsset().withNoRules().build();

        final List<CIFAssetUpdateRequest> updateRequests = new ExecutionRequestBuilder(relateToRuleExecutor).buildFor(cifAsset);

        assertThat(updateRequests.size(), is(0));
    }

    @Test
    public void shouldGenerateRequestsBasedOnTheRelateToRuleExecutorResponses() {
        final RuleRelateTo relateToRule = new RuleRelateTo("rule1", null, "", "", false);
        final CIFAsset cifAsset = aCIFAsset().withID("assetId").withVersion(1).withRule(relateToRule).build();
        final CIFAssetUpdateRequest mockRespondedRequest = mock(CIFAssetUpdateRequest.class);
        when(relateToRuleExecutor.execute(relateToRule, cifAsset)).thenReturn(newArrayList(mockRespondedRequest));

        final List<CIFAssetUpdateRequest> updateRequests = new ExecutionRequestBuilder(relateToRuleExecutor).buildFor(cifAsset);

        assertThat(updateRequests.size(), is(1));
        assertThat(updateRequests, hasItem(mockRespondedRequest));
    }

    @Test
    public void shouldNotGenerateAnyRequestsWhenTheAssetHasNoRelateToRulesButHasOtherRules() {
        final StructuredRule nonRelateToRule = mock(StructuredRule.class);
        final CIFAsset cifAsset = aCIFAsset().withID("assetId").withVersion(1).withRule(nonRelateToRule).build();

        final List<CIFAssetUpdateRequest> updateRequests = new ExecutionRequestBuilder(relateToRuleExecutor).buildFor(cifAsset);

        assertThat(updateRequests.size(), is(0));
    }

    @Test
    public void shouldGenerateRequestsBasedOnTheRelateToRuleExecutorResponsesForMultipleRules() {
        final RuleRelateTo relateToRule1 = new RuleRelateTo("rule1", null, "", "", false);
        final RuleRelateTo relateToRule2 = new RuleRelateTo("rule2", null, "", "", false);
        final CIFAsset cifAsset = aCIFAsset().withID("assetId").withVersion(1).withRule(relateToRule1).withRule(relateToRule2).build();
        final CIFAssetUpdateRequest mockRespondedRequest1 = mock(CIFAssetUpdateRequest.class);
        final CIFAssetUpdateRequest mockRespondedRequest2 = mock(CIFAssetUpdateRequest.class);
        when(relateToRuleExecutor.execute(relateToRule1, cifAsset)).thenReturn(newArrayList(mockRespondedRequest1));
        when(relateToRuleExecutor.execute(relateToRule2, cifAsset)).thenReturn(newArrayList(mockRespondedRequest2));

        final List<CIFAssetUpdateRequest> updateRequests = new ExecutionRequestBuilder(relateToRuleExecutor).buildFor(cifAsset);

        assertThat(updateRequests.size(), is(2));
        assertThat(updateRequests, hasItem(mockRespondedRequest1));
        assertThat(updateRequests, hasItem(mockRespondedRequest2));
    }
}