package com.bt.rsqe.projectengine.web.quoteoption.util;

import com.bt.rsqe.projectengine.web.model.LineItemModel;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.google.common.collect.Lists.*;

public class ProductCategoryFilter {
    private Comparator<LineItemModel> PRODUCT_CATEGORY_COMPARATOR = new ProductCategoryComparator();
    private List<LineItemModel> lineItemModels;

    public ProductCategoryFilter(List<LineItemModel> lineItemModels) {
        this.lineItemModels = lineItemModels;
    }

    public List<LineItemModel> filterLineItemsBasedOnProductCategoryCode() {
        List<LineItemModel> filteredLineItemModels = newArrayList();
        for(LineItemModel lineItemModel : lineItemModels) {
            boolean lineItemExists = lineItemExistForProductCategory(filteredLineItemModels, lineItemModel.getProductCategoryName());
            if (!lineItemExists) {
                filteredLineItemModels.add(lineItemModel);
            }
        }
        Collections.sort(filteredLineItemModels, PRODUCT_CATEGORY_COMPARATOR);
        return filteredLineItemModels;
    }

        private boolean lineItemExistForProductCategory(List<LineItemModel> filteredLineItemModels, final String productCategoryName) {
        Optional<LineItemModel> lineItemModelOptional = Iterables.tryFind(filteredLineItemModels, new Predicate<LineItemModel>() {
            @Override
            public boolean apply(LineItemModel input) {
                return (input.getProductCategoryName().equals(productCategoryName));
            }
        });

        return lineItemModelOptional.isPresent();
    }

        private class ProductCategoryComparator implements Comparator<LineItemModel> {
        @Override
        public int compare(LineItemModel lineItemOne, LineItemModel lineItemTwo) {
            return lineItemOne.getProductCategoryName().compareTo(lineItemTwo.getProductCategoryName());
        }
    }
}
