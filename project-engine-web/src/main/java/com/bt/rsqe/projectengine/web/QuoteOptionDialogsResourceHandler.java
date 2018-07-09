package com.bt.rsqe.projectengine.web;

import com.bt.rsqe.projectengine.web.facades.ProductIdentifierFacade;
import com.bt.rsqe.projectengine.web.facades.QuoteOptionFacade;
import com.bt.rsqe.projectengine.web.uri.UriFactory;
import com.bt.rsqe.projectengine.web.view.BulkTemplateDialogView;
import com.bt.rsqe.web.Presenter;
import com.bt.rsqe.web.ViewFocusedResourceHandler;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/rsqe/customers/{customerId}/contracts/{contractId}/projects/{projectId}/quote-options/{quoteOptionId}/dialogs")
@Produces(MediaType.TEXT_HTML)
public class QuoteOptionDialogsResourceHandler extends ViewFocusedResourceHandler{

    private UriFactory productConfiguratorUriFactory;
    private ProductIdentifierFacade productIdentifierFacade;
    private QuoteOptionFacade quoteOptionFacade;

    public QuoteOptionDialogsResourceHandler(UriFactory productConfiguratorUriFactory, ProductIdentifierFacade productIdentifierFacade, QuoteOptionFacade quoteOptionFacade) {
        super(new Presenter());
        this.productConfiguratorUriFactory = productConfiguratorUriFactory;
        this.productIdentifierFacade = productIdentifierFacade;
        this.quoteOptionFacade = quoteOptionFacade;
    }

    @GET
    @Path("/bulk-template")
    public Response getBulkTemplateDialog(@PathParam("customerId") String customerId, @PathParam("projectId") String projectId, @PathParam("quoteOptionId") String quoteOptionId) {

        final BulkTemplateDialogView view = new BulkTemplateDialogView(customerId, projectId,
                                                                       quoteOptionId,
                                                                       productIdentifierFacade.getAllSellableProducts(),
                                                                       productConfiguratorUriFactory,
                                                                       "S0205086",
                                                                       quoteOptionFacade.get(projectId, quoteOptionId).currency);
        return responseOk(presenter.render(view("BulkTemplateDownloadDialog.ftl").withContext("view", view)));
    }

}
