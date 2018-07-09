package com.bt.rsqe.projectengine.web.quoteoptiondetails;

import com.bt.rsqe.client.Pmr;
import com.bt.rsqe.customerinventory.client.ProductInstanceClient;
import com.bt.rsqe.customerinventory.driver.SiteDriver;
import com.bt.rsqe.customerinventory.dto.AssetDTO;
import com.bt.rsqe.customerrecord.ExpedioClientResources;
import com.bt.rsqe.customerrecord.SiteDTO;
import com.bt.rsqe.domain.Notification;
import com.bt.rsqe.domain.product.SellableProduct;
import com.bt.rsqe.enums.MoveConfigurationTypeEnum;
import com.bt.rsqe.expedio.project.ExpedioProjectResource;
import com.bt.rsqe.projectengine.web.EndOfLifeValidator;
import com.bt.rsqe.projectengine.web.facades.ProductIdentifierFacade;
import com.bt.rsqe.projectengine.web.facades.QuoteOptionFacade;
import com.bt.rsqe.projectengine.web.facades.SiteFacade;
import com.bt.rsqe.projectengine.web.uri.UriFactory;
import com.bt.rsqe.projectengine.web.view.ProductSitesDTO;
import com.bt.rsqe.projectengine.web.view.Products;
import com.bt.rsqe.projectengine.web.view.filtering.PaginatedFilter;
import com.bt.rsqe.projectengine.web.view.filtering.PaginatedFilterResult;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.bt.rsqe.utils.AssertObject.*;
import static com.google.common.collect.Lists.*;

public class MoveProductOrchestrator extends ProductOrchestrator<ProductSitesDTO> {
    private SiteDriver siteDriver;
    private EndOfLifeValidator endOfLifeValidator;

    public MoveProductOrchestrator(SiteFacade siteFacade, ProductIdentifierFacade productIdentifierFacade, UriFactory productConfiguratorUriFactory, QuoteOptionFacade quoteOptionFacade,
                                   ExpedioProjectResource projectResource, Pmr pmr, SiteDriver siteDriver, ExpedioClientResources expedioClientResources, ProductInstanceClient productInstanceClient) {
        super(siteFacade, productIdentifierFacade, productConfiguratorUriFactory, quoteOptionFacade, projectResource, pmr, expedioClientResources, productInstanceClient);
        this.siteDriver = siteDriver;
        this.endOfLifeValidator = new EndOfLifeValidator(productInstanceClient,pmr);
    }

    @Override
    public ProductSitesDTO buildSitesView(String customerId, String projectId, PaginatedFilter paginatedFilter, String forProduct, String newSiteId, List<String> existingSiteIds, Optional<String> productVersion) {
        if (newSiteId == null) {
            List<SiteDTO> siteDTOs = siteFacade.getAllBranchSites(customerId, projectId);

            if (isNotNull(forProduct)) {
                siteDTOs = filterSites(siteDTOs, siteDriver.get(customerId, forProduct));
            }

            final PaginatedFilterResult<SiteDTO> filterResult = paginatedFilter.applyTo(siteDTOs);
            boolean isSpecialBidProduct = productIdentifierFacade.isProductSpecialBid(forProduct);

            List<String> supportedStandardCountries = forProduct == null ? Lists.<String>newArrayList() : pmr.getSupportedCountries(forProduct);
            List<String> supportedSpecialCountries = forProduct == null ? Lists.<String>newArrayList() : pmr.getCountriesWithSpecialBidPricingType(forProduct);
            return new ProductSitesDTO(filterResult, supportedStandardCountries, supportedSpecialCountries, isSpecialBidProduct, null, false, Optional.<Map<AssetDTO,SiteDTO>>absent());
        } else {
           return buildSitesViewWithNewSiteDetails(customerId, projectId, paginatedFilter, forProduct, existingSiteIds, newSiteId);
        }
    }

    private ProductSitesDTO buildSitesViewWithNewSiteDetails(String customerId, String projectId, PaginatedFilter paginatedFilter, String forProduct, List<String> existingSiteIds, String newSiteId) {
        List<SiteDTO> siteDTOs = siteFacade.getAllBranchSites(customerId, projectId);

        SiteDTO newSiteDTO = findSiteDTOForNewSiteId(siteDTOs, newSiteId);
        List<SiteDTO> existingSiteDTOs = buildDTOsFromExistingSiteIds(existingSiteIds, siteDTOs);

        if (isNotNull(forProduct)) {
            existingSiteDTOs = filterSites(existingSiteDTOs, siteDriver.get(customerId, forProduct));
        }

        final PaginatedFilterResult<SiteDTO> filterResult = paginatedFilter.applyTo(existingSiteDTOs);
        boolean isSpecialBidProduct = productIdentifierFacade.isProductSpecialBid(forProduct);
        List<String> supportedStandardCountries = forProduct == null ? Lists.<String>newArrayList() : pmr.getSupportedCountries(forProduct);
        List<String> supportedSpecialCountries = forProduct == null ? Lists.<String>newArrayList() : pmr.getCountriesWithSpecialBidPricingType(forProduct);
        return new ProductSitesDTO(filterResult, supportedStandardCountries, supportedSpecialCountries, isSpecialBidProduct, newSiteDTO, newSiteId.equals(SAME_SITE), Optional.<Map<AssetDTO,SiteDTO>>absent());
    }


    private SiteDTO findSiteDTOForNewSiteId(List<SiteDTO> siteDTOs, final String newSiteId) {
        if (SAME_SITE.equals(newSiteId)) {
            return null;
        } else {
            return Iterables.find(siteDTOs, new Predicate<SiteDTO>() {
                @Override
                public boolean apply(SiteDTO input) {
                    return input.getSiteId().getValue().equals(new Long(newSiteId));
                }
            });
        }
    }

    private List<SiteDTO> buildDTOsFromExistingSiteIds(List<String> existingSiteIds, List<SiteDTO> siteDTOs) {
        List<SiteDTO> existingSiteDTOs = newArrayList();
        for (final String existingSiteId : existingSiteIds) {
            SiteDTO existingSiteDTO = Iterables.find(siteDTOs, new Predicate<SiteDTO>() {
                @Override
                public boolean apply(SiteDTO input) {
                    return input.getSiteId().getValue().equals(new Long(existingSiteId));
                }
            });
            existingSiteDTOs.add(existingSiteDTO);
        }
        return existingSiteDTOs;
    }

    @Override
    protected Products getProducts(String customerId, String productAction, String contractId) {
        Products products = super.getProducts(customerId, productAction, contractId);
        products = excludeNonMoveableProducts(products);
        return products;
    }

    private Products excludeNonMoveableProducts(Products products) {
        List<SellableProduct> sellableProducts = new ArrayList<SellableProduct>();
        for (SellableProduct product : products.sellableProducts()) {
            if (product.getMoveConfigurationType().equals(MoveConfigurationTypeEnum.ROOT_ONLY_COPY) ||
                product.getMoveConfigurationType().equals(MoveConfigurationTypeEnum.COPY_ALL)) {
                sellableProducts.add(product);
            }
        }
        return new Products(sellableProducts);
    }

    public Notification endOfLifeCheck(String siteId, String productCode, String productVersion, Date systemDate, String contractResignStatus, String lineItemId) {
        return endOfLifeValidator.endOfLifeCheck(siteId, productCode, productVersion, systemDate,lineItemId );
    }
}
