package com.bt.rsqe.projectengine.web.model;

import com.bt.rsqe.Percentage;
import com.bt.rsqe.customerinventory.dto.FutureAssetPricesDTO;
import com.bt.rsqe.customerinventory.dto.PriceLineDTO;
import com.bt.rsqe.customerinventory.parameter.PriceLineStatus;
import com.bt.rsqe.domain.product.chargingscheme.PricingStrategy;
import com.bt.rsqe.domain.product.chargingscheme.ProductChargingScheme;
import com.bt.rsqe.enums.PriceType;
import com.bt.rsqe.projectengine.web.facades.ProductIdentifierFacade;
import com.google.common.base.Optional;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.bt.rsqe.domain.product.chargingscheme.PricingStrategy.*;
import static com.bt.rsqe.domain.product.chargingscheme.ProductChargingScheme.*;
import static com.bt.rsqe.domain.product.chargingscheme.ProductChargingScheme.PriceVisibility.*;
import static com.bt.rsqe.enums.PriceCategory.*;
import static com.bt.rsqe.enums.PriceType.*;
import static com.bt.rsqe.projectengine.web.fixtures.FutureAssetPricesDTOFixture.*;
import static com.bt.rsqe.projectengine.web.fixtures.FutureAssetPricesModelFixture.*;
import static com.bt.rsqe.projectengine.web.fixtures.PriceLineDTOFixture.*;
import static com.google.common.collect.Lists.*;
import static junit.framework.Assert.assertNull;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.core.Is.*;
import static org.mockito.Mockito.*;

public class DiscountUpdaterTest {

    private ProductIdentifierFacade productIdentifierFacade;
    private DiscountUpdater discountUpdater;

    @Before
    public void before() {
        productIdentifierFacade = mock(ProductIdentifierFacade.class);
        discountUpdater = new DiscountUpdater();
    }

    @Test
    public void shouldApplyDiscountDeltasToPriceLines() throws Exception {
        FutureAssetPricesDTO assetPricesDTO = aFutureAssetPricesDTO().withPriceLine(aPriceLineDTO().withDiscount(CHARGE_PRICE, 10).withId("priceLine1").with(ONE_TIME).withPpsrId(10))
            .withPriceLine(aPriceLineDTO().withDiscount(CHARGE_PRICE, 12).withId("priceLine2").with(RECURRING).withPpsrId(10)).build();
        FutureAssetPricesModel futureAssetPricesModel = aFutureAssetPricesModel().with(assetPricesDTO).with(productIdentifierFacade).build();

        final HashMap<String, DiscountDelta> discounts = new HashMap<String, DiscountDelta>();
        discounts.put("priceLine1", new DiscountDelta(Optional.of(new BigDecimal(15)), Optional.of("aVendorDiscount"), Optional.of(PriceLineStatus.REPRICING)));
        discounts.put("priceLine2", new DiscountDelta(Optional.of(new BigDecimal(16)), Optional.of("aVendorDiscount"), Optional.of(PriceLineStatus.REPRICING)));
        discountUpdater.applyDiscount(discounts, futureAssetPricesModel.getPricesDTO(), new ArrayList<PriceLineModel>());

        for (PriceLineDTO dto : assetPricesDTO.getPriceLines()) {
            if (ONE_TIME.equals(dto.getPriceType())) {
                assertThat(dto.getPrice(CHARGE_PRICE).discountPercentage.doubleValue(), is(15.0));
            }

            if (PriceType.RECURRING.equals(dto.getPriceType())) {
                assertThat(dto.getPrice(CHARGE_PRICE).discountPercentage.doubleValue(), is(16.0));
            }

            assertThat(dto.getVendorDiscountRef(), is("aVendorDiscount"));
            assertThat(dto.getStatus(), is(PriceLineStatus.REPRICING));
        }
    }

    @Test
    public void shouldApplyDiscountDeltasToPriceLinesAndUpdateVendorReferenceToNullIfNotPresent() throws Exception {
        FutureAssetPricesDTO assetPricesDTO = aFutureAssetPricesDTO().withPriceLine(aPriceLineDTO().withDiscount(CHARGE_PRICE, 10).withId("priceLine1").with(ONE_TIME).withPpsrId(10))
            .withPriceLine(aPriceLineDTO().withDiscount(CHARGE_PRICE, 12).withId("priceLine2").with(RECURRING).withPpsrId(10)).build();
        FutureAssetPricesModel futureAssetPricesModel = aFutureAssetPricesModel().with(assetPricesDTO).with(productIdentifierFacade).build();

        Optional<String> vendorReference = Optional.absent();
        final HashMap<String, DiscountDelta> discounts = new HashMap<String, DiscountDelta>();
        discounts.put("priceLine1", new DiscountDelta(Optional.of(new BigDecimal(15)), vendorReference, Optional.of(PriceLineStatus.REPRICING)));
        discounts.put("priceLine2", new DiscountDelta(Optional.of(new BigDecimal(16)), vendorReference, Optional.of(PriceLineStatus.REPRICING)));
        discountUpdater.applyDiscount(discounts, futureAssetPricesModel.getPricesDTO(), new ArrayList<PriceLineModel>());

        for (PriceLineDTO dto : assetPricesDTO.getPriceLines()) {
            if (ONE_TIME.equals(dto.getPriceType())) {
                assertThat(dto.getPrice(CHARGE_PRICE).discountPercentage.doubleValue(), is(15.0));
            }

            if (PriceType.RECURRING.equals(dto.getPriceType())) {
                assertThat(dto.getPrice(CHARGE_PRICE).discountPercentage.doubleValue(), is(16.0));
            }

            assertNull(dto.getVendorDiscountRef());
            assertThat(dto.getStatus(), is(PriceLineStatus.REPRICING));
        }
    }

    @Test
    public void shouldRecalculateDiscountBasedOnChild() {
        PriceLineModel aggregateLineModel = createPriceLineModel(10, "priceLine1", 10, "testScheme", Aggregation, Customer, "testACC", "", 10.0);
        PriceLineModel childLineModel1 = createPriceLineModel(11, "priceLine2", 11, "testChildScheme", Aggregation, Sales, "", "testACC", 10.0);
        PriceLineModel childLineModel2 = createPriceLineModel(12, "priceLine3", 12, "testChildScheme", Aggregation, Sales, "", "testACC", 10.0);
        ArrayList<PriceLineModel> childPriceModels = newArrayList(childLineModel1, childLineModel2);

        List<PriceLineDTO> priceLines = discountUpdater.getPriceLinesBasedOnPriceType(childPriceModels, ONE_TIME);
        Percentage oneTime = discountUpdater.getAverageDiscountFor(childPriceModels, ONE_TIME);
        Percentage recurring = discountUpdater.getAverageDiscountFor(childPriceModels, ONE_TIME);
        discountUpdater.reCalculateAggregationDiscount(newArrayList(aggregateLineModel, childLineModel1, childLineModel2));

        assertThat(priceLines.size(), is(2));
        assertThat(oneTime.toString(), is("11.50000"));
        assertThat(recurring.toString(), is("11.50000"));
        assertThat(aggregateLineModel.getPriceLineDTO(ONE_TIME).getPrice(CHARGE_PRICE).discountPercentage.doubleValue(), is(11.5));
        assertThat(aggregateLineModel.getPriceLineDTO(RECURRING).getPrice(CHARGE_PRICE).discountPercentage.doubleValue(), is(11.5));
    }

    @Test
    public void shouldApplyDiscountToAggregationWhenItHasNoChildren() {
        PriceLineModel aggregateLineModel = createPriceLineModel(10, "priceLine1", 10, "testScheme", Aggregation, Customer, "testACC", "", 10.0);
        discountUpdater.reCalculateAggregationDiscount(newArrayList(aggregateLineModel));

        assertThat(aggregateLineModel.getPriceLineDTO(ONE_TIME).getPrice(CHARGE_PRICE).discountPercentage.doubleValue(), is(10.0));
        assertThat(aggregateLineModel.getPriceLineDTO(RECURRING).getPrice(CHARGE_PRICE).discountPercentage.doubleValue(), is(10.0));
    }

    @Test
    public void shouldReturnEmptyValueWhenGivenCollectionIsEmpty() {
        ArrayList<PriceLineModel> childPriceModels = newArrayList();

        List<PriceLineModel> childPriceLineModels = discountUpdater.getChildPriceModelsForAggregated("", childPriceModels);
        List<PriceLineDTO> priceLineDTOs = discountUpdater.getPriceLinesBasedOnPriceType(childPriceModels, PriceType.ONE_TIME);
        Percentage discount = discountUpdater.getAverageDiscountFor(childPriceModels, PriceType.ONE_TIME);

        assertThat(priceLineDTOs.size(), is(0));
        assertThat(discount.toString(), is("0.00000"));
        assertThat(childPriceLineModels.size(), is(0));

    }

    @Test
    public void shouldCalculateDiscountProperly() {
        PriceLineDTO priceLineOne = aPriceLineDTO().withDiscount(CHARGE_PRICE, 2.0).withChargePrice(10).withId("oneTime1").with(ONE_TIME).withPpsrId(123).build();
        PriceLineDTO priceLineSecond = aPriceLineDTO().withDiscount(CHARGE_PRICE, 3.0).withChargePrice(10).withId("oneTime1").with(ONE_TIME).withPpsrId(123).build();
        PriceLineDTO priceLineThird = aPriceLineDTO().withDiscount(CHARGE_PRICE, 4.0).withChargePrice(10).withId("oneTime1").with(ONE_TIME).withPpsrId(123).build();
        List<PriceLineDTO> priceLineDTOs = newArrayList(priceLineOne, priceLineSecond, priceLineThird);
        Percentage percentage = discountUpdater.calculateDiscount(priceLineDTOs);
        assertThat(percentage.toString(), is("3.00000"));

    }

    private PriceLineModel createPriceLineModel(double discount, String id, int ppsrId, String schemeName, PricingStrategy strategy,
                                                PriceVisibility visibility, String setAggregated, String aggregatedSet, Double price) {
        PriceLineDTO oneTime = aPriceLineDTO().withDiscount(CHARGE_PRICE, discount).withChargePrice(price).withId(id + "oneTime").with(ONE_TIME).withPpsrId(ppsrId).build();
        PriceLineDTO recurring = aPriceLineDTO().withDiscount(CHARGE_PRICE, discount).withChargePrice(price).withId(id + "recurring").with(RECURRING).withPpsrId(ppsrId).build();
        ProductChargingScheme scheme = new ProductChargingScheme(schemeName, strategy, aggregatedSet, visibility, setAggregated, null, null);
        return new PriceLineModel(oneTime, recurring, scheme, null, true);
    }
}
