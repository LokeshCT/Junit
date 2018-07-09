package com.bt.cqm.repository.user;

import com.bt.cqm.dto.user.RoleTypeDTO;
import com.bt.cqm.dto.user.UserDTO;
import com.bt.cqm.dto.user.UserRoleMasterDTO;
import com.bt.rsqe.persistence.JPAPersistenceManager;
import com.bt.rsqe.persistence.PersistenceManager;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;


/**
 * Created with IntelliJ IDEA.
 * User: 607982105
 * Date: 01/04/14
 * Time: 11:49
 * To change this template use File | Settings | File Templates.
 */

public class UserManagementRepositoryJPATest {

    UserManagementRepositoryJPA userManagementRepository;
    @Mock
    private PersistenceManager persistenceManagerMock;
    @Mock
    EntityManager entityManager;
    @Mock
    private Query query;

    private JPAPersistenceManager  persistenceManager;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        userManagementRepository = new UserManagementRepositoryJPA(persistenceManagerMock);

    }


    @Test
    public void shouldFindUserByUserId() throws Exception {
        String userId = "1234";
        Date dt = null;
        UserEntity userEntity = new UserEntity("1234", "KPN",/* null,*/ "Y", dt, "KPN", dt, "KPN");
        when(persistenceManagerMock.entityManager()).thenReturn(entityManager);
        when(entityManager.find(any(Class.class), anyObject())).thenReturn(userEntity);
        UserEntity responseUserEntity = userManagementRepository.findUserByUserId(userId);
        //Check
        assert ((userEntity).equals(responseUserEntity));
    }

    @Test
    public void shouldGetUserRoleMasterList() throws Exception {
        List<UserRoleMasterEntity> userRoleList = new ArrayList<UserRoleMasterEntity>(1);
        UserRoleMasterEntity usrRoleEntity = new UserRoleMasterEntity(123L, "ADMIN", null, null, null, null);
        userRoleList.add(usrRoleEntity);
        when(persistenceManagerMock.query(UserRoleMasterEntity.class,
                                      "select distinct urm from UserRoleMasterEntity urm ")).thenReturn(userRoleList);
        List<UserRoleMasterEntity> responseUserRoleList = userManagementRepository.getUserRoleMasterList();
        assert ((userRoleList).equals(responseUserRoleList));
    }

    @Test
    public void shouldGetSalesChannelsAssociatedWithUser() throws Exception {
        String userId = "1234";
        String salesChannel = "BTINDIA";
        List<UserSalesChannelEntity> salesChannelsAssociatedWithTheUser = new ArrayList<UserSalesChannelEntity>(1);
        UserSalesChannelEntity userSalesChannelConfigEntity = new UserSalesChannelEntity();
        userSalesChannelConfigEntity.setUserId(userId);
        userSalesChannelConfigEntity.setSalesChannel(salesChannel);
        salesChannelsAssociatedWithTheUser.add(userSalesChannelConfigEntity);
        when(persistenceManagerMock.query(any(Class.class),
                                      anyString(), anyVararg())).thenReturn(salesChannelsAssociatedWithTheUser);
        List<UserSalesChannelEntity> responseSalesChannelsAssociatedWithTheUser = userManagementRepository.getSalesChannelsAssociatedWithUser(userId);
        assert ((salesChannelsAssociatedWithTheUser).equals(responseSalesChannelsAssociatedWithTheUser));
    }

    @Test
    public void shouldGetAvailableSalesChannels() throws Exception {
        String salesChannel = "BTINDIA";
        String salesChannelId = "12";
        List<SalesChannelEntity> availableSalesChannelList = new ArrayList<SalesChannelEntity>(1);
        SalesChannelEntity salesChannelEntity = new SalesChannelEntity();
        salesChannelEntity.setId(salesChannelId);
        salesChannelEntity.setSalesChannelName(salesChannel);
        availableSalesChannelList.add(salesChannelEntity);

        RoleTypeEntity roleTypeEntity = new RoleTypeEntity();
        roleTypeEntity.setSalesChannels(availableSalesChannelList);

       // when(persistenceManagerMock.entityManager()).thenReturn(entityManager);
        when(persistenceManagerMock.query(any(Class.class),
                                      anyString())).thenReturn(availableSalesChannelList) ;
        List<SalesChannelEntity> responseAvailableSalesChannelList = userManagementRepository.getSalesChannels(1);
        assert ((availableSalesChannelList).equals(responseAvailableSalesChannelList));

    }

    @Test
    public void shouldUpdateUserInfo() throws Exception {
        Date dt = null;

        UserDTO userDTO = new UserDTO();
        userDTO.setUserId("1234");
        userDTO.setUserName("KPN");
        userDTO.setActive("Y");
        userDTO.setUserType(new RoleTypeDTO(1L, ""));
        userDTO.setCreatedDate(dt);
        userDTO.setCreateUser("KPN");
        userDTO.setUserRoleTypeId("1");
        UserRoleMasterDTO userRoleMasterDTO = new UserRoleMasterDTO();
        userRoleMasterDTO.setRoleId(1L);
        userRoleMasterDTO.setRoleName("ADMIN");
        when(persistenceManagerMock.entityManager()).thenReturn(entityManager);
        when(entityManager.createNativeQuery(anyString())).thenReturn(query);
        when(query.executeUpdate()).thenReturn(2);
        userManagementRepository.updateUserInfo(userDTO, userRoleMasterDTO);

    }

    @Test
    public void shouldUpdateUserSalesChannels() throws Exception {

        String userId="1234";
        String createdBy="TEST";
        List<String> userSalesChannelList=new ArrayList<String>(2);
        when(persistenceManagerMock.entityManager()).thenReturn(entityManager);
        when(entityManager.createNativeQuery(anyString())).thenReturn(query);
        when(query.executeUpdate()).thenReturn(2);
        userSalesChannelList.add("BTINDIA");
        userSalesChannelList.add("BTAMERICAS");
        userManagementRepository.updateUserSalesChannels(userId, createdBy, userSalesChannelList);
    }

    @Test
    public void shouldUpdateUserInfoForZeroUpdate() throws Exception {
        Date dt = null;

        UserDTO userDTO = new UserDTO();
        userDTO.setUserId("1234");
        userDTO.setUserName("KPN");
        userDTO.setActive("Y");
        userDTO.setUserType(new RoleTypeDTO(1L, ""));
        userDTO.setCreatedDate(dt);
        userDTO.setCreateUser("KPN");

        UserRoleMasterDTO userRoleMasterDTO = new UserRoleMasterDTO();
        userRoleMasterDTO.setRoleId(1L);
        userRoleMasterDTO.setRoleName("ADMIN");

        when(persistenceManagerMock.entityManager()).thenReturn(entityManager);
        when(entityManager.createNativeQuery(anyString())).thenReturn(query);
        when(query.executeUpdate()).thenReturn(0);
        userManagementRepository.updateUserInfo(userDTO, userRoleMasterDTO);

    }

    @Test
    public void shouldGetUserRoleConfig(){
        List<UserRoleConfigEntity> userRoleConfigsList = new ArrayList<UserRoleConfigEntity>();
        UserRoleConfigEntity userRoleConfigObj = new UserRoleConfigEntity();
        userRoleConfigsList.add(userRoleConfigObj);

        when(persistenceManagerMock.query(any(Class.class),anyString(),anyVararg())).thenReturn(userRoleConfigsList);

        List<UserRoleConfigEntity> roleConfigs=userManagementRepository.getUserRoleConfig("608026723");
        assert(userRoleConfigsList==roleConfigs);
    }

    @Test
    public void shouldGetSalesChannelFromGFRCode(){

        when(persistenceManagerMock.entityManager()).thenReturn(entityManager);
        when(entityManager.createNativeQuery(anyString())).thenReturn(query);
        when(query.getSingleResult()).thenReturn("BT AMERICAS");

        String salesChannelRet =userManagementRepository.getSalesChannelFromGFRCode("XXX");

        assert ("BT AMERICAS".equals(salesChannelRet));
    }

    @Test
    public void shouldGetColorCodeDetails() throws Exception {
        List<RagConfigurationEntity> ragConfigurationEntityList = new ArrayList<RagConfigurationEntity>(1);
        RagConfigurationEntity ragConfigurationEntity = new RagConfigurationEntity("Street","a","Amber","1");
        ragConfigurationEntityList.add(ragConfigurationEntity);
        when(persistenceManagerMock.query(RagConfigurationEntity.class,
                                          "select distinct rce from RagConfigurationEntity rce ")).thenReturn(ragConfigurationEntityList);
        List<RagConfigurationEntity> responseRagConfigurationEntityList = userManagementRepository.getColorCodeDetails();
        assert ((ragConfigurationEntityList).equals(responseRagConfigurationEntityList));
    }
}
