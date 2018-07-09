package com.bt.rsqe.ape.config;

/**
 * Created by 605875089 on 21/02/2016.
 */
public interface SupplierProductAvailabilityRequestRetrySchedulerConfig {
    String getDelayInMin();
    String getEnable();
    String getInterval();
    String ENABLE = "availability-request-retry-enabled";
    String DELAY_IN_MIN = "availability-request-retry-delay";
    String INTERVAL = "availability-request-retry-interval";
}
