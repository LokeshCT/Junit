package com.bt.rsqe.projectengine.web.model.lineitemvisitor.totalpricesvisitors;

import com.bt.rsqe.Money;
import com.bt.rsqe.Percentage;
import com.bt.rsqe.projectengine.web.model.lineitemvisitor.AbstractLineItemVisitor;

public abstract class BasePriceVisitor extends AbstractLineItemVisitor implements PriceVisitor {

    private Money gross = Money.ZERO;
    private Money net = Money.ZERO;

    protected void addGross(Money gross) {
        this.gross = this.gross.add(gross);
    }

    protected void addNet(Money net) {
        this.net = this.net.add(net);
    }

    @Override
    public Money getNet() {
        return net;
    }

    @Override
    public Money getGross() {
        return gross;
    }

    @Override
    public Percentage getDiscount() {
        return Percentage.from(gross, net);
    }

}
