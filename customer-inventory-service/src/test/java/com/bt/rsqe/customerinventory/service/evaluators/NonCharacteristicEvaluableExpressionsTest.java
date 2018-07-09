package com.bt.rsqe.customerinventory.service.evaluators;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class NonCharacteristicEvaluableExpressionsTest{
    @Test
    public void shouldKnowIfExpressionIsHandled(){
        boolean unknownExpression = NonCharacteristicEvaluableExpressions.containsExpression("UnknownExpression");
        boolean knownExpression = NonCharacteristicEvaluableExpressions.containsExpression("AssetUniqueId");
        boolean knownExpressionCaseInsensitive = NonCharacteristicEvaluableExpressions.containsExpression("assetUniqueId");

        assertThat(unknownExpression, is(false));
        assertThat(knownExpression, is(true));
        assertThat(knownExpressionCaseInsensitive, is(true));
    }
}