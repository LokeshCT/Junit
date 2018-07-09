package com.bt.usermanagement.handler;

import com.bt.rsqe.rest.ResponseBuilder;
import com.bt.rsqe.web.Presenter;
import com.bt.rsqe.web.ViewFocusedResourceHandler;
import com.bt.usermanagement.dto.RoleMasterDTO;
import com.bt.usermanagement.dto.UserDTO;
import com.bt.usermanagement.dto.UserRoleDTO;
import com.bt.usermanagement.ldap.LDAPConstants;
import com.bt.usermanagement.ldap.LdapSearchException;
import com.bt.usermanagement.ldap.SearchBTDirectoryHandler;
import com.bt.usermanagement.ldap.model.LdapSearchModel;
import com.bt.usermanagement.repository.UserManagementRepository;
import com.bt.usermanagement.repository.entitiy.RoleMasterEntity;
import com.bt.usermanagement.repository.entitiy.UserMasterEntity;
import com.bt.usermanagement.repository.entitiy.UserRoleEntity;
import com.bt.usermanagement.repository.entitiy.UserRoleID;
import com.bt.usermanagement.util.UserManagementConstants;
import com.bt.usermanagement.util.GeneralUtil;
import com.bt.usermanagement.util.UrlConfiguration;
import com.bt.usermanagement.web.WebUtils;
import org.codehaus.jettison.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.NoResultException;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.bt.rsqe.utils.AssertObject.*;

@Path("/user-management")
@Produces(MediaType.APPLICATION_JSON)
@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
public class UserManagementHandler extends ViewFocusedResourceHandler {
    private static final Logger LOG = LoggerFactory.getLogger(UserManagementHandler.class);
    private final UserManagementRepository userManagementRepository;

    public UserManagementHandler(UserManagementRepository repository) {
        super(new Presenter());
        this.userManagementRepository = repository;
    }

    @GET
    @Path("/getUserByUserId")
    public Response getUserByUserId(@QueryParam("userID") String userID) {
        try {
            UserMasterEntity userEntity = userManagementRepository.getUserByUserId(userID);
            //read user info from LDAP and update local USER MANAGEMENT DB user entry
            List<LdapSearchModel> ldapSearchModelList = searchBTDirectory(userID);
            if(ldapSearchModelList != null && ldapSearchModelList.size() != 0 ){
                userEntity = populateUpdatedLdapUserEntity(ldapSearchModelList.get(0),userEntity);
                userManagementRepository.updateUserFromLDAP(userEntity);
            }
            return Response.ok().entity(userEntity.toNewDto()).build();
        }catch(LdapSearchException lse) {
            return Response.ok().entity(userManagementRepository.getUserByUserId(userID).toNewDto()).build();
        }catch (NoResultException ex) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }catch (Exception e){
            e.printStackTrace();
            return Response.status(Response.Status.EXPECTATION_FAILED).build();
        }
    }

    @GET
    @Path("/getUserByEINOrName")
    public Response getUserByEINOrName(@QueryParam("einFirstNameLastName") String einFirstNameLastName) {
        try {
            List<UserDTO> userDTOList = new ArrayList<UserDTO>();
            List<UserMasterEntity> userEntityList = userManagementRepository.getUserByEINOrName(einFirstNameLastName);
            for(UserMasterEntity userEntity : userEntityList){
                userDTOList.add(userEntity.toNewDto());
            }
            return ResponseBuilder.anOKResponse()
                                  .withEntity(new GenericEntity<List<UserDTO>>(userDTOList) {
                                  })
                                  .build();

        } catch (NoResultException ex) {
            return Response.status(Response.Status.NOT_FOUND).build();
        } catch (Exception e){
            e.printStackTrace();
            return Response.status(Response.Status.EXPECTATION_FAILED).build();
        }
    }


    @GET
    @Path("/getAllRolesByRoleGroup")
    public Response getAllRolesByRoleGroup(@QueryParam("roleGroupId") String roleGroupId) {
        try {
            if(isNotNull(roleGroupId)){
                List<RoleMasterDTO> roleDTOs = new ArrayList<RoleMasterDTO>();
                List<RoleMasterEntity> roleEntities = userManagementRepository.getAllRolesByRoleGroup(roleGroupId);
                for(RoleMasterEntity roleEntity : roleEntities){
                    roleDTOs.add(roleEntity.toNewDTO());
                }
                return ResponseBuilder.anOKResponse()
                                      .withEntity(new GenericEntity<List<RoleMasterDTO>>(roleDTOs) { })
                                      .build();
            }
            return Response.status(Response.Status.BAD_REQUEST).build();

        } catch (NoResultException ex) {
            return Response.status(Response.Status.NOT_FOUND).build();
        } catch (Exception e){
            e.printStackTrace();
            return Response.status(Response.Status.EXPECTATION_FAILED).build();
        }
    }

    @GET
    @Path("/getAllRolesByUserId")
    public Response getUserRoleInfo(@QueryParam("userID") String userId) {

        try{
            List<RoleMasterEntity> roleEntities = userManagementRepository.getAllRoleForUserId(userId);
            List<RoleMasterDTO> roleDTOs = new ArrayList<RoleMasterDTO>();
            for(RoleMasterEntity roleEntity : roleEntities){
                roleDTOs.add(roleEntity.toNewDTO());
            }
            return ResponseBuilder.anOKResponse()
                    .withEntity(new GenericEntity<List<RoleMasterDTO>>(roleDTOs) {
                    })
                    .build();

        } catch (NoResultException ex) {
            return Response.status(Response.Status.NOT_FOUND).build();
        } catch (Exception e){
            e.printStackTrace();
            return Response.status(Response.Status.EXPECTATION_FAILED).build();
        }
    }

    @GET
    @Path("/getAllRoles")
    public Response getAllRoles() {
        try {
                List<RoleMasterDTO> roleDTOs = new ArrayList<RoleMasterDTO>();
                List<RoleMasterEntity> roleEntities = userManagementRepository.getAllRoles();
                for(RoleMasterEntity roleEntity : roleEntities){
                    roleDTOs.add(roleEntity.toNewDTO());
                }
                return ResponseBuilder.anOKResponse()
                                      .withEntity(new GenericEntity<List<RoleMasterDTO>>(roleDTOs) {
                                      })
                                      .build();

        } catch (NoResultException ex) {
            return Response.status(Response.Status.NOT_FOUND).build();
        } catch (Exception e){
            e.printStackTrace();
            return Response.status(Response.Status.EXPECTATION_FAILED).build();
        }
    }

    @POST
    @Path("/addRoleToUser")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML,MediaType.TEXT_PLAIN,MediaType.TEXT_HTML})
    public Response addRoleToUser(UserRoleDTO userRole) {
        try {
            if(isNotNull(userRole)){
                UserRoleEntity userRoleEntity = new UserRoleEntity(new UserRoleID(userRole.getUserId(), userRole.getRoleId()),
                                                                               GeneralUtil.getCurrentTimeStamp(), userRole.getCreatedUser());
                userManagementRepository.addRoleToUser(userRoleEntity);
                return ResponseBuilder.anOKResponse()
                                      .withEntity(new GenericEntity<Boolean>(true) {
                                      })
                                      .build();
            }
            return Response.status(Response.Status.BAD_REQUEST).build();

        } catch (NoResultException ex) {
            return Response.status(Response.Status.NOT_FOUND).build();
        } catch (Exception e){
            e.printStackTrace();
            return Response.status(Response.Status.EXPECTATION_FAILED).build();
        }
    }

    @POST
    @Path("/deleteRoleFromUser")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML,MediaType.TEXT_PLAIN,MediaType.TEXT_HTML})
    public Response deleteRoleFromUser(UserRoleDTO userRole) {
        try {
            if(isNotNull(userRole)){
                UserRoleEntity userRoleEntity = new UserRoleEntity(new UserRoleID(userRole.getUserId(), userRole.getRoleId()), null, null);
                userManagementRepository.deleteRoleFromUser(userRoleEntity);
                return ResponseBuilder.anOKResponse()
                                      .withEntity(new GenericEntity<Boolean>(true) { })
                                      .build();
            }
            return Response.status(Response.Status.BAD_REQUEST).build();

        } catch (NoResultException ex) {
            return Response.status(Response.Status.NOT_FOUND).build();
        } catch (Exception e){
            e.printStackTrace();
            return Response.status(Response.Status.EXPECTATION_FAILED).build();
        }
    }



    private List<LdapSearchModel> searchBTDirectory(String ein) throws LdapSearchException{
        Map<String, String> args = new HashMap<String, String>();
        args.put(LDAPConstants.EIN, ein);
        List<LdapSearchModel> ldapUsers = new SearchBTDirectoryHandler().searchBTDirectory(args);
        return ldapUsers;
    }

    private UserMasterEntity populateUpdatedLdapUserEntity(LdapSearchModel ldapSearchModel, UserMasterEntity userEntity){

        userEntity.setEmailId(ldapSearchModel.getMailId());                        //TODO find salesChannels and RoleType
        userEntity.setPhoneNumber(ldapSearchModel.getPhoneNum());
        userEntity.setFirstName(ldapSearchModel.getFirstName());
        userEntity.setLastName(ldapSearchModel.getLastName());
        userEntity.setJobTitle(ldapSearchModel.getJobTitle());
        userEntity.setMobile(ldapSearchModel.getMobile());
        userEntity.setLocation(GeneralUtil.getLocationFromPostalAddress(ldapSearchModel.getPostalAddress()));
        userEntity.setModifiedUser("LDAP Updated");
        userEntity.setModifiedDate(GeneralUtil.getCurrentTimeStamp());
        return userEntity;
    }

}
