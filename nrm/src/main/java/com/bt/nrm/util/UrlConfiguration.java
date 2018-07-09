package com.bt.nrm.util;

import com.google.gson.GsonBuilder;

import java.util.HashMap;
import java.util.Map;

public class UrlConfiguration {

    public static String build() {

        Map<String, String> urls = new HashMap<String, String>();

        urls.put("loginUri", "/nrm");
        urls.put("headerImageUri", "/nrm/static/assets/img/sqe_logo.jpg");
        urls.put("logoutUri", "/nrm/logout");

        urls.put("getNrmUserByUserIdUri", "/nrm/user/getNrmUserByUserId");
        urls.put("getUserByEINOrNameUri", "/nrm/user/getUserByEINOrName");
        urls.put("getUserManagementDataUri", "/nrm/user/getUserManagementData");
        urls.put("getAllGroupsUri", "/nrm/user/getAllGroups");
        urls.put("addProductsToUserUri", "/nrm/user/addProductsToUser");
        urls.put("addGroupToUserUri", "/nrm/user/addGroupToUser");
        urls.put("deleteGroupFromUserUri", "/nrm/user/deleteGroupFromUser");
        urls.put("addRoleToUserUri", "/nrm/user/addRoleToUser");
        urls.put("deleteRoleFromUserUri", "/nrm/user/deleteRoleFromUser");
        urls.put("getUserStatsUri", "/nrm/user/getUserStats");

        urls.put("getProductsByUserIdUri", "/nrm/productTemplates/getProductsByUserId");
        urls.put("getAllProductsUri", "/nrm/productTemplates/getAllProducts");
        urls.put("getTemplatesByProductIdUri", "/nrm/productTemplates/getTemplatesByProductId");
        urls.put("getTemplateByTemplateCode", "/nrm/productTemplates/getTemplateByTemplateCode");

        urls.put("getRequestsByUserIdUri", "/nrm/requests/getRequestsByUserId");
        urls.put("getRequestsByUserIdAndStatesUri", "/nrm/requests/getRequestsByUserIdAndStates");
        urls.put("getRequestByRequestIdUri", "/nrm/requests/getRequestByRequestId");
        urls.put("getDataBuildRequestsUri", "/nrm/requests/getDataBuildRequests");
        urls.put("saveRequestCommentsUri", "/nrm/requests/saveRequestComments");
        urls.put("saveRequestGroupCommentsUri", "/nrm/requests/saveRequestGroupComments");
        urls.put("createRequestUri", "/nrm/requests/createRequest");
        urls.put("updateRequestDetailUri", "/nrm/requests/updateRequestDetail");
        urls.put("updateDataBuildStatusUri","/nrm/requests//updateDataBuildStatus") ;
        urls.put("getAllQuoteOptionsUri","/nrm/requests/getAllQuoteOptions");
        urls.put("getAllRequestsByQuoteIdUri","/nrm/requests/getAllRequestsByQuoteId");

        urls.put("getListOfEvaluatorActionsUri", "/nrm/evaluator/getListOfEvaluatorActions");
        urls.put("updateEvaluatorPriceGroupUri", "/nrm/evaluator/updateEvaluatorPriceGroup");
        urls.put("startWorkingOnAction", "/nrm/evaluator/startWorkingOnActionUri");

        return new GsonBuilder().disableHtmlEscaping().create().toJson(urls);
    }

}
