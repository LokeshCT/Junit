package com.bt.rsqe.projectengine.web.quoteoption.bcmsheet;

public class HeaderCell {

    public int columnIndex;
    public String columnName;
    public boolean visibility;
    public String groupName;
    public String retrieveValueFrom;
    public int sheetIndex;
    public int dataType;
    public boolean isReadOnly;

    public HeaderCell(int columnIndex, String columnName, boolean visibility, String groupName, String retrieveValueFrom, int sheetIndex, int dataType) {
        this.columnIndex = columnIndex;
        this.columnName = columnName;
        this.visibility = visibility;
        this.groupName = groupName;
        this.retrieveValueFrom = retrieveValueFrom;
        this.sheetIndex = sheetIndex;
        this.dataType = dataType;
    }

    public HeaderCell(int columnIndex, String columnName,boolean visibility, String retrieveValueFrom, int dataType, boolean readOnly) {
        this.columnIndex = columnIndex;
        this.columnName = columnName;
        this.visibility = visibility;
        this.retrieveValueFrom = retrieveValueFrom;
        //this.sheetIndex = sheetIndex;
        this.dataType = dataType;
        this.isReadOnly = readOnly;
    }
}
