package com.bt.rsqe.projectengine.web.quoteoption;

import com.bt.rsqe.customerinventory.parameter.LineItemId;
import com.bt.rsqe.customerrecord.SiteDTO;
import com.bt.rsqe.enums.ProductCodes;
import com.bt.rsqe.Money;
import com.bt.rsqe.projectengine.web.facades.FlattenedProductStructure;
import com.bt.rsqe.projectengine.web.facades.FutureProductInstanceFacade;
import com.bt.rsqe.projectengine.web.model.FutureAssetPricesModel;
import com.bt.rsqe.projectengine.web.model.LineItemModel;
import com.bt.rsqe.projectengine.web.model.OneVoiceConfiguration;
import com.bt.rsqe.projectengine.web.model.OneVoicePriceTariff;
import com.bt.rsqe.projectengine.web.model.PriceLineModel;
import com.bt.rsqe.projectengine.web.view.filtering.Filters;
import com.google.common.base.Strings;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.bt.rsqe.projectengine.web.model.OneVoicePriceTariff.*;
import static com.bt.rsqe.utils.Channels.*;
import static com.google.common.base.Strings.*;
import static com.google.common.collect.Lists.*;

public class QuoteOptionBcmExportChannelInformationSheetFactory {
    private static final OneVoiceChannelBasedPriceLineFilter ONE_VOICE_CHANNEL_BASED_PRICE_LINE_FILTER = new OneVoiceChannelBasedPriceLineFilter();
    private FutureProductInstanceFacade futureProductInstanceFacade;

    public QuoteOptionBcmExportChannelInformationSheetFactory(FutureProductInstanceFacade futureProductInstanceFacade) {
        this.futureProductInstanceFacade = futureProductInstanceFacade;
    }

    List<Map<String, String>> createChannelInfoSheetRows(List<LineItemModel> lineItemModels) {
        List<Map<String, String>> rows = new ArrayList<Map<String, String>>();
        for (LineItemModel lineItem : lineItemModels) {
            Map<String, String> row = new HashMap<String, String>();
            addChannelInfoSiteDetails(lineItem.getSite(), row);
            addChannelInfoNumberOfChannels(lineItem.getLineItemId(), row);
            addChannelInfoPricingDetails(lineItem, row);
            addChannelInfoTariffType(lineItem, row);
            addChannelInfoEmptyValues(row);
            rows.add(row);
        }
        return rows;
    }

    private void addChannelInfoTariffType(LineItemModel lineItem, Map<String, String> row) {
        final FutureAssetPricesModel pricesModel = lineItem.getFutureAssetPricesModel();
        String tariffOptions = ""; //pricesModel.findFirstPriceLineWithAssetCharacteristicValue(OneVoiceConfiguration.BasicMPLS.BTPriceLine.TARIFF_OPTIONS);
        row.put("ov-channel-info.tariff-type", tariffOptions);
    }

    private void addChannelInfoNumberOfChannels(LineItemId lineItemId, Map<String, String> row) {
        row.put("ov-channel-info.number-of-channels", String.valueOf(retrieveNumberOfVoiceChannels(lineItemId)));
    }

    private void addChannelInfoSiteDetails(SiteDTO siteDTO, Map<String, String> row) {
        row.put("ov-channel-info.originating-country", siteDTO.country);
        row.put("ov-channel-info.city", siteDTO.city);
        row.put("ov-channel-info.site-id", siteDTO.bfgSiteID);
        row.put("ov-channel-info.site-name", siteDTO.name);
    }

    private void addChannelInfoPricingDetails(LineItemModel lineItem, Map<String, String> row) {
        final FutureAssetPricesModel pricesModel = lineItem.getFutureAssetPricesModel();
        List<PriceLineModel> channelBasedPriceLines = pricesModel.filterPriceLines(ONE_VOICE_CHANNEL_BASED_PRICE_LINE_FILTER);
        final PriceLineModel configPriceLine = findPriceLine(channelBasedPriceLines, configPpsrIds());
        final PriceLineModel subscriptionPriceLine = findPriceLine(channelBasedPriceLines, subscriptionPpsrIds());

        String configRrpPrice = "";
        String configRrpPricePerChannel = "";
        String configPtpPrice = "";
        String configPtpPricePerChannel = "";
        String configDiscount = "";
        String subscriptionRrpPrice = "";
        String subscriptionRrpPricePerChannel = "";
        String subscriptionPtpPrice = "";
        String subscriptionPtpPricePerChannel = "";
        String subscriptionDiscount = "";

        final int numberOfVoiceChannels = retrieveNumberOfVoiceChannels(lineItem.getLineItemId());

        if (configPriceLine != null) {
            final Money oneTimeValue = configPriceLine.getOneTimeCPValue();

            if (userCanViewIndirectPrices()) {
                final Money rrp = configPriceLine.getGrossOneTimeEUP();
                configRrpPrice = rrp.toString();
                configRrpPricePerChannel = rrp.divideBy(numberOfVoiceChannels).toString();
                configPtpPrice = oneTimeValue.toString();
                configPtpPricePerChannel = oneTimeValue.divideBy(numberOfVoiceChannels).toString();
            } else {
                configRrpPrice = oneTimeValue.toString();
                configRrpPricePerChannel = oneTimeValue.divideBy(numberOfVoiceChannels).toString();
            }
            configDiscount = configPriceLine.getOneTimeDto().discount;
        }
        if (subscriptionPriceLine != null) {

            final Money recurringValue = subscriptionPriceLine.getRecurringCPValue();

            if (userCanViewIndirectPrices()) {
                final Money rrp = subscriptionPriceLine.getGrossRecurringEUP();
                subscriptionRrpPrice = rrp.toString();
                subscriptionRrpPricePerChannel = rrp.divideBy(numberOfVoiceChannels).toString();
                subscriptionPtpPrice = recurringValue.toString();
                subscriptionPtpPricePerChannel = recurringValue.divideBy(numberOfVoiceChannels).toString();
            } else {
                subscriptionRrpPrice = recurringValue.toString();
                subscriptionRrpPricePerChannel = recurringValue.divideBy(numberOfVoiceChannels).toString();
            }
            subscriptionDiscount = subscriptionPriceLine.getRecurringDto().discount;
        }

        row.put("ov-channel-info.access-type", "MPLS");
        row.put("ov-channel-info.config-rrp-total", configRrpPrice);
        row.put("ov-channel-info.config-ptp-total", configPtpPrice);
        row.put("ov-channel-info.config-discount", changeDiscountToDecimalAndRound(configDiscount));
        row.put("ov-channel-info.subscription-rrp-total", subscriptionRrpPrice);
        row.put("ov-channel-info.subscription-ptp-total", subscriptionPtpPrice);
        row.put("ov-channel-info.subscription-discount", changeDiscountToDecimalAndRound(subscriptionDiscount));
        row.put("ov-channel-info.config-rrp-per-channel", configRrpPricePerChannel);
        row.put("ov-channel-info.config-ptp-per-channel", configPtpPricePerChannel);
        row.put("ov-channel-info.subscription-rrp-per-channel", subscriptionRrpPricePerChannel);
        row.put("ov-channel-info.subscription-ptp-per-channel", subscriptionPtpPricePerChannel);
    }

    private void addChannelInfoEmptyValues(Map<String, String> row) {
        row.put("ov-channel-info.site-status", "");
        row.put("ov-channel-info.number-of-ranges", "");
        row.put("ov-channel-info.number-of-recommended-ranges", "");
        row.put("ov-channel-info.requested-price-per-channel", "");
        row.put("ov-channel-info.effective-discount", "");
        row.put("ov-channel-info.eup-total-for-number-ranges", "");
        row.put("ov-channel-info.one-time-revenue-on-number-ranges", "");
    }

    private int retrieveNumberOfVoiceChannels(LineItemId lineItemId) {
        final FlattenedProductStructure productInstances = futureProductInstanceFacade.getProductInstances(lineItemId);
        final String voiceChannels = productInstances.firstAttributeValueFor(ProductCodes.OnevoiceOptions.productCode(),
                                                                             OneVoiceConfiguration.BasicMPLS.OneVoiceOptions.NUMBER_VOICE_CHANNELS);
        if (Strings.isNullOrEmpty(voiceChannels)) {
            return 0;
        }
        return Integer.valueOf(voiceChannels);
    }

    private String changeDiscountToDecimalAndRound(String value) {
        if (isNullOrEmpty(value)) {
            return "";
        }
        final BigDecimal decimalValue = new BigDecimal(value).movePointLeft(2);
        return round(decimalValue, 7).toString();
    }

    public static BigDecimal round(BigDecimal value, int scale) {
        BigDecimal nonNullValue = value == null ? BigDecimal.ZERO : value;
        return nonNullValue.setScale(scale, BigDecimal.ROUND_HALF_UP);
    }

    private List<Long> configPpsrIds() {
        return newArrayList(OneVoicePriceTariff.GLOBAL_DIRECT_SINGLE_CHANNEL_CONFIG.ppsrId(),
                            OneVoicePriceTariff.GLOBAL_DIRECT_LITE_CHANNEL_CONFIG.ppsrId(),
                            OneVoicePriceTariff.GLOBAL_INCLUSIVE_SINGLE_CHANNEL_CONFIG.ppsrId());
    }

    private List<Long> subscriptionPpsrIds() {
        return newArrayList(OneVoicePriceTariff.GLOBAL_DIRECT_SINGLE_CHANNEL_SUBSCRIPTION.ppsrId(),
                            OneVoicePriceTariff.GLOBAL_DIRECT_LITE_CHANNEL_SUBSCRIPTION.ppsrId(),
                            OneVoicePriceTariff.GLOBAL_INCLUSIVE_SINGLE_CHANNEL_SUBSCRIPTION.ppsrId());
    }

    private PriceLineModel findPriceLine(List<PriceLineModel> priceLines, List<Long> priceLinePpsrIds) {
        PriceLineModel configPriceLine = null;
        for (Long ppsrId : priceLinePpsrIds) {
            configPriceLine = findPriceLineByPpsrId(priceLines, ppsrId);
            if (configPriceLine != null) {
                break;
            }
        }
        return configPriceLine;
    }

    private PriceLineModel findPriceLineByPpsrId(List<PriceLineModel> channelBasedPriceLines, Long ppsrId) {
        PriceLineModel result = null;
        for (PriceLineModel priceLine : channelBasedPriceLines) {
            if (ppsrId.equals(priceLine.getPpsrId())) {
                result = priceLine;
                break;
            }
        }
        return result;
    }

    private static class OneVoiceChannelBasedPriceLineFilter implements Filters.Filter<PriceLineModel> {
        @Override
        public boolean apply(PriceLineModel model) {
            for (OneVoicePriceTariff oneVoicePpsrId : CHANNEL_BASED_PPSR_IDS) {
                if (oneVoicePpsrId.ppsrId().equals(model.getPpsrId())) {
                    return true;
                }
            }
            return false;
        }
    }
}
