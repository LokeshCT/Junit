package com.bt.cqm.handler;

import com.bt.cqm.dto.TreeNode;
import com.bt.cqm.dto.UserType;
import com.bt.rsqe.utils.AssertObject;
import com.google.gson.JsonObject;

import java.util.Set;

public class Tab {
    private String id;
    private String label;
    private String uri;
    private TreeNode treeNode;
    private Set<Long> roles;
    private String userType;

    public Tab(String id, String label,String uri, TreeNode treeNode, Set<Long> roles, String userType) {
        this.id = id;
        this.label = label;
        this.uri = uri;
        this.treeNode = treeNode;
        this.roles = roles;
        this.userType = userType;
    }

    public JsonObject asJson(Long roleId, UserType userType, boolean hasSuperUser) {
        JsonObject jsonObject = null;
        if (!AssertObject.anyEmpty(roleId, userType)) {
            if (AssertObject.anyEmpty(this.userType) || userType.getName().equalsIgnoreCase(this.userType)) {
                if (roles == null || roles.contains(roleId) || showUserManagementTab(hasSuperUser)) {
                    jsonObject = new JsonObject();
                    jsonObject.addProperty("id", id);
                    jsonObject.addProperty("label", label);

                    if (treeNode != null) {
                        if (treeNode.getChildren() != null && treeNode.getChildren().size() > 0) {
                            jsonObject.addProperty("uri", treeNode.getChildren().get(0).getUri());
                        }

                        JsonObject jsonObj = treeNode.asJson(roleId, userType);
                        if (jsonObj != null) {
                            jsonObject.add("treeNode", jsonObj);
                        }
                    }
                }
            }
        }
        return jsonObject;
    }

    public JsonObject asJson() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("id", id);
        jsonObject.addProperty("label", label);
        jsonObject.addProperty("uri",uri);

        if (treeNode != null) {
            if (treeNode.getChildren() != null && treeNode.getChildren().size() > 0) {
                jsonObject.addProperty("uri", treeNode.getChildren().get(0).getUri());
            }

            JsonObject jsonObj = treeNode.asJson();
            if (jsonObj != null) {
                jsonObject.add("treeNode", jsonObj);
            }
        }
        return jsonObject;
    }

    public String getId() {
        return id;
    }

    public Set<Long> getRoles() {
        return roles;
    }

    public TreeNode getTreeNode() {
        return treeNode;
    }

    private boolean showUserManagementTab(boolean isSuperUser) {
        if (isSuperUser && "UserManagement".equals(id)) {
            return true;
        } else {
            return false;
        }
    }
}
