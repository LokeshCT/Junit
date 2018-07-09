package com.bt.rsqe.projectengine.web.quoteoptionpricing;

import com.bt.rsqe.client.LookupHandler;
import com.bt.rsqe.customerinventory.dto.AssetDTO;
import com.bt.rsqe.customerinventory.parameter.LineItemId;
import com.bt.rsqe.customerrecord.SiteDTO;
import com.bt.rsqe.domain.bom.fixtures.SellableProductFixture;
import com.bt.rsqe.domain.product.ProductOffering;
import com.bt.rsqe.domain.product.SellableProduct;
import com.bt.rsqe.domain.product.lookup.LookupHandlerRegistry;
import com.bt.rsqe.domain.product.lookup.LookupStrategies;
import com.bt.rsqe.domain.project.PricingStatus;
import com.bt.rsqe.dto.TableResponseDTO;
import com.bt.rsqe.enums.PriceCategory;
import com.bt.rsqe.enums.PriceType;
import com.bt.rsqe.expedio.fixtures.SiteDTOFixture;
import com.bt.rsqe.pmr.client.PmrClient;
import com.bt.rsqe.pmr.client.PmrMocker;
import com.bt.rsqe.pricing.config.dto.PricingConfig;
import com.bt.rsqe.dto.BidManagerCommentsDTO;
import com.bt.rsqe.projectengine.QuoteOptionDTO;
import com.bt.rsqe.projectengine.web.facades.BidManagerCommentsFacade;
import com.bt.rsqe.projectengine.web.facades.FutureAssetPricesFacade;
import com.bt.rsqe.projectengine.web.facades.LineItemFacade;
import com.bt.rsqe.projectengine.web.facades.ProductIdentifierFacade;
import com.bt.rsqe.projectengine.web.facades.QuoteOptionFacade;
import com.bt.rsqe.projectengine.web.facades.SiteFacade;
import com.bt.rsqe.projectengine.web.fixtures.FutureAssetPricesDTOFixture;
import com.bt.rsqe.projectengine.web.fixtures.FutureAssetPricesModelFixture;
import com.bt.rsqe.projectengine.web.fixtures.LineItemModelFixture;
import com.bt.rsqe.projectengine.web.fixtures.PriceLineDTOFixture;
import com.bt.rsqe.projectengine.web.model.FutureAssetPricesModel;
import com.bt.rsqe.projectengine.web.model.LineItemModel;
import com.bt.rsqe.projectengine.web.model.lineitemvisitor.LineItemVisitor;
import com.bt.rsqe.projectengine.web.view.Products;
import com.bt.rsqe.projectengine.web.view.QuoteOptionPricingDTO;
import com.bt.rsqe.projectengine.web.view.QuoteOptionUsagePricingDTO;
import com.bt.rsqe.projectengine.web.view.filtering.DataTableFilterValues;
import com.bt.rsqe.projectengine.web.view.filtering.PaginatedFilter;
import com.bt.rsqe.projectengine.web.view.filtering.PaginatedPricingTabViewFilter;
import com.bt.rsqe.projectengine.web.view.filtering.PricingTabView;
import com.bt.rsqe.projectengine.web.view.filtering.QueryParamStub;
import com.bt.rsqe.projectengine.web.view.pagination.NoPagination;
import com.bt.rsqe.projectengine.web.view.pagination.Pagination;
import com.bt.rsqe.security.PermissionsDTO;
import com.bt.rsqe.security.UserContext;
import com.bt.rsqe.security.UserContextManager;
import com.bt.rsqe.security.UserPrincipal;
import com.google.common.collect.Lists;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.*;
import static com.google.common.collect.Maps.*;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@SuppressWarnings({"PMD.TooManyMethods", "PMD.TooManyStaticImports"})   // Test class
public class QuoteOptionPricingOrchestratorTest
{
    private static final Logger LOGGER = LoggerFactory.getLogger(QuoteOptionPricingOrchestratorTest.class);

    private static final String CUSTOMER_ID     = "CUSTOMER_ID";
    private static final String CONTRACT_ID     = "CONTRACT_ID";
    private static final String PROJECT_ID      = "projectId";
    private static final String QUOTE_OPTION_ID = "quoteOptionId";

    // Mocks and values for fields for testing.
    private static final LineItemModel MODEL_1 = mock(LineItemModel.class);
    private static final LineItemModel MODEL_2 = mock(LineItemModel.class);
    private static final LineItemModel MODEL_3 = mock(LineItemModel.class);

    private static final String MODEL_1_LINE_ITEM_ID = "id1";
    private static final String MODEL_2_LINE_ITEM_ID = "id2";
    private static final String MODEL_3_LINE_ITEM_ID = "id3";

    private static final String MODEL_1_DISPLAY_NAME = "Model1";
    private static final String MODEL_2_DISPLAY_NAME = "Model2";
    private static final String MODEL_3_DISPLAY_NAME = "Model3";

    private static final String MODEL_1_PRODUCT_NAME = "product1";
    private static final String MODEL_2_PRODUCT_NAME = "product2";
    private static final String MODEL_3_PRODUCT_NAME = "Connect Acceleration Site";

    private static final String MODEL_1_COUNTRY = "country1";
    private static final String MODEL_2_COUNTRY = "country2";
    private static final String MODEL_3_COUNTRY = "country3";

    private static final String MODEL_1_ACTION = "action1";
    private static final String MODEL_2_ACTION = "Cease";
    private static final String MODEL_3_ACTION = "Modify";

    private static final boolean MODEL_1_READ_ONLY = false;
    private static final boolean MODEL_2_READ_ONLY = false;
    private static final boolean MODEL_3_READ_ONLY = true;

    // The 'empty' result returned by a call to the serializeString() method.
    private static final String EMPTY_JSON = "{ }";

    private static final PaginatedFilter PAGINATED_FILTER = new PaginatedPricingTabViewFilter(
        DataTableFilterValues.parse(new QueryParamStub()), new NoPagination());

    private QuoteOptionPricingOrchestrator pricingOrchestrator;
    private ProductIdentifierFacade productIdentifierFacade;
    private SiteFacade siteFacade;
    private LineItemFacade lineItemFacade;
    private QuoteOptionFacade quoteOptionFacade;
    private SellableProduct product1, product2;
    private LookupHandler lookupHandler;
    private PmrClient pmr;
    private BidManagerCommentsFacade bidManagerCommentsFacade;

    @Before
    public void before() {
        lookupHandler = mock(LookupHandler.class);
        LookupHandlerRegistry.get().unRegisterAll_testOnly();
        LookupHandlerRegistry.get().register(LookupStrategies.RULESET_STRATEGY, lookupHandler);

        productIdentifierFacade = mock(ProductIdentifierFacade.class);
        siteFacade = mock(SiteFacade.class);
        lineItemFacade = mock(LineItemFacade.class);
        quoteOptionFacade = mock(QuoteOptionFacade.class);
        bidManagerCommentsFacade = mock(BidManagerCommentsFacade.class);
        pricingOrchestrator = new QuoteOptionPricingOrchestrator(productIdentifierFacade, lineItemFacade, quoteOptionFacade, siteFacade, bidManagerCommentsFacade);

        UserContextManager.setCurrent(new UserContext(new UserPrincipal("loginName"), "token", new PermissionsDTO(true, false, true, false, false, false)));

        product1 = SellableProductFixture.aProduct().withId("productCode1").withName("productName1").build();
        product2 = SellableProductFixture.aProduct().withId("productCode2").withName("productName2").build();

        pmr = PmrMocker.getMockedInstance(true);
    }

    /**
     * Builds Line Item mocks with values for testing.
     */
    @Before
    public void initializeMocks()
    {
        // Build Line Item 1 Mock:
        when(MODEL_1.getSite()).thenReturn(mock(SiteDTO.class));
        when(MODEL_1.getLineItemId()).thenReturn(new LineItemId(MODEL_1_LINE_ITEM_ID));
        when(MODEL_1.getDisplayName()).thenReturn(MODEL_1_DISPLAY_NAME);
        when(MODEL_1.getProductName()).thenReturn(MODEL_1_PRODUCT_NAME);
        when(MODEL_1.getSite().getCountry()).thenReturn(MODEL_1_COUNTRY);
        when(MODEL_1.getAction()).thenReturn(MODEL_1_ACTION);
        when(MODEL_1.isReadOnly()).thenReturn(MODEL_1_READ_ONLY);

        // Build Line Item 2 Mock:
        when(MODEL_2.getSite()).thenReturn(mock(SiteDTO.class));
        when(MODEL_2.getLineItemId()).thenReturn(new LineItemId(MODEL_2_LINE_ITEM_ID));
        when(MODEL_2.getDisplayName()).thenReturn(MODEL_2_DISPLAY_NAME);
        when(MODEL_2.getProductName()).thenReturn(MODEL_2_PRODUCT_NAME);
        when(MODEL_2.getSite().getCountry()).thenReturn(MODEL_2_COUNTRY);
        when(MODEL_2.getAction()).thenReturn(MODEL_2_ACTION);
        when(MODEL_3.isReadOnly()).thenReturn(MODEL_2_READ_ONLY);

        // Build Line Item 3 Mock:
        when(MODEL_3.getSite()).thenReturn(mock(SiteDTO.class));
        when(MODEL_3.getLineItemId()).thenReturn(new LineItemId(MODEL_3_LINE_ITEM_ID));
        when(MODEL_3.getDisplayName()).thenReturn(MODEL_3_DISPLAY_NAME);
        when(MODEL_3.getProductName()).thenReturn(MODEL_3_PRODUCT_NAME);
        when(MODEL_3.getSite().getCountry()).thenReturn(MODEL_3_COUNTRY);
        when(MODEL_3.getAction()).thenReturn(MODEL_3_ACTION);
        when(MODEL_3.isReadOnly()).thenReturn(MODEL_3_READ_ONLY);
    }

    @Test
    public void shouldVisitEachLineItem() throws Exception {
        final LineItemModel lineItemModel1 = mock(LineItemModel.class);
        final LineItemModel lineItemModel2 = mock(LineItemModel.class);
        when(lineItemModel1.isProductVisibleOnlineSummary()).thenReturn(true);
        when(lineItemModel2.isProductVisibleOnlineSummary()).thenReturn(true);
        List<LineItemModel> lineItemModelList = asList(lineItemModel1, lineItemModel2);
        when(lineItemFacade.fetchLineItems(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID, PriceSuppressStrategy.UI_PRICES)).thenReturn(lineItemModelList);
        when(lineItemModelList.get(0).getProductName()).thenReturn("a");
        when(lineItemModelList.get(1).getProductName()).thenReturn("a");
        when(lineItemModelList.get(0).getSite()).thenReturn(new SiteDTO("1", "SITE 1"));
        when(lineItemModelList.get(1).getSite()).thenReturn(new SiteDTO("2", "SITE 2"));
        pricingOrchestrator.buildStandardResponse(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID, PAGINATED_FILTER, PriceSuppressStrategy.UI_PRICES);
        verify(lineItemModel1, times(1)).accept(any(LineItemVisitor.class));
        verify(lineItemModel2, times(1)).accept(any(LineItemVisitor.class));
    }

    @Test
    public void shouldSortLineItemListByProductNameAndSite() {
        final Pagination mockPagination = mock(Pagination.class);
        final PaginatedFilter paginatedFilter = new PaginatedPricingTabViewFilter(DataTableFilterValues.parse(new QueryParamStub()), mockPagination);
        final ArrayList<LineItemModel> lineItems = newArrayList();

        FutureAssetPricesFacade futureAssetPricesFacade = mock(FutureAssetPricesFacade.class);
        final ProductIdentifierFacade productIdentifierFacade1 = mock(ProductIdentifierFacade.class);
        final ProductIdentifierFacade productIdentifierFacade2 = mock(ProductIdentifierFacade.class);
        final ProductIdentifierFacade productIdentifierFacade3 = mock(ProductIdentifierFacade.class);
        when(productIdentifierFacade1.getDisplayName(anyString())).thenReturn("aName");
        when(productIdentifierFacade1.getProductName(anyString())).thenReturn("aName");
        when(productIdentifierFacade2.getDisplayName(anyString())).thenReturn("bName");
        when(productIdentifierFacade2.getProductName(anyString())).thenReturn("bName");
        when(productIdentifierFacade3.getDisplayName(anyString())).thenReturn("aName");
        when(productIdentifierFacade3.getProductName(anyString())).thenReturn("aName");
        when(productIdentifierFacade1.getDisplayName(any(ProductOffering.class))).thenReturn("aName");
        when(productIdentifierFacade1.getProductName(any(ProductOffering.class))).thenReturn("aName");
        when(productIdentifierFacade2.getDisplayName(any(ProductOffering.class))).thenReturn("bName");
        when(productIdentifierFacade2.getProductName(any(ProductOffering.class))).thenReturn("bName");
        when(productIdentifierFacade3.getDisplayName(any(ProductOffering.class))).thenReturn("aName");
        when(productIdentifierFacade3.getProductName(any(ProductOffering.class))).thenReturn("aName");

        final FutureAssetPricesModel l1Model = FutureAssetPricesModelFixture.aFutureAssetPricesModel()
                                                                                      .with(productIdentifierFacade1)
                                                                                      .with(siteFacade)
                                                                                      .with(FutureAssetPricesDTOFixture.aFutureAssetPricesDTO()
                                                                                                                       .withProductCode("P1")
                                                                                                                       .withSiteId("1")
                                                                                                                       .withPricingModel("aPricingModel"))
                                                                                      .build();

        final FutureAssetPricesModel l2Model = FutureAssetPricesModelFixture.aFutureAssetPricesModel()
                                                                                      .with(productIdentifierFacade2)
                                                                                      .with(siteFacade)
                                                                                      .with(FutureAssetPricesDTOFixture.aFutureAssetPricesDTO()
                                                                                                                       .withProductCode("P2")
                                                                                                                       .withSiteId("2")
                                                                                                                       .withPricingModel("aPricingModel"))
                                                                                      .build();

        final FutureAssetPricesModel l3Model = FutureAssetPricesModelFixture.aFutureAssetPricesModel()
                                                                                      .with(productIdentifierFacade3)
                                                                                      .with(siteFacade)
                                                                                      .with(FutureAssetPricesDTOFixture.aFutureAssetPricesDTO()
                                                                                                                       .withProductCode("P1")
                                                                                                                       .withSiteId("3")
                                                                                                                       .withPricingModel("aPricingModel"))
                                                                                      .build();

        doAnswer(new Answer<FutureAssetPricesModel>() {
            @Override
            public FutureAssetPricesModel answer(InvocationOnMock invocation) throws Throwable {
                AssetDTO asset = (AssetDTO)invocation.getArguments()[3];
                asset.setPricingStatus(PricingStatus.FIRM);
                String lineItemId = asset.getLineItemId();

                if("L1".equals(lineItemId)) {
                    return l1Model;
                } else if("L2".equals(lineItemId)) {
                    return l2Model;
                } else {
                    return l3Model;
                }
            }
        }).when(futureAssetPricesFacade).get(eq(CUSTOMER_ID), eq(PROJECT_ID), eq(QUOTE_OPTION_ID), Matchers.<AssetDTO>any(), eq(PriceSuppressStrategy.None));

        lineItems.add(LineItemModelFixture.aLineItemModel()
                                          .with(futureAssetPricesFacade)
                                          .with(productIdentifierFacade1)
                                          .withProjectId(PROJECT_ID)
                                          .withQuoteOptionId(QUOTE_OPTION_ID)
                                          .withContractId(CONTRACT_ID)
                                          .withCustomerId(CUSTOMER_ID)
                                          .withQuoteOptionItemDTOId("L1").build());
        lineItems.add(LineItemModelFixture.aLineItemModel()
                                          .with(futureAssetPricesFacade)
                                          .with(productIdentifierFacade2)
                                          .withProjectId(PROJECT_ID)
                                          .withQuoteOptionId(QUOTE_OPTION_ID)
                                          .withContractId(CONTRACT_ID)
                                          .withCustomerId(CUSTOMER_ID)
                                          .withQuoteOptionItemDTOId("L2").build());
        lineItems.add(LineItemModelFixture.aLineItemModel()
                                          .with(futureAssetPricesFacade)
                                          .with(productIdentifierFacade3)
                                          .withProjectId(PROJECT_ID)
                                          .withQuoteOptionId(QUOTE_OPTION_ID)
                                          .withContractId(CONTRACT_ID)
                                          .withCustomerId(CUSTOMER_ID)
                                          .withQuoteOptionItemDTOId("L3").build());

        when(mockPagination.paginate(lineItems)).thenReturn(lineItems);
        when(productIdentifierFacade.getAllSellableProducts()).thenReturn(new Products());
        when(lineItemFacade.fetchLineItems(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID, PriceSuppressStrategy.UI_PRICES)).thenReturn(lineItems);
        when(siteFacade.get("customerId", PROJECT_ID, "1")).thenReturn(new SiteDTO("1", "name"));
        when(siteFacade.get("customerId", PROJECT_ID, "2")).thenReturn(new SiteDTO("2", "name"));
        when(siteFacade.get("customerId", PROJECT_ID, "3")).thenReturn(new SiteDTO("3", "name"));
        QuoteOptionPricingDTO quoteOptionPricingDTO = pricingOrchestrator.buildStandardResponse(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID, paginatedFilter, PriceSuppressStrategy.UI_PRICES);
        List<QuoteOptionPricingDTO.ItemRowDTO> items = quoteOptionPricingDTO.itemDTOs;

        assertThat(items.get(0).product, is("aName"));
        assertThat(items.get(1).product, is("aName"));
        assertThat(items.get(2).product, is("bName"));
    }

    @Test
    public void shouldGetPaginationInformation() throws Exception {
        final Pagination mockPagination = mock(Pagination.class);
        final PaginatedFilter paginatedFilter = new PaginatedPricingTabViewFilter(DataTableFilterValues.parse(new QueryParamStub()), mockPagination);

        when(productIdentifierFacade.getAllSellableProducts()).thenReturn(new Products());
        when(lineItemFacade.fetchLineItems(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID, PriceSuppressStrategy.None)).thenReturn(new ArrayList<LineItemModel>());
        when(mockPagination.getPageNumber()).thenReturn(1);

        final QuoteOptionPricingDTO result = pricingOrchestrator.buildStandardResponse(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID, paginatedFilter, PriceSuppressStrategy.UI_PRICES);

        assertThat(result.pageNumber, is(1));
    }

    @Test
    public void shouldPaginateTheResult() throws Exception {
        final Pagination mockPagination = mock(Pagination.class);
        final PaginatedFilter paginatedFilter = new PaginatedPricingTabViewFilter(DataTableFilterValues.parse(new QueryParamStub()), mockPagination);
        final ArrayList<LineItemModel> list = new ArrayList<LineItemModel>();

        when(productIdentifierFacade.getAllSellableProducts()).thenReturn(new Products());
        when(lineItemFacade.fetchLineItems(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID, PriceSuppressStrategy.None)).thenReturn(list);

        pricingOrchestrator.buildStandardResponse(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID, paginatedFilter, PriceSuppressStrategy.UI_PRICES);

        verify(mockPagination).paginate(list);
    }

    @Test
    public void shouldReturnPricingFilterView() throws Exception {
        when(quoteOptionFacade.get(PROJECT_ID, QUOTE_OPTION_ID)).thenReturn(QuoteOptionDTO.newInstance("", "", "currency", "", ""));
        when(productIdentifierFacade.getAllSellableProducts()).thenReturn(new Products(newArrayList(product1, product2)));
        when(siteFacade.getCountries(CUSTOMER_ID, PROJECT_ID)).thenReturn(newArrayList("UK", "India", "France"));

        BidManagerCommentsDTO bidManagerCommentsDTO = new BidManagerCommentsDTO("aComment","aCaveat", DateTime.now(),"","");
        ArrayList<BidManagerCommentsDTO> comments = newArrayList(bidManagerCommentsDTO);
        when(bidManagerCommentsFacade.getBidManagerComments(PROJECT_ID,QUOTE_OPTION_ID)).thenReturn(comments);

        PricingTabView pricingFilterView = pricingOrchestrator.getPricingTabView(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID);

        assertThat(pricingFilterView.getProductNames().size(), is(2));
        assertTrue(pricingFilterView.getProductNames().contains("productName1"));
        assertTrue(pricingFilterView.getProductNames().contains("productName2"));
        assertThat(pricingFilterView.getCurrency(), is("currency"));
        assertThat(pricingFilterView.getExportPricingSheetLink(), is("/rsqe/customers/CUSTOMER_ID/contracts/CONTRACT_ID/projects/projectId/quote-options/quoteOptionId/pricing-sheet?offerId="));
        assertThat(pricingFilterView.getComments().get(0).getComments(),is("aComment"));

        assertThat(pricingFilterView.getCountries().size(), is(3));
        assertThat(pricingFilterView.getCountries(), hasItems("UK", "India", "France"));
        assertThat(pricingFilterView.getCostAttachmentUrl(), is("/rsqe/customers/CUSTOMER_ID/contracts/CONTRACT_ID/projects/projectId/quote-options/quoteOptionId/attachments/form?isCostAttachmentDialog=true"));
    }

    @Test
    public void shouldNotAllowRequestDiscountWhenActivityIdIsSet() throws Exception {
        final QuoteOptionDTO quoteOptionDTO = QuoteOptionDTO.newInstance("", "", "currency", "", "");
        quoteOptionDTO.activityId = "1234";
        when(quoteOptionFacade.get(PROJECT_ID, QUOTE_OPTION_ID)).thenReturn(quoteOptionDTO);
        when(lineItemFacade.fetchLineItems(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID, PriceSuppressStrategy.None)).thenReturn(Lists.<LineItemModel>newArrayList());
        when(productIdentifierFacade.getAllSellableProducts()).thenReturn(new Products(newArrayList(product1, product2)));
        when(siteFacade.getCountries(CUSTOMER_ID, PROJECT_ID)).thenReturn(newArrayList("UK", "India", "France"));

        PricingTabView pricingFilterView = pricingOrchestrator.getPricingTabView(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID);

        assertFalse(pricingFilterView.getAllowRequestDiscount());

    }

    @Test
    public void shouldNotAllowRequestDiscountWhenAllLineItemsAreReadOnly() throws Exception {
        when(siteFacade.getCountries(CUSTOMER_ID, PROJECT_ID)).thenReturn(newArrayList("UK", "India", "France"));
        when(productIdentifierFacade.getAllSellableProducts()).thenReturn(new Products(newArrayList(product1, product2)));
        final QuoteOptionDTO quoteOptionDTO = QuoteOptionDTO.newInstance("", "", "currency", "", "");
        LineItemModel lineItemModel1 = mock(LineItemModel.class);
        LineItemModel lineItemModel2 = mock(LineItemModel.class);
        when(quoteOptionFacade.get(PROJECT_ID, QUOTE_OPTION_ID)).thenReturn(quoteOptionDTO);
        when(lineItemFacade.fetchLineItems(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID, PriceSuppressStrategy.None)).thenReturn(Lists.<LineItemModel>newArrayList(lineItemModel1, lineItemModel2));

        when(lineItemModel1.getFutureAssetPricesModel()).thenReturn(mock(FutureAssetPricesModel.class));
        when(lineItemModel2.getFutureAssetPricesModel()).thenReturn(mock(FutureAssetPricesModel.class));
        when(lineItemModel1.isReadOnly()).thenReturn(true);
        when(lineItemModel2.isReadOnly()).thenReturn(true);
        when(lineItemModel1.getProductName()).thenReturn("NAME");
        when(lineItemModel2.getProductName()).thenReturn("NAME");
        when(lineItemModel1.getAction()).thenReturn("Modify");
        when(lineItemModel2.getAction()).thenReturn("Modify");
        PricingTabView pricingFilterView = pricingOrchestrator.getPricingTabView(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID);

        assertFalse(pricingFilterView.getAllowRequestDiscount());

    }

    @Test
    public void shouldAllowRequestDiscountWhenSomeLineItemsAreNotReadOnly() throws Exception {
        when(siteFacade.getCountries(CUSTOMER_ID, PROJECT_ID)).thenReturn(newArrayList("UK", "India", "France"));
        when(productIdentifierFacade.getAllSellableProducts()).thenReturn(new Products(newArrayList(product1, product2)));
        final QuoteOptionDTO quoteOptionDTO = QuoteOptionDTO.newInstance("", "", "currency", "", "");
        LineItemModel lineItemModel1 = mock(LineItemModel.class);
        LineItemModel lineItemModel2 = mock(LineItemModel.class);

        when(quoteOptionFacade.get(PROJECT_ID, QUOTE_OPTION_ID)).thenReturn(quoteOptionDTO);
        when(lineItemFacade.fetchLineItems(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID, PriceSuppressStrategy.None)).thenReturn(Lists.<LineItemModel>newArrayList(lineItemModel1, lineItemModel2));

        when(lineItemModel1.getFutureAssetPricesModel()).thenReturn(mock(FutureAssetPricesModel.class));
        when(lineItemModel2.getFutureAssetPricesModel()).thenReturn(mock(FutureAssetPricesModel.class));
        when(lineItemModel1.isReadOnly()).thenReturn(false);
        when(lineItemModel2.isReadOnly()).thenReturn(true);
        when(lineItemModel1.getProductName()).thenReturn("NAME");
        when(lineItemModel2.getProductName()).thenReturn("NAME");
        when(lineItemModel1.getAction()).thenReturn("Modify");
        when(lineItemModel2.getAction()).thenReturn("Modify");
        PricingTabView pricingFilterView = pricingOrchestrator.getPricingTabView(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID);

        assertTrue(pricingFilterView.getAllowRequestDiscount());
    }

    @Test
    public void shouldGetPaginatedUsageInformation() throws Exception {
        Map<String, String> params = newHashMap();
        params.put("field", "Tier_Description");
        params.put("Tier = 'Tier 1'", "1");
        params.put("Currency = 'USD'", "1");
        params.put("ruleset", "R0301661");
        when(lookupHandler.lookupRuleSet("Tier_Description", params)).thenReturn(TableResponseDTO.from("", Lists.<String>newArrayList("Tier 1 Description")));

        final Pagination mockPagination = mock(Pagination.class);
        final PaginatedFilter paginatedFilter = new PaginatedPricingTabViewFilter(DataTableFilterValues.parse(new QueryParamStub()), mockPagination);
        final ArrayList<LineItemModel> lineItems = newArrayList();

        FutureAssetPricesFacade futureAssetPricesFacade = mock(FutureAssetPricesFacade.class);

        final PriceLineDTOFixture.Builder usageCharge = PriceLineDTOFixture.aPriceLineDTO()
                                                                     .withId("aPriceLineId")
                                                                     .withDescription("Usage Charges")
                                                                     .with(PriceType.USAGE_BASED)
                                                                     .withPrice(PriceCategory.FIXED_CHARGE, 1.0, "Tier 1")
                                                                     .withPrice(PriceCategory.MIN_CHARGE, 2.0, "Tier 1")
                                                                     .withPrice(PriceCategory.CHARGE_RATE, 3.0, "Tier 1");

        when(productIdentifierFacade.getProductName(any(ProductOffering.class))).thenReturn("Product 1");
        when(productIdentifierFacade.getDisplayName("P1")).thenReturn("Product 1");
        when(productIdentifierFacade.getDisplayName("P2")).thenReturn("Product 2");

        doAnswer(new Answer<FutureAssetPricesModel>() {
            @Override
            public FutureAssetPricesModel answer(InvocationOnMock invocation) throws Throwable {
                AssetDTO asset = (AssetDTO)invocation.getArguments()[3];
                String lineItemId = asset.getLineItemId();
                PricingConfig config = new PricingConfig();

                if("L1".equals(lineItemId)) {
                    return FutureAssetPricesModelFixture.aFutureAssetPricesModel()
                                                     .with(productIdentifierFacade)
                                                     .with(FutureAssetPricesDTOFixture.aFutureAssetPricesDTO().withLineItemId("aLineItemId").withProductCode("P1").withPriceLine(usageCharge).withPricingModel("aPricingModel"))
                                                     .with(config)
                                                     .build();
                } else {
                    return FutureAssetPricesModelFixture.aFutureAssetPricesModel()
                                                     .with(productIdentifierFacade)
                                                     .with(FutureAssetPricesDTOFixture.aFutureAssetPricesDTO()
                                                     .withProductCode("P1"))
                                                     .with(config)
                                                     .build();
                }
            }
        }).when(futureAssetPricesFacade).get(eq(CUSTOMER_ID), eq(PROJECT_ID), eq(QUOTE_OPTION_ID), Matchers.<AssetDTO>any(), eq(PriceSuppressStrategy.None));

        LineItemModelFixture.Builder lineItemModelFixture = LineItemModelFixture.aLineItemModel()
                                                                                .with(futureAssetPricesFacade)
                                                                                .withProjectId(PROJECT_ID)
                                                                                .withQuoteOptionId(QUOTE_OPTION_ID)
                                                                                .withContractId(CONTRACT_ID)
                                                                                .withCustomerId(CUSTOMER_ID);
        ProductIdentifierFacade productIdentifierFacade1 = mock(ProductIdentifierFacade.class);
        lineItems.add(lineItemModelFixture.withQuoteOptionItemDTOId("L1").with(productIdentifierFacade).build());
        lineItems.add(lineItemModelFixture.withQuoteOptionItemDTOId("L2").with(productIdentifierFacade1).build());
        when(mockPagination.paginate(lineItems)).thenReturn(lineItems);

        when(productIdentifierFacade.getAllSellableProducts()).thenReturn(new Products());
        when(lineItemFacade.fetchLineItems(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID, PriceSuppressStrategy.UI_PRICES)).thenReturn(lineItems);
        when(productIdentifierFacade.getProductName("sCode")).thenReturn("pName");
        when(productIdentifierFacade1.getProductName(any(ProductOffering.class))).thenReturn("pName1");
        when(productIdentifierFacade1.getProductName("sCode")).thenReturn("pName1");

        QuoteOptionUsagePricingDTO usagePricingDTO = pricingOrchestrator.buildUsageResponse(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID, paginatedFilter, PriceSuppressStrategy.UI_PRICES);

        assertThat(usagePricingDTO.products.size(), is(1));
        final QuoteOptionUsagePricingDTO.UsageProduct product = usagePricingDTO.products.get(0);
        assertThat(product.productName, is("Product 1"));
        assertThat(product.lineItemId, is("aLineItemId"));
        assertThat(product.pricingModel, is("aPricingModel"));
        assertThat(product.summary, is("")); // Should set summary from null to empty string when no visibleInSummary attributes
        assertThat(product.priceLines.size(), is(1));

        final QuoteOptionUsagePricingDTO.UsagePriceLine priceline = product.priceLines.get(0);
        assertThat(priceline.description, is("Usage Charges"));
        assertThat(priceline.tiers.size(), is(1));

        final QuoteOptionUsagePricingDTO.UsageItemRowDTO usageItem = priceline.tiers.get(0);
        assertThat(usageItem.lineItemId, is("aLineItemId"));
        assertThat(usageItem.priceLineId, is("aPriceLineId"));
        assertThat(usageItem.tier, is("Tier 1"));
        assertThat(usageItem.tierDescription, is("Tier 1 Description"));
        assertThat(usageItem.product, is("Product 1"));
        assertThat(usageItem.pricingModel, is("aPricingModel"));
        assertThat(product.summary, is(""));
        assertThat(usageItem.fixedCharge.value, is("1.00"));
        assertThat(usageItem.minCharge.value, is("2.00"));
        assertThat(usageItem.chargeRate.value, is("3.00"));
    }

    @Test
    public void shouldRemoveAnyProductsFromUsageResultWhichHaveNoAssociatedPriceLines() throws Exception {
        final Pagination mockPagination = mock(Pagination.class);
        final PaginatedFilter paginatedFilter = new PaginatedPricingTabViewFilter(DataTableFilterValues.parse(new QueryParamStub()), mockPagination);
        final ArrayList<LineItemModel> lineItems = newArrayList();

        FutureAssetPricesFacade futureAssetPricesFacade = mock(FutureAssetPricesFacade.class);

        when(productIdentifierFacade.getProductName(any(ProductOffering.class))).thenReturn("aProductName");
        when(productIdentifierFacade.getDisplayName("P1")).thenReturn("Product 1");
        when(productIdentifierFacade.getDisplayName("P2")).thenReturn("Product 2");
        final FutureAssetPricesModel model = FutureAssetPricesModelFixture.aFutureAssetPricesModel().with(productIdentifierFacade).with(FutureAssetPricesDTOFixture.aFutureAssetPricesDTO().withProductCode("P1")).build();
        when(futureAssetPricesFacade.get(eq(CUSTOMER_ID), eq(PROJECT_ID), eq(QUOTE_OPTION_ID), Matchers.<AssetDTO>any(), eq(PriceSuppressStrategy.None))).thenReturn(model);

        LineItemModelFixture.Builder lineItemModelFixture = LineItemModelFixture.aLineItemModel()
                                                                                .with(futureAssetPricesFacade)
                                                                                .withProjectId(PROJECT_ID)
                                                                                .withQuoteOptionId(QUOTE_OPTION_ID)
                                                                                .withContractId(CONTRACT_ID)
                                                                                .withCustomerId(CUSTOMER_ID)
                                                                                .forSite(SiteDTOFixture.aSiteDTO().withBfgSiteId("1").build());
        ProductIdentifierFacade productIdentifierFacade1 = mock(ProductIdentifierFacade.class);
        when(productIdentifierFacade1.getProductName(any(ProductOffering.class))).thenReturn("aProductName");
        lineItems.add(lineItemModelFixture.withQuoteOptionItemDTOId("L1").with(productIdentifierFacade).build());
        lineItems.add(lineItemModelFixture.withQuoteOptionItemDTOId("L2").with(productIdentifierFacade1).build());
        when(mockPagination.paginate(lineItems)).thenReturn(lineItems);

        when(productIdentifierFacade.getAllSellableProducts()).thenReturn(new Products());
        when(lineItemFacade.fetchLineItems(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID, PriceSuppressStrategy.UI_PRICES)).thenReturn(lineItems);
        when(productIdentifierFacade.getProductName("sCode")).thenReturn("pName");
        when(productIdentifierFacade1.getProductName("sCode")).thenReturn("pName1");

        QuoteOptionUsagePricingDTO usagePricingDTO = pricingOrchestrator.buildUsageResponse(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID, paginatedFilter, PriceSuppressStrategy.UI_PRICES);
        assertThat(usagePricingDTO.products.size(), is(0));
    }

    @Test
    public void shouldSortUsageItemsAscendingByTier() throws Exception {
        when(lookupHandler.lookupRuleSet(eq("Tier_Description"), any(Map.class))).thenReturn(TableResponseDTO.from("", Lists.<String>newArrayList("Some Description")));

        final Pagination mockPagination = mock(Pagination.class);
        final PaginatedFilter paginatedFilter = new PaginatedPricingTabViewFilter(DataTableFilterValues.parse(new QueryParamStub()), mockPagination);
        final ArrayList<LineItemModel> lineItems = newArrayList();

        FutureAssetPricesFacade futureAssetPricesFacade = mock(FutureAssetPricesFacade.class);

        PriceLineDTOFixture.Builder usageCharges = PriceLineDTOFixture.aPriceLineDTO()
                                                                      .withDescription("Usage Charges 1")
                                                                      .with(PriceType.USAGE_BASED)
                                                                      .withPrice(PriceCategory.FIXED_CHARGE, 1.0, "Tier 4")
                                                                      .withPrice(PriceCategory.MIN_CHARGE, 1.0, "Tier 2")
                                                                      .withPrice(PriceCategory.CHARGE_RATE, 1.0, "Tier 1");

        when(productIdentifierFacade.getDisplayName(anyString())).thenReturn("Some Product Name");
        final FutureAssetPricesModel model = FutureAssetPricesModelFixture.aFutureAssetPricesModel()
                                                                          .with(productIdentifierFacade)
                                                                          .with(FutureAssetPricesDTOFixture.aFutureAssetPricesDTO()
                                                                                                           .withProductCode("P1")
                                                                                                           .withPriceLine(usageCharges)
                                                                                                           .withPricingModel("aPricingModel"))
                                                                          .build();
        when(futureAssetPricesFacade.get(eq(CUSTOMER_ID), eq(PROJECT_ID), eq(QUOTE_OPTION_ID), Matchers.<AssetDTO>any(), eq(PriceSuppressStrategy.None)))
            .thenReturn(model);

        lineItems.add(LineItemModelFixture.aLineItemModel()
                                          .with(futureAssetPricesFacade)
                                          .withProjectId(PROJECT_ID)
                                          .withQuoteOptionId(QUOTE_OPTION_ID)
                                          .withContractId(CONTRACT_ID)
                                          .withCustomerId(CUSTOMER_ID)
                                          .withQuoteOptionItemDTOId("L1").build());

        when(mockPagination.paginate(lineItems)).thenReturn(lineItems);
        when(productIdentifierFacade.getAllSellableProducts()).thenReturn(new Products());
        when(lineItemFacade.fetchLineItems(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID, PriceSuppressStrategy.UI_PRICES)).thenReturn(lineItems);

        QuoteOptionUsagePricingDTO usagePricingDTO = pricingOrchestrator.buildUsageResponse(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID, paginatedFilter, PriceSuppressStrategy.UI_PRICES);

        final List<QuoteOptionUsagePricingDTO.UsageItemRowDTO> tiers = usagePricingDTO.products.get(0).priceLines.get(0).tiers;
        assertThat(tiers.get(0).tier, is("Tier 1"));
        assertThat(tiers.get(1).tier, is("Tier 2"));
        assertThat(tiers.get(2).tier, is("Tier 4"));
    }

    @Test
    public void shouldIncludeListOfVendorDiscountsInView() throws Exception
    {
        // TODO
    }

    /**
     * Test that the generatePriceLineProductList method correctly returns a list of all distinct Line Item Model Display Names.
     */
    @Test
    public void generatePriceLineProductList()
    {
        LOGGER.info("Testing generatePriceLineProductList method returns expected results.");
        List<LineItemModel> models = asList(MODEL_1, MODEL_1, MODEL_2, MODEL_2, MODEL_2);

        // Check that the Display Names of all models are present in the returned list.
        Collection<String> result = QuoteOptionPricingOrchestrator.generatePriceLineProductList(models);
        assertTrue("Result should contain both Line Item Model Display Names.", result.contains(MODEL_1_DISPLAY_NAME));
        assertTrue("Result should contain both Line Item Model Display Names.", result.contains(MODEL_2_DISPLAY_NAME));

        // Check that the result contains only distinct values.
        assertTrue("generatePriceLineProductList method should return a distinct list.", isCollectionDistinct(result));

        // Check that null and empty lists return an empty list as a result.
        Collection<String> expected = Collections.emptyList();
        assertEquals("With a null input an empty list should be the output.", expected,
                QuoteOptionPricingOrchestrator.generatePriceLineProductList(null));
        assertEquals("With an empty list input the output should be an empty list.", expected,
                QuoteOptionPricingOrchestrator.generatePriceLineProductList(Collections.<LineItemModel>emptyList()));
    }

    /**
     * Test that the generatePriceLineCountriesList method correctly returns a distinct list of all Line Item Country names.
     */
    @Test
    public void generatePriceLineCountriesList()
    {
        LOGGER.info("Testing generatePriceLineCountriesList method returns expected results.");
        List<LineItemModel> models = asList(MODEL_1, MODEL_1, MODEL_2, MODEL_2, MODEL_2);

        // Check that the Country Names of all models are present in the returned list.
        Collection<String> result = QuoteOptionPricingOrchestrator.generatePriceLineCountriesList(models);
        assertTrue("Result should contain both Line Item Model Country Names.", result.contains(MODEL_1_COUNTRY));
        assertTrue("Result should contain both Line Item Model Country Names.", result.contains(MODEL_2_COUNTRY));

        // Check that the result contains only distinct values.
        assertTrue("generatePriceLineProductList method should return a distinct list.", isCollectionDistinct(result));

        // Check that null and empty lists return an empty list as a result.
        Collection<String> expected = Collections.emptyList();
        assertEquals("With a null input an empty list should be the output.", expected,
                QuoteOptionPricingOrchestrator.generatePriceLineCountriesList(null));
        assertEquals("With an empty list input the output should be an empty list.", expected,
                QuoteOptionPricingOrchestrator.generatePriceLineCountriesList(Collections.<LineItemModel>emptyList()));
    }

    /**
     * Test that the generateIsManualModify returns correctly with all expected inputs.
     */
    @Test
    public void generateIsManualModify()
    {
        LOGGER.info("Testing generatePriceLineCountriesList method returns expected results.");

        // Check that Line Items that do not pass the isConnectAccelerationModifyOrCease check return as an empty JSON object.
        List<LineItemModel> models1 = asList(MODEL_1, MODEL_1, MODEL_2, MODEL_2, MODEL_2);
        String result1 = QuoteOptionPricingOrchestrator.generateIsManualModify(models1);
        assertEquals("Result should be an empty JSON object as neither line item passes the condition isConnectAccelerationModifyOrCease().",
                EMPTY_JSON, result1);

        // Check that Line Items that pass the isConnectAccelerationModifyOrCease check are returned correctly as serialised JSON.
        List<LineItemModel> models2 = asList(MODEL_1, MODEL_1, MODEL_2, MODEL_2, MODEL_2, MODEL_3);
        Map<String, String> hash = new HashMap<>(1);
            hash.put(MODEL_3_LINE_ITEM_ID, MODEL_3_ACTION);
        String expected = QuoteOptionPricingOrchestrator.serializeString(hash);

        assertEquals("Line Item 3 should be serialised into a String containing it's ID and Action.", expected,
                QuoteOptionPricingOrchestrator.generateIsManualModify(models2));

        // Check that a null input returns a null output.
        assertEquals("With a null input we expect an empty JSON object to be returned.", EMPTY_JSON,
                QuoteOptionPricingOrchestrator.generateIsManualModify(null));
    }

    /**
     * Tests that the isRequestDiscountAllowed method returns correctly with all expected inputs.
     * I.e. That the Activity ID is null and that the Line Item has no discountable items.
     */
    @Test
    public void isRequestDiscountAllowed()
    {
        LOGGER.info("Testing generatePriceLineCountriesList method returns expected results.");

        List<LineItemModel> models1 = Collections.singletonList(MODEL_3);
        List<LineItemModel> models2 = asList(MODEL_1, MODEL_2);

        // Line Items that should allow discount requests.
        assertTrue("This Line Item should be discountable.", QuoteOptionPricingOrchestrator.isRequestDiscountAllowed(null, models1));

        // Line Items that should not allow discount requests.
        assertFalse("This Line Item should not be discountable.", QuoteOptionPricingOrchestrator.isRequestDiscountAllowed(null, models2));

        // Non Null Activity ID should return false.
        assertFalse("This Line Item has a non null activity ID and therefore return false.",
                QuoteOptionPricingOrchestrator.isRequestDiscountAllowed("", models1));
    }

    /**
     * Tests that the isConnectAccelerationModifyOrCease method returns correctly with all expected inputs.
     * I.e. True if the Line Items Product Name is Connect Acceleration and it's action is Modify or Cease. False otherwise.
     */
    @Test
    public void isConnectAccelerationModifyOrCease()
    {
        LOGGER.info("Testing isConnectAccelerationModifyOrCease method returns expected results.");

        // Line Item that matches both conditions. Should return true.
        assertTrue("Line Items that match both conditions should return true.",
                QuoteOptionPricingOrchestrator.isConnectAccelerationModifyOrCease(MODEL_3));

        // Line Items that match one condition. Should return false.
        assertFalse("Line Items that match only one condition should return false.",
                QuoteOptionPricingOrchestrator.isConnectAccelerationModifyOrCease(MODEL_2));

        // Line Items that match neither condition. Should return false.
        assertFalse("Line Items that match neither condition should return false.", QuoteOptionPricingOrchestrator.isConnectAccelerationModifyOrCease(MODEL_1));
    }

    /**
     * Tests that the isConnectAcceleration method returns correctly with all expected inputs.
     * I.e. True if the Line Items Product Name is 'Connect Acceleration Site' or 'Connect Acceleration service'. False otherwise.
     */
    @Test
    public void isConnectAcceleration()
    {
        LOGGER.info("Testing isConnectAcceleration method returns expected results.");

        // Line Item that matches the condition. Should return true.
        assertTrue("Line Items with the 'Connect Acceleration Site' Product Name should return true.",
                QuoteOptionPricingOrchestrator.isConnectAcceleration(MODEL_3));

        // Line Items that do not match the condition. Should return false.
        assertFalse("Line Items without the 'Connect Acceleration Site' or 'Connect Acceleration service' Product Name should return false.",
                QuoteOptionPricingOrchestrator.isConnectAcceleration(MODEL_2));
        assertFalse("Line Items without the 'Connect Acceleration Site' or 'Connect Acceleration service' Product Name should return false.",
                QuoteOptionPricingOrchestrator.isConnectAcceleration(MODEL_1));
    }

    /**
     * Tests that the isModifyOrCease method returns correctly with all expected inputs.
     * I.e. True if the Line Items Action is 'Modify' or 'Cease'. False otherwise.
     */
    @Test
    public void isModifyOrCease()
    {
        LOGGER.info("Testing isModifyOrCease method returns expected results.");

        // Line Item that matches the condition. Should return true.
        assertTrue("Line Items with the 'Modify' Action should return true.", QuoteOptionPricingOrchestrator.isModifyOrCease(MODEL_3));
        assertTrue("Line Items with the 'Cease' Action should return true.", QuoteOptionPricingOrchestrator.isModifyOrCease(MODEL_2));

        // Line Items that do not match the condition. Should return false.
        assertFalse("Line Items without the 'Cease' or 'Modify' Action should return false.", QuoteOptionPricingOrchestrator.isModifyOrCease(MODEL_1));
    }

    /**
     * Tests if the given collection contains only unique values.
     * Do this by copying the collection into a Set. This will result in only unique values being kept. Then compare sizes.
     * @param collection The collection to test.
     * @param <T> The collection can be of any type.
     * @return True if all elements of the collection are distinct. An empty collection is considered distinct. False otherwise.
     */
    public static <T> boolean isCollectionDistinct(Collection<T> collection)
    {
        Collection<T> set = new HashSet<>(collection);
        boolean result = collection.size() == set.size();
        LOGGER.trace("Retuning = {} for is Collection = {} distinct", result, collection);
        return result;
    }

    /**
     * Tests that the isCollectionDistinct method returns correctly with all expected inputs.
     */
    @Test
    public void isCollectionDistinctTest()
    {
        // Distinct Collection.
        Collection<String> collection1 = asList("1", "2", "3");
        assertTrue("This collection should be reported as distinct.", isCollectionDistinct(collection1));

        // Non-Distinct Collection.
        Collection<String> collection2 = asList("1", "2", "2");
        assertFalse("This collection should be reported as not distinct.", isCollectionDistinct(collection2));

        // Empty Collection.
        Collection<String> collection3 = Collections.emptyList();
        assertTrue("This collection should be reported as distinct.", isCollectionDistinct(collection3));
    }

}