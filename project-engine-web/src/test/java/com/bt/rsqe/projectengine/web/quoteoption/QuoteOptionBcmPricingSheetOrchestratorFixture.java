package com.bt.rsqe.projectengine.web.quoteoption;

import com.bt.rsqe.client.Pmr.ProductOfferings;
import com.bt.rsqe.configuration.ConfigurationProvider;
import com.bt.rsqe.customerinventory.AsIsAssets;
import com.bt.rsqe.customerinventory.SpecialPriceBooks;
import com.bt.rsqe.customerinventory.ToBeAssets;
import com.bt.rsqe.customerinventory.client.CustomerInventoryProductInstanceStubClientManager;
import com.bt.rsqe.customerinventory.client.ProductInstanceClient;
import com.bt.rsqe.customerinventory.client.SpecialPriceBookClient;
import com.bt.rsqe.customerinventory.client.resource.ProductAgreementResourceClient;
import com.bt.rsqe.customerinventory.driver.CustomerInventoryStubDriverManager;
import com.bt.rsqe.customerinventory.dto.AssetDTO;
import com.bt.rsqe.customerinventory.dto.PriceLineDTO;
import com.bt.rsqe.customerinventory.dto.ProjectedUsageDTO;
import com.bt.rsqe.customerinventory.fixtures.AssetDTOFixture;
import com.bt.rsqe.customerinventory.parameter.LineItemId;
import com.bt.rsqe.customerinventory.parameter.ProductCode;
import com.bt.rsqe.customerinventory.parameter.SiteId;
import com.bt.rsqe.domain.SalesCatalogue;
import com.bt.rsqe.domain.bom.parameters.ProductSCode;
import com.bt.rsqe.domain.product.parameters.ProductIdentifier;
import com.bt.rsqe.domain.project.SpecialPriceBook;
import com.bt.rsqe.expedio.project.ExpedioProjectResource;
import com.bt.rsqe.pmr.client.PmrClient;
import com.bt.rsqe.pmr.client.PmrLookupClient;
import com.bt.rsqe.pricing.PricingClient;
import com.bt.rsqe.pricing.config.dto.PricingConfig;
import com.bt.rsqe.projectengine.QuoteOptionItemDTO;
import com.bt.rsqe.projectengine.server.ProjectEngineWebConfig;
import com.bt.rsqe.projectengine.web.facades.FutureAssetPricesFacade;
import com.bt.rsqe.projectengine.web.facades.LineItemFacade;
import com.bt.rsqe.projectengine.web.facades.ProductIdentifierFacade;
import com.bt.rsqe.projectengine.web.facades.SiteFacade;
import com.bt.rsqe.projectengine.web.model.DiscountUpdater;
import com.bt.rsqe.projectengine.web.model.LineItemModel;
import com.bt.rsqe.projectengine.web.model.modelfactory.FutureAssetPricesModelFactoryImpl;
import com.bt.rsqe.projectengine.web.model.modelfactory.LineItemModelFactoryImpl;
import com.bt.rsqe.projectengine.web.model.modelfactory.ProjectedUsageModelFactory;
import com.bt.rsqe.projectengine.web.quoteoption.bcmsheet.HeaderRowModelFactoryTest;
import com.bt.rsqe.projectengine.web.quoteoption.priceupdater.FutureAssetPriceUpdaterFactory;
import com.bt.rsqe.projectengine.web.quoteoptionpricing.PriceSuppressStrategy;
import com.bt.rsqe.projectengine.web.resourcestubs.CustomerResourceStub;
import com.bt.rsqe.projectengine.web.resourcestubs.ProjectResourceStub;
import com.bt.rsqe.projectengine.web.uri.UriFactoryImpl;
import com.bt.rsqe.security.PermissionsDTO;
import com.bt.rsqe.security.UserContext;
import com.bt.rsqe.security.UserContextManager;
import com.bt.rsqe.security.UserPrincipal;
import com.bt.rsqe.utils.Environment;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import java.util.List;
import java.util.UUID;

import static com.bt.rsqe.enums.ProductCodes.*;
import static org.mockito.Mockito.*;

public class QuoteOptionBcmPricingSheetOrchestratorFixture {

    private LineItemFacade lineItemFacade;
    private final ProjectResourceStub projectResource = new ProjectResourceStub();
    private ToBeAssets toBeAssets = new ToBeAssets();
    private AsIsAssets asIsAssets = new AsIsAssets();
    private SpecialPriceBooks priceBooks = new SpecialPriceBooks();
    private SalesCatalogue salesCatalogue;
    private List<ProductIdentifier> productIdentifiers;
    private ProductOfferings caSiteProductOfferings;
    private ProductOfferings caServiceProductOfferings;
    private ProductOfferings specialBidProductOfferings;
    private HeaderRowModelFactoryTest headerRowModelFactoryTest;
    private PmrClient pmr;
    private PmrLookupClient pmrLookupClient = mock(PmrLookupClient.class);
    ProductInstanceClient productInstanceClient = mock(ProductInstanceClient.class);
    ProductAgreementResourceClient productAgreementResourceClient=mock(ProductAgreementResourceClient.class);
    private ProductOfferings onevoiceproductOfferings;
    private ProjectEngineWebConfig config = ConfigurationProvider.provide(ProjectEngineWebConfig.class, Environment.env());
    private PricingClient pricingClient;

    public QuoteOptionBcmPricingSheetOrchestrator build() throws Exception {
        final CustomerInventoryStubDriverManager inventory = new CustomerInventoryStubDriverManager(toBeAssets, asIsAssets, priceBooks);
        final CustomerResourceStub customers = new CustomerResourceStub();
        final SiteFacade siteFacade = new SiteFacade(customers);
        final ProductIdentifierFacade productIdentifierFacade = new ProductIdentifierFacade(pmr, productAgreementResourceClient, pmrLookupClient);
        final DiscountUpdater discountUpdater = mock(DiscountUpdater.class);
        ProjectedUsageModelFactory projectedUsageModelFactory = mock(ProjectedUsageModelFactory.class);
        when(projectedUsageModelFactory.create(any(ProjectedUsageDTO.class), any(String.class), any(String.class))).thenThrow(new UnsupportedOperationException("no mock for projected usages"));
        pricingClient = mock(PricingClient.class);
        when(pricingClient.getPricingConfig()).thenReturn(new PricingConfig());
        final FutureAssetPricesModelFactoryImpl futureAssetPricesModelFactory = new FutureAssetPricesModelFactoryImpl(productInstanceClient, siteFacade, productIdentifierFacade, projectedUsageModelFactory, discountUpdater, null, pricingClient, projectResource);

        ExpedioProjectResource expedioProjectResource = mock(ExpedioProjectResource.class);
        when(expedioProjectResource.getProject(any(String.class))).thenThrow(new UnsupportedOperationException("getProject has not been mocked"));

        FutureAssetPricesFacade futureAssetPricesFacade = new FutureAssetPricesFacade(inventory, futureAssetPricesModelFactory);

        final LineItemModelFactoryImpl lineItemModelFactory = new LineItemModelFactoryImpl(expedioProjectResource,
                                                                                           futureAssetPricesFacade,
                                                                                           productIdentifierFacade,
                                                                                           new UriFactoryImpl(config),
                                                                                           pmr,
                                                                                           productInstanceClient,
                                                                                           productAgreementResourceClient, null);

        final SpecialPriceBookClient specialPriceBookClient = new CustomerInventoryProductInstanceStubClientManager(toBeAssets, asIsAssets, priceBooks, pmr).getSpecialPriceBookClient();
        lineItemFacade = new LineItemFacade(projectResource, lineItemModelFactory);
        return new QuoteOptionBcmPricingSheetOrchestrator(futureAssetPricesFacade, lineItemFacade, specialPriceBookClient, new FutureAssetPriceUpdaterFactory(), pmr);
    }

    public void setExpectationsForPmr() {
        when(pmr.getSalesCatalogue()).thenReturn(salesCatalogue);
        when(salesCatalogue.getAllSellableProductIdentifiers()).thenReturn(productIdentifiers);
        when(pmr.productOffering(ProductSCode.newInstance("S0308454"))).thenReturn(caSiteProductOfferings);
        when(caSiteProductOfferings.get()).thenReturn(headerRowModelFactoryTest.getCASiteProductOffering());
        when(pmr.productOffering(ProductSCode.newInstance("S0308491"))).thenReturn(caServiceProductOfferings);
        when(caServiceProductOfferings.get()).thenReturn(headerRowModelFactoryTest.getCAServiceProductOffering());
        when(pmr.productOffering(ProductSCode.newInstance("specialScode"))).thenReturn(specialBidProductOfferings);
        when(specialBidProductOfferings.get()).thenReturn(headerRowModelFactoryTest.getSpecialBidProductOffering());
        when(pmr.productOffering(ProductSCode.newInstance(Onevoice.productCode()))).thenReturn(onevoiceproductOfferings);
        when(onevoiceproductOfferings.get()).thenReturn(headerRowModelFactoryTest.getOneVoiceProductOffering());
        when(pmr.getProductHCode("S0205086")).thenReturn(Optional.of(new ProductIdentifier("S0205086","Onevoice", "1.0","Onevoice")));
        when(pmr.getProductHCode("S0308491")).thenReturn(Optional.of(new ProductIdentifier("S0308491","Connect Acceleration Service", "1.0","CA")));
    }

    public QuoteOptionBcmPricingSheetOrchestratorFixture withOneVoiceItem(String projectId, String quoteOptionId, QuoteOptionItemDTO lineItem) {
        lineItem.sCode = Onevoice.productCode();
        projectResource.quoteOptionResource(projectId).quoteOptionItemResource(quoteOptionId).with(lineItem);
        return this;
    }

    public QuoteOptionBcmPricingSheetOrchestratorFixture withCAServiceItem(String projectId, String quoteOptionId, QuoteOptionItemDTO lineItem) {
        lineItem.sCode = ConnectAccelerationService.productCode();
        projectResource.quoteOptionResource(projectId).quoteOptionItemResource(quoteOptionId).with(lineItem);
        return this;
    }

    public QuoteOptionBcmPricingSheetOrchestratorFixture with(String lineItemId, String siteId, List<PriceLineDTO> prices) {
        final AssetDTO asset = new AssetDTOFixture()
            .withId(UUID.randomUUID())
            .withLineItemId(lineItemId)
            .withSiteId(new SiteId(siteId))
            .withAssetPrice(prices).withProductCode(new ProductCode(Onevoice.productCode())).build();
        toBeAssets.put(new ToBeAssets.Key(asset), asset);
        when(productInstanceClient.getAssetDTO(new LineItemId(lineItemId))).thenReturn(asset);
        return this;
    }


    public LineItemModel fetchLineItem(String customerId, String contractId, String projectId, String quoteOptionId, final String lineItemId) {
        return Iterables.find(lineItemFacade.fetchLineItems(customerId, contractId, projectId, quoteOptionId, PriceSuppressStrategy.None), new Predicate<LineItemModel>() {
            @Override
            public boolean apply(LineItemModel input) {
                return lineItemId.equals(input.getId());
            }
        });
    }

    public QuoteOptionBcmPricingSheetOrchestratorFixture with(SpecialPriceBook specialPriceBook) {
        priceBooks.add(specialPriceBook);
        return this;
    }

    public SpecialPriceBooks priceBooks() {
        return priceBooks;
    }

    public void forIndirectChannel() {
        setChannel(true);
    }

    public void forDirectChannel() {
        setChannel(false);
    }

    private void setChannel(boolean indirect) {
        final PermissionsDTO permissions = new PermissionsDTO(false, true, indirect, indirect, true, false);
        final UserContext newContext = new UserContext(new UserPrincipal("login"), "token", permissions);
        UserContextManager.setCurrent(newContext);
    }

    public void setPmr(PmrClient pmr) {
        this.pmr = pmr;
    }

    public QuoteOptionBcmPricingSheetOrchestratorFixture withSalesCatalogue(SalesCatalogue salesCatalogue) {
        this.salesCatalogue = salesCatalogue;
        return this;
    }

    public QuoteOptionBcmPricingSheetOrchestratorFixture withProductIdentifiers(List<ProductIdentifier> productIdentifiers) {
        this.productIdentifiers = productIdentifiers;
        return this;
    }

    public QuoteOptionBcmPricingSheetOrchestratorFixture withCaSiteProductOfferings(ProductOfferings caSiteProductOfferings) {
        this.caSiteProductOfferings = caSiteProductOfferings;
        return this;
    }

    public QuoteOptionBcmPricingSheetOrchestratorFixture withCaServiceProductOfferings(ProductOfferings caServiceProductOfferings) {
        this.caServiceProductOfferings = caServiceProductOfferings;
        return this;
    }

    public QuoteOptionBcmPricingSheetOrchestratorFixture withSpecialBidProductOfferings(ProductOfferings specialBidProductOfferings) {
        this.specialBidProductOfferings = specialBidProductOfferings;
        return this;
    }

    public QuoteOptionBcmPricingSheetOrchestratorFixture withHeaderRowModelFactoryTest(HeaderRowModelFactoryTest headerRowModelFactoryTest) {
        this.headerRowModelFactoryTest = headerRowModelFactoryTest;
        return this;
    }

    public QuoteOptionBcmPricingSheetOrchestratorFixture withOneVoiceProductOfferings(ProductOfferings onevoiceproductOfferings) {
        this.onevoiceproductOfferings = onevoiceproductOfferings;
        return this;
    }

    public QuoteOptionBcmPricingSheetOrchestratorFixture withPmr(PmrClient pmr) {
        this.pmr = pmr;
        return this;
    }

}
