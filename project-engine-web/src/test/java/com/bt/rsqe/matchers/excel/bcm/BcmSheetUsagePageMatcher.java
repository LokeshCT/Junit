package com.bt.rsqe.matchers.excel.bcm;

import com.bt.rsqe.customerrecord.SiteDTO;
import com.bt.rsqe.matchers.excel.ExcelSheetCompositeMatcher;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static java.math.BigDecimal.*;

public class BcmSheetUsagePageMatcher extends ExcelSheetCompositeMatcher<BcmSheetUsagePageMatcher> {

    private static final String OV_USAGE_ORIGINATING_COUNTRY = "ov-usage.originating-country";
    private static final String OV_USAGE_CITY = "ov-usage.city";
    private static final String OV_USAGE_SITE_ID = "ov-usage.site-id";
    private static final String OV_USAGE_SITE_NAME = "ov-usage.site-name";
    static final String OV_USAGE_ORIGINATING_SITE_ACCESS_TYPE = "ov-usage.originating-site-access-type";
    static final String OV_USAGE_TERMINATING_COUNTRY = "ov-usage.terminating-country";
    static final String OV_USAGE_TERMINATION_TYPE = "ov-usage.termination-type";
    static final String OV_USAGE_OUTGOING_MINUTES = "ov-usage.outgoing-minutes";
    static final String OV_USAGE_INCOMING_OFFNET_MINUTES = "ov-usage.incoming-offnet-minutes";
    static final String OV_USAGE_TARIFF_TYPE = "ov-usage.tariff-type";
    static final String OV_USAGE_EUP_MINUTE = "ov-usage.eup-minute";
    static final String OV_USAGE_EUP_TOTAL = "ov-usage.eup-total";
    static final String OV_USAGE_PTP_MINUTE = "ov-usage.ptp-minute";
    static final String OV_USAGE_PTP_TOTAL = "ov-usage.ptp-total";

    private BcmSheetUsagePageMatcher() {
        super("Onevoice Usage");
    }

    public static BcmSheetUsagePageMatcher aOnevoiceUsagePage() {
        return new BcmSheetUsagePageMatcher();
    }

    public BcmSheetUsagePageMatcher with(OnevoiceUsagePageSite.Builder siteBuilder) {
        String[] siteFields = {"ov-usage.originating-country", OV_USAGE_CITY, OV_USAGE_SITE_ID,
            "ov-usage.site-name", OV_USAGE_ORIGINATING_SITE_ACCESS_TYPE, "ov-usage.tariff-type",
            OV_USAGE_TERMINATING_COUNTRY, OV_USAGE_TERMINATION_TYPE, OV_USAGE_OUTGOING_MINUTES,
            OV_USAGE_INCOMING_OFFNET_MINUTES};
        OnevoiceUsagePageSite site = siteBuilder.build();
        String[] expectedValues = {site.originatingCountry, site.city, site.siteId,
            site.siteName, site.originatingSiteAccessType, site.tariffType,
            site.terminatingCountry, site.terminationType, site.outgoingMinutes,
            site.incomingOffNetMinutes
        };
        expectRowWithValues(siteFields, expectedValues);
        return this;
    }

    public BcmSheetUsagePageMatcher withUsageRows(List<UsageRow> usageRows) {
        String[] labels = {OV_USAGE_ORIGINATING_COUNTRY, OV_USAGE_CITY, OV_USAGE_SITE_ID,
            OV_USAGE_SITE_NAME, OV_USAGE_ORIGINATING_SITE_ACCESS_TYPE, OV_USAGE_TARIFF_TYPE,
            OV_USAGE_TERMINATING_COUNTRY, OV_USAGE_TERMINATION_TYPE, OV_USAGE_OUTGOING_MINUTES,
            OV_USAGE_INCOMING_OFFNET_MINUTES, OV_USAGE_EUP_MINUTE, OV_USAGE_PTP_MINUTE,
            OV_USAGE_EUP_TOTAL, OV_USAGE_PTP_TOTAL
        };
        for (UsageRow usageRow : usageRows) {
            String[] expectedValues = {
                usageRow.originatingCountry,
                usageRow.city,
                usageRow.siteId,
                usageRow.siteName,
                usageRow.originatingSiteAccessType,
                usageRow.tariffType,
                usageRow.terminatingCountry,
                usageRow.terminationType,
                usageRow.outgoingMinutes,
                usageRow.incomingOffNetMinutes,
                usageRow.eupPerMinute,
                usageRow.ptpPerMinute,
                usageRow.eupTotal(),
                usageRow.ptpTotal()
            };
            expectRowWithValues(labels, expectedValues);
        }
        return this;
    }

    public static class OnevoiceUsagePageSite {
        private String originatingCountry = "";
        private String city = "";
        private String siteId = "";
        private String siteName = "";
        private String originatingSiteAccessType = "";
        private String tariffType = "";
        private String terminatingCountry = "";
        private String terminationType = "";
        private String outgoingMinutes = "";
        private String incomingOffNetMinutes = "";
        private String eupPerMinute = "";
        private String ptpPerMinute = "";
        private String eupTotal = "";
        private String ptpTotal = "";


        public static Builder aSite() {
            return new Builder();
        }

        public OnevoiceUsagePageSite(Builder builder) {
            this.originatingCountry = builder.originatingCountry;
            this.city = builder.city;
            this.siteId = builder.siteId;
            this.siteName = builder.siteName;
            this.originatingSiteAccessType = builder.originatingSiteAccessType;
            this.tariffType = builder.tariffType;
            this.terminatingCountry = builder.terminatingCountry;
            this.terminationType = builder.terminationType;
            this.outgoingMinutes = builder.outgoingMinutes;
            this.incomingOffNetMinutes = builder.incomingOffNetMinutes;
            this.eupPerMinute = builder.eupPerMinute;
            this.ptpPerMinute = builder.ptpPerMinute;
            this.eupTotal = builder.eupTotal;
            this.ptpTotal = builder.ptpTotal;
        }



        public static class Builder {
            private String originatingCountry = "";
            private String city = "";
            private String siteId = "";
            private String siteName = "";
            private String originatingSiteAccessType = "";
            private String tariffType = "";
            private String terminatingCountry = "";
            private String terminationType = "";
            private String outgoingMinutes = "";
            private String incomingOffNetMinutes = "";
            private String eupPerMinute = "";
            private String ptpPerMinute = "";
            public String eupTotal = "";
            public String ptpTotal = "";

            public static OnevoiceUsagePageSite.Builder aSite() {
                return new OnevoiceUsagePageSite.Builder();
            }

            public Builder withOriginatingCountry(String originatingCountry) {
                this.originatingCountry = originatingCountry;
                return this;
            }

            public Builder withCity(String city) {
                this.city = city;
                return this;
            }

            public Builder withSiteId(String siteId) {
                this.siteId = siteId;
                return this;
            }

            public Builder withSiteName(String siteName) {
                this.siteName = siteName;
                return this;
            }

            public Builder withOriginatingSiteAccessType(String originatingSiteAccessType) {
                this.originatingSiteAccessType = originatingSiteAccessType;
                return this;
            }

            public Builder withTariffType(String tariffType) {
                this.tariffType = tariffType;
                return this;
            }

            public Builder withTerminatingCountry(String terminatingCountry) {
                this.terminatingCountry = terminatingCountry;
                return this;
            }

            public Builder withTerminationType(String terminationType) {
                this.terminationType = terminationType;
                return this;
            }

            public Builder withOutgoingMinutes(String outgoingMinutes) {
                this.outgoingMinutes = outgoingMinutes;
                return this;
            }

            public Builder withIncomingOffNetMinutes(String incomingOffNetMinutes) {
                this.incomingOffNetMinutes = incomingOffNetMinutes;
                return this;
            }

            public Builder withPtpPerMinuteCharge(String charge) {
                this.ptpPerMinute = charge;
                return this;
            }

            public Builder withPtpTotalCharge(String charge) {
                this.ptpTotal = charge;
                return this;
            }

            public Builder withEupPerMinuteCharge(String charge) {
                this.eupPerMinute = charge;
                return this;
            }

            public Builder withEupTotalCharge(String charge) {
                this.eupTotal = charge;
                return this;
            }

            public OnevoiceUsagePageSite build() {
                return new OnevoiceUsagePageSite(this);
            }
        }
    }

    public static class UsageRow {
        String originatingCountry = "country1";
        String city = "city1";
        String siteId = "siteId1";
        String siteName = "siteName1";
        String originatingSiteAccessType = "MPLS";
        String tariffType = "tariffType1";
        String terminatingCountry = "terminatingCountry1";
        String terminationType = "terminationType1";
        String outgoingMinutes = "100";
        String incomingOffNetMinutes = "50";
        String eupPerMinute = "3";
        String ptpPerMinute = "2";

        public UsageRow(SiteDTO siteDTO, String terminatingCountry, String terminationType) {
            this.terminatingCountry = terminatingCountry;
            this.terminationType = terminationType;
            this.siteId = siteDTO.bfgSiteID;
            this.siteName = siteDTO.name;
            this.city = siteDTO.city;
            this.originatingCountry = siteDTO.country;
            this.eupPerMinute = new BigDecimal(new Random().nextDouble() * 3).setScale(2, ROUND_HALF_EVEN).toString();
            this.ptpPerMinute = new BigDecimal(new Random().nextDouble() * 2).setScale(2, ROUND_HALF_EVEN).toString();

        }

        public Map<String, String> toMap() {
            return new HashMap<String, String>() {{
                put(OV_USAGE_ORIGINATING_COUNTRY, originatingCountry);
                put(OV_USAGE_CITY, city);
                put(OV_USAGE_SITE_ID, siteId);
                put(OV_USAGE_SITE_NAME, siteName);
                put(OV_USAGE_ORIGINATING_SITE_ACCESS_TYPE, originatingSiteAccessType);
                put(OV_USAGE_TERMINATING_COUNTRY, terminatingCountry);
                put(OV_USAGE_TERMINATION_TYPE, terminationType);
                put(OV_USAGE_OUTGOING_MINUTES, outgoingMinutes);
                put(OV_USAGE_INCOMING_OFFNET_MINUTES, incomingOffNetMinutes);
                put(OV_USAGE_TARIFF_TYPE, tariffType);
                put(OV_USAGE_EUP_MINUTE, eupPerMinute);
                put(OV_USAGE_EUP_TOTAL, eupTotal());
                put(OV_USAGE_PTP_MINUTE, ptpPerMinute);
                put(OV_USAGE_PTP_TOTAL, ptpTotal());
            }};
        }

        String ptpTotal() {
            return new BigDecimal(ptpPerMinute)
                .multiply((
                              new BigDecimal(outgoingMinutes)
                                  .add(new BigDecimal(incomingOffNetMinutes)))).toString();
        }

        String eupTotal() {
            return new BigDecimal(eupPerMinute)
                .multiply((
                              new BigDecimal(outgoingMinutes)
                                  .add(new BigDecimal(incomingOffNetMinutes)))).toString();
        }
    }
}
