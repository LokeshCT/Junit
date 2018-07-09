package com.bt.cqm.handler;


import com.bt.rsqe.domain.AttachmentDTO;
import com.bt.rsqe.emppal.attachmentresource.EmpPalResource;
import com.bt.rsqe.expedio.order.OrderDTO;
import com.bt.rsqe.expedio.order.OrderSearchResource;
import com.bt.rsqe.expedio.order.OrderLineItemDTO;
import com.bt.rsqe.utils.AssertObject;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;

import static javax.ws.rs.core.Response.*;


@Path("/cqm/order")
@Produces({MediaType.APPLICATION_JSON})
@Consumes({MediaType.APPLICATION_JSON})
public class OrderDetailsHandler {


    private static final Logger LOG = LoggerFactory.getLogger(OrderDetailsHandler.class);
    private static final String SALES_CHANNEL = "salesChannel";
    private static final String FILE = "file";
    private static final String BFG_CUS_ID = "bfgCusId";
    private static final String ATTACH_TYPE = "attachType";
    private static final String QUOTE_ID = "quoteId";
    private static final String DOCUMENT_ID = "documentId";
    private static final String FILE_NAME = "fileName";
    private OrderSearchResource orderSearchResource = null;
    private EmpPalResource empPalResource = null;
    private static final String EMPTY_INPUT_ARG="Empty or Null input argument !!";

    public OrderDetailsHandler(OrderSearchResource orderSearchResource, EmpPalResource empPalResource) {
        this.orderSearchResource = orderSearchResource;
        this.empPalResource = empPalResource;
    }

    @GET
    @Path("search")
    public Response searchOrderDetails(@QueryParam(SALES_CHANNEL) String salesChannel, @QueryParam("customerID") String customerId) {
        if(AssertObject.anyEmpty(salesChannel,customerId)){
            return Response.status(Status.NOT_FOUND).entity(EMPTY_INPUT_ARG).build();
        }

        List<OrderDTO> orders = orderSearchResource.getOrder(salesChannel, customerId);

        if (orders != null) {
            return Response.status(Status.OK).entity(new GenericEntity<List<OrderDTO>>(orders) {
            }).build();
        } else {
            return Response.status(Status.NOT_FOUND).build();
        }
    }


    @GET
    @Path("orderLineItems")
    public Response getOrderLineItems(@QueryParam("orderID") String orderId) {

        if(AssertObject.anyEmpty(orderId)){
            return Response.status(Status.NOT_FOUND).entity(EMPTY_INPUT_ARG).build();
        }

        List<OrderLineItemDTO> orderLineItems = orderSearchResource.getOrderDetail(orderId);

        if (orderLineItems != null) {
            return Response.status(Status.OK).entity(new GenericEntity<List<OrderLineItemDTO>>(orderLineItems) {
            }).build();
        } else {
            return Response.status(Status.NOT_FOUND).build();
        }
    }


    @POST
    @Path("uploadAttachment")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.TEXT_PLAIN)
    public Response uploadAttachment(@FormDataParam(FILE) InputStream fileInputStream,
                                     @FormDataParam(FILE) FormDataContentDisposition contentDispositionHeader,
                                     @QueryParam(SALES_CHANNEL) String salesChannel,
                                     @QueryParam(BFG_CUS_ID) String customerId,
                                     @QueryParam(ATTACH_TYPE) String attachmentType,
                                     @QueryParam(QUOTE_ID) String quoteId) {
        String serviceRet = null;
        String parentPath = null;
        AttachmentDTO doc = null;
        if (AssertObject.anyEmpty(salesChannel, customerId, attachmentType, quoteId,fileInputStream, contentDispositionHeader)) {
            return Response.status(Status.NOT_FOUND).entity(EMPTY_INPUT_ARG).build();
        }

        try {
            parentPath = buildSpParentFldPath(salesChannel, customerId, attachmentType, quoteId);

            doc = new AttachmentDTO(parentPath);
            doc.setFileName(contentDispositionHeader.getFileName());
            doc.setFileContent(toByteArray(fileInputStream));
        } catch (Exception ex) {
            LOG.error("Exception in Uploading Doc to EMP PAL.", ex);
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(ex.getMessage()).build();
        }
        serviceRet = empPalResource.uploadAttachment(doc, attachmentType);


        return Response.status(Status.OK).entity(serviceRet).build();
    }

    @GET
    @Path("/listAttachments")
    public Response listOrderAttachments(@QueryParam(SALES_CHANNEL) String salesChannel,
                                         @QueryParam(BFG_CUS_ID) String customerId,
                                         @QueryParam(ATTACH_TYPE) String attachmentType,
                                         @QueryParam(QUOTE_ID) String quoteId) {

        List<AttachmentDTO> availableDocs = null;
        String parentPath = null;

        if (AssertObject.anyEmpty(salesChannel, customerId, attachmentType, quoteId)) {
            return Response.status(Status.NOT_FOUND).entity(EMPTY_INPUT_ARG).build();
        }

        try {
            parentPath = buildSpParentFldPath(salesChannel, customerId, attachmentType, quoteId);

        } catch (Exception ex) {
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(ex.getMessage()).build();
        }

        availableDocs = empPalResource.getAvailableAttachments(parentPath);

        if (availableDocs == null || availableDocs.size() < 1) {
            return Response.status(Status.NOT_FOUND).build();
        }

        return Response.status(Status.OK).entity(availableDocs).build();

    }

    @GET
    @Path("/getAttachment")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getOrderAttachment(@QueryParam(SALES_CHANNEL) String salesChannel,
                                       @QueryParam(BFG_CUS_ID) String customerId,
                                       @QueryParam(ATTACH_TYPE) String attachmentType,
                                       @QueryParam(QUOTE_ID) String quoteId,
                                       @QueryParam(DOCUMENT_ID) String documentId,
                                       @QueryParam(FILE_NAME) String fileName) {

        String parentPath = null;
        if (AssertObject.anyEmpty(salesChannel, customerId, attachmentType, quoteId, documentId, fileName)) {
            return Response.status(Status.NOT_FOUND).entity(EMPTY_INPUT_ARG).build();
        }


        parentPath = buildSpParentFldPath(salesChannel, customerId, attachmentType, quoteId);


        AttachmentDTO doc = empPalResource.getAttachment(parentPath, documentId);

        if (doc == null) {
            return Response.status(Status.NOT_FOUND).build();
        }

        return Response.status(Status.OK).entity(doc.getFileContent()).header("Content-Disposition",
                                                                              "attachment; filename=" + fileName).build();
    }

    private byte[] toByteArray(InputStream iStream) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int reads = iStream.read();

        while (reads != -1) {
            baos.write(reads);
            reads = iStream.read();
        }

        return baos.toByteArray();

    }

    private String buildSpParentFldPath(String salesChn, String custId, String attachType, String quoteId) {
        Long custID = Long.parseLong(custId);
        long lower = (custID / 5000) * 5000;
        long higher = lower + 5000;
        String range = lower + 1 + "-" + higher;
        String finalPath;

        String quoteID = "Sales".equals(attachType) ? "/" + quoteId : "";

        finalPath = salesChn + "/" + range + "/" + custID + "/" + attachType + quoteID;

        return finalPath;

    }


}
