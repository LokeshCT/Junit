package com.bt.rsqe.customerinventory.service.evaluators;

import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetCreatableCandidate;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static com.bt.rsqe.expressionevaluator.ContextualEvaluatorTypes.STRING_EVALUATOR;
import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class CIFAssetCandidateEvaluatorTest {
    @Test
    public void shouldGetValuesFromCandidate() {
        final CIFAssetCreatableCandidate candidate = new CIFAssetCreatableCandidate("p0001", "", "s0001", "", "", false);
        final CIFAssetCandidateEvaluator cifAssetCandidateEvaluator = new CIFAssetCandidateEvaluator(candidate);

        assertThat(cifAssetCandidateEvaluator.getValues(new ArrayList<String>(), "ProductCode"), is((List<Object>)newArrayList((Object)"p0001")));
        assertThat(cifAssetCandidateEvaluator.getValues(new ArrayList<String>(), "StencilCode"), is((List<Object>)newArrayList((Object)"s0001")));
        assertThat(cifAssetCandidateEvaluator.getValues(new ArrayList<String>(), "SomethingElse"), is((List<Object>)new ArrayList<Object>()));
    }

    @Test
    public void shouldGetStringBaseEvaluator() {
        final CIFAssetCandidateEvaluator cifAssetCandidateEvaluator = new CIFAssetCandidateEvaluator(null);
        assertThat(cifAssetCandidateEvaluator.getBaseConversion(), is(STRING_EVALUATOR.getName()));
    }
}