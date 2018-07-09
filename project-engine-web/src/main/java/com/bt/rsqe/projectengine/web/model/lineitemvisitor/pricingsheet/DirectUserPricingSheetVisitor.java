package com.bt.rsqe.projectengine.web.model.lineitemvisitor.pricingsheet;

import com.bt.rsqe.Money;
import com.bt.rsqe.customerrecord.SiteDTO;
import com.bt.rsqe.domain.product.AttributeName;
import com.bt.rsqe.domain.product.InstanceCharacteristic;
import com.bt.rsqe.domain.project.InstanceCharacteristicNotFound;
import com.bt.rsqe.domain.project.ProductInstance;
import com.bt.rsqe.enums.PriceCategory;
import com.bt.rsqe.enums.PriceType;
import com.bt.rsqe.projectengine.web.model.FutureAssetPricesModel;
import com.bt.rsqe.projectengine.web.model.LineItemModel;
import com.bt.rsqe.projectengine.web.model.PriceLineModel;
import com.bt.rsqe.projectengine.web.model.lineitemvisitor.CompositeLineItemVisitor;
import com.bt.rsqe.projectengine.web.model.lineitemvisitor.LineItemVisitorFactory;
import com.bt.rsqe.projectengine.web.model.lineitemvisitor.totalpricesvisitors.PriceVisitor;
import com.bt.rsqe.projectengine.web.model.lineitemvisitor.totalpricesvisitors.PricesTotalAggregator;
import com.bt.rsqe.projectengine.web.model.lineitemvisitor.totalpricesvisitors.UsagePriceVisitor;
import com.bt.rsqe.projectengine.web.quoteoptionpricing.PricingSheetKeys;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.bt.rsqe.projectengine.web.quoteoptionpricing.PricingSheetKeys.*;
import static com.google.common.collect.Lists.*;

public class DirectUserPricingSheetVisitor extends CompositeLineItemVisitor {
    private Map<String, Object> output;
    private PricesTotalAggregator priceLineAggregator;
    private Map<String, Object> siteAgnosticMap;
    private ProductInstance productInstance;
    private PriceVisitor recurringChargePriceVisitor;
    private PriceVisitor oneTimeChargePriceVisitor;
    private UsagePriceVisitor usagePriceVisitor;
    private List<Map<String,Object>> priceLineList;

    public DirectUserPricingSheetVisitor(ProductInstance productInstance, Map<String, Object> output,
                                         PricesTotalAggregator priceLineAggregator, LineItemVisitorFactory lineItemVisitorFactory, Map<String, Object> siteAgnosticMap) {
        this.productInstance = productInstance;
        this.output = output;
        this.priceLineAggregator = priceLineAggregator;
        this.siteAgnosticMap = siteAgnosticMap;

        recurringChargePriceVisitor = lineItemVisitorFactory.createPriceVisitor(PriceType.RECURRING, PriceCategory.CHARGE_PRICE);
        oneTimeChargePriceVisitor = lineItemVisitorFactory.createPriceVisitor(PriceType.ONE_TIME, PriceCategory.CHARGE_PRICE);
        usagePriceVisitor = lineItemVisitorFactory.createUsageVisitor(PriceCategory.CHARGE_PRICE);
        super.addVisitors(recurringChargePriceVisitor, oneTimeChargePriceVisitor, usagePriceVisitor);
    }

    @Override
    public void visit(LineItemModel lineItem) {
        super.visit(lineItem);

        if(productInstance.getProductOffering().isSiteInstallable()){
            output.put(PricingSheetKeys.ACTION,lineItem.getAction());
            output.put(PricingSheetKeys.CONTRACT_TERM, lineItem.getContractTerm());
            output.put(PricingSheetKeys.REMAINING_CONTRACT_TERM, lineItem.getContractTerm());
            output.put(PricingSheetKeys.PRICE_TYPE,"NEW");
            output.put(PricingSheetKeys.PRODUCT_INSTANCE_ID, productInstance.getProductInstanceId());
        try {
                output.put(PricingSheetKeys.RESILIENCE, getResilience());
            } catch (InstanceCharacteristicNotFound instanceCharacteristicNotFound) {
                output.put(PricingSheetKeys.RESILIENCE, "");
            }
        }
    }

    private String getResilience() throws InstanceCharacteristicNotFound {
        InstanceCharacteristic resilienceAttribute = null;
        if(productInstance.hasInstanceCharacteristic(new AttributeName(PricingSheetKeys.ATTRIBUTE_RESILIENCE))){
            resilienceAttribute = productInstance.getInstanceCharacteristic(PricingSheetKeys.ATTRIBUTE_RESILIENCE);
        }else{
            resilienceAttribute = productInstance.getInstanceCharacteristic("STENCIL");
        }
        return resilienceAttribute == null || resilienceAttribute.getValue() == null ? "" :
            resilienceAttribute.captionFrom(resilienceAttribute.getValue(),resilienceAttribute.getAllowedValuesProvider());
    }

    @Override
    public void visitAfterChildren(LineItemModel lineItem) {
        SiteDTO site = lineItem.getSite();

        final Money oneTime = oneTimeChargePriceVisitor.getNet();
        final Money recurring = recurringChargePriceVisitor.getNet();
        final Money usage = usagePriceVisitor.getTotalUsageCharge();
        final Money onnetUsage = usagePriceVisitor.getTotalOnNetUsageCharge();
        final Money offnetUsage = usagePriceVisitor.getTotalOffNetUsageCharge();

        if(productInstance.getProductOffering().isSiteInstallable()){
            output.put(PRODUCT_NAME, lineItem.getProductName());
            output.put(PRODUCT_SCODE, lineItem.getProductSCode());
            output.put(SITE_NAME, site.name);
            output.put(SITE_CITY, site.city);
            output.put(RRP_PRICE_BOOK_VERSION,lineItem.getPriceBook());
            output.put(SITE_USAGE_RRP, usage.toDouble());
            output.put(SITE_ONNET_USAGE_RRP, onnetUsage.toDouble());
            output.put(SITE_OFFNET_USAGE_RRP, offnetUsage.toDouble());
            output.put(SITE_ONE_TIME_RRP, oneTime.toDouble());
            output.put(SITE_RECURRING_RRP, recurring.toDouble());
            output.put(SITE_USAGE_RRP, usage.toDouble());
            output.put(SITE_OFFNET_USAGE_RRP, offnetUsage.toDouble());
            output.put(SITE_ONNET_USAGE_RRP, onnetUsage.toDouble());
        }
        if(siteAgnosticMap!= null) {
            siteAgnosticMap.put(SITE_ONE_TIME_RRP, oneTime.toDouble());
            siteAgnosticMap.put(SITE_RECURRING_RRP, recurring.toDouble());
        }
        populateChildValues(productInstance);
        if (lineItem.isSuperseded()) {
            output.put(SITE_IFC_STATUS, "OLD");
        } else {
            priceLineAggregator.addOneTimeRRP(oneTime);
            priceLineAggregator.addRecurringRRP(recurring);
            priceLineAggregator.addUsageRRP(usage);
            priceLineAggregator.addOffNetUsageRRP(offnetUsage);
            priceLineAggregator.addOnNetUsageRRP(onnetUsage);
        }
    }

    private void populateChildValues(ProductInstance productInstances) {
        List<Map<String,Object>> childList =  newArrayList();
        Map<String,Object> childAttributes = new HashMap<String, Object>();

        for(ProductInstance productInstance : productInstances.getChildren()){
            try {
                childAttributes.put(PricingSheetKeys.PRODUCT_INSTANCE_ID, productInstance.getProductInstanceId());
                childAttributes.put(PricingSheetKeys.SITE_BUNDLE_NAME, productInstance.getInstanceCharacteristic(PricingSheetKeys.ATTRIBUTE_BUNDLE_NAME).getStringValue());
                childAttributes.put(PricingSheetKeys.SITE_BUNDLE_TYPE,productInstance.getInstanceCharacteristic(PricingSheetKeys.ATTRIBUTE_BUNDLE_TYPE).getStringValue());

            } catch (InstanceCharacteristicNotFound instanceCharacteristicNotFound) {
                childAttributes.put(PricingSheetKeys.SITE_BUNDLE_NAME, "");
                childAttributes.put(PricingSheetKeys.SITE_BUNDLE_TYPE,"");
                }
            childList.add(childAttributes);
            childAttributes = new HashMap<String, Object>();
        }
        if(productInstances.getChildren().size()==0){
            childAttributes.put(PricingSheetKeys.SITE_BUNDLE_NAME, "");
            childAttributes.put(PricingSheetKeys.SITE_BUNDLE_TYPE,"");
            childList.add(childAttributes);
            Map<String, Object> priceValues = new HashMap<String, Object>();
            priceValues.put(SITE_ONE_TIME_RRP, "");
            priceValues.put(SITE_RECURRING_RRP,"");
            priceLineList = newArrayList();
            priceLineList.add(priceValues);
            output.put(PricingSheetKeys.CHILD_PRICES,priceLineList);
        }
        output.put(PricingSheetKeys.PRODUCT_CHILDREN,childList);
    }

    @Override
    public void visit(PriceLineModel priceLine) {
        super.visit(priceLine);

        Map<String, Object> priceValues = new HashMap<String, Object>();
        Money oneTimeValue =priceLine.getOneTimeCPValue();
        Money recurringValue = priceLine.getRecurringCPValue();
        priceValues.put(SITE_ONE_TIME_RRP, oneTimeValue.toString());
        priceValues.put(SITE_RECURRING_RRP,recurringValue.toString());
        priceLineList.add(priceValues);
    }

    @Override
    public void visit(FutureAssetPricesModel futureAssetPricesModel, int groupingLevel){
        priceLineList = newArrayList();
        output.put(PricingSheetKeys.CHILD_PRICES,priceLineList);
    }

}
