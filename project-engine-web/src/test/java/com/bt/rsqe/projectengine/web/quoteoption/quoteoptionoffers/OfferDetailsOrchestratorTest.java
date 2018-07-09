package com.bt.rsqe.projectengine.web.quoteoption.quoteoptionoffers;


import com.bt.rsqe.customerinventory.client.ProductInstanceClient;
import com.bt.rsqe.customerinventory.dto.AssetDTO;
import com.bt.rsqe.customerinventory.dto.FutureAssetPricesDTO;
import com.bt.rsqe.customerinventory.fixtures.AssetDTOFixture;
import com.bt.rsqe.customerinventory.parameter.LineItemId;
import com.bt.rsqe.customerinventory.parameter.PriceLineStatus;
import com.bt.rsqe.customerrecord.CustomerDTO;
import com.bt.rsqe.customerrecord.CustomerResource;
import com.bt.rsqe.domain.QuoteOptionItemStatus;
import com.bt.rsqe.domain.product.chargingscheme.ProductChargingScheme;
import com.bt.rsqe.domain.product.extensions.ValidationErrorType;
import com.bt.rsqe.domain.product.fixtures.ProductChargingSchemeFixture;
import com.bt.rsqe.domain.project.PricingStatus;
import com.bt.rsqe.enums.PriceCategory;
import com.bt.rsqe.enums.PriceType;
import com.bt.rsqe.expedio.fixtures.SiteDTOFixture;
import com.bt.rsqe.pricing.config.dto.PricingConfig;
import com.bt.rsqe.projectengine.LineItemValidationDescriptionDTO;
import com.bt.rsqe.projectengine.LineItemValidationResultDTO;
import com.bt.rsqe.projectengine.OfferDTO;
import com.bt.rsqe.projectengine.QuoteOptionItemDTO;
import com.bt.rsqe.projectengine.web.facades.FutureAssetPricesFacade;
import com.bt.rsqe.projectengine.web.facades.ProductIdentifierFacade;
import com.bt.rsqe.projectengine.web.facades.QuoteOptionOfferFacade;
import com.bt.rsqe.projectengine.web.facades.SiteFacade;
import com.bt.rsqe.projectengine.web.fixtures.FutureAssetPricesDTOFixture;
import com.bt.rsqe.projectengine.web.fixtures.FutureAssetPricesModelFixture;
import com.bt.rsqe.projectengine.web.fixtures.LineItemModelFixture;
import com.bt.rsqe.projectengine.web.model.FutureAssetPricesModel;
import com.bt.rsqe.projectengine.web.model.LineItemModel;
import com.bt.rsqe.projectengine.web.model.OfferDetailsModel;
import com.bt.rsqe.projectengine.web.model.modelfactory.LineItemModelFactory;
import com.bt.rsqe.projectengine.web.quoteoptionoffers.OfferDetailsOrchestrator;
import com.bt.rsqe.projectengine.web.quoteoptionpricing.PriceSuppressStrategy;
import com.bt.rsqe.projectengine.web.view.OfferDetailsDTO;
import com.bt.rsqe.projectengine.web.view.OfferDetailsTabView;
import com.bt.rsqe.projectengine.web.view.pagination.NoPagination;
import com.bt.rsqe.security.UserContext;
import com.bt.rsqe.security.UserContextManager;
import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;

import java.util.List;

import static com.bt.rsqe.domain.QuoteOptionItemStatus.*;
import static com.bt.rsqe.enums.ProductCodes.*;
import static com.bt.rsqe.projectengine.LineItemDiscountStatus.*;
import static com.bt.rsqe.projectengine.LineItemValidationResultDTO.Status.*;
import static com.bt.rsqe.projectengine.QuoteOptionItemDTOFixture.Builder;
import static com.bt.rsqe.projectengine.QuoteOptionItemDTOFixture.*;
import static com.bt.rsqe.projectengine.web.fixtures.LineItemModelFixture.*;
import static com.bt.rsqe.projectengine.web.fixtures.PriceLineDTOFixture.*;
import static com.bt.rsqe.security.UserContextBuilder.*;
import static com.google.common.collect.Lists.*;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@SuppressWarnings("PMD.TooManyMethods")
public class OfferDetailsOrchestratorTest {

    public static final String TOKEN = "aToken";
    private OfferDetailsOrchestrator offerDetailsOrchestrator;
    private static final String ONEVOICE = "Onevoice";
    private static final String CUSTOMER_NAME = "Customer Name";
    private static final String CUSTOMER_ID = "customerId";
    private static final String CONTRACT_ID = "contractId";
    private static final String PROJECT_ID = "projectId";
    private static final String QUOTE_OPTION_ID = "quoteOptionId";
    private static final String OFFER_ID = "offerId";
    private static final String PRODUCT_ID = "productId";
    private static final String SITE_NAME = "SITE NAME";
    private static final String SITE_ID = "siteId";
    private static final String LINE_ITEM_ID = "lineItemId";

    private QuoteOptionOfferFacade quoteOptionOfferFacade;
    private CustomerResource customerResource;
    private NoPagination noPagination = new NoPagination();
    private FutureAssetPricesFacade productInstancePricesFacade;
    private ProductInstanceClient productInstanceClient;
    private PricingConfig pricingConfig = new PricingConfig();

    @Before
    public void before() {
        quoteOptionOfferFacade = mock(QuoteOptionOfferFacade.class);
        productInstancePricesFacade = mock(FutureAssetPricesFacade.class);
        customerResource = mock(CustomerResource.class);
        offerDetailsOrchestrator = new OfferDetailsOrchestrator(quoteOptionOfferFacade, customerResource);
        productInstanceClient = mock(ProductInstanceClient.class);
        when(productInstanceClient.getAssetDTO(Mockito.any(LineItemId.class))).thenReturn(AssetDTOFixture.anAsset().build());

        UserContext userContext = aDirectUserContext().withToken(TOKEN).build();
        UserContextManager.setCurrent(userContext);
    }

    @Test
    public void shouldBuildEmptyViewWhenNoItems() throws Exception {
        final OfferDetailsModel offerDetailsModel = mock(OfferDetailsModel.class);
        when(offerDetailsModel.getLineItems(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID)).thenReturn(Lists.<LineItemModel>newArrayList());
        when(quoteOptionOfferFacade.getOfferDetails(PROJECT_ID, QUOTE_OPTION_ID, OFFER_ID)).thenReturn(offerDetailsModel);

        final OfferDetailsDTO result = offerDetailsOrchestrator.buildJsonResponse(CUSTOMER_ID,
                                                                                  CONTRACT_ID,
                                                                                  PROJECT_ID,
                                                                                  QUOTE_OPTION_ID,
                                                                                  OFFER_ID,
                                                                                  noPagination);
        verify(offerDetailsModel).getLineItems(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID);
        verify(quoteOptionOfferFacade).getOfferDetails(PROJECT_ID, QUOTE_OPTION_ID, OFFER_ID);

        assertThat(result.itemDTOs.size(), is(0));
        assertThat(result.totalDisplayRecords, is(0));
        assertThat(result.totalRecords, is(0));
    }

    @Test
    public void shouldReturnDTOWithOneOfferItem() throws Exception {
        Builder quoteOptionDtoBuilder = getQuoteOptionBuilder(LINE_ITEM_ID);

        QuoteOptionItemDTO quoteOptionItemDTO = quoteOptionDtoBuilder.build();

        OfferDTO offerDTO = new OfferDTO();
        offerDTO.name = "An offer";
        offerDTO.created = "2011-01-01T12:30:55.000+00:00";
        offerDTO.offerItems = newArrayList(quoteOptionItemDTO);

        LineItemModelFactory lineItemModelFactory = mock(LineItemModelFactory.class);

        final OfferDetailsModel offerDetailsModel = new OfferDetailsModel(lineItemModelFactory, offerDTO);
        final ProductIdentifierFacade productIdentifierFacade = mock(ProductIdentifierFacade.class);
        final SiteFacade siteFacade = mock(SiteFacade.class);


        final LineItemModel lineItemModel = LineItemModelFixture.aLineItemModel()
                                                                .with(productInstanceClient)
                                                                .withCustomerId(CUSTOMER_ID)
                                                                .with(quoteOptionDtoBuilder)
                                                                .with(productInstancePricesFacade)
                                                                .isVisibleOnOfferDetailsPage(true)
                                                                .withPriceSuppressStrategy(PriceSuppressStrategy.OFFERS_UI)
                                                                .build();

        final FutureAssetPricesDTO futureAssetPricesDTO = getFutureAssetPricesDTO(LINE_ITEM_ID);

        final FutureAssetPricesModel futureAssetPricesModel = getFutureAssetPriceModel(productIdentifierFacade, siteFacade, futureAssetPricesDTO);

        when(quoteOptionOfferFacade.getOfferDetails(PROJECT_ID, QUOTE_OPTION_ID, OFFER_ID)).thenReturn(offerDetailsModel);
        when(lineItemModelFactory.create(PROJECT_ID, QUOTE_OPTION_ID, CUSTOMER_ID, CONTRACT_ID, quoteOptionItemDTO, PriceSuppressStrategy.OFFERS_UI,null)).thenReturn(lineItemModel);
        when(productInstancePricesFacade.get(anyString(), eq(PROJECT_ID), anyString(), Matchers.<AssetDTO>any(), eq(PriceSuppressStrategy.OFFERS_UI))).thenReturn(futureAssetPricesModel);
        List<ProductChargingScheme> schemes = newArrayList();
        schemes.add(ProductChargingSchemeFixture.aChargingScheme().withName("Scheme1").withSetAggregated("A").build());
        schemes.add(ProductChargingSchemeFixture.aChargingScheme().withName("Scheme2").withAggregationSet("A").build());
        schemes.add(ProductChargingSchemeFixture.aChargingScheme().withName("Scheme3").withAggregationSet("A").build());
        when(productIdentifierFacade.getChargingSchemes(anyString(), anyString())).thenReturn(schemes);
        when(productIdentifierFacade.getDisplayName(Onevoice.productCode())).thenReturn(ONEVOICE);
        when(siteFacade.get(anyString(), eq(PROJECT_ID), anyString())).thenReturn(SiteDTOFixture.aSiteDTO().withBfgSiteId(SITE_ID).withName(SITE_NAME).build());
        final OfferDetailsDTO result = offerDetailsOrchestrator.buildJsonResponse(CUSTOMER_ID,
                                                                                  CONTRACT_ID, PROJECT_ID,
                                                                                  QUOTE_OPTION_ID,
                                                                                  OFFER_ID,
                                                                                  noPagination);
        assertThat(result.itemDTOs.size(), is(1));
        assertThat(result.totalDisplayRecords, is(1));
        assertThat(result.totalRecords, is(1));
        assertThat(result.name, is("An offer"));
        assertThat(result.created, is("2011-01-01T12:30:55.000+00:00"));

        OfferDetailsDTO.ItemRowDTO offerItem = result.itemDTOs.get(0);
        assertThat(offerItem.site, is(SITE_NAME));
        assertThat(offerItem.product, is(ONEVOICE));
        assertThat(offerItem.id, is(LINE_ITEM_ID));
        assertThat(offerItem.status, is(QuoteOptionItemStatus.INITIALIZING.getDescription()));
        assertThat(offerItem.discountStatus, is("Approved"));
        assertThat(offerItem.pricingStatus, is(PricingStatus.NOT_APPLICABLE.getDescription()));
        assertThat(offerItem.errorMessage, is("erroMessage"));
        assertThat(offerItem.validity, is(INVALID.name()));
        assertThat(offerItem.forIfc, is(false));

        verify(quoteOptionOfferFacade).getOfferDetails(PROJECT_ID, QUOTE_OPTION_ID, OFFER_ID);
        verify(lineItemModelFactory).create(PROJECT_ID, QUOTE_OPTION_ID, CUSTOMER_ID, CONTRACT_ID, quoteOptionItemDTO, PriceSuppressStrategy.OFFERS_UI,null);
        verify(productIdentifierFacade).getDisplayName(Onevoice.productCode());

    }

    @Test
    public void shouldReturnACustomerName() throws Exception {
        when(customerResource.getByToken(CUSTOMER_ID, TOKEN)).thenReturn(new CustomerDTO(CUSTOMER_ID, CUSTOMER_NAME, "Sales Channel"));

        assertThat(offerDetailsOrchestrator.getCustomerName(CUSTOMER_ID), is(CUSTOMER_NAME));
        verify(customerResource).getByToken(CUSTOMER_ID, TOKEN);
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowExceptionIfNoPricesModelExistsForAOfferItem() throws Exception {
        final LineItemModel lineItemModel = aLineItemModel().with(aQuoteOptionItemDTO()).with(productInstanceClient).isVisibleOnOfferDetailsPage(true).build();
        final OfferDetailsModel offerDetailsModel = mock(OfferDetailsModel.class);

        when(productInstancePricesFacade.getForLineItems(anyString(), eq(PROJECT_ID), anyString(), anyList())).thenReturn(Lists.<FutureAssetPricesModel>newArrayList());
        when(quoteOptionOfferFacade.getOfferDetails(PROJECT_ID, QUOTE_OPTION_ID, OFFER_ID)).thenReturn(offerDetailsModel);
        when(offerDetailsModel.getLineItems(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID)).thenReturn(newArrayList(lineItemModel));

        offerDetailsOrchestrator.buildJsonResponse(CUSTOMER_ID,
                                                   CONTRACT_ID,
                                                   PROJECT_ID,
                                                   QUOTE_OPTION_ID,
                                                   OFFER_ID,
                                                   noPagination);

        verify(productInstancePricesFacade).getForLineItems(anyString(), eq(PROJECT_ID), anyString(), anyList());
        verify(quoteOptionOfferFacade).getOfferDetails(PROJECT_ID, QUOTE_OPTION_ID, OFFER_ID);
        verify(offerDetailsModel).getLineItems(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID);

    }

    @Test
    public void buildDetailsTabViewShouldMapOfferStatus() throws Exception {
        final OfferDetailsModel offerDetailsModel = mock(OfferDetailsModel.class);

        when(quoteOptionOfferFacade.getOfferDetails(PROJECT_ID, QUOTE_OPTION_ID, OFFER_ID)).thenReturn(offerDetailsModel);
        when(offerDetailsModel.isCustomerApprovable()).thenReturn(true);
        when(offerDetailsModel.getId()).thenReturn(OFFER_ID);
        when(offerDetailsModel.isApproved()).thenReturn(false);
        when(offerDetailsModel.isActive()).thenReturn(true);
        when(offerDetailsModel.getCreatedDate()).thenReturn("2009-01-11");
        when(customerResource.getByToken(CUSTOMER_ID, TOKEN)).thenReturn(new CustomerDTO(CUSTOMER_ID, CUSTOMER_NAME, "sales channel"));

        final OfferDetailsTabView result = offerDetailsOrchestrator.buildDetailsTabView(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID, OFFER_ID);

        verify(quoteOptionOfferFacade).getOfferDetails(PROJECT_ID, QUOTE_OPTION_ID, OFFER_ID);
        verify(customerResource).getByToken(CUSTOMER_ID, TOKEN);

        assertThat(result, is(notNullValue()));
        assertThat(result.getIsApprovable(), is(true));
        assertThat(result.getShowApproveOffer(), is(String.valueOf(true)));
        assertThat(result.getShowRejectOffer(), is(String.valueOf(true)));
        assertThat(result.getShowCreateOrder(), is(String.valueOf(false)));
        assertThat(result.getIsRejectable(), is(true));
        assertThat(result.getCreated(), is("11/01/2009 00:00"));
        assertThat(result.getApproveAction(),
                   is("/rsqe/customers/customerId/contracts/contractId/projects/projectId/quote-options/quoteOptionId/offers/offerId/approve"));
        assertThat(result.getRejectAction(),
                   is("/rsqe/customers/customerId/contracts/contractId/projects/projectId/quote-options/quoteOptionId/offers/offerId/reject"));
        assertThat(result.getCreateOrderAction(), is(nullValue()));
        assertThat(result.getCustomerId(), is(CUSTOMER_ID));
        assertThat(result.getProjectId(), is(PROJECT_ID));
        assertThat(result.getQuoteOptionId(), is(QUOTE_OPTION_ID));
        assertThat(result.getOfferId(), is(OFFER_ID));
    }

    @Test
    public void buildDetailsTabViewShouldMapCustomerName() throws Exception {
        final OfferDetailsModel offerDetailsModel = mock(OfferDetailsModel.class);

        when(quoteOptionOfferFacade.getOfferDetails(anyString(), anyString(), anyString())).thenReturn(offerDetailsModel);
        when(customerResource.getByToken(CUSTOMER_ID, TOKEN)).thenReturn(new CustomerDTO(null, CUSTOMER_NAME, null));
        when(offerDetailsModel.getId()).thenReturn("id");
        when(offerDetailsModel.getCreatedDate()).thenReturn("2009-01-11");

        final OfferDetailsTabView result = offerDetailsOrchestrator.buildDetailsTabView(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID, OFFER_ID);

        assertThat(result.getCustomerName(), is(CUSTOMER_NAME));
    }

    @Test
    @Ignore("[13/01/2012: David + Raul] Pending customer offer approve constraints")
    public void buildDetailsTabViewShouldGetApprovableForAnOffer() throws Exception {

        final OfferDetailsModel offerDetailsModel = mock(OfferDetailsModel.class);

        when(quoteOptionOfferFacade.getOfferDetails(PROJECT_ID, QUOTE_OPTION_ID, OFFER_ID)).thenReturn(offerDetailsModel);
        when(customerResource.getByToken(CUSTOMER_ID, TOKEN)).thenReturn(new CustomerDTO(null, "customer name", null));
        when(offerDetailsModel.getCreatedDate()).thenReturn("2009-01-11");

        final OfferDetailsTabView result = offerDetailsOrchestrator.buildDetailsTabView(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID, OFFER_ID);
        assertThat(result.getIsApprovable(), is(true));
    }

    @Test
    public void shouldNotReturnCumulativePricesWhenMoreThanOneOfferItemPresent() throws Exception {
        Builder quoteOptionDtoBuilder = getQuoteOptionBuilder(LINE_ITEM_ID);

        Builder quoteOptionDtoBuilder_2 = getQuoteOptionBuilder(LINE_ITEM_ID + "2");
        Builder quoteOptionDtoBuilder_3 = getQuoteOptionBuilder(LINE_ITEM_ID + "3");

        QuoteOptionItemDTO quoteOptionItemDTO_1 = quoteOptionDtoBuilder.build();
        QuoteOptionItemDTO quoteOptionItemDTO_2 = quoteOptionDtoBuilder_2.build();
        QuoteOptionItemDTO quoteOptionItemDTO_3 = quoteOptionDtoBuilder_3.build();

        OfferDTO offerDTO = new OfferDTO();
        offerDTO.name = "An offer";
        offerDTO.created = "2011-01-01T12:30:55.000+00:00";
        offerDTO.offerItems = newArrayList(quoteOptionItemDTO_1,quoteOptionItemDTO_2,quoteOptionItemDTO_3);

        LineItemModelFactory lineItemModelFactory = mock(LineItemModelFactory.class);

        final OfferDetailsModel offerDetailsModel = new OfferDetailsModel(lineItemModelFactory, offerDTO);
        final ProductIdentifierFacade productIdentifierFacade = mock(ProductIdentifierFacade.class);
        final SiteFacade siteFacade = mock(SiteFacade.class);


        final LineItemModel lineItemModel = LineItemModelFixture.aLineItemModel()
                                                                .with(productInstanceClient)
                                                                .withCustomerId(CUSTOMER_ID)
                                                                .with(quoteOptionDtoBuilder)
                                                                .isVisibleOnOfferDetailsPage(true)
                                                                .with(productInstancePricesFacade)
                                                                .withPriceSuppressStrategy(PriceSuppressStrategy.OFFERS_UI)
                                                                .build();

        final LineItemModel lineItemModel_2 = LineItemModelFixture.aLineItemModel()
                                                                  .with(productInstanceClient)
                                                                .withCustomerId(CUSTOMER_ID)
                                                                .isVisibleOnOfferDetailsPage(true)
                                                                .with(quoteOptionDtoBuilder_2)
                                                                .with(productInstancePricesFacade)
                                                                .withPriceSuppressStrategy(PriceSuppressStrategy.OFFERS_UI)
                                                                .build();

        final LineItemModel lineItemModel_3 = LineItemModelFixture.aLineItemModel()
                                                                  .with(productInstanceClient)
                                                                  .withCustomerId(CUSTOMER_ID)
                                                                  .isVisibleOnOfferDetailsPage(false)
                                                                  .with(quoteOptionDtoBuilder_2)
                                                                  .with(productInstancePricesFacade)
                                                                  .withPriceSuppressStrategy(PriceSuppressStrategy.OFFERS_UI)
                                                                  .build();

        final FutureAssetPricesDTO futureAssetPricesDTO = getFutureAssetPricesDTO(LINE_ITEM_ID);

        final FutureAssetPricesDTO futureAssetPricesDTO_2 = getFutureAssetPricesDTO(LINE_ITEM_ID + "2");

        final FutureAssetPricesModel futureAssetPricesModel = getFutureAssetPriceModel(productIdentifierFacade, siteFacade, futureAssetPricesDTO);

        final FutureAssetPricesModel futureAssetPricesModel_2 = getFutureAssetPriceModel(productIdentifierFacade, siteFacade, futureAssetPricesDTO_2);

        when(quoteOptionOfferFacade.getOfferDetails(PROJECT_ID, QUOTE_OPTION_ID, OFFER_ID)).thenReturn(offerDetailsModel);
        when(lineItemModelFactory.create(PROJECT_ID, QUOTE_OPTION_ID, CUSTOMER_ID, CONTRACT_ID, quoteOptionItemDTO_1, PriceSuppressStrategy.OFFERS_UI,null)).thenReturn(lineItemModel);
        when(lineItemModelFactory.create(PROJECT_ID, QUOTE_OPTION_ID, CUSTOMER_ID, CONTRACT_ID, quoteOptionItemDTO_2, PriceSuppressStrategy.OFFERS_UI,null)).thenReturn(lineItemModel_2);
        when(lineItemModelFactory.create(PROJECT_ID, QUOTE_OPTION_ID, CUSTOMER_ID, CONTRACT_ID, quoteOptionItemDTO_3, PriceSuppressStrategy.OFFERS_UI,null)).thenReturn(lineItemModel_3);
        when(productInstancePricesFacade.get(anyString(), eq(PROJECT_ID), anyString(), Matchers.<AssetDTO>any(), eq(PriceSuppressStrategy.OFFERS_UI))).thenReturn(futureAssetPricesModel);
        when(productInstancePricesFacade.get(anyString(), eq(PROJECT_ID), anyString(), Matchers.<AssetDTO>any(), eq(PriceSuppressStrategy.OFFERS_UI))).thenReturn(futureAssetPricesModel_2);
        when(productIdentifierFacade.getProductName(Onevoice.productCode())).thenReturn(ONEVOICE);
        when(siteFacade.get(anyString(), eq(PROJECT_ID), anyString())).thenReturn(SiteDTOFixture.aSiteDTO().withBfgSiteId(SITE_ID).withName(SITE_NAME).build());
         List<ProductChargingScheme> schemes = newArrayList();
        schemes.add(ProductChargingSchemeFixture.aChargingScheme().withName("Scheme1").withSetAggregated("A").build());
        schemes.add(ProductChargingSchemeFixture.aChargingScheme().withName("Scheme2").withAggregationSet("A").build());
        schemes.add(ProductChargingSchemeFixture.aChargingScheme().withName("Scheme3").withAggregationSet("A").build());
        when(productIdentifierFacade.getChargingSchemes(anyString(), anyString())).thenReturn(schemes);
        final OfferDetailsDTO result = offerDetailsOrchestrator.buildJsonResponse(CUSTOMER_ID,
                                                                                  CONTRACT_ID,
                                                                                  PROJECT_ID,
                                                                                  QUOTE_OPTION_ID,
                                                                                  OFFER_ID,
                                                                                  noPagination);
        assertThat(result.itemDTOs.size(), is(2));
        assertThat(result.totalDisplayRecords, is(2));
        assertThat(result.totalRecords, is(2));
        assertThat(result.name, is("An offer"));
        assertThat(result.created, is("2011-01-01T12:30:55.000+00:00"));

        OfferDetailsDTO.ItemRowDTO offerItem = result.itemDTOs.get(0);
        assertThat(offerItem.id, is(LINE_ITEM_ID));

        OfferDetailsDTO.ItemRowDTO offerItem2 = result.itemDTOs.get(1);
        assertThat(offerItem2.id, is(LINE_ITEM_ID + "2"));
    }

    private FutureAssetPricesModel getFutureAssetPriceModel(ProductIdentifierFacade productIdentifierFacade, SiteFacade siteFacade, FutureAssetPricesDTO futureAssetPricesDTO) {
        return FutureAssetPricesModelFixture.aFutureAssetPricesModel()
                                                                                                   .with(futureAssetPricesDTO)
                                                                                                   .with(productIdentifierFacade)
                                                                                                   .with(siteFacade)
                                                                                                   .with(pricingConfig).build();
    }

    private FutureAssetPricesDTO getFutureAssetPricesDTO(String lineItemID) {
        return FutureAssetPricesDTOFixture.aFutureAssetPricesDTO()
                                                                                     .withLineItemId(lineItemID)
                                                                                     .withProductCode(Onevoice.productCode())
                                                                                     .withPriceLine(aPriceLineDTO().withChargingScheme("Scheme1").withPrice(PriceCategory.CHARGE_PRICE, 12.34).withDiscount(PriceCategory.CHARGE_PRICE, 0).with(PriceType.RECURRING).with(PriceLineStatus.BUDGETARY))
                                                                                     .withPriceLine(aPriceLineDTO().withChargingScheme("Scheme1").withPrice(PriceCategory.CHARGE_PRICE, 16).withDiscount(PriceCategory.CHARGE_PRICE, 50).with(PriceType.RECURRING).with(PriceLineStatus.BUDGETARY))
                                                                                     .withPriceLine(aPriceLineDTO().withChargingScheme("Scheme1").withPrice(PriceCategory.CHARGE_PRICE, 100).withDiscount(PriceCategory.CHARGE_PRICE, 10).with(PriceType.RECURRING).with(PriceLineStatus.BUDGETARY))
                                                                                     .withPriceLine(aPriceLineDTO().withChargingScheme("Scheme1").withPrice(PriceCategory.CHARGE_PRICE, 100).with(PriceType.ONE_TIME).with(PriceLineStatus.BUDGETARY))
                                                                                     .build();
    }

    private Builder getQuoteOptionBuilder(String lineItemId) {
        return aQuoteOptionItemDTO()
            .withId(lineItemId)
            .withStatus(INITIALIZING)
            .withDiscountStatus(APPROVED)
            .withValidity(new LineItemValidationResultDTO(INVALID, newArrayList(new LineItemValidationDescriptionDTO("erroMessage", "cat", ValidationErrorType.Error.toString()))));
    }

}
