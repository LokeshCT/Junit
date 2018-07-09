package com.bt.rsqe.customerinventory.service.rootAssetUpdater;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.math.BigDecimal;
import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: 605673548
 * Date: 28/05/15
 * Time: 11:39
 * To change this template use File | Settings | File Templates.
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class RootAssetDto {

    public Long rootElementId;
    public Long relElementId;
    public String relElementType;
    public Long elementId;
    public String elementType;
    public String elementRole;
    public String elementResilence;
    public String elementRelationshipType;
    public String elementRelationshipName;
    public Date elementCeasedDate;
    public String elementSourceSystem;
    public String elementInsPref;
    public String rootElementType;
    public String rootElementIdentifier;
    public String errorCode;
    public String errorMsg;
    public Long customerId;



    public String getRelElementType() {
        return relElementType;
    }

    public void setRelElementType(String relElementType) {
        this.relElementType = relElementType;
    }



    public String getElementType() {
        return elementType;
    }

    public void setElementType(String elementType) {
        this.elementType = elementType;
    }

    public String getElementRole() {
        return elementRole;
    }

    public void setElementRole(String elementRole) {
        this.elementRole = elementRole;
    }

    public String getElementResilence() {
        return elementResilence;
    }

    public void setElementResilence(String elementResilence) {
        this.elementResilence = elementResilence;
    }

    public String getElementRelationshipType() {
        return elementRelationshipType;
    }

    public void setElementRelationshipType(String elementRelationshipType) {
        this.elementRelationshipType = elementRelationshipType;
    }

    public String getElementRelationshipName() {
        return elementRelationshipName;
    }

    public void setElementRelationshipName(String elementRelationshipName) {
        this.elementRelationshipName = elementRelationshipName;
    }

    public Date getElementCeasedDate() {
        return elementCeasedDate;
    }

    public void setElementCeasedDate(Date elementCeasedDate) {
        this.elementCeasedDate = elementCeasedDate;
    }

    public String getElementSourceSystem() {
        return elementSourceSystem;
    }

    public void setElementSourceSystem(String elementSourceSystem) {
        this.elementSourceSystem = elementSourceSystem;
    }

    public String getElementInsPref() {
        return elementInsPref;
    }

    public void setElementInsPref(String elementInsPref) {
        this.elementInsPref = elementInsPref;
    }

    public String getRootElementType() {
        return rootElementType;
    }

    public void setRootElementType(String rootElementType) {
        this.rootElementType = rootElementType;
    }

    public Long getRootElementId() {
        return rootElementId;
    }

    public void setRootElementId(Long rootElementId) {
        this.rootElementId = rootElementId;
    }

    public Long getRelElementId() {
        return relElementId;
    }

    public void setRelElementId(Long relElementId) {
        this.relElementId = relElementId;
    }

    public Long getElementId() {
        return elementId;
    }

    public void setElementId(Long elementId) {
        this.elementId = elementId;
    }

    public String getRootElementIdentifier() {
        return rootElementIdentifier;
    }

    public void setRootElementIdentifier(String rootElementIdentifier) {
        this.rootElementIdentifier = rootElementIdentifier;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }
}
