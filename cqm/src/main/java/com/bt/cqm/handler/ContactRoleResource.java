package com.bt.cqm.handler;

import com.bt.rsqe.customerinventory.client.CustomerInventoryClientConfig;
import com.bt.rsqe.customerinventory.dto.contact.ContactRoleDTO;
import com.bt.rsqe.web.rest.ProxyAwareRestRequestBuilder;
import com.bt.rsqe.web.rest.RestRequestBuilder;
import com.bt.rsqe.web.rest.RestResponse;
import javax.ws.rs.core.GenericType;

import java.net.URI;

/**
 * Created with IntelliJ IDEA.
 * User: 607866849
 * Date: 27/03/14
 * Time: 12:13
 * To change this template use File | Settings | File Templates.
 */
public class ContactRoleResource {

    private RestRequestBuilder restRequestBuilder;

    public ContactRoleResource(URI baseURI, String secret) {
        URI uri = com.bt.rsqe.utils.UriBuilder.buildUri(baseURI, "rsqe", "customer-inventory", "contact-roles");
        this.restRequestBuilder = new ProxyAwareRestRequestBuilder(uri).withSecret(secret);
    }

    public ContactRoleResource(CustomerInventoryClientConfig clientConfig) {
        this(com.bt.rsqe.utils.UriBuilder.buildUri(clientConfig.getApplicationConfig()), clientConfig.getRestAuthenticationClientConfig().getSecret());
    }

 /*   public static ContactRoleResource getInstance(){
        try{
            return new ContactRoleResource(new URI("http://localhost:9999/rsqe"), "rsqe-secret");
        }catch (URISyntaxException e){
            return null;//TODO need to log the information.
        }
    }*/

    public String createContactRole(ContactRoleDTO contactRoleDTO) {
        RestResponse restResponse = this.restRequestBuilder.build().post(contactRoleDTO);
        String createStatus = restResponse.getEntity(new GenericType<String>() {
        });
        return createStatus;
    }


    public String updateContactRole(ContactRoleDTO contactRoleDTO) {
        RestResponse restResponse = this.restRequestBuilder.build().put(contactRoleDTO);
        String updateStatus = restResponse.getEntity(new GenericType<String>() {
        });
        return updateStatus;
    }
}
