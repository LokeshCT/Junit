package com.bt.cqm.dto;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created with IntelliJ IDEA.
 * User: Ranjit Roykrishna
 * Date: 23/01/14
 * Time: 20:05
 * To change this template use File | Settings | File Templates.
 */

/*<ns:ActivityDescription>Test</ns:ActivityDescription>
<ns:AssignedTo_EmailID>Test</ns:AssignedTo_EmailID>
<ns:CreatorReason>Test</ns:CreatorReason>
<ns:ExpedioReference>Test</ns:ExpedioReference>
<ns:OrderType>Test</ns:OrderType>
<ns:QuoteRefID>Test</ns:QuoteRefID>
<ns:QuoteVersion>Test</ns:QuoteVersion>
<ns:SubStatus>Test</ns:SubStatus>
<ns:SourceSystem>Test</ns:SourceSystem>
<ns:GroupEmailID>Test</ns:GroupEmailID>
<ns:CreatedBy_EmailID>Test</ns:CreatedBy_EmailID>
<ns:Role>Test</ns:Role>
<ns:ProductName>Test</ns:ProductName>*/

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class CreateActivityDTO {

    @XmlElement(name = "ActivityDescription")
    private String activityDescription;

    @XmlElement(name = "AssignedTo_EmailID")
    private String assignedToEmailId;

    @XmlElement(name = "CreatorReason")
    private String creatorReason;

    @XmlElement(name = "ExpedioReference")
    private String expedioReference;

    @XmlElement(name = "OrderType")
    private String orderType;

    @XmlElement(name = "QuoteRefID")
    private String quoteRefID;

    @XmlElement(name = "QuoteVersion")
    private String quoteVersion;

    @XmlElement(name = "SubStatus")
    private String subStatus;

    @XmlElement(name = "SourceSystem")
    private String sourceSystem;

    @XmlElement(name = "GroupEmailID")
    private String groupEmailID;

    @XmlElement(name = "CreatedBy_EmailID")
    private String createdByEmailId;

    @XmlElement(name = "Role")
    private String role;

    @XmlElement(name = "ProductName")
    private String productName;

    @XmlElement(name = "SalesChannel")
    private String salesChannel;

    @XmlElement(name = "BidManagerName")
    private String bidManagerName;

    @XmlElement(name = "Status")
    private String status;

    @XmlElement(name = "ActivityType")
    private String activityType;

    @XmlElement(name = "CreatedByName")
    private String createdByName;

    @XmlElement(name = "BFGCustomerID")
    private String bfgCustomerID;

    @XmlElement(name = "CustomerName")
    private String customerName;

    ///CLOVER:OFF
    public CreateActivityDTO() {
    }

    public String getActivityDescription() {
        return activityDescription;
    }

    public void setActivityDescription(String activityDescription) {
        this.activityDescription = activityDescription;
    }

    public String getAssignedToEmailId() {
        return assignedToEmailId;
    }

    public void setAssignedToEmailId(String assignedToEmailId) {
        this.assignedToEmailId = assignedToEmailId;
    }

    public String getCreatorReason() {
        return creatorReason;
    }

    public void setCreatorReason(String creatorReason) {
        this.creatorReason = creatorReason;
    }

    public String getExpedioReference() {
        return expedioReference;
    }

    public void setExpedioReference(String expedioReference) {
        this.expedioReference = expedioReference;
    }

    public String getOrderType() {
        return orderType;
    }

    public void setOrderType(String orderType) {
        this.orderType = orderType;
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

    public String getSubStatus() {
        return subStatus;
    }

    public void setSubStatus(String subStatus) {
        this.subStatus = subStatus;
    }

    public String getSourceSystem() {
        return sourceSystem;
    }

    public void setSourceSystem(String sourceSystem) {
        this.sourceSystem = sourceSystem;
    }

    public String getGroupEmailID() {
        return groupEmailID;
    }

    public void setGroupEmailID(String groupEmailID) {
        this.groupEmailID = groupEmailID;
    }

    public String getCreatedByEmailId() {
        return createdByEmailId;
    }

    public void setCreatedByEmailId(String createdByEmailId) {
        this.createdByEmailId = createdByEmailId;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getSalesChannel() {
        return salesChannel;
    }

    public void setSalesChannel(String salesChannel) {
        this.salesChannel = salesChannel;
    }

    public String getBidManagerName() {
        return bidManagerName;
    }

    public void setBidManagerName(String bidManagerName) {
        this.bidManagerName = bidManagerName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getActivityType() {
        return activityType;
    }

    public void setActivityType(String activityType) {
        this.activityType = activityType;
    }

    public String getCreatedByName() {
        return createdByName;
    }

    public void setCreatedByName(String createdByName) {
        this.createdByName = createdByName;
    }

    public String getBfgCustomerID() {
        return bfgCustomerID;
    }

    public void setBfgCustomerID(String bfgCustomerID) {
        this.bfgCustomerID = bfgCustomerID;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    ///CLOVER:ON
}
