package com.bt.cqm.handler;

import com.bt.rsqe.customerinventory.client.resource.ContractResourceClient;
import com.bt.rsqe.customerinventory.client.resource.SiteResourceClient;
import com.bt.rsqe.customerinventory.dto.site.SiteDTO;
import com.bt.rsqe.customerinventory.resources.CustomerResource;
import com.bt.rsqe.customerinventory.resources.SiteLocationResource;
import com.bt.rsqe.ppsr.client.pop.PpsrPOPDetailClient;
import com.bt.rsqe.projectengine.SiteModifiedResource;
import com.bt.rsqe.web.rest.exception.ConflictException;
import com.google.gson.JsonParser;
import org.junit.Test;

import javax.ws.rs.core.Response;

import static com.google.common.collect.Lists.newArrayList;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Created with IntelliJ IDEA.
 * User: 608026723
 * Date: 03/07/15
 * Time: 20:09
 * To change this template use File | Settings | File Templates.
 */
public class SiteResourceHandlerTest {

    private static final String SALES_CHANNEL = "BT INDIA";
    private static final Long CUSTOMER_ID = 2345678L;
    private ContractResourceClient contractResource = mock(ContractResourceClient.class);
    private CustomerResource customerResourceMock = mock(CustomerResource.class);
    private SiteResourceClient siteResourceClientMock = mock(SiteResourceClient.class);
    private SiteLocationResource siteLocationResourceMock = mock(SiteLocationResource.class);
    private SiteModifiedResource siteModifiedResourceMock = mock(SiteModifiedResource.class);
    private final PpsrPOPDetailClient ppsrPOPDetailClientMock = mock(PpsrPOPDetailClient.class);
    private SiteResourceHandler customerResourceHandler = new SiteResourceHandler(customerResourceMock, siteResourceClientMock,contractResource,siteModifiedResourceMock,ppsrPOPDetailClientMock);
    private JsonParser jsonParser = new JsonParser();
    private static final String USER ="USER_ID";


    @Test
    public void shouldGetCentralSite() {
        Long contractId = 1001L;
        Long customerId = 1001L;
        SiteDTO obj = new SiteDTO();

        when(siteResourceClientMock.getCentralSite(anyLong())).thenReturn(obj);
        Response resp = customerResourceHandler.getCentralSite(USER,contractId,null);

        assert (resp.getStatus() == Response.Status.OK.getStatusCode());
    }

    @Test
    public void shouldGetCentralSiteHandleNoResult() {
        Long contractId = 1001L;
        SiteDTO nullSite = null;

        when(siteResourceClientMock.getCentralSite(anyLong())).thenReturn(nullSite);
        when(siteResourceClientMock.getSites(anyLong(),anyString())).thenReturn(null);
        Response resp = customerResourceHandler.getCentralSite(USER,contractId,1001L);

        assert (resp.getStatus() == Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void shouldGetCentralSiteHandleInvalidReq() {
        Long nullContractId = null;
        SiteDTO obj = new SiteDTO();

        when(siteResourceClientMock.getCentralSite(anyLong())).thenReturn(obj);
        Response resp = customerResourceHandler.getCentralSite(USER,nullContractId,null);

        assert (resp.getStatus() == Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test(expected = ConflictException.class)
    public void shouldGetCentralSiteThrowRestExceptions() {
        Long contractId = 1001L;
        SiteDTO obj = new SiteDTO();

        doThrow(new ConflictException()).when(siteResourceClientMock).getCentralSite(anyLong());
        Response resp = customerResourceHandler.getCentralSite(USER,contractId,null);
    }

    @Test(expected = RuntimeException.class)
    public void shouldGetCentralSiteThrowRuntimeExceptions() {
        Long contractId = 1001L;
        SiteDTO obj = new SiteDTO();

        doThrow(new RuntimeException()).when(siteResourceClientMock).getCentralSite(anyLong());
        Response resp = customerResourceHandler.getCentralSite(USER,contractId,null);
    }

    @Test
    public void shouldGetCentralSiteHandleTransformDefaultCentralSite() {
        Long contractId = 1001L;
        SiteDTO defaultCentralSite = new SiteDTO();
        defaultCentralSite.setCity("CENTRAL");
        defaultCentralSite.setBuildingName("CENTRAL");

        when(siteResourceClientMock.getCentralSite(anyLong())).thenReturn(defaultCentralSite);
        Response resp = customerResourceHandler.getCentralSite(USER,contractId,null);

        assert (resp.getStatus() == Response.Status.OK.getStatusCode());
        SiteDTO retObj = ((SiteDTO) resp.getEntity());
        assert ("".equals(retObj.getBuildingName()));
    }

}
