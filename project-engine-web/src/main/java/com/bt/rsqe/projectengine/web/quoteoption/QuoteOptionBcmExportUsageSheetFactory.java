package com.bt.rsqe.projectengine.web.quoteoption;

import com.bt.rsqe.customerrecord.SiteDTO;
import com.bt.rsqe.projectengine.web.model.FutureAssetPricesModel;
import com.bt.rsqe.projectengine.web.model.LineItemModel;
import com.bt.rsqe.projectengine.web.model.ProjectedUsageModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.bt.rsqe.utils.Channels.*;
import static com.google.common.collect.Maps.*;

public class QuoteOptionBcmExportUsageSheetFactory {

    List<Map<String, String>> createUsageRows(List<LineItemModel> oneVoicePriceModels) {
        List<Map<String, String>> usageRows = new ArrayList<Map<String, String>>();
        for (LineItemModel oneVoicePriceModel : oneVoicePriceModels) {
            final FutureAssetPricesModel pricesModel = oneVoicePriceModel.getFutureAssetPricesModel();
            SiteDTO site = oneVoicePriceModel.getSite();
            //String tariffOptions = pricesModel.findFirstPriceLineWithAssetCharacteristicValue(OneVoiceConfiguration.BasicMPLS.BTPriceLine.TARIFF_OPTIONS);
            usageRows.addAll(rowsForTree(pricesModel, site, ""));
        }
        return usageRows;
    }

    private List<Map<String, String>> rowsForTree(FutureAssetPricesModel pricesModel, SiteDTO site, String tariffOptions) {
        List<Map<String, String>> rows = new ArrayList<Map<String, String>>();
        final List<ProjectedUsageModel> projectedUsages = pricesModel.getProjectedUsages();
        rows.addAll(rowsForProduct(site, tariffOptions, projectedUsages));
        for (FutureAssetPricesModel child : pricesModel.getChildren()) {
            rows.addAll(rowsForTree(child, site, tariffOptions));
        }
        return rows;
    }

    private List<Map<String, String>> rowsForProduct(SiteDTO site, String tariffOptions, List<ProjectedUsageModel> projectedUsages) {
        List<Map<String, String>> rows = new ArrayList<Map<String, String>>();
        for (ProjectedUsageModel projectedUsage : projectedUsages) {
            Map<String, String> row = newHashMap();
            row.put("ov-usage.terminating-country", projectedUsage.getDestinationCountry());
            row.put("ov-usage.termination-type", projectedUsage.getTerminationType());
            row.put("ov-usage.outgoing-minutes", projectedUsage.getOutgoingUnits().toString());
            row.put("ov-usage.incoming-offnet-minutes", projectedUsage.getIncomingUnits().toString());
            row.put("ov-usage.site-id", site.bfgSiteID);
            row.put("ov-usage.city", site.city);
            row.put("ov-usage.originating-country", site.country);
            row.put("ov-usage.site-name", site.name);
            //Hard coded to be always MPLS for phase 1
            row.put("ov-usage.originating-site-access-type", "MPLS");

            row.put("ov-usage.tariff-type", tariffOptions);
            if (userCanViewIndirectPrices()) {
                row.put("ov-usage.eup-minute", projectedUsage.getEUPPricePerMin().toString());
                row.put("ov-usage.eup-total", projectedUsage.getEUPAnyChargePerMonth().toString());
                row.put("ov-usage.ptp-minute", projectedUsage.getChargePricePerMin().toString());
                row.put("ov-usage.ptp-total", projectedUsage.getChargeAnyChargePerMonth().toString());
            } else {
                row.put("ov-usage.eup-minute", projectedUsage.getChargePricePerMin().toString());
                row.put("ov-usage.eup-total", projectedUsage.getChargeAnyChargePerMonth().toString());
                row.put("ov-usage.ptp-minute", "");
                row.put("ov-usage.ptp-total", "");
            }
            rows.add(row);
        }
        return rows;
    }

}
