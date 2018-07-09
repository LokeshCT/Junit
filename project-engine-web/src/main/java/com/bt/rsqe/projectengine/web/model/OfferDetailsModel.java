package com.bt.rsqe.projectengine.web.model;

import com.bt.rsqe.domain.QuoteOptionItemStatus;
import com.bt.rsqe.domain.project.OfferStatus;
import com.bt.rsqe.projectengine.OfferDTO;
import com.bt.rsqe.projectengine.QuoteOptionItemDTO;
import com.bt.rsqe.projectengine.web.model.modelfactory.LineItemModelFactory;
import com.bt.rsqe.projectengine.web.quoteoptionpricing.PriceSuppressStrategy;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;

import javax.annotation.Nullable;
import java.util.List;

import static com.google.common.collect.Lists.*;

public class OfferDetailsModel {
    private LineItemModelFactory lineItemModelFactory;
    private final OfferDTO offerDTO;

    public OfferDetailsModel(LineItemModelFactory lineItemModelFactory, OfferDTO offerDTO) {
        this.lineItemModelFactory = lineItemModelFactory;
        this.offerDTO = offerDTO;
    }

    public String getId() {
        return offerDTO.id;
    }

    public String getCreatedDate() {
        return offerDTO.created;
    }

    public String getName() {
        return offerDTO.name;
    }

    public OfferStatus getStatus() {
        return OfferStatus.valueOf(offerDTO.status);
    }

    public boolean isApproved() {
        return getStatus() == OfferStatus.APPROVED;
    }

    public boolean isActive() {
        return getStatus() == OfferStatus.ACTIVE;
    }

    public boolean  isCustomerCancellable() {

        for (QuoteOptionItemDTO offerItem : offerDTO.offerItems) {
            if ((offerItem.status.equals(QuoteOptionItemStatus.COMPLETE) || offerItem.status.equals(QuoteOptionItemStatus.ORDER_SUBMITTED))
                    || offerItem.status.equals(QuoteOptionItemStatus.ORDER_CREATED)) {
                return false;
            }

        }
        return !isCustomerApprovable();
    }



    public boolean isCustomerApprovable() {
        return (!offerDTO.status.equals(OfferStatus.APPROVED.toString()));
    }

    public List<LineItemModel> getLineItems(final String customerId, final String contractId, final String projectId, final String quoteOptionId) {
        return newArrayList(Iterables.transform(offerDTO.offerItems, new Function<QuoteOptionItemDTO, LineItemModel>() {
            @Override
            public LineItemModel apply(@Nullable QuoteOptionItemDTO input) {
                return lineItemModelFactory.create(projectId, quoteOptionId, customerId, contractId, input, PriceSuppressStrategy.OFFERS_UI,null);
            }
        }));
    }

    public String getCustomerOrderReference() {
        return offerDTO.customerOrderReference;
    }
}
