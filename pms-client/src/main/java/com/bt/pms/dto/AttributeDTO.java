package com.bt.pms.dto;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class AttributeDTO {
    @XmlAttribute
    private String attributeId;
    @XmlAttribute
    private String attributeCode;
    @XmlAttribute
    private String attributeName;
    @XmlAttribute
    private String attributeDescription;
    @XmlAttribute
    private String attributeValue;
    @XmlAttribute
    private String defaultValue;
    @XmlAttribute
    private List<String> attributeValueLOV;
    @XmlAttribute
    private String attributeValueHeader;
    @XmlAttribute
    private String attributeValueDisplayName;
    @XmlAttribute
    private Long attributeValueDisplayIndex;
    @XmlAttribute
    private String controllerType;
    @XmlAttribute
    private String displayName;
    @XmlAttribute
    private Long displayIndex;
    @XmlAttribute
    private String isRequired;
    @XmlAttribute
    private String dataType;
    @XmlAttribute
    private String tooltip;
    @XmlAttribute
    private Long minimumLength;
    @XmlAttribute
    private Long maximumLength;
    @XmlAttribute
    private String minimumDataValue;
    @XmlAttribute
    private String maximumDataValue;
    @XmlAttribute
    private Long freedomTextColumns;
    @XmlAttribute
    private Long freedomTextRows;
    @XmlAttribute
    private String attributePlaceHolder;

    public AttributeDTO() {
    }

    public AttributeDTO(String attributeId, String attributeCode, String attributeName, String attributeDescription, String attributeValue, String defaultValue, List<String> attributeValueLOV, String attributeValueHeader, String attributeValueDisplayName, Long attributeValueDisplayIndex, String controllerType, String displayName, Long displayIndex, String isRequired, String dataType, String tooltip, Long minimumLength, Long maximumLength, String minimumDataValue, String maximumDataValue, Long freedomTextColumns, Long freedomTextRows, String attributePlaceHolder) {
        this.attributeId = attributeId;
        this.attributeCode = attributeCode;
        this.attributeName = attributeName;
        this.attributeDescription = attributeDescription;
        this.attributeValue = attributeValue;
        this.defaultValue = defaultValue;
        this.attributeValueLOV = attributeValueLOV;
        this.attributeValueHeader = attributeValueHeader;
        this.attributeValueDisplayName = attributeValueDisplayName;
        this.attributeValueDisplayIndex = attributeValueDisplayIndex;
        this.controllerType = controllerType;
        this.displayName = displayName;
        this.displayIndex = displayIndex;
        this.isRequired = isRequired;
        this.dataType = dataType;
        this.tooltip = tooltip;
        this.minimumLength = minimumLength;
        this.maximumLength = maximumLength;
        this.minimumDataValue = minimumDataValue;
        this.maximumDataValue = maximumDataValue;
        this.freedomTextColumns = freedomTextColumns;
        this.freedomTextRows = freedomTextRows;
        this.attributePlaceHolder = attributePlaceHolder;
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

    public String getAttributeDescription() {
        return attributeDescription;
    }

    public void setAttributeDescription(String attributeDescription) {
        this.attributeDescription = attributeDescription;
    }

    public String getAttributeValue() {
        return attributeValue;
    }

    public void setAttributeValue(String attributeValue) {
        this.attributeValue = attributeValue;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public List<String> getAttributeValueLOV() {
        return attributeValueLOV;
    }

    public void setAttributeValueLOV(List<String> attributeValueLOV) {
        this.attributeValueLOV = attributeValueLOV;
    }

    public String getAttributeValueHeader() {
        return attributeValueHeader;
    }

    public void setAttributeValueHeader(String attributeValueHeader) {
        this.attributeValueHeader = attributeValueHeader;
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

    public String getIsRequired() {
        return isRequired;
    }

    public void setIsRequired(String isRequired) {
        this.isRequired = isRequired;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public String getTooltip() {
        return tooltip;
    }

    public void setTooltip(String tooltip) {
        this.tooltip = tooltip;
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

    public String getAttributePlaceHolder() {
        return attributePlaceHolder;
    }

    public void setAttributePlaceHolder(String attributePlaceHolder) {
        this.attributePlaceHolder = attributePlaceHolder;
    }
}
