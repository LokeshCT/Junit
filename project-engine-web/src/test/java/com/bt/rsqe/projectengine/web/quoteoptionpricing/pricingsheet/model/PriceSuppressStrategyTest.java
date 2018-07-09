package com.bt.rsqe.projectengine.web.quoteoptionpricing.pricingsheet.model;

import com.bt.rsqe.customerinventory.dto.PriceLineDTO;
import com.bt.rsqe.customerinventory.fixtures.PriceLineDTOFixture;
import com.bt.rsqe.customerinventory.parameter.Losb;
import com.bt.rsqe.customerinventory.parameter.PpsrId;
import com.bt.rsqe.domain.product.BillingTariffRuleSet;
import com.bt.rsqe.domain.product.PriceType;
import com.bt.rsqe.domain.product.chargingscheme.PricingStrategy;
import com.bt.rsqe.domain.product.chargingscheme.ProductChargingScheme;
import com.bt.rsqe.domain.project.Price;
import com.bt.rsqe.domain.project.PriceLine;
import com.bt.rsqe.domain.project.PricingStatus;
import com.bt.rsqe.enums.CostDiscountType;
import com.bt.rsqe.enums.PriceCategory;
import com.bt.rsqe.pricing.config.dto.BillingTariffRulesetConfig;
import com.bt.rsqe.pricing.config.dto.PricingConfig;
import com.bt.rsqe.pricing.domain.ExpiryDate;
import com.bt.rsqe.projectengine.web.quoteoptionpricing.PriceSuppressStrategy;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.google.common.collect.Iterables.*;
import static com.google.common.collect.Lists.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class PriceSuppressStrategyTest {
    @Test
    public void shouldReturnGivenPriceLinesWithDefaultPriceSuppressStrategy() {
        List<PriceLine> priceLines = constructPriceAndCostLines();
        List<PriceLine> suppressedPriceCostLines = PriceSuppressStrategy.None.suppressPriceCostLines(constructChargingSchemes(), priceLines);
        assertThat(suppressedPriceCostLines, is(priceLines));
    }

    @Test
    public void shouldReturnGivenPriceLinesForNoChargingSchemes() {
        List<PriceLine> priceLines = constructPriceAndCostLines();
        List<PriceLineDTO> priceLineDTOs = constructPriceAndCostLineDTOs();

        assertThat(priceLineDTOs, is(PriceSuppressStrategy.None.suppressPriceCostLineDTOs(Optional.<PricingConfig>absent(), Collections.<ProductChargingScheme>emptyList(), priceLineDTOs)));
        assertThat(priceLines, is(PriceSuppressStrategy.None.suppressPriceCostLines(Collections.<ProductChargingScheme>emptyList(), priceLines)));


        assertThat(priceLineDTOs, is(PriceSuppressStrategy.UI_PRICES.suppressPriceCostLineDTOs(Optional.<PricingConfig>absent(), Collections.<ProductChargingScheme>emptyList(), priceLineDTOs)));
        assertThat(priceLines, is(PriceSuppressStrategy.UI_PRICES.suppressPriceCostLines(Collections.<ProductChargingScheme>emptyList(), priceLines)));

        assertThat(priceLineDTOs, is(PriceSuppressStrategy.OFFERS_UI.suppressPriceCostLineDTOs(Optional.<PricingConfig>absent(), Collections.<ProductChargingScheme>emptyList(), priceLineDTOs)));
        assertThat(priceLines, is(PriceSuppressStrategy.OFFERS_UI.suppressPriceCostLines(Collections.<ProductChargingScheme>emptyList(), priceLines)));

        assertThat(priceLineDTOs, is(PriceSuppressStrategy.SummarySheet.suppressPriceCostLineDTOs(Optional.<PricingConfig>absent(), Collections.<ProductChargingScheme>emptyList(), priceLineDTOs)));
        assertThat(priceLines, is(PriceSuppressStrategy.SummarySheet.suppressPriceCostLines(Collections.<ProductChargingScheme>emptyList(), priceLines)));

        assertThat(priceLineDTOs, is(PriceSuppressStrategy.DetailedSheet.suppressPriceCostLineDTOs(Optional.<PricingConfig>absent(), Collections.<ProductChargingScheme>emptyList(), priceLineDTOs)));
        assertThat(priceLines, is(PriceSuppressStrategy.DetailedSheet.suppressPriceCostLines(Collections.<ProductChargingScheme>emptyList(), priceLines)));

    }

    /*
     * when Strategy is SummarySheet
     * minus CostLines
     * minus PriceLines of all All visibilities (except Customer).
     * only price lines of visibility Customer.
     */
    @Test
    public void shouldReturnOnlyPriceLinesOfVisibilityCustomerAndSuppressCostLinesWhenStrategyIsSummarySheet() {
        List<PriceLine> priceLines = constructPriceAndCostLines();
        List<ProductChargingScheme> chargingSchemes = constructChargingSchemes();

        List<PriceLine> suppressedPriceCostLines = PriceSuppressStrategy.SummarySheet.suppressPriceCostLines(constructChargingSchemes(), priceLines);

        assertFalse(suppressedPriceCostLines.isEmpty());
        assertThat(suppressedPriceCostLines.size(), is(2));
        assertNoCosts(suppressedPriceCostLines);
        assertPriceLinesOf(suppressedPriceCostLines, chargingSchemes, ProductChargingScheme.PriceVisibility.Customer);
    }

    /*
    * when Strategy is Detailed Sheet
    * minus CostLines
    * minus PriceLines of all All visibilities (except Sales and Customer).
    * only price lines of visibility Sales and Customer.
    */
    @Test
    public void shouldReturnOnlyPriceLinesOfVisibilitySalesAndCustomerAndSuppressCostLinesWhenStrategyIsDetailedSheet() {
        List<PriceLine> priceLines = constructPriceAndCostLines();
        List<ProductChargingScheme> chargingSchemes = constructChargingSchemes();

        List<PriceLine> suppressedPriceCostLines = PriceSuppressStrategy.DetailedSheet.suppressPriceCostLines(constructChargingSchemes(), priceLines);

        assertFalse(suppressedPriceCostLines.isEmpty());
        assertThat(suppressedPriceCostLines.size(), is(4));
        assertNoCosts(suppressedPriceCostLines);
        assertPriceLinesOf(suppressedPriceCostLines, chargingSchemes, ProductChargingScheme.PriceVisibility.Customer, ProductChargingScheme.PriceVisibility.Sales);
    }

    /*
    * when Strategy is UI
    * minus CostLines
    * minus PriceLines of all All visibilities (except Sales and Customer).
    * only price lines of visibility Sales and Customer.
    */
    @Test
    public void shouldReturnOnlyPriceLinesOfVisibilitySalesAndCustomerAndSuppressCostLinesWhenStrategyIsUI() {
        List<PriceLineDTO> priceLineDTOs = constructPriceAndCostLineDTOs();
        List<PriceLineDTO> suppressedPriceCostLines = PriceSuppressStrategy.UI_PRICES.suppressPriceCostLineDTOs(Optional.<PricingConfig>absent(), constructChargingSchemes(), priceLineDTOs);

        assertFalse(suppressedPriceCostLines.isEmpty());
        assertThat(suppressedPriceCostLines.size(), is(4));
    }

    /*
    * when Strategy is UI
    * minus CostLines
    * minus PriceLines of all All visibilities (except Sales and Customer).
    * only price lines of visibility Sales, Customer and LOSB set to yes.
    */
    @Test
    public void shouldReturnOnlyPriceLinesOfVisibilitySalesAndCustomerAndSuppressCostAndLosbCostLinesWhenStrategyIsOffersUI() {
        List<PriceLineDTO> priceLineDTOs = constructPriceAndCostAndLosbLineDTOs();
        List<PriceLineDTO> suppressedPriceCostLines = PriceSuppressStrategy.OFFERS_UI.suppressPriceCostLineDTOs(Optional.<PricingConfig>absent(), constructChargingSchemes(), priceLineDTOs);

        assertFalse(suppressedPriceCostLines.isEmpty());
        assertThat(suppressedPriceCostLines.size(), is(2));
    }

    @Test
    public void shouldReturnOnlyPricesExceptHiddenAndCustomerAggregatedWhenStrategyIsBCM(){
        List<PriceLine> priceLines = constructPriceAndCostLines();
        List<ProductChargingScheme> chargingSchemes = constructChargingSchemes();
        List<PriceLine> suppressedPriceLine = PriceSuppressStrategy.BCMSheet.suppressPriceCostLines(chargingSchemes,priceLines);
        assertThat(suppressedPriceLine.size() , is(2));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void shouldThrowUnsupportedOperationForCostSuppressionWhenPricingConfigIsAbsent() throws Exception {
        PriceSuppressStrategy.UI_COSTS.suppressPriceCostLineDTOs(Optional.<PricingConfig>absent(), Lists.<ProductChargingScheme>newArrayList(), Lists.<PriceLineDTO>newArrayList());
    }

    @Test
    public void shouldOnlyReturnCostsThatAreDiscountable() throws Exception {
        PricingConfig config = mock(PricingConfig.class);
        when(config.getTariffFor("C1", "M1")).thenReturn(Optional.<BillingTariffRulesetConfig>absent());
        when(config.getTariffFor("C2", "M2")).thenReturn(Optional.of(new BillingTariffRulesetConfig(null, null, null, null, null, null, null, null, CostDiscountType.NONE)));
        when(config.getTariffFor("C3", "M3")).thenReturn(Optional.of(new BillingTariffRulesetConfig(null, null, null, null, null, null, null, null, CostDiscountType.RECURRING)));

        final PriceLineDTO priceLine1 = com.bt.rsqe.projectengine.web.fixtures.PriceLineDTOFixture.aPriceLineDTO().withChargingScheme("C1").withPmfId("M1").withTariffType("Cost").build();
        final PriceLineDTO priceLine2 = com.bt.rsqe.projectengine.web.fixtures.PriceLineDTOFixture.aPriceLineDTO().withChargingScheme("C2").withPmfId("M2").withTariffType("Cost").build();
        final PriceLineDTO priceLine3 = com.bt.rsqe.projectengine.web.fixtures.PriceLineDTOFixture.aPriceLineDTO().withChargingScheme("C3").withPmfId("M3").withTariffType("Cost").build();
        final PriceLineDTO priceLine4 = com.bt.rsqe.projectengine.web.fixtures.PriceLineDTOFixture.aPriceLineDTO().withChargingScheme("C4").withPmfId("M4").withTariffType("Recommended Retail Price").build();

        final List<PriceLineDTO> results = PriceSuppressStrategy.UI_COSTS.suppressPriceCostLineDTOs(Optional.of(config),
                                                                                                    Lists.<ProductChargingScheme>newArrayList(),
                                                                                                    Lists.<PriceLineDTO>newArrayList(priceLine1, priceLine2, priceLine3, priceLine4));

        assertThat(results.size(), is(1));
        assertThat(results.get(0).getPmfId(), is("M3"));
    }

    @Test
    public void shouldNotSuppressHiddenPriceLinesForManualPricingStrategy() {
        List<PriceLineDTO> priceLineDTOs = constructPriceAndCostLineDTOs();
        List<ProductChargingScheme> chargingSchemes = newArrayList(
            constructChargingScheme("A", PricingStrategy.ManualPricing, ProductChargingScheme.PriceVisibility.Hidden));
        List<PriceLineDTO> suppressedPriceLineDTOs = PriceSuppressStrategy.UI_PRICES.suppressPriceCostLineDTOs(Optional.<PricingConfig>absent(),
            chargingSchemes, priceLineDTOs);
        assertThat(suppressedPriceLineDTOs.size() , is(2));
        assertThat(suppressedPriceLineDTOs.get(0).getChargingSchemeName(), is("A"));
    }

    private void assertNoCosts(List<PriceLine> suppressedPriceCostLines) {
        for (PriceLine suppressedPriceCostLine : suppressedPriceCostLines) {
            assertFalse(suppressedPriceCostLine.getTariffType().contains(PriceCategory.COST.getLabel()));
        }
    }

    private void assertPriceLinesOf(List<PriceLine> suppressedPriceCostLines, List<ProductChargingScheme> chargingSchemes, ProductChargingScheme.PriceVisibility... visibilities) {
        for (final PriceLine suppressedPriceCostLine : suppressedPriceCostLines) {
            final String chargingSchemeName = suppressedPriceCostLine.getChargingSchemeName();
            Optional<ProductChargingScheme> productChargingSchemeOptional = tryFind(chargingSchemes, new Predicate<ProductChargingScheme>() {
                @Override
                public boolean apply(ProductChargingScheme productChargingScheme) {
                    return productChargingScheme.getName().equalsIgnoreCase(chargingSchemeName);
                }
            });

            ProductChargingScheme productChargingScheme = productChargingSchemeOptional.get();
            assertTrue(isAvailableIn(productChargingScheme.getPriceVisibility(), visibilities));
        }
    }

    private boolean isAvailableIn(ProductChargingScheme.PriceVisibility priceVisibility, ProductChargingScheme.PriceVisibility[] visibilities) {
        for (ProductChargingScheme.PriceVisibility visibility : visibilities) {
            if (visibility.equals(priceVisibility)) {
                return true;
            }
        }
        return false;
    }

    private List<ProductChargingScheme> constructChargingSchemes() {
        return new ArrayList<ProductChargingScheme>() {{
            add(constructChargingScheme("A", PricingStrategy.StencilManagedItem, ProductChargingScheme.PriceVisibility.Sales));
            add(constructChargingScheme("B", PricingStrategy.ManagedItem, ProductChargingScheme.PriceVisibility.Sales));
            add(constructChargingScheme("C", PricingStrategy.Aggregation, ProductChargingScheme.PriceVisibility.Customer));
            add(constructChargingScheme("D", PricingStrategy.PricingEngine, ProductChargingScheme.PriceVisibility.Hidden));
        }};
    }

    private List<PriceLine> constructPriceAndCostLines() {
        return new ArrayList<PriceLine>() {{
            add(createPriceLine("1", 1L, PriceType.ONE_TIME, "1", PriceCategory.END_USER_PRICE.getLabel(), "A"));
            add(createPriceLine("2", 2L, PriceType.RECURRING, "1", PriceCategory.END_USER_PRICE.getLabel(), "A"));

            add(createPriceLine("3", 3L, PriceType.ONE_TIME, "2", PriceCategory.COST.getLabel(), "B"));
            add(createPriceLine("4", 4L, PriceType.RECURRING, "2", PriceCategory.COST.getLabel(), "B"));

            add(createPriceLine("5", 5L, PriceType.ONE_TIME, "3", PriceCategory.PRICE_TO_PARTNER.getLabel(), "C"));
            add(createPriceLine("6", 6L, PriceType.RECURRING, "3", PriceCategory.PRICE_TO_PARTNER.getLabel(), "C"));

            add(createPriceLine("7", 7L, PriceType.ONE_TIME, "4", PriceCategory.PRICE_TO_PARTNER.getLabel(), "D"));
            add(createPriceLine("8", 8L, PriceType.RECURRING, "4", PriceCategory.PRICE_TO_PARTNER.getLabel(), "D"));
        }};
    }

    private List<PriceLineDTO> constructPriceAndCostLineDTOs() {
        return new ArrayList<PriceLineDTO>() {{
            add(createPriceLineDto("1", 1L, com.bt.rsqe.enums.PriceType.ONE_TIME, "1", PriceCategory.END_USER_PRICE.getLabel(), "A"));
            add(createPriceLineDto("2", 2L, com.bt.rsqe.enums.PriceType.RECURRING, "1", PriceCategory.END_USER_PRICE.getLabel(), "A"));

            add(createPriceLineDto("3", 3L, com.bt.rsqe.enums.PriceType.ONE_TIME, "2", PriceCategory.COST.getLabel(), "B"));
            add(createPriceLineDto("4", 4L, com.bt.rsqe.enums.PriceType.RECURRING, "2", PriceCategory.COST.getLabel(), "B"));

            add(createPriceLineDto("5", 5L, com.bt.rsqe.enums.PriceType.ONE_TIME, "3", PriceCategory.PRICE_TO_PARTNER.getLabel(), "C"));
            add(createPriceLineDto("6", 6L, com.bt.rsqe.enums.PriceType.RECURRING, "3", PriceCategory.PRICE_TO_PARTNER.getLabel(), "C"));

            add(createPriceLineDto("7", 7L, com.bt.rsqe.enums.PriceType.ONE_TIME, "4", PriceCategory.PRICE_TO_PARTNER.getLabel(), "D"));
            add(createPriceLineDto("8", 8L, com.bt.rsqe.enums.PriceType.RECURRING, "4", PriceCategory.PRICE_TO_PARTNER.getLabel(), "D"));
        }};
    }

    private List<PriceLineDTO> constructPriceAndCostAndLosbLineDTOs() {
        return new ArrayList<PriceLineDTO>() {{
            add(createPriceLineDto("1", 1L, com.bt.rsqe.enums.PriceType.ONE_TIME, "1", PriceCategory.END_USER_PRICE.getLabel(), "A", com.bt.rsqe.domain.bom.parameters.Losb.no));
            add(createPriceLineDto("2", 2L, com.bt.rsqe.enums.PriceType.RECURRING, "1", PriceCategory.END_USER_PRICE.getLabel(), "A", com.bt.rsqe.domain.bom.parameters.Losb.no));

            add(createPriceLineDto("3", 3L, com.bt.rsqe.enums.PriceType.ONE_TIME, "2", PriceCategory.COST.getLabel(), "B", com.bt.rsqe.domain.bom.parameters.Losb.yes));
            add(createPriceLineDto("4", 4L, com.bt.rsqe.enums.PriceType.RECURRING, "2", PriceCategory.COST.getLabel(), "B", com.bt.rsqe.domain.bom.parameters.Losb.yes));

            add(createPriceLineDto("5", 5L, com.bt.rsqe.enums.PriceType.ONE_TIME, "3", PriceCategory.PRICE_TO_PARTNER.getLabel(), "C", com.bt.rsqe.domain.bom.parameters.Losb.yes));
            add(createPriceLineDto("6", 6L, com.bt.rsqe.enums.PriceType.RECURRING, "3", PriceCategory.PRICE_TO_PARTNER.getLabel(), "C", com.bt.rsqe.domain.bom.parameters.Losb.yes));
        }};
    }

    private ProductChargingScheme constructChargingScheme(String name, PricingStrategy pricingStrategy, ProductChargingScheme.PriceVisibility visibility) {
        return new ProductChargingScheme(name, pricingStrategy, "ABC", visibility, "", new ArrayList<BillingTariffRuleSet>(), null);
    }

    private PriceLine createPriceLine(String id, long ppsrId, PriceType priceType, String pmfId, String tariffType, String chargingSchemeName) {
        return new PriceLine(id, ppsrId, priceType, Price.NIL, Price.NIL, PricingStatus.FIRM, pmfId, "losb", "revenueOwner", "region", "basisofcharge", "pricelinename", "locs", tariffType, chargingSchemeName, "rated", null, null, null, ExpiryDate.NEVER, null, null);
    }

    private PriceLineDTO createPriceLineDto(String id, long ppsrId, com.bt.rsqe.enums.PriceType priceType, String pmfId, String tariffType, String chargingSchemeName) {
        return new PriceLineDTOFixture().withId(id).withPpsrId(new PpsrId(ppsrId)).withPriceType(priceType).withPmfId(pmfId).withTariffType(tariffType).withChargingSchemeName(chargingSchemeName).build();
    }

    private PriceLineDTO createPriceLineDto(String id, long ppsrId, com.bt.rsqe.enums.PriceType priceType, String pmfId, String tariffType, String chargingSchemeName, com.bt.rsqe.domain.bom.parameters.Losb losb) {
        return new PriceLineDTOFixture().withId(id).withPpsrId(new PpsrId(ppsrId)).withPriceType(priceType).withLosb(new Losb(losb.toString())).withPmfId(pmfId).withTariffType(tariffType).withChargingSchemeName(chargingSchemeName).build();
    }
}
