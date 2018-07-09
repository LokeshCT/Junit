package com.bt.rsqe.ape.repository.entities;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.bt.rsqe.utils.AssertObject.*;

@Entity
@Table(name = "SUPPLIER_CHECK_CLIENT_REQUEST")
public class SupplierCheckClientRequestEntity {

    @Id
    @Column(name = "SCCR_ID")
    private String id;

    @Column(name = "CALLBACK_URI")
    private String callbackUri;

    @Column(name = "TRIGGER_TYPE")
    private String triggerType;

    @Column(name = "AUTO_TRIGGER")
    private String autoTrigger;

    @Column(name = "SOURCE_SYSTEM_NAME")
    private String sourceSystemName;

    @Column(name = "REQUESTED_BY")
    private String user;

    @Column(name = "CUSTOMER_ID")
    private Long customerId;

    @Column(name = "STATUS")
    private String status;

    @Column(name = "CREATED_ON")
    Date createdOn;

    @Column(name = "UPDATED_ON")
    Date updatedOn;

    @OneToMany(mappedBy = "supplierCheckClientRequestEntity", cascade = CascadeType.ALL,fetch = FetchType.EAGER)
    private List<SupplierCheckApeRequestEntity> supplierCheckApeRequestEntities;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "supplierCheckClientRequestEntity", fetch = FetchType.EAGER)
    private List<SupplierRequestSiteEntity> supplierRequestSiteEntities;

    public SupplierCheckClientRequestEntity() {
    }

    public SupplierCheckClientRequestEntity(String id) {
        this.id = id;
    }

    public SupplierCheckClientRequestEntity(String id, String callbackUri, String triggerType, String autoTrigger, String sourceSystemName, String user, Long customerId, String status, Date createdOn, Date updatedOn) {
        this.id = id;
        this.callbackUri = callbackUri;
        this.triggerType = triggerType;
        this.autoTrigger = autoTrigger;
        this.sourceSystemName = sourceSystemName;
        this.user = user;
        this.customerId = customerId;
        this.status = status;
        this.createdOn = createdOn;
        this.updatedOn = updatedOn;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCallbackUri() {
        return callbackUri;
    }

    public void setCallbackUri(String callbackUri) {
        this.callbackUri = callbackUri;
    }

    public String getTriggerType() {
        return triggerType;
    }

    public void setTriggerType(String triggerType) {
        this.triggerType = triggerType;
    }

    public String getAutoTrigger() {
        return autoTrigger;
    }

    public void setAutoTrigger(String autoTrigger) {
        this.autoTrigger = autoTrigger;
    }

    public String getSourceSystemName() {
        return sourceSystemName;
    }

    public void setSourceSystemName(String sourceSystemName) {
        this.sourceSystemName = sourceSystemName;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
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

    public List<SupplierCheckApeRequestEntity> getSupplierCheckApeRequestEntities() {    //TODO: this would force to refactor the client codes, not to make null checks
        return isNull(supplierCheckApeRequestEntities) ? new ArrayList<SupplierCheckApeRequestEntity>() : supplierCheckApeRequestEntities;
    }

    public void setSupplierCheckApeRequestEntities(List<SupplierCheckApeRequestEntity> supplierCheckApeRequestEntities) {
        this.supplierCheckApeRequestEntities = supplierCheckApeRequestEntities;
    }

    public List<SupplierRequestSiteEntity> getSupplierRequestSiteEntities() {
        return supplierRequestSiteEntities;
    }

    public void setSupplierRequestSiteEntities(List<SupplierRequestSiteEntity> supplierRequestSiteEntities) {
        this.supplierRequestSiteEntities = supplierRequestSiteEntities;
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
