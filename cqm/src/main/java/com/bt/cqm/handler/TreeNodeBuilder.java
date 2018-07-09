package com.bt.cqm.handler;

import com.bt.commons.configuration.ConfigurationException;
import com.bt.cqm.dto.TreeNode;
import com.bt.cqm.web.config.ChildNodesConfig;
import com.bt.cqm.web.config.TreeNodeConfig;

import java.util.HashSet;
import java.util.Set;

import static com.bt.rsqe.utils.AssertObject.isNotNull;

public class TreeNodeBuilder {

    private TreeNodeConfig treeNodeConfig;

    public TreeNodeBuilder(TreeNodeConfig treeNodeConfig) {
        this.treeNodeConfig = treeNodeConfig;
    }

    public TreeNode build() {
        return toTreeNode(treeNodeConfig);
    }

    private TreeNode toTreeNode(TreeNodeConfig treeNodeConfig) {
        ChildNodesConfig childNodesConfig = null;
        TreeNode treeNode = new TreeNode(treeNodeConfig.getId(), treeNodeConfig.getLabel(), getUri(treeNodeConfig),getRoles(treeNodeConfig),getUserType(treeNodeConfig));
        try {
           childNodesConfig = treeNodeConfig.getChildNodes();
        } catch (ConfigurationException ex){
            //This can happen as a tree node might not have child nodes.
            //We can stop this by adding empty ChildNodes element for each TreeNode. But xml looks hard to understand.
        }

        if( isNotNull(childNodesConfig)) {
            for (int i = 0; i < childNodesConfig.getTreeNodes().length; i++) {
                treeNode.addChildNodes(
                    toTreeNode(childNodesConfig.getTreeNodes()[i])
                );
            }
        }

        return treeNode;
    }

    private String getUri(TreeNodeConfig config) {
         try {
           return config.getUri();
        } catch (ConfigurationException ex){
             return null;
         }
    }

    private Set<Long> getRoles(TreeNodeConfig config){
        String roles = null;
        try{
            roles =config.getRoles();
        }catch(Exception ex){}

        Set<Long> roleIds=null;
        int i=0;
        if(roles!=null){
            String[] strRoleIds=roles.split(",");
            for(String roleId: strRoleIds){
                try{
                    if(roleIds==null)
                    {
                        roleIds = new HashSet<Long>();
                    }
                    roleIds.add(Long.parseLong(roleId));
                }catch(Exception ex) {
                    //Ignore Exception.
                }
            }
        }
        return roleIds;
    }

    private String getUserType(TreeNodeConfig config){
        String userType = null;

        try{
           userType = config.getUserType();
        }catch (Exception ex){

        }
        return userType;
    }

}
