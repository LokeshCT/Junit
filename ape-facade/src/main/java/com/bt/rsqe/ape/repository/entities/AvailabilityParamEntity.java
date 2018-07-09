package com.bt.rsqe.ape.repository.entities;

import com.bt.rsqe.ape.dto.AvailabilityParam;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "AVAILABILITY_PARAM")
public class AvailabilityParamEntity {

    @Id
    @SequenceGenerator(name = "PARAM_ID", sequenceName = "PARAM_ID", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "PARAM_ID")
    @Column(name="PARAM_ID")
    private Long paramId;

    @Column(name = "PARAM_NAME")
    private String name;

    @Column(name = "PARAM_VALUE")
    private String value;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "SET_ID")
    private AvailabilitySetEntity availabilitySetEntity;

    public AvailabilityParamEntity() {
    }

    public AvailabilityParamEntity(Long paramId, String name, String value, AvailabilitySetEntity availabilitySetEntity) {
        this.paramId = paramId;
        this.name = name;
        this.value = value;
        this.availabilitySetEntity = availabilitySetEntity;
    }

    public Long getParamId() {
        return paramId;
    }

    public void setParamId(Long paramId) {
        this.paramId = paramId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public AvailabilitySetEntity getAvailabilitySetEntity() {
        return availabilitySetEntity;
    }

    public void setAvailabilitySetEntity(AvailabilitySetEntity availabilitySetEntity) {
        this.availabilitySetEntity = availabilitySetEntity;
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

    public AvailabilityParam toDto(){
        return new AvailabilityParam(getName(),getValue());
    }
}
