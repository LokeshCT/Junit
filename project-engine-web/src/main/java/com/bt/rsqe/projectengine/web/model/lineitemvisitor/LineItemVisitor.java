package com.bt.rsqe.projectengine.web.model.lineitemvisitor;

import com.bt.rsqe.projectengine.web.model.FutureAssetPricesModel;
import com.bt.rsqe.projectengine.web.model.LineItemModel;
import com.bt.rsqe.projectengine.web.model.PriceLineModel;
import com.bt.rsqe.projectengine.web.model.PriceModel;
import com.bt.rsqe.projectengine.web.model.ProjectedUsageModel;

public interface LineItemVisitor {
    void visit(LineItemModel lineItem);
    void visitAfterChildren(LineItemModel lineItem);
    void visit(FutureAssetPricesModel futureAssetPricesModel, int groupingLevel);
    void visit(PriceLineModel priceLine);
    void visit(ProjectedUsageModel projectedUsage);
    void visit(PriceModel priceModel);
}
