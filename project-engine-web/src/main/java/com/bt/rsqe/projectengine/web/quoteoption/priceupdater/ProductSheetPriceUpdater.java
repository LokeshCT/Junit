package com.bt.rsqe.projectengine.web.quoteoption.priceupdater;

import com.bt.rsqe.Percentage;
import com.bt.rsqe.enums.PriceType;
import com.bt.rsqe.projectengine.web.model.FutureAssetPricesModel;
import com.bt.rsqe.projectengine.web.model.LineItemModel;
import com.bt.rsqe.projectengine.web.model.PriceLineModel;
import com.bt.rsqe.projectengine.web.model.bcmspreadsheet.ProductSheet;
import com.bt.rsqe.projectengine.web.model.bcmspreadsheet.ProductSheetRow;
import com.google.common.base.Optional;

import static com.bt.rsqe.utils.AssertObject.isNotNull;

public class ProductSheetPriceUpdater implements FutureAssetPriceUpdater {
    private ProductSheet productSheet;

    public ProductSheetPriceUpdater(ProductSheet productSheet) {
        this.productSheet = productSheet;
    }

    @Override
    public void update(LineItemModel lineItem) {
        FutureAssetPricesModel futureAssetPricesModel = lineItem.getFutureAssetPricesModel();
        recursivelyUpdatePriceModel(futureAssetPricesModel);
    }

    private void recursivelyUpdatePriceModel(FutureAssetPricesModel futureAssetPricesModel) {
        updateDiscountFor(futureAssetPricesModel);
        for(FutureAssetPricesModel child : futureAssetPricesModel.getChildren()){
              recursivelyUpdatePriceModel(child);
        }
    }

    private void updateDiscountFor(FutureAssetPricesModel futureAssetPricesModel) {
        for(PriceLineModel priceLineModel : futureAssetPricesModel.getDeepFlattenedPriceLines()){
            Optional<ProductSheetRow> optionalOneTimeRow = productSheet.getOneTimeRowFor(priceLineModel);
            if(isNotNull(optionalOneTimeRow)  && optionalOneTimeRow.isPresent()){
                ProductSheetRow row = optionalOneTimeRow.get();
                Percentage discount = row.getOneTimeDiscount();
                priceLineModel.setDiscount(discount,PriceType.ONE_TIME);
            }
            Optional<ProductSheetRow> optionalRecurringRow = productSheet.getRecurringRowFor(priceLineModel);
            if(isNotNull(optionalRecurringRow) && optionalRecurringRow.isPresent()){
                ProductSheetRow row = optionalRecurringRow.get();
                Percentage discount = row.getMonthlyDiscount();
                priceLineModel.setDiscount(discount,PriceType.RECURRING);
            }

        }
    }
}
