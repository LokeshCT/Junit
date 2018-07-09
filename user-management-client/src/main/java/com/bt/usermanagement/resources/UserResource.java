package com.bt.usermanagement.resources;

import com.bt.rsqe.factory.RestRequestBuilderFactory;
import com.bt.rsqe.utils.UriBuilder;
import com.bt.rsqe.web.rest.RestRequestBuilder;
import com.bt.usermanagement.config.UserManagementClientConfig;
import com.bt.usermanagement.dto.RoleMasterDTO;
import com.bt.usermanagement.dto.UserDTO;
import com.bt.usermanagement.dto.UserRoleDTO;

import javax.ws.rs.core.GenericType;
import java.net.URI;
import java.util.HashMap;
import java.util.List;


public class UserResource {

    private RestRequestBuilder restRequestBuilder;

    public UserResource(URI baseURI, String secret, RestRequestBuilderFactory restRequestBuilderFactory) {
        URI uri = UriBuilder.buildUri(baseURI, "user-management");
        this.restRequestBuilder = restRequestBuilderFactory.createProxyAwareRestRequestBuilder(uri).withSecret(secret);
    }

    public UserResource(UserManagementClientConfig userManagementConfig) {
        this(UriBuilder.buildUri(userManagementConfig.getApplicationConfig()), userManagementConfig.getRestAuthenticationClientConfig().getSecret(), new RestRequestBuilderFactory());
    }

    /*
        This method is used to fetch single user based in userID.
     */
    public UserDTO getUserByUserId(String userId) {
        HashMap<String, String> qParams = new HashMap<String, String>();
        qParams.put("userID", userId);
        return this.restRequestBuilder.build("getUserByUserId", qParams)
                                      .get()
                                      .getEntity(new GenericType<UserDTO>(){});

    }

    /*
        This method is used to fetch single or multiple users based on EIN/First Name/Last Name.
        It can also accept some characters of EIN/First Name/Last Name and return the list of matching records.
        For Example : for input 60 it will return all the EINs/First Name/Last Name starting with 60.
     */
    public List<UserDTO> getUserByEINOrName(String einFirstNameLastName) {
        HashMap<String, String> qParams = new HashMap<String, String>();
        qParams.put("einFirstNameLastName", einFirstNameLastName);
        return this.restRequestBuilder.build("getUserByEINOrName", qParams)
                                       .get()
                                       .getEntity(new GenericType<List<UserDTO>>(){});

    }

    /*
        This method is used to fetch all roles under user management.
        This method should only be used by user management landing page which checks user's privileges under multiple role groups.
        For an individual module related roles in rSQE getAllRolesByRoleGroup should be used.
     */
    public List<RoleMasterDTO> getAllRoles(){
        HashMap<String, String> qParams = new HashMap<String, String>();
        return this.restRequestBuilder.build("getAllRoles", qParams)
                                      .get()
                                      .getEntity(new GenericType<List<RoleMasterDTO>>(){});

    }

    /*
        This method is used to fetch specific roles for a role group. For example : CQM or NRM etc
        Role Group Constants are declared in UserManagementConstants.java
     */
    public List<RoleMasterDTO> getAllRolesByRoleGroup(String roleGroupId) {
        HashMap<String, String> qParams = new HashMap<String, String>();
        qParams.put("roleGroupId", roleGroupId);
        return this.restRequestBuilder.build("getAllRolesByRoleGroup", qParams)
                                      .get()
                                      .getEntity(new GenericType<List<RoleMasterDTO>>(){});

    }

    /*
        This method is used to add single role to user.
     */
    public Boolean addRoleToUser(UserRoleDTO userRole) {
        return this.restRequestBuilder.build("addRoleToUser")
                                      .post(userRole)
                                      .getEntity(new GenericType<Boolean>(){});

    }

    /*
        This method is used to remove single role from user.
     */
    public Boolean deleteRoleFromUser(UserRoleDTO userRole) {
        return this.restRequestBuilder.build("deleteRoleFromUser")
                                      .post(userRole)
                                      .getEntity(new GenericType<Boolean>(){});

    }
}
