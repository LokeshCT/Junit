package com.bt.rsqe.expedio.services;


import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class GetActivityRequestDTO {

    private String salesChannel;
    private String filterName;
    private String bidManagerName;
    private String activityDescription;
    private String activityID;
    private String status;
    private String salesChannelType;
    private String productName;

    public GetActivityRequestDTO() {
    }

    public GetActivityRequestDTO(String salesChannel, String filterName, String bidManagerName, String activityDescription, String activityID, String status, String salesChannelType, String productName) {
        this.salesChannel = salesChannel;
        this.filterName = filterName;
        this.bidManagerName = bidManagerName;
        this.activityDescription = activityDescription;
        this.activityID = activityID;
        this.status = status;
        this.salesChannelType = salesChannelType;
        this.productName = productName;
    }

    public String getSalesChannel() {
        return salesChannel;
    }

    public String getFilterName() {
        return filterName;
    }

    public String getBidManagerName() {
        return bidManagerName;
    }

    public String getActivityDescription() {
        return activityDescription;
    }

    public String getActivityID() {
        return activityID;
    }

    public String getStatus() {
        return status;
    }

    public String getSalesChannelType() {
        return salesChannelType;
    }

    public String getProductName() {
        return productName;
    }

    @Override
    public boolean equals(Object that) {
        return EqualsBuilder.reflectionEquals(this, that);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    public static class Builder {

        private String salesChannel;
        private String filterName;
        private String bidManagerName;
        private String activityDescription;
        private String activityID;
        private String status;
        private String salesChannelType;
        private String productName;

        public static Builder get() {
            return new Builder();
        }

        public GetActivityRequestDTO build() {
            return new GetActivityRequestDTO(salesChannel, filterName, bidManagerName,
                                             activityDescription, activityID, status, salesChannelType, productName);
        }

        public Builder withSalesChannel(String salesChannel) {
            this.salesChannel = salesChannel;
            return this;
        }

        public Builder withFilterName(String filterName) {
            this.filterName = filterName;
            return this;
        }

        public Builder withBidManagerName(String bidManagerName) {
            this.bidManagerName = bidManagerName;
            return this;
        }

        public Builder withActivityDescription(String activityDescription) {
            this.activityDescription = activityDescription;
            return this;
        }

        public Builder withActivityID(String activityID) {
            this.activityID = activityID;
            return this;
        }

        public Builder withStatus(String status) {
            this.status = status;
            return this;
        }

        public Builder withSalesChannelType(String salesChannelType) {
            this.salesChannelType = salesChannelType;
            return this;
        }

        public Builder withProductName(String productName) {
            this.productName = productName;
            return this;
        }
    }
}
