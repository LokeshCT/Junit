package com.bt.rsqe.projectengine.web.quoteoption.bcmsheet;

public enum SiteManagementStaticColumns {

    SITE_STATUS("Site Status",0,"siteStatus",1,true, true),
    SITE_ID("Site ID",1,"siteID",2,true, true),
    SITE_NAME("Site",2,"siteName",1,true, true),
    COUNTRY("Country",3,"country",1,true, true),
    CITY("City",4,"city",1,true, true),
    ORDER_TYPE("Order Type",5,"orderType",1,true, true),
    SERVICE_TYPE("Service Type",6,"serviceType",1,true, true),
    //ATTRIBUTES("",)
    PRODUCT_INSTANCE("Product Instance", 7, "productInstance",1,true, true),
    PRODUCT_INSTANCE_VERSION("Product Version", 8, "productInstanceVersion",0,true, true),
    ONE_TIME_EUP("One Time EUP",9,"oneTimeEUP",0,true, true),
    ONE_TIME_PRICE_LINE("One Time Price Line", 10, "oneTimePriceLine",1,true, true),
    ONE_TIME_PTP("One Time PTP",11,"oneTimePTP",0,true, true),
    ONE_TIME_DISCOUNT("One Time Discount %",12,"oneTimeDiscount",0,false, true),
    MONTHLY_EUP("Monthly EUP",13,"monthlyEUP",0,true, true),
    MONTHLY_PRICE_LINE("Monthly Price Line", 14, "monthlyTimePriceLine",1,true, true),
    MONTHLY_PTP("Monthly PTP",15,"monthlyPTP",0,true, true),
    MONTHLY_DISCOUNT("Monthly Discount %",16,"monthlyDiscount",0,false, true),
    NRC("Site Management NRC",17,"nrc",0,false, true),
    MRC("Site Management MRC",18,"mrc",0,false, true);


    public final String columnName;
    public final int columnIndex;
    public final String retrieveValueFrom;
    public final int dataType;
    public final boolean isReadOnly;
    public boolean visibility;

    private SiteManagementStaticColumns(String columnName, int columnIndex, String retrieveValueFrom, int dataType, boolean isReadOnly, boolean visibility) {
        this.columnName = columnName;
        this.columnIndex = columnIndex;
        this.retrieveValueFrom = retrieveValueFrom;
        this.dataType = dataType;
        this.isReadOnly = isReadOnly;
        this.visibility = visibility;
    }

}
