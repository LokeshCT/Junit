package com.bt.rsqe.projectengine.web.quoteoptiondetails;

import com.bt.rsqe.client.InstanceClient;
import com.bt.rsqe.client.Pmr;
import com.bt.rsqe.container.ApplicationConfig;
import com.bt.rsqe.customerinventory.client.ProductInstanceClient;
import com.bt.rsqe.customerinventory.filter.AssetFilter;
import com.bt.rsqe.customerinventory.parameter.ContractId;
import com.bt.rsqe.customerinventory.parameter.CustomerId;
import com.bt.rsqe.customerinventory.parameter.ProductCode;
import com.bt.rsqe.customerinventory.parameter.ProductVersion;
import com.bt.rsqe.customerinventory.parameter.SiteId;
import com.bt.rsqe.customerrecord.CustomerDTO;
import com.bt.rsqe.customerrecord.CustomerResource;
import com.bt.rsqe.customerrecord.ExpedioClientResources;
import com.bt.rsqe.customerrecord.SiteDTO;
import com.bt.rsqe.customerrecord.SiteResource;
import com.bt.rsqe.domain.AbstractNotificationEvent;
import com.bt.rsqe.domain.AvailableAsset;
import com.bt.rsqe.domain.ErrorNotificationEvent;
import com.bt.rsqe.domain.Notification;
import com.bt.rsqe.domain.bom.fixtures.ProductOfferingFixture;
import com.bt.rsqe.domain.bom.fixtures.SellableProductFixture;
import com.bt.rsqe.domain.bom.parameters.ProductSCode;
import com.bt.rsqe.domain.product.Cardinality;
import com.bt.rsqe.domain.product.DefaultProductInstanceFixture;
import com.bt.rsqe.domain.product.PrerequisiteUrl;
import com.bt.rsqe.domain.product.ProductOffering;
import com.bt.rsqe.domain.product.SellableProduct;
import com.bt.rsqe.domain.product.extensions.Expression;
import com.bt.rsqe.domain.product.extensions.ExpressionExpectedResultType;
import com.bt.rsqe.domain.product.parameters.ProductIdentifier;
import com.bt.rsqe.domain.project.ProductInstance;
import com.bt.rsqe.enums.MoveConfigurationTypeEnum;
import com.bt.rsqe.expedio.project.ExpedioProjectResource;
import com.bt.rsqe.pmr.dto.SalesChannelDTO;
import com.bt.rsqe.projectengine.QuoteOptionDTO;
import com.bt.rsqe.projectengine.QuoteOptionItemDTO;
import com.bt.rsqe.projectengine.QuoteOptionItemDTOFixture;
import com.bt.rsqe.projectengine.web.CreateLineItemDTO;
import com.bt.rsqe.projectengine.web.facades.ProductIdentifierFacade;
import com.bt.rsqe.projectengine.web.facades.QuoteOptionFacade;
import com.bt.rsqe.projectengine.web.facades.SiteFacade;
import com.bt.rsqe.projectengine.web.quoteoption.validation.SiteValidator;
import com.bt.rsqe.projectengine.web.uri.UriFactory;
import com.bt.rsqe.projectengine.web.uri.UriFactoryImpl;
import com.bt.rsqe.projectengine.web.view.AddOrModifyProductView;
import com.bt.rsqe.projectengine.web.view.ProductSitesDTO;
import com.bt.rsqe.projectengine.web.view.Products;
import com.bt.rsqe.projectengine.web.view.SiteView;
import com.bt.rsqe.projectengine.web.view.filtering.PaginatedFilter;
import com.bt.rsqe.projectengine.web.view.filtering.PaginatedFilterResult;
import com.bt.rsqe.security.UserContext;
import com.bt.rsqe.security.UserContextManager;
import com.google.common.base.Optional;
import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static com.bt.rsqe.enums.ProductAction.*;
import static com.bt.rsqe.expedio.fixtures.ProjectDTOFixture.*;
import static com.bt.rsqe.factory.ServiceLocator.serviceLocatorInstance;
import static com.bt.rsqe.security.UserContextBuilder.aDirectUserContext;
import static com.google.common.collect.Lists.*;
import static junit.framework.Assert.*;
import static org.apache.axis.utils.StringUtils.isEmpty;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

public class AddProductOrchestratorTest {

    public static final String CUSTOMER_ID = "customerId";
    public static final String CONTRACT_ID = "CONTRACT_ID";
    public static final String PROJECT_ID = "projectId";
    public static final String QUOTE_OPTION_ID = "quoteOptionId";
    public static final String PRODUCT_ID = "S0308491";
    public static final String ID_1 = "id1";
    public static final String ID_2 = "id2";
    private String PRODUCT_VERSION = "A.33";
    private static final String SITE_ID = "12345";
    private CustomerDTO customerDTO = new CustomerDTO(CUSTOMER_ID, "ABC LTD", "UK BT");
    private SiteDTO siteDTO = new SiteDTO(SITE_ID, "LONDON");
    private final static List<String> PRODUCT_SITES_SELECTED_LIST = new ArrayList<String>() {{add(SITE_ID);}};
    private JUnit4Mockery context;
    private SiteFacade siteFacade;
    private AddProductOrchestrator addProductOrchestrator;
    private ProductIdentifierFacade productIdentifierFacade;
    private PaginatedFilter<SiteDTO> paginatedFilter;
    private PaginatedFilterResult<SiteDTO> filterResult;
    private UriFactory productConfiguratorUriFactory = new UriFactoryImpl(null);
    private QuoteOptionFacade quoteOptionFacade;
    private ExpedioProjectResource projectResource;
    private ApplicationConfig configuration;
    private Pmr pmr;
    private ExpedioClientResources expedioClientResources;
    private ArrayList productCategoryCodesList = new ArrayList<String>();
    private List<SellableProduct> productList = new ArrayList<SellableProduct>();
    private ArrayList emptyProductCategoryCodesList = new ArrayList<String>();
    private SellableProduct sellableProduct;
    private SellableProduct secondSellableProduct;
    private Products products;
    private Optional<ProductIdentifier> hCode;
    private ProductInstanceClient productInstanceClient;
    private Pmr.ProductOfferings productOfferings;
    private CustomerResource customerResource;
    private SiteResource siteResource;
    private QuoteOptionItemDTO quoteOptionItemDTO;
    private QuoteOptionItemDTO quoteOptionItemDTO2;
    private InstanceClient instanceClient ;
    private ProductOffering productOffering;
    private SiteValidator siteValidator;


    @Before
    public void before() throws Exception {
        context = new JUnit4Mockery() {{
            setImposteriser(ClassImposteriser.INSTANCE);
        }};

        pmr = context.mock(Pmr.class);
        siteFacade = context.mock(SiteFacade.class);
        productIdentifierFacade = mock(ProductIdentifierFacade.class);
        paginatedFilter = context.mock(PaginatedFilter.class);
        filterResult = context.mock(PaginatedFilterResult.class);
        quoteOptionFacade = context.mock(QuoteOptionFacade.class);
        projectResource = context.mock(ExpedioProjectResource.class);
        expedioClientResources = context.mock(ExpedioClientResources.class);
        configuration = context.mock(ApplicationConfig.class);
        productInstanceClient = context.mock(ProductInstanceClient.class);
        productOfferings = context.mock(Pmr.ProductOfferings.class);
        productOffering = context.mock(ProductOffering.class);
        customerResource = context.mock(CustomerResource.class);
        siteResource = context.mock(SiteResource.class);
        siteValidator = context.mock(SiteValidator.class);
        productCategoryCodesList.add("H");
        instanceClient = context.mock(InstanceClient.class);
        sellableProduct = SellableProductFixture.aProduct().withId(ID_1).withName("product1").withSiteInstallable(true).withPrerequisiteUrl(new PrerequisiteUrl("", "")).withMoveConfigurationType(MoveConfigurationTypeEnum.NOT_MOVEABLE).withIsImportable(false).withIsSeparatelyModifiable(false).build();
        secondSellableProduct = SellableProductFixture.aProduct().withId(ID_2).withName("product2").withSiteInstallable(true).withPrerequisiteUrl(new PrerequisiteUrl("", "")).withMoveConfigurationType(MoveConfigurationTypeEnum.COPY_ALL).withIsImportable(false).withIsSeparatelyModifiable(false).build();
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
            allowing(pmr).productOffering(ProductSCode.newInstance(PRODUCT_ID));
            will(returnValue(productOfferings));
            allowing(pmr).productOffering(ProductSCode.newInstance(ID_1));
            will(returnValue(productOfferings));
            allowing(pmr).productOffering(ProductSCode.newInstance(ID_2));
            will(returnValue(productOfferings));

            allowing(siteValidator).validateSite(siteDTO);
            will(returnValue(newArrayList()));

        }});
        when(productIdentifierFacade.getSalesChannelDto("")).thenReturn(SalesChannelDTO.newInstance("salesChannelName", "salesChannelId", true, productCategoryCodesList));
        when(productIdentifierFacade.getSellableProductsForSalesChannel("")).thenReturn(products);
        when(productIdentifierFacade.getProductHCode(sellableProduct.getProductId())).thenReturn(hCode);
        when(productIdentifierFacade.getProductHCode(secondSellableProduct.getProductId())).thenReturn(hCode);
        addProductOrchestrator = new AddProductOrchestrator(siteFacade,
                                                            productIdentifierFacade,
                                                            productConfiguratorUriFactory,
                                                            quoteOptionFacade,
                                                            projectResource,
                                                            pmr, expedioClientResources, productInstanceClient,
                                                            siteValidator);
        quoteOptionItemDTO = QuoteOptionItemDTOFixture.aQuoteOptionItemDTO().build();
        quoteOptionItemDTO2 = QuoteOptionItemDTOFixture.aQuoteOptionItemDTO().build();

        UserContext userContext = aDirectUserContext().withToken("aToken").build();
        UserContextManager.setCurrent(userContext);

        serviceLocatorInstance().register(instanceClient);

    }

    @Test
    public void shouldProperlyMapViewObject() throws Exception {
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
            oneOf(productOfferings).get();
            will(returnValue(ProductOfferingFixture.aProductOffering().withIsInFrontCatalogue(false).build()));
            oneOf(productOffering).isInFrontCatalogue();
            will(returnValue(false));

            oneOf(pmr).productOffering(ProductSCode.newInstance(ID_1));
            will(returnValue(productOfferings));
            oneOf(productOfferings).get();
            will(returnValue(ProductOfferingFixture.aProductOffering().withIsInFrontCatalogue(false).withIsSeparatelyModifiable(false).build()));
            oneOf(productOffering).isSeparatelyModifiable();
            will(returnValue(false));

            oneOf(pmr).productOffering(ProductSCode.newInstance(ID_2));
            will(returnValue(productOfferings));
            oneOf(productOfferings).get();
            will(returnValue(ProductOfferingFixture.aProductOffering().withIsInFrontCatalogue(false).withIsSeparatelyModifiable(false).build()));
            oneOf(productOffering).isSeparatelyModifiable();
            will(returnValue(false));
            ignoring(siteFacade);
            ignoring(expedioClientResources);

        }});
        AddOrModifyProductView addProductView = addProductOrchestrator.buildView(CUSTOMER_ID, CONTRACT_ID, "projectId", "quoteOptionId", false, Provide.description());
        assertThat(addProductView.getProjectId(), is("projectId"));
        assertThat(addProductView.getQuoteOptionId(), is("quoteOptionId"));
        assertThat(addProductView.getCurrency(), is("currency"));
        assertThat(addProductView.getName(), is("name"));
        assertThat(addProductView.getProductConfiguratorUriFactory(), is(productConfiguratorUriFactory));
        assertThat(addProductView.getQuoteOptionItemsSize(), is("1"));
    }

    @Test
    public void shouldAddProductNoSitesGivenNullProductSCode() throws Exception {
        context.checking(new Expectations() {{
            oneOf(siteFacade).getAllBranchSites(CUSTOMER_ID, PROJECT_ID);
            will(returnValue(new ArrayList<SiteDTO>()));
            ignoring(quoteOptionFacade);
            ignoring(expedioClientResources);
            allowing(pmr).getCountriesWithSpecialBidPricingType(null);
            will(returnValue(newArrayList()));
        }});

        when(productIdentifierFacade.isProductSpecialBid(null)).thenReturn(false);

        final ProductSitesDTO view = addProductOrchestrator.buildSitesView(CUSTOMER_ID, PROJECT_ID, new PaginatedFilter<SiteDTO>() {
            @Override
            public PaginatedFilterResult applyTo(List<SiteDTO> items) {
                return new PaginatedFilterResult(0, items, 0, 1);
            }
        }, null, null, null, Optional.<String>absent());
        assertThat(view.sites.size(), is(0));

    }

    @Test
    public void shouldAddNoSitesGivenEmptySiteList() throws Exception {
        final String sCode = "sCode";
        context.checking(new Expectations() {{
            oneOf(siteFacade).getAllBranchSites(CUSTOMER_ID, PROJECT_ID);
            will(returnValue(new ArrayList<SiteDTO>()));
            ignoring(quoteOptionFacade);
            allowing(pmr).getSupportedCountries(sCode);
            will(returnValue(newArrayList()));
            allowing(pmr).getCountriesWithSpecialBidPricingType(sCode);
            will(returnValue(newArrayList()));

        }});

        when(productIdentifierFacade.isProductSpecialBid(sCode)).thenReturn(false);

        final ProductSitesDTO view = addProductOrchestrator.buildSitesView(CUSTOMER_ID, PROJECT_ID, new PaginatedFilter<SiteDTO>() {
            @Override
            public PaginatedFilterResult applyTo(List<SiteDTO> items) {
                return new PaginatedFilterResult(0, items, 0, 1);
            }
        }, null, null, null, Optional.of(PRODUCT_VERSION));
        assertThat(view.sites.size(), is(0));

    }

    @Test
    public void shouldAddAllProductsGivenNoProductsInProductList() throws Exception {
        context.checking(new Expectations() {
            {
                oneOf(projectResource).getProject("");
                will(returnValue(aProjectDTO().build()));
                oneOf(pmr).productOffering(ProductSCode.newInstance(ID_1));
                will(returnValue(productOfferings));
                oneOf(productOfferings).get();
                will(returnValue(ProductOfferingFixture.aProductOffering().withIsInFrontCatalogue(false).withIsSeparatelyModifiable(false).build()));
                oneOf(productOffering).isSeparatelyModifiable();
                will(returnValue(false));

                oneOf(pmr).productOffering(ProductSCode.newInstance(ID_2));
                will(returnValue(productOfferings));
                oneOf(productOfferings).get();
                will(returnValue(ProductOfferingFixture.aProductOffering().withIsInFrontCatalogue(false).withIsSeparatelyModifiable(false).build()));
                oneOf(productOffering).isSeparatelyModifiable();
                will(returnValue(false));
                ignoring(siteFacade);
                ignoring(quoteOptionFacade);
                ignoring(expedioClientResources);

            }
        });
        when(productIdentifierFacade.getSalesChannelDto("")).thenReturn(SalesChannelDTO.newInstance("salesChannelName", "salesChannelId", true, null));

        final AddOrModifyProductView view = addProductOrchestrator.buildView(CUSTOMER_ID, CONTRACT_ID, "", "", false, Provide.description());
        assertThat(view.getProducts().products().size(), is(2));
    }

    @Test
    public void shouldAddProductsGivenProductsInProductList() throws Exception {
        context.checking(new Expectations() {
            {
                oneOf(projectResource).getProject("");
                will(returnValue(aProjectDTO().build()));
                oneOf(pmr).productOffering(ProductSCode.newInstance(ID_1));
                will(returnValue(productOfferings));
                oneOf(productOfferings).get();
                will(returnValue(ProductOfferingFixture.aProductOffering().withIsInFrontCatalogue(false).withIsSeparatelyModifiable(false).build()));
                oneOf(productOffering).isSeparatelyModifiable();
                will(returnValue(false));

                oneOf(pmr).productOffering(ProductSCode.newInstance(ID_2));
                will(returnValue(productOfferings));
                oneOf(productOfferings).get();
                will(returnValue(ProductOfferingFixture.aProductOffering().withIsInFrontCatalogue(false).withIsSeparatelyModifiable(false).build()));
                oneOf(productOffering).isSeparatelyModifiable();
                will(returnValue(false));
                ignoring(siteFacade);
                ignoring(quoteOptionFacade);
                ignoring(expedioClientResources);

            }
        });
        final AddOrModifyProductView view = addProductOrchestrator.buildView(CUSTOMER_ID, CONTRACT_ID, "", "", false, Provide.description());
        assertThat(view.getProducts().products().size(), is(2));
        assertThat(view.getProducts().getName(ID_1), is("product1"));
        assertThat(view.getProducts().getName(ID_2), is("product2"));
    }

    @Test
    public void shouldConstructLineItemJsonObject() throws Exception {
        CreateLineItemDTO createLineItemDTO = addProductOrchestrator.constructLineItem("rsqeQuoteOption",
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
    public void shouldReturnLaunchedValueFromPMR() {
        final String salesChannel = "TEST CHANNEL";
        final String expected = "Yes";
        context.checking(new Expectations() {{
            oneOf(pmr).getLaunched(salesChannel, "SCode");

            will(returnValue("Yes"));
        }});
        String returnValue = addProductOrchestrator.getLaunched(salesChannel, "SCode");
        assertThat(returnValue, is(expected));
    }

    @Test
    public void shouldConstructSiteViewForStandardProduct() throws Exception {
        final SiteDTO uk = new SiteDTO();
        uk.country = "United Kingdom";

        final SiteDTO india = new SiteDTO();
        india.country = "India";

        final String sCode = "sCode";

        context.checking(new Expectations() {{
            oneOf(siteFacade).getAllBranchSites(CUSTOMER_ID, PROJECT_ID);
            will(returnValue(newArrayList(uk, india)));
            oneOf(paginatedFilter).applyTo(newArrayList(uk, india));
            will(returnValue(filterResult));
            oneOf(filterResult).getPageNumber();
            will(returnValue(0));
            oneOf(filterResult).getTotalRecords();
            will(returnValue(2));
            oneOf(filterResult).getFilteredSize();
            will(returnValue(1));
            allowing(filterResult).getItems();
            will(returnValue(newArrayList(uk, india)));
            allowing(pmr).getSupportedCountries(sCode);
            will(returnValue(newArrayList(uk.getCountryISOCode())));
            allowing(pmr).getCountriesWithSpecialBidPricingType(sCode);
            will(returnValue(newArrayList()));
            oneOf(siteValidator).validateSite(uk);
            will(returnValue(newArrayList()));
            oneOf(siteValidator).validateSite(india);
            will(returnValue(newArrayList()));
        }});

        when(productIdentifierFacade.isProductSpecialBid(sCode)).thenReturn(false);

        ProductSitesDTO productSitesView = addProductOrchestrator.buildSitesView(CUSTOMER_ID,
                                                                                 PROJECT_ID,
                                                                                 paginatedFilter,
                                                                                 sCode,
                                                                                 null,
                                                                                 null,
                                                                                 Optional.<String>absent());
        List<ProductSitesDTO.SiteRowDTO> sites = productSitesView.sites;

        assertThat(sites.size(), is(2));

        assertThat(sites.get(0).fullAddress, is(new SiteView(uk).getFullAddress()));
        assertThat(sites.get(0).isValidForProduct, is(true));
        assertThat(sites.get(0).isValidForSpecialBidProduct, is(false));
        assertThat(sites.get(0).isSpecialBidProduct, is(false));
        assertThat(sites.get(1).fullAddress, is(new SiteView(india).getFullAddress()));
        assertThat(sites.get(1).isValidForProduct, is(false));
        assertThat(sites.get(0).isValidForSpecialBidProduct, is(false));
        assertThat(sites.get(0).isSpecialBidProduct, is(false));
        assertTrue(isEmpty(sites.get(0).newFullAddress));
        assertTrue(isEmpty(sites.get(1).newFullAddress));
    }

    @Test
    public void shouldConstructSpecialBidSiteViewWithNoProductVersionSet() throws Exception {
        final SiteDTO uk = new SiteDTO();
        uk.country = "United Kingdom";

        final SiteDTO india = new SiteDTO();
        india.country = "India";

        final String sCode = "sCode";

        context.checking(new Expectations() {{
            oneOf(siteFacade).getAllBranchSites(CUSTOMER_ID, PROJECT_ID);
            will(returnValue(newArrayList(uk, india)));
            oneOf(paginatedFilter).applyTo(newArrayList(uk, india));
            will(returnValue(filterResult));
            oneOf(filterResult).getPageNumber();
            will(returnValue(0));
            oneOf(filterResult).getTotalRecords();
            will(returnValue(2));
            oneOf(filterResult).getFilteredSize();
            will(returnValue(1));
            allowing(filterResult).getItems();
            will(returnValue(newArrayList(uk, india)));
            allowing(pmr).getSupportedCountries(sCode);
            will(returnValue(newArrayList()));
            allowing(pmr).getCountriesWithSpecialBidPricingType(sCode);
            will(returnValue(newArrayList(uk.getCountryISOCode())));
            oneOf(siteValidator).validateSite(uk);
            will(returnValue(newArrayList()));
            oneOf(siteValidator).validateSite(india);
            will(returnValue(newArrayList()));
        }});

        when(productIdentifierFacade.isProductSpecialBid(sCode)).thenReturn(true);

        ProductSitesDTO productSitesView = addProductOrchestrator.buildSitesView(CUSTOMER_ID,
                                                                                 PROJECT_ID,
                                                                                 paginatedFilter,
                                                                                 sCode,
                                                                                 null,
                                                                                 null,
                                                                                 null);
        List<ProductSitesDTO.SiteRowDTO> sites = productSitesView.sites;

        assertThat(sites.size(), is(2));

        assertThat(sites.get(0).fullAddress, is(new SiteView(uk).getFullAddress()));
        assertThat(sites.get(0).isValidForProduct, is(false));
        assertThat(sites.get(0).isValidForSpecialBidProduct, is(true));
        assertThat(sites.get(0).isSpecialBidProduct, is(true));
        assertThat(sites.get(1).fullAddress, is(new SiteView(india).getFullAddress()));
        assertThat(sites.get(1).isValidForProduct, is(false));
        assertThat(sites.get(0).isValidForSpecialBidProduct, is(true));
        assertThat(sites.get(0).isSpecialBidProduct, is(true));
    }

    @Test
    public void shouldReturnPricingFilterView() throws Exception {
        context.checking(new Expectations() {{
            allowing(siteFacade).getCountries(CUSTOMER_ID, PROJECT_ID);
            will(returnValue(newArrayList("United Kingdom", "India", "France")));
            oneOf(projectResource).getProject("projectId");
            will(returnValue(aProjectDTO().build()));
            oneOf(pmr).productOffering(ProductSCode.newInstance(ID_1));
            will(returnValue(productOfferings));
            oneOf(productOfferings).get();
            will(returnValue(ProductOfferingFixture.aProductOffering().withIsInFrontCatalogue(false).withIsSeparatelyModifiable(false).build()));
            oneOf(productOffering).isSeparatelyModifiable();
            will(returnValue(false));

            oneOf(pmr).productOffering(ProductSCode.newInstance(ID_2));
            will(returnValue(productOfferings));
            oneOf(productOfferings).get();
            will(returnValue(ProductOfferingFixture.aProductOffering().withIsInFrontCatalogue(false).withIsSeparatelyModifiable(false).build()));
            oneOf(productOffering).isSeparatelyModifiable();
            will(returnValue(false));
            ignoring(quoteOptionFacade);
            ignoring(expedioClientResources);


        }});

        AddOrModifyProductView addProductView = addProductOrchestrator.buildView(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID, false, Provide.description());

        assertThat(addProductView.getCountries().size(), is(3));
        assertThat(addProductView.getCountries(), hasItems("United Kingdom", "India", "France"));
    }

    @Test
    public void shouldReturnErrorNotificationUponFailCardinalityCheck() {
        final ProductOffering productOffering = ProductOfferingFixture.aProductOffering().withContractCardinality(new Cardinality(0, 1, null)).build();
        context.checking(new Expectations() {{
            oneOf(productOfferings).get();
            will(returnValue(productOffering));
            oneOf(expedioClientResources).getCustomerResource();
            will(returnValue(customerResource));
            oneOf(customerResource).getByToken(CUSTOMER_ID, "aToken");
            will(returnValue(customerDTO));
            oneOf(productInstanceClient).getContractAssets(with(new CustomerId(CUSTOMER_ID)),
                                                           with(new ContractId(CONTRACT_ID)),
                                                           with(new ProductCode(PRODUCT_ID)),
                                                           with(new ProductVersion(PRODUCT_VERSION)),
                                                           with(any(AssetFilter[].class)));
            will(returnValue(newArrayList(new AvailableAsset("assetId", 1L))));
        }});

        Notification notification = addProductOrchestrator.contractCardinalityCheck(CUSTOMER_ID, CONTRACT_ID, PRODUCT_ID, PRODUCT_VERSION, QUOTE_OPTION_ID, 1);

        assertTrue(notification.hasErrors());
        assertThat(notification.getErrorEvents().size(), is(1));
        assertThat(notification.getErrorEvents(),
                   hasItem(((AbstractNotificationEvent)new ErrorNotificationEvent("test can have only 1 instance(s) for the Customer ABC LTD"))));

    }

    @Test
    public void shouldReturnErrorNotificationUponFailCardinalityCheckWithMultipleProductsPassedIn() {
        final ProductOffering productOffering = ProductOfferingFixture.aProductOffering().withContractCardinality(new Cardinality(0, 4, null)).build();
        context.checking(new Expectations() {{
            oneOf(productOfferings).get();
            will(returnValue(productOffering));
            oneOf(expedioClientResources).getCustomerResource();
            will(returnValue(customerResource));
            oneOf(customerResource).getByToken(CUSTOMER_ID, "aToken");
            will(returnValue(customerDTO));
            oneOf(productInstanceClient).getContractAssets(with(new CustomerId(CUSTOMER_ID)),
                                                           with(new ContractId(CONTRACT_ID)),
                                                           with(new ProductCode(PRODUCT_ID)),
                                                           with(new ProductVersion(PRODUCT_VERSION)),
                                                           with(any(AssetFilter[].class)));
            will(returnValue(newArrayList(new AvailableAsset("assetId", 1L))));
        }});

        Notification notification = addProductOrchestrator.contractCardinalityCheck(CUSTOMER_ID, CONTRACT_ID, PRODUCT_ID, PRODUCT_VERSION, QUOTE_OPTION_ID, 4);

        assertTrue(notification.hasErrors());
        assertThat(notification.getErrorEvents().size(), is(1));
        assertThat(notification.getErrorEvents(),
                   hasItem(((AbstractNotificationEvent)new ErrorNotificationEvent("test can have only 4 instance(s) for the Customer ABC LTD"))));

    }

    @Test
    public void shouldReturnNotificationUponContractCardinalityCheckPass() {
        final ProductOffering productOffering = ProductOfferingFixture.aProductOffering().withContractCardinality(new Cardinality(0, 1, null)).build();
        context.checking(new Expectations() {{
            oneOf(productOfferings).get();
            will(returnValue(productOffering));
            oneOf(productInstanceClient).getContractAssets(with(new CustomerId(CUSTOMER_ID)),
                                                           with(new ContractId(CONTRACT_ID)),
                                                           with(new ProductCode(PRODUCT_ID)),
                                                           with(new ProductVersion(PRODUCT_VERSION)),
                                                           with(any(AssetFilter[].class)));
            will(returnValue(newArrayList()));
        }});

        Notification notification = addProductOrchestrator.contractCardinalityCheck(CUSTOMER_ID, CONTRACT_ID, PRODUCT_ID, PRODUCT_VERSION, QUOTE_OPTION_ID, 1);

        assertFalse(notification.hasErrors());
        assertThat(notification.getErrorEvents().size(), is(0));
    }

    @Test
    public void shouldReturnNotificationUponContractCardinalityExpressionCheckPass() {
        final ProductOffering productOffering = ProductOfferingFixture.aProductOffering().withContractCardinality(new Cardinality(0, 0,  new Expression("count(Scode.name)", ExpressionExpectedResultType.Double))).build();
        final ProductInstance productInstance = DefaultProductInstanceFixture.aProductInstance().withAttributes(new HashMap<String, Object>(){{put("name","value");}}).build();

        context.checking(new Expectations() {{
            allowing(instanceClient).getCustomerAssets(CUSTOMER_ID,"Scode");
            will(returnValue(newArrayList(productInstance)));
            oneOf(productOfferings).get();
            will(returnValue(productOffering));
            oneOf(productInstanceClient).getContractAssets(with(new CustomerId(CUSTOMER_ID)),
                                                           with(new ContractId(CONTRACT_ID)),
                                                           with(new ProductCode(PRODUCT_ID)),
                                                           with(new ProductVersion(PRODUCT_VERSION)),
                                                           with(any(AssetFilter[].class)));
            will(returnValue(newArrayList()));
        }});

        Notification notification = addProductOrchestrator.contractCardinalityCheck(CUSTOMER_ID, CONTRACT_ID, PRODUCT_ID, PRODUCT_VERSION, QUOTE_OPTION_ID, 1);

        assertFalse(notification.hasErrors());
        assertThat(notification.getErrorEvents().size(), is(0));
    }

    @Test
    public void shouldReturnNotificationUponContractCardinalityExpressionCheckFail() {
        final ProductOffering productOffering = ProductOfferingFixture.aProductOffering().withContractCardinality(new Cardinality(0, 0,  new Expression("count(Scode.name)", ExpressionExpectedResultType.Double))).build();
        final ProductInstance productInstance = DefaultProductInstanceFixture.aProductInstance().withAttributes(new HashMap<String, Object>(){{put("name","value");}}).build();

        context.checking(new Expectations() {{
            allowing(instanceClient).getCustomerAssets(CUSTOMER_ID,"Scode");
            will(returnValue(newArrayList(productInstance)));
            oneOf(productOfferings).get();
            will(returnValue(productOffering));
            oneOf(expedioClientResources).getCustomerResource();
            will(returnValue(customerResource));
            oneOf(customerResource).getByToken(CUSTOMER_ID, "aToken");
            will(returnValue(customerDTO));
            oneOf(productInstanceClient).getContractAssets(with(new CustomerId(CUSTOMER_ID)),
                                                           with(new ContractId(CONTRACT_ID)),
                                                           with(new ProductCode(PRODUCT_ID)),
                                                           with(new ProductVersion(PRODUCT_VERSION)),
                                                           with(any(AssetFilter[].class)));
            will(returnValue(newArrayList(new AvailableAsset("asset1",1l),new AvailableAsset("asset1",1l))));
        }});

        Notification notification = addProductOrchestrator.contractCardinalityCheck(CUSTOMER_ID, CONTRACT_ID, PRODUCT_ID, PRODUCT_VERSION, QUOTE_OPTION_ID, 1);

        assertTrue(notification.hasErrors());
        assertThat(notification.getErrorEvents().size(), is(1));
    }

    @Test
    public void shouldReturnEmptyNotificationForContractCardinalityCheckWhenMaxCardinalityIsUnbounded() {
        final ProductOffering productOffering = ProductOfferingFixture.aProductOffering().withContractCardinality(new Cardinality(0, Integer.MAX_VALUE, null)).build();
        context.checking(new Expectations() {{
            oneOf(productOfferings).get();
            will(returnValue(productOffering));
        }});

        Notification notification = addProductOrchestrator.contractCardinalityCheck(CUSTOMER_ID, CONTRACT_ID, PRODUCT_ID, PRODUCT_VERSION, QUOTE_OPTION_ID, 1);

        assertFalse(notification.hasErrors());
        assertThat(notification.getErrorEvents().size(), is(0));
    }

    @Test
    public void shouldReturnErrorNotificationUponFailSiteCardinalityCheck() {
        final ProductOffering productOffering = ProductOfferingFixture.aProductOffering().withSiteCardinality(new Cardinality(0, 2, null)).build();
        context.checking(new Expectations() {{
            oneOf(productOfferings).get();
            will(returnValue(productOffering));
            oneOf(expedioClientResources).getCustomerResource();
            will(returnValue(customerResource));
            oneOf(customerResource).siteResource(CUSTOMER_ID);
            will(returnValue(siteResource));
            oneOf(siteResource).get(SITE_ID, PROJECT_ID);
            will(returnValue(siteDTO));
            oneOf(productInstanceClient).getApprovedAssets(new SiteId(SITE_ID), new ProductCode(PRODUCT_ID), new ProductVersion(PRODUCT_VERSION));
            will(returnValue(newArrayList(new AvailableAsset("assetId", 1L))));
            oneOf(productInstanceClient).getDraftAssets(new SiteId(SITE_ID), new ProductCode(PRODUCT_ID), new ProductVersion(PRODUCT_VERSION), QUOTE_OPTION_ID);
            will(returnValue(newArrayList(new AvailableAsset("assetId", 1L))));
        }});

        Notification notification = addProductOrchestrator.siteCardinalityCheck(PROJECT_ID, CUSTOMER_ID, SITE_ID, PRODUCT_ID, PRODUCT_VERSION, QUOTE_OPTION_ID);

        assertTrue(notification.hasErrors());
        assertThat(notification.getErrorEvents().size(), is(1));
        assertThat(notification.getErrorEvents(),
                   hasItem(((AbstractNotificationEvent)new ErrorNotificationEvent("Connect Acceleration Service already exist under Site LONDON"))));
    }

    @Test
    public void shouldReturnErrorNotificationUponFailSiteCardinalityExpressionCheck() {
        final ProductOffering productOffering = ProductOfferingFixture.aProductOffering().withSiteCardinality(new Cardinality(0, 0, new Expression("count(Scode.name)", ExpressionExpectedResultType.Double))).build();

        final ProductInstance productInstance = DefaultProductInstanceFixture.aProductInstance().withAttributes(new HashMap<String, Object>(){{put("name","value");}}).build();
        final ProductInstance productInstance1 = DefaultProductInstanceFixture.aProductInstance().withAttributes(new HashMap<String, Object>(){{put("name","value");}}).build();

        context.checking(new Expectations() {{
            allowing(instanceClient).getCustomerAssets(CUSTOMER_ID,"Scode");
            will(returnValue(newArrayList(productInstance, productInstance1)));
            oneOf(productOfferings).get();
            will(returnValue(productOffering));
            oneOf(expedioClientResources).getCustomerResource();
            will(returnValue(customerResource));
            oneOf(customerResource).siteResource(CUSTOMER_ID);
            will(returnValue(siteResource));
            oneOf(siteResource).get(SITE_ID, PROJECT_ID);
            will(returnValue(siteDTO));
            oneOf(productInstanceClient).getApprovedAssets(new SiteId(SITE_ID), new ProductCode(PRODUCT_ID), new ProductVersion(PRODUCT_VERSION));
            will(returnValue(newArrayList(new AvailableAsset("assetId", 1L))));
            oneOf(productInstanceClient).getDraftAssets(new SiteId(SITE_ID), new ProductCode(PRODUCT_ID), new ProductVersion(PRODUCT_VERSION), QUOTE_OPTION_ID);
            will(returnValue(newArrayList(new AvailableAsset("assetId", 1L))));
        }});

        Notification notification = addProductOrchestrator.siteCardinalityCheck(PROJECT_ID, CUSTOMER_ID, SITE_ID, PRODUCT_ID, PRODUCT_VERSION, QUOTE_OPTION_ID);

        assertTrue(notification.hasErrors());
        assertThat(notification.getErrorEvents().size(), is(1));
        assertThat(notification.getErrorEvents(),
                   hasItem(((AbstractNotificationEvent)new ErrorNotificationEvent("Connect Acceleration Service already exist under Site LONDON"))));
    }

    @Test
    public void shouldReturnSuccessNotificationUponSiteCardinalityExpressionCheck() {
        final ProductOffering productOffering = ProductOfferingFixture.aProductOffering().withSiteCardinality(new Cardinality(0, 0, new Expression("count(Scode.name)", ExpressionExpectedResultType.Double))).build();

        final ProductInstance productInstance = DefaultProductInstanceFixture.aProductInstance().withAttributes(new HashMap<String, Object>(){{put("name","value");}}).build();
        final ProductInstance productInstance1 = DefaultProductInstanceFixture.aProductInstance().withAttributes(new HashMap<String, Object>(){{put("name","value");}}).build();
        final ProductInstance productInstance2 = DefaultProductInstanceFixture.aProductInstance().withAttributes(new HashMap<String, Object>(){{put("name","value");}}).build();
        final ProductInstance productInstance3 = DefaultProductInstanceFixture.aProductInstance().withAttributes(new HashMap<String, Object>(){{put("name","value");}}).build();

        context.checking(new Expectations() {{
            allowing(instanceClient).getCustomerAssets(CUSTOMER_ID, "Scode");
            will(returnValue(newArrayList(productInstance, productInstance1, productInstance2, productInstance3)));
            oneOf(productOfferings).get();
            will(returnValue(productOffering));
            oneOf(expedioClientResources).getCustomerResource();
            will(returnValue(customerResource));
            oneOf(customerResource).siteResource(CUSTOMER_ID);
            will(returnValue(siteResource));
            oneOf(siteResource).get(SITE_ID, PROJECT_ID);
            will(returnValue(siteDTO));
            oneOf(productInstanceClient).getApprovedAssets(new SiteId(SITE_ID), new ProductCode(PRODUCT_ID), new ProductVersion(PRODUCT_VERSION));
            will(returnValue(newArrayList(new AvailableAsset("assetId", 1L))));
            oneOf(productInstanceClient).getDraftAssets(new SiteId(SITE_ID), new ProductCode(PRODUCT_ID), new ProductVersion(PRODUCT_VERSION), QUOTE_OPTION_ID);
            will(returnValue(newArrayList(new AvailableAsset("assetId", 1L))));
        }});

        Notification notification = addProductOrchestrator.siteCardinalityCheck(PROJECT_ID, CUSTOMER_ID, SITE_ID, PRODUCT_ID, PRODUCT_VERSION, QUOTE_OPTION_ID);

        assertFalse(notification.hasErrors());
    }

    @Test
    public void shouldReturnEmptyNotificationForSiteCardinalityCheckWhenMaxCardinalityIsUnbounded() {
        final ProductOffering productOffering = ProductOfferingFixture.aProductOffering().withSiteCardinality(new Cardinality(0, Integer.MAX_VALUE, null)).build();
        context.checking(new Expectations() {{
            oneOf(productOfferings).get();
            will(returnValue(productOffering));
        }});

        Notification notification = addProductOrchestrator.siteCardinalityCheck(PROJECT_ID, CUSTOMER_ID, SITE_ID, PRODUCT_ID, PRODUCT_VERSION, QUOTE_OPTION_ID);

        assertFalse(notification.hasErrors());
        assertThat(notification.getErrorEvents().size(), is(0));
    }

    @Test
    public void shouldReturnNotificationUponSiteCardinalityCheckPass() {
        final ProductOffering productOffering = ProductOfferingFixture.aProductOffering().build();
        context.checking(new Expectations() {{
            oneOf(productOfferings).get();
            will(returnValue(productOffering));
            oneOf(productInstanceClient).getApprovedAssets(new SiteId(SITE_ID), new ProductCode(PRODUCT_ID), new ProductVersion(PRODUCT_VERSION));
            will(returnValue(newArrayList(new AvailableAsset("assetId", 1L))));
            oneOf(productInstanceClient).getDraftAssets(new SiteId(SITE_ID), new ProductCode(PRODUCT_ID), new ProductVersion(PRODUCT_VERSION), QUOTE_OPTION_ID);
            will(returnValue(newArrayList()));
        }});

        Notification notification = addProductOrchestrator.siteCardinalityCheck(PROJECT_ID, CUSTOMER_ID, SITE_ID, PRODUCT_ID, PRODUCT_VERSION, QUOTE_OPTION_ID);

        assertFalse(notification.hasErrors());
        assertThat(notification.getErrorEvents().size(), is(0));
    }

    @After
    public void shutdown() {
        serviceLocatorInstance().unRegister(instanceClient);
    }
}


