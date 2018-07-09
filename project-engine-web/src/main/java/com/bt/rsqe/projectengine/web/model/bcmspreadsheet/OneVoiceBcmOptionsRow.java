package com.bt.rsqe.projectengine.web.model.bcmspreadsheet;

import com.bt.rsqe.Money;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

import java.math.BigDecimal;

import static com.bt.rsqe.excel.ExcelUtil.*;

public class OneVoiceBcmOptionsRow extends BcmRow {

    protected static final int SITE_ID = 1;
    protected static final int VPN_CONFIG_DISCOUNT = 6;
    protected static final int VPN_SUBSCRIPTION_DISCOUNT = 9;
    protected static final int DIALPLAN_CHANGE_DISCOUNT = 12;
    protected static final int MMAC_CONFIG_DISCOUNT = 15;
    protected static final int AMENDMENT_CHARGE = 16;
    protected static final int CANCELLATION_CHARGE = 17;
    private Row row;

    public OneVoiceBcmOptionsRow(Row row) {
        this.row = row;
    }

    public String siteId() {
        final Cell siteIdCell = row.getCell(SITE_ID);
        validateNumericCell(siteIdCell, "Site Id", row);

        if (siteIdCell == null || isCellString(siteIdCell) || isCellBlank(siteIdCell)) {
            return "";
        }

        String siteId = Double.toString(siteIdCell.getNumericCellValue());
        siteId = siteId.substring(0, siteId.indexOf("."));
        return siteId;
    }

    public BigDecimal vpnConfigDiscount() {
        return getPercentageCellValue(VPN_CONFIG_DISCOUNT, "vpn Config Discount", row);
    }

    public BigDecimal vpnSubscriptionDiscount() {
        return getPercentageCellValue(VPN_SUBSCRIPTION_DISCOUNT, "vpn Subscription Discount", row);
    }

    public BigDecimal dialplanChangeConfigDiscount() {
        return getPercentageCellValue(DIALPLAN_CHANGE_DISCOUNT, "dialplan Change Config Discount", row);
    }

    public BigDecimal mmacConfigDiscount() {
        return getPercentageCellValue(MMAC_CONFIG_DISCOUNT, "mmac Config Discount", row);
    }

    public Money amendmentCharge() {
        return getMoneyCellValue(AMENDMENT_CHARGE, "amendment Charge");
    }

    public Money cancellationCharge() {
        return getMoneyCellValue(CANCELLATION_CHARGE, "cancellation Charge");
    }

    public boolean hasAmendmentCharge() {
        return isCellValid(row.getCell(AMENDMENT_CHARGE));
    }

    public boolean hasCancellationCharge() {
        return isCellValid(row.getCell(CANCELLATION_CHARGE));
    }

    private Money getMoneyCellValue(int cellIndex, String columnName) {
        final Cell cell = row.getCell(cellIndex);
        validateNumericCell(cell, columnName,row);

        if (cell == null || isCellString(cell) || isCellBlank(cell)) {
            return Money.ZERO;
        }

        return Money.from(Double.toString(cell.getNumericCellValue()));
    }



}
