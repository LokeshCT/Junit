package com.bt.nrm.dto;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class EvaluatorActionsDTO {

    private String requestId;
    private String requestName;
    private String quoteId;
    private String quoteName;
    private String quoteOptionId;
    private String quoteOptionName;
    private String productCategoryName;
    private String templateName;
    private String customerId;
    private String customerName;
    private String salesChannelName;
    private String requestState;

    private String createdDate;
    private String acceptedDate;
    private String acceptedBy;
    private String groupName;
    private String requestEvaluatorId;

    //From - Sales Channel Name
    //Created Date
    //Accepted Date
    //Agent-Accepted_By
    //EVALUATOR_GROUP_NAME


    public EvaluatorActionsDTO(String requestId, String requestName, String quoteId, String quoteName, String quoteOptionId, String quoteOptionName, String productCategoryName, String templateName, String customerId, String customerName, String requestState) {
        this.requestId = requestId;
        this.requestName = requestName;
        this.quoteId = quoteId;
        this.quoteName = quoteName;
        this.quoteOptionId = quoteOptionId;
        this.quoteOptionName = productCategoryName;
        this.productCategoryName = productCategoryName;
        this.templateName = templateName;
        this.customerId = customerId;
        this.customerName = customerName;
        this.requestState = requestState;
    }

    public EvaluatorActionsDTO(String requestId, String requestName, String quoteId, String quoteName, String productCategoryName, String templateName, String customerName, String requestState, String salesChannelName, String createdDate, String acceptedDate, String acceptedBy, String groupName, String requestEvaluatorId) {
        this.requestId = requestId;
        this.requestName = requestName;
        this.quoteId = quoteId;
        this.quoteName = quoteName;
        this.productCategoryName = productCategoryName;
        this.templateName = templateName;
        this.customerName = customerName;
        this.requestState = requestState;
        this.salesChannelName = salesChannelName;
        this.createdDate = createdDate;
        this.acceptedDate = acceptedDate;
        this.acceptedBy = acceptedBy;
        this.groupName = groupName;
        this.requestEvaluatorId = requestEvaluatorId;
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

    public String getQuoteId() {
        return quoteId;
    }

    public void setQuoteId(String quoteId) {
        this.quoteId = quoteId;
    }

    public String getQuoteName() {
        return quoteName;
    }

    public void setQuoteName(String quoteName) {
        this.quoteName = quoteName;
    }

    public String getQuoteOptionId() {
        return quoteOptionId;
    }

    public void setQuoteOptionId(String quoteOptionId) {
        this.quoteOptionId = quoteOptionId;
    }

    public String getQuoteOptionName() {
        return quoteOptionName;
    }

    public void setQuoteOptionName(String quoteOptionName) {
        this.quoteOptionName = quoteOptionName;
    }

    public String getProductCategoryName() {
        return productCategoryName;
    }

    public void setProductCategoryName(String productCategoryName) {
        this.productCategoryName = productCategoryName;
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getRequestState() {
        return requestState;
    }

    public void setRequestState(String requestState) {
        this.requestState = requestState;
    }

    public String getSalesChannelName() {
        return salesChannelName;
    }

    public void setSalesChannelName(String salesChannelName) {
        this.salesChannelName = salesChannelName;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

    public String getAcceptedDate() {
        return acceptedDate;
    }

    public void setAcceptedDate(String acceptedDate) {
        this.acceptedDate = acceptedDate;
    }

    public String getAcceptedBy() {
        return acceptedBy;
    }

    public void setAcceptedBy(String acceptedBy) {
        this.acceptedBy = acceptedBy;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getRequestEvaluatorId() {
        return requestEvaluatorId;
    }

    public void setRequestEvaluatorId(String requestEvaluatorId) {
        this.requestEvaluatorId = requestEvaluatorId;
    }
}
