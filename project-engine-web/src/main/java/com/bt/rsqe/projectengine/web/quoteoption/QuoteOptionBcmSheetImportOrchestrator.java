package com.bt.rsqe.projectengine.web.quoteoption;

import com.bt.rsqe.pmr.client.PmrClient;
import com.bt.rsqe.projectengine.web.facades.LineItemFacade;
import com.bt.rsqe.projectengine.web.model.bcmspreadsheet.InvalidExportDataException;
import com.bt.rsqe.projectengine.web.quoteoption.bcmsheet.BCMConstants;
import com.bt.rsqe.projectengine.web.quoteoption.bcmsheet.BCMDiscountUpdater;
import com.bt.rsqe.projectengine.web.quoteoption.bcmsheet.BCMExportOrchestrator;
import com.bt.rsqe.projectengine.web.quoteoption.bcmsheet.ImportDiscounts;
import com.bt.rsqe.projectengine.web.quoteoption.bcmsheet.PriceLineIndex;
import com.bt.rsqe.util.excel.ExcelSheetComparator;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.bt.rsqe.utils.AssertObject.*;
import static com.google.common.collect.Lists.*;

public class QuoteOptionBcmSheetImportOrchestrator {
    private final LineItemFacade lineItemFacade;
    private PmrClient pmrClient;
    private QuoteOptionBcmSheetExportOrchestrator quoteOptionBcmSheetExportOrchestrator;
    private BCMExportOrchestrator bcmExportOrchestrator;
    private BCMDiscountUpdater updater;

    public QuoteOptionBcmSheetImportOrchestrator(LineItemFacade lineItemFacade,
                                                 PmrClient pmrClient,
                                                 QuoteOptionBcmSheetExportOrchestrator quoteOptionBcmSheetExportOrchestrator,
                                                 BCMExportOrchestrator bcmExportOrchestrator,
                                                 BCMDiscountUpdater updater) {
        this.lineItemFacade = lineItemFacade;
        this.pmrClient = pmrClient;
        this.quoteOptionBcmSheetExportOrchestrator = quoteOptionBcmSheetExportOrchestrator;
        this.bcmExportOrchestrator = bcmExportOrchestrator;
        this.updater = updater;
    }

    public void importBCMSheetDetails(String customerId, String contractId, String projectId, String quoteOptionId, HSSFWorkbook bcmWorkBook) {
        HSSFWorkbook referenceBCMWorkbook = getCorrectBCMWorkbookVersion(customerId, contractId, projectId, quoteOptionId, bcmWorkBook);
        validateBCMWorkBook(referenceBCMWorkbook, bcmWorkBook);
        updateBCMSheetDetails(referenceBCMWorkbook, bcmWorkBook);
    }

    private HSSFWorkbook getCorrectBCMWorkbookVersion(String customerId, String contractId, String projectId, String quoteOptionId, HSSFWorkbook importedBCMWorkbook) {
        String sheetVersion = "";
        String offerNameInSheet = "";
        for(int i=0; i<importedBCMWorkbook.getNumberOfSheets(); i++)
        {
            HSSFSheet sheet = importedBCMWorkbook.getSheetAt(i);
            if (BCMConstants.BCM_BID_INFO_SHEET.equalsIgnoreCase(sheet.getSheetName())) {

                HSSFRow row = sheet.getRow(1);
                int importCellCount = row.getLastCellNum();
                int cellFromRight = importCellCount == 13 ? importCellCount - 3 : importCellCount - 2;
                try{
                    sheetVersion = row.getCell(cellFromRight).getStringCellValue();
                    if(importCellCount==13){//Hacky Fix, need to see why Last Column is picked as Null
                        offerNameInSheet = row.getCell(row.getLastCellNum() - 2).getStringCellValue();
                    }
                }catch (IllegalStateException e){
                    sheetVersion = String.valueOf(row.getCell(cellFromRight).getNumericCellValue());
                }
                break;
            }
        }

        if(!sheetVersion.equals("1.0") && !sheetVersion.equals("1")){
            return quoteOptionBcmSheetExportOrchestrator.renderBcmExportSheet(customerId, contractId, projectId, quoteOptionId, offerNameInSheet);
        }
        return  bcmExportOrchestrator.renderBCMExportSheet(customerId, contractId, projectId, quoteOptionId, offerNameInSheet);
    }

    private void validateBCMWorkBook(HSSFWorkbook referenceBCMWorkbook, HSSFWorkbook bcmWorkBook) {
        for (int i = 0; i < referenceBCMWorkbook.getNumberOfSheets(); i++) {
            HSSFSheet referenceSheet = referenceBCMWorkbook.getSheetAt(i);
            HSSFSheet importSheet = bcmWorkBook.getSheet(referenceSheet.getSheetName());
            List<Integer> skippedColumns = getSkipColumnsForComparison(referenceSheet);
            if (BCMConstants.BCM_BID_INFO_SHEET.equalsIgnoreCase(referenceSheet.getSheetName())) {
                validateForBCMSheetVersion(referenceSheet, importSheet);
            }
            if(!BCMConstants.BCM_PRODUCT_PER_SITE_SHEET.equalsIgnoreCase(referenceSheet.getSheetName())) {
                compareSheet(referenceSheet, importSheet, skippedColumns);
            }
            validateDiscountValues(importSheet, skippedColumns);
        }
    }

    private void validateForBCMSheetVersion(HSSFSheet referenceSheet, HSSFSheet importSheet) throws InvalidExportDataException {
        HSSFRow refRow = referenceSheet.getRow(1);
        HSSFRow importRow = importSheet.getRow(1);
        String importSheetVersion = "";
        String refSheetVersion = "";
        int importCellCount = importRow.getLastCellNum();
        int cellFromRight = importCellCount == 12 ? importCellCount - 2 : importCellCount - 1;
        try{
            importSheetVersion = importRow.getCell(cellFromRight).getStringCellValue();
        }catch (IllegalStateException e){
            importSheetVersion = String.valueOf(importRow.getCell(cellFromRight).getNumericCellValue());
        }

        try{
            refSheetVersion = refRow.getCell(refRow.getLastCellNum() - 2).getStringCellValue();
        }catch (IllegalStateException e){
            refSheetVersion = String.valueOf(refRow.getCell(refRow.getLastCellNum() - 2).getNumericCellValue());
        }

        if (refSheetVersion.equals("") || importSheetVersion.equals("")
                || !refSheetVersion.equals(importSheetVersion)) {
            throw new InvalidExportDataException("Import unsuccessful - BCM Sheet is an old version which is no longer supported.");
        }

    }

    private void validateDiscountValues(HSSFSheet importSheet, List<Integer> skippedColumns) throws InvalidExportDataException {
       if(isNotNull(importSheet)) {
        for (Integer columnIndex : skippedColumns) {
                for (int i = 1; i <= importSheet.getLastRowNum(); i++) {
                    if (isNotNull(importSheet.getRow(i)) && isNotNull(importSheet.getRow(i).getCell(columnIndex))) {
                        Double discountValue = 0.0;
                        String discountStrValue;
                        try{
                            discountValue = importSheet.getRow(i).getCell(columnIndex).getNumericCellValue();
                        }catch (IllegalStateException e){
                            discountStrValue = importSheet.getRow(i).getCell(columnIndex).getStringCellValue();
                            discountValue = StringUtils.EMPTY.equals(discountStrValue.trim()) ? discountValue : Double.valueOf(discountStrValue);
                        }

                        if (discountValue > 1) {
                            throw new InvalidExportDataException("Import unsuccessful - BCM Sheet contains discounts greater than 100%.");
                        }
                    }
                }
            }
        }
    }


    private List<Integer> getSkipColumnsForComparison(HSSFSheet referenceSheet) {
        List<Integer> skippColumns = newArrayList();
        HSSFRow headerRow = referenceSheet.getRow(0);
        for (int i = 0; i < headerRow.getLastCellNum(); i++) {
            if (StringUtils.isNotBlank(headerRow.getCell(i).getStringCellValue()) &&
                headerRow.getCell(i).getStringCellValue().contains("Discount %")) {
                skippColumns.add(i);
            }
        }

        return skippColumns;  //To change body of created methods use File | Settings | File Templates.
    }

    private void compareSheet(HSSFSheet referenceSheet, HSSFSheet importSheet, List<Integer> skipColumns) throws InvalidExportDataException {
        boolean compareHeadersOnly = false;
        boolean compareNullValues = false;

        if(importSheet.getSheetName().equalsIgnoreCase(BCMConstants.BCM_BID_INFO_SHEET)) {
            compareHeadersOnly = false;
        }
        ExcelSheetComparator excelSheetComparator = new ExcelSheetComparator(importSheet, referenceSheet, skipColumns, true, compareHeadersOnly, compareNullValues);
        List<String> errors = excelSheetComparator.compare();
        if (errors.size() > 0) {
            String allErrorMessages = "";
            boolean validError = false;
            for(String error: errors) {
                if(!error.toLowerCase().contains("cell is null at sheet")){
                    allErrorMessages += error + " \n";
                    validError = true;
                }
            }
            if(validError) {
                throw new InvalidExportDataException("Import unsuccessful - BCM Sheet format is different than expected. \n" + allErrorMessages);
            }
        }
    }

    private void updateBCMSheetDetails(HSSFWorkbook referenceBCMWorkbook, HSSFWorkbook bcmWorkBook) {
        for (int i = 0; i < bcmWorkBook.getNumberOfSheets(); i++) {
            HSSFSheet sheet = bcmWorkBook.getSheetAt(i);
            Map<Integer, List<PriceLineIndex>> indices = getPriceLineIndices(sheet);
            List<ImportDiscounts> discounts = getUpdatableDataFromSheet(sheet, indices);
            updater.updateDiscountsFrom(discounts);
        }

    }

    private List<ImportDiscounts> getUpdatableDataFromSheet(HSSFSheet sheet, Map<Integer, List<PriceLineIndex>> indices) {
        List<ImportDiscounts> discounts = newArrayList();
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            List<ImportDiscounts> importDiscounts = getDiscountDataFromRow(sheet.getRow(i), indices);
            discounts.addAll(importDiscounts);
        }
        return discounts;
    }

    private List<ImportDiscounts> getDiscountDataFromRow(HSSFRow row, Map<Integer, List<PriceLineIndex>> indices) {
        List<ImportDiscounts> importDiscounts = newArrayList();
        for (Integer productInstanceIndex : indices.keySet()) {
            List<PriceLineIndex> priceLineIndices = indices.get(productInstanceIndex);
            if (!priceLineIndices.isEmpty() && isNotNull(row) && isNotNull(row.getCell(productInstanceIndex))) {
                ImportDiscounts discounts = new ImportDiscounts();
                discounts.setProductInstanceId(row.getCell(productInstanceIndex).getStringCellValue());
                discounts.setProductInstanceVersion(Double.valueOf(row.getCell(productInstanceIndex + BCMConstants.VERSION_POSITION_AFTER_PRODUCT_ID).getNumericCellValue()).longValue());

                for (PriceLineIndex priceLineIndex : priceLineIndices) {
                    if (isNotNull(row.getCell(priceLineIndex.getPriceLineIndex())) && StringUtils.isNotBlank(row.getCell(priceLineIndex.getPriceLineIndex()).getStringCellValue())) {

                        Double discountValue = 0.0;
                        String discountStrValue;
                        try{
                            discountValue = row.getCell(priceLineIndex.getDiscountIndex()).getNumericCellValue();
                        }catch (IllegalStateException e){
                            discountStrValue = row.getCell(priceLineIndex.getDiscountIndex()).getStringCellValue();
                            discountValue = StringUtils.EMPTY.equals(discountStrValue.trim()) ? discountValue : Double.valueOf(discountStrValue);
                        }

                        discounts.putPriceLineAndDiscount(row.getCell(priceLineIndex.getPriceLineIndex()).getStringCellValue(),discountValue);
                    }
                }
                if(!discounts.getPriceLineToDiscountMap().keySet().isEmpty()){
                    importDiscounts.add(discounts);
                }
            }
        }
        return importDiscounts;
    }

    private Map<Integer, List<PriceLineIndex>> getPriceLineIndices(HSSFSheet sheet) {
        Map<Integer, List<PriceLineIndex>> indices = new HashMap<Integer, List<PriceLineIndex>>();
        HSSFRow header = sheet.getRow(0);

        for (int i = 0; i < header.getLastCellNum(); i++) {
            if (StringUtils.isNotBlank(header.getCell(i).getStringCellValue()) &&
                header.getCell(i).getStringCellValue().contains("Product Instance")) {
                indices.put(i, getPriceLinesIndicesForProductInstance(header, i, sheet.getSheetName().equals(BCMConstants.BCM_SPECIAL_BID_INFO_SHEET)));
            }
        }
        return indices;
    }

    private List<PriceLineIndex> getPriceLinesIndicesForProductInstance(HSSFRow header, int i, boolean nonStdSheet) {
        Map<String, PriceLineIndex> columnPositionsMap = new HashMap<String, PriceLineIndex>();

        for (int j = i+1; j < header.getLastCellNum(); j++) {
            String cellValue =  header.getCell(j).getStringCellValue();

            if (StringUtils.isNotBlank(header.getCell(j).getStringCellValue()) &&
                cellValue.contains("Product Instance")) {
                break;
            }

            if (cellValue.toUpperCase().contains("PRICE LINE")){
                String key = cellValue.substring(0, cellValue.toUpperCase().lastIndexOf("PRICE LINE")).trim();
                if(columnPositionsMap.containsKey(key)){
                    columnPositionsMap.get(key).setPriceLineIndex(j);
                }
                else {
                    columnPositionsMap.put(key, new PriceLineIndex(j, -1));
                }
            }

            if (cellValue.toUpperCase().contains("DISCOUNT %")){
                String key = cellValue.substring(0, cellValue.toUpperCase().lastIndexOf("DISCOUNT %")).trim();
                if(nonStdSheet) {
                  key = cellValue.substring(13, cellValue.length());
                }

                if(columnPositionsMap.containsKey(key)){
                    columnPositionsMap.get(key).setDiscountIndex(j);
                }
                else {
                    columnPositionsMap.put(key, new PriceLineIndex(-1, j));
                }
            }
        }
        return new ArrayList<PriceLineIndex>(columnPositionsMap.values());
    }
}
