package com.bt.rsqe.projectengine.web.model.lineitemvisitor;

import com.bt.rsqe.domain.project.PricingStatus;
import com.bt.rsqe.enums.ProductAction;
import com.bt.rsqe.expedio.project.ProjectDTO;
import com.bt.rsqe.logging.Log;
import com.bt.rsqe.logging.LogFactory;
import com.bt.rsqe.logging.LogLevel;
import com.bt.rsqe.projectengine.web.model.LineItemModel;
import com.bt.rsqe.projectengine.web.view.QuoteOptionDetailsDTO;

import java.util.List;

import static com.bt.rsqe.domain.project.PricingStatus.*;
import static com.bt.rsqe.projectengine.LineItemValidationResultDTO.Status.*;
import static com.google.common.base.Strings.*;

public class DetailsDTOLineItemVisitor extends AbstractLineItemVisitor {
    private static final DetailsDTOLineItemVisitorLogger LOG = LogFactory.createDefaultLogger(DetailsDTOLineItemVisitorLogger.class);
    private final List<QuoteOptionDetailsDTO.LineItem> lineItemList;
    private ProjectDTO project;

    public DetailsDTOLineItemVisitor(List<QuoteOptionDetailsDTO.LineItem> lineItemList) {
        this.lineItemList = lineItemList;
    }

    //Added Default statuses when Line Item is in Initialising State, This could be changed after Demo.
    public void visit(LineItemModel lineItem) {
        QuoteOptionDetailsDTO.LineItem row;
        LOG.logDetailsDTOLineItemVisitorCalled();
        row = new QuoteOptionDetailsDTO.LineItem(lineItem.getId(),
                                                 lineItem.getDisplayName(),
                                                 lineItem.isInitialised() ? miniAddress(lineItem) :"",
                                                 nullToEmpty(lineItem.getSubLocationName()),
                                                 nullToEmpty(lineItem.getFloor()),
                                                 nullToEmpty(lineItem.getRoom()),
                                                 lineItem.getProductSCode(),
                                                 lineItem.isInitialised() ? lineItem.getSite().name : "",
                                                 lineItem.getContractTerm(),
                                                 lineItem.getAction(),
                                                 lineItem.getOfferName(),
                                                 lineItem.getStatus(),
                                                 lineItem.getDiscountStatus(),
                                                 lineItem.getConfigureUrl(getProject(lineItem)),
                                                 lineItem.getOfferDetailsUrl(),
                                                 lineItem.getProductDetailsUrl(),
                                                 lineItem.getErrorMessage(),
                                                 lineItem.isInitialised() ? lineItem.getValidity() : PENDING.name(),
                                                 lineItem.getOrderStatus(),
                                                 getPricingStatus(lineItem),
                                                 lineItem.getIfcAction(),
                                                 lineItem.isForIfc(),
                                                 lineItem.getContractTerm(),
                                                 lineItem.getSummary(),
                                                 lineItem.getIsImportable(),
                                                 lineItem.isInFrontCatlogueProduct(),
                                                 getServiceLevelAgreementName(lineItem),
												 lineItem.getHasLineItemNotes(),
                                                 getMaintainerAgreementName(lineItem),
                                                 getRemainingContractTerm(lineItem),
                                                 lineItem.isProxyProduct()
                                                 );

        lineItemList.add(row);
    }

    private String getServiceLevelAgreementName(LineItemModel lineItem) {
        String serviceLevelAgreementName;
        if (lineItem.isSlaConfigured()) {
            serviceLevelAgreementName = "Advanced Agreements";
        } else if (lineItem.isInFrontCatlogueProduct()) {
            serviceLevelAgreementName = "Standard Agreements";
        } else {
            serviceLevelAgreementName = "N/A";
        }
        return serviceLevelAgreementName;
    }

    private String getMaintainerAgreementName(LineItemModel lineItem) {
        String maintainerAgreementName;

        if (lineItem.isMAGApplicable()
                && lineItem.isMAGConfigured()
                && lineItem.isInFrontCatlogueProduct()
                &&lineItem.isComplexContractCustomer()) {
            maintainerAgreementName = "Advanced Agreements";
        } else if (lineItem.isMAGApplicable()
                && !lineItem.isMAGConfigured()
                && lineItem.isInFrontCatlogueProduct()
                &&lineItem.isComplexContractCustomer()) {
            maintainerAgreementName = "No Agreements Selected";
        } else if (lineItem.isInFrontCatlogueProduct()) {
            maintainerAgreementName = "Standard Agreements";
        } else {
            maintainerAgreementName = "N/A";
        }
        return maintainerAgreementName;
    }

    private String getRemainingContractTerm(LineItemModel lineItem){
        if(ProductAction.Provide.toString().equals(lineItem.getAction())){
            return "NA";
        } else {
            return String.valueOf(lineItem.getRemainingMonths());
        }
    }

    private String getPricingStatus(LineItemModel lineItem) {
        if(!lineItem.isInitialised()) {
            return NOT_APPLICABLE.getDescription();
        }

        final PricingStatus pricingStatusOfTree = lineItem.getPricingStatusOfTree();
        if(NOT_PRICED.equals(pricingStatusOfTree) && lineItem.anyAssetsAreFirm() && !lineItem.anyAssetsAreContractResigned()) {
            return PricingStatus.PARTIALLY_PRICED;
        }

        return pricingStatusOfTree.getDescription();
    }

    private ProjectDTO getProject(LineItemModel lineItem) {
        if(null == project || !project.projectId.equals(lineItem.projectId())) {
            project = lineItem.getProjectDTO();
        }
        return project;
    }

     private String miniAddress(LineItemModel lineItem) {
         if (lineItem.getSite() != null) {
             if ((lineItem.getSite().city != null) && (lineItem.getSite().country != null)) {
                 return lineItem.getSite().city + ", " + lineItem.getSite().country;
             } else if ((lineItem.getSite().city != null) && (lineItem.getSite().country == null)) {
                 return lineItem.getSite().city;
             } else if ((lineItem.getSite().city == null) && (lineItem.getSite().country != null)) {
                 return lineItem.getSite().country;
             } else  {
                 return "";
             }

         } else {
             return "";
         }


     }

    interface  DetailsDTOLineItemVisitorLogger {

        @Log(level = LogLevel.DEBUG, loggerName = "DetailsDTOLineItemVisitorLogger", format = "DetailsDTOLineItemVisitor visit being called")
        void logDetailsDTOLineItemVisitorCalled();

    }

}

