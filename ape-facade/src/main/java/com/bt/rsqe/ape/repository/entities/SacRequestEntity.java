package com.bt.rsqe.ape.repository.entities;


import com.bt.rsqe.ape.dto.sac.SacSiteDTO;
import com.bt.rsqe.ape.dto.sac.SacSupplierProdAvailDTO;

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
import javax.persistence.Transient;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: 608026723
 * Date: 03/09/15
 * Time: 18:40
 * To change this template use File | Settings | File Templates.
 */
@Entity
@Table(name = "SAC_SITE_REQUESTS")
public class SacRequestEntity {
    @Id
    @SequenceGenerator(name = "SEQ_DUMMY_SITE_ID", sequenceName = "SEQ_DUMMY_SITE_ID", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_DUMMY_SITE_ID")
    @Column(name = "SITE_ID")
    private Long siteId;


    @Column(name = "FILE_NAME", insertable = true, updatable = true)
    private String fileName;

    @Column(name = "SITE_NAME")
    private String siteName;

    @Column(name = "COUNTRY_ISO_CODE")
    private String countryIsoCode;

    @Column(name = "TELEPHONE_NO")
    private String telephoneNo;

    @ManyToOne
    @JoinColumn(name = "FILE_NAME", referencedColumnName = "FILE_NAME", insertable = false, updatable = false)
    private SacBulkUploadEntity uploadFile;

    @OneToMany(mappedBy = "requestEntity",cascade = CascadeType.ALL)
    private List<SacSupplierProdAvailEntity> suppliers;

    @Column(name = "COUNTRY_NAME")
    private String countryName;


    @Column(name = "STATUS")
    private String status;

    @Column(name = "ERROR_DESCRIPTION")
    private String errorDesc;

    @Column(name = "REQUEST_TIMESTAMP")
    private Date reqTimeStamp;

    @Column(name = "APE_2ND_REQUEST_ID")
    private String ape2ReqId;

    @Column(name = "APE_3RD_REQUEST_ID")
    private String ape3ReqId;

    @Column(name = "CREATE_DATETIME")
    private Timestamp createDate;

    @Column(name = "CREATED_USER")
    private String createUser;

    @Column(name = "UPDATE_DATETIME")
    private Timestamp updateDate;

    @Column(name = "UPDATE_USER")
    private String updateUser;

    public SacRequestEntity(String fileName, String countryIsoCode, String telephoneNo) {
        this.fileName = fileName;
        this.countryIsoCode = countryIsoCode;
        this.telephoneNo = telephoneNo;
    }

    public SacRequestEntity(String fileName, String countryIsoCode, String telephoneNo, String status) {
        this.fileName = fileName;
        this.countryIsoCode = countryIsoCode;
        this.telephoneNo = telephoneNo;
        this.status = status;
    }

    public SacRequestEntity() {
    }

    @Transient
    public String getFileName() {
        return fileName;
    }


    public String getCountryIsoCode() {
        return countryIsoCode;
    }


    public String getTelephoneNo() {
        return telephoneNo;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setCountryIsoCode(String countryIsoCode) {
        this.countryIsoCode = countryIsoCode;
    }

    public void setTelephoneNo(String telephoneNo) {
        this.telephoneNo = telephoneNo;
    }

    public SacBulkUploadEntity getUploadFile() {
        return uploadFile;
    }

    public void setUploadFile(SacBulkUploadEntity uploadFile) {
        this.uploadFile = uploadFile;
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

    public Date getReqTimeStamp() {
        return reqTimeStamp;
    }

    public void setReqTimeStamp(Date reqTimeStamp) {
        this.reqTimeStamp = reqTimeStamp;
    }

    public String getApe2ReqId() {
        return ape2ReqId;
    }

    public void setApe2ReqId(String ape2ReqId) {
        this.ape2ReqId = ape2ReqId;
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

    public String getCountryName() {
        return countryName;
    }

    public void setCountryName(String countryName) {
        this.countryName = countryName;
    }

    public List<SacSupplierProdAvailEntity> getSuppliers() {
        return suppliers;
    }

    public void setSuppliers(List<SacSupplierProdAvailEntity> suppliers) {
        this.suppliers = suppliers;
    }

    public Long getSiteId() {
        return siteId;
    }

    public void setSiteId(Long siteId) {
        this.siteId = siteId;
    }

    public String getSiteName() {
        return siteName;
    }

    public void setSiteName(String siteName) {
        this.siteName = siteName;
    }

    public String getApe3ReqId() {
        return ape3ReqId;
    }

    public void setApe3ReqId(String ape3ReqId) {
        this.ape3ReqId = ape3ReqId;
    }

    public SacSiteDTO toDto(){
        SacSiteDTO sacSiteDTO= new SacSiteDTO(siteName,countryIsoCode,countryName,telephoneNo);
        sacSiteDTO.setSiteId(siteId.toString());
        sacSiteDTO.setErrorDesc(errorDesc);
        sacSiteDTO.setStatus(status);
        sacSiteDTO.setApe2ReqId(ape2ReqId);
         if(this.getSuppliers()!=null && this.getSuppliers().size()>0){
             List<SacSupplierProdAvailDTO> sacSupplierProdAvailDTOs = new ArrayList<SacSupplierProdAvailDTO>();
             for(SacSupplierProdAvailEntity sacSupplierProdAvailEntity : this.getSuppliers()){
               sacSupplierProdAvailDTOs.add(sacSupplierProdAvailEntity.toDto());
             }
             sacSiteDTO.setSacSupplierProdAvailDTOs(sacSupplierProdAvailDTOs);
         }
        return sacSiteDTO;
    }
}

