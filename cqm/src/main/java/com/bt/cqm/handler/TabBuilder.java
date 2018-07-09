package com.bt.cqm.handler;

import com.bt.cqm.web.config.TabConfig;
import com.bt.cqm.web.config.TreeNodeConfig;
import com.bt.cqm.web.config.UIConfig;
import com.bt.rsqe.configuration.ConfigurationProvider;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.google.common.collect.Lists.newArrayList;

public class TabBuilder {

    private UIConfig uiConfig;

    public TabBuilder() {
        uiConfig = ConfigurationProvider.provide(UIConfig.class, "web-ui-config");
    }

    public TabBuilder(String uiXmlName) {
        uiConfig = ConfigurationProvider.provide(UIConfig.class, uiXmlName);
    }

    public List<Tab> build() {
        List<Tab> tabs = newArrayList();
        TabConfig[] tabConfigs = uiConfig.getTabConfig();
        for (int i = 0; i < tabConfigs.length; i++) {
            tabs.add(transform(tabConfigs[i]));
        }

        return tabs;
    }

    private Tab transform(TabConfig tabConfig) {
        String roles = null;
        String userType = null;

        try {
            userType = tabConfig.getUserType();
        } catch (Exception ex) {
        }

        try {
            roles = tabConfig.getRoles();
        } catch (Exception ex) {
        }

        Set<Long> roleIds = null;
        int i = 0;
        if (roles != null) {
            String[] strRoleIds = roles.split(",");
            for (String roleId : strRoleIds) {
                try {
                    if (roleIds == null) {
                        roleIds = new HashSet<Long>();
                    }
                    roleIds.add(Long.parseLong(roleId));
                } catch (Exception ex) {
                    //Ignore Exception.
                }
            }
        }

        TreeNodeConfig treeNodeConfig = null;
        try {
            treeNodeConfig = tabConfig.getTreeNodeConfig();
        } catch (Exception ex) {

        }

        TreeNodeBuilder treeNodeBuilder = null;

        if (treeNodeConfig != null) {
            treeNodeBuilder = new TreeNodeBuilder(treeNodeConfig);
        }

        Tab tab = null;

        if (treeNodeBuilder != null) {
            String uri = null;
            try {
                uri = tabConfig.getUri();
            } catch (Exception ex) {
            }
            tab = new Tab(tabConfig.getId(),
                          tabConfig.getLabel(), uri,
                          new TreeNodeBuilder(treeNodeConfig).build(), roleIds, userType);
        } else {
            tab = new Tab(tabConfig.getId(),
                          tabConfig.getLabel(), tabConfig.getUri(),
                          null, roleIds, userType);
        }

        return tab;
    }

}
