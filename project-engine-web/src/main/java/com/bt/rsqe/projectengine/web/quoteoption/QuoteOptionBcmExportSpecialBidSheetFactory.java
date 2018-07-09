package com.bt.rsqe.projectengine.web.quoteoption;


import com.bt.rsqe.domain.bom.parameters.OrderType;
import com.bt.rsqe.domain.product.ProductOffering;
import com.bt.rsqe.domain.project.PriceLine;
import com.bt.rsqe.enums.PriceCategory;
import com.bt.rsqe.projectengine.web.quoteoption.bcmsheet.BCMUtil;
import com.bt.rsqe.projectengine.web.quoteoption.bcmsheet.SpecialBidInfoStaticColumn;
import com.bt.rsqe.projectengine.web.quoteoptionpricing.pricingsheet.model.PricingSheetDataModel;
import com.bt.rsqe.projectengine.web.quoteoptionpricing.pricingsheet.model.PricingSheetSpecialBidProduct;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static com.bt.rsqe.utils.AssertObject.*;
import static com.google.common.collect.Lists.*;
import static com.google.common.collect.Maps.*;

public class QuoteOptionBcmExportSpecialBidSheetFactory {

    private Comparator<PricingSheetSpecialBidProduct> LINE_ITEM_MODEL_COMPARATOR = new LineItemModelComparator();

    public List<Map<String, String>> createSpecialBidInfoSheetRow(PricingSheetDataModel dataModel) {
        List<Map<String, String>> rows = newArrayList();
        String currency = dataModel.getQuoteOption().getCurrency();
        final List<PricingSheetSpecialBidProduct> specialBidProducts = filterLineItemsBasedOnSiteID(dataModel.getSpecialBidProducts());

        for(PricingSheetSpecialBidProduct specialBidProduct : specialBidProducts){
            Map<String,String> row = newLinkedHashMap();
            String nonStdConfigurationCategory=specialBidProduct.getInstanceCharacteristic(ProductOffering.CONFIGURATION_CATEGORY_RESERVED_NAME);

            row.put(SpecialBidInfoStaticColumn.SITE_ID.retrieveValueFrom,
                    specialBidProduct.getSite().bfgSiteID);
            row.put(SpecialBidInfoStaticColumn.LINE_ITEM_ID.retrieveValueFrom,
                    specialBidProduct.getQuoteOptionItem().getId());
            row.put(SpecialBidInfoStaticColumn.BILL_DESC.retrieveValueFrom,
                    specialBidProduct.getInstanceCharacteristic(ProductOffering.SPECIAL_BID_BILL_DESCRIPTION));
            row.put(SpecialBidInfoStaticColumn.NON_STD_PRODUCT_TYPE.retrieveValueFrom,
                    specialBidProduct.getInstanceCharacteristic(ProductOffering.CONFIGURATION_TYPE_RESERVED_NAME));
            row.put(SpecialBidInfoStaticColumn.WELL_KNOWN_NON_STD.retrieveValueFrom,
                    specialBidProduct.getInstanceCharacteristic(ProductOffering.SPECIAL_BID_TEMPLATE_RESERVED_NAME));
            row.put(SpecialBidInfoStaticColumn.TPE_REF.retrieveValueFrom,
                    specialBidProduct.getInstanceCharacteristic(ProductOffering.SPECIAL_BID_ID_ATTRIBUTE_NAME));
            row.put(SpecialBidInfoStaticColumn.BRANCH_CENTRAL.retrieveValueFrom,
                    specialBidProduct.isSiteInstallable() ? "Branch":"Central");
            row.put(SpecialBidInfoStaticColumn.EUP_CURRENCY.retrieveValueFrom,currency);
            row.put(SpecialBidInfoStaticColumn.COST_CURRENCY.retrieveValueFrom,currency);
            row.put(SpecialBidInfoStaticColumn.PRODUCT_INSTANCE.retrieveValueFrom,
                    specialBidProduct.getProductInstanceID());
            row.put(SpecialBidInfoStaticColumn.PRODUCT_INSTANCE_VERSION.retrieveValueFrom,
                    String.valueOf(specialBidProduct.getProductInstance().getProductInstanceVersion()));

            List<PriceLine> priceLineList = specialBidProduct.getProductInstance().getPriceLines();
            for(PriceLine priceLine : priceLineList){
                String price = BCMUtil.getPriceInStr(priceLine.getChargePrice().getPrice());
                String discountPercentage = String.valueOf(BCMUtil.changeDiscountToDecimalAndRound(priceLine.getChargePrice().getDiscountPercentage().toString()));
                if(isNull(nonStdConfigurationCategory) || nonStdConfigurationCategory.isEmpty()) {
                    nonStdConfigurationCategory=priceLine.getPriceLineName();
                }
                String priceType = priceLine.getPriceType().name();
                String tariffType = priceLine.getTariffType();

                if(priceType.equalsIgnoreCase("ONE_TIME")){
                    if(tariffType.equalsIgnoreCase(PriceCategory.END_USER_PRICE.getLabel())) {
                        row.put(SpecialBidInfoStaticColumn.ONE_TIME_PRICE_LINE.retrieveValueFrom, priceLine.getId());
                        row.put(SpecialBidInfoStaticColumn.EUP_ONE_TIME.retrieveValueFrom,price);
                        row.put(SpecialBidInfoStaticColumn.DISCOUNT_ONE_TIME.retrieveValueFrom,discountPercentage);
                    }
                    if(tariffType.equalsIgnoreCase(PriceCategory.COST.getLabel())) {
                        row.put(SpecialBidInfoStaticColumn.COST_ONE_TIME.retrieveValueFrom,price);
                    }
                    if(tariffType.equalsIgnoreCase(PriceCategory.PRICE_TO_PARTNER.getLabel())){
                        row.put(SpecialBidInfoStaticColumn.PTP_ONE_TIME.retrieveValueFrom,price);
                    }
                    if(specialBidProduct.getAction().equalsIgnoreCase(OrderType.CEASE.getValue())){
                        if(tariffType.equalsIgnoreCase(PriceCategory.PRICE_TO_PARTNER.getLabel())){
                            row.put(SpecialBidInfoStaticColumn.PTP_DEINSTALL.retrieveValueFrom,price);
                        }
                        if(tariffType.equalsIgnoreCase(PriceCategory.END_USER_PRICE.getLabel())){
                            row.put(SpecialBidInfoStaticColumn.EUP_DEINSTALL.retrieveValueFrom,price);
                        }
                    }
                }
                if(priceType.equalsIgnoreCase("RECURRING")){
                    if(tariffType.equalsIgnoreCase(PriceCategory.END_USER_PRICE.getLabel())){
                        row.put(SpecialBidInfoStaticColumn.MONTHLY_PRICE_LINE.retrieveValueFrom, priceLine.getId());
                        row.put(SpecialBidInfoStaticColumn.EUP_MONTHLY.retrieveValueFrom,price);
                        row.put(SpecialBidInfoStaticColumn.DISCOUNT_MONTHLY.retrieveValueFrom,discountPercentage);
                    }
                    if(tariffType.equalsIgnoreCase(PriceCategory.COST.getLabel())){
                        row.put(SpecialBidInfoStaticColumn.COST_MONTHLY.retrieveValueFrom,price);
                    }
                    if(tariffType.equalsIgnoreCase(PriceCategory.PRICE_TO_PARTNER.getLabel())){
                        row.put(SpecialBidInfoStaticColumn.PTP_MONTHLY.retrieveValueFrom,price);
                    }
                }
            }
            row.put(SpecialBidInfoStaticColumn.NON_STD_CONF_CATEGORY.retrieveValueFrom,
                    nonStdConfigurationCategory);
            row.put(SpecialBidInfoStaticColumn.PRICING_STATUS.retrieveValueFrom,specialBidProduct.getProductInstance().getBcmPricingStatus());
            rows.add(row);
        }
        return rows;
    }

    private List<PricingSheetSpecialBidProduct> filterLineItemsBasedOnSiteID(List<PricingSheetSpecialBidProduct> specialBidProducts) {
        Collections.sort(specialBidProducts, LINE_ITEM_MODEL_COMPARATOR);
        return specialBidProducts;
    }

    private static class LineItemModelComparator implements Comparator<PricingSheetSpecialBidProduct> {
        @Override
        public int compare(PricingSheetSpecialBidProduct lineItemModelOne, PricingSheetSpecialBidProduct lineItemModelTwo) {
            return lineItemModelOne.getSiteId().compareTo(lineItemModelTwo.getSiteId());
        }
    }
}
