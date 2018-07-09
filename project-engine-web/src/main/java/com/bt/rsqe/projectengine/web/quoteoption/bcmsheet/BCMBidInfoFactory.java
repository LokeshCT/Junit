package com.bt.rsqe.projectengine.web.quoteoption.bcmsheet;

import com.bt.rsqe.customerrecord.CustomerDTO;
import com.bt.rsqe.expedio.project.ProjectDTO;
import com.bt.rsqe.projectengine.QuoteOptionDTO;

import static org.apache.commons.lang.StringUtils.*;

public class BCMBidInfoFactory {
    public BCMBidInfo create(BCMInformer bcmInformer) {
        final ProjectDTO expedioProject = bcmInformer.getProject();
        final CustomerDTO expedioCustomer = bcmInformer.getCustomer();
        final QuoteOptionDTO quoteOptionDTO = bcmInformer.getQuoteOption();
        final String offerName =  bcmInformer.getOfferName();

        return new BCMBidInfo(bcmInformer.getProjectId(),
                              bcmInformer.getQuoteOptionId(),
                              quoteOptionDTO.getCurrency(),
                              expedioProject.siebelId,
                              expedioProject.bidNumber,
                              expedioProject.salesRepName,
                              expedioProject.tradeLevel,
                              expedioCustomer.getName(),
                              expedioCustomer.getSalesChannel(),
                              Integer.valueOf(quoteOptionDTO.contractTerm),
                              isNotEmpty(offerName) ? offerName : null,
                              expedioCustomer.getSalesChannelType(), expedioProject.expRef);
    }
}