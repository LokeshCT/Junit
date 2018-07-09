package com.bt.rsqe.projectengine.web.quoteoption.bcmsheet;

import com.bt.rsqe.domain.bom.parameters.OrderType;
import com.bt.rsqe.domain.product.BillingTariffRuleSet;
import com.bt.rsqe.domain.product.PriceType;
import com.bt.rsqe.domain.product.chargingscheme.PricingStrategy;
import com.bt.rsqe.domain.product.chargingscheme.ProductChargingScheme;
import com.bt.rsqe.domain.project.PriceLine;
import com.bt.rsqe.domain.project.ProductInstance;
import com.bt.rsqe.projectengine.web.quoteoptionpricing.pricingsheet.PricingSheetTestDataFixture;
import com.bt.rsqe.security.UserContext;
import com.bt.rsqe.security.UserContextManager;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

import static com.bt.rsqe.security.UserContextBuilder.*;
import static org.hamcrest.core.Is.*;
import static org.junit.Assert.*;

public class BCMPriceModelTest {

    private BCMPriceModel bcmPriceModelWithValue;
    private ProductChargingScheme productChargingScheme;
    PriceLine oneTimePriceLine;
    PriceLine recurringPrice;
    ProductInstance productInstance;
    private UserContext userContext;

    @Before
    public void setUp() {
        PricingSheetTestDataFixture pricingSheetTestDataFixture = new PricingSheetTestDataFixture();
        oneTimePriceLine = pricingSheetTestDataFixture.aPriceLine("Root Product One time price", "M0302165", 100.00, PriceType.ONE_TIME, "Recommended Retail Price", 332.00, "A", "1");
        recurringPrice = pricingSheetTestDataFixture.aPriceLine("Root Product Rental price", "M0302165", 50.00, PriceType.RECURRING, "Recommended Retail Price", 333.00, "A", "2");
        productChargingScheme = new ProductChargingScheme("A", PricingStrategy.Aggregation, "ABC", ProductChargingScheme.PriceVisibility.Sales, "", new ArrayList<BillingTariffRuleSet>(), null);
        productInstance = pricingSheetTestDataFixture.anInstallableRootProductWithAChild();
        bcmPriceModelWithValue = new BCMPriceModel(oneTimePriceLine, recurringPrice, "price", productChargingScheme, productInstance, OrderType.PROVIDE.name());

    }

    @Test
    public void shouldGiveAllDetailsAboutRecurringAndNonRecurringPriceForIndirect() {
        userContext = anIndirectUserContext().withIndirectUser().withLoginName("INDIRECT_USER").build();
        UserContextManager.setCurrent(userContext);
        assertThat(bcmPriceModelWithValue.getOnetimeEUPPrice(), is("100.00"));
        assertThat(String.valueOf(bcmPriceModelWithValue.getOneTimeDiscount()), is("0"));
        assertThat(String.valueOf(bcmPriceModelWithValue.getMonthlyDiscount()), is("0"));
        assertThat(bcmPriceModelWithValue.getOneTimePriceLineId(), is("1"));
        assertThat(bcmPriceModelWithValue.getOneTimePTPPrice(), is("332.00"));
        assertThat(bcmPriceModelWithValue.getPriceDescription(), is("Root Product One time price"));
        assertThat(bcmPriceModelWithValue.getRecurringEUPPrice(), is("50.00"));
        assertThat(bcmPriceModelWithValue.getRecurringPriceLineId(), is("2"));
        assertThat(bcmPriceModelWithValue.getRecurringPTPPrice(), is("333.00"));
        assertThat(bcmPriceModelWithValue.getScheme().getName(), is("A"));
        assertThat(bcmPriceModelWithValue.getTariffType(), is("price"));
        assertThat(bcmPriceModelWithValue.getVisibility(), is("Sales"));
        assertThat(bcmPriceModelWithValue.getScheme(), is(productChargingScheme));
        assertThat(bcmPriceModelWithValue.getMonthlyPriceLine(), is(recurringPrice));
        assertThat(bcmPriceModelWithValue.getOneTimePriceLine(), is(oneTimePriceLine));
        assertThat(bcmPriceModelWithValue.getProductInstance(), is(productInstance));
        assertThat(bcmPriceModelWithValue.getPriceBookVersion(), is(""));
        assertThat(bcmPriceModelWithValue.getPrimaryTariffZone(), is(""));
    }

    @Test
    public void shouldGiveAllDetailsAboutRecurringAndNonRecurringPriceForDirect() {
        userContext = aDirectUserContext().build();
        UserContextManager.setCurrent(userContext);
        assertThat(bcmPriceModelWithValue.getOnetimeEUPPrice(), is("332.00"));
        assertThat(String.valueOf(bcmPriceModelWithValue.getOneTimeDiscount()), is("0"));
        assertThat(String.valueOf(bcmPriceModelWithValue.getMonthlyDiscount()), is("0"));
        assertThat(bcmPriceModelWithValue.getOneTimePriceLineId(), is("1"));
        assertThat(bcmPriceModelWithValue.getOneTimePTPPrice(), is("100.00"));
        assertThat(bcmPriceModelWithValue.getPriceDescription(), is("Root Product One time price"));
        assertThat(bcmPriceModelWithValue.getRecurringEUPPrice(), is("333.00"));
        assertThat(bcmPriceModelWithValue.getRecurringPriceLineId(), is("2"));
        assertThat(bcmPriceModelWithValue.getRecurringPTPPrice(), is("50.00"));
        assertThat(bcmPriceModelWithValue.getScheme().getName(), is("A"));
        assertThat(bcmPriceModelWithValue.getTariffType(), is("price"));
        assertThat(bcmPriceModelWithValue.getVisibility(), is("Sales"));
        assertThat(bcmPriceModelWithValue.getScheme(), is(productChargingScheme));
        assertThat(bcmPriceModelWithValue.getMonthlyPriceLine(), is(recurringPrice));
        assertThat(bcmPriceModelWithValue.getOneTimePriceLine(), is(oneTimePriceLine));
        assertThat(bcmPriceModelWithValue.getProductInstance(), is(productInstance));
        assertThat(bcmPriceModelWithValue.getPriceBookVersion(), is(""));
        assertThat(bcmPriceModelWithValue.getPrimaryTariffZone(), is(""));
    }

    @Test
    public void shouldCheckNullPriceLineValues(){
        PricingSheetTestDataFixture pricingSheetTestDataFixture = new PricingSheetTestDataFixture();
        productChargingScheme = new ProductChargingScheme("A", PricingStrategy.Aggregation, "ABC", ProductChargingScheme.PriceVisibility.Sales, "", new ArrayList<BillingTariffRuleSet>(), null);
        productInstance = pricingSheetTestDataFixture.anInstallableRootProductWithAChild();
        bcmPriceModelWithValue = new BCMPriceModel(null, null, "price", productChargingScheme, productInstance, OrderType.PROVIDE.name());
        assertThat(String.valueOf(bcmPriceModelWithValue.getMonthlyDiscount()), is("0"));
        assertNull(bcmPriceModelWithValue.getOnetimeEUPPrice());
        assertNull(bcmPriceModelWithValue.getOneTimePTPPrice());
        assertThat(bcmPriceModelWithValue.getRecurringPrice(),is(""));
        assertThat(bcmPriceModelWithValue.getNonRecurringPrice(),is(""));
        assertNull(bcmPriceModelWithValue.getRecurringPTPPrice());
        assertNull(bcmPriceModelWithValue.getRecurringEUPPrice());
        assertThat(String.valueOf(bcmPriceModelWithValue.getMonthlyDiscount()), is("0"));
    }
}
