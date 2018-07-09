package com.bt.cqm.handler;

import com.bt.cqm.dto.UserCreateDTO;
import com.bt.cqm.repository.user.RoleTypeEntity;
import com.bt.cqm.repository.user.UserEntity;
import com.bt.cqm.repository.user.UserManagementRepository;
import com.bt.cqm.repository.user.UserRoleMasterEntity;
import com.bt.cqm.repository.user.UserSalesChannelEntity;
import com.bt.rsqe.utils.AssertObject;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: 607937181
 * Date: 10/11/15
 * Time: 12:23
 * To change this template use File | Settings | File Templates.
 */

@Path("/cqm/user")
@Produces(MediaType.APPLICATION_JSON)
@Consumes({MediaType.APPLICATION_JSON})
public class CqmUserResourceHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(CQMBasePageResourceHandler.class);
    private Gson gson;

    UserManagementRepository userRepository;

    public CqmUserResourceHandler(final UserManagementRepository userRepository) {
        this.userRepository = userRepository;
        this.gson = new Gson();
    }


    @POST
    @Path("create")
    public Response createUser(UserCreateDTO userCreateDTO) {
        if (userCreateDTO == null) {
            LOGGER.error("CQM :: UserCreateDTO is null.");
            return Response.status(Response.Status.BAD_REQUEST).entity("User details is null").build();
        }

        LOGGER.debug(gson.toJson(userCreateDTO));// Logging the request.

        UserValidationStatusHolder userValidationStatus = validateUserCreateDTO(userCreateDTO);

        if (userValidationStatus.isValid == false) {
            LOGGER.error("CQM ::" + userValidationStatus.error);
            return Response.status(Response.Status.BAD_REQUEST).entity(userValidationStatus.error).build();

        }

        try {

            addUserDetails(userCreateDTO);
            addUserRoleConfig(userCreateDTO);
            addSalesChannels(userCreateDTO);
        } catch (Exception e) {
            LOGGER.error("CQM :: fail to insert user details. Request details : " + gson.toJson(userCreateDTO), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Fail").build();
        }

        return Response.status(Response.Status.OK).entity("Sucess").build();
    }

    private void addUserDetails(UserCreateDTO userCreateDTO) throws Exception {
        //UserEntity user = getUserEntity(userCreateDTO);
        try {
            userRepository.createUser(userCreateDTO);
        } catch (Exception e) {
            throw e;
        }
    }

    private UserEntity getUserEntity(UserCreateDTO userCreateDTO) {
        UserEntity user = new UserEntity();
        user.setUserId(userCreateDTO.userId);
        user.setUserName(userCreateDTO.userName);
        user.setEmailId(userCreateDTO.emailId);
        if (userCreateDTO.isActiveUser) {
            user.setActive("Y");
        } else {
            user.setActive("N");
        }
        user.setCreatedUser(userCreateDTO.createdBy);
        user.setModifiedUser(userCreateDTO.modifiedBy);
        user.setCreatedDate(getDateTime());
        user.setModifiedDate(getDateTime());
        user.setUserType(getRoleTypeEntity(userCreateDTO));
        return user;
    }

    private void addUserRoleConfig(UserCreateDTO userCreateDTO) throws Exception {
        Map<String, Long> roleMap = getUserRoleList();
        List<Long> userRoles = new ArrayList<Long>();
        for (String role : userCreateDTO.userRoleTypes) {

            Long roleType = null;
            if (roleMap.containsKey(role.trim().replace(" ", ""))) {
                roleType = roleMap.get(role.trim().replace(" ", ""));
                userRoles.add(roleType);
            } else {
                LOGGER.error("userRoleType does not match. For user:" + userCreateDTO.userId + ".RoleType: " + role);
                throw new Exception("userRoleType does not match.Provided roleTye is " + role);
            }
        }

        try {
            userRepository.createUserRoleConfig(userCreateDTO.userId, userRoles);
        } catch (Exception e) {
            LOGGER.error("fail to create user role. user Id:" + userCreateDTO.userId, e);
            throw e;
        }
    }

    private Map<String, Long> getUserRoleList() throws Exception {
        Map<String, Long> roleMapList = new HashMap<String, Long>();
        List<UserRoleMasterEntity> masterEntityList = userRepository.getUserRoleMasterList();
        for (UserRoleMasterEntity entity : masterEntityList) {
            roleMapList.put(entity.getRoleName().trim().replace(" ", ""), entity.getRoleId());
        }
        return roleMapList;
    }

    private void addSalesChannels(UserCreateDTO userCreateDTO) throws Exception {
        try {
            List<UserSalesChannelEntity> userSalesChannelEntities = userRepository.getAssociatedSalesChannel(userCreateDTO.userId);
            if (userSalesChannelEntities != null && userSalesChannelEntities.size() > 0) {
                for (UserSalesChannelEntity entity : userSalesChannelEntities) {
                    userCreateDTO.salesChannels.remove(entity.getSalesChannel());
                }
            }
            userRepository.insertSalesChannels(userCreateDTO.userId, "EXPEDIO", userCreateDTO.salesChannels);
        } catch (Exception ex) {
            throw ex;
        }
    }


    private RoleTypeEntity getRoleTypeEntity(UserCreateDTO userCreateDTO) {
        RoleTypeEntity roleTypeEntity = new RoleTypeEntity();

        if (userCreateDTO.directOrIndirectUser.trim().equalsIgnoreCase("DIRECT")) {
            roleTypeEntity.setRoleTypeId(1L);
        }

        if (userCreateDTO.directOrIndirectUser.trim().equalsIgnoreCase("INDIRECT")) {
            roleTypeEntity.setRoleTypeId(2L);
        }

        return roleTypeEntity;

    }


    private UserValidationStatusHolder validateUserCreateDTO(UserCreateDTO userCreateDTO) {
        UserValidationStatusHolder statusHolder = new UserValidationStatusHolder();

        if (AssertObject.isEmpty(userCreateDTO.userId)) {
            statusHolder.isValid = false;
            statusHolder.error = statusHolder.error + "userId is empty. userId=" + userCreateDTO.userId + ".";
        }

        if (AssertObject.isEmpty(userCreateDTO.userName)) {
            statusHolder.isValid = false;
            statusHolder.error = statusHolder.error + "userName is empty. userName=" + userCreateDTO.userName + ".";
        }

        if (AssertObject.isEmpty(userCreateDTO.emailId)) {
            statusHolder.isValid = false;
            statusHolder.error = statusHolder.error + "emailId is empty. emailId=" + userCreateDTO.emailId + ".";
        }

        if (AssertObject.isEmpty(userCreateDTO.directOrIndirectUser)) {
            statusHolder.isValid = false;
            statusHolder.error = statusHolder.error + "directOrIndirectUser is empty. directOrIndirectUser=" + userCreateDTO.directOrIndirectUser + ".";
        }

        if (AssertObject.anyEmpty(userCreateDTO.userRoleTypes)) {
            statusHolder.isValid = false;
            statusHolder.error = statusHolder.error + "userRoleTypes is empty.";
        }

        if (AssertObject.anyEmpty(userCreateDTO.salesChannels)) {
            statusHolder.isValid = false;
            statusHolder.error = statusHolder.error + "salesChannels is empty.";
        }

        if (AssertObject.anyEmpty(userCreateDTO.defaultSalesChannel)) {
            statusHolder.isValid = false;
            statusHolder.error = statusHolder.error + "defaultSalesChannel is empty.";
        }
        return statusHolder;
    }

    private Date getDateTime() {

        java.util.Date date = Calendar.getInstance().getTime();

        Date sqlDate = new Date(date.getTime());
        return sqlDate;
    }

    private class UserValidationStatusHolder {
        private boolean isValid = true;
        private String error = "";
    }
}

