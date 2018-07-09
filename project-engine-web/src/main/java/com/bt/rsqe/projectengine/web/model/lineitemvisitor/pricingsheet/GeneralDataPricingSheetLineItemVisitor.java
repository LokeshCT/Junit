package com.bt.rsqe.projectengine.web.model.lineitemvisitor.pricingsheet;

import com.bt.rsqe.customerinventory.client.ProductInstanceClient;
import com.bt.rsqe.domain.project.ProductInstance;
import com.bt.rsqe.projectengine.web.model.LineItemModel;
import com.bt.rsqe.projectengine.web.model.lineitemvisitor.AbstractLineItemVisitor;
import com.bt.rsqe.projectengine.web.model.lineitemvisitor.LineItemVisitor;
import com.bt.rsqe.projectengine.web.model.lineitemvisitor.LineItemVisitorFactory;
import com.bt.rsqe.projectengine.web.model.lineitemvisitor.totalpricesvisitors.PricesTotalAggregator;
import com.bt.rsqe.projectengine.web.quoteoptionpricing.PricingSheetKeys;

import java.util.List;
import java.util.Map;

import static com.bt.rsqe.projectengine.web.quoteoptionpricing.PricingSheetKeys.*;
import static com.google.common.collect.Maps.*;

public class GeneralDataPricingSheetLineItemVisitor extends AbstractLineItemVisitor {
    private ProductInstanceClient futureProductInstanceClient;
    private Map<String, Object> uniqueDataSet;
    private List<Map<String, Object>> siteMaps;
    private PricesTotalAggregator priceLineAggregator;
    private LineItemVisitorFactory lineItemVisitorFactory;
    private List<Map<String, Object>> siteAgnosticData;


    public GeneralDataPricingSheetLineItemVisitor(ProductInstanceClient futureProductInstanceClient, Map<String, Object> uniqueDataSet,
                                                  List<Map<String, Object>> siteMaps, PricesTotalAggregator priceLineAggregator,
                                                  LineItemVisitorFactory lineItemVisitorFactory, List<Map<String, Object>> siteAgnosticData) {
        this.futureProductInstanceClient = futureProductInstanceClient;
        this.uniqueDataSet = uniqueDataSet;
        this.siteMaps = siteMaps;
        this.priceLineAggregator = priceLineAggregator;
        this.lineItemVisitorFactory = lineItemVisitorFactory;
        this.siteAgnosticData = siteAgnosticData;
    }

    public void visit(LineItemModel lineItem) {
        final Map<String, Object> siteMap = newHashMap();
        final Map<String, Object> parentSiteMap = newHashMap();
        final Map<String, Object> siteAgnosticMap = newHashMap();
        final ProductInstance productInstance = futureProductInstanceClient.get(lineItem.getLineItemId());

        final LineItemVisitor siteMapVisitor = lineItemVisitorFactory.createPricingSheetVisitor(productInstance, siteMap, priceLineAggregator, siteAgnosticMap);
        final LineItemVisitor parentSiteMapVisitor = lineItemVisitorFactory.createPricingSheetVisitor(productInstance, parentSiteMap, priceLineAggregator, siteAgnosticMap);

        lineItem.accept(siteMapVisitor);
        lineItem.visitParent(parentSiteMapVisitor);
        if (!productInstance.getProductOffering().isSiteInstallable()) {
            populateSiteAgnosticMap(lineItem, siteAgnosticMap, productInstance);
        }

        // TODO: find a way to do this without redoing it for each visitOrderItem
        populateUniqueDataSet(lineItem);

        siteMaps.add(siteMap);

        if (parentSiteMap.size() != 0) {
            siteMaps.add(parentSiteMap);
        }
        if (!productInstance.getProductOffering().isSiteInstallable()) {
            siteAgnosticData.add(siteAgnosticMap);
        }
    }

    private void populateUniqueDataSet(LineItemModel lineItem) {
        uniqueDataSet.put(TOTAL_USAGE_RRP, priceLineAggregator.getUsageRRP());
        uniqueDataSet.put(TOTAL_OFFNET_USAGE_RRP, priceLineAggregator.getOffNetUsageRRP());
        uniqueDataSet.put(TOTAL_ONNET_USAGE_RRP, priceLineAggregator.getOnNetUsageRRP());
        String oneTimeRRP = priceLineAggregator.getOneTimeRRP();
        uniqueDataSet.put(TOTAL_ONE_TIME_RRP, oneTimeRRP);
        String recurringRRP = priceLineAggregator.getRecurringRRP();
        uniqueDataSet.put(TOTAL_RECURRING_RRP, recurringRRP);
        uniqueDataSet.put(TOTAL_RRP, priceLineAggregator.getTotalRRP());
        uniqueDataSet.put(TOTAL_USAGE_PTP, priceLineAggregator.getUsagePTP());
        uniqueDataSet.put(TOTAL_OFFNET_USAGE_PTP, priceLineAggregator.getOffNetUsagePTP());
        uniqueDataSet.put(TOTAL_ONNET_USAGE_PTP, priceLineAggregator.getOnNetUsagePTP());
        uniqueDataSet.put(TOTAL_ONE_TIME_PTP, priceLineAggregator.getOneTimePTP());
        uniqueDataSet.put(TOTAL_RECURRING_PTP, priceLineAggregator.getRecurringPTP());
        uniqueDataSet.put(TOTAL_PTP, priceLineAggregator.getTotalPTP());
        uniqueDataSet.put(PricingSheetKeys.CONTRACT_TERM, lineItem.getContractTerm());
        uniqueDataSet.put(TOTAL_OT_EXIST, 0.00);
        uniqueDataSet.put(TOTAL_MRR_EXIST, priceLineAggregator.getRecurringRRP());
    }

    private void populateSiteAgnosticMap(LineItemModel lineItem, Map<String, Object> siteAgnosticMap, ProductInstance productInstance) {
        siteAgnosticMap.put("ServiceName", productInstance.getProductIdentifier().getProductName());
        siteAgnosticMap.put("pricebookNameVersion", lineItem.getPriceBook());
        siteAgnosticMap.put("Action", lineItem.getAction());
        siteAgnosticMap.put("priceType", "NEW");
        siteAgnosticMap.put("productName", lineItem.getProductName());
    }
}
