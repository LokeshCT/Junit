package com.bt.rsqe.projectengine.web.quoteoptionorders.rfosheet;

import com.bt.rsqe.excel.ExcelStyler;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.bt.rsqe.excel.ExcelUtil.*;
import static com.google.common.collect.Lists.*;
import static com.google.common.collect.Maps.newHashMap;
import static java.util.Arrays.asList;
import static org.apache.commons.collections.CollectionUtils.*;
import static org.apache.poi.ss.usermodel.Cell.*;

public class RFOSheetMarshaller implements ExportExcelMarshaller.ExcelMarshaller {
    private static final double CELL_PADDING = 5.0;

    private final RFOSheetModel rfoSheetModel;
    private final XSSFWorkbook workbook;
    private ExcelStyler styler;
    private final XSSFSheet sheet;
    private CellStyle textFormat;
        private CellStyle textFormatWithCellLocked;
    private List<String> LIST_OF_HIDDEN_COLUMNS = Arrays.asList(Column.LINE_ITEM_ID.header, Column.PRODUCT_INSTANCE_ID.header);
    private CellStyle textFormatWithCellGrayOut;
    private static final int WORKBOOK_SHEET_NAME_LENGTH = 31;
    private static int sheetNameCount = 0;
    public static final String OPTIONAL_FLAG = " (O)";
    private Map<Integer, String> headerValues = newHashMap();
    private Map<Integer, String> cellBiggestValues = newHashMap();

    public enum Column {
        LINE_ITEM_ID("Line Item ID", 0),
        SITE_ID("Site Id", 1),
        SITE_NAME("Site Name", 2),
        SUMMARY("Summary", 3),
        PRODUCT_INSTANCE_ID("Product Instance Id", 4),
        PRODUCT_NAME("Product Name", 5),
        SUBLOCATION_NAME("Sublocation Name", 6),
        SUBLOCATION_ID("Sublocation ID", 7),
        ROOM("Room",8),
        FLOOR("Floor",9);

        public final String header;
        public final int column;

        Column(String header, int column) {
            this.header = header;
            this.column = column;
        }
    }
    public RFOSheetMarshaller(RFOSheetModel rfoSheetModel, XSSFWorkbook workbook, ExcelStyler styler) {
        this.rfoSheetModel = rfoSheetModel;
        this.workbook = workbook;
        this.styler = styler;
        this.sheet = workbook.createSheet(generateWorkBookSheetName(rfoSheetModel.sheetName(), workbook));
        textFormat = textFormat(workbook);
        textFormatWithCellLocked = textFormatWithCellLocked(workbook);
        textFormatWithCellGrayOut = textFormatWithCellGrayOut(workbook);
    }

    private String generateWorkBookSheetName(String rfoSheetName, Workbook workbook) {
        if(rfoSheetName.length() > WORKBOOK_SHEET_NAME_LENGTH) {
            rfoSheetName = rfoSheetName.substring(0, WORKBOOK_SHEET_NAME_LENGTH);
        }
        if(workbook.getSheetIndex(rfoSheetName)>-1) {
            if (rfoSheetName.length() == WORKBOOK_SHEET_NAME_LENGTH) {
                return rfoSheetName.substring(0, WORKBOOK_SHEET_NAME_LENGTH - 1) + (++sheetNameCount);
            } else {
                return rfoSheetName + (++sheetNameCount);
            }
        } else {
            return rfoSheetName;
        }
    }

    public void marshall() {
        boolean isHeader = true;
        int rowNum = 2;
        WorksheetHeaderNodeTree worksheetHeaderNodeTree = new WorksheetHeaderNodeTree();
        if (isNotEmpty(rfoSheetModel.getRFOExportModel())) {
            for (RFOSheetModel.RFORowModel rfoRowModel : rfoSheetModel.getRFOExportModel()) {
                for (String attributeName : rfoRowModel.getAttributes().keySet()) {
                    worksheetHeaderNodeTree.addHeader(new WorksheetHeaderNodeTree.HeaderNode(rfoSheetModel.getsCode(), attributeName), true);
                }
                for (String sCode : rfoRowModel.getRFOChildrenMap().keySet()) {
                    for (RFOSheetModel.RFORowModel rfoRowModelForScode : rfoRowModel.getRFOChildrenMap().get(sCode)) {
                        populateHeader(rfoRowModelForScode, worksheetHeaderNodeTree);
                    }
                }
            }

            List<WorksheetHeaderNodeTree.HeaderNode> traversedHeaderNodes = worksheetHeaderNodeTree.traverseHeader();

            for (RFOSheetModel.RFORowModel root : rfoSheetModel.getRFOExportModel()) {
                if (isHeader) {
                    printHeader(traversedHeaderNodes);
                    isHeader = false;
                }
                rowNum = printTree(rowNum, root, traversedHeaderNodes);
            }

            Row row = sheet.getRow(1);
            if (row == null) {
                return;
            }

            setCellWidths(row);

            for (Map.Entry<Integer, String[]> restriction : rfoSheetModel.getColumnRestrictions().entrySet()) {
                addConstraint(sheet, listConstraint(asList(restriction.getValue()), sheet), restriction.getKey(), 1, false,0);
            }
            styleCells();
            //sheets are protected with it's sheet name as password.
            sheet.protectSheet(sheet.getSheetName());
        }
    }

    private void setCellWidths(Row row) {
        for(int i=0;i<row.getLastCellNum();i++) {
            if(!sheet.isColumnHidden(i)) {
                int headerLength = headerValues.get(i).length();
                final String cellBiggestValue = cellBiggestValues.get(i);
                int cellWidth = null != cellBiggestValue ? cellBiggestValue.length() : 0;
                sheet.getColumnHelper().setColWidth(i, Math.max(headerLength, cellWidth) + CELL_PADDING);
            }
        }
    }

    private void populateHeader(RFOSheetModel.RFORowModel row, WorksheetHeaderNodeTree worksheetHeaderNodeTree) {
        String sCode = row.getsCode();
        WorksheetHeaderNodeTree childHeaders = worksheetHeaderNodeTree.getChildHeader(sCode);

        if (childHeaders == null) {
            childHeaders = new WorksheetHeaderNodeTree();
            for (String attributeName : row.getAttributes().keySet()) {
                childHeaders.addHeader(new WorksheetHeaderNodeTree.HeaderNode(sCode, attributeName));
            }
            worksheetHeaderNodeTree.addChildHeaders(sCode, childHeaders);
        }

        for (String childScode : row.getRFOChildrenMap().keySet()) {
            for (RFOSheetModel.RFORowModel rfoRowModel : row.getRFOChildrenMap().get(childScode)) {
                populateHeader(rfoRowModel, childHeaders);

            }
        }
    }


    private int printTree(int rowNum, RFOSheetModel.RFORowModel root, List<WorksheetHeaderNodeTree.HeaderNode> headerNodes) {
        int childTotalCount = root.getLeafNodeCount();
        int updatedRowNum = addEmptyRows(rowNum, childTotalCount);

        updateRows(root, childTotalCount, rowNum, headerNodes);

        return updatedRowNum;
    }

    private int updateRows(RFOSheetModel.RFORowModel rfoRowModel, int childTotalCount, int startRow, List<WorksheetHeaderNodeTree.HeaderNode> headerNodes) {
        Map<String, String> attributes = rfoRowModel.getAttributes();
        for (String attributeName : attributes.keySet()) {
            final int cellNumber = getColumnIndex(headerNodes, rfoRowModel, attributeName);
            combineCell(startRow, childTotalCount, cellNumber);
            writeAttributeValue(attributes.get(attributeName), startRow, cellNumber, rfoRowModel.getLockedColumns(attributeName),rfoRowModel.getGrayOutColumns(attributeName));
            populateLov(attributes.get(attributeName), rfoRowModel.getAllowedValues(attributeName), cellNumber,rfoRowModel.getGrayOutColumns(attributeName), rfoRowModel.getAllowOverride(attributeName));
        }
        int totalRows = 0;
        if(rfoRowModel.hasChildren()){
            for (String sCode: rfoRowModel.getRFOChildrenMap().keySet()) {
                for (RFOSheetModel.RFORowModel child: rfoRowModel.getChildren(sCode)) {
                    totalRows += updateRows(child, child.getLeafNodeCount(), startRow + totalRows, headerNodes);
                }
            }

            return totalRows;
        } else {
            return 1;
        }
    }

    private int getColumnIndex(List<WorksheetHeaderNodeTree.HeaderNode> headerNodeList, RFOSheetModel.RFORowModel rfoRowModel, String columnName) {
        WorksheetHeaderNodeTree.HeaderNode headerNode = new WorksheetHeaderNodeTree.HeaderNode(rfoRowModel.getsCode(), columnName);
        int index = headerNodeList.indexOf(headerNode);
        if (index < 0) {
            throw new IllegalStateException("Value does not match with the Column Name");
        }
        return index;
    }

    private void writeAttributeValue(String value, int startRow, int startCell, boolean shouldLockCell, boolean shouldGrayOutCell) {
        XSSFRow row = sheet.getRow(startRow);
        Cell cell = row.createCell(startCell, CELL_TYPE_STRING);
        cell.setCellStyle(shouldGrayOutCell? textFormatWithCellGrayOut: shouldLockCell ? textFormatWithCellLocked : textFormat);
        String cellValue = String.valueOf(value).equals("null") ? "" : value;
        cell.setCellValue(cellValue);
        putBiggestCellValue(startCell, cellValue);
    }

    private void putBiggestCellValue(int cellLocation, String newValue) {
        String currentValue = cellBiggestValues.get(cellLocation);

        if(null == currentValue || newValue.length() > currentValue.length()) {
            cellBiggestValues.put(cellLocation, newValue);
        }
    }

    private void populateLov(String currentValue, List<String> values, int startCell, boolean isGradOutCell, Boolean allowUserOverride) {
        if (values != null && !isGradOutCell) {
            List<String> fullValuesList = newArrayList(values);
            if(allowUserOverride && currentValue!=null && !values.contains(currentValue)){
                fullValuesList.add(0, currentValue);
            }
            addConstraint(sheet, listConstraint(fullValuesList, sheet), startCell, 2, allowUserOverride,startCell) ;
        }
    }

    private void combineCell(int startRow, int childTotalCount, int cellNumber) {
        sheet.addMergedRegion(new CellRangeAddress(startRow, startRow + childTotalCount - 1, cellNumber, cellNumber));
    }

    private int addEmptyRows(int rowNum, int childTotalCount) {
        int endColumn = sheet.getRow(1).getLastCellNum();
        for(int i = 0; i < childTotalCount; i++){
            Row row = sheet.createRow(rowNum);
            for(int j= 0;j<endColumn;j++){
              Cell cell = row.createCell(j, CELL_TYPE_STRING);
                cell.setCellValue("");
                styler.styleGrayCell(cell);
            }
            rowNum ++;
        }
        return rowNum;
    }

    private void styleCells() {
        final List<RFOSheetModel.RFORowModel> rfoExportModel = rfoSheetModel.getRFOExportModel();
        styler.styleCells(sheet, rfoExportModel.get(0).getAttributes().size(), rfoExportModel.size());
    }

    private Row printHeader(List<WorksheetHeaderNodeTree.HeaderNode> traversedHeaderNodes) {
        XSSFRow hiddenHeaderRow = sheet.createRow(0);

        Row headerRow = sheet.createRow(1);
        final Iterator<WorksheetHeaderNodeTree.HeaderNode> valuesIterator = traversedHeaderNodes.iterator();
        for (int i = 0; i < traversedHeaderNodes.size(); i++) {
            WorksheetHeaderNodeTree.HeaderNode value = valuesIterator.next();
            String scode = value.getScode();
            String column = value.getColumn();
            List<String> comments = newArrayList();
            if (column.contains(OPTIONAL_FLAG)) {
                comments = getComments(scode, column);
            }
            createInitializedHeaderCell(hiddenHeaderRow, i, scode);
            createInitializedHeaderCell(headerRow, i, column, comments);
        }
        return headerRow;
    }

    private List<String> getComments(String scode, String column) {
        List<String> comments = newArrayList();
        for (RFOSheetModel.RFORowModel rfoRowModel : rfoSheetModel.getRFOExportModel()) {
            if (rfoRowModel.getsCode().equals(scode)) {
                comments = getCommentsFromConditionalAttributes(rfoRowModel, column, comments);
                return comments;
            } else {
                comments = recursivelyGetComments(rfoRowModel, scode, column, comments);
            }
        }
        return comments;
    }

    private List<String> recursivelyGetComments(RFOSheetModel.RFORowModel rfoRowModel, String scode, String column, List<String> comments) {
        for (Map.Entry<String, List<RFOSheetModel.RFORowModel>> entry : rfoRowModel.getRFOChildrenMap().entrySet()) {
            if (entry.getKey().equals(scode)) {
                for (RFOSheetModel.RFORowModel model : entry.getValue()) {
                    comments = getCommentsFromConditionalAttributes(model, column, comments);
                }
                return comments;
            } else {
                for (RFOSheetModel.RFORowModel rowModel : entry.getValue()) {
                    comments = recursivelyGetComments(rowModel, scode, column, comments);
                }
            }
        }
        return comments;
    }

    private List<String> getCommentsFromConditionalAttributes(RFOSheetModel.RFORowModel rfoRowModel, String column, List<String> comments) {
        for (Map.Entry<String, List<String>> entry : rfoRowModel.getConditionalAttributes().entrySet()) {
            if (entry.getKey().equals(column)) {
                for (String errorText : entry.getValue()) {
                    comments.add(errorText);
                }
            }
        }
        return comments;
    }

    private void createInitializedHeaderCell(XSSFRow headerRow, int columnIndex, String columnValue) {
        XSSFCell cell = headerRow.createCell(columnIndex, CELL_TYPE_STRING);
        styler.styleHeaderCell(cell);
        cell.setCellValue(columnValue);
        if (LIST_OF_HIDDEN_COLUMNS.contains(columnValue)) {
            sheet.setColumnHidden(columnIndex, true);
        }

        headerRow.setZeroHeight(true);
        headerRow.setHeight((short) 0);
    }

    private void createInitializedHeaderCell(Row headerRow, int columnIndex, String columnValue, List<String> comments) {
        Cell cell = headerRow.createCell(columnIndex, CELL_TYPE_STRING);
        styler.styleHeaderCell(cell);
        cell.setCellValue(columnValue);
        headerValues.put(columnIndex, columnValue);
        if (LIST_OF_HIDDEN_COLUMNS.contains(columnValue)) {
            sheet.setColumnHidden(columnIndex, true);
        }
        if (!comments.isEmpty()) {
            CreationHelper factory = workbook.getCreationHelper();
            Drawing drawing = sheet.createDrawingPatriarch();

            ClientAnchor anchor = factory.createClientAnchor();
            anchor.setCol1(cell.getColumnIndex());
            anchor.setCol2(cell.getColumnIndex() + 2);
            anchor.setRow1(headerRow.getRowNum());
            anchor.setRow2(headerRow.getRowNum() + 3);

            Comment commentInCell = drawing.createCellComment(anchor);
            String stringComments = "";
            for (String comment : comments) {
               stringComments = stringComments.concat(comment + "\n");
            }
            RichTextString str = factory.createRichTextString(stringComments);
            commentInCell.setString(str);

            cell.setCellComment(commentInCell);
        }
    }
}
