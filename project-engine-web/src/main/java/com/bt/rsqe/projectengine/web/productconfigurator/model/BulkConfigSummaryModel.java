package com.bt.rsqe.projectengine.web.productconfigurator.model;

public class BulkConfigSummaryModel {

    private String modelName;
    private BulkConfigAttributeList attributeList;

    public BulkConfigSummaryModel(String modelName) {
        this(modelName, new BulkConfigAttributeList());
    }

    public BulkConfigSummaryModel(String modelName, BulkConfigAttributeList attributeList) {
        setModelName(modelName);
        setAttributeList(attributeList);
    }

    private void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public BulkConfigAttributeList getAttributeList() {
        return attributeList;
    }

    private void setAttributeList(BulkConfigAttributeList attributeList) {
        this.attributeList = attributeList;
    }
}
