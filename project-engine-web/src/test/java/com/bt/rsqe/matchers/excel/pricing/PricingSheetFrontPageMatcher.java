package com.bt.rsqe.matchers.excel.pricing;

import com.bt.rsqe.matchers.excel.ExcelSheetCompositeMatcher;

public class PricingSheetFrontPageMatcher extends ExcelSheetCompositeMatcher<PricingSheetFrontPageMatcher> {

    public static PricingSheetFrontPageMatcher aPricingSheetFrontPage() {
        return new PricingSheetFrontPageMatcher();
    }

    private PricingSheetFrontPageMatcher() {
        super("Front Page");
    }

    public PricingSheetFrontPageMatcher withCustomerName(final String expected) {
        expectCellWithValue("customerName", expected);
        return this;
    }

    public PricingSheetFrontPageMatcher withBuildingNumber(final String expected) {
        expectCellWithValue("buildingNumber", expected);
        return this;
    }

    public PricingSheetFrontPageMatcher withAddressLine1(final String expected) {
        expectCellWithValue("addressLine1", expected);
        return this;
    }

    public PricingSheetFrontPageMatcher withAddressLine2(final String expected) {
        expectCellWithValue("addressLine2", expected);
        return this;
    }

    public PricingSheetFrontPageMatcher withCity(final String expected) {
        expectCellWithValue("city", expected);
        return this;
    }

    public PricingSheetFrontPageMatcher withCountyOrState(final String expected) {
        expectCellWithValue("countyState", expected);
        return this;
    }

    public PricingSheetFrontPageMatcher withCountryPostcode(final String expected) {
        expectCellWithValue("countryPostcode", expected);
        return this;
    }

    public PricingSheetFrontPageMatcher withBtSubsidiaryName(final String expected) {
        expectCellWithValue("btSubsidiaryName", expected);
        return this;
    }

    public PricingSheetFrontPageMatcher withPricingStatus(final String expected) {
        expectCellWithValue("pricingStatus", expected);
        return this;
    }

    public PricingSheetFrontPageMatcher withAccountManagerName(final String expected) {
        expectCellWithValue("btAccountManagerName", expected);
        return this;
    }

    public PricingSheetFrontPageMatcher withAccountManagerPhone(final String expected) {
        expectCellWithValue("btAccountManagerPhone", expected);
        return this;
    }

    public PricingSheetFrontPageMatcher withAccountManagerFax(final String expected) {
        expectCellWithValue("btAccountManagerFax", expected);
        return this;
    }

    public PricingSheetFrontPageMatcher withAccountManagerEmail(final String expected) {
        expectCellWithValue("btAccountManagerEmail", expected);
        return this;
    }
}
