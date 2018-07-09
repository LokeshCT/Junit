package com.bt.rsqe.ape.config;

/**
 * Created by 605783162 on 28/08/2015.
 */
public interface RequestCompletionSchedulerConfig {

    String getEnable();
    String getDelayInMin();

    String ENABLE = "request-completion-service-enabled";
    String DELAY_IN_MIN = "request-completion-service-delay";
}
