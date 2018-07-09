package com.bt.rsqe.matchers.excel.pricing;

import com.bt.rsqe.matchers.excel.ExcelSheetCompositeMatcher;

public class IndirectPricingCASummaryPageMatcher extends ExcelSheetCompositeMatcher<PricingSheetSitesPageMatcher> {
    protected String beanPath;

    public static IndirectPricingCASummaryPageMatcher aCASummaryPricingPage() {
        return new IndirectPricingCASummaryPageMatcher();
    }

    protected IndirectPricingCASummaryPageMatcher() {
        super("CA Summary Pricing Sheet");
        this.beanPath = "site.";
    }

    public IndirectPricingCASummaryPageMatcher withCustomerName(final String expected) {
        expectCellWithValue("customerName", expected);
        return this;
    }

    public IndirectPricingCASummaryPageMatcher withSiteName(final String expected) {
        expectCellWithValue("site.name", expected);
        return this;
    }

    public IndirectPricingCASummaryPageMatcher withContractTerm(final String expected) {
        expectCellWithValue("site.contractTerm", expected);
        return this;
    }

    public IndirectPricingCASummaryPageMatcher withContractId(final String expected) {
        expectCellWithValue("contractId", expected);
        return this;
    }

    public IndirectPricingCASummaryPageMatcher withCountry(final String expected) {
        expectCellWithValue("country", expected);
        return this;
    }

    public IndirectPricingCASummaryPageMatcher withPriceType(final String expected) {
        expectCellWithValue("site.priceType", expected);
        return this;
    }

    public IndirectPricingCASummaryPageMatcher withCurrency(final String expected) {
        expectCellWithValue("currency", expected);
        return this;
    }

    public IndirectPricingCASummaryPageMatcher withSalesUserName(final String expected) {
        expectCellWithValue("salesUserName", expected);
        return this;
    }

    public IndirectPricingCASummaryPageMatcher withQuoteName(final String expected) {
        expectCellWithValue("quoteName", expected);
        return this;
    }

    public IndirectPricingCASummaryPageMatcher withQuoteId(final String expected) {
        expectCellWithValue("quoteId", expected);
        return this;
    }
}
