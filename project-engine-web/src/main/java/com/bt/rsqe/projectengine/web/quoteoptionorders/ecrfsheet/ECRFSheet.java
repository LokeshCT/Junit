package com.bt.rsqe.projectengine.web.quoteoptionorders.ecrfsheet;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import org.apache.poi.hssf.usermodel.HSSFErrorConstants;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.NumberToTextConverter;

import java.text.SimpleDateFormat;
import java.util.List;

import static com.google.common.collect.Lists.*;
import static java.lang.String.*;

public class ECRFSheet {

    public static final String CRF_DATE_FORMAT = "yyyy/MM/dd";
    private List<ECRFSheetModelRow> rows = newArrayList();
    private String SCode;
    private String sheetName;
    private int sheetIndex;
    public static final String RELATED_TO_PRODUCT_SHEET = "RelatedTo Products";
    private SheetTypeStrategy sheetTypeStrategy;

    public ECRFSheet() {
    }

    public void buildRowsFromSheet(Sheet sheet) {
        Row attributeNames = sheet.getRow(0);
        List<String> rowIds = newArrayList();
        int rowIndex = 2;
        while(sheet.getRow(rowIndex) != null && null != attributeNames) {
            ECRFSheetModelRow row = new ECRFSheetModelRow();
            List<ECRFSheetModelAttribute> attributes = newArrayList();
            Row attributeValues = sheet.getRow(rowIndex);
            for (int i = 0; i < attributeNames.getLastCellNum(); i++) {
                final Cell cell = attributeValues.getCell(i);
                final String name = attributeNames.getCell(i).toString();
                attributes.add(new ECRFSheetModelAttribute(name, cell != null ? convertCellValueToString(cell): null));
            }
            row.setAttributes(attributes);
            if(!this.isNonProductSheet()){
            row.setRowId(row.getAttributeByName(ECRFSheetModelRow.SHEET_ID).getValue());
            rowIds.add(row.getAttributeByName(ECRFSheetModelRow.SHEET_ID).getValue());
            row.setSheetName(sheetName);
            }
            try {
                if(this.isChildSheet()){
                row.setParentId(row.getAttributeByName(ECRFSheetModelRow.SHEET_PARENT_ID).getValue());
                row.setProductRelationName(row.getAttributeByName(ECRFSheetModelRow.RELATION).getValue());
                }
                if(this.isRelatedProductSheet()){
                row.setRelatedToId(row.getAttributeByName(ECRFSheetModelRow.RELATED_TO_ID).getValue());
                row.setOwnerId(row.getAttributeByName(ECRFSheetModelRow.OWNER_PRODUCT_ID).getValue());
                row.setRelationshipName(row.getAttributeByName(ECRFSheetModelRow.RELATIONSHIP_NAME).getValue());
                }
            } catch (ECRFImportException ex) {
                //keep null
            }
            this.rows.add(row);
            rowIndex++;
        }
        sheetContainsDuplicateId(rowIds);
    }

    private String convertCellValueToString(Cell cell) {
        switch (cell.getCellType()) {
            case Cell.CELL_TYPE_BLANK:
                return "";
            case Cell.CELL_TYPE_BOOLEAN:
                return cell.getBooleanCellValue() ? "TRUE" : "FALSE";
            case Cell.CELL_TYPE_STRING:
                return cell.getStringCellValue();
            case Cell.CELL_TYPE_NUMERIC:
                if(DateUtil.isCellDateFormatted(cell)) {
                  return new SimpleDateFormat(CRF_DATE_FORMAT).format(cell.getDateCellValue());
                }
                return NumberToTextConverter.toText(cell.getNumericCellValue());
            case Cell.CELL_TYPE_ERROR:
                   return HSSFErrorConstants.getText(cell.getErrorCellValue());
            case Cell.CELL_TYPE_FORMULA:
                switch (cell.getCachedFormulaResultType()){
                    case Cell.CELL_TYPE_NUMERIC:
                        return NumberToTextConverter.toText(cell.getNumericCellValue());
                    default:
                        return cell.getStringCellValue();
                }
            default:
                throw new IllegalStateException("Unexpected cell type (" + cell.getCellType() + "), cell value("+cell.toString()+")");
        }
    }

    private void sheetContainsDuplicateId(List<String> rowIds) {
        Multiset<String> ids = HashMultiset.create();
        ids.addAll(rowIds);
        for(Multiset.Entry<String> entry : ids.entrySet()) {
            if (entry.getCount() > 1) {
                throw new ECRFImportException(format(ECRFImportException.duplicateParentIdsFoundInSheet, entry.getElement(), this.sheetName));
            }
        }
    }

    public void setProductCode(String productCode) {
        this.SCode = productCode;
    }

    public void setSheetName(String sheetName) {
        this.sheetName = sheetName;
    }

    public ECRFSheetModelRow getRow(int index) {
        return this.rows.get(index);
    }

    public List<ECRFSheetModelRow> getRows() {
        return rows;
    }

    public String getProductCode() {
        return this.SCode;
    }

    public void addRow(ECRFSheetModelRow row) {
        this.rows.add(row);
    }

    public String getSheetName() {
        return sheetName;
    }

    public void setSheetIndex(int sheetIndex) {
        this.sheetIndex = sheetIndex;
    }

    public int getSheetIndex() {
        return sheetIndex;
    }

    public boolean isParentSheet() {
        return sheetTypeStrategy.equals(SheetTypeStrategy.Parent);
    }

    public boolean isRelatedProductSheet() {
        return sheetTypeStrategy.equals(SheetTypeStrategy.Related);
    }

    public boolean isChildSheet() {
        return sheetTypeStrategy.equals(SheetTypeStrategy.Child);
    }

    public boolean isNonProductSheet() {
        return sheetTypeStrategy.equals(SheetTypeStrategy.NonProduct);
    }

    public void setSheetTypeStrategy(SheetTypeStrategy sheetTypeStrategy) {
        this.sheetTypeStrategy = sheetTypeStrategy;
    }
}
