package com.bt.cqm.dto;

import com.bt.rsqe.utils.AssertObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.apache.commons.lang.StringUtils;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;
import java.util.Set;

import static com.google.common.collect.Lists.*;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class TreeNode {

    private String id;
    private String label;
    private String uri;
    private TreeNodeStatus status;
    private List<TreeNode> children = newArrayList();
    private Set<Long> roles;
    private String userType;

    public TreeNode() {
        /*for JAXB */
    }

    public TreeNode(String id, String label, String uri, Set<Long> roles, String userType) {
        this(id, label, uri, TreeNodeStatus.NOT_APPLICABLE, roles, userType);
    }

    private TreeNode(String id, String label, String uri, TreeNodeStatus status, Set<Long> roles, String userType) {
        this.id = id;
        this.label = label;
        this.uri = uri;
        this.status = status;
        this.roles = roles;
        this.userType = userType;
    }

    public TreeNode addChildNodes(TreeNode... nodes) {
        children.addAll(newArrayList(nodes));
        return this;
    }

    public String getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    public String getUri() {
        return uri;
    }

    public TreeNodeStatus getStatus() {
        return status;
    }

    public List<TreeNode> getChildren() {
        return children;
    }

    public JsonObject asJson(Long roleId, UserType userType) {

        JsonObject jsonObject = null;
        if (!AssertObject.anyEmpty(roleId, userType)) {
            if (AssertObject.anyEmpty(this.userType) || userType.getName().equalsIgnoreCase(this.userType)) {
                if (roles == null || roles.contains(roleId)) {
                    jsonObject = new JsonObject();

                    jsonObject.addProperty("id", id);
                    jsonObject.addProperty("label", label);
                    jsonObject.addProperty("status", status.toString());
                    jsonObject.addProperty("uri", uri);

                    JsonArray jsonArray = new JsonArray();
                    for (TreeNode child : this.children) {
                        JsonObject jsonObj = child.asJson(roleId, userType);
                        if (jsonObj != null) {
                            jsonArray.add(child.asJson(roleId, userType));
                        }
                    }
                    jsonObject.add("children", jsonArray);
                }
            }
        }
        return jsonObject;
    }

    public JsonObject asJson() {

        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("id", id);
        jsonObject.addProperty("label", label);
        jsonObject.addProperty("status", status.toString());
        jsonObject.addProperty("uri", uri);

        JsonArray jsonArray = new JsonArray();
        for (TreeNode child : this.children) {
            JsonObject jsonObj = child.asJson();
            if (jsonObj != null) {
                jsonArray.add(child.asJson());
            }
        }
        jsonObject.add("children", jsonArray);

        return jsonObject;
    }

    public TreeNode(String id, String label) {
        this(id, label, StringUtils.EMPTY, null, null);
    }

    public static enum TreeNodeStatus {
        NOT_APPLICABLE("Not Applicable"),
        NOT_CONFIGURED("Not Configured"),
        VALID("Configured"),
        ERROR("Configured with Error");

        private String status;

        private TreeNodeStatus(String status) {
            this.status = status;
        }

        @Override
        public String toString() {
            return status;
        }
    }
}
