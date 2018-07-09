package com.bt.rsqe.projectengine.web.quoteoption.bulktemplatesheet;

import com.bt.rsqe.domain.product.Attribute;
import com.bt.rsqe.domain.product.parameters.RelationshipType;
import com.bt.rsqe.excel.ExcelStyler;
import com.bt.rsqe.projectengine.web.quoteoptionorders.rfosheet.ExportExcelMarshaller;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.util.List;

import static com.bt.rsqe.excel.ExcelUtil.*;
import static com.bt.rsqe.utils.AssertObject.*;
import static com.google.common.collect.Lists.*;
import static org.apache.poi.ss.usermodel.Cell.*;

public class BulkTemplateDetailSheetMarshaller implements ExportExcelMarshaller.ExcelMarshaller{

    private final Workbook workbook;
    private ExcelStyler styler;
    private final XSSFSheet sheet;
    private static final int WORKBOOK_SHEET_NAME_LENGTH = 31;
    private static int sheetNameCount = 0;
    private BulkTemplateDetailSheetModel bulkTemplateDetailSheetModel;


    public BulkTemplateDetailSheetMarshaller(BulkTemplateDetailSheetModel bulkTemplateDetailSheetModel, XSSFWorkbook workbook, ExcelStyler styler) {
        this.workbook = workbook;
        this.bulkTemplateDetailSheetModel = bulkTemplateDetailSheetModel;
        this.sheet = workbook.createSheet(generateWorkBookSheetName(bulkTemplateDetailSheetModel.getSheetName(), workbook));
        this.styler = styler;
    }


    public enum HeaderColumn {
        ID("ID","Id"),
        PARENT_ID("PARENT PRODUCT ID","Parent Product id"),
        RELATED_TO_ID("RELATED TO ID","RelatedTo ID"),
        SITE_ID("SITE ID", "Site Id/Site Name");

        public final String columnName;
        public final String displayName;

        HeaderColumn(String columnName,String displayName) {
            this.columnName = columnName;
            this.displayName = displayName;
        }
    }

    private String generateWorkBookSheetName(String exportSheetName, XSSFWorkbook workbook) {
        if (WORKBOOK_SHEET_NAME_LENGTH < exportSheetName.length() ) {
            exportSheetName = exportSheetName.substring(0, WORKBOOK_SHEET_NAME_LENGTH);
        }
        if (workbook.getSheetIndex(exportSheetName) > -1) {
            if (WORKBOOK_SHEET_NAME_LENGTH == exportSheetName.length()) {
                return exportSheetName.substring(0, WORKBOOK_SHEET_NAME_LENGTH - 1) + (++sheetNameCount);
            } else {
                return exportSheetName + (++sheetNameCount);
            }
        } else {
            return exportSheetName;
        }
    }

    @Override
    public void marshall() {
        SheetHeaderNodes sheetHeaderNodes = new SheetHeaderNodes();
        int columnIndex =0;
        if (isNotNull(bulkTemplateDetailSheetModel.getBulkTemplateDetailRowModel())){
            BulkTemplateDetailSheetModel.BulkTemplateDetailRowModel bulkTemplateDetailRowModel= bulkTemplateDetailSheetModel.getBulkTemplateDetailRowModel();
            sheetHeaderNodes.addHeader(new HeaderNode(HeaderColumn.ID.columnName,HeaderColumn.ID.displayName,columnIndex++,false));
            //Parent ID for Child Sheets
            if(bulkTemplateDetailRowModel.getProductModel().getRelationType().equals(RelationshipType.Child.value())){
                sheetHeaderNodes.addHeader(new HeaderNode(HeaderColumn.PARENT_ID.columnName,HeaderColumn.PARENT_ID.displayName,columnIndex++,false));
            }
            //Related TO ID for Child Sheets
            if(bulkTemplateDetailRowModel.getProductModel().getRelationType().equals(RelationshipType.RelatedTo.value())){
                sheetHeaderNodes.addHeader(new HeaderNode(HeaderColumn.RELATED_TO_ID.columnName,HeaderColumn.RELATED_TO_ID.displayName,columnIndex++,false));
            }

            sheetHeaderNodes.addHeader(new HeaderNode(HeaderColumn.SITE_ID.columnName,HeaderColumn.SITE_ID.displayName,columnIndex++,false));

            for (Attribute attribute : bulkTemplateDetailRowModel.getAttributes()) {
                sheetHeaderNodes.addHeader(new HeaderNode(attribute.getName().getName(), attribute.getDisplayName(),columnIndex++,true));
            }

            printHeaderNodes(sheetHeaderNodes);

            printDefaultValues(sheetHeaderNodes,bulkTemplateDetailRowModel);
        }
    }

    private void printDefaultValues(SheetHeaderNodes sheetHeaderNodes, BulkTemplateDetailSheetModel.BulkTemplateDetailRowModel bulkTemplateDetailRowModel) {
        XSSFRow dataRow = sheet.createRow(2);
        for(HeaderNode headerNode : sheetHeaderNodes.getMyHeaderNodes()){
            if(headerNode.isAttribute){
                populateCell(dataRow,headerNode.columnIndex,bulkTemplateDetailRowModel.getAttributeDefaultValue(headerNode.columnName),false);
                populateAllowedLOVs(headerNode.columnIndex, bulkTemplateDetailRowModel.getAttributeAllowedValues(headerNode.columnName), false, dataRow.getRowNum());
            }
        }
    }

    private void populateAllowedLOVs(int columnIndex, List<String> attributeAllowedValues, boolean allowUserOverride, int rowNum) {
        if(isNotNull(attributeAllowedValues) && isAllowedInExcel(attributeAllowedValues)){
            addConstraint(sheet, listConstraint(attributeAllowedValues, sheet), columnIndex, rowNum, allowUserOverride,rowNum) ;
        }
    }

    //TODO in R37, In R36 if allowed list values exceed 250 characters will not be populated in Excel
    private boolean isAllowedInExcel(List<String> attributeAllowedValues) {
        StringBuilder builder = new StringBuilder("\"");
        for(String value : attributeAllowedValues){
            if( builder.length() > 1) {
                builder.append(",");
            }
                builder.append(value);
        }

        return builder.length()<250;
    }

    private void printHeaderNodes(SheetHeaderNodes sheetHeaderNodes) {
        XSSFRow attributeHeaderRow = sheet.createRow(0);
        XSSFRow displayNameHeaderRow = sheet.createRow(1);
        boolean isHeader=true;

        for(HeaderNode headerNode : sheetHeaderNodes.getMyHeaderNodes()){
            populateCell(attributeHeaderRow, headerNode.columnIndex, headerNode.columnName,isHeader);
            populateCell(displayNameHeaderRow, headerNode.columnIndex, headerNode.displayName,isHeader);
        }

        for(int i=0;i<displayNameHeaderRow.getLastCellNum();i++){
            sheet.autoSizeColumn(i);
        }
    }

    private void populateCell(XSSFRow xssfRow, int columnIndex, String columnValue, boolean isHeader) {
        XSSFCell cell = xssfRow.createCell(columnIndex, CELL_TYPE_STRING);
        if(isHeader){
            styler.styleHeaderCell(cell);
        }else {
            CellStyle cellStyle = textFormat(workbook);
            styler.defaultStyle(cellStyle);
        }
        cell.setCellValue(columnValue);

        setCellWidths(xssfRow);
    }

    private void setCellWidths(XSSFRow xssfRow) {
        for(int i=0;i<xssfRow.getLastCellNum();i++){
            sheet.autoSizeColumn(i);
        }
    }


    public class SheetHeaderNodes{

        List<HeaderNode> myHeaderNodes = newLinkedList();

        public void addHeader(HeaderNode headerNode){
            addHeader(headerNode, false);
        }

        public void addHeader(HeaderNode headerNode, boolean ignoreDuplicates) {
            if(ignoreDuplicates && myHeaderNodes.contains(headerNode)) {
                return;
            }
            myHeaderNodes.add(headerNode);
        }

        public List<HeaderNode> getMyHeaderNodes() {
            return myHeaderNodes;
        }
    }

    public static class HeaderNode{

        private String columnName;
        private String displayName;
        private int columnIndex;
        private boolean isAttribute;

        public HeaderNode(String columnName, String displayName, int columnIndex,boolean isAttribute) {
            this.columnName = columnName;
            this.displayName = displayName;
            this.columnIndex = columnIndex;
            this.isAttribute = isAttribute;
        }

        @Override
        public int hashCode(){
            return HashCodeBuilder.reflectionHashCode(this);
        }

        @Override
        public boolean equals(Object that){
            return EqualsBuilder.reflectionEquals(this, that);
        }


    }
}
