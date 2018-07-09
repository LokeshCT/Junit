package com.bt.rsqe.ape.repository.entities;

import com.bt.rsqe.ape.dto.AddressReferenceDTO;
import com.bt.rsqe.ape.dto.BritishAddressDTO;
import com.bt.rsqe.ape.dto.EFMAddressDTO;
import com.bt.rsqe.ape.dto.AddressInfo;
import com.bt.rsqe.ape.dto.TechnologysDTO;
import com.google.common.base.Function;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import java.util.Date;
import java.util.List;

import static com.google.common.collect.Lists.*;

@Entity
@Table(name = "EFM_ADDRESS")
public class EFMAddressEntity {

    @Id
    @SequenceGenerator(name = "EFM_ADDRESS_ID", sequenceName = "EFM_ADDRESS_ID", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "EFM_ADDRESS_ID")
    @Column(name = "EFM_ADDRESS_ID")
    private Long id;

    @Column(name = "SELECTION")
    private String selection;

    @Column(name = "CREATED_DATE")
    private Date createDate;

    @OneToOne(mappedBy = "efmAddressEntity", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private BritishAddressEntity britishAddressEntity;

    @OneToOne(mappedBy = "efmAddressEntity", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private AddressReferenceEntity addressReferenceEntity;

    @ManyToOne
    @JoinColumn(name = "BFG_SITE_ID")
    private OnnetBuildingsWithEFMEntity onnetBuildingsWithEFMEntity;

    public EFMAddressEntity() {/*for JAXB*/}

    public EFMAddressEntity(String selection, Date createDate, BritishAddressEntity britishAddressEntity, AddressReferenceEntity addressReferenceEntity, OnnetBuildingsWithEFMEntity onnetBuildingsWithEFMEntity) {
        this.selection = selection;
        this.createDate = createDate;
        this.britishAddressEntity = britishAddressEntity;
        this.addressReferenceEntity = addressReferenceEntity;
        this.onnetBuildingsWithEFMEntity = onnetBuildingsWithEFMEntity;
    }

    public EFMAddressEntity(String selection, Date createDate, OnnetBuildingsWithEFMEntity onnetBuildingsWithEFMEntity) {
        this.selection = selection;
        this.createDate = createDate;
        this.onnetBuildingsWithEFMEntity = onnetBuildingsWithEFMEntity;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSelection() {
        return selection;
    }

    public void setSelection(String selection) {
        this.selection = selection;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public BritishAddressEntity getBritishAddressEntity() {
        return britishAddressEntity;
    }

    public void setBritishAddressEntity(BritishAddressEntity britishAddressEntity) {
        this.britishAddressEntity = britishAddressEntity;
    }

    public AddressReferenceEntity getAddressReferenceEntity() {
        return addressReferenceEntity;
    }

    public void setAddressReferenceEntity(AddressReferenceEntity addressReferenceEntity) {
        this.addressReferenceEntity = addressReferenceEntity;
    }

    public OnnetBuildingsWithEFMEntity getOnnetBuildingsWithEFMEntity() {
        return onnetBuildingsWithEFMEntity;
    }

    public void setOnnetBuildingsWithEFMEntity(OnnetBuildingsWithEFMEntity onnetBuildingsWithEFMEntity) {
        this.onnetBuildingsWithEFMEntity = onnetBuildingsWithEFMEntity;
    }

    public EFMAddressDTO toEFMAddressDto() {
        return new EFMAddressDTO(toBritishAddressDTO(getBritishAddressEntity()), toAddressReferenceDTO(getAddressReferenceEntity()), getSelection());

    }

    public AddressInfo toAddressInfo() {
        return new AddressInfo(getAddressReferenceEntity().getRefNum(),
                                                                     getBritishAddressEntity().getThoroughfareName(),
                                                                     getBritishAddressEntity().getPostTown(),
                                                                     getAddressReferenceEntity().getDistrictCode());

    }

    private BritishAddressDTO toBritishAddressDTO(BritishAddressEntity britishAddress) {
        return new BritishAddressDTO(britishAddress.getSubPremises(), britishAddress.getPremisesName(), britishAddress.getThoroughfareName(), britishAddress.getPostTown(), britishAddress.getCounty(), britishAddress.getPostCode());
    }

    private AddressReferenceDTO toAddressReferenceDTO(AddressReferenceEntity addressReference) {
        return new AddressReferenceDTO(toTechnologysDTO(addressReference.getListOfTechnology()), addressReference.getRefNum(), addressReference.getQualifier(), addressReference.getDistrictCode(), addressReference.getCreateDate());

    }

    private List<TechnologysDTO> toTechnologysDTO(List<TechnologysEntity> listOfTechnology) {
        return newArrayList(transform(newArrayList(listOfTechnology), new Function<TechnologysEntity, TechnologysDTO>() {
            @Override
            public TechnologysDTO apply(TechnologysEntity input) {
                return new TechnologysDTO(input.getTechnology());
            }
        }));

    }
}
