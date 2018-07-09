package com.bt.rsqe.projectengine.web.quoteoptiondetails;

import com.bt.rsqe.client.Pmr;
import com.bt.rsqe.customerinventory.client.ProductInstanceClient;
import com.bt.rsqe.customerinventory.driver.SiteDriver;
import com.bt.rsqe.customerrecord.ExpedioClientResources;
import com.bt.rsqe.enums.ProductAction;
import com.bt.rsqe.expedio.project.ExpedioProjectResource;
import com.bt.rsqe.projectengine.web.facades.ProductIdentifierFacade;
import com.bt.rsqe.projectengine.web.facades.QuoteOptionFacade;
import com.bt.rsqe.projectengine.web.facades.SiteFacade;
import com.bt.rsqe.projectengine.web.quoteoption.validation.SiteValidator;
import com.bt.rsqe.projectengine.web.uri.UriFactory;

public class ProductOrchestratorFactory {
    ProductOrchestrator add, modify, move, migrate, selectNewSite;

    public ProductOrchestratorFactory(SiteFacade siteFacade,
                                      ProductIdentifierFacade productIdentifierFacade,
                                      UriFactory productConfiguratorUriFactory,
                                      QuoteOptionFacade quoteOptionFacade,
                                      Pmr pmr,
                                      ExpedioClientResources expedioClientResources,
                                      ExpedioProjectResource expedioProjectResource,
                                      ProductInstanceClient productInstanceClient,
                                      SiteDriver siteDriver,
                                      SiteValidator siteValidator) {
        add = new AddProductOrchestrator(siteFacade,
                                         productIdentifierFacade,
                                         productConfiguratorUriFactory,
                                         quoteOptionFacade,
                                         expedioProjectResource,
                                         pmr,
                                         expedioClientResources,
                                         productInstanceClient,
                                         siteValidator);

        modify = new ModifyProductOrchestrator(siteFacade,
                                               productIdentifierFacade,
                                               productConfiguratorUriFactory,
                                               quoteOptionFacade,
                                               expedioProjectResource,
                                               pmr,
                                               siteDriver,
                                               expedioClientResources, productInstanceClient);

        migrate = new MigrateProductOrchestrator(siteFacade,
                                                 productIdentifierFacade,
                                                 productConfiguratorUriFactory,
                                                 quoteOptionFacade,
                                                 expedioProjectResource,
                                                 pmr,
                                                 siteDriver,
                                                 expedioClientResources);

        move = new MoveProductOrchestrator(siteFacade,
                                           productIdentifierFacade,
                                           productConfiguratorUriFactory,
                                           quoteOptionFacade,
                                           expedioProjectResource,
                                           pmr,
                                           siteDriver,
                                           expedioClientResources,
                                           productInstanceClient);

        selectNewSite = new SelectNewSiteProductOrchestrator(siteFacade,
                                                             productIdentifierFacade,
                                                             productConfiguratorUriFactory,
                                                             quoteOptionFacade,
                                                             expedioProjectResource,
                                                             pmr,
                                                             expedioClientResources);
    }

    public ProductOrchestrator getOrchestratorFor(ProductAction action) {
        switch(action) {
            case Provide: return add;
            case Modify: return modify;
            case Migrate: return migrate;
            case Move: return move;
            case SelectNewSite: return selectNewSite;

            default: throw new IllegalArgumentException(String.format("No ProductOrchestrator found for action %s", action.description()));
        }
    }
}
