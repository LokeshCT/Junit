package com.bt.cqm.handler;

import com.bt.rsqe.customerinventory.client.resource.ContactResourceClient;
import com.bt.rsqe.customerinventory.client.resource.SiteResourceClient;
import com.bt.rsqe.customerinventory.dto.contact.ContactRoleDTO;
import com.bt.rsqe.customerinventory.dto.location.AddressDTO;
import com.bt.rsqe.customerinventory.dto.site.SiteDTO;
import com.bt.rsqe.rest.ResponseBuilder;
import com.bt.rsqe.utils.AssertObject;
import com.bt.rsqe.web.rest.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 * The Class ContactResourceHandler.
 */
@Path("/cqm/contacts")
@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
@Produces(MediaType.APPLICATION_JSON)
public class ContactResourceHandler {

    private static final String CUSTOMER_ID = "customerId";
    private static final String SM_USER = "SM_USER";
    private final ContactResourceClient contactResourceClient;
    private SiteResourceClient siteResourceClient;

    private static Logger LOGGER = LoggerFactory.getLogger(ContactResourceHandler.class);


    public ContactResourceHandler(ContactResourceClient contactResourceClient, SiteResourceClient siteResourceClient) {
        this.contactResourceClient = contactResourceClient;
        this.siteResourceClient = siteResourceClient;
    }

    @GET
    public Response searchCustomerContacts(@QueryParam(CUSTOMER_ID) String customerId, @QueryParam("siteId") String siteId) {
        if (AssertObject.anyEmpty(customerId, siteId)) {
            return ResponseBuilder.notFound().build();
        }

        long startTime = System.currentTimeMillis();
        List<ContactRoleDTO> customerContactDTOList = this.contactResourceClient.getContacts(customerId, siteId);
        LOGGER.debug("Received response in {} ms", System.currentTimeMillis() - startTime);
        return ResponseBuilder.anOKResponse().withEntity(new GenericEntity<List<ContactRoleDTO>>(customerContactDTOList) {
        }).build();

    }

    @GET
    @Path("customer")
    public Response searchCustomerContacts(@QueryParam(CUSTOMER_ID) String customerId) {
        if (AssertObject.anyEmpty(customerId)) {
            return ResponseBuilder.badRequest().build();
        }

        long startTime = System.currentTimeMillis();
        List<ContactRoleDTO> customerContactDTOList = null;
        try {
            customerContactDTOList = contactResourceClient.getContacts(customerId);
        } catch (ResourceNotFoundException e) {
            return ResponseBuilder.notFound().build();
        } catch (Exception e) {
            return ResponseBuilder.internalServerError().build();
        }

        LOGGER.debug("Received response in {} ms", System.currentTimeMillis() - startTime);

        return ResponseBuilder.anOKResponse().withEntity(new GenericEntity<List<ContactRoleDTO>>(customerContactDTOList) {
        }).build();
    }

    @POST
    @Path("/create")
    @Produces(MediaType.TEXT_PLAIN)
    public Response createCentralSiteContacts(@HeaderParam(SM_USER) String userId,
                                              @QueryParam(CUSTOMER_ID) Long customerId,
                                              ContactRoleDTO contactRole) throws Exception {

        if (AssertObject.isNotNull(contactRole) && AssertObject.anyEmpty(userId, customerId, contactRole.getSiteId(), contactRole.getCtpType(), contactRole.getContact())) {
            return ResponseBuilder.badRequest().build();
        }

        SiteDTO  siteDTO = siteResourceClient.getSite(contactRole.getSiteId().toString());
        AddressDTO addressDTO = new AddressDTO();
        addressDTO.setAdrId(siteDTO.getAddressId());
        contactRole.getContact().setAddress(addressDTO);
        ContactRoleDTO retContactRole = contactResourceClient.createContact(userId, contactRole);

        String contactId = retContactRole.getContact().getContactId().toString();

        return ResponseBuilder.anOKResponse().withEntity(contactId).build();
    }

    @POST
    @Path("/update")
    @Produces(MediaType.TEXT_PLAIN)
    public Response updateCentralSiteContacts(@HeaderParam(SM_USER) String userId,
                                              @QueryParam(CUSTOMER_ID) Long customerId,
                                              ContactRoleDTO contactRole) {

        if (AssertObject.isNotNull(contactRole) && AssertObject.anyEmpty(userId, customerId, contactRole.getSiteId(), contactRole.getCtpType(), contactRole.getContact())) {
            return ResponseBuilder.badRequest().build();
        }

        try {
            SiteDTO  siteDTO = siteResourceClient.getSite(contactRole.getSiteId().toString());
            AddressDTO addressDTO = new AddressDTO();
            addressDTO.setAdrId(siteDTO.getAddressId());
            contactRole.getContact().setAddress(addressDTO);
            contactResourceClient.updateContact(userId, customerId, contactRole);

            return ResponseBuilder.anOKResponse().build();

        } catch (Exception ex) {
            return ResponseBuilder.internalServerError().withEntity(ex.getMessage()).build();
        }
    }

}
