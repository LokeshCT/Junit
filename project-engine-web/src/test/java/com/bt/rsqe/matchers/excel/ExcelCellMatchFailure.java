package com.bt.rsqe.matchers.excel;

public class ExcelCellMatchFailure {
    private final ExcelTemplateField field;
    private final String value;

    public ExcelCellMatchFailure(ExcelTemplateField field, String value) {
        this.field = field;
        this.value = value;
    }

    public ExcelTemplateField getField() {
        return field;
    }

    public String getValue() {
        return value;
    }
}
