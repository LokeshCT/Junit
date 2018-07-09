package com.bt.rsqe.projectengine.web.model.lineitemvisitor;

import com.bt.rsqe.customerinventory.client.ProductInstanceClient;
import com.bt.rsqe.domain.project.ProductInstance;
import com.bt.rsqe.enums.PriceCategory;
import com.bt.rsqe.enums.PriceType;
import com.bt.rsqe.projectengine.web.model.lineitemvisitor.pricingsheet.GeneralDataPricingSheetLineItemVisitor;
import com.bt.rsqe.projectengine.web.model.lineitemvisitor.totalpricesvisitors.ChargePricesUsagePricesVisitor;
import com.bt.rsqe.projectengine.web.model.lineitemvisitor.totalpricesvisitors.EndUserPricesUsagePricesVisitor;
import com.bt.rsqe.projectengine.web.model.lineitemvisitor.totalpricesvisitors.OneTimePriceVisitor;
import com.bt.rsqe.projectengine.web.model.lineitemvisitor.totalpricesvisitors.PriceVisitor;
import com.bt.rsqe.projectengine.web.model.lineitemvisitor.totalpricesvisitors.PricesTotalAggregator;
import com.bt.rsqe.projectengine.web.model.lineitemvisitor.totalpricesvisitors.RecurringPriceVisitor;
import com.bt.rsqe.projectengine.web.model.lineitemvisitor.totalpricesvisitors.UsagePriceVisitor;
import com.bt.rsqe.projectengine.web.quoteoptionpricing.pricingsheet.PricingSheetStrategy;

import java.util.List;
import java.util.Map;

public class LineItemVisitorFactory {
    private ProductInstanceClient futureProductInstanceClient;

    public LineItemVisitorFactory(ProductInstanceClient futureProductInstanceClient) {
        this.futureProductInstanceClient = futureProductInstanceClient;
    }

    public PriceVisitor createPriceVisitor(PriceType priceType, PriceCategory priceCategory) {
        switch (priceType) {
            case RECURRING:
                return new RecurringPriceVisitor(priceCategory);
            case ONE_TIME:
                return new OneTimePriceVisitor(priceCategory);
            default:
                throw new UnsupportedOperationException("Unknown PriceType for this factory.  Only supports OneTime & Recurring");
        }
    }

    public UsagePriceVisitor createUsageVisitor(PriceCategory priceCategory) {
        if (priceCategory == PriceCategory.END_USER_PRICE) {
            return new EndUserPricesUsagePricesVisitor();
        } else if (priceCategory == PriceCategory.CHARGE_PRICE) {
            return new ChargePricesUsagePricesVisitor();
        } else {
            throw new UnsupportedOperationException("Unknown PriceCategory for this factory, only supports END_USER and CHARGE_PRICE");
        }
    }

    public LineItemVisitor createGeneralPricingSheetVisitor(List<Map<String, Object>> sitesTableData, Map<String, Object> uniqueDataSet, PricesTotalAggregator priceLineAggregator, List<Map<String, Object>> siteAgnosticTableData) {
        return new GeneralDataPricingSheetLineItemVisitor(futureProductInstanceClient, uniqueDataSet, sitesTableData, priceLineAggregator, this, siteAgnosticTableData);
    }

    public LineItemVisitor createPricingSheetVisitor(ProductInstance productInstance, Map<String, Object> siteMap, PricesTotalAggregator priceLineAggregator, Map<String, Object> siteAgnosticMap) {
        return PricingSheetStrategy.instance().pricingSheetVisitor(productInstance, siteMap, priceLineAggregator, this, siteAgnosticMap);
    }

    public LineItemVisitor createTrafficMatrixPricingSheetVisitor(List<Map<String, Object>> onNetTableData, List<Map<String, Object>> offNetTableData, Map<String, Object> uniqueDataSet) {
        return PricingSheetStrategy.instance().trafficMatrixPricingSheetVisitor(onNetTableData, offNetTableData, uniqueDataSet);
    }

    public LineItemVisitor createPricingSummaryVisitor(LineItemVisitor... lineItemVisitors) {
        return new CompositeLineItemVisitor(lineItemVisitors);
    }
}
