package com.bt.rsqe.projectengine.web.productconfigurator.model;

import java.util.List;

public class BulkConfigDataModel {

    private BulkConfigSummaryModel summaryModel;
    private List<BulkConfigDetailModel> detailModel;

    public BulkConfigDataModel(BulkConfigSummaryModel summaryModel, List<BulkConfigDetailModel> detailModel) {
        this.summaryModel = summaryModel;
        this.detailModel = detailModel;
    }


    public BulkConfigSummaryModel getSummaryModel() {
        return summaryModel;
    }

    public List<BulkConfigDetailModel> getDetailModel() {
        return detailModel;
    }
}
