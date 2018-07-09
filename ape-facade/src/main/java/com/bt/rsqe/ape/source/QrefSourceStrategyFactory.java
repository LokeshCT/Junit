package com.bt.rsqe.ape.source;

import com.bt.rsqe.ape.client.APECallbackClient;
import com.bt.rsqe.ape.client.APEClient;
import com.bt.rsqe.ape.dto.ApeQrefRequestDTO;
import com.bt.rsqe.ape.repository.APEQrefRepository;
import com.bt.rsqe.domain.product.ProductOffering;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import javax.annotation.Nullable;

public class QrefSourceStrategyFactory {
    private static final String APE_FLAG_NO = "No";
    private APEQrefRepository apeQrefRepository;
    private APEClient apeClient;

    public QrefSourceStrategyFactory(APEQrefRepository apeQrefRepository, APEClient apeClient) {
        this.apeQrefRepository = apeQrefRepository;
        this.apeClient = apeClient;
    }

    public QrefSourceStrategy getSourceStrategy(ApeQrefRequestDTO request) {
        Optional<ApeQrefRequestDTO.AssetAttribute> simulatedApeFlag = Iterables.tryFind(request.attributes(), new Predicate<ApeQrefRequestDTO.AssetAttribute>() {
            @Override
            public boolean apply(@Nullable ApeQrefRequestDTO.AssetAttribute input) {
                return ProductOffering.APE_FLAG.equals(input.getAttributeName()) && APE_FLAG_NO.equalsIgnoreCase(input.getAttributeValue());
            }
        });

        if(simulatedApeFlag.isPresent()) {
            return new SimulatedApeQrefSourceStrategy(request, apeQrefRepository, new APECallbackClient());
        } else {
            return new ApeQrefSourceStrategy(request, apeQrefRepository, apeClient);
        }
    }
}
