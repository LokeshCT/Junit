package com.bt.rsqe.projectengine.web.userImport;

import com.bt.rsqe.customerinventory.client.ProductInstanceClient;
import com.bt.rsqe.customerinventory.parameter.LineItemId;
import com.bt.rsqe.customerrecord.CustomerDTO;
import com.bt.rsqe.customerrecord.CustomerResource;
import com.bt.rsqe.customerrecord.ExpedioClientResources;
import com.bt.rsqe.customerrecord.SiteResource;
import com.bt.rsqe.domain.bom.parameters.ProductSCode;
import com.bt.rsqe.domain.product.ProductOffering;
import com.bt.rsqe.domain.product.parameters.ProductIdentifier;
import com.bt.rsqe.domain.project.ProductInstance;
import com.bt.rsqe.pmr.client.PmrClient;
import com.bt.rsqe.projectengine.ProjectResource;
import com.bt.rsqe.projectengine.QuoteOptionDTO;
import com.bt.rsqe.projectengine.QuoteOptionItemDTO;
import com.bt.rsqe.projectengine.QuoteOptionItemResource;
import com.bt.rsqe.projectengine.QuoteOptionResource;
import com.bt.rsqe.projectengine.web.ImportResults;
import com.bt.rsqe.projectengine.web.ImportStatus;
import com.bt.rsqe.projectengine.web.ImportStatusManager;
import com.bt.rsqe.projectengine.web.facades.ExpedioServicesFacade;
import com.bt.rsqe.projectengine.web.productconfigurator.BulkConfigSheetOrchestrator;
import com.bt.rsqe.security.UserContextManager;
import com.bt.rsqe.security.UserDTO;
import com.bt.rsqe.utils.JSONSerializer;
import com.bt.rsqe.web.AjaxResponseDTO;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.bt.rsqe.projectengine.web.ImportStatus.*;
import static com.google.common.collect.Lists.*;
import static com.google.common.collect.Maps.*;
import static com.google.common.collect.Sets.newHashSet;

@Path("/rsqe/customers/{customerId}/contracts/{contractId}/projects/{projectId}/quote-options/{quoteOptionId}/sCode/{sCode}")
@Produces(MediaType.TEXT_HTML)
public class UserImportResourceHandler {

    private static final String CUSTOMER_ID = "customerId";
    private static final String CONTRACT_ID = "contractId";
    private static final String PROJECT_ID = "projectId";
    private static final String QUOTE_OPTION_ID = "quoteOptionId";
    private static final String SCODE = "sCode";
    private static final String BULK_SHEET = "bulkSheet";
    private ProjectResource projectResource;
    private ProductInstanceClient productInstanceClient;
    private ExpedioClientResources expedioClientResources;
    private PmrClient pmr;
    private BulkConfigSheetOrchestrator bulkConfigSheetOrchestrator;
    private ExpedioServicesFacade expedioServicesFacade;

    public UserImportResourceHandler(ProjectResource projectResource, ProductInstanceClient productInstanceClient, ExpedioClientResources userImportDataFactory, PmrClient pmr, BulkConfigSheetOrchestrator bulkConfigSheetOrchestrator, ExpedioServicesFacade expedioServicesFacade) {
        this.projectResource = projectResource;
        this.productInstanceClient = productInstanceClient;
        this.expedioClientResources = userImportDataFactory;
        this.pmr = pmr;
        this.bulkConfigSheetOrchestrator = bulkConfigSheetOrchestrator;
        this.expedioServicesFacade = expedioServicesFacade;
    }

    @GET
    @Path("/user-export")
    @Produces({"application/vnd.ms-excel"})
    public Response userExport(@PathParam(CUSTOMER_ID) final String customerId,
                               @PathParam(PROJECT_ID) final String projectId,
                               @PathParam(QUOTE_OPTION_ID) final String quoteOptionId,
                               @PathParam(SCODE) final String productCode) {
        QuoteOptionResource quoteOptionResource = projectResource.quoteOptionResource(projectId);
        QuoteOptionDTO quoteOptionDTO = quoteOptionResource.get(quoteOptionId);
        QuoteOptionItemResource quoteOptionItemResource = quoteOptionResource.quoteOptionItemResource(quoteOptionId);
        CustomerResource customerResource = expedioClientResources.getCustomerResource();
        SiteResource siteResource = customerResource.siteResource(customerId);
        String rsqeToken = UserContextManager.getCurrent().getRsqeToken();
        CustomerDTO customerDTO = customerResource.getByToken(customerId, rsqeToken);
        List<QuoteOptionItemDTO> quoteOptionItemDTO = quoteOptionItemResource.getByScode(productCode);
        List<ProductInstance> productInstances = newArrayList();
        for (QuoteOptionItemDTO optionItemDTO : quoteOptionItemDTO) {
            productInstances.add(productInstanceClient.get(new LineItemId(optionItemDTO.getId())));
        }

        ProductOffering rootOffering = pmr.productOffering(ProductSCode.newInstance(productCode)).get();
        List<ProductIdentifier> exportableProducts = pmr.getNonRootExportableProducts();
        Map<ProductIdentifier, ProductOffering> offerings = populateExportedProductsOfferings(exportableProducts);

        XSSFWorkbook xssfWorkbook = null;
        if (null != productInstances && !productInstances.isEmpty()) {
            ProductModelBuilder productModelBuilder = new ProductModelBuilder(productInstances, quoteOptionDTO, siteResource, customerDTO, offerings);
            xssfWorkbook = productModelBuilder.constructWorkbook();
        }

        String exportSheetName = quoteOptionDTO.getName() + "_" + rootOffering.getProductIdentifier().getProductName() + "_" + quoteOptionId + ".xlsx";

        final XSSFWorkbook finalXssfWorkbook = xssfWorkbook;
        final StreamingOutput streamingOutput = new StreamingOutput() {
            @Override
            public void write(OutputStream output) throws IOException, WebApplicationException {
                finalXssfWorkbook.write(output);
            }
        };

        return Response.ok(streamingOutput).header("Content-Disposition", "attachment; filename=" + exportSheetName.replaceAll(" ", "")).build();
    }

    //todo : Tests to be covered
    @POST
    @Path("/user-import-validate")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.TEXT_HTML)
    public Response userImportValidate(@PathParam(CUSTOMER_ID) final String customerId,
                                       @PathParam(CONTRACT_ID) final String contractId,
                                       @PathParam(PROJECT_ID) final String projectId,
                                       @PathParam(QUOTE_OPTION_ID) final String quoteOptionId,
                                       @PathParam(SCODE) final String productCode,
                                       @FormDataParam(BULK_SHEET) final InputStream sheet,
                                       @FormDataParam(BULK_SHEET) FormDataContentDisposition fileDetail) throws IOException {

        QuoteOptionResource quoteOptionResource = projectResource.quoteOptionResource(projectId);
        String status = quoteOptionResource.getProductImportStatusForSCode(quoteOptionId, productCode);
        String response;

        if (ImportStatus.Initiated.name().equals(status)) {
            AjaxResponseDTO responseDTO = new AjaxResponseDTO(false, ImportStatus.Initiated.getImportErrorMessage());
            response = JSONSerializer.getInstance().serialize(responseDTO);
            return Response.ok().entity(response).build();
        } else {
            try {
                final Workbook bulkSheet = WorkbookFactory.create(sheet);
                Sheet headerSheet = bulkSheet.getSheet("Header");
                Row row = headerSheet.getRow(1);
                Cell cell = row.getCell(row.getFirstCellNum() + 1);

                ProductOffering productOffering = pmr.productOffering(ProductSCode.newInstance(productCode)).get();

                if (!cell.getStringCellValue().equals(productOffering.getProductIdentifier().getVersionNumber())) {
                    AjaxResponseDTO responseDTO = new AjaxResponseDTO(false, "Product model version is not matching");
                    response = JSONSerializer.getInstance().serialize(responseDTO);
                    return Response.ok().entity(response).build();
                }
            } catch (Exception e) {
                AjaxResponseDTO responseDTO = new AjaxResponseDTO(false, "Not a valid bulk excel sheet");
                response = JSONSerializer.getInstance().serialize(responseDTO);
                return Response.ok().entity(response).build();
            }

            response = JSONSerializer.getInstance().serialize(new AjaxResponseDTO(true, ""));
            return Response.ok().entity(response).build();
        }
    }

    //todo : Tests to be covered
    @GET
    @Path("/user-import-status")
    @Produces(MediaType.APPLICATION_JSON)
    public Response userImportStatus(@PathParam(PROJECT_ID) final String projectId,
                                     @PathParam(QUOTE_OPTION_ID) final String quoteOptionId,
                                     @PathParam(SCODE) final String productCode) throws IOException {

        QuoteOptionResource quoteOptionResource = projectResource.quoteOptionResource(projectId);
        String status = quoteOptionResource.getProductImportStatusForSCode(quoteOptionId, productCode);

        if (ImportStatus.Initiated.name().equals(status)) {
            AjaxResponseDTO responseDTO = new AjaxResponseDTO(false, "Processing is initiated for the uploaded spreadsheet.Email notification will be sent once processing is completed");
            return Response.status(Response.Status.OK).entity(responseDTO).build();
        }

        return Response.ok().entity(new AjaxResponseDTO(true, "")).build();
    }

    //todo:tests to be covered
    @POST
    @Path("/user-import")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response userImport(@PathParam(CUSTOMER_ID) final String customerId,
                               @PathParam(CONTRACT_ID) final String contractId,
                               @PathParam(PROJECT_ID) final String projectId,
                               @PathParam(QUOTE_OPTION_ID) final String quoteOptionId,
                               @PathParam(SCODE) final String productCode,
                               @FormDataParam(BULK_SHEET) final InputStream bulkSheet,
                               @FormDataParam(BULK_SHEET) FormDataContentDisposition fileDetail) throws IOException {



        ImportResults importResults = new ImportResults();

        QuoteOptionResource quoteOptionResource = projectResource.quoteOptionResource(projectId);
        QuoteOptionDTO quoteOptionDTO = quoteOptionResource.get(quoteOptionId);

        ImportStatusManager importStatusManager = new ImportStatusManager(quoteOptionResource);
        importStatusManager.markImportStatus(quoteOptionId, productCode, null, quoteOptionDTO.getCreatedBy(), fileDetail.getFileName(), ImportStatus.Initiated);

        QuoteOptionItemResource quoteOptionItemResource = quoteOptionResource.quoteOptionItemResource(quoteOptionId);
        CustomerResource customerResource = expedioClientResources.getCustomerResource();
        SiteResource siteResource = customerResource.siteResource(customerId);

        List<QuoteOptionItemDTO> quoteOptionItemDTOs = quoteOptionItemResource.getByScode(productCode);
        List<ProductInstance> rootInstances = newArrayList();
        List<ProductIdentifier> exportableProducts = pmr.getNonRootExportableProducts();
        Map<ProductIdentifier, ProductOffering> offerings = populateExportedProductsOfferings(exportableProducts);


        for (QuoteOptionItemDTO optionItemDTO : quoteOptionItemDTOs) {
            final ProductInstance productInstance = productInstanceClient.get(new LineItemId(optionItemDTO.getId()));
            rootInstances.add(productInstance);
        }

        try {
           final Workbook bulkWorkSheet = WorkbookFactory.create(bulkSheet);

            bulkConfigSheetOrchestrator.importProductByBulkSheet(bulkWorkSheet, rootInstances, importResults, siteResource, offerings, quoteOptionItemResource);
        } catch (Exception e) {
            importResults.addError(productCode, e.getMessage());
        }

        importStatusManager.updateImportStatus(quoteOptionId, importResults.hasErrors() ? Failed : Success);
        importStatusManager.storeImportErrorLog(quoteOptionResource, quoteOptionId, importResults, quoteOptionDTO.getCreatedBy(), productCode);


        String loginName = UserContextManager.getCurrent().getLoginName();
        UserDTO userDTO = expedioServicesFacade.getUserDetails(loginName);
        importStatusManager.sendImportStatusMail(quoteOptionId, productCode, userDTO);
        return Response.status(Response.Status.OK).build();
    }


    private Map<ProductIdentifier, ProductOffering> populateExportedProductsOfferings(List<ProductIdentifier> exportableProducts) {
        Map<ProductIdentifier, ProductOffering> offerings = newHashMap();
        for (ProductIdentifier productIdentifier : exportableProducts) {
            ProductOffering productOffering = pmr.productOffering(ProductSCode.newInstance(productIdentifier.getProductId())).get();
            offerings.put(productIdentifier,productOffering);
        }
        return offerings;
    }
}
