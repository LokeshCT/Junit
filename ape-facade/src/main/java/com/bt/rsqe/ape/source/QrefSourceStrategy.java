package com.bt.rsqe.ape.source;

import com.bt.rsqe.ape.dto.ApeQrefRequestDTO;
import com.bt.rsqe.ape.repository.APEQrefRepository;
import com.bt.rsqe.customerrecord.CustomerResource;
import com.bt.rsqe.domain.QrefRequestStatus;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

public abstract class QrefSourceStrategy {
    private APEQrefRepository apeQrefRepository;
    private ApeQrefRequestDTO request;

    public QrefSourceStrategy(ApeQrefRequestDTO request, APEQrefRepository apeQrefRepository) {
        this.apeQrefRepository = apeQrefRepository;
        this.request = request;
    }

    protected ApeQrefRequestDTO getRequest() {
        return request;
    }

    protected APEQrefRepository getApeQrefRepository() {
        return apeQrefRepository;
    }

    protected String getAttributeValue(final String attributeName) {
        com.google.common.base.Optional<ApeQrefRequestDTO.AssetAttribute> attribute = Iterables.tryFind(getRequest().attributes(), new Predicate<ApeQrefRequestDTO.AssetAttribute>() {
            @Override
            public boolean apply(ApeQrefRequestDTO.AssetAttribute input) {
                return input.getAttributeName().equalsIgnoreCase(attributeName);
            }
        });

        if(attribute.isPresent()) {
            return attribute.get().getAttributeValue();
        } else {
            return null;
        }
    }

    public abstract QrefRequestStatus requestQrefs(final String syncUri, final String uniqueId, final CustomerResource customerResource);
}
