package com.bt.rsqe.projectengine.web.userImport;

import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;


public enum StyleConfiguration {
    PALE_BLUE_HEADER(14, new HSSFColor.PALE_BLUE(), CellStyle.ALIGN_CENTER, true, Font.BOLDWEIGHT_BOLD, null, new short[]{1, 1, 1, 1}),
    CF_BLUE_HEADER(11, new HSSFColor.LIGHT_CORNFLOWER_BLUE(), CellStyle.ALIGN_CENTER, true, Font.BOLDWEIGHT_BOLD, null, new short[]{1, 1, 1, 1}),
    RED_HEADER(14, new HSSFColor.WHITE(), CellStyle.ALIGN_CENTER, true, Font.BOLDWEIGHT_BOLD, new HSSFColor.RED(), new short[]{1, 1, 1, 1}),
    HEADER_STYLE(11, new HSSFColor.LIGHT_BLUE(), CellStyle.ALIGN_CENTER, true, Font.SS_NONE, null, new short[]{1, 1, 1, 1}),
    GREY_STYLE(11, new HSSFColor.GREY_25_PERCENT(), CellStyle.ALIGN_LEFT, true, Font.SS_NONE, null, new short[]{7, 7, 1, 1}),
    NORMAL_STYLE(11, null, CellStyle.ALIGN_LEFT, true, Font.SS_NONE, null, new short[]{7, 7, 1, 1});

    private final short fontSize;
    private final HSSFColor foreColor;
    private final short cellAlignment;
    private short boldWeight;
    private final boolean wrapText;
    private final HSSFColor fontColor;
    private short[] borders;

    StyleConfiguration(int fontSize, HSSFColor foreColor, short cellAlignment, boolean wrapText, short boldWeight, HSSFColor fontColor, short[] borders) {
        this.wrapText = wrapText;
        this.fontColor = fontColor;
        this.borders = borders;
        this.fontSize = (short) fontSize;
        this.foreColor = foreColor;
        this.cellAlignment = cellAlignment;
        this.boldWeight = boldWeight;
    }

    public short getFontSize() {
        return fontSize;
    }

    public HSSFColor getForeColor() {
        return foreColor;
    }

    public short getCellAlignment() {
        return cellAlignment;
    }

    public boolean isWrapText() {
        return wrapText;
    }

    public short getBoldWeight() {
        return boldWeight;
    }

    public HSSFColor getFontColor() {
        return fontColor;
    }

    public short[] getBorders() {
        return borders;
    }
}
