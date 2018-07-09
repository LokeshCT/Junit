package com.bt.rsqe.projectengine.web.resourcestubs;

import com.bt.rsqe.domain.ContractDTO;
import com.bt.rsqe.domain.product.parameters.ProductCategoryCode;
import com.bt.rsqe.projectengine.QuoteOptionItemDTO;
import com.bt.rsqe.projectengine.QuoteOptionItemResource;
import com.bt.rsqe.web.rest.exception.ResourceNotFoundException;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.google.common.collect.Lists.*;
import static com.google.common.collect.Maps.*;

public class QuoteOptionItemResourceStub extends QuoteOptionItemResource {

    private Map<String, QuoteOptionItemDTO> items = newHashMap();

    QuoteOptionItemResourceStub() {
        super(URI.create(""));
    }

    public QuoteOptionItemResourceStub with(QuoteOptionItemDTO item) {
        items.put(item.id, item);
        return this;
    }

    public Map<String, QuoteOptionItemDTO> storedItemMap() {
        return items;
    }

    public List<QuoteOptionItemDTO> storedItemList() {
        return newArrayList(items.values());
    }

    public QuoteOptionItemDTO getItem(String id) {
        return items.get(id);
    }

    @Override
    public List<QuoteOptionItemDTO> get() {
        return newArrayList(items.values());
    }

    @Override
    public QuoteOptionItemDTO get(String lineItemId) {
        final QuoteOptionItemDTO item = items.get(lineItemId);
        if (item == null) {
            throw new ResourceNotFoundException();
        } else {
            return item;
        }
    }


    @Override
    public  QuoteOptionItemDTO put(QuoteOptionItemDTO resource) {
        final QuoteOptionItemDTO item = resource;
        if (item.id != null && items.containsKey(item.id)) {
            items.put(item.id, item);
            return item;
        } else {
            return post(item);
        }
    }

    @Override
    public QuoteOptionItemDTO post(QuoteOptionItemDTO resource) {
        resource.id = UUID.randomUUID().toString();
        items.put(resource.id, resource);
        return resource;
    }

    @Override
    public QuoteOptionItemDTO post(String sCode, String action, String offerId, String contractTerm, String orderId, ContractDTO contractDTO, ProductCategoryCode productCategoryCode, boolean isBundleProduct) {
        throw new UnsupportedOperationException();
    }

}
