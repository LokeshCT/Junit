package com.bt.rsqe.projectengine.web.quoteoption.bcmsheet;


public enum PriceColumns {

    //Column index defined here will be overwritten later, hence setting as 0
    DESCRIPTION("Description",0,"description",1,true, true, "LicensePriceColumn"),
    PRODUCT_INSTANCE("Product Instance", 0, "productInstance",1,true, true, "LicensePriceColumn"),
    PRODUCT_INSTANCE_VERSION("Product Version", 0, "productInstanceVersion",0,true, true, "LicensePriceColumn"),
    TOTAL_ONE_TIME_EUP("Total One time EUP",0,"totalOneTimeEUP",0,true, true, "LicensePriceColumn"),
    ONE_TIME_PRICE_LINE("One time Price Line",0,"oneTimePriceLine",1,true, true, "LicensePriceColumn"),
    TOTAL_ONE_TIME_PTP("Total One time PTP",0,"totalOneTimePTP",0,true, true, "LicensePriceColumn"),
    ONE_TIME_DISCOUNT("One time Discount %",0,"oneTimeDiscount",0,false, true, "LicensePriceColumn"),
    TOTAL_MONTHLY_EUP("Total Monthly EUP",0,"totalMonthlyEUP",0,true, true, "LicensePriceColumn"),
    MONTHLY_PRICE_LINE("Monthly Price Line",0,"monthlyPriceLine",1,true, true, "LicensePriceColumn"),
    TOTAL_MONTHLY_PTP("Total Monthly PTP",0,"totalMonthlyPTP",0,true, true, "LicensePriceColumn"),
    MONTHLY_DISCOUNT("Monthly Discount %",0,"monthlyDiscount",0,false, true, "LicensePriceColumn"),
    NRC("NRC",0,"nrc",0,true, true, "LicensePriceColumn"),
    MRC("MRC",0,"mrc",0,true, true, "LicensePriceColumn"),
    DISCOUNTED_MRC("Discounted MRC", 0, "discountedMRC", 0, true, true, "LicenseCostColumn"),
    VENDOR_DISCOUNT_REF("Vendor Discount Ref", 0, "vendorDiscountRef", 1, true, true, "LicenseCostColumn");

    public final String columnName;
    public final int columnIndex;
    public final String retrieveValueFrom;
    public final int dataType;
    public boolean isReadOnly;
    public boolean visibility;
    public String columnGroup;


    private PriceColumns(String columnName, int columnIndex, String retrieveValueFrom, int dataType, boolean isReadOnly, boolean visibility, String columnGroup) {
        this.columnName = columnName;
        this.columnIndex = columnIndex;
        this.retrieveValueFrom = retrieveValueFrom;
        this.dataType = dataType;
        this.isReadOnly = isReadOnly;
        this.visibility = visibility;
        this.columnGroup = columnGroup;
    }

}
