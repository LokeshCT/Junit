package com.bt.rsqe.customerinventory.service.updates;

import com.bt.rsqe.customerinventory.parameter.PriceLineStatus;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAsset;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetPriceLine;
import com.bt.rsqe.customerinventory.service.client.fixtures.CIFAssetFixture;
import com.bt.rsqe.customerinventory.service.externals.PmrHelper;
import com.bt.rsqe.domain.ChargingScheme;
import com.bt.rsqe.domain.product.ProductOffering;
import com.bt.rsqe.domain.product.SimpleProductOfferingType;
import com.bt.rsqe.domain.product.chargingscheme.PricingStrategy;
import com.bt.rsqe.domain.product.chargingscheme.ProductChargingScheme;
import com.bt.rsqe.domain.product.parameters.ProductIdentifier;
import com.bt.rsqe.domain.project.PricingStatus;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static com.bt.rsqe.domain.product.ProductOffering.SPECIAL_BID_ATTRIBUTE_INDICATOR;
import static com.bt.rsqe.domain.product.ProductOffering.CPE_OPTION;
import static com.bt.rsqe.domain.product.ProductOffering.CUSTOMER_OWNED_CPE;
import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PricingStatusHelperTest
{
    PmrHelper pmrHelper = mock(PmrHelper.class) ;

    @Before
    public void setUp () throws Exception
    {
    }

    private void checkAllPriceLineStatusInvalidated (CIFAsset cifAsset, PriceLineStatus priceLineStatus)
    {
        for (CIFAssetPriceLine cifAssetPriceLine : cifAsset.getPriceLines())
        {
            assertThat (cifAssetPriceLine.getStatus(), is(priceLineStatus)) ;
        }
    }

    @Test
    public void shouldSetToNotPricedWhenFirmAndNotSpecialBitWithdrawnAndPricable()
    {
        // Setup
        PricingStatusHelper helper = new PricingStatusHelper(pmrHelper) ;
        CIFAsset cifAsset = CIFAssetFixture.aCIFAsset().
                withPricingStatus(PricingStatus.FIRM).
                withPriceLine(PriceLineStatus.FIRM).
                withRelationships(0).
                build() ;
        ProductOffering productOffering = ProductOffering.Builder.
                offeringFor(new ProductIdentifier("S1234", "aproduct", "1")).
                withChargingSchemes(newArrayList(new ProductChargingScheme("name", PricingStrategy.LocalRuleBasedPricing, ProductChargingScheme.PriceVisibility.Customer))).
                buildOffering() ;
        when (pmrHelper.getProductOffering(cifAsset)).thenReturn(productOffering) ;

        // Execution
        helper.refreshPricingStatusBasedOnPriceLines(cifAsset);

        // Assertion
        assertThat (cifAsset.getPricingStatus(), is(PricingStatus.NOT_PRICED)) ;
        checkAllPriceLineStatusInvalidated (cifAsset, PriceLineStatus.IN_VALIDATED) ;
    }

    @Test
    public void shouldSetToNotPricedWhenNotPricedAndNotSpecialBitWithdrawnAndPricable()
    {
        // Setup
        PricingStatusHelper helper = new PricingStatusHelper(pmrHelper) ;
        CIFAsset cifAsset = CIFAssetFixture.aCIFAsset().
                withPricingStatus(PricingStatus.NOT_PRICED).
                withPriceLine(PriceLineStatus.NOT_PRICED).
                withRelationships(0).
                build() ;
        ProductOffering productOffering = ProductOffering.Builder.
                offeringFor(new ProductIdentifier("S1234", "aproduct", "1")).
                withChargingSchemes(newArrayList(new ProductChargingScheme("name", PricingStrategy.LocalRuleBasedPricing, ProductChargingScheme.PriceVisibility.Customer))).
                buildOffering() ;
        when (pmrHelper.getProductOffering(cifAsset)).thenReturn(productOffering) ;

        // Execution
        helper.refreshPricingStatusBasedOnPriceLines(cifAsset);

        // Assertion
        assertThat (cifAsset.getPricingStatus(), is(PricingStatus.NOT_PRICED)) ;
        checkAllPriceLineStatusInvalidated (cifAsset, PriceLineStatus.IN_VALIDATED) ;
    }

    @Test
    public void shouldBeLeftAloneWhenNotApplicabale()
    {
        // Setup
        PricingStatusHelper helper = new PricingStatusHelper(pmrHelper) ;
        CIFAsset cifAsset = CIFAssetFixture.aCIFAsset().
                withPricingStatus(PricingStatus.NOT_APPLICABLE).
                withPriceLines(0).
                withRelationships(0).
                build() ;
        ProductOffering productOffering = mock (ProductOffering.class);
        when (productOffering.isPriceable()).thenReturn(false) ;
        when (pmrHelper.getProductOffering(cifAsset)).thenReturn(productOffering) ;

        // Execution
        helper.refreshPricingStatusBasedOnPriceLines(cifAsset);

        // Assertion
        assertThat(cifAsset.getPricingStatus(), is(PricingStatus.NOT_APPLICABLE)) ;
    }


    @Test
    public void shouldLeaveSpecialBidWithdrawn () throws Exception
    {
        // Setup
        PricingStatusHelper helper = new PricingStatusHelper(pmrHelper) ;
        CIFAsset cifAsset = CIFAssetFixture.aCIFAsset().
                withPricingStatus(PricingStatus.WITHDRAWN).
                withCharacteristic(SPECIAL_BID_ATTRIBUTE_INDICATOR, "Yes").
                withPriceLines(0).
                withRelationships(0).
                build() ;
        ProductOffering productOffering = ProductOffering.Builder.offeringFor(new ProductIdentifier("S1234", "Name1", "v1")).buildOffering() ;

        when (pmrHelper.getProductOffering(cifAsset)).thenReturn(productOffering) ;

        // Execution
        helper.refreshPricingStatusBasedOnPriceLines(cifAsset);

        // Assertion
        assertThat (cifAsset.getPricingStatus(), is(PricingStatus.WITHDRAWN)) ;


    }

    @Test
    @Ignore("Until logic is verified to be correct")
    public void shouldLeaveSpecialBidProgressingForNonPricable () throws Exception
    {
        // Setup
        PricingStatusHelper helper = new PricingStatusHelper(pmrHelper) ;
        CIFAsset cifAsset = CIFAssetFixture.aCIFAsset().
                withPricingStatus(PricingStatus.PROGRESSING).
                withCharacteristic(SPECIAL_BID_ATTRIBUTE_INDICATOR, "Yes").
                withCharacteristic(CPE_OPTION, CUSTOMER_OWNED_CPE).
                withPriceLines(0).
                withRelationships(0).
                build() ;
        ProductOffering productOffering = ProductOffering.Builder.
                offeringFor(new ProductIdentifier("S1234", "Name1", "v1")).
                withSimpleProductOfferingType(SimpleProductOfferingType.NetworkNode).
                withChargingSchemes(newArrayList(new ProductChargingScheme("name", PricingStrategy.LocalRuleBasedPricing, ProductChargingScheme.PriceVisibility.Customer))).
                buildOffering() ;

        when (pmrHelper.getProductOffering(cifAsset)).thenReturn(productOffering) ;

        // Execution
        helper.refreshPricingStatusBasedOnPriceLines(cifAsset);

        // Assertion
        assertThat (cifAsset.getPricingStatus(), is(PricingStatus.PROGRESSING)) ;


    }
}