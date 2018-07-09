package com.bt.rsqe.matchers.excel.bcm;

import com.bt.rsqe.customerinventory.dto.PricePointDTO;
import com.bt.rsqe.customerinventory.dto.SpecialPriceBookDTO;
import com.bt.rsqe.matchers.excel.ExcelSheetCompositeMatcher;
import org.apache.poi.ss.usermodel.Sheet;

public class BcmSheetSpecialPriceBookPageMatcher extends ExcelSheetCompositeMatcher<BcmSheetSpecialPriceBookPageMatcher> {
    private final boolean direct;

    private BcmSheetSpecialPriceBookPageMatcher(String name, boolean direct) {
        super(name);
        this.direct = direct;
    }

    public static BcmSheetSpecialPriceBookPageMatcher aSpecialPriceBookPageForDirect() {
        return new BcmSheetSpecialPriceBookPageMatcher("Special Pricebook (Direct)", true);
    }

    public static BcmSheetSpecialPriceBookPageMatcher aSpecialPriceBookPageForIndirect() {
        return new BcmSheetSpecialPriceBookPageMatcher("Special Pricebook (Indirect)", false);
    }

    @Override
    protected void doMatchesSafely(Sheet typeToMatch) {
        super.doMatchesSafely(typeToMatch);
    }


    public BcmSheetSpecialPriceBookPageMatcher withPricePointRowsFor(SpecialPriceBookDTO specialPriceBook) {
        final String[] labels = {"priceBook.name", "priceBook.originatingCountry", "priceBook.destinationCountry", "priceBook.terminationType", "priceBook.rrpPrice", "priceBook.ptpPrice", "priceBook.discount"};
        for (PricePointDTO pricePoint : specialPriceBook.getPricePoints()) {
            final PriceBookSheetRow pricePointRow = new PriceBookSheetRow(specialPriceBook.getName(), pricePoint);

            String[] values;
            if (direct) {
                values = new String[]{pricePointRow.getPriceBookName(),
                    pricePointRow.getOriginatingCountry(),
                    pricePointRow.getTerminatingCountry(),
                    pricePointRow.getTerminatingType(),
                    pricePointRow.getPtpPrice(),
                    "",
                    pricePointRow.getDiscount()};
            } else {
                values = new String[]{pricePointRow.getPriceBookName(),
                    pricePointRow.getOriginatingCountry(),
                    pricePointRow.getTerminatingCountry(),
                    pricePointRow.getTerminatingType(),
                    "",
                    pricePointRow.getPtpPrice(),
                    pricePointRow.getDiscount()};
            }
            expectRowWithValues(labels, values);
        }
        return this;
    }


    public static class PriceBookSheetRow {
        private String originatingCountry;
        private String priceBookName;
        private String terminatingCountry;
        private String terminatingType;
        private String rrpPrice;
        private String ptpPrice;
        private String discount;

        private PriceBookSheetRow(String priceBookName, PricePointDTO pricePoint) {
            this.originatingCountry = pricePoint.getOriginCountry().getDisplayName();
            this.priceBookName = priceBookName;
            this.terminatingCountry = pricePoint.getDestination().getDisplayName();
            this.terminatingType = pricePoint.getTerminationType().getDisplayName();
            this.rrpPrice = "";
            this.ptpPrice = pricePoint.getBasePrice().toString();
            this.discount = pricePoint.getDiscountValue().toString();
        }


        public String getOriginatingCountry() {
            return originatingCountry;
        }

        public String getPriceBookName() {
            return priceBookName;
        }

        public String getTerminatingCountry() {
            return terminatingCountry;
        }

        public String getTerminatingType() {
            return terminatingType;
        }

        public String getRrpPrice() {
            return rrpPrice;
        }

        public String getPtpPrice() {
            return ptpPrice;
        }

        public String getDiscount() {
            return discount;
        }
    }
}
