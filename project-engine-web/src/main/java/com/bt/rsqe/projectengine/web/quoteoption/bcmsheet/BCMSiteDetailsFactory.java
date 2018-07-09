package com.bt.rsqe.projectengine.web.quoteoption.bcmsheet;

import com.bt.rsqe.customerrecord.SiteDTO;
import com.bt.rsqe.domain.project.ProductInstance;
import com.bt.rsqe.error.RsqeApplicationException;
import com.bt.rsqe.web.rest.exception.ResourceNotFoundException;

public class BCMSiteDetailsFactory {

    public BCMSiteDetails create(BCMInformer informer, ProductInstance toBeProductInstance) {
        SiteDTO siteDTO;

        if (toBeProductInstance.getProductOffering().isSiteInstallable()) {
            siteDTO = informer.getSite(toBeProductInstance.getSiteId());
        } else {
            try {
                siteDTO = informer.getCentralSite();
            } catch (ResourceNotFoundException e) {
                throw new RsqeApplicationException(e, "Unable to find central site for customer " + informer.getCustomerId());
            }
        }

        return new BCMSiteDetails(new Integer(siteDTO.bfgSiteID),
                                  siteDTO.name,
                                  siteDTO.country,
                                  siteDTO.city,
                                  toBeProductInstance.getProductOffering().isSiteInstallable());
    }
}
