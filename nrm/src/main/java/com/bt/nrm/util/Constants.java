package com.bt.nrm.util;

import com.google.gson.GsonBuilder;
import java.util.HashMap;
import java.util.Map;

public class Constants {

    public static Map<String, String> requestStateConstants = new HashMap<String, String>();
    public static Map<String, String> requestEvaluatorStateConstants = new HashMap<String, String>();
    public static Map<String, String> requestEvaluatorResponseConstants = new HashMap<String, String>();
    public static Map<String,String> requestResponseType = new HashMap<String,String>();
    public static Map<String,String> NRMUserRoles = new HashMap<String,String>();
    public static Map<String,String> templateWorkFlows = new HashMap<String,String>();

    public static Character YES = 'Y';
    public static Character NO = 'N';

    /*
        Request Creation message constants
     */
    public static String REQUEST_CREATION_SUCCESSFUL = "Non-standard request created successfully.";
    public static final String REQUEST_CREATION_SUCCESS_NOTE = "The below request has been successfully submitted";
    public static final String REQUEST_CREATION_TEMPLATE = "com/bt/nrm/emailtemplate/request-creation-email-template.ftl";
    public static final String REQUEST_CREATION_EMAIL_SUBJECT = "Request successfully created with Id: %s ";
    /*
        Placeholders for attributes and price groups
     */
    public static String TEMPLATE_PLACEHOLDER_PRIMARY = "PRIMARY";
    public static String TEMPLATE_PLACEHOLDER_SECONDARY = "SECONDARY";
    public static String TEMPLATE_PLACEHOLDER_COMMON = "COMMON";
    public static String TEMPLATE_PLACEHOLDER_RESPONSE = "RESPONSE";

    static{

        requestStateConstants.put("created", "created"); // Initial state when request comes to NRM
        requestStateConstants.put("issued", "issued"); // Controller will issue request by clicking on "Issue" button
        requestStateConstants.put("signedIn", "progressing"); // Controller will change to this state by clicking on "Sign In" button
        requestStateConstants.put("signedOff", "responded");  // Controller will change to this state by clicking on "Sign Off" button
        requestStateConstants.put("committed", "committed"); // When Offer is converted into Order in SQE
        requestStateConstants.put("expired", "expired"); // If the request is exceeded it's Maximum Validity then it becomes expired
        requestStateConstants.put("won", "won");  // When Order/BOM is submitted in SQE
        requestStateConstants.put("lost", "lost"); // User changes to this state manually
        requestStateConstants.put("withdrawn", "withdrawn"); //Controller will change to this state by clicking on "Cancel" button
        requestStateConstants.put("waiting", "waiting");  //Controller will change to this state by clicking on "Refuse" button
        requestStateConstants.put("closed", "closed");  //After responded, if controller needs to close the request for any reason.
        requestStateConstants.put("deleted", "deleted");  //To remove it from getting listed in Quotes page
        requestStateConstants.put("delivered", "delivered");  //Once the order is provisioned
        requestStateConstants.put("allAgentsHaveFinishedWork", "allAgentsHaveFinishedWork");
        requestStateConstants.put("noAgents", "noAgents");
        requestStateConstants.put("all", "all");

        requestEvaluatorStateConstants.put("requestEvaluatorState_created", "created");  //Request Group record's initial status when request is created
        requestEvaluatorStateConstants.put("requestEvaluatorState_accepted", "accepted"); //Request Group record's status when any agent from the particular group starts working on it
        requestEvaluatorStateConstants.put("requestEvaluatorState_closed", "closed");   //Request Group record's status when agent provides response in form of Go/No Go

        requestEvaluatorResponseConstants.put("requestEvaluatorResponse_go", "Go");   //Response given by the agent when she clicks on "Go" button
        requestEvaluatorResponseConstants.put("requestEvaluatorResponse_noGo", "No Go");  //Response given by the agent when she clicks on "No Go" button
        requestEvaluatorResponseConstants.put("requestEvaluatorResponse_none", "None");  //Initial value for response by agent for Request group

        requestResponseType.put("DEFAULT","None");
        requestResponseType.put("APPROVED","Approved");
        requestResponseType.put("PARTIALLY_APPROVED","Partially-Approved");
        requestResponseType.put("REJECTED","Rejected");

        NRMUserRoles.put("controller","Controller");
        NRMUserRoles.put("evaluator","Evaluator");
        NRMUserRoles.put("dataBuild","Data Build");
        NRMUserRoles.put("superUser","Super User");

        templateWorkFlows.put("evaluated","Evaluated");
        templateWorkFlows.put("fastTrack","FastTrack");
        templateWorkFlows.put("zeroTouch","ZeroTouch");
    }

    public static String requestStateConstantsBuild() {
        return new GsonBuilder().disableHtmlEscaping().create().toJson(requestStateConstants);
    }

    public static String requestEvaluatorStateConstantsBuild() {
        return new GsonBuilder().disableHtmlEscaping().create().toJson(requestEvaluatorStateConstants);
    }

    public static String requestEvaluatorResponseConstantsBuild() {
        return new GsonBuilder().disableHtmlEscaping().create().toJson(requestEvaluatorResponseConstants);
    }

    public static String requestResponseTypeBuild() {
        return new GsonBuilder().disableHtmlEscaping().create().toJson(requestResponseType);
    }

    public static String nrmUserRolesBuild() {
        return new GsonBuilder().disableHtmlEscaping().create().toJson(NRMUserRoles);
    }
  }

