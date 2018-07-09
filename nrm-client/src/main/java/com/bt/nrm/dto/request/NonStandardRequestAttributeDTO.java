package com.bt.nrm.dto.request;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by 608143048 on 10/12/2015.
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class NonStandardRequestAttributeDTO {

    private String attributeId;
    private String attributeCode;
    private String attributeName;
    private String attributeValue;

    public NonStandardRequestAttributeDTO() {
    }

    public NonStandardRequestAttributeDTO(String attributeId, String attributeCode, String attributeName, String attributeValue) {
        this.attributeId = attributeId;
        this.attributeCode = attributeCode;
        this.attributeName = attributeName;
        this.attributeValue = attributeValue;
    }

    public String getAttributeId() {
        return attributeId;
    }

    public void setAttributeId(String attributeId) {
        this.attributeId = attributeId;
    }

    public String getAttributeCode() {
        return attributeCode;
    }

    public void setAttributeCode(String attributeCode) {
        this.attributeCode = attributeCode;
    }

    public String getAttributeName() {
        return attributeName;
    }

    public void setAttributeName(String attributeName) {
        this.attributeName = attributeName;
    }

    public String getAttributeValue() {
        return attributeValue;
    }

    public void setAttributeValue(String attributeValue) {
        this.attributeValue = attributeValue;
    }
}
