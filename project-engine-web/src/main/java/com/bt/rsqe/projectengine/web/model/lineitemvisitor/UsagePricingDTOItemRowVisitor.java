package com.bt.rsqe.projectengine.web.model.lineitemvisitor;

import com.bt.rsqe.customerinventory.dto.PriceLineDTO;
import com.bt.rsqe.domain.product.extensions.Expression;
import com.bt.rsqe.domain.product.extensions.ExpressionExpectedResultType;
import com.bt.rsqe.domain.product.extensions.RuleRulesetAttributeSource;
import com.bt.rsqe.enums.PriceCategory;
import com.bt.rsqe.enums.PriceType;
import com.bt.rsqe.expressionevaluator.ContextualEvaluatorMap;
import com.bt.rsqe.projectengine.web.model.FutureAssetPricesModel;
import com.bt.rsqe.projectengine.web.model.LineItemModel;
import com.bt.rsqe.projectengine.web.model.PriceLineModel;
import com.bt.rsqe.projectengine.web.view.QuoteOptionPricingDTO;
import com.bt.rsqe.projectengine.web.view.QuoteOptionUsagePricingDTO;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.*;
import static com.google.common.collect.Maps.*;

public class UsagePricingDTOItemRowVisitor extends AbstractLineItemVisitor {
    private List<QuoteOptionUsagePricingDTO.UsageProduct> products;
    private LineItemModel lineItem;

    public UsagePricingDTOItemRowVisitor(List<QuoteOptionUsagePricingDTO.UsageProduct> products) {
        this.products = products;
    }

    public void visit(LineItemModel lineItem) {
        this.lineItem = lineItem;
    }

    @Override
    public void visit(FutureAssetPricesModel futureAssetPricesModel, int groupingLevel) {
        QuoteOptionUsagePricingDTO.UsageProduct product = new QuoteOptionUsagePricingDTO.UsageProduct();
        product.lineItemId = futureAssetPricesModel.getLineItemId();
        product.productName = futureAssetPricesModel.getDisplayName();
        product.pricingModel = futureAssetPricesModel.getPricesDTO().getPricingModel();
        product.summary = lineItem.getSummary();
        product.priceLines = newArrayList();
        products.add(product);
    }

    @Override
    public void visit(PriceLineModel priceLine) {
        if(!isUsagePriceLine(priceLine)) { return; } // only add usage priceLines!

        // create new price line
        QuoteOptionUsagePricingDTO.UsagePriceLine usagePriceLine = new QuoteOptionUsagePricingDTO.UsagePriceLine();
        usagePriceLine.description = priceLine.getDescription();
        usagePriceLine.tiers = newArrayList();

        // add all the prices to the price line ensuring to the condense Fixed, Min and Charge Rate Charges into a single price
        Map<String, QuoteOptionUsagePricingDTO.UsageItemRowDTO> tierNamesToItems = newHashMap();
        for(com.bt.rsqe.customerinventory.dto.PriceDTO price : getPriceLine(priceLine).getPrices()) {
            QuoteOptionUsagePricingDTO.UsageItemRowDTO row = fetchUsageRow(price, priceLine, tierNamesToItems);
            QuoteOptionPricingDTO.PriceLineDTO modelPrice = priceLine.createPriceFor(price);

            switch(price.getCategory()) {
                case FIXED_CHARGE: row.fixedCharge = modelPrice; break;
                case MIN_CHARGE: row.minCharge = modelPrice; break;
                case CHARGE_RATE: row.chargeRate = modelPrice; break;
            }
        }

        usagePriceLine.tiers.addAll(tierNamesToItems.values());
        Collections.sort(usagePriceLine.tiers); // sort by tier!
        getCurrentProduct().priceLines.add(usagePriceLine);
    }

    private QuoteOptionUsagePricingDTO.UsageItemRowDTO fetchUsageRow(com.bt.rsqe.customerinventory.dto.PriceDTO price,
                                                                     PriceLineModel priceLine,
                                                                     Map<String, QuoteOptionUsagePricingDTO.UsageItemRowDTO> tierNamesToItems) {
        QuoteOptionUsagePricingDTO.UsageItemRowDTO row = tierNamesToItems.get(price.getClassifier());
        if(null == row) {
            row = newUsageRow(priceLine, price);
            tierNamesToItems.put(price.getClassifier(), row);
        }
        return row;
    }

    private QuoteOptionUsagePricingDTO.UsageProduct getCurrentProduct() {
        return products.get(products.size()-1);
    }

    private boolean isUsagePriceLine(PriceLineModel priceLineModel) {
        PriceLineDTO priceLine = getPriceLine(priceLineModel);

        return PriceType.USAGE_BASED.equals(priceLineModel.getPriceType())
                    && null != priceLine
                    && !priceLine.getPrices().isEmpty()
                    && isUsageCategory(priceLine.getPrices().get(0).getCategory());
    }

    private boolean isUsageCategory(PriceCategory category) {
        switch(category) {
            case FIXED_CHARGE:
            case MIN_CHARGE:
            case CHARGE_RATE:
                return true;
            default:
                return false;
        }
    }

    private PriceLineDTO getPriceLine(PriceLineModel priceLineModel) {
        return priceLineModel.getPriceLineDTO(PriceType.RECURRING);
    }

    private QuoteOptionUsagePricingDTO.UsageItemRowDTO newUsageRow(PriceLineModel priceLineModel,
                                                                   com.bt.rsqe.customerinventory.dto.PriceDTO price) {
        QuoteOptionUsagePricingDTO.UsageProduct product = getCurrentProduct();
        QuoteOptionUsagePricingDTO.UsageItemRowDTO usageItemRow = new QuoteOptionUsagePricingDTO.UsageItemRowDTO();
        usageItemRow.lineItemId = product.lineItemId;
        usageItemRow.priceLineId = getPriceLine(priceLineModel).getId();
        usageItemRow.product = product.productName;
        usageItemRow.description = priceLineModel.getDescription();
        usageItemRow.pricingModel = product.pricingModel;
        usageItemRow.summary = lineItem.getSummary();
        usageItemRow.tier = price.getClassifier();
        usageItemRow.tierDescription = getTierDescription(usageItemRow.tier);

        return usageItemRow;
    }

    // TODO we need an easier/cleaner way to get values from a PMR ruleset without having to go through an AttributeSource rule...
    private String getTierDescription(String tier) {
        final String tierRuleSetId = "R0301661";
        final String tierDescription = "Tier_Description";
        final String tierExpression = "Tier = '%s'";
        final String currencyValueExpression = "Currency = '%s'";
        String currency = this.lineItem.getQuoteOptionDTO() != null ? this.lineItem.getQuoteOptionDTO().getCurrency() : "";

        RuleRulesetAttributeSource rulesetLookup = new RuleRulesetAttributeSource("",
                                                                                  new Expression(tierDescription, ExpressionExpectedResultType.String),
                                                                                  null,
                                                                                  null,
                                                                                  -1,
                                                                                  tierRuleSetId,
                                                                                  false,
                                                                                  "", null);

        rulesetLookup.addMappingExpression(new Expression(String.format(tierExpression, tier), ExpressionExpectedResultType.String));
        rulesetLookup.addMappingExpression(new Expression(String.format(currencyValueExpression, currency), ExpressionExpectedResultType.String));
        Optional<List<String>> response = rulesetLookup.execute(Lists.<ContextualEvaluatorMap>newArrayList());

        if(response.isPresent() && !response.get().isEmpty()) {
            return response.get().get(0);
        } else {
            return "";
        }
    }
}
