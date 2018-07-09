package com.bt.rsqe.projectengine.web.quoteoption.priceupdater;

import com.bt.rsqe.Money;
import com.bt.rsqe.Percentage;
import com.bt.rsqe.projectengine.web.model.FutureAssetPricesModel;
import com.bt.rsqe.projectengine.web.model.LineItemModel;
import com.bt.rsqe.projectengine.web.model.OneVoicePriceTariff;
import com.bt.rsqe.projectengine.web.model.PriceLineModel;
import com.bt.rsqe.projectengine.web.model.bcmspreadsheet.OneVoiceBcmOptionsRow;
import com.bt.rsqe.projectengine.web.model.bcmspreadsheet.OneVoiceBcmOptionsSheet;
import com.bt.rsqe.projectengine.web.view.filtering.Filters;

import java.util.List;

import static com.bt.rsqe.projectengine.web.model.OneVoicePriceTariff.*;

public class OptionsSheetPriceUpdater implements FutureAssetPriceUpdater {

    private final OneVoiceBcmOptionsSheet optionsSheet;

    public OptionsSheetPriceUpdater(OneVoiceBcmOptionsSheet optionsSheet) {
        this.optionsSheet = optionsSheet;
    }

    @Override
    public void update(LineItemModel lineItem) {
        if (optionsSheet.containsSiteId(lineItem.getSiteId())) {
            updatePrice(lineItem.getFutureAssetPricesModel(), optionsSheet.rowForSiteId(lineItem.getSiteId()), priceLineFilter());
        }
    }

    private void updatePrice(FutureAssetPricesModel futureAssetPrice, OneVoiceBcmOptionsRow row, Filters.Filter<PriceLineModel> priceLineFilter) {
        List<PriceLineModel> nonChannelBasedPriceLines = futureAssetPrice.filterPriceLines(priceLineFilter);
        updatePriceLineDiscount(nonChannelBasedPriceLines, VPN_CONFIG, Percentage.from(row.vpnConfigDiscount()));
        updatePriceLineDiscount(nonChannelBasedPriceLines, VPN_SUBSCRIPTION, Percentage.from(row.vpnSubscriptionDiscount()));
        updatePriceLineDiscount(nonChannelBasedPriceLines, DIAL_PLAN_CHANGE_CONFIG, Percentage.from(row.dialplanChangeConfigDiscount()));
        updatePriceLineDiscount(nonChannelBasedPriceLines, MAJOR_MOVE_ADDS_OR_CHANGE_CONFIG, Percentage.from(row.mmacConfigDiscount()));
        if (row.hasAmendmentCharge()) {
            updateOneTimeChargePrice(nonChannelBasedPriceLines, AMENDMENT_CHARGE, row.amendmentCharge());
        }
        if (row.hasCancellationCharge()) {
            updateOneTimeChargePrice(nonChannelBasedPriceLines, CANCELLATION_CHARGE, row.cancellationCharge());
        }
    }

    private void updateOneTimeChargePrice(List<PriceLineModel> nonChannelBasedPriceLines, OneVoicePriceTariff ppsrId, Money chargePrice) {
        PriceLineModel priceLineModel = findByPpsrId(nonChannelBasedPriceLines, ppsrId.ppsrId());
        if (priceLineModel != null) {
            priceLineModel.setOneTimeCPValue(chargePrice);
        }
    }

    protected void updatePriceLineDiscount(List<PriceLineModel> nonChannelBasedPriceLines, OneVoicePriceTariff ppsrId, Percentage discount) {
        final PriceLineModel priceLine = findByPpsrId(nonChannelBasedPriceLines, ppsrId.ppsrId());
        if (priceLine != null) {
            priceLine.setDiscount(discount, ppsrId.priceType());
        }
    }

    private PriceLineModel findByPpsrId(List<PriceLineModel> nonChannelBasedPriceLines, Long ppsrId) {
        for (PriceLineModel priceLine : nonChannelBasedPriceLines) {
            if (ppsrId.equals(priceLine.getPpsrId())) {
                return priceLine;
            }
        }
        // TODO: throw exception rather than returning null
        // throw new RuntimeException("Unable to find PriceLine with ppsrId " + ppsrId );
        return null;
    }

    private Filters.Filter<PriceLineModel> priceLineFilter() {
        return new Filters.Filter<PriceLineModel>() {
            @Override
            public boolean apply(PriceLineModel model) {
                for (OneVoicePriceTariff oneVoicePpsrId : NON_CHANNEL_BASED_PPSR_IDS) {
                    if (oneVoicePpsrId.ppsrId().equals(model.getPpsrId())) {
                        return true;
                    }
                }
                return false;
            }
        };
    }
}
