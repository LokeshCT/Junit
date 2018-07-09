package com.bt.rsqe.ape.repository.entities;

import com.bt.rsqe.ape.dto.SupplierProduct;
import com.bt.rsqe.ape.dto.sac.SacSupplierProdDetailDTO;
import com.bt.rsqe.utils.AssertObject;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: 608026723
 * Date: 03/09/15
 * Time: 23:10
 * To change this template use File | Settings | File Templates.
 */
@Entity
@Table(name = "SAC_SUPPLIER_PROD_MASTER")
public class SacSupplierProdMasterEntity {

    @Id
    @SequenceGenerator(name = "SEQ_SAC_SUP_PROD_ID", sequenceName = "SEQ_SAC_SUP_PROD_ID", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_SAC_SUP_PROD_ID")
    @Column(name = "SEQ_PROD_ID")
    private Long supProdId;

    @Column(name = "SITE_ID")
    private Long siteId;


    @Column(name = "SUPPLIER_ID")
    private Long supplierId;

    @Column(name = "SPAC_ID")
    private String spacId;


    @Column(name = "SUPPLIER_NAME")
    private String supplierName;

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
    private String numberOfCopperPairs;

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
    Timestamp requestedTime;

    @Column(name = "REQUEST_TIMEOUT")
    Timestamp requestTimeout;

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

    @Column(name = "AVAILABILITY_STATUS")
    private String availabilityStatus;

    @Column(name = "APE_REQUEST_ID")
    private String apeReqId;

    @Column(name = "CREATE_DATETIME")
    private Timestamp createDate;

    @Column(name = "CREATED_USER")
    private String createUser;

    @Column(name = "UPDATE_DATETIME")
    private Timestamp updateDate;

    @Column(name = "UPDATE_USER")
    private String updateUser;

    public Long getSiteId() {
        return siteId;
    }

    public void setSiteId(Long siteId) {
        this.siteId = siteId;
    }

    public Long getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(Long supplierId) {
        this.supplierId = supplierId;
    }

    public String getSpacId() {
        return spacId;
    }

    public void setSpacId(String spacId) {
        this.spacId = spacId;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
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

    public String getNumberOfCopperPairs() {
        return numberOfCopperPairs;
    }

    public void setNumberOfCopperPairs(String numberOfCopperPairs) {
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

    public String getActive() {
        return active;
    }

    public void setActive(String active) {
        this.active = active;
    }

    public Timestamp getRequestedTime() {
        return requestedTime;
    }

    public void setRequestedTime(Timestamp requestedTime) {
        this.requestedTime = requestedTime;
    }

    public Timestamp getRequestTimeout() {
        return requestTimeout;
    }

    public void setRequestTimeout(Timestamp requestTimeout) {
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

    public String getAvailabilityStatus() {
        return availabilityStatus;
    }

    public void setAvailabilityStatus(String availabilityStatus) {
        this.availabilityStatus = availabilityStatus;
    }

    public String getApeReqId() {
        return apeReqId;
    }

    public void setApeReqId(String apeReqId) {
        this.apeReqId = apeReqId;
    }

    public Timestamp getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Timestamp createDate) {
        this.createDate = createDate;
    }

    public String getCreateUser() {
        return createUser;
    }

    public void setCreateUser(String createUser) {
        this.createUser = createUser;
    }

    public Timestamp getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(Timestamp updateDate) {
        this.updateDate = updateDate;
    }

    public String getUpdateUser() {
        return updateUser;
    }

    public void setUpdateUser(String updateUser) {
        this.updateUser = updateUser;
    }

    public Long getSupProdId() {
        return supProdId;
    }

    public void setSupProdId(Long supProdId) {
        this.supProdId = supProdId;
    }

    public SacSupplierProdDetailDTO toSacSupplierProdDetailDTO(){
        SacSupplierProdDetailDTO supplierProdDetailDTO = new SacSupplierProdDetailDTO();
        supplierProdDetailDTO.setSupplierName(supplierName);
        supplierProdDetailDTO.setSeqProdId(supProdId);
        supplierProdDetailDTO.setSupplierProductName(supplierProductName);
        supplierProdDetailDTO.setAccessSpeed(accessSpeed);
        supplierProdDetailDTO.setAccessType(accessType);
        supplierProdDetailDTO.setSupplierId(supplierId);
        supplierProdDetailDTO.setSupplierProductDispName(displaySupplierProductName);
        supplierProdDetailDTO.setAccessSpeedUnit(accessUom);
        supplierProdDetailDTO.setServiceVarient(serviceVariant);
        if(numberOfCopperPairs!=null){
            supplierProdDetailDTO.setNoOfCopperPairs(numberOfCopperPairs.toString());
        }

        return supplierProdDetailDTO;
    }

    public SupplierProduct toDto(){
        SupplierProduct obj = new SupplierProduct();
        obj.setSiteId(this.siteId);
        obj.setAccessSpeed(this.accessSpeed);
        obj.setAccessType(this.accessType);
        obj.setAccessUom(this.accessUom);
        obj.setActive(active);
        obj.setAvailabilityCheckType(availabilityCheckType);
        obj.setAvailabilityDescription(availabilityDescription);
        //obj.setAvailabilityStatus(availabilityStatus);
        obj.setCentralizedAvailabilitySupported(centralizedAvailabilitySupported);
        obj.setCheckedReference(checkedReference);
        obj.setCommonAccessCPESupplier(commonAccessCPESupplier);
        obj.setConnectorId(connectorId);
        obj.setConnectorName(connectorName);
        obj.setContentionRatio(contentionRatio);
        obj.setCpeAccessType(cpeAccessType);
        obj.setCustomerLocationType(customerLocationType);
        obj.setDeliveryMode(deliveryMode);
        obj.setDescription(description);
        obj.setDisplaySupplierProductName(displaySupplierProductName);
        obj.setExchangeCode(exchangeCode);
        obj.setFramingId(framingId);
        obj.setFramingName(framingName);
        obj.setInterfaceId(interfaceId);
        obj.setInterfaceName(interfaceName);
        obj.setMaxDownstreamBandwidth(maxDownstreamBandwidth);
        obj.setMaxUpstreamSpeedBandwidth(maxUpstreamSpeedBandwidth);
        if(!AssertObject.isEmpty(numberOfCopperPairs)){
        obj.setNumberOfCopperPairs(Long.parseLong(numberOfCopperPairs));
        }
        obj.setParentAccessType(parentAccessType);
        obj.setProductAvailabilityCode(productAvailabilityCode);
        obj.setProductAvailable(productAvailable);
        obj.setRequestedTime(requestedTime);
        obj.setRequestTimeout(requestTimeout);
        obj.setServiceVariant(serviceVariant);
        obj.setSiteId(siteId);
        obj.setSpacId(spacId);
        obj.setStatus(status);
        obj.setSupplierId(supplierId);
        obj.setSupplierName(supplierName);
        obj.setSupplierProductId(supplierProductId);
        obj.setSupplierProductName(supplierProductName);
        obj.setSupplierTariffZone(supplierTariffZone);
        obj.setSupplierProductStatus(supplierProductStatus);
        obj.setSymmetricSpeedBandwidth(symmetricSpeedBandwidth);
        return   obj;
    }
}

