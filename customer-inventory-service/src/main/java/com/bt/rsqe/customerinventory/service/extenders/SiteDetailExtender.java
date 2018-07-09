package com.bt.rsqe.customerinventory.service.extenders;

import com.bt.rsqe.customerinventory.service.client.domain.CIFAsset;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetSiteDetail;
import com.bt.rsqe.customerinventory.service.client.resource.CIFAssetExtension;
import com.bt.rsqe.customerrecord.CustomerResource;
import com.bt.rsqe.customerrecord.SiteDTO;
import com.bt.rsqe.logging.Log;
import com.bt.rsqe.logging.LogFactory;

import java.util.List;

import static com.bt.rsqe.customerinventory.service.client.resource.CIFAssetExtension.*;
import static com.bt.rsqe.enums.AssetType.*;
import static com.bt.rsqe.logging.LogLevel.*;
import static org.apache.commons.lang.StringUtils.*;

public class SiteDetailExtender {
    private final Logger logger = LogFactory.createDefaultLogger(Logger.class);
    private final CustomerResource customerResource;

    public SiteDetailExtender(CustomerResource customerResource) {
        this.customerResource = customerResource;
    }

    public void extend(List<CIFAssetExtension> cifAssetExtensions, CIFAsset cifAsset) {
        if (SiteDetail.isInList(cifAssetExtensions)) {
            String siteId = cifAsset.getSiteId();
            String customerId = cifAsset.getCustomerId();
            String projectId = cifAsset.getProjectId();

            SiteDTO siteDTO;

            if (isEmpty(siteId) && isNotEmpty(customerId) && isNotEmpty(projectId) && REAL == cifAsset.getAssetType()) {
                siteDTO = getCentralSiteDTO(customerId, projectId);
                if (siteDTO != null) {
                    loadSiteDetails(cifAsset, siteDTO);
                } else {
                    cifAsset.loadSiteDetail(null);
                }
            } else if (isNotEmpty(siteId) && isNotEmpty(projectId)) {
                siteDTO = customerResource.siteResource(customerId).get(siteId, projectId);
                loadSiteDetails(cifAsset, siteDTO);
            } else {
                cifAsset.loadSiteDetail(null);
            }
        }
    }

    private SiteDTO getCentralSiteDTO(String customerId, String projectId) {
        SiteDTO siteDTO = null;
        try {
            siteDTO = customerResource.siteResource(customerId).getCentralSite(projectId);
        } catch (Exception e) {
            e.printStackTrace();
            logger.centralSiteDetailsError(customerId, projectId);
        }
        return siteDTO;
    }

    private void loadSiteDetails(CIFAsset cifAsset, SiteDTO siteDTO) {
        String postCode = siteDTO.getPostCode();
        String building = siteDTO.getBuilding();
        String buildingNumber = siteDTO.getBuildingNumber();
        String address = String.format("%s %s %s %s %s %s",
                siteDTO.getBuildingNumber(),
                siteDTO.getStreetName(),
                siteDTO.getCity(),
                siteDTO.getStateCountySProvince(),
                siteDTO.getCountry(),
                siteDTO.getPostCode());
        String telephoneNumber = siteDTO.getTelephoneNumber();

        CIFAssetSiteDetail cifAssetSiteDetail = new CIFAssetSiteDetail(siteDTO.getSiteId().getValue(), siteDTO.getSiteName(),
                siteDTO.getCity(), siteDTO.getCountryName(), siteDTO.getCountryISOCode());

        cifAssetSiteDetail.setPostCode(postCode);
        cifAssetSiteDetail.setBuilding(building);
        cifAssetSiteDetail.setBuildingNumber(buildingNumber);
        cifAssetSiteDetail.setAddress(address);
        cifAssetSiteDetail.setTelephoneNumber(telephoneNumber);

        cifAsset.loadSiteDetail(cifAssetSiteDetail);

    }

    interface Logger {
        @Log(level = WARN, format = "Unable to get central site details for customer - %s, Project - %s")
        void centralSiteDetailsError(String customerId, String projectId);
    }
}
