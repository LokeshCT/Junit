package com.bt.rsqe.customerrecord;

import com.bt.rsqe.client.ExpedioClient;
import com.bt.rsqe.container.ApplicationConfig;
import com.bt.rsqe.customerrecord.client.ExpedioFacadeConfig;
import com.bt.rsqe.expedio.contact.BFGContactsResource;
import com.bt.rsqe.expedio.order.OrderResource;
import com.bt.rsqe.expedio.project.ExpedioProjectResource;
import com.bt.rsqe.expedio.revenue.RevenueResource;
import com.bt.rsqe.expedio.site.SiteSubmissionResource;
import com.bt.rsqe.utils.UriBuilder;

import java.net.URI;

public class ExpedioClientResources implements ExpedioClient {
    private URI baseUri;
    private String secret;

    @Deprecated
    /**
     * @deprecated Use the URI and secret-based ExpedioFacadeConfig-based constructor instead
     */
    public ExpedioClientResources(ApplicationConfig applicationConfig) {
        this(applicationConfig.getScheme(), applicationConfig.getHost(), applicationConfig.getPort());
    }

    public ExpedioClientResources(ExpedioFacadeConfig expedioFacadeConfig) {
        this(expedioFacadeConfig.getApplicationConfig());
        this.secret = expedioFacadeConfig.getRestAuthenticationClientConfig().getSecret();
    }

    @Deprecated
    /**
     * @deprecated Use the URI and secret-based ExpedioFacadeConfig-based constructor instead
     */
    public ExpedioClientResources(String scheme, String host, int port) {
        this(new UriBuilder().scheme(scheme)
                                 .host(host)
                                 .port(port).build());
    }

    @Deprecated
    /**
     * @deprecated Use the URI and secret-based ExpedioFacadeConfig-based constructor instead
     */
    public ExpedioClientResources(URI baseUri) {
        this.baseUri = baseUri;
    }

    public ExpedioClientResources(URI baseUri, String secret) {
        this.baseUri = baseUri;
        this.secret = secret;
    }

    @Override
    public CustomerResource getCustomerResource() {
        return new CustomerResource(baseUri, secret);
    }

    public OrderResource orderResource() {
        return new OrderResource(baseUri, secret);
    }

    public SiteSubmissionResource siteSubmissionResource() {
        return new SiteSubmissionResource(baseUri, secret);
    }

    public BFGContactsResource bfgContactsResource() {
        return new BFGContactsResource(baseUri, secret);
    }

    public UserResource getUserResource() {
        return new UserResource(baseUri, secret);
    }

    public ExpedioProjectResource projectResource() {
        return new ExpedioProjectResource(baseUri, secret);
    }

    public ExpedioServicesResource getExpedioServicesResource() {
        return new ExpedioServicesResource(baseUri, secret);
    }

    public RevenueResource getRevenueResource(){
        return new RevenueResource(baseUri, secret);
    }
}
