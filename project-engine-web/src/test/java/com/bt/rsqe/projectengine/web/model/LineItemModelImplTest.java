package com.bt.rsqe.projectengine.web.model;

import com.bt.rsqe.customerinventory.client.ProductInstanceClient;
import com.bt.rsqe.customerinventory.dto.AssetCharacteristicDTO;
import com.bt.rsqe.customerinventory.dto.AssetDTO;
import com.bt.rsqe.customerinventory.dto.FutureAssetRelationshipDTO;
import com.bt.rsqe.customerinventory.fixtures.AssetCharacteristicDTOFixture;
import com.bt.rsqe.customerinventory.fixtures.AssetDTOFixture;
import com.bt.rsqe.customerinventory.fixtures.PriceLineDTOFixture;
import com.bt.rsqe.customerinventory.parameter.CharacteristicName;
import com.bt.rsqe.customerinventory.parameter.CharacteristicValue;
import com.bt.rsqe.customerinventory.parameter.LineItemId;
import com.bt.rsqe.customerinventory.parameter.PriceLineStatus;
import com.bt.rsqe.customerinventory.utils.Constants;
import com.bt.rsqe.customerrecord.SiteDTO;
import com.bt.rsqe.domain.QuoteOptionItemStatus;
import com.bt.rsqe.domain.StencilCode;
import com.bt.rsqe.domain.StencilId;
import com.bt.rsqe.domain.StencilVersion;
import com.bt.rsqe.domain.bom.fixtures.AttributeFixture;
import com.bt.rsqe.domain.bom.fixtures.ProductOfferingFixture;
import com.bt.rsqe.domain.bom.parameters.ProductName;
import com.bt.rsqe.domain.product.AssetProcessType;
import com.bt.rsqe.domain.product.ProductOffering;
import com.bt.rsqe.domain.product.extensions.ValidationErrorType;
import com.bt.rsqe.domain.product.parameters.ProductIdentifier;
import com.bt.rsqe.domain.product.parameters.RelationshipName;
import com.bt.rsqe.domain.product.parameters.RelationshipType;
import com.bt.rsqe.domain.project.LineItemAction;
import com.bt.rsqe.domain.project.PricingStatus;
import com.bt.rsqe.expedio.project.ExpedioProjectResource;
import com.bt.rsqe.expedio.project.ProjectDTO;
import com.bt.rsqe.pmr.client.PmrClient;
import com.bt.rsqe.pmr.client.PmrMocker;
import com.bt.rsqe.projectengine.LineItemDiscountStatus;
import com.bt.rsqe.projectengine.LineItemOrderStatus;
import com.bt.rsqe.projectengine.LineItemValidationDescriptionDTO;
import com.bt.rsqe.projectengine.LineItemValidationResultDTO;
import com.bt.rsqe.projectengine.QuoteOptionItemDTOFixture;
import com.bt.rsqe.projectengine.web.facades.FutureAssetPricesFacade;
import com.bt.rsqe.projectengine.web.facades.ProductIdentifierFacade;
import com.bt.rsqe.projectengine.web.quoteoptionpricing.PriceSuppressStrategy;
import com.bt.rsqe.projectengine.web.uri.UriFactory;
import com.bt.rsqe.utils.RSQEMockery;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Before;
import org.junit.Test;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.bt.rsqe.domain.QuoteOptionItemStatus.*;
import static com.bt.rsqe.expedio.fixtures.ProjectDTOFixture.*;
import static com.bt.rsqe.projectengine.LineItemDiscountStatus.*;
import static com.bt.rsqe.projectengine.QuoteOptionItemDTOFixture.*;
import static com.bt.rsqe.projectengine.web.fixtures.LineItemModelFixture.*;
import static com.google.common.collect.Lists.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@SuppressWarnings("PMD.TooManyMethods")
public class LineItemModelImplTest {

    private static final String CUSTOMER_ID = "customerId";
    private static final String CONTRACT_ID = "contractId";
    private static final String PROJECT_ID = "projectId";
    private static final String LINE_ITEM_ID = "lineItemId";
    private static final String QUOTE_OPTION_ID = "quoteOptionId";
    private static final String S_CODE = "sCode";
    private static final String A_HAPPY_URL = "a happy url";
    public static final String READ_ONLY = "readOnly";
    public static final String STATUS = "status";
    private static final String REVENUE_OWNER = "revenueOwner";
    private static final String LINE_ITEM_ACTION = "CEASE";

    private JUnit4Mockery context;
    private LineItemModel lineItemModel;
    private FutureAssetPricesFacade productInstancePricesFacade;
    private ProductIdentifierFacade productIdentifierFacade;
    private UriFactory productConfiguratorUriFactory;
    private ExpedioProjectResource projectResource;
    private ProductInstanceClient productInstanceClient;
    private PmrClient pmr;

    @Before
    public void before() {
        context = new RSQEMockery();
        productInstancePricesFacade = context.mock(FutureAssetPricesFacade.class);
        productIdentifierFacade = context.mock(ProductIdentifierFacade.class);
        productConfiguratorUriFactory = context.mock(UriFactory.class);
        projectResource = context.mock(ExpedioProjectResource.class);
        productInstanceClient = context.mock(ProductInstanceClient.class);
        pmr = PmrMocker.getMockedInstance(true);
    }

    @Test
    public void shouldGetOfferNameGivenThereIsAnOffer() throws Exception {
        lineItemModel = aLineItemModel()
            .with(aQuoteOptionItemDTO().withOfferName("offerName"))
            .build();

        assertThat(lineItemModel.getOfferName(), is("offerName"));
    }

    @Test
    public void shouldReturnEmptyOfferNameGivenThereIsNoOffer() throws Exception {
        lineItemModel = aLineItemModel().build();
        assertThat(lineItemModel.getOfferName(), is(""));
    }

    @Test
    public void shouldReturnSiteFromFutureAsset() throws Exception {

        lineItemModel = aLineItemModel().with(productInstancePricesFacade).withCustomerId(CUSTOMER_ID)
            .with(aQuoteOptionItemDTO().withId(LINE_ITEM_ID)).with(productInstanceClient).build();
        final FutureAssetPricesModel futureAssetPricesModel = context.mock(FutureAssetPricesModel.class);
        final SiteDTO siteDTO = new SiteDTO();
        final AssetDTO asset = AssetDTOFixture.anAsset().build();

        context.checking(new Expectations() {{
            oneOf(productInstanceClient).getAssetDTO(new LineItemId(LINE_ITEM_ID));
            will(returnValue(asset));
            oneOf(productInstancePricesFacade).get(CUSTOMER_ID, PROJECT_ID, QUOTE_OPTION_ID, asset, PriceSuppressStrategy.None);
            will(returnValue(futureAssetPricesModel));
            allowing(futureAssetPricesModel).getSite();
            will(returnValue(siteDTO));
        }});

        assertThat(lineItemModel.getSite(), is(siteDTO));
        assertThat(lineItemModel.hasSite(), is(true));

    }

    @Test
    public void shouldReturnFalseIfFutureAssetHasNoSite() throws Exception {
        lineItemModel = aLineItemModel().with(productInstancePricesFacade).with(productInstanceClient).withCustomerId(CUSTOMER_ID)
            .with(aQuoteOptionItemDTO().withId(LINE_ITEM_ID)).build();
        final FutureAssetPricesModel futureAssetPricesModel = context.mock(FutureAssetPricesModel.class);
        final AssetDTO asset = AssetDTOFixture.anAsset().build();

        context.checking(new Expectations() {{
            oneOf(productInstanceClient).getAssetDTO(new LineItemId(LINE_ITEM_ID));
            will(returnValue(asset));
            oneOf(productInstancePricesFacade).get(CUSTOMER_ID, PROJECT_ID, QUOTE_OPTION_ID, asset, PriceSuppressStrategy.None);
            will(returnValue(futureAssetPricesModel));
            oneOf(futureAssetPricesModel).getSite();
            will(returnValue(null));
        }});

        assertThat(lineItemModel.hasSite(), is(false));
    }

    @Test
    public void shouldOnlyFetchFutureAssetOnce() throws Exception {
        lineItemModel = aLineItemModel().with(productInstancePricesFacade).with(productInstanceClient).withCustomerId(CUSTOMER_ID)
            .with(aQuoteOptionItemDTO().withId(LINE_ITEM_ID)).build();
        final FutureAssetPricesModel futureAssetPricesModel = context.mock(FutureAssetPricesModel.class);
        final AssetDTO asset = AssetDTOFixture.anAsset().build();

        context.checking(new Expectations() {{
            oneOf(productInstanceClient).getAssetDTO(new LineItemId(LINE_ITEM_ID));
            will(returnValue(asset));
            oneOf(productInstancePricesFacade).get(CUSTOMER_ID, PROJECT_ID, QUOTE_OPTION_ID, asset, PriceSuppressStrategy.None);
            will(returnValue(futureAssetPricesModel));
            ignoring(futureAssetPricesModel);
        }});

        lineItemModel.hasSite();
        lineItemModel.getSite();
    }

    @Test
    public void shouldReturnInfoFromDTO() throws Exception {
        PmrClient pmr = PmrMocker.getMockedInstance(true,
                                                    ProductOfferingFixture.aProductOffering()
                                                                          .withAttribute(AttributeFixture.aVisibleInSummaryAttribute().called("visibleWithValue").build())
                                                                          .withAttribute(AttributeFixture.aVisibleInSummaryAttribute().called("visibleNoValue").build())
                                                                          .withAttribute(AttributeFixture.anInvisibleRfqAttribute().build())
                                                                          .withAttribute(AttributeFixture.aVisibleInSummaryAttribute().called(ProductOffering.STENCIL_RESERVED_NAME).build())
                                                                          .withStencil(StencilId.latestVersionFor(StencilCode.newInstance("123"),
                                                                                                                  ProductName.newInstance("stencilName")))
                                                                          .build());

        ProductInstanceClient productInstanceClient = mock(ProductInstanceClient.class);
        final String billingId = "1234467";
        lineItemModel = aLineItemModel().with(
            aQuoteOptionItemDTO().withSCode(S_CODE).withId("id").withContractTerm("contractTerm").withAction(
                LineItemAction.PROVIDE.getDescription()).withStatus(DRAFT)
                .withDiscountStatus(NOT_APPLICABLE).withValidity(LineItemValidationResultDTO.Status.INVALID).withBillingId(billingId)
        ).with(productInstanceClient).withPmr(pmr).build();
        AssetDTO asset = mock(AssetDTO.class);
        when(asset.hasCharacteristic(new CharacteristicName("visibleWithValue"))).thenReturn(true);
        when(asset.getCharacteristic("visibleWithValue")).thenReturn(new AssetCharacteristicDTOFixture().withValue("visible summary").build());
        when(asset.hasCharacteristic(new CharacteristicName("visibleNoValue"))).thenReturn(true);
        when(asset.getCharacteristic("visibleNoValue")).thenReturn(new AssetCharacteristicDTOFixture().withValue("").build());
        when(asset.hasCharacteristic(new CharacteristicName(ProductOffering.STENCIL_RESERVED_NAME))).thenReturn(true);
        when(asset.getCharacteristic("visibleNoValue")).thenReturn(new AssetCharacteristicDTOFixture().withValue("").build());
        when(productInstanceClient.getAssetDTO(lineItemModel.getLineItemId())).thenReturn(asset);
        when(asset.getStencilId()).thenReturn(StencilId.versioned(StencilCode.newInstance("123"), StencilVersion.LATEST));

        assertThat(lineItemModel.getProductSCode(), is(S_CODE));
        assertThat(lineItemModel.getId(), is("id"));
        assertThat(lineItemModel.getContractTerm(), is("contractTerm"));
        assertThat(lineItemModel.getAction(), is(LineItemAction.PROVIDE.getDescription()));
        assertThat(lineItemModel.getStatus(), is(DRAFT.getDescription()));
        assertThat(lineItemModel.getDiscountStatus(), is(NOT_APPLICABLE.getDescription()));
        assertThat(lineItemModel.getValidity(), is(LineItemValidationResultDTO.Status.INVALID.name()));
        assertThat(lineItemModel.getBillingId(), is(billingId));
        assertThat(lineItemModel.getSummary(), is("visible summary, stencilName"));
        assertThat(lineItemModel.getIsImportable(), is(false));
        assertThat(lineItemModel.isInFrontCatlogueProduct(), is(false));
    }

    @Test
    public void shouldReturnInfoFromDTOForNonStandardProducts() throws Exception {
        PmrClient pmr = PmrMocker.getMockedInstance(true,
                                                    ProductOfferingFixture.aProductOffering()
                                                                          .withIsInFrontCatalogue(true)
                                                                          .withAttribute(AttributeFixture.anInvisibleRfqAttribute().called(ProductOffering.SPECIAL_BID_TEMPLATE_RESERVED_NAME).build())
                                                                          .withAttribute(ProductOffering.SPECIAL_BID_ATTRIBUTE_INDICATOR)
                                                                          .build());

        ProductInstanceClient productInstanceClient = mock(ProductInstanceClient.class);
        lineItemModel = aLineItemModel().with(
            aQuoteOptionItemDTO().withSCode(S_CODE).withId("id").withContractTerm("contractTerm").withAction(
                LineItemAction.PROVIDE.getDescription()).withStatus(DRAFT)
                .withDiscountStatus(NOT_APPLICABLE).withValidity(LineItemValidationResultDTO.Status.INVALID)
        ).with(productInstanceClient).withPmr(pmr).build();
        AssetDTO asset = mock(AssetDTO.class);
        when(productInstanceClient.getAssetDTO(lineItemModel.getLineItemId())).thenReturn(asset);
        when(asset.isSpecialBid()).thenReturn(true);
        when(asset.hasCharacteristic(new CharacteristicName(ProductOffering.SPECIAL_BID_TEMPLATE_RESERVED_NAME))).thenReturn(true);

        when(asset.getCharacteristic(ProductOffering.SPECIAL_BID_ATTRIBUTE_INDICATOR)).thenReturn(new AssetCharacteristicDTO(new CharacteristicName(ProductOffering.SPECIAL_BID_ATTRIBUTE_INDICATOR), new CharacteristicValue("Yes")));
        when(asset.getCharacteristic(ProductOffering.SPECIAL_BID_TEMPLATE_RESERVED_NAME)).thenReturn(new AssetCharacteristicDTO(new CharacteristicName(ProductOffering.SPECIAL_BID_TEMPLATE_RESERVED_NAME), new CharacteristicValue("aTemplateName")));
        assertThat(lineItemModel.getSummary(), is("aTemplateName"));
        assertThat(lineItemModel.isInFrontCatlogueProduct(), is(true));
    }

    @Test
    public void shouldReturnNullSummaryForNonStencilableAndNonSpecialBidProduct() {
        PmrClient pmr = PmrMocker.getMockedInstance(true,
                                                    ProductOfferingFixture.aProductOffering()
                                                                          .withAttribute(AttributeFixture.aVisibleInSummaryAttribute().called(ProductOffering.STENCIL_RESERVED_NAME).build())
                                                                          .build());

        ProductInstanceClient productInstanceClient = mock(ProductInstanceClient.class);
        final String billingId = "1234467";
        lineItemModel = aLineItemModel().with(
            aQuoteOptionItemDTO().withSCode(S_CODE).withId("id").withContractTerm("contractTerm").withAction(
                LineItemAction.PROVIDE.getDescription()).withStatus(DRAFT)
                .withDiscountStatus(NOT_APPLICABLE).withValidity(LineItemValidationResultDTO.Status.INVALID).withBillingId(billingId)
        ).with(productInstanceClient).withPmr(pmr).build();
        AssetDTO asset = mock(AssetDTO.class);
        when(productInstanceClient.getAssetDTO(lineItemModel.getLineItemId())).thenReturn(asset);
        when(asset.getStencilId()).thenReturn(StencilId.NIL);
        when(asset.isSpecialBid()).thenReturn(false);
        when(asset.hasCharacteristic(new CharacteristicName(ProductOffering.STENCIL_RESERVED_NAME))).thenReturn(true);
        assertThat(lineItemModel.getSummary(), is(StringUtils.EMPTY));
    }

    @Test
    public void shouldReturnEmptyStringWhenNoMessage() {
        assertThat(aLineItemModel().with(aQuoteOptionItemDTO().withValidity(LineItemValidationResultDTO.Status.PENDING)).build().getErrorMessage(),
                   is(""));
    }

    @Test
    public void shouldReturnConcatenatedMessage() {
        List<LineItemValidationDescriptionDTO> descriptions = newArrayList();
        descriptions.add(new LineItemValidationDescriptionDTO("Err1", "DP", ValidationErrorType.Error.toString()));
        descriptions.add(new LineItemValidationDescriptionDTO("Errr2", "DP", ValidationErrorType.Pending.toString()));

        assertThat(aLineItemModel().with(aQuoteOptionItemDTO().withValidity(new LineItemValidationResultDTO(LineItemValidationResultDTO.Status.INVALID, descriptions))).build().getErrorMessage(),
                   is(String.format("Err1%sErrr2", System.getProperty("line.separator"))));
    }

    @Test
    public void shouldReturnProductName() throws Exception {
        lineItemModel = aLineItemModel().with(aQuoteOptionItemDTO().withSCode(S_CODE)).withPmr(pmr).with(productIdentifierFacade).build();
        final ProductOffering offering = ProductOfferingFixture.aProductOffering().withProductIdentifier(new ProductIdentifier(S_CODE, "productName", "1.0")).build();
        PmrMocker.returnForProduct(pmr, offering);

        context.checking(new Expectations() {
            {
                oneOf(productIdentifierFacade).getProductName(offering);
                will(returnValue("productName"));
            }
        });

        assertThat(lineItemModel.getProductName(), is("productName"));
    }

    @Test
    public void shouldReturnDisplayName() throws Exception {
        lineItemModel = aLineItemModel().with(aQuoteOptionItemDTO().withSCode(S_CODE)).withPmr(pmr).with(productIdentifierFacade).build();
        final ProductOffering offering = ProductOfferingFixture.aProductOffering().withProductIdentifier(new ProductIdentifier(S_CODE, "productName", "1.0")).build();
        PmrMocker.returnForProduct(pmr, offering);

        context.checking(new Expectations() {
            {
                oneOf(productIdentifierFacade).getDisplayName(offering);
                will(returnValue("displayName"));
            }
        });

        assertThat(lineItemModel.getDisplayName(), is("displayName"));
    }

    @Test
    public void shouldReturnConfigurationUrlIfQuoteOptionItemInDraftStatus() throws Exception {
        lineItemModel = aLineItemModel().with(aQuoteOptionItemDTO().withStatus(DRAFT).withSCode(S_CODE).withId(LINE_ITEM_ID))
            .withCustomerId(CUSTOMER_ID).withContractId(CONTRACT_ID)
            .with(productConfiguratorUriFactory).with(projectResource).build();

        final ProjectDTO projectDTO = aProjectDTO().build();

        final Map<String, String> parameters = new HashMap<String, String>() {{
            put(READ_ONLY, "false");
            put(STATUS, DRAFT.getDescription());
            put(REVENUE_OWNER, projectDTO.organisation.name);
        }};

        context.checking(new Expectations() {{
            oneOf(projectResource).getProject("projectId");
            will(returnValue(projectDTO));

            allowing(productConfiguratorUriFactory).getConfigurationUri(S_CODE, CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID, LINE_ITEM_ID, parameters);
            will(returnValue(A_HAPPY_URL));
        }});

        assertThat(lineItemModel.getConfigureUrl(projectDTO), is(A_HAPPY_URL));
    }

    @Test
    public void shouldReturnConfigurationUrlIfQuoteOptionItemInOfferedStatus() throws Exception {
        lineItemModel = aLineItemModel().with(aQuoteOptionItemDTO().withStatus(OFFERED).withSCode(S_CODE).withId(LINE_ITEM_ID)).withCustomerId(CUSTOMER_ID)
            .withContractId(CONTRACT_ID).with(productConfiguratorUriFactory).with(projectResource).build();

        final ProjectDTO projectDTO = aProjectDTO().build();

        final Map<String, String> parameters = new HashMap<String, String>() {{
            put(READ_ONLY, "true");
            put(STATUS, OFFERED.getDescription());
            put(REVENUE_OWNER, projectDTO.organisation.name);
        }};

        context.checking(new Expectations() {{
            oneOf(projectResource).getProject("projectId");
            will(returnValue(projectDTO));

            allowing(productConfiguratorUriFactory).getConfigurationUri(S_CODE, CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID, LINE_ITEM_ID, parameters);
            will(returnValue(A_HAPPY_URL));
        }});

        assertThat(lineItemModel.getConfigureUrl(projectDTO), is(A_HAPPY_URL));
    }

    @Test
    public void shouldReturnConfigurationUrlIfQuoteOptionItemInAcceptedStatus() throws Exception {
        lineItemModel = aLineItemModel().with(aQuoteOptionItemDTO().withStatus(CUSTOMER_APPROVED).withSCode(S_CODE).withId(LINE_ITEM_ID))
            .withCustomerId(CUSTOMER_ID).withContractId(CONTRACT_ID).with(productConfiguratorUriFactory).with(projectResource).build();

        final ProjectDTO projectDTO = aProjectDTO().build();

        final Map<String, String> parameters = new HashMap<String, String>() {{
            put(READ_ONLY, "true");
            put(STATUS, CUSTOMER_APPROVED.getDescription());
            put(REVENUE_OWNER, projectDTO.organisation.name);
        }};

        context.checking(new Expectations() {{
            oneOf(projectResource).getProject("projectId");
            will(returnValue(projectDTO));
            allowing(productConfiguratorUriFactory).getConfigurationUri(S_CODE, CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID, LINE_ITEM_ID, parameters);
            will(returnValue(A_HAPPY_URL));
        }});

        assertThat(lineItemModel.getConfigureUrl(projectDTO), is(A_HAPPY_URL));
    }

    @Test
    public void shouldReturnEmptyOfferUrlIfQuoteItemHasNoOffer() throws Exception {
        lineItemModel = aLineItemModel().build();

        assertThat(lineItemModel.getOfferDetailsUrl(), is(""));
    }

    @Test
    public void shouldReturnOfferDetailsUrlIfQuoteItemHasOffer() throws Exception {
        QuoteOptionItemDTOFixture.Builder quoteOptionItemDto = new QuoteOptionItemDTOFixture.Builder();
        quoteOptionItemDto.withOfferId("offerId");
        lineItemModel = aLineItemModel().with(quoteOptionItemDto).withCustomerId("customerId").withContractId("contractId").build();

        assertThat(lineItemModel.getOfferDetailsUrl(),
                   is("/rsqe/customers/customerId/contracts/contractId/projects/projectId/quote-options/quoteOptionId/offers/offerId"));
    }

    @Test
    public void shouldReturnPricingStatusOfSingleLevelTree() throws Exception {
        lineItemModel = aLineItemModel().with(productInstancePricesFacade).with(productInstanceClient).withCustomerId(CUSTOMER_ID)
            .with(aQuoteOptionItemDTO().withId(LINE_ITEM_ID)).build();
        final FutureAssetPricesModel futureAssetPricesModel = context.mock(FutureAssetPricesModel.class);
        final AssetDTO asset = AssetDTOFixture.anAsset().withPricingStatus(PricingStatus.BUDGETARY).build();

        context.checking(new Expectations() {{
            oneOf(productInstanceClient).getAssetDTO(new LineItemId(LINE_ITEM_ID));
            will(returnValue(asset));
            oneOf(productInstancePricesFacade).get(CUSTOMER_ID, PROJECT_ID, QUOTE_OPTION_ID, asset, PriceSuppressStrategy.None);
            will(returnValue(futureAssetPricesModel));
        }});

        assertThat(lineItemModel.getPricingStatusOfTree(), is(PricingStatus.BUDGETARY));
    }

    @Test
    public void testGetOrderStatus() throws Exception {
        for (LineItemOrderStatus lineItemOrderStatus : LineItemOrderStatus.values()) {
            final LineItemModel model = aLineItemModel().with(aQuoteOptionItemDTO().withOrderStatus(lineItemOrderStatus)).build();
            assertThat(model.getOrderStatus(), is(LineItemModel.ORDER_STATUSES.get(lineItemOrderStatus)));
        }
    }


    @Test
    public void shouldReturnDiscountApprovalRequested() throws Exception {
        createLineItemWithDiscountStatus(LineItemDiscountStatus.APPROVAL_REQUESTED);

        assertTrue(lineItemModel.isDiscountApprovalRequested());

        for (LineItemDiscountStatus discountStatus : LineItemDiscountStatus.values()) {
            if (LineItemDiscountStatus.APPROVAL_REQUESTED != discountStatus) {
                createLineItemWithDiscountStatus(discountStatus);
                assertFalse(lineItemModel.isDiscountApprovalRequested());
            }
        }
    }

    @Test
    public void shouldReturnWhetherPriceLinesCanBeUnlockedBasedOnStatusDiscountStatusAndPricingStatus() throws Exception {
        for (QuoteOptionItemStatus lineItemStatus : QuoteOptionItemStatus.values()) {
            for (LineItemDiscountStatus discountStatus : LineItemDiscountStatus.values()) {
                for (PricingStatus pricingStatus : PricingStatus.values()) {
                    if (discountStatus == LineItemDiscountStatus.APPROVED &&
                        (lineItemStatus == QuoteOptionItemStatus.DRAFT || lineItemStatus == QuoteOptionItemStatus.OFFERED ||
                                              lineItemStatus  == QuoteOptionItemStatus.COMMERCIAL_NON_STANDARD_APPROVED ||
                                              lineItemStatus == QuoteOptionItemStatus.COMMERCIAL_NON_STANDARD_REJECTED) &&
                        !PricingStatus.RESTRICT_UNLOCK_LINE_ITEM_STATUSES.contains(pricingStatus)) {
                        assertPriceLinesCanBeUnlocked(discountStatus, lineItemStatus, pricingStatus);
                    } else {
                        assertPriceLinesCanNotBeUnlocked(discountStatus, lineItemStatus, pricingStatus);
                    }
                }
            }
        }

    }

    @Test
    public void testGetPriceBook() {
        lineItemModel = aLineItemModel()
            .with(aQuoteOptionItemDTO().withOfferName("offerName"))
            .build();

        assertThat(lineItemModel.getPriceBook(), is("eup"));
    }

    @Test
    public void testIfcAction() {
        lineItemModel = aLineItemModel()
            .with(aQuoteOptionItemDTO().withOfferName("offerName"))
            .build();

        assertThat(lineItemModel.getIfcAction(), is("Not_applicable"));
    }

    @Test
    public void testIsReadOnly() {
        lineItemModel = aLineItemModel()
            .with(aQuoteOptionItemDTO().withOfferName("offerName"))
            .build();

        assertFalse(lineItemModel.isReadOnly());
    }

    @Test
    public void testGetLineItemId() {
        lineItemModel = aLineItemModel()
            .with(aQuoteOptionItemDTO().withOfferName("offerName"))
            .build();

        assertNotNull(lineItemModel.getLineItemId());
    }

    @Test
    public void testIsSuperseded() {
        lineItemModel = aLineItemModel()
            .with(aQuoteOptionItemDTO().withOfferName("offerName"))
            .build();

        assertFalse(lineItemModel.isSuperseded());
    }

    @Test
    public void testGetProjectId() {
        lineItemModel = aLineItemModel()
            .with(aQuoteOptionItemDTO().withOfferName("offerName"))
            .build();

        assertThat(lineItemModel.projectId(), is("projectId"));
    }

    @Test
    public void testGetQuoteOptionId() {
        lineItemModel = aLineItemModel()
            .with(aQuoteOptionItemDTO().withOfferName("offerName"))
            .build();

        assertThat(lineItemModel.quoteOptionId(), is("quoteOptionId"));
    }

    @Test
    public void testGetCustomerId() {
        lineItemModel = aLineItemModel()
            .with(aQuoteOptionItemDTO().withOfferName("offerName"))
            .build();

        assertNull(lineItemModel.customerId());
    }

    @Test
    public void shouldGetInitialBillingStartDateFromProductInstance() throws Exception {
        ProductInstanceClient productInstanceClient = mock(ProductInstanceClient.class);
        Date expectedDate = Calendar.getInstance().getTime();

        LineItemModel lineItemModel = aLineItemModel().with(productInstanceClient)
                                                      .with(QuoteOptionItemDTOFixture.aQuoteOptionItemDTO().withId("aLineItemId"))
                                                      .build();

        when(productInstanceClient.getAssetDTO(new LineItemId("aLineItemId")))
            .thenReturn(AssetDTOFixture.anAsset().withInitialBillingStartDate(expectedDate).build());

        assertThat(DateUtils.isSameDay(expectedDate, lineItemModel.getInitialBillingStartDate()), is(true));
    }

    @Test
    public void shouldCacheAssetDTOFromCIF() throws Exception {
        ProductInstanceClient productInstanceClient = mock(ProductInstanceClient.class);
        Date expectedDate = Calendar.getInstance().getTime();

        LineItemModel lineItemModel = aLineItemModel().with(productInstanceClient)
                                                      .with(QuoteOptionItemDTOFixture.aQuoteOptionItemDTO().withId("aLineItemId"))
                                                      .build();

        when(productInstanceClient.getAssetDTO(new LineItemId("aLineItemId")))
            .thenReturn(AssetDTOFixture.anAsset().withInitialBillingStartDate(expectedDate).build());

        lineItemModel.getInitialBillingStartDate();
        lineItemModel.getInitialBillingStartDate();

        verify(productInstanceClient, times(1)).getAssetDTO(new LineItemId("aLineItemId"));
    }

    @Test
    public void shouldSetSummaryForMovedProduct() {
        PmrClient pmr = PmrMocker.getMockedInstance(true,
                                                    ProductOfferingFixture.aProductOffering()
                                                                          .withAttribute(AttributeFixture.anInvisibleRfqAttribute().called(ProductOffering.MOVE_TYPE).build())
                                                                          .build());

        ProductInstanceClient productInstanceClient = mock(ProductInstanceClient.class);
        final String billingId = "1234467";
        lineItemModel = aLineItemModel().with(
            aQuoteOptionItemDTO().withSCode(S_CODE).withId("id").withContractTerm("contractTerm").withAction(
                LineItemAction.PROVIDE.getDescription()).withStatus(DRAFT)
                .withDiscountStatus(NOT_APPLICABLE).withValidity(LineItemValidationResultDTO.Status.INVALID).withBillingId(billingId)
        ).with(productInstanceClient).withPmr(pmr).build();
        AssetDTO asset = mock(AssetDTO.class);
        when(productInstanceClient.getAssetDTO(lineItemModel.getLineItemId())).thenReturn(asset);
        when(asset.hasCharacteristic(new CharacteristicName(ProductOffering.MOVE_TYPE))).thenReturn(true);

        when(asset.getCharacteristic(ProductOffering.MOVE_TYPE)).thenReturn(new AssetCharacteristicDTO(new CharacteristicName(ProductOffering.MOVE_TYPE), new CharacteristicValue(AssetProcessType.DIFFERENT_SITE.value())));
        assertThat(lineItemModel.getSummary(), is(AssetProcessType.DIFFERENT_SITE.value()));
    }

    @Test
    public void shouldNotSetSummaryForProvideProductThatIsMoveable() {
        PmrClient pmr = PmrMocker.getMockedInstance(true);

        ProductInstanceClient productInstanceClient = mock(ProductInstanceClient.class);
        final String billingId = "1234467";
        lineItemModel = aLineItemModel().with(
            aQuoteOptionItemDTO().withSCode(S_CODE).withId("id").withContractTerm("contractTerm").withAction(
                LineItemAction.PROVIDE.getDescription()).withStatus(DRAFT)
                .withDiscountStatus(NOT_APPLICABLE).withValidity(LineItemValidationResultDTO.Status.INVALID).withBillingId(billingId)
        ).with(productInstanceClient).withPmr(pmr).build();
        AssetDTO asset = mock(AssetDTO.class);
        when(productInstanceClient.getAssetDTO(lineItemModel.getLineItemId())).thenReturn(asset);
        when(asset.isSpecialBid()).thenReturn(true);
        when(asset.hasCharacteristic(new CharacteristicName(ProductOffering.SPECIAL_BID_TEMPLATE_RESERVED_NAME))).thenReturn(true);

        when(asset.getCharacteristic(ProductOffering.SPECIAL_BID_ATTRIBUTE_INDICATOR)).thenReturn(new AssetCharacteristicDTO(new CharacteristicName(ProductOffering.SPECIAL_BID_ATTRIBUTE_INDICATOR), new CharacteristicValue("Yes")));
        when(asset.getCharacteristic(ProductOffering.SPECIAL_BID_TEMPLATE_RESERVED_NAME)).thenReturn(new AssetCharacteristicDTO(new CharacteristicName(ProductOffering.SPECIAL_BID_TEMPLATE_RESERVED_NAME), new CharacteristicValue("aTemplateName")));
        when(asset.hasCharacteristic(new CharacteristicName(ProductOffering.MOVE_TYPE))).thenReturn(true);

        when(asset.getCharacteristic(ProductOffering.MOVE_TYPE)).thenReturn(new AssetCharacteristicDTO(new CharacteristicName(ProductOffering.MOVE_TYPE), new CharacteristicValue(AssetProcessType.NOT_APPLICABLE.value())));
        assertFalse(lineItemModel.getSummary().equals(AssetProcessType.NOT_APPLICABLE.value()));
    }

    @Test
    public void shouldNotSetSummaryForMovedProductIfMoveTypeIsNull() {
        PmrClient pmr = PmrMocker.getMockedInstance(true);

        ProductInstanceClient productInstanceClient = mock(ProductInstanceClient.class);
        final String billingId = "1234467";
        lineItemModel = aLineItemModel().with(
            aQuoteOptionItemDTO().withSCode(S_CODE).withId("id").withContractTerm("contractTerm").withAction(
                LineItemAction.PROVIDE.getDescription()).withStatus(DRAFT)
                .withDiscountStatus(NOT_APPLICABLE).withValidity(LineItemValidationResultDTO.Status.INVALID).withBillingId(billingId)
        ).with(productInstanceClient).withPmr(pmr).build();
        AssetDTO asset = mock(AssetDTO.class);
        when(productInstanceClient.getAssetDTO(lineItemModel.getLineItemId())).thenReturn(asset);
        when(asset.hasCharacteristic(new CharacteristicName(ProductOffering.MOVE_TYPE))).thenReturn(true);

        when(asset.getCharacteristic(ProductOffering.MOVE_TYPE)).thenReturn(new AssetCharacteristicDTO(new CharacteristicName(ProductOffering.MOVE_TYPE), new CharacteristicValue(null)));
        assertThat(lineItemModel.getSummary(), is(""));
    }

    @Test
    public void shouldCalculateStatusToNotPricedWhereAtLeastOneDescendantIsUnPriced() throws Exception {
        lineItemModel = aLineItemModel().with(productInstancePricesFacade).with(productInstanceClient).withCustomerId(CUSTOMER_ID)
            .with(aQuoteOptionItemDTO().withId(LINE_ITEM_ID)).build();
        final FutureAssetPricesModel futureAssetPricesModel = context.mock(FutureAssetPricesModel.class);
        final AssetDTO asset = AssetDTOFixture.anAsset().withPricingStatus(PricingStatus.BUDGETARY).build();
        final AssetDTO childAsset = AssetDTOFixture.anAsset().withPricingStatus(PricingStatus.NOT_PRICED).build();

        context.checking(new Expectations() {{
            oneOf(productInstanceClient).getAssetDTO(new LineItemId(LINE_ITEM_ID));
            will(returnValue(asset));
            oneOf(productInstancePricesFacade).get(CUSTOMER_ID, PROJECT_ID, QUOTE_OPTION_ID, asset, PriceSuppressStrategy.None);
            will(returnValue(futureAssetPricesModel));
        }});

        assertThat(lineItemModel.getPricingStatusOfTree(), is(PricingStatus.BUDGETARY));
        asset.addChild(childAsset);
        assertThat(lineItemModel.getPricingStatusOfTree(), is(PricingStatus.NOT_PRICED));
    }

    @Test
    public void shouldCalculateStatusToBudgetaryWhereAtLeastOneRelationBudgetaryAndNoneAreNotPriced() throws Exception {
        lineItemModel = aLineItemModel().with(productInstancePricesFacade).with(productInstanceClient).withCustomerId(CUSTOMER_ID)
            .with(aQuoteOptionItemDTO().withId(LINE_ITEM_ID)).build();
        final FutureAssetPricesModel futureAssetPricesModel = context.mock(FutureAssetPricesModel.class);
        final String quoteOptionId = "aQuoteOptionId";
        final AssetDTO asset = AssetDTOFixture.anAsset().withPricingStatus(PricingStatus.NOT_APPLICABLE).withQuoteOptionId(quoteOptionId).build();
        final AssetDTO childAsset1 = AssetDTOFixture.anAsset().withPricingStatus(PricingStatus.NOT_APPLICABLE).withQuoteOptionId(quoteOptionId).build();
        final AssetDTO childAsset2 = AssetDTOFixture.anAsset().withPricingStatus(PricingStatus.BUDGETARY).withQuoteOptionId(quoteOptionId).build();
        final AssetDTO childAsset3 = AssetDTOFixture.anAsset().withPricingStatus(PricingStatus.FIRM).withQuoteOptionId(quoteOptionId).build();

        context.checking(new Expectations() {{
            oneOf(productInstanceClient).getAssetDTO(new LineItemId(LINE_ITEM_ID));
            will(returnValue(asset));
            oneOf(productInstancePricesFacade).get(CUSTOMER_ID, PROJECT_ID, QUOTE_OPTION_ID, asset, PriceSuppressStrategy.None);
            will(returnValue(futureAssetPricesModel));
        }});

        FutureAssetRelationshipDTO relatedToRelationshipDTO = new FutureAssetRelationshipDTO(RelationshipName.newInstance("relName"), RelationshipType.RelatedTo, childAsset2);
        assertThat(lineItemModel.getPricingStatusOfTree(), is(PricingStatus.NOT_APPLICABLE));
        asset.addChild(childAsset1);
        asset.addRelationship(relatedToRelationshipDTO);
        asset.addChild(childAsset3);
        assertThat(lineItemModel.getPricingStatusOfTree(), is(PricingStatus.BUDGETARY));
    }

    @Test
    public void shouldCalculateStatusToNotPricedWhereAtLeastOneRelatedToChildAssetAreNotPriced1() throws Exception {
        lineItemModel = aLineItemModel().with(productInstancePricesFacade).with(productInstanceClient).withCustomerId(CUSTOMER_ID)
            .with(aQuoteOptionItemDTO().withId(LINE_ITEM_ID)).build();
        final FutureAssetPricesModel futureAssetPricesModel = context.mock(FutureAssetPricesModel.class);
        final String quoteOptionId = "aQuoteOptionId";
        final AssetDTO asset = AssetDTOFixture.anAsset().withPricingStatus(PricingStatus.NOT_APPLICABLE).withQuoteOptionId(quoteOptionId).build();
        final AssetDTO childAsset1 = AssetDTOFixture.anAsset().withPricingStatus(PricingStatus.NOT_APPLICABLE).withQuoteOptionId(quoteOptionId).build();
        final AssetDTO childAsset2 = AssetDTOFixture.anAsset().withPricingStatus(PricingStatus.BUDGETARY).withQuoteOptionId(quoteOptionId).build();
        final AssetDTO childAsset3 = AssetDTOFixture.anAsset().withPricingStatus(PricingStatus.FIRM).withQuoteOptionId(quoteOptionId).build();
        final AssetDTO relatedProductChildAsset = AssetDTOFixture.anAsset().withPricingStatus(PricingStatus.NOT_PRICED).withQuoteOptionId(quoteOptionId).build();

        context.checking(new Expectations() {{
            oneOf(productInstanceClient).getAssetDTO(new LineItemId(LINE_ITEM_ID));
            will(returnValue(asset));
            oneOf(productInstancePricesFacade).get(CUSTOMER_ID, PROJECT_ID, QUOTE_OPTION_ID, asset, PriceSuppressStrategy.None);
            will(returnValue(futureAssetPricesModel));
        }});

        childAsset2.addChild(relatedProductChildAsset);
        FutureAssetRelationshipDTO relatedToRelationshipDTO = new FutureAssetRelationshipDTO(RelationshipName.newInstance("relName"), RelationshipType.RelatedTo, childAsset2);
        assertThat(lineItemModel.getPricingStatusOfTree(), is(PricingStatus.NOT_APPLICABLE));
        asset.addChild(childAsset1);
        asset.addRelationship(relatedToRelationshipDTO);
        asset.addChild(childAsset3);
        assertThat(lineItemModel.getPricingStatusOfTree(), is(PricingStatus.NOT_PRICED));
    }

    @Test
    public void shouldIgnoreRelatedToAssetsOfOtherQuoteOptionWhenCalculatingPricingStatusForAHierarchy() throws Exception {
        lineItemModel = aLineItemModel().with(productInstancePricesFacade).with(productInstanceClient).withCustomerId(CUSTOMER_ID)
                .with(aQuoteOptionItemDTO().withId(LINE_ITEM_ID)).build();
        final FutureAssetPricesModel futureAssetPricesModel = context.mock(FutureAssetPricesModel.class);
        final AssetDTO asset = AssetDTOFixture.anAsset().withPricingStatus(PricingStatus.NOT_APPLICABLE).withQuoteOptionId("aQuoteOptionId").build();
        final AssetDTO relatedAssetOfOtherQuoteOption = AssetDTOFixture.anAsset().withPricingStatus(PricingStatus.FIRM).withQuoteOptionId("anotherQuoteOptionId").build();

        context.checking(new Expectations() {{
            oneOf(productInstanceClient).getAssetDTO(new LineItemId(LINE_ITEM_ID));
            will(returnValue(asset));
            oneOf(productInstancePricesFacade).get(CUSTOMER_ID, PROJECT_ID, QUOTE_OPTION_ID, asset, PriceSuppressStrategy.None);
            will(returnValue(futureAssetPricesModel));
        }});

        FutureAssetRelationshipDTO relatedToRelationshipDTO = new FutureAssetRelationshipDTO(RelationshipName.newInstance("relName"), RelationshipType.RelatedTo, relatedAssetOfOtherQuoteOption);
        assertThat(lineItemModel.getPricingStatusOfTree(), is(PricingStatus.NOT_APPLICABLE));
        asset.addRelationship(relatedToRelationshipDTO);
        assertThat(lineItemModel.getPricingStatusOfTree(), is(PricingStatus.NOT_APPLICABLE));
    }


    @Test
    public void shouldDisplayProductsOnThePricingTabWhichArePriceable() throws Exception {
        lineItemModel = aLineItemModel().with(productInstancePricesFacade).with(productInstanceClient).withCustomerId(CUSTOMER_ID)
            .with(aQuoteOptionItemDTO().withId(LINE_ITEM_ID)).build();
        final FutureAssetPricesModel futureAssetPricesModel = context.mock(FutureAssetPricesModel.class);
        final AssetDTO asset = AssetDTOFixture.anAsset().withPricingStatus(PricingStatus.NOT_APPLICABLE).build();
        final AssetDTO relatedAsset = AssetDTOFixture.anAsset().withPricingStatus(PricingStatus.NOT_PRICED).build();

        context.checking(new Expectations() {{
            oneOf(productInstanceClient).getAssetDTO(new LineItemId(LINE_ITEM_ID));
            will(returnValue(asset));
            oneOf(productInstancePricesFacade).get(CUSTOMER_ID, PROJECT_ID, QUOTE_OPTION_ID, asset, PriceSuppressStrategy.None);
            will(returnValue(futureAssetPricesModel));
        }});

        FutureAssetRelationshipDTO relatedToRelationshipDTO = new FutureAssetRelationshipDTO(RelationshipName.newInstance("relName"), RelationshipType.RelatedTo, relatedAsset);
        assertThat(lineItemModel.isPricingStatusOfTreeApplicableForOnPricingTab(), is(false));
        asset.addRelationship(relatedToRelationshipDTO);
        assertThat(lineItemModel.isPricingStatusOfTreeApplicableForOnPricingTab(), is(true));
        relatedAsset.setPricingStatus(PricingStatus.FIRM);
        assertThat(lineItemModel.isPricingStatusOfTreeApplicableForOnPricingTab(), is(false));
    }
    @Test
    public void shouldNotDisplayProductsOnThePricingTabWhichisVisibleInOnlineSummary() throws Exception {
        lineItemModel = aLineItemModel().with(productInstancePricesFacade).with(productInstanceClient).withCustomerId(CUSTOMER_ID)
            .with(aQuoteOptionItemDTO().withAction(LINE_ITEM_ACTION).withId(LINE_ITEM_ID)).build();
        final FutureAssetPricesModel futureAssetPricesModel = context.mock(FutureAssetPricesModel.class);
        final AssetDTO asset = AssetDTOFixture.anAsset().withPricingStatus(PricingStatus.NOT_APPLICABLE).build();
        final AssetDTO relatedAsset = AssetDTOFixture.anAsset().withPricingStatus(PricingStatus.FIRM).build();
        context.checking(new Expectations() {{
            oneOf(productInstanceClient).getAssetDTO(new LineItemId(LINE_ITEM_ID));
            will(returnValue(asset));
            oneOf(productInstancePricesFacade).get(CUSTOMER_ID, PROJECT_ID, QUOTE_OPTION_ID, asset, PriceSuppressStrategy.None);
            will(returnValue(futureAssetPricesModel));
        }});
        FutureAssetRelationshipDTO relatedToRelationshipDTO = new FutureAssetRelationshipDTO(RelationshipName.newInstance("relName"), RelationshipType.RelatedTo, relatedAsset);
        assertThat(lineItemModel.isPricingStatusOfTreeApplicableForOnPricingTab(), is(false));
        asset.addRelationship(relatedToRelationshipDTO);
        assertThat(lineItemModel.isPricingStatusOfTreeApplicableForOnPricingTab(), is(false));
    }
    @Test
    public void shouldNotDisplayProductsOnThePricingTabWhichHaveRespondedRelatedToProducts() throws Exception {
        lineItemModel = aLineItemModel().with(productInstancePricesFacade).with(productInstanceClient).withCustomerId(CUSTOMER_ID)
            .with(aQuoteOptionItemDTO().withId(LINE_ITEM_ID)).build();
        final FutureAssetPricesModel futureAssetPricesModel = context.mock(FutureAssetPricesModel.class);
        final AssetDTO asset = AssetDTOFixture.anAsset().withPricingStatus(PricingStatus.NOT_APPLICABLE).build();
        final AssetDTO relatedAsset = AssetDTOFixture.anAsset().withPricingStatus(PricingStatus.RESPONDED).build();

        context.checking(new Expectations() {{
            oneOf(productInstanceClient).getAssetDTO(new LineItemId(LINE_ITEM_ID));
            will(returnValue(asset));
            oneOf(productInstancePricesFacade).get(CUSTOMER_ID, PROJECT_ID, QUOTE_OPTION_ID, asset, PriceSuppressStrategy.None);
            will(returnValue(futureAssetPricesModel));
        }});

        FutureAssetRelationshipDTO relatedToRelationshipDTO = new FutureAssetRelationshipDTO(RelationshipName.newInstance("relName"), RelationshipType.RelatedTo, relatedAsset);
        assertThat(lineItemModel.isPricingStatusOfTreeApplicableForOnPricingTab(), is(false));
        asset.addRelationship(relatedToRelationshipDTO);
        assertThat(lineItemModel.isPricingStatusOfTreeApplicableForOnPricingTab(), is(false));
    }

    @Test
    public void shouldCalculateStatusToFirmWhereApplicableStatusesAreFirm() throws Exception {
        lineItemModel = aLineItemModel().with(productInstancePricesFacade).with(productInstanceClient).withCustomerId(CUSTOMER_ID)
            .with(aQuoteOptionItemDTO().withId(LINE_ITEM_ID)).build();
        final FutureAssetPricesModel futureAssetPricesModel = context.mock(FutureAssetPricesModel.class);
        final AssetDTO asset = AssetDTOFixture.anAsset().withPricingStatus(PricingStatus.NOT_APPLICABLE).build();
        final AssetDTO childAsset1 = AssetDTOFixture.anAsset().withPricingStatus(PricingStatus.NOT_APPLICABLE).build();
        final AssetDTO childAsset2 = AssetDTOFixture.anAsset().withPricingStatus(PricingStatus.FIRM).build();
        final AssetDTO childAsset3 = AssetDTOFixture.anAsset().withPricingStatus(PricingStatus.FIRM).build();

        context.checking(new Expectations() {{
            oneOf(productInstanceClient).getAssetDTO(new LineItemId(LINE_ITEM_ID));
            will(returnValue(asset));
            oneOf(productInstancePricesFacade).get(CUSTOMER_ID, PROJECT_ID, QUOTE_OPTION_ID, asset, PriceSuppressStrategy.None);
            will(returnValue(futureAssetPricesModel));
        }});

        FutureAssetRelationshipDTO relatedToRelationshipDTO = new FutureAssetRelationshipDTO(RelationshipName.newInstance("relName"), RelationshipType.RelatedTo, childAsset2);
        assertThat(lineItemModel.getPricingStatusOfTree(), is(PricingStatus.NOT_APPLICABLE));
        asset.addChild(childAsset1);
        asset.addRelationship(relatedToRelationshipDTO);
        asset.addChild(childAsset3);
        assertThat(lineItemModel.getPricingStatusOfTree(), is(PricingStatus.FIRM));
    }

    @Test
    public void shouldReturnTrueIfAssetHasAnyFirmPriceLines() throws Exception {
        lineItemModel = aLineItemModel().with(productInstancePricesFacade).with(productInstanceClient).withCustomerId(CUSTOMER_ID)
            .with(aQuoteOptionItemDTO().withId(LINE_ITEM_ID)).build();
        final FutureAssetPricesModel futureAssetPricesModel = context.mock(FutureAssetPricesModel.class);
        final AssetDTO asset = AssetDTOFixture.anAsset().withPriceLine(new PriceLineDTOFixture().withStatus(PriceLineStatus.FIRM).build()).build();

        context.checking(new Expectations() {{
            oneOf(productInstanceClient).getAssetDTO(new LineItemId(LINE_ITEM_ID));
            will(returnValue(asset));
            oneOf(productInstancePricesFacade).get(CUSTOMER_ID, PROJECT_ID, QUOTE_OPTION_ID, asset, PriceSuppressStrategy.None);
            will(returnValue(futureAssetPricesModel));
        }});

        assertThat(lineItemModel.anyAssetsAreFirm(), is(true));
    }

    @Test
    public void shouldReturnTrueIfRelatedAssetHasAnyFirmPriceLines() throws Exception {
        lineItemModel = aLineItemModel().with(productInstancePricesFacade).with(productInstanceClient).withCustomerId(CUSTOMER_ID)
            .with(aQuoteOptionItemDTO().withId(LINE_ITEM_ID)).build();
        final FutureAssetPricesModel futureAssetPricesModel = context.mock(FutureAssetPricesModel.class);
        final AssetDTO asset = AssetDTOFixture.anAsset().withPricingStatus(PricingStatus.NOT_APPLICABLE).build();
        final AssetDTO childAsset2 = AssetDTOFixture.anAsset().withPriceLine(new PriceLineDTOFixture().withStatus(PriceLineStatus.FIRM).build()).withPricingStatus(PricingStatus.FIRM).build();

        context.checking(new Expectations() {{
            oneOf(productInstanceClient).getAssetDTO(new LineItemId(LINE_ITEM_ID));
            will(returnValue(asset));
            oneOf(productInstancePricesFacade).get(CUSTOMER_ID, PROJECT_ID, QUOTE_OPTION_ID, asset, PriceSuppressStrategy.None);
            will(returnValue(futureAssetPricesModel));
        }});

        FutureAssetRelationshipDTO relatedToRelationshipDTO = new FutureAssetRelationshipDTO(RelationshipName.newInstance("relName"), RelationshipType.RelatedTo, childAsset2);
        asset.addRelationship(relatedToRelationshipDTO);

        assertThat(lineItemModel.anyAssetsAreFirm(), is(true));
    }

    @Test
    public void shouldReturnFalseWhenAssetTreeHasNoFirmPriceLines() throws Exception {
        lineItemModel = aLineItemModel().with(productInstancePricesFacade).with(productInstanceClient).withCustomerId(CUSTOMER_ID)
            .with(aQuoteOptionItemDTO().withId(LINE_ITEM_ID)).build();
        final FutureAssetPricesModel futureAssetPricesModel = context.mock(FutureAssetPricesModel.class);
        final AssetDTO asset = AssetDTOFixture.anAsset().withPriceLine(new PriceLineDTOFixture().withStatus(PriceLineStatus.NOT_PRICED).build()).build();

        context.checking(new Expectations() {{
            oneOf(productInstanceClient).getAssetDTO(new LineItemId(LINE_ITEM_ID));
            will(returnValue(asset));
            oneOf(productInstancePricesFacade).get(CUSTOMER_ID, PROJECT_ID, QUOTE_OPTION_ID, asset, PriceSuppressStrategy.None);
            will(returnValue(futureAssetPricesModel));
        }});

        assertThat(lineItemModel.anyAssetsAreFirm(), is(false));
    }

    @Test
    public void shouldReturnTrueIfAssetIsContractResigned() throws Exception {
        lineItemModel = aLineItemModel().with(productInstancePricesFacade).with(productInstanceClient).withCustomerId(CUSTOMER_ID)
                                        .with(aQuoteOptionItemDTO().withId(LINE_ITEM_ID)).build();
        final FutureAssetPricesModel futureAssetPricesModel = context.mock(FutureAssetPricesModel.class);
        final AssetDTO asset = AssetDTOFixture.anAsset().withPricingStatus(PricingStatus.NOT_PRICED).build();
        asset.detail().setContractResignStatus(Constants.NO);
        final AssetDTO childAsset = AssetDTOFixture.anAsset().withPriceLine(new PriceLineDTOFixture().withStatus(PriceLineStatus.FIRM).build())
                                                    .withPricingStatus(PricingStatus.NOT_PRICED)
                                                    .build();
        childAsset.detail().setContractResignStatus(Constants.YES);

        context.checking(new Expectations() {{
            oneOf(productInstanceClient).getAssetDTO(new LineItemId(LINE_ITEM_ID));
            will(returnValue(asset));
            oneOf(productInstancePricesFacade).get(CUSTOMER_ID, PROJECT_ID, QUOTE_OPTION_ID, asset, PriceSuppressStrategy.None);
            will(returnValue(futureAssetPricesModel));
        }});

        FutureAssetRelationshipDTO relatedToRelationshipDTO = new FutureAssetRelationshipDTO(RelationshipName.newInstance("relName"), RelationshipType.RelatedTo, childAsset);
        asset.addRelationship(relatedToRelationshipDTO);

        assertThat(lineItemModel.anyAssetsAreContractResigned(), is(true));
    }

    private void assertPriceLinesCanBeUnlocked(LineItemDiscountStatus discountStatus, QuoteOptionItemStatus lineItemStatus, PricingStatus pricingStatus) {
        assertPriceLinesCanBeUnlocked(true, discountStatus, lineItemStatus, pricingStatus);
    }

    private void assertPriceLinesCanNotBeUnlocked(LineItemDiscountStatus discountStatus, QuoteOptionItemStatus lineItemStatus, PricingStatus pricingStatus) {
        assertPriceLinesCanBeUnlocked(false, discountStatus, lineItemStatus, pricingStatus);
    }

    private void assertPriceLinesCanBeUnlocked(boolean priceLinesCanBeUnlocked, LineItemDiscountStatus discountStatus, QuoteOptionItemStatus lineItemStatus, final PricingStatus pricingStatus) {
        final FutureAssetPricesFacade facade = context.mock(FutureAssetPricesFacade.class);
        lineItemModel = aLineItemModel().with(
            aQuoteOptionItemDTO()
                .withId(LINE_ITEM_ID)
                .withStatus(lineItemStatus)
                .withDiscountStatus(discountStatus))
            .with(facade)
            .withCustomerId(CUSTOMER_ID)
            .with(productInstanceClient)
            .build();

        final AssetDTO asset = AssetDTOFixture.anAsset().withPricingStatus(pricingStatus).build();

        context.checking(new Expectations() {{

            final FutureAssetPricesModel futureAssetPricesModel = context.mock(FutureAssetPricesModel.class);

            oneOf(productInstanceClient).getAssetDTO(new LineItemId(LINE_ITEM_ID));
            will(returnValue(asset));
            allowing(facade).get(CUSTOMER_ID, PROJECT_ID, QUOTE_OPTION_ID, asset, PriceSuppressStrategy.None);
            will(returnValue(futureAssetPricesModel));
        }});
        assertThat(discountStatus.getDescription() + " " + lineItemStatus.getDescription() + " " + pricingStatus.getDescription() + " Unlocked: " + priceLinesCanBeUnlocked,
                   lineItemModel.priceLinesCanBeUnLocked(), is(priceLinesCanBeUnlocked));
    }

    private void createLineItemWithDiscountStatus(LineItemDiscountStatus discountStatus) {
        lineItemModel = aLineItemModel().with(
            aQuoteOptionItemDTO().withDiscountStatus(discountStatus))
            .build();
    }


}
