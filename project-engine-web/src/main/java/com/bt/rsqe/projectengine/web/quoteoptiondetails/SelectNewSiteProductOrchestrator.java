package com.bt.rsqe.projectengine.web.quoteoptiondetails;

import com.bt.rsqe.client.Pmr;
import com.bt.rsqe.customerrecord.ExpedioClientResources;
import com.bt.rsqe.customerrecord.SiteDTO;
import com.bt.rsqe.domain.Notification;
import com.bt.rsqe.expedio.project.ExpedioProjectResource;
import com.bt.rsqe.projectengine.web.facades.ProductIdentifierFacade;
import com.bt.rsqe.projectengine.web.facades.QuoteOptionFacade;
import com.bt.rsqe.projectengine.web.facades.SiteFacade;
import com.bt.rsqe.projectengine.web.uri.UriFactory;
import com.bt.rsqe.projectengine.web.view.MoveProductSitesDTO;
import com.bt.rsqe.projectengine.web.view.filtering.PaginatedFilter;
import com.bt.rsqe.projectengine.web.view.filtering.PaginatedFilterResult;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;

import java.util.Date;
import java.util.List;

public class SelectNewSiteProductOrchestrator extends ProductOrchestrator<MoveProductSitesDTO> {
    protected SelectNewSiteProductOrchestrator(SiteFacade siteFacade,
                                               ProductIdentifierFacade productIdentifierFacade,
                                               UriFactory productConfiguratorUriFactory,
                                               QuoteOptionFacade quoteOptionFacade,
                                               ExpedioProjectResource projectResource,
                                               Pmr pmr,
                                               ExpedioClientResources expedioClientResources) {
        super(siteFacade, productIdentifierFacade, productConfiguratorUriFactory, quoteOptionFacade, projectResource, pmr, expedioClientResources, null);
    }

    @Override
    public MoveProductSitesDTO buildSitesView(String customerId, String projectId, PaginatedFilter paginatedFilter, String forProduct, String newSiteId, List<String> existingSiteIds, Optional<String> productVersion) {
        final List<SiteDTO> siteDTOs = siteFacade.getAllBranchSites(customerId, projectId);
        final PaginatedFilterResult<SiteDTO> filterResult = paginatedFilter.applyTo(siteDTOs);
        List<String> supportedCountries = forProduct == null ? Lists.<String>newArrayList() : pmr.getSupportedCountries(forProduct);
        return new MoveProductSitesDTO(filterResult, supportedCountries);
    }

    public Notification endOfLifeCheck(String siteId, String productCode, String productVersion, Date systemDate, String contractResignStatus, String lineItemId) {
        return null;
    }
}
