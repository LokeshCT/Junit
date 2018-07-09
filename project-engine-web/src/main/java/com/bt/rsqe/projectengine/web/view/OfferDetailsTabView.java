package com.bt.rsqe.projectengine.web.view;

import com.bt.rsqe.projectengine.web.model.OfferDetailsModel;
import com.bt.rsqe.projectengine.web.uri.UriFactoryImpl;
import com.bt.rsqe.web.LocalDateTimeFormatter;

public class OfferDetailsTabView {
    private final String customerId;
    private final String contractId;
    private final String projectId;
    private final String quoteOptionId;
    private final String offerId;
    private final String customerName;
    private final String created;
    private final String approveAction;
    private final String rejectAction;
    private final String createOrderAction;
    private final String validateAction;
    private String exportPricingSheetLink;
    private String cancelApprovalAction;

    public OfferDetailsTabView(String customerId, String contractId, String projectId, String quoteOptionId, OfferDetailsModel offerDetailsModel,
                               String customerName, String exportPricingSheetLink) {
        this.customerId = customerId;
        this.contractId = contractId;
        this.projectId = projectId;
        this.quoteOptionId = quoteOptionId;
        this.offerId = offerDetailsModel.getId();
        this.customerName = customerName;
        this.created = new LocalDateTimeFormatter(offerDetailsModel.getCreatedDate()).format();
        this.createOrderAction = offerDetailsModel.isApproved() ?
            UriFactoryImpl.orders(customerId, contractId, projectId, quoteOptionId).toString() :
            null;
        this.approveAction = getCanCreateOrder() ?
            null :
            UriFactoryImpl.offerApprove(customerId, contractId, projectId, quoteOptionId, offerId).toString();
        this.rejectAction = offerDetailsModel.isActive() ?
            UriFactoryImpl.offerReject(customerId, projectId, quoteOptionId, offerId, contractId).toString() :
            null;
        this.validateAction = UriFactoryImpl.lineItemValidationUri(customerId, contractId, projectId, quoteOptionId, "(id)").toString();
        this.exportPricingSheetLink = exportPricingSheetLink;
        this.cancelApprovalAction = offerDetailsModel.isApproved() ?
            UriFactoryImpl.cancelOfferApproval(customerId, contractId, projectId, quoteOptionId, offerId).toString() :
            null;
    }

    public String getProjectId() {
        return projectId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getContractId() {
        return contractId;
    }

    public String getQuoteOptionId() {
        return quoteOptionId;
    }

    public String getOfferId() {
        return offerId;
    }

    public String getCreated() {
        return created;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getApproveAction() {
        return approveAction;
    }

    public String getRejectAction() {
        return rejectAction;
    }

    public String getCreateOrderAction() {
        return createOrderAction;
    }

    public String getValidateAction() {
        return validateAction;
    }

    @SuppressWarnings("PMD.BooleanGetMethodName")    // needed for FTL
    public boolean getIsApprovable() {
        return approveAction != null;
    }

    @SuppressWarnings("PMD.BooleanGetMethodName")    // needed for FTL
    public boolean getIsRejectable() {
        return rejectAction != null;
    }

    @SuppressWarnings("PMD.BooleanGetMethodName")    // needed for FTL
    public boolean getCanCreateOrder() {
        return createOrderAction != null;
    }

    public String getShowApproveOffer() {
        return String.valueOf(getIsApprovable());
    }

    public String getShowRejectOffer() {
        return String.valueOf(getIsRejectable());
    }

    public String getShowCreateOrder() {
        return String.valueOf(getCanCreateOrder());
    }

    public String getExportPricingSheetLink() {
        return exportPricingSheetLink;
    }

    public boolean getShowCancelOfferApproval() {
        return cancelApprovalAction != null;
    }

    public String getCancelApprovalAction() {
        return cancelApprovalAction;
    }

    public String getCancelOfferApproval() {
        return String.valueOf(getShowCancelOfferApproval());
    }
}
