package com.bt.usermanagement.repository;

import com.bt.usermanagement.repository.entitiy.RoleMasterEntity;
import com.bt.usermanagement.repository.entitiy.UserMasterEntity;
import com.bt.usermanagement.repository.entitiy.UserRoleEntity;

import java.util.List;

public interface UserManagementRepository {
    UserMasterEntity getUserByUserId(String userId);
    List<UserMasterEntity> getUserByEINOrName(String einFirstNameLastName);
    void updateUserFromLDAP(UserMasterEntity user);
    void addRoleToUser(UserRoleEntity userRole);
    void deleteRoleFromUser(UserRoleEntity userRole);
    List<RoleMasterEntity> getAllRoles();
    List<RoleMasterEntity> getAllRolesByRoleGroup(String targetSystem);
    int updateLastLoginByUser(String userId);
    List<RoleMasterEntity> getAllRoleForUserId(String userId);
}

