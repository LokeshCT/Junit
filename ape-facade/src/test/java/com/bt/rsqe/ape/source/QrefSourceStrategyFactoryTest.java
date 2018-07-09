package com.bt.rsqe.ape.source;

import com.bt.rsqe.ape.dto.ApeQrefRequestDTO;
import com.bt.rsqe.ape.dto.AsIsAsset;
import com.bt.rsqe.domain.product.ProductOffering;
import com.bt.rsqe.productinstancemerge.ChangeType;
import com.google.common.collect.Lists;
import org.hamcrest.core.Is;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static com.google.common.collect.Lists.*;
import static org.junit.Assert.*;

public class QrefSourceStrategyFactoryTest {
    private QrefSourceStrategyFactory sourceStrategyFactory;

    @Before
    public void setup() {
        sourceStrategyFactory = new QrefSourceStrategyFactory(null, null);
    }

    @Test
    public void shouldGetApeStrategyForARequestContainingNoAPE_FLAG() throws Exception {
        ApeQrefRequestDTO requestDTO = new ApeQrefRequestDTO(null, null, null, null, null, Lists.<ApeQrefRequestDTO.AssetAttribute>newArrayList(), null, null, null, null, null, AsIsAsset.NIL, null, ChangeType.ADD, null,"1234", "5678");
        assertThat(sourceStrategyFactory.getSourceStrategy(requestDTO), Is.is(ApeQrefSourceStrategy.class));
    }

    @Test
    public void shouldGetApeStrategyForARequestContainingAPE_FLAGasYes() throws Exception {
        List<ApeQrefRequestDTO.AssetAttribute> attributes = newArrayList(new ApeQrefRequestDTO.AssetAttribute(ProductOffering.APE_FLAG, "Yes"));
        ApeQrefRequestDTO requestDTO = new ApeQrefRequestDTO(null, null, null, null, null, attributes, null, null, null, null, null, AsIsAsset.NIL, null, ChangeType.ADD, null,"1234", "5678");
        assertThat(sourceStrategyFactory.getSourceStrategy(requestDTO), Is.is(ApeQrefSourceStrategy.class));
    }

    @Test
    public void shouldGetSimulatedApeStrategyForARequestContainingAPE_FLAGasNo() throws Exception {
        List<ApeQrefRequestDTO.AssetAttribute> attributes = newArrayList(new ApeQrefRequestDTO.AssetAttribute(ProductOffering.APE_FLAG, "No"));
        ApeQrefRequestDTO requestDTO = new ApeQrefRequestDTO(null, null, null, null, null, attributes, null, null, null, null, null, AsIsAsset.NIL, null, ChangeType.ADD, null,"1234", "5678");
        assertThat(sourceStrategyFactory.getSourceStrategy(requestDTO), Is.is(SimulatedApeQrefSourceStrategy.class));
    }
}
