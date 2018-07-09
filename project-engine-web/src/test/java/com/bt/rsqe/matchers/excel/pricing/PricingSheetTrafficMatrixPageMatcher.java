package com.bt.rsqe.matchers.excel.pricing;

import com.bt.rsqe.matchers.excel.ExcelSheetCompositeMatcher;

public class PricingSheetTrafficMatrixPageMatcher extends ExcelSheetCompositeMatcher<PricingSheetTrafficMatrixPageMatcher> {

    public PricingSheetTrafficMatrixPageMatcher(String name) {
        super(name);
    }

    public static PricingSheetTrafficMatrixPageMatcher aTrafficMatrixPage() {
        return new PricingSheetTrafficMatrixPageMatcher("Traffic Matrix");
    }


}
