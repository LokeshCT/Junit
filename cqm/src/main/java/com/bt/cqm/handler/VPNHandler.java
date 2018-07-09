package com.bt.cqm.handler;

import com.bt.cqm.exception.DuplicateCustomerException;
import com.bt.rsqe.customerinventory.dto.VPNDTO;
import com.bt.rsqe.customerinventory.resources.VPNResource;
import com.bt.rsqe.rest.ResponseBuilder;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

import static com.bt.cqm.utils.Utility.*;

/**
 * Created with IntelliJ IDEA.
 * User: 607982105
 * Date: 19/02/14
 * Time: 17:32
 * Handler for the VPN Tab of the CQM.
 */
@Path("/cqm/vpn")
@Produces(MediaType.APPLICATION_JSON)
@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
public class VPNHandler {

    private final VPNResource vpnResource;

    public VPNHandler(VPNResource vpnResource) {
        this.vpnResource = vpnResource;
    }

    @GET
    @Path("customerId")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCustomerVPNDetails(@QueryParam("customerId") String customerId) {

        if (null == customerId||0==customerId.trim().length()) {
            return ResponseBuilder.notFound().build();
        }
        List<VPNDTO> vpnDTO = null;
        try {
            vpnDTO = vpnResource.getCustomerVPNDetails(customerId);

        } catch (Exception e) {
            return handleException(e);
        }
        GenericEntity<List<VPNDTO>> vpnEntity = new GenericEntity<List<VPNDTO>>(vpnDTO) {
        };
        return ResponseBuilder.anOKResponse().withEntity(vpnEntity).build();
    }

    @GET
    @Path("sharedCustomerId")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getSharedCustomerVPNDetails(@QueryParam("customerId") String customerId) {

        if (customerId == null) {
            return ResponseBuilder.notFound().build();
        }
        List<VPNDTO> vpnDTO = null;
        try {
            vpnDTO = vpnResource.getSharedCustomerVPNDetails(customerId);

        }  catch (Exception e) {
            return handleException(e);
        }
        GenericEntity<List<VPNDTO>> vpnEntity = new GenericEntity<List<VPNDTO>>(vpnDTO) {
        };
        return ResponseBuilder.anOKResponse().withEntity(vpnEntity).build();
    }


    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Path("createSharedVPNServiceId")
    public Response createSharedVPN(@QueryParam("vpnServiceID") String vpnServiceID, @QueryParam("customerID") String customerID) {

        if (null== vpnServiceID  || null== customerID ||vpnServiceID.trim().length()==0||customerID.trim().length()==0) {
            return ResponseBuilder.internalServerError().withEntity("Mandatory Parameters Customer Id/Service Id  are Missing").build();
        }

        try {
            String status = vpnResource.createVPNSharedCustomer(vpnServiceID, customerID);
            if (customerID.equals("0")) {
                throw new DuplicateCustomerException("Invalid Customer ID");
            }

            if(null!=status)
            {
                return ResponseBuilder.anOKResponse().withEntity(vpnServiceID).build();
            }
            else
            {
                return ResponseBuilder.internalServerError().withEntity("Request could not be processed").build();
            }
        }
        catch (Exception e) {
            return handleException(e);
        }
    }

    private Response handleException(Exception ex) {
        return ResponseBuilder.notFound().withEntity(buildGenericError(ex.getMessage())).build();
    }


}
