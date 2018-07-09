package com.bt.rsqe.projectengine.web.quoteoptiondetails;

import com.bt.rsqe.MBeanTools;
import com.bt.rsqe.client.Pmr;
import com.bt.rsqe.customerinventory.client.ProductInstanceClient;
import com.bt.rsqe.customerinventory.filter.AssetFilter;
import com.bt.rsqe.customerinventory.parameter.ContractId;
import com.bt.rsqe.customerinventory.parameter.CustomerId;
import com.bt.rsqe.customerinventory.parameter.ProductCode;
import com.bt.rsqe.customerinventory.parameter.ProductVersion;
import com.bt.rsqe.customerinventory.parameter.SiteId;
import com.bt.rsqe.customerrecord.ExpedioClientResources;
import com.bt.rsqe.customerrecord.SiteDTO;
import com.bt.rsqe.domain.AvailableAsset;
import com.bt.rsqe.domain.ErrorNotificationEvent;
import com.bt.rsqe.domain.Notification;
import com.bt.rsqe.domain.bom.parameters.ProductSCode;
import com.bt.rsqe.domain.product.Cardinality;
import com.bt.rsqe.domain.product.ProductOffering;
import com.bt.rsqe.domain.product.SellableProduct;
import com.bt.rsqe.dto.BFGAssetIdentifier;
import com.bt.rsqe.enums.ProductAction;
import com.bt.rsqe.expedio.project.ExpedioProjectResource;
import com.bt.rsqe.expedio.project.ProjectDTO;
import com.bt.rsqe.projectengine.QuoteOptionDTO;
import com.bt.rsqe.projectengine.QuoteOptionItemDTO;
import com.bt.rsqe.projectengine.web.CreateLineItemDTO;
import com.bt.rsqe.projectengine.web.facades.ProductIdentifierFacade;
import com.bt.rsqe.projectengine.web.facades.QuoteOptionFacade;
import com.bt.rsqe.projectengine.web.facades.SiteFacade;
import com.bt.rsqe.projectengine.web.uri.UriFactory;
import com.bt.rsqe.projectengine.web.view.AddOrModifyProductView;
import com.bt.rsqe.projectengine.web.view.ProductServiceDTO;
import com.bt.rsqe.projectengine.web.view.Products;
import com.bt.rsqe.projectengine.web.view.filtering.PaginatedFilter;
import com.bt.rsqe.projectengine.web.view.pagination.Pagination;
import com.bt.rsqe.security.UserContextManager;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.apache.commons.collections.ListUtils;

import javax.annotation.Nullable;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.bt.rsqe.enums.ProductCodes.*;
import static com.google.common.collect.Lists.*;
import static com.google.common.collect.Sets.*;
import static com.google.common.collect.Maps.*;

import static java.lang.String.*;
import static org.apache.commons.lang.StringUtils.*;

public abstract class ProductOrchestrator<T> implements ProductOrchestratorMBean {
    protected SiteFacade siteFacade;
    protected ProductIdentifierFacade productIdentifierFacade;
    protected ProductInstanceClient productInstanceClient;
    private UriFactory productConfiguratorUriFactory;
    private QuoteOptionFacade quoteOptionFacade;
    private ExpedioProjectResource projectResource;
    protected Pmr pmr;
    protected ExpedioClientResources expedioClientResources;
    protected boolean singleSiteEnabled = false;
    protected String siteId = "";
    protected static Map<String, Boolean> MODIFYABLE_PRODUCT_CACHE = newConcurrentMap();

    public static final String SAME_SITE = "sameSite";

    protected ProductOrchestrator(SiteFacade siteFacade,
                                  ProductIdentifierFacade productIdentifierFacade,
                                  UriFactory productConfiguratorUriFactory,
                                  QuoteOptionFacade quoteOptionFacade,
                                  ExpedioProjectResource projectResource,
                                  Pmr pmr,
                                  ExpedioClientResources expedioClientResources,
                                  ProductInstanceClient productInstanceClient) {
        this.siteFacade = siteFacade;
        this.productIdentifierFacade = productIdentifierFacade;
        this.productConfiguratorUriFactory = productConfiguratorUriFactory;
        this.quoteOptionFacade = quoteOptionFacade;
        this.projectResource = projectResource;
        this.pmr = pmr;
        this.expedioClientResources = expedioClientResources;
        this.productInstanceClient = productInstanceClient;

        MBeanTools.registerMBeanForClass(this);
    }

    public AddOrModifyProductView buildView(String customerId, String contractId, String projectId, String quoteOptionId, boolean indirectUser, String productAction) {
        List<String> countries = siteFacade.getCountries(customerId, projectId);
        final QuoteOptionDTO quoteOptionDTO = quoteOptionFacade.get(projectId, quoteOptionId);
        String quoteOptionItemsSize = Integer.toString(getFrontCatalogueProducts(quoteOptionFacade.getAllQuoteOptionItem(projectId, quoteOptionId)).size());
        final ProjectDTO project = projectResource.getProject(projectId);
        final AddOrModifyProductView view = new AddOrModifyProductView(customerId,
                                                                       contractId, projectId,
                                                                       quoteOptionId,
                                                                       countries,
                                                                       productConfiguratorUriFactory,
                                                                       quoteOptionDTO.currency,
                                                                       quoteOptionDTO.name,
                                                                       project.organisation.name,
                                                                       indirectUser,
                                                                       productAction,
                                                                       quoteOptionItemsSize,
                                                                       project.orderType,
                                                                       Strings.nullToEmpty(project.subOrderType));

        view.setProducts(getProducts(customerId, productAction, contractId));

        return view;
    }

    private List<QuoteOptionItemDTO> getFrontCatalogueProducts(List<QuoteOptionItemDTO> quoteOptionItemDTOs) {
        List<QuoteOptionItemDTO> frontCatalogueItems = newArrayList();
        for(QuoteOptionItemDTO quoteOptionDTO : quoteOptionItemDTOs) {
            if (pmr.productOffering(ProductSCode.newInstance(quoteOptionDTO.sCode)).get().isInFrontCatalogue()) {
                frontCatalogueItems.add(quoteOptionDTO);
            }
        }
        return frontCatalogueItems;
    }

    public abstract T buildSitesView(String customerId, String projectId, PaginatedFilter paginatedFilter, String forProduct, String newSiteId, List<String> existingSiteIds, Optional<String> productVersion);

    public ProductServiceDTO buildServicesView(String customerId, String sCode, String contractId, String productVersion, Pagination pagination) {
        return null; // do nothing only applies for modify
    }

    protected Products getProducts(String customerId, String productAction, String contractId) {
        String salesChannel = expedioClientResources.getCustomerResource().getByToken(customerId, UserContextManager.getCurrent().getRsqeToken()).getSalesChannel();
        Products products = productIdentifierFacade.getSellableProductsForSalesChannel(salesChannel);

        //check contract is complex customer i.e. BFG_CONTRACTS.CON_MANGED_SOLUTION_FLAG is "Y"
        boolean isComplexContractProduct = false;
        /*Check for  contract complex customer check is commented since RCode file was not delivered from PMF
          once it is delivered uncomment check for Customer based lookup availablity
         */
        //boolean isComplexContractProduct = productIdentifierFacade.isComplexContractCustomer(Long.parseLong(contractId));
        if(isComplexContractProduct){
            //get filter product categories from customer based product matrix lookup
            products = productIdentifierFacade.getSellableProductsForCustomerBased(products, customerId);
        }

        return filterProductsBasedOnIsSeparatelyModifiable(products, productAction);
    }

    private Products filterProductsBasedOnIsSeparatelyModifiable(Products products, final String productAction) {
        List<SellableProduct> addableProducts = newArrayList(Iterables.filter(products.sellableProducts(), new Predicate<SellableProduct>() {
            @Override
            public boolean apply(SellableProduct input) {
                boolean isModifiable = isSeparatelyModifyable(input);
                return !isModifiable || (isModifiable && ProductAction.Modify.description().equals(productAction));
            }
        }));
        return new Products(addableProducts);
    }

    private boolean isSeparatelyModifyable(SellableProduct input) {

        final String productId = input.getProductId();
        if (MODIFYABLE_PRODUCT_CACHE.containsKey(productId)) {
            return MODIFYABLE_PRODUCT_CACHE.get(productId);
        }

        final Pmr.ProductOfferings productOfferings = pmr.productOffering(ProductSCode.newInstance(productId));

        final boolean separatelyModifiable = productOfferings.get().isSeparatelyModifiable();
        MODIFYABLE_PRODUCT_CACHE.put(productId, separatelyModifiable);

        return separatelyModifiable;
    }

    protected CreateLineItemDTO constructLineItem(String rsqeQuoteOptionId, String expedioQuoteId, String expedioCustomerId, String authenToken, List<String> siteIds) {

        List<CreateLineItemDTO.ActionDTO> actionDTOs = Lists.transform(siteIds, new Function<String, CreateLineItemDTO.ActionDTO>() {
            @Override
            public CreateLineItemDTO.ActionDTO apply(@Nullable String siteId) {
                return new CreateLineItemDTO.ActionDTO(siteId, "Provide");
            }
        });

        return new CreateLineItemDTO(rsqeQuoteOptionId, expedioQuoteId, expedioCustomerId, authenToken, actionDTOs);
    }

    public Notification contractCardinalityCheck(String customerId, String contractId, String sCode, String productVersion, String quoteOptionId, int numberOfProducts) {
        Notification notification = new Notification();
        ProductOffering productOffering = pmr.productOffering(ProductSCode.newInstance(sCode)).get();

        Cardinality contractCardinality = productOffering.getContractCardinality();
        int maxAllowableCardinality = contractCardinality.isDynamicCardinality() ? contractCardinality.getMaxCardinalityByExpression(customerId) : contractCardinality.getMax() ;


        // max cardinality is unbounded so no need to check anything here...
        if(cardinalityIsUnbounded(maxAllowableCardinality)) {
            return notification;
        }

        // Get all 'approved' contract assets as well as any DRAFT assets on the current quote option
        List<AvailableAsset> contractAssets = productInstanceClient.getContractAssets(new CustomerId(customerId),
                new ContractId(contractId),
                new ProductCode(sCode),
                new ProductVersion(productVersion),
                AssetFilter.approvedAssetsFilter(),
                AssetFilter.draftAssetsForQuoteOptionFilter(quoteOptionId));

        if((contractAssets.size() + numberOfProducts) > maxAllowableCardinality ) {
            notification.addEvent(new ErrorNotificationEvent(format("%s can have only %s instance(s) for the Customer %s",
                                                                    productOffering.getProductIdentifier().getProductName(), maxAllowableCardinality,
                                                                    expedioClientResources.getCustomerResource().getByToken(customerId, UserContextManager.getCurrent().getRsqeToken()).getName())));
        }

        return notification;
    }

    public Notification siteCardinalityCheck(String projectId, String customerId, String stringSiteId, String sCode, String productVersion, String quoteOptionId) {
        Notification notification = new Notification();
        Cardinality siteCardinality = pmr.productOffering(ProductSCode.newInstance(sCode)).get().getSiteCardinality();
        int maxAllowableCardinality = siteCardinality.isDynamicCardinality() ? siteCardinality.getMaxCardinalityByExpression(customerId) : siteCardinality.getMax() ;


        // max cardinality is unbounded so no need to check anything here...
        if(cardinalityIsUnbounded(maxAllowableCardinality)) {
            return notification;
        }

        SiteId siteId = isEmpty(stringSiteId) ? getCentralSite(customerId, projectId) : new SiteId(stringSiteId);

        List<AvailableAsset> availableProducts = productInstanceClient.getApprovedAssets(siteId, new ProductCode(sCode), new ProductVersion(productVersion));
        List<AvailableAsset> draftProducts = productInstanceClient.getDraftAssets(siteId, new ProductCode(sCode), new ProductVersion(productVersion), quoteOptionId);

        return getNotification(ListUtils.union(availableProducts, draftProducts), maxAllowableCardinality, sCode, customerId, stringSiteId, projectId, notification);
    }

    private Notification getNotification(List<AvailableAsset> availableProducts, int maxAllowableCardinality, String sCode, String customerId, String stringSiteId, String projectId, Notification notification) {
        if (availableProducts.size() >= maxAllowableCardinality) {
            notification.addEvent(new ErrorNotificationEvent(format("%s already exist under Site %s",
                                                                    getByScode(sCode).productName(),
                                                                    expedioClientResources.getCustomerResource().siteResource(customerId).get(stringSiteId, projectId).name)));
        }

        return notification;
    }

    private boolean cardinalityIsUnbounded(int cardinality) {
        return cardinality == Integer.MAX_VALUE;
    }

    public String getLaunched(String salesChannel, String SCode) {
        String launched = pmr.getLaunched(salesChannel, SCode);
        return launched;
    }

    private SiteId getCentralSite(String customerId, String projectId) {
        return new SiteId(siteFacade.getCentralSite(customerId, projectId).bfgSiteID);
    }

    private Set<String> getActiveSites(final List<BFGAssetIdentifier> assetIdentifiers) {
        return newHashSet(Iterables.transform(assetIdentifiers, new Function<BFGAssetIdentifier, String>() {
            @Override
            public String apply(@Nullable BFGAssetIdentifier input) {
                return input.getSiteId();
            }
        }));
    }

    protected List<SiteDTO> filterSites(final List<SiteDTO> siteDTOs,
                                        final List<BFGAssetIdentifier> assetIdentifiers) {
        final Set<String> productActiveSites = getActiveSites(assetIdentifiers);

        return newArrayList(Iterables.filter(siteDTOs, new Predicate<SiteDTO>() {
            @Override
            public boolean apply(SiteDTO siteDTO) {
                return productActiveSites.contains(siteDTO.bfgSiteID);
            }
        }));
    }

    public abstract Notification endOfLifeCheck(String siteId, String productCode, String productVersion, Date systemDate, String contractResignStatus, String lineItemId);

    public Notification checkProductAvailability(String projectId, String quoteOptionId, String productCode) {
        Notification notification = new Notification();
        List<QuoteOptionItemDTO> quoteOptionItems = quoteOptionFacade.getAllQuoteOptionItem(projectId, quoteOptionId);
        for(QuoteOptionItemDTO quoteOptionItemDTO : quoteOptionItems){
            if(quoteOptionItemDTO.getProductCode().equalsIgnoreCase(productCode)){
                return notification;
            }
        }
        notification.addEvent(new ErrorNotificationEvent("Selected product does not exist in the quote"));
        return notification;
    }

    @Override
    public void modifyFor(boolean singleSiteEnabled, String siteId) {
        this.singleSiteEnabled = singleSiteEnabled;
        this.siteId = siteId;
    }

}
