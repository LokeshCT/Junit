package com.bt.rsqe.projectengine.web.quoteoptionpricing;

import com.bt.rsqe.logging.Log;
import com.bt.rsqe.logging.LogFactory;
import com.bt.rsqe.logging.LogLevel;
import com.bt.rsqe.projectengine.web.quoteoptionpricing.pricingsheet.DirectUserDetailedPricingSheetServiceLevelSectionCellMerger;
import com.bt.rsqe.projectengine.web.quoteoptionpricing.pricingsheet.DirectUserDetailedPricingSheetSiteLevelSectionCellMerger;
import com.bt.rsqe.projectengine.web.quoteoptionpricing.pricingsheet.PricingSheetCellMerger;
import com.bt.rsqe.projectengine.web.quoteoptionpricing.pricingsheet.PricingSheetStrategy;
import com.bt.rsqe.projectengine.web.quoteoptionpricing.pricingsheet.model.PricingSheetDataModel;
import com.bt.rsqe.projectengine.web.quoteoptionpricing.pricingsheet.model.PricingSheetDataModelFactory;
import com.google.common.base.Optional;
import net.sf.jxls.transformer.XLSTransformer;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.*;

public class PricingSheetOrchestrator {
    private static final PricingSheetLog LOG = LogFactory.createDefaultLogger(PricingSheetLog.class);
    List<PricingSheetCellMerger> cellMergers;
    private PricingSheetDataModelFactory pricingSheetDataModelFactory;

     public PricingSheetOrchestrator(PricingSheetDataModelFactory pricingSheetDataModelFactory) {
        this.pricingSheetDataModelFactory = pricingSheetDataModelFactory;
        cellMergers = newArrayList(
            new DirectUserDetailedPricingSheetSiteLevelSectionCellMerger(),
            new DirectUserDetailedPricingSheetServiceLevelSectionCellMerger()
        );
    }

    public Workbook renderPricingSheet(String customerId, String projectId, String quoteOptionId, Optional<String> offerId) {
        PricingSheetDataModel pricingSheetDataModel = buildPricingSheetDataModel(customerId, projectId, quoteOptionId, offerId);
        return exportToExcel(pricingSheetDataModel.map(), PricingSheetStrategy.instance().template());
    }

    private PricingSheetDataModel buildPricingSheetDataModel(String customerId, String projectId, String quoteOptionId, Optional<String> offerId) {
       return pricingSheetDataModelFactory.create(customerId, projectId, quoteOptionId, offerId);
    }

    private Workbook exportToExcel(Map params, String template) {
        XLSTransformer transformer = new XLSTransformer();
        transformer.setJexlInnerCollectionsAccess(true);

        try {
            Workbook workbook = transformer.transformXLS(getClass().getClassLoader().getResource(template).openStream(), params);
            if (!pricingSheetDataModelFactory.isSpecialBidAvailable){
                workbook.removeSheetAt(PricingSheetKeys.SPECIAL_BID_PRICING_SHEET_NAME);//removing Special Bid(TPE) Caveats sheet from price book  if special bid is not configured
            }
            if (!pricingSheetDataModelFactory.isAccessAvailable){
                for (int sheet = 0; sheet < workbook.getNumberOfSheets(); sheet++) {
                    if (workbook.getSheetName(sheet).equalsIgnoreCase(PricingSheetKeys.ACCESS_CAVEATS_PRICING_SHEET_NAME)) {
                        workbook.removeSheetAt(sheet); //removing Access Caveats sheet from price book if ICG product is not configured
                    }
                }
            }
            return doCellMerge(workbook);
        } catch (InvalidFormatException e) {
            LOG.invalidPricingSheetTemplate(e);
            throw new RuntimeException(String.format("unable to locate template '%s'. Exception : ", template, e.getMessage()));
        } catch (IOException e) {
            LOG.invalidPricingSheetTemplate(e);
            throw new RuntimeException(String.format("unable to locate template '%s'. Exception : ", template, e.getMessage()));
        } catch (Exception e) {
            LOG.invalidPricingSheetTemplate(e);
            throw new RuntimeException(String.format("unable to process template '%s'. Exception : ", template, e.getMessage()));
        }
    }

    private Workbook doCellMerge(Workbook workbook) {
        Sheet detailSheet = workbook.getSheet(PricingSheetKeys.CA_DIRECT_USER_DETAILED_PRICING_SHEET_NAME);
        for (PricingSheetCellMerger cellMerger : cellMergers) {
            cellMerger.mergeCellsForSheet(detailSheet);
        }
        Sheet summarySheet = workbook.getSheet(PricingSheetKeys.CA_DIRECT_USER_SUMMARY_PRICING_SHEET_NAME);
              for (PricingSheetCellMerger cellMerger : cellMergers) {
                  cellMerger.mergeCellsForSheet(summarySheet);
              }

        return workbook;
    }

    private interface PricingSheetLog {
        @Log(level = LogLevel.WARN, format = "%s")
        void invalidPricingSheetTemplate(Throwable error);
    }
}

