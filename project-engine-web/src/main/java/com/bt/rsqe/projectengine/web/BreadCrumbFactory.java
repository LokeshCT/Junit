package com.bt.rsqe.projectengine.web;

import com.bt.rsqe.projectengine.ProjectDTO;
import com.bt.rsqe.projectengine.ProjectResource;
import com.bt.rsqe.projectengine.QuoteOptionDTO;
import com.bt.rsqe.projectengine.QuoteOptionResource;
import com.bt.rsqe.projectengine.web.uri.UriFactoryImpl;

import java.util.List;

import static com.google.common.collect.Lists.*;

public class BreadCrumbFactory {

    private ProjectResource projects;

    private BreadCrumbFactory(ProjectResource projects) {
        this.projects = projects;
    }

    public static BreadCrumbFactory getInstance(ProjectResource projects) {
        return new BreadCrumbFactory(projects);
    }

    public List<BreadCrumb> createBreadCrumbsForQuoteOptionResource(String projectId) {
        List<BreadCrumb> breadCrumbs = newArrayList();
        final ProjectDTO projectDTO = projects.get(projectId);
        breadCrumbs.add(new BreadCrumb(UriFactoryImpl.projects(projectDTO.customerId, projectDTO.contractId, projectId), "Quote Options"));
        return breadCrumbs;
    }

    public List<BreadCrumb> createBreadCrumbsForOfferResource(String projectId, String quoteOptionId) {
        List<BreadCrumb> breadCrumbs = newArrayList();
        final ProjectDTO projectDTO = projects.get(projectId);
        breadCrumbs.add(new BreadCrumb(UriFactoryImpl.projects(projectDTO.customerId, projectDTO.contractId, projectId), "Quote Options"));

        final QuoteOptionResource quoteOptionResource = projects.quoteOptionResource(projectId);
        final QuoteOptionDTO quoteOptionDTO = (quoteOptionResource.get(quoteOptionId));
        breadCrumbs.add(new BreadCrumb(UriFactoryImpl.quoteOption(projectDTO.customerId, projectDTO.contractId, projectId, quoteOptionId), "Quote Option Details"));

        return breadCrumbs;
    }
}
