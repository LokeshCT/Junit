package com.bt.rsqe.projectengine.web.quoteoption.bcmsheet;

public enum ProductDetailsColumn {

    PRIMARY_IDENTIFIER("PrimaryIdentifier",-1, "ProductIdentifier.ProductId", false, "product"),
    VERSION_NUMBER("Version Number",-1, "ProductIdentifier.VersionNumber", false, "product"),
    PRODUCT_INSTANCE_ID("productInstanceID",-1, "ProductInstanceId", false, "product"),
    CHILD_RELATIONSHIP_NAME("Child relationship",-1, "RelationshipName", true, "product");

    public String columnName;
    public int columnIndex;
    public String retrieveValueFrom;
    public boolean visible;
    public String type;

    ProductDetailsColumn(String columnName, int columnIndex, String retrieveValueFrom, boolean visible, String type) {
        this.columnName = columnName;
        this.columnIndex = columnIndex;
        this.retrieveValueFrom = retrieveValueFrom;
        this.visible = visible;
        this.type = type;
    }
}
