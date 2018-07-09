package com.bt.rsqe.projectengine.web.model.lineitemvisitor.pricingsheet;

import com.bt.rsqe.projectengine.web.model.ProjectedUsageModel;

import java.util.List;
import java.util.Map;

import static com.bt.rsqe.projectengine.web.quoteoptionpricing.PricingSheetKeys.*;
import static com.google.common.collect.Maps.newHashMap;

public class IndirectUserTrafficMatrixPricingSheetLineItemVisitor extends TrafficMatrixPricingSheetLineItemVisitor{
    public IndirectUserTrafficMatrixPricingSheetLineItemVisitor(List<Map<String, Object>> onNetTableData, List<Map<String, Object>> offNetTableData, Map<String, Object> uniqueDataSet) {
        super(uniqueDataSet, offNetTableData, onNetTableData);
    }

    @Override
    public void visit(ProjectedUsageModel projectedUsage) {

        if (projectedUsage.isOnNet()) {
            Map<String, Object> onNetTableRow = newHashMap();
            onNetTableRow.put(ON_NET_CONFIG_TERMINATING_COUNTRY, projectedUsage.getDestinationCountry());
            onNetTableRow.put(ON_NET_CONFIG_OUTGOING_MINS_PER_MONTH, projectedUsage.getOutgoingUnits().toString());
            onNetTableRow.put(ON_NET_CONFIG_INCOMING_MINUTES_PER_MONTH, projectedUsage.getIncomingUnits().toString());
            onNetTableRow.put(ON_NET_CONFIG_RRP_USAGE_CHARGE_PER_MIN, projectedUsage.getChargePricePerMin().toString());
            onNetTableRow.put(ON_NET_CONFIG_RRP_USAGE_CHARGE_PER_MONTH, projectedUsage.getChargeOnNetChargePerMonth().toString());
            if (lineItem.isSuperseded()) {
                onNetTableRow.put(ON_NET_CONFIG_IFC_STATUS, "OLD");
            } else {
                onNetTableRow.put(ON_NET_CONFIG_IFC_STATUS, lineItem.isForIfc() ? "IFC" : "");
            }
            onNetTableData.add(onNetTableRow);
        } else if (projectedUsage.isOffNet()) {
            Map<String, Object> offNetTableRow = newHashMap();
            offNetTableRow.put(OFF_NET_CONFIG_TERMINATION_TYPE, projectedUsage.getTerminationType());
            offNetTableRow.put(OFF_NET_CONFIG_TERMINATING_COUNTRY, projectedUsage.getDestinationCountry());
            offNetTableRow.put(OFF_NET_CONFIG_OUTGOING_MINS_PER_MONTH, projectedUsage.getOutgoingUnits().toString());
            offNetTableRow.put(OFF_NET_CONFIG_RRP_USAGE_CHARGE_PER_MIN, projectedUsage.getChargePricePerMin().toString());
            offNetTableRow.put(OFF_NET_CONFIG_RRP_USAGE_CHARGE_PER_MONTH, projectedUsage.getChargeOffNetChargePerMonth().toString());
            if (lineItem.isSuperseded()) {
                offNetTableRow.put(OFF_NET_CONFIG_IFC_STATUS, "OLD");
            } else {
                offNetTableRow.put(OFF_NET_CONFIG_IFC_STATUS, lineItem.isForIfc() ? "IFC" : "");
            }
            offNetTableData.add(offNetTableRow);
        }
    }
}
