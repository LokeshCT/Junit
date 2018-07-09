package com.bt.nrm.dto;

import com.bt.nrm.repository.entity.RequestEntity;
import com.microsoft.schemas.x2003.x10.serialization.Char;

import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Date;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class RequestAttributeDTO {

    private String requestAttributeId;
    private String requestId;
    private String siteId;
    private String sqeUniqueId;
    private String attributeName;
    private String defaultValue;
    private String attributeValueHeader;
    private String attributeValue;
    private String attributeValueDisplayName;
    private Long attributeValueDisplayIndex;
    private String controllerType;
    private Character required;
    private String dataType;
    private String displayName;
    private Long displayIndex;
    private String toolTip;
    private Long minimumLength;
    private Long maximumLength;
    private String minimumDataValue;
    private String maximumDataValue;
    private Long freedomTextColumns;
    private Long freedomTextRows;
    private String attributePlaceholder;

    public RequestAttributeDTO() {
    }

    public RequestAttributeDTO(String requestAttributeId, String requestId, String siteId, String sqeUniqueId, String attributeName, String defaultValue, String attributeValueHeader, String attributeValue, String attributeValueDisplayName, Long attributeValueDisplayIndex, String controllerType, Character required, String dataType, String displayName, Long displayIndex, String toolTip, Long minimumLength, Long maximumLength, String minimumDataValue, String maximumDataValue, Long freedomTextColumns, Long freedomTextRows, String attributePlaceholder) {
        this.requestAttributeId = requestAttributeId;
        this.requestId = requestId;
        this.siteId = siteId;
        this.sqeUniqueId = sqeUniqueId;
        this.attributeName = attributeName;
        this.defaultValue = defaultValue;
        this.attributeValueHeader = attributeValueHeader;
        this.attributeValue = attributeValue;
        this.attributeValueDisplayName = attributeValueDisplayName;
        this.attributeValueDisplayIndex = attributeValueDisplayIndex;
        this.controllerType = controllerType;
        this.required = required;
        this.dataType = dataType;
        this.displayName = displayName;
        this.displayIndex = displayIndex;
        this.toolTip = toolTip;
        this.minimumLength = minimumLength;
        this.maximumLength = maximumLength;
        this.minimumDataValue = minimumDataValue;
        this.maximumDataValue = maximumDataValue;
        this.freedomTextColumns = freedomTextColumns;
        this.freedomTextRows = freedomTextRows;
        this.attributePlaceholder = attributePlaceholder;
    }

    public String getRequestAttributeId() {
        return requestAttributeId;
    }

    public void setRequestAttributeId(String requestAttributeId) {
        this.requestAttributeId = requestAttributeId;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getSiteId() {
        return siteId;
    }

    public void setSiteId(String siteId) {
        this.siteId = siteId;
    }

    public String getSqeUniqueId() {
        return sqeUniqueId;
    }

    public void setSqeUniqueId(String sqeUniqueId) {
        this.sqeUniqueId = sqeUniqueId;
    }

    public String getAttributeName() {
        return attributeName;
    }

    public void setAttributeName(String attributeName) {
        this.attributeName = attributeName;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getAttributeValueHeader() {
        return attributeValueHeader;
    }

    public void setAttributeValueHeader(String attributeValueHeader) {
        this.attributeValueHeader = attributeValueHeader;
    }

    public String getAttributeValue() {
        return attributeValue;
    }

    public void setAttributeValue(String attributeValue) {
        this.attributeValue = attributeValue;
    }

    public String getAttributeValueDisplayName() {
        return attributeValueDisplayName;
    }

    public void setAttributeValueDisplayName(String attributeValueDisplayName) {
        this.attributeValueDisplayName = attributeValueDisplayName;
    }

    public Long getAttributeValueDisplayIndex() {
        return attributeValueDisplayIndex;
    }

    public void setAttributeValueDisplayIndex(Long attributeValueDisplayIndex) {
        this.attributeValueDisplayIndex = attributeValueDisplayIndex;
    }

    public String getControllerType() {
        return controllerType;
    }

    public void setControllerType(String controllerType) {
        this.controllerType = controllerType;
    }

    public Character getRequired() {
        return required;
    }

    public void setRequired(Character required) {
        this.required = required;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Long getDisplayIndex() {
        return displayIndex;
    }

    public void setDisplayIndex(Long displayIndex) {
        this.displayIndex = displayIndex;
    }

    public String getToolTip() {
        return toolTip;
    }

    public void setToolTip(String toolTip) {
        this.toolTip = toolTip;
    }

    public Long getMinimumLength() {
        return minimumLength;
    }

    public void setMinimumLength(Long minimumLength) {
        this.minimumLength = minimumLength;
    }

    public Long getMaximumLength() {
        return maximumLength;
    }

    public void setMaximumLength(Long maximumLength) {
        this.maximumLength = maximumLength;
    }

    public String getMinimumDataValue() {
        return minimumDataValue;
    }

    public void setMinimumDataValue(String minimumDataValue) {
        this.minimumDataValue = minimumDataValue;
    }

    public String getMaximumDataValue() {
        return maximumDataValue;
    }

    public void setMaximumDataValue(String maximumDataValue) {
        this.maximumDataValue = maximumDataValue;
    }

    public Long getFreedomTextColumns() {
        return freedomTextColumns;
    }

    public void setFreedomTextColumns(Long freedomTextColumns) {
        this.freedomTextColumns = freedomTextColumns;
    }

    public Long getFreedomTextRows() {
        return freedomTextRows;
    }

    public void setFreedomTextRows(Long freedomTextRows) {
        this.freedomTextRows = freedomTextRows;
    }

    public String getAttributePlaceholder() {
        return attributePlaceholder;
    }

    public void setAttributePlaceholder(String attributePlaceholder) {
        this.attributePlaceholder = attributePlaceholder;
    }
}

