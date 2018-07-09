package com.bt.rsqe.projectengine.web.view;

import com.bt.rsqe.customerrecord.SiteDTO;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

public class SiteView {
    private static final String SEPARATOR = ", ";
    private SiteDTO siteDTO = new SiteDTO();

    public SiteView() {
        //required by jaxb
    }

    public SiteView(SiteDTO siteDTO) {
        this.siteDTO = siteDTO;
    }

    public String getId() {
        return siteDTO.bfgSiteID;
    }

    public String getName() {
        return siteDTO.name;
    }


    public String getCountry() {
        return siteDTO.country;
    }

    public String getFullAddress() {
       return UISiteAddressStrategy.siteAddress(siteDTO);
    }

    @Override
    public boolean equals(Object o) {
        return EqualsBuilder.reflectionEquals(this, o);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }
}
