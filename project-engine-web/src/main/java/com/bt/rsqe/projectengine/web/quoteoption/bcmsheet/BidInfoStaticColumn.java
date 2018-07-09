package com.bt.rsqe.projectengine.web.quoteoption.bcmsheet;


public enum BidInfoStaticColumn {


    QUOTE_ID("Quote Id",0,"bid-Info.projectId",1,true, true),
    CUSTOMER_NAME("Customer Name",1,"bid-Info.customerName",1,true, true),
    OPPORTUNITY_ID("Opportunity Id",2,"bid-Info.opportunityId",1,true, true),
    BID_NUMBER("Bid Number",3,"bid-Info.bidNumber",1,true, true),
    QUOTE_VERSION_NUMBER("Quote Version Number",4,"bid-Info.quoteOptionVersion",1,true, true),
    QUOTE_CURRENCY("Quote Currency",5,"bid-Info.quoteCurrency",1,true, true),
    USER_NAME("User Name",6,"bid-Info.username",1,true, true),
    TRADE_LEVEL("Trade Level",7,"bid-Info.tradeLevel",1,true, true),
    SALES_CHANNEL ("Sales Channel",8,"bid-Info.salesChannel",1,true, true),
    CONTRACT_TERM("Contract Term",9,"bid-Info.contractTerm",2,true, true),
    SHEET_VERSION_NO("Sheet Version No",10,"bid-Info.sheetVersion",1,true, true),
    OFFER_NAME("Offer",11,"bid-Info.offerName",1,true, true),
    EXPEDIO_REFERENCE("Offer",12,"bid-Info.expref",1,true, true);

    public String columnName;
    public int columnIndex;
    public String retrieveValueFrom;
    public int dataType;
    public boolean isReadOnly;
    public boolean visibility;

    BidInfoStaticColumn(String columnName, int columnIndex, String retrieveValueFrom, int dataType, boolean isReadOnly, boolean visibility) {
        this.columnName = columnName;
        this.columnIndex = columnIndex;
        this.retrieveValueFrom = retrieveValueFrom;
        this.dataType = dataType;
        this.isReadOnly=isReadOnly;
        this.visibility = visibility;
    }

}
