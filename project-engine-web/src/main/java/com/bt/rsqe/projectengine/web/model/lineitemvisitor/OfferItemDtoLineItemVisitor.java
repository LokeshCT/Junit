package com.bt.rsqe.projectengine.web.model.lineitemvisitor;

import com.bt.rsqe.projectengine.web.model.FutureAssetPricesModel;
import com.bt.rsqe.projectengine.web.model.LineItemModel;
import com.bt.rsqe.projectengine.web.view.OfferDetailsDTO;
import com.bt.rsqe.projectengine.web.view.SiteView;

import java.util.List;

public class OfferItemDtoLineItemVisitor extends CompositeLineItemVisitor {
    private List<OfferDetailsDTO.ItemRowDTO> itemRowDTOs;

    public OfferItemDtoLineItemVisitor(List<OfferDetailsDTO.ItemRowDTO> itemRowDTOs) {
        this.itemRowDTOs = itemRowDTOs;
    }

    @Override
    public void visitAfterChildren(LineItemModel lineItem) {
        super.visitAfterChildren(lineItem);
        FutureAssetPricesModel futureAssetPricesModel = lineItem.getFutureAssetPricesModel();
        boolean isManualModify = false;
        if (lineItem.getAction().equals("Modify")) {
            isManualModify = true;
        }
        itemRowDTOs.add(new OfferDetailsDTO.ItemRowDTO(lineItem.getId(),
                                                       futureAssetPricesModel.getDisplayName(),
                                                       miniAddress(lineItem),
                                                       new SiteView(lineItem.getSite()),
                                                       lineItem.getStatus(),
                                                       lineItem.getDiscountStatus(),
                                                       lineItem.getPricingStatusOfTree().getDescription(),
                                                       lineItem.getValidity(),
                                                       lineItem.getErrorMessage(),
                                                       lineItem.getSummary(),
                                                       lineItem.isForIfc(),
                                                       isManualModify,
                                                       lineItem.isQuoteOnlyLeadToCashPhase()));

    }

    private String miniAddress(LineItemModel lineItem) {
        if (lineItem.getSite() != null) {
            if ((lineItem.getSite().city != null) && (lineItem.getSite().country != null)) {
                return lineItem.getSite().city + ", " + lineItem.getSite().country;
            } else if ((lineItem.getSite().city != null) && (lineItem.getSite().country == null)) {
                return lineItem.getSite().city;
            } else if ((lineItem.getSite().city == null) && (lineItem.getSite().country != null)) {
                return lineItem.getSite().country;
            } else {
                return "";
            }

        } else {
            return "";
        }

    }

}
