package com.bt.rsqe.projectengine.web.quoteoption;

import com.bt.rsqe.domain.project.PricingStatus;
import com.bt.rsqe.projectengine.web.model.LineItemModel;
import com.bt.rsqe.projectengine.web.quoteoption.util.ProductCategoryFilter;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class QuoteOptionBcmExportProductLevelInfoSheetFactory {
    private String PRODUCT_CATEGORY_COLUMN_NAME = "product-level-info.product-category";
    private String MONTHLY_REVENUE_COMMITMENT_COLUMN_NAME = "product-level-info.monthly-revenue-commitment";

    /**
     * TODO : Populate monthly revenue commitment.
     * This will be a new story to identify from where the data needs to be fetched, maybe expedio..?
     */
    public List<Map<String, String>> createProductLevelInfoSheetRows(List<LineItemModel> lineItemModels) {
        List<Map<String, String>> rows = new LinkedList();
        List<LineItemModel> filteredLineItemModels = new ProductCategoryFilter(lineItemModels).filterLineItemsBasedOnProductCategoryCode();

        for (LineItemModel lineItem : filteredLineItemModels) {
            Map<String, String> row = new HashMap<String, String>();
            if(!lineItem.getPricingStatusOfTree().equals(PricingStatus.NOT_APPLICABLE)){
                row.put(PRODUCT_CATEGORY_COLUMN_NAME, lineItem.getProductCategoryName());
                row.put(MONTHLY_REVENUE_COMMITMENT_COLUMN_NAME, lineItem.getMonthlyRevenueCommitment());
                rows.add(row);
            }
        }
        return rows;
    }

 }
