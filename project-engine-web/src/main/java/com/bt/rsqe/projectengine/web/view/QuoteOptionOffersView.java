package com.bt.rsqe.projectengine.web.view;

import com.bt.rsqe.projectengine.web.model.OfferDetailsModel;
import com.bt.rsqe.projectengine.web.uri.UriFactoryImpl;
import com.bt.rsqe.web.LocalDateTimeFormatter;
import com.google.common.base.Strings;

import java.util.List;

import static com.google.common.collect.Lists.*;

public class QuoteOptionOffersView {
    private final String customerId;
    private final String contractId;
    private final String projectId;
    private final String quoteOptionId;
    private boolean isOrderSubmitted;
    private boolean isOrderCreated;
    private String APPROVED_STATUS = "Approved";
    private final List<OfferRowItem> offers = newArrayList();

    public QuoteOptionOffersView(String customerId, String contractId, String projectId, String quoteOptionId) {
        this.customerId = customerId;
        this.contractId = contractId;
        this.projectId = projectId;
        this.quoteOptionId = quoteOptionId;
    }

    public String getProjectId() {
        return projectId;
    }

    public String getQuoteOptionId() {
        return quoteOptionId;
    }

    public boolean isOrderSubmitted() {
        return isOrderSubmitted;
    }

    public void setOrderSubmittedFlag(boolean orderSubmitted) {
        isOrderSubmitted = orderSubmitted;
    }

    public boolean isOrderCreated() {
        return isOrderCreated;
    }

    public void setOrderCreatedFlag(boolean orderCreated) {
        isOrderCreated = orderCreated;
    }
    public List<OfferRowItem> getOffers() {
        return offers;
    }

    public void addOffer(OfferRowItem offer) {
        offers.add(offer);
    }

    public class OfferRowItem {
        private final OfferDetailsModel model;
        private final String created;


        public OfferRowItem(OfferDetailsModel model) {
            this.model = model;
            this.created = new LocalDateTimeFormatter(model.getCreatedDate()).format();
        }

        public String getName() {
            return model.getName();
        }

        public String getCreated() {
            return created;
        }

        public String getStatus() {
            return model.getStatus().getDescription();
        }

        public String getOfferDetailsLink() {
            return UriFactoryImpl.offerDetails(customerId, contractId, projectId, quoteOptionId, model.getId()).toString();
        }

        @SuppressWarnings("PMD.BooleanGetMethodName")    // needed for FTL
        public boolean getIsActive() {
            return model.isActive();
        }

        @SuppressWarnings("PMD.BooleanGetMethodName")    // needed for FTL
        public boolean getCanApprove() {
            // FIXME Hugh : need to call model.isCustomerApprovable() here instead but it's not implemented yet
              return model.isCustomerApprovable();
            //return getIsActive();
        }

        public boolean getCanCancel(){
            return model.isCustomerCancellable();
        }

        @SuppressWarnings("PMD.BooleanGetMethodName")    // needed for FTL
        public boolean getCanShowRejectButton() {
            // FIXME Hugh : need to call model.isCustomerApprovable() here instead but it's not implemented yet
            // return model.isCustomerApprovable();
            if(isOrderSubmitted){
                   return false;
            }
            if(isOrderCreated){
                return true;
            }
            if(getCanApprove() && !isOrderSubmitted){
                return true;
            }
            if(getCanApprove() && isOrderCreated){
                return true;
            }
            if(model.getStatus().getDescription().equalsIgnoreCase(APPROVED_STATUS) && !isOrderSubmitted){
                              return true;
            }
            else {
                return false;
            }
        }



        public boolean getShowRejectButton() {
            // FIXME Hugh : need to call model.isCustomerApprovable() here instead but it's not implemented yet
            // return model.isCustomerApprovable();
            return getIsActive();
        }

        public String getApproveOfferLink() {
            // FIXME need to hit approveOffer() on QORH
            return UriFactoryImpl.offerApprove(customerId, contractId, projectId, quoteOptionId, model.getId()).toString();
        }

        public String getRejectOfferLink() {
            // FIXME need to hit approveOffer() on QORH
            return UriFactoryImpl.offerReject(customerId, projectId, quoteOptionId, model.getId(), contractId).toString();
        }

        public String getCustomerOrderReference(){
            return Strings.nullToEmpty(model.getCustomerOrderReference());
        }

        public String getCancelOfferApprovalUri() {
            return UriFactoryImpl.cancelOfferApproval(customerId, contractId, projectId, quoteOptionId, model.getId()).toString();
        }

        public boolean getShowCancelOfferApprovalButton() {
            return model.isApproved();
        }

    }
}
