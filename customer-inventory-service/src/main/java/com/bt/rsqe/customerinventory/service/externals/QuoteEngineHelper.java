package com.bt.rsqe.customerinventory.service.externals;

import com.bt.rsqe.domain.ContractDTO;
import com.bt.rsqe.domain.QuoteOptionItemStatus;
import com.bt.rsqe.domain.product.parameters.ProductCategoryCode;
import com.bt.rsqe.projectengine.IfcAction;
import com.bt.rsqe.projectengine.LineItemDiscountStatus;
import com.bt.rsqe.projectengine.LineItemIcbApprovalStatus;
import com.bt.rsqe.projectengine.LineItemOrderStatus;
import com.bt.rsqe.projectengine.LineItemValidationResultDTO;
import com.bt.rsqe.projectengine.ProjectResource;
import com.bt.rsqe.projectengine.QuoteOptionItemDTO;
import com.bt.rsqe.projectengine.QuoteOptionItemResource;
import com.bt.rsqe.projectengine.QuoteOptionResource;
import com.bt.rsqe.web.rest.dto.types.JaxbDateTime;

public class QuoteEngineHelper {
    private final ProjectResource projectResource;

    public QuoteEngineHelper(ProjectResource projectResource) {
        this.projectResource = projectResource;
    }

    public void associateQuoteOptionItem(String projectId, String quoteOptionId, QuoteOptionItemDTO quoteOptionItemDTO) {
        final QuoteOptionResource quoteOptionResource = projectResource.quoteOptionResource(projectId);
        final QuoteOptionItemResource quoteOptionItemResource = quoteOptionResource.quoteOptionItemResource(quoteOptionId);
        quoteOptionItemResource.associate(quoteOptionItemDTO);
    }

    public QuoteOptionItemDTO createQuoteOptionItem(String projectId, String quoteOptionId, String lineItemId, String productCode, String action, String contractTerm, ContractDTO contractDTO,
                                                    boolean isIFC, boolean isImportable, JaxbDateTime customerRequiredDate, ProductCategoryCode productCategoryCode, String bundleItemId, boolean bundleProduct) {

        final QuoteOptionResource quoteOptionResource = projectResource.quoteOptionResource(projectId);
        final QuoteOptionItemResource quoteOptionItemResource = quoteOptionResource.quoteOptionItemResource(quoteOptionId);

        QuoteOptionItemDTO quoteOptionItemDTO = new QuoteOptionItemDTO(lineItemId, productCode, action,
                null, null, //offerId
                null, //offerName
                contractTerm,
                QuoteOptionItemStatus.INITIALIZING,
                LineItemDiscountStatus.NOT_APPLICABLE,
                LineItemIcbApprovalStatus.NOT_APPLICABLE,
                null, //orderId
                new LineItemValidationResultDTO(LineItemValidationResultDTO.Status.PENDING),
                LineItemOrderStatus.NOT_APPLICABLE,
                IfcAction.NOT_APPLICABLE,
                null, //billingId
                null, //parent
                contractDTO,
                false, //superseded
                isIFC,
                isImportable,
                customerRequiredDate,
                null //holderLineItemId
                ,
                true, productCategoryCode, bundleItemId, bundleProduct);
        quoteOptionItemResource.post(quoteOptionItemDTO);

        quoteOptionItemResource.putInitialValidationSuccessNotification(lineItemId);

        return quoteOptionItemDTO;
    }

    public void removeQuoteOptionItem(String projectId, String quoteOptionId, String lineItemId) {
        final QuoteOptionResource quoteOptionResource = projectResource.quoteOptionResource(projectId);
        final QuoteOptionItemResource quoteOptionItemResource = quoteOptionResource.quoteOptionItemResource(quoteOptionId);
        quoteOptionItemResource.delete(lineItemId);
    }


    public QuoteOptionItemDTO getQuoteOptionItem(String projectId, String quoteOptionId, String lineItemId) {
        final QuoteOptionResource quoteOptionResource = projectResource.quoteOptionResource(projectId);
        final QuoteOptionItemResource quoteOptionItemResource = quoteOptionResource.quoteOptionItemResource(quoteOptionId);
        return quoteOptionItemResource.get(lineItemId);
    }
}
