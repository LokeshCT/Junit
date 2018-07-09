package com.bt.rsqe.projectengine.web.model.bcmspreadsheet;

import com.bt.rsqe.Percentage;
import com.bt.rsqe.logging.Log;
import com.bt.rsqe.logging.LogFactory;
import com.bt.rsqe.logging.LogLevel;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

import java.math.BigDecimal;

import static com.bt.rsqe.excel.ExcelUtil.*;

public class OneVoiceChannelInformationRow extends BcmRow {

    public static final int DISCOUNT_CELL_INDEX_FOR_PTP_OR_RRP_CONFIG = 14;
    public static final int DISCOUNT_CELL_INDEX_FOR_PTP_OR_RRP_SUBSCRIPTION = 19;
    private final Logger logger = LogFactory.createDefaultLogger(Logger.class);

    private final Row row;

    public OneVoiceChannelInformationRow(Row row) {
        this.row = row;
    }

    public Percentage getPTPConfigDiscount() {
        return getPercentageForCell(DISCOUNT_CELL_INDEX_FOR_PTP_OR_RRP_CONFIG);
    }

    public Percentage getPTPSubscriptionDiscount() {
        return getPercentageForCell(DISCOUNT_CELL_INDEX_FOR_PTP_OR_RRP_SUBSCRIPTION);
    }

    public Percentage getRRPConfigDiscount() {
        return getPercentageForCell(DISCOUNT_CELL_INDEX_FOR_PTP_OR_RRP_CONFIG);
    }

    public Percentage getRRPSubscriptionDiscount() {
        return getPercentageForCell(DISCOUNT_CELL_INDEX_FOR_PTP_OR_RRP_SUBSCRIPTION);
    }

    private Percentage getPercentageForCell(int index) {
        final Cell cell = row.getCell(index);

        if (cell == null || isCellBlank(cell) || (isCellString(cell) && isCellValid(cell))) {
            return Percentage.ZERO;
        }

        if (isCellNumeric(cell)) {
            return Percentage.from(new BigDecimal(cell.getNumericCellValue()).movePointRight(2));
        }

        logger.logNonNumericDiscount(cell);
        return Percentage.NIL;
    }

    public interface Logger {
        @Log(level = LogLevel.DEBUG, format = "Cell for discount does not contain a numeric value: %s")
        void logNonNumericDiscount(Cell cell);
    }

}
