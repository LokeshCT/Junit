package com.bt.rsqe.customerinventory.service.evaluators;

import com.bt.rsqe.customerinventory.parameter.ProductInstanceState;
import com.bt.rsqe.productinstancemerge.ChangeType;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class ChangeTypeEvaluatorTest {
    @Test
    public void shouldGetAddChangeTypeWhenNoSourceAsset() {
        assertThat(ChangeTypeEvaluator.fromSourceAndState(false, ProductInstanceState.LIVE), is(ChangeType.ADD));
    }

    @Test
    public void shouldGetDeleteChangeTypeWhenSourceAssetAndCeasing() {
        assertThat(ChangeTypeEvaluator.fromSourceAndState(true, ProductInstanceState.CEASED), is(ChangeType.DELETE));
    }

    @Test
    public void shouldGetUpdateChangeTypeWhenSourceAssetAndNotCeasing() {
        assertThat(ChangeTypeEvaluator.fromSourceAndState(true, ProductInstanceState.LIVE), is(ChangeType.UPDATE));
    }
}