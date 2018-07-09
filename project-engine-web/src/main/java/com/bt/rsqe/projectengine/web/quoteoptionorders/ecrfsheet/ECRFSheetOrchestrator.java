package com.bt.rsqe.projectengine.web.quoteoptionorders.ecrfsheet;

import com.bt.rsqe.customerinventory.parameter.LineItemId;
import com.bt.rsqe.domain.product.parameters.ProductCategoryCode;
import com.bt.rsqe.projectengine.web.AssetKeyContainer;
import com.bt.rsqe.projectengine.web.ImportResults;
import com.google.common.base.Optional;
import org.apache.poi.ss.usermodel.Workbook;

import java.util.Collections;
import java.util.Set;


public class ECRFSheetOrchestrator {

    private ECRFSheetModelBuilder ecrfSheetModelBuilder;
    private ProductBasedImporter productBasedImporter;
    private LineItemBasedImporter lineItemBasedImporter;

    public ECRFSheetOrchestrator(ECRFSheetModelBuilder ecrfSheetModelBuilder, ProductBasedImporter productBasedImporter, LineItemBasedImporter lineItemBasedImporter) {
        this.ecrfSheetModelBuilder = ecrfSheetModelBuilder;
        this.productBasedImporter = productBasedImporter;
        this.lineItemBasedImporter = lineItemBasedImporter;
    }

    public Set<LineItemId> importUsingLineItem(String customerId, String contractId, String projectId, String quoteOptionId, String importedLineItemId,
                                               Workbook workBook, ImportResults importResults, String productCode, boolean isMigration, ProductCategoryCode productCategoryCode) throws ECRFImportException {
        try {
            ECRFWorkBook ecrfWorkBook = ecrfSheetModelBuilder.build(productCode, workBook);
            return lineItemBasedImporter.importFromSheet(customerId, contractId, null, projectId, quoteOptionId, ecrfWorkBook, importResults,
                                                  new AssetKeyContainer(), productCode, Optional.of(new LineItemId(importedLineItemId)), isMigration, productCategoryCode);
        } catch (Exception e) {
            importResults.addError(productCode, e.getMessage());
        }
        return Collections.emptySet();
    }

    public Set<LineItemId> importUsingProduct(String customerId, String contractId, String contractTerm, String projectId, String quoteOptionId, Workbook workBook,
                                              String productCode, ImportResults importResults, boolean isMigration, ProductCategoryCode productCategoryCode) throws ECRFImportException {

        try {
            ECRFWorkBook ecrfWorkBook = ecrfSheetModelBuilder.build(productCode, workBook);
            return productBasedImporter.importFromSheet(customerId, contractId, contractTerm, projectId, quoteOptionId, ecrfWorkBook, importResults,
                                                        new AssetKeyContainer(), productCode, Optional.<LineItemId>absent(), isMigration, productCategoryCode);
        } catch (Exception e) {
            importResults.addError(productCode, e.getMessage());
        }
        return Collections.emptySet();
    }
}
