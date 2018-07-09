package com.bt.rsqe.ape.config;

/**
 * Created by 605783162 on 28/08/2015.
 */
public interface SupplierTimeoutSchedulerConfig {

    String getEnable();
    String getDelayInMin();

    String ENABLE = "timeout-service-enabled";
    String DELAY_IN_MIN = "timeout-service-delay";
}
