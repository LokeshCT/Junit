package com.bt.rsqe.projectengine.web.quoteoptionpricing.pricingsheet.model;

import com.bt.rsqe.domain.product.DefaultProductInstanceFixture;
import com.bt.rsqe.domain.project.Price;
import com.bt.rsqe.domain.project.PriceLine;
import com.bt.rsqe.domain.project.ProductInstance;
import com.bt.rsqe.enums.PriceCategory;
import com.bt.rsqe.integration.PriceLineFixture;
import com.bt.rsqe.projectengine.web.quoteoptionpricing.pricingsheet.PriceType;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import org.apache.cxf.tools.common.toolspec.parser.Option;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static com.google.common.collect.Lists.*;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;

public class UsageChargeTierPricingSheetModelTest {
    private static final String PMF_ID = "aPmfId";
    private static final String PRICE_LINE_NAME = "aPriceLine";
    private static final PriceLine ONE_TIME_PRICE = PriceLineFixture.aPriceLine().withPriceLineName(PRICE_LINE_NAME).withEupPrice(1.0).build();
    private static final PriceLine RENTAL_PRICE = PriceLineFixture.aPriceLine().withPriceLineName(PRICE_LINE_NAME).withEupPrice(2.0).build();
    private static final PriceLine USAGE_PRICE = PriceLineFixture.aPriceLine().withPriceLineName(PRICE_LINE_NAME).withEupPrice(3.0).build();
    private static final String CONTRACT_TERM = "12";
    private static final PriceType PRICE_TYPE = PriceType.NEW;
    private static final ProductInstance OWNING_INSTANCE = DefaultProductInstanceFixture.aProductInstance().withProductInstanceId("owningInstance").build();
    private static final ProductInstance AS_IS_INSTANCE = DefaultProductInstanceFixture.aProductInstance().withProductInstanceId("liveInstance").build();
    private static final String CLASSIFIER = "aClassifier";
    private static final Price MIN_CHARGE = new Price(null, new BigDecimal("30"), new BigDecimal("50"), CLASSIFIER, null, null, PriceCategory.MIN_CHARGE.getLabel());
    private static final Price FIXED_CHARGE = new Price(null, new BigDecimal("20"), new BigDecimal("50"), CLASSIFIER, null, null, PriceCategory.FIXED_CHARGE.getLabel());
    private static final Price CHARGE_RATE = new Price(null, new BigDecimal("10"), new BigDecimal("50"), CLASSIFIER, null, null, PriceCategory.CHARGE_RATE.getLabel());

    private UsageChargeTierPricingSheetModel priceModel;
    private PricingSheetPriceModel parent;

    @Before
    public void setup() {
        parent = new PricingSheetPriceModel(PMF_ID, ONE_TIME_PRICE, RENTAL_PRICE, USAGE_PRICE, CONTRACT_TERM, PRICE_TYPE.name(), OWNING_INSTANCE, Optional.of(AS_IS_INSTANCE));
        priceModel = new UsageChargeTierPricingSheetModel(parent, CLASSIFIER, newArrayList(MIN_CHARGE, FIXED_CHARGE, CHARGE_RATE));
    }

    @Test
    public void shouldCopyDetailsFromParent() throws Exception {
        assertThat(priceModel.getPmfId(), is(PMF_ID));
        assertThat(priceModel.getOneTimePrice(), is(ONE_TIME_PRICE));
        assertThat(priceModel.getRentalPrice(), is(RENTAL_PRICE));
        assertThat(priceModel.getUsagePrice(), is(USAGE_PRICE));
        assertThat(priceModel.getContractTerm(), is(12));
        assertThat(priceModel.getPriceType(), is(PRICE_TYPE.name()));
        assertThat(priceModel.getOwningInstance(), is(OWNING_INSTANCE));
        assertThat(priceModel.getAsIsInstance(), is(Optional.of(AS_IS_INSTANCE)));
    }

    @Test
    public void shouldSetPricesIntoRelevantChargeFields() throws Exception {
        assertThat(priceModel.getMinCharge().intValue(), is(15));
        assertThat(priceModel.getFixedCharge().intValue(), is(10));
        assertThat(priceModel.getChargeRate().intValue(), is(5));
    }

    @Test
    public void shouldReturnNullForChargesWhenNoneHaveBeenSpecified() throws Exception {
        priceModel = new UsageChargeTierPricingSheetModel(parent, CLASSIFIER, Lists.<Price>newArrayList());
        assertThat(priceModel.getMinCharge(), is(nullValue()));
        assertThat(priceModel.getFixedCharge(), is(nullValue()));
        assertThat(priceModel.getChargeRate(), is(nullValue()));
    }

    @Test
    public void shouldReturnNullForEUPPricesAsDefault() throws Exception {
        assertThat(priceModel.getNonRecurringEupPrice(), is(nullValue()));
        assertThat(priceModel.getRecurringEupPrice(), is(nullValue()));
    }

    @Test
    public void shouldAppendClassifierToPriceLineDescription() throws Exception {
        assertThat(priceModel.getDescription(), is(PRICE_LINE_NAME + " - " + CLASSIFIER));
    }

    @Test
    public void shouldSortModelsByClassifierInAscendingOrder() throws Exception {
        UsageChargeTierPricingSheetModel model1 = new UsageChargeTierPricingSheetModel(parent, "Tier 4", Lists.<Price>newArrayList());
        UsageChargeTierPricingSheetModel model2 = new UsageChargeTierPricingSheetModel(parent, "Tier 1", Lists.<Price>newArrayList());
        UsageChargeTierPricingSheetModel model3 = new UsageChargeTierPricingSheetModel(parent, "Tier 7", Lists.<Price>newArrayList());

        List<UsageChargeTierPricingSheetModel> models = newArrayList(model1, model2, model3);
        Collections.sort(models);
        assertThat(models.get(0), is(model2));
        assertThat(models.get(1), is(model1));
        assertThat(models.get(2), is(model3));
    }
}
