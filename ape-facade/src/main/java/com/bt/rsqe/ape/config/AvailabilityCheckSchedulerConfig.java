package com.bt.rsqe.ape.config;

/**
 * Created by 605783162 on 28/09/2015.
 */
public interface AvailabilityCheckSchedulerConfig {
    String getDelayInMin();
    String getEnable();
    String DELAY_IN_MIN = "availability-queue-delay";
    String ENABLE = "availability-queue-enabled";
}
