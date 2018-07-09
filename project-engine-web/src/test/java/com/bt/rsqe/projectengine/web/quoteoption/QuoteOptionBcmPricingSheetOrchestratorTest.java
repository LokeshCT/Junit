package com.bt.rsqe.projectengine.web.quoteoption;

import com.bt.rsqe.Money;
import com.bt.rsqe.Percentage;
import com.bt.rsqe.client.Pmr;
import com.bt.rsqe.customerinventory.client.SpecialPriceBookClient;
import com.bt.rsqe.customerinventory.dto.PriceLineDTO;
import com.bt.rsqe.customerinventory.dto.SpecialPriceBookDTO;
import com.bt.rsqe.domain.SalesCatalogue;
import com.bt.rsqe.domain.bom.fixtures.ProductOfferingFixture;
import com.bt.rsqe.domain.bom.parameters.ProductSCode;
import com.bt.rsqe.domain.bom.parameters.QuoteOptionId;
import com.bt.rsqe.domain.product.SimpleProductOfferingType;
import com.bt.rsqe.domain.product.parameters.ProductIdentifier;
import com.bt.rsqe.domain.project.PricePoint;
import com.bt.rsqe.domain.project.SpecialPriceBook;
import com.bt.rsqe.domain.project.TerminationType;
import com.bt.rsqe.utils.countries.Countries;
import com.bt.rsqe.utils.countries.Country;
import com.bt.rsqe.enums.PriceCategory;
import com.bt.rsqe.enums.ProductCodes;
import com.bt.rsqe.pmr.client.PmrClient;
import com.bt.rsqe.projectengine.LineItemDiscountStatus;
import com.bt.rsqe.projectengine.QuoteOptionItemDTO;
import com.bt.rsqe.projectengine.QuoteOptionItemDTOFixture;
import com.bt.rsqe.projectengine.web.facades.FutureAssetPricesFacade;
import com.bt.rsqe.projectengine.web.facades.LineItemFacade;
import com.bt.rsqe.projectengine.web.fixtures.LineItemModelFixture;
import com.bt.rsqe.projectengine.web.model.LineItemModel;
import com.bt.rsqe.projectengine.web.model.OneVoicePriceTariff;
import com.bt.rsqe.projectengine.web.model.PriceLineModel;
import com.bt.rsqe.projectengine.web.model.bcmspreadsheet.BcmSpreadSheet;
import com.bt.rsqe.projectengine.web.model.bcmspreadsheet.OneVoiceBcmOptionsRow;
import com.bt.rsqe.projectengine.web.model.bcmspreadsheet.OneVoiceBcmOptionsSheet;
import com.bt.rsqe.projectengine.web.model.bcmspreadsheet.OneVoiceChannelInformationSheet;
import com.bt.rsqe.projectengine.web.model.bcmspreadsheet.OneVoiceSpecialPriceBookRow;
import com.bt.rsqe.projectengine.web.model.bcmspreadsheet.OneVoiceSpecialPriceBookSheet;
import com.bt.rsqe.projectengine.web.model.bcmspreadsheet.ProductInfoSheet;
import com.bt.rsqe.projectengine.web.model.bcmspreadsheet.ProductSheet;
import com.bt.rsqe.projectengine.web.model.bcmspreadsheet.ProductSheetRow;
import com.bt.rsqe.projectengine.web.quoteoption.bcmsheet.BCMProductSheetProperty;
import com.bt.rsqe.projectengine.web.quoteoption.bcmsheet.HeaderRowModelFactoryTest;
import com.bt.rsqe.projectengine.web.quoteoption.bcmsheet.ProductSheetStaticColumn;
import com.bt.rsqe.projectengine.web.quoteoption.priceupdater.FutureAssetPriceUpdater;
import com.bt.rsqe.projectengine.web.quoteoption.priceupdater.FutureAssetPriceUpdaterFactory;
import com.bt.rsqe.projectengine.web.quoteoptionpricing.PriceSuppressStrategy;
import com.bt.rsqe.quoteengine.domain.Contract;
import com.bt.rsqe.quoteengine.domain.LineItem;
import com.bt.rsqe.quoteengine.domain.PriceBook;
import com.bt.rsqe.quoteengine.domain.builder.LineItemFixture;
import com.bt.rsqe.quoteengine.rest.LineItemDtoMapper;
import com.bt.rsqe.security.UserContext;
import com.bt.rsqe.security.UserContextManager;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static com.bt.rsqe.enums.PriceType.*;
import static com.bt.rsqe.projectengine.LineItemValidationResultDTO.*;
import static com.bt.rsqe.projectengine.web.fixtures.PriceLineDTOFixture.*;
import static com.bt.rsqe.projectengine.web.model.bcmspreadsheet.OneVoiceSpecialPriceBookRow.*;
import static com.bt.rsqe.security.UserContextBuilder.*;
import static com.google.common.collect.Lists.*;
import static java.util.Arrays.asList;
import static org.apache.poi.ss.usermodel.Cell.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.core.Is.*;
import static org.mockito.Mockito.*;

public class QuoteOptionBcmPricingSheetOrchestratorTest {
    private static final String CUSTOMER_ID = "CUSTOMER_ID";
    private static final String CONTRACT_ID = "CONTRACT_ID";
    private static final String PROJECT_ID = "PROJECT_ID";
    private static final String QUOTE_OPTION_ID = "QUOTE_OPTION_ID";
    private static final String SITE_ID = "123";
    private static final boolean APPROVED = true;
    private static final boolean NOT_APPROVED = false;
    public static final int DEFAULT_DISCOUNT = 0;
    private static final Country SOURCE_COUNTRY = Countries.byIsoStatic("GB");
    private static final String SPECIAL_PRICE_BOOK_NAME = "someSpecialPriceBookName";
    private static final Country DESTINATION_COUNTRY = Countries.byIsoStatic("IN");
    private static final Double NEW_DISCOUNT_VALUE = 10.5;
    private static final double OLD_DISCOUNT_VALUE = 20;
    private static final double DEFAULT_AMENDMENT_CHARGE = 10;
    private static final Money AMENDMENT_CHARGE = Money.from("70");
    private static final Money DEFAULT_CANCELLATION_CHARGE = Money.from("20");
    private static final Money CANCELLATION_CHARGE = Money.from("80");

    private final BcmSpreadSheet mockOneVoiceBcmSpreadsheet = mock(BcmSpreadSheet.class);
    private final QuoteOptionBcmPricingSheetOrchestratorFixture fixture = new QuoteOptionBcmPricingSheetOrchestratorFixture();
    private SpecialPriceBook specialPriceBook = new SpecialPriceBook(new QuoteOptionId(QUOTE_OPTION_ID),
                                                                     SOURCE_COUNTRY, SPECIAL_PRICE_BOOK_NAME,
                                                                     asList(new PricePoint(SOURCE_COUNTRY, DESTINATION_COUNTRY,
                                                                                           TerminationType.MOBILE, "tariff",
                                                                                           Money.from(BigDecimal.valueOf(OLD_DISCOUNT_VALUE)).toBigDecimal(), BigDecimal.TEN)));




    private static final Double VPN_CONFIG_RRP_PRICE = 25.12;
    private static final Double VPN_SUBSCRIPTION_RRP_PRICE = 50.12;
    private static final Double DIAL_PLAN_CHANGE_CONFIG_RRP_PRICE = 10.12;
    private static final Double MAJOR_MOVE_ADD_CHANGE_CONFIG_RRP_PRICE = 20.12;
    private static final Double VPN_CONFIG_PTP_PRICE = 25.12;
    private static final Double VPN_SUBSCRIPTION_PTP_PRICE = 50.12;
    private static final Double DIAL_PLAN_CHANGE_CONFIG_PTP_PRICE = 10.12;
    private static final Double MAJOR_MOVE_ADD_CHANGE_CONFIG_PTP_PRICE = 20.12;
    private static final int VPN_CONFIG_RRP_DISCOUNT_PERCENT = 10;
    private static final int VPN_SUBSCRIPTION_RRP_DISCOUNT_PERCENT = 15;
    private static final int DIAL_PLAN_CHANGE_CONFIG_RRP_DISCOUNT_PERCENT = 12;
    private static final int MAJOR_MOVE_ADD_CHANGE_CONFIG_RRP_DISCOUNT_PERCENT = 20;
    public static final String LINE_ITEM_ID = "lineItemId";
    public static final String LINE_ITEM_ID_2 = "lineItemId2";
    public static final String CA_LINE_ITEM_ID = "caLineItemId";
    UserContext userContext;
    HSSFSheet caServiceSheet;
    private SalesCatalogue salesCatalogue;
    private Pmr.ProductOfferings caSiteProductOfferings;
    private Pmr.ProductOfferings caServiceProductOfferings;
    private Pmr.ProductOfferings specialBidProductOfferings;
    private Pmr.ProductOfferings onevoiceproductOfferings;
    private List<ProductIdentifier> productIdentifiers = newArrayList();
    private HeaderRowModelFactoryTest headerRowModelFactoryTest;
    private PmrClient pmrClient;

    @Before
    public void before() {
        HSSFSheet specialPriceBookSheet = new HSSFWorkbook().createSheet();

        HSSFRow numericRow = specialPriceBookSheet.createRow(1);
        numericRow.createCell(SPECIAL_PRICEBOOK_CELL_INDEX, CELL_TYPE_STRING).setCellValue(SPECIAL_PRICE_BOOK_NAME);
        numericRow.createCell(ORIGINATING_COUNTRY_CELL_INDEX, CELL_TYPE_STRING).setCellValue(SOURCE_COUNTRY.getDisplayName());
        numericRow.createCell(TERMINATING_COUNTRY_CELL_INDEX, CELL_TYPE_STRING).setCellValue(DESTINATION_COUNTRY.getDisplayName());
        numericRow.createCell(TERMINATION_TYPE_CELL_INDEX, CELL_TYPE_STRING).setCellValue(TerminationType.MOBILE.getDisplayName());
        numericRow.createCell(TARIFF_TYPE_CELL_INDEX, CELL_TYPE_STRING).setCellValue("tariff");
        numericRow.createCell(DISCOUNT_CELL_INDEX, CELL_TYPE_NUMERIC).setCellValue(NEW_DISCOUNT_VALUE);


        OneVoiceSpecialPriceBookRow oneVoiceSpecialPriceBookRow = new OneVoiceSpecialPriceBookRow(specialPriceBookSheet.getRow(1));

        OneVoiceChannelInformationSheet mockChannelInformationSheet = mock(OneVoiceChannelInformationSheet.class);
        OneVoiceSpecialPriceBookSheet mockOneVoiceSpecialPriceBookSheet = mock(OneVoiceSpecialPriceBookSheet.class);
        when(mockChannelInformationSheet.containsSiteId(any(String.class))).thenReturn(false);
        when(mockOneVoiceBcmSpreadsheet.getOneVoiceChannelInformationSheet()).thenReturn(mockChannelInformationSheet);
        when(mockOneVoiceBcmSpreadsheet.getSpecialPriceBookSheet()).thenReturn(mockOneVoiceSpecialPriceBookSheet);
        when(mockOneVoiceSpecialPriceBookSheet.getSpecialPriceBookFor(SPECIAL_PRICE_BOOK_NAME, SOURCE_COUNTRY.getDisplayName())).thenReturn(
            Lists.<OneVoiceSpecialPriceBookRow>newArrayList(oneVoiceSpecialPriceBookRow)
        );
        ProductInfoSheet mockProductInfoSheet = mock(ProductInfoSheet.class);
        when(mockOneVoiceBcmSpreadsheet.getProductInfoSheet()).thenReturn(mockProductInfoSheet);
        salesCatalogue = mock(SalesCatalogue.class);
        caSiteProductOfferings = mock(Pmr.ProductOfferings.class);
        caServiceProductOfferings = mock(Pmr.ProductOfferings.class);
        specialBidProductOfferings = mock(Pmr.ProductOfferings.class);
        onevoiceproductOfferings = mock(Pmr.ProductOfferings.class);
        pmrClient = mock(PmrClient.class);
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

    private void createCASheet() {
        caServiceSheet = new HSSFWorkbook().createSheet(BCMProductSheetProperty.SiteAgnostic.sheetName);
        HSSFRow caServiceSheetRow = caServiceSheet.createRow(1);
        caServiceSheetRow.createCell(ProductSheetStaticColumn.ONE_TIME_PRICE_LINE_ID.columnIndex, CELL_TYPE_STRING).setCellValue("123");
        caServiceSheetRow.createCell(ProductSheetStaticColumn.ONE_TIME_DISCOUNT.columnIndex, CELL_TYPE_NUMERIC).setCellValue(0.10);
        caServiceSheetRow.createCell(ProductSheetStaticColumn.MONTHLY_RECURRING_PRICE_LINE_ID.columnIndex, CELL_TYPE_STRING).setCellValue("124");
        caServiceSheetRow.createCell(ProductSheetStaticColumn.MONTHLY_RECURRING_DISCOUNT.columnIndex, CELL_TYPE_NUMERIC).setCellValue(0.10);
    }

    public void  setCellStringValueInCASheetForOneTimeDiscount()throws Exception{
        caServiceSheet = new HSSFWorkbook().createSheet(BCMProductSheetProperty.SiteAgnostic.sheetName);
        HSSFRow caServiceSheetRow = caServiceSheet.createRow(1);
        caServiceSheetRow.createCell(ProductSheetStaticColumn.ONE_TIME_PRICE_LINE_ID.columnIndex, CELL_TYPE_STRING).setCellValue("123");
        caServiceSheetRow.createCell(ProductSheetStaticColumn.ONE_TIME_DISCOUNT.columnIndex, CELL_TYPE_NUMERIC).setCellValue(0.10);
        caServiceSheetRow.createCell(ProductSheetStaticColumn.MONTHLY_RECURRING_PRICE_LINE_ID.columnIndex, CELL_TYPE_STRING);


    }

    public void setCellStringValueInCASheet() throws Exception{
        caServiceSheet = new HSSFWorkbook().createSheet(BCMProductSheetProperty.SiteAgnostic.sheetName);
        HSSFRow caServiceSheetRow = caServiceSheet.createRow(1);
        caServiceSheetRow.createCell(ProductSheetStaticColumn.ONE_TIME_PRICE_LINE_ID.columnIndex, CELL_TYPE_STRING).setCellValue("123");
        caServiceSheetRow.createCell(ProductSheetStaticColumn.ONE_TIME_DISCOUNT.columnIndex, CELL_TYPE_STRING).setCellValue("");
        caServiceSheetRow.createCell(ProductSheetStaticColumn.MONTHLY_RECURRING_PRICE_LINE_ID.columnIndex, CELL_TYPE_STRING).setCellValue("124");
    }

    @Test
    public void shouldImportPricingSheetWithApprovedDiscounts() throws Exception {
        createCASheet();
        assertExpectationSatisfiedForImportingPricingSheet();
        final LineItemDiscountStatus discountStatus = LineItemDiscountStatus.fromDescription(fixture.fetchLineItem(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID, LINE_ITEM_ID).getDiscountStatus());
        assertThat(discountStatus, is(LineItemDiscountStatus.APPROVED));
    }

    @Test
    public void shouldImportPricingSheetWithStringCellTypeInSheet() throws Exception {
        setCellStringValueInCASheet();
        assertExpectationSatisfiedForImportingPricingSheetForCellTypeString();
        final LineItemDiscountStatus discountStatus = LineItemDiscountStatus.fromDescription(fixture.fetchLineItem(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID, LINE_ITEM_ID).getDiscountStatus());
        assertThat(discountStatus, is(LineItemDiscountStatus.APPROVED));
    }

    @Test
    public void shouldImportPricingSheetForOneTimeDiscount() throws Exception {
        setCellStringValueInCASheetForOneTimeDiscount();
        assertExpectationSatisfiedForImportingPricingSheetForOneTimeDiscount();
        final LineItemDiscountStatus discountStatus = LineItemDiscountStatus.fromDescription(fixture.fetchLineItem(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID, LINE_ITEM_ID).getDiscountStatus());
        assertThat(discountStatus, is(LineItemDiscountStatus.APPROVED));
    }

    @Test
    public void shouldRejectDiscounts() throws Exception {
        createCASheet();
        final String lineItemId = "lineItemId";

        fixture.withOneVoiceItem(PROJECT_ID, QUOTE_OPTION_ID, dto(LineItemFixture.aLineItem().withContract(new Contract("id", "12", new PriceBook("1", "someRequestId", "eup", null, null, null))).with(LineItemDiscountStatus.APPROVAL_REQUESTED).withId(lineItemId).build()));
        fixture.with(lineItemId, SITE_ID, createPricesModel());

        fixture.build().rejectDiscounts(PROJECT_ID, QUOTE_OPTION_ID);

        final LineItemDiscountStatus status = LineItemDiscountStatus.fromDescription(fixture.fetchLineItem(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID, lineItemId).getDiscountStatus());
        assertThat(status, is(LineItemDiscountStatus.REJECTED));
    }

    @Test
    public void shouldImportExcelSpreadsheetForContractLineItem() throws Exception {
        LineItemFacade lineItemFacade = mock(LineItemFacade.class);
        LineItemModel contractLineItem = LineItemModelFixture.aLineItemModel().with(QuoteOptionItemDTOFixture.aQuoteOptionItemDTO()
                                                                                                             .withDiscountStatus(LineItemDiscountStatus.APPROVAL_REQUESTED)
                                                                                                             .withSCode(ProductCodes.OneCloudCiscoContract.productCode())).build();
        when(lineItemFacade.fetchLineItems(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID, PriceSuppressStrategy.None)).thenReturn(newArrayList(contractLineItem));

        PmrClient pmrClient = mock(PmrClient.class);
        SalesCatalogue salesCatalogue = mock(SalesCatalogue.class);
        when(pmrClient.getSalesCatalogue()).thenReturn(salesCatalogue);
        when(salesCatalogue.getAllSellableProductIdentifiers()).thenReturn(newArrayList(new ProductIdentifier(ProductCodes.OneCloudCiscoContract.productCode(), ProductCodes.OneCloudCiscoContract.productName(), "1.0")));
        Pmr.ProductOfferings productOfferings = mock(Pmr.ProductOfferings.class);
        when(pmrClient.productOffering(ProductSCode.newInstance(ProductCodes.OneCloudCiscoContract.productCode()))).thenReturn(productOfferings);
        when(productOfferings.get()).thenReturn(ProductOfferingFixture.aProductOffering().withSimpleProductOfferingType(SimpleProductOfferingType.Contract).build());
        when(pmrClient.getProductHCode(ProductCodes.OneCloudCiscoContract.productCode())).thenReturn(Optional.of(new ProductIdentifier("H1", "Contract Family", "1.0")));

        ProductSheet productSheet = mock(ProductSheet.class);

        BcmSpreadSheet bcmSpreadSheet = mock(BcmSpreadSheet.class);
        when(bcmSpreadSheet.getSheet("Contract Family", BCMProductSheetProperty.Contract)).thenReturn(productSheet);

        FutureAssetPriceUpdater priceUpdater = mock(FutureAssetPriceUpdater.class);

        FutureAssetPriceUpdaterFactory updaterFactory = mock(FutureAssetPriceUpdaterFactory.class);
        when(updaterFactory.updaterFor(productSheet)).thenReturn(priceUpdater);

        FutureAssetPricesFacade futureAssetPricesFacade = mock(FutureAssetPricesFacade.class);

        SpecialPriceBookClient specialPriceBookClient = mock(SpecialPriceBookClient.class);
        when(specialPriceBookClient.get(new com.bt.rsqe.customerinventory.parameter.QuoteOptionId(QUOTE_OPTION_ID))).thenReturn(Lists.<SpecialPriceBook>newArrayList());

        QuoteOptionBcmPricingSheetOrchestrator orchestrator = new QuoteOptionBcmPricingSheetOrchestrator(futureAssetPricesFacade, lineItemFacade, specialPriceBookClient, updaterFactory, pmrClient);
        orchestrator.importBCMData(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID, bcmSpreadSheet);

        verify(priceUpdater).update(contractLineItem);
        verify(futureAssetPricesFacade).save(contractLineItem.getFutureAssetPricesModel());
    }

    private QuoteOptionItemDTO dto(LineItem item) {
        return new LineItemDtoMapper().dto(item, pending());
    }

    private void assertExpectationSatisfiedForImportingPricingSheet() throws Exception {

        createLineItems();

        final DiscountAndPriceAssertionVisitor assertionVisitor = createDiscountAndPriceVisitor();

        expectationsForBcmSheet();
        fixture.withPmr(pmrClient)
               .withSalesCatalogue(salesCatalogue)
               .withProductIdentifiers(productIdentifiers)
               .withCaSiteProductOfferings(caSiteProductOfferings)
               .withHeaderRowModelFactoryTest(headerRowModelFactoryTest)
               .withCaServiceProductOfferings(caServiceProductOfferings)
               .withSpecialBidProductOfferings(specialBidProductOfferings)
               .withOneVoiceProductOfferings(onevoiceproductOfferings)
               .setExpectationsForPmr();
        fixture.build().importBCMData(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID, mockOneVoiceBcmSpreadsheet);

        fixture.fetchLineItem(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID, LINE_ITEM_ID).accept(assertionVisitor);
        fixture.fetchLineItem(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID, CA_LINE_ITEM_ID).accept(assertionVisitor);
        assertionVisitor.verfifyAllAssertionsSatisfied();
        final SpecialPriceBookDTO priceBook = newArrayList(fixture.priceBooks().values()).get(0);
        assertThat(priceBook.getPricePoints().get(0).getDiscountValue(), is(Money.from("10.5").toBigDecimal()));
    }

    private void assertExpectationSatisfiedForImportingPricingSheetForCellTypeString() throws Exception {

        createLineItems();

        final DiscountAndPriceAssertionVisitor assertionVisitor = createDiscountAndPriceVisitorForCA();

        expectationsForBcmSheet();
        fixture.withPmr(pmrClient)
               .withSalesCatalogue(salesCatalogue)
               .withProductIdentifiers(productIdentifiers)
               .withCaSiteProductOfferings(caSiteProductOfferings)
               .withHeaderRowModelFactoryTest(headerRowModelFactoryTest)
               .withCaServiceProductOfferings(caServiceProductOfferings)
               .withSpecialBidProductOfferings(specialBidProductOfferings)
               .withOneVoiceProductOfferings(onevoiceproductOfferings)
               .setExpectationsForPmr();
        fixture.build().importBCMData(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID, mockOneVoiceBcmSpreadsheet);

        fixture.fetchLineItem(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID, LINE_ITEM_ID).accept(assertionVisitor);
        fixture.fetchLineItem(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID, CA_LINE_ITEM_ID).accept(assertionVisitor);
        assertionVisitor.verfifyAllAssertionsSatisfied();
        final SpecialPriceBookDTO priceBook = newArrayList(fixture.priceBooks().values()).get(0);
        assertThat(priceBook.getPricePoints().get(0).getDiscountValue(), is(Money.from("10.5").toBigDecimal()));
    }

    private void assertExpectationSatisfiedForImportingPricingSheetForOneTimeDiscount() throws Exception {

        createLineItems();
        final DiscountAndPriceAssertionVisitor assertionVisitor = createDiscountAndPriceVisitor();
        expectationsForBcmSheet();
        fixture.withPmr(pmrClient)
               .withSalesCatalogue(salesCatalogue)
               .withProductIdentifiers(productIdentifiers)
               .withCaSiteProductOfferings(caSiteProductOfferings)
               .withHeaderRowModelFactoryTest(headerRowModelFactoryTest)
               .withCaServiceProductOfferings(caServiceProductOfferings)
               .withSpecialBidProductOfferings(specialBidProductOfferings)
               .withOneVoiceProductOfferings(onevoiceproductOfferings)
               .setExpectationsForPmr();
        fixture.build().importBCMData(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID, mockOneVoiceBcmSpreadsheet);

        fixture.fetchLineItem(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID, LINE_ITEM_ID).accept(assertionVisitor);
        fixture.fetchLineItem(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID, CA_LINE_ITEM_ID).accept(assertionVisitor);
        assertionVisitor.verfifyAllAssertionsSatisfied();
        final SpecialPriceBookDTO priceBook = newArrayList(fixture.priceBooks().values()).get(0);
        assertThat(priceBook.getPricePoints().get(0).getDiscountValue(), is(Money.from("10.5").toBigDecimal()));
    }

    private DiscountAndPriceAssertionVisitor createDiscountAndPriceVisitorForCA() {
        return new DiscountAndPriceAssertionVisitor.Builder()
            .withDiscount(OneVoicePriceTariff.VPN_CONFIG.ppsrId(), PriceCategory.CHARGE_PRICE, Percentage.from(VPN_CONFIG_RRP_DISCOUNT_PERCENT))
            .withDiscount(OneVoicePriceTariff.VPN_SUBSCRIPTION.ppsrId(), PriceCategory.CHARGE_PRICE, Percentage.from(VPN_SUBSCRIPTION_RRP_DISCOUNT_PERCENT))
            .withDiscount(OneVoicePriceTariff.DIAL_PLAN_CHANGE_CONFIG.ppsrId(), PriceCategory.CHARGE_PRICE, Percentage.from(DIAL_PLAN_CHANGE_CONFIG_RRP_DISCOUNT_PERCENT))
            .withDiscount(OneVoicePriceTariff.MAJOR_MOVE_ADDS_OR_CHANGE_CONFIG.ppsrId(), PriceCategory.CHARGE_PRICE, Percentage.from(MAJOR_MOVE_ADD_CHANGE_CONFIG_RRP_DISCOUNT_PERCENT))
            .withDiscount(1l, PriceCategory.CHARGE_PRICE, Percentage.from(0))
            .withPrice(OneVoicePriceTariff.AMENDMENT_CHARGE, PriceCategory.CHARGE_PRICE, AMENDMENT_CHARGE)
            .withPrice(OneVoicePriceTariff.CANCELLATION_CHARGE, PriceCategory.CHARGE_PRICE, CANCELLATION_CHARGE)
            .build();
    }

    private void createLineItems() {
        fixture.withOneVoiceItem(PROJECT_ID, QUOTE_OPTION_ID, dto(LineItemFixture.aLineItem().withContract(new Contract("id", "12", new PriceBook("1", "someRequestId", "eup", null, null, null))).with(LineItemDiscountStatus.APPROVAL_REQUESTED).withId(LINE_ITEM_ID).build()));
        fixture.withOneVoiceItem(PROJECT_ID, QUOTE_OPTION_ID, dto(LineItemFixture.aLineItem().withContract(new Contract("id", "12", new PriceBook("1", "someRequestId", "eup", null, null, null))).with(LineItemDiscountStatus.APPROVED).withId(LINE_ITEM_ID_2).build()));
        fixture.withCAServiceItem(PROJECT_ID, QUOTE_OPTION_ID, dto(LineItemFixture.aLineItem().withContract(new Contract("id", "12", new PriceBook("1", "someRequestId", "eup", null, null, null))).with(LineItemDiscountStatus.APPROVAL_REQUESTED).withId(CA_LINE_ITEM_ID).build()));
        fixture.with(LINE_ITEM_ID, SITE_ID, createPricesModel());
        fixture.with(CA_LINE_ITEM_ID, SITE_ID, createCAPricesModelWithReccurring());
        fixture.with(specialPriceBook);
    }

    private DiscountAndPriceAssertionVisitor createDiscountAndPriceVisitor() {
        return new DiscountAndPriceAssertionVisitor.Builder()
            .withDiscount(OneVoicePriceTariff.VPN_CONFIG.ppsrId(), PriceCategory.CHARGE_PRICE, Percentage.from(VPN_CONFIG_RRP_DISCOUNT_PERCENT))
            .withDiscount(OneVoicePriceTariff.VPN_SUBSCRIPTION.ppsrId(), PriceCategory.CHARGE_PRICE, Percentage.from(VPN_SUBSCRIPTION_RRP_DISCOUNT_PERCENT))
            .withDiscount(OneVoicePriceTariff.DIAL_PLAN_CHANGE_CONFIG.ppsrId(), PriceCategory.CHARGE_PRICE, Percentage.from(DIAL_PLAN_CHANGE_CONFIG_RRP_DISCOUNT_PERCENT))
            .withDiscount(OneVoicePriceTariff.MAJOR_MOVE_ADDS_OR_CHANGE_CONFIG.ppsrId(), PriceCategory.CHARGE_PRICE, Percentage.from(MAJOR_MOVE_ADD_CHANGE_CONFIG_RRP_DISCOUNT_PERCENT))
            .withDiscount(1l, PriceCategory.CHARGE_PRICE, Percentage.from(10))
            .withPrice(OneVoicePriceTariff.AMENDMENT_CHARGE, PriceCategory.CHARGE_PRICE, AMENDMENT_CHARGE)
            .withPrice(OneVoicePriceTariff.CANCELLATION_CHARGE, PriceCategory.CHARGE_PRICE, CANCELLATION_CHARGE)
            .build();
    }

    private List<PriceLineDTO> createCAPricesModelWithReccurring() {
        return newArrayList(
            aPriceLineDTO()
                .withPpsrId(1l)
                .withId("123")
                .with(ONE_TIME)
                .withChargePrice(120)
                .withChargePriceDiscount(DEFAULT_DISCOUNT)
                .withEupPrice(0)
                .withEupPriceDiscount(DEFAULT_DISCOUNT).build()
        );
    }

    private void expectationsForBcmSheet() {
        final OneVoiceBcmOptionsSheet mockOptionsSheet = mock(OneVoiceBcmOptionsSheet.class);
        final OneVoiceBcmOptionsRow mockOptionsRow = mock(OneVoiceBcmOptionsRow.class);

        when(mockOneVoiceBcmSpreadsheet.getOneVoiceOptionsSheet()).thenReturn(mockOptionsSheet);
        when(mockOptionsSheet.containsSiteId(any(String.class))).thenReturn(true);
        when(mockOptionsSheet.rowForSiteId(any(String.class))).thenReturn(mockOptionsRow);
        when(mockOptionsRow.vpnConfigDiscount()).thenReturn(BigDecimal.valueOf(VPN_CONFIG_RRP_DISCOUNT_PERCENT));
        when(mockOptionsRow.vpnSubscriptionDiscount()).thenReturn(BigDecimal.valueOf(VPN_SUBSCRIPTION_RRP_DISCOUNT_PERCENT));
        when(mockOptionsRow.dialplanChangeConfigDiscount()).thenReturn(BigDecimal.valueOf(DIAL_PLAN_CHANGE_CONFIG_RRP_DISCOUNT_PERCENT));
        when(mockOptionsRow.mmacConfigDiscount()).thenReturn(BigDecimal.valueOf(MAJOR_MOVE_ADD_CHANGE_CONFIG_RRP_DISCOUNT_PERCENT));

        when(mockOptionsRow.hasAmendmentCharge()).thenReturn(true);
        when(mockOptionsRow.amendmentCharge()).thenReturn(AMENDMENT_CHARGE);
        when(mockOptionsRow.hasCancellationCharge()).thenReturn(true);
        when(mockOptionsRow.cancellationCharge()).thenReturn(CANCELLATION_CHARGE);

        final ProductSheet mockProductSheet = mock(ProductSheet.class);
        when(mockOneVoiceBcmSpreadsheet.getCASiteSheet(any(String.class))).thenReturn(mockProductSheet);
        when(mockOneVoiceBcmSpreadsheet.getCAServiceSheet(any(String.class))).thenReturn(new ProductSheet(caServiceSheet));
        when(mockOneVoiceBcmSpreadsheet.getSpecialBidServiceSheet()).thenReturn(mockProductSheet);
        when(mockProductSheet.getOneTimeRowFor(any(PriceLineModel.class))).thenReturn(Optional.<ProductSheetRow>absent());
        userContext = anIndirectUserContext().withIndirectUser().build();
        UserContextManager.setCurrent(userContext);
    }

    private ArrayList<PriceLineDTO> createPricesModel() {
        return newArrayList(
            aPriceLineDTO()
                .withPpsrId(OneVoicePriceTariff.VPN_CONFIG.ppsrId())
                .with(ONE_TIME)
                .withChargePrice(VPN_CONFIG_RRP_PRICE)
                .withChargePriceDiscount(DEFAULT_DISCOUNT)
                .withEupPrice(VPN_CONFIG_PTP_PRICE)
                .withEupPriceDiscount(DEFAULT_DISCOUNT).build(),
            aPriceLineDTO()
                .withPpsrId(OneVoicePriceTariff.VPN_SUBSCRIPTION.ppsrId())
                .with(RECURRING)
                .withChargePrice(VPN_SUBSCRIPTION_RRP_PRICE)
                .withChargePriceDiscount(DEFAULT_DISCOUNT)
                .withEupPrice(VPN_SUBSCRIPTION_PTP_PRICE)
                .withEupPriceDiscount(DEFAULT_DISCOUNT).build(),
            aPriceLineDTO()
                .withPpsrId(OneVoicePriceTariff.DIAL_PLAN_CHANGE_CONFIG.ppsrId())
                .with(ONE_TIME)
                .withChargePrice(DIAL_PLAN_CHANGE_CONFIG_RRP_PRICE)
                .withChargePriceDiscount(DEFAULT_DISCOUNT)
                .withEupPrice(DIAL_PLAN_CHANGE_CONFIG_PTP_PRICE)
                .withEupPriceDiscount(DEFAULT_DISCOUNT).build(),
            aPriceLineDTO()
                .withPpsrId(OneVoicePriceTariff.MAJOR_MOVE_ADDS_OR_CHANGE_CONFIG.ppsrId())
                .with(ONE_TIME)
                .withChargePrice(MAJOR_MOVE_ADD_CHANGE_CONFIG_RRP_PRICE)
                .withChargePriceDiscount(DEFAULT_DISCOUNT)
                .withEupPrice(MAJOR_MOVE_ADD_CHANGE_CONFIG_PTP_PRICE)
                .withEupPriceDiscount(DEFAULT_DISCOUNT).build(),
            aPriceLineDTO()
                .withPpsrId(OneVoicePriceTariff.AMENDMENT_CHARGE.ppsrId())
                .with(ONE_TIME)
                .withChargePrice(DEFAULT_AMENDMENT_CHARGE).build(),
            aPriceLineDTO()
                .withPpsrId(OneVoicePriceTariff.CANCELLATION_CHARGE.ppsrId())
                .with(ONE_TIME)
                .withChargePrice(DEFAULT_CANCELLATION_CHARGE.toDouble()).build()
        );
    }
}
