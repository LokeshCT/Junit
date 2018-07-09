package com.bt.rsqe.projectengine.web.model.modelfactory;

import com.bt.rsqe.projectengine.OfferDTO;
import com.bt.rsqe.projectengine.web.model.OfferDetailsModel;

public class OfferDetailsModelFactory {

    private final LineItemModelFactory lineItemModelFactory;

    public OfferDetailsModelFactory(LineItemModelFactory lineItemModelFactory) {
        this.lineItemModelFactory = lineItemModelFactory;
    }

    public OfferDetailsModel create(OfferDTO offerDTO) {
        return new OfferDetailsModel(lineItemModelFactory, offerDTO);
    }
}
