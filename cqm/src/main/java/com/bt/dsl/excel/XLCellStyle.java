package com.bt.dsl.excel;

import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.xssf.usermodel.XSSFColor;


/**
 * Created with IntelliJ IDEA.
 * User: 607937181
 * Date: 27/08/15
 * Time: 19:45
 * To change this template use File | Settings | File Templates.
 */
public class XLCellStyle {

    private short fontColor = HSSFColor.BLACK.index;
    private short fontStyle = Font.BOLDWEIGHT_NORMAL;
    private short fontVerticalAlign = CellStyle.VERTICAL_CENTER;
    private short fontHorizontalAlign = CellStyle.ALIGN_CENTER;

    private XSSFColor bgColor = new XSSFColor(new java.awt.Color(255, 255,255)); //white.

    public short getFontColor() {
        return fontColor;
    }

    public void setFontColor(short fontColor) {
        this.fontColor = fontColor;
    }

    public short getFontStyle() {
        return fontStyle;
    }

    public void setFontStyle(short fontStyle) {
        this.fontStyle = fontStyle;
    }

    public short getFontVerticalAlign() {
        return fontVerticalAlign;
    }

    public void setFontVerticalAlign(short fontVerticalAlign) {
        this.fontVerticalAlign = fontVerticalAlign;
    }

    public short getFontHorizontalAlign() {
        return fontHorizontalAlign;
    }

    public void setFontHorizontalAlign(short fontHorizontalAlign) {
        this.fontHorizontalAlign = fontHorizontalAlign;
    }

    public XSSFColor getBgColor() {
        return bgColor;
    }

    public void setBgColor(XSSFColor bgColor) {
        this.bgColor = bgColor;
    }
}
