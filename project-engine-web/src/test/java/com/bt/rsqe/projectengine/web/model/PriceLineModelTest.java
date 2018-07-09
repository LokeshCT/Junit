package com.bt.rsqe.projectengine.web.model;

import com.bt.rsqe.Money;
import com.bt.rsqe.customerinventory.dto.PriceDTO;
import com.bt.rsqe.customerinventory.dto.PriceLineDTO;
import com.bt.rsqe.customerinventory.parameter.PriceLineStatus;
import com.bt.rsqe.domain.product.chargingscheme.PricingStrategy;
import com.bt.rsqe.domain.product.chargingscheme.ProductChargingScheme;
import com.bt.rsqe.enums.CostDiscountType;
import com.bt.rsqe.enums.Currency;
import com.bt.rsqe.enums.PriceCategory;
import com.bt.rsqe.enums.PriceType;
import com.bt.rsqe.pricing.PricingClient;
import com.bt.rsqe.pricing.config.dto.BillingTariffRulesetConfig;
import com.bt.rsqe.pricing.config.dto.PricingConfig;
import com.bt.rsqe.projectengine.web.view.BaseQuoteOptionPricingDTO;
import com.bt.rsqe.projectengine.web.view.QuoteOptionPricingDTO;
import com.bt.rsqe.security.PermissionsDTO;
import com.bt.rsqe.security.UserContext;
import com.bt.rsqe.security.UserContextManager;
import com.bt.rsqe.security.UserPrincipal;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.List;

import static com.bt.rsqe.domain.product.chargingscheme.PricingStrategy.*;
import static com.bt.rsqe.domain.product.chargingscheme.ProductChargingScheme.PriceVisibility.*;
import static com.bt.rsqe.projectengine.web.fixtures.PriceLineDTOFixture.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class PriceLineModelTest {

    public static final String DESCRIPTION = "description";
    public PricingClient pricingClient;
    public PricingConfig pricingConfig;
    private static final String SCHEMA = "schema";
    private String S_CODE = "scode";
    public static final ProductChargingScheme defaultProductChargingScheme = new ProductChargingScheme(SCHEMA, ManagedItem, Customer);
    public static final ProductChargingScheme manualPricingProductChargingScheme = new ProductChargingScheme(SCHEMA, ManualPricing, Customer);

    public List<BillingTariffRulesetConfig> billingTariffRulesetConfigList;

    @Before
    public void before() {
        UserContextManager.setCurrent(new UserContext(new UserPrincipal("loginName"), "token", new PermissionsDTO(true, false, true, false, false, false)));
        pricingClient = mock(PricingClient.class);
        pricingConfig = mock(PricingConfig.class);
        when(pricingClient.getPricingConfig()).thenReturn(pricingConfig);
        PricingConfig.ChargingSchemeFilterCriteria chargingSchemeFilterCriteria = mock(PricingConfig.ChargingSchemeFilterCriteria.class);
        when(pricingConfig.chargingSchemes()).thenReturn(chargingSchemeFilterCriteria);
        when(chargingSchemeFilterCriteria.forName(SCHEMA)).thenReturn(chargingSchemeFilterCriteria);
        when(chargingSchemeFilterCriteria.forScode(S_CODE)).thenReturn(chargingSchemeFilterCriteria);
        BillingTariffRulesetConfig billingTariffRulesetConfig = mock(BillingTariffRulesetConfig.class);
        billingTariffRulesetConfigList = java.util.Arrays.asList(billingTariffRulesetConfig);
        when(chargingSchemeFilterCriteria.billingTariffRulesetConfigs()).thenReturn(billingTariffRulesetConfigList);
    }

    @Test
    public void shouldGetDescriptionFromOneTimePriceLineWhenItExists() throws Exception {
        final PriceLineModel priceLineModel = new PriceLineModel(aPriceLineDTO().with(PriceType.ONE_TIME).withDescription(DESCRIPTION).build(), null, null, pricingClient, true);
        assertThat(priceLineModel.getDescription(), is(DESCRIPTION));
    }

    @Test
    public void shouldGetDescriptionFromRecurringPriceLineWhenOneTimeDoesNotExist() throws Exception {
        final PriceLineModel priceLineModel = new PriceLineModel(null, aPriceLineDTO().with(PriceType.RECURRING).withDescription(DESCRIPTION).build(), null, pricingClient, true);
        assertThat(priceLineModel.getDescription(), is(DESCRIPTION));
    }

    @Test
    public void shouldGetStatusFromOneTimePriceLineWhenItExists() throws Exception {
        final PriceLineModel priceLineModel = new PriceLineModel(aPriceLineDTO().with(PriceType.ONE_TIME).with(PriceLineStatus.BUDGETARY).build(), null, null, pricingClient, true);
        assertThat(priceLineModel.getStatus(), is(PriceLineStatus.BUDGETARY.getDescription()));
    }

    @Test
    public void shouldGetStatusFromRecurringPriceLineWhenOneTimeDoesNotExist() throws Exception {
        final PriceLineModel priceLineModel = new PriceLineModel(null, aPriceLineDTO().with(PriceType.RECURRING).with(PriceLineStatus.BUDGETARY).build(), null, pricingClient, true);
        assertThat(priceLineModel.getStatus(), is(PriceLineStatus.BUDGETARY.getDescription()));
    }

    @Test
    public void shouldSetVendorDiscountReferenceIntoModel() throws Exception {
        final PriceLineDTO oneTimePriceLine = aPriceLineDTO().with(PriceType.ONE_TIME).withVendorDiscountRef("aVendorDiscountRef").withDiscount(PriceCategory.CHARGE_PRICE, 10).withId("id").withPrice(PriceCategory.CHARGE_PRICE, 15).build();
        final PriceLineModel priceLineModel = new PriceLineModel(oneTimePriceLine, null, defaultProductChargingScheme, pricingClient, true);

        final QuoteOptionPricingDTO.PriceLineDTO result = priceLineModel.getOneTimeDto();

        assertThat(result.vendorDiscountRef, is("aVendorDiscountRef"));
    }

    @Test
    public void shouldSetEmptyVendorDiscountReferenceIntoModelWhenNull() throws Exception {
        final PriceLineDTO oneTimePriceLine = aPriceLineDTO().with(PriceType.ONE_TIME).withVendorDiscountRef(null).withDiscount(PriceCategory.CHARGE_PRICE, 10).withId("id").withPrice(PriceCategory.CHARGE_PRICE, 15).build();
        final PriceLineModel priceLineModel = new PriceLineModel(oneTimePriceLine, null, defaultProductChargingScheme, pricingClient, true);

        final QuoteOptionPricingDTO.PriceLineDTO result = priceLineModel.getOneTimeDto();

        assertThat(result.vendorDiscountRef, is(""));
    }

    @Test
    public void shouldMapOneTimeDto() throws Exception {
        final PriceLineDTO oneTimePriceLine = aPriceLineDTO().with(PriceType.ONE_TIME).withDiscount(PriceCategory.CHARGE_PRICE, 10).withId("id").withPrice(PriceCategory.CHARGE_PRICE, 15).build();
        final PriceLineModel priceLineModel = new PriceLineModel(oneTimePriceLine, null, defaultProductChargingScheme, pricingClient, true);

        final QuoteOptionPricingDTO.PriceLineDTO result = priceLineModel.getOneTimeDto();

        assertThat(result.discount, is("10.00000"));
        assertThat(result.value, is("15.00"));
        assertThat(result.netTotal, is("13.50"));
        assertThat(result.id, is("id"));

    }

    @Test
    public void shouldMapRecurringDto() throws Exception {
        final PriceLineDTO recurringDto = aPriceLineDTO().with(PriceType.RECURRING).withDiscount(PriceCategory.CHARGE_PRICE, 10).withId("id").withPrice(PriceCategory.CHARGE_PRICE, 15).build();
        final PriceLineModel priceLineModel = new PriceLineModel(null, recurringDto, defaultProductChargingScheme, pricingClient, true);

        final QuoteOptionPricingDTO.PriceLineDTO result = priceLineModel.getRecurringDto();

        assertThat(result.discount, is("10.00000"));
        assertThat(result.netTotal, is("13.50"));
        assertThat(result.value, is("15.00"));
        assertThat(result.id, is("id"));
    }

    @Test
    public void getUserEnteredFlag() {
        PriceLineDTO nonRecurringDto = aPriceLineDTO().with(PriceType.ONE_TIME).withDiscount(PriceCategory.CHARGE_PRICE, 10).withId("id").withPrice(PriceCategory.CHARGE_PRICE, 15).withUserEntered("Y").build();

        PriceLineModel priceLineModel = new PriceLineModel(nonRecurringDto, null, defaultProductChargingScheme, pricingClient, true);
        assertThat(priceLineModel.getUserEntered(), is("Y"));

        nonRecurringDto = aPriceLineDTO().with(PriceType.ONE_TIME).withDiscount(PriceCategory.CHARGE_PRICE, 10).withId("id").withPrice(PriceCategory.CHARGE_PRICE, 15).build();
        PriceLineDTO recurringDto = aPriceLineDTO().with(PriceType.RECURRING).withDiscount(PriceCategory.CHARGE_PRICE, 10).withId("id").withPrice(PriceCategory.CHARGE_PRICE, 15).withUserEntered("Y").build();

        priceLineModel = new PriceLineModel(nonRecurringDto, recurringDto, defaultProductChargingScheme, pricingClient, true);
        assertThat(priceLineModel.getUserEntered(), is("Y"));

        nonRecurringDto = aPriceLineDTO().with(PriceType.ONE_TIME).withDiscount(PriceCategory.CHARGE_PRICE, 10).withId("id").withPrice(PriceCategory.CHARGE_PRICE, 15).build();
        recurringDto = aPriceLineDTO().with(PriceType.RECURRING).withDiscount(PriceCategory.CHARGE_PRICE, 10).withId("id").withPrice(PriceCategory.CHARGE_PRICE, 15).build();

        priceLineModel = new PriceLineModel(nonRecurringDto, recurringDto, defaultProductChargingScheme, pricingClient, true);
        assertNull(priceLineModel.getUserEntered());

    }

    @Test
    public void shouldReturnEnabledForSalesPriceLines() throws Exception {
        final PriceLineDTO recurringDto = aPriceLineDTO().with(PriceType.RECURRING).withDiscount(PriceCategory.CHARGE_PRICE, 10).withId("id")
            .withPrice(PriceCategory.CHARGE_PRICE, 15).withPmfId(S_CODE).build();
        ProductChargingScheme productChargingScheme = new ProductChargingScheme(SCHEMA, ManagedItem, Sales);
        BillingTariffRulesetConfig billingTariffRulesetConfig = billingTariffRulesetConfigList.get(0);
        when(billingTariffRulesetConfig.getCostDiscountApplicable()).thenReturn(CostDiscountType.RECURRING);
        final PriceLineModel priceLineModel = new PriceLineModel(null, recurringDto, productChargingScheme, pricingClient, true);
        final BaseQuoteOptionPricingDTO.PriceLineDTO result = priceLineModel.getRecurringDto();
        assertTrue(result.discountEnabled);
    }

    @Test
    public void shouldReturnNotEnabledForCustomerAggregatedPriceLines() throws Exception {
        final PriceLineDTO recurringDto = aPriceLineDTO().with(PriceType.RECURRING).withDiscount(PriceCategory.CHARGE_PRICE, 10).withId("id")
            .withPrice(PriceCategory.CHARGE_PRICE, 15).withPmfId(S_CODE).build();
        ProductChargingScheme productChargingScheme = new ProductChargingScheme(SCHEMA, Aggregation, Customer);
        BillingTariffRulesetConfig billingTariffRulesetConfig = billingTariffRulesetConfigList.get(0);
        when(billingTariffRulesetConfig.getCostDiscountApplicable()).thenReturn(CostDiscountType.NONE);
        final PriceLineModel priceLineModel = new PriceLineModel(null, recurringDto, productChargingScheme, pricingClient, true);
        final BaseQuoteOptionPricingDTO.PriceLineDTO result = priceLineModel.getRecurringDto();
        assertFalse(result.discountEnabled);
    }

    @Test
    public void shouldReturnEnabledForSalesAggregatedPriceLines() throws Exception {
        final PriceLineDTO recurringDto = aPriceLineDTO().with(PriceType.RECURRING).withDiscount(PriceCategory.CHARGE_PRICE, 10).withId("id")
            .withPrice(PriceCategory.CHARGE_PRICE, 15).withPmfId(S_CODE).build();
        ProductChargingScheme productChargingScheme = new ProductChargingScheme(SCHEMA, Aggregation, Sales);
        BillingTariffRulesetConfig billingTariffRulesetConfig = billingTariffRulesetConfigList.get(0);
        when(billingTariffRulesetConfig.getCostDiscountApplicable()).thenReturn(CostDiscountType.NONE);
        final PriceLineModel priceLineModel = new PriceLineModel(null, recurringDto, productChargingScheme, pricingClient, true);
        final BaseQuoteOptionPricingDTO.PriceLineDTO result = priceLineModel.getRecurringDto();
        assertTrue(result.discountEnabled);
    }

    @Test
    public void shouldReturnEnabledForCustomerNonAggregatedPriceLines() throws Exception {
        //When
        final PriceLineDTO recurringDto = aPriceLineDTO().with(PriceType.RECURRING).withDiscount(PriceCategory.CHARGE_PRICE, 10).withId("id")
            .withPrice(PriceCategory.CHARGE_PRICE, 15).withPmfId(S_CODE).build();
        ProductChargingScheme productChargingScheme = new ProductChargingScheme(SCHEMA, ManagedItem, Customer);
        BillingTariffRulesetConfig billingTariffRulesetConfig = billingTariffRulesetConfigList.get(0);
        when(billingTariffRulesetConfig.getCostDiscountApplicable()).thenReturn(CostDiscountType.RECURRING);
        final PriceLineModel priceLineModel = new PriceLineModel(null, recurringDto, productChargingScheme, pricingClient, true);
        //Then
        final BaseQuoteOptionPricingDTO.PriceLineDTO result = priceLineModel.getRecurringDto();
        assertTrue(result.discountEnabled);
    }

    @Test
    public void shouldRetrieveOneTimeRrpForIndirectOrder() throws Exception {
        final PriceLineDTO oneTimePriceLine = aPriceLineDTO().with(PriceType.ONE_TIME).withId("id").withPrice(PriceCategory.END_USER_PRICE, 10).build();
        final PriceLineDTO recurringPriceLine = aPriceLineDTO().with(PriceType.RECURRING).withId("id").withPrice(PriceCategory.END_USER_PRICE, 15).build();
        final PriceLineModel priceLineModel = new PriceLineModel(oneTimePriceLine, recurringPriceLine, null, pricingClient, true);
        assertThat(priceLineModel.getGrossOneTimeEUP(), is(Money.from("10")));
    }

    @Test
    public void shouldRetrieveRecurringTimeRrpForIndirectOrder() throws Exception {
        final PriceLineDTO oneTimePriceLine = aPriceLineDTO().with(PriceType.ONE_TIME).withId("id").withPrice(PriceCategory.END_USER_PRICE, 10).build();
        final PriceLineDTO recurringPriceLine = aPriceLineDTO().with(PriceType.RECURRING).withId("id").withPrice(PriceCategory.END_USER_PRICE, 15).build();
        final PriceLineModel priceLineModel = new PriceLineModel(oneTimePriceLine, recurringPriceLine, null, pricingClient, true);
        assertThat(priceLineModel.getGrossRecurringEUP(), is(Money.from("15")));
    }

    @Test
    public void shouldRetrieveOneTimeValue() throws Exception {
        final PriceLineDTO oneTimePriceLine = aPriceLineDTO().with(PriceType.ONE_TIME).withId("id").withPrice(PriceCategory.CHARGE_PRICE, 10).build();
        final PriceLineModel priceLineModel = new PriceLineModel(oneTimePriceLine, null, null, pricingClient, true);
        assertThat(priceLineModel.getOneTimeCPValue(), is(Money.from("10")));
    }

    @Test
    public void shouldReturnZeroForOneTimeValueWhenNoOneTimeValueExists() throws Exception {
        final PriceLineModel priceLineModel = new PriceLineModel(null, aPriceLineDTO().with(PriceType.RECURRING).build(), null, null, true);
        assertThat(priceLineModel.getOneTimeCPValue(), is(Money.from("0")));
    }

    @Test
    public void shouldRetrieveRecurringValue() throws Exception {
        final PriceLineDTO recurringPriceLine = aPriceLineDTO().with(PriceType.RECURRING).withId("id").withPrice(PriceCategory.CHARGE_PRICE, 10).build();
        final PriceLineModel priceLineModel = new PriceLineModel(null, recurringPriceLine, null, pricingClient, true);
        assertThat(priceLineModel.getRecurringCPValue(), is(Money.from("10")));
    }

    @Test
    public void shouldReturnZeroForRecurringValueWhenNoRecurringValueExists() throws Exception {
        final PriceLineModel priceLineModel = new PriceLineModel(aPriceLineDTO().with(PriceType.ONE_TIME).build(), null, null, pricingClient, true);
        assertThat(priceLineModel.getRecurringCPValue(), is(Money.ZERO));
    }

    @Test
    public void shouldRetrieveTotalChargeForSpecificContractTermPTP() throws Exception {
        final PriceLineDTO oneTimePriceLine = aPriceLineDTO().with(PriceType.ONE_TIME).withId("id").withPrice(PriceCategory.CHARGE_PRICE, 10).build();
        final PriceLineDTO recurringPriceLine = aPriceLineDTO().with(PriceType.RECURRING).withId("id").withPrice(PriceCategory.CHARGE_PRICE, 15).build();
        final PriceLineModel priceLineModel = new PriceLineModel(oneTimePriceLine, recurringPriceLine, null, pricingClient, true);

        final int contractTerm = 4;
        assertThat(priceLineModel.getTotalChargePrice(contractTerm), is(Money.from("70")));
    }

    @Test
    public void shouldRetrieveTotalChargeForSpecificContractTermRRP() throws Exception {
        final PriceLineDTO oneTimePriceLine = aPriceLineDTO().with(PriceType.ONE_TIME).withId("id").withPrice(PriceCategory.END_USER_PRICE, 10).build();
        final PriceLineDTO recurringPriceLine = aPriceLineDTO().with(PriceType.RECURRING).withId("id").withPrice(PriceCategory.END_USER_PRICE, 15).build();
        final PriceLineModel priceLineModel = new PriceLineModel(oneTimePriceLine, recurringPriceLine, null, pricingClient, true);

        final int contractTerm = 4;
        assertThat(priceLineModel.getTotalEUP(contractTerm), is(Money.from("70")));
    }

    @Test
    public void shouldGetPPSRId() throws Exception {
        final PriceLineDTO oneTimePriceLine = aPriceLineDTO().with(PriceType.ONE_TIME).withId("id").withPrice(PriceCategory.END_USER_PRICE, 10).withPpsrId(132).build();

        PriceLineModel priceLineModel = new PriceLineModel(oneTimePriceLine, null, null, pricingClient, true);
        assertTrue(priceLineModel.getPpsrId().equals(Long.valueOf("132")));

        final PriceLineDTO recurringPriceLine = aPriceLineDTO().with(PriceType.RECURRING).withId("id").withPrice(PriceCategory.END_USER_PRICE, 15).withPpsrId(121).build();
        priceLineModel = new PriceLineModel(null, recurringPriceLine, null, pricingClient, true);
        assertTrue(priceLineModel.getPpsrId().equals(Long.valueOf("121")));
    }

    @Test
    public void shouldReturnRoundedHalfEvenChargePerChannelRRP() throws Exception {
        final PriceLineDTO oneTimePriceLine = aPriceLineDTO().with(PriceType.ONE_TIME).withPrice(PriceCategory.END_USER_PRICE, 25).build();
        final PriceLineDTO recurringPriceLine = aPriceLineDTO().with(PriceType.RECURRING).withPrice(PriceCategory.END_USER_PRICE, 17).build();
        PriceLineModel priceLineModel = new PriceLineModel(oneTimePriceLine, recurringPriceLine, null, pricingClient, true);


        final Money chargePerChannelRRP = priceLineModel.getEUPPerChannel(7, 24);
        assertThat(chargePerChannelRRP, is(Money.from("61.86")));
    }

    @Test
    public void shouldReturnRoundedHalfEvenChargePerChannelPTP() throws Exception {
        final PriceLineDTO oneTimePriceLine = aPriceLineDTO().with(PriceType.ONE_TIME).withPrice(PriceCategory.CHARGE_PRICE, 25).build();
        final PriceLineDTO recurringPriceLine = aPriceLineDTO().with(PriceType.RECURRING).withPrice(PriceCategory.CHARGE_PRICE, 17).build();
        PriceLineModel priceLineModel = new PriceLineModel(oneTimePriceLine, recurringPriceLine, null, pricingClient, true);


        final Money chargePerChannelRRP = priceLineModel.getChargePricePerChannel(7, 24);
        assertThat(chargePerChannelRRP, is(Money.from("61.86")));
    }

    @Test
    public void checkPriceLineIsACustomerAggregatedPrice() {
        final PriceLineDTO oneTimePriceLine = aPriceLineDTO().with(PriceType.ONE_TIME).withPrice(PriceCategory.CHARGE_PRICE, 25).build();
        final PriceLineDTO recurringPriceLine = aPriceLineDTO().with(PriceType.RECURRING).withPrice(PriceCategory.CHARGE_PRICE, 17).build();
        ProductChargingScheme scheme = new ProductChargingScheme("Test Scheme", Aggregation, Customer);
        PriceLineModel priceLineModel = new PriceLineModel(oneTimePriceLine, recurringPriceLine, scheme, pricingClient, true);

        assertThat(priceLineModel.isCustomerAggregatedPrice(), is(true));
    }

    @Test
    public void shouldCreatePriceLineForGivenPrice() throws Exception {
        final PriceLineDTO recurringPriceLine = aPriceLineDTO().with(PriceType.RECURRING).withPrice(PriceCategory.CHARGE_PRICE, 17).withVendorDiscountRef("aVendorDiscountRef").build();
        ProductChargingScheme scheme = new ProductChargingScheme(SCHEMA, PricingStrategy.UsageManagedItem, Customer);

        PriceDTO price = new PriceDTO(PriceCategory.FIXED_CHARGE, Currency.GBP, "V1", new BigDecimal("10"), new BigDecimal("5"));

        PriceLineModel priceLineModel = new PriceLineModel(null, recurringPriceLine, scheme, pricingClient, true);
        BaseQuoteOptionPricingDTO.PriceLineDTO priceLineDTO = priceLineModel.createPriceFor(price);
        assertThat(priceLineDTO.value, is("10.00"));
        assertThat(priceLineDTO.netTotal, is("9.50"));
        assertThat(priceLineDTO.discount, is("5.00000"));
        assertThat(priceLineDTO.vendorDiscountRef, is("aVendorDiscountRef"));
    }

    @Test
    public void shouldReturnEnabledForOneTimeAndDisabledForRecurringPriceLines() throws Exception {
        //When
        when(pricingConfig.getAction(SCHEMA)).thenReturn("ADD");
        final PriceLineDTO recurringDto = aPriceLineDTO().with(PriceType.RECURRING).withDiscount(PriceCategory.CHARGE_PRICE, 10).withId("id")
            .withPrice(PriceCategory.CHARGE_PRICE, 15).withPmfId(S_CODE).build();
        final PriceLineDTO oneTimeDto = aPriceLineDTO().with(PriceType.ONE_TIME).withDiscount(PriceCategory.CHARGE_PRICE, 10).withId("id")
            .withPrice(PriceCategory.CHARGE_PRICE, 15).withPmfId(S_CODE).build();
        ProductChargingScheme productChargingScheme = new ProductChargingScheme(SCHEMA, ManagedItem, Customer);
        BillingTariffRulesetConfig billingTariffRulesetConfig = billingTariffRulesetConfigList.get(0);
        when(billingTariffRulesetConfig.getId()).thenReturn("scode");
        when(billingTariffRulesetConfig.getCostDiscountApplicable()).thenReturn(CostDiscountType.ONE_TIME);
        PriceLineModel priceLineModel = new PriceLineModel(null, recurringDto, productChargingScheme, pricingClient, true);
        BaseQuoteOptionPricingDTO.PriceLineDTO result = priceLineModel.getRecurringDto();
        assertFalse(result.discountEnabled);

        priceLineModel = new PriceLineModel(null, oneTimeDto, productChargingScheme, pricingClient, true);
        result = priceLineModel.getRecurringDto();
        assertTrue(result.discountEnabled);
    }

     @Test
    public void shouldReturnEnabledForBothOneTimeAndForRecurringPriceLines() throws Exception {
        final PriceLineDTO recurringDto = aPriceLineDTO().with(PriceType.RECURRING).withDiscount(PriceCategory.CHARGE_PRICE, 10).withId("id")
            .withPrice(PriceCategory.CHARGE_PRICE, 15).withPmfId(S_CODE).build();
        final PriceLineDTO oneTimeDto = aPriceLineDTO().with(PriceType.ONE_TIME).withDiscount(PriceCategory.CHARGE_PRICE, 10).withId("id")
            .withPrice(PriceCategory.CHARGE_PRICE, 15).withPmfId(S_CODE).build();
        ProductChargingScheme productChargingScheme = new ProductChargingScheme(SCHEMA, ManagedItem, Customer);
        BillingTariffRulesetConfig billingTariffRulesetConfig = billingTariffRulesetConfigList.get(0);
        when(billingTariffRulesetConfig.getCostDiscountApplicable()).thenReturn(CostDiscountType.BOTH);
        PriceLineModel priceLineModel = new PriceLineModel(null, recurringDto, productChargingScheme, pricingClient, true);
        BaseQuoteOptionPricingDTO.PriceLineDTO result = priceLineModel.getRecurringDto();
        assertTrue(result.discountEnabled);

        priceLineModel = new PriceLineModel(null, oneTimeDto, productChargingScheme, pricingClient, true);
        result = priceLineModel.getRecurringDto();
        assertTrue(result.discountEnabled);
    }

    @Test
    public void shouldReturnDisabledWhenDiscountIsApplicableButAssetIsNotAProvide() throws Exception {
        final PriceLineDTO recurringDto = aPriceLineDTO().with(PriceType.RECURRING)
                                                        .withDiscount(PriceCategory.CHARGE_PRICE, 10)
                                                        .withId("id")
                                                        .withPrice(PriceCategory.CHARGE_PRICE, 15)
                                                        .withPmfId(S_CODE).build();

        ProductChargingScheme productChargingScheme = new ProductChargingScheme(SCHEMA, ManagedItem, Customer);
        BillingTariffRulesetConfig billingTariffRulesetConfig = billingTariffRulesetConfigList.get(0);
        when(billingTariffRulesetConfig.getId()).thenReturn("scode");
        when(billingTariffRulesetConfig.getCostDiscountApplicable()).thenReturn(CostDiscountType.RECURRING);

        PriceLineModel priceLineModel = new PriceLineModel(null, recurringDto, productChargingScheme, pricingClient, false);
        BaseQuoteOptionPricingDTO.PriceLineDTO result = priceLineModel.getRecurringDto();
        assertFalse(result.discountEnabled);
    }

    @Test
    public void shouldReturnFalseIfNotAnManualPricing() throws Exception {
        PriceLineDTO oneTimePriceLine = aPriceLineDTO().with(PriceType.ONE_TIME).withVendorDiscountRef(null).withDiscount(PriceCategory.CHARGE_PRICE, 10).withId("id").withPrice(PriceCategory.CHARGE_PRICE, 15).build();
        PriceLineModel priceLineModel = new PriceLineModel(oneTimePriceLine, null, manualPricingProductChargingScheme, pricingClient, true);

        assertTrue(priceLineModel.isManualPricing());

        priceLineModel = new PriceLineModel(oneTimePriceLine, null, defaultProductChargingScheme, pricingClient, true);
        assertFalse(priceLineModel.isManualPricing());

        priceLineModel = new PriceLineModel(oneTimePriceLine, null, null, pricingClient, true);
        assertFalse(priceLineModel.isManualPricing());
    }
}
