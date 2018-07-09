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
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "SUPPLIER_REQUEST_SITE")
public class SupplierRequestSiteEntity {
    @Id
    @SequenceGenerator(name = "SRS_ID", sequenceName = "SRS_ID", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SRS_ID")
    @Column(name = "SRS_ID")
    private Long id;

    @Column(name = "SITE_ID")
    private String siteId;

    @Column(name = "STATUS")
    private String status;

    @Column(name = "SUB_STATUS")
    private String subStatus;

    @Column(name = "CREATED_ON")
    Date createdOn;

    @Column(name = "UPDATED_ON")
    Date updatedOn;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "supplierRequestSiteEntity")
    private List<SupplierRequestSiteSpacEntity> supplierRequestSiteSpacEntities;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "SCCR_ID", referencedColumnName = "SCCR_ID", insertable = true, updatable = true)
    private SupplierCheckClientRequestEntity supplierCheckClientRequestEntity;

    public SupplierRequestSiteEntity() {
    }

    public SupplierRequestSiteEntity(String siteId, String status, String subStatus, Date createdOn, Date updatedOn) {
        this.siteId = siteId;
        this.status = status;
        this.subStatus = subStatus;
        this.createdOn = createdOn;
        this.updatedOn = updatedOn;
    }

    public SupplierRequestSiteEntity(String siteId, String status, String subStatus, Date createdOn, Date updatedOn, SupplierCheckClientRequestEntity supplierCheckClientRequestEntity) {
        this.siteId = siteId;
        this.status = status;
        this.subStatus = subStatus;
        this.createdOn = createdOn;
        this.updatedOn = updatedOn;
        this.supplierCheckClientRequestEntity = supplierCheckClientRequestEntity;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSiteId() {
        return siteId;
    }

    public void setSiteId(String siteId) {
        this.siteId = siteId;
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

    public SupplierCheckClientRequestEntity getSupplierCheckClientRequestEntity() {
        return supplierCheckClientRequestEntity;
    }

    public void setSupplierCheckClientRequestEntity(SupplierCheckClientRequestEntity supplierCheckClientRequestEntity) {
        this.supplierCheckClientRequestEntity = supplierCheckClientRequestEntity;
    }

    public List<SupplierRequestSiteSpacEntity> getSupplierRequestSiteSpacEntities() {
        return supplierRequestSiteSpacEntities;
    }

    public void setSupplierRequestSiteSpacEntities(List<SupplierRequestSiteSpacEntity> supplierRequestSiteSpacEntities) {
        this.supplierRequestSiteSpacEntities = supplierRequestSiteSpacEntities;
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
