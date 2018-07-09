package com.bt.nrm.repository.entity;

import com.bt.nrm.dto.RequestAttributeDTO;
import com.microsoft.schemas.x2003.x10.serialization.Char;
import org.hibernate.annotations.GenericGenerator;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.sql.Timestamp;

import static com.bt.rsqe.utils.AssertObject.isNotNull;

@Entity
@Table(name = "REQUEST_ATTRIBUTE")
public class RequestAttributeEntity {

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    @Column(name = "REQUEST_ATTRIBUTE_ID")
    private String requestAttributeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "REQUEST_ID")
    private RequestEntity requestEntity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "REQUEST_SITE_ID")
    private RequestSiteEntity requestSiteEntity;

    @Column(name = "SQE_UNIQUE_ID")
    private String sqeUniqueId;

    @Column(name = "ATTRIBUTE_NAME")
    private String attributeName;

    @Column(name = "DEFAULT_VALUE")
    private String defaultValue;

    @Column(name = "ATTRIBUTE_VALUE_HEADER")
    private String attributeValueHeader;

    @Column(name = "ATTRIBUTE_VALUE")
    private String attributeValue;

    @Column(name = "ATTRIBUTE_VALUE_DISPLAY_NAME")
    private String attributeValueDisplayName;

    @Column(name = "ATTRIBUTE_VALUE_DISPLAY_INDEX")
    private Long attributeValueDisplayIndex;

    @Column(name = "CONTROLLER_TYPE")
    private String controllerType;

    @Column(name = "REQUIRED")
    private Character required;

    @Column(name = "DATA_TYPE")
    private String dataType;

    @Column(name = "DISPLAY_NAME")
    private String displayName;

    @Column(name = "DISPLAY_INDEX")
    private Long displayIndex;

    @Column(name = "TOOLTIP")
    private String toolTip;

    @Column(name = "MINIMUM_LENGTH")
    private Long minimumLength;

    @Column(name = "MAXIMUM_LENGTH")
    private Long maximumLength;

    @Column(name = "MINIMUM_DATA_VALUE")
    private String minimumDataValue;

    @Column(name = "MAXIMUM_DATA_VALUE")
    private String maximumDataValue;

    @Column(name = "FREEDOM_TEXT_COLUMNS")
    private Long freedomTextColumns;

    @Column(name = "FREEDOM_TEXT_ROWS")
    private Long freedomTextRows;

    @Column(name = "ATTRIBUTE_PLACEHOLDER")
    private String attributePlaceholder;

    @Column(name = "CREATED_DATE")
    private Timestamp createdDate;

    @Column(name = "CREATED_USER")
    private String createdUser;

    @Column(name = "MODIFIED_DATE")
    private Timestamp modifiedDate;

    @Column(name = "MODIFIED_USER")
    private String modifiedUser;


    public RequestAttributeEntity() {
    }

    public RequestAttributeEntity(String requestAttributeId, RequestEntity requestEntity, RequestSiteEntity requestSiteEntity, String sqeUniqueId, String attributeName, String defaultValue, String attributeValueHeader, String attributeValue, String attributeValueDisplayName, Long attributeValueDisplayIndex, String controllerType, Character required, String dataType, String displayName, Long displayIndex, String toolTip, Long minimumLength, Long maximumLength, String minimumDataValue, String maximumDataValue, Long freedomTextColumns, Long freedomTextRows, String attributePlaceholder, Timestamp createdDate, String createdUser, Timestamp modifiedDate, String modifiedUser) {
        this.requestAttributeId = requestAttributeId;
        this.requestEntity = requestEntity;
        this.requestSiteEntity = requestSiteEntity;
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
        this.createdDate = createdDate;
        this.createdUser = createdUser;
        this.modifiedDate = modifiedDate;
        this.modifiedUser = modifiedUser;
    }

    public String getRequestAttributeId() {
        return requestAttributeId;
    }

    public void setRequestAttributeId(String requestAttributeId) {
        this.requestAttributeId = requestAttributeId;
    }

    public RequestEntity getRequestEntity() {
        return requestEntity;
    }

    public void setRequestEntity(RequestEntity requestEntity) {
        this.requestEntity = requestEntity;
    }

    public RequestSiteEntity getRequestSiteEntity() {
        return requestSiteEntity;
    }

    public void setRequestSiteEntity(RequestSiteEntity requestSiteEntity) {
        this.requestSiteEntity = requestSiteEntity;
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

    public Timestamp getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Timestamp createdDate) {
        this.createdDate = createdDate;
    }

    public String getCreatedUser() {
        return createdUser;
    }

    public void setCreatedUser(String createdUser) {
        this.createdUser = createdUser;
    }

    public Timestamp getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(Timestamp modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    public String getModifiedUser() {
        return modifiedUser;
    }

    public void setModifiedUser(String modifiedUser) {
        this.modifiedUser = modifiedUser;
    }

    public RequestAttributeDTO toDTO(RequestAttributeDTO dto){
        if(dto!=null){
            dto.setRequestAttributeId(this.getRequestAttributeId());
            dto.setRequestId(this.getRequestEntity().getRequestId());
            if(isNotNull(this.getRequestSiteEntity())) {
                dto.setSiteId(this.getRequestSiteEntity().getSiteId());
            }
            dto.setSqeUniqueId(this.getSqeUniqueId());
            dto.setAttributeName(this.getAttributeName());
            dto.setDefaultValue(this.getDefaultValue());
            dto.setAttributeValueHeader(this.getAttributeValueHeader());
            dto.setAttributeValue(this.getAttributeValue());
            dto.setAttributeValueDisplayName(this.getAttributeValueDisplayName());
            dto.setAttributeValueDisplayIndex(this.getAttributeValueDisplayIndex());
            dto.setControllerType(this.getControllerType());
            dto.setDataType(this.getDataType());
            dto.setRequired(this.getRequired());
            dto.setDataType(this.getDataType());
            dto.setDisplayName(this.getDisplayName());
            dto.setDisplayIndex(this.getDisplayIndex());
            dto.setToolTip(this.getToolTip());
            dto.setMinimumLength(this.getMinimumLength());
            dto.setMaximumLength(this.getMaximumLength());
            dto.setMinimumDataValue(this.getMinimumDataValue());
            dto.setMaximumDataValue(this.getMaximumDataValue());
            dto.setFreedomTextRows(this.getFreedomTextRows());
            dto.setFreedomTextColumns(this.getFreedomTextColumns());
            dto.setAttributePlaceholder(this.getAttributePlaceholder());
        }
        return dto;
    }

    public RequestAttributeDTO toNewDTO(){
        return toDTO(new RequestAttributeDTO());
    }


}
