package com.bt.rsqe.projectengine.web.model.modelfactory;

import com.bt.rsqe.projectengine.QuoteOptionDTO;
import com.bt.rsqe.projectengine.QuoteOptionItemDTO;
import com.bt.rsqe.projectengine.web.model.LineItemModel;
import com.bt.rsqe.projectengine.web.quoteoptionpricing.PriceSuppressStrategy;

import java.util.List;

public interface LineItemModelFactory {
    LineItemModel create(String projectId, String quoteOptionId, String customerId, String contractId, QuoteOptionItemDTO dto, PriceSuppressStrategy priceSuppressStrategy,QuoteOptionDTO quoteOptionDTO);
    List<LineItemModel> create(final String projectId, final String quoteOptionId, final String customerId, String contractId, List<QuoteOptionItemDTO> lineItems);
}
