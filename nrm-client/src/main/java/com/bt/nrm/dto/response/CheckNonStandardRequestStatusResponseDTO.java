package com.bt.nrm.dto.response;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * Created by 608143048 on 21/01/2016.
 *
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class CheckNonStandardRequestStatusResponseDTO {

    private String responseStatus;
    private String errorCode;
    private String errorDescription;
    private String quoteId;
    private String quoteOptionId;
    private String requestId;
    private String requestStatus;
    private String controllerName;
    private String dataBuildRequired;
    private String dataBuildCompleted;
    private String dataBuildCompletionDate;
    private String comments;
    private NonStandardRequestPriceGroupDTO COTCPriceGroups;
    private List<NonStandardRequestSiteDTO> sites;

    public CheckNonStandardRequestStatusResponseDTO() {
    }

    public CheckNonStandardRequestStatusResponseDTO(String responseStatus, String errorCode, String errorDescription, String quoteId, String quoteOptionId, String requestId, String requestStatus, String controllerName, String dataBuildRequired, String dataBuildCompleted, String dataBuildCompletionDate, String comments, NonStandardRequestPriceGroupDTO COTCPriceGroups, List<NonStandardRequestSiteDTO> sites) {
        this.responseStatus = responseStatus;
        this.errorCode = errorCode;
        this.errorDescription = errorDescription;
        this.quoteId = quoteId;
        this.quoteOptionId = quoteOptionId;
        this.requestId = requestId;
        this.requestStatus = requestStatus;
        this.controllerName = controllerName;
        this.dataBuildRequired = dataBuildRequired;
        this.dataBuildCompleted = dataBuildCompleted;
        this.dataBuildCompletionDate = dataBuildCompletionDate;
        this.comments = comments;
        this.COTCPriceGroups = COTCPriceGroups;
        this.sites = sites;
    }

    public String getResponseStatus() {
        return responseStatus;
    }

    public void setResponseStatus(String responseStatus) {
        this.responseStatus = responseStatus;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorDescription() {
        return errorDescription;
    }

    public void setErrorDescription(String errorDescription) {
        this.errorDescription = errorDescription;
    }

    public String getQuoteId() {
        return quoteId;
    }

    public void setQuoteId(String quoteId) {
        this.quoteId = quoteId;
    }

    public String getQuoteOptionId() {
        return quoteOptionId;
    }

    public void setQuoteOptionId(String quoteOptionId) {
        this.quoteOptionId = quoteOptionId;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getRequestStatus() {
        return requestStatus;
    }

    public void setRequestStatus(String requestStatus) {
        this.requestStatus = requestStatus;
    }

    public String getControllerName() {
        return controllerName;
    }

    public void setControllerName(String controllerName) {
        this.controllerName = controllerName;
    }

    public String getDataBuildRequired() {
        return dataBuildRequired;
    }

    public void setDataBuildRequired(String dataBuildRequired) {
        this.dataBuildRequired = dataBuildRequired;
    }

    public String getDataBuildCompleted() {
        return dataBuildCompleted;
    }

    public void setDataBuildCompleted(String dataBuildCompleted) {
        this.dataBuildCompleted = dataBuildCompleted;
    }

    public String getDataBuildCompletionDate() {
        return dataBuildCompletionDate;
    }

    public void setDataBuildCompletionDate(String dataBuildCompletionDate) {
        this.dataBuildCompletionDate = dataBuildCompletionDate;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public NonStandardRequestPriceGroupDTO getCOTCPriceGroups() {
        return COTCPriceGroups;
    }

    public void setCOTCPriceGroups(NonStandardRequestPriceGroupDTO COTCPriceGroups) {
        this.COTCPriceGroups = COTCPriceGroups;
    }

    public List<NonStandardRequestSiteDTO> getSites() {
        return sites;
    }

    public void setSites(List<NonStandardRequestSiteDTO> sites) {
        this.sites = sites;
    }
}
