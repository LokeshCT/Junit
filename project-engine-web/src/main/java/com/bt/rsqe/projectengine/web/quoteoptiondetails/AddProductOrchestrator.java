package com.bt.rsqe.projectengine.web.quoteoptiondetails;

import com.bt.rsqe.client.Pmr;
import com.bt.rsqe.customerinventory.client.ProductInstanceClient;
import com.bt.rsqe.customerinventory.dto.AssetDTO;
import com.bt.rsqe.customerrecord.ExpedioClientResources;
import com.bt.rsqe.customerrecord.SiteDTO;
import com.bt.rsqe.domain.Notification;
import com.bt.rsqe.domain.product.SellableProduct;
import com.bt.rsqe.expedio.project.ExpedioProjectResource;
import com.bt.rsqe.projectengine.web.facades.ProductIdentifierFacade;
import com.bt.rsqe.projectengine.web.facades.QuoteOptionFacade;
import com.bt.rsqe.projectengine.web.facades.SiteFacade;
import com.bt.rsqe.projectengine.web.quoteoption.validation.SiteValidator;
import com.bt.rsqe.projectengine.web.uri.UriFactory;
import com.bt.rsqe.projectengine.web.view.ProductSitesDTO;
import com.bt.rsqe.projectengine.web.view.Products;
import com.bt.rsqe.projectengine.web.view.filtering.PaginatedFilter;
import com.bt.rsqe.projectengine.web.view.filtering.PaginatedFilterResult;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.bt.rsqe.utils.AssertObject.*;
import static com.google.common.collect.Lists.*;

public class AddProductOrchestrator extends ProductOrchestrator<ProductSitesDTO> {
    private final SiteValidator siteValidator;

    public AddProductOrchestrator(SiteFacade siteFacade,
                                  ProductIdentifierFacade productIdentifierFacade,
                                  UriFactory productConfiguratorUriFactory,
                                  QuoteOptionFacade quoteOptionFacade,
                                  ExpedioProjectResource projectResource,
                                  Pmr pmr,
                                  ExpedioClientResources expedioClientResources,
                                  ProductInstanceClient productInstanceClient,
                                  SiteValidator siteValidator) {
        super(siteFacade,
              productIdentifierFacade,
              productConfiguratorUriFactory,
              quoteOptionFacade,
              projectResource,
              pmr,
              expedioClientResources,
              productInstanceClient);
        this.siteValidator = siteValidator;
    }

    @Override
    public ProductSitesDTO buildSitesView(String customerId, String projectId, PaginatedFilter paginatedFilter, String forProduct, String newSiteId, List<String> existingSiteIds, Optional<String> productVersion) {
        final List<SiteDTO> siteDTOs = siteFacade.getAllBranchSites(customerId, projectId);
        final PaginatedFilterResult<SiteDTO> filterResult = paginatedFilter.applyTo(siteDTOs);
        boolean isSpecialBidProduct = productIdentifierFacade.isProductSpecialBid(forProduct);

        List<String> supportedStandardCountries = forProduct == null ? Lists.<String>newArrayList() : pmr.getSupportedCountries(forProduct);
        List<String> supportedSpecialCountries = forProduct == null ? Lists.<String>newArrayList() : pmr.getCountriesWithSpecialBidPricingType(forProduct);

        for (SiteDTO siteDTO : filterResult.getItems()) {
            siteDTO.partialSite = !siteValidator.validateSite(siteDTO).isEmpty();
        }
        return new ProductSitesDTO(filterResult, supportedStandardCountries, supportedSpecialCountries, isSpecialBidProduct, null, false, Optional.<Map<AssetDTO,SiteDTO>>absent());
    }

    @Override
    protected Products getProducts(String customerId, String productAction, String contractId) {
        Products products = super.getProducts(customerId, productAction, contractId);
        products = applyEndOfFilters(products);
        return products;
    }

    private Products applyEndOfFilters(Products products) {
        List<SellableProduct> addableProducts = newArrayList(Iterables.filter(products.sellableProducts(), new Predicate<SellableProduct>() {
            @Override
            public boolean apply(SellableProduct input) {
                return isAddableProduct(input);
            }
        }));
        return new Products(addableProducts);
    }

    private boolean isAddableProduct(SellableProduct sellableProduct) {
        Date today = Calendar.getInstance().getTime();
        if(isNotNull(sellableProduct.getQuotableStartDate()) && today.before(sellableProduct.getQuotableStartDate())){
            return false;
        }
        if(isNotNull(sellableProduct.getQuoteEndDate()) && today.after(sellableProduct.getQuoteEndDate())){
            return false;
        }
        if(isNotNull(sellableProduct.getSaleEndDate()) && today.after(sellableProduct.getSaleEndDate())){
            return false;
        }
        if(isNotNull(sellableProduct.getEffectiveEndDate()) && today.after(sellableProduct.getEffectiveEndDate())){
            return false;
        }
        return true;
    }

    public Notification endOfLifeCheck(String siteId, String productCode, String productVersion, Date systemDate, String contractResignStatus, String lineItemId) {
        return null;
    }
}
