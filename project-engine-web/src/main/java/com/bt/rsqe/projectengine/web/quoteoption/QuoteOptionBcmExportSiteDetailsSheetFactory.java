package com.bt.rsqe.projectengine.web.quoteoption;

import com.bt.rsqe.customerrecord.SiteDTO;
import com.bt.rsqe.projectengine.web.model.LineItemModel;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.*;

public class QuoteOptionBcmExportSiteDetailsSheetFactory {
    private String COUNTRY_COLUMN_NAME = "site-details.originating-country";
    private String CITY_COLUMN_NAME = "site-details.city";
    private String SITE_ID_COLUMN_NAME = "site-details.site-id";
    private String SITE_NAME_COLUMN_NAME = "site-details.site-name";
    private String PRODUCT_CATEGORY_COLUMN_NAME = "site-details.product-category";
    private Comparator<LineItemModel> LINE_ITEM_MODEL_COMPARATOR = new LineItemModelComparator();

    public List<Map<String, String>> createSiteDetailsRows(List<LineItemModel> lineItemModels) {
        List<Map<String, String>> rows = new LinkedList();
        List<LineItemModel> filteredLineItemModels = filterLineItemsBasedOnSiteIDAndProductCategoryCode(lineItemModels);

        for (LineItemModel lineItem : filteredLineItemModels) {
            Map<String, String> row = new HashMap<String, String>();

            SiteDTO site = lineItem.getSite();
            row.put(COUNTRY_COLUMN_NAME, site.country);
            row.put(CITY_COLUMN_NAME, site.city);
            row.put(SITE_ID_COLUMN_NAME, site.bfgSiteID);
            row.put(SITE_NAME_COLUMN_NAME, site.name);
            row.put(PRODUCT_CATEGORY_COLUMN_NAME, lineItem.getProductCategoryName());

            rows.add(row);
        }
        return rows;
    }


    private List<LineItemModel> filterLineItemsBasedOnSiteIDAndProductCategoryCode(List<LineItemModel> lineItemModels) {
        List<LineItemModel> filteredLineItemModels = newArrayList();
        for(LineItemModel lineItemModel : lineItemModels) {
            boolean lineItemExists = lineItemExistForSiteWithProductCategory(filteredLineItemModels,
                                                                             lineItemModel.getProductCategoryName(),
                                                                             lineItemModel.getSite().bfgSiteID);
            if(!lineItemExists) {
                filteredLineItemModels.add(lineItemModel);
            }
        }
        Collections.sort(filteredLineItemModels, LINE_ITEM_MODEL_COMPARATOR);
        return filteredLineItemModels;
    }

    private boolean lineItemExistForSiteWithProductCategory(List<LineItemModel> filteredLineItemModels,
                                                            final String productCategoryName, final String siteId) {
        Optional<LineItemModel> lineItemModelOptional = Iterables.tryFind(filteredLineItemModels, new Predicate<LineItemModel>() {
            @Override
            public boolean apply(LineItemModel input) {
                return (input.getProductCategoryName().equals(productCategoryName) && input.getSite() != null && input.getSite().bfgSiteID != null && input.getSite().bfgSiteID.equals(siteId));
            }
        });

        return lineItemModelOptional.isPresent();
    }

    private static class LineItemModelComparator implements Comparator<LineItemModel> {
        @Override
        public int compare(LineItemModel lineItemModelOne, LineItemModel lineItemModelTwo) {
            return lineItemModelOne.getSite().name.compareTo(lineItemModelTwo.getSite().name);
        }
    }

}
