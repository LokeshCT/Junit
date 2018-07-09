package com.bt.nrm.dto;

import com.bt.nrm.dto.request.QuoteDTO;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Date;
import java.util.List;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class RequestDTO {

    private String requestId;
    private String requestName;
    private QuoteDTO quote;
    private String productCategoryCode;
    private String productCategoryName;
    private String templateCode;
    private String templateVersion;
    private String templateName;
    private String workFlowType;
    private String evaluatedMode;
    private String outputDescription;
    private String responseType;
    private String configurationType;
    private Character isReusable;
    private String specialBidCategory;
    private String USTaxCategory;
    private Character dataBuildRequired;
    private Character dataBuildCompleted;
    private String expectedResponseTime;
    private String state;
    private String publicURL;
    private String attachments;
    private String billDescription;
    private String detailedResponse;
    private Character isAlertOnApprovalOnly;
    private String alertMessage;
    private String alertEmailAddress;
    private Character isVPNDataRequired;
    private Character isCOTCVisible;
    private String comments;
    private List<RequestSiteDTO> requestSites;
    private List<RequestAttributeDTO> commonNResponseAttributes;
    private List<RequestEvaluatorDTO> requestEvaluators;
    private List<RequestHistoryDTO> requestHistoryLogs;
    private List<RequestWhoHasSeenDTO> requestWhoHasSeenLogs;
    private String createdUser;
    private Date createdDate;
    private String modifiedUser;
    private Date modifiedDate;

    public RequestDTO() {
    }

    public RequestDTO(String requestId, String requestName, QuoteDTO quote, String productCategoryCode, String productCategoryName, String templateCode, String templateVersion, String templateName, String workFlowType, String evaluatedMode, String outputDescription, String responseType, String configurationType, Character isReusable, String specialBidCategory, String USTaxCategory, Character dataBuildRequired, Character dataBuildCompleted, String expectedResponseTime, String state, String publicURL, String attachments, String billDescription, String detailedResponse, Character isAlertOnApprovalOnly, String alertMessage, String alertEmailAddress, Character isVPNDataRequired, Character isCOTCVisible, String comments, List<RequestSiteDTO> requestSites, List<RequestAttributeDTO> commonNResponseAttributes, List<RequestEvaluatorDTO> requestEvaluators, List<RequestHistoryDTO> requestHistoryLogs, List<RequestWhoHasSeenDTO> requestWhoHasSeenLogs, String createdUser, Date createdDate, String modifiedUser, Date modifiedDate) {
        this.requestId = requestId;
        this.requestName = requestName;
        this.quote = quote;
        this.productCategoryCode = productCategoryCode;
        this.productCategoryName = productCategoryName;
        this.templateCode = templateCode;
        this.templateVersion = templateVersion;
        this.templateName = templateName;
        this.workFlowType = workFlowType;
        this.evaluatedMode = evaluatedMode;
        this.outputDescription = outputDescription;
        this.responseType = responseType;
        this.configurationType = configurationType;
        this.isReusable = isReusable;
        this.specialBidCategory = specialBidCategory;
        this.USTaxCategory = USTaxCategory;
        this.dataBuildRequired = dataBuildRequired;
        this.dataBuildCompleted = dataBuildCompleted;
        this.expectedResponseTime = expectedResponseTime;
        this.state = state;
        this.publicURL = publicURL;
        this.attachments = attachments;
        this.billDescription = billDescription;
        this.detailedResponse = detailedResponse;
        this.isAlertOnApprovalOnly = isAlertOnApprovalOnly;
        this.alertMessage = alertMessage;
        this.alertEmailAddress = alertEmailAddress;
        this.isVPNDataRequired = isVPNDataRequired;
        this.isCOTCVisible = isCOTCVisible;
        this.comments = comments;
        this.requestSites = requestSites;
        this.commonNResponseAttributes = commonNResponseAttributes;
        this.requestEvaluators = requestEvaluators;
        this.requestHistoryLogs = requestHistoryLogs;
        this.requestWhoHasSeenLogs = requestWhoHasSeenLogs;
        this.createdUser = createdUser;
        this.createdDate = createdDate;
        this.modifiedUser = modifiedUser;
        this.modifiedDate = modifiedDate;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getRequestName() {
        return requestName;
    }

    public void setRequestName(String requestName) {
        this.requestName = requestName;
    }

    public QuoteDTO getQuote() {
        return quote;
    }

    public void setQuote(QuoteDTO quote) {
        this.quote = quote;
    }

    public String getProductCategoryCode() {
        return productCategoryCode;
    }

    public void setProductCategoryCode(String productCategoryCode) {
        this.productCategoryCode = productCategoryCode;
    }

    public String getProductCategoryName() {
        return productCategoryName;
    }

    public void setProductCategoryName(String productCategoryName) {
        this.productCategoryName = productCategoryName;
    }

    public String getTemplateCode() {
        return templateCode;
    }

    public void setTemplateCode(String templateCode) {
        this.templateCode = templateCode;
    }

    public String getTemplateVersion() {
        return templateVersion;
    }

    public void setTemplateVersion(String templateVersion) {
        this.templateVersion = templateVersion;
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public String getWorkFlowType() {
        return workFlowType;
    }

    public void setWorkFlowType(String workFlowType) {
        this.workFlowType = workFlowType;
    }

    public String getEvaluatedMode() {
        return evaluatedMode;
    }

    public void setEvaluatedMode(String evaluatedMode) {
        this.evaluatedMode = evaluatedMode;
    }

    public String getOutputDescription() {
        return outputDescription;
    }

    public void setOutputDescription(String outputDescription) {
        this.outputDescription = outputDescription;
    }

    public String getResponseType() {
        return responseType;
    }

    public void setResponseType(String responseType) {
        this.responseType = responseType;
    }

    public String getConfigurationType() {
        return configurationType;
    }

    public void setConfigurationType(String configurationType) {
        this.configurationType = configurationType;
    }

    public Character getIsReusable() {
        return isReusable;
    }

    public void setIsReusable(Character isReusable) {
        this.isReusable = isReusable;
    }

    public String getSpecialBidCategory() {
        return specialBidCategory;
    }

    public void setSpecialBidCategory(String specialBidCategory) {
        this.specialBidCategory = specialBidCategory;
    }

    public String getUSTaxCategory() {
        return USTaxCategory;
    }

    public void setUSTaxCategory(String USTaxCategory) {
        this.USTaxCategory = USTaxCategory;
    }

    public Character getDataBuildRequired() {
        return dataBuildRequired;
    }

    public void setDataBuildRequired(Character dataBuildRequired) {
        this.dataBuildRequired = dataBuildRequired;
    }

    public Character getDataBuildCompleted() {
        return dataBuildCompleted;
    }

    public void setDataBuildCompleted(Character dataBuildCompleted) {
        this.dataBuildCompleted = dataBuildCompleted;
    }

    public String getExpectedResponseTime() {
        return expectedResponseTime;
    }

    public void setExpectedResponseTime(String expectedResponseTime) {
        this.expectedResponseTime = expectedResponseTime;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getPublicURL() {
        return publicURL;
    }

    public void setPublicURL(String publicURL) {
        this.publicURL = publicURL;
    }

    public String getAttachments() {
        return attachments;
    }

    public void setAttachments(String attachments) {
        this.attachments = attachments;
    }

    public String getBillDescription() {
        return billDescription;
    }

    public void setBillDescription(String billDescription) {
        this.billDescription = billDescription;
    }

    public String getDetailedResponse() {
        return detailedResponse;
    }

    public void setDetailedResponse(String detailedResponse) {
        this.detailedResponse = detailedResponse;
    }

    public Character getIsAlertOnApprovalOnly() {
        return isAlertOnApprovalOnly;
    }

    public void setIsAlertOnApprovalOnly(Character isAlertOnApprovalOnly) {
        this.isAlertOnApprovalOnly = isAlertOnApprovalOnly;
    }

    public String getAlertMessage() {
        return alertMessage;
    }

    public void setAlertMessage(String alertMessage) {
        this.alertMessage = alertMessage;
    }

    public String getAlertEmailAddress() {
        return alertEmailAddress;
    }

    public void setAlertEmailAddress(String alertEmailAddress) {
        this.alertEmailAddress = alertEmailAddress;
    }

    public Character getIsVPNDataRequired() {
        return isVPNDataRequired;
    }

    public void setIsVPNDataRequired(Character isVPNDataRequired) {
        this.isVPNDataRequired = isVPNDataRequired;
    }

    public Character getIsCOTCVisible() {
        return isCOTCVisible;
    }

    public void setIsCOTCVisible(Character isCOTCVisible) {
        this.isCOTCVisible = isCOTCVisible;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public List<RequestSiteDTO> getRequestSites() {
        return requestSites;
    }

    public void setRequestSites(List<RequestSiteDTO> requestSites) {
        this.requestSites = requestSites;
    }

    public List<RequestAttributeDTO> getCommonNResponseAttributes() {
        return commonNResponseAttributes;
    }

    public void setCommonNResponseAttributes(List<RequestAttributeDTO> commonNResponseAttributes) {
        this.commonNResponseAttributes = commonNResponseAttributes;
    }

    public List<RequestEvaluatorDTO> getRequestEvaluators() {
        return requestEvaluators;
    }

    public void setRequestEvaluators(List<RequestEvaluatorDTO> requestEvaluators) {
        this.requestEvaluators = requestEvaluators;
    }

    public List<RequestHistoryDTO> getRequestHistoryLogs() {
        return requestHistoryLogs;
    }

    public void setRequestHistoryLogs(List<RequestHistoryDTO> requestHistoryLogs) {
        this.requestHistoryLogs = requestHistoryLogs;
    }

    public List<RequestWhoHasSeenDTO> getRequestWhoHasSeenLogs() {
        return requestWhoHasSeenLogs;
    }

    public void setRequestWhoHasSeenLogs(List<RequestWhoHasSeenDTO> requestWhoHasSeenLogs) {
        this.requestWhoHasSeenLogs = requestWhoHasSeenLogs;
    }

    public String getCreatedUser() {
        return createdUser;
    }

    public void setCreatedUser(String createdUser) {
        this.createdUser = createdUser;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public String getModifiedUser() {
        return modifiedUser;
    }

    public void setModifiedUser(String modifiedUser) {
        this.modifiedUser = modifiedUser;
    }

    public Date getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(Date modifiedDate) {
        this.modifiedDate = modifiedDate;
    }
}
