package com.bt.rsqe.expedio.order;


import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class OrderLineItemDTO {
    private String siteName;

    private String room;

    private String floor;

    private String orderID;

    private String orderStatus;

    private String orderSubStatus;

    private String orderVersion;

    private String parentID;

    private String productName;

    private String productStatus;

    private String productSubStatus;

    private String expedioReference;

    private String productType;

    private String romFirstName;

    private String romLastName;

    private String romPhoneNumber;

    private String romEmailID;

    private Long submittedToAib;

    private String productCode;

    private String supplierErrorCode;

    private String suReasonForRejection;

    private String suspendReason;

    private String romReasonForRejection;

    private String romReasonForCancellation;

    private String orderLineID;

    private String subGroup;


    public String getSiteName() {
        return siteName;
    }

    public void setSiteName(String siteName) {
        this.siteName = siteName;
    }

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public String getFloor() {
        return floor;
    }

    public void setFloor(String floor) {
        this.floor = floor;
    }



    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    public String getOrderSubStatus() {
        return orderSubStatus;
    }

    public void setOrderSubStatus(String orderSubStatus) {
        this.orderSubStatus = orderSubStatus;
    }

    public String getOrderVersion() {
        return orderVersion;
    }

    public void setOrderVersion(String orderVersion) {
        this.orderVersion = orderVersion;
    }


    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductStatus() {
        return productStatus;
    }

    public void setProductStatus(String productStatus) {
        this.productStatus = productStatus;
    }

    public String getProductSubStatus() {
        return productSubStatus;
    }

    public void setProductSubStatus(String productSubStatus) {
        this.productSubStatus = productSubStatus;
    }

   public String getProductType() {
        return productType;
    }

    public void setProductType(String productType) {
        this.productType = productType;
    }

    public String getRomFirstName() {
        return romFirstName;
    }

    public void setRomFirstName(String romFirstName) {
        this.romFirstName = romFirstName;
    }

    public String getRomLastName() {
        return romLastName;
    }

    public void setRomLastName(String romLastName) {
        this.romLastName = romLastName;
    }

    public String getRomPhoneNumber() {
        return romPhoneNumber;
    }

    public void setRomPhoneNumber(String romPhoneNumber) {
        this.romPhoneNumber = romPhoneNumber;
    }


    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }



    public String getSuspendReason() {
        return suspendReason;
    }

    public void setSuspendReason(String suspendReason) {
        this.suspendReason = suspendReason;
    }

    public String getOrderID() {
        return orderID;
    }

    public void setOrderID(String orderID) {
        this.orderID = orderID;
    }

    public String getParentID() {
        return parentID;
    }

    public void setParentID(String parentID) {
        this.parentID = parentID;
    }

    public String getExpedioReference() {
        return expedioReference;
    }

    public void setExpedioReference(String expedioReference) {
        this.expedioReference = expedioReference;
    }

    public String getRomEmailID() {
        return romEmailID;
    }

    public void setRomEmailID(String romEmailID) {
        this.romEmailID = romEmailID;
    }

    public Long getSubmittedToAib() {
        return submittedToAib;
    }

    public void setSubmittedToAib(Long submittedToAib) {
        this.submittedToAib = submittedToAib;
    }

    public String getSupplierErrorCode() {
        return supplierErrorCode;
    }

    public void setSupplierErrorCode(String supplierErrorCode) {
        this.supplierErrorCode = supplierErrorCode;
    }

    public String getSuReasonForRejection() {
        return suReasonForRejection;
    }

    public void setSuReasonForRejection(String suReasonForRejection) {
        this.suReasonForRejection = suReasonForRejection;
    }

    public String getRomReasonForRejection() {
        return romReasonForRejection;
    }

    public void setRomReasonForRejection(String romReasonForRejection) {
        this.romReasonForRejection = romReasonForRejection;
    }

    public String getRomReasonForCancellation() {
        return romReasonForCancellation;
    }

    public void setRomReasonForCancellation(String romReasonForCancellation) {
        this.romReasonForCancellation = romReasonForCancellation;
    }

    public String getOrderLineID() {
        return orderLineID;
    }

    public void setOrderLineID(String orderLineID) {
        this.orderLineID = orderLineID;
    }

    public String getSubGroup() {
        return subGroup;
    }

    public void setSubGroup(String subGroup) {
        this.subGroup = subGroup;
    }
}
