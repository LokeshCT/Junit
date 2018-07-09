package com.bt.rsqe.projectengine.web.quoteoptionorders.rfosheet;

import com.bt.rsqe.domain.bom.parameters.OrderFormSignDate;
import com.bt.rsqe.utils.DateTimeSplitter;
import com.bt.rsqe.utils.NullableOptional;
import com.bt.rsqe.web.rest.dto.types.JaxbDateTime;
import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.bt.rsqe.excel.ExcelUtil.*;
import static com.google.common.base.Strings.*;
import static org.apache.commons.lang.StringUtils.*;

public class OrderSheetDemarshaller {

    private Workbook rfoWorkbook;

    public OrderSheetDemarshaller(Workbook rfoWorkbook) {
        this.rfoWorkbook = rfoWorkbook;
    }

    public List<OrderSheetModel.OrderSheetRow> getOrderDetailsModel() {
        final Logger logger = Logger.getLogger(OrderSheetDemarshaller.class.getName());
        Sheet orderSheet = rfoWorkbook.getSheet(OrderSheetMarshaller.SHEET_NAME);
        List<OrderSheetModel.OrderSheetRow> orderSheetRows = new ArrayList<OrderSheetModel.OrderSheetRow>();
        for (Row row : orderSheet) {
            if (isHeaderRow(row)) {
                continue;
            }
            String lineItemId = getValueAtCell(row, OrderSheetMarshaller.Column.LINE_ITEM_ID.column);
            if (!isBlank(lineItemId) && !lineItemId.equalsIgnoreCase("0.0")) {
                String siteId = getValueAtCell(row, OrderSheetMarshaller.Column.SITE_ID.column);
                String siteName = getValueAtCell(row, OrderSheetMarshaller.Column.SITE_NAME.column);
                String summary = getValueAtCell(row, OrderSheetMarshaller.Column.SUMMARY.column);
                String productName = emptyToNull(getStringValueAtCell(row, OrderSheetMarshaller.Column.PRODUCT_NAME.column));
                String subLocationName = getStringValueAtCell(row, OrderSheetMarshaller.Column.SUBLOCATION_NAME.column);
                String floor = getStringValueAtCell(row, OrderSheetMarshaller.Column.FLOOR.column);
                String room = getStringValueAtCell(row, OrderSheetMarshaller.Column.ROOM.column);
                String billingId = emptyToNull(getStringValueAtCell(row, OrderSheetMarshaller.Column.BILLING_ID.column));
                String orderSignedDateString = getStringValueAtCell(row, OrderSheetMarshaller.Column.SIGNED_DATE.column);
                DateTime orderSignedDate = DateTimeSplitter.convertStringUTCToDateTime(orderSignedDateString);
                String customerRequiredDateString = getStringValueAtCell(row, OrderSheetMarshaller.Column.CUSTOMER_REQUIRED_DATE.column);
                DateTime customerRequiredDate = DateTimeSplitter.convertStringUTCToDateTime(customerRequiredDateString);
				String expedioReference = emptyToNull(getStringValueAtCell(row, OrderSheetMarshaller.Column.EXPEDIO_REFERENCE.column));
                NullableOptional<Date> initialBillingStartDate = !orderSheet.isColumnHidden(OrderSheetMarshaller.Column.INITIAL_BILLING_START_DATE.column)
                                                                    ? NullableOptional.of(getSimpleDateValueAtCell(row, OrderSheetMarshaller.Column.INITIAL_BILLING_START_DATE.column))
                                                                    : NullableOptional.<Date>absent();

                orderSheetRows.add(new OrderSheetModel.OrderSheetRow(lineItemId,
                                                                     siteId,
                                                                     siteName,
                                                                     summary,
                                                                     productName,
                                                                     subLocationName,
                                                                     floor,
                                                                     room,
                                                                     OrderFormSignDate.newInstance(new DateTime(orderSignedDate)),
                                                                     billingId,
                                                                     JaxbDateTime.valueOf(customerRequiredDate),
                                                                     initialBillingStartDate,"", expedioReference));
            }
        }
        return orderSheetRows;
    }
}

