package com.bt.rsqe.projectengine.web.quoteoptiondetails;

import com.bt.rsqe.client.Pmr;
import com.bt.rsqe.container.ApplicationConfig;
import com.bt.rsqe.customerrecord.ExpedioClientResources;
import com.bt.rsqe.customerrecord.SiteDTO;
import com.bt.rsqe.domain.bom.fixtures.SellableProductFixture;
import com.bt.rsqe.domain.bom.parameters.ProductSCode;
import com.bt.rsqe.domain.product.PrerequisiteUrl;
import com.bt.rsqe.domain.product.SellableProduct;
import com.bt.rsqe.domain.product.parameters.ProductIdentifier;
import com.bt.rsqe.enums.MoveConfigurationTypeEnum;
import com.bt.rsqe.expedio.project.ExpedioProjectResource;
import com.bt.rsqe.pmr.dto.SalesChannelDTO;
import com.bt.rsqe.projectengine.web.facades.ProductIdentifierFacade;
import com.bt.rsqe.projectengine.web.facades.QuoteOptionFacade;
import com.bt.rsqe.projectengine.web.facades.SiteFacade;
import com.bt.rsqe.projectengine.web.uri.UriFactory;
import com.bt.rsqe.projectengine.web.uri.UriFactoryImpl;
import com.bt.rsqe.projectengine.web.view.MoveProductSitesDTO;
import com.bt.rsqe.projectengine.web.view.Products;
import com.bt.rsqe.projectengine.web.view.SiteView;
import com.bt.rsqe.projectengine.web.view.filtering.PaginatedFilter;
import com.bt.rsqe.projectengine.web.view.filtering.PaginatedFilterResult;
import com.google.common.base.Optional;
import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.collect.Lists.*;
import static junit.framework.Assert.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

public class SelectNewSiteProductOrchestratorTest {
    public static final String CUSTOMER_ID = "customerId";
    public static final String CONTRACT_ID = "CONTRACT_ID";
    public static final String PROJECT_ID = "projectId";
    public static final String QUOTE_OPTION_ID = "quoteOptionId";
    public static final String PRODUCT_ID = "S0308491";
    private JUnit4Mockery context;
    private SiteFacade siteFacade;
    private SelectNewSiteProductOrchestrator selectNewSiteProductOrchestrator;
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
    private SellableProduct sellableProduct;
    private SellableProduct secondSellableProduct;
    private Products products;
    private Optional<ProductIdentifier> hCode;
    private Pmr.ProductOfferings productOfferings;

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
        productOfferings = context.mock(Pmr.ProductOfferings.class);
        productCategoryCodesList.add("H");
        sellableProduct = SellableProductFixture.aProduct().withId("id1").withName("product1").withSiteInstallable(true).withPrerequisiteUrl(new PrerequisiteUrl("", "")).withMoveConfigurationType(MoveConfigurationTypeEnum.NOT_MOVEABLE).withIsImportable(false).build();
        secondSellableProduct = SellableProductFixture.aProduct().withId("id2").withName("product2").withSiteInstallable(true).withPrerequisiteUrl(new PrerequisiteUrl("", "")).withMoveConfigurationType(MoveConfigurationTypeEnum.COPY_ALL).withIsImportable(false).build();
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
        }});
        when(productIdentifierFacade.getSalesChannelDto("")).thenReturn(SalesChannelDTO.newInstance("salesChannelName", "salesChannelId", true, productCategoryCodesList));
        when(productIdentifierFacade.getSellableProductsForSalesChannel("")).thenReturn(products);
        when(productIdentifierFacade.getProductHCode(sellableProduct.getProductId())).thenReturn(hCode);
        when(productIdentifierFacade.getProductHCode(secondSellableProduct.getProductId())).thenReturn(hCode);
        selectNewSiteProductOrchestrator = new SelectNewSiteProductOrchestrator(siteFacade,
                                                                                productIdentifierFacade,
                                                                                productConfiguratorUriFactory,
                                                                                quoteOptionFacade,
                                                                                projectResource,
                                                                                pmr,
                                                                                expedioClientResources);
    }

    @Test
    public void shouldConstructSelectNewSiteView() throws Exception {
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
            oneOf(filterResult).getItems();
            will(returnValue(newArrayList(uk, india)));
            allowing(pmr).getSupportedCountries(sCode);
            will(returnValue(newArrayList(uk.getCountryISOCode())));
        }});

        MoveProductSitesDTO productSitesView = selectNewSiteProductOrchestrator.buildSitesView(CUSTOMER_ID,
                                                                                               PROJECT_ID,
                                                                                               paginatedFilter,
                                                                                               sCode,
                                                                                               null,
                                                                                               null,
                                                                                               Optional.<String>absent());
        List<MoveProductSitesDTO.MoveSiteRowDTO> sites = productSitesView.sites;

        assertThat(sites.size(), is(2));

        assertThat(sites.get(0).country, is(new SiteView(uk).getFullAddress()));
        assertThat(sites.get(0).isValidForProduct, is(true));
        assertThat(sites.get(1).country, is(new SiteView(india).getFullAddress()));
        assertThat(sites.get(1).isValidForProduct, is(false));
    }

        @Test
    public void shouldConstructSelectNewSiteViewWithNoSupportedCountries() throws Exception {
        final SiteDTO uk = new SiteDTO();
        uk.country = "United Kingdom";
        uk.streetName = "streetName";

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
            oneOf(filterResult).getItems();
            will(returnValue(newArrayList(uk, india)));
            allowing(pmr).getSupportedCountries(sCode);
            will(returnValue(newArrayList()));
        }});

        MoveProductSitesDTO productSitesView = selectNewSiteProductOrchestrator.buildSitesView(CUSTOMER_ID,
                                                                                               PROJECT_ID,
                                                                                               paginatedFilter,
                                                                                               null,
                                                                                               null,
                                                                                               null,
                                                                                               Optional.<String>absent());
        List<MoveProductSitesDTO.MoveSiteRowDTO> sites = productSitesView.sites;

        assertThat(sites.size(), is(2));

        assertThat(sites.get(0).country, is(new SiteView(uk).getCountry()));
        assertThat(sites.get(0).isValidForProduct, is(false));
        assertThat(sites.get(1).country, is(new SiteView(india).getFullAddress()));
        assertThat(sites.get(1).isValidForProduct, is(false));
        assertNotNull(sites.get(0).addressLine1);
        assertNotNull(sites.get(1).addressLine1);
        assertThat(sites.get(0).addressLine3, is("streetName"));
    }

    @Test
    public void shouldConstructSelectNewSiteViewWithBlankFields() throws Exception {
        final SiteDTO uk = new SiteDTO();
        uk.building = "building";
        uk.buildingNumber = "buildingNumber";
        uk.city = "city";
        uk.postCode = "postCode";
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
            oneOf(filterResult).getItems();
            will(returnValue(newArrayList(uk, india)));
            allowing(pmr).getSupportedCountries(sCode);
            will(returnValue(newArrayList(uk.getCountryISOCode())));
        }});

        MoveProductSitesDTO productSitesView = selectNewSiteProductOrchestrator.buildSitesView(CUSTOMER_ID,
                                                                                               PROJECT_ID,
                                                                                               paginatedFilter,
                                                                                               sCode,
                                                                                               null,
                                                                                               null,
                                                                                               Optional.<String>absent());
        List<MoveProductSitesDTO.MoveSiteRowDTO> sites = productSitesView.sites;

        assertThat(sites.size(), is(2));

        assertThat(sites.get(0).country, is("United Kingdom"));
        assertThat(sites.get(0).isValidForProduct, is(true));
        assertThat(sites.get(1).country, is(new SiteView(india).getCountry()));
        assertThat(sites.get(1).isValidForProduct, is(false));
        assertNotNull(sites.get(0).addressLine1);
        assertNotNull(sites.get(1).addressLine1);
        assertThat(sites.get(0).addressLine1, is("building"));
        assertThat(sites.get(1).addressLine1, is(""));
    }
}
