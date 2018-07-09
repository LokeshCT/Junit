package com.bt.rsqe.projectengine.web;

import com.bt.rsqe.customerinventory.client.ProductInstanceClient;
import com.bt.rsqe.customerinventory.parameter.LengthConstrainingProductInstanceId;
import com.bt.rsqe.customerinventory.parameter.LineItemId;
import com.bt.rsqe.domain.PriceBookDTO;
import com.bt.rsqe.domain.product.ProductSalesRelationshipInstance;
import com.bt.rsqe.domain.product.parameters.RelationshipType;
import com.bt.rsqe.domain.project.PriceLine;
import com.bt.rsqe.domain.project.ProductInstance;
import com.bt.rsqe.domain.ContractDTO;
import com.bt.rsqe.projectengine.ProjectResource;
import com.bt.rsqe.projectengine.QuoteOptionItemDTO;
import com.bt.rsqe.projectengine.web.facades.PriceBookFacade;
import com.bt.rsqe.projectengine.web.view.ContractDialogView;
import com.bt.rsqe.security.UserContext;
import com.bt.rsqe.security.UserContextManager;
import com.bt.rsqe.utils.JSONSerializer;
import com.bt.rsqe.web.Presenter;
import com.bt.rsqe.web.rest.exception.BadRequestException;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import javax.annotation.Nullable;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static com.bt.rsqe.utils.AssertObject.*;


@Path("/rsqe/customers/{customerId}/contracts/{contractId}/projects/{projectId}/quote-options/{quoteOptionId}/line-items/{lineItemId}/contract")
@Produces(MediaType.TEXT_HTML + ";charset=ISO-8859-15")
public class ContractDialogResourceHandler extends QuoteViewFocusedResourceHandler {

    private static final String CUSTOMER_ID = "customerId";
    private static final String CONTRACT_ID = "contractId";
    private static final String PROJECT_ID = "projectId";
    private static final String QUOTE_OPTION_ID = "quoteOptionId";
    private static final String QUOTE_OPTION_ITEM = "lineItemId";
    private final ProjectResource projectResource;
    private final PriceBookFacade priceBookFacade;
    private final ProductInstanceClient productInstanceClient;
    private JSONSerializer jsonSerializer;

    public ContractDialogResourceHandler(final Presenter presenter,
                                         ProjectResource projectResource,
                                         JSONSerializer jsonSerializer, PriceBookFacade priceBookFacade, ProductInstanceClient productInstanceClient) {
        super(presenter);
        this.projectResource = projectResource;
        this.jsonSerializer = jsonSerializer;
        this.priceBookFacade = priceBookFacade;
        this.productInstanceClient = productInstanceClient;
    }

    @GET
    @Path("/form")
    public Response contractForm(@PathParam(CUSTOMER_ID) String customerId,
                                 @PathParam(CONTRACT_ID) String contractId,
                                 @PathParam(PROJECT_ID) String projectId,
                                 @PathParam(QUOTE_OPTION_ID) String quoteOptionId,
                                 @PathParam(QUOTE_OPTION_ITEM) String quoteOptionItemId) {

        QuoteOptionItemDTO quoteOptionItemDTO = projectResource.quoteOptionResource(projectId).quoteOptionItemResource(quoteOptionId).get(quoteOptionItemId);
        ContractDTO contractDTO = quoteOptionItemDTO.contractDTO;
        UserContext userContext = UserContextManager.getCurrent();
        final PriceBookDTO priceBookDTO;
        contractDTO.addAllPriceBook(getAssetPriceBooks(quoteOptionItemId));
        if (userContext.getPermissions().indirectUser) {
            priceBookDTO = priceBookFacade.getLatestPriceBookForIndirectUser(customerId, quoteOptionItemDTO.sCode, quoteOptionItemDTO.getProductCategoryCode());
        } else {
            priceBookDTO = priceBookFacade.getLatestPriceBookForDirectUser(quoteOptionItemDTO.sCode, quoteOptionItemDTO.getProductCategoryCode());
        }
        contractDTO.addPriceBook(priceBookDTO);

        ContractDialogView view = new ContractDialogView(customerId, contractId, projectId, quoteOptionId,
                                                         quoteOptionItemId, contractDTO, quoteOptionItemDTO.status);
        String page = presenter.render(view("ContractForm.ftl").withContext("view", view));
        return Response.ok().entity(page).build();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateContract(@PathParam(PROJECT_ID) String projectId,
                                   @PathParam(QUOTE_OPTION_ID) String quoteOptionId,
                                   @PathParam(QUOTE_OPTION_ITEM) String quoteOptionItemId,
                                   @FormParam("eupPriceBook") String eupPriceBook,
                                   @FormParam("ptpPriceBook") String ptpPriceBook) {
        QuoteOptionItemDTO quoteOptionItemDTO = projectResource.quoteOptionResource(projectId).quoteOptionItemResource(quoteOptionId).get(quoteOptionItemId);
        ContractDTO contractDTO = quoteOptionItemDTO.contractDTO;

        //pricebooks is always going to have one element except when it's shown on UI(where we talk to expedio to get latest priceBook)
        PriceBookDTO savedPriceBook = contractDTO.priceBooks.get(0);

        if (hasUserSelectedDifferentPriceBook(savedPriceBook, eupPriceBook, ptpPriceBook)) {
            savedPriceBook.eupPriceBook = eupPriceBook;
            savedPriceBook.ptpPriceBook = ptpPriceBook;
            try {
                projectResource.quoteOptionResource(projectId).quoteOptionItemResource(quoteOptionId).put(quoteOptionItemDTO);
                if (!isDirectUser(savedPriceBook)) {
                    ProductInstance productInstance = productInstanceClient.get(new LineItemId(quoteOptionItemId));
                    productInstance.invalidatePriceLines();
                    productInstance.refreshPricingStatusBasedOnPriceLines();
                    productInstance.invalidateChildProductsPriceLines();
                    productInstanceClient.put(productInstance);
                }
            } catch (BadRequestException exception) {
                return Response.status(Response.Status.BAD_REQUEST).entity(exception.errorDto().description).build();
            }
        }
        return Response.ok().build();
    }

    private boolean hasUserSelectedDifferentPriceBook(PriceBookDTO savedPriceBook, String eupPriceBook, String ptpPriceBook) {
        boolean eupPriceBookHasChanged = !savedPriceBook.eupPriceBook.equals(eupPriceBook);
        if (isDirectUser(savedPriceBook)) {
            return eupPriceBookHasChanged;
        }

        boolean ptpPriceBookHasChanged = !savedPriceBook.ptpPriceBook.equals(ptpPriceBook);
        return ptpPriceBookHasChanged || eupPriceBookHasChanged;
    }

    private boolean isDirectUser(PriceBookDTO savedPriceBook) {
        return savedPriceBook.ptpPriceBook == null;
    }

    public List<PriceBookDTO> getAssetPriceBooks(String quoteOptionItemId) {
        final ProductInstance productInstance = productInstanceClient.get(new LineItemId(quoteOptionItemId));
        Optional<ProductInstance> asIsProductInstance = productInstanceClient.getSourceAsset(new LengthConstrainingProductInstanceId(productInstance.getProductInstanceId().getValue()));
        if (asIsProductInstance.isPresent() && !asIsProductInstance.get().isCpe()) {
            return recursiveAddPriceBook(asIsProductInstance.get());
        }
        return Collections.emptyList();
    }

    private List<PriceBookDTO> recursiveAddPriceBook(ProductInstance productInstance) {
        List<PriceBookDTO> priceBookDTOs = new ArrayList<PriceBookDTO>();
        PriceLine priceLine = getNonCostPriceLine(productInstance.getPriceLines());
        if (isNull(priceLine)) {
            return priceBookDTOs;
        }
        priceBookDTOs.add(new PriceBookDTO(UUID.randomUUID().toString(), null,
                                           priceLine.getEupPrice().getBookVersion(),
                                           priceLine.getChargePrice().getBookVersion(), null, null));
        for (ProductSalesRelationshipInstance relInstance : productInstance.getActiveRelationships()) {
            if (!RelationshipType.Child.equals(relInstance.getType())) {
                priceBookDTOs.addAll(recursiveAddPriceBook(relInstance.getRelatedProductInstance()));
            }
        }
        return priceBookDTOs;
    }

    @GET
    @Path("/monthly-revenue")
    public Response findExitingMonthlyRevenue(@PathParam(CUSTOMER_ID) String customerId,
                                              @PathParam(CONTRACT_ID) String contractId,
                                              @PathParam(PROJECT_ID) String projectId,
                                              @PathParam(QUOTE_OPTION_ID) String quoteOptionId,
                                              @PathParam(QUOTE_OPTION_ITEM) String quoteOptionItemId) {
        QuoteOptionItemDTO quoteOptionItemDTO = projectResource.quoteOptionResource(projectId).quoteOptionItemResource(quoteOptionId).get(quoteOptionItemId);
        ContractDTO contractDTO = quoteOptionItemDTO.contractDTO;
        return Response.ok().entity(contractDTO.priceBooks.get(0)).build();

    }
    private PriceLine getNonCostPriceLine(List<PriceLine> priceLines) {
        Optional<PriceLine> priceLineOptional = Iterables.tryFind(priceLines, new Predicate<PriceLine>() {
            @Override
            public boolean apply(@Nullable PriceLine input) {
                return !"Cost".equalsIgnoreCase(input.getTariffType());
            }
        });
        return priceLineOptional.isPresent() ? priceLineOptional.get() : null;
    }
}

