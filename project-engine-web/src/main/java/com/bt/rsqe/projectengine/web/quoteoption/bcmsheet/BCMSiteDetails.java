package com.bt.rsqe.projectengine.web.quoteoption.bcmsheet;

public class BCMSiteDetails {

    private Integer bfgSiteId;
    private String name, country, city;
    private boolean isSiteInstallable;

    public BCMSiteDetails(Integer bfgSiteId, String name, String country, String city, boolean isSiteInstallable) {
        this.bfgSiteId = bfgSiteId;
        this.name = name;
        this.country = country;
        this.city = city;
        this.isSiteInstallable  = isSiteInstallable;
    }

    public Integer getId() {
        return bfgSiteId;
    }

    public String getName() {
        return name;
    }

    public String getCountry() {
        return country;
    }

    public String getCity() {
        return city;
    }

    public boolean isSiteInstallable() {
        return isSiteInstallable;
    }
}