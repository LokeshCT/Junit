package com.bt.rsqe.projectengine.web;


import com.bt.rsqe.projectengine.ImportProductErrorLogDTO;
import com.bt.rsqe.projectengine.ImportProductStatusLogDTO;
import com.bt.rsqe.projectengine.QuoteOptionResource;
import com.bt.rsqe.security.UserDTO;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

public class ImportStatusManager {

    private QuoteOptionResource quoteOptionResource;

    public ImportStatusManager(QuoteOptionResource quoteOptionResource) {
        this.quoteOptionResource = quoteOptionResource;
    }

    public void markImportStatus(String quoteOptionId, String productCode, String lineItemId, String createdBy, String importWorkbookName, ImportStatus importStatus) {
        quoteOptionResource.importProductLogItemResource(quoteOptionId).put(constructLog(quoteOptionId, productCode, importStatus, createdBy, importWorkbookName, lineItemId));
    }

    private ImportProductStatusLogDTO constructLog(String quoteOptionId, String productCode, ImportStatus importStatus, String createdBy, String importWorkbookName, String lineItemId) {
        return new ImportProductStatusLogDTO(quoteOptionId, lineItemId, importStatus.name(), productCode, createdBy, importWorkbookName);
    }

    public void storeImportErrorLog(QuoteOptionResource quoteOptionResource, String quoteOptionId, ImportResults results, String createdBy, String importedProductCode) {
        flushImportErrorLogsForQuote(quoteOptionResource, quoteOptionId, importedProductCode);
        List<ImportProductErrorLogDTO> importProductErrorLogs = constructRootProductLog(quoteOptionId, results.importErrors(), createdBy, importedProductCode);
        quoteOptionResource.importErrorLogResource(quoteOptionId).put(importProductErrorLogs);
    }

    private void flushImportErrorLogsForQuote(QuoteOptionResource quoteOptionResource, String quoteOptionId, String importedProductCode) {
        quoteOptionResource.importErrorLogResource(quoteOptionId).delete(importedProductCode);
    }

    private List<ImportProductErrorLogDTO> constructRootProductLog(String quoteOptionId, List<ImportLineItemError> errors, String createdBy, String importedProductCode) {
        List<ImportProductErrorLogDTO> productErrorLogDTOs = newArrayList();
        for (ImportLineItemError importLineItemError : errors) {
            productErrorLogDTOs.add(new ImportProductErrorLogDTO(quoteOptionId, ImportStatus.Failed.name(), importLineItemError.getErrorMessage(), createdBy, importedProductCode));
        }
        return productErrorLogDTOs;
    }

    public void updateImportStatus(String quoteOptionId, ImportStatus importStatus) {
        ImportProductStatusLogDTO importStatusLogDTO = quoteOptionResource.getProductImportStatusForQuote(quoteOptionId);
        importStatusLogDTO.status = importStatus.name();
        quoteOptionResource.importProductLogItemResource(quoteOptionId).put(importStatusLogDTO);
    }

    public void sendImportStatusMail(String quoteOptionId, String productCode, UserDTO userDTO) {
        quoteOptionResource.importProductLogItemResource(quoteOptionId).sendEmail(productCode, userDTO);
    }
}
