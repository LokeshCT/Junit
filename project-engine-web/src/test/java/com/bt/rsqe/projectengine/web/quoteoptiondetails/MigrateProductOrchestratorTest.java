package com.bt.rsqe.projectengine.web.quoteoptiondetails;

import com.bt.rsqe.client.Pmr;
import com.bt.rsqe.customerrecord.CustomerDTO;
import com.bt.rsqe.customerrecord.CustomerResource;
import com.bt.rsqe.customerrecord.ExpedioClientResources;
import com.bt.rsqe.customerrecord.SiteDTO;
import com.bt.rsqe.domain.ProductCategoryMigration;
import com.bt.rsqe.domain.bom.fixtures.ProductOfferingFixture;
import com.bt.rsqe.domain.bom.fixtures.SellableProductFixture;
import com.bt.rsqe.domain.bom.parameters.ProductSCode;
import com.bt.rsqe.domain.product.ProductCategory;
import com.bt.rsqe.domain.product.ProductOffering;
import com.bt.rsqe.domain.product.SellableProduct;
import com.bt.rsqe.domain.product.parameters.ProductIdentifier;
import com.bt.rsqe.enums.ProductAction;
import com.bt.rsqe.expedio.fixtures.CustomerDTOFixture;
import com.bt.rsqe.projectengine.web.facades.ProductIdentifierFacade;
import com.bt.rsqe.projectengine.web.facades.SiteFacade;
import com.bt.rsqe.projectengine.web.view.ProductSitesDTO;
import com.bt.rsqe.projectengine.web.view.Products;
import com.bt.rsqe.projectengine.web.view.SiteView;
import com.bt.rsqe.projectengine.web.view.filtering.PaginatedFilter;
import com.bt.rsqe.projectengine.web.view.filtering.PaginatedFilterResult;
import com.bt.rsqe.security.UserContext;
import com.bt.rsqe.security.UserContextManager;
import com.google.common.base.Optional;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static com.bt.rsqe.security.UserContextBuilder.aDirectUserContext;
import static com.google.common.collect.Lists.*;
import static org.apache.axis.utils.StringUtils.isEmpty;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

public class MigrateProductOrchestratorTest {
    private static final String CUSTOMER_ID = "aCustomerId";
    private static final String CONTRACT_ID = "aContractId";
    private static final String PROJECT_ID = "aProjectId";
    private static final String SALES_CHANNEL = "aSalesChannel";
    private static final String CATEGORY1 = "H1";
    private static final String CATEGORY2 = "H2";
    private static final CustomerDTO CUSTOMER = CustomerDTOFixture.aCustomerDTO().withId(CUSTOMER_ID).withSalesChannel(SALES_CHANNEL).build();
    private static final SellableProduct PRODUCT1 = SellableProductFixture.aProduct().withCategory(CATEGORY1, "H1 Name").build();
    private static final SellableProduct PRODUCT2 = SellableProductFixture.aProduct().withCategory(CATEGORY2, "H2 Name").build();
    private ExpedioClientResources expedioClientResources;
    private ProductIdentifierFacade productIdentifierFacade;
    private SiteFacade siteFacade;
    private Pmr pmr;
    private MigrateProductOrchestrator migrateProductOrchestrator;
    private Pmr.ProductOfferings productOfferings;
    private ProductOffering productOffering;

    @Before
    public void setUp() throws Exception {

        UserContext userContext = aDirectUserContext().withToken("aToken").build();
        UserContextManager.setCurrent(userContext);

        expedioClientResources = mock(ExpedioClientResources.class);
        CustomerResource customerResource = mock(CustomerResource.class);
        when(expedioClientResources.getCustomerResource()).thenReturn(customerResource);
        when(customerResource.getByToken(CUSTOMER_ID, "aToken")).thenReturn(CUSTOMER);

        productIdentifierFacade = mock(ProductIdentifierFacade.class);
        when(productIdentifierFacade.getSellableProductsForSalesChannel(SALES_CHANNEL)).thenReturn(new Products(newArrayList(PRODUCT1, PRODUCT2)));

        siteFacade = mock(SiteFacade.class);

        ProductCategory productCategory1 = new ProductCategory();
        productCategory1.setProductIdentifier(new ProductIdentifier(CATEGORY1, "aVersion"));

        ProductCategory productCategory2 = new ProductCategory();
        productCategory2.setProductIdentifier(new ProductIdentifier(CATEGORY2, "aVersion"));
        productCategory2.setMigrationFlags(new ProductCategoryMigration(true, true, false));

        pmr = mock(Pmr.class);
        when(pmr.getProductCategories()).thenReturn(newArrayList(productCategory1, productCategory2));

        productOfferings = mock(Pmr.ProductOfferings.class);
        productOffering = mock(ProductOffering.class);

        when(pmr.productOffering(ProductSCode.newInstance(null))).thenReturn(productOfferings);
        when(productOfferings.get()).thenReturn(ProductOfferingFixture.aProductOffering().withIsInFrontCatalogue(false).withIsSeparatelyModifiable(false).build());
        when(productOffering.isSeparatelyModifiable()).thenReturn(false);

        migrateProductOrchestrator = new MigrateProductOrchestrator(siteFacade, productIdentifierFacade, null, null, null, pmr, null, expedioClientResources);
    }

    @Test
    public void shouldExcludeNonMigratableProductsFromSellableProducts() throws Exception {
        Products products = migrateProductOrchestrator.getProducts("aCustomerId", ProductAction.Migrate.description(),CONTRACT_ID );
        assertThat(products.sellableProducts().size(), is(1));
        assertThat(products.sellableProducts().get(0).getProductCategory().getProductId(), is(CATEGORY2));
    }

    @Test
    public void shouldAddMigrateNoSitesGivenEmptySiteList() throws Exception {
        when(siteFacade.getAllBranchSites(CUSTOMER_ID, PROJECT_ID)).thenReturn(new ArrayList<SiteDTO>());
        when(productIdentifierFacade.isProductSpecialBid(null)).thenReturn(false);

        final ProductSitesDTO view = migrateProductOrchestrator.buildSitesView(CUSTOMER_ID, PROJECT_ID, new PaginatedFilter<SiteDTO>() {
            @Override
            public PaginatedFilterResult applyTo(List<SiteDTO> items) {
                return new PaginatedFilterResult(0, items, 0, 1);
            }
        }, null, null, null, Optional.<String>absent());
        assertThat(view.sites.size(), is(0));

    }

    @Test
    public void shouldConstructSpecialBidSiteView() throws Exception {
        final SiteDTO uk = new SiteDTO();
        uk.country = "United Kingdom";
        final SiteDTO india = new SiteDTO();
        india.country = "India";
        final String sCode = "sCode";

        when(siteFacade.getAllBranchSites(CUSTOMER_ID, PROJECT_ID)).thenReturn(newArrayList(uk, india));
        when(productIdentifierFacade.isProductSpecialBid(sCode)).thenReturn(true);
        when(pmr.getCountriesWithSpecialBidPricingType(sCode)).thenReturn(newArrayList(uk.getCountryISOCode()));

        ProductSitesDTO productSitesView = migrateProductOrchestrator.buildSitesView(CUSTOMER_ID, PROJECT_ID, new PaginatedFilter<SiteDTO>() {
            @Override
            public PaginatedFilterResult applyTo(List<SiteDTO> items) {
                return new PaginatedFilterResult(0, items, 0, 1);
            }
        }, sCode, null, null, Optional.<String>absent());

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
        assertTrue(isEmpty(sites.get(0).newFullAddress));
        assertTrue(isEmpty(sites.get(1).newFullAddress));
    }
}
