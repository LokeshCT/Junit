package com.bt.rsqe.customerrecord;

import com.bt.rsqe.container.ApplicationConfig;
import com.bt.rsqe.security.UserDTO;
import com.bt.rsqe.web.rest.ProxyAwareRestRequestBuilder;
import com.bt.rsqe.web.rest.RestRequestBuilder;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;

public class UserResource {
    private URI uri;
    private String secret;

    @Deprecated
    /**
    * @deprecated  Use the URI and secret-based constructor instead
    */
    public UserResource(URI baseUri) {
        uri = UriBuilder.fromUri(baseUri).path("rsqe").path("expedio")
                        .path("users")
                        .build();
    }

    public UserResource(URI baseUri, String secret) {
        this(baseUri);
        this.secret = secret;
    }

    @Deprecated
    /**
    * @deprecated  Use the URI and secret-based constructor instead
    */
    public UserResource(ApplicationConfig applicationConfig) {
        this(applicationConfig.getScheme(), applicationConfig.getHost(), applicationConfig.getPort());

    }

    @Deprecated
    /**
    * @deprecated  Use the URI and secret-based constructor instead
    */
    public UserResource(String scheme, String host, int port) {
        this(new com.bt.rsqe.utils.UriBuilder().scheme(scheme)
                                               .host(host)
                                               .port(port).build());
    }

    @Deprecated
    /**
    * @deprecated  Access site through customer resource
    */
    public SiteResource siteResource(String customerId) {
        return new SiteResource(UriBuilder.fromUri(uri).path(customerId).build());
    }

    public UsersDTO find(String customerId, String userRole, String loggedInUser, String rsqeToken) {
        URI uri = UriBuilder.fromUri(this.uri)
                            .queryParam("customerId", customerId)
                            .queryParam("role", userRole)
                            .queryParam("user", loggedInUser)
                            .queryParam("token", rsqeToken)
                            .build();
        RestRequestBuilder restRequestBuilder = new ProxyAwareRestRequestBuilder(uri);
        return restRequestBuilder.withSecret(secret).build().get().getEntity(UsersDTO.class);
    }

    public UserDTO findUser(String loginName) {
        URI uri = UriBuilder.fromUri(this.uri)
                            .path(loginName)
                            .build();
        RestRequestBuilder restRequestBuilder = new ProxyAwareRestRequestBuilder(uri);
        return restRequestBuilder.withSecret(secret).build().get().getEntity(UserDTO.class);
    }
}
