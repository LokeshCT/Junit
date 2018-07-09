package com.bt.rsqe.expedio.services;

import com.bt.rsqe.dto.BidManagerCommentsDTO;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class CloseBidManagerActivityDTO {
    @XmlElement
    public String approverReason;
    @XmlElement
    public String bidManagerTermsAndConditions;
    @XmlElement
    public String activityId;
    @XmlElement
    public String projectId;
    @XmlElement
    public String userName;
    @XmlElement
    public List<BidManagerCommentsDTO> bidManagerComments;

    public CloseBidManagerActivityDTO() {
        //for jaxb
    }

    public CloseBidManagerActivityDTO(String approverReason, String activityId, String projectId, String userName) {
        this.approverReason = approverReason;
        this.activityId = activityId;
        this.projectId = projectId;
        this.userName = userName;
    }

    public CloseBidManagerActivityDTO(List<BidManagerCommentsDTO> bidManagerComments, String bidManagerTermsAndConditions, String activityId, String projectId) {
        this.bidManagerComments = bidManagerComments;
        this.bidManagerTermsAndConditions = bidManagerTermsAndConditions;
        this.activityId = activityId;
        this.projectId = projectId;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("CloseDiscountActivityDTO");
        sb.append("{approverReason='").append(approverReason).append('\'');
        sb.append(", activityId='").append(activityId).append('\'');
        sb.append(", projectId='").append(projectId).append('\'');
        sb.append(", bidManagerTermsAndConditions='").append(bidManagerTermsAndConditions).append('\'');
        sb.append('}');
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        CloseBidManagerActivityDTO that = (CloseBidManagerActivityDTO) o;

        if (activityId != null ? !activityId.equals(that.activityId) : that.activityId != null) {
            return false;
        }
        if (approverReason != null ? !approverReason.equals(that.approverReason) : that.approverReason != null) {
            return false;
        }
        if (projectId != null ? !projectId.equals(that.projectId) : that.projectId != null) {
            return false;
        }
        if (bidManagerComments != null ? !bidManagerComments.equals(that.bidManagerComments) : that.bidManagerComments != null) {
            return false;
        }
        if (bidManagerTermsAndConditions != null ? !bidManagerTermsAndConditions.equals(that.bidManagerTermsAndConditions) : that.bidManagerTermsAndConditions != null) {
            return false;
        }
        if (userName != null ? !userName.equals(that.userName) : that.userName != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = approverReason != null ? approverReason.hashCode() : 0;
        result = 31 * result + (activityId != null ? activityId.hashCode() : 0);
        result = 31 * result + (projectId != null ? projectId.hashCode() : 0);
        result = 31 * result + (bidManagerTermsAndConditions != null ? bidManagerTermsAndConditions.hashCode() : 0);
        result = 31 * result + (bidManagerComments != null ? bidManagerComments.hashCode() : 0);
        result = 31 * result + (userName != null ? userName.hashCode() : 0);

        return result;
    }
}
