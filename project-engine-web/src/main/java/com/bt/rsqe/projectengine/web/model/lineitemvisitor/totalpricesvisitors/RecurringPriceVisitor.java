package com.bt.rsqe.projectengine.web.model.lineitemvisitor.totalpricesvisitors;

import com.bt.rsqe.domain.AggregationSet;
import com.bt.rsqe.enums.PriceCategory;
import com.bt.rsqe.enums.PriceType;
import com.bt.rsqe.projectengine.web.model.LineItemModel;
import com.bt.rsqe.projectengine.web.model.PriceModel;

import static com.bt.rsqe.utils.AssertObject.isNotNull;

public class RecurringPriceVisitor extends BasePriceVisitor {

    private PriceCategory priceCategory;
    private Integer contractTerm;

    public RecurringPriceVisitor(PriceCategory priceCategory) {
        this.priceCategory = priceCategory;
    }

    @Override
    public void visit(PriceModel priceModel) {
        if (contractTerm == null) {
            throw new IllegalStateException("LineItemModel must first be passed.");
        }
        if (priceModel.isSatisfiedBy(PriceType.RECURRING, priceCategory)) {
            addGross(priceModel.getValue().multiplyBy(contractTerm));
            addNet(priceModel.getNetValue().multiplyBy(contractTerm));
        }
    }

    public void visit(LineItemModel lineItem) {
        contractTerm = Integer.valueOf(lineItem.getContractTerm());
    }
}

