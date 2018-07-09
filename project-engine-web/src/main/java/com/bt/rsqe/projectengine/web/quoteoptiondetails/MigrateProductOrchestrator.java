package com.bt.rsqe.projectengine.web.quoteoptiondetails;

import com.bt.rsqe.client.Pmr;
import com.bt.rsqe.customerinventory.driver.SiteDriver;
import com.bt.rsqe.customerinventory.dto.AssetDTO;
import com.bt.rsqe.customerrecord.ExpedioClientResources;
import com.bt.rsqe.customerrecord.SiteDTO;
import com.bt.rsqe.domain.Notification;
import com.bt.rsqe.domain.product.ProductCategory;
import com.bt.rsqe.domain.product.SellableProduct;
import com.bt.rsqe.expedio.project.ExpedioProjectResource;
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

import javax.annotation.Nullable;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.*;

public class MigrateProductOrchestrator extends ProductOrchestrator<ProductSitesDTO> {

    public MigrateProductOrchestrator(SiteFacade siteFacade, ProductIdentifierFacade productIdentifierFacade, UriFactory productConfiguratorUriFactory, QuoteOptionFacade quoteOptionFacade, ExpedioProjectResource projectResource, Pmr pmr, SiteDriver siteDriver, ExpedioClientResources expedioClientResources) {
        super(siteFacade, productIdentifierFacade, productConfiguratorUriFactory, quoteOptionFacade, projectResource, pmr, expedioClientResources, null);
    }

    @Override
    public ProductSitesDTO buildSitesView(String customerId, String projectId, PaginatedFilter paginatedFilter, String forProduct, String newSiteId, List<String> existingSiteIds, Optional<String> productVersion) {
        final List<SiteDTO> siteDTOs = siteFacade.getAllBranchSites(customerId, projectId);
        final PaginatedFilterResult<SiteDTO> filterResult = paginatedFilter.applyTo(siteDTOs);
        boolean isSpecialBidProduct = productIdentifierFacade.isProductSpecialBid(forProduct);

        List<String> supportedStandardCountries = forProduct == null ? Lists.<String>newArrayList() : pmr.getSupportedCountries(forProduct);
        List<String> supportedSpecialCountries = forProduct == null ? Lists.<String>newArrayList() : pmr.getCountriesWithSpecialBidPricingType(forProduct);
        return new ProductSitesDTO(filterResult, supportedStandardCountries, supportedSpecialCountries, isSpecialBidProduct, null, false, Optional.<Map<AssetDTO,SiteDTO>>absent());
    }

    @Override
    protected Products getProducts(String customerId, String productAction, String contractId) {
        Products products = super.getProducts(customerId, productAction, contractId);
        products = filterMigratableProducts(products);
        return products;
    }

    private Products filterMigratableProducts(Products products) {
        final List<ProductCategory> categories = pmr.getProductCategories();

        List<SellableProduct> migratableProducts = newArrayList(Iterables.filter(products.sellableProducts(), new Predicate<SellableProduct>() {
            @Override
            public boolean apply(@Nullable SellableProduct input) {
                return findProductCategory(input.getProductCategory().getProductId(), categories).hasMigrationFlags();
            }
        }));

        return new Products(migratableProducts);
    }

    private ProductCategory findProductCategory(final String hCode, List<ProductCategory> categories) {
        return Iterables.find(categories, new Predicate<ProductCategory>() {
            @Override
            public boolean apply(@Nullable ProductCategory input) {
                return hCode.equals(input.getProductIdentifier().getProductId());
            }
        });
    }

    public Notification endOfLifeCheck(String siteId, String productCode, String productVersion, Date systemDate, String contractResignStatus, String lineItemId) {
        return null;
    }
}
