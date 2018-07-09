package com.bt.cqm.web.config;

public interface UIConfig {
    TabConfig[] getTabConfig();
    TabConfig getTabConfig(String tabId);

    String CUSTOMER_TAB_ID = "Customer";
    String SITE_TAB_ID = "Site";
    String QUOTE_TAB_ID = "Quote";
    String ORDER_TAB_ID = "Order";
    String ACTIVITY_TAB_ID = "Activity";
    String AUDIT_TRAIL_TAB_ID="audit";
    String USER_MANAGEMENT_TAB_ID="UserManagement";
}
