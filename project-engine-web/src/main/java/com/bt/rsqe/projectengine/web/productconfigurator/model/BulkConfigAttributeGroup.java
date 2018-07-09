package com.bt.rsqe.projectengine.web.productconfigurator.model;

public class BulkConfigAttributeGroup {

    private String attributeGroupName;
    private GroupType attributeGroupType;
    private BulkConfigAttributeList attributeList;

    public BulkConfigAttributeGroup(String attributeGroupName) {
        this(attributeGroupName, GroupType.BASE_CONFIG);
    }

    public BulkConfigAttributeGroup(String attributeGroupName, GroupType attributeGroupType) {
        this(attributeGroupName, attributeGroupType, new BulkConfigAttributeList());
    }

    public BulkConfigAttributeGroup(String attributeGroupName, GroupType attributeGroupType, BulkConfigAttributeList attributeList) {

        setAttributeGroupName(attributeGroupName);
        setAttributeGroupType(attributeGroupType);
        setAttributeList(attributeList);
    }

    public String getAttributeGroupName() {
        return attributeGroupName;
    }

    private void setAttributeGroupName(String attributeGroupName) {
        this.attributeGroupName = attributeGroupName;
    }

    public GroupType getAttributeGroupType() {
        return attributeGroupType;
    }

    private void setAttributeGroupType(GroupType attributeGroupType) {
        this.attributeGroupType = attributeGroupType;
    }

    public BulkConfigAttributeList getAttributeList() {
        return attributeList;
    }

    private void setAttributeList(BulkConfigAttributeList attributeList) {
        this.attributeList = attributeList;
    }

    public enum GroupType {
        STENCIL,
        BASE_CONFIG,
        PARENT_CHILD,
        RELATED_TO,
        RELATIONSHIP_GROUP
    }
}
