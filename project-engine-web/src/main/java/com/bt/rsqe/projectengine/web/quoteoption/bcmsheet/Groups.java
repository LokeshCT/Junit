package com.bt.rsqe.projectengine.web.quoteoption.bcmsheet;


import org.apache.poi.hssf.record.cf.PatternFormatting;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.CellStyle;

import java.util.HashMap;
import java.util.Map;

import static org.apache.poi.ss.usermodel.Font.*;

public enum Groups{
    site ("site") {
        @Override
        public CellStyle getDataRowStyle(HSSFWorkbook workbook) {
            return createDataRowStyle(workbook, new HSSFColor.LIGHT_TURQUOISE().getIndex());
        }

        @Override
        public CellStyle getHeaderRowStyle(HSSFWorkbook workbook) {
            return createHeaderRowStyle(workbook, new HSSFColor.LIGHT_TURQUOISE().getIndex());
        }
    },
    common("common") {
        @Override
        public CellStyle getDataRowStyle(HSSFWorkbook workbook) {
            return createDataRowStyle(workbook, new HSSFColor.LIGHT_TURQUOISE().getIndex());
        }

        @Override
        public CellStyle getHeaderRowStyle(HSSFWorkbook workbook) {
            return createHeaderRowStyle(workbook, new HSSFColor.LIGHT_TURQUOISE().getIndex());
        }
    },
    price("price") {
        @Override
        public CellStyle getDataRowStyle(HSSFWorkbook workbook) {
            return createDataRowStyle(workbook, new HSSFColor.LIGHT_YELLOW().getIndex());
        }

        @Override
        public CellStyle getHeaderRowStyle(HSSFWorkbook workbook) {
            return createHeaderRowStyle(workbook, new HSSFColor.LIGHT_YELLOW().getIndex());
        }
    },
    cost("cost") {
        @Override
        public CellStyle getDataRowStyle(HSSFWorkbook workbook) {
            return createDataRowStyle(workbook, new HSSFColor.LIGHT_YELLOW().getIndex());
        }

        @Override
        public CellStyle getHeaderRowStyle(HSSFWorkbook workbook) {
            return createHeaderRowStyle(workbook, new HSSFColor.LIGHT_YELLOW().getIndex());
        }
    },
    product("product") {
        @Override
        public CellStyle getDataRowStyle(HSSFWorkbook workbook) {
            return createDataRowStyle(workbook, (short)0);
        }

        @Override
        public CellStyle getHeaderRowStyle(HSSFWorkbook workbook) {
            return createHeaderRowStyle(workbook, new HSSFColor.LIGHT_GREEN().getIndex());
        }
    };

    private static final Short FONT_HEIGHT = 11;
    private static final Short BORDER_SIZE = 1;
    private static final int COLUMN_WIDTH = 6035;
    public String groupName;

    Groups(String groupName) {
        this.groupName = groupName;
    }

    public abstract CellStyle getDataRowStyle(HSSFWorkbook workbook);
    public abstract CellStyle getHeaderRowStyle(HSSFWorkbook workbook);

    public CellStyle createHeaderRowStyle(HSSFWorkbook workbook, short colorIndex) {
        CellStyle cellStyle = workbook.createCellStyle();
        HSSFFont font = workbook.createFont();
        font.setFontHeightInPoints(FONT_HEIGHT);
        font.setBoldweight(BOLDWEIGHT_BOLD);
        cellStyle.setFont(font);
        cellStyle.setBorderLeft(BORDER_SIZE);
        cellStyle.setBorderTop(BORDER_SIZE);
        cellStyle.setBorderRight(BORDER_SIZE);
        cellStyle.setBorderBottom(BORDER_SIZE);
        cellStyle.setWrapText(true);
        cellStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
        cellStyle.setFillForegroundColor(colorIndex);
        cellStyle.setFillPattern(PatternFormatting.SOLID_FOREGROUND);
        return cellStyle;
    }

    public CellStyle createDataRowStyle(HSSFWorkbook workbook, short colorIndex) {
        CellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
        cellStyle.setWrapText(true);
        if(colorIndex > 0){
            cellStyle.setFillForegroundColor(colorIndex);
            cellStyle.setFillPattern(PatternFormatting.SOLID_FOREGROUND);
        }
        return cellStyle;
    }

    public static Groups getGroup(String groupName) {
        for(Groups group : Groups.values()){
            if(group.groupName.equalsIgnoreCase(groupName)){
                return group;
            }
        }
        return null;
    }

    public static void setColumnWidth(HSSFSheet sheet, int columnIndex) {
        sheet.setColumnWidth(columnIndex, COLUMN_WIDTH);
    }

    public static Map<String, CellStyle> getHeaderStyleForGroup(HSSFWorkbook workbook){
        Map<String, CellStyle> groupHeaderStyle = new HashMap<String, CellStyle>();
        for(Groups group : Groups.values()){
            groupHeaderStyle.put(group.groupName, group.getHeaderRowStyle(workbook));
        }
        return groupHeaderStyle;
    }
}
