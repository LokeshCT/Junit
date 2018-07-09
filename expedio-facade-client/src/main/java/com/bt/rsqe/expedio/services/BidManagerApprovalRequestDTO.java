package com.bt.rsqe.expedio.services;

import com.bt.rsqe.dto.BidManagerCommentsDTO;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class BidManagerApprovalRequestDTO {
    @XmlElement
    public List<BidManagerCommentsDTO> bidManagerCommentsDTOs;
    @XmlElement
    public String bidManagerEmail;
    @XmlElement
    public String expedioReference;
    @XmlElement
    public String creatorReason;
    @XmlElement
    public String orderType;
    @XmlElement
    public String quoteRefId;
    @XmlElement
    public String quoteVersion;
    @XmlElement
    public String groupEmailId;
    @XmlElement
    public String userEmailId;
    @XmlElement
    public String userName;

    public BidManagerApprovalRequestDTO() {
        /* for jaxb*/
    }

    public BidManagerApprovalRequestDTO(List<BidManagerCommentsDTO> bidManagerCommentsDTOs, String bidManagerEmail, String expedioReference, String creatorReason, String orderType, String quoteRefId, String quoteVersion, String groupEmailId, String userEmailId, String userName) {
        this.bidManagerCommentsDTOs = bidManagerCommentsDTOs;
        this.bidManagerEmail = bidManagerEmail;
        this.expedioReference = expedioReference;
        this.creatorReason = creatorReason;
        this.orderType = orderType;
        this.quoteRefId = quoteRefId;
        this.quoteVersion = quoteVersion;
        this.groupEmailId = groupEmailId;
        this.userEmailId = userEmailId;
        this.userName = userName;
    }

    public BidManagerApprovalRequestDTO(String bidManagerEmail, String expedioReference, String creatorReason, String orderType, String quoteRefId, String quoteVersion, String groupEmailId, String userEmailId, String userName) {
        this(null, bidManagerEmail, expedioReference, creatorReason, orderType, quoteRefId, quoteVersion, groupEmailId, userEmailId, userName);
    }

    @Override
    public String toString() {
        return "DiscountApprovalDTO{" +
               "bidManagerCommentsDTOs='" + bidManagerCommentsDTOs + '\'' +
               "bidManagerEmail='" + bidManagerEmail + '\'' +
               ", expedioReference='" + expedioReference + '\'' +
               ", creatorReason='" + creatorReason + '\'' +
               ", orderType='" + orderType + '\'' +
               ", quoteRefId='" + quoteRefId + '\'' +
               ", quoteVersion='" + quoteVersion + '\'' +
               ", groupEmailId='" + groupEmailId + '\'' +
               ", userEmailId='" + userEmailId + '\'' +
               ", userName='" + userName + '\'' +
               '}';
    }
}
