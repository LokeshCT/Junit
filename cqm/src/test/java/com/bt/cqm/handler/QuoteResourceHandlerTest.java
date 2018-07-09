package com.bt.cqm.handler;

import com.bt.cqm.config.BundlingAppConfig;
import com.bt.cqm.config.SqeAppConfig;
import com.bt.cqm.repository.user.RoleTypeEntity;
import com.bt.cqm.repository.user.UserEntity;
import com.bt.cqm.repository.user.UserManagementRepository;
import com.bt.cqm.repository.user.UserRoleConfigEntity;
import com.bt.cqm.repository.user.UserRoleConfigID;
import com.bt.cqm.repository.user.UserRoleMasterEntity;
import com.bt.rsqe.expedio.services.quote.QuoteLaunchConfiguratorDTO;
import com.bt.rsqe.expedio.services.quote.QuoteResource;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import javax.ws.rs.core.Response;

import static com.google.common.collect.Lists.*;
import static org.hamcrest.core.Is.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class QuoteResourceHandlerTest {

    private static final String USER_ID = "userId";
    private static final String GUID = "1234-1234-1212-2121";

    private QuoteResource quoteResource = mock(QuoteResource.class);
    private SqeAppConfig sqeAppConfig = mock(SqeAppConfig.class);
    private BundlingAppConfig bundlingAppConfig = mock(BundlingAppConfig.class);
    private UserManagementRepository userManagementRepository = mock(UserManagementRepository.class);
    private QuoteResourceHandler quoteResourceHandler;
    private UserEntity userEntity;

    @Before
    public void setup() throws Exception {

        quoteResourceHandler = new QuoteResourceHandler(userManagementRepository, quoteResource, null, bundlingAppConfig, sqeAppConfig, null, null, null, null, null);
        userEntity = new UserEntity("userId", "KPN", "Y", null, "KPN", null, "KPN");
        userEntity.setUserType(new RoleTypeEntity());

        when(userManagementRepository.findUserByUserId(USER_ID)).thenReturn(userEntity);
        when(quoteResource.generateGUID(any(QuoteLaunchConfiguratorDTO.class))).thenReturn(GUID);
        when(sqeAppConfig.getUrl()).thenReturn("/PROD_SQE_iVPN/homePage/homePage.html?initQH=y&productName=BT IVPN2 GLOBAL&internal=y&guid=%s&quoteHeaderId=%s&#/quoteDetails");
        when(bundlingAppConfig.getUrl()).thenReturn("/sqe/bundle/bundleHome.html?guid=%s");
    }


    @Test
    public void shouldReturnBadRequestIfQuoteIdOrQuoteVersionIsNull() throws Exception {

        Response response = quoteResourceHandler.getBundlingAppURL(null, "1", USER_ID, null, null, null, null);
        assertThat(response.getStatus(), is(Response.Status.BAD_REQUEST.getStatusCode()));

        response = quoteResourceHandler.getBundlingAppURL("quoteId", null, USER_ID, null, null, null, null);
        assertThat(response.getStatus(), is(Response.Status.BAD_REQUEST.getStatusCode()));

        response = quoteResourceHandler.getBundlingAppURL("quoteId", "1", null, null, null, null, null);
        assertThat(response.getStatus(), is(Response.Status.BAD_REQUEST.getStatusCode()));
    }


    @Test
    public void shouldGetSqeAppURL() throws Exception {

        Response response = quoteResourceHandler.getSqeAppURL("quote1", "1", "1234", USER_ID, "sqe.user@bt.com", "Indirect", "", "Y");

        assertThat(response.getStatus(), is(Response.Status.OK.getStatusCode()));
        assertThat(response.getEntity().toString(),
                is("/PROD_SQE_iVPN/homePage/homePage.html?initQH=y&productName=BT IVPN2 GLOBAL&internal=y&guid=1234-1234-1212-2121&quoteHeaderId=1234&#/quoteDetails"));
    }

    @Test
    public void shouldCreateSessionWithValidParams() throws Exception {

        quoteResourceHandler.getSqeAppURL("quote1", "1", "1234", USER_ID, "sqe.user@bt.com", "Direct", "BOATID", "Y");

        assertThat(
                QuoteLaunchConfiguratorDTO.builder()
                        .withQuoteID("quote1")
                        .withQuoteVersion("1")
                        .withSalesRepName("KPN")
                        .withBoatID("boatid")
                        .withEIN(USER_ID)
                        .withCeaseOptimizationFlag("No")
                        .withUserEmailId("sqe.user@bt.com")
                        .withManagedCustomer("Y")
                        .withCeaseOptimizationFlag("No")
                        .build(),
                is(createdSessionParams()));
    }

    @Test
    public void shouldCreateSessionWithBoatIdInLowerCase() throws Exception {

        quoteResourceHandler.getBundlingAppURL("quote1", "1", USER_ID, "sqe.user@bt.com", "Direct", "BOATID", "N");
        assertThat(createdSessionParams().getBoatID(), is("boatid"));
    }


    @Test
    public void shouldPassUserIdAsBoardIdForIndirectUsers() throws Exception {

        quoteResourceHandler.getBundlingAppURL("quote1", "1", USER_ID, "sqe.user@bt.com", "Indirect", "", "N");
        assertThat(createdSessionParams().getBoatID(), is(USER_ID));
    }


    @Test
    public void shouldCreateSessionWithValidMncFlag() throws Exception {

        quoteResourceHandler.getBundlingAppURL("quote1", "1", USER_ID, null, null, "", "N");
        assertThat(createdSessionParams().getManagedCustomer(), is("N"));
    }


    @Test
    public void shouldCreateSessionIgnoringBlankEmailId() throws Exception {

        quoteResourceHandler.getBundlingAppURL("quote1", "1", USER_ID, " ", "Indirect", "", "Y");
        assertNull(createdSessionParams().getUserEmailId());
    }


    @Test
    public void shouldCreateSessionWithSalesUserRoleIfUserHavingCeaseRoleAssociated() throws Exception {

        setupCeaseOptimizationUser();
        quoteResourceHandler.getBundlingAppURL("quote1", "1", USER_ID, " ", "Indirect", "", "Y");

        assertThat(createdSessionParams().getUserRole(), is("Sales User"));
    }


    @Test
    public void shouldSetCeaseOptimizationFlag() throws Exception {
        setupCeaseOptimizationUser();
        quoteResourceHandler.getBundlingAppURL("quote1", "1", USER_ID, " ", "Indirect", "", "Y");

        assertThat(createdSessionParams().getCeaseOptimizationFlag(), is("Yes"));
    }


    @Test
    public void shouldGetBundlingAppURL() throws Exception {

        Response response = quoteResourceHandler.getBundlingAppURL("quote1", "1", USER_ID, "sqe.user@bt.com", "Direct", "BOATID", "N");

        assertThat(response.getStatus(), is(Response.Status.OK.getStatusCode()));
        assertThat(response.getEntity().toString(), is("/sqe/bundle/bundleHome.html?guid=1234-1234-1212-2121"));
    }


    @Test
    public void shouldReturnErrorResponseIfSessionCreationFails() throws Exception {
        when(quoteResource.generateGUID(any(QuoteLaunchConfiguratorDTO.class))).thenReturn(null);
        Response response = quoteResourceHandler.getBundlingAppURL("quote1", "1", USER_ID, "sqe.user@bt.com", "Direct", "BOATID", "N");

        assertThat(response.getStatus(), is(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()));
        assertThat(response.getEntity().toString(), is("GUID is null"));
    }


    @Test
    public void shouldThrowExceptionIfUserNotFound() {
        when(userManagementRepository.findUserByUserId(USER_ID)).thenReturn(null);
        Response response = quoteResourceHandler.getBundlingAppURL("quote1", "1", USER_ID, "sqe.user@bt.com", "Direct", "BOATID", "N");

        assertThat(response.getStatus(), is(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()));
        assertThat(response.getEntity().toString(), is("Invalid user : userId"));
    }


    private QuoteLaunchConfiguratorDTO createdSessionParams() throws Exception {
        ArgumentCaptor<QuoteLaunchConfiguratorDTO> argumentCaptor = ArgumentCaptor.forClass(QuoteLaunchConfiguratorDTO.class);
        verify(quoteResource).generateGUID(argumentCaptor.capture());
        return argumentCaptor.getValue();
    }


    private void setupCeaseOptimizationUser() {
        UserRoleConfigID userRoleConfigID = new UserRoleConfigID();
        userRoleConfigID.setRole(new UserRoleMasterEntity(1L, "Cease Optimization Team", null, null, null, null));
        userEntity.setUserRoleConfig(newArrayList(new UserRoleConfigEntity(userRoleConfigID)));
    }
}