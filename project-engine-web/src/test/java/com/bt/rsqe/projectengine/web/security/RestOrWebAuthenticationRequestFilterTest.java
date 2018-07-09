package com.bt.rsqe.projectengine.web.security;

import com.bt.rsqe.error.RsqeAuthenticationException;
import com.bt.rsqe.security.RestAuthenticationRequestFilter;
import com.bt.rsqe.web.rest.exception.UnauthorizedException;
import org.junit.Before;
import org.junit.Test;
import org.simpleframework.util.lease.LeaseException;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;

import static org.mockito.Mockito.*;

public class RestOrWebAuthenticationRequestFilterTest {

    private RestAuthenticationRequestFilter restAuthRequestFilter;
    private WebAuthenticationRequestFilter webAuthRequestFilter;
    private ContainerRequestContext containerRequestContext;
    private UriInfo uriInfo;

    @Before
    public void setUp() throws Exception {
        restAuthRequestFilter = mock(RestAuthenticationRequestFilter.class);
        webAuthRequestFilter = mock(WebAuthenticationRequestFilter.class);
        containerRequestContext = mock(ContainerRequestContext.class);
        uriInfo = mock(UriInfo.class);
        when(containerRequestContext.getUriInfo()).thenReturn(uriInfo);
    }

    @Test
    public void shouldAuthenticateARequestFromAnApplication() throws Exception {
        expectingValidApplicationAuthentication();

        RestOrWebAuthenticationRequestFilter requestFilter = new RestOrWebAuthenticationRequestFilter(restAuthRequestFilter, webAuthRequestFilter);
        requestFilter.filter(containerRequestContext);
    }

    @Test
    public void shouldAuthenticateARequestWithValidWebAuthenticationButNotFromAnApplication() throws Exception {
        expectingInvalidApplicationAuthentication();
        expectingValidWebAuthentication();

        RestOrWebAuthenticationRequestFilter requestFilter = new RestOrWebAuthenticationRequestFilter(restAuthRequestFilter, webAuthRequestFilter);
        requestFilter.filter(containerRequestContext);
    }

    @Test(expected = RsqeAuthenticationException.class)
    public void shouldRejectARequestWithoutValidAuthentication() throws Exception {
        expectingInvalidApplicationAuthentication();
        expectingInvalidWebAuthentication();

       new RestOrWebAuthenticationRequestFilter(restAuthRequestFilter, webAuthRequestFilter).filter(containerRequestContext);
    }

    private void expectingValidApplicationAuthentication() throws LeaseException, IOException {
        doNothing().when(restAuthRequestFilter).filter(containerRequestContext);
    }

    private void expectingValidWebAuthentication() throws LeaseException, IOException {
        doNothing().when(webAuthRequestFilter).filter(containerRequestContext);
    }

    private void expectingInvalidApplicationAuthentication() throws LeaseException, IOException {
        doThrow(UnauthorizedException.class).when(restAuthRequestFilter).filter(containerRequestContext);
    }

    private void expectingInvalidWebAuthentication() throws LeaseException, IOException {
        doThrow(new RsqeAuthenticationException("TEST")).when(webAuthRequestFilter).filter(containerRequestContext);
    }
}
