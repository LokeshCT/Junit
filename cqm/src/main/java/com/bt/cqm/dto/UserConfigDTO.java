package com.bt.cqm.dto;


import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class UserConfigDTO {

  @XmlElement
  private String requestId;
  @XmlElement
  private String submitter;
  @XmlElement
  private int createDate;
  @XmlElement
  private String assignedTo;
  @XmlElement
  private String lastModifiedBy;
  @XmlElement
  private int modifiedDate;
  @XmlElement
  private int status;
  @XmlElement
  private String shortDescription;
  @XmlElement
  private String userId;
  @XmlElement
  private String salesChannel;
  @XmlElement
  private int deleteFlag;
  @XmlElement
  private int intSalesChannelID;

  public UserConfigDTO() {
    //for jaxb
  }

  public UserConfigDTO(String requestId, String submitter, int createDate, String assignedTo, String lastModifiedBy, int modifiedDate, int status, String shortDescription, String userId, String salesChannel, int deleteFlag, int intSalesChannelID) {
    this.requestId = requestId;
    this.submitter = submitter;
    this.createDate = createDate;
    this.assignedTo = assignedTo;
    this.lastModifiedBy = lastModifiedBy;
    this.modifiedDate = modifiedDate;
    this.status = status;
    this.shortDescription = shortDescription;
    this.userId = userId;
    this.salesChannel = salesChannel;
    this.deleteFlag = deleteFlag;
    this.intSalesChannelID = intSalesChannelID;
  }

    ///CLOVER:OFF

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getSubmitter() {
        return submitter;
    }

    public void setSubmitter(String submitter) {
        this.submitter = submitter;
    }

    public int getCreateDate() {
        return createDate;
    }

    public void setCreateDate(int createDate) {
        this.createDate = createDate;
    }

    public String getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(String assignedTo) {
        this.assignedTo = assignedTo;
    }

    public String getLastModifiedBy() {
        return lastModifiedBy;
    }

    public void setLastModifiedBy(String lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }

    public int getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(int modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getSalesChannel() {
        return salesChannel;
    }

    public void setSalesChannel(String salesChannel) {
        this.salesChannel = salesChannel;
    }

    public int getDeleteFlag() {
        return deleteFlag;
    }

    public void setDeleteFlag(int deleteFlag) {
        this.deleteFlag = deleteFlag;
    }

    public int getIntSalesChannelID() {
        return intSalesChannelID;
    }

    public void setIntSalesChannelID(int intSalesChannelID) {
        this.intSalesChannelID = intSalesChannelID;
    }


///CLOVER:ON

}
