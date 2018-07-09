package com.bt.rsqe.ape.config;

/**
 * Created by 605783162 on 14/08/2015.
 */
public interface TimeoutConfig {
    int getValue();
    
    String COUNTRY_APPLICABILITY = "applicability-timeout";
    String PRODUCT_LIST = "product-list-timeout";
    String AVAILABILITY_AUTO = "availability-auto-timeout";
    String AVAILABILITY_MANUAL = "availability-manual-timeout";
    String SAC_RECORDS_VISBILE_MONTHS = "visible-months";
}
