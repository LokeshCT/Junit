package com.bt.rsqe.sqefacade;

public interface SqeIvpnFacadeConfig {
    ServiceEndPointConfig getServiceEndPointConfig(String id);

    String INPROGRESS_ASSETS = "FetchInProgressAssets";
    String USER_QUOTE_STATISTICS = "FetchUserQuoteStatistics";
}
