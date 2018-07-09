package com.bt.rsqe.projectengine.web.quoteoptionpricing.pricingsheet;

import net.sf.jxls.parser.Cell;
import net.sf.jxls.parser.Expression;
import net.sf.jxls.parser.Property;
import net.sf.jxls.processor.CellProcessor;
import net.sf.jxls.util.Util;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.util.HSSFColor;

import java.util.Map;

public class PricingSheetCellProcessor implements CellProcessor{

    private Map params;

    public PricingSheetCellProcessor(Map params) {
        this.params = params;
    }

    @Override
    public void processCell(Cell cell, Map namedCells) {
        if( cell.getExpressions().size()>0 ){
            Expression expression = (Expression) cell.getExpressions().get(0);
            if(expression.getProperties().size()>0){
            Property property = (Property)expression.getProperties().get(0);
                if (property == null || property.getBeanName() == null || property.getPropertyValue()==null|| property.getPropertyValue() == "") {

                String key =property.getPropertyNameAfterLastDot();
                    if(params.get(key)==null || params.get(key)==""){
                        grayCell(cell);
                    }
                }
            }
        }
    }

    private void grayCell(Cell cell) {
        HSSFCell hssfCell = (HSSFCell)cell.getPoiCell();
        HSSFCellStyle newStyle = (HSSFCellStyle) Util.duplicateStyle(cell.getRow().getSheet().getPoiWorkbook(), cell.getPoiCell().getCellStyle());
        newStyle.setFillBackgroundColor(HSSFColor.GREY_40_PERCENT.index);
        hssfCell.setCellStyle(newStyle);
    }
}
