package com.bt.rsqe.projectengine.web.error;

import com.bt.rsqe.error.RsqeApplicationException;
import com.bt.rsqe.web.Presenter;
import com.bt.rsqe.web.ViewFocusedResourceHandler;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;

@Path("/rsqe/static/exception")
public class EndToEndErrorSimulationResourceHandler extends ViewFocusedResourceHandler {

    private static final String TEST_UNCAUGHT_EXCEPTION_MESSAGE = "RunTime Error";
    private static final String TEST_CHECKED_EXCEPTION_MESSAGE = "IO Exception";
    private static final String RSQE_ERROR_PAGE_TITLE = "RSQE Error";
    private static final String ERROR_PAGE_HEADING = "Internal Server Error";
    private static final String ERROR_TITLE_ELEMENT = "error.title";

    public EndToEndErrorSimulationResourceHandler(final Presenter presenter) {
        super(presenter);
    }


    @GET
    @Path("/runtime")
    @Produces(MediaType.TEXT_HTML)
    public Response throwApplicationException() {
        try {
            String html = getNonErrorHtml();
            String array[] = new String[0];
            //should throw array index out of bound exception
            array[2].concat("JamesBond");
            return Response.ok().entity(html).build();
        } catch (RuntimeException ex) {
            throw new RsqeApplicationException(ex, "TEST_MESSAGE");
        }
    }

    @GET
    @Path("/runtimeuncaught")
    @Produces(MediaType.TEXT_HTML)
    public Response throwUnCaughtRuntimeException() {
        throw new ArrayIndexOutOfBoundsException(TEST_UNCAUGHT_EXCEPTION_MESSAGE);
    }

    @GET
    @Path("/freemarker")
    public Response respondWithFreemarkerError(@PathParam("customerId") String customerId) {
        return responseOk(new Presenter().render(view("InvalidReference.ftl")));
    }

    @GET
    @Path("/correct")
    @Produces(MediaType.TEXT_HTML)
    public Response respondWithCorrectPage() {
        String html = getNonErrorHtml();
        return Response.ok().entity(html).build();
    }

    @GET
    @Path("/checkedexception")
    @Produces(MediaType.TEXT_HTML)
    public Response throwCheckedException() throws IOException {
        throw new IOException(TEST_CHECKED_EXCEPTION_MESSAGE);
    }

    private String getNonErrorHtml() {
        return "<html><title>No SQE Error</title>" +
               "<body>No Error<body>" +
               "</html>";
    }

    private String getInternalServerErrorHtml() {
        return "<html><title>" + RSQE_ERROR_PAGE_TITLE + "</title>" +
               "<head><div id= " + ERROR_TITLE_ELEMENT + ">" + ERROR_PAGE_HEADING + "</div></head>" +
               "<body>Error<body>" +
               "</html>";
    }


}



