package com.bt.cqm.handler;

import com.bt.cqm.dto.TreeNode;
import com.bt.cqm.ldap.LdapRepository;
import com.bt.cqm.ldap.model.LdapSearchModel;
import com.bt.cqm.model.SalesUserDTO;
import com.bt.cqm.repository.user.RoleTypeEntity;
import com.bt.cqm.repository.user.UserEntity;
import com.bt.cqm.repository.user.UserManagementRepository;
import com.bt.cqm.repository.user.UserRoleConfigEntity;
import com.bt.cqm.repository.user.UserRoleConfigID;
import com.bt.cqm.repository.user.UserRoleMasterEntity;
import com.bt.cqm.repository.user.UserSalesChannelEntity;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.hamcrest.core.Is;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static org.apache.commons.lang.StringUtils.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class CQMBasePageResourceHandlerTest {

    private TabBuilder tabBuilder = mock(TabBuilder.class);
    private CQMBasePageResourceHandler cqmBasePageResourceHandler;
    private UserManagementRepository userManagementRepositoryMock;
    private LdapRepository ldapRepository;

    @Before
    public void setup() {
        userManagementRepositoryMock = Mockito.mock(UserManagementRepository.class);
        ldapRepository = Mockito.mock(LdapRepository.class);
        cqmBasePageResourceHandler = new CQMBasePageResourceHandler(userManagementRepositoryMock, tabBuilder, ldapRepository,null);
        List<Tab> tabs = new ArrayList<Tab>();
        Set<Long> role1 = new HashSet<Long>();
        role1.add(1L);
        role1.add(2L);
        Tab tab1 = new Tab("customerTab", "label","", new TreeNode("treeid", "treelabel").addChildNodes(
            new TreeNode("childNodeId", "child node", "uri", null, "")
        ), role1, "direct");


        Tab tab2 = new Tab("activityTab", "label","", new TreeNode("u", "treelabel").addChildNodes(
            new TreeNode("childNodeId", "child node", "uri", null, "")
        ), null, "indirect");


        Set<Long> role2 = new HashSet<Long>();
        role2.add(5L);
        Tab tab3 = new Tab("UserManagement", "label","", new TreeNode("u", "treelabel").addChildNodes(
            new TreeNode("childNodeId", "child node", "uri", null, "")
        ), role2, "");

        tabs.add(tab1);
        tabs.add(tab2);
        tabs.add(tab3);

        when(tabBuilder.build()).thenReturn(tabs);
    }

    @Test
    public void shouldGetSalesUser() throws Exception {
        String userId = "1001";
        String salesChannel = "BT AMERICAS";
        UserEntity userEntityMock = new UserEntity();
        userEntityMock.setUserId(userId);
        userEntityMock.setActive("Y");

        UserSalesChannelEntity salesChannelEntityMock = new UserSalesChannelEntity();
        salesChannelEntityMock.setSalesChannel(salesChannel);
        salesChannelEntityMock.setDefaultSalesChannel(true);

        List<UserSalesChannelEntity> salesChannelEntityList = new ArrayList<UserSalesChannelEntity>();
        salesChannelEntityList.add(salesChannelEntityMock);

        UserRoleConfigEntity userRoleConfigEntityMock = new UserRoleConfigEntity();
        UserRoleConfigID userRoleId = new UserRoleConfigID();
        UserRoleMasterEntity roleMaster = new UserRoleMasterEntity();
        roleMaster.setRoleId(1L);
        roleMaster.setRoleName("Sales User");
        userRoleId.setRole(roleMaster);
        userRoleConfigEntityMock.setId(userRoleId);
        List<UserRoleConfigEntity> userRoleConfigEntityList = new ArrayList<UserRoleConfigEntity>();
        userRoleConfigEntityList.add(userRoleConfigEntityMock);

        LdapSearchModel ldapSearchModel = new LdapSearchModel();
        ldapSearchModel.setEin(userId);
        ldapSearchModel.setFirstName("First Name");
        ldapSearchModel.setLastName("Last Name");
        ldapSearchModel.setMailId("fn.sn@bt.com");

        List<LdapSearchModel> ldapSearchModelListMockResult = new ArrayList<LdapSearchModel>();
        ldapSearchModelListMockResult.add(ldapSearchModel);

        when(userManagementRepositoryMock.findUserByUserId(anyString())).thenReturn(userEntityMock);
        when(userManagementRepositoryMock.getAssociatedSalesChannel(anyString())).thenReturn(salesChannelEntityList);
        when(userManagementRepositoryMock.getUserRoleConfig(anyString())).thenReturn(userRoleConfigEntityList);
        when(ldapRepository.search(anyMap())).thenReturn(ldapSearchModelListMockResult);

        Response resp = cqmBasePageResourceHandler.getSalesUser(userId, null);
        assertEquals(Response.Status.OK.getStatusCode(), resp.getStatus());
    }

    @Test
    public void shouldGetSalesUserIdentifyGSPLogin() throws Exception {
        String userId = "1001";
        String salesChannel = "BT AMERICAS";
        UserEntity userEntityMock = new UserEntity();
        userEntityMock.setUserId(userId);
        userEntityMock.setActive("Y");

        HttpHeaders httpHeaders = new HttpHeaders() {
            @Override
            public List<String> getRequestHeader(String name) {
                if ("Host".equals(name)) {
                    List<String> hosts = new ArrayList<String>();
                    hosts.add(null);
                    hosts.add("abc.globalservices.com");

                    return hosts;
                } else {
                    return null;
                }
            }

            @Override
            public String getHeaderString(String name) {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public MultivaluedMap<String, String> getRequestHeaders() {
                return null;
            }

            @Override
            public List<MediaType> getAcceptableMediaTypes() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public List<Locale> getAcceptableLanguages() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public MediaType getMediaType() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public Locale getLanguage() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public Map<String, Cookie> getCookies() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public Date getDate() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public int getLength() {
                return 0;  //To change body of implemented methods use File | Settings | File Templates.
            }
        };

        UserSalesChannelEntity salesChannelEntityMock = new UserSalesChannelEntity();
        salesChannelEntityMock.setSalesChannel(salesChannel);
        salesChannelEntityMock.setDefaultSalesChannel(true);

        List<UserSalesChannelEntity> salesChannelEntityList = new ArrayList<UserSalesChannelEntity>();
        salesChannelEntityList.add(salesChannelEntityMock);

        UserRoleConfigEntity userRoleConfigEntityMock = new UserRoleConfigEntity();
        UserRoleConfigID userRoleId = new UserRoleConfigID();
        UserRoleMasterEntity roleMaster = new UserRoleMasterEntity();
        roleMaster.setRoleId(1L);
        roleMaster.setRoleName("Sales User");
        userRoleId.setRole(roleMaster);
        userRoleConfigEntityMock.setId(userRoleId);
        List<UserRoleConfigEntity> userRoleConfigEntityList = new ArrayList<UserRoleConfigEntity>();
        userRoleConfigEntityList.add(userRoleConfigEntityMock);

        LdapSearchModel ldapSearchModel = new LdapSearchModel();
        ldapSearchModel.setEin(userId);
        ldapSearchModel.setFirstName("First Name");
        ldapSearchModel.setLastName("Last Name");
        ldapSearchModel.setMailId("fn.sn@bt.com");

        List<LdapSearchModel> ldapSearchModelListMockResult = new ArrayList<LdapSearchModel>();
        ldapSearchModelListMockResult.add(ldapSearchModel);

        when(userManagementRepositoryMock.findUserByUserId(anyString())).thenReturn(userEntityMock);
        when(userManagementRepositoryMock.getAssociatedSalesChannel(anyString())).thenReturn(salesChannelEntityList);
        when(userManagementRepositoryMock.getUserRoleConfig(anyString())).thenReturn(userRoleConfigEntityList);
        when(ldapRepository.search(anyMap())).thenReturn(ldapSearchModelListMockResult);

        Response resp = cqmBasePageResourceHandler.getSalesUser(userId, httpHeaders);
        SalesUserDTO ret = (SalesUserDTO) resp.getEntity();

        assert ("Indirect".equalsIgnoreCase(ret.getUserType()));
    }

    @Test
    public void shouldGetSalesUserReturnNotFoundExcep() throws Exception {
        String userId = "1001";
        String salesChannel = "BT AMERICAS";
        UserEntity nullUserEntityMock = null;

        UserSalesChannelEntity salesChannelEntityMock = new UserSalesChannelEntity();
        salesChannelEntityMock.setSalesChannel(salesChannel);
        salesChannelEntityMock.setDefaultSalesChannel(true);

        List<UserSalesChannelEntity> salesChannelEntityList = new ArrayList<UserSalesChannelEntity>();
        salesChannelEntityList.add(salesChannelEntityMock);

        UserRoleConfigEntity userRoleConfigEntityMock = new UserRoleConfigEntity();
        UserRoleConfigID userRoleId = new UserRoleConfigID();
        UserRoleMasterEntity roleMaster = new UserRoleMasterEntity();
        roleMaster.setRoleId(1L);
        roleMaster.setRoleName("Sales User");
        userRoleId.setRole(roleMaster);
        userRoleConfigEntityMock.setId(userRoleId);
        List<UserRoleConfigEntity> userRoleConfigEntityList = new ArrayList<UserRoleConfigEntity>();
        userRoleConfigEntityList.add(userRoleConfigEntityMock);

        when(userManagementRepositoryMock.findUserByUserId(anyString())).thenReturn(nullUserEntityMock);
        when(userManagementRepositoryMock.getAssociatedSalesChannel(anyString())).thenReturn(salesChannelEntityList);
        when(userManagementRepositoryMock.getUserRoleConfig(anyString())).thenReturn(userRoleConfigEntityList);

        Response resp = cqmBasePageResourceHandler.getSalesUser(userId, null);
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), resp.getStatus());
    }

    @Test
    public void shouldGetSalesUserHandleInvalidInput() throws Exception {
        String userId = "";

        Response resp = cqmBasePageResourceHandler.getSalesUser(userId, null);
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), resp.getStatus());
    }

    @Test
    public void shouldBuildTabsForDirectUser() throws JSONException {

        String userId = "1001";
        String roleName = "Sales User";
        String userRoleType = "Direct";

        RoleTypeEntity roleType = new RoleTypeEntity();
        roleType.setRoleTypeId(1L);
        roleType.setRoleTypeName(userRoleType);
        UserEntity userEntityMock = new UserEntity();
        userEntityMock.setUserId(userId);
        userEntityMock.setActive("Y");
        userEntityMock.setUserType(roleType);


        UserRoleConfigEntity userRoleConfigEntityMock = new UserRoleConfigEntity();
        UserRoleConfigID userRoleId = new UserRoleConfigID();
        UserRoleMasterEntity roleMaster = new UserRoleMasterEntity();
        roleMaster.setRoleId(1L);
        roleMaster.setRoleName(roleName);
        userRoleId.setRole(roleMaster);
        userRoleConfigEntityMock.setId(userRoleId);
        List<UserRoleConfigEntity> userRoleConfigEntityList = new ArrayList<UserRoleConfigEntity>();
        userRoleConfigEntityList.add(userRoleConfigEntityMock);


        when(userManagementRepositoryMock.findUserByUserId(anyString())).thenReturn(userEntityMock);
        when(userManagementRepositoryMock.getUserRoleConfig(anyString())).thenReturn(userRoleConfigEntityList);

        Response response = cqmBasePageResourceHandler.getTabs("1001", null,null);
        JSONObject jsonObject = new JSONObject(response.getEntity().toString());

        assertTrue(jsonObject.has("tabs"));
        assertThat(jsonObject.getJSONArray("tabs").length(), Is.is(1));
        assertTrue(((JSONObject) jsonObject.getJSONArray("tabs").get(0)).get("id").equals("customerTab"));
        assertThat(response.getStatus(), Is.is(Response.Status.OK.getStatusCode()));
    }


    @Test
    public void shouldBuildTabsForInDirectUser() throws JSONException {

        String userId = "1001";
        String roleName = "Super User";
        String userRoleType = "direct";
        Long roleTypeId = 2L;

        RoleTypeEntity roleType = new RoleTypeEntity();
        roleType.setRoleTypeId(roleTypeId);
        roleType.setRoleTypeName(userRoleType);
        UserEntity userEntityMock = new UserEntity();
        userEntityMock.setUserId(userId);
        userEntityMock.setActive("Y");
        userEntityMock.setUserType(roleType);


        UserRoleConfigEntity userRoleConfigEntityMock = new UserRoleConfigEntity();
        UserRoleConfigID userRoleId = new UserRoleConfigID();
        UserRoleMasterEntity roleMaster = new UserRoleMasterEntity();
        roleMaster.setRoleId(2L);
        roleMaster.setRoleName(roleName);
        userRoleId.setRole(roleMaster);
        userRoleConfigEntityMock.setId(userRoleId);
        List<UserRoleConfigEntity> userRoleConfigEntityList = new ArrayList<UserRoleConfigEntity>();
        userRoleConfigEntityList.add(userRoleConfigEntityMock);


        when(userManagementRepositoryMock.findUserByUserId(anyString())).thenReturn(userEntityMock);
        when(userManagementRepositoryMock.getUserRoleConfig(anyString())).thenReturn(userRoleConfigEntityList);

        Response response = cqmBasePageResourceHandler.getTabs("1001", null,null);
        JSONObject jsonObject = new JSONObject(response.getEntity().toString());

        assertTrue(jsonObject.has("tabs"));
        assertThat(jsonObject.getJSONArray("tabs").length(), Is.is(2));
        assertTrue(((JSONObject) jsonObject.getJSONArray("tabs").get(0)).get("id").equals("activityTab"));
        assertThat(response.getStatus(), Is.is(Response.Status.OK.getStatusCode()));
    }

    @Test
    public void shouldBuildTabsWithUserManagementForSuperUser() throws JSONException {

        String userId = "1001";
        String roleName = "Super User";

        String userRoleType = "direct";
        Long roleTypeId = 1L;

        RoleTypeEntity roleType = new RoleTypeEntity();
        roleType.setRoleTypeId(roleTypeId);
        roleType.setRoleTypeName(userRoleType);
        UserEntity userEntityMock = new UserEntity();
        userEntityMock.setUserId(userId);
        userEntityMock.setActive("Y");
        userEntityMock.setUserType(roleType);


        UserRoleConfigEntity userRoleConfigEntityMock = new UserRoleConfigEntity();
        UserRoleConfigID userRoleId = new UserRoleConfigID();
        UserRoleMasterEntity roleMaster = new UserRoleMasterEntity();
        roleMaster.setRoleId(3L);
        roleMaster.setRoleName(roleName);
        userRoleId.setRole(roleMaster);
        userRoleConfigEntityMock.setId(userRoleId);
        List<UserRoleConfigEntity> userRoleConfigEntityList = new ArrayList<UserRoleConfigEntity>();
        userRoleConfigEntityList.add(userRoleConfigEntityMock);


        when(userManagementRepositoryMock.findUserByUserId(anyString())).thenReturn(userEntityMock);
        when(userManagementRepositoryMock.getUserRoleConfig(anyString())).thenReturn(userRoleConfigEntityList);

        Response response = cqmBasePageResourceHandler.getTabs("1001", null,null);
        JSONObject jsonObject = new JSONObject(response.getEntity().toString());

        assertTrue(jsonObject.has("tabs"));
        assertThat(jsonObject.getJSONArray("tabs").length(), Is.is(1));
        assertTrue(((JSONObject) jsonObject.getJSONArray("tabs").get(0)).get("id").equals("UserManagement"));
        assertThat(response.getStatus(), Is.is(Response.Status.OK.getStatusCode()));
    }


    @Test
    public void shouldBuildCqmBasePage() throws org.codehaus.jettison.json.JSONException {
        Response response = cqmBasePageResourceHandler.getCqmBasePage();
        assertThat(response.getStatus(), Is.is(Response.Status.OK.getStatusCode()));
        assertFalse(isEmpty(response.getEntity().toString()));
    }

    @Test
    public void shouldLogout() {
        Response resp = cqmBasePageResourceHandler.logout("1001");

        assertEquals(Response.Status.OK.getStatusCode(), resp.getStatus());
    }

    @Test
    public void shouldGetDashboardPage() throws JSONException {
        Response resp = cqmBasePageResourceHandler.getDashboardPage();
        assertEquals(Response.Status.OK.getStatusCode(), resp.getStatus());
    }
}
