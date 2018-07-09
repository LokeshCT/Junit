package com.bt.nrm.dto.request;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by 608143048 on 10/12/2015.
 * This DTO will be used as an output parameter of the interfaces(SQE/rSQE) method to create non-standard request.
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class NonStandardResponseDTO {

    private String responseStatus;
    private String errorCode;
    private String errorDescription;
    private String message;
    private String requestStatus;
    private String quoteId;
    private String quoteOptionId;
    private String requestId;
    private String requestURL;

    public NonStandardResponseDTO() {
    }

    public NonStandardResponseDTO(String responseStatus, String errorCode, String errorDescription, String message, String requestStatus, String quoteId, String quoteOptionId, String requestId, String requestURL) {
        this.responseStatus = responseStatus;
        this.errorCode = errorCode;
        this.errorDescription = errorDescription;
        this.message = message;
        this.requestStatus = requestStatus;
        this.quoteId = quoteId;
        this.quoteOptionId = quoteOptionId;
        this.requestId = requestId;
        this.requestURL = requestURL;
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

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getRequestStatus() {
        return requestStatus;
    }

    public void setRequestStatus(String requestStatus) {
        this.requestStatus = requestStatus;
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

    public String getRequestURL() {
        return requestURL;
    }

    public void setRequestURL(String requestURL) {
        this.requestURL = requestURL;
    }
}
