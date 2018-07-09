package com.bt.rsqe.projectengine.web.model.lineitemvisitor.pricingsheet;

import com.bt.rsqe.Money;
import com.bt.rsqe.domain.bom.fixtures.ProductOfferingFixture;
import com.bt.rsqe.domain.bom.fixtures.SalesRelationshipFixture;
import com.bt.rsqe.domain.product.AttributeName;
import com.bt.rsqe.domain.product.DefaultProductInstanceFixture;
import com.bt.rsqe.domain.product.InstanceCharacteristic;
import com.bt.rsqe.domain.product.InstanceCharacteristicValue;
import com.bt.rsqe.domain.product.ProductOffering;
import com.bt.rsqe.domain.product.ProductSalesRelationshipInstance;
import com.bt.rsqe.domain.product.constraints.AttributeValue;
import com.bt.rsqe.domain.product.parameters.ProductIdentifier;
import com.bt.rsqe.domain.product.parameters.RelationshipType;
import com.bt.rsqe.domain.project.InstanceCharacteristicNotFound;
import com.bt.rsqe.domain.project.ProductInstance;
import com.bt.rsqe.enums.PriceCategory;
import com.bt.rsqe.enums.PriceType;
import com.bt.rsqe.enums.ProductCodes;
import com.bt.rsqe.projectengine.web.model.FutureAssetPricesModel;
import com.bt.rsqe.projectengine.web.model.LineItemModel;
import com.bt.rsqe.projectengine.web.model.PriceLineModel;
import com.bt.rsqe.projectengine.web.model.lineitemvisitor.LineItemVisitor;
import com.bt.rsqe.projectengine.web.model.lineitemvisitor.LineItemVisitorFactory;
import com.bt.rsqe.projectengine.web.model.lineitemvisitor.totalpricesvisitors.PriceVisitor;
import com.bt.rsqe.projectengine.web.model.lineitemvisitor.totalpricesvisitors.PricesTotalAggregator;
import com.bt.rsqe.projectengine.web.model.lineitemvisitor.totalpricesvisitors.UsagePriceVisitor;
import com.bt.rsqe.projectengine.web.quoteoptionpricing.PricingSheetKeys;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.bt.rsqe.domain.bom.fixtures.AttributeFixture.*;
import static com.bt.rsqe.expedio.fixtures.SiteDTOFixture.*;
import static com.bt.rsqe.projectengine.web.quoteoptionpricing.PricingSheetKeys.*;
import static com.google.common.collect.Maps.*;
import static junit.framework.Assert.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.core.Is.*;
import static org.mockito.Mockito.*;

public class DirectUserPricingSheetVisitorTest {

    private static final long PPSR_ID = 123L;
    private static final int CONTRACT_TERM = 24;
    private static final int VOICE_CHANNELS_REQUIRED = 10;
    private static final Money TOTAL_CHARGE_RRP = Money.from("456");
    private static final Money CHARGE_PER_CHANNEL_RRP = Money.from("345");

    private LineItemModel lineItem;
    private ProductInstance productInstance;
    private LineItemVisitor visitor;
    private Map<String, Object> output;
    private PricesTotalAggregator priceLineAggregator;
    private LineItemVisitorFactory lineItemVisitorFactory;
    private PriceVisitor recurringChargeVisitor;
    private PriceVisitor oneTimeChargeVisitor;
    private UsagePriceVisitor usageChargeVisitor;
    private FutureAssetPricesModel futureAssetPricesModel;
    private PriceLineModel priceLine;
    private ProductOffering productOffering;

    String steelheadScode = ProductCodes.ConnectAccelerationSteelhead.productCode();
    String connectAccelerationCode = ProductCodes.ConnectAccelerationSite.productCode();
    String connectAccelerationName = ProductCodes.ConnectAccelerationSite.productName();
    @Before
    public void before() {
        priceLineAggregator = new PricesTotalAggregator();
        lineItem = mock(LineItemModel.class);
        priceLine = mock(PriceLineModel.class);
        recurringChargeVisitor = mock(PriceVisitor.class);
        oneTimeChargeVisitor = mock(PriceVisitor.class);
        usageChargeVisitor = mock(UsagePriceVisitor.class);
        lineItemVisitorFactory = mock(LineItemVisitorFactory.class);
        futureAssetPricesModel = mock(FutureAssetPricesModel.class);
        productOffering = mock(ProductOffering.class);
        stubLineItemAndVisitors();
        output = newHashMap();
    }

    @Test
    public void shouldPopulateProductInstancesData() throws Exception {
        stubChildren();
        visitor = new DirectUserPricingSheetVisitor(productInstance, output, priceLineAggregator, lineItemVisitorFactory, null);
        stubLineItemAndVisitors();

        visitor.visit(lineItem);
        visitor.visitAfterChildren(lineItem);
        assertThat((String) output.get(PricingSheetKeys.RESILIENCE), is("resilience"));
        assertNotNull(output.get(PricingSheetKeys.PRODUCT_CHILDREN));
    }

    @Test
    public void shouldPopulateLineItemData() throws Exception {
        stubChildren();
        visitor = new DirectUserPricingSheetVisitor(productInstance, output, priceLineAggregator, lineItemVisitorFactory, null);
        visitor.visit(lineItem);
        visitor.visitAfterChildren(lineItem);
        assertThat((String) output.get(SITE_NAME), is("SITE_NAME"));
        assertThat((String) output.get(SITE_CITY), is("CITY"));
        assertThat((String) output.get(RRP_PRICE_BOOK_VERSION), is("default"));
    }

    @Test
    public void shouldGetTotalsFromThePriceVisitors() throws Exception {
        stubChildren();
        visitor = new DirectUserPricingSheetVisitor(productInstance, output, priceLineAggregator, lineItemVisitorFactory, null);
        visitor.visit(lineItem);
        visitor.visitAfterChildren(lineItem);
        assertThat((Double) output.get(SITE_RECURRING_RRP), is(Money.from("1.00").toDouble()));
        assertThat((Double) output.get(SITE_ONE_TIME_RRP), is(Money.from("2.00").toDouble()));
        assertThat((Double) output.get(SITE_USAGE_RRP), is(Money.from("5.00").toDouble()));
        assertThat((Double) output.get(SITE_OFFNET_USAGE_RRP), is(Money.from("5.10").toDouble()));
        assertThat((Double) output.get(SITE_ONNET_USAGE_RRP), is(Money.from("5.20").toDouble()));
    }

    @Test
    public void shouldPopulatePriceLineData() throws Exception {
        stubChildren();
        visitor = new DirectUserPricingSheetVisitor(productInstance, output, priceLineAggregator, lineItemVisitorFactory, null);
        visitor.visit(lineItem);
        visitor.visit(futureAssetPricesModel, 0);
        visitor.visit(priceLine);
        assertNotNull(output.get("prices"));
        List<Map<String, Object>> priceValues = (List<Map<String, Object>>) output.get("prices");
        Map<String, Object> stringObjectMap = priceValues.get(0);
        assertThat((String) stringObjectMap.get(SITE_ONE_TIME_RRP), is("100.00"));
        assertThat((String) stringObjectMap.get(SITE_RECURRING_RRP), is("10.00"));
    }


    @Test
    public void shouldAggregateTotalsOfMultipleLineItemPricelines() throws Exception {
        stubChildren();
        visitor = new DirectUserPricingSheetVisitor(productInstance, output, priceLineAggregator, lineItemVisitorFactory, null);
        visitor.visit(lineItem);
        visitor.visit(lineItem);
        visitor.visitAfterChildren(lineItem);
        visitor.visitAfterChildren(lineItem);
        assertThat(priceLineAggregator.getRecurringRRP(), is("2.00"));
        assertThat(priceLineAggregator.getOneTimeRRP(), is("4.00"));
        assertThat(priceLineAggregator.getUsageRRP(), is("10.00"));
        assertThat(priceLineAggregator.getOffNetUsageRRP(), is("10.20"));
        assertThat(priceLineAggregator.getOnNetUsageRRP(), is("10.40"));
        assertThat(priceLineAggregator.getTotalRRP(), is("16.00"));
    }

    @Test
    public void shouldVisitAllPriceVisitorsForPriceLineModel() throws Exception {
        stubChildren();
        visitor = new DirectUserPricingSheetVisitor(productInstance, output, priceLineAggregator, lineItemVisitorFactory, null);
        visitor.visit(lineItem);
        visitor.visit(futureAssetPricesModel, 0);
        visitor.visit(priceLine);
        verify(recurringChargeVisitor).visit(priceLine);
        verify(oneTimeChargeVisitor).visit(priceLine);
        verify(usageChargeVisitor).visit(priceLine);
    }

    @Test
    public void shouldVisitAllPriceVisitorsForLineItem() throws Exception {
        stubChildren();
        visitor = new DirectUserPricingSheetVisitor(productInstance, output, priceLineAggregator, lineItemVisitorFactory, null);
        visitor.visit(lineItem);
        verify(recurringChargeVisitor).visit(lineItem);
        verify(oneTimeChargeVisitor).visit(lineItem);
        verify(usageChargeVisitor).visit(lineItem);
    }

    @Test
    public void shouldPopulateResilienceValue() throws InstanceCharacteristicNotFound {
        stubLineItemAndVisitors();
        ProductInstance productInstance = mock(ProductInstance.class);
        visitor = new DirectUserPricingSheetVisitor(productInstance, output, priceLineAggregator, lineItemVisitorFactory, null);
        ProductOffering productOffering = mock(ProductOffering.class);
        when(productInstance.getProductOffering()).thenReturn(productOffering);
        when(productOffering.isSiteInstallable()).thenReturn(true);
        AttributeValue value = AttributeValue.newInstance("H00007","Stencil Name");
        AttributeValue value2 = AttributeValue.newInstance("H00009","Stencil Name2");
        AttributeValue value3 = AttributeValue.newInstance("H00010","Stencil Name3");
        InstanceCharacteristic instanceCharacteristic = new InstanceCharacteristic(anAttribute().withAllowedValues(value,value2,value3).build(), new InstanceCharacteristic.ValueChangeListener() {
            @Override
            public void valueChanged(InstanceCharacteristic instanceCharacteristic, InstanceCharacteristicValue oldValue, InstanceCharacteristicValue newValue) {
                // null impl - do nothing
            }
        });
        instanceCharacteristic.setValue("H00009");
        when(productInstance.getInstanceCharacteristic(anyString())).thenReturn(instanceCharacteristic);
        visitor.visit(lineItem);
        assertThat((String) output.get(PricingSheetKeys.RESILIENCE), is("Stencil Name2"));
    }

    private void stubLineItemAndVisitors() {
        when(lineItem.getSite()).thenReturn(aSiteDTO().withName("SITE_NAME").withCity("CITY").build());
        when(lineItem.getContractTerm()).thenReturn(String.valueOf(CONTRACT_TERM));
        when(lineItem.getPriceBook()).thenReturn("default");
        when(priceLine.getPpsrId()).thenReturn(PPSR_ID);
        when(priceLine.getOneTimeCPValue()).thenReturn(Money.from("100"));
        when(priceLine.getRecurringCPValue()).thenReturn(Money.from("10"));
        when(priceLine.getTotalChargePrice(CONTRACT_TERM)).thenReturn(TOTAL_CHARGE_RRP);
        when(priceLine.getChargePricePerChannel(VOICE_CHANNELS_REQUIRED, CONTRACT_TERM)).thenReturn(CHARGE_PER_CHANNEL_RRP);

        when(lineItemVisitorFactory.createPriceVisitor(PriceType.RECURRING, PriceCategory.CHARGE_PRICE)).thenReturn(recurringChargeVisitor);
        when(lineItemVisitorFactory.createPriceVisitor(PriceType.ONE_TIME, PriceCategory.CHARGE_PRICE)).thenReturn(oneTimeChargeVisitor);
        when(lineItemVisitorFactory.createUsageVisitor(PriceCategory.CHARGE_PRICE)).thenReturn(usageChargeVisitor);

        when(recurringChargeVisitor.getNet()).thenReturn(Money.from("1"));
        when(oneTimeChargeVisitor.getNet()).thenReturn(Money.from("2"));
        when(usageChargeVisitor.getTotalUsageCharge()).thenReturn(Money.from("5"));
        when(usageChargeVisitor.getTotalOffNetUsageCharge()).thenReturn(Money.from("5.1"));
        when(usageChargeVisitor.getTotalOnNetUsageCharge()).thenReturn(Money.from("5.2"));
    }

    private void stubChildren() {
        final HashMap<AttributeName, InstanceCharacteristic> rootInstanceCharacteristics = new HashMap<AttributeName, InstanceCharacteristic>();
        InstanceCharacteristic instanceCharacteristic = new InstanceCharacteristic(anAttribute().build(), new InstanceCharacteristic.ValueChangeListener() {
            @Override
            public void valueChanged(InstanceCharacteristic instanceCharacteristic, InstanceCharacteristicValue oldValue, InstanceCharacteristicValue newValue) {
                // null impl - do nothing
            }
        });
        AttributeName attributeName = new AttributeName(PricingSheetKeys.ATTRIBUTE_RESILIENCE);

        rootInstanceCharacteristics.put(attributeName, instanceCharacteristic);
        productInstance = DefaultProductInstanceFixture.aProductInstance()
                                                       .withProductOffering(new ProductOfferingFixture().withSiteSpecific())
                                                       .withProductIdentifier(connectAccelerationCode, connectAccelerationName)
                                                       .withInstanceCharacteristics(rootInstanceCharacteristics)
                                                       .withAttributeValue(PricingSheetKeys.ATTRIBUTE_RESILIENCE, "resilience")
                                                       .build();
        final HashMap<AttributeName, InstanceCharacteristic> steelHeadOneInstanceCharacteristics = new HashMap<AttributeName, InstanceCharacteristic>();
        steelHeadOneInstanceCharacteristics.put(new AttributeName(PricingSheetKeys.ATTRIBUTE_BUNDLE_NAME), instanceCharacteristic);
        ProductInstance steelHeadOne = DefaultProductInstanceFixture.aProductInstance()
                                                                    .withProductIdentifier(new ProductIdentifier(steelheadScode, "1.0"))
                                                                    .withInstanceCharacteristics(steelHeadOneInstanceCharacteristics)
                                                                    .withAttributeValue(PricingSheetKeys.ATTRIBUTE_BUNDLE_NAME, "bundle name")
                                                                    .withAttributeValue(PricingSheetKeys.ATTRIBUTE_BUNDLE_TYPE, "bundle type")
                                                                    .build();
        final SalesRelationshipFixture steelHeadSalesRelationshipFixture = SalesRelationshipFixture.aSalesRelationship()
                                                                                                   .withRelationType(RelationshipType.Child)
                                                                                                   .withProductIdentifier(steelheadScode);
        productInstance.addRelationship(new ProductSalesRelationshipInstance(steelHeadSalesRelationshipFixture.build(), steelHeadOne));
    }
}
