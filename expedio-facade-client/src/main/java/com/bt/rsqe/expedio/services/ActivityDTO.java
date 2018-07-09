package com.bt.rsqe.expedio.services;


import com.bt.rsqe.dto.BidManagerCommentsDTO;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static org.apache.commons.collections.CollectionUtils.isEmpty;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class ActivityDTO {

    @XmlElement(name = "ActivityID")
    private String activityID;

    @XmlElement(name = "ActivityType")
    private String activityType;

    @XmlElement(name = "ActivityDescription")
    private String activityDescription;

    @XmlElement(name = "Status")
    private String status;

    @XmlElement(name = "SubStatus")
    private String subStatus;

    @XmlElement(name = "bidManagerCommentsDTOList")
    private List<BidManagerCommentsDTO> bidManagerCommentsDTOList;

    @XmlElement(name = "CustomerName")
    private String customerName;

    @XmlElement(name = "SalesChannel")
    private String salesChannel;

    @XmlElement(name = "AssignedTo")
    private String assignedTo;

    @XmlElement(name = "AssignedtoEmailid")
    private String assignedtoEmailid;

    @XmlElement(name = "Creator")
    private String creator;

    @XmlElement(name = "CreatedbyEmailid")
    private String createdbyEmailid;

    @XmlElement(name = "QuoteRefID")
    private String quoteRefID;

    @XmlElement(name = "QuoteVersion")
    private String quoteVersion;

    @XmlElement(name = "ExpedioReference")
    private String expedioReference;

    @XmlElement(name = "SourceSystem")
    private String sourceSystem;

    @XmlElement(name = "SalesUsersComments")
    private String salesUsersComments;

    @XmlElement(name = "BidMangersComments")
    private String bidMangersComments;

    @XmlElement(name = "OrderType")
    private String orderType;

    @XmlElement(name = "BFGcustomerid")
    private String bfgCustomerId;

    @XmlElement(name = "ActivityCreatedDate")
    private String activityCreatedDate;

    @XmlElement(name = "ChannelType")
    private String channelType;

    @XmlElement(name = "ProductName")
    private String productName;

    @XmlElement(name = "ActivityClosedDate")
    private String activityClosedDate;

    @XmlElement(name = "CreatorReason")
    private String creatorReason;

    @XmlElement(name = "GroupEmailID")
    private String groupEmailID;

    @XmlElement(name = "Role")
    private String role;

    @XmlElement(name = "BidManagerName")
    private String bidManagerName;

    @XmlElement(name = "ApproverReason")
    private String approverReason;

    @XmlElement(name = "QuoteName")
    private String quoteName;

    @XmlElement(name = "CommentsHistory")
    private String commentsHistory;

    @XmlElement
    private String userId;

    @XmlElement
    private String boatId;


    ///CLOVER:OFF
    public ActivityDTO() {
    }

    public ActivityDTO(String activityID, String activityType, String activityDescription, String status, String subStatus,
                       String customerName, String salesChannel, String assignedTo, String assignedtoEmailid, String creator,
                       String createdbyEmailid, String quoteRefID, String quoteVersion, String expedioReference, String sourceSystem,
                       String salesUsersComments, String bidMangersComments, String orderType, String bfgCustomerId,
                       String activityCreatedDate, String channelType, String productName, String activityClosedDate, String creatorReason,
                       String groupEmailID, String role, String bidManagerName, String approverReason,String quoteName,String commentsHistory,
                       List<BidManagerCommentsDTO> bidManagerCommentsDTOList) {
        this.activityID = activityID;
        this.activityType = activityType;
        this.activityDescription = activityDescription;
        this.status = status;
        this.subStatus = subStatus;
        this.customerName = customerName;
        this.salesChannel = salesChannel;
        this.assignedTo = assignedTo;
        this.assignedtoEmailid = assignedtoEmailid;
        this.creator = creator;
        this.createdbyEmailid = createdbyEmailid;
        this.quoteRefID = quoteRefID;
        this.quoteVersion = quoteVersion;
        this.expedioReference = expedioReference;
        this.sourceSystem = sourceSystem;
        this.salesUsersComments = salesUsersComments;
        this.bidMangersComments = bidMangersComments;
        this.orderType = orderType;
        this.bfgCustomerId = bfgCustomerId;
        this.activityCreatedDate = activityCreatedDate;
        this.channelType = channelType;
        this.productName = productName;
        this.activityClosedDate = activityClosedDate;
        this.creatorReason = creatorReason;
        this.groupEmailID = groupEmailID;
        this.role = role;
        this.bidManagerName = bidManagerName;
        this.approverReason = approverReason;
        this.quoteName =quoteName;
        this.commentsHistory=commentsHistory;
        this.bidManagerCommentsDTOList = bidManagerCommentsDTOList;
    }


    public void setActivityID(String activityID) {
        this.activityID = activityID;
    }

    public String getActivityID() {
        return activityID;
    }

    public String getActivityType() {
        return activityType;
    }

    public String getActivityDescription() {
        return activityDescription;
    }

    public String getBoatId() {
        return boatId;
    }

    public void setBoatId(String boatId) {
        this.boatId = boatId;
    }

    public String getStatus() {
        return status;
    }

    public String getSubStatus() {
        return subStatus;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getSalesChannel() {
        return salesChannel;
    }

    public String getAssignedTo() {
        return assignedTo;
    }

    public String getAssignedtoEmailid() {
        return assignedtoEmailid;
    }

    public String getCreator() {
        return creator;
    }

    public String getCreatedbyEmailid() {
        return createdbyEmailid;
    }

    public String getQuoteRefID() {
        return quoteRefID;
    }

    public String getQuoteVersion() {
        return quoteVersion;
    }

    public String getExpedioReference() {
        return expedioReference;
    }

    public String getSourceSystem() {
        return sourceSystem;
    }

    public void setSourceSystem(String sourceSystem) {
        this.sourceSystem = sourceSystem;
    }


    public String getSalesUsersComments() {
        return salesUsersComments;
    }

    public String getBidMangersComments() {
        return bidMangersComments;
    }

    public String getOrderType() {
        return orderType;
    }

    public String getBfgCustomerId() {
        return bfgCustomerId;
    }

    public String getActivityCreatedDate() {
        return activityCreatedDate;
    }

    public String getChannelType() {
        return channelType;
    }

    public String getProductName() {
        return productName;
    }

    public String getActivityClosedDate() {
        return activityClosedDate;
    }

    public String getCreatorReason() {
        return creatorReason;
    }

    public String getGroupEmailID() {
        return groupEmailID;
    }

    public String getRole() {
        return role;
    }

    public String getBidManagerName() {
        return bidManagerName;
    }

    public String getApproverReason() {
        return approverReason;
    }

    public String getQuoteName() {
        return quoteName;
    }

    public void setQuoteName(String quoteName) {
        this.quoteName = quoteName;
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

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public List<BidManagerCommentsDTO> getBidManagerCommentsDTOList() {
        return bidManagerCommentsDTOList;
    }

    public static class Builder {

        private String activityID;
        private String activityType;
        private String activityDescription;
        private String status;
        private String subStatus;
        private String customerName;
        private String salesChannel;
        private String assignedTo;
        private String assignedtoEmailid;
        private String creator;
        private String createdbyEmailid;
        private String quoteRefID;
        private String quoteVersion;
        private String expedioReference;
        private String sourceSystem;
        private String salesUsersComments;
        private String bidMangersComments;
        private String orderType;
        private String bfgCustomerId;
        private String activityCreatedDate;
        private String channelType;
        private String productName;
        private String activityClosedDate;
        private String creatorReason;
        private String groupEmailID;
        private String role;
        private String bidManagerName;
        private String approverReason;
        private String quoteName;
        private String commentsHistory;
        private List<BidManagerCommentsDTO> bidManagerCommentsDTOList = newArrayList();



        public static Builder get() {
            return new Builder();
        }

        public ActivityDTO build() {
            return new ActivityDTO(activityID, activityType, activityDescription, status, subStatus, customerName, salesChannel, assignedTo,
                                   assignedtoEmailid, creator, createdbyEmailid, quoteRefID, quoteVersion, expedioReference, sourceSystem,
                                   salesUsersComments, bidMangersComments, orderType, bfgCustomerId, activityCreatedDate, channelType,
                                   productName, activityClosedDate, creatorReason, groupEmailID, role, bidManagerName, approverReason,quoteName, commentsHistory, bidManagerCommentsDTOList);
        }

        public Builder withActivityID(String activityID) {
            this.activityID = activityID;
            return this;
        }

        public Builder withBidManagerComments(List<BidManagerCommentsDTO> bidManagerComments) {
            if (!isEmpty(bidManagerComments)) {
            this.bidManagerCommentsDTOList.addAll(bidManagerComments);
            }
            return this;
        }

        public Builder withActivityType(String activityType) {
            this.activityType = activityType;
            return this;
        }

        public Builder withActivityDescription(String activityDescription) {
            this.activityDescription = activityDescription;
            return this;
        }

        public Builder withStatus(String status) {
            this.status = status;
            return this;
        }

        public Builder withSubStatus(String subStatus) {
            this.subStatus = subStatus;
            return this;
        }

        public Builder withCustomerName(String customerName) {
            this.customerName = customerName;
            return this;
        }

        public Builder withSalesChannel(String salesChannel) {
            this.salesChannel = salesChannel;
            return this;
        }

        public Builder withAssignedTo(String assignedTo) {
            this.assignedTo = assignedTo;
            return this;
        }

        public Builder withAssignedtoEmailid(String assignedtoEmailid) {
            this.assignedtoEmailid = assignedtoEmailid;
            return this;
        }

        public Builder withCreator(String creator) {
            this.creator = creator;
            return this;
        }

        public Builder withCreatedbyEmailid(String createdbyEmailid) {
            this.createdbyEmailid = createdbyEmailid;
            return this;
        }

        public Builder withQuoteRefID(String quoteRefID) {
            this.quoteRefID = quoteRefID;
            return this;
        }

        public Builder withQuoteVersion(String quoteVersion) {
            this.quoteVersion = quoteVersion;
            return this;
        }

        public Builder withExpedioReference(String expedioReference) {
            this.expedioReference = expedioReference;
            return this;
        }

        public Builder withSourceSystem(String sourceSystem) {
            this.sourceSystem = sourceSystem;
            return this;
        }

        public Builder withSalesUsersComments(String salesUsersComments) {
            this.salesUsersComments = salesUsersComments;
            return this;
        }

        public Builder withBidMangersComments(String bidMangersComments) {
            this.bidMangersComments = bidMangersComments;
            return this;
        }

        public Builder withOrderType(String orderType) {
            this.orderType = orderType;
            return this;
        }

        public Builder withBfgCustomerId(String bfgCustomerId) {
            this.bfgCustomerId = bfgCustomerId;
            return this;
        }

        public Builder withActivityCreatedDate(String activityCreatedDate) {
            this.activityCreatedDate = activityCreatedDate;
            return this;
        }

        public Builder withChannelType(String channelType) {
            this.channelType = channelType;
            return this;
        }

        public Builder withProductName(String productName) {
            this.productName = productName;
            return this;
        }

        public Builder withActivityClosedDate(String activityClosedDate) {
            this.activityClosedDate = activityClosedDate;
            return this;
        }

        public Builder withBidManagerName(String bidManagerName) {
            this.bidManagerName = bidManagerName;
            return this;
        }

        public Builder withRole(String role) {
            this.role = role;
            return this;
        }

        public Builder withGroupEmailID(String groupEmailID) {
            this.groupEmailID = groupEmailID;
            return this;
        }

        public Builder withCreatorReason(String creatorReason) {
            this.creatorReason = creatorReason;
            return this;
        }

        public Builder withApproverReason(String approverReason) {
            this.approverReason = approverReason;
            return this;
        }

        public Builder withQuoteName(String approverReason) {
            this.quoteName = approverReason;
            return this;
        }
        public Builder withCommentsHistory(String commentsHistory) {
            this.commentsHistory = commentsHistory;
            return this;
    }

    }
    ///CLOVER:ON
}
