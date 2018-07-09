package com.bt.cqm.handler;


import com.bt.rsqe.customerinventory.dto.VPNDTO;
import com.bt.rsqe.customerinventory.resources.VPNResource;
import com.bt.rsqe.utils.RSQEMockery;
import org.hamcrest.Matchers;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.core.Is.*;

/**
 * Created with IntelliJ IDEA.
 * User: 607866849
 * Date: 25/07/14
 * Time: 17:57
 * To change this template use File | Settings | File Templates.
 */

public class VPNHandlerTest {

    private VPNHandler vpnHandler;
    private VPNResource vpnResource;
    private final Mockery context = new RSQEMockery();
    private String customerId="12788798";
    private String serviceId = "12345";
    private String status ="1123";

    @Before
    public void setUp(){
        try {
            vpnResource = context.mock(VPNResource.class);
            vpnHandler = new VPNHandler(vpnResource);
        } catch (Throwable ex) {
            System.out.println("Fail to create setup for VPNHandlerTest." + ex);
        }
    }

    @Test
   public void shouldGetCustomerVPNDetails() throws Exception{

        final List<VPNDTO> vpndtoList = new  ArrayList<VPNDTO>();
        VPNDTO vpndto = new VPNDTO(customerId,"","","","","","","");
        vpndtoList.add(vpndto);
        context.checking(new Expectations() {{
            allowing(vpnResource).getCustomerVPNDetails(with(Matchers.any(String.class)));
            will(returnValue(vpndtoList));
        }});

        Response res = vpnHandler.getCustomerVPNDetails(customerId);
        //List<VPNDTO> customerDTOList = (List<VPNDTO>) res.getEntity();
        //assert ((customerDTOList.get(0)).getCustomerID().equals("12788798"));
        assertEquals(res.getStatus(),200);

    }

    @Test
    public void shouldGetSharedCustomerVPNDetails() throws Exception{

        final List<VPNDTO> vpndtoList = new  ArrayList<VPNDTO>();
        VPNDTO vpndto = new VPNDTO(customerId,"","","","","","","");
        vpndtoList.add(vpndto);
        context.checking(new Expectations() {{
            allowing(vpnResource).getSharedCustomerVPNDetails(with(Matchers.any(String.class)));
            will(returnValue(vpndtoList));
        }});

        Response res = vpnHandler.getSharedCustomerVPNDetails(customerId);
        assertEquals(res.getStatus(),200);
    }

    @Test
    public void shouldCreateSharedVPN() throws Exception{

        context.checking(new Expectations() {{
            allowing(vpnResource).createVPNSharedCustomer(with(Matchers.any(String.class)), with(Matchers.any(String.class)));
            will(returnValue(status));
        }});

        Response res = vpnHandler.createSharedVPN(customerId,serviceId);
        assert(res.getEntity().equals("12788798"));

    }

    @Test
    public void shouldGetCustomerVPNDetailsCustomerNotFound() throws Exception{
        Response res = vpnHandler.getCustomerVPNDetails(null);
        assertThat(res.getStatus(), is(Response.Status.NOT_FOUND.getStatusCode()));
    }

    @Test
    public void shouldGetSharedCustomerVPNDetailsNotFound() throws Exception{
        Response res = vpnHandler.getSharedCustomerVPNDetails(null);
        assertThat(res.getStatus(), is(Response.Status.NOT_FOUND.getStatusCode()));
    }

    @Test
    public void shouldCreateSharedVPNNotFound() throws Exception{
        Response res = vpnHandler.createSharedVPN(null, null);
        assertThat(res.getStatus(), is(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()));
    }
}
