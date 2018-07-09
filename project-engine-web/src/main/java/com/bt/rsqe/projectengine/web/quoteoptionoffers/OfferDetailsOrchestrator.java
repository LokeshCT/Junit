package com.bt.rsqe.projectengine.web.quoteoptionoffers;


import com.bt.rsqe.customerrecord.CustomerResource;
import com.bt.rsqe.projectengine.OfferDTO;
import com.bt.rsqe.projectengine.web.facades.QuoteOptionOfferFacade;
import com.bt.rsqe.projectengine.web.model.LineItemModel;
import com.bt.rsqe.projectengine.web.model.OfferDetailsModel;
import com.bt.rsqe.projectengine.web.model.lineitemvisitor.OfferItemDtoLineItemVisitor;
import com.bt.rsqe.projectengine.web.uri.UriFactoryImpl;
import com.bt.rsqe.projectengine.web.view.OfferDetailsDTO;
import com.bt.rsqe.projectengine.web.view.OfferDetailsTabView;
import com.bt.rsqe.projectengine.web.view.PageView;
import com.bt.rsqe.projectengine.web.view.pagination.Pagination;
import com.bt.rsqe.security.UserContextManager;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

public class OfferDetailsOrchestrator {
    private final QuoteOptionOfferFacade quoteOptionOfferFacade;
    private final CustomerResource customerResource;

    public OfferDetailsOrchestrator(QuoteOptionOfferFacade quoteOptionOfferFacade, CustomerResource customerResource) {
        this.quoteOptionOfferFacade = quoteOptionOfferFacade;
        this.customerResource = customerResource;
    }

    // NOTE: This method should not be used except to build the JSON response for "getQuoteOptionOfferDetails"
    public OfferDetailsDTO buildJsonResponse(String customerId, String contractId, String projectId, String quoteOptionId, String offerId,
                                             Pagination pagination) {
        OfferDetailsModel offer = quoteOptionOfferFacade.getOfferDetails(projectId, quoteOptionId, offerId);
        List<OfferDetailsDTO.ItemRowDTO> itemRowDTOs = addOfferItems(newArrayList(Iterables.filter(offer.getLineItems(customerId, contractId, projectId, quoteOptionId), new Predicate<LineItemModel>() {
            @Override
            public boolean apply(@Nullable LineItemModel model) {
                return model.isProductVisibleOnlineSummary() && model.isInFrontCatlogueProduct();
            }
        })));
        final int size = itemRowDTOs.size(); // This is currently used for both total records & total display records as we don't have filtering
        return new OfferDetailsDTO(offer.getName(), offer.getCreatedDate(), pagination.getPageNumber(), size, size,
                                   pagination.paginate(itemRowDTOs));
    }

    public String getCustomerName(String customerId) {
        return customerResource.getByToken(customerId, UserContextManager.getCurrent().getRsqeToken()).name;
    }


    private List<OfferDetailsDTO.ItemRowDTO> addOfferItems(List<LineItemModel> offerItems) {
        List<OfferDetailsDTO.ItemRowDTO> itemRowDTOs = new ArrayList<OfferDetailsDTO.ItemRowDTO>();

        for (LineItemModel offerItem : offerItems) {
            OfferItemDtoLineItemVisitor visitor = new OfferItemDtoLineItemVisitor(itemRowDTOs);
            offerItem.accept(visitor);
        }
        return itemRowDTOs;
    }

    public PageView buildDetailsView(String projectId, String quoteOptionId, String offerId, List breadCrumbs) {
        OfferDTO offerDTO = quoteOptionOfferFacade.getOffer(projectId, quoteOptionId, offerId);
        return new PageView("Offer Details", offerDTO.name, breadCrumbs);
    }

    public OfferDetailsTabView buildDetailsTabView(String customerId, String contractId, String projectId, String quoteOptionId, String offerId) {
        final OfferDetailsModel offerDetails = quoteOptionOfferFacade.getOfferDetails(projectId, quoteOptionId, offerId);
        final String customerName = getCustomerName(customerId);
        return new OfferDetailsTabView(customerId, contractId, projectId, quoteOptionId, offerDetails, customerName, UriFactoryImpl.exportPricingSheet(customerId, contractId, projectId, quoteOptionId, offerId).toString());
    }
}
