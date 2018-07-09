package com.bt.rsqe.projectengine.web.quoteoptionorders.ecrfsheet;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.*;
import static java.lang.String.*;

public class ECRFWorkBook {

    private List<ECRFSheet> workSheets = newArrayList();
    private List<ECRFSheet> nonProductSheets = newArrayList();
    private Map<String, String> controlSheet;
    public boolean hasRelatedToSheet;
    private int controlSheetIndex;

    public void setWorksheets(List<ECRFSheet> ecrfSheetModels) {
        this.workSheets = ecrfSheetModels;
    }

    public void addSheet(ECRFSheet ecrfSheetModel) {
        this.workSheets.add(ecrfSheetModel);
    }


    public List<ECRFSheet> getSheets() {
        return this.workSheets;
    }

    public ECRFSheet getSheetByProductCode(final String sCode) {
        Optional<ECRFSheet> sheetOptional = Iterables.tryFind(this.workSheets, new Predicate<ECRFSheet>() {
            @Override
            public boolean apply(ECRFSheet input) {
                return input.getProductCode().equals(sCode);
            }
        });
        if (sheetOptional.isPresent()) {
            return sheetOptional.get();
        } else {
            throw new ECRFImportException(format(ECRFImportException.workSheetNotFound, sCode));
        }
    }

    public ECRFSheet getSheetBySheetIndex(final int index) {
        Optional<ECRFSheet> sheetOptional = Iterables.tryFind(this.workSheets, new Predicate<ECRFSheet>() {
            @Override
            public boolean apply(ECRFSheet input) {
                return input.getSheetIndex() == index;
            }
        });
        if (sheetOptional.isPresent()) {
            return sheetOptional.get();
        } else {
            throw new ECRFImportException(format(ECRFImportException.workSheetNotFoundForIndex, index));
        }
    }

    public Map<String, String> getControlSheet() {
        return controlSheet;
    }

    public void setControlSheet(Map<String, String> sheetNameToSCode) {
        this.controlSheet = sheetNameToSCode;
    }

    public void parentIdExistsInWorkBook(String parentId, String sheetName) {
        boolean found = false;
        for(ECRFSheet sheet : this.workSheets) {
            for(ECRFSheetModelRow row : sheet.getRows()) {
                if (row.getRowId().equals(parentId)) {
                    found = true;
                    break;
                }
            }
        }
        if (!found) {
            throw new ECRFImportException(String.format(ECRFImportException.parentIdNotFound, parentId, sheetName));
        }

    }


    public boolean hasRelatedToSheet() {
        return hasRelatedToSheet;
    }

    public void setHasRelatedToSheet(boolean hasRelatedToSheet) {
        this.hasRelatedToSheet = hasRelatedToSheet;
    }

    public int getControlSheetIndex() {
        return controlSheetIndex;
    }

    public void setControlSheetIndex(int controlSheetIndex) {
        this.controlSheetIndex = controlSheetIndex;
    }

    public void addNonProductSheet(ECRFSheet ecrfSheetModel) {
        this.nonProductSheets.add(ecrfSheetModel);
    }

    public List<ECRFSheet> getNonProductSheets() {
        return this.nonProductSheets;
    }

    public void setNonProductSheet(List<ECRFSheet> nonProductSheetModels) {
        this.nonProductSheets = nonProductSheetModels;
    }
}
