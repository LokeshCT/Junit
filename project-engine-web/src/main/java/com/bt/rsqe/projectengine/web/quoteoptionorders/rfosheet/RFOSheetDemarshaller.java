package com.bt.rsqe.projectengine.web.quoteoptionorders.rfosheet;

import com.bt.rsqe.logging.Log;
import com.bt.rsqe.logging.LogFactory;
import com.bt.rsqe.logging.LogLevel;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.collect.Sets.*;

public class RFOSheetDemarshaller {
    protected static Logger logger = LogFactory.createDefaultLogger(Logger.class);

    public static RFOSheetModel updateModelUsingSheet(RFOSheetModel expectedRfoSheetModel, Sheet sheet) {
        int rowNum = 2;
        Row sCodeRow = sheet.getRow(0);
        Row attributeRow = sheet.getRow(1);
        List<WorksheetHeaderNodeTree.HeaderNode> headerNodes = new ArrayList<WorksheetHeaderNodeTree.HeaderNode>();
        int numberOfCells = sCodeRow.getLastCellNum();
        for (int i = 0; i < numberOfCells; i++) {
            WorksheetHeaderNodeTree.HeaderNode headerNode = new WorksheetHeaderNodeTree.HeaderNode(sCodeRow.getCell(i).getStringCellValue(), attributeRow.getCell(i).getStringCellValue());
            headerNodes.add(headerNode);
        }
        for (RFOSheetModel.RFORowModel expectedRowModels : expectedRfoSheetModel.getRFOExportModel()) {
           rowNum += readRows(expectedRowModels, rowNum, headerNodes, sheet);
        }
        return expectedRfoSheetModel;
    }

    private static int readRows(RFOSheetModel.RFORowModel rfoRowModel, int startRow, List<WorksheetHeaderNodeTree.HeaderNode> headerNodes, Sheet sheet) {
        Map<String, String> attributes = rfoRowModel.getAttributes();

        Set<String> sheetAttributes = getAttributesFromSheet(headerNodes, rfoRowModel.getsCode());

        if(!isRfoAttributesPresentInOffering(removeOptionalFlags(sheetAttributes), removeOptionalFlags(attributes.keySet()))){
            logger.sheetAttributes(sheetAttributes);
            logger.rfoAttributes(attributes.keySet());
            throw new RFOImportException("Product model has been changed since last export. Please download a new template from the system and Import your data again");
        }

        for (String attributeName : attributes.keySet()) {
            final int cellNumber = getColumnIndex(headerNodes, rfoRowModel, attributeName);
            String attributeValue = readAttributeValue(sheet, startRow, cellNumber);
            rfoRowModel.getAttributes().put(attributeName, attributeValue);
        }
        int totalRows = 0;
        if (rfoRowModel.hasChildren()) {
            for (String sCode : rfoRowModel.getRFOChildrenMap().keySet()) {
                for (RFOSheetModel.RFORowModel child : rfoRowModel.getChildren(sCode)) {
                    totalRows += readRows(child, startRow + totalRows, headerNodes, sheet);
                }
            }
            return totalRows;
        } else {
            return 1;
        }
    }

    private static int getColumnIndex(List<WorksheetHeaderNodeTree.HeaderNode> headerNodeList, RFOSheetModel.RFORowModel rfoRowModel, String columnName) {
        WorksheetHeaderNodeTree.HeaderNode headerNode = new WorksheetHeaderNodeTree.HeaderNode(rfoRowModel.getsCode(), columnName);
        int index = headerNodeList.indexOf(headerNode);
        if (index < 0) {
            throw new IllegalStateException("Value does not match with the Column Name");
        }
        return index;
    }

    private static Set<String> removeOptionalFlags(Set<String> attributes) {
        return newHashSet(Iterables.transform(attributes, new Function<String, String>() {
            @Override
            public String apply(@Nullable String attribute) {
                if(attribute.contains("(O)") || attribute.contains("(M)")) {
                    return attribute.substring(0, attribute.length() - 4);
                } else {
                    return attribute;
                }
            }
        }));

    }

    private static boolean isRfoAttributesPresentInOffering(Set<String> sheetAttributes, Set<String> expectedRfoAttributes) {
        return sheetAttributes.containsAll(expectedRfoAttributes);
    }

    private static Set<String> getAttributesFromSheet(List<WorksheetHeaderNodeTree.HeaderNode> headerNodes, final String sCode) {

        Collection<WorksheetHeaderNodeTree.HeaderNode> collection = Collections2.filter(headerNodes, new Predicate<WorksheetHeaderNodeTree.HeaderNode>() {
            @Override
            public boolean apply(@Nullable WorksheetHeaderNodeTree.HeaderNode input) {
                return input.getScode().equals(sCode);
            }
        });

        return newHashSet(Iterables.transform(collection, new Function<WorksheetHeaderNodeTree.HeaderNode, String>() {
            @Override
            public String apply(@Nullable WorksheetHeaderNodeTree.HeaderNode input) {
                return input.getColumn();
            }
        }));
    }

    private static String readAttributeValue(Sheet sheet, int rowNum, int colNum) {
        Cell currentColumn = sheet.getRow(rowNum).getCell(colNum);
        return currentColumn != null ? currentColumn.getStringCellValue() : "";
    }

    interface Logger {
        @Log(level = LogLevel.INFO, format = "Sheet Attributes are %s")
        void sheetAttributes(Set<String> sheetAttributes);

        @Log(level = LogLevel.INFO, format = "RFO Attributes are %s")
        void rfoAttributes(Set<String> rfoAttributes);
    }
}