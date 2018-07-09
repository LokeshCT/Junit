package com.bt.rsqe.projectengine.web.model;

import com.bt.rsqe.domain.project.PricePoint;
import com.bt.rsqe.domain.project.SpecialPriceBook;
import com.bt.rsqe.Money;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SpecialPriceBookModel {
    private Map<String, SpecialPriceBook> specialPriceBookMap;

    public SpecialPriceBookModel(List<SpecialPriceBook> specialPriceBooks) {
        this.specialPriceBookMap = new HashMap<String, SpecialPriceBook>();
        for (SpecialPriceBook specialPriceBook : specialPriceBooks) {
            this.specialPriceBookMap.put(specialPriceBook.getCountry().getDisplayName(), specialPriceBook);
        }
    }

    public PricePointModel getPricePointFor(ProjectedUsageModel projectedUsageModel, BigDecimal priceIfNoSpecialPriceBook) {
        if (!specialPriceBookMap.containsKey(projectedUsageModel.getOriginCountry())) {
            return new NoSpecialPriceBookPricePointModel(Money.from(priceIfNoSpecialPriceBook));
        }

        SpecialPriceBook specialPriceBook = specialPriceBookMap.get(projectedUsageModel.getOriginCountry());
        final List<PricePoint> pricePoints = specialPriceBook.getPricePoints();
        for (PricePoint pricePoint : pricePoints) {
            if (projectedUsageModel.satisfies(pricePoint)) {
                return new PricePointModel(pricePoint);
            }
        }

        return new NoSpecialPriceBookPricePointModel(Money.from(priceIfNoSpecialPriceBook));
    }
}
