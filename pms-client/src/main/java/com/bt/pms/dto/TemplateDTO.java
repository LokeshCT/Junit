package com.bt.pms.dto;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.sql.Timestamp;
import java.util.List;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class TemplateDTO {

    @XmlAttribute
    private String templateId;
    @XmlAttribute
    private String templateCode;
    @XmlAttribute
    private String versionNumber;
    @XmlAttribute
    private String templateName;
    @XmlAttribute
    private String templateDescription;
    @XmlAttribute
    private Timestamp effectiveStartDate;
    @XmlAttribute
    private Timestamp effectiveEndDate;
    @XmlAttribute
    private String legacyIdentifier;
    @XmlAttribute
    private String batchIdentifier;
    @XmlAttribute
    private String fileSequenceNumber;
    @XmlAttribute
    private String batchFileCount;
    @XmlAttribute
    private String toolVersion;
    @XmlAttribute
    private String productCode;
    @XmlAttribute
    private String productName;
    @XmlAttribute
    private WorkflowTypeDTO workflowType = new WorkflowTypeDTO();
    @XmlAttribute
    private String currencyCode;
    @XmlAttribute
    private List<AttributeDTO> commonDetails;
    @XmlAttribute
    private List<AttributeDTO> responseDetails;
    @XmlAttribute
    private TemplateFlexibleDetailDTO primaryDetails;
    @XmlAttribute
    private TemplateFlexibleDetailDTO secondaryDetails;
    @XmlAttribute
    private CustomerOneTimeChargeDTO COTC;
    @XmlAttribute
    private String configurationType;
    @XmlAttribute
    private String reusable;
    @XmlAttribute
    private String maximumValidity;
    @XmlAttribute
    private String specialBidCategory;
    @XmlAttribute
    private String usTaxCategory;
    @XmlAttribute
    private Long turnAroundTime;
    @XmlAttribute
    private Boolean dataBuildRequired;
    @XmlAttribute
    private String wikiURL;
    @XmlAttribute
    private String attachmentURL;
    @XmlAttribute
    private String billDescription;
    @XmlAttribute
    private String decisionCriteria;
    @XmlAttribute
    private String negativeResponseDefaultText;
    @XmlAttribute
    private String positiveResponseDefaultText;
    @XmlAttribute
    private String versionCreatedBy;
    @XmlAttribute
    private Timestamp versionCreatedDate;
    @XmlAttribute
    private String versionActivatedBy;
    @XmlAttribute
    private String versionDeactivatedBy;
    @XmlAttribute
    private String alertEmailAddress;
    @XmlAttribute
    private String isAlertOnApprovalOnly;
    @XmlAttribute
    private String alertMessage;
    @XmlAttribute
    private String isVPNDataRequired;
    @XmlAttribute
    private String isCOTCVisible;
    // Added new to hold Template State based on effective (start and end date)
    @XmlAttribute
    private String templateState;

    public TemplateDTO() {
    }

    public TemplateDTO(String templateCode, String versionNumber, String templateName, String templateDescription, Timestamp effectiveStartDate, Timestamp effectiveEndDate, String legacyIdentifier, String batchIdentifier, String fileSequenceNumber, String batchFileCount, String toolVersion, String productCode, String productName, WorkflowTypeDTO workflowType, String currencyCode, List<AttributeDTO> commonDetails, List<AttributeDTO> responseDetails, TemplateFlexibleDetailDTO primaryDetails, TemplateFlexibleDetailDTO secondaryDetails, CustomerOneTimeChargeDTO COTC, String configurationType, String reusable, String maximumValidity, String specialBidCategory, String usTaxCategory, Long turnAroundTime, Boolean dataBuildRequired, String wikiURL, String attachmentURL, String billDescription, String decisionCriteria, String negativeResponseDefaultText, String positiveResponseDefaultText, String versionCreatedBy, Timestamp versionCreatedDate, String versionActivatedBy, String versionDeactivatedBy, String alertEmailAddress, String alertOnApprovalOnly, String alertMessage, String VPNDataRequired, String COTCVisible, String templateState) {
        this.templateCode = templateCode;
        this.versionNumber = versionNumber;
        this.templateName = templateName;
        this.templateDescription = templateDescription;
        this.effectiveStartDate = effectiveStartDate;
        this.effectiveEndDate = effectiveEndDate;
        this.legacyIdentifier = legacyIdentifier;
        this.batchIdentifier = batchIdentifier;
        this.fileSequenceNumber = fileSequenceNumber;
        this.batchFileCount = batchFileCount;
        this.toolVersion = toolVersion;
        this.productCode = productCode;
        this.productName = productName;
        this.workflowType = workflowType;
        this.currencyCode = currencyCode;
        this.commonDetails = commonDetails;
        this.responseDetails = responseDetails;
        this.primaryDetails = primaryDetails;
        this.secondaryDetails = secondaryDetails;
        this.COTC = COTC;
        this.configurationType = configurationType;
        this.reusable = reusable;
        this.maximumValidity = maximumValidity;
        this.specialBidCategory = specialBidCategory;
        this.usTaxCategory = usTaxCategory;
        this.turnAroundTime = turnAroundTime;
        this.dataBuildRequired = dataBuildRequired;
        this.wikiURL = wikiURL;
        this.attachmentURL = attachmentURL;
        this.billDescription = billDescription;
        this.decisionCriteria = decisionCriteria;
        this.negativeResponseDefaultText = negativeResponseDefaultText;
        this.positiveResponseDefaultText = positiveResponseDefaultText;
        this.versionCreatedBy = versionCreatedBy;
        this.versionCreatedDate = versionCreatedDate;
        this.versionActivatedBy = versionActivatedBy;
        this.versionDeactivatedBy = versionDeactivatedBy;
        this.alertEmailAddress = alertEmailAddress;
        isAlertOnApprovalOnly = alertOnApprovalOnly;
        this.alertMessage = alertMessage;
        isVPNDataRequired = VPNDataRequired;
        isCOTCVisible = COTCVisible;
        this.templateState = templateState;
    }

    public String getTemplateCode() {
        return templateCode;
    }

    public void setTemplateCode(String templateCode) {
        this.templateCode = templateCode;
    }

    public String getVersionNumber() {
        return versionNumber;
    }

    public void setVersionNumber(String versionNumber) {
        this.versionNumber = versionNumber;
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public String getTemplateDescription() {
        return templateDescription;
    }

    public void setTemplateDescription(String templateDescription) {
        this.templateDescription = templateDescription;
    }

    public Timestamp getEffectiveStartDate() {
        return effectiveStartDate;
    }

    public void setEffectiveStartDate(Timestamp effectiveStartDate) {
        this.effectiveStartDate = effectiveStartDate;
    }

    public Timestamp getEffectiveEndDate() {
        return effectiveEndDate;
    }

    public void setEffectiveEndDate(Timestamp effectiveEndDate) {
        this.effectiveEndDate = effectiveEndDate;
    }

    public String getLegacyIdentifier() {
        return legacyIdentifier;
    }

    public void setLegacyIdentifier(String legacyIdentifier) {
        this.legacyIdentifier = legacyIdentifier;
    }

    public String getBatchIdentifier() {
        return batchIdentifier;
    }

    public void setBatchIdentifier(String batchIdentifier) {
        this.batchIdentifier = batchIdentifier;
    }

    public String getFileSequenceNumber() {
        return fileSequenceNumber;
    }

    public void setFileSequenceNumber(String fileSequenceNumber) {
        this.fileSequenceNumber = fileSequenceNumber;
    }

    public String getBatchFileCount() {
        return batchFileCount;
    }

    public void setBatchFileCount(String batchFileCount) {
        this.batchFileCount = batchFileCount;
    }

    public String getToolVersion() {
        return toolVersion;
    }

    public void setToolVersion(String toolVersion) {
        this.toolVersion = toolVersion;
    }

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public WorkflowTypeDTO getWorkflowType() {
        return workflowType;
    }

    public void setWorkflowType(WorkflowTypeDTO workflowType) {
        this.workflowType = workflowType;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public List<AttributeDTO> getCommonDetails() {
        return commonDetails;
    }

    public void setCommonDetails(List<AttributeDTO> commonDetails) {
        this.commonDetails = commonDetails;
    }

    public TemplateFlexibleDetailDTO getPrimaryDetails() {
        return primaryDetails;
    }

    public void setPrimaryDetails(TemplateFlexibleDetailDTO primaryDetails) {
        this.primaryDetails = primaryDetails;
    }

    public TemplateFlexibleDetailDTO getSecondaryDetails() {
        return secondaryDetails;
    }

    public void setSecondaryDetails(TemplateFlexibleDetailDTO secondaryDetails) {
        this.secondaryDetails = secondaryDetails;
    }

    public CustomerOneTimeChargeDTO getCOTC() {
        return COTC;
    }

    public void setCOTC(CustomerOneTimeChargeDTO COTC) {
        this.COTC = COTC;
    }

    public String getConfigurationType() {
        return configurationType;
    }

    public void setConfigurationType(String configurationType) {
        this.configurationType = configurationType;
    }

    public String getReusable() {
        return reusable;
    }

    public void setReusable(String reusable) {
        this.reusable = reusable;
    }

    public String getMaximumValidity() {
        return maximumValidity;
    }

    public void setMaximumValidity(String maximumValidity) {
        this.maximumValidity = maximumValidity;
    }

    public String getSpecialBidCategory() {
        return specialBidCategory;
    }

    public void setSpecialBidCategory(String specialBidCategory) {
        this.specialBidCategory = specialBidCategory;
    }

    public String getUsTaxCategory() {
        return usTaxCategory;
    }

    public void setUsTaxCategory(String usTaxCategory) {
        this.usTaxCategory = usTaxCategory;
    }

    public Long getTurnAroundTime() {
        return turnAroundTime;
    }

    public void setTurnAroundTime(Long turnAroundTime) {
        this.turnAroundTime = turnAroundTime;
    }

    public Boolean getDataBuildRequired() {
        return dataBuildRequired;
    }

    public void setDataBuildRequired(Boolean dataBuildRequired) {
        this.dataBuildRequired = dataBuildRequired;
    }

    public String getWikiURL() {
        return wikiURL;
    }

    public void setWikiURL(String wikiURL) {
        this.wikiURL = wikiURL;
    }

    public String getAttachmentURL() {
        return attachmentURL;
    }

    public void setAttachmentURL(String attachmentURL) {
        this.attachmentURL = attachmentURL;
    }

    public String getBillDescription() {
        return billDescription;
    }

    public void setBillDescription(String billDescription) {
        this.billDescription = billDescription;
    }

    public String getDecisionCriteria() {
        return decisionCriteria;
    }

    public void setDecisionCriteria(String decisionCriteria) {
        this.decisionCriteria = decisionCriteria;
    }

    public String getNegativeResponseDefaultText() {
        return negativeResponseDefaultText;
    }

    public void setNegativeResponseDefaultText(String negativeResponseDefaultText) {
        this.negativeResponseDefaultText = negativeResponseDefaultText;
    }

    public String getPositiveResponseDefaultText() {
        return positiveResponseDefaultText;
    }

    public void setPositiveResponseDefaultText(String positiveResponseDefaultText) {
        this.positiveResponseDefaultText = positiveResponseDefaultText;
    }

    public String getVersionCreatedBy() {
        return versionCreatedBy;
    }

    public void setVersionCreatedBy(String versionCreatedBy) {
        this.versionCreatedBy = versionCreatedBy;
    }

    public Timestamp getVersionCreatedDate() {
        return versionCreatedDate;
    }

    public void setVersionCreatedDate(Timestamp versionCreatedDate) {
        this.versionCreatedDate = versionCreatedDate;
    }

    public String getVersionActivatedBy() {
        return versionActivatedBy;
    }

    public void setVersionActivatedBy(String versionActivatedBy) {
        this.versionActivatedBy = versionActivatedBy;
    }

    public String getVersionDeactivatedBy() {
        return versionDeactivatedBy;
    }

    public void setVersionDeactivatedBy(String versionDeactivatedBy) {
        this.versionDeactivatedBy = versionDeactivatedBy;
    }

    public String getAlertEmailAddress() {
        return alertEmailAddress;
    }

    public void setAlertEmailAddress(String alertEmailAddress) {
        this.alertEmailAddress = alertEmailAddress;
    }

    public String getIsAlertOnApprovalOnly() {
        return isAlertOnApprovalOnly;
    }

    public void setIsAlertOnApprovalOnly(String isAlertOnApprovalOnly) {
        this.isAlertOnApprovalOnly = isAlertOnApprovalOnly;
    }

    public String getAlertMessage() {
        return alertMessage;
    }

    public void setAlertMessage(String alertMessage) {
        this.alertMessage = alertMessage;
    }

    public List<AttributeDTO> getResponseDetails() {
        return responseDetails;
    }

    public void setResponseDetails(List<AttributeDTO> responseDetails) {
        this.responseDetails = responseDetails;
    }

    public String getTemplateState() {
        return templateState;
    }

    public void setTemplateState(String templateState) {
        this.templateState = templateState;
    }

    public String getVPNDataRequired() {
        return isVPNDataRequired;
    }

    public void setVPNDataRequired(String VPNDataRequired) {
        isVPNDataRequired = VPNDataRequired;
    }

    public String getCOTCVisible() {
        return isCOTCVisible;
    }

    public void setCOTCVisible(String COTCVisible) {
        isCOTCVisible = COTCVisible;
    }

    public String getAlertOnApprovalOnly() {
        return isAlertOnApprovalOnly;
    }

    public void setAlertOnApprovalOnly(String alertOnApprovalOnly) {
        isAlertOnApprovalOnly = alertOnApprovalOnly;
    }

    public String getTemplateId() {
        return templateId;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }
}
