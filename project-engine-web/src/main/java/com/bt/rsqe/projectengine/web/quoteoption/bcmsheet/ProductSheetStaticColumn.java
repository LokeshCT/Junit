package com.bt.rsqe.projectengine.web.quoteoption.bcmsheet;

public enum ProductSheetStaticColumn {

    SITE_ID("SiteID",0, "Site.bfgSiteID" , true, "site", 1),
    SITE("Site",1, "Site.name", true, "site", 1),
    COUNTRY("Country",2, "Site.country", true, "site", 1),
    CITY("City",3, "Site.city", true, "site", 1),
    PRODUCT_CATEGORY_NAME("Product Category Name",4, "ProductCategoryName", true, "common", 1),
    ROOT_PRODUCT_ID("Root Product ID",5, "ProductId", false, "common", 1),
    ROOT_PRODUCT_NAME("Root Product Name",6, "ProductName", true, "common", 1),
    LINE_ITEM_ACTION("line item Action",7, "LineItemAction", true, "common", 1),
    LINE_ITEM_ORDER_STATUS("LIne  Item Order Status",8, "LineItemOrderStatus", true, "common", 1),
    CONTRACT_TERM("Contract term",9, "ContractTerm", true, "common", 1),
    PRICE_BOOK_VERSION("Price book version",10, "PriceBook", true, "common", 1),
    PRIMARY_TARIFF_ZONE("Primary Tariff Zone",11, "", true, "common", 1),
    PRICE_DESCRIPTION("Price description",12, "PriceLine.PriceDescription", true, "price", 1),
    VISIBILITY("Visibility",13, "PriceLine.Visibility", true, "price", 1),
    ONE_TIME_PRICE_LINE_ID("One Time Price Line ID",14, "PriceLine.OneTimePriceLineId", false, "price", 1),
    ONE_TIME_EUP_PRICE("One time EUP price",15, "PriceLine.OnetimeEUPPrice", true, "price", 1),
    ONE_TIME_PTP_PRICE("One time PTP price",16, "PriceLine.OneTimePTPPrice", true, "price", 1),
    ONE_TIME_DISCOUNT("One time discount",17, "PriceLine.OneTimeDiscount", true, "price", 0),
    MONTHLY_RECURRING_PRICE_LINE_ID("Monthly Recurring Price Line ID",18, "PriceLine.RecurringPriceLineId", false, "price", 1),
    MONTHLY_RECURRING_EUP_PRICE("Monthly Recurring EUP price",19, "PriceLine.RecurringEUPPrice", true, "price", 1),
    MONTHLY_RECURRING_PTP_PRICE("Monthly Recurring PTP price",20, "PriceLine.RecurringPTPPrice", true, "price", 1),
    MONTHLY_RECURRING_DISCOUNT("Monthly Recurring discount",21, "PriceLine.MonthlyDiscount", true, "price",0);


    public final String columnName;
    public final int columnIndex;
    public final String retrieveValueFrom;
    public boolean visible;
    public final String type;
    public int dataType;


    ProductSheetStaticColumn(String columnName, int columnIndex, String retrieveValueFrom, boolean visible, String type, int dataType) {
        this.columnName = columnName;
        this.columnIndex = columnIndex;
        this.retrieveValueFrom = retrieveValueFrom;
        this.visible = visible;
        this.type = type;
        this.dataType = dataType;
    }

    public static ProductSheetStaticColumn getColumn(String columnName){
        for(ProductSheetStaticColumn column: ProductSheetStaticColumn.values()){
            if(columnName.equalsIgnoreCase(column.columnName)){
                return column;
            }
        }
        return null;
    }

    public static ProductSheetStaticColumn getColumn(int columnIndex){
        for(ProductSheetStaticColumn column: ProductSheetStaticColumn.values()){
            if(columnIndex  == column.columnIndex){
                return column;
            }
        }
        return null;
    }
}