package com.bt.rsqe.projectengine.web.facades;

import com.bt.rsqe.customerinventory.parameter.LineItemId;
import com.bt.rsqe.projectengine.ProjectResource;
import com.bt.rsqe.projectengine.QuoteOptionDTO;
import com.bt.rsqe.projectengine.QuoteOptionItemDTO;
import com.bt.rsqe.projectengine.QuoteOptionItemResource;
import com.bt.rsqe.domain.QuoteOptionItemStatus;
import com.bt.rsqe.projectengine.web.model.LineItemModel;
import com.bt.rsqe.projectengine.web.model.modelfactory.LineItemModelFactory;
import com.bt.rsqe.projectengine.web.quoteoptionpricing.PriceSuppressStrategy;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;

import static com.bt.rsqe.projectengine.LineItemDiscountStatus.*;
import static com.google.common.collect.Lists.*;

public class LineItemFacade {

    public ProjectResource projectResource;
    private LineItemModelFactory lineItemModelFactory;

    public LineItemFacade(ProjectResource projectResource, LineItemModelFactory lineItemModelFactory) {

        this.projectResource = projectResource;
        this.lineItemModelFactory = lineItemModelFactory;
    }

    private List<QuoteOptionItemDTO> fetch(String projectId, String quoteOptionId, boolean withFailedLineItems) {
        QuoteOptionItemResource quoteOptionItemResource = projectResource.quoteOptionResource(projectId).quoteOptionItemResource(quoteOptionId);

        final List<QuoteOptionItemDTO> quoteOptionItemDTOs = quoteOptionItemResource.get();
        if (withFailedLineItems) {
            return quoteOptionItemDTOs;
        }
        final List<QuoteOptionItemDTO> withoutFailedItems = newArrayList();
        for (QuoteOptionItemDTO quoteOptionItemDTO : quoteOptionItemDTOs) {
            if (quoteOptionItemDTO.status != QuoteOptionItemStatus.FAILED) {
                withoutFailedItems.add(quoteOptionItemDTO);
            }
        }
        return withoutFailedItems;
    }

    public List<LineItemModel> fetchLineItems(String customerId, String contractId, String projectId, String quoteOptionId, PriceSuppressStrategy priceSuppressStrategy) {
        return fetchLineItems(customerId, contractId, projectId, quoteOptionId, null, priceSuppressStrategy);
    }

    public List<LineItemModel> fetchLineItems(String customerId, String contractId, String projectId, String quoteOptionId, String productCode, PriceSuppressStrategy priceSuppressStrategy) {
        return fetchLineItems(customerId, contractId, projectId, quoteOptionId, productCode, false, priceSuppressStrategy);
    }

    public List<LineItemModel> fetchLineItems(String customerId, String contractId, String projectId, String quoteOptionId, String productCode, boolean withFailedLineItems, PriceSuppressStrategy priceSuppressStrategy) {
        List<LineItemModel> lineItemModels = new ArrayList<LineItemModel>();
        QuoteOptionDTO quoteOptionDTO = projectResource.quoteOptionResource(projectId).get(quoteOptionId);
        for (QuoteOptionItemDTO dto : fetch(projectId, quoteOptionId, withFailedLineItems)) {
            if (productCode == null || productCode.equals(dto.sCode)) {
                lineItemModels.add(lineItemModelFactory.create(projectId, quoteOptionId, customerId, contractId, dto, priceSuppressStrategy, quoteOptionDTO));
            }
        }
        return lineItemModels;
    }

    public List<LineItemModel> fetchVisibleLineItems(String customerId, String contractId, String projectId, String quoteOptionId, String productCode, boolean withFailedLineItems, PriceSuppressStrategy priceSuppressStrategy) {
        return newArrayList(Iterables.filter(fetchLineItems(customerId, contractId, projectId, quoteOptionId, productCode, withFailedLineItems, priceSuppressStrategy), new Predicate<LineItemModel>() {
            @Override
            public boolean apply(LineItemModel lineItemModel) {
                return lineItemModel.isProductVisibleOnlineSummary() && lineItemModel.isInFrontCatlogueProduct();
            }
        }));

    }

    public List<LineItemId> fetchLineItemIds(String projectId, String quoteOptionId) {
        return fetchLineItemIds(projectId, quoteOptionId, null);
    }

    public List<LineItemId> fetchLineItemIds(String projectId, String quoteOptionId, String productCode) {
        List<QuoteOptionItemDTO> quoteOptionItemDTOs = this.fetch(projectId, quoteOptionId, false);
        List<LineItemId> lineItemIds = new ArrayList<LineItemId>();
        for (QuoteOptionItemDTO quoteOptionItemDTO : quoteOptionItemDTOs) {
            if (productCode == null || productCode.equals(quoteOptionItemDTO.sCode)) {
                lineItemIds.add(new LineItemId(quoteOptionItemDTO.id));
            }
        }
        return lineItemIds;
    }

    //todo: move this to  the REST layer
    public void approveDiscounts(String projectId, String quoteOptionId, List<LineItemId> lineItemIds) {
        QuoteOptionItemResource quoteOptionItemResource = projectResource.quoteOptionResource(projectId).quoteOptionItemResource(quoteOptionId);
        for (LineItemId lineItemId : lineItemIds) {
            final QuoteOptionItemDTO quoteOptionItemDTO = quoteOptionItemResource.get(lineItemId.toString());
            quoteOptionItemDTO.discountStatus = APPROVED;
            if(QuoteOptionItemStatus.COMMERCIAL_NON_STANDARD_REQUESTED.getDescription().equalsIgnoreCase(quoteOptionItemDTO.status.getDescription())){
               quoteOptionItemDTO.status = QuoteOptionItemStatus.COMMERCIAL_NON_STANDARD_APPROVED;
            }
            quoteOptionItemResource.put(quoteOptionItemDTO);
        }
    }

    //todo: move this to  the REST layer
    public void rejectDiscounts(String projectId, String quoteOptionId, List<LineItemId> lineItemIds) {
        QuoteOptionItemResource quoteOptionItemResource = projectResource.quoteOptionResource(projectId).quoteOptionItemResource(quoteOptionId);
        for (LineItemId lineItemId : lineItemIds) {
            final QuoteOptionItemDTO quoteOptionItemDTO = quoteOptionItemResource.get(lineItemId.toString());
            if(quoteOptionItemDTO.discountStatus.equals(APPROVAL_REQUESTED)){
                quoteOptionItemDTO.discountStatus = REJECTED;
            }
            if(QuoteOptionItemStatus.COMMERCIAL_NON_STANDARD_REQUESTED.getDescription().equalsIgnoreCase(quoteOptionItemDTO.status.getDescription())){
               quoteOptionItemDTO.status = QuoteOptionItemStatus.COMMERCIAL_NON_STANDARD_REJECTED;
            }
            quoteOptionItemResource.put(quoteOptionItemDTO);
        }
    }

    public void persistMinimumRevenueCommitment(String projectId, String quoteOptionId, LineItemId lineItemId, String minimumRevenueCommitment, String triggerMonths) {
        final QuoteOptionItemResource quoteOptionItemResource = projectResource.quoteOptionResource(projectId).quoteOptionItemResource(quoteOptionId);
        QuoteOptionItemDTO itemDTO = quoteOptionItemResource.get(lineItemId.toString());
        String monthlyRevenue = itemDTO.contractDTO.priceBooks.get(0).monthlyRevenue;
        if (!StringUtils.isEmpty(minimumRevenueCommitment)) {
            if (!minimumRevenueCommitment.equals(monthlyRevenue)) {
                itemDTO.contractDTO.priceBooks.get(0).monthlyRevenue = minimumRevenueCommitment;
            }
            if (!StringUtils.isEmpty(triggerMonths) && !triggerMonths.equals(itemDTO.contractDTO.priceBooks.get(0).triggerMonths)) {
                itemDTO.contractDTO.priceBooks.get(0).triggerMonths = triggerMonths;
            }
            quoteOptionItemResource.put(itemDTO);
        }
    }
}
