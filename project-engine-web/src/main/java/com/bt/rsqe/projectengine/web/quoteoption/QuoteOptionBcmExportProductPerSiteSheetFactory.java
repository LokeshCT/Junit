package com.bt.rsqe.projectengine.web.quoteoption;

import com.bt.rsqe.customerrecord.SiteDTO;
import com.bt.rsqe.projectengine.web.model.LineItemModel;
import com.bt.rsqe.projectengine.web.quoteoption.bcmsheet.ProductPerSiteStaticColumn;
import com.bt.rsqe.projectengine.web.quoteoption.bcmsheet.RootProductsCategory;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.*;
import static com.google.common.collect.Maps.newLinkedHashMap;

public class QuoteOptionBcmExportProductPerSiteSheetFactory {
    private Comparator<LineItemModel> LINE_ITEM_MODEL_COMPARATOR = new LineItemModelComparator();

    private List<LineItemModel> filterLineItemsBasedOnSiteIDAndProductCategoryCode(List<LineItemModel> lineItemModels) {
        List<LineItemModel> filteredLineItemModels = newLinkedList();
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

    public Map<String, String> getDynamicColumnsInPPS() {
        Map<String,String> dynamicColumns= newLinkedHashMap();
        for(RootProductsCategory productCategory : RootProductsCategory.values()){
            dynamicColumns.put(productCategory.columnName,"pps."+productCategory.columnName);
        }
        return  dynamicColumns;
    }

    public List<Map<String, String>> createProductPerSiteInfoRows(List<LineItemModel> lineItemModels){
        List<Map<String, String>> rows = newLinkedList();
        List<LineItemModel> filteredLineItemModels = filterLineItemsBasedOnSiteIDAndProductCategoryCode(lineItemModels);
        Map<String, String> productCategoryMap = getDynamicColumnsInPPS();
        for (LineItemModel lineItem : filteredLineItemModels) {
            Map<String, String> row = newLinkedHashMap();
            SiteDTO site = lineItem.getSite();
            if(productCategoryMap.get(lineItem.getProductCategoryName()) != null
                && site.siteType.equalsIgnoreCase("BRANCH")){
                row.put(ProductPerSiteStaticColumn.COUNTRY.retrieveValueFrom, site.country);
                row.put(ProductPerSiteStaticColumn.CITY.retrieveValueFrom, site.city);
                row.put(ProductPerSiteStaticColumn.SITE_ID.retrieveValueFrom, site.bfgSiteID);
                row.put(ProductPerSiteStaticColumn.SITE_NAME.retrieveValueFrom, site.name);
                row.put(productCategoryMap.get(lineItem.getProductCategoryName()), "Y");
                rows.add(row);
            }
        }
        return rows;
    }
}
