package com.bt.rsqe.ape.config;

/**
 * Created by 605783162 on 21/08/2015.
 */
public interface CountryApplicabilityConfig {
    String getEnable();
    String getDelayInHr();
    String getInitialDelayInHr();

    String ENABLE = "applicability-check-enabled";
    String DELAY_IN_HR = "applicability-check-delay";
    String INITIAL_DELAY_IN_HR = "applicability-check-initial-delay";
}
