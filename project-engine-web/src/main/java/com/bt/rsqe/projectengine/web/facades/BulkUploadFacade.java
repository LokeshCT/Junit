package com.bt.rsqe.projectengine.web.facades;

import com.bt.rsqe.security.Credentials;
import com.bt.rsqe.security.UserContextManager;
import com.bt.rsqe.web.AjaxResponseDTO;
import com.bt.rsqe.web.rest.ClientFactory;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;

public class BulkUploadFacade {

    public AjaxResponseDTO upload(String bulkUploadUri, FormDataMultiPart multiPartFormData) {
        final Response response = uploadToUri(bulkUploadUri, multiPartFormData);
        return new AjaxResponseDTO(isResponseAccepted(response), response.readEntity(String.class));
    }

    private Response uploadToUri(String bulkUploadUri, FormDataMultiPart multiPartFormData) {
        Cookie rsqeToken = new Cookie(Credentials.RSQE_TOKEN, UserContextManager.getCurrent().getRsqeToken());
        //TODO: Move this to RestRequestBuilder
        Invocation.Builder builder = new ClientFactory().createClient()
                                                  .target(bulkUploadUri)
                                                  .request(MediaType.MULTIPART_FORM_DATA)
                                                  .accept(MediaType.TEXT_HTML)
                                                  .cookie(rsqeToken);
        return builder.post(Entity.entity(multiPartFormData, MediaType.MULTIPART_FORM_DATA_TYPE));
    }

    private boolean isResponseAccepted(Response response) {
        return response.getStatus() == Response.Status.ACCEPTED.getStatusCode();
    }
}
