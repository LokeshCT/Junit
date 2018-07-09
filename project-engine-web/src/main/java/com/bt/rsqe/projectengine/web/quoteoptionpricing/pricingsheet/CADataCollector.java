package com.bt.rsqe.projectengine.web.quoteoptionpricing.pricingsheet;

import com.bt.rsqe.projectengine.web.model.LineItemModel;
import com.bt.rsqe.projectengine.web.model.lineitemvisitor.LineItemVisitor;
import com.bt.rsqe.projectengine.web.model.lineitemvisitor.LineItemVisitorFactory;
import com.bt.rsqe.projectengine.web.model.lineitemvisitor.totalpricesvisitors.PricesTotalAggregator;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static com.bt.rsqe.projectengine.web.quoteoptionpricing.PricingSheetKeys.*;
import static com.google.common.collect.Lists.*;
import static com.google.common.collect.Maps.*;

@Deprecated
public class CADataCollector implements DataCollector{
    private LineItemVisitorFactory lineItemVisitorFactory;

    public CADataCollector(LineItemVisitorFactory lineItemVisitorFactory) {
        this.lineItemVisitorFactory = lineItemVisitorFactory;
    }

    @Override
    public void process(List<LineItemModel> lineItemModels, Map sheetModel) {
        List<Map<String, Object>> sitesTableData = newArrayList();
        final PricesTotalAggregator priceLineAggregator = new PricesTotalAggregator(); // we need to use just one aggregator, to aggregate across line-items
        List<Map<String, Object>> sitesAgnosticTableData = newArrayList();
        for (LineItemModel lineItemModel : lineItemModels) {
            final LineItemVisitor visitor = lineItemVisitorFactory.createGeneralPricingSheetVisitor(sitesTableData, sheetModel, priceLineAggregator, sitesAgnosticTableData);
            lineItemModel.accept(visitor);
        }

        sortListOfMapsBasedOnAKey(sitesTableData, SITE_NAME);
        sheetModel.put("sites", sitesTableData);
        sheetModel.put("siteAgnostic", sitesAgnosticTableData);
        sheetModel.put("projectedUsages", collectTrafficMatrixData(lineItemModels));
    }

    public List<Map> collectTrafficMatrixData(List<LineItemModel> lineItemModels) {
        List<Map> trafficData = newArrayList();
        for (LineItemModel lineItemModel : lineItemModels) {
            Map<String, Object> siteTrafficData = newHashMap();
            List<Map<String, Object>> offNetTableData = newArrayList();
            List<Map<String, Object>> onNetTableData = newArrayList();
            final LineItemVisitor visitor = lineItemVisitorFactory.createTrafficMatrixPricingSheetVisitor(onNetTableData, offNetTableData, siteTrafficData);
            lineItemModel.accept(visitor);
            sortListOfMapsBasedOnAKey(onNetTableData, ON_NET_CONFIG_IFC_STATUS, ON_NET_CONFIG_TERMINATING_COUNTRY);
            sortListOfMapsBasedOnAKey(offNetTableData, OFF_NET_CONFIG_IFC_STATUS, OFF_NET_CONFIG_TERMINATING_COUNTRY);
            siteTrafficData.put("onNet", onNetTableData);
            siteTrafficData.put("offNet", offNetTableData);
            trafficData.add(siteTrafficData);
        }

        return trafficData;
    }

    void sortListOfMapsBasedOnAKey(final List<Map<String, Object>> sitesTableData, final String... keys) {
        Collections.sort(sitesTableData, new Comparator<Map<String, Object>>() {
            @Override
            public int compare(Map<String, Object> site1, Map<String, Object> site2) {
                String site1Compare = "";
                String site2Compare = "";

                for (String key : keys) {
                    if (site1.get(key) != null) {
                        site1Compare += site1.get(key);
                    }
                    if (site2.get(key) != null) {
                        site2Compare += site2.get(key);
                    }
                }

                return site1Compare.compareTo(site2Compare);
            }
        });
    }
}
