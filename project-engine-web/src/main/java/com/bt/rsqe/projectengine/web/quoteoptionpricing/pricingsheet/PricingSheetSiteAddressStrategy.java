package com.bt.rsqe.projectengine.web.quoteoptionpricing.pricingsheet;

import com.bt.rsqe.customerrecord.SiteDTO;
import com.bt.rsqe.projectengine.web.SiteAddressStrategy;

public class PricingSheetSiteAddressStrategy extends SiteAddressStrategy {

    private SiteDTO siteDTO;

    public PricingSheetSiteAddressStrategy(SiteDTO siteDTO) {
        this.siteDTO = siteDTO;
    }

    public String getBuilding() {
        return format(siteDTO.subBuilding, siteDTO.building);
    }

    public String getAddressLine1() {
        return format(siteDTO.buildingNumber, siteDTO.subStreet, siteDTO.streetName, siteDTO.postBox);
    }

    public String getAddressLine2() {
        return format(siteDTO.subLocality, siteDTO.locality);
    }

    public String getCity() {
        return format(siteDTO.city);
    }

    public String getState() {
        return format(siteDTO.subStateCountyProvince, siteDTO.stateCountySProvince);
    }

    public String getPostCode() {
        return format(siteDTO.postCode);
    }
}
