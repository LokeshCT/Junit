package com.bt.rsqe.projectengine.web.quoteoption;

import com.bt.rsqe.Percentage;
import com.bt.rsqe.client.Pmr;
import com.bt.rsqe.customerinventory.dto.PriceLineDTO;
import com.bt.rsqe.domain.SalesCatalogue;
import com.bt.rsqe.domain.product.parameters.ProductIdentifier;
import com.bt.rsqe.enums.PriceCategory;
import com.bt.rsqe.pmr.client.PmrClient;
import com.bt.rsqe.projectengine.LineItemDiscountStatus;
import com.bt.rsqe.projectengine.QuoteOptionItemDTO;
import com.bt.rsqe.domain.QuoteOptionItemStatus;
import com.bt.rsqe.projectengine.web.model.OneVoicePriceTariff;
import com.bt.rsqe.projectengine.web.model.PriceLineModel;
import com.bt.rsqe.projectengine.web.model.bcmspreadsheet.BcmSpreadSheet;
import com.bt.rsqe.projectengine.web.model.bcmspreadsheet.OneVoiceBcmOptionsSheet;
import com.bt.rsqe.projectengine.web.model.bcmspreadsheet.OneVoiceChannelInformationRow;
import com.bt.rsqe.projectengine.web.model.bcmspreadsheet.OneVoiceChannelInformationSheet;
import com.bt.rsqe.projectengine.web.model.bcmspreadsheet.ProductSheet;
import com.bt.rsqe.projectengine.web.model.bcmspreadsheet.ProductSheetRow;
import com.bt.rsqe.projectengine.web.quoteoption.bcmsheet.HeaderRowModelFactoryTest;
import com.bt.rsqe.quoteengine.domain.Contract;
import com.bt.rsqe.quoteengine.domain.PriceBook;
import com.bt.rsqe.quoteengine.domain.builder.LineItemFixture;
import com.bt.rsqe.quoteengine.rest.LineItemDtoMapper;
import com.bt.rsqe.security.UserContext;
import com.bt.rsqe.security.UserContextManager;
import com.google.common.base.Optional;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static com.bt.rsqe.enums.PriceType.*;
import static com.bt.rsqe.projectengine.LineItemValidationResultDTO.*;
import static com.bt.rsqe.domain.QuoteOptionItemStatus.*;
import static com.bt.rsqe.projectengine.web.fixtures.PriceLineDTOFixture.*;
import static com.bt.rsqe.projectengine.web.model.OneVoicePriceTariff.*;
import static com.bt.rsqe.security.UserContextBuilder.*;
import static com.bt.rsqe.utils.Channels.*;
import static com.google.common.collect.Lists.*;
import static org.mockito.Mockito.*;

public class QuoteOptionBcmPricingSheetOrchestratorChannelBasedTest {
    private static final String CUSTOMER_ID = "CUSTOMER_ID";
    private static final String CONTRACT_ID = "CONTRACT_ID";
    private static final String PROJECT_ID = "PROJECT_ID";
    private static final String QUOTE_OPTION_ID = "QUOTE_OPTION_ID";
    private static final String SITE_ID = "123";
    private static final int DEFAULT_DISCOUNT = 0;
    private static final double CONFIG_CHARGE_PRICE = 1.1;
    private static final int CONFIG_CHARGE_DISCOUNT = 10;
    private static final double CONFIG_EUP_PRICE = 1.2;
    private static final double SUBSCRIPTION_CHARGE_PRICE = 1.3;
    private static final int SUBSCRIPTION_CHARGE_DISCOUNT = 20;
    private static final double SUBSCRIPTION_EUP_PRICE = 1.4;

    private BcmSpreadSheet oneVoiceBcmSpreadsheet;
    private OneVoiceChannelInformationSheet sheet;
    private final QuoteOptionBcmPricingSheetOrchestratorFixture fixture = new QuoteOptionBcmPricingSheetOrchestratorFixture();
    UserContext userContext;
    private SalesCatalogue salesCatalogue;
    private Pmr.ProductOfferings caSiteProductOfferings;
    private Pmr.ProductOfferings caServiceProductOfferings;
    private Pmr.ProductOfferings specialBidProductOfferings;
    private Pmr.ProductOfferings onevoiceproductOfferings;
    private List<ProductIdentifier> productIdentifiers = newArrayList();
    private HeaderRowModelFactoryTest headerRowModelFactoryTest;
    private PmrClient pmrClient;

    @Before
    public void before() throws Exception {
        oneVoiceBcmSpreadsheet = mock(BcmSpreadSheet.class);
        sheet = mock(OneVoiceChannelInformationSheet.class);
        when(oneVoiceBcmSpreadsheet.getOneVoiceChannelInformationSheet()).thenReturn(sheet);
        OneVoiceBcmOptionsSheet options = mock(OneVoiceBcmOptionsSheet.class);
        when(options.containsSiteId(any(String.class))).thenReturn(false);
        when(oneVoiceBcmSpreadsheet.getOneVoiceOptionsSheet()).thenReturn(options);
        final ProductSheet mockProductSheet = mock(ProductSheet.class);
        when(oneVoiceBcmSpreadsheet.getSpecialBidServiceSheet()).thenReturn(mockProductSheet);
        when(mockProductSheet.getOneTimeRowFor(any(PriceLineModel.class))).thenReturn(Optional.<ProductSheetRow>absent());
        userContext = anIndirectUserContext().withIndirectUser().build();
        UserContextManager.setCurrent(userContext);
        salesCatalogue = mock(SalesCatalogue.class);
        caSiteProductOfferings = mock(Pmr.ProductOfferings.class);
        caServiceProductOfferings = mock(Pmr.ProductOfferings.class);
        specialBidProductOfferings = mock(Pmr.ProductOfferings.class);
        onevoiceproductOfferings = mock(Pmr.ProductOfferings.class);
        pmrClient = mock(PmrClient.class);
        when(oneVoiceBcmSpreadsheet.getCASiteSheet(any(String.class))).thenReturn(mockProductSheet);
        when(oneVoiceBcmSpreadsheet.getCAServiceSheet(any(String.class))).thenReturn(mockProductSheet);
        headerRowModelFactoryTest = new HeaderRowModelFactoryTest();
        productIdentifiers = getRootProductIdentifiers();
    }
    private List<ProductIdentifier> getRootProductIdentifiers() {
        List<ProductIdentifier> productIdentifiers = new ArrayList<ProductIdentifier>();
        productIdentifiers.add(new ProductIdentifier("S0308454","Connect Acceleration Site", "1.0","CA"));
        productIdentifiers.add(new ProductIdentifier("S0308491","Connect Acceleration Service", "1.0","CA"));
        productIdentifiers.add(new ProductIdentifier("specialScode", "Special Bid"));
        return productIdentifiers;
    }

    @Test
    public void shouldImportDirectTariffDiscountsForIndirectChannel() throws Exception {
        fixture.forIndirectChannel();
        fixture.withPmr(pmrClient)
               .withSalesCatalogue(salesCatalogue)
               .withProductIdentifiers(productIdentifiers)
               .withCaSiteProductOfferings(caSiteProductOfferings)
               .withHeaderRowModelFactoryTest(headerRowModelFactoryTest)
               .withCaServiceProductOfferings(caServiceProductOfferings)
               .withSpecialBidProductOfferings(specialBidProductOfferings)
               .withOneVoiceProductOfferings(onevoiceproductOfferings)
               .setExpectationsForPmr();

        final String lineItemId = "lineItemId";
        createLineItem(lineItemId, QuoteOptionItemStatus.OFFERED, createDirectTariffPricesModel());
        useRow(Percentage.from(CONFIG_CHARGE_DISCOUNT), Percentage.from(SUBSCRIPTION_CHARGE_DISCOUNT));

        fixture.build().importBCMData(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID, oneVoiceBcmSpreadsheet);

        final DiscountAndPriceAssertionVisitor assertionVisitor = new DiscountAndPriceAssertionVisitor.Builder()
            .withDiscount(GLOBAL_DIRECT_SINGLE_CHANNEL_CONFIG.ppsrId(), PriceCategory.CHARGE_PRICE, Percentage.from(CONFIG_CHARGE_DISCOUNT))
            .withDiscount(OneVoicePriceTariff.GLOBAL_DIRECT_SINGLE_CHANNEL_SUBSCRIPTION.ppsrId(), PriceCategory.CHARGE_PRICE, Percentage.from(SUBSCRIPTION_CHARGE_DISCOUNT))
            .withDiscount(GLOBAL_DIRECT_SINGLE_CHANNEL_CONFIG.ppsrId(), PriceCategory.END_USER_PRICE, Percentage.from(DEFAULT_DISCOUNT))
            .withDiscount(OneVoicePriceTariff.GLOBAL_DIRECT_SINGLE_CHANNEL_SUBSCRIPTION.ppsrId(), PriceCategory.END_USER_PRICE, Percentage.from(DEFAULT_DISCOUNT))
            .build();

        verifyUpdatedPrices(lineItemId, assertionVisitor);
    }

    @Test
    public void shouldImportInclusiveTariffDiscountsForIndirectChannel() throws Exception {
        fixture.forIndirectChannel();
        fixture.withPmr(pmrClient)
               .withSalesCatalogue(salesCatalogue)
               .withProductIdentifiers(productIdentifiers)
               .withCaSiteProductOfferings(caSiteProductOfferings)
               .withHeaderRowModelFactoryTest(headerRowModelFactoryTest)
               .withCaServiceProductOfferings(caServiceProductOfferings)
               .withSpecialBidProductOfferings(specialBidProductOfferings)
               .withOneVoiceProductOfferings(onevoiceproductOfferings)
               .setExpectationsForPmr();

        final String lineItemId = "lineItemId";
        createLineItem(lineItemId, QuoteOptionItemStatus.OFFERED, createInclusiveTariffPricesModel());
        useRow(Percentage.from(CONFIG_CHARGE_DISCOUNT), Percentage.from(SUBSCRIPTION_CHARGE_DISCOUNT));

        fixture.build().importBCMData(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID, oneVoiceBcmSpreadsheet);

        final DiscountAndPriceAssertionVisitor assertionVisitor = new DiscountAndPriceAssertionVisitor.Builder()
            .withDiscount(OneVoicePriceTariff.GLOBAL_INCLUSIVE_SINGLE_CHANNEL_CONFIG.ppsrId(), PriceCategory.CHARGE_PRICE, Percentage.from(CONFIG_CHARGE_DISCOUNT))
            .withDiscount(OneVoicePriceTariff.GLOBAL_INCLUSIVE_SINGLE_CHANNEL_SUBSCRIPTION.ppsrId(), PriceCategory.CHARGE_PRICE, Percentage.from(SUBSCRIPTION_CHARGE_DISCOUNT))
            .withDiscount(OneVoicePriceTariff.GLOBAL_INCLUSIVE_SINGLE_CHANNEL_CONFIG.ppsrId(), PriceCategory.END_USER_PRICE, Percentage.from(DEFAULT_DISCOUNT))
            .withDiscount(OneVoicePriceTariff.GLOBAL_INCLUSIVE_SINGLE_CHANNEL_SUBSCRIPTION.ppsrId(), PriceCategory.END_USER_PRICE, Percentage.from(DEFAULT_DISCOUNT))
            .build();

        verifyUpdatedPrices(lineItemId, assertionVisitor);
    }

    @Test
    public void shouldImportIndirectTariffDiscountsForIndirectChannel() throws Exception {
        fixture.forIndirectChannel();
        fixture.withPmr(pmrClient)
               .withSalesCatalogue(salesCatalogue)
               .withProductIdentifiers(productIdentifiers)
               .withCaSiteProductOfferings(caSiteProductOfferings)
               .withHeaderRowModelFactoryTest(headerRowModelFactoryTest)
               .withCaServiceProductOfferings(caServiceProductOfferings)
               .withSpecialBidProductOfferings(specialBidProductOfferings)
               .withOneVoiceProductOfferings(onevoiceproductOfferings)
               .setExpectationsForPmr();

        final String lineItemId = "lineItemId";
        createLineItem(lineItemId, QuoteOptionItemStatus.OFFERED, createDirectLiteTariffPricesModel());
        useRow(Percentage.from(CONFIG_CHARGE_DISCOUNT), Percentage.from(SUBSCRIPTION_CHARGE_DISCOUNT));

        fixture.build().importBCMData(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID, oneVoiceBcmSpreadsheet);

        final DiscountAndPriceAssertionVisitor assertionVisitor = new DiscountAndPriceAssertionVisitor.Builder()
            .withDiscount(OneVoicePriceTariff.GLOBAL_DIRECT_LITE_CHANNEL_CONFIG.ppsrId(), PriceCategory.CHARGE_PRICE, Percentage.from(CONFIG_CHARGE_DISCOUNT))
            .withDiscount(OneVoicePriceTariff.GLOBAL_DIRECT_LITE_CHANNEL_SUBSCRIPTION.ppsrId(), PriceCategory.CHARGE_PRICE, Percentage.from(SUBSCRIPTION_CHARGE_DISCOUNT))
            .withDiscount(OneVoicePriceTariff.GLOBAL_DIRECT_LITE_CHANNEL_CONFIG.ppsrId(), PriceCategory.END_USER_PRICE, Percentage.from(DEFAULT_DISCOUNT))
            .withDiscount(OneVoicePriceTariff.GLOBAL_DIRECT_LITE_CHANNEL_SUBSCRIPTION.ppsrId(), PriceCategory.END_USER_PRICE, Percentage.from(DEFAULT_DISCOUNT))
            .build();

        verifyUpdatedPrices(lineItemId, assertionVisitor);
    }

    @Test
    public void shouldImportDirectTariffDiscountsForDirectChannel() throws Exception {
        fixture.forDirectChannel();
        fixture.withPmr(pmrClient)
               .withSalesCatalogue(salesCatalogue)
               .withProductIdentifiers(productIdentifiers)
               .withCaSiteProductOfferings(caSiteProductOfferings)
               .withHeaderRowModelFactoryTest(headerRowModelFactoryTest)
               .withCaServiceProductOfferings(caServiceProductOfferings)
               .withSpecialBidProductOfferings(specialBidProductOfferings)
               .withOneVoiceProductOfferings(onevoiceproductOfferings)
               .setExpectationsForPmr();

        final String lineItemId = "lineItemId";
        createLineItem(lineItemId, QuoteOptionItemStatus.OFFERED, createDirectTariffPricesModel());
        useRow(Percentage.from(CONFIG_CHARGE_DISCOUNT), Percentage.from(SUBSCRIPTION_CHARGE_DISCOUNT));

        fixture.build().importBCMData(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID, oneVoiceBcmSpreadsheet);

        final DiscountAndPriceAssertionVisitor assertionVisitor = new DiscountAndPriceAssertionVisitor.Builder()
            .withDiscount(OneVoicePriceTariff.GLOBAL_DIRECT_SINGLE_CHANNEL_CONFIG.ppsrId(), PriceCategory.CHARGE_PRICE, Percentage.from(CONFIG_CHARGE_DISCOUNT))
            .withDiscount(OneVoicePriceTariff.GLOBAL_DIRECT_SINGLE_CHANNEL_SUBSCRIPTION.ppsrId(), PriceCategory.CHARGE_PRICE, Percentage.from(SUBSCRIPTION_CHARGE_DISCOUNT))
            .withDiscount(OneVoicePriceTariff.GLOBAL_DIRECT_SINGLE_CHANNEL_CONFIG.ppsrId(), PriceCategory.END_USER_PRICE, Percentage.from(DEFAULT_DISCOUNT))
            .withDiscount(OneVoicePriceTariff.GLOBAL_DIRECT_SINGLE_CHANNEL_SUBSCRIPTION.ppsrId(), PriceCategory.END_USER_PRICE, Percentage.from(DEFAULT_DISCOUNT))
            .build();

        verifyUpdatedPrices(lineItemId, assertionVisitor);
    }

    @Test
    public void shouldImportInclusiveTariffDiscountsForDirectChannel() throws Exception {
        fixture.forDirectChannel();
        fixture.withPmr(pmrClient)
               .withSalesCatalogue(salesCatalogue)
               .withProductIdentifiers(productIdentifiers)
               .withCaSiteProductOfferings(caSiteProductOfferings)
               .withHeaderRowModelFactoryTest(headerRowModelFactoryTest)
               .withCaServiceProductOfferings(caServiceProductOfferings)
               .withSpecialBidProductOfferings(specialBidProductOfferings)
               .withOneVoiceProductOfferings(onevoiceproductOfferings)
               .setExpectationsForPmr();

        final String lineItemId = "lineItemId";
        createLineItem(lineItemId, QuoteOptionItemStatus.OFFERED, createInclusiveTariffPricesModel());
        useRow(Percentage.from(CONFIG_CHARGE_DISCOUNT), Percentage.from(SUBSCRIPTION_CHARGE_DISCOUNT));

        fixture.build().importBCMData(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID, oneVoiceBcmSpreadsheet);

        final DiscountAndPriceAssertionVisitor assertionVisitor = new DiscountAndPriceAssertionVisitor.Builder()
            .withDiscount(OneVoicePriceTariff.GLOBAL_INCLUSIVE_SINGLE_CHANNEL_CONFIG.ppsrId(), PriceCategory.CHARGE_PRICE, Percentage.from(CONFIG_CHARGE_DISCOUNT))
            .withDiscount(OneVoicePriceTariff.GLOBAL_INCLUSIVE_SINGLE_CHANNEL_SUBSCRIPTION.ppsrId(), PriceCategory.CHARGE_PRICE, Percentage.from(SUBSCRIPTION_CHARGE_DISCOUNT))
            .withDiscount(OneVoicePriceTariff.GLOBAL_INCLUSIVE_SINGLE_CHANNEL_CONFIG.ppsrId(), PriceCategory.END_USER_PRICE, Percentage.from(DEFAULT_DISCOUNT))
            .withDiscount(OneVoicePriceTariff.GLOBAL_INCLUSIVE_SINGLE_CHANNEL_SUBSCRIPTION.ppsrId(), PriceCategory.END_USER_PRICE, Percentage.from(DEFAULT_DISCOUNT))
            .build();

        verifyUpdatedPrices(lineItemId, assertionVisitor);
    }

    @Test
    public void shouldImportIndirectTariffDiscountsForDirectChannel() throws Exception {
        fixture.forDirectChannel();
        fixture.withPmr(pmrClient)
               .withSalesCatalogue(salesCatalogue)
               .withProductIdentifiers(productIdentifiers)
               .withCaSiteProductOfferings(caSiteProductOfferings)
               .withHeaderRowModelFactoryTest(headerRowModelFactoryTest)
               .withCaServiceProductOfferings(caServiceProductOfferings)
               .withSpecialBidProductOfferings(specialBidProductOfferings)
               .withOneVoiceProductOfferings(onevoiceproductOfferings)
               .setExpectationsForPmr();

        final String lineItemId = "lineItemId";
        createLineItem(lineItemId, QuoteOptionItemStatus.OFFERED, createDirectLiteTariffPricesModel());
        useRow(Percentage.from(CONFIG_CHARGE_DISCOUNT), Percentage.from(SUBSCRIPTION_CHARGE_DISCOUNT));

        fixture.build().importBCMData(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID, oneVoiceBcmSpreadsheet);

        final DiscountAndPriceAssertionVisitor assertionVisitor = new DiscountAndPriceAssertionVisitor.Builder()
            .withDiscount(OneVoicePriceTariff.GLOBAL_DIRECT_LITE_CHANNEL_CONFIG.ppsrId(), PriceCategory.CHARGE_PRICE, Percentage.from(CONFIG_CHARGE_DISCOUNT))
            .withDiscount(OneVoicePriceTariff.GLOBAL_DIRECT_LITE_CHANNEL_SUBSCRIPTION.ppsrId(), PriceCategory.CHARGE_PRICE, Percentage.from(SUBSCRIPTION_CHARGE_DISCOUNT))
            .withDiscount(OneVoicePriceTariff.GLOBAL_DIRECT_LITE_CHANNEL_CONFIG.ppsrId(), PriceCategory.END_USER_PRICE, Percentage.from(DEFAULT_DISCOUNT))
            .withDiscount(OneVoicePriceTariff.GLOBAL_DIRECT_LITE_CHANNEL_SUBSCRIPTION.ppsrId(), PriceCategory.END_USER_PRICE, Percentage.from(DEFAULT_DISCOUNT))
            .build();

        verifyUpdatedPrices(lineItemId, assertionVisitor);
    }

    @Test
    public void shouldNotImportDiscountForLineItemsThatAreCustomerApprovedOrOrdered() throws Exception {
        fixture.withPmr(pmrClient)
               .withSalesCatalogue(salesCatalogue)
               .withProductIdentifiers(productIdentifiers)
               .withCaSiteProductOfferings(caSiteProductOfferings)
               .withHeaderRowModelFactoryTest(headerRowModelFactoryTest)
               .withCaServiceProductOfferings(caServiceProductOfferings)
               .withSpecialBidProductOfferings(specialBidProductOfferings)
               .withOneVoiceProductOfferings(onevoiceproductOfferings)
               .setExpectationsForPmr();

        final String approvedId = "approved";
        final String orderCreatedId = "orderCreated";
        final String orderSubmittedId = "orderSubmitted";
        createLineItem(approvedId, CUSTOMER_APPROVED, createDirectTariffPricesModel());
        createLineItem(orderCreatedId, ORDER_CREATED, createDirectTariffPricesModel());
        createLineItem(orderSubmittedId, ORDER_SUBMITTED, createDirectTariffPricesModel());
        useRow(Percentage.from(CONFIG_CHARGE_DISCOUNT), Percentage.from(SUBSCRIPTION_CHARGE_DISCOUNT));

        fixture.build().importBCMData(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID, oneVoiceBcmSpreadsheet);

        final DiscountAndPriceAssertionVisitor assertionVisitor = new DiscountAndPriceAssertionVisitor.Builder()
            .withDiscount(GLOBAL_DIRECT_SINGLE_CHANNEL_CONFIG.ppsrId(), PriceCategory.CHARGE_PRICE, Percentage.from(DEFAULT_DISCOUNT))
            .withDiscount(OneVoicePriceTariff.GLOBAL_DIRECT_SINGLE_CHANNEL_SUBSCRIPTION.ppsrId(), PriceCategory.CHARGE_PRICE, Percentage.from(DEFAULT_DISCOUNT))
            .build();

        verifyUpdatedPrices(approvedId, assertionVisitor);
        verifyUpdatedPrices(orderCreatedId, assertionVisitor);
        verifyUpdatedPrices(orderSubmittedId, assertionVisitor);
    }

    @Test
    public void shouldNotUpdateDiscountsForNonChannelBasedPrices() throws Exception {
        fixture.forIndirectChannel();
        fixture.withPmr(pmrClient)
               .withSalesCatalogue(salesCatalogue)
               .withProductIdentifiers(productIdentifiers)
               .withCaSiteProductOfferings(caSiteProductOfferings)
               .withHeaderRowModelFactoryTest(headerRowModelFactoryTest)
               .withCaServiceProductOfferings(caServiceProductOfferings)
               .withSpecialBidProductOfferings(specialBidProductOfferings)
               .withOneVoiceProductOfferings(onevoiceproductOfferings)
               .setExpectationsForPmr();

        final String lineItemId = "lineItemId";
        createLineItem(lineItemId, QuoteOptionItemStatus.OFFERED, createChannelAndAllNonChannelPriceLines(GLOBAL_DIRECT_SINGLE_CHANNEL_CONFIG, GLOBAL_DIRECT_SINGLE_CHANNEL_SUBSCRIPTION));
        useRow(Percentage.from(CONFIG_CHARGE_DISCOUNT), Percentage.from(SUBSCRIPTION_CHARGE_DISCOUNT));

        fixture.build().importBCMData(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID, oneVoiceBcmSpreadsheet);

        final DiscountAndPriceAssertionVisitor.Builder assertionBuilder = new DiscountAndPriceAssertionVisitor.Builder()
            .withDiscount(OneVoicePriceTariff.GLOBAL_DIRECT_SINGLE_CHANNEL_CONFIG.ppsrId(), PriceCategory.CHARGE_PRICE, Percentage.from(CONFIG_CHARGE_DISCOUNT))
            .withDiscount(OneVoicePriceTariff.GLOBAL_DIRECT_SINGLE_CHANNEL_SUBSCRIPTION.ppsrId(), PriceCategory.CHARGE_PRICE, Percentage.from(SUBSCRIPTION_CHARGE_DISCOUNT));
        for (OneVoicePriceTariff nonChannelBasedTariff : OneVoicePriceTariff.NON_CHANNEL_BASED_PPSR_IDS) {
            assertionBuilder.withDiscount(nonChannelBasedTariff.ppsrId(), PriceCategory.CHARGE_PRICE, Percentage.from(DEFAULT_DISCOUNT));
        }

        verifyUpdatedPrices(lineItemId, assertionBuilder.build());
    }

    private List<PriceLineDTO> createDirectTariffPricesModel() {
        return newArrayList(
            aPriceLineDTO()
                .withPpsrId(GLOBAL_DIRECT_SINGLE_CHANNEL_CONFIG.ppsrId())
                .with(ONE_TIME)
                .withChargePrice(CONFIG_CHARGE_PRICE)
                .withChargePriceDiscount(DEFAULT_DISCOUNT)
                .withEupPrice(CONFIG_EUP_PRICE)
                .withEupPriceDiscount(DEFAULT_DISCOUNT).build(),
            aPriceLineDTO()
                .withPpsrId(OneVoicePriceTariff.GLOBAL_DIRECT_SINGLE_CHANNEL_SUBSCRIPTION.ppsrId())
                .with(RECURRING)
                .withChargePrice(SUBSCRIPTION_CHARGE_PRICE)
                .withChargePriceDiscount(DEFAULT_DISCOUNT)
                .withEupPrice(SUBSCRIPTION_EUP_PRICE)
                .withEupPriceDiscount(DEFAULT_DISCOUNT).build()
        );
    }

    private List<PriceLineDTO> createInclusiveTariffPricesModel() {
        return newArrayList(
            aPriceLineDTO()
                .withPpsrId(OneVoicePriceTariff.GLOBAL_INCLUSIVE_SINGLE_CHANNEL_CONFIG.ppsrId())
                .with(ONE_TIME)
                .withChargePrice(CONFIG_CHARGE_PRICE)
                .withChargePriceDiscount(DEFAULT_DISCOUNT)
                .withEupPrice(CONFIG_EUP_PRICE)
                .withEupPriceDiscount(DEFAULT_DISCOUNT).build(),
            aPriceLineDTO()
                .withPpsrId(OneVoicePriceTariff.GLOBAL_INCLUSIVE_SINGLE_CHANNEL_SUBSCRIPTION.ppsrId())
                .with(RECURRING)
                .withChargePrice(SUBSCRIPTION_CHARGE_PRICE)
                .withChargePriceDiscount(DEFAULT_DISCOUNT)
                .withEupPrice(SUBSCRIPTION_EUP_PRICE)
                .withEupPriceDiscount(DEFAULT_DISCOUNT).build()
        );
    }

    private List<PriceLineDTO> createDirectLiteTariffPricesModel() {
        return newArrayList(
            aPriceLineDTO()
                .withPpsrId(OneVoicePriceTariff.GLOBAL_DIRECT_LITE_CHANNEL_CONFIG.ppsrId())
                .with(ONE_TIME)
                .withChargePrice(CONFIG_CHARGE_PRICE)
                .withChargePriceDiscount(DEFAULT_DISCOUNT)
                .withEupPrice(CONFIG_EUP_PRICE)
                .withEupPriceDiscount(DEFAULT_DISCOUNT).build(),
            aPriceLineDTO()
                .withPpsrId(OneVoicePriceTariff.GLOBAL_DIRECT_LITE_CHANNEL_SUBSCRIPTION.ppsrId())
                .with(RECURRING)
                .withChargePrice(SUBSCRIPTION_CHARGE_PRICE)
                .withChargePriceDiscount(DEFAULT_DISCOUNT)
                .withEupPrice(SUBSCRIPTION_EUP_PRICE)
                .withEupPriceDiscount(DEFAULT_DISCOUNT).build()
        );
    }

    private List<PriceLineDTO> createChannelAndAllNonChannelPriceLines(OneVoicePriceTariff... channelBasedTariffs) {
        final ArrayList<PriceLineDTO> priceLines = newArrayList();
        for (OneVoicePriceTariff nonChannelBasedTariff : OneVoicePriceTariff.NON_CHANNEL_BASED_PPSR_IDS) {
            priceLines.add(aPriceLineDTO()
                               .withPpsrId(nonChannelBasedTariff.ppsrId())
                               .with(nonChannelBasedTariff.priceType())
                               .withChargePrice(CONFIG_CHARGE_PRICE)
                               .withChargePriceDiscount(DEFAULT_DISCOUNT)
                               .withEupPrice(CONFIG_EUP_PRICE)
                               .withEupPriceDiscount(DEFAULT_DISCOUNT).build());
        }
        for (OneVoicePriceTariff channelBasedTariff : channelBasedTariffs) {
            priceLines.add(aPriceLineDTO()
                               .withPpsrId(channelBasedTariff.ppsrId())
                               .with(channelBasedTariff.priceType())
                               .withChargePrice(CONFIG_CHARGE_PRICE)
                               .withChargePriceDiscount(DEFAULT_DISCOUNT)
                               .withEupPrice(CONFIG_EUP_PRICE)
                               .withEupPriceDiscount(DEFAULT_DISCOUNT).build());
        }
        return priceLines;
    }

    private void useRow(Percentage configDiscount, Percentage subscriptionDiscount) {
        final OneVoiceChannelInformationRow row = mock(OneVoiceChannelInformationRow.class);
        if (userCanViewIndirectPrices()) {
            when(row.getPTPConfigDiscount()).thenReturn(configDiscount);
            when(row.getPTPSubscriptionDiscount()).thenReturn(subscriptionDiscount);
        } else {
            when(row.getRRPConfigDiscount()).thenReturn(configDiscount);
            when(row.getRRPSubscriptionDiscount()).thenReturn(subscriptionDiscount);
        }
        when(sheet.containsSiteId(SITE_ID)).thenReturn(true);
        when(sheet.getOneVoiceChannelInformationRow(SITE_ID)).thenReturn(row);
    }

    private void createLineItem(String lineItemId, QuoteOptionItemStatus status, List<PriceLineDTO> prices) {
        final QuoteOptionItemDTO dto = new LineItemDtoMapper().dto(LineItemFixture.aLineItem().withContract(new Contract("id", "12", new PriceBook("1", "someRequestId", "eup", null, null, null))).withStatus(status).with(LineItemDiscountStatus.APPROVAL_REQUESTED).withId(lineItemId).build(), pending());

        fixture.withOneVoiceItem(PROJECT_ID, QUOTE_OPTION_ID, dto);
        fixture.with(lineItemId, SITE_ID, prices);
    }

    private void verifyUpdatedPrices(String lineItemId, DiscountAndPriceAssertionVisitor assertionVisitor) {
        fixture.fetchLineItem(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID, lineItemId).accept(assertionVisitor);
        assertionVisitor.verfifyAllAssertionsSatisfied();
        assertionVisitor.reset();
    }
}
