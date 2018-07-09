package com.bt.rsqe.projectengine.web.quoteoption;

import com.bt.rsqe.Money;
import com.bt.rsqe.customerinventory.dto.PricePointDTO;
import com.bt.rsqe.customerinventory.dto.SpecialPriceBookDTO;
import com.bt.rsqe.customerinventory.fixtures.SpecialPriceBookFixture;
import com.bt.rsqe.customerrecord.SiteDTO;
import com.bt.rsqe.domain.project.PricingStatus;
import com.bt.rsqe.domain.project.TerminationType;
import com.bt.rsqe.enums.PriceType;
import com.bt.rsqe.matchers.excel.bcm.BcmSheetSpecialPriceBookPageMatcher;
import com.bt.rsqe.matchers.excel.bcm.BcmSheetUsagePageMatcher;
import com.bt.rsqe.pricing.config.dto.PricingConfig;
import com.bt.rsqe.projectengine.web.facades.LineItemFacade;
import com.bt.rsqe.projectengine.web.facades.ProductIdentifierFacade;
import com.bt.rsqe.projectengine.web.facades.SiteFacade;
import com.bt.rsqe.projectengine.web.model.FutureAssetPricesModel;
import com.bt.rsqe.projectengine.web.model.LineItemModel;
import com.bt.rsqe.projectengine.web.model.OneVoicePriceTariff;
import com.bt.rsqe.projectengine.web.quoteoption.bcmsheet.ProductsBCMSheetFactory;
import com.bt.rsqe.projectengine.web.quoteoptionpricing.PriceSuppressStrategy;
import com.bt.rsqe.security.UserContextManager;
import com.bt.rsqe.utils.countries.Countries;
import com.google.common.collect.Lists;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.bt.rsqe.customerinventory.fixtures.PricePointFixture.*;
import static com.bt.rsqe.enums.ProductCodes.*;
import static com.bt.rsqe.expedio.fixtures.SiteDTOFixture.*;
import static com.bt.rsqe.matchers.excel.bcm.BcmSheetMatcher.*;
import static com.bt.rsqe.matchers.excel.bcm.BcmSheetOnevoiceChannelInformationPageMatcher.OnevoiceChannelInformationSite.*;
import static com.bt.rsqe.matchers.excel.bcm.BcmSheetOnevoiceChannelInformationPageMatcher.*;
import static com.bt.rsqe.matchers.excel.bcm.BcmSheetOnevoiceOptionsPageMatcher.*;
import static com.bt.rsqe.matchers.excel.bcm.BcmSheetOnevoiceOptionsPageMatcher.OnevoiceOptionsPageSite.*;
import static com.bt.rsqe.projectengine.web.fixtures.FutureAssetPricesDTOFixture.*;
import static com.bt.rsqe.projectengine.web.fixtures.FutureAssetPricesModelFixture.*;
import static com.bt.rsqe.projectengine.web.fixtures.PriceLineDTOFixture.*;
import static com.bt.rsqe.security.UserContextBuilder.*;
import static com.google.common.collect.Lists.*;
import static com.google.common.collect.Maps.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.core.Is.*;
import static org.mockito.Mockito.*;

public class QuoteOptionBcmExportPricingSheetOrchestratorTest {
    private static final String CUSTOMER_ID = "CUSTOMER_ID";
    private static final String CONTRACT_ID = "CONTRACT_ID";
    private static final String PROJECT_ID = "PROJECT_ID";
    private static final String QUOTE_OPTION_ID = "QUOTE_OPTION_ID";
    protected static final String CURRENCY = "GBP";
    protected static final String CUSTOMER_NAME = "CustomerName";
    protected static final String SALES_REP = "SalesRep";
    protected static final String BID_NUMBER = "BidNumber";
    protected static final String OPPORTUNITY_ID = "opportunityId";
    protected static final String TRADE_LEVEL = "trade level";
    private static final String EXPECTED_ORIGINATING_COUNTRY = "country1";
    private static final String EXPECTED_CITY = "city1";
    private static final String EXPECTED_SITE_ID = "siteId1";
    private static final String EXPECTED_SITE_NAME = "siteName1";
    private static final String EXPECTED_SITE_ACCESS_TYPE = "MPLS";
    private QuoteOptionBcmExportPricingSheetOrchestrator orchestrator;
    private final LineItemFacade mockLineItemFacade = mock(LineItemFacade.class);
    private final SiteFacade mockSiteFacade = mock(SiteFacade.class);
    private final ProductIdentifierFacade mockProductIdentifierFacade = mock(ProductIdentifierFacade.class);
    private final QuoteOptionBcmExportChannelInformationSheetFactory mockChannelInformationSheetFactory = mock(QuoteOptionBcmExportChannelInformationSheetFactory.class);
    private final QuoteOptionBcmExportSpecialPriceBookSheetFactory mockPriceBookFactory = mock(QuoteOptionBcmExportSpecialPriceBookSheetFactory.class);
    private final QuoteOptionBcmExportBidInfoSheetFactory mockBidInfoSheetFactory = mock(QuoteOptionBcmExportBidInfoSheetFactory.class);
    private final QuoteOptionBcmExportUsageSheetFactory mockUsageSheetFactory = mock(QuoteOptionBcmExportUsageSheetFactory.class);

    private static final Double VPN_CONFIG_RRP_PRICE = 25.12;
    private static final Double VPN_CONFIG_PTP_PRICE = 50.24;
    private static final Double VPN_SUBSCRIPTION_RRP_PRICE = 50.12;
    private static final Double VPN_SUBSCRIPTION_PTP_PRICE = 100.24;
    private static final Double DIAL_PLAN_CHANGE_CONFIG_RRP_PRICE = 10.12;
    private static final Double DIAL_PLAN_CHANGE_CONFIG_PTP_PRICE = 20.24;
    private static final Double MAJOR_MOVE_ADD_CHANGE_CONFIG_RRP_PRICE = 20.12;
    private static final Double MAJOR_MOVE_ADD_CHANGE_CONFIG_PTP_PRICE = 40.24;
    private static final Double VPN_CONFIG_RRP_DISCOUNT_PERCENT = 10.65721;
    private static final Double VPN_SUBSCRIPTION_RRP_DISCOUNT_PERCENT = 15.98271;
    private static final Double DIAL_PLAN_CHANGE_CONFIG_RRP_DISCOUNT_PERCENT = 12.34689;
    private static final Double MAJOR_MOVE_ADD_CHANGE_CONFIG_RRP_DISCOUNT_PERCENT = 20.01025;
    private static final Double CANCELLATION_CHARGE_PRICE = 75.50;
    private LineItemModel lineItem;
    private QuoteOptionBcmExportSiteDetailsSheetFactory mockSiteDetailsFactory = mock(QuoteOptionBcmExportSiteDetailsSheetFactory.class);
    private QuoteOptionBcmExportProductLevelInfoSheetFactory mockProductLevelInfoSheetFactory = mock(QuoteOptionBcmExportProductLevelInfoSheetFactory.class);
    private ProductsBCMSheetFactory mockProductBCMSheetFactory = mock(ProductsBCMSheetFactory.class);
    private PricingConfig pricingConfig;

    @Before
    public void before() {
        orchestrator = new QuoteOptionBcmExportPricingSheetOrchestrator(mockLineItemFacade,
                                                                        mockChannelInformationSheetFactory,
                                                                        mockBidInfoSheetFactory,
                                                                        mockUsageSheetFactory,
                                                                        mockPriceBookFactory,
                                                                        mockSiteDetailsFactory,
                                                                        mockProductLevelInfoSheetFactory, mockProductBCMSheetFactory);
        lineItem = mock(LineItemModel.class);

        pricingConfig = mock(PricingConfig.class);
    }

    @Test
    public void shouldGenerateOptionsSheetWithSiteDetails() throws Exception {
        final SiteDTO siteDTO = createSiteDTO();


        when(lineItem.getSite()).thenReturn(siteDTO);
        when(lineItem.getSite()).thenReturn(siteDTO);
        when(lineItem.getFutureAssetPricesModel()).thenReturn(mock(FutureAssetPricesModel.class));

        stubDependencies(newArrayList(lineItem), newArrayList(siteDTO), Lists.<Map<String, String>>newArrayList(), Lists.<Map<String, String>>newArrayList(), Lists.<Map<String, String>>newArrayList());

        HSSFWorkbook bcmSheet = generateBcmExportSheetForDirectUser();
        assertThat(bcmSheet, is(aBcmSheet()
                                    .with(
                                        aOnevoiceOptionsPage()
                                            .with(OnevoiceOptionsPageSite.aSite()
                                                                         .withId("siteId1")
                                                                         .withName("siteName1")
                                                                         .withAddress("building1, city1, country1, postcode1")
                                            ))
        ));
    }

    @Test
    public void shouldGenerateOptionsSheetWithPriceDetailsForDirectUser() throws Exception {
        final FutureAssetPricesModel oneVoicePriceModelWithPriceLines = createPricesModel();

        when(lineItem.getFutureAssetPricesModel()).thenReturn(oneVoicePriceModelWithPriceLines);
        when(lineItem.getSite()).thenReturn(SiteDTO.CUSTOMER_OWNED);


        stubDependencies(newArrayList(lineItem), newArrayList(aSiteDTO().build()), Lists.<Map<String, String>>newArrayList(), Lists.<Map<String, String>>newArrayList(), Lists.<Map<String, String>>newArrayList());
        HSSFWorkbook bcmSheet = generateBcmExportSheetForDirectUser();
        assertThat(bcmSheet, is(aBcmSheet()
                                    .with(
                                        aOnevoiceOptionsPage()
                                            .with(aSite()
                                                      .withName(SiteDTO.CUSTOMER_OWNED.name)
                                                      .withVpnConfigRrp(VPN_CONFIG_RRP_PRICE.toString())
                                                      .withVpnConfigPtp("")
                                                      .withVpnConfigDiscount("0.1065721")

                                                      .withVpnSubscriptionRrp(VPN_SUBSCRIPTION_RRP_PRICE.toString())
                                                      .withVpnSubscriptionPtp("")
                                                      .withVpnSubscriptionDiscount("0.1598271")

                                                      .withDialPlanChangeConfigRrp(DIAL_PLAN_CHANGE_CONFIG_RRP_PRICE.toString())
                                                      .withDialPlanChangeConfigPtp("")
                                                      .withDialPlanChangeConfigDiscount("0.1234689")

                                                      .withMajorMoveAddChangeRrp(MAJOR_MOVE_ADD_CHANGE_CONFIG_RRP_PRICE.toString())
                                                      .withMajorMoveAddChangePtp("")
                                                      .withMajorMoveAddChangeDiscount("0.2001025")

                                                      .withCancellationCharge(CANCELLATION_CHARGE_PRICE.toString())
                                            )
                                    )
        ));
    }

    @Test
    public void shouldGenerateOptionsSheetWithPriceDetailsForIndirectUser() throws Exception {
        final FutureAssetPricesModel oneVoicePriceModelWithPriceLines = createPricesModel();

        when(lineItem.getFutureAssetPricesModel()).thenReturn(oneVoicePriceModelWithPriceLines);
        when(lineItem.getSite()).thenReturn(SiteDTO.CUSTOMER_OWNED);

        stubDependencies(newArrayList(lineItem), newArrayList(aSiteDTO().build()), Lists.<Map<String, String>>newArrayList(), Lists.<Map<String, String>>newArrayList(), Lists.<Map<String, String>>newArrayList());

        HSSFWorkbook bcmSheet = generateBcmExportSheetForIndirectUser();
        assertThat(bcmSheet, is(aBcmSheet()
                                    .with(
                                        aOnevoiceOptionsPage()
                                            .with(aSite()
                                                      .withName(SiteDTO.CUSTOMER_OWNED.name)
                                                      .withVpnConfigRrp(VPN_CONFIG_PTP_PRICE.toString())
                                                      .withVpnConfigPtp(VPN_CONFIG_RRP_PRICE.toString())
                                                      .withVpnConfigDiscount("0.1065721")

                                                      .withVpnSubscriptionRrp(VPN_SUBSCRIPTION_PTP_PRICE.toString())
                                                      .withVpnSubscriptionPtp(VPN_SUBSCRIPTION_RRP_PRICE.toString())
                                                      .withVpnSubscriptionDiscount("0.1598271")

                                                      .withDialPlanChangeConfigRrp(DIAL_PLAN_CHANGE_CONFIG_PTP_PRICE.toString())
                                                      .withDialPlanChangeConfigPtp(DIAL_PLAN_CHANGE_CONFIG_RRP_PRICE.toString())
                                                      .withDialPlanChangeConfigDiscount("0.1234689")

                                                      .withMajorMoveAddChangeRrp(MAJOR_MOVE_ADD_CHANGE_CONFIG_PTP_PRICE.toString())
                                                      .withMajorMoveAddChangePtp(MAJOR_MOVE_ADD_CHANGE_CONFIG_RRP_PRICE.toString())
                                                      .withMajorMoveAddChangeDiscount("0.2001025")

                                                      .withCancellationCharge(CANCELLATION_CHARGE_PRICE.toString())
                                            )
                                    )
        ));
    }

    @Test
    public void shouldSortLineItemModelsBeforeGeneratingRows() throws Exception {
        final LineItemModel lineItem1 = mock(LineItemModel.class);
        final LineItemModel lineItem2 = mock(LineItemModel.class);
        final List<LineItemModel> unsortedLineItems = newArrayList(lineItem2, lineItem1);
        final List<LineItemModel> sortedLineItems = newArrayList(lineItem1, lineItem2);
        final List<Map<String, String>> sheetRows = new ArrayList<Map<String, String>>();


        when(mockLineItemFacade.fetchLineItems(isA(String.class), isA(String.class), isA(String.class), isA(String.class), isA(PriceSuppressStrategy.class))).thenReturn(newArrayList(unsortedLineItems));

        when(lineItem1.getSite()).thenReturn(new SiteDTO("1", "Site_1"));

        when(lineItem1.getSite()).thenReturn(new SiteDTO("1", "Site_1"));

        when(lineItem2.getSite()).thenReturn(new SiteDTO("2", "Site_2"));

        when(lineItem2.getSite()).thenReturn(new SiteDTO("2", "Site_2"));

        when(lineItem1.getFutureAssetPricesModel()).thenReturn(mock(FutureAssetPricesModel.class));
        when(lineItem2.getFutureAssetPricesModel()).thenReturn(mock(FutureAssetPricesModel.class));


        when(mockChannelInformationSheetFactory.createChannelInfoSheetRows(sortedLineItems)).thenReturn(sheetRows);

        when(mockUsageSheetFactory.createUsageRows(sortedLineItems)).thenReturn(sheetRows);


        orchestrator.renderBcmExportSheet(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID);
    }

    @Test
    public void shouldExcludeNotPricedLineItems() throws Exception {
        final LineItemModel notPricedLineItem = mock(LineItemModel.class);


        when(mockLineItemFacade.fetchLineItems(isA(String.class), isA(String.class), isA(String.class), isA(String.class), isA(PriceSuppressStrategy.class))).thenReturn(newArrayList(notPricedLineItem));

        when(notPricedLineItem.getPricingStatusOfTree()).thenReturn(PricingStatus.NOT_PRICED);

        when(mockChannelInformationSheetFactory.createChannelInfoSheetRows(new ArrayList<LineItemModel>())).thenReturn(new ArrayList<Map<String, String>>());

        when(mockUsageSheetFactory.createUsageRows(new ArrayList<LineItemModel>())).thenReturn(new ArrayList<Map<String, String>>());


        orchestrator.renderBcmExportSheet(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID);
    }


    @Test
    public void shouldGenerateUsageSheetForIndirectUser() throws Exception {
        final FutureAssetPricesModel oneVoicePriceModelWithPriceLines = createPricesModel();

        when(lineItem.getFutureAssetPricesModel()).thenReturn(oneVoicePriceModelWithPriceLines);
        when(lineItem.getSite()).thenReturn(SiteDTO.CUSTOMER_OWNED);

        SiteDTO site = createSiteDTO();
        List<BcmSheetUsagePageMatcher.UsageRow> usageRows = someProjectedUsageDTOs(site);
        List<Map<String, String>> usageRowList = new ArrayList<Map<String, String>>();
        for (BcmSheetUsagePageMatcher.UsageRow usageRow : usageRows) {
            usageRowList.add(usageRow.toMap());
        }
        stubDependencies(newArrayList(lineItem),
                         newArrayList(site),
                         Lists.<Map<String, String>>newArrayList(),
                         Lists.<Map<String, String>>newArrayList(),
                         usageRowList);

        HSSFWorkbook bcmSheet = generateBcmExportSheetForIndirectUser();
        assertThat(bcmSheet, is(aBcmSheet()
                                    .with(BcmSheetUsagePageMatcher
                                              .aOnevoiceUsagePage()
                                              .withUsageRows(usageRows)
                                    )));
    }

    private List<BcmSheetUsagePageMatcher.UsageRow> someProjectedUsageDTOs(SiteDTO siteDTO) {
        return Arrays.asList(
            new BcmSheetUsagePageMatcher.UsageRow(siteDTO, Countries.byIsoStatic("AE").getDisplayName(), TerminationType.ON_NET.getDisplayName()),
            new BcmSheetUsagePageMatcher.UsageRow(siteDTO, Countries.byIsoStatic("IN").getDisplayName(), TerminationType.OFF_NET.getDisplayName()),
            new BcmSheetUsagePageMatcher.UsageRow(siteDTO, Countries.byIsoStatic("IN").getDisplayName(), TerminationType.ON_NET.getDisplayName()),
            new BcmSheetUsagePageMatcher.UsageRow(siteDTO, Countries.byIsoStatic("QA").getDisplayName(), TerminationType.MOBILE.getDisplayName())
        );
    }

    @Test
    public void shouldGenerateSpecialPriceBookSheetForIndirectUser() throws Exception {
        final FutureAssetPricesModel oneVoicePriceModelWithPriceLines = createPricesModel();

        when(lineItem.getFutureAssetPricesModel()).thenReturn(oneVoicePriceModelWithPriceLines);
        when(lineItem.getSite()).thenReturn(SiteDTO.CUSTOMER_OWNED);

        SpecialPriceBookDTO specialPriceBook = createSpecialPriceBook();
        stubDependencies(newArrayList(lineItem),
                         newArrayList(aSiteDTO().build()),
                         Lists.<Map<String, String>>newArrayList(),
                         Lists.<Map<String, String>>newArrayList(),
                         Lists.<Map<String, String>>newArrayList());

        HSSFWorkbook bcmSheet = generateBcmExportSheetForIndirectUser();
        assertThat(bcmSheet, is(aBcmSheet()
                                    .with(BcmSheetSpecialPriceBookPageMatcher
                                              .aSpecialPriceBookPageForIndirect()
                                              .withPricePointRowsFor(specialPriceBook))));
    }

    private SpecialPriceBookDTO createSpecialPriceBook() {
        return new SpecialPriceBookDTO(
            new SpecialPriceBookFixture()
                .withName("quote option name")
                .withPricePoints(
                    aPricePoint().withDestination(Countries.byIsoStatic("AE")),
                    aPricePoint().withTerminationType(TerminationType.ON_NET),
                    aPricePoint().withBasePrice("10.5"),
                    aPricePoint().withDestination(Countries.byIsoStatic("LK")).withDiscountValue("5").withTariffOption("tariff")
                ).build());
    }

    @Test
    public void shouldGenerateOptionsSheetWithEmptyPriceDetailsWhenRequiredPriceLinesNotPresent() throws Exception {


        stubDependencies(newArrayList(lineItem), newArrayList(aSiteDTO().build()), Lists.<Map<String, String>>newArrayList(), Lists.<Map<String, String>>newArrayList(), Lists.<Map<String, String>>newArrayList());
        when(lineItem.getSite()).thenReturn(SiteDTO.CUSTOMER_OWNED);
        when(lineItem.getFutureAssetPricesModel()).thenReturn(mock(FutureAssetPricesModel.class));


        HSSFWorkbook bcmSheet = generateBcmExportSheetForDirectUser();
        assertThat(bcmSheet, is(aBcmSheet()
                                    .with(
                                        aOnevoiceOptionsPage()
                                            .with(withCustomerOwnedSiteData()))
        ));
    }

    @Test
    public void shouldGenerateEmptyOptionsSheetForNonOnevoiceProducts() throws Exception {
        withDirectUser();
        stubDependencies(Lists.<LineItemModel>newArrayList(), newArrayList(aSiteDTO().build()), Lists.<Map<String, String>>newArrayList(), Lists.<Map<String, String>>newArrayList(), Lists.<Map<String, String>>newArrayList());

        HSSFWorkbook bcmSheet = generateBcmExportSheetForDirectUser();
        assertThat(bcmSheet, is(aBcmSheet()
                                    .with(
                                        aOnevoiceOptionsPage()
                                            .with(noSiteData()))
        ));
    }

    @Test
    public void shouldGenerateChannelInformationSheetWithSiteDetails() throws Exception {

        final SiteDTO siteDTO = createSiteDTO();

        final Map<String, String> channelInfoRow = new HashMap<String, String>();
        channelInfoRow.put("ov-channel-info.originating-country", EXPECTED_ORIGINATING_COUNTRY);
        channelInfoRow.put("ov-channel-info.city", EXPECTED_CITY);
        channelInfoRow.put("ov-channel-info.site-id", EXPECTED_SITE_ID);
        channelInfoRow.put("ov-channel-info.site-name", EXPECTED_SITE_NAME);
        channelInfoRow.put("ov-channel-info.access-type", EXPECTED_SITE_ACCESS_TYPE);

        final List<Map<String, String>> channelInfoRows = newArrayList(channelInfoRow);

        stubDependencies(newArrayList(newArrayList(lineItem)), newArrayList(siteDTO), channelInfoRows, Lists.<Map<String, String>>newArrayList(), Lists.<Map<String, String>>newArrayList());
        when(lineItem.getSite()).thenReturn(SiteDTO.CUSTOMER_OWNED);
        when(lineItem.getFutureAssetPricesModel()).thenReturn(mock(FutureAssetPricesModel.class));


        HSSFWorkbook bcmSheet = generateBcmExportSheetForIndirectUser();
        assertThat(bcmSheet, is(aBcmSheet()
                                    .with(
                                        aOnevoiceChannelInformationPage()
                                            .with(aOnevoiceChannelInformationSite()
                                                      .withCountry(EXPECTED_ORIGINATING_COUNTRY)
                                                      .withCity(EXPECTED_CITY)
                                                      .withId(EXPECTED_SITE_ID)
                                                      .withName(EXPECTED_SITE_NAME)
                                                      .withAccessType(EXPECTED_SITE_ACCESS_TYPE))
                                    )));
    }


    private void stubDependencies(final List<LineItemModel> lineItems, final List<SiteDTO> sites, final List<Map<String, String>> channelInfoRows, final List<Map<String, String>> bidInfoRow, final List<Map<String, String>> usageRow) {

        when(mockLineItemFacade.fetchLineItems(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID, PriceSuppressStrategy.None)).thenReturn(newArrayList(lineItems));

        when(mockSiteFacade.get(isA(String.class), eq(PROJECT_ID), isA(String.class))).thenReturn(sites.get(0));       /// THIS WILL BE A PROBLEM FOR MULTIPLE SITE ASSERTIONS

        when(mockChannelInformationSheetFactory.createChannelInfoSheetRows(lineItems)).thenReturn(channelInfoRows);

        when(mockBidInfoSheetFactory.createBidInfoRow(CUSTOMER_ID, PROJECT_ID, QUOTE_OPTION_ID)).thenReturn(bidInfoRow);

        when(mockUsageSheetFactory.createUsageRows(lineItems)).thenReturn(usageRow);

        when(mockPriceBookFactory.createPriceBookSheetRows(QUOTE_OPTION_ID)).thenReturn(expectedSpecialPriceBookAsMap());

    }

    private List<Map<String, String>> expectedSpecialPriceBookAsMap() {
        final SpecialPriceBookDTO specialPriceBook = createSpecialPriceBook();
        List<Map<String, String>> rows = new ArrayList<Map<String, String>>();
        for (PricePointDTO pricePoint : specialPriceBook.getPricePoints()) {
            final Map<String, String> row = newHashMap();
            row.put("priceBook.name", specialPriceBook.getName());
            row.put("priceBook.originatingCountry", pricePoint.getOriginCountry().getDisplayName());
            row.put("priceBook.destinationCountry", pricePoint.getDestination().getDisplayName());
            row.put("priceBook.terminationType", pricePoint.getTerminationType().getDisplayName());
            row.put("priceBook.rrpPrice", "");
            row.put("priceBook.ptpPrice", Money.from(pricePoint.getBasePrice()).toString());
            row.put("priceBook.discount", Money.from(pricePoint.getDiscountValue()).toString());
            row.put("priceBook.tariffType", pricePoint.getTariffOption());
            rows.add(row);
        }
        return rows;
    }

    private SiteDTO createSiteDTO() {
        return aSiteDTO()
            .withBfgSiteId(EXPECTED_SITE_ID)
            .withName(EXPECTED_SITE_NAME)
            .withFloor("floor1")
            .withBuilding("building1")
            .withCity(EXPECTED_CITY)
            .withCountry(EXPECTED_ORIGINATING_COUNTRY)
            .withPostCode("postcode1")
            .build();
    }

    private FutureAssetPricesModel createPricesModel() {
        return aFutureAssetPricesModel()
            .with(
                aFutureAssetPricesDTO()
                    .withProductCode(Onevoice.productCode())
                    .withChild(
                        aFutureAssetPricesDTO()
                            .withProductCode(OnevoiceBasicMPLS.productCode())
                            .withPriceLine(
                                aPriceLineDTO()
                                    .withPpsrId(OneVoicePriceTariff.VPN_CONFIG.ppsrId())
                                    .with(PriceType.ONE_TIME)
                                    .withChargePrice(VPN_CONFIG_RRP_PRICE)
                                    .withChargePriceDiscount(VPN_CONFIG_RRP_DISCOUNT_PERCENT)
                                    .withEupPrice(VPN_CONFIG_PTP_PRICE))
                            .withPriceLine(
                                aPriceLineDTO()
                                    .withPpsrId(OneVoicePriceTariff.VPN_SUBSCRIPTION.ppsrId())
                                    .with(PriceType.RECURRING)
                                    .withChargePrice(VPN_SUBSCRIPTION_RRP_PRICE)
                                    .withChargePriceDiscount(VPN_SUBSCRIPTION_RRP_DISCOUNT_PERCENT)
                                    .withEupPrice(VPN_SUBSCRIPTION_PTP_PRICE))
                            .withPriceLine(
                                aPriceLineDTO()
                                    .withPpsrId(OneVoicePriceTariff.DIAL_PLAN_CHANGE_CONFIG.ppsrId())
                                    .with(PriceType.ONE_TIME)
                                    .withChargePrice(DIAL_PLAN_CHANGE_CONFIG_RRP_PRICE)
                                    .withChargePriceDiscount(DIAL_PLAN_CHANGE_CONFIG_RRP_DISCOUNT_PERCENT)
                                    .withEupPrice(DIAL_PLAN_CHANGE_CONFIG_PTP_PRICE))
                            .withPriceLine(
                                aPriceLineDTO()
                                    .withPpsrId(OneVoicePriceTariff.MAJOR_MOVE_ADDS_OR_CHANGE_CONFIG.ppsrId())
                                    .with(PriceType.ONE_TIME)
                                    .withChargePrice(MAJOR_MOVE_ADD_CHANGE_CONFIG_RRP_PRICE)
                                    .withChargePriceDiscount(MAJOR_MOVE_ADD_CHANGE_CONFIG_RRP_DISCOUNT_PERCENT)
                                    .withEupPrice(MAJOR_MOVE_ADD_CHANGE_CONFIG_PTP_PRICE))
                            .withPriceLine(
                                aPriceLineDTO()
                                    .withPpsrId(OneVoicePriceTariff.CANCELLATION_CHARGE.ppsrId())
                                    .with(PriceType.ONE_TIME)
                                    .withChargePrice(CANCELLATION_CHARGE_PRICE))
                    )
            )
            .with(mockSiteFacade).with(mockProductIdentifierFacade)
            .with(pricingConfig)
            .build();
    }

    private HSSFWorkbook generateBcmExportSheetForDirectUser() {
        withDirectUser();
        return orchestrator.renderBcmExportSheet(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID);
    }

    private HSSFWorkbook generateBcmExportSheetForIndirectUser() {
        withIndirectUser();
        return orchestrator.renderBcmExportSheet(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID);
    }

    private void withDirectUser() {
        UserContextManager.setCurrent(aDirectUserContext().build());
    }

    private void withIndirectUser() {
        UserContextManager.setCurrent(anIndirectUserContext().build());
    }
}