package com.bt.rsqe.expedio.usermanagement;

import com.bt.rsqe.ContainerUtils;
import com.bt.rsqe.container.Application;
import com.bt.rsqe.container.ApplicationConfig;
import com.bt.rsqe.container.StubApplicationConfig;
import com.bt.rsqe.rest.ResponseBuilder;
import com.bt.rsqe.soap.WebServiceConfigException;
import com.bt.rsqe.utils.UriBuilder;
import com.bt.rsqe.web.rest.RestResponse;
import org.hamcrest.core.Is;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.collect.Lists.*;
import static org.junit.Assert.*;

/**
 * Created with IntelliJ IDEA.
 * User: 607982105
 * Date: 01/04/15
 * Time: 12:58
 * To change this template use File | Settings | File Templates.
 */
public class UserManagementResourceTest {


    private static ApplicationConfig applicationConfig = StubApplicationConfig.defaultTestConfig();
    private static Application application;
    private static UserManagementResource userManagementResource;

    @BeforeClass
    public static void startContainer() throws IOException {
        application = ContainerUtils.startContainer(applicationConfig, new TestHandler());
        userManagementResource = new UserManagementResource(UriBuilder.buildUri(applicationConfig), null);
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void shouldGetUserRoleDetails() throws Exception {
        List<RoleDetails> roleDetailsList = userManagementResource.getUserRoleDetails("kvkkxx");
        assertThat(roleDetailsList.size(), Is.is(1));
    }

    @Test
    public void shouldGetUserRoleDetailsException() throws Exception {
        List<RoleDetails> roleDetailsList = userManagementResource.getUserRoleDetails(null);
        assertEquals(roleDetailsList, null);
    }

    @Test
    public void shouldUpdateUserRoleType() throws Exception {
        boolean result = userManagementResource.updateUserRoleType("kvkkxx","Direct");
        assertEquals(result, true);
    }


    @Test
    public void shouldUpdateUserRoleTypeFail() throws Exception {
        boolean result = userManagementResource.updateUserRoleType(null,null);
        assertEquals(result, false);
    }


    @Test
    public void shouldUpdateUserRoleTypeInternalServerError() throws Exception {
        boolean result = userManagementResource.updateUserRoleType("roygxx","Indirect");
        assertEquals(result, false);
    }

    @Test
    public void shouldUpdateUserRoles() throws Exception {
        UserRoleDetails roleDetails = new UserRoleDetails("Indirect", "", "Add","kvkkxx");
        List<UserRoleDetails> roleDetailsList = new ArrayList<UserRoleDetails>(1);
        roleDetailsList.add(roleDetails);
        UserRoleDetailsList userRoleDetailsList=new UserRoleDetailsList(roleDetailsList);
        boolean result=userManagementResource.updateUserRoles(userRoleDetailsList);
        assertTrue(result);
    }

    @Test
    public void shouldUpdateUserRolesFailure() throws Exception {
        UserRoleDetails roleDetails = new UserRoleDetails("Indirect", "", "Add","roygxx");
        List<UserRoleDetails> roleDetailsList = new ArrayList<UserRoleDetails>(1);
        roleDetailsList.add(roleDetails);
        UserRoleDetailsList userRoleDetailsList=new UserRoleDetailsList(roleDetailsList);
        boolean result=userManagementResource.updateUserRoles(userRoleDetailsList);
        assertFalse(result);
    }

    @Test
    public void shouldGetGroups() throws Exception {
        SubGroup subGroup= userManagementResource.getSubGroups();
        assertEquals("SUCCESS",subGroup.getMessage());
    }

    @Test
    public void shouldGetUserGroups() throws Exception {
        SubGroup subGroup= userManagementResource.getUserSubGroups("kvkkxx");
        assertEquals("SUCCESS",subGroup.getMessage());
    }

    @Test
    public void shouldAddUserGroup() throws Exception {
        RestResponse restResponse= userManagementResource.addUserSubGroup("BB1","kvkkxx","123");
        assertEquals(Response.Status.OK.getStatusCode(),restResponse.getStatus());

    }

    @Test
    public void shouldAddGroup() throws Exception {
        RestResponse restResponse = userManagementResource.addSubGroup("BB1");
        assertEquals(Response.Status.OK.getStatusCode(),restResponse.getStatus());
    }

    @Test
    public void shouldDeleteUserSubGroup() throws Exception {
        RestResponse restResponse =userManagementResource.deleteUserSubGroup("BB1","kvkkxx","123");
        assertEquals(Response.Status.OK.getStatusCode(),restResponse.getStatus());
    }


    @Path("/rsqe/expedio/usermanagement")
    public static class TestHandler {

        @GET
        @Path("/getUserDetails")
        @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
        public Response getUserRoleDetails(@QueryParam("userName") String userName) throws URISyntaxException, WebServiceConfigException {
            GenericEntity<List<RoleDetails>> entity = new GenericEntity<List<RoleDetails>>(newArrayList(new RoleDetails("TestRoleType", "TestRole")
            )) {
            };
            return ResponseBuilder.anOKResponse().withEntity(entity).build();
        }


        @GET
        @Path("/updateRoleType")
        public Response updateUserRoleType(@QueryParam("userName") String userName, @QueryParam("roleType") String roleType ) throws URISyntaxException, WebServiceConfigException {
            if("kvkkxx".equals(userName)) {
                return ResponseBuilder.anOKResponse().build();
            }
            return null;
        }

        @POST
        @Path("/updateUserRoles")
        public Response updateUserRoles(UserRoleDetailsList userRoleDetailsList) throws URISyntaxException, WebServiceConfigException {
            if("kvkkxx".equals(userRoleDetailsList.getUserRoleDetails().get(0).getUserName())) {
                return ResponseBuilder.anOKResponse().build();
            }
            return null;
        }

        @GET
        @Path("/getSubGroups")
        public Response getSubGroups(@QueryParam("userName") String userName) throws URISyntaxException, WebServiceConfigException {
            SubGroup subGroup=new SubGroup("SUCCESS","BB1,BB2") ;
            GenericEntity<SubGroup> entity = new GenericEntity<SubGroup>(subGroup) {
            };
            return ResponseBuilder.anOKResponse().withEntity(entity).build();
        }

        @GET
        @Path("/getUserSubGroups")
        public Response getUserSubGroups(@QueryParam("loginId") String loginId) throws URISyntaxException, WebServiceConfigException {
            SubGroup subGroup=new SubGroup("SUCCESS","BB1,BB2") ;
            GenericEntity<SubGroup> entity = new GenericEntity<SubGroup>(subGroup) {
            };
            return ResponseBuilder.anOKResponse().withEntity(entity).build();
        }

        @POST
        @Path("/addUserSubGroup")
        public Response addUserSubGroup(@QueryParam("userSubGroup") String userSubGroup,@QueryParam("loginId") String loginId,@QueryParam("ein") String ein) throws URISyntaxException, WebServiceConfigException {
            SubGroup subGroup=new SubGroup("SUCCESS","BB1,BB2") ;
            GenericEntity<SubGroup> entity = new GenericEntity<SubGroup>(subGroup) {
            };
            return ResponseBuilder.anOKResponse().withEntity(entity).build();
        }

        @POST
        @Path("/addSubGroup")
        public Response addSubGroup(@QueryParam("userSubGroup") String userSubGroup) throws URISyntaxException, WebServiceConfigException {
            SubGroup subGroup=new SubGroup("SUCCESS","BB1,BB2") ;
            GenericEntity<SubGroup> entity = new GenericEntity<SubGroup>(subGroup) {
            };
            return ResponseBuilder.anOKResponse().withEntity(entity).build();
        }

        @POST
        @Path("/deleteUserSubGroup")
        public Response deleteUserSubGroup(@QueryParam("userSubGroup") String userSubGroup,@QueryParam("loginId") String loginId,@QueryParam("ein") String ein) throws URISyntaxException, WebServiceConfigException {
            SubGroup subGroup=new SubGroup("SUCCESS","BB1,BB2") ;
            GenericEntity<SubGroup> entity = new GenericEntity<SubGroup>(subGroup) {
            };
            return ResponseBuilder.anOKResponse().withEntity(entity).build();
        }
    }

}


