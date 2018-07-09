package com.bt.cqm.handler;

import com.bt.rsqe.customerinventory.client.resource.ContactResourceClient;
import com.bt.rsqe.customerinventory.client.resource.SiteResourceClient;
import com.bt.rsqe.customerinventory.dto.contact.ContactRoleDTO;
import com.bt.rsqe.logging.Log;
import com.bt.rsqe.logging.LogFactory;
import com.bt.rsqe.logging.LogLevel;
import com.bt.rsqe.rest.ResponseBuilder;
import com.bt.rsqe.utils.AssertObject;

import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/cqm/branchContact")
@Produces(MediaType.APPLICATION_JSON)
@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
public class BranchSiteContactResourceHandler {
    private final ContactsLogger LOG = LogFactory.createDefaultLogger(ContactsLogger.class);

    private final SiteResourceClient siteResourceClient;
    private ContactResourceClient contactResourceClient;


    public BranchSiteContactResourceHandler(SiteResourceClient siteResourceClient, ContactResourceClient contactResourceClient) {
        this.contactResourceClient = contactResourceClient;
        this.siteResourceClient = siteResourceClient;
    }


    @POST
    @Path("/create")
    @Produces(MediaType.TEXT_PLAIN)
    public Response createBranchSiteContacts(@HeaderParam("SM_USER") String userId,
                                             @QueryParam("customerId") Long customerId,
                                             ContactRoleDTO contactRoleDTO) {

        if (AssertObject.anyEmpty(userId, customerId, contactRoleDTO) || AssertObject.anyEmpty(contactRoleDTO.getSiteId())) {
            return ResponseBuilder.badRequest().build();
        }
        Long contactId = null;
        ContactRoleDTO retContactRoleDTO=null;

        try {
            retContactRoleDTO = contactResourceClient.createContact(userId, contactRoleDTO);
            contactId = retContactRoleDTO.getContact().getContactId();
        } catch (Exception e) {
            return handleException(e);
        }

        return ResponseBuilder.anOKResponse().withEntity(contactId.toString()).build();
    }


    @POST
    @Path("/update")
    public Response updateBranchSiteContacts(@HeaderParam("SM_USER") String userId,
                                             @QueryParam("customerId") Long customerId,
                                             ContactRoleDTO contactRoleDTO) {

        if (AssertObject.anyEmpty(userId, customerId, contactRoleDTO) || AssertObject.anyEmpty(contactRoleDTO.getContact()) || AssertObject.anyEmpty(contactRoleDTO.getId(),contactRoleDTO.getContact().getContactId())) {
            return ResponseBuilder.badRequest().build();
        }

        try {

            contactResourceClient.updateContact(userId, customerId, contactRoleDTO);
        } catch (Exception e) {
            return handleException(e);
        }

        return ResponseBuilder.anOKResponse().build();
    }

    private Response handleException(Exception ex) {
        LOG.receivedSalesChannelErrorResponse(ex);
        return ResponseBuilder.notFound().withEntity(ex.getMessage()).build();
    }

    private interface ContactsLogger {
        @Log(level = LogLevel.INFO, format = "Received DB response with time in seconds: [%s]")
        void receivedSalesChannelResponse(long endTimeId);

        @Log(level = LogLevel.ERROR, format = "Received error response when fetching user config with:[%s]")
        void receivedSalesChannelErrorResponse(String faultDesc);

        @Log(level = LogLevel.ERROR, format = "The  Contact not created with Exception:[%s]")
        void receivedSalesChannelErrorResponse(Exception faultDesc);
    }
}
