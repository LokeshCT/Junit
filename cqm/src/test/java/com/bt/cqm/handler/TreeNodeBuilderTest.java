package com.bt.cqm.handler;

import com.bt.commons.configuration.ConfigurationException;
import com.bt.cqm.dto.TreeNode;
import com.bt.cqm.web.config.TreeNodeConfig;
import com.bt.cqm.web.config.UIConfig;
import com.bt.rsqe.configuration.ConfigurationProvider;
import org.hamcrest.core.Is;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class TreeNodeBuilderTest {

    @Test
    public void shouldBuildCustomerNode() {

        UIConfig uiConfig = ConfigurationProvider.provide(UIConfig.class, "web-ui-config");
        TreeNodeConfig customerTreeNodeConfig = uiConfig.getTabConfig(UIConfig.CUSTOMER_TAB_ID).getTreeNodeConfig();

        TreeNode treeNode = new TreeNodeBuilder(customerTreeNodeConfig).build();
        final int lastRecordedChildCountForCustomer = 5;
        final int lastRecordedChildCountForBilling = 2;
        final int firstNodeIndex = 1;

        Boolean isBillingActionExist =false;
        Boolean isPartnerActionExist =false;
        Boolean isVpnActionExist =false;


        assertThat(treeNode.getId(), Is.is("customerRootNode") );
        assert (treeNode.getChildren().size() >= lastRecordedChildCountForCustomer);

        TreeNode firstNode = treeNode.getChildren().get(firstNodeIndex);
        assert(firstNode!=null);

        List<TreeNode> childNodes =treeNode.getChildren();

        for(TreeNode aChild: childNodes){
            String id = aChild.getId();
            if("billingDetails".equals(id)){
                isBillingActionExist =true;
                assert (aChild.getChildren().size()>= lastRecordedChildCountForBilling);
                assertThat(aChild.getChildren().get(0).getStatus(), Is.is(TreeNode.TreeNodeStatus.NOT_APPLICABLE));
            }else if("partnerDetails".equals(id)){
                isPartnerActionExist = true;
            }else if("vpnDetails".equals(id)){
                isVpnActionExist = true;
            }
        }


        assert (isBillingActionExist);
        assert (isPartnerActionExist);
        assert (isVpnActionExist);
    }


    @Test(expected = ConfigurationException.class)
    public void shouldThrowExceptionForInvalidConfig() {
        ConfigurationProvider.provide(UIConfig.class, "invalid-ui-configuration");
    }
}
