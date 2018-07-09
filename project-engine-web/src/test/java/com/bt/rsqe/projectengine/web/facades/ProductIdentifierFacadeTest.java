package com.bt.rsqe.projectengine.web.facades;

import com.bt.rsqe.client.Pmr;
import com.bt.rsqe.customerinventory.client.resource.ProductAgreementResourceClient;
import com.bt.rsqe.domain.ProductOfferingVersion;
import com.bt.rsqe.domain.SalesCatalogue;
import com.bt.rsqe.domain.StencilId;
import com.bt.rsqe.domain.bom.fixtures.AttributeFixture;
import com.bt.rsqe.domain.bom.fixtures.ProductOfferingFixture;
import com.bt.rsqe.domain.bom.fixtures.SellableProductFixture;
import com.bt.rsqe.domain.bom.parameters.ProductSCode;
import com.bt.rsqe.domain.product.AccessDetail;
import com.bt.rsqe.domain.product.PrerequisiteUrl;
import com.bt.rsqe.domain.product.ProductOffering;
import com.bt.rsqe.domain.product.SellableProduct;
import com.bt.rsqe.domain.product.parameters.ProductCategoryCode;
import com.bt.rsqe.domain.product.parameters.ProductIdentifier;
import com.bt.rsqe.dto.TableResponseDTO;
import com.bt.rsqe.pmr.client.PmrLookupClient;
import com.bt.rsqe.utils.countries.Country;
import com.bt.rsqe.pmr.client.PmrClient;
import com.bt.rsqe.pmr.dto.SalesChannelDTO;
import com.bt.rsqe.projectengine.web.view.Products;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static com.bt.rsqe.domain.bom.fixtures.ProductOfferingFixture.*;
import static com.google.common.collect.Lists.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.core.Is.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

public class ProductIdentifierFacadeTest {
    @Mock
    private PmrClient pmrClient;
    @Mock
    private ProductAgreementResourceClient productAgreementResourceClient;
    @Mock
    private PmrLookupClient pmrLookupClient;
    @Mock
    private Pmr.ProductOfferings productOfferings;
    @Mock
    private ProductIdentifierFacade productIdentifierFacade;
    @Mock
    private TableResponseDTO tableResponseDTO;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void shouldGetAllSellableProductsWhenSalesChannelHasNoCategories() throws Exception {
        SalesChannelDTO salesChannelDTO = new SalesChannelDTO("aSalesChannel", "aSalesChannelId", false, null);
        setupPmr(salesChannelDTO);

        Products products = new ProductIdentifierFacade(pmrClient, productAgreementResourceClient, pmrLookupClient).getSellableProductsForSalesChannel("aSalesChannel");

        assertThat(products.products().size(), is(2));
        assertThat(products.products().get(0).getId(), is("aProductId"));
        assertThat(products.products().get(1).getId(), is("anotherProductId"));
    }

    @Test
    public void shouldGetAllSellableProductsWhenExcludedSalesChannelHasNoCategories() throws Exception {
        SalesChannelDTO salesChannelDTO = new SalesChannelDTO("aSalesChannel", "aSalesChannelId", true, null);
        setupPmr(salesChannelDTO);

        Products products = new ProductIdentifierFacade(pmrClient, productAgreementResourceClient, pmrLookupClient).getSellableProductsForSalesChannel("aSalesChannel");

        assertThat(products.products().size(), is(2));
        assertThat(products.products().get(0).getId(), is("aProductId"));
        assertThat(products.products().get(1).getId(), is("anotherProductId"));
    }

    @Test
    public void shouldGetSellableProductsForSalesChannel() throws Exception {
        final String salesChannelName = "aSalesChannel";
        SalesChannelDTO salesChannelDTO = new SalesChannelDTO(salesChannelName, "aSalesChannelId", false, newArrayList("H1"));
        setupPmr(salesChannelDTO);

        when(pmrClient.getSalesChannelDto(salesChannelName)).thenReturn(salesChannelDTO);

        Products products = new ProductIdentifierFacade(pmrClient, productAgreementResourceClient, pmrLookupClient).getSellableProductsForSalesChannel(salesChannelName);

        assertThat(products.products().size(), is(1));
        assertThat(products.products().get(0).getId(), is("aProductId"));
    }

    @Test
    public void shouldGetSellableProductsForCustomerBased() throws Exception {

        final String salesChannelName = "aSalesChannel";
        final String customerId = "aCustomerId";

        SalesChannelDTO salesChannelDTO = new SalesChannelDTO(salesChannelName, "aSalesChannelId", false, newArrayList("H1"));
        setupPmr(salesChannelDTO);
        PrerequisiteUrl prerequisiteUrl = new PrerequisiteUrl("direct", "indirect");
        Products products = new Products(Arrays.asList(SellableProductFixture.aProduct()
                                                                             .withId("sCode1")
                                                                             .withName("Product1")
                                                                             .withFamily("F1", "F1 Group Category")
                                                                             .withCategory("H1", "H1 Category")
                                                                             .withSiteInstallable(false)
                                                                             .withPrerequisiteUrl(prerequisiteUrl)
                                                                             .build(),
                                                       SellableProductFixture.aProduct()
                                                                             .withId("sCode2")
                                                                             .withName("Product2")
                                                                             .withFamily("F1", "F1 Group Category")
                                                                             .withCategory("H2", "H2 Category")
                                                                             .build()));

        when(pmrClient.getSalesChannelDto(salesChannelName)).thenReturn(salesChannelDTO);
        when(pmrLookupClient.lookupRuleSet(anyString(), Matchers.<HashMap<String, String>>any())).thenReturn(TableResponseDTO.from("", newArrayList("H1")));

        products = new ProductIdentifierFacade(pmrClient, productAgreementResourceClient, pmrLookupClient).getSellableProductsForCustomerBased(products, customerId);

        assertThat(products.sellableProducts().size(), is(1));

    }

    @Test
    public void shouldReturnSellableProductsWhenProductCategoryNotAvailable() throws Exception {

        final String salesChannelName = "aSalesChannel";
        final String customerId = "aCustomerId";

        SalesChannelDTO salesChannelDTO = new SalesChannelDTO(salesChannelName, "aSalesChannelId", false, newArrayList("H1"));
        setupPmr(salesChannelDTO);
        PrerequisiteUrl prerequisiteUrl = new PrerequisiteUrl("direct", "indirect");
        Products products = new Products(Arrays.asList(SellableProductFixture.aProduct()
                                                                             .withId("sCode1")
                                                                             .withName("Product1")
                                                                             .withFamily("F1", "F1 Group Category")
                                                                             .withCategory("H1", "H1 Category")
                                                                             .withSiteInstallable(false)
                                                                             .withPrerequisiteUrl(prerequisiteUrl)
                                                                             .build(),
                                                       SellableProductFixture.aProduct()
                                                                             .withId("sCode2")
                                                                             .withName("Product2")
                                                                             .withFamily("F1", "F1 Group Category")
                                                                             .withCategory("H2", "H2 Category")
                                                                             .build()));

        when(pmrClient.getSalesChannelDto(salesChannelName)).thenReturn(salesChannelDTO);
        when(pmrLookupClient.lookupRuleSet(anyString(), Matchers.<HashMap<String, String>>any())).thenReturn(TableResponseDTO.from("", newArrayList("H3")));

        products = new ProductIdentifierFacade(pmrClient, productAgreementResourceClient, pmrLookupClient).getSellableProductsForCustomerBased(products, customerId);

        assertThat(products.sellableProducts().size(), is(2));

    }

    @Test
    public void shouldGetSellableProductsForExcludedSalesChannel() throws Exception {
        final String salesChannelName = "aSalesChannel";
        SalesChannelDTO salesChannelDTO = new SalesChannelDTO(salesChannelName, "aSalesChannelId", true, newArrayList("H1"));
        setupPmr(salesChannelDTO);

        when(pmrClient.getSalesChannelDto(salesChannelName)).thenReturn(salesChannelDTO);

        Products products = new ProductIdentifierFacade(pmrClient, productAgreementResourceClient, pmrLookupClient).getSellableProductsForSalesChannel(salesChannelName);

        assertThat(products.products().size(), is(1));
        assertThat(products.products().get(0).getId(), is("anotherProductId"));
    }

    @Test
    public void shouldGetAllSellableProducts() {
        SellableProduct productOne = SellableProductFixture.aProduct().withId("productId").withName("productName").build();
        final List<SellableProduct> sellableProducts = newArrayList(productOne);
        final SalesCatalogue salesCatalogue = new SalesCatalogue(sellableProducts);
        when(pmrClient.getSalesCatalogue()).thenReturn(salesCatalogue);

        Products actualSellableProducts = new ProductIdentifierFacade(pmrClient, productAgreementResourceClient, pmrLookupClient).getAllSellableProducts();

        assertThat(actualSellableProducts, is(actualSellableProducts));
    }

    @Test
    public void shouldGetProductName() {
        //Given
        final ProductOffering productOffering = aProductOffering().withProductIdentifier(new ProductIdentifier("product id", "product name", "version")).build();
        when(pmrClient.productOffering(ProductSCode.newInstance("scode"))).thenReturn(new Pmr.ProductOfferings() {
            @Override
            public ProductOffering get() {
                return productOffering;
            }

            @Override
            public Pmr.ProductOfferings forOfferingVersion(ProductOfferingVersion version) {
                return null;
            }

            @Override
            public Pmr.ProductOfferings withStencil(StencilId stencil) {
                return null;
            }

            @Override
            public Pmr.ProductOfferings forCountry(Country country) {
                return null;
            }

            @Override
            public Pmr.ProductOfferings withAccessDetail(AccessDetail accessDetail) {
                return null;
            }

            @Override
            public Pmr.ProductOfferings withProductCategoryCode(ProductCategoryCode catCode) {
                return null;
            }
        });
        //When
        String productName = new ProductIdentifierFacade(pmrClient, productAgreementResourceClient, pmrLookupClient).getProductName("scode");
        //Then
        assertThat(productName, is("product name"));
    }

    @Test
    public void shouldGetDisplayName() {
        //Given
        final ProductOffering productOffering = spy(aProductOffering().withProductIdentifier(new ProductIdentifier("product id", "product name", "version", "display name")).build());
        doReturn("display name").when(productOffering).getDisplayName();
        when(pmrClient.productOffering(ProductSCode.newInstance("scode"))).thenReturn(new Pmr.ProductOfferings() {
            @Override
            public ProductOffering get() {
                return productOffering;
            }

            @Override
            public Pmr.ProductOfferings forOfferingVersion(ProductOfferingVersion version) {
                return null;
            }

            @Override
            public Pmr.ProductOfferings withStencil(StencilId stencil) {
                return null;
            }

            @Override
            public Pmr.ProductOfferings forCountry(Country country) {
                return null;
            }

            @Override
            public Pmr.ProductOfferings withAccessDetail(AccessDetail accessDetail) {
                return null;
            }

            @Override
            public Pmr.ProductOfferings withProductCategoryCode(ProductCategoryCode catCode) {
                return null;
            }
        });
        //When
        String displayName = new ProductIdentifierFacade(pmrClient, productAgreementResourceClient, pmrLookupClient).getDisplayName("scode");
        //Then
        assertThat(displayName, is("display name"));
    }

    @Test
    public void shouldGetDisplayNameFromProductIdentifierWhenNullInOffering() {
        //Given
        final ProductOffering productOffering = spy(aProductOffering().withProductIdentifier(new ProductIdentifier("product id", "product name", "version", "display name")).build());
        doReturn(null).when(productOffering).getDisplayName();
        when(pmrClient.productOffering(ProductSCode.newInstance("scode"))).thenReturn(new Pmr.ProductOfferings() {
            @Override
            public ProductOffering get() {
                return productOffering;
            }

            @Override
            public Pmr.ProductOfferings forOfferingVersion(ProductOfferingVersion version) {
                return null;
            }

            @Override
            public Pmr.ProductOfferings withStencil(StencilId stencil) {
                return null;
            }

            @Override
            public Pmr.ProductOfferings forCountry(Country country) {
                return null;
            }

            @Override
            public Pmr.ProductOfferings withAccessDetail(AccessDetail accessDetail) {
                return null;
            }

            @Override
            public Pmr.ProductOfferings withProductCategoryCode(ProductCategoryCode catCode) {
                return null;
            }
        });
        //When
        String displayName = new ProductIdentifierFacade(pmrClient, productAgreementResourceClient, pmrLookupClient).getDisplayName("scode");
        //Then
        assertThat(displayName, is("display name"));
    }

    @Test
    public void shouldGetProductNameIfDisplayNameIsUndefined() {
        //Given
        final ProductOffering productOffering = aProductOffering().withProductIdentifier(new ProductIdentifier("product id", "product name", "version", null)).build();
        when(pmrClient.productOffering(ProductSCode.newInstance("scode"))).thenReturn(new Pmr.ProductOfferings() {
            @Override
            public ProductOffering get() {
                return productOffering;
            }

            @Override
            public Pmr.ProductOfferings forOfferingVersion(ProductOfferingVersion version) {
                return null;
            }

            @Override
            public Pmr.ProductOfferings withStencil(StencilId stencil) {
                return null;
            }

            @Override
            public Pmr.ProductOfferings forCountry(Country country) {
                return null;
            }

            @Override
            public Pmr.ProductOfferings withAccessDetail(AccessDetail accessDetail) {
                return null;
            }

            @Override
            public Pmr.ProductOfferings withProductCategoryCode(ProductCategoryCode catCode) {
                return null;
            }
        });
        //When
        String displayName = new ProductIdentifierFacade(pmrClient, productAgreementResourceClient, pmrLookupClient).getDisplayName("scode");
        //Then
        assertThat(displayName, is("product name"));
    }

    @Test
    public void shouldGetSpecialBidTrueForSpecialBidProduct(){
        ProductOffering productOfferingFixture = ProductOfferingFixture.aProductOffering().withAttribute(AttributeFixture.anAttribute()
                                                                                          .called(ProductOffering.SPECIAL_BID_ATTRIBUTE_INDICATOR)
                                                                                          .withDefaultValue("Yes").build())
                                                                       .build();

        when(pmrClient.productOffering(ProductSCode.newInstance("test"))).thenReturn(productOfferings);
        when(productOfferings.get()).thenReturn(productOfferingFixture);

        assertThat(new ProductIdentifierFacade(pmrClient, productAgreementResourceClient, pmrLookupClient).isProductSpecialBid("test"), is(true));
    }

    @Test
    public void shouldReturnFalseForProductWithNoValueForNonStandard(){
        ProductOffering productOfferingFixture = ProductOfferingFixture.aProductOffering().withAttribute(AttributeFixture.anAttribute()
                                                                                                                         .called(ProductOffering.SPECIAL_BID_ATTRIBUTE_INDICATOR)
                                                                                                                         .withDefaultValue("No").build())
                                                                       .build();

        when(pmrClient.productOffering(ProductSCode.newInstance("test"))).thenReturn(productOfferings);
        when(productOfferings.get()).thenReturn(productOfferingFixture);

        assertThat(new ProductIdentifierFacade(pmrClient, productAgreementResourceClient, pmrLookupClient).isProductSpecialBid("test"), is(false));
    }

    @Test
    public void shouldReturnFalseForProductWithAttributesButNoNonStandard(){
        ProductOffering productOfferingFixture = ProductOfferingFixture.aProductOffering().withAttribute(AttributeFixture.anAttribute()
                                                                                                                         .called("testAttribute")
                                                                                                                         .withDefaultValue("No").build())
                                                                       .build();

        when(pmrClient.productOffering(ProductSCode.newInstance("test"))).thenReturn(productOfferings);
        when(productOfferings.get()).thenReturn(productOfferingFixture);

        assertThat(new ProductIdentifierFacade(pmrClient, productAgreementResourceClient, pmrLookupClient).isProductSpecialBid("test"), is(false));
    }

    @Test
    public void shouldReturnFalseWhenNoProductOfferingForSCode(){
        assertThat(new ProductIdentifierFacade(pmrClient, productAgreementResourceClient, pmrLookupClient).isProductSpecialBid("test"), is(false));
    }

    private void setupPmr(SalesChannelDTO salesChannelDTO) {
        when(pmrClient.getSalesChannelDto(salesChannelDTO.getSalesChannelName())).thenReturn(salesChannelDTO);
        SalesCatalogue salesCatalogue = mock(SalesCatalogue.class);
        when(pmrClient.getSalesCatalogue()).thenReturn(salesCatalogue);
        final ArrayList<SellableProduct> sellableProducts = newArrayList(SellableProductFixture.aProduct().withId("aProductId").withCategory("H1", "H1").build(),
                                                                         SellableProductFixture.aProduct().withId("anotherProductId").withCategory("H2", "H2").build());
        when(salesCatalogue.getAllSellableProducts()).thenReturn(sellableProducts);
    }
}