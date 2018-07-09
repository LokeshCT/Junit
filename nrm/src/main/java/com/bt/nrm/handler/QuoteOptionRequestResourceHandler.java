package com.bt.nrm.handler;

import com.bt.nrm.dto.request.QuoteDTO;
import com.bt.nrm.dto.RequestDTO;
import com.bt.nrm.repository.QuoteOptionRequestRepository;
import com.bt.nrm.repository.entity.QuoteEntity;
import com.bt.nrm.repository.entity.RequestEntity;
import com.bt.rsqe.rest.ResponseBuilder;
import com.bt.rsqe.web.Presenter;
import com.bt.rsqe.web.ViewFocusedResourceHandler;
import com.google.common.base.Function;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

import static com.bt.rsqe.utils.AssertObject.*;
import static com.google.common.collect.Lists.*;

@Path("/nrm/requests")
public class QuoteOptionRequestResourceHandler extends ViewFocusedResourceHandler {

    private final QuoteOptionRequestRepository quoteOptionRequestRepository;

    public QuoteOptionRequestResourceHandler(final QuoteOptionRequestRepository quoteOptionRequestRepository) {
        super(new Presenter());
        this.quoteOptionRequestRepository = quoteOptionRequestRepository;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/getRequestsByUserId")
    public Response getRequestsByUserId(@QueryParam("userId") String userEIN) {
        try {
            if(isNotNull(userEIN)){
                List<RequestEntity> requestEntities = quoteOptionRequestRepository.getRequestsByUserId(userEIN);
                List<RequestDTO> requestDTOs = newArrayList(transform(requestEntities, new Function<RequestEntity, RequestDTO>() {
                    @Override
                    public RequestDTO apply(RequestEntity input) {
                        return input.toDTO(new RequestDTO());
                    }
                }));
                return ResponseBuilder.anOKResponse()
                                      .withEntity(new GenericEntity<List<RequestDTO>>(requestDTOs) {})
                                      .build();
            }else{
                return Response.status(Response.Status.BAD_REQUEST).build();
            }
        } catch (Exception e){
            e.printStackTrace();
            return Response.status(Response.Status.EXPECTATION_FAILED).build();
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/getRequestsByUserIdAndStates")
    public Response getRequestsByUserIdAndStates(@QueryParam("userId") String userEIN, @QueryParam("requestStates") String requestStates) {
        try {
            if(isNotNull(userEIN) && isNotNull(requestStates)){
                List<RequestEntity> requestEntities = quoteOptionRequestRepository.getRequestsByUserIdAndStates(userEIN, requestStates);
                List<RequestDTO> requestDTOs = newArrayList(transform(requestEntities, new Function<RequestEntity, RequestDTO>() {
                    @Override
                    public RequestDTO apply(RequestEntity input) {
                        return input.toDTO(new RequestDTO());
                    }
                }));
                return ResponseBuilder.anOKResponse().withEntity(new GenericEntity<List<RequestDTO>>(requestDTOs) {
                }).build();
            }else{
                return Response.status(Response.Status.BAD_REQUEST).build();
            }
        } catch (Exception e){
            e.printStackTrace();
            return Response.status(Response.Status.EXPECTATION_FAILED).build();
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/getRequestByRequestId")
    public Response getRequestByRequestId(@QueryParam("requestId") String requestId) {
        try {
            if(isNotNull(requestId)){
                RequestEntity request = quoteOptionRequestRepository.getRequestsByRequestId(requestId);
                GenericEntity<RequestDTO> entity = new GenericEntity<RequestDTO>(request.toDTO(new RequestDTO())){};
                return ResponseBuilder.anOKResponse().withEntity(entity).build();
            }else{
                return Response.status(Response.Status.BAD_REQUEST).build();
            }
        } catch (Exception e){
            e.printStackTrace();
            return Response.status(Response.Status.EXPECTATION_FAILED).build();
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/getDataBuildRequests")
    public Response getDataBuildRequests(@QueryParam("userId") String userId) {
        try {
            if(isNotNull(userId)){
                List<RequestEntity> requestEntities = quoteOptionRequestRepository.getDataBuildRequests(userId);
                List<RequestDTO> requestDTOs = newArrayList(transform(requestEntities, new Function<RequestEntity, RequestDTO>() {
                    @Override
                    public RequestDTO apply(RequestEntity input) {
                        return input.toDTO(new RequestDTO());
                    }
                }));
                return ResponseBuilder.anOKResponse()
                                      .withEntity(new GenericEntity<List<RequestDTO>>(requestDTOs) { })
                                      .build();
            }else{
                return Response.status(Response.Status.BAD_REQUEST).build();
            }
        } catch (Exception e){
            e.printStackTrace();
            return Response.status(Response.Status.EXPECTATION_FAILED).build();
        }
    }

    @POST
    @Path("/updateDataBuildStatus")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response updateDataBuildStatus(@QueryParam("requestId") String requestId, @QueryParam("dataBuildCompletedStatus") String dataBuildState,@QueryParam("modifiedBy") String modifiedBy) {
        try {
            if(isNotNull(requestId) && isNotNull(dataBuildState)){
                int noOfRowsUpdated = quoteOptionRequestRepository.updateDataBuildStatus(requestId,dataBuildState,modifiedBy);
                if(noOfRowsUpdated > 0){
                    return Response.status(Response.Status.OK).build();
                }
                return Response.status(Response.Status.NOT_FOUND).build();
            }else{
                return Response.status(Response.Status.BAD_REQUEST).build();
            }
        } catch (Exception e){
            e.printStackTrace();
            return Response.status(Response.Status.EXPECTATION_FAILED).build();
        }
    }

    @POST
    @Path("/saveRequestComments")
    public Response saveRequestComments(@QueryParam("modifiedBy") String modifiedBy, @QueryParam("requestId") String requestId, @QueryParam("comments") String comments) {
        try {
            if(isNotNull(requestId) && isNotNull(comments)){
                int noOfRowsUpdated = quoteOptionRequestRepository.saveRequestComments(modifiedBy, requestId, comments);
                if(noOfRowsUpdated > 0){
                    return Response.status(Response.Status.OK).build();
                }
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            return Response.status(Response.Status.BAD_REQUEST).build();
        }catch (Exception e){
            e.printStackTrace();
            return Response.status(Response.Status.EXPECTATION_FAILED).build();
        }
    }

    @POST
    @Path("/saveRequestGroupComments")
    public Response saveRequestGroupComments(@QueryParam("modifiedBy") String modifiedBy, @QueryParam("requestGroupId") String requestGroupId, @QueryParam("comments") String comments) {
        try {
             if(isNotNull(requestGroupId) && isNotNull(comments)){
                 int noOfRowsUpdated = quoteOptionRequestRepository.saveRequestGroupComments(modifiedBy, requestGroupId, comments);
                 if(noOfRowsUpdated > 0){
                     return Response.status(Response.Status.OK).build();
                 }
                 return Response.status(Response.Status.NOT_FOUND).build();
             }
             return Response.status(Response.Status.BAD_REQUEST).build();
        }catch (Exception e){
            e.printStackTrace();
            return Response.status(Response.Status.EXPECTATION_FAILED).build();
        }
    }


    @GET
    @Path("/getAllQuoteOptions")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllQuoteOptions(){
        try{
        List<QuoteEntity> quoteEntityList = quoteOptionRequestRepository.getAllQuoteOptions();
        List<QuoteDTO> quoteDTOList = newArrayList(transform(quoteEntityList, new Function<QuoteEntity, QuoteDTO>() {
            @Override
            public QuoteDTO apply(QuoteEntity input) {
                return input.toDTO(new QuoteDTO());
            }
        }));
        return ResponseBuilder.anOKResponse()
                              .withEntity(new GenericEntity<List<QuoteDTO>>(quoteDTOList) { })
                              .build();

        } catch(Exception e){
            e.printStackTrace();
            return Response.status(Response.Status.EXPECTATION_FAILED).build();
        }
    }

    @GET
    @Path("/getAllRequestsByQuoteId")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllRequestsByQuoteId(@QueryParam("quoteOptionId") String quoteOptionId){
        try{
        if(isNotNull(quoteOptionId)){
            List<RequestEntity> requestEntityList = quoteOptionRequestRepository.getAllRequestsByQuoteId(quoteOptionId);
            List<RequestDTO> requestDTOList = newArrayList(transform(requestEntityList, new Function<RequestEntity, RequestDTO>() {
                @Override
                public RequestDTO apply(RequestEntity input) {
                    return input.toDTO(new RequestDTO());
                }
            }));
            return ResponseBuilder.anOKResponse()
                                  .withEntity(new GenericEntity<List<RequestDTO>>(requestDTOList) { })
                                  .build();
        }
        else{
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
     }catch(Exception e){
            e.printStackTrace();
            return Response.status(Response.Status.EXPECTATION_FAILED).build();
        }
    }
}
