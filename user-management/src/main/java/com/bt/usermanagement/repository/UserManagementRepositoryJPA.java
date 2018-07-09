package com.bt.usermanagement.repository;

import com.bt.rsqe.persistence.PersistenceManager;
import com.bt.usermanagement.repository.entitiy.RoleMasterEntity;
import com.bt.usermanagement.repository.entitiy.UserMasterEntity;
import com.bt.usermanagement.repository.entitiy.UserRoleEntity;
import com.bt.usermanagement.util.GeneralUtil;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.criteria.Root;
import java.util.List;

import static com.bt.rsqe.utils.AssertObject.*;

public class UserManagementRepositoryJPA implements UserManagementRepository {
    private final PersistenceManager persistenceManager;

    public UserManagementRepositoryJPA(PersistenceManager persistenceManager) {
        this.persistenceManager = persistenceManager;
    }

    @Override
    public UserMasterEntity getUserByUserId(String userId) {
        return persistenceManager.get(UserMasterEntity.class, userId);
    }

    @Override
    public int updateLastLoginByUser(String userId){
        CriteriaBuilder cb = persistenceManager.entityManager().getCriteriaBuilder();
        CriteriaUpdate<UserMasterEntity> userEntityCriteriaUpdate = cb.createCriteriaUpdate(UserMasterEntity.class);
        Root<UserMasterEntity> root = userEntityCriteriaUpdate.from(UserMasterEntity.class);
        // update properties
        userEntityCriteriaUpdate.set(root.get("lastLogin"), GeneralUtil.getCurrentTimeStamp());
        // set where clause
        userEntityCriteriaUpdate.where(cb.equal(root.get("userId"),userId));
        // update
        int affectedRows = persistenceManager.entityManager().createQuery(userEntityCriteriaUpdate).executeUpdate();
        return affectedRows;
    }

    @Override
    public List<UserMasterEntity> getUserByEINOrName(String einFirstNameLastName) {
        if(isNotNull(einFirstNameLastName) && ! einFirstNameLastName.equals("")) {
            einFirstNameLastName = einFirstNameLastName.toUpperCase();
            return persistenceManager.query(UserMasterEntity.class, "FROM UserMasterEntity UA WHERE (UPPER(UA.firstName) LIKE '%"+ einFirstNameLastName +"%' " +
                                                                  "OR UPPER(UA.lastName) LIKE '%"+ einFirstNameLastName +"%' OR UA.userId LIKE '%"+ einFirstNameLastName +"%')");
        }
        return null;
    }

    @Override
    public void updateUserFromLDAP(UserMasterEntity userEntity) {
        persistenceManager.save(userEntity);
    }

    @Override
    public void addRoleToUser(UserRoleEntity userRole) {
        persistenceManager.save(userRole);
    }

    @Override
    public void deleteRoleFromUser(UserRoleEntity userRole) {
        persistenceManager.entityManager().remove(persistenceManager.entityManager().contains(userRole)
                                                      ? userRole : persistenceManager.entityManager().merge(userRole));
    }

    @Override
    public List<RoleMasterEntity> getAllRoles() {
        return persistenceManager.query(RoleMasterEntity.class, "from RoleMasterEntity r where r.ACTIVE = 'Y' ");
    }

    @Override
    public List<RoleMasterEntity> getAllRolesByRoleGroup(String roleGroupId) {
        return  persistenceManager.query(RoleMasterEntity.class, "from RoleMasterEntity r where r.isActive = 'Y' and r.roleGroup.roleGroupId =?0 ", roleGroupId);
    }

    @Override
    public List<RoleMasterEntity> getAllRoleForUserId(String userId){
        return persistenceManager.query(RoleMasterEntity.class,"from RoleMasterEntity r where r.roleId in(select ur.id.roleId from UserRoleEntity ur where ur.id.userId =?0)",userId) ;
    }



}
