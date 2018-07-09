package com.bt.rsqe.projectengine.web.resourcestubs;

import com.bt.rsqe.projectengine.QuoteOptionDTO;
import com.bt.rsqe.projectengine.QuoteOptionResource;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.google.common.collect.Lists.*;
import static com.google.common.collect.Maps.*;

public class QuoteOptionResourceStub extends QuoteOptionResource {

    private Map<String, QuoteOptionDTO> quoteOptions = newHashMap();
    private Map<String, QuoteOptionItemResourceStub> quoteOptionItemResourceStubs = newHashMap();
    private Map<String, QuoteOptionOfferResourceStub> quoteOptionOfferResourceStubs = newHashMap();
    private Map<String, QuoteOptionOrderResourceStub> quoteOptionOrderResourceStubs = newHashMap();

    QuoteOptionResourceStub() {
        super(URI.create(""), "");
    }

    public QuoteOptionResourceStub with(QuoteOptionDTO quoteOption) {
        quoteOptions.put(quoteOption.friendlyQuoteId, quoteOption);
        return this;
    }


    @Override
    public void post(String friendlyId, String name, String currency, String contractTerm, String createdBy) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void put(String friendlyId, String id, String name, String currency, String contractTerm, String createdBy) {
        throw new UnsupportedOperationException();
    }

    @Override
    public QuoteOptionItemResourceStub quoteOptionItemResource(String quoteOptionId) {
        if (!quoteOptionItemResourceStubs.containsKey(quoteOptionId)) {
            quoteOptionItemResourceStubs.put(quoteOptionId, new QuoteOptionItemResourceStub());
        }
        return quoteOptionItemResourceStubs.get(quoteOptionId);
    }

    @Override
    public QuoteOptionOfferResourceStub quoteOptionOfferResource(String quoteOptionId) {
        if (!quoteOptionOfferResourceStubs.containsKey(quoteOptionId)) {
            quoteOptionOfferResourceStubs.put(quoteOptionId, new QuoteOptionOfferResourceStub());
        }
        return quoteOptionOfferResourceStubs.get(quoteOptionId);
    }

    @Override
    public QuoteOptionOrderResourceStub quoteOptionOrderResource(String quoteOptionId) {
        if (!quoteOptionOrderResourceStubs.containsKey(quoteOptionId)) {
            quoteOptionOrderResourceStubs.put(quoteOptionId, new QuoteOptionOrderResourceStub());
        }
        return quoteOptionOrderResourceStubs.get(quoteOptionId);
    }

    @Override
    public List<QuoteOptionDTO> get() {
        return newArrayList(quoteOptions.values());

    }

    @Override
    public QuoteOptionDTO get(String id) {
        final QuoteOptionDTO quoteOption = quoteOptions.get(id);
        if (quoteOption == null) {
            throw new com.bt.rsqe.web.rest.exception.ResourceNotFoundException();
        } else {
            return quoteOption;
        }
    }


    @Override
    public void put(QuoteOptionDTO quoteOptionDTO) {
        if (quoteOptionDTO.id != null && quoteOptions.containsKey(quoteOptionDTO.id)) {
            quoteOptions.put(quoteOptionDTO.id, quoteOptionDTO);
        } else {
            post(quoteOptionDTO);
        }
    }

    @Override
    public void post(QuoteOptionDTO quoteOptionDTO) {
        quoteOptionDTO.id = UUID.randomUUID().toString();
        quoteOptions.put(quoteOptionDTO.id, quoteOptionDTO);
    }
}
