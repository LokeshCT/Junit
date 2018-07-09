package com.bt.cqm.handler;

import com.bt.cqm.config.CqmConfig;
import com.bt.rsqe.EmailService;
import com.bt.rsqe.container.ApplicationConfig;
import com.bt.rsqe.customerinventory.utils.Constants;
import com.bt.rsqe.expedio.activity.AcceptActivityTaskDTO;
import com.bt.rsqe.expedio.activity.ReassignActivityTaskDTO;
import com.bt.rsqe.expedio.activity.RejectActivityTaskDTO;
import com.bt.rsqe.expedio.activity.WithdrawApprovalActivityTaskDTO;
import com.bt.rsqe.expedio.services.ActivityAssignedToContactDTO;
import com.bt.rsqe.expedio.services.ActivityDTO;
import com.bt.rsqe.expedio.services.ActivityResource;
import com.bt.rsqe.expedio.services.AssignedToContactResource;
import com.bt.rsqe.expedio.services.ChangeOwnershipDTO;
import com.bt.rsqe.expedio.services.GetActivityRequestDTO;
import com.bt.rsqe.expedio.services.UpdateActivityResponseDTO;
import com.bt.rsqe.expedio.services.UpdateStatusDTO;
import com.bt.rsqe.rest.ResponseBuilder;
import com.bt.rsqe.utils.AssertObject;
import com.bt.rsqe.utils.UriBuilder;
import com.bt.rsqe.web.ClasspathConfiguration;
import com.bt.rsqe.web.rest.exception.ResourceNotFoundException;
import com.bt.rsqe.web.rest.exception.RestException;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.bt.cqm.utils.Utility.*;
import static java.lang.String.*;

@Path("/cqm/activity")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ActivityHandler {
    private static final String BID_MANAGER = "Bid Manager";
    private final AssignedToContactResource assignedToContactsResource;
    private ActivityResource activityResource;
    private final EmailService emailService;
    private static Logger LOG = LoggerFactory.getLogger(ActivityHandler.class);
    private final CqmConfig cqmConfig;
    private static final String ACTIVITY_CREATION_SUCCESS_TEMPLATE = "com/bt/cqm/activity-creation-email-template.ftl";
    private static final String ACTIVITY_CREATION_SUCCESS_SUBJECT = "A New Activity Request for Customer %s Requires Action";
    private static final String ACTIVITY_CREATION_SUCCESS_NOTE = "A new activity has been  assigned to you. \n" +
                                                                 "Please refer to the sales user's comments for the action required and accept/reject/re-assign in the system as applicable\n.";


    private static final String ACTIVITY_UPDATE_STATUS_TEMPLATE = "com/bt/cqm/activity-update-status-email-template.ftl";
    private static final String ACTIVITY_UPDATE_OPEN_SUCCESS_SUBJECT = "%s - %s - Activity has been Re-opened";
    private static final String ACTIVITY_UPDATE_CLOSED_SUCCESS_SUBJECT = "%s - %s - Activity has been Re-opened";
    private static final String ACTIVITY_UPDATE_OPEN_SUCCESS_NOTE = "The following  Activity has been Re-opened by %s. \n";
    private static final String ACTIVITY_UPDATE_CLOSE_SUCCESS_NOTE = "The following  activity has been marked as completed by  %s. \n";

    public ActivityHandler(AssignedToContactResource assignedToContactsResource, ActivityResource activityResource, EmailService emailService, CqmConfig cqmConfig) {
        this.assignedToContactsResource = assignedToContactsResource;
        this.activityResource = activityResource;
        this.emailService = emailService;
        this.cqmConfig = cqmConfig;
    }

    @POST
    @Path("/create")
    @Produces(MediaType.TEXT_PLAIN)
    public Response createActivity(@HeaderParam("SM_USER") String userId, @HeaderParam("BOAT_ID")  String boatId,ActivityDTO activityDTO) {
        try {
            activityDTO.setUserId(userId);
            activityDTO.setBoatId(boatId);
            activityDTO.setSourceSystem(Constants.RSQE_SYSTEM);
            String activityId = activityResource.create(activityDTO);
            activityDTO.setActivityID(activityId);
            sendEmail(activityDTO);
            return com.bt.rsqe.rest.ResponseBuilder.anOKResponse().withEntity(activityId).build();
        } catch (Exception e) {
            return ResponseBuilder.internalServerError().withEntity(buildGenericError(e.getMessage())).build();
        }
    }

    @GET
    @Path("/assignedToList")
    public Response getAssignedToList(@QueryParam("salesChannel") String salesChannel, @QueryParam("userRole") String userRole, @QueryParam("bfgId") String bfgID) {

        List<ActivityAssignedToContactDTO> assignedToContacts = assignedToContactsResource.getAssignedToContacts(salesChannel, userRole, bfgID);

        GenericEntity<List<ActivityAssignedToContactDTO>> genericEntity = new GenericEntity<List<ActivityAssignedToContactDTO>>(assignedToContacts) {
        };
        return ResponseBuilder.anOKResponse().withEntity(genericEntity).build();
    }


    @POST
    @Path("/searchActivity")
    public Response getActivityList(GetActivityRequestDTO getActivityRequestDTO) {

        try {
            List<com.bt.rsqe.expedio.services.ActivityDTO> activityList = activityResource.getActivityList(getActivityRequestDTO);
            //Incorrect response returned by Expedio. Always the first element in the List is empty. Temporary fix to remove the first element.
            if (null != activityList && activityList.size() > 1) {
                if (null == activityList.get(0).getActivityID()) {
                    activityList.remove(0);
                }

            }
            GenericEntity<List<com.bt.rsqe.expedio.services.ActivityDTO>> genericEntityActivityArray = new GenericEntity<List<com.bt.rsqe.expedio.services.ActivityDTO>>(activityList) {
            };
            return ResponseBuilder.anOKResponse().withEntity(genericEntityActivityArray).build();

        } catch (ResourceNotFoundException e) {
            return ResponseBuilder.notFound().withEntity(buildGenericError(e.errorDto().description)).build();
        } catch (Exception e) {
            return ResponseBuilder.internalServerError().withEntity(buildGenericError("Error occured while fetching the list of Activities... Details: " + e.getMessage())).build();
        }

    }

    @PUT
    @Path("/assignTo")
    public Response updateAssignedTo(ChangeOwnershipDTO changeOwnershipDTO) {
        try {
            UpdateActivityResponseDTO updateActivityResponseDTO = activityResource.changeOwnership(changeOwnershipDTO);
            return Response.ok(updateActivityResponseDTO).build();
        } catch (Exception e) {
            return ResponseBuilder.notFound().withEntity(buildGenericError(e.getMessage())).build();
        }
    }

    @POST
    @Path("/approve")
    public Response approve(UpdateStatusDTO updateStatusDTO) {
        updateStatusDTO.setState("Closed");
        updateStatusDTO.setSubstate("Approved");
        updateStatusDTO.setClosedDate(new Date(System.currentTimeMillis()).toString());
        return updateActivity(updateStatusDTO);
    }

    @POST
    @Path("/reassign")
    public Response reAssign(ReassignActivityTaskDTO taskDTO, @HeaderParam("USER_ROLE") String role, @HeaderParam("USER_EMAIL") String userEmail) throws RestException {
        if (AssertObject.isNull(taskDTO) || AssertObject.anyEmpty(taskDTO.getCommentsforReAssignment(), taskDTO.getActivityID(), taskDTO.getAssignedToFirstName(), taskDTO.getAssignedToFullName(), userEmail)) {
            if (taskDTO != null) {
                LOG.warn("Activity Reassign :: Invalid Inputs -> Activity Id =" + taskDTO.getActivityID() + " , AssignedToFirstName =" + taskDTO.getAssignedToFirstName() + " ,AssignedToFullName =" + taskDTO.getAssignedToFullName() + " , Comments =" + taskDTO.getCommentsforReAssignment(), userEmail);
            }
            return Response.status(Response.Status.BAD_REQUEST).entity(buildGenericError("Invalid Input !!")).build();
        }

        if (BID_MANAGER.equalsIgnoreCase(role)) {
            taskDTO.setTask("Assign_Bid");
        } else {
            taskDTO.setTask("Assign");
        }
        taskDTO.setSourceSystem(Constants.RSQE_SYSTEM);

        Boolean isSuccess = activityResource.reassignActivityDelegation(taskDTO);

        if (isSuccess) {
            return Response.status(Response.Status.OK).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @POST
    @Path("/acceptDelegation")
    public Response acceptDelegation(AcceptActivityTaskDTO taskDTO, @HeaderParam("USER_ROLE") String role,@HeaderParam("USER_EMAIL") String userEmail) throws RestException {
        if (AssertObject.isNull(taskDTO)) {
            return Response.status(Response.Status.BAD_REQUEST).entity(buildGenericError("Invalid Input !!")).build();
        }

        if (BID_MANAGER.equalsIgnoreCase(role)) {
            taskDTO.setTask("Accept_Bid");
        } else {
            taskDTO.setTask("Accept");
        }
        taskDTO.setAssigneeMailID(userEmail);
        taskDTO.setSourceSystem(Constants.RSQE_SYSTEM);

        Boolean isSuccess = activityResource.acceptActivityDelegation(taskDTO);

        if (isSuccess) {
            return Response.status(Response.Status.OK).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @POST
    @Path("/rejectDelegation")
    public Response rejectDelegation(RejectActivityTaskDTO taskDTO, @HeaderParam("USER_ROLE") String role,@HeaderParam("USER_EMAIL") String userEmail) throws RestException {
        if (AssertObject.isNull(taskDTO)) {
            return Response.status(Response.Status.BAD_REQUEST).entity(buildGenericError("Invalid Input !!")).build();
        }

        if (BID_MANAGER.equalsIgnoreCase(role)) {
            taskDTO.setTask("Reject_Bid");
        } else {
            taskDTO.setTask("Reject");
        }
        //taskDTO.setAssigneeMailID(userEmail);
        taskDTO.setSourceSystem(Constants.RSQE_SYSTEM);

        Boolean isSuccess = activityResource.rejectActivityDelegation(taskDTO);

        if (isSuccess) {
            return Response.status(Response.Status.OK).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @POST
    @Path("/withdrawApproval")
    public Response withdrawApproval(WithdrawApprovalActivityTaskDTO taskDTO, @HeaderParam("USER_ROLE") String role,@HeaderParam("USER_EMAIL") String userEmail) throws RestException {
        if (AssertObject.isNull(taskDTO)) {
            return Response.status(Response.Status.BAD_REQUEST).entity(buildGenericError("Invalid Input !!")).build();
        }

        if (BID_MANAGER.equalsIgnoreCase(role)) {
            taskDTO.setTask("Withdraw");
        } else {
            return Response.status(Response.Status.UNAUTHORIZED).entity(buildGenericError("Only BidManager is permitted to withdraw approval !!")).build();
        }

        taskDTO.setBidManagerMailID(userEmail);
        taskDTO.setSourceSystem(Constants.RSQE_SYSTEM);

        Boolean isSuccess = activityResource.withdrawApproval(taskDTO);

        if (isSuccess) {
            return Response.status(Response.Status.OK).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @POST
    @Path("/reject")
    public Response reject(UpdateStatusDTO updateStatusDTO) {
        updateStatusDTO.setState("Closed");
        updateStatusDTO.setSubstate("Rejected");
        updateStatusDTO.setClosedDate(new Date(System.currentTimeMillis()).toString());
        return updateActivity(updateStatusDTO);
    }

    private Response updateActivity(UpdateStatusDTO updateStatusDTO) {
        try {
            UpdateActivityResponseDTO updateActivityResponseDTO = activityResource.updateStatus(updateStatusDTO);
            return Response.ok(updateActivityResponseDTO).build();
        } catch (Exception e) {
            return ResponseBuilder.notFound().withEntity(buildGenericError(e.getMessage())).build();
        }
    }

    private Response updateActivityStatus(UpdateStatusDTO updateStatusDTO) {
        try {
            UpdateActivityResponseDTO updateActivityResponseDTO = activityResource.updateStatus(updateStatusDTO);
            sendEmailUpdateStatus(updateStatusDTO);
            return Response.ok(updateActivityResponseDTO).build();
        } catch (Exception e) {
            return ResponseBuilder.notFound().withEntity(buildGenericError(e.getMessage())).build();
        }
    }

    @POST
    @Path("/updateActivityStatus")
    public Response updateStatus(UpdateStatusDTO updateStatusDTO) {
        updateStatusDTO.setClosedDate(new Date(System.currentTimeMillis()).toString());
        return updateActivityStatus(updateStatusDTO);

    }
    private void sendEmail(ActivityDTO activityDTO) {
        try {
            String mailSubject = constructMessageSubject(activityDTO.getCustomerName());
            String mailBody = constructMessageBody(activityDTO);
            String[] ccList= {activityDTO.getCreatedbyEmailid()} ;
            emailService.sendEmail(EmailService.DEFAULT_FROM_MAIL_ACCOUNT, mailSubject, mailBody, activityDTO.getAssignedtoEmailid(),ccList);
        } catch (Exception e) {
            LOG.error("Failed to send Email for Customer Name :" + activityDTO.getCustomerName() + "  having EMAIL_ID :" + activityDTO.getAssignedtoEmailid());
        }
    }
    private void sendEmailUpdateStatus(UpdateStatusDTO updateStatusDTO) {
        try {
            String mailSubject = constructMessageSubjectUpdateStatus(updateStatusDTO);
            String mailBody = constructMessageBodyUpdateStatus(updateStatusDTO);
            emailService.sendEmail(EmailService.DEFAULT_FROM_MAIL_ACCOUNT, mailSubject, mailBody, updateStatusDTO.getAssignedToEmailId());
        } catch (Exception e) {
            LOG.error("Failed to send Email for Customer Name :" + updateStatusDTO.getCustomerName() + "  having EMAIL_ID :" + updateStatusDTO.getAssignedToEmailId());
        }
    }
    private String constructMessageBody(ActivityDTO activityDTO) throws IOException, TemplateException {
        Configuration config = new ClasspathConfiguration();

        Map<String, Object> rootMap = new HashMap<String, Object>();
        rootMap.put("messageNote", StringUtils.isEmpty(ACTIVITY_CREATION_SUCCESS_NOTE) ? "" : ACTIVITY_CREATION_SUCCESS_NOTE);
        rootMap.put("salesUser", StringUtils.isEmpty(activityDTO.getAssignedTo()) ? "" : activityDTO.getAssignedTo());
        rootMap.put("salesChannel", StringUtils.isEmpty(activityDTO.getSalesChannel()) ? "" : activityDTO.getSalesChannel());
        rootMap.put("customerName", StringUtils.isEmpty(activityDTO.getCustomerName()) ? "" : activityDTO.getCustomerName());
        rootMap.put("quoteName", StringUtils.isEmpty(activityDTO.getQuoteName()) ? "" : activityDTO.getQuoteName());
        rootMap.put("quoteRefId", StringUtils.isEmpty(activityDTO.getQuoteRefID()) ? "" : activityDTO.getQuoteRefID());
        rootMap.put("quoteVersion", StringUtils.isEmpty(activityDTO.getQuoteVersion()) ? "" : activityDTO.getQuoteVersion());
        rootMap.put("url", StringUtils.isEmpty(getCQMWebAppURL()) ? "" : getCQMWebAppURL());
        rootMap.put("status", StringUtils.isEmpty(activityDTO.getStatus()) ? "" : activityDTO.getStatus());
        rootMap.put("activityID", StringUtils.isEmpty(activityDTO.getActivityID()) ? "" : activityDTO.getActivityID());
        rootMap.put("salesUsersComments", StringUtils.isEmpty(activityDTO.getSalesUsersComments()) ? "" : activityDTO.getSalesUsersComments());
        rootMap.put("creator",StringUtils.isEmpty(activityDTO.getCreator())? "" : activityDTO.getCreator());
        Template emailTemplate = config.getTemplate(ACTIVITY_CREATION_SUCCESS_TEMPLATE);

        Writer out = new StringWriter();
        emailTemplate.process(rootMap, out);

        return out.toString();
    }

    public String getCQMWebAppURL() {
        ApplicationConfig config = cqmConfig.getApplicationConfig();
        String cqmAppWebURI = new UriBuilder().scheme(config.getScheme())
                                              .host(config.getHost()).port(config.getPort()).build().toString();
        return cqmAppWebURI + "/cqm";
    }

    private String constructMessageSubject(String customerName) {
        return format(ACTIVITY_CREATION_SUCCESS_SUBJECT, customerName);
    }
    private String constructMessageSubjectUpdateStatus(UpdateStatusDTO updateStatusDTO) {
        if ("Open".equals(updateStatusDTO.getState())) {
            return format(ACTIVITY_UPDATE_OPEN_SUCCESS_SUBJECT, updateStatusDTO.getSalesChannel(), updateStatusDTO.getCustomerName());
        } else {
            return format(ACTIVITY_UPDATE_CLOSED_SUCCESS_SUBJECT, updateStatusDTO.getSalesChannel(), updateStatusDTO.getCustomerName());
        }
    }

    private String constructMessageBodyUpdateStatus(UpdateStatusDTO updateStatusDTO) throws IOException, TemplateException {
        Configuration config = new ClasspathConfiguration();

        Map<String, Object> rootMap = new HashMap<String, Object>();
        if ("Open".equals(updateStatusDTO.getState())) {
            rootMap.put("messageNote", StringUtils.isEmpty(ACTIVITY_UPDATE_OPEN_SUCCESS_NOTE) ? "" : format(ACTIVITY_UPDATE_OPEN_SUCCESS_NOTE,updateStatusDTO.getCustomerName()));
        } else {
            rootMap.put("messageNote", StringUtils.isEmpty(ACTIVITY_UPDATE_CLOSE_SUCCESS_NOTE) ? "" : format(ACTIVITY_UPDATE_CLOSE_SUCCESS_NOTE,updateStatusDTO.getCustomerName()));
        }
        rootMap.put("salesChannel", StringUtils.isEmpty(updateStatusDTO.getSalesChannel()) ? "" : updateStatusDTO.getSalesChannel());
        rootMap.put("customerName", StringUtils.isEmpty(updateStatusDTO.getCustomerName()) ? "" : updateStatusDTO.getCustomerName());
        rootMap.put("url", StringUtils.isEmpty(getCQMWebAppURL()) ? "" : getCQMWebAppURL());
        rootMap.put("status", StringUtils.isEmpty(updateStatusDTO.getState()) ? "" : updateStatusDTO.getState());
        rootMap.put("activityID", StringUtils.isEmpty(updateStatusDTO.getActivityID()) ? "" : updateStatusDTO.getActivityID());
        rootMap.put("comments", StringUtils.isEmpty(updateStatusDTO.getBidManagerComment()) ? "" : updateStatusDTO.getBidManagerComment());
        Template emailTemplate = config.getTemplate(ACTIVITY_UPDATE_STATUS_TEMPLATE);
        Writer out = new StringWriter();
        emailTemplate.process(rootMap, out);

        return out.toString();
    }
}
