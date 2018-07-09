package com.bt.rsqe.projectengine.web.quoteoption;

import com.bt.rsqe.enums.PriceCategory;
import com.bt.rsqe.enums.PriceType;
import com.bt.rsqe.projectengine.web.facades.LineItemFacade;
import com.bt.rsqe.projectengine.web.model.LineItemModel;
import com.bt.rsqe.projectengine.web.model.lineitemvisitor.LineItemVisitor;
import com.bt.rsqe.projectengine.web.model.lineitemvisitor.LineItemVisitorFactory;
import com.bt.rsqe.projectengine.web.model.lineitemvisitor.totalpricesvisitors.PriceVisitor;
import com.bt.rsqe.projectengine.web.model.lineitemvisitor.totalpricesvisitors.UsagePriceVisitor;
import com.bt.rsqe.projectengine.web.quoteoptionpricing.PriceSuppressStrategy;

import java.util.List;

public class QuoteOptionPricingSummaryOrchestrator {
    private final LineItemFacade lineItemFacade;
    private LineItemVisitorFactory lineItemVisitorFactory;

    public QuoteOptionPricingSummaryOrchestrator(LineItemFacade lineItemFacade, LineItemVisitorFactory lineItemVisitorFactory) {
        this.lineItemFacade = lineItemFacade;
        this.lineItemVisitorFactory = lineItemVisitorFactory;
    }

    public QuoteOptionPricingSummaryDTO getPricingSummary(String projectId, String quoteOptionId, String customerId, String contractId, PriceSuppressStrategy priceSuppressStrategy) {
        final List<LineItemModel> lineItemModels = lineItemFacade.fetchLineItems(customerId, contractId, projectId, quoteOptionId, priceSuppressStrategy);

        final QuoteOptionPricingSummaryDTO quoteOptionPricingSummaryDTO = new QuoteOptionPricingSummaryDTO();

        PriceVisitor oneTime = lineItemVisitorFactory.createPriceVisitor(PriceType.ONE_TIME, PriceCategory.CHARGE_PRICE);
        PriceVisitor recurring= lineItemVisitorFactory.createPriceVisitor(PriceType.RECURRING, PriceCategory.CHARGE_PRICE);
        UsagePriceVisitor usage = lineItemVisitorFactory.createUsageVisitor(PriceCategory.CHARGE_PRICE);
        LineItemVisitor visitor = lineItemVisitorFactory.createPricingSummaryVisitor(oneTime, recurring, usage);
        for (LineItemModel lineItemModel : lineItemModels) {
            lineItemModel.accept(visitor);
        }

        quoteOptionPricingSummaryDTO.totalOneTimeGross = oneTime.getGross().toBigDecimal();
        quoteOptionPricingSummaryDTO.totalOneTimeNet = oneTime.getNet().toBigDecimal();
        quoteOptionPricingSummaryDTO.totalOneTimeDiscount = oneTime.getDiscount().toBigDecimal();
        quoteOptionPricingSummaryDTO.totalRecurringGross = recurring.getGross().toBigDecimal();
        quoteOptionPricingSummaryDTO.totalRecurringNet = recurring.getNet().toBigDecimal();
        quoteOptionPricingSummaryDTO.totalRecurringDiscount = recurring.getDiscount().toBigDecimal();
        quoteOptionPricingSummaryDTO.totalOffNetUsage = usage.getTotalOffNetUsageCharge().toBigDecimal();
        quoteOptionPricingSummaryDTO.totalOnNetUsage =  usage.getTotalOnNetUsageCharge().toBigDecimal();
        quoteOptionPricingSummaryDTO.totalUsage = usage.getTotalUsageCharge().toBigDecimal();
        return quoteOptionPricingSummaryDTO;
    }

}
