package com.bt.rsqe.projectengine.web.resourcestubs;

import com.bt.rsqe.projectengine.OfferDTO;
import com.bt.rsqe.projectengine.OfferResource;
import com.bt.rsqe.web.rest.exception.ResourceNotFoundException;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.google.common.collect.Lists.*;
import static com.google.common.collect.Maps.*;

public class QuoteOptionOfferResourceStub extends OfferResource {
    private Map<String, OfferDTO> offers = newHashMap();

    QuoteOptionOfferResourceStub() {
        super(URI.create(""));
    }

    public QuoteOptionOfferResourceStub with(OfferDTO offer) {
        offers.put(offer.id, offer);
        return this;
    }

    @Override
    public List<OfferDTO> get() {
        return newArrayList(offers.values());

    }

    @Override
    public OfferDTO get(String id) {
        final OfferDTO offer = offers.get(id);
        if (offer == null) {
            throw new ResourceNotFoundException();
        } else {
            return offer;
        }
    }

    @Override
    public OfferDTO post(OfferDTO resource) {
        resource.id = UUID.randomUUID().toString();
        offers.put(resource.id, resource);
        return resource;
    }

    public List<OfferDTO> storedOfferList() {
        return newArrayList(offers.values());
    }
}
