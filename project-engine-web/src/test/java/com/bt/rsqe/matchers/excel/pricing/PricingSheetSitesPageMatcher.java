package com.bt.rsqe.matchers.excel.pricing;

import com.bt.rsqe.matchers.excel.ExcelSheetCompositeMatcher;
import com.bt.rsqe.projectengine.web.quoteoptionpricing.PricingSheetKeys;
import com.google.common.base.Function;

import java.util.List;
import java.util.Map;

import static com.bt.rsqe.projectengine.web.model.OneVoicePriceTariff.*;
import static com.bt.rsqe.projectengine.web.quoteoptionpricing.PricingSheetKeys.*;
import static com.google.common.collect.Lists.*;
import static com.google.common.collect.Maps.*;
import static java.util.Arrays.asList;

public class PricingSheetSitesPageMatcher extends ExcelSheetCompositeMatcher<PricingSheetSitesPageMatcher> {
    protected String beanPath;
    private int siteCount = 0;

    public static PricingSheetSitesPageMatcher aPricingSheetSitesPage() {
        return new PricingSheetSitesPageMatcher();
    }

    protected PricingSheetSitesPageMatcher() {
        super("Sites Page");
        beanPath = "sites.";
    }

    public PricingSheetSitesPageMatcher withCurrency(final String expected) {
        expectCellWithValue("currency", expected);
        return this;
    }

    public PricingSheetSitesPageMatcher withPricingStatus(final String expected) {
        expectCellWithValue("pricingStatus", expected);
        return this;
    }

    public PricingSheetSitesPageMatcher withTotalUsageRRP(final String expected) {
        expectCellWithValue(TOTAL_USAGE_RRP, expected,siteCount);
        return this;
    }

    public PricingSheetSitesPageMatcher withTotalOneTimeRRP(final String expected) {
        return withCellValue(9, 10, expected);
    }

    public PricingSheetSitesPageMatcher withTotalRecurringRRP(final String expected) {
        expectCellWithValue(TOTAL_RECURRING_RRP, expected,siteCount);
        return this;
    }

    public PricingSheetSitesPageMatcher withTotalRRP(final String expected) {
        expectCellWithValue(TOTAL_RRP, expected,siteCount);
        return this;
    }

    public PricingSheetSitesPageMatcher withTotalUsagePTP(final String expected) {
        expectCellWithValue(TOTAL_USAGE_PTP, expected,siteCount);
        return this;
    }

    public PricingSheetSitesPageMatcher withTotalOneTimePTP(final String expected) {
        expectCellWithValue(TOTAL_ONE_TIME_PTP, expected,siteCount);
        return this;
    }

    public PricingSheetSitesPageMatcher withTotalRecurringPTP(final String expected) {
        expectCellWithValue(TOTAL_RECURRING_PTP, expected,siteCount);
        return this;
    }

    public PricingSheetSitesPageMatcher withTotalPTP(final String expected) {
        expectCellWithValue(TOTAL_PTP, expected,siteCount);
        return this;
    }

    public PricingSheetSitesPageMatcher withQuoteId(final String expected) {
        expectCellWithValue("quoteId", expected);
        return this;
    }

    public PricingSheetSitesPageMatcher withQuoteVersion(String expected) {
        expectCellWithValue("quoteVersion", expected);
        return this;
    }

    public PricingSheetSitesPageMatcher withQuoteName(String expected) {
        expectCellWithValue("quoteName", expected);
        return this;
    }

    public PricingSheetSitesPageMatcher withBidNumber(String expected) {
        expectCellWithValue("bidNumber", expected);
        return this;
    }

    public PricingSheetSitesPageMatcher withSalesUser(String expected) {
        expectCellWithValue("salesUserName", expected);
        return this;
    }


    public PricingSheetSitesPageMatcher withContractId(String expected) {
        expectCellWithValue(PricingSheetKeys.CONTRACT_ID, expected);
        return this;
    }

    public PricingSheetSitesPageMatcher withContractTerms(String expected) {
        expectCellWithValue(PricingSheetKeys.CONTRACT_TERM, expected);
        return this;
    }


    public PricingSheetSitesPageMatcher with(final SitesPageSiteBuilder siteBuilder) {
        String[] siteFields = getFieldNames(beanPath, siteBuilder.getAllFieldNames());
        String[] expectedValues = siteBuilder.getAllExpectedValues();

        expectRowWithValues(siteFields, expectedValues);

        this.siteCount++;
        return this;
    }

    protected String[] getFieldNames(final String beanPath, String[] fieldNames) {
        List<String> transformedList = transform(asList(fieldNames), new Function<String, String>() {
            public String apply(String fieldName) {
                return beanPath + fieldName;
            }
        });
        return transformedList.toArray(new String[0]);
    }

    public static class SitesPageSiteBuilder {

        private Map<String, Object> expectedFields = newHashMap();

        public static SitesPageSiteBuilder aSite() {
            return new SitesPageSiteBuilder();
        }

        public String[] getAllFieldNames() {
            return expectedFields.keySet().toArray(new String[]{});
        }

        public String[] getAllExpectedValues() {
            return expectedFields.values().toArray(new String[]{});
        }

        public SitesPageSiteBuilder withName(String name) {
            expectedFields.put(SITE_NAME, name);
            return this;
        }

        public SitesPageSiteBuilder withCity(String city) {
            expectedFields.put(SITE_CITY, city);
            return this;
        }

        public SitesPageSiteBuilder withUsageRRP(String usageRRP) {
            expectedFields.put(SITE_USAGE_RRP, usageRRP);
            return this;
        }

        public SitesPageSiteBuilder withOneTimeRRP(String oneTimeRRP) {
            expectedFields.put(SITE_ONE_TIME_RRP, oneTimeRRP);
            return this;
        }

        public SitesPageSiteBuilder withRecurringRRP(String recurringRRP) {
            expectedFields.put(SITE_RECURRING_RRP, recurringRRP);
            return this;
        }

        public SitesPageSiteBuilder withRRPPriceBookVersion(String rrpPriceBookVersion) {
            expectedFields.put(RRP_PRICE_BOOK_VERSION, rrpPriceBookVersion);
            return this;
        }

        public SitesPageSiteBuilder withPTPPriceBookVersion(String ptpPriceBookVersion) {
            expectedFields.put(PTP_PRICE_BOOK_VERSION, ptpPriceBookVersion);
            return this;
        }

        public SitesPageSiteBuilder withUsagePTP(String usagePTP) {
            expectedFields.put(SITE_USAGE_PTP, usagePTP);
            return this;
        }

        public SitesPageSiteBuilder withOneTimePTP(String oneTimePTP) {
            expectedFields.put(SITE_ONE_TIME_PTP, oneTimePTP);
            return this;
        }

        public SitesPageSiteBuilder withRecurringPTP(String recurringPTP) {
            expectedFields.put(SITE_RECURRING_PTP, recurringPTP);
            return this;
        }

        public SitesPageSiteBuilder withOneVoiceType(String oneVoiceType) {
            expectedFields.put(SITE_CONFIG_TYPE, oneVoiceType);
            return this;
        }

        public SitesPageSiteBuilder withTariffOption(String tariffOption) {
            expectedFields.put(SITE_TARIFF_OPTIONS, tariffOption);
            return this;
        }

        public SitesPageSiteBuilder withVoiceChannelRequired(String voiceChannelRequired) {
            expectedFields.put(SITE_VOICE_CHANNELS_REQUIRED, voiceChannelRequired);
            return this;
        }

        public SitesPageSiteBuilder withIfcStatus(String ifcStatus) {
            expectedFields.put(SITE_IFC_STATUS, ifcStatus);
            return this;
        }

        public SitesPageSiteBuilder withGlobalDirectLiteSingleChannelConfigChargePerChannelRRP(String chargePerChannel) {
            expectedFields.put(String.format(SITE_PPSR_ID_CHARGE_PER_CHANNEL_RRP, GLOBAL_DIRECT_LITE_CHANNEL_CONFIG.ppsrId()), chargePerChannel);
            return  this;
        }

        public SitesPageSiteBuilder withGlobalDirectLiteSingleChannelConfigTotalCharge(String totalCharge) {
            expectedFields.put(String.format(SITE_PPSR_ID_TOTAL_CHARGE_RRP, GLOBAL_DIRECT_LITE_CHANNEL_CONFIG.ppsrId()), totalCharge);
            return  this;
        }

        public SitesPageSiteBuilder withGlobalDirectLiteSingleChannelSubscriptionChargePerChannel(String chargePerChannel) {
            expectedFields.put(String.format(SITE_PPSR_ID_CHARGE_PER_CHANNEL_RRP, GLOBAL_DIRECT_LITE_CHANNEL_SUBSCRIPTION.ppsrId()), chargePerChannel);
            return  this;
        }

        public SitesPageSiteBuilder withGlobalDirectLiteSingleChannelSubscriptionTotalCharge(String totalCharge) {
            expectedFields.put(String.format(SITE_PPSR_ID_TOTAL_CHARGE_RRP, GLOBAL_DIRECT_LITE_CHANNEL_SUBSCRIPTION.ppsrId()), totalCharge);
            return  this;
        }


  }
}
