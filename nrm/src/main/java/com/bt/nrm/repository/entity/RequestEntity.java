package com.bt.nrm.repository.entity;

import com.bt.nrm.dto.RequestAttributeDTO;
import com.bt.nrm.dto.RequestDTO;
import com.bt.nrm.dto.RequestEvaluatorDTO;
import com.bt.nrm.dto.RequestHistoryDTO;
import com.bt.nrm.dto.RequestSiteDTO;
import com.bt.nrm.dto.RequestWhoHasSeenDTO;
import com.bt.nrm.dto.request.QuoteDTO;
import org.hibernate.annotations.Where;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import static com.bt.rsqe.utils.AssertObject.*;

@Entity
@Table(name = "REQUEST_MASTER")
public class RequestEntity {

    @Id
    @Column(name = "REQUEST_ID")
    private String requestId;

    @Column(name = "REQUEST_NAME")
    private String requestName;

    @ManyToOne(fetch = FetchType.LAZY ,cascade = CascadeType.ALL)
    @JoinColumn(name = "quoteMasterId")
    private QuoteEntity quote;

    @Column(name = "PRODUCT_CATEGORY_CODE")
    private String productCategoryCode;

    @Column(name = "PRODUCT_CATEGORY_NAME")
    private String productCategoryName;

    @Column(name = "TEMPLATE_CODE")
    private String templateCode;

    @Column(name = "TEMPLATE_NAME")
    private String templateName;

    @Column(name = "TEMPLATE_VERSION")
    private String templateVersion;

    @Column(name = "WORK_FLOW_TYPE")
    private String workFlowType;

    @Column(name = "EVALUATED_MODE")
    private String evaluatedMode;

    @Column(name = "OUTPUT_DESC")
    private String outputDescription;

    @Column(name="RESPONSE_TYPE")
    private String responseType;

    @Column(name="CONFIGURATION_TYPE")
    private String configurationType;

    @Column(name="IS_REUSABLE")
    private Character isReusable;

    @Column(name="SPECIAL_BID_CATEGORY")
    private String specialBidCategory;

    @Column(name="US_TAX_CATEGORY")
    private String USTaxCategory;

    @Column(name="DATA_BUILD_REQUIRED")
    private Character dataBuildRequired;

    @Column(name="DATA_BUILD_COMPLETED")
    private Character dataBuildCompleted;

    @Column(name="EXPECTED_RESPONSE_TIME")
    private String expectedResponseTime;

    @Column(name = "STATE")
    private String state;

    @Column(name="PUBLIC_URL")
    private String publicURL;

    @Column(name="ATTACHMENTS")
    private String attachments;

    @Column(name="BILL_DESCRIPTION")
    private String billDescription;

    @Column(name="DETAILED_RESPONSE")
    @Lob
    private String detailedResponse;

    @Column(name="IS_ALERT_ON_APPROVAL_ONLY")
    private Character isAlertOnApprovalOnly;

    @Column(name="ALERT_EMAIL_ADDRESS")
    private String alertEmailAddress;

    @Column(name="ALERT_MESSAGE")
    private String alertMessage;

    @Column(name="IS_VPN_DATA_REQD")
    private Character isVPNDataRequired;

    @Column(name="IS_COTC_VISIBLE")
    private Character isCOTCVisible;

    @Column(name="COMMENTS")
    @Lob
    private String comments;

    @OneToMany(mappedBy = "requestEntity", targetEntity = RequestSiteEntity.class, cascade = CascadeType.ALL)
    private List<RequestSiteEntity> requestSites;

    @OneToMany(mappedBy = "requestEntity",cascade = CascadeType.ALL)
    @Where(clause = "ATTRIBUTE_PLACEHOLDER='RESPONSE' or ATTRIBUTE_PLACEHOLDER='COMMON'")
    private List<RequestAttributeEntity> commonNResponseAttributes;

    @OneToMany(mappedBy = "requestEntity", targetEntity = RequestEvaluatorEntity.class, cascade = CascadeType.ALL)
    private List<RequestEvaluatorEntity> requestEvaluators;

    @OneToMany(mappedBy = "requestEntity", targetEntity = RequestHistoryEntity.class, cascade = CascadeType.ALL)
    private List<RequestHistoryEntity> requestHistoryLogs;

    @OneToMany(mappedBy = "requestEntity", targetEntity = RequestWhoHasSeenEntity.class, cascade = CascadeType.ALL)
    private List<RequestWhoHasSeenEntity> requestWhoHasSeenLogs;

    @Column(name = "CREATED_DATE")
    private Timestamp createdDate;

    @Column(name = "CREATED_USER")
    private String createdUser;

    @Column(name = "MODIFIED_DATE")
    private Timestamp modifiedDate;

    @Column(name = "MODIFIED_USER")
    private String modifiedUser;

    public RequestEntity() {
    }

    public RequestEntity(String requestId, String requestName, QuoteEntity quote, String productCategoryCode, String productCategoryName, String templateCode, String templateName, String templateVersion, String workFlowType, String evaluatedMode, String outputDescription, String responseType, String configurationType, Character isReusable, String specialBidCategory, String USTaxCategory, Character dataBuildRequired, Character dataBuildCompleted, String expectedResponseTime, String state, String publicURL, String attachments, String billDescription, String detailedResponse, Character isAlertOnApprovalOnly, String alertEmailAddress, String alertMessage, Character isVPNDataRequired, Character isCOTCVisible, String comments, List<RequestSiteEntity> requestSites, List<RequestAttributeEntity> commonNResponseAttributes, List<RequestEvaluatorEntity> requestEvaluators, List<RequestHistoryEntity> requestHistoryLogs, List<RequestWhoHasSeenEntity> requestWhoHasSeenLogs, Timestamp createdDate, String createdUser, Timestamp modifiedDate, String modifiedUser) {
        this.requestId = requestId;
        this.requestName = requestName;
        this.quote = quote;
        this.productCategoryCode = productCategoryCode;
        this.productCategoryName = productCategoryName;
        this.templateCode = templateCode;
        this.templateName = templateName;
        this.templateVersion = templateVersion;
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
        this.alertEmailAddress = alertEmailAddress;
        this.alertMessage = alertMessage;
        this.isVPNDataRequired = isVPNDataRequired;
        this.isCOTCVisible = isCOTCVisible;
        this.comments = comments;
        this.requestSites = requestSites;
        this.commonNResponseAttributes = commonNResponseAttributes;
        this.requestEvaluators = requestEvaluators;
        this.requestHistoryLogs = requestHistoryLogs;
        this.requestWhoHasSeenLogs = requestWhoHasSeenLogs;
        this.createdDate = createdDate;
        this.createdUser = createdUser;
        this.modifiedDate = modifiedDate;
        this.modifiedUser = modifiedUser;
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

    public QuoteEntity getQuote() {
        return quote;
    }

    public void setQuote(QuoteEntity quote) {
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

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public String getTemplateVersion() {
        return templateVersion;
    }

    public void setTemplateVersion(String templateVersion) {
        this.templateVersion = templateVersion;
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

    public String getAlertEmailAddress() {
        return alertEmailAddress;
    }

    public void setAlertEmailAddress(String alertEmailAddress) {
        this.alertEmailAddress = alertEmailAddress;
    }

    public String getAlertMessage() {
        return alertMessage;
    }

    public void setAlertMessage(String alertMessage) {
        this.alertMessage = alertMessage;
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

    public List<RequestSiteEntity> getRequestSites() {
        return requestSites;
    }

    public void setRequestSites(List<RequestSiteEntity> requestSites) {
        this.requestSites = requestSites;
    }

    public List<RequestAttributeEntity> getCommonNResponseAttributes() {
        return commonNResponseAttributes;
    }

    public void setCommonNResponseAttributes(List<RequestAttributeEntity> commonNResponseAttributes) {
        this.commonNResponseAttributes = commonNResponseAttributes;
    }

    public List<RequestEvaluatorEntity> getRequestEvaluators() {
        return requestEvaluators;
    }

    public void setRequestEvaluators(List<RequestEvaluatorEntity> requestEvaluators) {
        this.requestEvaluators = requestEvaluators;
    }

    public List<RequestHistoryEntity> getRequestHistoryLogs() {
        return requestHistoryLogs;
    }

    public void setRequestHistoryLogs(List<RequestHistoryEntity> requestHistoryLogs) {
        this.requestHistoryLogs = requestHistoryLogs;
    }

    public List<RequestWhoHasSeenEntity> getRequestWhoHasSeenLogs() {
        return requestWhoHasSeenLogs;
    }

    public void setRequestWhoHasSeenLogs(List<RequestWhoHasSeenEntity> requestWhoHasSeenLogs) {
        this.requestWhoHasSeenLogs = requestWhoHasSeenLogs;
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

    public RequestDTO toDTO(RequestDTO dto){
        if(dto!=null){
            dto.setRequestId(this.getRequestId());
            dto.setRequestName(this.getRequestName());
            dto.setQuote(this.getQuote().toDTO(new QuoteDTO()));
            dto.setProductCategoryCode(this.getProductCategoryCode());
            dto.setProductCategoryName(this.getProductCategoryName());
            dto.setTemplateCode(this.getTemplateCode());
            dto.setTemplateName(this.getTemplateName());
            dto.setTemplateVersion(this.getTemplateVersion());
            dto.setWorkFlowType(this.getWorkFlowType());
            dto.setEvaluatedMode(this.getEvaluatedMode());
            dto.setOutputDescription(this.getOutputDescription());
            dto.setResponseType(this.getResponseType());
            dto.setConfigurationType(this.getConfigurationType());
            dto.setIsReusable(this.getIsReusable());
            dto.setSpecialBidCategory(this.getSpecialBidCategory());
            dto.setUSTaxCategory(this.getUSTaxCategory());
            dto.setDataBuildRequired(this.getDataBuildRequired());
            dto.setDataBuildCompleted(this.getDataBuildCompleted());
            dto.setExpectedResponseTime(this.getExpectedResponseTime());
            dto.setState(this.getState());
            dto.setPublicURL(this.getPublicURL());
            dto.setAttachments(this.getAttachments());
            dto.setBillDescription(this.getBillDescription());
            dto.setDetailedResponse(this.getDetailedResponse());
            dto.setIsAlertOnApprovalOnly(this.getIsAlertOnApprovalOnly());
            dto.setAlertEmailAddress(this.getAlertEmailAddress());
            dto.setAlertMessage(this.getAlertMessage());
            dto.setIsVPNDataRequired(this.getIsVPNDataRequired());
            dto.setIsCOTCVisible(this.getIsCOTCVisible());
            dto.setComments(this.getComments());
            dto.setCreatedUser(this.getCreatedUser());
            dto.setCreatedDate(this.getCreatedDate());
            dto.setModifiedDate(this.getModifiedDate());
            dto.setModifiedUser(this.getModifiedUser());

            if((isNull(this.getRequestSites())) || (this.getRequestSites().size() == 0)){
                dto.setRequestSites(new ArrayList<RequestSiteDTO>());
            }else{
                if(isNull(dto.getRequestSites())){
                    dto.setRequestSites(new ArrayList<RequestSiteDTO>());
                }
                for(RequestSiteEntity requestSiteEntity : this.getRequestSites()){
                    dto.getRequestSites().add(requestSiteEntity.toDTO(new RequestSiteDTO()));
                }
            }

            if((isNull(this.getCommonNResponseAttributes())) || (this.getCommonNResponseAttributes().size() == 0)){
                dto.setCommonNResponseAttributes(new ArrayList<RequestAttributeDTO>());
            }else{
                if(isNull(dto.getCommonNResponseAttributes())){
                    dto.setCommonNResponseAttributes(new ArrayList<RequestAttributeDTO>());
                }
                for(RequestAttributeEntity requestAttributeEntity:this.getCommonNResponseAttributes()){
                    dto.getCommonNResponseAttributes().add(requestAttributeEntity.toDTO(new RequestAttributeDTO()));
                }
            }

            if((isNull(this.getRequestEvaluators())) || (this.getRequestEvaluators().size() == 0)){
                dto.setRequestEvaluators(new ArrayList<RequestEvaluatorDTO>());
            }else{
                if(isNull(dto.getRequestEvaluators())){
                    dto.setRequestEvaluators(new ArrayList<RequestEvaluatorDTO>());
                }
                for(RequestEvaluatorEntity requestGroup:this.getRequestEvaluators()){
                    dto.getRequestEvaluators().add(requestGroup.toDTO(new RequestEvaluatorDTO()));
                }
            }

            if((isNull(this.getRequestHistoryLogs())) || (this.getRequestHistoryLogs().size() == 0)){
                dto.setRequestHistoryLogs(new ArrayList<RequestHistoryDTO>());
            }else{
                if(isNull(dto.getRequestHistoryLogs())){
                    dto.setRequestHistoryLogs(new ArrayList<RequestHistoryDTO>());
                }
                for(RequestHistoryEntity requestHistoryLog:this.getRequestHistoryLogs()){
                    dto.getRequestHistoryLogs().add(requestHistoryLog.toDTO(new RequestHistoryDTO()));
                }
            }

            if((isNull(this.getRequestWhoHasSeenLogs())) || (this.getRequestWhoHasSeenLogs().size() == 0)){
                dto.setRequestWhoHasSeenLogs(new ArrayList<RequestWhoHasSeenDTO>());
            }else{
                if(isNull(dto.getRequestWhoHasSeenLogs())){
                    dto.setRequestWhoHasSeenLogs(new ArrayList<RequestWhoHasSeenDTO>());
                }
                for(RequestWhoHasSeenEntity requestWhoHasSeenEntity : this.getRequestWhoHasSeenLogs()){
                    dto.getRequestWhoHasSeenLogs().add(requestWhoHasSeenEntity.toDTO(new RequestWhoHasSeenDTO()));
                }
            }
        }
        return dto;
    }

    public RequestDTO toNewDTO(){
        return toDTO(new RequestDTO());
    }

}

