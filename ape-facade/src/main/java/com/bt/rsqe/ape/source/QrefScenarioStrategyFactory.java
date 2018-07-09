package com.bt.rsqe.ape.source;

import com.bt.rsqe.ape.dto.ApeQrefRequestDTO;
import com.bt.rsqe.customerrecord.CustomerResource;

public class QrefScenarioStrategyFactory {
    private QrefScenarioStrategyFactory() {}

    public static QrefScenarioStrategy getScenarioStrategy(ApeQrefRequestDTO request, String syncUri,CustomerResource customerResource ) {
        if(ApeQrefRequestDTO.ProcessType.PROVIDE.equals(request.getProcessType())) {
            if("MBP Access".equals(request.getAccessMethodType())){
                return new ProvideQuoteForGlobalPricingStrategy(request, syncUri, customerResource);
            }
            return new MultipleProvideQuoteStrategy(request, syncUri);
        } else {
            if ("MBP Access".equals(request.getAccessMethodType())) {
                return new ModifyQuoteForGlobalPricingStrategy(request, syncUri, customerResource);
            }
            return new BulkModifyQuoteStrategy(request, syncUri);
        }
    }
}
