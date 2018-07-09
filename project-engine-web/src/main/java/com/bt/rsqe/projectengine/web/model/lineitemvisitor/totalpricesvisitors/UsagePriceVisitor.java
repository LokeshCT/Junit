package com.bt.rsqe.projectengine.web.model.lineitemvisitor.totalpricesvisitors;

import com.bt.rsqe.Money;
import com.bt.rsqe.projectengine.web.model.lineitemvisitor.LineItemVisitor;

public interface UsagePriceVisitor extends LineItemVisitor{
    Money getTotalOnNetUsageCharge();
    Money getTotalOffNetUsageCharge();
    Money getTotalUsageCharge();
}
