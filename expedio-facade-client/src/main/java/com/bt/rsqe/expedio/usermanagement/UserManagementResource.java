package com.bt.rsqe.expedio.usermanagement;

import com.bt.rsqe.customerrecord.client.ExpedioFacadeConfig;
import com.bt.rsqe.utils.UriBuilder;
import com.bt.rsqe.web.rest.ProxyAwareRestRequestBuilder;
import com.bt.rsqe.web.rest.RestRequestBuilder;
import com.bt.rsqe.web.rest.RestResponse;
import org.apache.commons.lang.StringUtils;

import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: 607982105
 * Date: 23/03/15
 * Time: 15:55
 * To change this template use File | Settings | File Templates.
 */
public class UserManagementResource {

    private RestRequestBuilder restRequestBuilder;

    public UserManagementResource(ExpedioFacadeConfig expedioFacadeConfig) {
        this(UriBuilder.buildUri(expedioFacadeConfig.getApplicationConfig()),
             expedioFacadeConfig.getRestAuthenticationClientConfig().getSecret());
    }

    public UserManagementResource(URI baseUri, String secret) {
        URI uri = UriBuilder.buildUri(priceBookURI(baseUri));
        restRequestBuilder = new ProxyAwareRestRequestBuilder(uri).withSecret(secret);
    }

    private URI priceBookURI(URI baseURI) {
        return javax.ws.rs.core.UriBuilder.fromUri(baseURI).path("rsqe").path("expedio").path("usermanagement").build();
    }


    public List<RoleDetails> getUserRoleDetails(String userName) {
        Map<String, String> qParam = new HashMap<String, String>();
        List<RoleDetails> roleDetailsList = null;
        qParam.put("userName", userName);
        try {
            RestResponse restResponse = this.restRequestBuilder.build("getUserDetails", qParam).get();
            roleDetailsList = restResponse.getEntity(new GenericType<List<RoleDetails>>() {
            });
        } catch (Exception e) {
            return roleDetailsList;
        }
        return roleDetailsList;
    }

    public boolean updateUserRoleType(String userName, String roleType) {
        Map<String, String> qParam = new HashMap<String, String>();
        if (StringUtils.isNotEmpty(userName) && StringUtils.isNotEmpty(roleType)) {
            qParam.put("userName", userName);
            qParam.put("roleType", roleType);
        } else {
            return false;
        }
        RestResponse restResponse = this.restRequestBuilder.build("updateRoleType", qParam).get();
        if (Response.Status.OK.getStatusCode() == restResponse.getStatus()) {
            return true;
        } else {
            return false;
        }
    }

    public boolean updateUserRoles(UserRoleDetailsList userRoleDetailsList) {
        RestResponse restResponse = this.restRequestBuilder.build("updateUserRoles").post(userRoleDetailsList);
        if (Response.Status.OK.getStatusCode() == restResponse.getStatus()) {
            return true;
        } else {
            return false;
        }
    }

    public SubGroup getSubGroups() {
        SubGroup subGroup =null;
        try {
            RestResponse restResponse = this.restRequestBuilder.build("getSubGroups").get();
             subGroup= restResponse.getEntity(new GenericType<SubGroup>() {
            });
        } catch (Exception e) {
            return subGroup;
        }
        return subGroup;
    }

    public SubGroup getUserSubGroups(String loginId) {
        SubGroup subGroup =null;
        Map<String, String> qParam = new HashMap<String, String>();
        qParam.put("loginId", loginId);
        try {
            RestResponse restResponse = this.restRequestBuilder.build("getUserSubGroups",qParam).get();
            subGroup= restResponse.getEntity(new GenericType<SubGroup>() {
            });
        } catch (Exception e) {
            return subGroup;
        }
        return subGroup;
    }

    public RestResponse addUserSubGroup(String userSubGroup, String loginId, String ein) {
        Map<String, String> qParam = new HashMap<String, String>();
        qParam.put("userSubGroup", userSubGroup);
        qParam.put("loginId", loginId);
        qParam.put("ein", ein);
        try {
            return this.restRequestBuilder.build("addUserSubGroup", qParam).post();
        } catch (Exception e) {
               return null;
        }

    }


    public RestResponse addSubGroup(String subGroup) {
        Map<String, String> qParam = new HashMap<String, String>();
        qParam.put("subGroup", subGroup);
        try {
            return this.restRequestBuilder.build("addSubGroup",qParam).post();
        } catch (Exception e) {
            return null;
        }
    }


    public RestResponse deleteUserSubGroup(String userSubGroup, String loginId, String ein) {
        Map<String, String> qParam = new HashMap<String, String>();
        qParam.put("userSubGroup", userSubGroup);
        qParam.put("loginId", loginId);
        qParam.put("ein", ein);
        try {
            return this.restRequestBuilder.build("deleteUserSubGroup",qParam).post();
        } catch (Exception e) {
            return null;
        }
    }
}
