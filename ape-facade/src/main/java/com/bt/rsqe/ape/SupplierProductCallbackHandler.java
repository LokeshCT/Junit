package com.bt.rsqe.ape;

import com.bt.rsqe.ape.repository.entities.AvailabilityRequestQueue;
import com.bt.rsqe.ape.repository.entities.SupplierCheckClientRequestEntity;
import com.bt.rsqe.ape.repository.entities.SupplierRequestSiteEntity;
import com.bt.rsqe.ape.source.SupplierProductStore;
import com.bt.rsqe.ape.source.extractor.ResponseExtractorStrategyFactory;
import com.bt.rsqe.customerrecord.CustomerResource;
import com.bt.rsqe.customerrecord.SiteResource;
import com.bt.rsqe.logging.Log;
import com.bt.rsqe.logging.LogFactory;
import com.bt.rsqe.logging.LogLevel;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

import static com.bt.rsqe.ape.constants.SupplierProductConstants.*;
import static com.bt.rsqe.ape.source.SupplierProductHelper.*;
import static com.bt.rsqe.ape.source.SupplierProductStore.*;
import static com.google.common.collect.Lists.newArrayList;
import static javax.ws.rs.core.Response.Status.*;

@Path("/rsqe/ape-facade/supplier-product-callback/")
public class SupplierProductCallbackHandler {

    private SupplierProductCallbackLogger logger = LogFactory.createDefaultLogger(SupplierProductCallbackLogger.class);
    private ResponseExtractorStrategyFactory strategyFactory;
    private CustomerResource customerResource;

    public SupplierProductCallbackHandler(CustomerResource customerResource, ResponseExtractorStrategyFactory strategyFactory) {
        this.customerResource = customerResource;
        this.strategyFactory = strategyFactory;
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes({MediaType.APPLICATION_XML, MediaType.TEXT_XML})
    @Path("async/requestId/{requestId}")
    public Response supplierProductCallback(@PathParam("requestId") String requestId, String payload) {
        logger.supplierProductCallbackResponse(requestId, payload);
        try {
            String operation = getOperation(payload);
            storeLog(requestId, RESPONSE, operation, payload, APE_CALLBACK, "");
            if (!isRequestIdValid(requestId)) { //TODO: Discard if requested sites are timed out
                logger.invalidRequest(requestId);
                return Response.status(BAD_REQUEST).build();
            }
            SiteResource siteResource = customerResource.siteResource(getCustomerIdByApeRequestId(requestId));
            strategyFactory.getExtractorStrategy(operation, null).extractResponse(payload, siteResource);

            if (isAutoTriggerRequired(requestId, operation)) {
                addRequestToQueue(requestId);
            }
        } catch (Exception e) {
            logger.supplierProductCallbackResponseError(e);
            return Response.ok("Callback response received successfully but not updated").build();
        }

        return Response.ok("Callback response received successfully.").build();
    }


    private void addRequestToQueue(String requestId) throws Exception {
        SupplierCheckClientRequestEntity client = getClientRequestByApeRequestId(requestId);
        List<AvailabilityRequestQueue> queues = newArrayList();
        for (SupplierRequestSiteEntity siteEntity : client.getSupplierRequestSiteEntities()) {
            queues.add(new AvailabilityRequestQueue(client.getId(), requestId, siteEntity.getSiteId(), "AvailabilityCheck", "Queued", timestamp()));
        }
        SupplierProductStore.storeRequestQueue(queues);
    }

    private boolean isAutoTriggerRequired(String requestId, String responseType) throws Exception {
        return isAvailabilityCheckAutoTriggerRequired(requestId) && SUPPLIER_PRODUCT_LIST_RESPONSE.equalsIgnoreCase(responseType);
    }

    private String getOperation(String payload) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(new InputSource(new ByteArrayInputStream(payload.getBytes(UTF_8))));
        doc.getDocumentElement().normalize();
        return doc.getFirstChild().getNodeName();
    }

    private interface SupplierProductCallbackLogger {

        @Log(level = LogLevel.INFO, format = "Callback response for '%s' is '%s'")
        void supplierProductCallbackResponse(String requestId, String apeResponseXml);

        @Log(level = LogLevel.ERROR, format = "Callback response error : '%s'")
        void supplierProductCallbackResponseError(Exception error);

        @Log(level = LogLevel.ERROR, format = "Request : '%s' is not valid one to process")
        void invalidRequest(String requestId);
    }
}
