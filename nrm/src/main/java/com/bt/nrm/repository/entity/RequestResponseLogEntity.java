package com.bt.nrm.repository.entity;


import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import java.sql.Timestamp;

@Entity
@Table(name = "REQUEST_RESPONSE_LOGS")
public class RequestResponseLogEntity {

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    @Column(name = "LOG_ID")
    private String logId;

    @Column(name = "REQUEST_ID")
    private Long requestId;

    @Column(name = "REQUEST_NAME")
    private String requestName;

    @Column(name = "QUOTE_OPTION_ID")
    private String quoteOptionId;

    @Column(name = "QUOTE_OPTION_NAME")
    private String quoteOptionName;

    @Column(name = "CUSTOMER_ID")
    private String customerId;

    @Column(name = "CUSTOMER_NAME")
    private String customerName;

    @Column(name = "REQUEST")
    @Lob
    private String request;

    @Column(name = "RESPONSE")
    @Lob
    private String response;

    @Column(name = "ORIGINATING_SYSTEM")
    private String originatingSystem;

    @Column(name = "SERVICE_CALL")
    private String serviceCall;

    @Column(name = "CREATED_USER")
    private String createdUser;

    @Column(name = "CREATED_DATE")
    private Timestamp createdDate;

    public RequestResponseLogEntity() {
    }

    public RequestResponseLogEntity(String logId, Long requestId, String requestName, String quoteOptionId, String quoteOptionName, String customerId,
                                    String customerName, String request, String response, String originatingSystem, String serviceCall, String createdUser, Timestamp createdDate) {
        this.logId = logId;
        this.requestId = requestId;
        this.requestName = requestName;
        this.quoteOptionId = quoteOptionId;
        this.quoteOptionName = quoteOptionName;
        this.customerId = customerId;
        this.customerName = customerName;
        this.request = request;
        this.response = response;
        this.originatingSystem = originatingSystem;
        this.serviceCall = serviceCall;
        this.createdUser = createdUser;
        this.createdDate = createdDate;
    }

    public String getLogId() {
        return logId;
    }

    public void setLogId(String logId) {
        this.logId = logId;
    }

    public Long getRequestId() {
        return requestId;
    }

    public void setRequestId(Long requestId) {
        this.requestId = requestId;
    }

    public String getRequestName() {
        return requestName;
    }

    public void setRequestName(String requestName) {
        this.requestName = requestName;
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

    public String getRequest() {
        return request;
    }

    public void setRequest(String request) {
        this.request = request;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public String getOriginatingSystem() {
        return originatingSystem;
    }

    public void setOriginatingSystem(String originatingSystem) {
        this.originatingSystem = originatingSystem;
    }

    public String getServiceCall() {
        return serviceCall;
    }

    public void setServiceCall(String serviceCall) {
        this.serviceCall = serviceCall;
    }

    public String getCreatedUser() {
        return createdUser;
    }

    public void setCreatedUser(String createdUser) {
        this.createdUser = createdUser;
    }

    public Timestamp getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Timestamp createdDate) {
        this.createdDate = createdDate;
    }
}
