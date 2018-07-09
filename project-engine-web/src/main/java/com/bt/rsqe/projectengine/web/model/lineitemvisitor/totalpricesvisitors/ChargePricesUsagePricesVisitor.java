package com.bt.rsqe.projectengine.web.model.lineitemvisitor.totalpricesvisitors;

import com.bt.rsqe.Money;
import com.bt.rsqe.projectengine.web.model.LineItemModel;
import com.bt.rsqe.projectengine.web.model.ProjectedUsageModel;
import com.bt.rsqe.projectengine.web.model.lineitemvisitor.AbstractLineItemVisitor;

public class ChargePricesUsagePricesVisitor extends AbstractLineItemVisitor implements UsagePriceVisitor {

    private Money totalOnNet = Money.ZERO;
    private Money totalOffNet = Money.ZERO;
    private int contractTerm;


    public ChargePricesUsagePricesVisitor() {
    }

    @Override
    public Money getTotalOnNetUsageCharge() {
        return totalOnNet;
    }

    @Override
    public Money getTotalOffNetUsageCharge() {
        return totalOffNet;
    }

    @Override
    public Money getTotalUsageCharge() {
        return totalOffNet.add(totalOnNet);
    }

    public void visit(LineItemModel lineItem) {
        contractTerm = Integer.valueOf(lineItem.getContractTerm());
    }

    @Override
    public void visit(ProjectedUsageModel projectedUsage) {
        totalOnNet = totalOnNet.add(projectedUsage.getChargeOnNetChargePerMonth().multiplyBy(contractTerm));
        totalOffNet = totalOffNet.add(projectedUsage.getChargeOffNetChargePerMonth().multiplyBy(contractTerm));
    }



}
