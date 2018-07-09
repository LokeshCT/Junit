package com.bt.rsqe.projectengine.web.productconfigurator.model;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

public class BulkConfigSiteModel {

    private String siteName;
    private String siteId;
    private String serviceId;
    private String city;
    private String country;

    public BulkConfigSiteModel(String siteName, String siteId, String serviceId, String city, String country) {

        this.siteName = siteName;
        this.siteId = siteId;
        this.serviceId = serviceId;
        this.city = city;
        this.country = country;
    }

    public String getSiteName() {
        return siteName;
    }

    public String getSiteId() {
        return siteId;
    }

    public String getServiceId() {
        return serviceId;
    }

    public String getCity() {
        return city;
    }

    public String getCountry() {
        return country;
    }


    @Override
    public boolean equals(Object o) {
        return EqualsBuilder.reflectionEquals(o, this);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

}
