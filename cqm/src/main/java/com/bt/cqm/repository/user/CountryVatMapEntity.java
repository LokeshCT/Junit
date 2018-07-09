package com.bt.cqm.repository.user;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Created with IntelliJ IDEA.
 * User: 607937181
 * Date: 04/01/16
 * Time: 18:26
 * To change this template use File | Settings | File Templates.
 */

@Entity
@Table(name = "LE_COUNTRY_REGION_VAT")
public class CountryVatMapEntity {
    @Id
    @Column(name = "COUNTRY")
    private String country;

    @Column(name = "REGION")
    private String region;

    @Column(name = "VAT_PREFIX")
    private String vatPrefix;

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getVatPrefix() {
        return vatPrefix;
    }

    public void setVatPrefix(String vatPrefix) {
        this.vatPrefix = vatPrefix;
    }
}
