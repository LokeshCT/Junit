package com.bt.rsqe.ape.repository.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import java.util.Date;

@Entity
@Table(name = "BRITISH_ADDRESS")
public class BritishAddressEntity {

    @Id
    @SequenceGenerator(name = "BRITISH_ADDRESS_ID", sequenceName = "BRITISH_ADDRESS_ID", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "BRITISH_ADDRESS_ID")
    @Column(name = "BRITISH_ADDRESS_ID")
    private Long id;

    @Column(name = "SUB_PREMISES")
    private String subPremises;

    @Column(name = "PREMISES_NAME")
    private String premisesName;

    @Column(name = "THOROUGHFARE_NAME")
    private String thoroughfareName;

    @Column(name = "POST_TOWN")
    private String postTown;

    @Column(name = "COUNTY")
    private String county;

    @Column(name = "POST_CODE")
    private String postCode;

    @Column(name = "CREATED_DATE")
    private Date createDate;

    @OneToOne
    @JoinColumn(name = "EFM_ADDRESS_ID")
    private EFMAddressEntity efmAddressEntity;


    public BritishAddressEntity() {/*for JAXB*/}

    public BritishAddressEntity(String subPremises, String premisesName, String thoroughfareName, String postTown, String county, String postCode, Date createDate, EFMAddressEntity efmAddressEntity) {
        this.subPremises = subPremises;
        this.premisesName = premisesName;
        this.thoroughfareName = thoroughfareName;
        this.postTown = postTown;
        this.county = county;
        this.postCode = postCode;
        this.createDate = createDate;
        this.efmAddressEntity = efmAddressEntity;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSubPremises() {
        return subPremises;
    }

    public void setSubPremises(String subPremises) {
        this.subPremises = subPremises;
    }

    public String getPremisesName() {
        return premisesName;
    }

    public void setPremisesName(String premisesName) {
        this.premisesName = premisesName;
    }

    public String getThoroughfareName() {
        return thoroughfareName;
    }

    public void setThoroughfareName(String thoroughfareName) {
        this.thoroughfareName = thoroughfareName;
    }

    public String getPostTown() {
        return postTown;
    }

    public void setPostTown(String postTown) {
        this.postTown = postTown;
    }

    public String getCounty() {
        return county;
    }

    public void setCounty(String county) {
        this.county = county;
    }

    public String getPostCode() {
        return postCode;
    }

    public void setPostCode(String postCode) {
        this.postCode = postCode;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public EFMAddressEntity getEfmAddressEntity() {
        return efmAddressEntity;
    }

    public void setEfmAddressEntity(EFMAddressEntity efmAddressEntity) {
        this.efmAddressEntity = efmAddressEntity;
    }
}
