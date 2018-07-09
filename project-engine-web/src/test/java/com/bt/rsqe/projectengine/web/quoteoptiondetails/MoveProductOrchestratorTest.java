package com.bt.rsqe.projectengine.web.quoteoptiondetails;

import com.bt.rsqe.client.Pmr;
import com.bt.rsqe.container.ApplicationConfig;
import com.bt.rsqe.customerinventory.client.ProductInstanceClient;
import com.bt.rsqe.customerinventory.driver.SiteDriver;
import com.bt.rsqe.customerinventory.parameter.ProductCode;
import com.bt.rsqe.customerinventory.parameter.ProductVersion;
import com.bt.rsqe.customerinventory.parameter.SiteId;
import com.bt.rsqe.customerrecord.CustomerDTO;
import com.bt.rsqe.customerrecord.CustomerResource;
import com.bt.rsqe.customerrecord.ExpedioClientResources;
import com.bt.rsqe.customerrecord.SiteDTO;
import com.bt.rsqe.domain.Notification;
import com.bt.rsqe.domain.StencilCode;
import com.bt.rsqe.domain.StencilId;
import com.bt.rsqe.domain.StencilVersion;
import com.bt.rsqe.domain.bom.fixtures.ProductOfferingFixture;
import com.bt.rsqe.domain.bom.fixtures.SellableProductFixture;
import com.bt.rsqe.domain.bom.parameters.ProductSCode;
import com.bt.rsqe.domain.product.DefaultProductInstanceFixture;
import com.bt.rsqe.domain.product.ProductOffering;
import com.bt.rsqe.domain.product.SellableProduct;
import com.bt.rsqe.domain.product.SimpleProductOfferingType;
import com.bt.rsqe.domain.product.parameters.ProductIdentifier;
import com.bt.rsqe.domain.project.PriceLine;
import com.bt.rsqe.domain.project.ProductInstance;
import com.bt.rsqe.dto.BFGAssetIdentifier;
import com.bt.rsqe.enums.MoveConfigurationTypeEnum;
import com.bt.rsqe.enums.ProductAction;
import com.bt.rsqe.expedio.fixtures.CustomerDTOFixture;
import com.bt.rsqe.expedio.project.ExpedioProjectResource;
import com.bt.rsqe.fixtures.CalendarFixture;
import com.bt.rsqe.integration.PriceLineFixture;
import com.bt.rsqe.pmr.dto.SalesChannelDTO;
import com.bt.rsqe.projectengine.web.facades.ProductIdentifierFacade;
import com.bt.rsqe.projectengine.web.facades.QuoteOptionFacade;
import com.bt.rsqe.projectengine.web.facades.SiteFacade;
import com.bt.rsqe.projectengine.web.uri.UriFactory;
import com.bt.rsqe.projectengine.web.uri.UriFactoryImpl;
import com.bt.rsqe.projectengine.web.view.ProductSitesDTO;
import com.bt.rsqe.projectengine.web.view.Products;
import com.bt.rsqe.projectengine.web.view.SiteView;
import com.bt.rsqe.projectengine.web.view.filtering.PaginatedFilter;
import com.bt.rsqe.projectengine.web.view.filtering.PaginatedFilterResult;
import com.bt.rsqe.security.UserContext;
import com.bt.rsqe.security.UserContextManager;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.bt.rsqe.security.UserContextBuilder.*;
import static com.google.common.collect.Lists.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class MoveProductOrchestratorTest {

    public static final String CUSTOMER_ID = "customerId";
    public static final String CONTRACT_ID = "contractId";
    public static final String PROJECT_ID = "projectId";
    public static final String QUOTE_OPTION_ID = "quoteOptionId";
    private static final String SALES_CHANNEL = "aSalesChannel";
    private static final String SITE_ID = "siteId";
    private static final String PRODUCT_CODE = "productCode";
    private static final String PRODUCT_VERSION = "productVersion";
    private JUnit4Mockery context;
    private SiteFacade siteFacade;
    private MoveProductOrchestrator moveProductOrchestrator;
    private ProductIdentifierFacade productIdentifierFacade;
    private PaginatedFilter<SiteDTO> paginatedFilter;
    private PaginatedFilterResult<SiteDTO> filterResult;
    private UriFactory productConfiguratorUriFactory = new UriFactoryImpl(null);
    private QuoteOptionFacade quoteOptionFacade;
    private ExpedioProjectResource projectResource;
    private ApplicationConfig configuration;
    private SiteDriver siteDriver;
    private Pmr pmr;
    private ExpedioClientResources expedioClientResources;
    private ProductInstanceClient productInstanceClient;
    private ArrayList productCategoryCodesList = new ArrayList<String>();
    private List<SellableProduct> productList = new ArrayList<SellableProduct>();
    private SellableProduct sellableProduct;
    private SellableProduct secondSellableProduct;
    private Products products;
    private Optional<ProductIdentifier> hCode;
    CustomerResource customerResource;
    private CustomerDTO customerDto;
    private SellableProduct nonMovableProduct;
    private SellableProduct rootOnlyMovableProduct;
    private SellableProduct movableProduct;
    private List<ProductInstance> productInstanceList = new ArrayList<ProductInstance>();
    private ProductInstance asIsProductInstance;
    private ProductInstance childProductInstance;
    private Pmr.ProductOfferings productOfferings;
    private ProductOffering productOffering;

    @Before
    public void before() throws Exception {
        context = new JUnit4Mockery() {{
            setImposteriser(ClassImposteriser.INSTANCE);
        }};

        UserContext userContext = aDirectUserContext().withToken("aToken").build();
        UserContextManager.setCurrent(userContext);

        pmr = context.mock(Pmr.class);
        siteFacade = context.mock(SiteFacade.class);
        productIdentifierFacade = mock(ProductIdentifierFacade.class);
        paginatedFilter = context.mock(PaginatedFilter.class);
        filterResult = context.mock(PaginatedFilterResult.class);
        quoteOptionFacade = context.mock(QuoteOptionFacade.class);
        projectResource = context.mock(ExpedioProjectResource.class);
        expedioClientResources = mock(ExpedioClientResources.class);
        productInstanceClient = mock(ProductInstanceClient.class);
        configuration = context.mock(ApplicationConfig.class);
        productOfferings = context.mock(Pmr.ProductOfferings.class);
        productOffering = context.mock(ProductOffering.class);
        siteDriver = context.mock(SiteDriver.class);
        productCategoryCodesList.add("H");
        sellableProduct = SellableProductFixture.aProduct().withId("id1").withName("product1").withIsSeparatelyModifiable(false).build();
        secondSellableProduct = SellableProductFixture.aProduct().withId("id2").withName("product2").withIsSeparatelyModifiable(false).build();
        productList.add(sellableProduct);
        productList.add(secondSellableProduct);
        products = new Products(productList);
        customerResource = mock(CustomerResource.class);
        hCode = Optional.of(new ProductIdentifier("productId", "productName", "versionNumber"));
        context.checking(new Expectations() {{
            allowing(configuration).getScheme();
            will(returnValue("http"));
            allowing(configuration).getHost();
            will(returnValue("127.0.0.1"));
            allowing(configuration).getPort();
            will(returnValue(1234));
            allowing(pmr).productOffering(ProductSCode.newInstance(null));
            will(returnValue(productOfferings));
            oneOf(productOfferings).get();
            will(returnValue(ProductOfferingFixture.aProductOffering().withIsInFrontCatalogue(false).withIsSeparatelyModifiable(false).build()));
            oneOf(productOffering).isSeparatelyModifiable();
            will(returnValue(false));
            oneOf(productOfferings).get();
            will(returnValue(ProductOfferingFixture.aProductOffering().withIsInFrontCatalogue(false).withIsSeparatelyModifiable(false).build()));
            oneOf(productOffering).isSeparatelyModifiable();
            will(returnValue(false));
        }});

        customerDto = CustomerDTOFixture.aCustomerDTO().withId(CUSTOMER_ID).withSalesChannel(SALES_CHANNEL).build();
        nonMovableProduct = SellableProductFixture.aProduct().withMoveConfigurationType(MoveConfigurationTypeEnum.NOT_MOVEABLE).build();
        rootOnlyMovableProduct = SellableProductFixture.aProduct().withMoveConfigurationType(MoveConfigurationTypeEnum.ROOT_ONLY_COPY).build();
        movableProduct = SellableProductFixture.aProduct().withMoveConfigurationType(MoveConfigurationTypeEnum.COPY_ALL).build();

        when(productIdentifierFacade.getSalesChannelDto("")).thenReturn(SalesChannelDTO.newInstance("salesChannelName", "salesChannelId", true, productCategoryCodesList));
        when(productIdentifierFacade.getAllSellableProducts()).thenReturn(products);
        when(productIdentifierFacade.getProductHCode(sellableProduct.getProductId())).thenReturn(hCode);
        when(productIdentifierFacade.getProductHCode(secondSellableProduct.getProductId())).thenReturn(hCode);
        when(expedioClientResources.getCustomerResource()).thenReturn(customerResource);
        when(customerResource.getByToken("aCustomerId", "aToken")).thenReturn(customerDto);

        productOfferings = mock(Pmr.ProductOfferings.class);
        moveProductOrchestrator = new MoveProductOrchestrator(siteFacade,
                                                            productIdentifierFacade,
                                                            productConfiguratorUriFactory,
                                                            quoteOptionFacade,
                                                            projectResource,
                                                            pmr,
                                                            siteDriver,
                                                            expedioClientResources,
                                                            productInstanceClient);
    }

    @Test
    public void shouldAddNoSitesGivenEmptySiteList() throws Exception {
        context.checking(new Expectations() {{
            oneOf(siteFacade).getAllBranchSites(CUSTOMER_ID, PROJECT_ID);
            will(returnValue(new ArrayList<SiteDTO>()));
            ignoring(quoteOptionFacade);
            allowing(siteDriver).get(CUSTOMER_ID, null);
            will(returnValue(newArrayList()));
            allowing(pmr).getSupportedCountries(null);
            will(returnValue(newArrayList()));
            allowing(pmr).getCountriesWithSpecialBidPricingType(null);
            will(returnValue(newArrayList()));

        }});

        when(productIdentifierFacade.isProductSpecialBid(null)).thenReturn(false);

        final ProductSitesDTO view = moveProductOrchestrator.buildSitesView(CUSTOMER_ID, PROJECT_ID, new PaginatedFilter<SiteDTO>() {
            @Override
            public PaginatedFilterResult applyTo(List<SiteDTO> items) {
                return new PaginatedFilterResult(0, items, 0, 1);
            }
        }, null, null, null, Optional.<String>absent());
        assertThat(view.sites.size(), is(0));

    }

    @Test
    public void shouldConstructInitialMoveSiteView() throws Exception {
        final SiteDTO uk = new SiteDTO();
        uk.country = "United Kingdom";
        uk.bfgSiteID = "100";

        final SiteDTO india = new SiteDTO();
        india.country = "India";
        india.bfgSiteID = "200";

        final String sCode = "sCode";

        context.checking(new Expectations() {{
            oneOf(siteFacade).getAllBranchSites(CUSTOMER_ID, PROJECT_ID);
            will(returnValue(newArrayList(uk, india)));
            allowing(siteDriver).get(CUSTOMER_ID, sCode);
            will(returnValue(newArrayList(new BFGAssetIdentifier("100", "A1"), new BFGAssetIdentifier("200", "A2"))));
            oneOf(paginatedFilter).applyTo(newArrayList(uk, india));
            will(returnValue(filterResult));
            oneOf(filterResult).getPageNumber();
            will(returnValue(0));
            oneOf(filterResult).getTotalRecords();
            will(returnValue(2));
            oneOf(filterResult).getFilteredSize();
            will(returnValue(1));
            oneOf(filterResult).getItems();
            will(returnValue(newArrayList(uk, india)));
            allowing(pmr).getCountriesWithSpecialBidPricingType(sCode);
            will(returnValue(newArrayList()));
            allowing(pmr).getSupportedCountries(sCode);
            will(returnValue(newArrayList(uk.getCountryISOCode())));
        }});

        when(productIdentifierFacade.isProductSpecialBid(sCode)).thenReturn(false);

        ProductSitesDTO addModifyProductSitesView = moveProductOrchestrator.buildSitesView(CUSTOMER_ID,
                                                                                       PROJECT_ID,
                                                                                       paginatedFilter,
                                                                                       sCode, null, null,
                                                                                       Optional.<String>absent());
        List<ProductSitesDTO.SiteRowDTO> sites = addModifyProductSitesView.sites;

        assertThat(sites.size(), is(2));

        assertThat(sites.get(0).fullAddress, is(new SiteView(uk).getFullAddress()));
        assertThat(sites.get(0).isValidForProduct, is(true));
        assertThat(sites.get(1).fullAddress, is(new SiteView(india).getFullAddress()));
        assertThat(sites.get(1).isValidForProduct, is(false));
    }

    @Test
    public void shouldConstructMoveSiteViewWithNewSiteDetails() throws Exception {
        final SiteDTO uk = new SiteDTO();
        uk.country = "United Kingdom";
        uk.bfgSiteID = "100";

        final SiteDTO india = new SiteDTO();
        india.country = "India";
        india.bfgSiteID = "200";

        final String sCode = "sCode";
        List<String> existingSiteIds = newArrayList("100");
        final String newSiteId = "200";

        context.checking(new Expectations() {{
            oneOf(siteFacade).getAllBranchSites(CUSTOMER_ID, PROJECT_ID);
            will(returnValue(newArrayList(uk, india)));
            allowing(siteDriver).get(CUSTOMER_ID, sCode);
            will(returnValue(newArrayList(new BFGAssetIdentifier("100", "A1"), new BFGAssetIdentifier("200", "A2"))));
            oneOf(paginatedFilter).applyTo(newArrayList(uk));
            will(returnValue(filterResult));
            oneOf(filterResult).getPageNumber();
            will(returnValue(0));
            oneOf(filterResult).getTotalRecords();
            will(returnValue(2));
            oneOf(filterResult).getFilteredSize();
            will(returnValue(1));
            oneOf(filterResult).getItems();
            will(returnValue(newArrayList(uk, india)));
            allowing(pmr).getCountriesWithSpecialBidPricingType(sCode);
            will(returnValue(newArrayList()));
            allowing(pmr).getSupportedCountries(sCode);
            will(returnValue(newArrayList(uk.getCountryISOCode())));
        }});

        when(productIdentifierFacade.isProductSpecialBid(sCode)).thenReturn(false);

        ProductSitesDTO addModifyProductSitesView = moveProductOrchestrator.buildSitesView(CUSTOMER_ID,
                                                                                       PROJECT_ID,
                                                                                       paginatedFilter,
                                                                                       sCode,
                                                                                       newSiteId,
                                                                                       existingSiteIds,
                                                                                       Optional.<String>absent());
        List<ProductSitesDTO.SiteRowDTO> sites = addModifyProductSitesView.sites;

        assertThat(sites.size(), is(2));

        assertThat(sites.get(0).fullAddress, is(new SiteView(uk).getFullAddress()));
        assertThat(sites.get(0).isValidForProduct, is(true));
        assertThat(sites.get(1).fullAddress, is(new SiteView(india).getFullAddress()));
        assertThat(sites.get(1).isValidForProduct, is(false));
        assertThat(sites.get(0).newFullAddress, is(new SiteView(india).getFullAddress()));
        assertThat(sites.get(1).newFullAddress, is(new SiteView(india).getFullAddress()));
    }

    @Test
    public void shouldReturnEmptySiteListForNullProduct() throws Exception {
        final SiteDTO uk = new SiteDTO();
        uk.country = "United Kingdom";
        uk.bfgSiteID = "100";

        final SiteDTO india = new SiteDTO();
        india.country = "India";
        india.bfgSiteID = "200";

        final String sCode = "sCode";
        List<String> existingSiteIds = newArrayList("100");
        final String newSiteId = "200";

        context.checking(new Expectations() {{
            oneOf(siteFacade).getAllBranchSites(CUSTOMER_ID, PROJECT_ID);
            will(returnValue(newArrayList(uk, india)));
            allowing(siteDriver).get(CUSTOMER_ID, sCode);
            will(returnValue(newArrayList("100", "200")));
            oneOf(paginatedFilter).applyTo(newArrayList(uk));
            will(returnValue(filterResult));
            oneOf(filterResult).getPageNumber();
            will(returnValue(0));
            oneOf(filterResult).getTotalRecords();
            will(returnValue(2));
            oneOf(filterResult).getFilteredSize();
            will(returnValue(1));
            oneOf(filterResult).getItems();
            will(returnValue(newArrayList(uk, india)));
            allowing(pmr).getCountriesWithSpecialBidPricingType(sCode);
            will(returnValue(newArrayList()));
            allowing(pmr).getSupportedCountries(sCode);
            will(returnValue(newArrayList(uk.getCountryISOCode())));
        }});

        when(productIdentifierFacade.isProductSpecialBid(sCode)).thenReturn(false);

        ProductSitesDTO addModifyProductSitesView = moveProductOrchestrator.buildSitesView(CUSTOMER_ID,
                                                                                       PROJECT_ID,
                                                                                       paginatedFilter,
                                                                                       null,
                                                                                       newSiteId,
                                                                                       existingSiteIds,
                                                                                       Optional.<String>absent());
        List<ProductSitesDTO.SiteRowDTO> sites = addModifyProductSitesView.sites;

        assertThat(sites.size(), is(2));

        assertThat(sites.get(0).fullAddress, is(new SiteView(uk).getFullAddress()));
        assertThat(sites.get(0).isValidForProduct, is(false));
        assertThat(sites.get(1).fullAddress, is(new SiteView(india).getFullAddress()));
        assertThat(sites.get(1).isValidForProduct, is(false));
    }

    @Test
    public void shouldIncludeMovableProductsInSellableProducts() throws Exception {

        when(productIdentifierFacade.getSellableProductsForSalesChannel(SALES_CHANNEL)).thenReturn(new Products(newArrayList(rootOnlyMovableProduct, movableProduct)));
        Products products = moveProductOrchestrator.getProducts("aCustomerId", ProductAction.Move.description(),CONTRACT_ID );
        assertThat(products.sellableProducts().size(), is(2));
        assertThat(products.sellableProducts().get(0).getMoveConfigurationType(), is(MoveConfigurationTypeEnum.ROOT_ONLY_COPY));
        assertThat(products.sellableProducts().get(1).getMoveConfigurationType(), is(MoveConfigurationTypeEnum.COPY_ALL));
    }

    @Test
    public void shouldExcludeMovableProductsFromSellableProducts() throws Exception {
        when(productIdentifierFacade.getSellableProductsForSalesChannel(SALES_CHANNEL)).thenReturn(new Products(newArrayList(nonMovableProduct)));
        Products products = moveProductOrchestrator.getProducts("aCustomerId", ProductAction.Move.description(), CONTRACT_ID);
        assertThat(products.sellableProducts().size(), is(0));
    }

    @Test
    public void shouldConstructMoveSiteViewWithMultipleSitesMovingToTheSameNewSite() throws Exception {
        final SiteDTO uk = new SiteDTO();
        uk.country = "United Kingdom";
        uk.bfgSiteID = "100";

        final SiteDTO france = new SiteDTO();
        france.country = "France";
        france.bfgSiteID = "400";

        final SiteDTO ireland = new SiteDTO();
        ireland.country = "Ireland";
        ireland.bfgSiteID = "500";

        final SiteDTO india = new SiteDTO();
        india.country = "India";
        india.bfgSiteID = "200";

        final String sCode = "sCode";
        List<String> existingSiteIds = newArrayList("100", "400", "500");
        final String newSiteId = "200";

        context.checking(new Expectations() {{
            oneOf(siteFacade).getAllBranchSites(CUSTOMER_ID, PROJECT_ID);
            will(returnValue(newArrayList(uk, france, ireland, india)));
            allowing(siteDriver).get(CUSTOMER_ID, sCode);
            will(returnValue(newArrayList(new BFGAssetIdentifier("100", "A1"), new BFGAssetIdentifier("400", "A2"), new BFGAssetIdentifier("500", "A3"), new BFGAssetIdentifier("200", "A4"), new BFGAssetIdentifier("300", "A5"))));
            oneOf(paginatedFilter).applyTo(newArrayList(uk, france, ireland));
            will(returnValue(filterResult));
            oneOf(filterResult).getPageNumber();
            will(returnValue(0));
            oneOf(filterResult).getTotalRecords();
            will(returnValue(2));
            oneOf(filterResult).getFilteredSize();
            will(returnValue(1));
            oneOf(filterResult).getItems();
            will(returnValue(newArrayList(uk, india, france, ireland)));
            allowing(pmr).getCountriesWithSpecialBidPricingType(sCode);
            will(returnValue(newArrayList()));
            allowing(pmr).getSupportedCountries(sCode);
            will(returnValue(newArrayList(uk.getCountryISOCode())));
        }});

        when(productIdentifierFacade.isProductSpecialBid(sCode)).thenReturn(false);

        ProductSitesDTO addModifyProductSitesView = moveProductOrchestrator.buildSitesView(CUSTOMER_ID,
                                                                                       PROJECT_ID,
                                                                                       paginatedFilter,
                                                                                       sCode,
                                                                                       newSiteId,
                                                                                       existingSiteIds,
                                                                                       Optional.<String>absent());
        List<ProductSitesDTO.SiteRowDTO> sites = addModifyProductSitesView.sites;

        assertThat(sites.size(), is(4));

        validateExistingToNewSite(sites.get(0), new SiteView(uk), new SiteView(india));
        validateExistingToNewSite(sites.get(1), new SiteView(india), new SiteView(india));
        validateExistingToNewSite(sites.get(2), new SiteView(france), new SiteView(india));
        validateExistingToNewSite(sites.get(3), new SiteView(ireland), new SiteView(india));
    }

    @Test
    public void shouldConstructMoveSiteViewWithMultipleSitesMovingInCampus() throws Exception {
        final SiteDTO uk = new SiteDTO();
        uk.country = "United Kingdom";
        uk.bfgSiteID = "100";

        final SiteDTO france = new SiteDTO();
        france.country = "France";
        france.bfgSiteID = "400";

        final SiteDTO ireland = new SiteDTO();
        ireland.country = "Ireland";
        ireland.bfgSiteID = "500";

        final SiteDTO india = new SiteDTO();
        india.country = "India";
        india.bfgSiteID = "200";

        final String sCode = "sCode";
        List<String> existingSiteIds = newArrayList("100", "400", "500");
        final String newSiteId = "sameSite";

        context.checking(new Expectations() {{
            oneOf(siteFacade).getAllBranchSites(CUSTOMER_ID, PROJECT_ID);
            will(returnValue(newArrayList(uk, france, ireland, india)));
            allowing(siteDriver).get(CUSTOMER_ID, sCode);
            will(returnValue(newArrayList(new BFGAssetIdentifier("100", "A1"), new BFGAssetIdentifier("400", "A2"), new BFGAssetIdentifier("500", "A3"), new BFGAssetIdentifier("200", "A4"), new BFGAssetIdentifier("300", "A5"))));
            oneOf(paginatedFilter).applyTo(newArrayList(uk, france, ireland));
            will(returnValue(filterResult));
            oneOf(filterResult).getPageNumber();
            will(returnValue(0));
            oneOf(filterResult).getTotalRecords();
            will(returnValue(2));
            oneOf(filterResult).getFilteredSize();
            will(returnValue(1));
            oneOf(filterResult).getItems();
            will(returnValue(newArrayList(uk, india, france, ireland)));
            allowing(pmr).getCountriesWithSpecialBidPricingType(sCode);
            will(returnValue(newArrayList()));
            allowing(pmr).getSupportedCountries(sCode);
            will(returnValue(newArrayList(uk.getCountryISOCode())));
        }});

        when(productIdentifierFacade.isProductSpecialBid(sCode)).thenReturn(false);

        ProductSitesDTO addModifyProductSitesView = moveProductOrchestrator.buildSitesView(CUSTOMER_ID,
                                                                                       PROJECT_ID,
                                                                                       paginatedFilter,
                                                                                       sCode,
                                                                                       newSiteId,
                                                                                       existingSiteIds,
                                                                                       Optional.<String>absent());
        List<ProductSitesDTO.SiteRowDTO> sites = addModifyProductSitesView.sites;

        assertThat(sites.size(), is(4));

        validateExistingToNewSite(sites.get(0), new SiteView(uk), new SiteView(uk));
        validateExistingToNewSite(sites.get(1), new SiteView(india), new SiteView(india));
        validateExistingToNewSite(sites.get(2), new SiteView(france), new SiteView(france));
        validateExistingToNewSite(sites.get(3), new SiteView(ireland), new SiteView(ireland));
    }

    @Test
    public void shouldAllowMoveForNonCpeProduct() throws Exception {
        childProductInstance = DefaultProductInstanceFixture
            .aProductInstance()
            .withProductIdentifier(new ProductIdentifier("childProductId", "childProductName", "childVersionNumber"))
            .withInitialBillingStartDate(new Date())
            .build();
        asIsProductInstance = DefaultProductInstanceFixture
            .aProductInstance()
            .withProductIdentifier(new ProductIdentifier("productId", "productName", "versionNumber"))
            .withChildProductInstance(childProductInstance)
            .withInitialBillingStartDate(new Date())
            .build();
        productInstanceList = newArrayList(asIsProductInstance);
        when(productInstanceClient.getInServiceAssets(new SiteId("1"), new ProductCode("productCode"), new ProductVersion("productVersion"), true)).thenReturn(productInstanceList);
        Notification notification = moveProductOrchestrator.endOfLifeCheck("1", "productCode", "productVersion", new Date(), null, null);
        assertThat(notification.getErrorEvents().size(), is(0));
    }

    @Test
    public void shouldAllowMoveAfterEndOfLifeCheckForNullEffectiveEndDate() throws Exception {
        ProductOfferingFixture offeringFixture = ProductOfferingFixture.aProductOffering().withSimpleProductOfferingType(SimpleProductOfferingType.NetworkNode).withEffectiveEndDate(null);

        when(productOfferings.get()).thenReturn(offeringFixture.build());
        asIsProductInstance = DefaultProductInstanceFixture
            .aProductInstance()
            .withProductIdentifier(new ProductIdentifier("productId", "productName", "versionNumber"))
            .withProductOffering(offeringFixture)
            .withContractTerm("12")
            .withPriceLines(Lists.<PriceLine>newArrayList(PriceLineFixture.aPriceLine().withBillingStartDate(new Date()).build()))
            .build();
        productInstanceList = newArrayList(asIsProductInstance);
        when(productInstanceClient.getInServiceAssets(new SiteId("1"), new ProductCode("productCode"), new ProductVersion("productVersion"), true)).thenReturn(productInstanceList);
        context.checking(new Expectations() {{
            allowing(pmr).productOffering(com.bt.rsqe.domain.bom.parameters.ProductSCode.newInstance("test"));
            will(returnValue(productOfferings));
        }});

        Notification notification = moveProductOrchestrator.endOfLifeCheck("1", "productCode", "productVersion", new Date(), null, null);
        assertThat(notification.getErrorEvents().size(), is(0));
    }

    @Test
    public void shouldAllowMoveAfterEndOfLifeCheckForEffectiveEndDateAfterSystemDate() throws Exception {
        Date effectiveEndDate = CalendarFixture.aCalendar()
                                     .day(10)
                                     .month(CalendarFixture.Month.JUL)
                                     .year(2016)
                                     .get()
                                     .getTime();
        Date systemDate = CalendarFixture.aCalendar()
                                     .day(10)
                                     .month(CalendarFixture.Month.JUL)
                                     .year(2014)
                                     .get()
                                     .getTime();
        ProductOfferingFixture offeringFixture = ProductOfferingFixture.aProductOffering().withSimpleProductOfferingType(SimpleProductOfferingType.NetworkNode).withEffectiveEndDate(effectiveEndDate);

        when(productOfferings.get()).thenReturn(offeringFixture.build());
        Date currentDate = new Date();
        asIsProductInstance = DefaultProductInstanceFixture
            .aProductInstance()
            .withProductIdentifier(new ProductIdentifier("productId", "productName", "versionNumber"))
            .withProductOffering(offeringFixture)
            .withContractTerm("12")
            .withPriceLines(Lists.<PriceLine>newArrayList(PriceLineFixture.aPriceLine().withBillingStartDate(currentDate).build()))
            .withInitialBillingStartDate(currentDate)
            .build();
        productInstanceList = newArrayList(asIsProductInstance);
        when(productInstanceClient.getInServiceAssets(new SiteId("1"), new ProductCode("productCode"), new ProductVersion("productVersion"), true)).thenReturn(productInstanceList);
        context.checking(new Expectations() {{
            allowing(pmr).productOffering(com.bt.rsqe.domain.bom.parameters.ProductSCode.newInstance("test"));
            will(returnValue(productOfferings));
        }});

        Notification notification = moveProductOrchestrator.endOfLifeCheck("1", "productCode", "productVersion", systemDate, null, null);
        assertThat(notification.getErrorEvents().size(), is(0));
    }

    @Test
    public void shouldThrowHardStopMessageDuringEndOfLifeCheckForEffectiveEndDateBeforeSystemDate() throws Exception {
        Date effectiveEndDate = CalendarFixture.aCalendar()
                                     .day(10)
                                     .month(CalendarFixture.Month.JUL)
                                     .year(2013)
                                     .get()
                                     .getTime();
        Date systemDate = CalendarFixture.aCalendar()
                                     .day(10)
                                     .month(CalendarFixture.Month.JUL)
                                     .year(2014)
                                     .get()
                                     .getTime();
        ProductOfferingFixture offeringFixture = ProductOfferingFixture.aProductOffering().withSimpleProductOfferingType(SimpleProductOfferingType.NetworkNode).withEffectiveEndDate(effectiveEndDate);

        when(productOfferings.get()).thenReturn(offeringFixture.build());
        Date currentDate = new Date();
        asIsProductInstance = DefaultProductInstanceFixture
            .aProductInstance()
            .withProductIdentifier(new ProductIdentifier("productId", "productName", "versionNumber"))
            .withProductOffering(offeringFixture)
            .withContractTerm("12")
            .withPriceLines(Lists.<PriceLine>newArrayList(PriceLineFixture.aPriceLine().withBillingStartDate(currentDate).build()))
            .withInitialBillingStartDate(currentDate)
            .build();
        productInstanceList = newArrayList(asIsProductInstance);
        when(productInstanceClient.getInServiceAssets(new SiteId("1"), new ProductCode("productCode"), new ProductVersion("productVersion"), true)).thenReturn(productInstanceList);
        context.checking(new Expectations() {{
            allowing(pmr).productOffering(com.bt.rsqe.domain.bom.parameters.ProductSCode.newInstance("test"));
            will(returnValue(productOfferings));
        }});

        Notification notification = moveProductOrchestrator.endOfLifeCheck("1", "productCode", "productVersion", systemDate, null, asIsProductInstance.getLineItemId());
        assertThat(notification.getErrorEvents().size(), is(1));
        assertThat(notification.getErrorEvents().get(0).getMessage(), is("Error: The associated CPE bundle is marked as End of Life or will become End of Life during the remaining contract period and needs to be replaced. " +
                                                                         "Please return to the Config screens to replace the CPE bundle as part of a new quote"));
    }

    @Test
    public void shouldGetWarningMessageDuringEndOfLifeCheck() throws Exception {
        Date effectiveEndDate = CalendarFixture.aCalendar()
                                     .day(10)
                                     .month(CalendarFixture.Month.MAY)
                                     .year(2015)
                                     .get()
                                     .getTime();
        Date systemDate = CalendarFixture.aCalendar()
                                     .day(10)
                                     .month(CalendarFixture.Month.JUL)
                                     .year(2014)
                                     .get()
                                     .getTime();
        Date billingStartDate = CalendarFixture.aCalendar()
                                     .day(10)
                                     .month(CalendarFixture.Month.JUL)
                                     .year(2014)
                                     .get()
                                     .getTime();
        ProductOfferingFixture offeringFixture = ProductOfferingFixture.aProductOffering().withSimpleProductOfferingType(SimpleProductOfferingType.NetworkNode).withEffectiveEndDate(effectiveEndDate);

        when(productOfferings.get()).thenReturn(offeringFixture.build());
        asIsProductInstance = DefaultProductInstanceFixture
            .aProductInstance()
            .withProductIdentifier(new ProductIdentifier("productId", "productName", "versionNumber"))
            .withProductOffering(offeringFixture)
            .withContractTerm("12")
            .withPriceLines(Lists.<PriceLine>newArrayList(PriceLineFixture.aPriceLine().withBillingStartDate(billingStartDate).build()))
            .withInitialBillingStartDate(billingStartDate)
            .build();
        productInstanceList = newArrayList(asIsProductInstance);
        when(productInstanceClient.getInServiceAssets(new SiteId("1"), new ProductCode("productCode"), new ProductVersion("productVersion"), true)).thenReturn(productInstanceList);
        context.checking(new Expectations() {{
            allowing(pmr).productOffering(com.bt.rsqe.domain.bom.parameters.ProductSCode.newInstance("test"));
            will(returnValue(productOfferings));
        }});

        Notification notification = moveProductOrchestrator.endOfLifeCheck("1", "productCode", "productVersion", systemDate, null, asIsProductInstance.getLineItemId());
        assertThat(notification.getWarningEvents().size(), is(1));
        assertThat(notification.getWarningEvents().get(0).getMessage(), is("Warning: The associated CPE bundle will reach End of Life within the remaining 6 months of the contract end date and it " +
                                                                         "is recommended that the CPE bundle is replaced. Please click OK to continue or return to the Config screens to replace the CPE bundle as part of a new quote"));
    }

    @Test
    public void shouldThrowHardWarningMessageForGreaterThanSixMonthDifferenceBetweenEEDandCED() throws Exception {
        Date effectiveEndDate = CalendarFixture.aCalendar()
                                     .day(10)
                                     .month(CalendarFixture.Month.MAY)
                                     .year(2015)
                                     .get()
                                     .getTime();
        Date systemDate = CalendarFixture.aCalendar()
                                     .day(10)
                                     .month(CalendarFixture.Month.JUL)
                                     .year(2014)
                                     .get()
                                     .getTime();
        Date billingStartDate = CalendarFixture.aCalendar()
                                     .day(10)
                                     .month(CalendarFixture.Month.JUL)
                                     .year(2014)
                                     .get()
                                     .getTime();
        ProductOfferingFixture offeringFixture = ProductOfferingFixture.aProductOffering().withSimpleProductOfferingType(SimpleProductOfferingType.NetworkNode).withEffectiveEndDate(effectiveEndDate);

        when(productOfferings.get()).thenReturn(offeringFixture.build());
        asIsProductInstance = DefaultProductInstanceFixture
            .aProductInstance()
            .withProductIdentifier(new ProductIdentifier("productId", "productName", "versionNumber"))
            .withProductOffering(offeringFixture)
            .withContractTerm("24")
            .withPriceLines(Lists.<PriceLine>newArrayList(PriceLineFixture.aPriceLine().withBillingStartDate(billingStartDate).build()))
            .withInitialBillingStartDate(billingStartDate)
            .build();
        productInstanceList = newArrayList(asIsProductInstance);
        when(productInstanceClient.getInServiceAssets(new SiteId("1"), new ProductCode("productCode"), new ProductVersion("productVersion"), true)).thenReturn(productInstanceList);
        context.checking(new Expectations() {{
            allowing(pmr).productOffering(com.bt.rsqe.domain.bom.parameters.ProductSCode.newInstance("test"));
            will(returnValue(productOfferings));
        }});

        Notification notification = moveProductOrchestrator.endOfLifeCheck("1", "productCode", "productVersion", systemDate, null, asIsProductInstance.getLineItemId());
        assertThat(notification.getErrorEvents().size(), is(1));
        assertThat(notification.getErrorEvents().get(0).getMessage(), is("Error: The associated CPE bundle is marked as End of Life or will become End of Life during the remaining contract period and needs to be replaced. " +
                                                                         "Please return to the Config screens to replace the CPE bundle as part of a new quote"));
    }


    @Test
    public void shouldAllowMoveForStencilProduct() throws Exception {
        ProductOfferingFixture offeringFixture = ProductOfferingFixture.aProductOffering().withSimpleProductOfferingType(SimpleProductOfferingType.NetworkNode).withAttribute(ProductOffering.STENCIL_RESERVED_NAME);
        ProductOfferingFixture stencilOfferingFixture = ProductOfferingFixture.aStencilableProductOffering()
                                                                              .withStencil(StencilId.versioned(StencilCode.newInstance("aStencil"),
                                                                                                               StencilVersion.newInstance("aStencilVersion")));

        when(productOfferings.get()).thenReturn(offeringFixture.build());
        Pmr.ProductOfferings stencilableOfferings = mock(Pmr.ProductOfferings.class);
        when(productOfferings.withStencil(any(StencilId.class))).thenReturn(stencilableOfferings);
        when(stencilableOfferings.get()).thenReturn(stencilOfferingFixture.build());
        asIsProductInstance = DefaultProductInstanceFixture
            .aProductInstance()
            .withProductIdentifier(new ProductIdentifier("productId", "productName", "versionNumber"))
            .withProductOffering(offeringFixture)
            .withContractTerm("12")
            .withStencilId("aStencil")
            .withPriceLines(Lists.<PriceLine>newArrayList(PriceLineFixture.aPriceLine().withBillingStartDate(new Date()).build()))
            .build();
        productInstanceList = newArrayList(asIsProductInstance);
        when(productInstanceClient.getInServiceAssets(new SiteId("1"), new ProductCode("productCode"), new ProductVersion("productVersion"), true)).thenReturn(productInstanceList);
        context.checking(new Expectations() {{
            allowing(pmr).productOffering(com.bt.rsqe.domain.bom.parameters.ProductSCode.newInstance("test"));
            will(returnValue(productOfferings));
        }});

        Notification notification = moveProductOrchestrator.endOfLifeCheck("1", "productCode", "productVersion", new Date(), null, null);
        assertThat(notification.getErrorEvents().size(), is(0));
    }

    @Test
    public void shouldNotAllowMoveForStencilProduct() throws Exception {
        Date effectiveEndDate = CalendarFixture.aCalendar()
                                               .day(10)
                                               .month(CalendarFixture.Month.JAN)
                                               .year(2015)
                                               .get()
                                               .getTime();
        Date billingStartDate = CalendarFixture.aCalendar()
                                               .day(10)
                                               .month(CalendarFixture.Month.FEB)
                                               .year(2015)
                                               .get()
                                               .getTime();
        ProductOfferingFixture offeringFixture = ProductOfferingFixture.aProductOffering().withSimpleProductOfferingType(SimpleProductOfferingType.NetworkNode).withAttribute(ProductOffering.STENCIL_RESERVED_NAME);
        ProductOfferingFixture stencilOfferingFixture = ProductOfferingFixture.aStencilableProductOffering()
                                                                              .withStencil(StencilId.versioned(StencilCode.newInstance("aStencil"),
                                                                                                               StencilVersion.newInstance("aStencilVersion")))
                                                        .withEffectiveEndDate(effectiveEndDate);

        when(productOfferings.get()).thenReturn(offeringFixture.build());
        Pmr.ProductOfferings stencilableOfferings = mock(Pmr.ProductOfferings.class);
        when(productOfferings.withStencil(any(StencilId.class))).thenReturn(stencilableOfferings);
        when(stencilableOfferings.get()).thenReturn(stencilOfferingFixture.build());
        asIsProductInstance = DefaultProductInstanceFixture
            .aProductInstance()
            .withProductIdentifier(new ProductIdentifier("productId", "productName", "versionNumber"))
            .withProductOffering(offeringFixture)
            .withContractTerm("12")
            .withStencilId("aStencil")
            .withPriceLines(Lists.<PriceLine>newArrayList(PriceLineFixture.aPriceLine().withBillingStartDate(billingStartDate).build()))
            .withInitialBillingStartDate(billingStartDate)
            .build();
        productInstanceList = newArrayList(asIsProductInstance);
        when(productInstanceClient.getInServiceAssets(new SiteId("1"), new ProductCode("productCode"), new ProductVersion("productVersion"), true)).thenReturn(productInstanceList);
        context.checking(new Expectations() {{
            allowing(pmr).productOffering(com.bt.rsqe.domain.bom.parameters.ProductSCode.newInstance("test"));
            will(returnValue(productOfferings));
        }});

        Notification notification = moveProductOrchestrator.endOfLifeCheck("1", "productCode", "productVersion", new Date(), null, asIsProductInstance.getLineItemId());
        assertThat(notification.getErrorEvents().size(), is(1));
        assertThat(notification.getErrorEvents().get(0).getMessage(), is("Error: The associated CPE bundle is marked as End of Life or will become End of Life during the remaining contract period and needs to be replaced. " +
                                                                         "Please return to the Config screens to replace the CPE bundle as part of a new quote"));
    }


    private void validateExistingToNewSite(ProductSitesDTO.SiteRowDTO site, SiteView existingSite, SiteView newSite) {
        assertThat(site.fullAddress, is(existingSite.getFullAddress()));
        assertThat(site.newFullAddress, is(newSite.getFullAddress()));
    }
}