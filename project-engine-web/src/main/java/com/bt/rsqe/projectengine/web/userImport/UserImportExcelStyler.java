package com.bt.rsqe.projectengine.web.userImport;

import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;

import static com.bt.rsqe.excel.ExcelUtil.*;
import static com.bt.rsqe.utils.AssertObject.*;

public class UserImportExcelStyler {

    private static final String DEFAULT_FONT_NAME = "Calibri";
    private static final HSSFColor DEFAULT_FOREGROUND_COLOR = new HSSFColor.WHITE();
    private Workbook workbook;

    public UserImportExcelStyler(Workbook workbook) {
        this.workbook = workbook;
        updateColourInWorkbookPalette(workbook);
    }

    public CellStyle buildStyle(StyleConfiguration configuration) {
        CellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setAlignment(configuration.getCellAlignment());
        foregroundColor(cellStyle, isNull(configuration.getForeColor()) ? DEFAULT_FOREGROUND_COLOR : configuration.getForeColor());
        cellStyle.setWrapText(configuration.isWrapText());
        cellStyle.setFont(getFont(configuration));
        setBorders(cellStyle, configuration.getBorders());
        return cellStyle;
    }

    private void setBorders(CellStyle style, short[] borders) {
        style.setBorderBottom(borders[0]);
        style.setBorderTop(borders[1]);
        style.setBorderLeft(borders[2]);
        style.setBorderRight(borders[3]);
    }

    private void updateColourInWorkbookPalette(Workbook workbook) {
        XSSFCellStyle style1 = (XSSFCellStyle) workbook.createCellStyle();
        style1.setFillForegroundColor(new XSSFColor(new java.awt.Color(200, 221, 229)));
        style1.setFillPattern(CellStyle.SOLID_FOREGROUND);
    }

    private Font getFont(StyleConfiguration configuration) {
        Font font = workbook.createFont();
        font.setFontName(DEFAULT_FONT_NAME);
        font.setFontHeightInPoints(configuration.getFontSize());
        font.setBoldweight(configuration.getBoldWeight());
        if (isNotNull(configuration.getFontColor())) {
            font.setColor(configuration.getFontColor().getIndex());
        }
        return font;
    }
}
