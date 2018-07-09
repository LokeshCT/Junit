package com.bt.rsqe.projectengine.web.quoteoption.bulktemplatesheet;


import com.bt.rsqe.pmr.client.PmrClient;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class BulkTemplateDetailSheetModelBuilder {

    private PmrClient pmrClient;

    public BulkTemplateDetailSheetModelBuilder(PmrClient pmrClient) {
        this.pmrClient = pmrClient;
    }

    public Map<String, BulkTemplateDetailSheetModel> build(List<BulkTemplateProductModel> productModels) {
        Map<String, BulkTemplateDetailSheetModel> detailSheetModelMap = new LinkedHashMap<String, BulkTemplateDetailSheetModel>();

        for (BulkTemplateProductModel productModel : productModels) {
            detailSheetModelMap.put(productModel.getProductId(),
                                               new BulkTemplateDetailSheetModel(pmrClient, productModel));
        }

        return detailSheetModelMap;
    }

}
