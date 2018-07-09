package com.bt.nrm.handler.interfaces;

import com.bt.nrm.dto.request.NonStandardRequestDTO;
import com.bt.nrm.dto.response.CheckNonStandardRequestStatusResponseDTO;
import com.bt.nrm.dto.response.NonStandardRequestResponseDTO;
import com.bt.nrm.handler.NRMEmailHandler;
import com.bt.nrm.repository.QuoteOptionRequestRepository;
import com.bt.nrm.repository.entity.RequestEntity;
import com.bt.nrm.request.RequestCreationUtil;
import com.bt.nrm.util.Constants;
import com.bt.pms.dto.TemplateDTO;
import com.bt.pms.resources.PMSResource;
import com.bt.rsqe.EmailService;
import com.bt.rsqe.rest.ResponseBuilder;
import com.bt.rsqe.web.Presenter;
import com.bt.rsqe.web.ViewFocusedResourceHandler;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static com.bt.rsqe.utils.AssertObject.*;

@Path("/nrm/interface")
public class NonStandardRequestResponseHandler extends ViewFocusedResourceHandler {
    private PMSResource pmsResource;
    private QuoteOptionRequestRepository requestRepository;
    private RequestCreationUtil requestCreationUtil;
    private final EmailService emailService;

    public NonStandardRequestResponseHandler(PMSResource pmsResource, QuoteOptionRequestRepository requestRepository, EmailService emailService) {
        super(new Presenter());
        this.pmsResource = pmsResource;
        this.requestRepository = requestRepository;
        this.requestCreationUtil = new RequestCreationUtil(requestRepository, pmsResource);
        this.emailService = emailService;
    }

    @POST
    @Path("/createNonStandardRequest")
    public Response createNonStandardRequest(NonStandardRequestDTO requestDTO) {
        //Test Data creation
        requestDTO = requestCreationUtil.getTestRequestData(); //ToDo this line needs to be removed once rSQE/SQE integration is complete
        NonStandardRequestResponseDTO errorDTO = new NonStandardRequestResponseDTO();
        try {
            if (isNotNull(requestDTO)) {
                if (isNotNull(requestDTO.getQuote())) {
                    if (isNotNull(requestDTO.getProduct())) {
                        if (isNotNull(requestDTO.getSites()) && requestDTO.getSites().size() > 0) {
                            //Fetch template
                            TemplateDTO templateDTO = pmsResource.getCompleteTemplateDetails(requestDTO.getTemplateCode(), requestDTO.getTemplateVersion());

                            //Read requestDTO, templateDTO and convert it into NRM specific request Entity
                            RequestEntity requestEntity = requestCreationUtil.constructNRMRequestEntity(requestDTO, templateDTO);

                            //Save Request into database
                            requestEntity = requestRepository.createRequest(requestEntity);

                            //Send email to customer regarding request creation
                            new NRMEmailHandler(emailService).sendEmail(requestCreationUtil.constructMessageSubjectForRequestCreation(requestEntity.getRequestId()), requestCreationUtil.constructMessageBodyForRequestCreation(requestEntity.getRequestId()), requestEntity.getQuote().getCreatedByEmailId());

                            NonStandardRequestResponseDTO nonStandardRequestResponseDTO = new NonStandardRequestResponseDTO(String.valueOf(Response.Status.OK.getStatusCode()), null, null,
                                    Constants.REQUEST_CREATION_SUCCESSFUL, requestEntity.getState(), requestEntity.getQuote().getQuoteId(), requestEntity.getQuote().getQuoteOptionId(),
                                    requestEntity.getRequestId() , requestEntity.getPublicURL());

                            //Fill values in nonStandardRequestResponseDTO object
                            return ResponseBuilder.anOKResponse()
                                    .withEntity(new GenericEntity<NonStandardRequestResponseDTO>(nonStandardRequestResponseDTO) {
                                    })
                                    .build();
                        } else {
                            errorDTO.setErrorDescription("");
                        }
                    } else {
                        errorDTO.setErrorDescription("");
                    }
                } else {
                    errorDTO.setErrorDescription("");
                }
            } else {
                errorDTO.setErrorDescription("");
            }
            errorDTO.setErrorCode(String.valueOf(Response.Status.EXPECTATION_FAILED.getStatusCode()));
            return ResponseBuilder.anOKResponse()
                    .withEntity(new GenericEntity<NonStandardRequestResponseDTO>(errorDTO) {
                    })
                    .build();
        } catch (Exception e){
            e.printStackTrace();
            errorDTO.setErrorCode(String.valueOf(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()));
            errorDTO.setErrorDescription(e.getMessage());
            return ResponseBuilder.anOKResponse()
                    .withEntity(new GenericEntity<NonStandardRequestResponseDTO>(errorDTO) {})
                    .build();
        }
    }

    @GET
    @Path("/checkNonStandardRequestStatus")
    @Produces(MediaType.APPLICATION_JSON)
    public Response checkNonStandardRequestStatus(@QueryParam("quoteId") String quoteId, @QueryParam("quoteOptionId") String quoteOptionId, @QueryParam("requestId") String requestId) {
        CheckNonStandardRequestStatusResponseDTO checkNonStandardRequestStatusResponseDTO = new CheckNonStandardRequestStatusResponseDTO();
        //Fill the response DTO
        return ResponseBuilder.anOKResponse()
                .withEntity(new GenericEntity<CheckNonStandardRequestStatusResponseDTO>(checkNonStandardRequestStatusResponseDTO) {})
                .build();
    }


}
