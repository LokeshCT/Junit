package com.bt.rsqe.projectengine.web.view;

import com.bt.rsqe.customerrecord.SiteDTO;
import com.bt.rsqe.projectengine.web.SiteAddressStrategy;

public class UISiteAddressStrategy extends SiteAddressStrategy {

    public static String siteAddress(SiteDTO siteDTO) {
        return format(siteDTO.subBuilding,
                      siteDTO.building,
                      siteDTO.buildingNumber,
                      siteDTO.subStreet,
                      siteDTO.streetName,
                      siteDTO.subLocality,
                      siteDTO.locality,
                      siteDTO.city,
                      siteDTO.subStateCountyProvince,
                      siteDTO.stateCountySProvince,
                      siteDTO.country,
                      siteDTO.postCode,
                      siteDTO.postBox);
    }

}
