package com.bt.rsqe.matchers.excel.pricing;

public class PricingConfigSheetMatcher extends PricingSheetSitesPageMatcher {
    protected PricingConfigSheetMatcher() {
        super();
        this.beanPath = "site.";
    }

    public static PricingConfigSheetMatcher aConfigPricelinePage() {
        return new PricingConfigSheetMatcher();
    }

    public PricingConfigSheetMatcher withTotalOneTimeRRP(final String expected) {
        return (PricingConfigSheetMatcher) withCellValue(9, 10, expected);
    }
}
