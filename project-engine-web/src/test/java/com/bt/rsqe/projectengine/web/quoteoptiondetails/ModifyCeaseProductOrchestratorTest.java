package com.bt.rsqe.projectengine.web.quoteoptiondetails;

import com.bt.rsqe.client.Pmr;
import com.bt.rsqe.container.ApplicationConfig;
import com.bt.rsqe.customerinventory.client.ProductInstanceClient;
import com.bt.rsqe.customerinventory.driver.SiteDriver;
import com.bt.rsqe.customerinventory.dto.AssetDTO;
import com.bt.rsqe.customerinventory.fixtures.AssetDTOFixture;
import com.bt.rsqe.customerinventory.parameter.ContractId;
import com.bt.rsqe.customerinventory.parameter.CustomerId;
import com.bt.rsqe.customerinventory.parameter.ProductCode;
import com.bt.rsqe.customerinventory.parameter.ProductVersion;
import com.bt.rsqe.customerinventory.parameter.SiteId;
import com.bt.rsqe.customerrecord.ExpedioClientResources;
import com.bt.rsqe.customerrecord.ServiceDTO;
import com.bt.rsqe.customerrecord.SiteDTO;
import com.bt.rsqe.domain.StencilId;
import com.bt.rsqe.domain.bom.fixtures.ProductOfferingFixture;
import com.bt.rsqe.domain.bom.fixtures.SellableProductFixture;
import com.bt.rsqe.domain.bom.parameters.ProductSCode;
import com.bt.rsqe.domain.product.Attribute;
import com.bt.rsqe.domain.product.AttributeName;
import com.bt.rsqe.domain.product.InstanceCharacteristic;
import com.bt.rsqe.domain.product.ProductOffering;
import com.bt.rsqe.domain.product.SellableProduct;
import com.bt.rsqe.domain.product.parameters.ProductIdentifier;
import com.bt.rsqe.domain.project.ProductInstance;
import com.bt.rsqe.dto.BFGAssetIdentifier;
import com.bt.rsqe.expedio.fixtures.SiteDTOFixture;
import com.bt.rsqe.expedio.project.ExpedioProjectResource;
import com.bt.rsqe.pmr.dto.SalesChannelDTO;
import com.bt.rsqe.projectengine.QuoteOptionDTO;
import com.bt.rsqe.projectengine.QuoteOptionItemDTO;
import com.bt.rsqe.projectengine.QuoteOptionItemDTOFixture;
import com.bt.rsqe.projectengine.web.CreateLineItemDTO;
import com.bt.rsqe.projectengine.web.facades.ProductIdentifierFacade;
import com.bt.rsqe.projectengine.web.facades.QuoteOptionFacade;
import com.bt.rsqe.projectengine.web.facades.SiteFacade;
import com.bt.rsqe.projectengine.web.uri.UriFactory;
import com.bt.rsqe.projectengine.web.uri.UriFactoryImpl;
import com.bt.rsqe.projectengine.web.view.AddOrModifyProductView;
import com.bt.rsqe.projectengine.web.view.ProductServiceDTO;
import com.bt.rsqe.projectengine.web.view.ProductSitesDTO;
import com.bt.rsqe.projectengine.web.view.Products;
import com.bt.rsqe.projectengine.web.view.SiteView;
import com.bt.rsqe.projectengine.web.view.filtering.PaginatedFilter;
import com.bt.rsqe.projectengine.web.view.filtering.PaginatedFilterResult;
import com.bt.rsqe.projectengine.web.view.pagination.DefaultPagination;
import com.bt.rsqe.projectengine.web.view.pagination.Pagination;
import com.bt.rsqe.security.UserContext;
import com.bt.rsqe.security.UserContextManager;
import com.bt.rsqe.security.UserPrincipal;
import com.bt.rsqe.web.rest.exception.ResourceNotFoundException;
import com.google.common.base.Optional;
import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static com.bt.rsqe.domain.bom.fixtures.AttributeFixture.*;
import static com.bt.rsqe.domain.bom.fixtures.BehaviourFixture.*;
import static com.bt.rsqe.domain.bom.fixtures.ProductOfferingFixture.*;
import static com.bt.rsqe.domain.product.DefaultProductInstanceFixture.*;
import static com.bt.rsqe.enums.ProductAction.*;
import static com.bt.rsqe.expedio.fixtures.ProjectDTOFixture.*;
import static com.google.common.collect.Lists.*;
import static org.apache.axis.utils.StringUtils.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class ModifyCeaseProductOrchestratorTest {

    public static final String CUSTOMER_ID = "customerId";
    public static final String CONTRACT_ID = "contractId";
    public static final String PROJECT_ID = "projectId";
    public static final String QUOTE_OPTION_ID = "quoteOptionId";
    private JUnit4Mockery context;
    private SiteFacade siteFacade;
    private ModifyProductOrchestrator modifyProductOrchestrator;
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
    private ArrayList productCategoryCodesList = new ArrayList<String>();
    private List<SellableProduct> productList = new ArrayList<SellableProduct>();
    private ArrayList emptyProductCategoryCodesList = new ArrayList<String>();
    private SellableProduct sellableProduct;
    private SellableProduct secondSellableProduct;
    private Products products;
    private Optional<ProductIdentifier> hCode;
    private QuoteOptionItemDTO quoteOptionItemDTO;
    private QuoteOptionItemDTO quoteOptionItemDTO2;
    private ProductInstanceClient productInstanceClient;
    private Pmr.ProductOfferings productOfferings;
    private ProductOffering productOffering;

    @Before
    public void before() throws Exception {
        context = new JUnit4Mockery() {{
            setImposteriser(ClassImposteriser.INSTANCE);
        }};
        UserContextManager.setCurrent(new UserContext(new UserPrincipal("login-name"), "TOKEN"));
        pmr = context.mock(Pmr.class);
        siteFacade = context.mock(SiteFacade.class);
        productIdentifierFacade = mock(ProductIdentifierFacade.class);
        paginatedFilter = context.mock(PaginatedFilter.class);
        filterResult = context.mock(PaginatedFilterResult.class);
        quoteOptionFacade = context.mock(QuoteOptionFacade.class);
        projectResource = context.mock(ExpedioProjectResource.class);
        expedioClientResources = context.mock(ExpedioClientResources.class);
        configuration = context.mock(ApplicationConfig.class);
        siteDriver = context.mock(SiteDriver.class);
        productInstanceClient = context.mock(ProductInstanceClient.class);
        productCategoryCodesList.add("H");
        productOfferings = context.mock(Pmr.ProductOfferings.class);
        productOffering = context.mock(ProductOffering.class);
        sellableProduct = SellableProductFixture.aProduct().withId("id1").withName("product1").build();
        secondSellableProduct = SellableProductFixture.aProduct().withId("id2").withName("product2").build();
        productList.add(sellableProduct);
        productList.add(secondSellableProduct);
        products = new Products(productList);
        hCode = Optional.of(new ProductIdentifier("productId", "productName", "versionNumber"));
        context.checking(new Expectations() {{
            allowing(configuration).getScheme();
            will(returnValue("http"));
            allowing(configuration).getHost();
            will(returnValue("127.0.0.1"));
            allowing(configuration).getPort();
            will(returnValue(1234));
        }});

        when(productIdentifierFacade.getSalesChannelDto("")).thenReturn(SalesChannelDTO.newInstance("salesChannelName", "salesChannelId", true, productCategoryCodesList));
        when(productIdentifierFacade.getSellableProductsForSalesChannel("")).thenReturn(products);
        when(productIdentifierFacade.getProductHCode(sellableProduct.getProductId())).thenReturn(hCode);
        when(productIdentifierFacade.getProductHCode(secondSellableProduct.getProductId())).thenReturn(hCode);

        modifyProductOrchestrator = new ModifyProductOrchestrator(siteFacade,
                                                            productIdentifierFacade,
                                                            productConfiguratorUriFactory,
                                                            quoteOptionFacade,
                                                            projectResource,
                                                            pmr, siteDriver,
                                                            expedioClientResources,
                                                            productInstanceClient);

        quoteOptionItemDTO = QuoteOptionItemDTOFixture.aQuoteOptionItemDTO().build();
        quoteOptionItemDTO2 = QuoteOptionItemDTOFixture.aQuoteOptionItemDTO().build();
    }

    @Test
    public void shouldModifyProperlyMapViewObject() throws Exception {
        context.checking(new Expectations() {{
            oneOf(quoteOptionFacade).get("projectId", "quoteOptionId");
            will(returnValue(QuoteOptionDTO.newInstance("friendlyId", "name", "currency", "", "user")));
            oneOf(projectResource).getProject("projectId");
            will(returnValue(aProjectDTO().build()));
            oneOf(quoteOptionFacade).getAllQuoteOptionItem("projectId", "quoteOptionId");
            will(returnValue(newArrayList(quoteOptionItemDTO, quoteOptionItemDTO2)));
            oneOf(pmr).productOffering(ProductSCode.newInstance("sCode"));
            will(returnValue(productOfferings));
            oneOf(productOfferings).get();
            will(returnValue(ProductOfferingFixture.aProductOffering().withIsInFrontCatalogue(true).build()));
            oneOf(productOffering).isInFrontCatalogue();
            will(returnValue(true));
            oneOf(pmr).productOffering(ProductSCode.newInstance("sCode"));
            will(returnValue(productOfferings));
            oneOf(pmr).productOffering(ProductSCode.newInstance("id1"));
            will(returnValue(productOfferings));
            oneOf(pmr).productOffering(ProductSCode.newInstance("id2"));
            will(returnValue(productOfferings));
            allowing(productOfferings).get();
            will(returnValue(ProductOfferingFixture.aProductOffering().withIsInFrontCatalogue(true).build()));
            oneOf(productOffering).isInFrontCatalogue();
            will(returnValue(true));
            ignoring(siteFacade);
            ignoring(expedioClientResources);
        }});

        AddOrModifyProductView view = modifyProductOrchestrator.buildView(CUSTOMER_ID, CONTRACT_ID, "projectId", "quoteOptionId", false, Modify.description());
        assertThat(view.getProjectId(), is("projectId"));
        assertThat(view.getQuoteOptionId(), is("quoteOptionId"));
        assertThat(view.getCurrency(), is("currency"));
        assertThat(view.getName(), is("name"));
        assertThat(view.getProductConfiguratorUriFactory(), is(productConfiguratorUriFactory));
        assertThat(view.getQuoteOptionItemsSize(), is("2"));
    }

    @Test
    public void shouldAddModifyNoSitesGivenEmptySiteList() throws Exception {
        context.checking(new Expectations() {{
            oneOf(siteFacade).getAllBranchSites(CUSTOMER_ID, PROJECT_ID);
            will(returnValue(new ArrayList<SiteDTO>()));
            ignoring(quoteOptionFacade);
            ignoring(expedioClientResources);
            allowing(pmr).getCountriesWithSpecialBidPricingType(null);
            will(returnValue(newArrayList()));
        }});

        when(productIdentifierFacade.isProductSpecialBid(null)).thenReturn(false);

        final ProductSitesDTO view = modifyProductOrchestrator.buildSitesView(CUSTOMER_ID, PROJECT_ID, new PaginatedFilter<SiteDTO>() {
            @Override
            public PaginatedFilterResult applyTo(List<SiteDTO> items) {
                return new PaginatedFilterResult(0, items, 0, 1);
            }
        }, null, null, null, Optional.of(""));
        assertThat(view.sites.size(), is(0));

    }

    @Test
    public void shouldAddModifyAllProductsGivenNoProductsInProductList() throws Exception {
        context.checking(new Expectations() {
            {
                oneOf(projectResource).getProject("");
                will(returnValue(aProjectDTO().build()));
                ignoring(siteFacade);
                ignoring(quoteOptionFacade);
                ignoring(expedioClientResources);
                oneOf(pmr).productOffering(ProductSCode.newInstance("id1"));
                will(returnValue(productOfferings));
                oneOf(pmr).productOffering(ProductSCode.newInstance("id2"));
                will(returnValue(productOfferings));
                allowing(productOfferings).get();
            }
        });
        when(productIdentifierFacade.getSalesChannelDto("")).thenReturn(SalesChannelDTO.newInstance("salesChannelName", "salesChannelId", true, null));

        AddOrModifyProductView view = modifyProductOrchestrator.buildView(CUSTOMER_ID, CONTRACT_ID, "", "", false, Modify.description());
        assertThat(view.getProducts().products().size(), is(2));
    }

    @Test
    public void shouldAddModifyProductsGivenProductsInProductList() throws Exception {
        context.checking(new Expectations() {
            {
                oneOf(projectResource).getProject("");
                will(returnValue(aProjectDTO().build()));
                ignoring(siteFacade);
                ignoring(quoteOptionFacade);
                ignoring(expedioClientResources);
                oneOf(pmr).productOffering(ProductSCode.newInstance("id1"));
                will(returnValue(productOfferings));
                oneOf(pmr).productOffering(ProductSCode.newInstance("id2"));
                will(returnValue(productOfferings));
                allowing(productOfferings).get();
            }
        });
        AddOrModifyProductView view = modifyProductOrchestrator.buildView(CUSTOMER_ID, CONTRACT_ID, "", "", false, Modify.description());
        assertThat(view.getProducts().products().size(), is(2));
        assertThat(view.getProducts().getName("id1"), is("product1"));
        assertThat(view.getProducts().getName("id2"), is("product2"));
    }

    @Test
    public void shouldModifyConstructLineItemJsonObject() throws Exception {
        CreateLineItemDTO createLineItemDTO = modifyProductOrchestrator.constructLineItem("rsqeQuoteOption",
                                                                                       "expedioQuoteId",
                                                                                       "expedioCustomerId",
                                                                                       "auth token",
                                                                                       Arrays.asList(new String[]{"1", "2"})
        );

        assertThat(createLineItemDTO.getRsqeQuoteOptionId(), is("rsqeQuoteOption"));
        assertThat(createLineItemDTO.getAuthenticationToken(), is("auth token"));
        assertThat(createLineItemDTO.getExpedioCustomerId(), is("expedioCustomerId"));
        assertThat(createLineItemDTO.getExpedioQuoteId(), is("expedioQuoteId"));
        assertThat(createLineItemDTO.getLineItems().size(), is(2));
    }

    @Test
    public void shouldModifyConstructSiteViewWithProductVersion() throws Exception {
        final SiteDTO uk = new SiteDTO();
        uk.country = "United Kingdom";
        uk.bfgSiteID = "100";

        final SiteDTO india = new SiteDTO();
        india.country = "India";
        india.bfgSiteID = "200";

        final String sCode = "sCode";
        final String productVersion = "productVersion";
        final ProductInstance productInstance1 = mock(ProductInstance.class);
        when(productInstance1.describe(false)).thenReturn("summary");
        final ProductInstance productInstance2 = mock(ProductInstance.class);
        when(productInstance2.describe(false)).thenReturn("summary");

        context.checking(new Expectations() {{
            oneOf(siteFacade).getAllBranchSites(CUSTOMER_ID, PROJECT_ID);
            will(returnValue(newArrayList(uk, india)));
            allowing(siteDriver).get(CUSTOMER_ID, sCode);
            will(returnValue(newArrayList(new BFGAssetIdentifier("100", "A1"), new BFGAssetIdentifier("200", "A2"))));
            allowing(productInstanceClient).getSourceAsset("A1");
            will(returnValue(Optional.of(productInstance1)));
            allowing(productInstanceClient).getSourceAsset("A2");
            will(returnValue(Optional.of(productInstance2)));
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
            allowing(pmr).getSupportedCountries(sCode);
            allowing(pmr).getCountriesWithSpecialBidPricingType(sCode);
            will(returnValue(newArrayList()));
            will(returnValue(newArrayList(uk.getCountryISOCode())));
            allowing(productInstanceClient).get(new SiteId(uk.bfgSiteID), new ProductCode(sCode), new ProductVersion(productVersion));
            will(returnValue(newArrayList(productInstance1)));
            allowing(productInstanceClient).get(new SiteId(india.bfgSiteID), new ProductCode(sCode), new ProductVersion(productVersion));
            will(returnValue(newArrayList()));
        }});

        when(productIdentifierFacade.isProductSpecialBid(sCode)).thenReturn(false);

        ProductSitesDTO addModifyProductSitesView = modifyProductOrchestrator.buildSitesView(CUSTOMER_ID,
                                                                                       PROJECT_ID,
                                                                                       paginatedFilter,
                                                                                       sCode,
                                                                                       null,
                                                                                       null,
                                                                                       Optional.of(productVersion));
        List<ProductSitesDTO.SiteRowDTO> sites = addModifyProductSitesView.sites;

        assertThat(sites.size(), is(2));

        assertThat(sites.get(0).fullAddress, is(new SiteView(uk).getFullAddress()));
        assertThat(sites.get(0).isValidForProduct, is(false));
        assertThat(sites.get(0).summary, is("summary"));
        assertThat(sites.get(1).fullAddress, is(new SiteView(india).getFullAddress()));
        assertThat(sites.get(1).isValidForProduct, is(false));
        assertThat(sites.get(1).summary, is("summary"));
        assertTrue(isEmpty(sites.get(0).newFullAddress));
        assertTrue(isEmpty(sites.get(1).newFullAddress));
    }

    @Test
    public void shouldModifyReturnPricingFilterView() throws Exception {
        context.checking(new Expectations() {{
            allowing(siteFacade).getCountries(CUSTOMER_ID, PROJECT_ID);
            will(returnValue(newArrayList("UK", "India", "France")));
            oneOf(projectResource).getProject("projectId");
            will(returnValue(aProjectDTO().build()));
            ignoring(quoteOptionFacade);
            ignoring(expedioClientResources);
            oneOf(pmr).productOffering(ProductSCode.newInstance("id1"));
            will(returnValue(productOfferings));
            oneOf(pmr).productOffering(ProductSCode.newInstance("id2"));
            will(returnValue(productOfferings));
            allowing(productOfferings).get();
        }});

        AddOrModifyProductView addModifyProductView = modifyProductOrchestrator.buildView(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID, false, Modify.description());

        assertThat(addModifyProductView.getCountries().size(), is(3));
        assertThat(addModifyProductView.getCountries(), hasItems("UK", "India", "France"));
    }

    @Test
    public void shouldModifyNoServicesGivenEmptyServiceList() throws Exception {
        context.checking(new Expectations() {{
            oneOf(productInstanceClient).getInServiceAssets(new CustomerId(CUSTOMER_ID), new ContractId(CONTRACT_ID), new ProductCode("productCode"), new ProductVersion("productVersion"));
            will(returnValue(newArrayList()));
        }});
        Pagination pagination = new DefaultPagination(0, 0, 1);
        final ProductServiceDTO view = modifyProductOrchestrator.buildServicesView(CUSTOMER_ID, "productCode", CONTRACT_ID, "productVersion", pagination);
        assertTrue(view.services.isEmpty());
    }

    @Test
    public void shouldModifyServicesGivenServiceList() throws Exception {
        final Attribute attribute1 = anAttribute().called("attr 1","ATTR_1").withBehaviours(aBehaviour().withVisibleInSummaryStrategy().build()).build();
        final Attribute attribute2 = anAttribute().called("attr 2","ATTR_2").withBehaviours(aBehaviour().withHiddenBehaviourStrategy().build()).build();
        HashMap<AttributeName, InstanceCharacteristic> instanceCharacteristicHashMap = new HashMap<AttributeName, InstanceCharacteristic>();
        instanceCharacteristicHashMap.put(new AttributeName("attr 1","ATTR_1"), attribute1.newInstance());
        instanceCharacteristicHashMap.put(new AttributeName("attr 2","ATTR_2"), attribute2.newInstance());
        final ProductInstance mockProductInstance = aProductInstance().withProductOffering(aProductOffering()
                                                                                               .withAttribute(attribute1).withAttribute(attribute2))
            .withInstanceCharacteristics(instanceCharacteristicHashMap).build();

        context.checking(new Expectations() {{
            oneOf(productInstanceClient).getInServiceAssets(new CustomerId(CUSTOMER_ID), new ContractId(CONTRACT_ID), new ProductCode("productCode"), new ProductVersion("productVersion"));
            will(returnValue(newArrayList(mockProductInstance)));
        }});
        Pagination pagination = new DefaultPagination(0, 0, 1);
        final ProductServiceDTO view = modifyProductOrchestrator.buildServicesView(CUSTOMER_ID, "productCode", CONTRACT_ID, "productVersion", pagination);
        assertFalse(view.services.isEmpty());
        assertThat(view.services.size(), is(1));
        ServiceDTO serviceDTO = view.services.get(0);
        assertThat(serviceDTO.attributes.size(), is(1));
    }

    @Test
    public void shouldAddSiteWithDetailsInSiteList() throws Exception {

        //Given
        SiteFacade siteFacade = mock(SiteFacade.class);
        ProductIdentifierFacade productIdentifierFacade = mock(ProductIdentifierFacade.class);
        SiteDriver siteDriver = mock(SiteDriver.class);
        QuoteOptionFacade quoteOptionFacade = mock(QuoteOptionFacade.class);
        Pmr pmr = mock(Pmr.class);
        ProductInstanceClient productInstanceClient = mock(ProductInstanceClient.class);
        ExpedioProjectResource projectResource = mock(ExpedioProjectResource.class);
        ExpedioClientResources expedioClientResources = mock(ExpedioClientResources.class);
        UriFactory uriFactory = mock(UriFactory.class);
        PaginatedFilter paginatedFilter = mock(PaginatedFilter.class);
        Pmr.ProductOfferings productOfferings = mock(Pmr.ProductOfferings.class);

        ModifyProductOrchestrator modifyProductOrchestrator = new ModifyProductOrchestrator(siteFacade,
                productIdentifierFacade,
                uriFactory,
                quoteOptionFacade,
                projectResource,
                pmr,
                siteDriver,
                expedioClientResources,
                productInstanceClient);

        SiteDTO siteDTO_1 = SiteDTOFixture.aSiteDTO().withBfgSiteId("1").withCountryISOCode("GB").build();
        SiteDTO siteDTO_2 = SiteDTOFixture.aSiteDTO().withBfgSiteId("2").withCountryISOCode("GB").build();
        SiteDTO siteDTO_3 = SiteDTOFixture.aSiteDTO().withBfgSiteId("3").withCountryISOCode("GB").build();
        when(siteFacade.getAllBranchSites("aCustomerId", "aProjectId")).thenReturn(newArrayList(siteDTO_1, siteDTO_2, siteDTO_3));
        when(siteDriver.get("aCustomerId", "S123")).thenReturn(newArrayList(new BFGAssetIdentifier("1", "100"), new BFGAssetIdentifier("2", "200")));

        AssetDTO assetDTO_1 = new AssetDTOFixture().withId("1").withProductCode(new ProductCode("S123")).withAssetCharacteristic("A", "One")
                                                                                                        .withAssetCharacteristic("B", "Two")
                                                                                                        .withAssetCharacteristic("STENCIL", "S234").build();
        AssetDTO assetDTO_2 = new AssetDTOFixture().withId("2").withProductCode(new ProductCode("S123")).withAssetCharacteristic("C", "Three")
                                                                                                        .withAssetCharacteristic("D", "Four")
                                                                                                        .withAssetCharacteristic("STENCIL", "").build();

        PaginatedFilterResult paginatedFilterResult = mock(PaginatedFilterResult.class);

        when(pmr.productOffering(ProductSCode.newInstance("S123"))).thenReturn(productOfferings);
        when(productOfferings.withStencil(org.mockito.Matchers.any(StencilId.class))).thenReturn(productOfferings);
        when(productOfferings.get()).thenReturn(new ProductOfferingFixture("S123").withAttribute(aVisibleInSummaryAttribute().called("A").build())
                .withAttribute(aReadOnlyAttribute().called("B").build()).withAttribute(aVisibleInSummaryAttribute().called("C").build())
                .withAttribute(aReadOnlyAttribute().called("D").build()).withAttribute(aVisibleInSummaryAttribute().called("STENCIL").build()).build());

                when(productInstanceClient.getSourceAssetDTO("100")).thenReturn(Optional.of(assetDTO_1));
        when(productInstanceClient.getSourceAssetDTO("200")).thenReturn(Optional.of(assetDTO_2));

        when(productIdentifierFacade.isProductSpecialBid("S123")).thenReturn(false);
        when(pmr.getSupportedCountries("S123")).thenReturn(Collections.<String>emptyList());
        when(pmr.getCountriesWithSpecialBidPricingType("S123")).thenReturn(Collections.<String>emptyList());

        when(paginatedFilter.applyTo(anyList())).thenReturn(paginatedFilterResult);
        when(paginatedFilterResult.getPageNumber()).thenReturn(1);
        when(paginatedFilterResult.getFilteredSize()).thenReturn(2);
        when(paginatedFilterResult.getItems()).thenReturn(newArrayList(siteDTO_1, siteDTO_2));
        when(paginatedFilterResult.getTotalRecords()).thenReturn(2);

        //When
        ProductSitesDTO productSitesDTO = modifyProductOrchestrator.buildSitesView("aCustomerId", "aProjectId", paginatedFilter, "S123", "", Collections.<String>emptyList(), Optional.of("A.1"));

        //Then
        Assert.assertThat(productSitesDTO, notNullValue());
        Assert.assertThat(productSitesDTO.sites.size(), is(2));
    }

    @Test
    public void shouldNotAddSiteWhoseAssetIsAProblematicOneInSiteList() throws Exception {

        //Given
        SiteFacade siteFacade = mock(SiteFacade.class);
        ProductIdentifierFacade productIdentifierFacade = mock(ProductIdentifierFacade.class);
        SiteDriver siteDriver = mock(SiteDriver.class);
        QuoteOptionFacade quoteOptionFacade = mock(QuoteOptionFacade.class);
        Pmr pmr = mock(Pmr.class);
        ProductInstanceClient productInstanceClient = mock(ProductInstanceClient.class);
        ExpedioProjectResource projectResource = mock(ExpedioProjectResource.class);
        ExpedioClientResources expedioClientResources = mock(ExpedioClientResources.class);
        UriFactory uriFactory = mock(UriFactory.class);
        PaginatedFilter paginatedFilter = mock(PaginatedFilter.class);
        Pmr.ProductOfferings productOfferings = mock(Pmr.ProductOfferings.class);

        ModifyProductOrchestrator modifyProductOrchestrator = new ModifyProductOrchestrator(siteFacade,
                productIdentifierFacade,
                uriFactory,
                quoteOptionFacade,
                projectResource,
                pmr,
                siteDriver,
                expedioClientResources,
                productInstanceClient);

        SiteDTO siteDTO_1 = SiteDTOFixture.aSiteDTO().withBfgSiteId("1").withCountryISOCode("GB").build();
        SiteDTO siteDTO_2 = SiteDTOFixture.aSiteDTO().withBfgSiteId("2").withCountryISOCode("GB").build();
        SiteDTO siteDTO_3 = SiteDTOFixture.aSiteDTO().withBfgSiteId("3").withCountryISOCode("GB").build();
        when(siteFacade.getAllBranchSites("aCustomerId", "aProjectId")).thenReturn(newArrayList(siteDTO_1, siteDTO_2, siteDTO_3));
        when(siteDriver.get("aCustomerId", "S123")).thenReturn(newArrayList(new BFGAssetIdentifier("1", "100"), new BFGAssetIdentifier("2", "200")));

        AssetDTO assetDTO_1 = new AssetDTOFixture().withId("1").withProductCode(new ProductCode("S123")).withAssetCharacteristic("A", "One")
                .withAssetCharacteristic("B", "Two")
                .withAssetCharacteristic("STENCIL", "S234").build();
        AssetDTO assetDTO_2 = new AssetDTOFixture().withId("2").withProductCode(new ProductCode("S123")).withAssetCharacteristic("C", "Three")
                .withAssetCharacteristic("D", "Four")
                .withAssetCharacteristic("STENCIL", "").build();

        PaginatedFilterResult paginatedFilterResult = mock(PaginatedFilterResult.class);

        when(pmr.productOffering(ProductSCode.newInstance("S123"))).thenReturn(productOfferings);
        when(productOfferings.withStencil(org.mockito.Matchers.any(StencilId.class))).thenReturn(productOfferings);
        when(productOfferings.get()).thenReturn(new ProductOfferingFixture("S123").withAttribute(aVisibleInSummaryAttribute().called("A").build())
                .withAttribute(aReadOnlyAttribute().called("B").build()).withAttribute(aVisibleInSummaryAttribute().called("C").build())
                .withAttribute(aReadOnlyAttribute().called("D").build()).withAttribute(aVisibleInSummaryAttribute().called("STENCIL").build()).build());

        when(productInstanceClient.getSourceAssetDTO("100")).thenReturn(Optional.of(assetDTO_1));
        when(productInstanceClient.getSourceAssetDTO("200")).thenThrow(Exception.class);

        when(productIdentifierFacade.isProductSpecialBid("S123")).thenReturn(false);
        when(pmr.getSupportedCountries("S123")).thenReturn(Collections.<String>emptyList());
        when(pmr.getCountriesWithSpecialBidPricingType("S123")).thenReturn(Collections.<String>emptyList());

        when(paginatedFilter.applyTo(anyList())).thenReturn(paginatedFilterResult);
        when(paginatedFilterResult.getPageNumber()).thenReturn(1);
        when(paginatedFilterResult.getFilteredSize()).thenReturn(2);
        when(paginatedFilterResult.getItems()).thenReturn(newArrayList(siteDTO_1, siteDTO_2));
        when(paginatedFilterResult.getTotalRecords()).thenReturn(2);

        //When
        ProductSitesDTO productSitesDTO = modifyProductOrchestrator.buildSitesView("aCustomerId", "aProjectId", paginatedFilter, "S123", "", Collections.<String>emptyList(), Optional.of("A.1"));

        //Then
        Assert.assertThat(productSitesDTO, notNullValue());
        Assert.assertThat(productSitesDTO.sites.size(), is(1));
        ProductSitesDTO.SiteRowDTO siteRowDTO = productSitesDTO.sites.get(0);
        Assert.assertThat(siteRowDTO.id, is("1"));
        Assert.assertThat(siteRowDTO.summary, is("One,S234"));
        Assert.assertThat(siteRowDTO.sourceLineItemId, notNullValue());
        Assert.assertThat(siteRowDTO.sourceQuoteOptionId, notNullValue());
    }


}


