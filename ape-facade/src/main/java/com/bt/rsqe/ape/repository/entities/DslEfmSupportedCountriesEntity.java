package com.bt.rsqe.ape.repository.entities;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Entity
@Table(name = "DSL_EFM_SUPPORTED_COUNTRIES")
public class DslEfmSupportedCountriesEntity {
    @Id
    @Column(name = "ISO_CODE")
    String isoCode;

    @Column(name = "COUNTRY")
    String country;

    @Column(name = "DSL_EFM_SUPPORTED")
    String dslEfmSupported;

    @Column(name = "UPDATED_ON")
    Date updatedOn;

    public DslEfmSupportedCountriesEntity() {
    }

    public DslEfmSupportedCountriesEntity(String isoCode, String country, String dslEfmSupported, Date updatedOn) {
        this.isoCode = isoCode;
        this.country = country;
        this.dslEfmSupported = dslEfmSupported;
        this.updatedOn = updatedOn;
    }

    public String getIsoCode() {
        return isoCode;
    }

    public void setIsoCode(String isoCode) {
        this.isoCode = isoCode;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getDslEfmSupported() {
        return dslEfmSupported;
    }

    public void setDslEfmSupported(String dslEfmSupported) {
        this.dslEfmSupported = dslEfmSupported;
    }

    public Date getUpdatedOn() {
        return updatedOn;
    }

    public void setUpdatedOn(Date updatedOn) {
        this.updatedOn = updatedOn;
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

}
