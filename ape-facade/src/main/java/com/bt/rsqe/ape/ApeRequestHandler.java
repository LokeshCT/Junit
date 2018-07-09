package com.bt.rsqe.ape;

import com.bt.rsqe.ape.client.APEClient;
import com.bt.rsqe.ape.config.CallbackEndpointConfig;
import com.bt.rsqe.ape.dto.ApeQrefRequestDTO;
import com.bt.rsqe.ape.repository.APEQrefRepository;
import com.bt.rsqe.ape.repository.entities.ApeRequestEntity;
import com.bt.rsqe.ape.repository.entities.SupplierProductEntity;
import com.bt.rsqe.ape.source.QrefSourceStrategy;
import com.bt.rsqe.ape.source.QrefSourceStrategyFactory;
import com.bt.rsqe.customerrecord.CustomerResource;
import com.bt.rsqe.domain.QrefRequestStatus;
import com.bt.rsqe.logging.Log;
import com.bt.rsqe.logging.LogFactory;
import com.bt.rsqe.logging.LogLevel;
import com.bt.rsqe.rest.ResponseBuilder;
import com.bt.rsqe.web.rest.exception.ResourceNotFoundException;
import com.google.common.base.Function;
import com.google.common.collect.Lists;

import javax.annotation.Nullable;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

import static com.google.common.collect.Lists.*;

@Path("/rsqe/ape-facade/access/request/{uniqueId}")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ApeRequestHandler {
    private static final String UNIQUE_ID = "uniqueId";
    private Logger logger = LogFactory.createDefaultLogger(Logger.class);
    private final APEQrefRepository apeQrefRepository;
    private final CallbackEndpointConfig callbackEndpointConfig;
    private final QrefSourceStrategyFactory qrefSourceStrategyFactory;
    private final CustomerResource customerResource;

    public ApeRequestHandler(APEQrefRepository apeQrefRepository, APEClient apeClient, CallbackEndpointConfig callbackEndpointConfig, CustomerResource customerResource) {
        this.apeQrefRepository = apeQrefRepository;
        this.callbackEndpointConfig = callbackEndpointConfig;
        this.qrefSourceStrategyFactory = new QrefSourceStrategyFactory(apeQrefRepository, apeClient);
        this.customerResource = customerResource;
    }

    @GET
    @Path("status")
    public Response getAccessRequestStatus(@PathParam(UNIQUE_ID) String accessUniqueId) {
        try {
            ApeRequestEntity apeRequest = apeQrefRepository.getAPERequestByUniqueId(accessUniqueId);
            if (apeRequest != null) {
                return ResponseBuilder.anOKResponse().withEntity(apeRequest.toQrefRequestStatusDto()).build();
            }
            return ResponseBuilder.notFound().build();
        } catch (ResourceNotFoundException ex) {
            return ResponseBuilder.notFound().build();
        }
    }

    @GET
    @Path("status/{requestId}")
    public Response getAccessRequestStatusForRequestId(@PathParam("requestId") String requestId) {
        try {
            ApeRequestEntity apeRequest = apeQrefRepository.getAPERequestByRequestId(requestId);
            if (apeRequest != null && !QrefRequestStatus.Status.CANCELLED.equals(apeRequest.getStatus())) {
                return ResponseBuilder.anOKResponse().withEntity(apeRequest.toQrefRequestStatusDto()).build();
            }
        } catch (ResourceNotFoundException ex) {

        }

        return ResponseBuilder.notFound().build();
    }

    @PUT
    public Response updateRequestStatus(@PathParam(UNIQUE_ID) String uniqueId, QrefRequestStatus requestStatus) {
        ApeRequestEntity apeRequest = apeQrefRepository.getAPERequestByUniqueId(uniqueId);
        if(!isExpectedRequest(apeRequest, requestStatus.getRequestId())) {
            // requests are not the same so return Not Found!
            return ResponseBuilder.notFound().build();
        }

        apeRequest.setStatus(requestStatus.getStatus());
        apeRequest.setErrorMessage(requestStatus.getErrorMessage());
        apeQrefRepository.save(apeRequest);
        return ResponseBuilder.anOKResponse().build();
    }

    @POST
    public Response create(@PathParam(UNIQUE_ID) String uniqueId, ApeQrefRequestDTO requestDTO) {
        logger.createApeRequest(uniqueId, requestDTO);

        String syncUri = String.format(callbackEndpointConfig.getUri(), uniqueId);
        QrefSourceStrategy qrefSourceStrategy = qrefSourceStrategyFactory.getSourceStrategy(requestDTO);
        requestDTO.setSupplierProducts(getSupplierProducts(requestDTO.siteDetail().getSiteId().getValue()));
        QrefRequestStatus qrefRequestStatus = qrefSourceStrategy.requestQrefs(syncUri, uniqueId,customerResource);

        return ResponseBuilder.anOKResponse().withEntity(qrefRequestStatus).build();
    }

    @POST
    @Path("cancel")
    public Response cancel(@PathParam(UNIQUE_ID) String accessUniqueId) {
        try {
            ApeRequestEntity apeRequest = apeQrefRepository.getAPERequestByUniqueId(accessUniqueId);
            if (apeRequest != null) {
                /*
                    (Marcus) TODO in R28 - come up with a a mechanism for deleting QREFs, being careful not to
                    delete QREF's (essentially stencils) that are still needed for existing assets in CIF
                 */
				//final List<ApeQrefDetailEntity> apeQrefsByUniqueId = apeQrefRepository.getAPEQrefsByUniqueId(accessUniqueId);
                //for (ApeQrefDetailEntity apeQrefDetailEntity : apeQrefsByUniqueId) {
                //                  apeQrefRepository.deleteApeQref(apeQrefDetailEntity.getQrefId());
                //              }

                apeRequest.setStatus(QrefRequestStatus.Status.CANCELLED);
                apeQrefRepository.save(apeRequest);

                return ResponseBuilder.anOKResponse().build();
            }
            return ResponseBuilder.notFound().build();
        } catch (ResourceNotFoundException ex) {
            return ResponseBuilder.notFound().build();
        }
    }

    public List<SupplierProduct> getSupplierProducts(final Long siteId) {
        List<SupplierProduct> supplierProducts = newArrayList();
        try {
            supplierProducts = getSupplierProductEntityList(apeQrefRepository.getSupplierProducts(siteId));
        } catch (Exception e) {
            //do nothing as there may be a case where no supplier products are fetched for site
        }
        return supplierProducts;
    }

    private List<SupplierProduct> getSupplierProductEntityList(final List<SupplierProductEntity> list) {
        return newArrayList(Lists.transform(list, new Function<SupplierProductEntity, SupplierProduct>() {
            @Override
            public SupplierProduct apply(@Nullable SupplierProductEntity input) {
                SupplierProduct product = new SupplierProduct();
                if(input!=null){
                    product.setSPACID(input.getSpacId());
                    product.setAvailabilityStatus(input.getStatus());
                    product.setSymetricBandwidth(input.getSymmetricSpeedBandwidth());
                    product.setMaxUpstreamBandwidth(input.getMaxUpstreamSpeedBandwidth());
                    product.setMaxDownstreamBandwidth(input.getMaxDownstreamBandwidth());
                    product.setExchangeCode(input.getExchangeCode());
                    product.setCopperPairNumber(String.valueOf(input.getNumberOfCopperPairs()));
                    product.setCheckReference(input.getCheckedReference());
                    product.setSupplierTariffZone(input.getSupplierTariffZone());
                }
                return product;
            }
        }));
    }

    private boolean isExpectedRequest(final ApeRequestEntity request, final String requestId) {
        return request.getRequestId().equals(requestId);
    }

    public static interface Logger {
        @Log(level = LogLevel.DEBUG, format = "APE Request for unique ID %s. %s")
        void createApeRequest(String uniqueId, ApeQrefRequestDTO apeQrefRequest);
    }
}
