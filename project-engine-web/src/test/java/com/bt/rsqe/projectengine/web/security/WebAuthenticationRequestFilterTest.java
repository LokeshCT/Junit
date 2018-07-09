package com.bt.rsqe.projectengine.web.security;


import com.bt.rsqe.container.fixtures.ContainerRequestContextFixture;
import com.bt.rsqe.security.ExpedioSessionResourceStub;
import com.bt.rsqe.security.ExpedioUserContextResolver;
import com.bt.rsqe.security.UserContextManager;
import com.bt.rsqe.web.UserContextResolver;
import org.junit.Before;
import org.junit.Test;
import org.simpleframework.util.lease.LeaseException;

import javax.ws.rs.container.ContainerRequestContext;
import java.io.IOException;

import static com.bt.rsqe.container.fixtures.ContainerRequestContextFixture.*;
import static org.junit.Assert.*;

public class WebAuthenticationRequestFilterTest {

    @Before
    public void setup() {
        UserContextManager.clear();
    }

    @Test
    public void shouldNotFilterExemptedURIs() throws Exception {
        filterRequest(aContainerRequestContext().withMethod("GET").withUri("rsqe/static/").build());
        assertNull(UserContextManager.getCurrent());
    }

    @Test
    public void shouldNotFilterFaviconURIs() throws Exception {
        filterRequest(ContainerRequestContextFixture.aContainerRequestContext().withMethod("GET").withUri("/favicon.ico").build());
        assertNull(UserContextManager.getCurrent());
    }

    @Test(expected = WebAuthenticationRequestFilter.MethodNotAllowedException.class)
    public void shouldThrowMethodNotAllowedExceptionForOptions() throws Exception {
        filterRequest(aContainerRequestContext().withUri("/rsqe/customer/123/projects/12").withQueryParams("guid", "1234").withMethod("OPTIONS").build());
    }

    @Test
    public void shouldNotThrowMethodNotAllowedExceptionForPOSTAndGETAndPUT() throws Exception {
        filterRequest(aContainerRequestContext().withUri("/rsqe/customer/123/projects/12").withQueryParams("guid", "1234").withMethod("POST").build());
        filterRequest(aContainerRequestContext().withUri("/rsqe/customer/123/projects/12").withQueryParams("guid", "1234").withMethod("GET").build());
        filterRequest(aContainerRequestContext().withUri("/rsqe/customer/123/projects/12").withQueryParams("guid", "1234").withMethod("PUT").build());
    }

    private UserContextResolver expedioUserContextResolver() {
        return new ExpedioUserContextResolver(sessionResource(), null);
    }

    private void filterRequest(ContainerRequestContext request) throws LeaseException, IOException {
        new WebAuthenticationRequestFilter(expedioUserContextResolver()).filter(request);
    }

    private ExpedioSessionResourceStub sessionResource() {
        return new ExpedioSessionResourceStub();
    }
}
