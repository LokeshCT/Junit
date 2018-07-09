package com.bt.rsqe.projectengine.web.quoteoptionpricing.pricingsheet.model;

import com.bt.rsqe.domain.product.DefaultProductInstanceFixture;
import com.bt.rsqe.domain.product.PriceType;
import com.bt.rsqe.domain.project.PriceLine;
import com.bt.rsqe.domain.project.ProductInstance;
import com.bt.rsqe.enums.AssetVersionStatus;
import com.bt.rsqe.projectengine.web.quoteoptionpricing.pricingsheet.PricingSheetTestDataFixture;
import com.google.common.base.Optional;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import static com.bt.rsqe.util.DateHelper.maxRoundedOffMonthsBetween;
import static com.google.common.collect.Collections2.*;
import static com.google.common.collect.Lists.*;
import static java.lang.System.currentTimeMillis;
import static org.apache.commons.lang.time.DateUtils.addMonths;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

public class PricingSheetPriceModelTest {

    private static final ProductInstance owningInstance = DefaultProductInstanceFixture.aProductInstance().withRFOAttributeValue("anAttribute", "anAttributeValue", false, false).build();
    private PricingSheetPriceModel priceModel;
    PriceLine usagePrice;

    @Before
    public void setup() {
        PricingSheetTestDataFixture pricingSheetTestDataFixture = new PricingSheetTestDataFixture();
        PriceLine oneTimePriceLine = pricingSheetTestDataFixture.aPriceLine("Root Product One time price", "M0302165", 100.00, PriceType.ONE_TIME, "Recommended Retail Price", 333.00, "A", "1");
        PriceLine recurringPrice = pricingSheetTestDataFixture. aPriceLine("Root Product Rental price", "M0302165", 50.00, PriceType.RECURRING, "Recommended Retail Price", 333.00, "A", "2");
        usagePrice = pricingSheetTestDataFixture. aPriceLine("Root Product Rental price", "M0302165", 50.00, PriceType.RECURRING, "Recommended Retail Price", 222.00, "A", "2");
        priceModel = new PricingSheetPriceModel("M0302165", oneTimePriceLine, recurringPrice, usagePrice, "36", "NEW", owningInstance, null);
    }

    @Test
    public void shouldReturnRecurringEupPrice() {
        assertThat(priceModel.getRecurringEupPrice().doubleValue(), is(333.00));
    }

    @Test
    public void shouldReturnNonRecurringEupPrice() {
        assertThat(priceModel.getNonRecurringEupPrice().doubleValue(), is(333.00));
    }

    @Test
    public void shouldReturnContractTermOfPriceLine() {
        assertThat(priceModel.getContractTerm(), is(36));
    }

    @Test
    public void shouldReturnRecurringEupPriceForContract() {
        assertThat(priceModel.getRecurringEupPriceForContract().doubleValue(), is(36*333.00));
    }

    @Test
    public void shouldReturnIndirectUserRecurringEupPriceForContract() {
        assertThat(priceModel.getRecurringPtpPriceForContract().doubleValue(), is(36*333.00));
    }

    @Test
    public void shouldReturnUsageBasedEupPrice(){
        assertThat(priceModel.getUsageBasedEupPrice().doubleValue(), is(222.0));
    }

    @Test
    public void shouldReturnUsageBasedPtpPrice(){
        assertThat(priceModel.getUsageBasedPtpPrice().doubleValue(), is(50.0));
    }

    @Test
    public void shouldReturnUsagePrice(){
        assertThat(priceModel.getUsagePrice(), is(usagePrice));
    }

    @Test
    public void shouldFilterOutDummyPriceLines() {
        ArrayList<PricingSheetPriceModel> pricingSheetPriceModels = newArrayList(priceModel, PricingSheetPriceModel.dummyPriceModel());
        assertThat(filter(pricingSheetPriceModels, PricingSheetPriceModel.notDummyPriceModelPredicate()).size(), is(1));
    }

    @Test
    public void shouldHaveGetterMethodsForJxls() throws NoSuchMethodException {
        priceModel.getClass().getMethod("getPmfId");
        priceModel.getClass().getMethod("getContractTerm");
    }

    @Test
    public void shouldGetOwningInstance() throws Exception {
        assertThat(priceModel.getOwningInstance(), is(owningInstance));
    }

    @Test
    public void shouldGetAttributeValueFromOwningInstance() throws Exception {
        assertThat(priceModel.getInstanceCharacteristic("anAttribute"), is("anAttributeValue"));
    }

    @Test
    public void shouldReturnEmptyStringWhenOwningInstanceIsNull() throws Exception {
        assertThat(new PricingSheetPriceModel(null, null, null, null, "12", null, null, null).getInstanceCharacteristic("anAttribute"), is(""));
    }

    @Test
    public void shouldReturnDefaultQuantity() throws Exception {
        final String quantityCharacteristic1 =priceModel.getInstanceCharacteristic("QUANTITY");
        assertThat(quantityCharacteristic1,is("1"));

        final String quantityCharacteristic2 =priceModel.getInstanceCharacteristic("ABC");
        assertThat(quantityCharacteristic2,is(""));
    }

    @Test
    public void shouldReturnNullForUsageBasedChargesByDefault() throws Exception {
        PricingSheetPriceModel model = new PricingSheetPriceModel(null, null, null, null, "12", null, null, null);
        assertThat(model.getMinCharge(), is(nullValue()));
        assertThat(model.getFixedCharge(), is(nullValue()));
        assertThat(model.getChargeRate(), is(nullValue()));
    }

    @Test
    public void shouldGetSummaryDetails() {
        assertThat(priceModel.getSummary(), is(" anAttributeValue"));
    }

    @Test
    public void shouldReturnRemainingContractTerm(){

        Calendar calender = Calendar.getInstance();
        calender.add(Calendar.DATE, -90);
        Date initialBillingStartDate = calender.getTime();
        int asIsContractTerm = 12;
        ProductInstance toBeInstance = new DefaultProductInstanceFixture("productId").withProductInstanceId("productInstanceID")
                                                                                     .withProductInstanceVersion(2L)
                                                                                     .withContractTerm("24")
                                                                                     .withAssetVersionStatus(AssetVersionStatus.DRAFT)
                                                                                     .withInitialBillingStartDate(initialBillingStartDate)
                                                                                     .build();
        ProductInstance asIsInstance = new DefaultProductInstanceFixture("productId").withProductInstanceId("productInstanceID")
                                                                                     .withProductInstanceVersion(1L)
                                                                                     .withContractTerm(String.valueOf(asIsContractTerm))
                                                                                     .withAssetVersionStatus(AssetVersionStatus.IN_SERVICE)
                                                                                     .withInitialBillingStartDate(initialBillingStartDate)
                                                                                     .build();
        priceModel = new PricingSheetPriceModel("M0302165", null, null, usagePrice, "24", "EXISTING", toBeInstance, Optional.of(asIsInstance));
        int actualRemainingMonths = maxRoundedOffMonthsBetween(addMonths(initialBillingStartDate, asIsContractTerm), new Date(currentTimeMillis()));
        assertThat(priceModel.getRemainingContractTerm(), is(actualRemainingMonths));
    }
}
