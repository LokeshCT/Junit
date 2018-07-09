package com.bt.rsqe.projectengine.web;


import com.bt.rsqe.projectengine.ProjectDTO;
import com.bt.rsqe.projectengine.ProjectResource;
import com.bt.rsqe.projectengine.QuoteOptionDTO;
import com.bt.rsqe.projectengine.QuoteOptionResource;
import com.bt.rsqe.projectengine.web.quoteoption.validation.QuoteOptionDependencyValidator;
import com.bt.rsqe.projectengine.web.quoteoption.validation.RfoBillAccountCurrencyValidator;
import com.bt.rsqe.projectengine.web.quoteoptionorders.rfosheet.ExcelWorkbook;
import com.bt.rsqe.projectengine.web.quoteoptionorders.rfosheet.OrderRFOSheetOrchestrator;
import com.bt.rsqe.projectengine.web.quoteoptionorders.rfosheet.RFOImportException;
import com.bt.rsqe.rest.ResponseBuilder;
import com.bt.rsqe.utils.JSONSerializer;
import com.bt.rsqe.web.AjaxResponseDTO;
import com.bt.rsqe.web.Presenter;
import com.google.common.base.Joiner;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
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
import java.util.Set;

@Path("/rsqe/customers/{customerId}/contracts/{contractId}/projects/{projectId}/quote-options/{quoteOptionId}/orders/{orderId}/rfo")
@Produces(MediaType.TEXT_HTML)
public class OrderRFOResourceHandler extends QuoteViewFocusedResourceHandler {

    private OrderRFOSheetOrchestrator orderRFOSheetOrchestrator;
    private ProjectResource projectResource;
    private QuoteOptionDependencyValidator quoteDependencyValidator;

    public OrderRFOResourceHandler(final Presenter presenter,
                                   OrderRFOSheetOrchestrator orderRFOSheetOrchestrator,
                                   ProjectResource projectResource,
                                   QuoteOptionDependencyValidator quoteDependencyValidator) {
        super(presenter);
        this.orderRFOSheetOrchestrator = orderRFOSheetOrchestrator;
        this.projectResource = projectResource;
        this.quoteDependencyValidator = quoteDependencyValidator;
    }

    @Path("/validate")
    @GET
    public Response canRFOSheetBeExported(@PathParam("customerId") final String customerId,
                                          @PathParam("projectId") final String projectId,
                                          @PathParam("quoteOptionId") final String quoteOptionId) {

        QuoteOptionDTO quoteOptionDTO = projectResource.quoteOptionResource(projectId).get(quoteOptionId);
        Set<String> validationErrors = quoteDependencyValidator.validate(customerId, new RfoBillAccountCurrencyValidator(quoteOptionDTO.getCurrency()));
        if(!validationErrors.isEmpty()) {
            return ResponseBuilder.internalServerError().withEntity(Joiner.on(", ").join(validationErrors)).build();
        }

        return ResponseBuilder.anOKResponse().build();
    }

    @GET
    @Produces({"application/vnd.ms-excel"})
    public Response getRFOExportSheet(
        @PathParam("customerId") final String customerId,
        @PathParam("contractId") final String contractId,
        @PathParam("projectId") final String projectId,
        @PathParam("quoteOptionId") final String quoteOptionId,
        @PathParam("orderId") final String orderId) {
        final ExcelWorkbook excelWorkbook = orderRFOSheetOrchestrator.buildRFOExportExcelSheet(customerId, contractId, projectId, quoteOptionId, orderId);
        final XSSFWorkbook hssfWorkbook = excelWorkbook.getFile();
        ProjectDTO projectDTO = projectResource.get(projectId);
        final QuoteOptionResource quoteOptions = projectResource.quoteOptionResource(projectId);
        QuoteOptionDTO quoteOptionDTO = quoteOptions.get(quoteOptionId);
        String rfoSheetName = "SQE_"+projectDTO.name+"_"+quoteOptionDTO.getName()+"_"+"RFO Sheet"+".xlsx";

        final StreamingOutput streamingOutput = new StreamingOutput() {
            @Override
            public void write(OutputStream output) throws IOException, WebApplicationException {
                hssfWorkbook.write(output);
            }
        };
        return Response.ok(streamingOutput).header("Content-Disposition", "attachment; filename=" + rfoSheetName.replaceAll(" ","")).build();
    }

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.TEXT_HTML)
    public Response post(@PathParam("customerId") final String customerId,
                         @PathParam("contractId") final String contractId,
                         @PathParam("projectId") final String projectId,
                         @PathParam("quoteOptionId") final String quoteOptionId,
                         @PathParam("orderId") final String orderId,
                         @FormDataParam("rfoSheet") final InputStream rfoSheet) throws IOException {
        return new HandlerActionAttempt(true) {
            @Override
            protected Response action() throws Exception {
                try {
                    final XSSFWorkbook rfoWorkbook = new XSSFWorkbook(rfoSheet);
                    orderRFOSheetOrchestrator.importRfo(customerId, contractId, projectId, quoteOptionId, orderId, rfoWorkbook);
                } catch (RFOImportException e) {
                    AjaxResponseDTO dto = new AjaxResponseDTO(false, e.getMessage());
                    return Response.status(Response.Status.OK).entity(JSONSerializer.getInstance().serialize(dto)).build();
                }
                return Response.ok().entity(JSONSerializer.getInstance().serialize(new AjaxResponseDTO(true, ""))).build();
            }
        }.tryToPerformAction(quoteOptionId);
    }
}
