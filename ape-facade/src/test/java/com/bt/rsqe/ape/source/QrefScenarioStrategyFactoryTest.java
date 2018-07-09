package com.bt.rsqe.ape.source;

import com.bt.rsqe.ape.dto.ApeQrefRequestDTO;
import com.bt.rsqe.ape.dto.AsIsAsset;
import com.bt.rsqe.productinstancemerge.ChangeType;
import org.hamcrest.core.Is;
import org.junit.Test;

import static org.junit.Assert.*;

public class QrefScenarioStrategyFactoryTest {
    @Test
    public void shouldGetMultipleProvideQuoteStrategyIfInProvideJourney() {
        ApeQrefRequestDTO apeQrefRequestDTO = new ApeQrefRequestDTO(null, null, null, null, null, null, null, ApeQrefRequestDTO.ProcessType.PROVIDE, null, null, null, AsIsAsset.NIL, null, ChangeType.ADD, null,"1234", "5678");
        assertThat(QrefScenarioStrategyFactory.getScenarioStrategy(apeQrefRequestDTO, null,null), Is.is(MultipleProvideQuoteStrategy.class));
    }

    @Test
    public void shouldGetbulkModifyQuoteStrategyIfInModifyJourney() {
        ApeQrefRequestDTO apeQrefRequestDTO = new ApeQrefRequestDTO(null, null, null, null, null, null, null, ApeQrefRequestDTO.ProcessType.MODIFY, null, null, null, AsIsAsset.NIL, null, ChangeType.UPDATE, null,"1234", "5678");
        assertThat(QrefScenarioStrategyFactory.getScenarioStrategy(apeQrefRequestDTO, null,null), Is.is(BulkModifyQuoteStrategy.class));
    }

    @Test
    public void shouldGetProvideQuoteForGlobalPricingStrategyInProvideJourney() {
        ApeQrefRequestDTO apeQrefRequestDTO = new ApeQrefRequestDTO(null, null, null, null, null, null, null, ApeQrefRequestDTO.ProcessType.PROVIDE, null, null, null, AsIsAsset.NIL, null, ChangeType.ADD, "MBP Access","1234", "5678");
        assertThat(QrefScenarioStrategyFactory.getScenarioStrategy(apeQrefRequestDTO, null,null), Is.is(ProvideQuoteForGlobalPricingStrategy.class));
    }
}
