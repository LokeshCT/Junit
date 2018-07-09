package com.bt.rsqe.projectengine.web.model.bcmspreadsheet;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.ss.usermodel.Row;

import java.util.List;

import static com.google.common.collect.Lists.*;

public class ProductInfoSheet extends BcmSpreadSheet {
    private HSSFSheet sheet;

    public ProductInfoSheet(HSSFSheet sheet) {
        this.sheet = sheet;
    }

    public List<ProductInfoSheetRow> rows() {
        List<ProductInfoSheetRow> rows = newArrayList();
        for (Row row : sheet) {
            if (isHeader(row) || isEmpty(row)) {
                continue;
            }
            rows.add(new ProductInfoSheetRow(row));
        }
        return rows;
    }
}
