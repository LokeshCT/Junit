package com.bt.rsqe.matchers.excel.pricing;

import com.bt.rsqe.matchers.excel.ExcelSheetCompositeMatcher;

public class IndirectPricingCADetailedPageMatcher extends ExcelSheetCompositeMatcher<IndirectPricingCADetailedPageMatcher> {
    protected String beanPath;

    public static IndirectPricingCADetailedPageMatcher aCADetailedPricingPage() {
        return new IndirectPricingCADetailedPageMatcher();
    }

    protected IndirectPricingCADetailedPageMatcher() {
        super("CA Detailed Pricing Sheet");
        this.beanPath = "site.";
    }

    public IndirectPricingCADetailedPageMatcher withCustomerName(final String expected) {
        expectCellWithValue("customerName", expected);
        return this;
    }

    public IndirectPricingCADetailedPageMatcher withSiteName(final String expected) {
        expectCellWithValue("site.name", expected);
        return this;
    }

    public IndirectPricingCADetailedPageMatcher withContractTerm(final String expected) {
        expectCellWithValue("site.contractTerm", expected);
        return this;
    }

    public IndirectPricingCADetailedPageMatcher withContractId(final String expected) {
        expectCellWithValue("contractId", expected);
        return this;
    }

    public IndirectPricingCADetailedPageMatcher withCountry(final String expected) {
        expectCellWithValue("country", expected);
        return this;
    }

    public IndirectPricingCADetailedPageMatcher withPriceType(final String expected) {
        expectCellWithValue("site.priceType", expected);
        return this;
    }

    public IndirectPricingCADetailedPageMatcher withCurrency(final String expected) {
        expectCellWithValue("currency", expected);
        return this;
    }

    public IndirectPricingCADetailedPageMatcher withSalesUserName(final String expected) {
        expectCellWithValue("salesUserName", expected);
        return this;
    }

    public IndirectPricingCADetailedPageMatcher withQuoteName(final String expected) {
        expectCellWithValue("quoteName", expected);
        return this;
    }

    public IndirectPricingCADetailedPageMatcher withQuoteId(final String expected) {
        expectCellWithValue("quoteId", expected);
        return this;
    }
}
