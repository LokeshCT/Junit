package com.bt.rsqe.projectengine.web.quoteoption.bcmsheet;


public enum SpecialBidInfoStaticColumn {

    SITE_ID( "Site Id",  0, "sbid.siteID", 2, true, true),
    LINE_ITEM_ID( "Line Id",  1, "sbid.lineId", 1, true, true),
    NON_STD_CONF_CATEGORY( "Description",  2, "sbid.nonStdConfigurationCategory", 1, true, true),
    PRODUCT_INSTANCE("Product Instance", 3, "sbid.productInstance", 1, true, true),
    PRODUCT_INSTANCE_VERSION("Product Version", 4, "sbid.productInstanceVersion", 0, true, true),
    EUP_CURRENCY ( "EUP Currency",  5,  "sbid.eupCurrency", 1, true, true),
    ONE_TIME_PRICE_LINE("One Time Price Line", 6, "sbid.priceLineOneTime", 1, true, true),
    EUP_ONE_TIME( "EUP  - One Time",  7,  "sbid.eupOneTime", 0, true, true),
    DISCOUNT_ONE_TIME ( "Discount % - One Time",  8,  "sbid.discountOneTIme", 0, false, true),
    MONTHLY_PRICE_LINE("Monthly Price Line", 9, "sbid.priceLineMonthly", 1, true, true),
    EUP_MONTHLY ( "EUP - Monthly",  10,  "sbid.eupMonthly", 0, true, true),
    DISCOUNT_MONTHLY ( "Discount % - Monthly",  11,  "sbid.discountMonthly", 0, false, true),
    COST_ONE_TIME ( "Cost One Time",  12,  "sbid.costOneTime", 0, true, true),
    COST_MONTHLY ( "Cost Monthly",  13,  "sbid.costMonthly", 0, true, true),
    COST_CURRENCY ( "Cost Currency",  14,  "sbid.costCurrency", 1, true, true),
    BILL_DESC ( "Bill Description",  15,  "sbid.billDescription", 1, true, true),
    PTP_ONE_TIME ( "PTP - One time",  16,  "sbid.ptpOneTime", 0, true, true),
    PTP_MONTHLY ( "PTP - Monthly",  17,  "sbid.ptpMonthly", 0, true, true),
    NON_STD_PRODUCT_TYPE ( "NON STANDARD PRODUCT TYPE",  18,  "sbid.nonStdProductType", 1, true, true),
    WELL_KNOWN_NON_STD ( "WELL KNOWN NON STANDARDS",  19,  "sbid.wellKnownNonStds", 1, true, true),
    TPE_REF ( "TPE Ref",  20,  "sbid.tpeRef", 1, true, true),
    BRANCH_CENTRAL ( "Branch/Central",  21,  "sbid.branchCentral", 1, true, true),
    EUP_DEINSTALL ( "EUP - Deinstall",  22,  "sbid.eupDeinstall", 0, true, true),
    PTP_DEINSTALL( "PTP - Deinstall",  23,  "sbid.ptpDeinstall", 0, true, true),
    PRICING_STATUS("Pricing Status", 24, "sbid.pricingStatus", 1, true, true);


    public String columnName;
    public int columnIndex;
    public String retrieveValueFrom;
    public int dataType;
    public boolean isReadOnly;
    public boolean visibility;


    SpecialBidInfoStaticColumn(String columnName, int columnIndex, String retrieveValueFrom, int dataType, boolean isReadOnly, boolean visibility) {
        this.columnName = columnName;
        this.columnIndex = columnIndex;
        this.retrieveValueFrom = retrieveValueFrom;
        this.dataType = dataType;
        this.isReadOnly=isReadOnly;
        this.visibility = visibility;
    }


}
