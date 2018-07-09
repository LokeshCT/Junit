package com.bt.rsqe.projectengine.web.quoteoption.bcmsheet;

public enum ServiceProductSheetStaticColumn {

    ORDER_TYPE("Order Type",0,"orderType",1,true, true),
    SERVICE_TYPE("Service Type",1,"serviceType",1,true, true),
    //ATTRIBUTES("",)
    PRODUCT_INSTANCE("Product Instance", 2, "productInstance",1,true, true),
    PRODUCT_INSTANCE_VERSION("Product Version", 3, "productInstanceVersion",0,true, true),
    ONE_TIME_EUP("One Time EUP",4,"oneTimeEUP",0,true, true),
    ONE_TIME_PRICE_LINE("One Time Price Line", 5, "oneTimePriceLine",1,true, true),
    ONE_TIME_PTP("One Time PTP",6,"oneTimePTP",0,true, true),
    ONE_TIME_DISCOUNT("One Time Discount %",7,"oneTimeDiscount",0,false, true),
    MONTHLY_EUP("Monthly EUP",8,"monthlyEUP",0,true, true),
    MONTHLY_PRICE_LINE("Monthly Price Line", 9, "monthlyTimePriceLine",1,true, true),
    MONTHLY_PTP("Monthly PTP",10,"monthlyPTP",0,true, true),
    MONTHLY_DISCOUNT("Monthly Discount %",11,"monthlyDiscount",0,false, true),
    NRC("NRC",12,"nrc",0,false, true),
    MRC("MRC",13,"mrc",0,false, true);

    public final String columnName;
    public final int columnIndex;
    public final String retrieveValueFrom;
    public final int dataType;
    public final boolean isReadOnly;
    public boolean visibility;

    private ServiceProductSheetStaticColumn(String columnName, int columnIndex, String retrieveValueFrom, int dataType, boolean isReadOnly, boolean visibility) {
        this.columnName = columnName;
        this.columnIndex = columnIndex;
        this.retrieveValueFrom = retrieveValueFrom;
        this.dataType = dataType;
        this.isReadOnly = isReadOnly;
        this.visibility = visibility;
    }
}
