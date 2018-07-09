package com.bt.rsqe.customerinventory.service.updates;

import com.bt.rsqe.customerinventory.service.client.domain.CIFAsset;
import com.bt.rsqe.customerinventory.service.client.domain.updates.CIFAssetUpdateRequest;
import com.bt.rsqe.customerinventory.service.rules.RelateToRuleExecutor;
import com.bt.rsqe.domain.product.extensions.RuleRelateTo;
import com.bt.rsqe.domain.product.extensions.StructuredRule;

import java.util.ArrayList;
import java.util.List;

public class ExecutionRequestBuilder {
    private RelateToRuleExecutor relateToRuleExecutor;

    public ExecutionRequestBuilder(RelateToRuleExecutor relateToRuleExecutor) {
        this.relateToRuleExecutor = relateToRuleExecutor;
    }

    public List<CIFAssetUpdateRequest> buildFor(CIFAsset cifAsset) {
        final ArrayList<CIFAssetUpdateRequest> updateRequests = new ArrayList<CIFAssetUpdateRequest>();

        for (StructuredRule structuredRule : cifAsset.getProductRules()) {
            if(structuredRule.isExecutionRule()){
                updateRequests.addAll(relateToRuleExecutor.execute((RuleRelateTo)structuredRule, cifAsset));
            }
        }

        return updateRequests;
    }
}
