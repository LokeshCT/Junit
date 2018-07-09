package com.bt.rsqe.projectengine.web.model.lineitemvisitor;

import com.bt.rsqe.projectengine.web.model.FutureAssetPricesModel;
import com.bt.rsqe.projectengine.web.model.LineItemModel;
import com.bt.rsqe.projectengine.web.model.PriceLineModel;
import com.bt.rsqe.projectengine.web.model.PriceModel;
import com.bt.rsqe.projectengine.web.model.ProjectedUsageModel;

import java.util.List;

import static com.google.common.collect.Lists.*;

public class CompositeLineItemVisitor implements LineItemVisitor {
    private List<LineItemVisitor> lineItemVisitors;

    public CompositeLineItemVisitor(LineItemVisitor... lineItemVisitors) {
        this.lineItemVisitors = newArrayList(lineItemVisitors);
    }

    @Override
    public void visit(LineItemModel lineItem) {
        for (LineItemVisitor visitor : lineItemVisitors) {
            visitor.visit(lineItem);
        }
    }

    @Override
    public void visitAfterChildren(LineItemModel lineItem) {
        for (LineItemVisitor visitor : lineItemVisitors) {
            visitor.visitAfterChildren(lineItem);
        }
    }

    @Override
    public void visit(PriceModel priceModel) {
        for (LineItemVisitor visitor : lineItemVisitors) {
            visitor.visit(priceModel);
        }
    }

    @Override
    public void visit(PriceLineModel priceLine) {
        for (LineItemVisitor priceVisitor : lineItemVisitors) {
            priceVisitor.visit(priceLine);
        }
    }

    @Override
    public void visit(ProjectedUsageModel projectedUsage) {
        for (LineItemVisitor priceVisitor : lineItemVisitors) {
            priceVisitor.visit(projectedUsage);
        }
    }

    @Override
    public void visit(FutureAssetPricesModel futureAssetPricesModel, int groupingLevel) {
        for (LineItemVisitor priceVisitor : lineItemVisitors) {
            priceVisitor.visit(futureAssetPricesModel, groupingLevel);
        }
    }

    protected final void addVisitors(LineItemVisitor... lineItemVisitors) {
        this.lineItemVisitors.addAll(newArrayList(lineItemVisitors));
    }
}
