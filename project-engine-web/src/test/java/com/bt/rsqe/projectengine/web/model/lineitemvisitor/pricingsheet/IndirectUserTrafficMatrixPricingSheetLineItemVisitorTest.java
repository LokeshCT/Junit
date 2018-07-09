package com.bt.rsqe.projectengine.web.model.lineitemvisitor.pricingsheet;

import com.bt.rsqe.Money;
import com.bt.rsqe.projectengine.web.model.LineItemModel;
import com.bt.rsqe.projectengine.web.model.ProjectedUsageModel;
import com.bt.rsqe.projectengine.web.model.lineitemvisitor.LineItemVisitor;
import com.google.common.collect.Maps;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Map;

import static com.bt.rsqe.expedio.fixtures.SiteDTOFixture.*;
import static com.bt.rsqe.projectengine.web.quoteoptionpricing.PricingSheetKeys.*;
import static com.google.common.collect.Lists.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.core.Is.*;
import static org.mockito.Mockito.*;

public class IndirectUserTrafficMatrixPricingSheetLineItemVisitorTest {

    private static final String SITE_NAME = "SITE_NAME";
    private static final String CITY = "CITY";
    private static final String DESTINATION_COUNTRY = "destinationCountry";
    private static final String OFF_NET = "OFF NET";
    private LineItemModel lineItemModel;
    private Map<String, Object> uniqueDataSet;
    private ArrayList<Map<String,Object>> onNetTable;
    private ArrayList<Map<String,Object>> offNetTable;

    @Before
    public void before(){
        lineItemModel = mock(LineItemModel.class);
        when(lineItemModel.getSite()).thenReturn(aSiteDTO().withName(SITE_NAME).withCity(CITY).build());
        uniqueDataSet = Maps.newHashMap();
        onNetTable = newArrayList();
        offNetTable = newArrayList();
    }

    @Test
    public void shouldGetDataFromLineItem() throws Exception {
        LineItemVisitor visitor = new IndirectUserTrafficMatrixPricingSheetLineItemVisitor(onNetTable, offNetTable, uniqueDataSet);

        visitor.visit(lineItemModel);
        assertThat(uniqueDataSet.get(TRAFFIC_MATRIX_SITE_NAME).toString(), is(SITE_NAME));
        assertThat(uniqueDataSet.get(TRAFFIC_MATRIX_SITE_CITY).toString(), is(CITY));

        verify(lineItemModel).visitParent(visitor);
    }

    @Test
    public void shouldGetOffNetValuesFromProjectedUsageModel() throws Exception {
        final ProjectedUsageModel onNetProjectedUsage = mock(ProjectedUsageModel.class);

        when(lineItemModel.isForIfc()).thenReturn(true);
        when(onNetProjectedUsage.isOffNet()).thenReturn(true);
        when(onNetProjectedUsage.getTerminationType()).thenReturn(OFF_NET);
        when(onNetProjectedUsage.getDestinationCountry()).thenReturn(DESTINATION_COUNTRY);
        when(onNetProjectedUsage.getOutgoingUnits()).thenReturn(110);
        when(onNetProjectedUsage.getChargePricePerMin()).thenReturn(Money.from("16.5"));
        when(onNetProjectedUsage.getChargeOffNetChargePerMonth()).thenReturn(Money.from("18.5"));

        final ArrayList<Map<String, Object>> offNetTable = newArrayList();
        LineItemVisitor visitor = new IndirectUserTrafficMatrixPricingSheetLineItemVisitor(null, offNetTable, uniqueDataSet);

        visitor.visit(lineItemModel);
        visitor.visit(onNetProjectedUsage);

        final Map<String, Object> firstOffNetOccurence = offNetTable.get(0);

        assertThat(String.valueOf(firstOffNetOccurence.get(OFF_NET_CONFIG_TERMINATION_TYPE)), is(OFF_NET));
        assertThat(String.valueOf(firstOffNetOccurence.get(OFF_NET_CONFIG_TERMINATING_COUNTRY)), is(DESTINATION_COUNTRY));
        assertThat(String.valueOf(firstOffNetOccurence.get(OFF_NET_CONFIG_OUTGOING_MINS_PER_MONTH)), is("110"));
        assertThat(String.valueOf(firstOffNetOccurence.get(OFF_NET_CONFIG_RRP_USAGE_CHARGE_PER_MIN)), is("16.50"));
        assertThat(String.valueOf(firstOffNetOccurence.get(OFF_NET_CONFIG_RRP_USAGE_CHARGE_PER_MONTH)), is("18.50"));
        assertThat(String.valueOf(firstOffNetOccurence.get(OFF_NET_CONFIG_IFC_STATUS)), is("IFC"));
    }


    @Test
    public void shouldGetOnNetValuesFromProjectedUsageModel() throws Exception {
        final ProjectedUsageModel onNetProjectedUsage = mock(ProjectedUsageModel.class);
        when(onNetProjectedUsage.isOnNet()).thenReturn(true);
        when(onNetProjectedUsage.getDestinationCountry()).thenReturn(DESTINATION_COUNTRY);
        when(onNetProjectedUsage.getOutgoingUnits()).thenReturn(10);
        when(onNetProjectedUsage.getIncomingUnits()).thenReturn(5);
        when(onNetProjectedUsage.getChargePricePerMin()).thenReturn(Money.from("6.5"));
        when(onNetProjectedUsage.getChargeOnNetChargePerMonth()).thenReturn(Money.from("8.5"));

        final ArrayList<Map<String, Object>> onNetTable = newArrayList();
        LineItemVisitor visitor = new IndirectUserTrafficMatrixPricingSheetLineItemVisitor(onNetTable, null, uniqueDataSet);

        visitor.visit(lineItemModel);
        visitor.visit(onNetProjectedUsage);

        final Map<String, Object> firstOnNetOccurence = onNetTable.get(0);
        assertThat(String.valueOf(firstOnNetOccurence.get(ON_NET_CONFIG_TERMINATING_COUNTRY)), is(DESTINATION_COUNTRY));
        assertThat(String.valueOf(firstOnNetOccurence.get(ON_NET_CONFIG_OUTGOING_MINS_PER_MONTH)), is("10"));
        assertThat(String.valueOf(firstOnNetOccurence.get(ON_NET_CONFIG_INCOMING_MINUTES_PER_MONTH)), is("5"));
        assertThat(String.valueOf(firstOnNetOccurence.get(ON_NET_CONFIG_RRP_USAGE_CHARGE_PER_MIN)), is("6.50"));
        assertThat(String.valueOf(firstOnNetOccurence.get(ON_NET_CONFIG_RRP_USAGE_CHARGE_PER_MONTH)), is("8.50"));
    }


}
