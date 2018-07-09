package com.bt.rsqe.projectengine.web.userImport;

import com.google.common.collect.Maps;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import java.util.EnumMap;

import static com.bt.rsqe.projectengine.web.userImport.UserImportUtil.*;

public class HeaderRows {

    private EnumMap<RowType, Row> headerRowMap;

    public HeaderRows(Sheet sheet) {
        headerRowMap = Maps.newEnumMap(RowType.class);
        createHeaderRows(sheet);
    }

    private void createHeaderRows(Sheet sheet) {
        headerRowMap.put(RowType.GROUP, sheet.createRow(RowType.GROUP.index()));
        headerRowMap.put(RowType.RFQ, sheet.createRow(RowType.RFQ.index()));
        headerRowMap.put(RowType.HEADER, sheet.createRow(RowType.HEADER.index()));
        headerRowMap.put(RowType.META, sheet.createRow(RowType.META.index()));
        headerRowMap.put(RowType.ATTRIBUTE_NAME, sheet.createRow(RowType.ATTRIBUTE_NAME.index()));
    }

    public Row getGroupNameRow() {
        return headerRowMap.get(RowType.GROUP);
    }

    public Row getRfqRow() {
        return headerRowMap.get(RowType.RFQ);
    }

    public Row getHeaderRow() {
        return headerRowMap.get(RowType.HEADER);
    }

    public Row getHeaderMetaRow() {
        return headerRowMap.get(RowType.META);
    }

    public Row getAttributeNameRow() {
        return headerRowMap.get(RowType.ATTRIBUTE_NAME);
    }

    public void applyHeaderStyle(UserImportExcelStyler styler) {

        setCellWidths(getGroupNameRow());
        setCellWidths(getRfqRow());
        setCellWidths(getHeaderRow());
        setCellWidths(getHeaderMetaRow());

        getHeaderMetaRow().setZeroHeight(true);
        getAttributeNameRow().setZeroHeight(true);

        setStyleForAllCells(getGroupNameRow(), styler.buildStyle(StyleConfiguration.PALE_BLUE_HEADER));
        setStyleForAllCells(getRfqRow(), styler.buildStyle(StyleConfiguration.RED_HEADER));
        setStyleForAllCells(getHeaderRow(), styler.buildStyle(StyleConfiguration.CF_BLUE_HEADER));
    }

    public enum RowType {
        GROUP(0),
        RFQ(1),
        HEADER(2),
        META(3),
        ATTRIBUTE_NAME(4);

        private int rowIndex;

        RowType(int rowIndex) {
            this.rowIndex = rowIndex;
        }

        public int index() {
            return rowIndex;
        }
    }
}
