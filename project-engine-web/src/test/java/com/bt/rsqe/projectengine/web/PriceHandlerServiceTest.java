package com.bt.rsqe.projectengine.web;

import com.bt.rsqe.customerinventory.client.ProductInstanceClient;
import com.bt.rsqe.customerinventory.dto.AssetDTO;
import com.bt.rsqe.customerinventory.fixtures.AssetDTOFixture;
import com.bt.rsqe.customerinventory.parameter.LengthConstrainingProductInstanceId;
import com.bt.rsqe.customerinventory.parameter.LineItemId;
import com.bt.rsqe.customerinventory.parameter.ProductCode;
import com.bt.rsqe.customerinventory.parameter.ProductInstanceVersion;
import com.bt.rsqe.customerrecord.CustomerResource;
import com.bt.rsqe.customerrecord.PriceBookResource;
import com.bt.rsqe.customerrecord.SiteResource;
import com.bt.rsqe.domain.ContractDTO;
import com.bt.rsqe.domain.PriceBookDTO;
import com.bt.rsqe.domain.bom.fixtures.ProductOfferingFixture;
import com.bt.rsqe.domain.bom.parameters.ProductInstanceId;
import com.bt.rsqe.domain.product.DefaultProductInstanceFixture;
import com.bt.rsqe.domain.product.chargingscheme.ProductChargingScheme;
import com.bt.rsqe.domain.product.fixtures.ProductChargingSchemeFixture;
import com.bt.rsqe.domain.product.parameters.ProductIdentifier;
import com.bt.rsqe.domain.project.PricingStatus;
import com.bt.rsqe.domain.project.ProductInstance;
import com.bt.rsqe.domain.project.ProductInstanceStatus;
import com.bt.rsqe.expedio.fixtures.SiteDTOFixture;
import com.bt.rsqe.pmr.client.PmrClient;
import com.bt.rsqe.pricing.PriceClientBidManagerAssetResponse;
import com.bt.rsqe.pricing.PriceClientRequest;
import com.bt.rsqe.pricing.PriceClientResponse;
import com.bt.rsqe.pricing.PriceRequestType;
import com.bt.rsqe.pricing.PriceResponse;
import com.bt.rsqe.pricing.PricingClient;
import com.bt.rsqe.pricing.PricingFacadeService;
import com.bt.rsqe.pricing.PricingStatusNADecider;
import com.bt.rsqe.projectengine.ProjectResource;
import com.bt.rsqe.projectengine.QuoteOptionDTO;
import com.bt.rsqe.projectengine.QuoteOptionItemDTO;
import com.bt.rsqe.projectengine.QuoteOptionItemDTOFixture;
import com.bt.rsqe.projectengine.QuoteOptionItemResource;
import com.bt.rsqe.projectengine.QuoteOptionResource;
import com.bt.rsqe.projectengine.web.facades.SiteFacade;
import com.bt.rsqe.security.UserContext;
import com.bt.rsqe.security.UserContextManager;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.hamcrest.core.Is;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.bt.rsqe.domain.product.chargingscheme.PricingStrategy.*;
import static com.bt.rsqe.domain.product.chargingscheme.ProductChargingScheme.PriceVisibility.*;
import static com.google.common.collect.Lists.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Created by IntelliJ IDEA.
 * User: 605908589
 * Date: 20/08/14
 * Time: 09:05
 * To change this template use File | Settings | File Templates.
 */
public class PriceHandlerServiceTest {
    private PricingClient pricingClient = mock(PricingClient.class);
    private ProductInstanceClient futureProductInstanceClient;
    private SiteFacade siteFacade;
    private PricingFacadeService pricingFacadeService;
    private PmrClient pmrClient;
    private ProjectResource projectResource;
    private CustomerResource customerResource;
    private PriceHandlerService priceHandlerService;
    private PriceBookResource priceBookResource;
    private PricingStatusNADecider pricingStatusNADecider;

    private static final String LINE_ITEM_ID_CA_SERVICE = "lineItemIdCAService";
    private static final String SITE_ID_CA_SERVICE = "SiteIdCAService";
    private static final String PRODUCT_CODE_CA_SERVICE = "productCodeCAService";

    private static final String LINE_ITEM_ID_STEEL_HEAD = "lineItemIdSteelhead";
    private static final String SITE_ID_STEEL_HEAD = "SiteIdSteelhead";
    private static final String PRODUCT_CODE_STEEL_HEAD = "productCodeSteelhead";

    private static final String LINE_ITEM_ID_CA_SITE = "lineItemCASite";
    private static final String SITE_ID_CA_SITE = "SiteIdCASite";
    private static final String PRODUCT_CODE_CA_SITE = "productCodeCASite";

    private static final String CUSTOMER_ID = "customerId";
    private static final String PROJECT_ID = "projectId";
    private static final String QUOTE_OPTION_ID = "quoteOptionId";
    private static final String PRODUCT_CODE = "sCode";
    public static final ProductIdentifier PRODUCT_CATEGORY_CODE = new ProductIdentifier("H012345", "product category", "versionNumber");

    private List<QuoteOptionDTO> quoteOptionDTOs = new ArrayList<QuoteOptionDTO>();

    @Captor
    private ArgumentCaptor<List<PriceClientRequest>> priceClientRequestsCaptor;
    private List<String> discountApprovedLineItems = newArrayList();

    @Before
    public void before() throws Exception {
        MockitoAnnotations.initMocks(this);

        siteFacade = mock(SiteFacade.class);
        futureProductInstanceClient = mock(ProductInstanceClient.class);
        pricingFacadeService = mock(PricingFacadeService.class);
        pmrClient = mock(PmrClient.class);
        projectResource = mock(ProjectResource.class);
        customerResource =  mock(CustomerResource.class);
        priceBookResource = mock(PriceBookResource.class);
        pricingStatusNADecider = mock(PricingStatusNADecider.class);
        priceHandlerService = new PriceHandlerService(customerResource, pmrClient, pricingFacadeService, siteFacade, futureProductInstanceClient,
                                                      pricingClient, projectResource, pricingStatusNADecider);
        quoteOptionDTOs.add(QuoteOptionDTO.newInstance(QUOTE_OPTION_ID, QUOTE_OPTION_ID, "name", "currency", "contractTerm", "createdBy", null));
        QuoteOptionResource quoteOptionResource = mock(QuoteOptionResource.class);
        when(projectResource.quoteOptionResource(PROJECT_ID)).thenReturn(quoteOptionResource);
        when(quoteOptionResource.get()).thenReturn(quoteOptionDTOs);
        QuoteOptionItemResource optionItemResource = mock(QuoteOptionItemResource.class);
        when(quoteOptionResource.quoteOptionItemResource(QUOTE_OPTION_ID)).thenReturn(optionItemResource);
        UserContext userContext = new UserContext("login", "token", "channel");
        userContext.getPermissions().indirectUser = false;
        UserContextManager.setCurrent(userContext);

        QuoteOptionItemDTO quoteOptionItemDTO = QuoteOptionItemDTOFixture.aQuoteOptionItemDTO()
                                                                         .withContract(new ContractDTO("id", "",
                                                                          newArrayList(new PriceBookDTO("id",
                                                                          "", "eupId", "ptpId", null, null)))).build();
        when(optionItemResource.get(Matchers.<String>any())).thenReturn(quoteOptionItemDTO);

        when(pricingStatusNADecider.decide(anyListOf(PriceClientRequest.class), Mockito.<PriceResponse>any(), anyListOf(PriceClientBidManagerAssetResponse.class))).thenReturn(Maps.<String, PriceClientResponse>newHashMap());
    }


    @Test
    public void shouldPriceProducts() throws Exception {
        final ProductChargingScheme productChargingScheme = new ProductChargingScheme("aCeaseScheme1", PricingEngine, Sales);
        final ProductInstance connectAccelerationService = new DefaultProductInstanceFixture(
            new ProductOfferingFixture()
                .withChargingScheme(productChargingScheme))
            .withLineItemId(LINE_ITEM_ID_CA_SERVICE)
            .withProductInstanceId("caService")
            .withProductIdentifier(PRODUCT_CODE_CA_SERVICE, "ConnectAccelerationService")
            .withSiteId(SITE_ID_CA_SERVICE)
            .withProjectId(PROJECT_ID)
            .withQuoteOptionId(QUOTE_OPTION_ID)
            .build();
        final ProductInstance steelHead = new DefaultProductInstanceFixture(
            new ProductOfferingFixture()
                .withChargingScheme(productChargingScheme)
                .withSiteSpecific())
            .withLineItemId(LINE_ITEM_ID_STEEL_HEAD)
            .withProductInstanceId("steelhead")
            .withProductIdentifier(PRODUCT_CODE_STEEL_HEAD, "SteelHead")
            .withSiteId(SITE_ID_STEEL_HEAD)
            .withProjectId(PROJECT_ID)
            .withQuoteOptionId(QUOTE_OPTION_ID)
            .build();
        final ProductInstance connectAccelerationSite = new DefaultProductInstanceFixture(
            new ProductOfferingFixture()
                .withChargingScheme(productChargingScheme)
                .withSiteSpecific())
            .withLineItemId(LINE_ITEM_ID_CA_SITE)
            .withProductInstanceId("caSite")
            .withProductIdentifier(PRODUCT_CODE_CA_SITE, "ConnectAccelerationSite")
            .withSiteId(SITE_ID_CA_SITE)
            .withProjectId(PROJECT_ID)
            .withQuoteOptionId(QUOTE_OPTION_ID)
            .withChildProductInstance(steelHead)
            .withRelatedToProductInstance(connectAccelerationService)
            .build();

        when(futureProductInstanceClient.get(new LineItemId(LINE_ITEM_ID_CA_SERVICE))).thenReturn(connectAccelerationService);
        when(futureProductInstanceClient.get(new LineItemId(LINE_ITEM_ID_CA_SITE))).thenReturn(connectAccelerationSite);
        when(futureProductInstanceClient.get(new LineItemId(LINE_ITEM_ID_STEEL_HEAD))).thenReturn(steelHead);
        when(pmrClient.getProductHCode(PRODUCT_CODE_STEEL_HEAD)).thenReturn(Optional.of(PRODUCT_CATEGORY_CODE));
        when(pmrClient.getProductHCode(PRODUCT_CODE_CA_SERVICE)).thenReturn(Optional.of(PRODUCT_CATEGORY_CODE));
        when(pmrClient.getProductHCode(PRODUCT_CODE_CA_SITE)).thenReturn(Optional.of(PRODUCT_CATEGORY_CODE));
        when(pmrClient.getProductHCode(PRODUCT_CODE)).thenReturn(Optional.of(PRODUCT_CATEGORY_CODE));
        when(customerResource.priceBookResource("customerId")).thenReturn(priceBookResource);
        when(futureProductInstanceClient.getSourceAsset(Matchers.<LengthConstrainingProductInstanceId>any())).thenReturn(Optional.<ProductInstance>absent());
        when(pricingClient.filterChargingSchemes(org.mockito.Matchers.any(ProductInstance.class), anyString(), anyString(), anyString(), anyBoolean())).thenReturn(newArrayList(productChargingScheme));

        Map<String, PriceClientResponse> lineItemPricing = priceHandlerService.processLineItemsForPricing(LINE_ITEM_ID_CA_SERVICE + "," + LINE_ITEM_ID_CA_SITE,
                                                                                                          CUSTOMER_ID, PROJECT_ID,
                                                                                                          QUOTE_OPTION_ID,
                                                                                                          true, "userToken");
        assertEquals(lineItemPricing.size(), 3);
        PriceClientResponse priceClientResponse = lineItemPricing.get("caSite");
        assertEquals(priceClientResponse.getProductInstanceId(), "caSite");
        assertEquals(priceClientResponse.getLineItemId(), LINE_ITEM_ID_CA_SITE);
        priceClientResponse = lineItemPricing.get("caService");
        assertEquals(priceClientResponse.getProductInstanceId(), "caService");
        assertEquals(priceClientResponse.getLineItemId(), LINE_ITEM_ID_CA_SERVICE);
        priceClientResponse = lineItemPricing.get("steelhead");
        assertEquals(priceClientResponse.getProductInstanceId(), "steelhead");
        assertEquals(priceClientResponse.getLineItemId(), LINE_ITEM_ID_CA_SITE);
    }

    @Test
    public void shouldPriceProductsWithRelatedToChilds() throws Exception {
        final ProductChargingScheme productChargingScheme = new ProductChargingScheme("aCeaseScheme1", PricingEngine, Sales);

        final ProductInstance relatedToChild = new DefaultProductInstanceFixture(
            new ProductOfferingFixture()
                .withChargingScheme(productChargingScheme).withIsInFrontCatalogue(true))
            .withLineItemId(LINE_ITEM_ID_CA_SERVICE)
            .withProductInstanceId("caServiceChild")
            .withProductIdentifier(PRODUCT_CODE_CA_SERVICE, "relatedChild")
            .withSiteId(SITE_ID_CA_SERVICE)
            .withProjectId(PROJECT_ID)
            .withQuoteOptionId(QUOTE_OPTION_ID)
            .build();

        final ProductInstance relatedTo = new DefaultProductInstanceFixture(
            new ProductOfferingFixture()
                .withChargingScheme(productChargingScheme))
            .withLineItemId(LINE_ITEM_ID_CA_SERVICE)
            .withProductInstanceId("caService")
            .withProductIdentifier(PRODUCT_CODE_CA_SERVICE, "related")
            .withSiteId(SITE_ID_CA_SERVICE)
            .withProjectId(PROJECT_ID)
            .withQuoteOptionId(QUOTE_OPTION_ID)
            .withChildProductInstance(relatedToChild).
             build();

        final ProductInstance child = new DefaultProductInstanceFixture(
            new ProductOfferingFixture()
                .withChargingScheme(productChargingScheme)
                .withSiteSpecific())
            .withLineItemId(LINE_ITEM_ID_STEEL_HEAD)
            .withProductInstanceId("steelhead")
            .withProductIdentifier(PRODUCT_CODE_STEEL_HEAD, "child")
            .withSiteId(SITE_ID_STEEL_HEAD)
            .withProjectId(PROJECT_ID)
            .withQuoteOptionId(QUOTE_OPTION_ID)
            .build();
        final ProductInstance parent = new DefaultProductInstanceFixture(
            new ProductOfferingFixture()
                .withChargingScheme(productChargingScheme)
                .withSiteSpecific())
            .withLineItemId(LINE_ITEM_ID_CA_SITE)
            .withProductInstanceId("caSite")
            .withProductIdentifier(PRODUCT_CODE_CA_SITE, "parent")
            .withSiteId(SITE_ID_CA_SITE)
            .withProjectId(PROJECT_ID)
            .withQuoteOptionId(QUOTE_OPTION_ID)
            .withChildProductInstance(child)
            .withRelatedToProductInstance(relatedTo)
            .build();

        when(futureProductInstanceClient.get(new LineItemId(LINE_ITEM_ID_CA_SERVICE))).thenReturn(relatedTo);
        when(futureProductInstanceClient.get(new LineItemId(LINE_ITEM_ID_CA_SITE))).thenReturn(parent);
        when(futureProductInstanceClient.get(new LineItemId(LINE_ITEM_ID_STEEL_HEAD))).thenReturn(child);

        when(pmrClient.getProductHCode(PRODUCT_CODE_STEEL_HEAD)).thenReturn(Optional.of(PRODUCT_CATEGORY_CODE));
        when(pmrClient.getProductHCode(PRODUCT_CODE_CA_SERVICE)).thenReturn(Optional.of(PRODUCT_CATEGORY_CODE));
        when(pmrClient.getProductHCode(PRODUCT_CODE_CA_SITE)).thenReturn(Optional.of(PRODUCT_CATEGORY_CODE));
        when(pmrClient.getProductHCode(PRODUCT_CODE)).thenReturn(Optional.of(PRODUCT_CATEGORY_CODE));
        when(customerResource.priceBookResource("customerId")).thenReturn(priceBookResource);

        when(futureProductInstanceClient.getSourceAsset(Matchers.<LengthConstrainingProductInstanceId>any())).thenReturn(Optional.<ProductInstance>absent());
        when(pricingClient.filterChargingSchemes(org.mockito.Matchers.any(ProductInstance.class), anyString(), anyString(), anyString(), anyBoolean())).thenReturn(newArrayList(productChargingScheme));

        Map<String, PriceClientResponse> lineItemPricing = priceHandlerService.processLineItemsForPricing(LINE_ITEM_ID_CA_SERVICE + "," + LINE_ITEM_ID_CA_SITE,
                                                                                                          CUSTOMER_ID, PROJECT_ID,
                                                                                                          QUOTE_OPTION_ID,
                                                                                                          true, "userToken");
        assertEquals(lineItemPricing.size(), 4);
        PriceClientResponse priceClientResponse = lineItemPricing.get("caSite");
        assertEquals(priceClientResponse.getProductInstanceId(), "caSite");
        assertEquals(priceClientResponse.getLineItemId(), LINE_ITEM_ID_CA_SITE);
        priceClientResponse = lineItemPricing.get("caService");
        assertEquals(priceClientResponse.getProductInstanceId(), "caService");
        assertEquals(priceClientResponse.getLineItemId(), LINE_ITEM_ID_CA_SERVICE);
        priceClientResponse = lineItemPricing.get("caServiceChild");
        assertEquals(priceClientResponse.getProductInstanceId(), "caServiceChild");
        assertEquals(priceClientResponse.getLineItemId(), LINE_ITEM_ID_CA_SERVICE);
        priceClientResponse = lineItemPricing.get("steelhead");
        assertEquals(priceClientResponse.getProductInstanceId(), "steelhead");
        assertEquals(priceClientResponse.getLineItemId(), LINE_ITEM_ID_CA_SITE);
    }

    @Test(expected = RuntimeException.class)
    public void shouldThrowRuntimeExceptionWhenProductCategoryCanNotBeFound() throws Exception {
        final ProductChargingScheme productChargingScheme = new ProductChargingScheme("aCeaseScheme1", PricingEngine, Sales);

        final ProductInstance connectAccelerationSite = new DefaultProductInstanceFixture(
            new ProductOfferingFixture()
                .withChargingScheme(productChargingScheme)
                .withSiteSpecific())
            .withLineItemId(LINE_ITEM_ID_CA_SITE)
            .withProductInstanceId("caSite")
            .withProductIdentifier(PRODUCT_CODE_CA_SITE, "ConnectAccelerationSite")
            .withSiteId(SITE_ID_CA_SITE)
            .withProjectId(PROJECT_ID)
            .withQuoteOptionId(QUOTE_OPTION_ID)
            .build();


        when(futureProductInstanceClient.get(new LineItemId(LINE_ITEM_ID_CA_SITE))).thenReturn(connectAccelerationSite);
        when(pmrClient.getProductHCode(anyString())).thenReturn(Optional.<ProductIdentifier>absent());
        when(futureProductInstanceClient.getSourceAsset(Matchers.<LengthConstrainingProductInstanceId>any())).thenReturn(Optional.<ProductInstance>absent());
        when(pricingClient.filterChargingSchemes(org.mockito.Matchers.any(ProductInstance.class), anyString(), anyString(), anyString(), anyBoolean())).thenReturn(newArrayList(productChargingScheme));

        priceHandlerService.processLineItemsForPricing(LINE_ITEM_ID_CA_SITE,
                                                       CUSTOMER_ID, PROJECT_ID,
                                                       QUOTE_OPTION_ID,
                                                       true, "userToken");
    }

    @Test
    public void shouldUseProductCategoryFromAssetOwnerWhenPricingIfTheAssetIsACommonProduct() throws Exception {
        final ProductChargingScheme productChargingScheme = new ProductChargingScheme("aCeaseScheme1", PricingEngine, Sales);

        final ProductInstance connectAccelerationSite = new DefaultProductInstanceFixture(
            new ProductOfferingFixture()
                .withChargingScheme(productChargingScheme)
                .withSiteSpecific())
            .withLineItemId(LINE_ITEM_ID_CA_SITE)
            .withProductInstanceId("caSite")
            .withProductIdentifier(PRODUCT_CODE_CA_SITE, "ConnectAccelerationSite")
            .withSiteId(SITE_ID_CA_SITE)
            .withProjectId(PROJECT_ID)
            .withQuoteOptionId(QUOTE_OPTION_ID)
            .build();


        when(futureProductInstanceClient.get(new LineItemId(LINE_ITEM_ID_CA_SITE))).thenReturn(connectAccelerationSite);

        when(futureProductInstanceClient.getRelatedToLineItemIdsWhoOwnLineItemId(LINE_ITEM_ID_CA_SITE)).thenReturn(Lists.<String>newArrayList("ownerLineItem"));
        when(futureProductInstanceClient.getAssetDTO(new LineItemId("ownerLineItem"))).thenReturn(AssetDTOFixture.anAsset().withProductCode(new ProductCode("ownerAssetCode")).build());
        when(pmrClient.getProductHCode("ownerAssetCode")).thenReturn(Optional.<ProductIdentifier>of(new ProductIdentifier("OwnerCategory", "owner product category", "versionNumber")));
        when(pmrClient.getProductHCode(PRODUCT_CODE)).thenReturn(Optional.of(PRODUCT_CATEGORY_CODE));
        when(customerResource.priceBookResource("customerId")).thenReturn(priceBookResource);

        when(pmrClient.getProductHCode(PRODUCT_CODE_CA_SITE)).thenReturn(Optional.<ProductIdentifier>absent());
        when(futureProductInstanceClient.getSourceAsset(Matchers.<LengthConstrainingProductInstanceId>any())).thenReturn(Optional.<ProductInstance>absent());
        when(pricingClient.filterChargingSchemes(org.mockito.Matchers.any(ProductInstance.class), anyString(), anyString(), anyString(), anyBoolean())).thenReturn(newArrayList(productChargingScheme));

        Map<String, PriceClientResponse> lineItemPricing = priceHandlerService.processLineItemsForPricing(LINE_ITEM_ID_CA_SITE,
                                                                                                          CUSTOMER_ID, PROJECT_ID,
                                                                                                          QUOTE_OPTION_ID,
                                                                                                          true, "userToken");
        assertEquals(lineItemPricing.size(), 1);
        PriceClientResponse priceClientResponse = lineItemPricing.get("caSite");
        assertEquals(priceClientResponse.getProductInstanceId(), "caSite");
        assertEquals(priceClientResponse.getLineItemId(), LINE_ITEM_ID_CA_SITE);
    }

    @Test
    public void shouldSetProductInstanceSiteIdFromCustomerResourceWhen() throws Exception {
        final ProductChargingScheme productChargingScheme = new ProductChargingScheme("aCeaseScheme1", PricingEngine, Sales);
        final ProductInstance steelHead = new DefaultProductInstanceFixture(
            new ProductOfferingFixture()
                .withChargingScheme(productChargingScheme)
                .withSiteSpecific()
            )
            .withLineItemId(LINE_ITEM_ID_STEEL_HEAD)
            .withProductInstanceId("steelhead")
            .withStatus(ProductInstanceStatus.LIVE)
            .withProductIdentifier(PRODUCT_CODE_STEEL_HEAD, "SteelHead")
            .withProjectId(PROJECT_ID)
            .withQuoteOptionId(QUOTE_OPTION_ID)
            .withCustomerId(CUSTOMER_ID)
            .build();

        final ProductInstance connectAccelerationSite = new DefaultProductInstanceFixture(
            new ProductOfferingFixture()
                .withChargingScheme(productChargingScheme)
                .withSiteSpecific()
            )
            .withLineItemId(LINE_ITEM_ID_CA_SITE)
            .withProductInstanceId("caSite")
            .withStatus(ProductInstanceStatus.LIVE)
            .withProductIdentifier(PRODUCT_CODE_CA_SITE, "ConnectAccelerationSite")
            .withProjectId(PROJECT_ID)
            .withQuoteOptionId(QUOTE_OPTION_ID)
            .withCustomerId(CUSTOMER_ID)
            .withChildProductInstance(steelHead)
            .build();

        when(futureProductInstanceClient.get(new LineItemId(LINE_ITEM_ID_CA_SITE))).thenReturn(connectAccelerationSite);
        when(futureProductInstanceClient.get(new LineItemId(LINE_ITEM_ID_STEEL_HEAD))).thenReturn(steelHead);

        when(pmrClient.getProductHCode(PRODUCT_CODE_STEEL_HEAD)).thenReturn(Optional.of(PRODUCT_CATEGORY_CODE));
        when(pmrClient.getProductHCode(PRODUCT_CODE_CA_SITE)).thenReturn(Optional.of(PRODUCT_CATEGORY_CODE));
        when(pricingClient.filterChargingSchemes(org.mockito.Matchers.any(ProductInstance.class), anyString(), anyString(), anyString(), anyBoolean())).thenReturn(newArrayList(productChargingScheme));

        when(pmrClient.getProductHCode(PRODUCT_CODE)).thenReturn(Optional.of(PRODUCT_CATEGORY_CODE));
        when(customerResource.priceBookResource("customerId")).thenReturn(priceBookResource);
        SiteResource siteResource = mock(SiteResource.class);
        when(customerResource.siteResource(CUSTOMER_ID)).thenReturn(siteResource);
        when(siteResource.getCentralSite(PROJECT_ID)).thenReturn(SiteDTOFixture.aSiteDTO().withBfgSiteId(SITE_ID_CA_SITE).build());

        when(futureProductInstanceClient.getSourceAsset(Matchers.<LengthConstrainingProductInstanceId>any())).thenReturn(Optional.<ProductInstance>absent());

        Map<String, PriceClientResponse> lineItemPricing = priceHandlerService.processLineItemsForPricing(LINE_ITEM_ID_CA_SITE, CUSTOMER_ID,
                                                                                                          PROJECT_ID, QUOTE_OPTION_ID, true,
                                                                                                          "userToken");
        assertEquals(lineItemPricing.size(), 2);
        PriceClientResponse priceClientResponse = lineItemPricing.get("caSite");
        assertEquals(priceClientResponse.getProductInstanceId(), "caSite");
        assertEquals(priceClientResponse.getLineItemId(), LINE_ITEM_ID_CA_SITE);
        priceClientResponse = lineItemPricing.get("steelhead");
        assertEquals(priceClientResponse.getProductInstanceId(), "steelhead");
        assertEquals(priceClientResponse.getLineItemId(), LINE_ITEM_ID_CA_SITE);
        verify(pricingFacadeService).fetchPricesAndApplyAggregation(priceClientRequestsCaptor.capture(), anyMapOf(String.class, PriceClientResponse.class),
                                                                    Matchers.<PriceRequestType>any(), anyMapOf(String.class, PriceBookDTO.class), anyListOf(String.class));

        List<PriceClientRequest> priceClientRequests = priceClientRequestsCaptor.getValue();
        assertThat(priceClientRequests.size(), Is.is(2));

        for(PriceClientRequest priceClientRequest : priceClientRequests) {
            assertThat(priceClientRequest.getSiteId(), Is.is(SITE_ID_CA_SITE));
        }

        verify(pricingStatusNADecider).decide(eq(priceClientRequests), Mockito.<PriceResponse>any(), anyListOf(PriceClientBidManagerAssetResponse.class));
    }

    @Test
    public void shouldReturnCurrentPriceStatusForAlreadyFIRMLineItem() throws Exception {
        ProductInstance instance = DefaultProductInstanceFixture.aProductInstance()
                                                                .withLineItemId("aLineItemId")
                                                                .withProductInstanceId("aProductInstanceId")
                                                                .withProductOffering(ProductOfferingFixture.aProductOffering()
                                                                                                           .withChargingScheme(ProductChargingSchemeFixture.aChargingScheme().build()))
                                                                .withPricingStatus(PricingStatus.FIRM)
                                                                .withSiteId(SITE_ID_CA_SITE)
                                                                .build();

        when(futureProductInstanceClient.get(new LineItemId("aLineItemId"))).thenReturn(instance);

        Map<String, PriceClientResponse> response = priceHandlerService.processLineItemsForPricing("aLineItemId", CUSTOMER_ID, PROJECT_ID,
                                                                                                   QUOTE_OPTION_ID, true, "userToken");
        assertThat(response.size(), is(1));

        PriceClientResponse restResponse = response.get("aProductInstanceId");
        assertThat(restResponse.getPriceStatus(), is(PricingStatus.FIRM.getDescription()));

        verify(pricingFacadeService).fetchPricesAndApplyAggregation(eq(Lists.<PriceClientRequest>newArrayList()), eq(response), eq(PriceRequestType.getPrice), anyMapOf(String.class, PriceBookDTO.class), eq(discountApprovedLineItems));
    }

    @Test
    public void shouldFetchNAPriceResponse() throws Exception {
        final ProductChargingScheme productChargingScheme = new ProductChargingScheme("aScheme", PricingEngine, Sales);
        final ProductInstance connectAccelerationSite = new DefaultProductInstanceFixture(
            new ProductOfferingFixture()
                .withChargingScheme(productChargingScheme)
                .withSiteSpecific())
            .withLineItemId(LINE_ITEM_ID_CA_SITE)
            .withProductInstanceId("caSite")
            .withProductIdentifier(PRODUCT_CODE_CA_SITE, "ConnectAccelerationSite")
            .withProjectId(PROJECT_ID)
            .withQuoteOptionId(QUOTE_OPTION_ID)
            .withCustomerId(CUSTOMER_ID)
            .build();

        when(futureProductInstanceClient.get(new LineItemId(LINE_ITEM_ID_CA_SITE))).thenReturn(connectAccelerationSite);

        when(pmrClient.getProductHCode(PRODUCT_CODE_STEEL_HEAD)).thenReturn(Optional.of(PRODUCT_CATEGORY_CODE));
        when(pmrClient.getProductHCode(PRODUCT_CODE_CA_SITE)).thenReturn(Optional.of(PRODUCT_CATEGORY_CODE));
        when(pricingClient.filterChargingSchemes(org.mockito.Matchers.any(ProductInstance.class), anyString(), anyString(), anyString(), anyBoolean())).thenReturn(Collections.<ProductChargingScheme>emptyList());

        SiteResource siteResource = mock(SiteResource.class);
        when(customerResource.siteResource(CUSTOMER_ID)).thenReturn(siteResource);
        when(siteResource.getCentralSite(PROJECT_ID)).thenReturn(SiteDTOFixture.aSiteDTO().withBfgSiteId(SITE_ID_CA_SITE).build());

        when(futureProductInstanceClient.getSourceAsset(Matchers.<LengthConstrainingProductInstanceId>any())).thenReturn(Optional.<ProductInstance>absent());

        AssetDTO assetDTO = mock(AssetDTO.class);
        when(futureProductInstanceClient.getAssetDtoByAssetKey(new LengthConstrainingProductInstanceId(anyString()), new ProductInstanceVersion(anyLong()))).thenReturn(assetDTO);
        connectAccelerationSite.setPricingStatus(PricingStatus.NOT_APPLICABLE);
        when(futureProductInstanceClient.getLatestProduct(new ProductInstanceId(anyString()), anyString())).thenReturn(connectAccelerationSite);

        Map<String, PriceClientResponse> lineItemPricing = priceHandlerService.processLineItemsForPricing(LINE_ITEM_ID_CA_SITE, CUSTOMER_ID,
                                                                                                          PROJECT_ID, QUOTE_OPTION_ID, true,
                                                                                                          "userToken");
        assertEquals(lineItemPricing.size(), 1);
        PriceClientResponse priceClientResponse = lineItemPricing.get("caSite");
        assertEquals(priceClientResponse.getProductInstanceId(), "caSite");
        assertEquals(priceClientResponse.getLineItemId(), LINE_ITEM_ID_CA_SITE);
        assertEquals(priceClientResponse.getPriceStatus(), PricingStatus.NOT_APPLICABLE.getDescription());
    }

    @Test
    public void shouldUseProductCategoryFromAssetCharacteristicsWhenPricingIfTheAssetIsASharedProduct() throws Exception {
        final ProductChargingScheme productChargingScheme = new ProductChargingScheme("aCeaseScheme1", PricingEngine, Sales);

        final ProductInstance connectAccelerationSite = new DefaultProductInstanceFixture(
            new ProductOfferingFixture()
                .withChargingScheme(productChargingScheme)
                .withSiteSpecific())
            .withLineItemId(LINE_ITEM_ID_CA_SITE)
            .withProductInstanceId("caSite")
            .withProductIdentifier(PRODUCT_CODE_CA_SITE, "ConnectAccelerationSite")
            .withSiteId(SITE_ID_CA_SITE)
            .withProjectId(PROJECT_ID)
            .withQuoteOptionId(QUOTE_OPTION_ID)
            .withAttributes(new HashMap<String, Object>() {{
                put("PRODUCT CATEGORY CODE", "aProductCategoryCode");
            }})
            .build();


        when(futureProductInstanceClient.get(new LineItemId(LINE_ITEM_ID_CA_SITE))).thenReturn(connectAccelerationSite);

        when(futureProductInstanceClient.getRelatedToLineItemIdsWhoOwnLineItemId(LINE_ITEM_ID_CA_SITE)).thenReturn(Lists.<String>newArrayList("ownerLineItem"));
        when(futureProductInstanceClient.getAssetDTO(new LineItemId("ownerLineItem"))).thenReturn(AssetDTOFixture.anAsset().withProductCode(new ProductCode("ownerAssetCode")).build());
        when(futureProductInstanceClient.getSourceAsset(Matchers.<LengthConstrainingProductInstanceId>any())).thenReturn(Optional.<ProductInstance>absent());
        when(pmrClient.getProductHCode(PRODUCT_CODE)).thenReturn(Optional.of(PRODUCT_CATEGORY_CODE));
        when(customerResource.priceBookResource("customerId")).thenReturn(priceBookResource);
        when(pricingClient.filterChargingSchemes(org.mockito.Matchers.any(ProductInstance.class), anyString(), anyString(), anyString(), anyBoolean())).thenReturn(newArrayList(productChargingScheme));

        Map<String, PriceClientResponse> lineItemPricing = priceHandlerService.processLineItemsForPricing(LINE_ITEM_ID_CA_SITE,
                                                                                                          CUSTOMER_ID, PROJECT_ID,
                                                                                                          QUOTE_OPTION_ID,
                                                                                                          true, "userToken");
        assertEquals(lineItemPricing.size(), 1);
        PriceClientResponse priceClientResponse = lineItemPricing.get("caSite");
        assertEquals(priceClientResponse.getProductInstanceId(), "caSite");
        assertEquals(priceClientResponse.getLineItemId(), LINE_ITEM_ID_CA_SITE);
        assertEquals(connectAccelerationSite.isSharedProduct(), true);
        assertEquals(connectAccelerationSite.getInheritedHCode().get().getProductId(), "aProductCategoryCode");
    }
}
