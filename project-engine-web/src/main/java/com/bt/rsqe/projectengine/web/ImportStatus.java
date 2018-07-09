package com.bt.rsqe.projectengine.web;

public enum ImportStatus {
    Success("Import already complete for the selected product.", false),
    Failed("Initial import failed for the selected product, hence try to import with new quote.", true),
    Initiated("Import is in progress for this selected product.", true);
    private String importErrorMessage;
    private boolean onlyForBulkUpload;

    ImportStatus(String importErrorMessage, boolean onlyForBulkUpload) {
        this.importErrorMessage = importErrorMessage;
        this.onlyForBulkUpload = onlyForBulkUpload;
    }

    public String getImportErrorMessage() {
        return importErrorMessage;
    }

    public static ImportStatus get(String status) {
        for (ImportStatus importStatus : values()) {
            if(importStatus.name().equals(status)){
                return importStatus;
            }
        }
        return null;
    }

    public boolean isOnlyForBulkUpload() {
        return onlyForBulkUpload;
    }
}
