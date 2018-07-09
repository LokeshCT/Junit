package com.bt.rsqe.ape.config;

/**
 * Created with IntelliJ IDEA.
 * User: 608026723
 * Date: 18/10/15
 * Time: 15:54
 * To change this template use File | Settings | File Templates.
 */
public interface SACRequestReSubmitConfig {
    String getInterval();
    String getEnable();
    String getRescheduleOnRestart();
    String RESUBMIT_INTERVAL = "re-submit-request";
}
