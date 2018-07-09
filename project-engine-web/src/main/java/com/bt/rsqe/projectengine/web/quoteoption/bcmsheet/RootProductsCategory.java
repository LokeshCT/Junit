package com.bt.rsqe.projectengine.web.quoteoption.bcmsheet;


public enum RootProductsCategory {

    CONNECT_ACCELERATION(1,"Connect Acceleration","pps.Connect Acceleration",1,true, true),
    CONNECT_ASSESSMENT(2,"Connect Assessment","pps.Connect Assessment",1,true, true),
    CONNECT_INTELLIGENCE (3,"Connect Intelligence","pps.Connect Intelligence",1,true, true),
    CONNECT_OPTIMISATION (4,"Connect Optimisation","pps.Connect Optimisation",1,true, true),
    IP_CONNECT (5,"IP Connect Global","pps.IP Connect Global",1,true, true),
    INTERNET_CONNECT_GLOBAL (6,"Internet Connect Global","pps.Internet Connect Global",1,true, true),
    INTERNET_CONNECT_REACH (7,"Internet Connect Reach","pps.Internet Connect Reach",1,true, true),
    WEB_VPN (8,"Web Vpn","pps.Web Vpn",1,true, true),
    SERVICE_FROM_BT (9,"Service from BT","pps.Service from BT",1,true, true),
    CUSTOM_ACCESS(10,"Custom Access","pps.Custom Access",1,true, true);
    //Any New Product Category will be added Here

    public int columnIndex;
    public String columnName;
    public String retrieveValueFrom;
    public int dataType;
    public boolean isReadOnly;
    public boolean visibility;


    private RootProductsCategory(int columnIndex, String columnName, String retrieveValueFrom, int dataType, boolean readOnly, boolean visibility) {
        this.columnName = columnName;
        this.columnIndex = columnIndex;
        this.retrieveValueFrom = retrieveValueFrom;
        this.dataType = dataType;
        this.isReadOnly = readOnly;
        this.visibility = visibility;
    }


}
