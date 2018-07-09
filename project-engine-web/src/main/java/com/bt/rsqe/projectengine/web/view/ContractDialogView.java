package com.bt.rsqe.projectengine.web.view;

import com.bt.rsqe.domain.PriceBookDTO;
import com.bt.rsqe.domain.ContractDTO;
import com.bt.rsqe.domain.QuoteOptionItemStatus;
import com.bt.rsqe.projectengine.web.uri.UriFactoryImpl;
import com.google.common.base.Function;
import org.apache.commons.lang.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static com.google.common.collect.Collections2.transform;
import static com.google.common.collect.Sets.*;

public class ContractDialogView {
    private String customerId;
    private String quoteOptionItemId;
    private String quoteOptionId;
    private String projectId;
    private String formAction;
    private ContractDTO contract;
    private QuoteOptionItemStatus quoteOptionItemStatus;
    private List<QuoteOptionItemStatus> PRICE_BOOK_EDITABLE_ON = Arrays.asList(QuoteOptionItemStatus.DRAFT,
                                                                               QuoteOptionItemStatus.FAILED);

    public ContractDialogView(String customerId, String contractId, String projectId, String quoteOptionId, String quoteOptionItemId) {
        this.customerId = customerId;
        this.quoteOptionId = quoteOptionId;
        this.quoteOptionItemId = quoteOptionItemId;
        this.projectId = projectId;
        formAction = UriFactoryImpl.contract(customerId, contractId, projectId, quoteOptionId, quoteOptionItemId).toString();
    }

    public ContractDialogView(String customerId, String contractId, String projectId, String quoteOptionId, String quoteOptionItemId,
                              ContractDTO dto, QuoteOptionItemStatus quoteOptionItemStatus) {
        this(customerId, contractId, projectId, quoteOptionId, quoteOptionItemId);
        this.contract = dto;
        this.quoteOptionItemStatus = quoteOptionItemStatus;
    }

    public String getProjectId() {
        return projectId;
    }

    public String getQuoteOptionId() {
        return quoteOptionId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getFormAction() {
        return formAction;
    }

    public String getQuoteOptionItemId() {
        return quoteOptionItemId;
    }

    public String getContractTerm() {
        return contract.term;
    }

    public Set<String> getEupPriceBooks() {
        return newHashSet(transform(contract.priceBooks, new Function<PriceBookDTO, String>() {
            @Override
            public String apply(PriceBookDTO priceBookDTO) {
                return priceBookDTO.eupPriceBook;
            }
        }));
    }

    public Set<String> getPtpPriceBooks() {
        return newHashSet(transform(contract.priceBooks, new Function<PriceBookDTO, String>() {
            @Override
            public String apply(PriceBookDTO priceBookDTO) {
                return priceBookDTO.ptpPriceBook;
            }
        }));
    }

    public String getPriceBookEditable() {
        if(PRICE_BOOK_EDITABLE_ON.contains(quoteOptionItemStatus)) {
            return StringUtils.EMPTY;
        }
        return "disabled";
    }

    public String getEupPriceBook() {
        return contract.priceBooks.get(0).eupPriceBook;
    }

    public String getPtpPriceBook() {
        return contract.priceBooks.get(0).ptpPriceBook;
    }
}
