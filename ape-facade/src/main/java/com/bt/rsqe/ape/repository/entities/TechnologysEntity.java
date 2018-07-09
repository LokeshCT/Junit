package com.bt.rsqe.ape.repository.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import java.util.Date;

@Entity
@Table(name = "TECHNOLOGYS")
public class TechnologysEntity {

    @Id
    @SequenceGenerator(name = "TECHNOLOGYS_ID", sequenceName = "TECHNOLOGYS_ID", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "TECHNOLOGYS_ID")
    @Column(name = "TECHNOLOGYS_ID")
    private Long id;

    @Column(name = "TECHNOLOGY")
    private String technology;

    @Column(name = "CREATED_DATE")
    private Date createDate;

    @ManyToOne
    @JoinColumn(name = "ADDRESS_REFERENCE_ID")
    private AddressReferenceEntity addressReferenceEntity;

    public TechnologysEntity() {/*for JAXB*/}

    public TechnologysEntity(String technology) {
        this.technology = technology;
    }

    public TechnologysEntity(String technology, Date createDate, AddressReferenceEntity addressReferenceEntity) {
        this.technology = technology;
        this.createDate = createDate;
        this.addressReferenceEntity = addressReferenceEntity;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTechnology() {
        return technology;
    }

    public void setTechnology(String technology) {
        this.technology = technology;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public AddressReferenceEntity getAddressReferenceEntity() {
        return addressReferenceEntity;
    }

    public void setAddressReferenceEntity(AddressReferenceEntity addressReferenceEntity) {
        this.addressReferenceEntity = addressReferenceEntity;
    }
}
