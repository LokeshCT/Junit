package com.bt.nrm.handler;

import com.bt.nrm.dto.EvaluatorActionsDTO;
import com.bt.nrm.dto.RequestEvaluatorDTO;
import com.bt.nrm.dto.RequestEvaluatorPriceGroupDTO;
import com.bt.nrm.dto.UserGroupDTO;
import com.bt.nrm.repository.EvaluatorActionRepository;
import com.bt.nrm.repository.QuoteOptionRequestRepository;
import com.bt.nrm.util.GeneralUtil;
import com.bt.rsqe.rest.ResponseBuilder;
import com.bt.rsqe.web.Presenter;
import com.bt.rsqe.web.ViewFocusedResourceHandler;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.bt.rsqe.utils.AssertObject.*;

@Path("/nrm/evaluator")
public class EvaluatorActionResourceHandler extends ViewFocusedResourceHandler {

    private final QuoteOptionRequestRepository requestRepository;
    private final EvaluatorActionRepository evaluatorActionRepository;

    public EvaluatorActionResourceHandler(final QuoteOptionRequestRepository requestRepository, EvaluatorActionRepository evaluatorActionRepository) {
        super(new Presenter());
        this.requestRepository = requestRepository;
        this.evaluatorActionRepository = evaluatorActionRepository;
    }

    @POST
    @Path("/getListOfEvaluatorActions")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getListOfEvaluatorActions(@QueryParam("userId") String userId, List<UserGroupDTO> userGroups) {
        try {
            if(isNotNull(userId) && isNotNull(userGroups)){
                //Get Groups Associated to User
                //List<UserGroupDTO> userGroups = getNRMUserGroupDTOs(userId);

                Map<String, List<EvaluatorActionsDTO>> map = evaluatorActionRepository.getAllEvaluatorActions(userGroups, userId);
                // System.out.println("Map : "+map);
                return ResponseBuilder.anOKResponse().withEntity(new GenericEntity<Map>(map){}).build();
            }
            return Response.status(Response.Status.BAD_REQUEST).build();

        } catch (Exception e){
            e.printStackTrace();
            return Response.status(Response.Status.EXPECTATION_FAILED).build();
        }
    }



    @POST
    @Path("/updateEvaluatorPriceGroup")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getEvaluatorActionDetailsUri(@QueryParam("requestId") String requestId,
                                                 Map<String,RequestEvaluatorPriceGroupDTO> requestEvaluatorPriceGroupDTOMap,
                                                 @QueryParam("modifiedBy") String modifiedBy) {
        try {
            if(isNotNull(requestId) && isNotNull(requestEvaluatorPriceGroupDTOMap)){
               System.out.print("requestEvaluatorPriceGroupDTOMap : "+requestEvaluatorPriceGroupDTOMap);
                int requestEvaluatorPriceGrpEntityUpdateCount = evaluatorActionRepository.updatePriceGroups(requestId, requestEvaluatorPriceGroupDTOMap, modifiedBy);
                if(requestEvaluatorPriceGrpEntityUpdateCount > 0){
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
    @Path("/startWorkingOnActionUri")
    @Produces(MediaType.APPLICATION_JSON)
    public Response startWorkingOnActionUri(RequestEvaluatorDTO requestEvaluatorDTO) {
        try {
            if(isNotNull(requestEvaluatorDTO) && isNotNull(requestEvaluatorDTO.getRequestEvaluatorId())){
                requestEvaluatorDTO.setAcceptedDate(new Date());
                requestEvaluatorDTO.setModifiedDate(GeneralUtil.getCurrentTimeStamp());
                int requestEvaluatorEntityUpdateCount = evaluatorActionRepository.acceptAgentAction(requestEvaluatorDTO);
                if(requestEvaluatorEntityUpdateCount > 0){
                    return ResponseBuilder.anOKResponse().withEntity(new GenericEntity<RequestEvaluatorDTO>(requestEvaluatorDTO) {}).build();
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

}
