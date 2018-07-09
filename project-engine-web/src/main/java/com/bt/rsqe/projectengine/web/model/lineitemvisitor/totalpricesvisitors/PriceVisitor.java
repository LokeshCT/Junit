package com.bt.rsqe.projectengine.web.model.lineitemvisitor.totalpricesvisitors;

import com.bt.rsqe.Money;
import com.bt.rsqe.Percentage;
import com.bt.rsqe.projectengine.web.model.lineitemvisitor.LineItemVisitor;

public interface PriceVisitor extends LineItemVisitor{
    Money getNet();
    Money getGross();
    Percentage getDiscount();
}
