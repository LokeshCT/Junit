package com.bt.rsqe.projectengine.web.quoteoption.bcmsheet;


public enum ProductPerSiteStaticColumn {

    SITE_ID("Site Id",0, "pps.siteID" , 2, true, true),
    SITE_NAME("Site",1, "pps.siteName",  1, true, true),
    COUNTRY("Country",2, "pps.country", 1, true, true),
    CITY("City",3, "pps.city",  1, true, true);

    public String columnName;
    public int columnIndex;
    public String retrieveValueFrom;
    public int dataType;
    public boolean isReadOnly;
    public boolean visibility;


    ProductPerSiteStaticColumn(String columnName, int columnIndex, String retrieveValueFrom, int dataType, boolean isReadOnly, boolean visibility) {
        this.columnName = columnName;
        this.columnIndex = columnIndex;
        this.retrieveValueFrom = retrieveValueFrom;
        this.dataType = dataType;
        this.isReadOnly=isReadOnly;
        this.visibility = visibility;
    }


}
