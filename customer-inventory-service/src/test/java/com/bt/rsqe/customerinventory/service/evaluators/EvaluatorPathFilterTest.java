package com.bt.rsqe.customerinventory.service.evaluators;

import static junit.framework.Assert.assertTrue;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

import com.bt.rsqe.customerinventory.service.orchestrators.CIFAssetOrchestrator;
import org.junit.Test;

public class EvaluatorPathFilterTest {
    @Test
    public void shouldHaveWorkingEqualsAndHashcodeMethods() {
        CIFAssetOrchestrator cifAssetOrchestrator = mock(CIFAssetOrchestrator.class);
        CIFAssetCharacteristicEvaluatorFactory evaluatorFactory = mock(CIFAssetCharacteristicEvaluatorFactory.class);

        EvaluatorPathFilter evaluatorPathFilter1 = new EvaluatorPathFilter("", cifAssetOrchestrator, evaluatorFactory);
        EvaluatorPathFilter evaluatorPathFilter2 = new EvaluatorPathFilter("", cifAssetOrchestrator, evaluatorFactory);

        assertTrue(evaluatorPathFilter1.equals(evaluatorPathFilter2));
        assertThat(evaluatorPathFilter1.hashCode(), is(evaluatorPathFilter2.hashCode()));
    }
}