package com.bt.rsqe.projectengine.web.quoteoptionpricing.pricingsheet;

import com.bt.rsqe.domain.project.ProductInstance;
import com.bt.rsqe.projectengine.web.model.lineitemvisitor.LineItemVisitor;
import com.bt.rsqe.projectengine.web.model.lineitemvisitor.LineItemVisitorFactory;
import com.bt.rsqe.projectengine.web.model.lineitemvisitor.pricingsheet.DirectUserPricingSheetVisitor;
import com.bt.rsqe.projectengine.web.model.lineitemvisitor.pricingsheet.DirectUserTrafficMatrixPricingSheetLineItemVisitor;
import com.bt.rsqe.projectengine.web.model.lineitemvisitor.pricingsheet.IndirectUserPricingSheetVisitor;
import com.bt.rsqe.projectengine.web.model.lineitemvisitor.pricingsheet.IndirectUserTrafficMatrixPricingSheetLineItemVisitor;
import com.bt.rsqe.projectengine.web.model.lineitemvisitor.totalpricesvisitors.PricesTotalAggregator;

import java.util.List;
import java.util.Map;

import static com.bt.rsqe.utils.Channels.*;

public abstract class PricingSheetStrategy {
    private static final String TEMPLATE_PRICING_SHEET_DIRECT = "PricingSheetDirect.xls";
    private static final String TEMPLATE_PRICING_SHEET_INDIRECT = "PricingSheetIndirect.xls";

    public abstract String template();

    public abstract LineItemVisitor pricingSheetVisitor(ProductInstance productInstance, Map<String, Object> siteMap, PricesTotalAggregator priceLineAggregator, LineItemVisitorFactory factory, Map<String, Object> siteAgnosticMap);

    public abstract LineItemVisitor trafficMatrixPricingSheetVisitor(List<Map<String, Object>> onNetTableData, List<Map<String, Object>> offNetTableData, Map<String, Object> uniqueDataSet);


    public static final PricingSheetStrategy DIRECT = new PricingSheetStrategy() {
        @Override
        public String template() {
            return TEMPLATE_PRICING_SHEET_DIRECT;
        }

        @Override
        public LineItemVisitor pricingSheetVisitor(ProductInstance productInstance, Map<String, Object> siteMap, PricesTotalAggregator priceLineAggregator, LineItemVisitorFactory factory, Map<String, Object> siteAgnosticMap) {
            return new DirectUserPricingSheetVisitor(productInstance, siteMap, priceLineAggregator, factory, siteAgnosticMap);
        }

        @Override
        public LineItemVisitor trafficMatrixPricingSheetVisitor(List<Map<String, Object>> onNetTableData, List<Map<String, Object>> offNetTableData, Map<String, Object> uniqueDataSet) {
            return new DirectUserTrafficMatrixPricingSheetLineItemVisitor(onNetTableData, offNetTableData, uniqueDataSet);
        }

    };

    public static final PricingSheetStrategy INDIRECT = new PricingSheetStrategy() {
        @Override
        public String template() {
            return TEMPLATE_PRICING_SHEET_INDIRECT;
        }

        @Override
        public LineItemVisitor pricingSheetVisitor(ProductInstance productInstance, Map<String, Object> siteMap, PricesTotalAggregator priceLineAggregator, LineItemVisitorFactory factory, Map<String, Object> siteAgnosticMap) {
            return new IndirectUserPricingSheetVisitor(productInstance, siteMap, priceLineAggregator, factory, siteAgnosticMap);
        }

        @Override
        public LineItemVisitor trafficMatrixPricingSheetVisitor(List<Map<String, Object>> onNetTableData, List<Map<String, Object>> offNetTableData, Map<String, Object> uniqueDataSet) {
            return new IndirectUserTrafficMatrixPricingSheetLineItemVisitor(onNetTableData, offNetTableData, uniqueDataSet);
        }

    };

    public static PricingSheetStrategy instance() {
        return userCanViewIndirectPrices() ? INDIRECT : DIRECT;
    }

}
