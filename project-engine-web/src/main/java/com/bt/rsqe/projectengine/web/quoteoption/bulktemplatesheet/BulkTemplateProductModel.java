package com.bt.rsqe.projectengine.web.quoteoption.bulktemplatesheet;


public class BulkTemplateProductModel {

    private String productId;
    private String productName;
    private String relationshipType;

    public BulkTemplateProductModel(String productId, String productName, String relationshipType) {
        this.productId = productId;
        this.productName = productName;
        this.relationshipType = relationshipType;
    }

    public String getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public String getRelationType() {
        return relationshipType;
    }
}
