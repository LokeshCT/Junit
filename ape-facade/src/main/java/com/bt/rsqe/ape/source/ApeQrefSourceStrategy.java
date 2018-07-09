package com.bt.rsqe.ape.source;

import com.bt.rsqe.ape.MultisiteResponse;
import com.bt.rsqe.ape.client.APEClient;
import com.bt.rsqe.ape.dto.ApeQrefRequestDTO;
import com.bt.rsqe.ape.repository.APEQrefRepository;
import com.bt.rsqe.ape.repository.entities.ApeRequestEntity;
import com.bt.rsqe.customerrecord.CustomerResource;
import com.bt.rsqe.domain.QrefRequestStatus;
import com.google.common.base.Strings;

public class ApeQrefSourceStrategy extends QrefSourceStrategy {
    private APEClient apeClient;

    public ApeQrefSourceStrategy(ApeQrefRequestDTO request, APEQrefRepository apeQrefRepository, APEClient apeClient) {
        super(request, apeQrefRepository);
        this.apeClient = apeClient;
    }

    @Override
    public QrefRequestStatus requestQrefs(final String syncUri, final String uniqueId, final CustomerResource customerResource) {
        QrefScenarioStrategy qrefScenarioStrategy = QrefScenarioStrategyFactory.getScenarioStrategy(getRequest(), syncUri, customerResource);
        MultisiteResponse multisiteResponse = qrefScenarioStrategy.getMultiSiteResponse(apeClient);

        RequestId requestId = RequestId.newInstance(multisiteResponse.getRequestId());
        String responseErrors = getErrorsFromProvideQuoteResponse(multisiteResponse);

        ApeRequestEntity apeRequestEntity = ApeRequestEntity.toEntity(requestId.value(), uniqueId, getRequest());

        if(!Strings.isNullOrEmpty(responseErrors)) {
            apeRequestEntity.setStatus(QrefRequestStatus.Status.ERROR);
            apeRequestEntity.setErrorMessage("APE Request failed: " + responseErrors);
        }

        getApeQrefRepository().save(apeRequestEntity);

        return apeRequestEntity.toQrefRequestStatusDto();
    }

    private String getErrorsFromProvideQuoteResponse(MultisiteResponse provideQuoteResponse) {
        if(!Strings.isNullOrEmpty(provideQuoteResponse.getComments())
                && provideQuoteResponse.getComments().contains("[Failure]")) {
            return provideQuoteResponse.getComments();
        }

        return null;
    }
}
