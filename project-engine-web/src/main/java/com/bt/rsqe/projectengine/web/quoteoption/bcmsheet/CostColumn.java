package com.bt.rsqe.projectengine.web.quoteoption.bcmsheet;

public enum CostColumn {

    COST_DESCRIPTION("Cost Description",22, "PriceDescription", true, "cost"),
    ONE_TIME_COST("One Time Cost",23, "RecurringPrice", true, "cost"),
    RECURRING_COST("Recurring Cost",24, "NonRecurringPrice", true, "cost");

    public final String columnName;
    public final int columnIndex;
    public final String retrieveValueFrom;
    public boolean visible;
    public final String type;

    CostColumn(String columnName, int columnIndex, String retrieveValueFrom, boolean visible, String type) {
        this.columnName = columnName;
        this.columnIndex = columnIndex;
        this.retrieveValueFrom = retrieveValueFrom;
        this.visible = visible;
        this.type = type;
    }
}
