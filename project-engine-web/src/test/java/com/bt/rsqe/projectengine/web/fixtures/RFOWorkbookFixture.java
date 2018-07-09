package com.bt.rsqe.projectengine.web.fixtures;

import com.bt.rsqe.projectengine.web.quoteoptionorders.rfosheet.OrderSheetMarshaller;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.bt.rsqe.projectengine.web.quoteoptionorders.rfosheet.OrderSheetMarshaller.Column.*;

public class RFOWorkbookFixture {
    private Workbook workbook;
    private OrderDetailsSheetFixture orderDetailsSheetFixture;
    private OnevoiceSheetFixture onevoiceSheetFixture;

    public static RFOWorkbookFixture anRFOWorkbook() {
        return new RFOWorkbookFixture();
    }

    private RFOWorkbookFixture() {
        this.workbook = new HSSFWorkbook();
    }

    public Workbook build() {
        if (orderDetailsSheetFixture != null) {
            orderDetailsSheetFixture.build(workbook);
        }
        if (onevoiceSheetFixture != null) {
            onevoiceSheetFixture.build(workbook);
        }
        return workbook;
    }

    public RFOWorkbookFixture with(OrderDetailsSheetFixture orderDetailsSheetFixture) {
        this.orderDetailsSheetFixture = orderDetailsSheetFixture;
        return this;
    }

    public RFOWorkbookFixture with(OnevoiceSheetFixture onevoiceSheetFixture) {
        this.onevoiceSheetFixture = onevoiceSheetFixture;
        return this;
    }

    public static class OrderDetailsSheetFixture {
        private List<OrderDetailsRowFixture> orderDetailsRowFixtures = new ArrayList<OrderDetailsRowFixture>();
        private HeaderRowFixture headerRowFixture = HeaderRowFixture.headerRow();

        public static OrderDetailsSheetFixture anOrderDetailsSheet() {
            return new OrderDetailsSheetFixture();
        }

        private OrderDetailsSheetFixture() {
        }

        public Sheet build(Workbook workbook) {
            Sheet sheet = workbook.createSheet(OrderSheetMarshaller.SHEET_NAME);
            headerRowFixture.appendToSheet(sheet);
            for (OrderDetailsRowFixture fixture : orderDetailsRowFixtures) {
                fixture.appendToSheet(sheet);
            }
            return sheet;
        }

        public OrderDetailsSheetFixture with(OrderDetailsRowFixture orderDetailsRowFixture) {
            this.orderDetailsRowFixtures.add(orderDetailsRowFixture);
            return this;
        }

        public OrderDetailsSheetFixture with(HeaderRowFixture headerRowFixture) {
            this.headerRowFixture = headerRowFixture;
            return this;
        }
    }

    public static class OrderDetailsRowFixture {
        private String siteId;
        private String siteName;
        private String productName;
        private DateTime orderSignDate;
        private String billingId;
        private String lineItemId;

        public static OrderDetailsRowFixture orderDetailsRow() {
            return new OrderDetailsRowFixture();
        }

        private OrderDetailsRowFixture() {
            this.siteId = UUID.randomUUID().toString();
            this.siteName = "My Site Name";
            this.orderSignDate = DateTimeFormat.forPattern("dd/MM/yyyy").parseDateTime("01/01/1970");
            this.billingId = UUID.randomUUID().toString();
        }

        public void appendToSheet(Sheet sheet) {
            Row row = sheet.createRow(sheet.getLastRowNum() + 1);
            row.createCell(LINE_ITEM_ID.column).setCellValue(lineItemId);
            row.createCell(SITE_ID.column).setCellValue(siteId);
            row.createCell(SITE_NAME.column).setCellValue(siteName);
            row.createCell(PRODUCT_NAME.column).setCellValue(productName);
            row.createCell(SIGNED_DATE.column).setCellValue(orderSignDate.toString("dd/MM/yyyy"));
            row.createCell(BILLING_ID.column).setCellValue(billingId);
        }

        public OrderDetailsRowFixture withSiteId(String siteId) {
            this.siteId = siteId;
            return this;
        }

        public OrderDetailsRowFixture withProductName(String name) {
            this.productName = name;
            return this;
        }

        public OrderDetailsRowFixture withSiteName(String siteName) {
            this.siteName = siteName;
            return this;
        }

        public OrderDetailsRowFixture withOrderSignDate(String orderSignDate) {
            this.orderSignDate = DateTimeFormat.forPattern("dd/MM/yyyy").parseDateTime(orderSignDate);
            return this;
        }

        public OrderDetailsRowFixture withOrderSignDate(DateTime orderSignDate) {
            this.orderSignDate = orderSignDate;
            return this;
        }

        public OrderDetailsRowFixture withBillingId(String billingId) {
            this.billingId = billingId;
            return this;
        }

        public OrderDetailsRowFixture withLineItemId(String lineItemId) {
            this.lineItemId = lineItemId;
            return this;
        }
    }

    public static class OnevoiceSheetFixture {
        private List<AttributesRowFixture> attributesRowFixtures = new ArrayList<AttributesRowFixture>();
        private HeaderRowFixture headerRowFixture = HeaderRowFixture.headerRow();

        public static OnevoiceSheetFixture aOnevoiceSheet() {
            return new OnevoiceSheetFixture();
        }

        private OnevoiceSheetFixture() {
        }

        public Sheet build(Workbook workbook) {
            Sheet sheet = workbook.createSheet("Onevoice");
            headerRowFixture.appendToSheet(sheet);
            for (AttributesRowFixture fixture : attributesRowFixtures) {
                fixture.appendToSheet(sheet);
            }
            return sheet;
        }

        public OnevoiceSheetFixture with(AttributesRowFixture attributesRowFixture) {
            this.attributesRowFixtures.add(attributesRowFixture);
            return this;
        }

        public OnevoiceSheetFixture with(HeaderRowFixture headerRowFixture) {
            this.headerRowFixture = headerRowFixture;
            return this;
        }
    }

    public static class HeaderRowFixture {
        AttributesRowFixture attributesRowFixture = new AttributesRowFixture();

        public static HeaderRowFixture headerRow() {
            return new HeaderRowFixture().withHeaders();
        }


        public HeaderRowFixture withHeaders(String... headers) {
            attributesRowFixture = attributesRowFixture.withLineItemId(LINE_ITEM_ID.header).withSiteId(SITE_ID.header).withSiteName(SITE_NAME.header);
            for (String header : headers) {
                attributesRowFixture.withAttributeValue(header);
            }
            return this;
        }

        public void appendToSheet(Sheet sheet) {
            attributesRowFixture.appendToSheet(sheet, 0);
        }
    }

    public static class AttributesRowFixture {
        private String lineItemId;
        private String siteId;
        private String siteName;
        private List<String> values;

        public static AttributesRowFixture productAttributeRow() {
            return new AttributesRowFixture();
        }

        private AttributesRowFixture() {
            this.lineItemId = UUID.randomUUID().toString();
            this.siteId = UUID.randomUUID().toString();
            this.siteName = "My Site Name";
            this.values = new ArrayList<String>();
        }

        public void appendToSheet(Sheet sheet) {
            appendToSheet(sheet, sheet.getLastRowNum() + 1);
        }

        public void appendToSheet(Sheet sheet, int rowNum) {
            final Row row = sheet.createRow(rowNum);
            int cellCount = 0;
            row.createCell(cellCount++).setCellValue(lineItemId);
            row.createCell(cellCount++).setCellValue(siteId);
            row.createCell(cellCount++).setCellValue(siteName);
            for (String value : values) {
                row.createCell(cellCount++).setCellValue(value);
            }
        }

        public AttributesRowFixture withLineItemId(String lineItemId) {
            this.lineItemId = lineItemId;
            return this;
        }

        public AttributesRowFixture withSiteId(String siteId) {
            this.siteId = siteId;
            return this;
        }

        public AttributesRowFixture withSiteName(String siteName) {
            this.siteName = siteName;
            return this;
        }

        public AttributesRowFixture withAttributeValue(String attributeValue) {
            this.values.add(attributeValue);
            return this;
        }

    }

}
