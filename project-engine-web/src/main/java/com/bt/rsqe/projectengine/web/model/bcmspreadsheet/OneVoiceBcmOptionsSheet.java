package com.bt.rsqe.projectengine.web.model.bcmspreadsheet;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.ss.usermodel.Row;

import java.util.List;

import static com.google.common.collect.Lists.*;

public class OneVoiceBcmOptionsSheet extends BcmSpreadSheet {
    private HSSFSheet sheet;

    public OneVoiceBcmOptionsSheet(HSSFSheet sheet) {
        this.sheet = sheet;
    }

    public boolean containsSiteId(String siteId) {
        for (OneVoiceBcmOptionsRow row : rows()) {
            if(row.siteId().equals(siteId)){
                return true;
            }
        }
        return false;
    }

    public OneVoiceBcmOptionsRow rowForSiteId(String siteId){
        for (OneVoiceBcmOptionsRow row : rows()) {
            if(row.siteId().equals(siteId)){
                return row;
            }
        }
        return null;
    }

    private List<OneVoiceBcmOptionsRow> rows() {
        List<OneVoiceBcmOptionsRow> rows = newArrayList();
        for (Row row : sheet) {
            if (isHeader(row) || isEmpty(row)){
                continue;
            }
            rows.add(new OneVoiceBcmOptionsRow(row));
        }
        return rows;
    }
}
