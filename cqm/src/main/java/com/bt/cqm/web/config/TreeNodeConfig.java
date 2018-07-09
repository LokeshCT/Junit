package com.bt.cqm.web.config;

public interface TreeNodeConfig {
    String getId();
    String getLabel();
    String getUri();
    String getRoles();
    String getUserType();
    ChildNodesConfig getChildNodes();
}
