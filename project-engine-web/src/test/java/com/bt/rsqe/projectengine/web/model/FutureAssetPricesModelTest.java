package com.bt.rsqe.projectengine.web.model;

import com.bt.rsqe.customerinventory.dto.FutureAssetPricesDTO;
import com.bt.rsqe.customerinventory.parameter.LengthConstrainingProductInstanceId;
import com.bt.rsqe.customerinventory.parameter.ProductInstanceVersion;
import com.bt.rsqe.customerinventory.parameter.RandomSiteId;
import com.bt.rsqe.customerrecord.SiteDTO;
import com.bt.rsqe.domain.product.BillingTariffRuleSet;
import com.bt.rsqe.domain.product.chargingscheme.PricingStrategy;
import com.bt.rsqe.domain.product.chargingscheme.ProductChargingScheme;
import com.bt.rsqe.domain.product.fixtures.ProductChargingSchemeFixture;
import com.bt.rsqe.enums.CostDiscountType;
import com.bt.rsqe.enums.PriceCategory;
import com.bt.rsqe.enums.PriceType;
import com.bt.rsqe.pricing.PricingStrategyDecider;
import com.bt.rsqe.pricing.config.dto.BillingTariffRulesetConfig;
import com.bt.rsqe.pricing.config.dto.ChargingSchemeConfig;
import com.bt.rsqe.pricing.config.dto.CopyDuringMoveProductFamiliesConfig;
import com.bt.rsqe.pricing.config.dto.PricingConfig;
import com.bt.rsqe.projectengine.ProjectResource;
import com.bt.rsqe.projectengine.QuoteOptionItemDTO;
import com.bt.rsqe.projectengine.QuoteOptionItemDTOFixture;
import com.bt.rsqe.projectengine.QuoteOptionItemResource;
import com.bt.rsqe.projectengine.QuoteOptionResource;
import com.bt.rsqe.projectengine.web.facades.ProductIdentifierFacade;
import com.bt.rsqe.projectengine.web.facades.SiteFacade;
import com.bt.rsqe.projectengine.web.fixtures.FutureAssetPricesDTOFixture;
import com.bt.rsqe.projectengine.web.model.lineitemvisitor.AbstractLineItemVisitor;
import com.bt.rsqe.projectengine.web.model.lineitemvisitor.LineItemVisitor;
import com.bt.rsqe.projectengine.web.quoteoptionpricing.ManualPrice;
import com.bt.rsqe.projectengine.web.quoteoptionpricing.PriceSuppressStrategy;
import com.bt.rsqe.security.PermissionsDTO;
import com.bt.rsqe.security.UserContext;
import com.bt.rsqe.security.UserContextManager;
import com.bt.rsqe.security.UserPrincipal;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.bt.rsqe.projectengine.web.fixtures.FutureAssetPricesDTOFixture.*;
import static com.bt.rsqe.projectengine.web.fixtures.FutureAssetPricesModelFixture.*;
import static com.bt.rsqe.projectengine.web.fixtures.PriceLineDTOFixture.*;
import static com.google.common.collect.Lists.*;
import static com.google.common.collect.Sets.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@SuppressWarnings("PMD.TooManyStaticImports")      // Test class
public class FutureAssetPricesModelTest {

    private DiscountUpdater discountUpdater;
    private FutureAssetPricesModel futureAssetPricesModel;
    private SiteFacade siteFacade;
    private ProductIdentifierFacade productIdentifierFacade;
    private final static String PROJECT_ID = "projectId";
    private final static String CUSTOMER_ID = "customerId";
    private final static String LINE_ITEM_ID = "lineItemId";
    private final static String QUOTE_OPTION_ID = "quoteOptionId";
    private PricingConfig pricingConfig;
    private ProjectResource projectResource =mock(ProjectResource.class);
    private QuoteOptionResource quoteOptionResource = mock(QuoteOptionResource.class);
    private QuoteOptionItemResource quoteOptionItemResource = mock(QuoteOptionItemResource.class);
    private QuoteOptionItemDTO quoteOptionItemDTO;

    @Before
    public void before() {

        siteFacade = mock(SiteFacade.class);
        productIdentifierFacade = mock(ProductIdentifierFacade.class);
        when(projectResource.quoteOptionResource(PROJECT_ID)).thenReturn(quoteOptionResource);
        when(quoteOptionResource.quoteOptionItemResource(QUOTE_OPTION_ID)).thenReturn(quoteOptionItemResource);
        quoteOptionItemDTO = QuoteOptionItemDTOFixture.aQuoteOptionItemDTO().withId(LINE_ITEM_ID)
                                                                            .withAction("Provide")
                                                                            .build();
        when(quoteOptionItemResource.get(LINE_ITEM_ID)).thenReturn(quoteOptionItemDTO);

        UserContextManager.setCurrent(new UserContext(new UserPrincipal("loginName"), "token", new PermissionsDTO(true, false, true, false, false, false)));

        when(productIdentifierFacade.getChargingSchemes(any(String.class), any(String.class))).thenReturn(Collections.<ProductChargingScheme>emptyList());
        discountUpdater = new DiscountUpdater();
        pricingConfig = new PricingConfig();
    }

    @Test
    public void shouldReturnSiteGivenThereIsASite() throws Exception {
        final SiteDTO returnedSiteDTO = new SiteDTO();
        String siteId = new RandomSiteId().value();

        futureAssetPricesModel = aFutureAssetPricesModel()
            .with(siteFacade)
            .with(aFutureAssetPricesDTO().withSiteId(siteId))
            .with(pricingConfig).build();

        when(siteFacade.get(CUSTOMER_ID, PROJECT_ID, siteId)).thenReturn(returnedSiteDTO);

        assertThat(futureAssetPricesModel.getSite(), is(returnedSiteDTO));
    }

    @Test
    public void shouldReturnNullGivenThereIsNoSite() throws Exception {

        futureAssetPricesModel = aFutureAssetPricesModel()
            .with(siteFacade)
            .with(aFutureAssetPricesDTO().withSiteId(null))
            .with(pricingConfig).build();

        assertNull(futureAssetPricesModel.getSite());

        verify(siteFacade, never()).get("customerId", PROJECT_ID, "siteId");
    }

    @Test
    public void shouldReturnSiteNameGivenThereIsASite() throws Exception {
        final SiteDTO returnedSiteDTO = new SiteDTO();
        returnedSiteDTO.name = "site name";
        String siteId = new RandomSiteId().value();

        futureAssetPricesModel = aFutureAssetPricesModel()
            .with(siteFacade)
            .with(aFutureAssetPricesDTO().withSiteId(siteId)).build();

        when(siteFacade.get("customerId", PROJECT_ID, siteId)).thenReturn(returnedSiteDTO);

        assertThat(futureAssetPricesModel.getSiteName(), is("site name"));
    }

    @Test
    public void shouldReturnEmptyStringGivenThereIsNoSite() throws Exception {

        futureAssetPricesModel = aFutureAssetPricesModel()
            .with(siteFacade)
            .with(aFutureAssetPricesDTO().withSiteId(null)).build();

        assertThat(futureAssetPricesModel.getSiteName(), is(""));

        verify(siteFacade, never()).get("customerId", PROJECT_ID, "siteId");
    }

    @Test
    public void shouldCombineOneTimeAndRecurringAsSinglePriceLineModel() throws Exception {
        futureAssetPricesModel = aFutureAssetPricesModel().with(aFutureAssetPricesDTO()
                                                                    .withLineItemId(LINE_ITEM_ID)

                                                                    .withPriceLine(aPriceLineDTO().withPrice(PriceCategory.CHARGE_PRICE, 10).withDiscount(PriceCategory.CHARGE_PRICE, 10).with(PriceType.RECURRING).withPpsrId(10))
                                                                    .withPriceLine(aPriceLineDTO().withPrice(PriceCategory.CHARGE_PRICE, 12).withDiscount(PriceCategory.CHARGE_PRICE, 12).with(PriceType.ONE_TIME).withPpsrId(10)))
            .with(productIdentifierFacade)
            .with(projectResource)
            .build();
        final List<PriceLineModel> priceLines = futureAssetPricesModel.getPriceLines();
        assertThat(priceLines.size(), is(1));
        assertThat(priceLines.get(0).getRecurringDto().value, is("10.00"));
        assertThat(priceLines.get(0).getOneTimeDto().value, is("12.00"));
    }


    @Test
    public void shouldHaveUsageBaseAsRecurringPriceLineModel() throws Exception {
        futureAssetPricesModel = aFutureAssetPricesModel().with(aFutureAssetPricesDTO()
                                                                    .withLineItemId(LINE_ITEM_ID)
                                                                    .withPriceLine(aPriceLineDTO().withPrice(PriceCategory.CHARGE_PRICE, 10).withDiscount(PriceCategory.CHARGE_PRICE, 10).with(PriceType.RECURRING).withPpsrId(10))
                                                                    .withPriceLine(aPriceLineDTO().withPrice(PriceCategory.CHARGE_PRICE, 11).withDiscount(PriceCategory.CHARGE_PRICE, 11).with(PriceType.USAGE_BASED).withPpsrId(11))
                                                                    .withPriceLine(aPriceLineDTO().withPrice(PriceCategory.CHARGE_PRICE, 12).withDiscount(PriceCategory.CHARGE_PRICE, 12).with(PriceType.ONE_TIME).withPpsrId(12)))
            .with(productIdentifierFacade)
            .with(projectResource)
            .build();
        final List<PriceLineModel> priceLines = futureAssetPricesModel.getPriceLines();
        assertThat(priceLines.size(), is(3));
        assertThat(priceLines.get(0).getRecurringDto().value, is("10.00"));
        assertThat(priceLines.get(1).getRecurringDto().value, is("11.00"));
        assertThat(priceLines.get(2).getOneTimeDto().value, is("12.00"));
    }


    @Test
    public void shouldMapPriceLinesEvenWhenThereIsNoMatchingPriceLine() throws Exception {
        futureAssetPricesModel = aFutureAssetPricesModel().with(aFutureAssetPricesDTO()
                                                                    .withLineItemId(LINE_ITEM_ID)
                                                                    .withPriceLine(aPriceLineDTO().withPrice(PriceCategory.CHARGE_PRICE, 10).withDiscount(PriceCategory.CHARGE_PRICE, 10).with(PriceType.RECURRING).withPpsrId(11))
                                                                    .withPriceLine(aPriceLineDTO().withPrice(PriceCategory.CHARGE_PRICE, 12).withDiscount(PriceCategory.CHARGE_PRICE, 12).with(PriceType.ONE_TIME).withPpsrId(10)))
            .with(productIdentifierFacade)
            .with(projectResource)
            .build();
        final List<PriceLineModel> priceLines = futureAssetPricesModel.getPriceLines();
        assertThat(priceLines.size(), is(2));
    }

    @Test
    public void shouldReturnFutureAssetPricesDTO() throws Exception {
        final FutureAssetPricesDTO dto = aFutureAssetPricesDTO().build();
        futureAssetPricesModel = aFutureAssetPricesModel().with(dto).build();
        assertThat(futureAssetPricesModel.getPricesDTO(), is(dto));
    }

    @Test
    public void shouldReturnId() throws Exception {
        futureAssetPricesModel = aFutureAssetPricesModel().with(aFutureAssetPricesDTO().withId("id")).build();
        assertThat(futureAssetPricesModel.getId(), is("id"));
    }

    @Test
    public void shouldApplyDiscountsToPriceLines() throws Exception {
        futureAssetPricesModel = aFutureAssetPricesModel().with(aFutureAssetPricesDTO().withProductCode("testProduct")
                                                                    .withLineItemId(LINE_ITEM_ID)
                                                                    .withPriceLine(aPriceLineDTO().withDiscount(PriceCategory.CHARGE_PRICE, 10).withId("priceLine1").with(PriceType.ONE_TIME).withPpsrId(10))
                                                                    .withPriceLine(aPriceLineDTO().withDiscount(PriceCategory.CHARGE_PRICE, 12).withId("priceLine2").with(PriceType.RECURRING).withPpsrId(10)))
                                                                    .with(productIdentifierFacade).with(discountUpdater).with(projectResource).build();

        final HashMap<String, BigDecimal> discounts = new HashMap<String, BigDecimal>();
        discounts.put("priceLine1", new BigDecimal(15));
        discounts.put("priceLine2", new BigDecimal(16));
        futureAssetPricesModel.applyDiscount(discounts);

        assertThat(futureAssetPricesModel.getPriceLines().get(0).getOneTimeDto().discount, is("15.00000"));
        assertThat(futureAssetPricesModel.getPriceLines().get(0).getRecurringDto().discount, is("16.00000"));
    }

    @Test
    public void shouldApplyDiscountsToChildrenPriceLines() throws Exception {
        futureAssetPricesModel = aFutureAssetPricesModel().with(projectResource)
                                                          .with(aFutureAssetPricesDTO().withLineItemId(LINE_ITEM_ID)
                                                                                       .withChild(aFutureAssetPricesDTO().withLineItemId(LINE_ITEM_ID)
                                                                                                                         .withPriceLine(aPriceLineDTO().withDiscount(PriceCategory.CHARGE_PRICE, 10).withId("priceLine1").with(PriceType.ONE_TIME).withPpsrId(10))
                                                                                                                         .withPriceLine(aPriceLineDTO().withDiscount(PriceCategory.CHARGE_PRICE, 12).withId("priceLine2").with(PriceType.RECURRING).withPpsrId(10))))
                                                                                                                         .with(productIdentifierFacade).with(discountUpdater).build();

        final HashMap<String, BigDecimal> discounts = new HashMap<String, BigDecimal>();
        discounts.put("priceLine1", new BigDecimal(15));
        discounts.put("priceLine2", new BigDecimal(16));
        futureAssetPricesModel.applyDiscount(discounts);

        final PriceLineModel priceLineModel = futureAssetPricesModel.getChildren().get(0).getPriceLines().get(0);
        assertThat(priceLineModel.getOneTimeDto().discount, is("15.00000"));
        assertThat(priceLineModel.getRecurringDto().discount, is("16.00000"));
    }

    @Test
    public void shouldApplyManualPricesToPriceLines() throws Exception {
        //Given
        futureAssetPricesModel = aFutureAssetPricesModel().with(aFutureAssetPricesDTO()
                                                                    .withLineItemId(LINE_ITEM_ID)
                                                                    .withPriceLine(aPriceLineDTO().withId("100").withDiscount(PriceCategory.CHARGE_PRICE, 10).withDescription("priceLine1").with(PriceType.ONE_TIME).withPpsrId(10))
                                                                    .withPriceLine(aPriceLineDTO().withId("200").withDiscount(PriceCategory.CHARGE_PRICE, 12).withDescription("priceLine2").with(PriceType.RECURRING).withPpsrId(10)))
            .with(productIdentifierFacade).with(discountUpdater).with(projectResource).build();

        //When
        futureAssetPricesModel.applyGrossPriceUpdate(newHashSet(new ManualPrice("100", new BigDecimal("11"), "oneTime", "priceLine1"),
                                                                new ManualPrice("200", new BigDecimal("22"), "recurring", "priceLine2")));

        final PriceLineModel priceLineModel = futureAssetPricesModel.getPriceLines().get(0);
        assertThat(priceLineModel.getOneTimeDto().value, is("11.00"));
        assertThat(priceLineModel.getRecurringDto().value, is("22.00"));
    }

    @Test
    public void shouldApplyManualPricesToChildrenPriceLines() throws Exception {
        //Given
        futureAssetPricesModel = aFutureAssetPricesModel().with(aFutureAssetPricesDTO().withChild(aFutureAssetPricesDTO()
                                                                                                      .withLineItemId(LINE_ITEM_ID)
                                                                                                      .withPriceLine(aPriceLineDTO().withId("100").withDiscount(PriceCategory.CHARGE_PRICE, 10).withDescription("priceLine1").with(PriceType.ONE_TIME).withPpsrId(10))
                                                                                                      .withPriceLine(aPriceLineDTO().withId("200").withDiscount(PriceCategory.CHARGE_PRICE, 12).withDescription("priceLine2").with(PriceType.RECURRING).withPpsrId(10))))
            .with(productIdentifierFacade).with(discountUpdater).with(projectResource).build();

        //When
        futureAssetPricesModel.applyGrossPriceUpdate(newHashSet(new ManualPrice("100", new BigDecimal("33"), "oneTime", "priceLine1"),
                                                                new ManualPrice("200", new BigDecimal("44"), "recurring", "priceLine2")));

        final PriceLineModel priceLineModel = futureAssetPricesModel.getChildren().get(0).getPriceLines().get(0);
        assertThat(priceLineModel.getOneTimeDto().value, is("33.00"));
        assertThat(priceLineModel.getRecurringDto().value, is("44.00"));
    }

    @Test
    public void shouldReturnOnlyPriceLinesOfVisibilityCustomerAndSalesWhenPricingStrategyIsUI() {
        when(productIdentifierFacade.getChargingSchemes(any(String.class), any(String.class))).thenReturn(chargingSchemes());
        futureAssetPricesModel = aFutureAssetPricesModel().with(PriceSuppressStrategy.UI_PRICES).with(aFutureAssetPricesDTO()
                                                                                                          .withLineItemId(LINE_ITEM_ID)
                                                                                                          .withPriceLine(aPriceLineDTO().withPrice(PriceCategory.CHARGE_PRICE, 10).withDiscount(PriceCategory.CHARGE_PRICE, 10).with(PriceType.RECURRING).withPpsrId(10).withChargingScheme("A").withTariffType("Cost"))
                                                                                                          .withPriceLine(aPriceLineDTO().withPrice(PriceCategory.CHARGE_PRICE, 12).withDiscount(PriceCategory.CHARGE_PRICE, 12).with(PriceType.ONE_TIME).withPpsrId(12).withChargingScheme("B").withTariffType("price"))
                                                                                                          .withPriceLine(aPriceLineDTO().withPrice(PriceCategory.CHARGE_PRICE, 12).withDiscount(PriceCategory.CHARGE_PRICE, 12).with(PriceType.ONE_TIME).withPpsrId(13).withChargingScheme("C").withTariffType("price"))
                                                                                                          .withPriceLine(aPriceLineDTO().withPrice(PriceCategory.CHARGE_PRICE, 12).withDiscount(PriceCategory.CHARGE_PRICE, 12).with(PriceType.ONE_TIME).withPpsrId(14).withChargingScheme("D").withTariffType("price")))
            .with(productIdentifierFacade).with(projectResource).build();
        final List<PriceLineModel> priceLines = futureAssetPricesModel.getPriceLines();
        assertThat(priceLines.size(), is(2));
    }

    @Test
    public void shouldGetStencilledChargingSchemesWhenFilteringViewablePrices() throws Exception {
        when(productIdentifierFacade.getChargingSchemes(any(String.class), any(String.class))).thenReturn(chargingSchemes());
        futureAssetPricesModel = aFutureAssetPricesModel().with(PriceSuppressStrategy.UI_PRICES).with(aFutureAssetPricesDTO()
                                                                                                          .withLineItemId(LINE_ITEM_ID)
                                                                                                          .withProductCode("aProductCode")
                                                                                                          .withStencilId("aStencilCode")
                                                                                                          .withPriceLine(aPriceLineDTO().withPrice(PriceCategory.CHARGE_PRICE, 10).withDiscount(PriceCategory.CHARGE_PRICE, 10).with(PriceType.RECURRING).withPpsrId(10).withChargingScheme("A").withTariffType("Cost"))
                                                                                                          .withPriceLine(aPriceLineDTO().withPrice(PriceCategory.CHARGE_PRICE, 12).withDiscount(PriceCategory.CHARGE_PRICE, 12).with(PriceType.ONE_TIME).withPpsrId(12).withChargingScheme("B").withTariffType("price"))
                                                                                                          .withPriceLine(aPriceLineDTO().withPrice(PriceCategory.CHARGE_PRICE, 12).withDiscount(PriceCategory.CHARGE_PRICE, 12).with(PriceType.ONE_TIME).withPpsrId(13).withChargingScheme("C").withTariffType("price"))
                                                                                                          .withPriceLine(aPriceLineDTO().withPrice(PriceCategory.CHARGE_PRICE, 12).withDiscount(PriceCategory.CHARGE_PRICE, 12).with(PriceType.ONE_TIME).withPpsrId(14).withChargingScheme("D").withTariffType("price")))
                                                                                                   .with(productIdentifierFacade)
                                                                                                   .with(projectResource)
                                                                                                   .build();

        final List<PriceLineModel> priceLines = futureAssetPricesModel.getPriceLines();
        assertThat(priceLines.size(), is(2));
        verify(productIdentifierFacade).getChargingSchemes("aProductCode", "aStencilCode");
    }

    @Test
    public void shouldReturnAllPricesAndCostsWhenPricingStrategyIsNone() {
        when(productIdentifierFacade.getChargingSchemes(any(String.class), any(String.class))).thenReturn(chargingSchemes());
        futureAssetPricesModel = aFutureAssetPricesModel().with(PriceSuppressStrategy.None).with(aFutureAssetPricesDTO()
                                                                                                     .withLineItemId(LINE_ITEM_ID)
                                                                                                     .withPriceLine(aPriceLineDTO().withPrice(PriceCategory.CHARGE_PRICE, 10).withDiscount(PriceCategory.CHARGE_PRICE, 10).with(PriceType.RECURRING).withPpsrId(10).withChargingScheme("A").withTariffType("Cost"))
                                                                                                     .withPriceLine(aPriceLineDTO().withPrice(PriceCategory.CHARGE_PRICE, 12).withDiscount(PriceCategory.CHARGE_PRICE, 12).with(PriceType.ONE_TIME).withPpsrId(12).withChargingScheme("B").withTariffType("price"))
                                                                                                     .withPriceLine(aPriceLineDTO().withPrice(PriceCategory.CHARGE_PRICE, 12).withDiscount(PriceCategory.CHARGE_PRICE, 12).with(PriceType.ONE_TIME).withPpsrId(13).withChargingScheme("C").withTariffType("price"))
                                                                                                     .withPriceLine(aPriceLineDTO().withPrice(PriceCategory.CHARGE_PRICE, 12).withDiscount(PriceCategory.CHARGE_PRICE, 12).with(PriceType.ONE_TIME).withPpsrId(14).withChargingScheme("D").withTariffType("price")))
            .with(productIdentifierFacade)
            .with(projectResource)
            .build();
        final List<PriceLineModel> priceLines = futureAssetPricesModel.getPriceLines();
        assertThat(priceLines.size(), is(4));
    }

    private List<ProductChargingScheme> chargingSchemes() {
        return new ArrayList<ProductChargingScheme>() {{
            add(new ProductChargingScheme("A", PricingStrategy.StencilManagedItem, "ABC", ProductChargingScheme.PriceVisibility.Sales, "", new ArrayList<BillingTariffRuleSet>(), null));
            add(new ProductChargingScheme("B", PricingStrategy.ManagedItem, "ABC", ProductChargingScheme.PriceVisibility.Sales, "", new ArrayList<BillingTariffRuleSet>(), null));
            add(new ProductChargingScheme("C", PricingStrategy.Aggregation, "ABC", ProductChargingScheme.PriceVisibility.Hidden, "", new ArrayList<BillingTariffRuleSet>(), null));
            add(new ProductChargingScheme("D", PricingStrategy.PricingEngine, "ABC", ProductChargingScheme.PriceVisibility.Customer, "", new ArrayList<BillingTariffRuleSet>(), null));
        }};
    }

    @Test
    public void shouldVisitAllItsChildrenAndAllTheirChildrenAtTheCorrectLevel() throws Exception {

        final FutureAssetPricesDTOFixture.Builder child1 = aFutureAssetPricesDTO().withId("Child1");
        final FutureAssetPricesDTOFixture.Builder child2 = aFutureAssetPricesDTO().withId("Child2");
        final FutureAssetPricesDTOFixture.Builder parent1 = aFutureAssetPricesDTO().withId("Parent1").withChild(child1).withChild(child2);
        final FutureAssetPricesDTOFixture.Builder parent2 = aFutureAssetPricesDTO().withId("Parent2");
        final FutureAssetPricesDTOFixture.Builder grandParent = aFutureAssetPricesDTO().withId("GrandParent").withChild(parent1).withChild(parent2);

        futureAssetPricesModel = aFutureAssetPricesModel().with(grandParent).with(productIdentifierFacade).build();

        final TestVisitor visitor = new TestVisitor();
        futureAssetPricesModel.accept(visitor);

        final Map<String, Integer> visitedElements = visitor.getVisitedFutureAssetPricesModels();
        assertThat(visitedElements.get("GrandParent"), is(0));
        assertThat(visitedElements.get("Parent1"), is(1));
        assertThat(visitedElements.get("Parent2"), is(1));
        assertThat(visitedElements.get("Child1"), is(2));
        assertThat(visitedElements.get("Child2"), is(2));

    }

    @Test
    public void shouldReturnCentralSiteDetailsForSiteAgnosticProduct() {
        final String centralSiteName = "AdastralParkSite";
        final String centralSiteID = new RandomSiteId().value();

        SiteDTO centralSite = new SiteDTO(centralSiteID, centralSiteName);
        when(siteFacade.getCentralSite(CUSTOMER_ID, PROJECT_ID)).thenReturn(centralSite);

        futureAssetPricesModel = aFutureAssetPricesModel()
            .with(siteFacade)
            .with(aFutureAssetPricesDTO()
                      .withSiteId(null))
            .build();

        assertThat(futureAssetPricesModel.getSiteName(), is(centralSiteName));
        verify(siteFacade).getCentralSite(CUSTOMER_ID, PROJECT_ID);
    }

    @Test
    public void shouldReturnDisplayName() throws Exception {
        when(productIdentifierFacade.getDisplayName(any(String.class))).thenReturn("productDisplayName");
        futureAssetPricesModel = aFutureAssetPricesModel().with(aFutureAssetPricesDTO().withChild(aFutureAssetPricesDTO()
                                                                                                      .withPriceLine(aPriceLineDTO().withDiscount(PriceCategory.CHARGE_PRICE, 10).withId("priceLine1").with(PriceType.ONE_TIME).withPpsrId(10))
                                                                                                      .withPriceLine(aPriceLineDTO().withDiscount(PriceCategory.CHARGE_PRICE, 12).withId("priceLine2").with(PriceType.RECURRING).withPpsrId(10))))
            .with(productIdentifierFacade).with(discountUpdater).build();


        assertThat(futureAssetPricesModel.getDisplayName(), is("productDisplayName"));


    }

    @Test
    public void shouldOverridePricingStrategyOfProductChargingSchemes() throws Exception {
        ProductChargingScheme chargingScheme = ProductChargingSchemeFixture.aChargingScheme().withName("aChargingScheme").build();
        when(productIdentifierFacade.getChargingSchemes("aProductCode", null)).thenReturn(newArrayList(chargingScheme));

        PricingStrategyDecider pricingStrategyDecider = mock(PricingStrategyDecider.class);

        FutureAssetPricesModel model = aFutureAssetPricesModel()
                                            .with(productIdentifierFacade)
                                            .with(pricingStrategyDecider)
                                            .with(FutureAssetPricesDTOFixture.aFutureAssetPricesDTO()
                                                                             .withId("anAssetId")
                                                                             .withAssetVersion(2L)
                                                                             .withProductCode("aProductCode"))
                                            .build();

        model.getPriceLines();

        verify(pricingStrategyDecider).resetStrategyForScenario(newArrayList(chargingScheme),
                                                                new LengthConstrainingProductInstanceId("anAssetId"),
                                                                new ProductInstanceVersion(2L));
    }

    @Test
    public void shouldGetCostLines() throws Exception {
        PricingConfig pricingConfig = new PricingConfig(newArrayList(new ChargingSchemeConfig(Lists.<BillingTariffRulesetConfig>newArrayList(new BillingTariffRulesetConfig("P1", null, null, null, null, null, null, null, CostDiscountType.BOTH)),
                                                                                              "C1",
                                                                                              null,
                                                                                              null,
                                                                                              null,
                                                                                              null,
                                                                                              null,
                                                                                              null,
                                                                                              Lists.<String>newArrayList(),
                                                                                              null)),
                                                        Lists.<CopyDuringMoveProductFamiliesConfig>newArrayList());

        when(productIdentifierFacade.getChargingSchemes(any(String.class), any(String.class))).thenReturn(chargingSchemes());
        futureAssetPricesModel = aFutureAssetPricesModel().with(PriceSuppressStrategy.UI_COSTS).with(aFutureAssetPricesDTO()
                                                                    .withLineItemId(LINE_ITEM_ID)
                                                                    .withPriceLine(aPriceLineDTO().withPrice(PriceCategory.CHARGE_PRICE, 10).withChargingScheme("C1").withPmfId("P1").withDiscount(PriceCategory.CHARGE_PRICE, 10).with(PriceType.RECURRING).withPpsrId(10).withTariffType("Cost"))
                                                                    .withPriceLine(aPriceLineDTO().withPrice(PriceCategory.CHARGE_PRICE, 12).withChargingScheme("C1").withPmfId("P1").withDiscount(PriceCategory.CHARGE_PRICE, 12).with(PriceType.ONE_TIME).withPpsrId(12).withTariffType("Cost"))
                                                                    .withPriceLine(aPriceLineDTO().withPrice(PriceCategory.CHARGE_PRICE, 12).withChargingScheme("C1").withPmfId("P1").withDiscount(PriceCategory.CHARGE_PRICE, 12).with(PriceType.ONE_TIME).withPpsrId(13).withTariffType("Cost"))
                                                                    .withPriceLine(aPriceLineDTO().withPrice(PriceCategory.CHARGE_PRICE, 12).withChargingScheme("C2").withDiscount(PriceCategory.CHARGE_PRICE, 12).with(PriceType.ONE_TIME).withPpsrId(14).withTariffType("price")))
            .with(productIdentifierFacade)
            .with(projectResource)
            .with(pricingConfig).build();
        final List<PriceLineModel> costLines = futureAssetPricesModel.getPriceLines();
        assertThat(costLines.size(), is(3));
        findPriceLineModel(costLines, 10L);
        findPriceLineModel(costLines, 12L);
        findPriceLineModel(costLines, 13L);
    }

    private PriceLineModel findPriceLineModel(List<PriceLineModel> models, final long ppsrId) {
        return Iterables.find(models, new Predicate<PriceLineModel>() {
            @Override
            public boolean apply(@Nullable PriceLineModel input) {
                return ppsrId == input.getPpsrId();
            }
        });
    }

    private class TestVisitor extends AbstractLineItemVisitor implements LineItemVisitor {
        private HashMap<String, Integer> visitedFutureAssetPricesModels;
        private HashMap<String, String> visitedProjectedUsages;
        private FutureAssetPricesModel currentFutureAssetPricesModel;

        private TestVisitor() {
            visitedFutureAssetPricesModels = new HashMap<String, Integer>();
            visitedProjectedUsages = new HashMap<String, String>();
        }

        @Override
        public void visit(PriceModel priceModel) {
        }

        @Override
        public void visit(PriceLineModel priceLine) {
        }

        @Override
        public void visit(ProjectedUsageModel projectedUsage) {
            visitedProjectedUsages.put(currentFutureAssetPricesModel.getId(), projectedUsage.getDestinationCountry());
        }

        @Override
        public void visit(FutureAssetPricesModel futureAssetPricesModel, int groupingLevel) {
            this.currentFutureAssetPricesModel = futureAssetPricesModel;

            visitedFutureAssetPricesModels.put(futureAssetPricesModel.getId(), groupingLevel);
        }

        public Map<String, Integer> getVisitedFutureAssetPricesModels() {
            return visitedFutureAssetPricesModels;
        }

        public HashMap<String, String> getVisitedProjectedUsages() {
            return visitedProjectedUsages;
        }
    }

}
