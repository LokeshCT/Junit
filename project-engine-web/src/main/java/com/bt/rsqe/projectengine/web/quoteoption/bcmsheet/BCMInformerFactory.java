package com.bt.rsqe.projectengine.web.quoteoption.bcmsheet;

import com.bt.rsqe.customerinventory.client.ProductInstanceClient;
import com.bt.rsqe.customerrecord.ExpedioClientResources;
import com.bt.rsqe.expedio.project.ExpedioProjectResource;
import com.bt.rsqe.projectengine.ProjectResource;
import com.bt.rsqe.projectengine.web.facades.CustomerFacade;
import com.bt.rsqe.projectengine.web.facades.QuoteOptionFacade;

public class BCMInformerFactory {
    private final QuoteOptionFacade quoteOptionFacade;
    private final ExpedioProjectResource expedioProjectsResource;
    private final CustomerFacade customerFacade;
    private ExpedioClientResources expedioClientResources;
    private ProjectResource projectResource;
    private ProductInstanceClient futureProductInstanceClient;

    public BCMInformerFactory(final QuoteOptionFacade quoteOptionFacade,
                              final ExpedioProjectResource expedioProjectsResource,
                              final CustomerFacade customerFacade,
                              final ExpedioClientResources expedioClientResources,
                              final ProjectResource projectResource,
                              final ProductInstanceClient futureProductInstanceClient) {
        this.quoteOptionFacade = quoteOptionFacade;
        this.expedioProjectsResource = expedioProjectsResource;
        this.customerFacade = customerFacade;
        this.expedioClientResources = expedioClientResources;
        this.projectResource = projectResource;
        this.futureProductInstanceClient = futureProductInstanceClient;
    }

    public BCMInformer informerFor(final String customerId,
                                   final String contractId,
                                   final String projectId,
                                   final String quoteOptionId, String bcmExportType) {
        return new BCMInformer(quoteOptionFacade,
                               expedioProjectsResource,
                               customerFacade,
                               expedioClientResources,
                               projectResource,
                               futureProductInstanceClient,
                               customerId,
                               contractId,
                               projectId,
                               quoteOptionId, bcmExportType);
    }
}
