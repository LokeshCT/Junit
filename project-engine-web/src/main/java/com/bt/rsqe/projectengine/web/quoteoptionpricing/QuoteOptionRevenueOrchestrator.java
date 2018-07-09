package com.bt.rsqe.projectengine.web.quoteoptionpricing;

import com.bt.rsqe.customerinventory.parameter.LineItemId;
import com.bt.rsqe.domain.PriceBookDTO;
import com.bt.rsqe.projectengine.web.facades.LineItemFacade;
import com.bt.rsqe.projectengine.web.facades.PriceBookFacade;
import com.bt.rsqe.projectengine.web.model.LineItemModel;
import com.bt.rsqe.projectengine.web.quoteoption.util.ProductCategoryFilter;
import com.bt.rsqe.projectengine.web.view.QuoteOptionRevenueDTO;
import com.bt.rsqe.projectengine.web.view.pagination.Pagination;

import java.util.List;

import static com.bt.rsqe.utils.AssertObject.*;
import static com.google.common.collect.Lists.*;

public class QuoteOptionRevenueOrchestrator {

    LineItemFacade lineItemFacade;
    private PriceBookFacade priceBookFacade;

    public QuoteOptionRevenueOrchestrator(LineItemFacade lineItemFacade, PriceBookFacade priceBookFacade) {
        this.lineItemFacade = lineItemFacade;
        this.priceBookFacade = priceBookFacade;
    }

    public QuoteOptionRevenueDTO getRevenueFor(String customerId, String contractId, String projectId, String quoteOptionId, Pagination pagination){
        List<QuoteOptionRevenueDTO.ItemRowDTO> revenueDTOList = newArrayList();
        List<LineItemModel> lineItemModels = lineItemFacade.fetchLineItems(customerId, contractId, projectId, quoteOptionId, PriceSuppressStrategy.None);
        for(LineItemModel item : filterByCategory(lineItemModels)){
            PriceBookDTO priceBookDTO = priceBookFacade.getLatestPriceBookForIndirectUser(item.customerId(), item.getProductSCode(), item.getProductCategoryCode());
            if(isNotNull(priceBookDTO)){
                revenueDTOList.add(toRevenueDTO(priceBookDTO,item));
            }
        }
        int size = revenueDTOList.size();
        return new QuoteOptionRevenueDTO(pagination.paginate(revenueDTOList),pagination.getPageNumber(), size, size);

    }

    public void persistRevenueDetails(String projectId, String quoteOptionId, String customerId, String contractId, QuoteOptionRevenueDTO dto) {
        List<LineItemModel> lineItemModels = lineItemFacade.fetchLineItems(customerId, contractId, projectId, quoteOptionId, PriceSuppressStrategy.None);
        for(QuoteOptionRevenueDTO.ItemRowDTO revenueDTO: dto.getItemDTOs()){
            for(LineItemModel itemModel :lineItemModels){
                if(itemModel.getProductCategoryName().equalsIgnoreCase(revenueDTO.productCategoryName)){
                    persist(projectId,quoteOptionId,itemModel.getLineItemId(),revenueDTO);
                }
            }
        }
    }


    private void persist(String projectId, String quoteOptionId, LineItemId lineItemId, QuoteOptionRevenueDTO.ItemRowDTO revenueDTO) {
        lineItemFacade.persistMinimumRevenueCommitment(projectId,quoteOptionId,lineItemId,String.valueOf(revenueDTO.proposedRevenue),String.valueOf(revenueDTO.triggerMonths));
    }

    private QuoteOptionRevenueDTO.ItemRowDTO toRevenueDTO(PriceBookDTO priceBookDTO, LineItemModel item) {
        return new QuoteOptionRevenueDTO.ItemRowDTO(priceBookDTO.id, priceBookDTO.monthlyRevenue, "", priceBookDTO.triggerMonths,item.getProductCategoryName());
    }

    private List<LineItemModel> filterByCategory(List<LineItemModel> lineItemModels) {
        return new ProductCategoryFilter(lineItemModels).filterLineItemsBasedOnProductCategoryCode();
    }
}
