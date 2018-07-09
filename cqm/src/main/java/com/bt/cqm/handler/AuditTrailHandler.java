package com.bt.cqm.handler;

import com.bt.rsqe.expedio.audit.AuditDetailDTO;
import com.bt.rsqe.expedio.audit.AuditSummaryDTO;
import com.bt.rsqe.expedio.audit.AuditTrailResource;
import com.bt.rsqe.rest.ResponseBuilder;
import com.bt.rsqe.utils.AssertObject;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

import static com.bt.cqm.utils.Utility.*;

/**
 * Created with IntelliJ IDEA.
 * User: 608026723
 * Date: 3/26/15
 * Time: 7:17 PM
 * To change this template use File | Settings | File Templates.
 */
@Path("/cqm/audit")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuditTrailHandler {

    private static final String CUSTOMER_ID = "customerID";
    private static final String INVALID_CUSTOMER_ID = "Invalid customerId";
    AuditTrailResource auditTrailResource;

    public AuditTrailHandler(AuditTrailResource auditTrailResource) {
        this.auditTrailResource = auditTrailResource;
    }

    @GET
    @Path("/quoteSummary")
    public Response getQuoteAuditSummary(@QueryParam(CUSTOMER_ID) String customerId) {
        if (AssertObject.anyEmpty(customerId)) {
            return Response.status(Response.Status.BAD_REQUEST).entity(buildGenericError(INVALID_CUSTOMER_ID)).build();
        }
        List<AuditSummaryDTO> quoteSummaryList = auditTrailResource.getQuoteAuditSummary(customerId);
        GenericEntity<List<AuditSummaryDTO>> genericEntity = new GenericEntity<List<AuditSummaryDTO>>(quoteSummaryList){};
        return ResponseBuilder.anOKResponse().withEntity(genericEntity).build();
    }

    @GET
    @Path("/orderSummary")
    public Response getOrderAuditSummary(@QueryParam(CUSTOMER_ID) String customerId) {

        if (AssertObject.anyEmpty(customerId)) {
            return Response.status(Response.Status.BAD_REQUEST).entity(buildGenericError(INVALID_CUSTOMER_ID)).build();
        }
        List<AuditSummaryDTO> quoteSummaryList = auditTrailResource.getOrderAuditSummary(customerId);
        GenericEntity<List<AuditSummaryDTO>> genericEntity = new GenericEntity<List<AuditSummaryDTO>>(quoteSummaryList){};
        return Response.status(Response.Status.OK).entity(genericEntity).build();
    }

    @GET
    @Path("/orderDetail")
    public Response getQuoteAuditDetail(@QueryParam(CUSTOMER_ID) String customerId, @QueryParam("orderID") String orderId) {
        if (AssertObject.anyEmpty(customerId,orderId)) {
            return Response.status(Response.Status.BAD_REQUEST).entity(buildGenericError("Invalid customerId/quoteId")).build();
        }
        List<AuditDetailDTO> quoteDetailList = auditTrailResource.getOrderAuditDetail(customerId, orderId);
        GenericEntity<List<AuditDetailDTO>> genericEntity = new GenericEntity<List<AuditDetailDTO>>(quoteDetailList){};
        return Response.status(Response.Status.OK).entity(genericEntity).build();
    }

    @GET
    @Path("/quoteDetail")
    public Response getOrderAuditDetail(@QueryParam(CUSTOMER_ID) String customerId, @QueryParam("quoteID") String quoteId) {
        if (AssertObject.anyEmpty(customerId,quoteId)) {
            return Response.status(Response.Status.BAD_REQUEST).entity(buildGenericError("Invalid customerId/orderId")).build();
        }
        List<AuditDetailDTO> quoteDetailList = auditTrailResource.getQuoteAuditDetail(customerId, quoteId);
        GenericEntity<List<AuditDetailDTO>> genericEntity = new GenericEntity<List<AuditDetailDTO>>(quoteDetailList){};
        return Response.status(Response.Status.OK).entity(genericEntity).build();
    }

}
