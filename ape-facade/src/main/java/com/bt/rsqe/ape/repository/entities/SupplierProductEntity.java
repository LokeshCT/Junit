package com.bt.rsqe.ape.repository.entities;

import com.bt.rsqe.ape.dto.AvailabilitySet;
import com.bt.rsqe.ape.dto.SupplierProduct;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
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

import static com.google.common.collect.Lists.*;

@Entity
@Table(name = "SUPPLIER_PRODUCT")
public class SupplierProductEntity {

    @Id
    @SequenceGenerator(name = "SUPP_PROD_ID", sequenceName = "SUPP_PROD_ID", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SUPP_PROD_ID")
    @Column(name = "SUPP_PROD_ID")
    private Long suppProdId;

    @Column(name = "SUPPLIER_ID")
    private Long supplierId;

    @Column(name = "SUPPLIER_NAME")
    private String supplierName;

    @Column(name = "SPAC_ID")
    private String spacId;

    @Column(name = "SUPPLIER_PRODUCT_ID")
    private Long supplierProductId;

    @Column(name = "SUPPLIER_PRODUCT_NAME")
    private String supplierProductName;

    @Column(name = "DISPLAY_SUPPLIER_PRODUCT_NAME")
    private String displaySupplierProductName;

    @Column(name = "PRODUCT_AVAILABILITY_CODE")
    private String productAvailabilityCode;

    @Column(name = "AVAILABILITY_CHECK_TYPE")
    private String availabilityCheckType;

    @Column(name = "CUSTOMER_LOCATION_TYPE")
    private String customerLocationType;

    @Column(name = "SUPPLIER_PRODUCT_STATUS")
    private String supplierProductStatus;

    @Column(name = "ACCESS_TYPE")
    private String accessType;

    @Column(name = "PARENT_ACCESS_TYPE")
    private String parentAccessType;

    @Column(name = "SERVICE_VARIANT")
    private String serviceVariant;

    @Column(name = "CONTENTION_RATIO")
    private String contentionRatio;

    @Column(name = "DELIVERY_MODE")
    private String deliveryMode;

    @Column(name = "CPE_ACCESS_TYPE")
    private String cpeAccessType;

    @Column(name = "CENTRALIZED_AVAIL_SUPPORT")
    private String centralizedAvailabilitySupported;

    @Column(name = "COMMON_ACCESS_CPE_SUPPLIER")
    private String commonAccessCPESupplier;

    @Column(name = "ACCESS_SPEED")
    private String accessSpeed;

    @Column(name = "ACCESS_UOM")
    private String accessUom;

    @Column(name = "PRODUCT_AVAILABLE")
    private String productAvailable;

    @Column(name = "AVAILABILITY_DESCRIPTION")
    private String availabilityDescription;

    @Column(name = "MAX_DOWNSTREAM_BANDWIDTH")
    private String maxDownstreamBandwidth;

    @Column(name = "SYMMETRIC_BANDWIDTH")
    private String symmetricSpeedBandwidth;

    @Column(name = "MAX_UPSTREAM_BANDWIDTH")
    private String maxUpstreamSpeedBandwidth;

    @Column(name = "NUMBER_OF_COPPER_PAIRS")
    private Long numberOfCopperPairs;

    @Column(name = "EXCHANGE_CODE")
    private String exchangeCode;

    @Column(name = "SUPPLIER_TARIFF_ZONE")
    private String supplierTariffZone;

    @Column(name = "CHECK_REFERENCE")
    private String checkedReference;

    @Column(name = "DESCRIPTION")
    private String description;

    @Column(name = "STATUS")
    private String status;

    @Column(name = "ACTIVE")
    private String active;

    @Column(name = "REQUESTED_TIME")
    Date requestedTime;

    @Column(name = "REQUEST_TIMEOUT")
    Date requestTimeout;

    @Column(name = "INTERFACE_ID")
    private String interfaceId;

    @Column(name = "INTERFACE_NAME")
    private String interfaceName;

    @Column(name = "FRAMING_ID")
    private String framingId;

    @Column(name = "FRAMING_NAME")
    private String framingName;

    @Column(name = "CONNECTOR_ID")
    private String connectorId;

    @Column(name = "CONNECTOR_NAME")
    private String connectorName;

    @Column(name = "MANDATORY_ATTRIBUTES")
    private String mandatroyAttributes;

    @Column(name = "RETRY_COUNT")
    private Integer retryCount;

    @OneToMany(mappedBy = "supplierProductEntity", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<AvailabilitySetEntity> setEntityList;

    @ManyToOne
    @JoinColumn(name = "SITE_ID")
    private SupplierSiteEntity supplierSiteEntity;


    public SupplierProductEntity() {
    }

    public SupplierProductEntity(Long suppProdId, Long supplierId, String supplierName, String spacId, Long supplierProductId, String supplierProductName, String displaySupplierProductName, String productAvailabilityCode, String availabilityCheckType, String customerLocationType, String supplierProductStatus, String accessType, String parentAccessType, String serviceVariant, String contentionRatio, String deliveryMode, String cpeAccessType, String centralizedAvailabilitySupported, String commonAccessCPESupplier, String accessSpeed, String accessUom, String productAvailable, String availabilityDescription, String maxDownstreamBandwidth, String symmetricSpeedBandwidth, String maxUpstreamSpeedBandwidth, Long numberOfCopperPairs, String exchangeCode, String supplierTariffZone, String checkedReference, String description, String status, Date requestedTime, Date requestTimeout, String interfaceId, String interfaceName, String framingId, String framingName, String connectorId, String connectorName, String mandatoryAttributes, List<AvailabilitySetEntity> setEntityList, SupplierSiteEntity supplierSiteEntity, String active, Integer retryCount) {
        this.suppProdId = suppProdId;
        this.supplierId = supplierId;
        this.supplierName = supplierName;
        this.spacId = spacId;
        this.supplierProductId = supplierProductId;
        this.supplierProductName = supplierProductName;
        this.displaySupplierProductName = displaySupplierProductName;
        this.productAvailabilityCode = productAvailabilityCode;
        this.availabilityCheckType = availabilityCheckType;
        this.customerLocationType = customerLocationType;
        this.supplierProductStatus = supplierProductStatus;
        this.accessType = accessType;
        this.parentAccessType = parentAccessType;
        this.serviceVariant = serviceVariant;
        this.contentionRatio = contentionRatio;
        this.deliveryMode = deliveryMode;
        this.cpeAccessType = cpeAccessType;
        this.centralizedAvailabilitySupported = centralizedAvailabilitySupported;
        this.commonAccessCPESupplier = commonAccessCPESupplier;
        this.accessSpeed = accessSpeed;
        this.accessUom = accessUom;
        this.productAvailable = productAvailable;
        this.availabilityDescription = availabilityDescription;
        this.maxDownstreamBandwidth = maxDownstreamBandwidth;
        this.symmetricSpeedBandwidth = symmetricSpeedBandwidth;
        this.maxUpstreamSpeedBandwidth = maxUpstreamSpeedBandwidth;
        this.numberOfCopperPairs = numberOfCopperPairs;
        this.exchangeCode = exchangeCode;
        this.supplierTariffZone = supplierTariffZone;
        this.checkedReference = checkedReference;
        this.description = description;
        this.status = status;
        this.requestedTime = requestedTime;
        this.requestTimeout = requestTimeout;
        this.interfaceId = interfaceId;
        this.interfaceName = interfaceName;
        this.framingId = framingId;
        this.framingName = framingName;
        this.connectorId = connectorId;
        this.connectorName = connectorName;
        this.mandatroyAttributes = mandatoryAttributes;
        this.active = active;
        this.setEntityList = setEntityList;
        this.supplierSiteEntity = supplierSiteEntity;
    }

    public Long getSuppProdId() {
        return suppProdId;
    }

    public void setSuppProdId(Long suppProdId) {
        this.suppProdId = suppProdId;
    }

    public Long getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(Long supplierId) {
        this.supplierId = supplierId;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }

    public String getSpacId() {
        return spacId;
    }

    public void setSpacId(String spacId) {
        this.spacId = spacId;
    }

    public Long getSupplierProductId() {
        return supplierProductId;
    }

    public void setSupplierProductId(Long supplierProductId) {
        this.supplierProductId = supplierProductId;
    }

    public String getSupplierProductName() {
        return supplierProductName;
    }

    public void setSupplierProductName(String supplierProductName) {
        this.supplierProductName = supplierProductName;
    }

    public String getDisplaySupplierProductName() {
        return displaySupplierProductName;
    }

    public void setDisplaySupplierProductName(String displaySupplierProductName) {
        this.displaySupplierProductName = displaySupplierProductName;
    }

    public String getProductAvailabilityCode() {
        return productAvailabilityCode;
    }

    public void setProductAvailabilityCode(String productAvailabilityCode) {
        this.productAvailabilityCode = productAvailabilityCode;
    }

    public String getAvailabilityCheckType() {
        return availabilityCheckType;
    }

    public void setAvailabilityCheckType(String availabilityCheckType) {
        this.availabilityCheckType = availabilityCheckType;
    }

    public String getCustomerLocationType() {
        return customerLocationType;
    }

    public void setCustomerLocationType(String customerLocationType) {
        this.customerLocationType = customerLocationType;
    }

    public String getSupplierProductStatus() {
        return supplierProductStatus;
    }

    public void setSupplierProductStatus(String supplierProductStatus) {
        this.supplierProductStatus = supplierProductStatus;
    }

    public String getAccessType() {
        return accessType;
    }

    public void setAccessType(String accessType) {
        this.accessType = accessType;
    }

    public String getParentAccessType() {
        return parentAccessType;
    }

    public void setParentAccessType(String parentAccessType) {
        this.parentAccessType = parentAccessType;
    }

    public String getServiceVariant() {
        return serviceVariant;
    }

    public void setServiceVariant(String serviceVariant) {
        this.serviceVariant = serviceVariant;
    }

    public String getContentionRatio() {
        return contentionRatio;
    }

    public void setContentionRatio(String contentionRatio) {
        this.contentionRatio = contentionRatio;
    }

    public String getDeliveryMode() {
        return deliveryMode;
    }

    public void setDeliveryMode(String deliveryMode) {
        this.deliveryMode = deliveryMode;
    }

    public String getCpeAccessType() {
        return cpeAccessType;
    }

    public void setCpeAccessType(String cpeAccessType) {
        this.cpeAccessType = cpeAccessType;
    }

    public String getCentralizedAvailabilitySupported() {
        return centralizedAvailabilitySupported;
    }

    public void setCentralizedAvailabilitySupported(String centralizedAvailabilitySupported) {
        this.centralizedAvailabilitySupported = centralizedAvailabilitySupported;
    }

    public String getCommonAccessCPESupplier() {
        return commonAccessCPESupplier;
    }

    public void setCommonAccessCPESupplier(String commonAccessCPESupplier) {
        this.commonAccessCPESupplier = commonAccessCPESupplier;
    }

    public String getAccessSpeed() {
        return accessSpeed;
    }

    public void setAccessSpeed(String accessSpeed) {
        this.accessSpeed = accessSpeed;
    }

    public String getAccessUom() {
        return accessUom;
    }

    public void setAccessUom(String accessUom) {
        this.accessUom = accessUom;
    }

    public String getProductAvailable() {
        return productAvailable;
    }

    public void setProductAvailable(String productAvailable) {
        this.productAvailable = productAvailable;
    }

    public String getAvailabilityDescription() {
        return availabilityDescription;
    }

    public void setAvailabilityDescription(String availabilityDescription) {
        this.availabilityDescription = availabilityDescription;
    }

    public String getMaxDownstreamBandwidth() {
        return maxDownstreamBandwidth;
    }

    public void setMaxDownstreamBandwidth(String maxDownstreamBandwidth) {
        this.maxDownstreamBandwidth = maxDownstreamBandwidth;
    }

    public String getSymmetricSpeedBandwidth() {
        return symmetricSpeedBandwidth;
    }

    public void setSymmetricSpeedBandwidth(String symmetricSpeedBandwidth) {
        this.symmetricSpeedBandwidth = symmetricSpeedBandwidth;
    }

    public String getMaxUpstreamSpeedBandwidth() {
        return maxUpstreamSpeedBandwidth;
    }

    public void setMaxUpstreamSpeedBandwidth(String maxUpstreamSpeedBandwidth) {
        this.maxUpstreamSpeedBandwidth = maxUpstreamSpeedBandwidth;
    }

    public Long getNumberOfCopperPairs() {
        return numberOfCopperPairs;
    }

    public void setNumberOfCopperPairs(Long numberOfCopperPairs) {
        this.numberOfCopperPairs = numberOfCopperPairs;
    }

    public String getExchangeCode() {
        return exchangeCode;
    }

    public void setExchangeCode(String exchangeCode) {
        this.exchangeCode = exchangeCode;
    }

    public String getSupplierTariffZone() {
        return supplierTariffZone;
    }

    public void setSupplierTariffZone(String supplierTariffZone) {
        this.supplierTariffZone = supplierTariffZone;
    }

    public String getCheckedReference() {
        return checkedReference;
    }

    public void setCheckedReference(String checkedReference) {
        this.checkedReference = checkedReference;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getRequestedTime() {
        return requestedTime;
    }

    public void setRequestedTime(Date requestedTime) {
        this.requestedTime = requestedTime;
    }

    public Date getRequestTimeout() {
        return requestTimeout;
    }

    public void setRequestTimeout(Date requestTimeout) {
        this.requestTimeout = requestTimeout;
    }

    public String getInterfaceId() {
        return interfaceId;
    }

    public void setInterfaceId(String interfaceId) {
        this.interfaceId = interfaceId;
    }

    public String getInterfaceName() {
        return interfaceName;
    }

    public void setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
    }

    public String getFramingId() {
        return framingId;
    }

    public void setFramingId(String framingId) {
        this.framingId = framingId;
    }

    public String getFramingName() {
        return framingName;
    }

    public void setFramingName(String framingName) {
        this.framingName = framingName;
    }

    public String getConnectorId() {
        return connectorId;
    }

    public void setConnectorId(String connectorId) {
        this.connectorId = connectorId;
    }

    public String getConnectorName() {
        return connectorName;
    }

    public void setConnectorName(String connectorName) {
        this.connectorName = connectorName;
    }

    public List<AvailabilitySetEntity> getSetEntityList() {
        return setEntityList;
    }

    public void setSetEntityList(List<AvailabilitySetEntity> setEntityList) {
        this.setEntityList = setEntityList;
    }

    public SupplierSiteEntity getSupplierSiteEntity() {
        return supplierSiteEntity;
    }

    public void setSupplierSiteEntity(SupplierSiteEntity supplierSiteEntity) {
        this.supplierSiteEntity = supplierSiteEntity;
    }

    public String getActive() {
        return active;
    }

    public void setActive(String active) {
        this.active = active;
    }

    public String getMandatroyAttributes() {
        return mandatroyAttributes;
    }

    public void setMandatroyAttributes(String mandatroyAttributes) {
        this.mandatroyAttributes = mandatroyAttributes;
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

    public Integer getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(Integer retryCount) {
        this.retryCount = retryCount;
    }

    public SupplierProduct toDto() {
        return new SupplierProduct(getSupplierSiteEntity().getSiteId(), getSupplierId(), getSupplierName(), getSpacId(), getSupplierProductId(), getSupplierProductName(),
                getDisplaySupplierProductName(), getProductAvailabilityCode(), getAvailabilityCheckType(), getCustomerLocationType(), getSupplierProductStatus(), getAccessType(), getParentAccessType(),
                getServiceVariant(), getContentionRatio(), getDeliveryMode(), getCpeAccessType(), getCentralizedAvailabilitySupported(), getCommonAccessCPESupplier(), getAccessSpeed(), getAccessUom(),
                getProductAvailable(), getAvailabilityDescription(), getMaxDownstreamBandwidth(), getSymmetricSpeedBandwidth(), getMaxUpstreamSpeedBandwidth(), getNumberOfCopperPairs(),
                getExchangeCode(), getSupplierTariffZone(), getCheckedReference(), getDescription(), getStatus(), getActive(), getRequestedTime(), getRequestTimeout(), getInterfaceId(), getInterfaceName(),
                getFramingId(), getFramingName(), getConnectorId(), getConnectorName(), getMandatroyAttributes(), toAvailabilitySetListDto());
    }

    private List<AvailabilitySet> toAvailabilitySetListDto() {
        List<AvailabilitySet> sets = newArrayList();
        for (AvailabilitySetEntity setEntity : getSetEntityList()) {
            sets.add(setEntity.toDto());
        }
        return sets;
    }
}
