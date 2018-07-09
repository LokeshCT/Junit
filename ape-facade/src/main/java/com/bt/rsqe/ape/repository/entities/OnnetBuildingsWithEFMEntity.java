package com.bt.rsqe.ape.repository.entities;

import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "ONNET_BUILDINGS_WITH_EFM")
public class OnnetBuildingsWithEFMEntity {

    @Id
    @Column(name = "BFG_SITE_ID")
    private Long bfgSiteId;

    @Column(name = "CREATED_DATE")
    private Date createDate;

    @OneToMany(mappedBy = "onnetBuildingsWithEFMEntity", cascade = CascadeType.ALL)
    @LazyCollection(LazyCollectionOption.FALSE)
    private List<OnnetBuildingEntity> onnetBuildingEntityList;

    @OneToMany(mappedBy = "onnetBuildingsWithEFMEntity", cascade = CascadeType.ALL)
    @LazyCollection(LazyCollectionOption.FALSE)
    private List<EFMAddressEntity> efmAddressEntityList;

    public OnnetBuildingsWithEFMEntity() {/*for JAXB*/}

    public OnnetBuildingsWithEFMEntity(Long bfgSiteId, Date createDate, List<OnnetBuildingEntity> onnetBuildingEntityList, List<EFMAddressEntity> efmAddressEntityList) {
        this.bfgSiteId = bfgSiteId;
        this.createDate = createDate;
        this.onnetBuildingEntityList = onnetBuildingEntityList;
        this.efmAddressEntityList = efmAddressEntityList;
    }

    public OnnetBuildingsWithEFMEntity(Long bfgSiteId, Date createDate) {
        this.bfgSiteId = bfgSiteId;
        this.createDate = createDate;
    }

    public Long getBfgSiteId() {
        return bfgSiteId;
    }

    public void setBfgSiteId(Long bfgSiteId) {
        this.bfgSiteId = bfgSiteId;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public List<OnnetBuildingEntity> getOnnetBuildingEntityList() {
        return onnetBuildingEntityList;
    }

    public void setOnnetBuildingEntityList(List<OnnetBuildingEntity> onnetBuildingEntityList) {
        this.onnetBuildingEntityList = onnetBuildingEntityList;
    }

    public List<EFMAddressEntity> getEfmAddressEntityList() {
        return efmAddressEntityList;
    }

    public void setEfmAddressEntityList(List<EFMAddressEntity> efmAddressEntityList) {
        this.efmAddressEntityList = efmAddressEntityList;
    }
}
