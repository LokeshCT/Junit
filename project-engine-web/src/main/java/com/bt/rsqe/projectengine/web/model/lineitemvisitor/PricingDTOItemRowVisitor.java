package com.bt.rsqe.projectengine.web.model.lineitemvisitor;

import com.bt.rsqe.projectengine.LineItemDiscountStatus;
import com.bt.rsqe.projectengine.web.model.FutureAssetPricesModel;
import com.bt.rsqe.projectengine.web.model.LineItemModel;
import com.bt.rsqe.projectengine.web.model.PriceLineModel;
import com.bt.rsqe.projectengine.web.view.QuoteOptionPricingDTO;

import java.util.List;

import static com.bt.rsqe.enums.PriceType.*;
import static com.bt.rsqe.security.UserContextManager.getPermissions;

public class PricingDTOItemRowVisitor extends AbstractLineItemVisitor {

    private List<QuoteOptionPricingDTO.ItemRowDTO> list;
    private LineItemModel lineItem;
    private FutureAssetPricesModel futureAssetPricesModel;
    private int groupingLevel;
    private static final String USER_ENTERED = "User Entered";

    public PricingDTOItemRowVisitor(List<QuoteOptionPricingDTO.ItemRowDTO> list) {
        this.list = list;
    }

    public void visit(LineItemModel lineItem) {
        this.lineItem = lineItem;
    }

    @Override
    public void visit(FutureAssetPricesModel futureAssetPricesModel, int groupingLevel) {
        this.futureAssetPricesModel = futureAssetPricesModel;
        this.groupingLevel = groupingLevel;

        addAggregateRow(futureAssetPricesModel, groupingLevel);
    }

    @Override
    public void visit(PriceLineModel priceLine) {
        if (priceLine.getPriceType() == USAGE_BASED || skipCurrentFutureAsset()) {
            return;
        }

        QuoteOptionPricingDTO.ItemRowDTO row = new QuoteOptionPricingDTO.ItemRowDTO();
        row.product = futureAssetPricesModel.getDisplayName();
        row.summary = lineItem.getSummary();
        row.site = futureAssetPricesModel.getSiteName();
        row.miniAddress=miniAddress(lineItem);
        String description = priceLine.getDescription();
        row.description = description == null ? "" : description;
        row.userEntered = priceLine.getUserEntered();
        row.status = getStatus(priceLine);
        row.discountStatus = lineItem.getDiscountStatus();
        row.offerName = lineItem.getOfferName();
        row.groupingLevel = groupingLevel;
        row.aggregateRow = false;
        row.oneTime = priceLine.getOneTimeDto();
        row.recurring = priceLine.getRecurringDto();
        row.lineItemId = lineItem.getId();
        row.readOnly = resolvePriceReadOnly(priceLine);
        row.isManualPricing = priceLine.isManualPricing();
        list.add(row);




    }

    private String getStatus(PriceLineModel priceLine) {
        if (priceLine.getUserEntered() != null) {
            return priceLine.getStatus() + " (" + USER_ENTERED + ")";

        }
        return priceLine.getStatus();
    }

    private boolean skipCurrentFutureAsset() {
        return futureAssetPricesModel.hasNoPriceLines();
    }

    private boolean resolvePriceReadOnly(PriceLineModel priceLine) {
        return priceLine.isCustomerAggregatedPrice() || isPriceReadOnly();
    }

    public boolean isPriceReadOnly() {
        if (LineItemDiscountStatus.APPROVAL_REQUESTED.getDescription().equals(lineItem.getDiscountStatus())) {
            if(getPermissions().discountAccess) {
                return false;
            } else if(!getPermissions().bcmAccess) {
                return true;
            }
        }
        return lineItem.isReadOnly();
    }

    private void addAggregateRow(FutureAssetPricesModel futureAssetPricesModel, int groupingLevel) {
        if (lineItem.isPricingStatusOfTreeApplicableForOnPricingTab() && skipCurrentFutureAsset() && groupingLevel == 0) {
            QuoteOptionPricingDTO.ItemRowDTO row = new QuoteOptionPricingDTO.ItemRowDTO();
            row.product = futureAssetPricesModel.getDisplayName();
            row.summary = lineItem.getSummary();
            row.site = futureAssetPricesModel.getSiteName();
            row.miniAddress = miniAddress(lineItem); //added by richie
            row.groupingLevel = groupingLevel;
            row.aggregateRow = true;
            row.description = "";
            row.status = "";
            row.discountStatus = lineItem.getDiscountStatus();
            row.offerName = lineItem.getOfferName();
            row.lineItemId = "";
            row.oneTime = new QuoteOptionPricingDTO.PriceLineDTO();
            row.recurring = new QuoteOptionPricingDTO.PriceLineDTO();
            row.readOnly = lineItem.isReadOnly();
            row.forIfc = lineItem.isForIfc();
            list.add(row);
        }
    }

        private String miniAddress(LineItemModel lineItem) {
            if (lineItem.getSite() != null) {
                if ((lineItem.getSite().city != null) && (lineItem.getSite().country != null)) {
                    return lineItem.getSite().city + ", " + lineItem.getSite().country;
                } else if ((lineItem.getSite().city != null) && (lineItem.getSite().country == null)) {
                    return lineItem.getSite().city;
                } else if ((lineItem.getSite().city == null) && (lineItem.getSite().country != null)) {
                    return lineItem.getSite().country;
                } else  {
                    return "";
                }

            } else {
                return "";
            }


        }


}
