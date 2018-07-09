package com.bt.rsqe.projectengine.web;

import com.bt.rsqe.customerinventory.client.ProductInstanceClient;
import com.bt.rsqe.customerinventory.parameter.LineItemId;
import com.bt.rsqe.domain.bom.parameters.ProductSCode;
import com.bt.rsqe.domain.project.ProductInstance;
import com.bt.rsqe.pmr.client.PmrClient;
import com.bt.rsqe.projectengine.ProjectResource;
import com.bt.rsqe.projectengine.QuoteOptionResource;
import com.bt.rsqe.web.AjaxResponseDTO;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;

@Path("/rsqe/customers/{customerId}/contracts/{contractId}/projects/{projectId}/quote-options/{quoteOptionId}/validation")
@Produces(MediaType.TEXT_HTML)
public class ValidationResourceHandler {
    private static final String CUSTOMER_ID = "customerId";
    private static final String CONTRACT_ID = "contractId";
    private static final String PROJECT_ID = "projectId";
    private static final String QUOTE_OPTION_ID = "quoteOptionId";
    private final ProjectResource projects;
    private final ProductInstanceClient productInstanceClient;
    private PmrClient pmr;

    public ValidationResourceHandler(ProjectResource projects, ProductInstanceClient productInstanceClient, PmrClient pmr) {
        this.projects = projects;
        this.productInstanceClient = productInstanceClient;
        this.pmr = pmr;
    }

    @GET
    @Path("/validate-import-with-product-code/{productScode}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response validateProductImportWithSCode(@PathParam(QUOTE_OPTION_ID) final String quoteOptionId,
                                                   @PathParam(PROJECT_ID) final String projectId,
                                                   @PathParam("productScode") final String productCode) throws IOException {
        QuoteOptionResource quoteOptionResource = projects.quoteOptionResource(projectId);
        String status = quoteOptionResource.getProductImportStatusForSCode(quoteOptionId, productCode);
        AjaxResponseDTO responseDTO = buildResponseDTO(productCode, status);
        return Response.status(Response.Status.OK).entity(responseDTO).build();
    }

    @GET
    @Path("/validate-import-with-line-item/{lineItemId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response validateProductImportWithLineItemId(@PathParam(QUOTE_OPTION_ID) final String quoteOptionId,
                                                        @PathParam(PROJECT_ID) final String projectId,
                                                        @PathParam("lineItemId") final String lineItemId) throws IOException {
        QuoteOptionResource quoteOptionResource = projects.quoteOptionResource(projectId);
        ProductInstance productInstance = productInstanceClient.get(new LineItemId(lineItemId));
        String productCode = productInstance.getProductIdentifier().getProductId();
        String status = quoteOptionResource.getProductImportStatusForSCode(quoteOptionId, productCode);
        AjaxResponseDTO responseDTO = buildResponseDTO(productCode, status);
        return Response.status(Response.Status.OK).entity(responseDTO).build();
    }

    private boolean isBulkImport(String productCode) {
        return pmr.productOffering(ProductSCode.newInstance(productCode)).get().isBulk();
    }

    private AjaxResponseDTO buildResponseDTO(String productCode, String status) {
        boolean responseStatus;
        AjaxResponseDTO responseDTO;
        ImportStatus importStatus = null;
        if (null == status) {
            responseStatus = true;
        } else {
            importStatus = ImportStatus.get(status);
            responseStatus = isBulkImport(productCode) ? false : importStatus.isOnlyForBulkUpload();
        }
        responseDTO = new AjaxResponseDTO(responseStatus, responseStatus ? "" : importStatus.getImportErrorMessage());
        return responseDTO;
    }
}
