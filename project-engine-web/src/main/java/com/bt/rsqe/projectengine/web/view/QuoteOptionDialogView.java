package com.bt.rsqe.projectengine.web.view;

import com.bt.rsqe.domain.product.constraints.AttributeValue;
import com.bt.rsqe.projectengine.QuoteOptionContractTerm;
import com.bt.rsqe.projectengine.QuoteOptionCurrency;
import com.bt.rsqe.projectengine.QuoteOptionDTO;
import com.bt.rsqe.projectengine.web.uri.UriFactoryImpl;
import com.google.common.base.Optional;

import java.util.List;

public class QuoteOptionDialogView {
    private String customerId;
    private String projectId;
    private String formAction;
    private QuoteOptionDTO quoteOption = new QuoteOptionDTO();
    private Optional<List<AttributeValue>> allowedValues;

    public QuoteOptionDialogView(String customerId, String contractId, String projectId) {
        this.customerId = customerId;
        this.projectId = projectId;
        formAction = UriFactoryImpl.quoteOptions(customerId, contractId, projectId).toString();
    }

    public QuoteOptionDialogView(String customerId, String contractId, String projectId, QuoteOptionDTO dto) {
        this(customerId, contractId, projectId);
        this.quoteOption = dto;
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getProjectId() {
        return projectId;
    }

    public String getFormAction() {
        return formAction;
    }

    public QuoteOptionCurrency[] getCurrencies() {
        return QuoteOptionCurrency.values();
    }

    public QuoteOptionContractTerm[] getContractTerms() {
        return QuoteOptionContractTerm.values();
    }

    public String getQuoteOptionId() {
        return quoteOption.id;
    }

    public String getExpedioQuoteOptionId() {
        return quoteOption.friendlyQuoteId;
    }

    public String getName() {
        return quoteOption.name;
    }

    public String getCurrency() {
        return quoteOption.currency;
    }

    public String getContractTerm() {
        return quoteOption.contractTerm;
    }

    public String getUpdateCurrency() {
        return quoteOption.isHasLineItems() ? "disabled" : "";
    }
}
