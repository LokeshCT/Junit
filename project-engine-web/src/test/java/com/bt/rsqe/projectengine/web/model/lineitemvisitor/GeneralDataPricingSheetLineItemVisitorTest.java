package com.bt.rsqe.projectengine.web.model.lineitemvisitor;

import com.bt.rsqe.customerinventory.client.ProductInstanceClient;
import com.bt.rsqe.customerinventory.parameter.LineItemId;
import com.bt.rsqe.domain.product.ProductOffering;
import com.bt.rsqe.domain.product.parameters.ProductIdentifier;
import com.bt.rsqe.domain.project.ProductInstance;
import com.bt.rsqe.enums.ProductCodes;
import com.bt.rsqe.projectengine.web.model.LineItemModel;
import com.bt.rsqe.projectengine.web.model.lineitemvisitor.pricingsheet.GeneralDataPricingSheetLineItemVisitor;
import com.bt.rsqe.projectengine.web.model.lineitemvisitor.totalpricesvisitors.PricesTotalAggregator;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;

import java.util.List;
import java.util.Map;

import static com.bt.rsqe.domain.bom.fixtures.ProductOfferingFixture.*;
import static com.bt.rsqe.projectengine.web.quoteoptionpricing.PricingSheetKeys.*;
import static com.google.common.collect.Lists.*;
import static com.google.common.collect.Maps.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.core.Is.*;
import static org.mockito.Mockito.*;

public class GeneralDataPricingSheetLineItemVisitorTest {

    private LineItemVisitor visitor;
    private Map<String, Object> uniqueDataSet;
    private List<Map<String, Object>> siteMaps;
    private List<Map<String, Object>> siteAgnosticMaps;
    private LineItemModel lineItem;
    private ProductInstanceClient futureProductInstanceClient;
    private LineItemId lineItemId = new LineItemId("1");
    private ProductInstance productInstance;
    private PricesTotalAggregator priceTotalAggregator;
    private LineItemVisitorFactory lineItemVisitorFactory;
    private LineItemVisitor parentVisitor;

    @Before
    public void before() {
        futureProductInstanceClient = mock(ProductInstanceClient.class);
        priceTotalAggregator = mock(PricesTotalAggregator.class);
        lineItemVisitorFactory = mock(LineItemVisitorFactory.class);

        uniqueDataSet = newHashMap();
        siteMaps = newArrayList();
        siteAgnosticMaps = newArrayList();
        visitor = new GeneralDataPricingSheetLineItemVisitor(futureProductInstanceClient, uniqueDataSet, siteMaps, priceTotalAggregator, lineItemVisitorFactory, siteAgnosticMaps);
        mockLineItem();

    }

    @Test
    public void shouldMapTotalsFromPricesAggregator() throws Exception {
        when(priceTotalAggregator.getOneTimeRRP()).thenReturn("1.00");
        when(priceTotalAggregator.getOneTimePTP()).thenReturn("2.00");
        when(priceTotalAggregator.getRecurringPTP()).thenReturn("3.00");
        when(priceTotalAggregator.getRecurringRRP()).thenReturn("4.00");
        when(priceTotalAggregator.getTotalPTP()).thenReturn("5.00");
        when(priceTotalAggregator.getTotalRRP()).thenReturn("6.00");
        when(priceTotalAggregator.getUsagePTP()).thenReturn("7.00");
        when(priceTotalAggregator.getOffNetUsagePTP()).thenReturn("7.10");
        when(priceTotalAggregator.getOnNetUsagePTP()).thenReturn("7.20");
        when(priceTotalAggregator.getUsageRRP()).thenReturn("8.00");
        when(priceTotalAggregator.getOffNetUsageRRP()).thenReturn("8.10");
        when(priceTotalAggregator.getOnNetUsageRRP()).thenReturn("8.20");
        ProductOffering productOffering = aProductOffering().withProductIdentifier(ProductCodes.ConnectAccelerationSite.productCode()).withSiteSpecific().build();
        when(productInstance.getProductOffering()).thenReturn(productOffering);
        parentVisitor = mock(LineItemVisitor.class);
        when(lineItemVisitorFactory.createPricingSheetVisitor(eq(productInstance), Matchers.<Map<String,Object>>any(),
                                                              eq(priceTotalAggregator), Matchers.<Map<String,Object>>any())).thenReturn(parentVisitor);
        visitor.visit(lineItem);

        assertThat(uniqueDataSet.get(TOTAL_ONE_TIME_RRP).toString(), is("1.00"));
        assertThat(uniqueDataSet.get(TOTAL_ONE_TIME_PTP).toString(), is("2.00"));
        assertThat(uniqueDataSet.get(TOTAL_RECURRING_PTP).toString(), is("3.00"));
        assertThat(uniqueDataSet.get(TOTAL_RECURRING_RRP).toString(), is("4.00"));
        assertThat(uniqueDataSet.get(TOTAL_PTP).toString(), is("5.00"));
        assertThat(uniqueDataSet.get(TOTAL_RRP).toString(), is("6.00"));
        assertThat(uniqueDataSet.get(TOTAL_USAGE_PTP).toString(), is("7.00"));
        assertThat(uniqueDataSet.get(TOTAL_OFFNET_USAGE_PTP).toString(), is("7.10"));
        assertThat(uniqueDataSet.get(TOTAL_ONNET_USAGE_PTP).toString(), is("7.20"));
        assertThat(uniqueDataSet.get(TOTAL_USAGE_RRP).toString(), is("8.00"));
        assertThat(uniqueDataSet.get(TOTAL_OFFNET_USAGE_RRP).toString(), is("8.10"));
        assertThat(uniqueDataSet.get(TOTAL_ONNET_USAGE_RRP).toString(), is("8.20"));

    }

    @Test
    public void shouldCallVisitParentForEachLineItem() throws Exception {
        ProductIdentifier productIdentifier = new ProductIdentifier(ProductCodes.ConnectAccelerationSite.productCode(), ProductCodes.ConnectAccelerationSite.productName(), "1.0");
        ProductOffering productOffering = aProductOffering().withProductIdentifier(productIdentifier).withSiteSpecific().build();
        when(productInstance.getProductOffering()).thenReturn(productOffering);
        parentVisitor = mock(LineItemVisitor.class);
        when(lineItemVisitorFactory.createPricingSheetVisitor(eq(productInstance), Matchers.<Map<String, Object>>any(),
                                                              eq(priceTotalAggregator), Matchers.<Map<String, Object>>any())).thenReturn(parentVisitor);

        visitor.visit(lineItem);
        verify(lineItem).visitParent(parentVisitor);
    }

    @Test
    public void shouldMapSiteAgnosticData() throws Exception {
        ProductIdentifier productIdentifier = new ProductIdentifier(ProductCodes.ConnectAccelerationSite.productCode(), ProductCodes.ConnectAccelerationSite.productName(), "1.0");
        ProductOffering productOffering = aProductOffering().withProductIdentifier(productIdentifier).build();
        when(productInstance.getProductOffering()).thenReturn(productOffering);
        when(productInstance.getProductIdentifier()).thenReturn(productIdentifier);
        visitor.visit(lineItem);
        assertThat(siteAgnosticMaps.size(), is(1));
    }

    private void mockLineItem() {
        lineItem = mock(LineItemModel.class);
        productInstance = mock(ProductInstance.class);

        when(lineItem.getLineItemId()).thenReturn(lineItemId);
        when(futureProductInstanceClient.get(lineItemId)).thenReturn(productInstance);
    }

}
