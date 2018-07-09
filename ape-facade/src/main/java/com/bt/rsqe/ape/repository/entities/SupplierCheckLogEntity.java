package com.bt.rsqe.ape.repository.entities;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import java.util.Date;

@Entity
@Table(name = "SUPPLIER_CHECK_LOGS")
public class SupplierCheckLogEntity {

    @Id
    @SequenceGenerator(name = "SUPPLIER_CHECK_LOG_ID", sequenceName = "SUPPLIER_CHECK_LOG_ID", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SUPPLIER_CHECK_LOG_ID")
    @Column(name="SUPPLIER_CHECK_LOG_ID")
    private Long id;

    @Column(name = "REQUEST_ID")
    private String requestId;

    @Column(name = "LOG_TYPE")
    private String logType;

    @Column(name = "REQUEST_NAME")
    private String requestName;

    @Column(name = "PAYLOAD")
    private String payload;

    @Column(name = "CREATED_BY")
    private String createdBy;

    @Column(name = "CREATED_ON")
    private Date createdOn;

    @Column(name = "ERROR_DESCRIPTION")
    private String errorDescription;

    public SupplierCheckLogEntity() {
    }

    public SupplierCheckLogEntity(String requestId, String logType, String requestName, String payload, String createdBy, Date createdOn, String errorDescription) {
        this.requestId = requestId;
        this.logType = logType;
        this.requestName = requestName;
        this.payload = payload;
        this.createdBy = createdBy;
        this.createdOn = createdOn;
        this.errorDescription = errorDescription;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getLogType() {
        return logType;
    }

    public void setLogType(String logType) {
        this.logType = logType;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Date getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(Date createdOn) {
        this.createdOn = createdOn;
    }

    public String getErrorDescription() {
        return errorDescription;
    }

    public void setErrorDescription(String errorDescription) {
        this.errorDescription = errorDescription;
    }

    public String getRequestName() {
        return requestName;
    }

    public void setRequestName(String requestName) {
        this.requestName = requestName;
    }

    @Override
    public boolean equals(Object o) {
        return EqualsBuilder.reflectionEquals(this, o);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
