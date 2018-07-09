package com.bt.rsqe.expedio.services;

import com.bt.rsqe.dto.BidManagerCommentsDTO;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static org.apache.commons.collections.CollectionUtils.isEmpty;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class UpdateActivityDTO {
    @XmlElement(name = "ActivityID")
    private String activityID;

    @XmlElement(name = "QuoteRefID")
    private String quoteRefID;

    @XmlElement(name = "QuoteVersion")
    private String quoteVersion;

    @XmlElement(name = "ExpedioReference")
    private String expedioReference;

    @XmlElement(name = "activityComments")
    private List<BidManagerCommentsDTO> activityComments;

    @XmlElement(name = "approverReason")
    private String approverReason;

    @XmlElement(name = "BID_Manager_T_and_Cs")
    private String bidManagerCaveats;

    @XmlElement(name = "Status")
    private String status;

    @XmlElement(name = "SubStatus")
    private String subStatus;

    @XmlElement(name = "AssignedTo")
    private String assignedTo;

    @XmlElement(name = "AssignedtoEmailid")
    private String assignedToEmailID;

    @XmlElement(name = "SourceSystem")
    private String sourceSystem;

    @XmlElement(name = "ActivityClosedDate")
    private String closeDate;

    @XmlElement(name = "userName")
    private String userName;

    public UpdateActivityDTO() {
    }

    public UpdateActivityDTO(Builder builder) {

        this.activityID = builder.activityID;
        this.quoteRefID = builder.quoteRefID;
        this.quoteVersion = builder.quoteVersion;
        this.expedioReference = builder.expedioReference;
        this.approverReason = builder.approverReason;
        this.bidManagerCaveats = builder.bidManagerCaveats;
        this.subStatus = builder.subStatus;
        this.sourceSystem = builder.sourceSystem;
        this.activityComments = builder.activityComments;
        this.approverReason = builder.approverReason;
        this.userName = builder.userName;

    }

    public String getActivityID() {
        return activityID;
    }

    public void setActivityID(String activityID) {
        this.activityID = activityID;
    }

    public String getQuoteRefID() {
        return quoteRefID;
    }

    public void setQuoteRefID(String quoteRefID) {
        this.quoteRefID = quoteRefID;
    }

    public String getQuoteVersion() {
        return quoteVersion;
    }

    public void setQuoteVersion(String quoteVersion) {
        this.quoteVersion = quoteVersion;
    }

    public String getExpedioReference() {
        return expedioReference;
    }

    public void setExpedioReference(String expedioReference) {
        this.expedioReference = expedioReference;
    }

    public String getApproverReason() {
        return approverReason;
    }

    public void setApproverReason(String approverReason) {
        this.approverReason = approverReason;
    }

    public String getBidManagerCaveats() {
        return bidManagerCaveats;
    }

    public void setBidManagerCaveats(String bidManagerCaveats) {
        this.bidManagerCaveats = bidManagerCaveats;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSubStatus() {
        return subStatus;
    }

    public void setSubStatus(String subStatus) {
        this.subStatus = subStatus;
    }

    public String getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(String assignedTo) {
        this.assignedTo = assignedTo;
    }

    public String getAssignedToEmailID() {
        return assignedToEmailID;
    }

    public void setAssignedToEmailID(String assignedToEmailID) {
        this.assignedToEmailID = assignedToEmailID;
    }

    public String getSourceSystem() {
        return sourceSystem;
    }

    public void setSourceSystem(String sourceSystem) {
        this.sourceSystem = sourceSystem;
    }

    public String getCloseDate() {
        return closeDate;
    }

    public void setCloseDate(String closeDate) {
        this.closeDate = closeDate;
    }

    public List<BidManagerCommentsDTO> getActivityComments() {
        return this.activityComments;
    }

    public String getUserName() {
        return userName;
    }

    public static final class Builder {

        private String activityID;
        private String quoteRefID;
        private String quoteVersion;
        private String expedioReference;
        private  String approverReason;
        private List<BidManagerCommentsDTO> activityComments = newArrayList();
        private String bidManagerCaveats;
        private String subStatus;
        private String sourceSystem;
        private String userName;

        public static Builder get() {
            return new Builder();
        }

        public Builder withActivityID(String activityID) {
            this.activityID = activityID;
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

        public Builder withApproverReason(String approverReason) {
            this.approverReason = approverReason;
            return this;
        }

        public Builder withActivityComments(List<BidManagerCommentsDTO> activityComments) {
            if (!isEmpty(activityComments)) {
                this.activityComments.addAll(activityComments);
            }
            return this;
        }

        public Builder withBidManagerCaveats(String bidManagerCaveats) {
            this.bidManagerCaveats = bidManagerCaveats;
            return this;
        }

        public Builder withSubStatus(String subStatus) {
            this.subStatus = subStatus;
            return this;
        }

        public Builder withSourceSystem(String sourceSystem) {
            this.sourceSystem = sourceSystem;
            return this;
        }

        public Builder withUserName(String userName) {
            this.userName = userName;
            return this;
        }

        public UpdateActivityDTO build() {
            return new UpdateActivityDTO(this);
        }
    }
}
