package com.bt.rsqe.projectengine.web.model.lineitemvisitor.pricingsheet;

import com.bt.rsqe.customerrecord.SiteDTO;
import com.bt.rsqe.projectengine.web.model.LineItemModel;
import com.bt.rsqe.projectengine.web.model.lineitemvisitor.AbstractLineItemVisitor;

import java.util.List;
import java.util.Map;

import static com.bt.rsqe.projectengine.web.quoteoptionpricing.PricingSheetKeys.*;

public abstract class TrafficMatrixPricingSheetLineItemVisitor extends AbstractLineItemVisitor {

    protected Map<String, Object> uniqueDataSet;
    protected LineItemModel lineItem;
    protected List<Map<String, Object>> offNetTableData;
    protected List<Map<String, Object>> onNetTableData;

    protected TrafficMatrixPricingSheetLineItemVisitor(Map<String, Object> uniqueDataSet, List<Map<String, Object>> offNetTableData, List<Map<String, Object>> onNetTableData) {
        this.uniqueDataSet = uniqueDataSet;
        this.offNetTableData = offNetTableData;
        this.onNetTableData = onNetTableData;
    }

    public void visit(LineItemModel lineItem) {
        lineItem.visitParent(this);
        this.lineItem = lineItem;
        final SiteDTO site = lineItem.getSite();
        uniqueDataSet.put(TRAFFIC_MATRIX_SITE_NAME, site.name);
        uniqueDataSet.put(TRAFFIC_MATRIX_SITE_CITY, site.city);
        uniqueDataSet.put(PRODUCT_SCODE, lineItem.getProductSCode());
    }
}
