package com.bt.rsqe.projectengine.web.quoteoption.bulktemplatesheet;

import java.util.LinkedList;
import java.util.List;

public class BulkTemplateControlSheetModel {

    private List<BulkTemplateControlSheetRow> rows;
    List<BulkTemplateProductModel> productModels;

    public BulkTemplateControlSheetModel(List<BulkTemplateProductModel> productModels) {
        this.productModels = productModels;
        this.rows = createRows(productModels);
    }

    public List<BulkTemplateControlSheetRow> getRows() {
        return rows;
    }

    private List<BulkTemplateControlSheetRow> createRows(List<BulkTemplateProductModel> productModels){
        final List<BulkTemplateControlSheetRow> bulkTemplateControlSheetRows = new LinkedList<BulkTemplateControlSheetRow>();
        for (BulkTemplateProductModel productModel: productModels) {
            bulkTemplateControlSheetRows.add(new BulkTemplateControlSheetRow(productModel.getProductId(),productModel.getProductName()));
        }
        return bulkTemplateControlSheetRows;
    }

    public static class BulkTemplateControlSheetRow {
        private final String productName;
        private final String sCode;

        public BulkTemplateControlSheetRow(String sCode, String productName) {
            this.sCode = sCode;
            this.productName = productName;
        }

        public String getProductName() {
            return productName;
        }

        public String getsCode() {
            return sCode;
        }
    }
}
