package com.bt.rsqe.matchers.excel.bcm;

import com.bt.rsqe.matchers.excel.ExcelSheetCompositeMatcher;

public class BcmSheetBidInfoPageMatcher extends ExcelSheetCompositeMatcher<BcmSheetBidInfoPageMatcher> {

    public static BcmSheetBidInfoPageMatcher aBidInfoPage() {
        return new BcmSheetBidInfoPageMatcher();
    }

    private BcmSheetBidInfoPageMatcher() {
        super("Bid Info");
    }

    public BcmSheetBidInfoPageMatcher with(BidInfo.Builder bidInfoBuilder) {
        String[] bidInfoFields = {"bid-info.projectId", "bid-info.customerName", "bid-info.opportunityId"
            ,"bid-info.bidNumber", "bid-info.quoteOptionVersion", "bid-info.quoteCurrency", "bid-info.username", "bid-info.tradeLevel"
            ,"bid-info.salesChannel", "bid-info.monthlyRevenueCommitment", "bid-info.priceBookVersion"};
        BidInfo bidInfo = bidInfoBuilder.build();
        String[] expectedValues = { bidInfo.quoteId, bidInfo.customerName, bidInfo.opportunityId,
            bidInfo.bidNumber, bidInfo.quoteOptionId, bidInfo.quoteCurrency, bidInfo.username, bidInfo.tradeLevel,
            bidInfo.salesChannel, bidInfo.monthlyRevenueCommitment, bidInfo.priceBookVersion};
        expectRowWithValues(bidInfoFields, expectedValues);
        return this;
    }

    public static class BidInfo {
        private String quoteId = "";
        private String customerName = "";
        private String opportunityId;
        private String bidNumber;
        private String quoteCurrency;
        private String username;
        private String tradeLevel;
        private String salesChannel;
        private String monthlyRevenueCommitment;
        private String priceBookVersion;
        private String quoteOptionId;

        public BidInfo(Builder builder) {
            this.quoteId = builder.quoteId;
            this.customerName = builder.customerName;
            this.opportunityId = builder.opportunityId;
            this.bidNumber = builder.bidNumber;
            this.quoteOptionId = builder.quoteOptionId;
            this.quoteCurrency = builder.quoteCurrency;
            this.username = builder.username;
            this.tradeLevel = builder.tradeLevel;
            this.salesChannel = builder.salesChannel;
            this.monthlyRevenueCommitment = builder.monthlyRevenueCommitment;
            this.priceBookVersion = builder.priceBookVersion;
        }

        public static Builder aBidInfo() {
            return new BidInfo.Builder();
        }

        public static class Builder {
            private String quoteId = "";
            private String customerName = "";
            private String opportunityId;
            private String bidNumber;
            private String quoteCurrency;
            private String username;
            private String tradeLevel;
            private String salesChannel;
            private String monthlyRevenueCommitment;
            private String priceBookVersion;
            private String quoteOptionId;

            public Builder withQuoteId(String quoteId) {
                this.quoteId = quoteId;
                return this;
            }

            public Builder withCustomerName(String customerName) {
                this.customerName = customerName;
                return this;
            }

            public Builder withOpportunityId(String opportunityId) {
                this.opportunityId = opportunityId;
                return this;
            }

            public Builder withBidNumber(String bidNumber) {
                this.bidNumber = bidNumber;
                return this;
            }

            public Builder withQuoteCurrency(String quoteCurrency) {
                this.quoteCurrency = quoteCurrency;
                return this;
            }

            public Builder withUserName(String username) {
                this.username = username;
                return this;
            }

            public Builder withTradeLevel(String tradeLevel) {
                this.tradeLevel = tradeLevel;
                return this;
            }

            public Builder withSalesChannel(String salesChannel) {
                this.salesChannel = salesChannel;
                return this;
            }

            public Builder withMonthlyRevenueCommitment(String monthlyRevenueCommitment) {
                this.monthlyRevenueCommitment = monthlyRevenueCommitment;
                return this;
            }

            public Builder withPriceBookVersion(String priceBookVersion) {
                this.priceBookVersion = priceBookVersion;
                return this;
            }

            public Builder withQuoteVersionNumber(String quoteOptionId) {
                this.quoteOptionId = quoteOptionId;
                return this;
            }

            public BidInfo build() {
                return new BidInfo(this);
            }
        }

    }


}
