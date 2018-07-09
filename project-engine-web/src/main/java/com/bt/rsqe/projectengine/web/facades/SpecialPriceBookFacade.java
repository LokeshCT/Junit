package com.bt.rsqe.projectengine.web.facades;

import com.bt.rsqe.customerinventory.client.SpecialPriceBookClient;
import com.bt.rsqe.customerinventory.parameter.QuoteOptionId;
import com.bt.rsqe.domain.project.SpecialPriceBook;
import com.bt.rsqe.projectengine.web.model.SpecialPriceBookModel;

import java.util.List;

public class SpecialPriceBookFacade {
    private SpecialPriceBookClient specialPriceBookClient;

    public SpecialPriceBookFacade(SpecialPriceBookClient specialPriceBookClient) {
        this.specialPriceBookClient = specialPriceBookClient;
    }

    public SpecialPriceBookModel get(String quoteOptionId) {
        final List<SpecialPriceBook> specialPriceBooks = specialPriceBookClient.get(new QuoteOptionId(quoteOptionId));
        return new SpecialPriceBookModel(specialPriceBooks);
    }
}
