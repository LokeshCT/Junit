package com.bt.cqm.handler;

import com.bt.cqm.dto.user.RoleTypeDTO;
import com.bt.cqm.dto.user.SalesChannelDTO;
import com.bt.cqm.dto.user.SubGroupDTO;
import com.bt.cqm.dto.user.UserDTO;
import com.bt.cqm.dto.user.UserRoleMasterDTO;
import com.bt.cqm.exception.SalesChannelNotFoundException;
import com.bt.cqm.ldap.LDAPConstants;
import com.bt.cqm.ldap.SearchBTDirectoryHandler;
import com.bt.cqm.ldap.model.LdapSearchModel;
import com.bt.cqm.repository.user.SalesChannelEntity;
import com.bt.cqm.repository.user.UserEntity;
import com.bt.cqm.repository.user.UserManagementRepository;
import com.bt.cqm.repository.user.UserRoleConfigEntity;
import com.bt.cqm.repository.user.UserRoleMasterEntity;
import com.bt.cqm.repository.user.UserSalesChannelEntity;
import com.bt.cqm.web.WebUtils;
import com.bt.rsqe.expedio.usermanagement.RoleDetails;
import com.bt.rsqe.expedio.usermanagement.SubGroup;
import com.bt.rsqe.expedio.usermanagement.UserManagementResource;
import com.bt.rsqe.expedio.usermanagement.UserRoleDetails;
import com.bt.rsqe.expedio.usermanagement.UserRoleDetailsList;
import com.bt.rsqe.rest.ResponseBuilder;
import com.bt.rsqe.utils.AssertObject;
import com.bt.rsqe.web.rest.RestResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Path("/cqm/userManagement")
@Produces(MediaType.APPLICATION_JSON)
@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
public class UserManagementHandler {


    private static final Logger LOG = LoggerFactory.getLogger(UserManagementHandler.class);
    private final UserManagementRepository userManagementRepository;
    private final UserManagementResource userManagementResource;
    private static final String DIRECT_ROLE_CONSTANT = "1";

    public UserManagementHandler(UserManagementRepository repository, UserManagementResource userManagementResource) {
        this.userManagementRepository = repository;
        this.userManagementResource = userManagementResource;
    }

    @GET
    @Path("/getUserInfo")
    public Response getUserRoleList(@QueryParam("loginId") String loginId) {

        if (AssertObject.isEmpty(loginId)) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        UserDTO userDTO = new UserDTO();
        try {

            //Fetch the EIN based on the boatId
            LdapSearchModel ldapSearchResult = searchBTDirectory(loginId.toUpperCase());
            UserEntity userEntity = null;
            String userId = null;

            if (ldapSearchResult != null) {
                userId = ldapSearchResult.getEin();
                //Fetch the USER INFO  based on the EIN
                userEntity = userManagementRepository.findUserByUserId(userId);
            } else {
                try {
                    userEntity = userManagementRepository.findUserByUserId(loginId);
                } catch (Exception e) {

                }
            }

            //List<RoleDetails> roleDetailsList = userManagementResource.getUserRoleDetails(loginId);
            //if user is not active then he should not be able to to see any sales channel.
            if (userEntity == null || userEntity.getActive().equals("N")) {
                return WebUtils.responseNotFound("No Active User found for login ID - \"" + loginId + "\"");
            }
            userDTO = getUserDTO(userEntity);


        } catch (Exception e) {
            LOG.error("Failed to fetch UserInfo", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }

        GenericEntity<UserDTO> entity = new GenericEntity<UserDTO>(userDTO) {
        };

        return ResponseBuilder.anOKResponse().withEntity(entity).build();
    }

    @POST
    @Path("/saveUserInfo")
    public Response saveUserInfo(UserDTO userDTO) {

        try {
            userManagementRepository.updateUserInfo(userDTO);
            //Updating Expedio Role Type
            try {
                String roleType = DIRECT_ROLE_CONSTANT.equals(userDTO.getUserRoleTypeId()) ? "Direct" : "Indirect";
                boolean result = userManagementResource.updateUserRoleType(userDTO.getLoginId(), roleType);
            } catch (Exception e) {
            }
            //Updating Expedio User Roles
            try {
                updateExpedioUserRoles(userDTO);
            } catch (Exception e) {
            }

        } catch (Exception e) {
            LOG.error("Couldn't save user info.", e);
            return ResponseBuilder.internalServerError().build();
        }

        return ResponseBuilder.anOKResponse().build();
    }

    @GET
    @Path("/getAllRoles")
    public Response getRoles(@QueryParam("roleTypeID") Integer roleTypeId) {
        if (AssertObject.isNull(roleTypeId)) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        List<UserRoleMasterEntity> roles = null;
        try {
            roles = userManagementRepository.getRoles(roleTypeId);
        } catch (Exception ex) {
            LOG.error("Failed to get All Roles", ex);
            return ResponseBuilder.internalServerError().build();
        }

        List<UserRoleMasterDTO> rolesDTO = new ArrayList<UserRoleMasterDTO>();
        for (UserRoleMasterEntity roleEntity : roles) {
            rolesDTO.add(roleEntity.toNewDTO());
        }

        GenericEntity<List<UserRoleMasterDTO>> entity = new GenericEntity<List<UserRoleMasterDTO>>(rolesDTO) {
        };
        return ResponseBuilder.anOKResponse().withEntity(entity).build();
    }

    @GET
    @Path("/getAllSalesChannels")
    public Response getSalesChannel(@QueryParam("roleTypeID") Integer roleTypeId) {
        if (AssertObject.isNull(roleTypeId)) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        List<SalesChannelEntity> salesChannelList = null;
        try {
            salesChannelList = userManagementRepository.getSalesChannels(roleTypeId);

        } catch (Exception ex) {
            LOG.error("Failed to get All Roles", ex);
            return ResponseBuilder.internalServerError().build();
        }

        List<SalesChannelDTO> salesChannelDTOs = new ArrayList<SalesChannelDTO>();
        for (SalesChannelEntity scEntity : salesChannelList) {
            salesChannelDTOs.add(scEntity.toNewDTO());
        }

        GenericEntity<List<SalesChannelDTO>> entity = new GenericEntity<List<SalesChannelDTO>>(salesChannelDTOs) {
        };
        return ResponseBuilder.anOKResponse().withEntity(entity).build();
    }


    @GET
    @Path("/getUserSubGroups")
    public Response getUserSubGroups(@QueryParam("loginId") String loginId, @HeaderParam("SM_USER") String userId) {

        if (AssertObject.isEmpty(loginId)) {
            loginId = userId;
            //return Response.status(Response.Status.NOT_FOUND).build();
        }
        List<SubGroupDTO> subGroupDTOList = new ArrayList<SubGroupDTO>();
        String[] listOfSubGroups = null;
        try {
            //Fetch the EIN based on the boatId
            LdapSearchModel ldapSearchResult = searchBTDirectory(loginId.toUpperCase());
            if (ldapSearchResult == null) {
                ldapSearchResult = searchBTDirectoryWithEIN(loginId);
                if (ldapSearchResult != null) {
                    loginId = ldapSearchResult.getBoatId();
                }

            }
            UserEntity userEntity = null;
            //String userId = null;
            if (ldapSearchResult != null) {
                userId = ldapSearchResult.getEin();
                //Fetch the USER INFO  based on the EIN
                userEntity = userManagementRepository.findUserByUserId(userId);
            } else {
                try {
                    userEntity = userManagementRepository.findUserByUserId(loginId);
                } catch (Exception e) {

                }
            }
            //if user is not active then he should not be able to to see any sales channel.
            if (userEntity == null || userEntity.getActive().equals("N")) {
                return WebUtils.responseNotFound("No Active User found for login ID - \"" + loginId + "\"");
            }
            listOfSubGroups = getUserSubGroupDTO(loginId.toLowerCase());
            if (listOfSubGroups == null) {
                return ResponseBuilder.notFound().withEntity("No Sub User found for login ID - \"" + loginId + "\"").build();
                // return WebUtils.responseNotFound("No Sub User found for login ID - \"" + loginId + "\"");
            }
        } catch (Exception e) {
            LOG.error("Failed to fetch UserInfo", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
        GenericEntity<String[]> entity = new GenericEntity<String[]>(listOfSubGroups) {
        };
        return ResponseBuilder.anOKResponse().withEntity(entity).build();
    }


    @GET
    @Path("/getSubGroups")
    public Response getSubGroups(@QueryParam("loginId") String loginId) {
        String[] listOfSubGroups = null;
        try {
            listOfSubGroups = getSubGroupDTO();
        } catch (Exception e) {
            LOG.error("Failed to fetch UserInfo", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
        GenericEntity<String[]> entity = new GenericEntity<String[]>(listOfSubGroups) {
        };
        return ResponseBuilder.anOKResponse().withEntity(entity).build();
    }

    private String[] getUserSubGroupDTO(String loginId) throws SalesChannelNotFoundException {
        SubGroupDTO subGroupDTO = new SubGroupDTO();
        String[] listOfSubGroups = null;
        SubGroup subGroup = userManagementResource.getUserSubGroups(loginId);
        String subGroupString = "";
        if (null != subGroup) {
            subGroupString = subGroup.getSubGroupList();
        }
        if (null != subGroupString && subGroupString.length() > 0) {
            listOfSubGroups = subGroupString.split(",");
        }
        return listOfSubGroups;
    }

    @GET
    @Path("/addUserSubGroup")
    @Produces(MediaType.TEXT_HTML)
    public Response addUserSubGroup(@QueryParam("userSubGroup") String userSubGroup,
                                    @QueryParam("loginId") String loginId,
                                    @QueryParam("ein") String ein) {
        boolean result = false;
        RestResponse response = null;
        if (AssertObject.isEmpty(userSubGroup)) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        try {
            response = userManagementResource.addUserSubGroup(userSubGroup, loginId, ein);
        } catch (Exception e) {
        }

        if (response != null && response.getStatus() == Response.Status.OK.getStatusCode()) {
            String successMessage = response.getEntity(new GenericType<String>() {
            });
            if (null != successMessage) {
                successMessage = successMessage.substring(1, successMessage.length() - 1);
            }
            GenericEntity<String> entity = new GenericEntity<String>(successMessage) {
            };
            return ResponseBuilder.anOKResponse().withEntity(entity).build();
        } else {
            return WebUtils.responseNotFound("Sub Group Addition Failed ");

        }
    }

    @GET
    @Path("/addSubGroup")
    @Produces(MediaType.TEXT_HTML)
    public Response addSubGroup(@QueryParam("subGroup") String subGroup) {
        RestResponse response = null;
        if (AssertObject.isEmpty(subGroup)) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        try {
            response = userManagementResource.addSubGroup(subGroup);
        } catch (Exception e) {

        }
        if (response != null && response.getStatus() == Response.Status.OK.getStatusCode()) {
            String successMessage = response.getEntity(new GenericType<String>() {
            });
            if (null != successMessage) {
                successMessage = successMessage.substring(1, successMessage.length() - 1);
            }
            GenericEntity<String> entity = new GenericEntity<String>(successMessage) {
            };
            return ResponseBuilder.anOKResponse().withEntity(entity).build();
        } else {
            return ResponseBuilder.notFound().withEntity("Sub Group Addition Failed").build();
        }
    }

    @GET
    @Path("/deleteUserSubGroup")
    @Produces(MediaType.TEXT_HTML)
    public Response deleteUserSubGroup(@QueryParam("userSubGroup") String userSubGroup,
                                       @QueryParam("loginId") String loginId,
                                       @QueryParam("ein") String ein) {
        RestResponse response = null;
        if (AssertObject.isEmpty(userSubGroup)) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        try {
            response = userManagementResource.deleteUserSubGroup(userSubGroup, loginId, ein);
        } catch (Exception e) {

        }
        if (response != null && response.getStatus() == Response.Status.OK.getStatusCode()) {
            String successMessage = response.getEntity(new GenericType<String>() {
            });
            if (null != successMessage) {
                successMessage = successMessage.substring(1, successMessage.length() - 1);
            }
            GenericEntity<String> entity = new GenericEntity<String>(successMessage) {
            };
            return ResponseBuilder.anOKResponse().withEntity(entity).build();
        } else {
            return ResponseBuilder.notFound().withEntity("Sub Group Deletion Failed").build();
        }
    }

    private String[] getSubGroupDTO() throws SalesChannelNotFoundException {
        SubGroupDTO subGroupDTO = new SubGroupDTO();
        String[] listOfSubGroups = null;
        SubGroup subGroup = userManagementResource.getSubGroups();
        String subGroupString = "";
        if (null != subGroup) {
            subGroupString = subGroup.getSubGroupList();
        }
        if (null != subGroupString && subGroupString.length() > 0) {
            listOfSubGroups = subGroupString.split(",");
        }
        List<SubGroupDTO> subGroupDTOList = new ArrayList<SubGroupDTO>();
        for (int i = 0; i < listOfSubGroups.length; i++) {
            subGroupDTO.setSubGroupName(listOfSubGroups[i]);
            subGroupDTOList.add(subGroupDTO);
        }
        return listOfSubGroups;
    }

    private UserDTO getUserDTO(UserEntity userEntity) throws SalesChannelNotFoundException {

        RoleTypeDTO roleTypeDTO = new RoleTypeDTO();
        if (userEntity.getUserType() != null) {
            roleTypeDTO = new RoleTypeDTO(userEntity.getUserType().getRoleTypeId(), userEntity.getUserType().getRoleTypeName());
        }
        //Set All Assigned Roles
        List<UserRoleConfigEntity> roleConfig = userEntity.getUserRoleConfig();
        List<UserRoleMasterDTO> assignedRoles = null;
        if (roleConfig != null && roleConfig.size() > 0) {
            assignedRoles = new ArrayList<UserRoleMasterDTO>();

            for (UserRoleConfigEntity aRoleConfig : roleConfig) {
                UserRoleMasterEntity role = aRoleConfig.getRole();
                assignedRoles.add(new UserRoleMasterDTO(role.getRoleId(), role.getRoleName(), aRoleConfig.isDefaultRole()));
            }
        }

        //Set Associated Sales Channel
        //Fetch all the Sales Channel associated with the LoginID
        List<UserSalesChannelEntity> salesChannelsAssociatedWithTheUser =
            userManagementRepository.getSalesChannelsAssociatedWithUser(userEntity.getUserId());

        List<SalesChannelDTO> userSalesChannelDTOList = new ArrayList<SalesChannelDTO>();
        if (salesChannelsAssociatedWithTheUser != null && !salesChannelsAssociatedWithTheUser.isEmpty()) {
            for (UserSalesChannelEntity userSalesChannelEntity : salesChannelsAssociatedWithTheUser) {
                SalesChannelDTO salesChannelDTO = new SalesChannelDTO();
                salesChannelDTO.setUserId(userSalesChannelEntity.getUserId());
                salesChannelDTO.setName(userSalesChannelEntity.getSalesChannel());
                salesChannelDTO.setCreateUser(userSalesChannelEntity.getCreatedUser());
                salesChannelDTO.setCreatedDate(userSalesChannelEntity.getCreatedDate());
                salesChannelDTO.setModifiedUser(userSalesChannelEntity.getModifiedUser());
                salesChannelDTO.setModifiedDate(userSalesChannelEntity.getModifiedDate());
                salesChannelDTO.setDefault(userSalesChannelEntity.isDefaultSalesChannel());

                userSalesChannelDTOList.add(salesChannelDTO);
            }
        } else {
            userSalesChannelDTOList.add(new SalesChannelDTO());
        }
        UserDTO userDTO = new UserDTO();
        userDTO.setUserId(userEntity.getUserId());
        userDTO.setUserName(userEntity.getUserName());
        userDTO.setActive(userEntity.getActive());
        userDTO.setCreateUser(userEntity.getCreatedUser());
        userDTO.setCreatedDate(userEntity.getCreatedDate());
        userDTO.setModifiedUser(userEntity.getModifiedUser());
        userDTO.setModifiedDate(userEntity.getModifiedDate());
        userDTO.setUserType(roleTypeDTO);
        userDTO.setRoles(assignedRoles);
        userDTO.setUserSalesChannelList(userSalesChannelDTOList);
        return userDTO;

    }


    private LdapSearchModel searchBTDirectory(String boatId) {
        Map<String, String> args = new HashMap<String, String>();
        args.put(LDAPConstants.BOAT_ID, boatId);
        List<LdapSearchModel> resultList = new SearchBTDirectoryHandler().searchBTDirectory(args);
        LdapSearchModel ldapSearchResult = null;
        if (resultList != null && !resultList.isEmpty()) {
            ldapSearchResult = resultList.get(0);
        }
        return ldapSearchResult;
    }

    private LdapSearchModel searchBTDirectoryWithEIN(String ein) {
        Map<String, String> args = new HashMap<String, String>();
        args.put(LDAPConstants.EIN, ein);
        List<LdapSearchModel> resultList = new SearchBTDirectoryHandler().searchBTDirectory(args);
        LdapSearchModel ldapSearchResult = null;
        if (resultList != null && !resultList.isEmpty()) {
            ldapSearchResult = resultList.get(0);
        }
        return ldapSearchResult;
    }

    private void updateExpedioUserRoles(UserDTO userDTO) {
        List<RoleDetails> roleDetailsList = (List<RoleDetails>) userManagementResource.getUserRoleDetails(userDTO.getLoginId());
        List<UserRoleMasterDTO> userRoleMasterDTOList = userDTO.getRoles();
        List<UserRoleDetails> roleDetailsListNew = new LinkedList<UserRoleDetails>();
        //Removing the Deleted Roles
        for (int i = 0; i < roleDetailsList.size(); i++) {
            boolean isEntityFound = false;
            for (int j = 0; j < userRoleMasterDTOList.size(); j++) {

                if (roleDetailsList.get(i).getUserRole().equals(userRoleMasterDTOList.get(j).getRoleName())) {
                    isEntityFound = true;
                    break;
                }
            }
            if (!isEntityFound) {
                UserRoleDetails roleDetails = new UserRoleDetails();
                roleDetails.setUserRole(roleDetailsList.get(i).getUserRole());
                roleDetails.setOperation("Remove");
                roleDetails.setUserName(userDTO.getLoginId());
                roleDetails.setOrderType("");
                roleDetailsListNew.add(roleDetails);
            }
        }
        //Adding the new Roles
        for (int i = 0; i < userRoleMasterDTOList.size(); i++) {
            boolean isEntityFound = false;
            for (int j = 0; j < roleDetailsList.size(); j++) {

                if (roleDetailsList.get(j).getUserRole().equals(userRoleMasterDTOList.get(i).getRoleName())) {
                    isEntityFound = true;
                    break;
                }
            }
            if (!isEntityFound) {
                UserRoleDetails roleDetails = new UserRoleDetails();
                roleDetails.setUserRole(userRoleMasterDTOList.get(i).getRoleName());
                roleDetails.setOperation("Add");
                roleDetails.setOrderType("");
                roleDetails.setUserName(userDTO.getLoginId());
                roleDetailsListNew.add(roleDetails);
            }
        }
        UserRoleDetailsList userRoleDetailsList = new UserRoleDetailsList();
        userRoleDetailsList.setUserRoleDetails(roleDetailsListNew);
        userManagementResource.updateUserRoles(userRoleDetailsList);
    }

}
