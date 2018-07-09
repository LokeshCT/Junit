package com.bt.rsqe.sqefacade.domain;

import com.bt.rsqe.utils.CalendarDeserializer;
import com.bt.rsqe.utils.CalendarSerializer;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Calendar;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class QuoteStatusSummary {

    private String expedioQuoteId;
    private String expedioQuoteVersion;
    private String quoteHeaderId;
    private String quoteVersion;
    private String quoteName;
    private String customerName;
    private int siteCount;
    private Calendar lastAccessedOn;
    private RAGStatus configStatus;
    private RAGStatus pricingStatus;
    private RAGStatus offerStatus;
    private RAGStatus orderStatus;
    protected String product;

    public static Builder newBuilder() {
        return new Builder();
    }

    public int getSiteCount() {
        return siteCount;
    }

    public String getQuoteName() {
        return quoteName;
    }

    public RAGStatus getConfigStatus() {
        return configStatus;
    }

    public String getProduct() {
        return product;
    }

    @JsonDeserialize(using = CalendarDeserializer.class)
    @JsonSerialize(using = CalendarSerializer.class)
    public Calendar getLastAccessedOn() {
        return lastAccessedOn;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    public static class Builder {
        private String expedioQuoteId;
        private String expedioQuoteVersion;
        private String quoteHeaderId;
        private String quoteName;
        private String quoteVersion;
        private String customerName;
        private int siteCount;
        private Calendar lastAccessedOn;
        private RAGStatus configStatus;
        private RAGStatus pricingStatus;
        private RAGStatus offerStatus;
        private RAGStatus orderStatus;

        public Builder withExpedioQuoteId(String projectId) {
            this.expedioQuoteId = projectId;
            return this;
        }

        public Builder withExpedioQuoteVersion(String projectVersion) {
            this.expedioQuoteVersion = projectVersion;
            return this;
        }

        public Builder withQuoteHeaderId(String quoteHeaderId) {
            this.quoteHeaderId = quoteHeaderId;
            return this;
        }

        public Builder withQuoteVersion(String quoteVersion) {
            this.quoteVersion = quoteVersion;
            return this;
        }

        public Builder withQuoteName(String quoteName) {
            this.quoteName = quoteName;
            return this;
        }

        public Builder withCustomerName(String customerName) {
            this.customerName = customerName;
            return this;
        }

        public Builder withSiteCount(int siteCount) {
            this.siteCount = siteCount;
            return this;
        }

        public Builder withLastAccessedOn(Calendar lastAccessedOn) {
            this.lastAccessedOn = lastAccessedOn;
            return this;
        }

        public Builder withConfigStatus(RAGStatus configStatus) {
            this.configStatus = configStatus;
            return this;
        }

        public Builder withPricingStatus(RAGStatus pricingStatus) {
            this.pricingStatus = pricingStatus;
            return this;
        }

        public Builder withOfferStatus(RAGStatus offerStatus) {
            this.offerStatus = offerStatus;
            return this;
        }

        public Builder withOrderStatus(RAGStatus orderStatus) {
            this.orderStatus = orderStatus;
            return this;
        }

        public QuoteStatusSummary build() {
            QuoteStatusSummary quoteStatusSummary = new QuoteStatusSummary();
            quoteStatusSummary.expedioQuoteId = this.expedioQuoteId;
            quoteStatusSummary.expedioQuoteVersion = this.expedioQuoteVersion;
            quoteStatusSummary.quoteHeaderId = this.quoteHeaderId;
            quoteStatusSummary.quoteName = this.quoteName;
            quoteStatusSummary.customerName = this.customerName;
            quoteStatusSummary.quoteVersion = this.quoteVersion;
            quoteStatusSummary.siteCount = this.siteCount;
            quoteStatusSummary.lastAccessedOn = this.lastAccessedOn;
            quoteStatusSummary.configStatus = this.configStatus;
            quoteStatusSummary.pricingStatus = this.pricingStatus;
            quoteStatusSummary.offerStatus = this.offerStatus;
            quoteStatusSummary.orderStatus = this.orderStatus;
            return quoteStatusSummary;
        }
    }
}
