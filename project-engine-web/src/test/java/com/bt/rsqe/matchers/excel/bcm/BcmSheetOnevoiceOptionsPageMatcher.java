package com.bt.rsqe.matchers.excel.bcm;

import com.bt.rsqe.customerrecord.SiteDTO;
import com.bt.rsqe.matchers.excel.ExcelSheetCompositeMatcher;

public class BcmSheetOnevoiceOptionsPageMatcher extends ExcelSheetCompositeMatcher<BcmSheetOnevoiceOptionsPageMatcher> {
    public static BcmSheetOnevoiceOptionsPageMatcher aOnevoiceOptionsPage() {
        return new BcmSheetOnevoiceOptionsPageMatcher();
    }

    private BcmSheetOnevoiceOptionsPageMatcher() {
        super("Onevoice Options");
    }

    public BcmSheetOnevoiceOptionsPageMatcher with(OnevoiceOptionsPageSite.Builder siteBuilder) {
        String[] siteFields = {"ov-options.site-id", "ov-options.site-name", "ov-options.site-address",
            "ov-options.vpnConfig-rrp", "ov-options.vpnConfig-ptp", "ov-options.vpnConfig-discount",
            "ov-options.vpnSubscription-rrp", "ov-options.vpnSubscription-ptp", "ov-options.vpnSubscription-discount",
            "ov-options.dialplanChangeConfig-rrp", "ov-options.dialplanChangeConfig-ptp", "ov-options.dialplanChangeConfig-discount",
            "ov-options.mmacConfig-rrp", "ov-options.mmacConfig-ptp", "ov-options.mmacConfig-discount", "ov-options.amendmentCharge",
            "ov-options.cancellationCharge"};
        OnevoiceOptionsPageSite site = siteBuilder.build();
        String[] expectedValues = {site.getId(), site.getName(), site.getAddress(),
            site.getVpnConfigRrp(), site.getVpnConfigPtp(), site.getVpnConfigDiscount(),
            site.getVpnSubscriptionRrp(), site.getVpnSubscriptionPtp(), site.getVpnSubscriptionDiscount(),
            site.getDialPlanChangeConfigRrp(), site.getDialPlanChangeConfigPtp(), site.getDialPlanChangeConfigDiscount(),
            site.getMajorMoveAddChangeRrp(), site.getMajorMoveAddChangePtp(), site.getMajorMoveAddChangeDiscount(),
            site.getAmendmentCharge(), site.getCancellationCharge()};
        expectRowWithValues(siteFields, expectedValues);
        return this;
    }


    public static class OnevoiceOptionsPageSite {
        private final String id;
        private final String name;
        private final String address;
        private final String vpnConfigRrp;
        private final String vpnConfigPtp;
        private final String vpnConfigDiscount;
        private final String vpnSubscriptionRrp;
        private final String vpnSubscriptionPtp;
        private final String vpnSubscriptionDiscount;
        private final String dialPlanChangeConfigRrp;
        private final String dialPlanChangeConfigPtp;
        private final String dialPlanChangeConfigDiscount;
        private final String majorMoveAddChangeRrp;
        private final String majorMoveAddChangePtp;
        private final String majorMoveAddChangeDiscount;
        private final String amendmentCharge;
        private final String cancellationCharge;

        public static Builder aSite() {
            return new OnevoiceOptionsPageSite.Builder();
        }

        public static Builder withCustomerOwnedSiteData() {
            return noSiteData().withName(SiteDTO.CUSTOMER_OWNED.name);
        }
        public static Builder noSiteData() {
            return aSite()
                .withVpnConfigRrp("")
                .withVpnConfigPtp("")
                .withVpnConfigDiscount("")

                .withVpnSubscriptionRrp("")
                .withVpnSubscriptionPtp("")
                .withVpnSubscriptionDiscount("")

                .withDialPlanChangeConfigRrp("")
                .withDialPlanChangeConfigPtp("")
                .withDialPlanChangeConfigDiscount("")

                .withMajorMoveAddChangeRrp("")
                .withMajorMoveAddChangePtp("")
                .withMajorMoveAddChangeDiscount("")

                .withAmendmentCharge("")
                .withCancellationCharge("");
        }

        private OnevoiceOptionsPageSite(Builder builder) {
            this.id = builder.id;
            this.name = builder.name;
            this.address = builder.address;

            this.vpnConfigRrp = builder.vpnConfigRrp;
            this.vpnConfigPtp = builder.vpnConfigPtp;
            this.vpnConfigDiscount = builder.vpnConfigDiscount;

            this.vpnSubscriptionRrp = builder.vpnSubscriptionRrp;
            this.vpnSubscriptionPtp = builder.vpnSubscriptionPtp;
            this.vpnSubscriptionDiscount = builder.vpnSubscriptionDiscount;

            this.dialPlanChangeConfigRrp = builder.dialPlanChangeConfigRrp;
            this.dialPlanChangeConfigPtp = builder.dialPlanChangeConfigPtp;
            this.dialPlanChangeConfigDiscount = builder.dialPlanChangeConfigDiscount;

            this.majorMoveAddChangeRrp = builder.majorMoveAddChangeRrp;
            this.majorMoveAddChangePtp = builder.majorMoveAddChangePtp;
            this.majorMoveAddChangeDiscount = builder.majorMoveAddChangeDiscount;

            this.amendmentCharge = builder.amendmentCharge;
            this.cancellationCharge = builder.cancellationCharge;
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getAddress() {
            return address;
        }

        public String getVpnConfigRrp() {
            return vpnConfigRrp;
        }

        public String getVpnConfigPtp() {
            return vpnConfigPtp;
        }

        public String getVpnConfigDiscount() {
            return vpnConfigDiscount;
        }

        public String getVpnSubscriptionRrp() {
            return vpnSubscriptionRrp;
        }

        public String getVpnSubscriptionPtp() {
            return vpnSubscriptionPtp;
        }

        public String getVpnSubscriptionDiscount() {
            return vpnSubscriptionDiscount;
        }

        public String getDialPlanChangeConfigRrp() {
            return dialPlanChangeConfigRrp;
        }

        public String getDialPlanChangeConfigPtp() {
            return dialPlanChangeConfigPtp;
        }

        public String getDialPlanChangeConfigDiscount() {
            return dialPlanChangeConfigDiscount;
        }

        public String getMajorMoveAddChangeRrp() {
            return majorMoveAddChangeRrp;
        }

        public String getMajorMoveAddChangePtp() {
            return majorMoveAddChangePtp;
        }

        public String getMajorMoveAddChangeDiscount() {
            return majorMoveAddChangeDiscount;
        }

        public String getAmendmentCharge() {
            return amendmentCharge;
        }

        public String getCancellationCharge() {
            return cancellationCharge;
        }

        public static class Builder {
            private String id = "";
            private String name = "";
            private String address = "";
            private String vpnConfigRrp = "";
            private String vpnConfigPtp = "";
            private String vpnConfigDiscount = "";
            private String vpnSubscriptionRrp = "";
            private String vpnSubscriptionPtp = "";
            private String vpnSubscriptionDiscount = "";
            private String dialPlanChangeConfigRrp = "";
            private String dialPlanChangeConfigPtp = "";
            private String dialPlanChangeConfigDiscount = "";
            private String majorMoveAddChangeRrp = "";
            private String majorMoveAddChangePtp = "";
            private String majorMoveAddChangeDiscount = "";
            private String amendmentCharge = "";
            private String cancellationCharge = "";

            public Builder withId(String id) {
                this.id = id;
                return this;
            }

            public Builder withName(String name) {
                this.name = name;
                return this;
            }

            public Builder withAddress(String address) {
                this.address = address;
                return this;
            }

            public Builder withVpnConfigRrp(String vpnConfigRrp) {
                this.vpnConfigRrp = vpnConfigRrp;
                return this;
            }

            public Builder withVpnConfigPtp(String vpnConfigPtp) {
                this.vpnConfigPtp = vpnConfigPtp;
                return this;
            }

            public Builder withVpnConfigDiscount(String vpnConfigDiscount) {
                this.vpnConfigDiscount = vpnConfigDiscount;
                return this;
            }

            public Builder withVpnSubscriptionRrp(String vpnSubscriptionRrp) {
                this.vpnSubscriptionRrp = vpnSubscriptionRrp;
                return this;
            }

            public Builder withVpnSubscriptionPtp(String vpnSubscriptionPtp) {
                this.vpnSubscriptionPtp = vpnSubscriptionPtp;
                return this;
            }

            public Builder withVpnSubscriptionDiscount(String vpnSubscriptionDiscount) {
                this.vpnSubscriptionDiscount = vpnSubscriptionDiscount;
                return this;
            }

            public Builder withDialPlanChangeConfigRrp(String dialPlanChangeConfigRrp) {
                this.dialPlanChangeConfigRrp = dialPlanChangeConfigRrp;
                return this;
            }

            public Builder withDialPlanChangeConfigPtp(String dialPlanChangeConfigPtp) {
                this.dialPlanChangeConfigPtp = dialPlanChangeConfigPtp;
                return this;
            }

            public Builder withDialPlanChangeConfigDiscount(String dialPlanChangeConfigDiscount) {
                this.dialPlanChangeConfigDiscount = dialPlanChangeConfigDiscount;
                return this;
            }

            public Builder withMajorMoveAddChangeRrp(String majorMoveAddChangeRrp) {
                this.majorMoveAddChangeRrp = majorMoveAddChangeRrp;
                return this;
            }

            public Builder withMajorMoveAddChangePtp(String majorMoveAddChangePtp) {
                this.majorMoveAddChangePtp = majorMoveAddChangePtp;
                return this;
            }

            public Builder withMajorMoveAddChangeDiscount(String majorMoveAddChangeDiscount) {
                this.majorMoveAddChangeDiscount = majorMoveAddChangeDiscount;
                return this;
            }

            public Builder withAmendmentCharge(String amendmentCharge) {
                this.amendmentCharge = amendmentCharge;
                return this;
            }

            public Builder withCancellationCharge(String cancellationCharge) {
                this.cancellationCharge = cancellationCharge;
                return this;
            }

            public OnevoiceOptionsPageSite build() {
                return new OnevoiceOptionsPageSite(this);
            }
        }
    }
}
