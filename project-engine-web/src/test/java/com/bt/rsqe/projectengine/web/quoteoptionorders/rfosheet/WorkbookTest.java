package com.bt.rsqe.projectengine.web.quoteoptionorders.rfosheet;

import com.bt.rsqe.util.TestWithRules;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.joda.time.format.DateTimeFormat;

import java.util.Date;

public abstract class WorkbookTest extends TestWithRules {
    protected void addOrderDetailsSheetRow(Sheet sheet,
                                           int rowNum,
                                           String lineItemId,
                                           String siteId,
                                           String siteName,
                                           String productName,
                                           String orderSignDate,
                                           String customerRequiredDate,
                                           Date initialBillingStartDate,
                                           String billingId) {
        final Row row = sheet.createRow(rowNum);

        row.createCell(OrderSheetMarshaller.Column.LINE_ITEM_ID.column).setCellValue(lineItemId);
        row.createCell(OrderSheetMarshaller.Column.SITE_ID.column).setCellValue(siteId);
        row.createCell(OrderSheetMarshaller.Column.SITE_NAME.column).setCellValue(siteName);
        row.createCell(OrderSheetMarshaller.Column.PRODUCT_NAME.column).setCellValue(productName);

        if (orderSignDate != null) {
            row.createCell(OrderSheetMarshaller.Column.SIGNED_DATE.column).setCellValue(DateTimeFormat.forPattern("yyyy-MMM-dd").parseDateTime(orderSignDate).toString("yyyy-MMM-dd"));
        }

        if (customerRequiredDate != null) {
            row.createCell(OrderSheetMarshaller.Column.CUSTOMER_REQUIRED_DATE.column).setCellValue(DateTimeFormat.forPattern("yyyy-MMM-dd").parseDateTime(customerRequiredDate).toString("yyyy-MMM-dd"));
        }

        Cell cell = row.createCell(OrderSheetMarshaller.Column.INITIAL_BILLING_START_DATE.column);
        if (initialBillingStartDate != null) {
            cell.setCellValue(initialBillingStartDate);
        }

        sheet.setColumnHidden(OrderSheetMarshaller.Column.BILLING_ID.column, true);
        row.createCell(OrderSheetMarshaller.Column.BILLING_ID.column).setCellValue(billingId);
    }
}
