package com.bt.rsqe.customerinventory.service.evaluators;

import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetCandidate;
import com.bt.rsqe.expressionevaluator.ContextualEvaluator;

import java.util.ArrayList;
import java.util.List;
import static com.bt.rsqe.expressionevaluator.ContextualEvaluatorTypes.STRING_EVALUATOR;
import static com.google.common.collect.Lists.newArrayList;

public class CIFAssetCandidateEvaluator extends ContextualEvaluator {
    private final CIFAssetCandidate candidate;
    public static final String PRODUCT_CODE = "ProductCode";
    public static final String STENCIL_CODE = "StencilCode";

    public CIFAssetCandidateEvaluator(CIFAssetCandidate candidate) {
        this.candidate = candidate;
    }

    @Override
    protected List<Object> getValues(List<String> expressionPath, String expression) {
        if(PRODUCT_CODE.equals(expression)){
            return newArrayList((Object)candidate.getProductCode());
        }else if(STENCIL_CODE.equals(expression)){
            return newArrayList((Object)candidate.getStencilCode());
        }
        return new ArrayList<Object>();
    }

    @Override
    protected String getBaseConversion() {
        return STRING_EVALUATOR.getName();
    }
}
