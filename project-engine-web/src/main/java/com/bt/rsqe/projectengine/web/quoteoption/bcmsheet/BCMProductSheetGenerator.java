package com.bt.rsqe.projectengine.web.quoteoption.bcmsheet;

import com.bt.rsqe.domain.product.InstanceCharacteristic;
import com.bt.rsqe.domain.project.ProductInstance;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CellStyle;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.bt.rsqe.projectengine.web.quoteoption.bcmsheet.BCMConstants.*;
import static com.bt.rsqe.projectengine.web.quoteoption.bcmsheet.Groups.*;
import static com.bt.rsqe.projectengine.web.quoteoption.bcmsheet.ProductDetailsColumn.*;
import static com.bt.rsqe.utils.AssertObject.*;
import static com.google.common.collect.Maps.*;

public class BCMProductSheetGenerator {
    private static final String VALUE = "Value";
    private static final String GET = "get";
    private static final int HEADER_ROW = 0;
    private static final int DATA_ROW = 1;

    public HSSFSheet createSheet(HSSFWorkbook workbook, HeaderRowModel headerModel, List<BCMDataRowModel> dataRowModel, String sheetName, int costSetCount, String[] groups) {
        HSSFSheet sheet = workbook.createSheet(sheetName);
        headerModel.generateSheetsBasedOnHeaderSize(workbook, sheetName);
        createHeader(headerModel);
        populateData(sheet, dataRowModel, headerModel, costSetCount, groups);
        return sheet;
    }

    private void populateData(HSSFSheet sheet, List<BCMDataRowModel> dataRowModel, HeaderRowModel headerModel, int costSetCount, String... groups) {
        int rowIndex = DATA_ROW;
        for (BCMDataRowModel rowModel : dataRowModel) {
            HSSFRow row = createRow(sheet, rowIndex++, DATA_ROW_HEIGHT);
            List<HeaderCell> staticColumn = headerModel.getColumnsByGroupName(groups);
            for (HeaderCell cell : staticColumn) {
                createCell(row, cell.columnIndex, retrieveValue(cell.retrieveValueFrom, rowModel), getGroup(cell.groupName).getDataRowStyle(sheet.getWorkbook()), cell.visibility, cell.dataType);
            }
            populateCostDetails(row, rowModel, costSetCount);
            populateProductDetails(row, headerModel, rowModel);
        }
    }

    private void populateProductDetails(HSSFRow currentRow, HeaderRowModel headerModel, BCMDataRowModel rowModel) {
        int startIndex;
        String productInstanceId = "";
        CellStyle style = Groups.product.getDataRowStyle(currentRow.getSheet().getWorkbook());
        Map<String, String> relationshipNamesMap = newHashMap();
        populateRelationshipNames(rowModel.getRootProductInstance(), relationshipNamesMap);
        for (ProductInstance productInstance : rowModel.getProductInstanceAndItsChildProducts()) {
            HeaderCell cell = headerModel.getCellFor(productInstance.getProductName() + " " + PRIMARY_IDENTIFIER.columnName + " " + productInstance.getProductIdentifier().getProductId());
            if (isNotNull(cell)) {
                startIndex = cell.columnIndex;
                HSSFRow row = headerModel.getRowFor(cell.sheetIndex, currentRow.getRowNum());
                createCell(row, startIndex++, retrieveValue(PRIMARY_IDENTIFIER.retrieveValueFrom, productInstance), style, PRIMARY_IDENTIFIER.visible, cell.dataType);
                createCell(row, startIndex++, retrieveValue(VERSION_NUMBER.retrieveValueFrom, productInstance), style, VERSION_NUMBER.visible, cell.dataType);
                createCell(row, startIndex++, retrieveValue(PRODUCT_INSTANCE_ID.retrieveValueFrom, productInstance), style, PRODUCT_INSTANCE_ID.visible, cell.dataType);
                productInstanceId = productInstance.getProductInstanceId().getValue();
                createCell(row, startIndex++, relationshipNamesMap.get(productInstanceId), style, CHILD_RELATIONSHIP_NAME.visible, cell.dataType);
                for (InstanceCharacteristic instanceCharacteristic : HeaderRowModelFactory.getRFQInstanceCharacteristics(productInstance)) {
                    createCell(row, startIndex++, retrieveValue(VALUE, instanceCharacteristic), style, true, cell.dataType);
                }
            }
        }
    }

    private void populateRelationshipNames(ProductInstance productInstance, Map<String, String> relationshipNamesMap) {
        if (productInstance != null) {
            for (ProductInstance child : productInstance.getChildren()) {
                relationshipNamesMap.put(child.getProductInstanceId().getValue(), productInstance.getRelationshipNameWith(child.getProductInstanceId().getValue()));
                populateRelationshipNames(child, relationshipNamesMap);
            }
        }
    }

    private void populateCostDetails(HSSFRow row, BCMDataRowModel rowModel, int costSetCount) {
        int startCostIndex = row.getLastCellNum();
        for (int i = 0; i < costSetCount; i++) {
            BCMPriceModel model = rowModel.getCostLines().size() > i ? rowModel.getCostLines().get(i) : null;
            for (CostColumn column : CostColumn.values()) {
                createCell(row, startCostIndex++, retrieveValue(column.retrieveValueFrom, model), cost.getDataRowStyle(row.getSheet().getWorkbook()), column.visible, HSSFCell.CELL_TYPE_STRING);
            }
        }
    }

    public void createHeader(HeaderRowModel headerModel) {
        Map<String, CellStyle> headerStyleForGroup = Groups.getHeaderStyleForGroup(headerModel.getWorkBook());
        createHeaderRowInAllSheet(headerModel.getRequiredSheets());
        for (HeaderCell headerCell : headerModel.getHeaderRow()) {
            HSSFCell cell = createCell(headerModel.getHeaderRow(headerCell.sheetIndex),
                                       headerCell.columnIndex,
                                       headerCell.columnName,
                                       headerStyleForGroup.get(headerCell.groupName),
                                       headerCell.visibility, HSSFCell.CELL_TYPE_STRING);
            Groups.setColumnWidth(cell.getSheet(), headerCell.columnIndex);
        }
    }

    public void createHeaderRowInAllSheet(List<HSSFSheet> sheets) {
        for (HSSFSheet sheet : sheets) {
            createRow(sheet, HEADER_ROW, HEADER_ROW_HEIGHT);
        }
    }

    public Object retrieveValue(String from, Object retrieveObj) {
        List<String> lookUps = Arrays.asList(from.split("\\."));
        if (isNull(retrieveObj)) {
            return "";
        }
        Object obj = retrieveObj;
        for (String lookup : lookUps) {
            try {
                obj = obj.getClass().getDeclaredMethod(GET + lookup).invoke(obj);
            } catch (InvocationTargetException e) {
                return "";
            } catch (NoSuchMethodException e) {
                try {
                    Field field = obj.getClass().getDeclaredField(lookup);
                    obj = field.get(obj);
                } catch (NoSuchFieldException e1) {
                    return "";
                } catch (IllegalAccessException e1) {
                    return "";
                }
            } catch (IllegalAccessException e) {
                return "";
            }
        }
        return obj;
    }

    public HSSFCell createCell(HSSFRow row, int index, Object value, CellStyle cellStyle, boolean isHidden, int cellType) {
        HSSFCell cell = row.createCell(index);
        cell.setCellStyle(cellStyle);
        String stringValue = value == null ? "" : value.toString();
        if (value != null) {
            cell.setCellType(cellType);
        }
        if (cellType == HSSFCell.CELL_TYPE_STRING) {
            cell.setCellValue(stringValue);
        } else if (cellType == HSSFCell.CELL_TYPE_NUMERIC && !isEmpty(stringValue)) {
            cell.setCellValue(Double.parseDouble(stringValue));
        }
        if (!isHidden) {
            row.getSheet().setColumnHidden(index, true);
        }
        return cell;
    }

    public HSSFRow createRow(HSSFSheet sheet, int index, short rowHeight) {
        HSSFRow row = sheet.createRow(index);
        row.setHeight(rowHeight);
        return row;
    }
}
