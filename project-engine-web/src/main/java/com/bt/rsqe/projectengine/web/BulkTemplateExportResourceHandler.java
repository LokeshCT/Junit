package com.bt.rsqe.projectengine.web;

import com.bt.rsqe.projectengine.ProjectDTO;
import com.bt.rsqe.projectengine.ProjectResource;
import com.bt.rsqe.projectengine.QuoteOptionDTO;
import com.bt.rsqe.projectengine.QuoteOptionResource;
import com.bt.rsqe.projectengine.web.quoteoption.bulktemplatesheet.BulkTemplateExportSheetOrchestrator;
import com.bt.rsqe.projectengine.web.quoteoptionorders.rfosheet.ExcelWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.IOException;
import java.io.OutputStream;

@Path("/rsqe/customers/{customerId}/contracts/{contractId}/projects/{projectId}/quote-options/{quoteOptionId}/bulk-template-export")
@Produces(MediaType.TEXT_HTML)
public class BulkTemplateExportResourceHandler {

    private static final String CUSTOMER_ID = "customerId";
    private static final String CONTRACT_ID = "contractId";
    private static final String PROJECT_ID = "projectId";
    private static final String QUOTE_OPTION_ID = "quoteOptionId";
    private static final String SCODE = "sCode";
    private BulkTemplateExportSheetOrchestrator bulkTemplateExportSheetOrchestrator;
    private ProjectResource projectResource;

    public BulkTemplateExportResourceHandler(BulkTemplateExportSheetOrchestrator bulkTemplateExportSheetOrchestrator, ProjectResource projectResource) {
        this.bulkTemplateExportSheetOrchestrator = bulkTemplateExportSheetOrchestrator;
        this.projectResource = projectResource;
    }

    @GET
    @Path("/sCode/{sCode}")
    @Produces({"application/vnd.ms-excel"})
    public Response bulkTemplateExportSheet(@PathParam(PROJECT_ID) final String projectId,
                                      @PathParam(QUOTE_OPTION_ID) final String quoteOptionId,
                                      @PathParam(SCODE) final String sCode){

        final ExcelWorkbook excelWorkbook = bulkTemplateExportSheetOrchestrator.buildBulkTemplateExportSheet(sCode, quoteOptionId);

        final XSSFWorkbook xssfWorkbook = excelWorkbook.getFile();
        final ProjectDTO projectDTO = projectResource.get(projectId);
        final QuoteOptionResource quoteOptions = projectResource.quoteOptionResource(projectId);
        QuoteOptionDTO quoteOptionDTO = quoteOptions.get(quoteOptionId);
        String exportSheetName = "SQE_"+quoteOptionDTO.getName()+"_Template_"+excelWorkbook.getName();

        final StreamingOutput streamingOutput = new StreamingOutput() {
            @Override
            public void write(OutputStream output) throws IOException, WebApplicationException {
                xssfWorkbook.write(output);
            }
        };
        return Response.ok(streamingOutput).header("Content-Disposition", "attachment; filename=" + exportSheetName.replaceAll(" ","")).build();
    }
}
