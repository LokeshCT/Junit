package com.bt.rsqe.ape.repository.entities;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import java.util.Date;

@Entity
@Table(name = "SUPPLIER_REQUEST_SITE_SPAC")
public class SupplierRequestSiteSpacEntity {

    @Id
    @SequenceGenerator(name = "SRSS_ID", sequenceName = "SRSS_ID", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SRSS_ID")
    @Column(name="SRSS_ID")
    private Long id;

    @Column(name = "SPAC_ID")
    private String spacId;

    @Column(name = "STATUS")
    private String status;

    @Column(name = "SUB_STATUS")
    private String subStatus;

    @Column(name = "CREATED_ON")
    Date createdOn;

    @Column(name = "UPDATED_ON")
    Date updatedOn;

    @ManyToOne
    @JoinColumn(name="SRS_ID")
    private SupplierRequestSiteEntity supplierRequestSiteEntity;

    public SupplierRequestSiteSpacEntity() {
    }

    public SupplierRequestSiteSpacEntity(String spacId, String status, String subStatus, Date createdOn, Date updatedOn, SupplierRequestSiteEntity supplierRequestSiteEntity) {
        this.spacId = spacId;
        this.status = status;
        this.subStatus = subStatus;
        this.createdOn = createdOn;
        this.updatedOn = updatedOn;
        this.supplierRequestSiteEntity = supplierRequestSiteEntity;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSubStatus() {
        return subStatus;
    }

    public void setSubStatus(String subStatus) {
        this.subStatus = subStatus;
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

    public String getSpacId() {
        return spacId;
    }

    public void setSpacId(String spacId) {
        this.spacId = spacId;
    }

    public SupplierRequestSiteEntity getSupplierRequestSiteEntity() {
        return supplierRequestSiteEntity;
    }

    public void setSupplierRequestSiteEntity(SupplierRequestSiteEntity supplierRequestSiteEntity) {
        this.supplierRequestSiteEntity = supplierRequestSiteEntity;
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
