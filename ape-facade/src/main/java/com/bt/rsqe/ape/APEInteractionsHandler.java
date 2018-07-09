package com.bt.rsqe.ape;

import com.bt.rsqe.ape.client.APEClient;
import com.bt.rsqe.ape.config.ApeMappingConfigLoader;
import com.bt.rsqe.ape.config.LocalIdentifier;
import com.bt.rsqe.ape.config.OfferingAttribute;
import com.bt.rsqe.ape.dto.ApeQref;
import com.bt.rsqe.ape.dto.ApeQrefAttributeDetail;
import com.bt.rsqe.ape.dto.ManualQuote;
import com.bt.rsqe.ape.dto.ManualQuoteItem;
import com.bt.rsqe.ape.dto.QrefRecallDTO;
import com.bt.rsqe.ape.repository.APEQrefRepository;
import com.bt.rsqe.ape.repository.entities.AccessUserCommentsEntity;
import com.bt.rsqe.ape.repository.entities.ApeQrefDetailEntity;
import com.bt.rsqe.ape.repository.entities.ApeRequestEntity;
import com.bt.rsqe.ape.workflow.AccessWorkflowStatus;
import com.bt.rsqe.ape.workflow.TransitionName;
import com.bt.rsqe.customerrecord.CustomerDTO;
import com.bt.rsqe.customerrecord.CustomerResource;
import com.bt.rsqe.customerrecord.UserResource;
import com.bt.rsqe.domain.AccessUserCommentsDTO;
import com.bt.rsqe.domain.QrefIdFormat;
import com.bt.rsqe.logging.Log;
import com.bt.rsqe.logging.LogFactory;
import com.bt.rsqe.logging.LogLevel;
import com.bt.rsqe.rest.ResponseBuilder;
import com.bt.rsqe.security.Credentials;
import com.bt.rsqe.security.ExpedioUserContextResolver;
import com.bt.rsqe.security.UserDTO;
import com.bt.rsqe.utils.Lists;
import com.google.common.base.Joiner;
import com.google.common.base.Optional;

import javax.ws.rs.CookieParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.bt.rsqe.utils.AssertObject.*;
import static com.google.common.base.Strings.*;
import static com.google.common.collect.Lists.*;

@Path("/rsqe/ape-facade")
public class APEInteractionsHandler extends ApeHandler {
    private Logger logger = LogFactory.createDefaultLogger(Logger.class);

    private APEClient apeClient;
    private APEQrefRepository apeQrefRepository;
    private ExpedioUserContextResolver expedioUserContextResolver;
    private UserResource expedioUserResource;
    private CustomerResource customerResource;
    private static final String WORKFLOW_STATUS = "WORKFLOW STATUS";

    public APEInteractionsHandler(APEClient apeClient, APEQrefRepository apeQrefRepository,
                                  ExpedioUserContextResolver expedioUserContextResolver,
                                  UserResource expedioUserResource, CustomerResource customerResource) {
        this.apeClient = apeClient;
        this.apeQrefRepository = apeQrefRepository;
        this.expedioUserContextResolver = expedioUserContextResolver;
        this.expedioUserResource = expedioUserResource;
        this.customerResource = customerResource;
    }

    @GET
    @Path("/getManualQuote/qrefId/{qrefId}")
    public Response getManualQuote(@PathParam("qrefId") String qrefId) {
        logger.getManualQuoteCalled(qrefId);
        ManualQuoteItem manualQuoteItem = new ManualQuoteItem(qrefId);
        List<ManualQuoteItem> manualQuoteItems = newArrayList();
        manualQuoteItems.add(manualQuoteItem);
        ManualQuote manualQuote = new ManualQuote(manualQuoteItems);
        final ApeQref apeQrefs = apeQrefRepository.getApeQref(qrefId);
        ApeRequestEntity apeRequestEntity = apeQrefRepository.getAPERequestByRequestId(apeQrefs.getRequestId());
        manualQuote.setPstnTelephoneLine(apeRequestEntity.getPstnTelLine());
        manualQuote.setSiteTelephoneNumber(apeRequestEntity.getSiteTelNumber());
        manualQuote.setExpectedResponseTime(apeRequestEntity.getExpectedResponseTime());

        if (manualQuote == null) {
            return ResponseBuilder.anOKResponse().withEntity(new ManualQuote()).build();
        } else {
            logger.createdManualQuote(manualQuote);
            return ResponseBuilder.anOKResponse().withEntity(manualQuote).build();
        }
    }


    @POST
    @Path("/saveManualQuote")
    public Response saveManualQuote(ManualQuote manualQuote) {
        ManualQuoteItem manualQuoteItem = manualQuote.getManualQuoteItems().get(0);
        final ApeQref apeQrefs = apeQrefRepository.getApeQref(manualQuoteItem.getQrefId());
        ApeRequestEntity apeRequestEntity = apeQrefRepository.getAPERequestByRequestId(apeQrefs.getRequestId());
        apeRequestEntity.setPstnTelLine(manualQuote.getPstnTelephoneLine());
        apeRequestEntity.setSiteTelNumber(manualQuote.getSiteTelephoneNumber());
        apeRequestEntity.setExpectedResponseTime(manualQuote.getExpectedResponseTime());
        apeQrefRepository.save(apeRequestEntity);

        return ResponseBuilder.anOKResponse().build();

    }
    @POST
        @Path("/manual-quote/customerId/{customerId}/projectId/{projectId}")
    public Response manualQuote(ManualQuote manualQuote, @PathParam("customerId") String customerId,
                                                         @PathParam("projectId") String projectId,
                                                         @CookieParam(Credentials.RSQE_TOKEN) String userToken) {

        List<String> errorDetails = checkManualQuoteInputs(manualQuote);
        if(!errorDetails.isEmpty()) {
            return ResponseBuilder.badRequest().withEntity("Missing values: " + Joiner.on(", ").join(errorDetails)).build();
        }

        SqeQrefPairCommentsInput[] manualQuoteRequest = new SqeQrefPairCommentsInput[manualQuote.getManualQuoteItems().size()];

        Map<String, ApeQref> qrefs = new HashMap<String, ApeQref>();
        HashMap<String, String> invalidTransitions = new HashMap<String, String>();

        for(int a = 0; a< manualQuoteRequest.length; a++) {
            ManualQuoteItem manualQuoteItem = manualQuote.getManualQuoteItems().get(a);

            final ApeQref apeQref = apeQrefRepository.getApeQref(manualQuoteItem.getQrefId());
            final QrefAttributeExtractor attributeExtractor = new QrefAttributeExtractor(apeQref);
            final String apeQrefReference = attributeExtractor.getAttributeValue(LocalIdentifier.QREF);

            qrefs.put(apeQrefReference, apeQref);

            if(!transitionAllowed(TransitionName.MANUAL_QUOTE, attributeExtractor)) {
                invalidTransitions.put(apeQrefReference, INVALID_WORKFLOW_STATE_ERROR);
                continue;
            }
            List<AccessUserCommentsEntity> userComments = apeQrefRepository.getUserCommentsForQrefId(manualQuoteItem.getQrefId());

            String addressError = manualQuote.validateMandatoryAddressFieldDetails(QrefIdFormat.convert(apeQrefReference), (userComments == null || userComments.isEmpty()) ? null : userComments.get(0).getComment());
            if (isNotNull(addressError)) {
                invalidTransitions.put(apeQrefReference, addressError);
                continue;
            }


            SqeQrefPairCommentsInput manualQuoteRequestPiece = new SqeQrefPairCommentsInput();
            manualQuoteRequestPiece.setQref(apeQrefReference);
            manualQuoteRequestPiece.setPairId(attributeExtractor.getAttributeValue(LocalIdentifier.PAIR_ID));

            UserDTO userDTO = expedioUserResource.findUser(expedioUserContextResolver.resolve(userToken, projectId).getLoginName());
            manualQuoteRequestPiece.setSalesUserFirstName(userDTO.getForename());
            manualQuoteRequestPiece.setSalesUserLastName(userDTO.getSurname());
            manualQuoteRequestPiece.setSalesUserEMailId(userDTO.getEmail());
            manualQuoteRequestPiece.setSalesUserPhoneNo(userDTO.getPhoneNumber());
            if(null != manualQuote.getBfgContact()) {
                manualQuoteRequestPiece.setPocEmail(manualQuote.getBfgContact().getEmailAddress());
                manualQuoteRequestPiece.setPocFirstName(manualQuote.getBfgContact().getFirstName());
                manualQuoteRequestPiece.setPocLastName(manualQuote.getBfgContact().getLastName());
            }
            manualQuoteRequestPiece.setPocPhoneNo(manualQuote.getSiteTelephoneNumber());
            manualQuoteRequestPiece.setPocPSTNNo(manualQuote.getPstnTelephoneLine());
            if (isNullOrEmpty(manualQuoteItem.getMbpFlag()) && "MBP".equalsIgnoreCase(manualQuoteItem.getMbpFlag())) {
                manualQuoteRequestPiece.setQuoteStage(manualQuoteItem.getQuoteStage());
                manualQuoteRequestPiece.setExpectedResponseTime(manualQuoteItem.getExpectedResponseTime());
                manualQuoteRequestPiece.setMBPFlag(manualQuoteItem.getMbpFlag());
            }

            if (isNotNull(userDTO.getEin())) {
                manualQuoteRequestPiece.setIEin(Integer.parseInt(userDTO.getEin().trim()));
            }

            CustomerDTO customerDTO = customerResource.getByToken(customerId, userToken);
            manualQuoteRequestPiece.setSalesChannel(customerDTO.getSalesChannel());

            if (null != userComments && !userComments.isEmpty()) {
                manualQuoteRequestPiece.setUserComments(userComments.get(0).getComment());
            }else{
                manualQuoteRequestPiece.setUserComments("");
            }

            manualQuoteRequest[a] = manualQuoteRequestPiece;
        }

        if(!invalidTransitions.isEmpty()) {
            return ResponseBuilder.badRequest().withEntity(buildManualQuoteError(manualQuote.getManualQuoteItems().size(), invalidTransitions)).build();
        }

        SqeQrefPairInputResponse[] manualQuoteResponse = apeClient.manualQuote(manualQuoteRequest);

        List<SqeQrefPairInputResponse> unsuccessfulQuotes = newArrayList();

        for(SqeQrefPairInputResponse response : manualQuoteResponse) {
            if(response.isSuccess()) {
                updateWorkflowStatus(qrefs.get(response.getQref()), AccessWorkflowStatus.SENT_TO_WORKFLOW);
            } else {
                unsuccessfulQuotes.add(response);
            }
        }

        saveManualQuote(manualQuote);

        if(unsuccessfulQuotes.isEmpty()) {
            return ResponseBuilder.anOKResponse().build();
        } else {
            return ResponseBuilder.badRequest().withEntity(buildManualQuoteError(manualQuote.getManualQuoteItems().size(), unsuccessfulQuotes)).build();
        }
    }

    private List checkManualQuoteInputs(ManualQuote manualQuote) {
        List<String> errorDetails = new ArrayList<String>();
        if(isNullOrEmpty(manualQuote.getSiteTelephoneNumber())) {
            errorDetails.add("Site Telephone Number");
        }
        return errorDetails;
    }

    @POST
    @Path("comments/qref/{qref}")
    public Response sendComments(String comments, @PathParam("qref") String qref) {
        ApeQref apeQref = apeQrefRepository.getApeQref(qref);

        QrefAttributeExtractor attributeExtractor = new QrefAttributeExtractor(apeQref);
        String apeQrefReference = attributeExtractor.getAttributeValue(LocalIdentifier.QREF);

        if(!transitionAllowed(TransitionName.SEND_COMMENTS, attributeExtractor)) {
            return invalidWorkflowResponse();
        }

        QrefPairCommentsInput commentsInput = new QrefPairCommentsInput();
        commentsInput.setUserComments(comments);
        commentsInput.setQref(apeQrefReference);
        commentsInput.setPairId(attributeExtractor.getAttributeValue(LocalIdentifier.PAIR_ID));

        QrefPairCommentsInputResponse[] commentsInputResponses = apeClient.sendComment(new QrefPairCommentsInput[]{commentsInput});

        if(commentsInputResponses[0].isSuccess()) {
            return ResponseBuilder.anOKResponse().build();
        } else {
            return ResponseBuilder.badRequest().withEntity(commentsInputResponses[0].getErrorDesc()).build();
        }
    }

    @POST
    @Path("saveComments/qref/{qref}/userName/{userName}")
    public Response saveComments(String comments, @PathParam("qref") String qref, @PathParam("userName") String userName) {
        java.sql.Date currentDate = new java.sql.Date(System.currentTimeMillis());
        AccessUserCommentsEntity accessUserCommentsEntity = new AccessUserCommentsEntity(UUID.randomUUID().toString(), qref, userName, comments, currentDate);
        apeQrefRepository.save(accessUserCommentsEntity);

        return ResponseBuilder.anOKResponse().build();
    }

    @GET
    @Path("loadComments/qref/{qref}")
    public Response loadComments(@PathParam("qref") String qref) {
        List<AccessUserCommentsEntity> accessUserCommentsEntity = apeQrefRepository.getUserCommentsForQrefId(qref);

        if (accessUserCommentsEntity != null && !accessUserCommentsEntity.isEmpty()) {
            return ResponseBuilder.anOKResponse().withEntity(new GenericEntity<List<AccessUserCommentsEntity>>(accessUserCommentsEntity) {
            }).build();
        } else {
            return ResponseBuilder.anOKResponse().withEntity(new GenericEntity<List<AccessUserCommentsEntity>>(newArrayList(new AccessUserCommentsEntity("", "", "", "", null))) {
            }).build();
        }
    }

    @POST
    @Path("/recall-qrefs")
    public Response recallQrefs(QrefRecallDTO qrefRecallDTO) {
        if(Lists.isNullOrEmpty(qrefRecallDTO.getQrefStencilIds())) {
            return ResponseBuilder.badRequest().withEntity("No QREF Stencil ID's supplied").build();
        }

        QrefPairInput[] recallQrefRequest = new QrefPairInput[qrefRecallDTO.getQrefStencilIds().size()];
        Map<String, ApeQref> qrefs = new HashMap<String, ApeQref>();

        for(int a = 0; a < qrefRecallDTO.getQrefStencilIds().size(); a++) {
            String qrefStencilId = qrefRecallDTO.getQrefStencilIds().get(a);
            ApeQref apeQref = apeQrefRepository.getApeQref(qrefStencilId);
            final Optional<ApeQrefAttributeDetail> pricingType = apeQref.getAttribute("PRICING REQUEST TYPE");


            QrefAttributeExtractor attributeExtractor = new QrefAttributeExtractor(apeQref);

            String qrefId = attributeExtractor.getAttributeValue(LocalIdentifier.QREF);

            if(!transitionAllowed(TransitionName.RECALL_QREF, attributeExtractor)) {
                return invalidWorkflowResponse(String.format("QREF '%s' cannot be recalled as it has not been sent to workflow. Please un-select this QREF and try again.", qrefId));
            }

            qrefs.put(qrefId, apeQref);
            if (pricingType.isPresent()) {
                recallQrefRequest[a] = new QrefPairInput(qrefId, null, pricingType.get().getAttributeValue());
            } else {
                recallQrefRequest[a] = new QrefPairInput(qrefId, null, "");
            }
        }

        QrefPairInputResponse[] recallQrefResponse = apeClient.recallQrefs(recallQrefRequest);

        Map<String, String> unsuccessfulQrefs = new HashMap<String, String>();

        for(QrefPairInputResponse responsePiece : recallQrefResponse) {
            if(responsePiece.isSuccess()) {
                updateWorkflowStatus(qrefs.get(responsePiece.getQref()), AccessWorkflowStatus.REQUIRED);
            } else {
                unsuccessfulQrefs.put(responsePiece.getQref(), responsePiece.getErrorDesc());
            }
        }

        if(unsuccessfulQrefs.isEmpty()) {
            return ResponseBuilder.anOKResponse().build();
        } else {
            return ResponseBuilder.badRequest().withEntity(buildQrefError("Recall QREF Failed", unsuccessfulQrefs.size(), unsuccessfulQrefs)).build();
        }
    }

    private String buildManualQuoteError(int totalQuotes, List<SqeQrefPairInputResponse> unsuccessfulQuotes) {
        HashMap<String, String> unsuccessfulQuotesMap = new HashMap<String, String>();
        Iterator<SqeQrefPairInputResponse> unsuccessfulQuotesIterator = unsuccessfulQuotes.iterator();

        while(unsuccessfulQuotesIterator.hasNext()) {
            SqeQrefPairInputResponse unsuccessfulQuote = unsuccessfulQuotesIterator.next();
            unsuccessfulQuotesMap.put(unsuccessfulQuote.getQref(), unsuccessfulQuote.getErrorDesc());
        }

        return buildManualQuoteError(totalQuotes, unsuccessfulQuotesMap);
    }

    private String buildManualQuoteError(int totalQuotes, Map<String, String> unsuccessfulQuotes) {
        return buildQrefError("Access pricing Manual workflow request has failed for ", totalQuotes, unsuccessfulQuotes);
    }

    private String buildQrefError(String messageStart, int totalQrefs, Map<String, String> unsuccessfulQrefs) {
        StringBuilder sb = new StringBuilder();
        sb.append(messageStart);
        sb.append(" for ");
        sb.append(totalQrefs == unsuccessfulQrefs.size() ? "all" : "some");
        sb.append(" QREFs. ");


        Iterator unsuccessfulQuotesIterator = unsuccessfulQrefs.entrySet().iterator();

        while(unsuccessfulQuotesIterator.hasNext()) {
            Map.Entry qrefErrorAndDescriptionPairs = (Map.Entry)unsuccessfulQuotesIterator.next();

            sb.append("[").append(qrefErrorAndDescriptionPairs.getKey()).append("]").append(" ").append(qrefErrorAndDescriptionPairs.getValue());

            if(unsuccessfulQuotesIterator.hasNext()) {
                sb.append(", ");
            }
        }

        return sb.toString();
    }

    private void updateWorkflowStatus(ApeQref qref, AccessWorkflowStatus workflowStatus) {
        OfferingAttribute[] offeringAttributes = ApeMappingConfigLoader.getLocalIdentifierMappings()
                                                                       .getLocalIdentifierMappingConfig(LocalIdentifier.WORKFLOW_STATUS.name())
                                                                       .getOfferingAttributeConfig();

        ApeQrefDetailEntity workflowStatusDetail = new ApeQrefDetailEntity(qref.getRequestId(),
                                                                           qref.getQrefId(),
                                                                           offeringAttributes[0].getName(),
                                                                           workflowStatus == null ? null : String.valueOf(workflowStatus.getStatus()),
                                                                           null);
        ApeQrefDetailEntity offeringWorkflowStatusDetail = new ApeQrefDetailEntity(qref.getRequestId(),
                                                                           qref.getQrefId(),
                                                                           WORKFLOW_STATUS,
                                                                           workflowStatus == null ? null : String.valueOf(workflowStatus.getStatus()),
                                                                           null);
        apeQrefRepository.save(workflowStatusDetail);
        apeQrefRepository.save(offeringWorkflowStatusDetail);
    }

    public static interface Logger {
        @Log(level = LogLevel.INFO, format = "get ManualQuote for QREF %s")
        void getManualQuoteCalled(String qref);

        @Log(level = LogLevel.INFO, format = "created ManualQuote %s")
        void createdManualQuote(ManualQuote manualQuote);
    }
}
