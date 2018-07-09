package com.bt.rsqe.projectengine.web.facades;


import com.bt.rsqe.domain.project.OfferStatus;
import com.bt.rsqe.projectengine.OfferDTO;
import com.bt.rsqe.projectengine.OfferResource;
import com.bt.rsqe.projectengine.ProjectResource;
import com.bt.rsqe.projectengine.QuoteOptionItemDTO;
import com.bt.rsqe.projectengine.web.OfferAndOrderValidator;
import com.bt.rsqe.projectengine.web.model.OfferDetailsModel;
import com.bt.rsqe.projectengine.web.model.modelfactory.OfferDetailsModelFactory;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import org.joda.time.DateTime;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.collect.Lists.*;

public class QuoteOptionOfferFacade {
    private final ProjectResource projects;
    private OfferDetailsModelFactory offerDetailsModelFactory;

    public QuoteOptionOfferFacade(ProjectResource projects, OfferDetailsModelFactory offerDetailsModelFactory) {
        this.projects = projects;
        this.offerDetailsModelFactory = offerDetailsModelFactory;
    }

    public OfferDTO getOffer(String projectId, String quoteOptionId, String offerId) {
        return projects.quoteOptionResource(projectId).quoteOptionOfferResource(quoteOptionId).get(offerId);
    }

    public OfferDetailsModel getOfferDetails(String projectId, String quoteOptionId, String offerId) {
        return offerDetailsModelFactory.create(getOffer(projectId, quoteOptionId, offerId));
    }

    private OfferResource offerResource(String projectId, String quoteOptionId) {
        return projects.quoteOptionResource(projectId).quoteOptionOfferResource(quoteOptionId);
    }

    public OfferDTO createOffer(String projectId, String quoteOptionId, String offerName, List<String> lineItems, String customerOrderReference) {
        final ArrayList<QuoteOptionItemDTO> offerItemDTOs = new ArrayList<QuoteOptionItemDTO>();
        for (String lineItem : lineItems) {
            offerItemDTOs.add(QuoteOptionItemDTO.fromId(lineItem));
        }
        final OfferDTO offer = OfferDTO.newInstance(offerName, new DateTime().toString(), OfferStatus.ACTIVE.toString(), offerItemDTOs, customerOrderReference);
        return projects.quoteOptionResource(projectId).quoteOptionOfferResource(quoteOptionId).post(offer);
    }

    public void approve(String projectId, String quoteOptionId, String offerId) {
        offerResource(projectId, quoteOptionId).approve(offerId);
    }

    public void reject(String projectId, String quoteOptionId, String offerId) {
        offerResource(projectId, quoteOptionId).reject(offerId);
    }

    public List<OfferDetailsModel> get(String projectId, String quoteOptionId) {
        final List<OfferDTO> offerDTOs = offerResource(projectId, quoteOptionId).get();
        return newArrayList(Iterables.transform(offerDTOs, new Function<OfferDTO, OfferDetailsModel>() {
            @Override
            public OfferDetailsModel apply(@Nullable OfferDTO input) {
                return offerDetailsModelFactory.create(input);
            }
        }));
    }

    public void cancelOfferApproval(String projectId, String quoteOptionId, String offerId) {
        offerResource(projectId, quoteOptionId).cancelOfferApproval(offerId);
    }
}
