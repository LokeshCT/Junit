package com.bt.rsqe.matchers.excel;

import java.util.HashMap;
import java.util.Map;

public class ExcelTemplateFields {
    private Map<Integer, ExcelTemplateSheetFields> sheets = new HashMap<Integer, ExcelTemplateSheetFields>();

    public ExcelTemplateSheetFields getSheetFields(int sheetIndex) {
        return sheets.get(sheetIndex);
    }

    public void add(Integer sheetIndex, ExcelTemplateSheetFields sheetFields) {
        sheets.put(sheetIndex, sheetFields);
    }

}
