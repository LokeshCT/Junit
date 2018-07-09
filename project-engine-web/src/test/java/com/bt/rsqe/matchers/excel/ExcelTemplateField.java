package com.bt.rsqe.matchers.excel;

public class ExcelTemplateField {
    private final String name;
    private final int rowIndex;
    private final int columnIndex;

    public ExcelTemplateField(String name, int rowIndex, int columnIndex) {
        this.name = name;
        this.rowIndex = rowIndex;
        this.columnIndex = columnIndex;
    }

    public String getName() {
        return name;
    }

    public int getRowIndex() {
        return rowIndex;
    }

    public int getColumnIndex() {
        return columnIndex;
    }
}
