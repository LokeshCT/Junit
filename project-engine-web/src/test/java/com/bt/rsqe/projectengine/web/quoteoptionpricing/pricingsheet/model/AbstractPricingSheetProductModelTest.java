package com.bt.rsqe.projectengine.web.quoteoptionpricing.pricingsheet.model;

import com.bt.rsqe.domain.bom.fixtures.ProductOfferingFixture;
import com.bt.rsqe.domain.order.ItemPrice;
import com.bt.rsqe.domain.product.DefaultProductInstanceFixture;
import com.bt.rsqe.domain.product.ProductOffering;
import com.bt.rsqe.domain.product.chargingscheme.PricingStrategy;
import com.bt.rsqe.domain.product.chargingscheme.ProductChargingScheme;
import com.bt.rsqe.domain.product.fixtures.ProductChargingSchemeFixture;
import com.bt.rsqe.domain.project.InstanceCharacteristicNotFound;
import com.bt.rsqe.domain.project.PriceLine;
import com.bt.rsqe.domain.project.ProductInstance;
import com.bt.rsqe.enums.PriceCategory;
import com.bt.rsqe.integration.PriceLineFixture;
import com.bt.rsqe.pricing.PricingClient;
import com.bt.rsqe.pricing.config.dto.ChargingSchemeConfig;
import com.bt.rsqe.pricing.config.dto.PricingConfig;
import com.bt.rsqe.productinstancemerge.ChangeType;
import com.bt.rsqe.productinstancemerge.MergeResult;
import com.bt.rsqe.productinstancemerge.changetracker.ChangeTracker;
import com.bt.rsqe.projectengine.QuoteOptionItemDTOFixture;
import com.bt.rsqe.projectengine.web.quoteoptionpricing.pricingsheet.PriceType;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import org.junit.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.List;

import static com.google.common.collect.Lists.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.*;

public class AbstractPricingSheetProductModelTest {
    private ProductInstance productInstance;


    @Test
    public void shouldReplaceUsagePriceLinesWithTieredPriceLines() throws Exception {
        PriceLine priceLine = PriceLineFixture.aPriceLine().withEupPrice(10.0).withPriceType(com.bt.rsqe.domain.product.PriceType.USAGE_BASED).withChargingSchemeName("usageScheme").build();
        priceLine.getUsageCharges().add(new com.bt.rsqe.domain.project.Price(null, new BigDecimal("100"), new BigDecimal("50"), "Tier 2", null, null, PriceCategory.FIXED_CHARGE.getLabel()));
        priceLine.getUsageCharges().add(new com.bt.rsqe.domain.project.Price(null, new BigDecimal("50"), new BigDecimal("50"), "Tier 2", null, null, PriceCategory.MIN_CHARGE.getLabel()));
        priceLine.getUsageCharges().add(new com.bt.rsqe.domain.project.Price(null, new BigDecimal("200"), new BigDecimal("50"), "Tier 1", null, null, PriceCategory.FIXED_CHARGE.getLabel()));
        priceLine.getUsageCharges().add(new com.bt.rsqe.domain.project.Price(null, new BigDecimal("150"), new BigDecimal("50"), "Tier 1", null, null, PriceCategory.MIN_CHARGE.getLabel()));

        ProductInstance owner = DefaultProductInstanceFixture.aProductInstance()
                                                             .withProductOffering(ProductOfferingFixture.aProductOffering()
                                                                                                        .withChargingScheme(ProductChargingSchemeFixture.aChargingScheme()
                                                                                                                                                        .withName("usageScheme")
                                                                                                                                                        .withPricingStrategy(PricingStrategy.UsageManagedItem)
                                                                                                                                                        .withPriceVisibility(ProductChargingScheme.PriceVisibility.Customer)
                                                                                                                                                        .build()))
                                                             .withPriceLines(newArrayList(priceLine))
                                                             .build();

        ChangeTracker changeTracker = mock(ChangeTracker.class);
        when(changeTracker.changeFor(any(ItemPrice.class))).thenReturn(ChangeType.ADD);
        MergeResult mergeResult = new MergeResult(Lists.<ProductInstance>newArrayList(), changeTracker);

        PricingClient pricingClient = mock(PricingClient.class);
        PricingConfig pricingConfig = mock(PricingConfig.class);
        PricingConfig.ChargingSchemeFilterCriteria chargingSchemeFilterCriteria = mock(PricingConfig.ChargingSchemeFilterCriteria.class);
        when(chargingSchemeFilterCriteria.forName("usageScheme")).thenReturn(chargingSchemeFilterCriteria);
        when(chargingSchemeFilterCriteria.forIfc("Y")).thenReturn(chargingSchemeFilterCriteria);
        when(chargingSchemeFilterCriteria.search()).thenReturn(Lists.<ChargingSchemeConfig>newArrayList());
        when(pricingConfig.chargingSchemes()).thenReturn(chargingSchemeFilterCriteria);
        when(pricingClient.getPricingConfig()).thenReturn(pricingConfig);

        AbstractPricingSheetProductModel pricingSheetProductModel = new AbstractPricingSheetProductModel(null, owner, QuoteOptionItemDTOFixture.aQuoteOptionItemDTO().build(), mergeResult, pricingClient, null);
        List<PricingSheetPriceModel> pricingSheetPriceModels = pricingSheetProductModel.getAllDetailSheetPriceLines(PriceType.NEW.name());

        assertThat(pricingSheetPriceModels.size(), is(2));

        UsageChargeTierPricingSheetModel tierModel1 = (UsageChargeTierPricingSheetModel)pricingSheetPriceModels.get(0);
        assertThat(tierModel1.getMinCharge().intValue(), is(75));
        assertThat(tierModel1.getFixedCharge().intValue(), is(100));

        UsageChargeTierPricingSheetModel tierModel2 = (UsageChargeTierPricingSheetModel)pricingSheetPriceModels.get(1);
        assertThat(tierModel2.getMinCharge().intValue(), is(25));
        assertThat(tierModel2.getFixedCharge().intValue(), is(50));

    }
    @Test
    public void shouldGetQuantityInstanceQuantityInstanceCharacteristic()
    {
        PriceLine priceLine = PriceLineFixture.aPriceLine().withEupPrice(10.0).withPriceType(com.bt.rsqe.domain.product.PriceType.USAGE_BASED).withChargingSchemeName("usageScheme").build();
        ProductInstance owner = DefaultProductInstanceFixture.aProductInstance()
                                                             .withProductOffering(ProductOfferingFixture.aProductOffering()
                                                                                                        .withChargingScheme(ProductChargingSchemeFixture.aChargingScheme()
                                                                                                                                                        .withName("usageScheme")
                                                                                                                                                        .withPricingStrategy(PricingStrategy.UsageManagedItem)
                                                                                                                                                        .withPriceVisibility(ProductChargingScheme.PriceVisibility.Customer)
                                                                                                                                                        .build()))
                                                             .withPriceLines(newArrayList(priceLine))
                                                             .build();
        PricingClient pricingClient = mock(PricingClient.class);
        PricingConfig pricingConfig = mock(PricingConfig.class);
        PricingConfig.ChargingSchemeFilterCriteria chargingSchemeFilterCriteria = mock(PricingConfig.ChargingSchemeFilterCriteria.class);
        when(chargingSchemeFilterCriteria.forName("usageScheme")).thenReturn(chargingSchemeFilterCriteria);
        when(chargingSchemeFilterCriteria.forIfc("Y")).thenReturn(chargingSchemeFilterCriteria);
        when(chargingSchemeFilterCriteria.search()).thenReturn(Lists.<ChargingSchemeConfig>newArrayList());
        when(pricingConfig.chargingSchemes()).thenReturn(chargingSchemeFilterCriteria);
        when(pricingClient.getPricingConfig()).thenReturn(pricingConfig);
        ChangeTracker changeTracker = mock(ChangeTracker.class);
        when(changeTracker.changeFor(any(ItemPrice.class))).thenReturn(ChangeType.ADD);
        MergeResult mergeResult = new MergeResult(Lists.<ProductInstance>newArrayList(), changeTracker);
        AbstractPricingSheetProductModel pricingSheetProductModel = new AbstractPricingSheetProductModel(null, owner, QuoteOptionItemDTOFixture.aQuoteOptionItemDTO().build(), mergeResult, pricingClient, null);
        assertThat(pricingSheetProductModel.getQuantityInstanceCharacteristic("QUANTITY",PriceType.NEW.name()),is("1"));
        assertThat(pricingSheetProductModel.getQuantityInstanceCharacteristic("QUANTITY",PriceType.EXISTING.name()),is("1"));
        assertThat(pricingSheetProductModel.getQuantityInstanceCharacteristic("QUANTITY",PriceType.NEW_IFC.name()),is("1"));
        assertThat(pricingSheetProductModel.getQuantityInstanceCharacteristic("QUANTITY",null),is("1"));
    }


    @Test
    public void ShouldReturnConcatenatedAccessSpeedInstanceCharacteristicValue() throws InstanceCharacteristicNotFound {
        productInstance = Mockito.mock(ProductInstance.class);
        productInstance = DefaultProductInstanceFixture.aProductInstance()
                                               .withProductIdentifier("S0316770","Access Circuit")
                                               .withAttributeValue(ProductOffering.ACCESS_DOWNSTREAM_SPEED_DISPLAY_VALUE, "1024Mbps")
                                               .withAttributeValue(ProductOffering.ACCESS_UPSTREAM_SPEED_DISPLAY_VALUE, "512Mbps")
                                               .build();
        AbstractPricingSheetProductModel pricingSheetProductModel = new AbstractPricingSheetProductModel(null, productInstance, QuoteOptionItemDTOFixture.aQuoteOptionItemDTO().build(), null, null, null);
        //Then
        assertThat(String.valueOf(pricingSheetProductModel.getInstanceCharacteristic(ProductOffering.ACCESS_DOWNSTREAM_SPEED_DISPLAY_VALUE)), is("512Mbps/1024Mbps"));
    }


    @Test
    public void shouldGetRemainingContractTerm(){
        productInstance = Mockito.mock(ProductInstance.class);
        ProductInstance asIsProductInstance = Mockito.mock(ProductInstance.class);

        when(asIsProductInstance.getContractTerm()).thenReturn("12");
        AbstractPricingSheetProductModel abstractPricingSheetProductModel = new AbstractPricingSheetProductModel(null, productInstance, QuoteOptionItemDTOFixture.aQuoteOptionItemDTO().build(), null, null, Optional.of(asIsProductInstance));
        assertThat(abstractPricingSheetProductModel.getRemainingContractTerm(),is("12"));
    }
}
