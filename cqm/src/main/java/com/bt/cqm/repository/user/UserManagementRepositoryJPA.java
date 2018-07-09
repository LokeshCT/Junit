package com.bt.cqm.repository.user;


import com.bt.cqm.dto.UserCreateDTO;
import com.bt.cqm.dto.user.SalesChannelDTO;
import com.bt.cqm.dto.user.UserDTO;
import com.bt.cqm.dto.user.UserRoleMasterDTO;
import com.bt.cqm.exception.SalesChannelNotFoundException;
import com.bt.cqm.exception.UserConfigNotFoundException;
import com.bt.rsqe.persistence.JPAPersistenceManager;
import com.bt.rsqe.persistence.PersistenceManager;

import javax.persistence.Query;
import java.math.BigDecimal;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

public class UserManagementRepositoryJPA implements UserManagementRepository {
    private static final String ROLE_ID = "roleId";
    private static final String MODIFIED_BY = "modifiedBy";
    private static final String USER_ID = "userId";
    private final PersistenceManager persistenceManager;

    //private static final Logger LOG = LoggerFactory.getLogger(UserManagementRepositoryJPA.class);

    public UserManagementRepositoryJPA(PersistenceManager persistenceManager) {
        this.persistenceManager = persistenceManager;
    }

    @Override
    public CountryVatMapEntity getCountryVatPrefix(String countryName) {
        CountryVatMapEntity countryVatMapEntity = persistenceManager.get(CountryVatMapEntity.class, countryName);
        return countryVatMapEntity;
    }

    @Override
    public UserEntity findUserByUserId(String userId) {
        return persistenceManager.entityManager().find(UserEntity.class, userId);
    }

    @Override
    public List<UserRoleMasterEntity> getUserRoleMasterList() throws UserConfigNotFoundException {
        List<UserRoleMasterEntity> userRoleList = persistenceManager.query(UserRoleMasterEntity.class, "select distinct urm from UserRoleMasterEntity urm ");
        return userRoleList;
    }

    @Override
    public List<UserSalesChannelEntity> getSalesChannelsAssociatedWithUser(String userId) throws SalesChannelNotFoundException {
        List<UserSalesChannelEntity> salesChannelList = persistenceManager.query(UserSalesChannelEntity.class, "select distinct usc from UserSalesChannelEntity usc where usc.userId = ?0", userId);
        return salesChannelList;
    }


    @Override
    public List<UserRoleConfigEntity> getUserRoleConfig(String userId) {
        List<UserRoleConfigEntity> userRoleConfigs = persistenceManager.query(UserRoleConfigEntity.class, "from UserRoleConfigEntity u where id.user.userId=?0 order by defaultRole desc", userId);
        return userRoleConfigs;
    }

    @Override
    public void updateUserInfo(UserDTO userDto, UserRoleMasterDTO userRoleMasterDto) throws UserConfigNotFoundException {
        boolean isUserAuthUpdated = updateUserAuthorization(userDto);
        boolean isUserRoleConfUpdated = updateUserRoleConfig(userDto, userRoleMasterDto.getRoleId());
    }

    @Override
    public void updateUserInfo(UserDTO userDto) throws UserConfigNotFoundException {
        //To change body of implemented methods use File | Settings | File Templates.
        UserEntity userEntity = persistenceManager.get(UserEntity.class, userDto.getUserId());
        Date currentTime = new Date(new java.util.Date().getTime());
        userEntity.setModifiedDate(currentTime);
        userEntity.setModifiedUser(userDto.getModifiedUser());

        List<UserRoleConfigEntity> roleConfigEntities = userEntity.getUserRoleConfig();
        List<UserRoleConfigEntity> newRoleConfigEntitiesList = null;

        //Delete removed role configs
        for (int i = 0; i < roleConfigEntities.size(); i++) {
            boolean isEntityFound = false;
            for (UserRoleMasterDTO role : userDto.getRoles()) {
                if (roleConfigEntities.get(i).getId().getRole().getRoleId().equals(role.getRoleId())) {

                    if (role.isDefault() != roleConfigEntities.get(i).isDefaultRole()) {
                        roleConfigEntities.get(i).setModifiedUser(userDto.getModifiedUser());
                        currentTime = new Date(new java.util.Date().getTime());
                        roleConfigEntities.get(i).setModifiedDate(currentTime);
                        roleConfigEntities.get(i).setDefaultRole(role.isDefault());
                    }
                    isEntityFound = true;
                    break;
                }
            }

            if (!isEntityFound) {
                persistenceManager.remove(roleConfigEntities.get(i));
                roleConfigEntities.remove(i);

            }
        }

        //Add new role config
        for (UserRoleMasterDTO role : userDto.getRoles()) {
            boolean isConfigEntityFound = false;
            for (UserRoleConfigEntity roleConfigEntity : roleConfigEntities) {

                if (role.getRoleId().equals(roleConfigEntity.getRole().getRoleId())) {
                    isConfigEntityFound = true;
                    break;
                }
            }
            if (!isConfigEntityFound) {
                if (newRoleConfigEntitiesList == null) {
                    newRoleConfigEntitiesList = new ArrayList<UserRoleConfigEntity>();
                }
                UserRoleConfigEntity newRoleConfigEntity = new UserRoleConfigEntity();
                UserRoleConfigID id = new UserRoleConfigID();
                id.setUser(userEntity);
                id.setRole(new UserRoleMasterEntity(role.getRoleId()));

                newRoleConfigEntity.setId(id);
                newRoleConfigEntity.setDefaultRole(role.isDefault());
                newRoleConfigEntity.setCreatedUser(userDto.getModifiedUser());
                currentTime = new Date(new java.util.Date().getTime());
                newRoleConfigEntity.setCreatedDate(currentTime);

                newRoleConfigEntitiesList.add(newRoleConfigEntity);
            }

        }

        if (newRoleConfigEntitiesList != null) {
            roleConfigEntities.addAll(newRoleConfigEntitiesList);
        }


        List<SalesChannelDTO> salesChannelDTOs = userDto.getUserSalesChannelList();

        if (salesChannelDTOs != null && salesChannelDTOs.size() > 0) {
            List<UserSalesChannelEntity> salesChannelEntityList = getAssociatedSalesChannel(userDto.getUserId());
            List<UserSalesChannelEntity> newSalesChannelEntityList = null;
            //Delete the excluded sales channel
            for (int i = 0; i < salesChannelEntityList.size(); i++) {
                boolean isEntityFound = false;

                for (SalesChannelDTO salesChannelDTO : salesChannelDTOs) {

                    if (salesChannelDTO.getName().equals(salesChannelEntityList.get(i).getSalesChannel())) {
                        isEntityFound = true;
                        salesChannelEntityList.get(i).setModifiedUser(userDto.getModifiedUser());
                        salesChannelEntityList.get(i).setModifiedDate(currentTime);
                        salesChannelEntityList.get(i).setDefaultSalesChannel(salesChannelDTO.isDefault());
                        break;
                    }
                }

                if (!isEntityFound) {
                    persistenceManager.remove(salesChannelEntityList.get(i));
                    salesChannelEntityList.remove(i);
                }

            }

            // Add new salesChannel


            for (SalesChannelDTO salesChannelDTO : salesChannelDTOs) {
                boolean isNew = true;

                for (UserSalesChannelEntity salesChannelEntity : salesChannelEntityList) {
                    if (salesChannelDTO.getName().equals(salesChannelEntity.getSalesChannel())) {
                        isNew = false;
                        break;
                    }
                }

                if (isNew) {
                    if (newSalesChannelEntityList == null) {
                        newSalesChannelEntityList = new ArrayList<UserSalesChannelEntity>();
                    }

                    UserSalesChannelEntity newSalesChannelEntity = new UserSalesChannelEntity();
                    /*UserSalesChannelID id = new UserSalesChannelID(userDto.getUserId(), salesChannelDTO.getName());*/
                    newSalesChannelEntity.setUserId(userDto.getUserId());
                    newSalesChannelEntity.setSalesChannel(salesChannelDTO.getName());
                    newSalesChannelEntity.setCreatedUser(userDto.getModifiedUser());
                    newSalesChannelEntity.setCreatedDate(currentTime);
                    newSalesChannelEntity.setDefaultSalesChannel(salesChannelDTO.isDefault());

                    newSalesChannelEntityList.add(newSalesChannelEntity);
                }

            }

            if (newSalesChannelEntityList != null) {
                salesChannelEntityList.addAll(newSalesChannelEntityList);
            }

            for (UserSalesChannelEntity salesChannelEntity : salesChannelEntityList) {
                persistenceManager.save(salesChannelEntity);
            }
        }
        if (null != userDto.getUserRoleTypeId()) {
            try {
                RoleTypeEntity roleTypeEntity = persistenceManager.get(RoleTypeEntity.class, Long.parseLong(userDto.getUserRoleTypeId()));
                userEntity.setUserType(roleTypeEntity);
            } catch (NumberFormatException e) {
            }
        }
        persistenceManager.save(userEntity);
        ((JPAPersistenceManager) persistenceManager).flush();

    }

    private boolean updateUserRoleConfig(UserDTO userDto, Long roleId) {
        String updateSQL = "UPDATE USER_ROLE_CONFIG " +
                           " SET ROLE_ID =:roleId, MODIFIED_USER = :modifiedBy, MODIFIED_DATE = SYSDATE " +
                           " WHERE USER_ID = :userId and DEFAULT_ROLE='T'";

        Query query = persistenceManager.entityManager().createNativeQuery(updateSQL);
        query.setParameter(ROLE_ID, roleId);
        query.setParameter(MODIFIED_BY, userDto.getModifiedUser());
        query.setParameter(USER_ID, userDto.getUserId());

        if (query.executeUpdate() > 0) {
            return true;
        } else {
            return false;
        }
    }


    private boolean updateUserAuthorization(UserDTO userDto) {
        String updateSQL = "UPDATE USER_AUTHORIZATION " +
                           " SET ROLE_TYPE_ID =:roleTypeId, MODIFIED_USER = :modifiedBy, MODIFIED_DATE = SYSDATE " +
                           " WHERE USER_ID = :userId";

        Query query = persistenceManager.entityManager().createNativeQuery(updateSQL);
        //query.setParameter("roleTypeId", userDto.getUserType().getRoleTypeId());
        query.setParameter(MODIFIED_BY, userDto.getModifiedUser());
        query.setParameter(USER_ID, userDto.getUserId());

        if (query.executeUpdate() > 0) {
            return true;
        } else {
            return false;
        }
    }


    @Override
    public void updateUserSalesChannels(String userId, String createdBy, List<String> userSalesChannelList) throws SalesChannelNotFoundException {
        int deletedSalesChannelCount = deleteSalesChannels(userId);

        if (userSalesChannelList != null) {
            int insertedSalesChannelCount = insertSalesChannels(userId, createdBy, userSalesChannelList);
        }
    }

    private int deleteSalesChannels(String userId) {
        String deleteSQL = "DELETE FROM USER_SALES_CHANNEL WHERE  USER_ID =:userId";

        Query query = persistenceManager.entityManager().createNativeQuery(deleteSQL);
        query.setParameter(USER_ID, userId);

        return query.executeUpdate();
    }

    public int insertSalesChannels(String userId, String createdBy, List<String> userSalesChannelList) {
        String insertSQL = "INSERT INTO USER_SALES_CHANNEL (USER_ID,SALES_CHANNEL,CREATED_USER,CREATED_DATE,DEFAULT_SALES_CHANNEL) " +
                           "VALUES (:userId,:salesChannelName,:createdBy,SYSDATE,:defaultSalesChannel)";

        int i = 0;
        String defaultSalesChannel = "N";
        for (String salesChannelName : userSalesChannelList) {
            try {
                if (!persistenceManager.entityManager().getTransaction().isActive()) {
                    persistenceManager.entityManager().getTransaction().begin();
                }

                //TODO
                //Time Being the 1st Sales Channel is the Default one
                if (i == 0) {
                    defaultSalesChannel = "Y";
                } else {
                    defaultSalesChannel = "N";
                }

                Query query = persistenceManager.entityManager().createNativeQuery(insertSQL);
                query.setParameter(USER_ID, userId);
                query.setParameter("salesChannelName", salesChannelName);
                query.setParameter("createdBy", createdBy);
//          query.setParameter("createdDate", sqlDate);
                query.setParameter("defaultSalesChannel", defaultSalesChannel);

                query.executeUpdate();
                persistenceManager.entityManager().flush();
                i++;
            } catch (javax.persistence.PersistenceException ex) {
                persistenceManager.entityManager().getTransaction().rollback();
            }
        }

        return i;
    }

    @Override
    public List<UserSalesChannelEntity> findUserConfigById(String userId) throws UserConfigNotFoundException {
        List<UserSalesChannelEntity> salesChannelConfigEntitiesList = persistenceManager.query(UserSalesChannelEntity.class, "select distinct usc from UserSalesChannelEntity usc where usc.userId = ?0", userId);

        if (salesChannelConfigEntitiesList.size() == 0) {
            throw new UserConfigNotFoundException("User config not found for userId: " + userId);
        }
        return salesChannelConfigEntitiesList;
    }

    @Override
    public List<UserSalesChannelEntity> getAssociatedSalesChannel(String userId) {
        List<UserSalesChannelEntity> salesChannelList = persistenceManager.query(UserSalesChannelEntity.class, "select distinct ue from UserSalesChannelEntity ue where ue.userId = ?0 order by salesChannel", userId);
        return salesChannelList;
    }

    @Override
    public List<UserRoleMasterEntity> getRoles(Integer roleTypeId) {
        RoleTypeEntity roleTypeEntity = persistenceManager.entityManager().find(RoleTypeEntity.class, roleTypeId.longValue());
        return roleTypeEntity.getRoles();  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<SalesChannelEntity> getSalesChannels(Integer roleTypeId) {
        List<SalesChannelEntity> salesChannelEntities = persistenceManager.query(SalesChannelEntity.class, "select distinct sce from SalesChannelEntity sce");
        return salesChannelEntities;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getSalesChannelGfrCode(String salesChannelName) {
        BigDecimal gfrCode = null;
        try {
            String selectSQL = "SELECT unique GFR_CODE from SALES_CHANNEL where  SALES_CHANNEL_NAME like '" + salesChannelName + "\'";
            gfrCode = (BigDecimal) persistenceManager.entityManager().createNativeQuery(selectSQL).getSingleResult();
        } catch (Exception e) {
        }
        if (null != gfrCode) {
            return gfrCode.toString();
        } else {
            return null;
        }
    }

    @Override
    public String getSalesChannelFromGFRCode(String gfrCode) {
        String salesChannelName = null;
        try {
            String selectSQL = "SELECT unique SALES_CHANNEL_NAME from SALES_CHANNEL where  GFR_CODE like '" + gfrCode + "\'";
            salesChannelName = (String) persistenceManager.entityManager().createNativeQuery(selectSQL).getSingleResult();
        } catch (Exception e) {
        }
        return salesChannelName;
    }

    @Override
    public List<RagConfigurationEntity> getColorCodeDetails() {
        List<RagConfigurationEntity> ragConfigurationEntities = persistenceManager.query(RagConfigurationEntity.class, "select distinct rce from RagConfigurationEntity rce ");
        return ragConfigurationEntities;
    }

    @Override
    public void createUser(UserCreateDTO user) throws Exception {
        Query query = null;
        if (!persistenceManager.entityManager().getTransaction().isActive()) {
            persistenceManager.entityManager().getTransaction().begin();
        }

        String selectSQL = "select count(*) from USER_AUTHORIZATION where USER_ID='" + user.userId + "'";

        Query selectQuery = persistenceManager.entityManager().createNativeQuery(selectSQL);

        List resultList = selectQuery.getResultList();

        if (resultList == null || resultList.size() == 0 || resultList.get(0).toString().equals("0")) {

            String insertSql = "INSERT INTO USER_AUTHORIZATION (USER_ID,USER_NAME,ROLE_TYPE_ID,ACTIVE,CREATED_DATE,CREATED_USER,MODIFIED_DATE,email_id) VALUES" +
                               "(:userId,:userName,:roleTypeId,:isActive,sysdate,:createdBy,sysdate,:emailId)";
            query = persistenceManager.entityManager().createNativeQuery(insertSql);
            query.setParameter("createdBy", user.createdBy);

        } else {

            String updateSql = "update USER_AUTHORIZATION set USER_NAME=:userName, ROLE_TYPE_ID=:roleTypeId, ACTIVE=:isActive, MODIFIED_DATE=sysdate, MODIFIED_USER=:modifiedBy,  email_id=:emailId where USER_ID=:userId";
            query = persistenceManager.entityManager().createNativeQuery(updateSql);
            query.setParameter("modifiedBy", user.modifiedBy);

        }


        query.setParameter("userId", user.userId);
        query.setParameter("userName", user.userName);
        if (user.directOrIndirectUser.trim().equalsIgnoreCase("DIRECT")) {
            query.setParameter("roleTypeId", 1L);
        }

        if (user.directOrIndirectUser.trim().equalsIgnoreCase("INDIRECT")) {
            query.setParameter("roleTypeId", 2L);
        }

        if (user.isActiveUser) {
            query.setParameter("isActive", "Y");
        } else {
            query.setParameter("isActive", "N");
        }

        query.setParameter("emailId", user.emailId);
        query.executeUpdate();


        persistenceManager.flush();
    }

    @Override
    public void createUserRoleConfig(String userId, List<Long> roleIds) throws Exception {

        for (Long roleId : roleIds) {
            if (!persistenceManager.entityManager().getTransaction().isActive()) {
                persistenceManager.entityManager().getTransaction().begin();
            }
            String insertSQL = "INSERT INTO USER_ROLE_CONFIG (USER_ID,ROLE_ID,CREATED_DATE,CREATED_USER,MODIFIED_DATE,MODIFIED_USER,DEFAULT_ROLE)" +
                               "VALUES('" + userId + "','" + roleId.longValue() + "'," + "sysdate,'EXPEDIO',sysdate,'EXPEDIO','T')";

            Query query = persistenceManager.entityManager().createNativeQuery(insertSQL);
            try {
                query.executeUpdate();
                persistenceManager.flush();
            } catch (javax.persistence.PersistenceException ex) {
                persistenceManager.entityManager().getTransaction().rollback();
            }
        }

    }


}
