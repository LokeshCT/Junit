package com.bt.rsqe.projectengine.web.quoteoptionoffers;

import com.bt.rsqe.domain.project.PricingStatus;
import com.bt.rsqe.projectengine.web.OfferAndOrderValidationResult;
import com.bt.rsqe.projectengine.web.OfferAndOrderValidator;
import com.bt.rsqe.projectengine.web.facades.QuoteOptionOfferFacade;
import com.bt.rsqe.projectengine.web.model.LineItemModel;
import com.bt.rsqe.projectengine.web.model.OfferDetailsModel;
import com.bt.rsqe.projectengine.web.view.QuoteOptionOffersView;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.joda.time.DateTime;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

public class QuoteOptionOffersOrchestrator {
    private static final Comparator<OfferDetailsModel> DATE_CREATED_ORDER = new Comparator<OfferDetailsModel>() {
        @Override
        public int compare(OfferDetailsModel o1, OfferDetailsModel o2) {
            return new DateTime(o2.getCreatedDate()).compareTo(new DateTime(o1.getCreatedDate()));
        }
    };
    private final QuoteOptionOfferFacade offerFacade;
    private OfferAndOrderValidator validator;

    public QuoteOptionOffersOrchestrator(QuoteOptionOfferFacade offerFacade, OfferAndOrderValidator validator) {
        this.offerFacade = offerFacade;
        this.validator = validator;
    }

    public QuoteOptionOffersView buildView(String customerId, String contractId, String projectId, String quoteOptionId) throws UnableToBuildViewException {
        final QuoteOptionOffersView view = new QuoteOptionOffersView(customerId, contractId, projectId, quoteOptionId);
        List<OfferDetailsModel> offers = offerFacade.get(projectId, quoteOptionId);
        Collections.sort(offers, DATE_CREATED_ORDER);
        for (OfferDetailsModel offer : offers) {
            view.addOffer(view.new OfferRowItem(offer));
        }
        return view;
    }

    public void approveOffer(String projectId, String quoteOptionId, String offerId) {
        offerFacade.approve(projectId, quoteOptionId, offerId);
    }

    public void rejectOffer(String projectId, String quoteOptionId, String offerId) {
        offerFacade.reject(projectId, quoteOptionId, offerId);
    }

    public void buildOffer(String projectId, String quoteOptionId, String offerName, String quoteOptionItemIds, String customerOrderReference) {
        offerFacade.createOffer(projectId, quoteOptionId, offerName, Arrays.asList(quoteOptionItemIds.split(",")), customerOrderReference);
    }

    public void cancelOfferApproval(String projectId, String quoteOptionId, String offerId) {
        offerFacade.cancelOfferApproval(projectId, quoteOptionId, offerId);
    }

    public OfferAndOrderValidationResult validateStatusForOfferCreation(String projectId, String quoteOptionId, String customerId, String contractId, List<String> lineItems) {
        List<PricingStatus> statusList = newArrayList(PricingStatus.values());
        statusList.remove(PricingStatus.NOT_PRICED);
        return validator.anyLineItemsWithPricingStatus(projectId, quoteOptionId, customerId, contractId, lineItems, statusList);
    }

    public OfferAndOrderValidationResult validateStatusForOfferApproval(String projectId, String quoteOptionId, String customerId, String contractId) {
        List<OfferDetailsModel> offerDetailsModels = offerFacade.get(projectId, quoteOptionId);
        List lineItems = newArrayList();
        for (OfferDetailsModel model : offerDetailsModels) {
            List<LineItemModel> lineItem = model.getLineItems(customerId, contractId, projectId, quoteOptionId);
            lineItems.addAll(Lists.newArrayList(Iterables.transform(lineItem, new Function<LineItemModel, String>() {
                @Override
                public String apply(LineItemModel input) {
                    return input.getId();
                }
            })));
        }

        List<PricingStatus> statusList = newArrayList(PricingStatus.values());
        statusList.remove(PricingStatus.NOT_PRICED);
        OfferAndOrderValidationResult validationResult = validator.anyLineItemsWithPricingStatus(projectId, quoteOptionId, customerId, contractId, lineItems, statusList);
        if (validationResult.isValid()) {
            validationResult = validator.anyLineItemHavingInvalidDiscountStatus(lineItems, projectId, quoteOptionId);
        }
        return validationResult;
    }

    public OfferAndOrderValidationResult validateProxyAssetConfigurationDetails(List<String> lineItems){
        return validator.proxyAssetConfigurationStatus(lineItems);
    }


    public static class UnableToBuildViewException extends Exception {

    }
}
