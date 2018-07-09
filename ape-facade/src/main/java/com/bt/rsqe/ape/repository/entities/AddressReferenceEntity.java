package com.bt.rsqe.ape.repository.entities;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "ADDRESS_REFERENCE")
public class AddressReferenceEntity {

    @Id
    @SequenceGenerator(name = "ADDRESS_REFERENCE_ID", sequenceName = "ADDRESS_REFERENCE_ID", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ADDRESS_REFERENCE_ID")
    @Column(name = "ADDRESS_REFERENCE_ID")
    private Long id;

    @OneToMany(mappedBy = "addressReferenceEntity", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<TechnologysEntity> listOfTechnology;

    @Column(name = "REF_NUM")
    private String refNum;

    @Column(name = "QUALIFIER")
    private String qualifier;

    @Column(name = "DISTRICT_CODE")
    private String districtCode;

    @OneToOne
    @JoinColumn(name = "EFM_ADDRESS_ID")
    private EFMAddressEntity efmAddressEntity;


    @Column(name = "CREATED_DATE")
    private Date createDate;

    public AddressReferenceEntity() {/*for JAXB*/}

    public AddressReferenceEntity(List<TechnologysEntity> listOfTechnology, String refNum, String qualifier, String districtCode, EFMAddressEntity efmAddressEntity, Date createDate) {
        this.listOfTechnology = listOfTechnology;
        this.refNum = refNum;
        this.qualifier = qualifier;
        this.districtCode = districtCode;
        this.efmAddressEntity = efmAddressEntity;
        this.createDate = createDate;
    }

    public AddressReferenceEntity(String refNum, String qualifier, String districtCode, EFMAddressEntity efmAddressEntity, Date createDate) {
        this.refNum = refNum;
        this.qualifier = qualifier;
        this.districtCode = districtCode;
        this.efmAddressEntity = efmAddressEntity;
        this.createDate = createDate;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<TechnologysEntity> getListOfTechnology() {
        return listOfTechnology;
    }

    public void setListOfTechnology(List<TechnologysEntity> listOfTechnology) {
        this.listOfTechnology = listOfTechnology;
    }

    public String getRefNum() {
        return refNum;
    }

    public void setRefNum(String refNum) {
        this.refNum = refNum;
    }

    public String getQualifier() {
        return qualifier;
    }

    public void setQualifier(String qualifier) {
        this.qualifier = qualifier;
    }

    public String getDistrictCode() {
        return districtCode;
    }

    public void setDistrictCode(String districtCode) {
        this.districtCode = districtCode;
    }

    public EFMAddressEntity getEfmAddressEntity() {
        return efmAddressEntity;
    }

    public void setEfmAddressEntity(EFMAddressEntity efmAddressEntity) {
        this.efmAddressEntity = efmAddressEntity;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

}
