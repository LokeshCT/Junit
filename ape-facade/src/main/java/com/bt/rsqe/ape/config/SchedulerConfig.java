package com.bt.rsqe.ape.config;

public interface SchedulerConfig {
    CountryApplicabilityConfig getCountryApplicabilityConfig(String id);
    SupplierTimeoutSchedulerConfig getSupplierTimeoutConfig(String id);
    RequestCompletionSchedulerConfig getRequestCompletionConfig(String id);
    AvailabilityCheckSchedulerConfig getAvailabilityCheckSchedulerConfig(String id);
    SACRequestReSubmitConfig getSACRequestReSubmitConfig(String id);
    SupplierProductAvailabilityRequestRetrySchedulerConfig getAvailabilityRetrySchedulerConfig(String id);
}
