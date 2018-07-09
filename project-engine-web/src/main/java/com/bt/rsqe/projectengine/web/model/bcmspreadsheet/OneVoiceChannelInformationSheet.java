package com.bt.rsqe.projectengine.web.model.bcmspreadsheet;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

import java.util.Map;

import static com.bt.rsqe.excel.ExcelUtil.*;
import static com.google.common.collect.Maps.*;

public class OneVoiceChannelInformationSheet extends BcmSpreadSheet {

    private Map<String, Row> siteIdbasedRows = newHashMap();

    public OneVoiceChannelInformationSheet(HSSFSheet sheet) {
        for (Row row : sheet) {
            if (isHeader(row)) {
                continue;
            }

            final Cell siteIdCell = row.getCell(3);
            if (siteIdCell != null && isCellNumeric(siteIdCell)) {
                String siteId = Double.valueOf(siteIdCell.getNumericCellValue()).toString();
                siteId = siteId.substring(0, siteId.indexOf("."));
                if (siteId != null && (!siteId.equals(""))) {
                    if (siteIdbasedRows.containsKey(siteId)) {
                        throw new IllegalArgumentException("There are duplicated site id in the spreadsheet!");
                    }
                    siteIdbasedRows.put(siteId, row);
                }
            }
        }
    }

    public boolean containsSiteId(String siteId) {
        return siteIdbasedRows.containsKey(siteId);
    }

    public OneVoiceChannelInformationRow getOneVoiceChannelInformationRow(String siteId) {
        return new OneVoiceChannelInformationRow(siteIdbasedRows.get(siteId));
    }

}
