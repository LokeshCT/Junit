package com.bt.rsqe.ape.repository.entities;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.Date;

@Entity
@Table(name = "SUPPLIER_CHECK_APE_REQUEST")
public class SupplierCheckApeRequestEntity {

    @Id
    @Column(name="SCAR_ID")
    private String id;

    @Column(name = "OPERATION_NAME")
    private String operationName;

    @Column(name = "AVAIL_CHECK_TYPE")
    private String availCheckType;

    @Column(name = "STATUS")
    private String status;

    @Column(name = "CREATED_ON")
    Date createdOn;

    @Column(name = "UPDATED_ON")
    Date updatedOn;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name="SCCR_ID")
    private SupplierCheckClientRequestEntity supplierCheckClientRequestEntity;

    public SupplierCheckApeRequestEntity() {
    }

    public SupplierCheckApeRequestEntity(String id, String operationName, String availCheckType, String status, Date createdOn, Date updatedOn, SupplierCheckClientRequestEntity supplierCheckClientRequestEntity) {
        this.id = id;
        this.operationName = operationName;
        this.availCheckType = availCheckType;
        this.status = status;
        this.createdOn = createdOn;
        this.updatedOn = updatedOn;
        this.supplierCheckClientRequestEntity = supplierCheckClientRequestEntity;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOperationName() {
        return operationName;
    }

    public void setOperationName(String operationName) {
        this.operationName = operationName;
    }

    public String getAvailCheckType() {
        return availCheckType;
    }

    public void setAvailCheckType(String availCheckType) {
        this.availCheckType = availCheckType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(Date createdOn) {
        this.createdOn = createdOn;
    }

    public Date getUpdatedOn() {
        return updatedOn;
    }

    public void setUpdatedOn(Date updatedOn) {
        this.updatedOn = updatedOn;
    }

    public SupplierCheckClientRequestEntity getSupplierCheckClientRequestEntity() {
        return supplierCheckClientRequestEntity;
    }

    public void setSupplierCheckClientRequestEntity(SupplierCheckClientRequestEntity supplierCheckClientRequestEntity) {
        this.supplierCheckClientRequestEntity = supplierCheckClientRequestEntity;
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
