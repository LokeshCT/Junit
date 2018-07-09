package com.bt.cqm.handler;

import com.bt.cqm.repository.user.UserManagementRepository;
import com.bt.rsqe.customerinventory.client.resource.BillingAccountResourceClient;
import com.bt.rsqe.customerinventory.client.resource.ContactResourceClient;
import com.bt.rsqe.customerinventory.client.resource.SiteResourceClient;
import com.bt.rsqe.customerinventory.dto.billing.CustomerBillingDetailDTO;
import com.bt.rsqe.customerinventory.dto.contact.ContactRoleDTO;
import com.bt.rsqe.customerinventory.dto.site.SiteDTO;
import com.bt.rsqe.customerinventory.dto.site.SiteUpdateDTO;
import com.bt.rsqe.customerinventory.resources.CustomerResource;
import com.bt.rsqe.customerrecord.ExpedioClientResources;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.ws.rs.core.Form;
import javax.ws.rs.core.Response;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;

/**
 * Created with IntelliJ IDEA.
 * User: 608026723
 * Date: 2/16/15
 * Time: 6:30 PM
 * To change this template use File | Settings | File Templates.
 */
public class BillingAccountHandlerTest {
    @Mock
    BillingAccountResourceClient billingAccountResourceClientMock;

    @Mock
    SiteResourceClient siteResourceClientMock;

    @Mock
    CustomerResource customerResourceMock;

    @Mock
    ContactResourceClient contactResourceClientMock;

    @Mock
    UserManagementRepository userManagementRepositoryMock;

    @Mock
    ExpedioClientResources expedioClientResources;

    BillingAccountHandler billingAccountHandler = null;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        billingAccountHandler = new BillingAccountHandler(billingAccountResourceClientMock, siteResourceClientMock, customerResourceMock, contactResourceClientMock, userManagementRepositoryMock, expedioClientResources);
    }

    @Test
    public void shouldUpdateBilling() throws Exception {
        Form form = new Form();
        form.param("city", "bangalore");

        SiteDTO siteDTO = new SiteDTO();

        when(siteResourceClientMock.createSite(anyString(), any(SiteUpdateDTO.class))).thenReturn(100L);
        when(siteResourceClientMock.getSite(anyString())).thenReturn(siteDTO);

        when(contactResourceClientMock.updateContact(anyString(), anyLong(), any(ContactRoleDTO.class))).thenReturn(true);

        when(billingAccountResourceClientMock.updateBillingAccount(anyString(), any(CustomerBillingDetailDTO.class))).thenReturn(true);

        Response resp = billingAccountHandler.updateBilling("USER_ID", 100L, form);

        assert (resp.getStatus() == Response.Status.OK.getStatusCode());
    }

    @Test
    public void shouldUpdateBillingHandleInvalidInput() throws Exception {
        String emptyUserId = "";
        Form form = new Form();
        form.param("city", "bangalore");

        Response resp = billingAccountHandler.updateBilling(emptyUserId, 100L, form);

        assert (resp.getStatus() == Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void shouldUpdateBillingCreateSiteOnEmptySiteId() throws Exception {
        Form form = new Form();
        form.param("siteId","");

        SiteDTO siteDTO = new SiteDTO();

        when(siteResourceClientMock.createSite(anyString(), any(SiteUpdateDTO.class))).thenReturn(100L);
        when(siteResourceClientMock.getSite(anyString())).thenReturn(siteDTO);

        when(contactResourceClientMock.updateContact(anyString(), anyLong(), any(ContactRoleDTO.class))).thenReturn(true);

        when(billingAccountResourceClientMock.updateBillingAccount(anyString(), any(CustomerBillingDetailDTO.class))).thenReturn(true);

        Response resp = billingAccountHandler.updateBilling("USER_ID", 100L, form);

        verify(siteResourceClientMock,times(1)).createSite(anyString(), any(SiteUpdateDTO.class));
        verify(siteResourceClientMock,times(0)).updateSite(anyString(), any(SiteUpdateDTO.class));
    }

    @Test
    public void shouldUpdateBillingUpdatSiteOnNonEmptySiteId() throws Exception {
        Form form = new Form();
        form.param("siteId","1000");

        SiteDTO siteDTO = new SiteDTO();

        when(siteResourceClientMock.updateSite(anyString(), any(SiteUpdateDTO.class))).thenReturn("100");
        when(siteResourceClientMock.getSite(anyString())).thenReturn(siteDTO);

        when(contactResourceClientMock.updateContact(anyString(), anyLong(), any(ContactRoleDTO.class))).thenReturn(true);

        when(billingAccountResourceClientMock.updateBillingAccount(anyString(), any(CustomerBillingDetailDTO.class))).thenReturn(true);

        Response resp = billingAccountHandler.updateBilling("USER_ID", 100L, form);

        verify(siteResourceClientMock,times(0)).createSite(anyString(), any(SiteUpdateDTO.class));
        verify(siteResourceClientMock,times(1)).updateSite(anyString(), any(SiteUpdateDTO.class));
    }

    @Test
    public void shouldShowSearchBillingAccounts(){
        CustomerBillingDetailDTO customerBillingDetailDTO = new CustomerBillingDetailDTO();
        List<CustomerBillingDetailDTO> customerBillingDetailDTOs = new ArrayList<CustomerBillingDetailDTO>();
        customerBillingDetailDTOs.add(customerBillingDetailDTO);

        when(billingAccountResourceClientMock.getBillingAccountForCustomer(anyString(),anyString())).thenReturn(customerBillingDetailDTOs);
        when(userManagementRepositoryMock.getSalesChannelFromGFRCode(anyString())).thenReturn("BT AMERICAS");

        Response resp =billingAccountHandler.showSearchBillingAccounts("1001","2001");

        assert (Response.Status.OK.getStatusCode() == resp.getStatus());
    }
}
