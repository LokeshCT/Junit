package com.bt.rsqe.projectengine.web.quoteoption.priceupdater;

import com.bt.rsqe.enums.PriceType;
import com.bt.rsqe.Percentage;
import com.bt.rsqe.domain.QuoteOptionItemStatus;
import com.bt.rsqe.projectengine.web.model.FutureAssetPricesModel;
import com.bt.rsqe.projectengine.web.model.LineItemModel;
import com.bt.rsqe.projectengine.web.model.OneVoicePriceTariff;
import com.bt.rsqe.projectengine.web.model.PriceLineModel;
import com.bt.rsqe.projectengine.web.model.bcmspreadsheet.OneVoiceChannelInformationRow;
import com.bt.rsqe.projectengine.web.model.bcmspreadsheet.OneVoiceChannelInformationSheet;
import com.bt.rsqe.projectengine.web.view.filtering.Filters;

import java.util.List;

import static com.bt.rsqe.domain.QuoteOptionItemStatus.*;
import static com.bt.rsqe.projectengine.web.model.OneVoicePriceTariff.*;

public class ChannelInfoSheetPriceUpdater implements FutureAssetPriceUpdater {

    private final OneVoiceChannelInformationSheet channelInformationSheet;
    private final boolean applyPtpDiscount;
    private final boolean applyRrpDiscount;

    private ChannelInfoSheetPriceUpdater(OneVoiceChannelInformationSheet channelInformationSheet, boolean applyPtpDiscount, boolean applyRrpDiscount) {
        this.channelInformationSheet = channelInformationSheet;
        this.applyPtpDiscount = applyPtpDiscount;
        this.applyRrpDiscount = applyRrpDiscount;
    }

    public static ChannelInfoSheetPriceUpdater forIndirect(OneVoiceChannelInformationSheet channelInformationSheet) {
        return new ChannelInfoSheetPriceUpdater(channelInformationSheet, true, false);
    }

    public static ChannelInfoSheetPriceUpdater forDirect(OneVoiceChannelInformationSheet channelInformationSheet) {
        return new ChannelInfoSheetPriceUpdater(channelInformationSheet, false, true);
    }

    @Override
    public void update(LineItemModel lineItem) {
        final String siteId = lineItem.getSiteId();
        if (channelInformationSheet.containsSiteId(siteId) && updatableStatus(lineItem)) {
            updatePrice(lineItem.getFutureAssetPricesModel(),
                        channelInformationSheet.getOneVoiceChannelInformationRow(siteId),
                        priceLineFilter());
        }
    }

    private boolean updatableStatus(LineItemModel lineItem) {
        final QuoteOptionItemStatus status = lineItem.getLineItemStatus();
        return status != CUSTOMER_APPROVED
               && status != ORDER_CREATED
               && status != ORDER_SUBMITTED;
    }

    private void updatePrice(FutureAssetPricesModel futureAssetPrice, OneVoiceChannelInformationRow oneVoiceChannelInformationRow, Filters.Filter<PriceLineModel> priceLineFilter) {
        List<PriceLineModel> channelBasedPriceLines = futureAssetPrice.filterPriceLines(priceLineFilter);
        if (applyPtpDiscount) {
            updatePriceLineDiscount(channelBasedPriceLines, oneVoiceChannelInformationRow.getPTPConfigDiscount(), PriceType.ONE_TIME);
            updatePriceLineDiscount(channelBasedPriceLines, oneVoiceChannelInformationRow.getPTPSubscriptionDiscount(), PriceType.RECURRING);
        }
        if (applyRrpDiscount) {
            updatePriceLineDiscount(channelBasedPriceLines, oneVoiceChannelInformationRow.getRRPConfigDiscount(), PriceType.ONE_TIME);
            updatePriceLineDiscount(channelBasedPriceLines, oneVoiceChannelInformationRow.getRRPSubscriptionDiscount(), PriceType.RECURRING);
        }
    }

    protected void updatePriceLineDiscount(List<PriceLineModel> channelBasedPriceLines, Percentage discount, PriceType priceType) {
        final PriceLineModel priceLine = findByPpsrId(channelBasedPriceLines, priceType);
        priceLine.setDiscount(discount, priceType);
    }

    private PriceLineModel findByPpsrId(List<PriceLineModel> channelBasedPriceLines, PriceType priceType) {
        PriceLineModel result = null;
        for (PriceLineModel priceLine : channelBasedPriceLines) {
            final OneVoicePriceTariff ppsrId = OneVoicePriceTariff.forId(priceLine.getPpsrId());
            if (CHANNEL_BASED_PPSR_IDS.contains(ppsrId) && ppsrId.isForType(priceType)) {
                result = priceLine;
                break;
            }
        }
        return result;
    }

    private Filters.Filter<PriceLineModel> priceLineFilter() {
        return new Filters.Filter<PriceLineModel>() {
            @Override
            public boolean apply(PriceLineModel model) {
                for (OneVoicePriceTariff oneVoicePpsrId : CHANNEL_BASED_PPSR_IDS) {
                    if (oneVoicePpsrId.ppsrId().equals(model.getPpsrId())) {
                        return true;
                    }
                }
                return false;
            }
        };
    }
}
