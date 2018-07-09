package com.bt.rsqe.ape.repository.entities;

import com.bt.rsqe.ape.dto.SupplierProduct;
import com.bt.rsqe.ape.dto.sac.SacSupplierProdAvailDTO;
import com.bt.rsqe.ape.dto.sac.SacSupplierProdDetailDTO;
import com.bt.rsqe.utils.AssertObject;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Fetch;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.io.Serializable;
import java.sql.Timestamp;

/**
 * Created with IntelliJ IDEA.
 * User: 608026723
 * Date: 03/09/15
 * Time: 18:48
 * To change this template use File | Settings | File Templates.
 */
@Entity
@IdClass(SacSupplierProdAvailPK.class)
@Table(name = "SAC_SUPPLIER_AVAILABILITY")
public class SacSupplierProdAvailEntity {

    @Id
    @Column(name = "SITE_ID", insertable = true, updatable = true)
    private Long siteId;

    @Id
    @Column(name = "SPAC_ID", insertable = true, updatable = true)
    private String spacId;


    @ManyToOne
    @JoinColumns({
                    @JoinColumn(name = "SITE_ID", referencedColumnName = "SITE_ID", insertable = false, updatable = false)
                 })
    private SacRequestEntity requestEntity;

    @Column(name = "AVAILABILITY_STATUS")
    private String availStatus;

    @OneToOne(cascade = {CascadeType.PERSIST,CascadeType.MERGE,CascadeType.REFRESH})
    @JoinColumns(
        {
            @JoinColumn(name = "SUP_PROD_ID", insertable = true, updatable = true, referencedColumnName = "SEQ_PROD_ID")
        }
    )
    private SacSupplierProdMasterEntity supplierProduct;

    @Column(name = "STATUS")
    private String status;

    @Column(name = "ERROR_DESCRIPTION")
    private String errorDesc;

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

    public SacRequestEntity getRequestEntity() {
        return requestEntity;
    }

    public void setRequestEntity(SacRequestEntity requestEntity) {
        this.requestEntity = requestEntity;
    }

    public String getSpacId() {
        return spacId;
    }

    public void setSpacId(String spacId) {
        this.spacId = spacId;
    }

    public String getAvailStatus() {
        return availStatus;
    }

    public void setAvailStatus(String availStatus) {
        this.availStatus = availStatus;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getErrorDesc() {
        return errorDesc;
    }

    public void setErrorDesc(String errorDesc) {
        this.errorDesc = errorDesc;
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

    public SacSupplierProdMasterEntity getSupplierProduct() {
        return supplierProduct;
    }

    public void setSupplierProduct(SacSupplierProdMasterEntity supplierProduct) {
        this.supplierProduct = supplierProduct;
    }

    public Long getSiteId() {
        return siteId;
    }

    public void setSiteId(Long siteId) {
        this.siteId = siteId;
    }

    public SacSupplierProdAvailDTO toDto(){
        SacSupplierProdAvailDTO sacSupplierProdAvailDTO = new SacSupplierProdAvailDTO();
        sacSupplierProdAvailDTO.setAvailStatus(this.getAvailStatus());
        sacSupplierProdAvailDTO.setSiteId(this.getSiteId().toString());
        sacSupplierProdAvailDTO.setSpacId(this.getSpacId());
        SacSupplierProdMasterEntity supplierProdMasterEntity = this.getSupplierProduct();
        if(supplierProdMasterEntity!=null ){
            SacSupplierProdDetailDTO supplierProdDetailDTO = new SacSupplierProdDetailDTO();
            SupplierProduct supplierProduct = new SupplierProduct();
            /*Get away this DTO*/
            supplierProdDetailDTO.setSeqProdId(supplierProdMasterEntity.getSupProdId());
            supplierProdDetailDTO.setAccessSpeed(supplierProdMasterEntity.getAccessSpeed());
            supplierProdDetailDTO.setAccessType(supplierProdMasterEntity.getAccessType());
            if(supplierProdMasterEntity.getNumberOfCopperPairs()!=null){
            supplierProdDetailDTO.setNoOfCopperPairs(supplierProdMasterEntity.getNumberOfCopperPairs().toString());
            }
            supplierProdDetailDTO.setApplicability(this.getAvailStatus());
            supplierProdDetailDTO.setSpacId(supplierProdMasterEntity.getSpacId());
            supplierProdDetailDTO.setSupplierName(supplierProdMasterEntity.getSupplierName());
            supplierProdDetailDTO.setSupplierProductName(supplierProdMasterEntity.getSupplierProductName());
            supplierProdDetailDTO.setSupplierProductDispName(supplierProdMasterEntity.getDisplaySupplierProductName());
            supplierProdDetailDTO.setAccessSpeedUnit(supplierProdMasterEntity.getAccessUom());
            supplierProdDetailDTO.setServiceVarient(supplierProdMasterEntity.getServiceVariant());

            /*Should use this DTO instead*/
            supplierProduct.setSupplierProductId(supplierProdMasterEntity.getSupplierProductId());
            supplierProduct.setSupplierName(supplierProdMasterEntity.getSupplierName());
            supplierProduct.setSupplierId(supplierProdMasterEntity.getSupplierId());
            supplierProduct.setSpacId(supplierProdMasterEntity.getSpacId());
            supplierProduct.setSupplierProductName(supplierProdMasterEntity.getSupplierProductName());
            supplierProduct.setAvailabilityCheckType(supplierProdMasterEntity.getAvailabilityCheckType());
            if(!AssertObject.isEmpty(supplierProdMasterEntity.getNumberOfCopperPairs())){
            supplierProduct.setNumberOfCopperPairs(Long.parseLong(supplierProdMasterEntity.getNumberOfCopperPairs()));
            }
            supplierProduct.setSupplierProductStatus(supplierProdMasterEntity.getSupplierProductStatus());
            supplierProduct.setAccessSpeed(supplierProdMasterEntity.getAccessSpeed());
            supplierProduct.setActive(supplierProdMasterEntity.getActive());
            supplierProduct.setAvailabilityDescription(supplierProdMasterEntity.getAvailabilityDescription());
            supplierProduct.setDisplaySupplierProductName(supplierProdMasterEntity.getDisplaySupplierProductName());

            sacSupplierProdAvailDTO.setSupplierProduct(supplierProduct);
            sacSupplierProdAvailDTO.setSacSupplierProdDetailDTO(supplierProdDetailDTO);
        }

        return sacSupplierProdAvailDTO;
    }

    public SacSupplierProdAvailDTO toSimpleDto(){
        SacSupplierProdAvailDTO sacSupplierProdAvailDTO = new SacSupplierProdAvailDTO();
        sacSupplierProdAvailDTO.setAvailStatus(this.getAvailStatus());
        sacSupplierProdAvailDTO.setSiteId(this.getSiteId().toString());
        sacSupplierProdAvailDTO.setSpacId(this.getSpacId());
        return sacSupplierProdAvailDTO;
    }
}

class SacSupplierProdAvailPK implements Serializable {

    private Long siteId;

    private String spacId;

    SacSupplierProdAvailPK(Long siteId, String spacId) {
        this.siteId = siteId;
        this.spacId = spacId;
    }

    SacSupplierProdAvailPK() {
    }

    public String getSpacId() {
        return spacId;
    }

    public Long getSiteId() {
        return siteId;
    }

    @Override
    public boolean equals(Object o) {
        return EqualsBuilder.reflectionEquals(this, o);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }
}