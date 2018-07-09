package com.bt.rsqe.projectengine.web.facades;

import com.bt.rsqe.projectengine.ProjectResource;
import com.bt.rsqe.projectengine.QuoteOptionDTO;
import com.bt.rsqe.projectengine.QuoteOptionItemDTO;

import java.util.List;

public class QuoteOptionFacade {

    private final ProjectResource projects;

    public QuoteOptionFacade(ProjectResource projects) {
        this.projects = projects;
    }

    public List<QuoteOptionDTO> getAll(String projectId) {
        return projects.quoteOptionResource(projectId).get();
    }

    public QuoteOptionDTO get(String projectId, String quoteOptionId) {
        return projects.quoteOptionResource(projectId).get(quoteOptionId);
    }

    public List<QuoteOptionItemDTO> getAllQuoteOptionItem(String projectId, String quoteOptionId) {
        return projects.quoteOptionResource(projectId).quoteOptionItemResource(quoteOptionId).get();
    }

    public void putDiscountRequest(String projectId, String quoteOptionId) {
        projects.quoteOptionResource(projectId).discountRequest(quoteOptionId);
    }

    public void unlockApprovedPriceLines(String projectId, String quoteOptionId) {
        projects.quoteOptionResource(projectId).quoteOptionItemResource(quoteOptionId).unlockApprovedPriceLines(quoteOptionId);
}

}
