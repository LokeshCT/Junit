package com.bt.rsqe.projectengine.web.model.lineitemvisitor;

import com.bt.rsqe.projectengine.web.model.FutureAssetPricesModel;
import com.bt.rsqe.projectengine.web.model.LineItemModel;
import com.bt.rsqe.projectengine.web.model.PriceLineModel;
import com.bt.rsqe.projectengine.web.model.PriceModel;
import com.bt.rsqe.projectengine.web.model.ProjectedUsageModel;

//keep it empty now to decouple the mandatory of implementing this method as empty method in the child Visitor implementations.
public abstract class AbstractLineItemVisitor implements LineItemVisitor {

    @Override
    public void visit(LineItemModel lineItem) {
    }
    @Override
    public void visitAfterChildren(LineItemModel lineItem) {
    }
    @Override
    public void visit(FutureAssetPricesModel futureAssetPricesModel, int groupingLevel) {
    }
    @Override
    public void visit(PriceLineModel priceLine) {
    }
    @Override
    public void visit(ProjectedUsageModel projectedUsage) {
    }
    @Override
    public void visit(PriceModel priceModel) {
    }
}
