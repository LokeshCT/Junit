package com.bt.rsqe.matchers.excel.bcm;

import com.bt.rsqe.matchers.excel.ExcelSheetCompositeMatcher;

import java.util.List;

import static com.google.common.collect.Lists.*;

public class BcmSheetOnevoiceChannelInformationPageMatcher extends ExcelSheetCompositeMatcher<BcmSheetOnevoiceChannelInformationPageMatcher> {

    public static BcmSheetOnevoiceChannelInformationPageMatcher aOnevoiceChannelInformationPage() {
        return new BcmSheetOnevoiceChannelInformationPageMatcher();
    }

    private BcmSheetOnevoiceChannelInformationPageMatcher() {
        super("Onevoice Channel Information");
    }

    public BcmSheetOnevoiceChannelInformationPageMatcher with(OnevoiceChannelInformationSite.Builder builder) {
        final OnevoiceChannelInformationSite site = builder.build();

        List<String> siteFields = newArrayList();
        List<String> expectedValues = newArrayList();
        if (site.country != null) {
            siteFields.add("ov-channel-info.originating-country");
            expectedValues.add(site.country);
        }
        if (site.city != null) {
            siteFields.add("ov-channel-info.city");
            expectedValues.add(site.city);
        }
        if (site.id != null) {
            siteFields.add("ov-channel-info.site-id");
            expectedValues.add(site.id);
        }
        if (site.name != null) {
            siteFields.add("ov-channel-info.site-name");
            expectedValues.add(site.name);
        }
        if (site.accessType != null) {
            siteFields.add("ov-channel-info.access-type");
            expectedValues.add(site.accessType);
        }
        if (site.tariffType != null) {
            siteFields.add("ov-channel-info.tariff-type");
            expectedValues.add(site.tariffType);
        }
        if (site.configRrpTotal != null) {
            siteFields.add("ov-channel-info.config-rrp-total");
            expectedValues.add(site.configRrpTotal);
        }
        if (site.configPtpTotal != null) {
            siteFields.add("ov-channel-info.config-ptp-total");
            expectedValues.add(site.configPtpTotal);
        }
        if (site.subscriptionRrpTotal != null) {
            siteFields.add("ov-channel-info.subscription-rrp-total");
            expectedValues.add(site.subscriptionRrpTotal);
        }
        if (site.subscriptionPtpTotal != null) {
            siteFields.add("ov-channel-info.subscription-ptp-total");
            expectedValues.add(site.subscriptionPtpTotal);
        }
        if (site.numberOfChannels != null) {
            siteFields.add("ov-channel-info.number-of-channels");
            expectedValues.add(site.numberOfChannels);
        }
        if (site.configRrpPerChannel != null) {
            siteFields.add("ov-channel-info.config-rrp-per-channel");
            expectedValues.add(site.configRrpPerChannel);
        }
        if (site.configPtpPerChannel != null) {
            siteFields.add("ov-channel-info.config-ptp-per-channel");
            expectedValues.add(site.configPtpPerChannel);
        }
        if (site.subscriptionRrpPerChannel != null) {
            siteFields.add("ov-channel-info.subscription-rrp-per-channel");
            expectedValues.add(site.subscriptionRrpPerChannel);
        }
        if (site.subscriptionPtpPerChannel != null) {
            siteFields.add("ov-channel-info.subscription-ptp-per-channel");
            expectedValues.add(site.subscriptionPtpPerChannel);
        }
        if (site.configDiscount != null) {
            siteFields.add("ov-channel-info.config-discount");
            expectedValues.add(site.configDiscount);
        }
        if (site.subscriptionDiscount != null) {
            siteFields.add("ov-channel-info.subscription-discount");
            expectedValues.add(site.subscriptionDiscount);
        }

        expectRowWithValues(siteFields.toArray(new String[0]), expectedValues.toArray(new String[0]));
        return this;
    }

    public static class OnevoiceChannelInformationSite {
        private final String id;
        private final String name;
        private final String city;
        private final String country;
        private final String accessType;
        private final String tariffType;
        private final String configRrpTotal;
        private final String configPtpTotal;
        private final String configDiscount;
        private final String subscriptionRrpTotal;
        private final String subscriptionPtpTotal;
        private final String subscriptionDiscount;
        private final String numberOfChannels;
        private final String configRrpPerChannel;
        private final String configPtpPerChannel;
        private final String subscriptionRrpPerChannel;
        private final String subscriptionPtpPerChannel;

        public OnevoiceChannelInformationSite(Builder builder) {
            this.id = builder.id;
            this.name = builder.name;
            this.city = builder.city;
            this.country = builder.country;
            this.accessType = builder.accessType;
            this.tariffType = builder.tariffType;
            this.configRrpTotal = builder.configRrpTotal;
            this.configPtpTotal = builder.configPtpTotal;
            this.configDiscount = builder.configDiscount;
            this.subscriptionRrpTotal = builder.subscriptionRrpTotal;
            this.subscriptionPtpTotal = builder.subscriptionPtpTotal;
            this.subscriptionDiscount = builder.subscriptionDiscount;
            this.numberOfChannels = builder.numberOfChannels;
            this.configRrpPerChannel = builder.configRrpPerChannel;
            this.configPtpPerChannel = builder.configPtpPerChannel;
            this.subscriptionRrpPerChannel = builder.subscriptionRrpPerChannel;
            this.subscriptionPtpPerChannel = builder.subscriptionPtpPerChannel;
        }

        public static Builder aOnevoiceChannelInformationSite() {
            return new OnevoiceChannelInformationSite.Builder();
        }

        public static class Builder {
            private String id;
            private String name;
            private String city;
            private String country;
            private String accessType;
            private String tariffType;
            private String configRrpTotal;
            private String configPtpTotal;
            private String configDiscount;
            private String subscriptionRrpTotal;
            private String subscriptionPtpTotal;
            private String subscriptionDiscount;
            private String numberOfChannels;
            private String configRrpPerChannel;
            private String configPtpPerChannel;
            private String subscriptionRrpPerChannel;
            private String subscriptionPtpPerChannel;

            public Builder withId(String id) {
                this.id = id;
                return this;
            }

            public Builder withName(String name) {
                this.name = name;
                return this;
            }

            public Builder withCity(String city) {
                this.city = city;
                return this;
            }

            public Builder withCountry(String country) {
                this.country = country;
                return this;
            }

            public Builder withAccessType(String accessType) {
                this.accessType = accessType;
                return this;
            }

            public Builder withTariffType(String tariffType) {
                this.tariffType = tariffType;
                return this;
            }

            public Builder withConfigRrpTotal(String configRrpTotal) {
                this.configRrpTotal = configRrpTotal;
                return this;
            }

            public Builder withConfigPtpTotal(String configPtpTotal) {
                this.configPtpTotal = configPtpTotal;
                return this;
            }

            public Builder withSubscriptionRrpTotal(String subscriptionRrpTotal) {
                this.subscriptionRrpTotal = subscriptionRrpTotal;
                return this;
            }

            public Builder withSubscriptionPtpTotal(String subscriptionPtpTotal) {
                this.subscriptionPtpTotal = subscriptionPtpTotal;
                return this;
            }

            public Builder withNumberOfChannels(String numberOfChannels) {
                this.numberOfChannels = numberOfChannels;
                return this;
            }

            public Builder withConfigRrpPerChannel(String configRrpPerChannel) {
                this.configRrpPerChannel = configRrpPerChannel;
                return this;
            }

            public Builder withConfigPtpPerChannel(String configPtpPerChannel) {
                this.configPtpPerChannel = configPtpPerChannel;
                return this;
            }

            public Builder withSubscriptionRrpPerChannel(String subscriptionRrpPerChannel) {
                this.subscriptionRrpPerChannel = subscriptionRrpPerChannel;
                return this;
            }

            public Builder withSubscriptionPtpPerChannel(String subscriptionPtpPerChannel) {
                this.subscriptionPtpPerChannel = subscriptionPtpPerChannel;
                return this;
            }

            public Builder withConfigDiscount(String configDiscount) {
                this.configDiscount = configDiscount;
                return this;
            }

            public Builder withSubscriptionDiscount(String subscriptionDiscount) {
                this.subscriptionDiscount = subscriptionDiscount;
                return this;
            }

            public OnevoiceChannelInformationSite build() {
                return new OnevoiceChannelInformationSite(this);
            }
        }
    }
}
