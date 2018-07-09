package com.bt.rsqe.ape.source.extractor;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.core.Is.*;
import static org.hamcrest.core.IsInstanceOf.*;

/**
 * Created by 605783162 on 14/08/2015.
 */
public class ResponseExtractorStrategyFactoryTest {

    @Test
    public void shouldReturnCorrectExtractorStrategy() {
        ResponseExtractorStrategy strategy;
        ResponseExtractorStrategyFactory strategyFactory = new ResponseExtractorStrategyFactory();

        strategy = strategyFactory.getExtractorStrategy("SupplierProductListResponse",null);
        assertThat(strategy, is(instanceOf(SupplierProductResponseExtractor.class)));

        strategy = strategyFactory.getExtractorStrategy("AvailabiltyResponse",null);
        assertThat(strategy, is(instanceOf(SupplierAvailabilityResponseExtractor.class)));
    }

    @Test(expected = RuntimeException.class)
    public void shouldThrowAnExceptionForAnUnknownResponseType() {
        ResponseExtractorStrategyFactory strategyFactory = new ResponseExtractorStrategyFactory();
        strategyFactory.getExtractorStrategy("sqeUnknownResponse",null);
    }

}