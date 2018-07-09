package com.bt.rsqe.projectengine.web.quoteoption.priceupdater;

import com.bt.rsqe.projectengine.web.facades.LineItemFacade;
import com.bt.rsqe.projectengine.web.model.LineItemModel;
import com.bt.rsqe.projectengine.web.model.bcmspreadsheet.ProductInfoSheet;
import com.bt.rsqe.projectengine.web.model.bcmspreadsheet.ProductInfoSheetRow;

import java.util.List;

public class ProductInfoSheetUpdater implements FutureAssetPriceUpdater {
    private ProductInfoSheet productInfoSheet;
    private LineItemFacade lineItemFacade;

    public ProductInfoSheetUpdater(ProductInfoSheet productInfoSheet, LineItemFacade lineItemFacade) {
        this.productInfoSheet = productInfoSheet;
        this.lineItemFacade = lineItemFacade;
    }

    @Override
    public void update(LineItemModel lineItem) {
        final List<ProductInfoSheetRow> rows = productInfoSheet.rows();
        for (ProductInfoSheetRow row : rows) {
            if (lineItem.getProductCategoryName().equalsIgnoreCase(row.getProductCategoryName())) {
                updateMinimumRevenueCommitment(lineItem, row.getMinimumRevenueCommitment());
            }
        }

    }

    private void updateMinimumRevenueCommitment(LineItemModel lineItem, String minimumRevenueCommitment) {
        lineItemFacade.persistMinimumRevenueCommitment(lineItem.projectId(), lineItem.quoteOptionId(), lineItem.getLineItemId(), minimumRevenueCommitment, lineItem.getTriggerMonths());
    }
}
