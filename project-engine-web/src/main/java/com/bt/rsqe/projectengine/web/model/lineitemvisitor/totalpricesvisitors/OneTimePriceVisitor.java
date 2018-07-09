package com.bt.rsqe.projectengine.web.model.lineitemvisitor.totalpricesvisitors;

import com.bt.rsqe.domain.AggregationSet;
import com.bt.rsqe.enums.PriceCategory;
import com.bt.rsqe.enums.PriceType;
import com.bt.rsqe.projectengine.web.model.PriceModel;

import static com.bt.rsqe.utils.AssertObject.isNotNull;

public class OneTimePriceVisitor extends BasePriceVisitor {

    private final PriceCategory priceCategory;

    public OneTimePriceVisitor(PriceCategory priceCategory) {
        this.priceCategory = priceCategory;
    }

    @Override
    public void visit(PriceModel priceModel) {
        if (priceModel.isSatisfiedBy(PriceType.ONE_TIME, priceCategory)) {
            addGross(priceModel.getValue());
            addNet(priceModel.getNetValue());
        }
    }

}
