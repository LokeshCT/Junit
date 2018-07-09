package com.bt.rsqe.ape.repository.entities;

import com.bt.rsqe.ape.dto.AvailabilityParam;
import com.bt.rsqe.ape.dto.AvailabilitySet;

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
import java.util.List;

import static com.google.common.collect.Lists.*;

@Entity
@Table(name = "AVAILABILITY_SET")
public class AvailabilitySetEntity {

    @Id
    @SequenceGenerator(name = "SET_ID", sequenceName = "SET_ID", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SET_ID")
    @Column(name = "SET_ID")
    private Long setId;

    @Column(name = "SET_NAME")
    private String setName;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "SUPP_PROD_ID")
    private SupplierProductEntity supplierProductEntity;

    @OneToMany(mappedBy = "availabilitySetEntity",fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private List<AvailabilityParamEntity> paramEntityList;

    public AvailabilitySetEntity() {
    }

    public AvailabilitySetEntity(Long setId, String setName, SupplierProductEntity supplierProductEntity, List<AvailabilityParamEntity> paramEntityList) {
        this.setId = setId;
        this.setName = setName;
        this.supplierProductEntity = supplierProductEntity;
        this.paramEntityList = paramEntityList;
    }

    public Long getSetId() {
        return setId;
    }

    public void setSetId(Long setId) {
        this.setId = setId;
    }

    public String getSetName() {
        return setName;
    }

    public void setSetName(String setName) {
        this.setName = setName;
    }

    public SupplierProductEntity getSupplierProductEntity() {
        return supplierProductEntity;
    }

    public void setSupplierProductEntity(SupplierProductEntity supplierProductEntity) {
        this.supplierProductEntity = supplierProductEntity;
    }

    public List<AvailabilityParamEntity> getParamEntityList() {
        return paramEntityList;
    }

    public void setParamEntityList(List<AvailabilityParamEntity> paramEntityList) {
        this.paramEntityList = paramEntityList;
    }

    public AvailabilitySet toDto(){
        return new AvailabilitySet(getSetName(),toAvailabilityParamListDto());
    }

    public List<AvailabilityParam> toAvailabilityParamListDto() {
        List<AvailabilityParam> availabilityParams = newArrayList();
        for (AvailabilityParamEntity paramEntity : getParamEntityList()) {
            availabilityParams.add(paramEntity.toDto());
        }
        return availabilityParams;
    }
}
