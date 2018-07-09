package com.bt.rsqe.projectengine.web.quoteoption;

import com.bt.rsqe.customerrecord.SiteDTO;
import com.bt.rsqe.domain.project.PricingStatus;
import com.bt.rsqe.excel.ExcelMerge;
import com.bt.rsqe.projectengine.web.facades.LineItemFacade;
import com.bt.rsqe.projectengine.web.model.FutureAssetPricesModel;
import com.bt.rsqe.projectengine.web.model.LineItemModel;
import com.bt.rsqe.projectengine.web.model.OneVoicePriceTariff;
import com.bt.rsqe.projectengine.web.model.PriceLineModel;
import com.bt.rsqe.projectengine.web.quoteoption.bcmsheet.ProductsBCMSheetFactory;
import com.bt.rsqe.projectengine.web.quoteoptionpricing.PriceSuppressStrategy;
import com.bt.rsqe.projectengine.web.view.SiteView;
import com.bt.rsqe.projectengine.web.view.filtering.Filters;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.bt.rsqe.projectengine.web.model.OneVoicePriceTariff.*;
import static com.bt.rsqe.utils.Channels.*;
import static com.google.common.base.Strings.*;
import static com.google.common.collect.Lists.*;
import static com.google.common.collect.Maps.*;

public class QuoteOptionBcmExportPricingSheetOrchestrator {
    private static final OneVoiceNonChannelBasedPriceLineFilter ONE_VOICE_NON_CHANNEL_BASED_PRICE_LINE_FILTER = new OneVoiceNonChannelBasedPriceLineFilter();

    private final LineItemFacade lineItemFacade;
    private QuoteOptionBcmExportChannelInformationSheetFactory channelInformationSheetFactory;
    private QuoteOptionBcmExportBidInfoSheetFactory bidInfoSheetFactory;
    private final QuoteOptionBcmExportUsageSheetFactory usageSheetFactory;
    private QuoteOptionBcmExportSpecialPriceBookSheetFactory priceBookSheetFactory;
    private final QuoteOptionBcmExportSiteDetailsSheetFactory siteDetailsSheetFactory;
    private QuoteOptionBcmExportProductLevelInfoSheetFactory productLevelInfoSheetFactory;
    private final ProductsBCMSheetFactory productsBCMSheetFactory;


    public QuoteOptionBcmExportPricingSheetOrchestrator(LineItemFacade lineItemFacade,
                                                        QuoteOptionBcmExportChannelInformationSheetFactory channelInformationSheetFactory,
                                                        QuoteOptionBcmExportBidInfoSheetFactory bidInfoSheetFactory,
                                                        QuoteOptionBcmExportUsageSheetFactory usageSheetFactory,
                                                        QuoteOptionBcmExportSpecialPriceBookSheetFactory priceBookSheetFactory,
                                                        QuoteOptionBcmExportSiteDetailsSheetFactory siteDetailsSheetFactory,
                                                        QuoteOptionBcmExportProductLevelInfoSheetFactory productLevelInfoSheetFactory,
                                                        ProductsBCMSheetFactory productsBCMSheetFactory) {
        this.lineItemFacade = lineItemFacade;
        this.channelInformationSheetFactory = channelInformationSheetFactory;
        this.bidInfoSheetFactory = bidInfoSheetFactory;
        this.usageSheetFactory = usageSheetFactory;
        this.priceBookSheetFactory = priceBookSheetFactory;
        this.siteDetailsSheetFactory = siteDetailsSheetFactory;
        this.productLevelInfoSheetFactory = productLevelInfoSheetFactory;
        this.productsBCMSheetFactory = productsBCMSheetFactory;
    }

    public HSSFWorkbook renderBcmExportSheet(String customerId, String contractId, String projectId, String quoteOptionId) {
        Map<String, List<Map<String, String>>> data = buildExportView(customerId, contractId, projectId, quoteOptionId);
        HSSFWorkbook bcmWorkBook = ExcelMerge.merge("BCM-Details.xls", null, data);
        createBCMProductSheetsAndRenderWithExistingWorkBook(bcmWorkBook, customerId, projectId, quoteOptionId);
        return bcmWorkBook;
    }

    protected void createBCMProductSheetsAndRenderWithExistingWorkBook(HSSFWorkbook bcmWorkBook, String customerId, String projectId, String quoteOptionId) {
        productsBCMSheetFactory.createProductSheets(bcmWorkBook, customerId, projectId, quoteOptionId);
    }

    protected Map<String, List<Map<String, String>>> buildExportView(String customerId, String contractId, String projectId, String quoteOptionId) {
        List<LineItemModel> lineItemModels = getOneVoiceLineItemModels(customerId, contractId, projectId, quoteOptionId);
        List<LineItemModel> pricedLineItemModels = filterNonPricedLineItemModels(lineItemModels);
        List<LineItemModel> sortedLineItemModels = sortOneVoiceLineItemModels(pricedLineItemModels);

        List<Map<String, String>> optionsSheetRows = createOptionsSheetRows(sortedLineItemModels);
        List<Map<String, String>> channelInfoRows = channelInformationSheetFactory.createChannelInfoSheetRows(sortedLineItemModels);
        List<Map<String, String>> bidInfoRow = bidInfoSheetFactory.createBidInfoRow(customerId, projectId, quoteOptionId);
        List<Map<String, String>> usageInfoRow = usageSheetFactory.createUsageRows(sortedLineItemModels);
        List<Map<String, String>> priceBookRows = priceBookSheetFactory.createPriceBookSheetRows(quoteOptionId);
        List<Map<String, String>> siteDetailsSheetRows = siteDetailsSheetFactory.createSiteDetailsRows(lineItemModels);
        List<Map<String, String>> productLevelInfoSheetRows = productLevelInfoSheetFactory.createProductLevelInfoSheetRows(lineItemModels);

        Map<String, List<Map<String, String>>> table = newHashMap();
        table.put("ov-options", optionsSheetRows);
        table.put("ov-channel-info", channelInfoRows);
        table.put("bid-info", bidInfoRow);
        table.put("ov-usage", usageInfoRow);
        table.put("priceBook", priceBookRows);
        table.put("site-details", siteDetailsSheetRows);
        table.put("product-level-info", productLevelInfoSheetRows);
        return table;
    }

    private List<LineItemModel> filterNonPricedLineItemModels(List<LineItemModel> lineItemModels) {
        final ArrayList<LineItemModel> pricedLineItemModels = newArrayList();

        for (LineItemModel lineItemModel : lineItemModels) {
            if (lineItemModel.getPricingStatusOfTree() != PricingStatus.NOT_PRICED) {
                pricedLineItemModels.add(lineItemModel);
            }
        }

        return pricedLineItemModels;
    }

    private List<LineItemModel> sortOneVoiceLineItemModels(List<LineItemModel> oneVoiceLineItemModels) {
        Comparator<LineItemModel> lineItemModelComparator = new Comparator<LineItemModel>() {
            @Override
            public int compare(LineItemModel o1, LineItemModel o2) {
                if(o1 != null && o1.hasSite() && o2 != null && o2.hasSite()){
                    return o1.getSite().bfgSiteID.compareTo(o2.getSite().bfgSiteID);
                }else {
                    return -1;
                }
            }
        };
        Collections.sort(oneVoiceLineItemModels, lineItemModelComparator);
        return oneVoiceLineItemModels;
    }

    private List<LineItemModel> getOneVoiceLineItemModels(String customerId, String contractId, String projectId, String quoteOptionId) {
        return lineItemFacade.fetchLineItems(customerId, contractId, projectId, quoteOptionId, PriceSuppressStrategy.None);
    }

    private List<Map<String, String>> createOptionsSheetRows(List<LineItemModel> lineItems) {
        List<Map<String, String>> rows = new ArrayList<Map<String, String>>();
        for (LineItemModel lineItem : lineItems) {
            Map<String, String> row = new HashMap<String, String>();
            addSiteDetails(lineItem.getSite(), row);
            addOptionsPricingSheetDetails(lineItem.getFutureAssetPricesModel(), row);
            rows.add(row);
        }
        return rows;
    }

    private void addSiteDetails(SiteDTO siteDTO, Map<String, String> row) {
        SiteView site = new SiteView(siteDTO);
        row.put("ov-options.site-id", site.getId());
        row.put("ov-options.site-name", site.getName());
        row.put("ov-options.site-address", site.getFullAddress());
    }

    private void addOptionsPricingSheetDetails(FutureAssetPricesModel pricesModel, Map<String, String> row) {
        List<PriceLineModel> nonChannelBasedPriceLines = pricesModel.filterPriceLines(ONE_VOICE_NON_CHANNEL_BASED_PRICE_LINE_FILTER);
        addOneTimePriceLineDetailsToRow(nonChannelBasedPriceLines, row, VPN_CONFIG.ppsrId(), "ov-options.vpnConfig-rrp", "ov-options.vpnConfig-ptp", "ov-options.vpnConfig-discount");
        addRecurringPriceLineDetailsToRow(nonChannelBasedPriceLines, row, VPN_SUBSCRIPTION.ppsrId(), "ov-options.vpnSubscription-rrp", "ov-options.vpnSubscription-ptp", "ov-options.vpnSubscription-discount");
        addOneTimePriceLineDetailsToRow(nonChannelBasedPriceLines, row, DIAL_PLAN_CHANGE_CONFIG.ppsrId(), "ov-options.dialplanChangeConfig-rrp", "ov-options.dialplanChangeConfig-ptp", "ov-options.dialplanChangeConfig-discount");
        addOneTimePriceLineDetailsToRow(nonChannelBasedPriceLines, row, MAJOR_MOVE_ADDS_OR_CHANGE_CONFIG.ppsrId(), "ov-options.mmacConfig-rrp", "ov-options.mmacConfig-ptp", "ov-options.mmacConfig-discount");
        addAmendmentCharge(nonChannelBasedPriceLines, row);
        addCancellationCharge(nonChannelBasedPriceLines, row);
    }

    private void addOneTimePriceLineDetailsToRow(List<PriceLineModel> nonChannelBasedPriceLines, Map<String, String> row, Long ppsrId, String eupPriceName, String ptpPriceName, String discountName) {
        String eupPrice = "";
        String ptpPrice = "";
        String discount = "";

        final PriceLineModel priceLine = findByPpsrId(nonChannelBasedPriceLines, ppsrId);
        if (priceLine != null) {
            if (userCanViewIndirectPrices()) {
                eupPrice = priceLine.getGrossOneTimeEUP().toString();
                ptpPrice = priceLine.getOneTimeCPValue().toString();
            } else {
                eupPrice = priceLine.getOneTimeCPValue().toString();
            }
            discount = changeDiscountToDecimalAndRound(priceLine.getOneTimeDto().discount);
        }

        row.put(eupPriceName, eupPrice);
        row.put(ptpPriceName, ptpPrice);
        row.put(discountName, discount);
    }

    private void addRecurringPriceLineDetailsToRow(List<PriceLineModel> nonChannelBasedPriceLines, Map<String, String> row, Long ppsrId, String eupPriceName, String ptpPriceName, String discountName) {
        String eupPrice = "";
        String ptpPrice = "";
        String discount = "";

        final PriceLineModel priceLine = findByPpsrId(nonChannelBasedPriceLines, ppsrId);
        if (priceLine != null) {
            if (userCanViewIndirectPrices()) {
                eupPrice = priceLine.getGrossRecurringEUP().toString();
                ptpPrice = priceLine.getRecurringCPValue().toString();
            } else {
                eupPrice = priceLine.getRecurringCPValue().toString();
            }
            discount = changeDiscountToDecimalAndRound(priceLine.getRecurringDto().discount);
        }

        row.put(eupPriceName, eupPrice);
        row.put(ptpPriceName, ptpPrice);
        row.put(discountName, discount);
    }

    private void addAmendmentCharge(List<PriceLineModel> nonChannelBasedPriceLines, Map<String, String> row) {
        String amendmentCharge = "";
        final PriceLineModel priceLine = findByPpsrId(nonChannelBasedPriceLines, AMENDMENT_CHARGE.ppsrId());
        if (priceLine != null) {
            amendmentCharge = priceLine.getOneTimeCPValue().toString();
        }
        row.put("ov-options.amendmentCharge", amendmentCharge);
    }

    private void addCancellationCharge(List<PriceLineModel> nonChannelBasedPriceLines, Map<String, String> row) {
        String cancellationCharge = "";
        final PriceLineModel priceLineModel = findByPpsrId(nonChannelBasedPriceLines, CANCELLATION_CHARGE.ppsrId());
        if (priceLineModel != null) {
            cancellationCharge = priceLineModel.getOneTimeCPValue().toString();
        }
        row.put("ov-options.cancellationCharge", cancellationCharge);
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

    private PriceLineModel findByPpsrId(List<PriceLineModel> priceLines, Long ppsrId) {
        PriceLineModel result = null;
        for (PriceLineModel priceLine : priceLines) {
            if (ppsrId.equals(priceLine.getPpsrId())) {
                result = priceLine;
                break;
            }
        }
        return result;
    }

    private static class OneVoiceNonChannelBasedPriceLineFilter implements Filters.Filter<PriceLineModel> {
        @Override
        public boolean apply(PriceLineModel model) {
            for (OneVoicePriceTariff oneVoicePpsrId : NON_CHANNEL_BASED_PPSR_IDS) {
                if (oneVoicePpsrId.ppsrId().equals(model.getPpsrId())) {
                    return true;
                }
            }
            return false;
        }
    }

}
