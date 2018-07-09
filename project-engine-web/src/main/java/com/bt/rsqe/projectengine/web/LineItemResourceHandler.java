package com.bt.rsqe.projectengine.web;

import com.bt.rsqe.CallbackConditional;
import com.bt.rsqe.customerinventory.client.ProductInstanceClient;
import com.bt.rsqe.customerinventory.parameter.LineItemId;
import com.bt.rsqe.domain.QuoteOptionItemStatus;
import com.bt.rsqe.domain.product.parameters.ProductCategoryCode;
import com.bt.rsqe.domain.project.ProductInstance;
import com.bt.rsqe.enums.ProductAction;
import com.bt.rsqe.enums.ProductCodes;
import com.bt.rsqe.inlife.client.ApplicationCapabilityProvider;
import com.bt.rsqe.logging.Log;
import com.bt.rsqe.logging.LogFactory;
import com.bt.rsqe.logging.LogLevel;
import com.bt.rsqe.projectengine.ProjectEngineClientResources;
import com.bt.rsqe.projectengine.ProjectResource;
import com.bt.rsqe.projectengine.QuoteOptionDTO;
import com.bt.rsqe.projectengine.QuoteOptionItemDTO;
import com.bt.rsqe.projectengine.QuoteOptionItemResource;
import com.bt.rsqe.projectengine.QuoteOptionResource;
import com.bt.rsqe.projectengine.web.facades.ExpedioServicesFacade;
import com.bt.rsqe.projectengine.web.facades.FlattenedProductStructure;
import com.bt.rsqe.projectengine.web.facades.FutureProductInstanceFacade;
import com.bt.rsqe.projectengine.web.model.LineItemModel;
import com.bt.rsqe.projectengine.web.quoteoptiondetails.QuoteOptionDetailsOrchestrator;
import com.bt.rsqe.projectengine.web.quoteoptionorders.ecrfsheet.ECRFSheetOrchestrator;
import com.bt.rsqe.projectengine.web.tpe.TpeStatusManager;
import com.bt.rsqe.projectengine.web.view.QuoteOptionDetailsDTO;
import com.bt.rsqe.projectengine.web.view.filtering.DataTableFilterValues;
import com.bt.rsqe.projectengine.web.view.filtering.FilterValues;
import com.bt.rsqe.projectengine.web.view.filtering.PaginatedDetailsTabViewFilter;
import com.bt.rsqe.projectengine.web.view.filtering.PaginatedFilter;
import com.bt.rsqe.projectengine.web.view.pagination.DefaultPagination;
import com.bt.rsqe.projectengine.web.view.pagination.Pagination;
import com.bt.rsqe.projectengine.web.view.sorting.DataTableSortValues;
import com.bt.rsqe.projectengine.web.view.sorting.PaginatedDetailsTabViewSort;
import com.bt.rsqe.projectengine.web.view.sorting.PaginatedSort;
import com.bt.rsqe.projectengine.web.view.sorting.SortValues;
import com.bt.rsqe.rest.ResponseBuilder;
import com.bt.rsqe.security.Credentials;
import com.bt.rsqe.security.UserContextManager;
import com.bt.rsqe.security.UserDTO;
import com.bt.rsqe.web.Presenter;
import com.google.common.base.Optional;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.ws.rs.Consumes;
import javax.ws.rs.CookieParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Set;

import static com.bt.rsqe.projectengine.web.ImportStatus.*;
import static com.google.common.collect.Sets.*;

@Path("/rsqe/customers/{customerId}/contracts/{contractId}/projects/{projectId}/quote-options/{quoteOptionId}/line-items")
@Produces(MediaType.TEXT_HTML)
public class LineItemResourceHandler extends QuoteViewFocusedResourceHandler {
    private static final Logger LOG = LogFactory.createDefaultLogger(Logger.class);
    private static final String CUSTOMER_ID = "customerId";
    private static final String CONTRACT_ID = "contractId";
    private static final String PROJECT_ID = "projectId";
    private static final String QUOTE_OPTION_ID = "quoteOptionId";

    private final QuoteOptionDetailsOrchestrator detailsOrchestrator;
    private final ProjectEngineClientResources projectEngineClientResources;
    private FutureProductInstanceFacade futureProductInstanceFacade;
    private ECRFSheetOrchestrator eCRFSheetOrchestrator;
    private final ProjectResource projects;
    private ProductInstanceClient productInstanceClient;
    private ExpedioServicesFacade expedioServicesFacade;
    private PriceHandlerService priceHandlerService;
    private TpeStatusManager tpeStatusManager;
    private ApplicationCapabilityProvider capabilityProvider;

    private static final String LINE_ITEM_ID = "lineItemId";

    public LineItemResourceHandler(final Presenter presenter, QuoteOptionDetailsOrchestrator detailsOrchestrator,
                                   ProjectEngineClientResources clientResources, FutureProductInstanceFacade futureProductInstanceFacade,
                                   ECRFSheetOrchestrator eCRFSheetOrchestrator, ProjectResource projects, ProductInstanceClient productInstanceClient,
                                   ExpedioServicesFacade expedioServicesFacade, PriceHandlerService priceHandlerService, TpeStatusManager tpeStatusManager, ApplicationCapabilityProvider capabilityProvider) {
        super(presenter);
        this.detailsOrchestrator = detailsOrchestrator;
        this.projectEngineClientResources = clientResources;
        this.futureProductInstanceFacade = futureProductInstanceFacade;
        this.eCRFSheetOrchestrator = eCRFSheetOrchestrator;
        this.projects = projects;
        this.productInstanceClient = productInstanceClient;
        this.expedioServicesFacade = expedioServicesFacade;
        this.priceHandlerService = priceHandlerService;
        this.tpeStatusManager = tpeStatusManager;
        this.capabilityProvider = capabilityProvider;
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{lineItemId}/validate")
    public Response validate(@PathParam(PROJECT_ID) final String projectId,
                             @PathParam(QUOTE_OPTION_ID) final String quoteOptionId,
                             @PathParam("lineItemId") final String lineItemId) {

        /* SQE OneVoice are not creating all child products by default any more so validation fails. Create missing mandatory child products
         here to ensure the offer can be accepted.*/
        FlattenedProductStructure structure = futureProductInstanceFacade.buildFullFlattenedRelationshipStructure(new LineItemId(lineItemId));
        if (ProductCodes.Onevoice.productCode().equals(structure.getRootProductCode())) {
            structure.markAll();
            futureProductInstanceFacade.saveProductInstance(structure);
        }
        return Response.ok().entity(projectEngineClientResources.quoteOptionItemResource(projectId, quoteOptionId).validate(lineItemId)).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{lineItemId}/remove")
    public Response removeLineItemFromQuoteOption(@PathParam(PROJECT_ID) final String projectId,
                                                  @PathParam(QUOTE_OPTION_ID) final String quoteOptionId,
                                                  @PathParam("lineItemId") final String lineItemId) {
        try {
            final QuoteOptionItemResource quoteOptionItemResource = projects.quoteOptionResource(projectId).quoteOptionItemResource(quoteOptionId);
            final QuoteOptionItemDTO item = quoteOptionItemResource.get(lineItemId);
            if(null == item.status || item.status.isLocked() || QuoteOptionItemStatus.OFFERED.equals(item.status) || item.isIfc) {
                return ResponseBuilder.internalServerError().withEntity("Line Item is not eligible to be removed from this Quote Option.  Only Draft line items can be removed.").build();
            }

            List<LineItemId> lineItemsRemoved = productInstanceClient.removeLineItemFromInventory(new LineItemId(lineItemId), new CallbackConditional<LineItemId>() {
                @Override
                public boolean actionAllowedFor(LineItemId lineItemId) {
                    // don't allow line item removal if any in progress special bid requests can't be cancelled on TPE...
                    return tpeStatusManager.cancelSiteForLineItemIfRequired(lineItemId.value());
                }
            });

            for(LineItemId removedLineItem : lineItemsRemoved) {
                quoteOptionItemResource.delete(removedLineItem.value());
                LOG.lineItemRemovedFromQuoteOption(UserContextManager.getCurrent().getLoginName(), removedLineItem.value(), quoteOptionId);
            }

            return ResponseBuilder.anOKResponse().build();
        } catch (IllegalStateException e) {
            return ResponseBuilder.internalServerError().withEntity(e.getMessage()).build();
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getQuoteOptionLineItems(@PathParam(CUSTOMER_ID) final String customerId,
                                            @PathParam(CONTRACT_ID) final String contractId,
                                            @PathParam(PROJECT_ID) final String projectId,
                                            @PathParam(QUOTE_OPTION_ID) final String quoteOptionId,
                                            @QueryParam("iDisplayStart") final int pageStart,
                                            @QueryParam("iDisplayLength") final int pageSize,
                                            @QueryParam("sEcho") final int pageNumber,
                                            @CookieParam(Credentials.RSQE_TOKEN) final String userToken,
                                            @Context final UriInfo uriInfo) {
        return new QuoteViewFocusedResourceHandler.HandlerActionAttempt() {
            @Override
            protected Response action() throws Exception {
                Pagination pagination = new DefaultPagination(pageNumber, pageStart, pageSize);
                MultivaluedMap<String, String> queryParameters = uriInfo.getQueryParameters(true);
                // Get filtering query parameters
                final FilterValues filterValues = DataTableFilterValues.parse(queryParameters);
                final PaginatedFilter<LineItemModel> paginatedFilter = new PaginatedDetailsTabViewFilter(filterValues, pagination);
                // Get sorting query parameters
                final SortValues sortValues = DataTableSortValues.parse(queryParameters);
                final PaginatedSort<LineItemModel> paginatedSort = new PaginatedDetailsTabViewSort(sortValues);

                String siteCeaseItemsDescription = "";
                if(capabilityProvider.isFunctionalityEnabled(ApplicationCapabilityProvider.Capability.FILTER_CEASED_BFG_SITES, true, Optional.of(quoteOptionId))) {
                    siteCeaseItemsDescription = projects.quoteOptionResource(projectId).getSiteCeaseItemsDescription(customerId, quoteOptionId);
                }

                QuoteOptionDetailsDTO dto = detailsOrchestrator.buildJsonResponse(customerId, contractId, projectId, quoteOptionId,
                                                                                  paginatedFilter, paginatedSort, userToken, siteCeaseItemsDescription);
                return Response.ok().entity(dto).build();
            }
        }.tryToPerformAction(quoteOptionId);
    }

    @POST
    @Path("/add-import-product-configuration/{productScode}/action/{productAction}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.TEXT_HTML)
    public Response importUsingProduct(@PathParam(CUSTOMER_ID) final String customerId,
                                       @PathParam(CONTRACT_ID) final String contractId,
                                       @PathParam(PROJECT_ID) final String projectId,
                                       @PathParam(QUOTE_OPTION_ID) final String quoteOptionId,
                                       @PathParam("productScode") final String productCode,
                                       @PathParam("productAction") final String productAction,
                                       @FormDataParam("eCRFSheet") final InputStream eCRFSheet,
                                       @FormDataParam("eCRFSheet") FormDataContentDisposition fileDetail,
                                       @QueryParam("productCategoryCode") final String productCategoryCode) throws IOException {

        ImportResults importResults = new ImportResults();
        Set<LineItemId> impactedLineItems = newHashSet();
        QuoteOptionResource quoteOptionResource = projects.quoteOptionResource(projectId);
        QuoteOptionDTO quoteOptionDTO = quoteOptionResource.get(quoteOptionId);

        ImportStatusManager importStatusManager = new ImportStatusManager(quoteOptionResource);
        importStatusManager.markImportStatus(quoteOptionId, productCode, null, quoteOptionDTO.getCreatedBy(), fileDetail.getFileName(), ImportStatus.Initiated);

        boolean isMigration = productAction.equals(ProductAction.Migrate.toString());
        quoteOptionDTO.setMigrationQuote(isMigration);
        quoteOptionResource.put(quoteOptionDTO);

        try {
            final Workbook eCRFWorkSheet = WorkbookFactory.create(eCRFSheet);
            impactedLineItems = eCRFSheetOrchestrator.importUsingProduct(customerId, contractId, quoteOptionDTO.contractTerm, projectId, quoteOptionId, eCRFWorkSheet, productCode, importResults, isMigration, new ProductCategoryCode(productCategoryCode));
        } catch (Exception e) {
            importResults.addError(productCode, e.getMessage());
        }
        importStatusManager.updateImportStatus(quoteOptionId, importResults.hasErrors() ? Failed : Success);
        importStatusManager.storeImportErrorLog(quoteOptionResource, quoteOptionId, importResults, quoteOptionDTO.getCreatedBy(), productCode);
        initiatePricing(impactedLineItems, customerId, projectId, quoteOptionId);
        String loginName = UserContextManager.getCurrent().getLoginName();
        UserDTO userDTO = expedioServicesFacade.getUserDetails(loginName);
        importStatusManager.sendImportStatusMail(quoteOptionId, productCode, userDTO);
        return Response.status(Response.Status.OK).build();
    }

    private void initiatePricing(Set<LineItemId> impactedLineItems, String customerId, String projectId, String quoteOptionId) {
        priceHandlerService.processLineItemsForPricing(impactedLineItems, customerId, projectId, quoteOptionId, UserContextManager.getCurrent().getPermissions().indirectUser,
                                                       UserContextManager.getCurrent().getRsqeToken());
    }

    @POST
    @Path("/import-product-configuration/{lineItemId}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.TEXT_HTML)
    public Response importUsingLineItem(@PathParam(CUSTOMER_ID) final String customerId,
                                        @PathParam(CONTRACT_ID) final String contractId,
                                        @PathParam(PROJECT_ID) final String projectId,
                                        @PathParam(QUOTE_OPTION_ID) final String quoteOptionId,
                                        @PathParam(LINE_ITEM_ID) final String lineItemId,
                                        @FormDataParam("eCRFSheet") final InputStream eCRFSheet,
                                        @FormDataParam("eCRFSheet") FormDataContentDisposition fileDetail) throws IOException {

        ImportResults importResults = new ImportResults();
        Set<LineItemId> impactedLineItems = newHashSet();
        QuoteOptionResource quoteOptionResource = projects.quoteOptionResource(projectId);
        QuoteOptionDTO quoteOptionDTO = quoteOptionResource.get(quoteOptionId);

        ProductInstance markerInstance = productInstanceClient.get(new LineItemId(lineItemId));
        String productCode = markerInstance.getProductOffering().getProductIdentifier().getProductId();

        ImportStatusManager importStatusManager = new ImportStatusManager(quoteOptionResource);
        importStatusManager.markImportStatus(quoteOptionId, productCode, markerInstance.getLineItemId(), quoteOptionDTO.getCreatedBy(), fileDetail.getFileName(), ImportStatus.Initiated);

        boolean isMigrationQuote = quoteOptionDTO.migrationQuote;
        try {
            final Workbook eCRFWorkSheet = WorkbookFactory.create(eCRFSheet);
            impactedLineItems = eCRFSheetOrchestrator.importUsingLineItem(customerId, contractId, projectId, quoteOptionId, lineItemId, eCRFWorkSheet, importResults, markerInstance.getProductIdentifier().getProductId(), isMigrationQuote, markerInstance.getProductCategoryCode());

        } catch (Exception e) {
            importResults.addError(productCode, e.getMessage());
        }

        importStatusManager.updateImportStatus(quoteOptionId, importResults.hasErrors() ? Failed : Success);
        importStatusManager.storeImportErrorLog(quoteOptionResource, quoteOptionId, importResults, quoteOptionDTO.getCreatedBy(), productCode);
        initiatePricing(impactedLineItems, customerId, projectId, quoteOptionId);
        String loginName = UserContextManager.getCurrent().getLoginName();
        UserDTO userDTO = expedioServicesFacade.getUserDetails(loginName);
        importStatusManager.sendImportStatusMail(quoteOptionId, productCode, userDTO);

        return Response.ok().build();
    }

    interface Logger {
        @Log(level = LogLevel.INFO, format = "%s removed Line Item %s from Quote Option %s.")
        void lineItemRemovedFromQuoteOption(String user, String lineItemId, String quoteOptionId);
    }
}
