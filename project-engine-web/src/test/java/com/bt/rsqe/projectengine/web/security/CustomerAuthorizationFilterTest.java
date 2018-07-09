package com.bt.rsqe.projectengine.web.security;

import com.bt.rsqe.error.RsqeAuthorizationException;
import com.bt.rsqe.security.PermissionsDTO;
import com.bt.rsqe.security.UserContext;
import com.bt.rsqe.security.UserContextDTO;
import com.bt.rsqe.security.UserContextManager;
import com.bt.rsqe.session.client.PermissionResource;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.*;


public class CustomerAuthorizationFilterTest {

    private CustomerAuthorizationFilter systemUnderTest;
    private ContainerRequestContext requestContext;
    private UriInfo uriInfo;
    private PermissionResource permissionResource;

    @Before
    public void before() {
        requestContext = mock(ContainerRequestContext.class);
        uriInfo = mock(UriInfo.class);
        permissionResource = mock(PermissionResource.class);
        UserContextManager.setCurrent(new UserContext(new UserContextDTO("loginName", "")));
        systemUnderTest = new CustomerAuthorizationFilter(permissionResource);
        when(requestContext.getUriInfo()).thenReturn(uriInfo);
    }

    @Test(expected = RsqeAuthorizationException.class)
    public void shouldThrowAuthorizationExceptionGivenUserDoesNotHavePermissionToTheCustomer() throws Exception {

        List<PathSegment> segments = new ArrayList<PathSegment>();
        segments.add(new PathSegmentImpl("customers"));
        segments.add(new PathSegmentImpl("1234"));
        when(uriInfo.getPath()).thenReturn("/customers/1234");
        when(uriInfo.getPathSegments(true)).thenReturn(segments);
        when(permissionResource.userPermissionsForCustomer("1234")).thenReturn(new PermissionsDTO(false, false, false, false, false, false));

        systemUnderTest.filter(requestContext);
    }

    @Test
    public void shouldSetTheUserContextWithPermissions() throws Exception {

        List<PathSegment> segments = new ArrayList<PathSegment>();
        segments.add(new PathSegmentImpl("customers"));
        segments.add(new PathSegmentImpl("1234"));
        when(uriInfo.getPathSegments(true)).thenReturn(segments);
        when(uriInfo.getPath()).thenReturn("/customers/1234");
        when(permissionResource.userPermissionsForCustomer("1234")).thenReturn(new PermissionsDTO(true, true, true, true, true, false));

        systemUnderTest.filter(requestContext);
        assertThat(UserContextManager.getCurrent().getPermissions(), is(notNullValue()));
        assertThat(UserContextManager.getCurrent().getPermissions().bcmAccess, is(true));
        assertThat(UserContextManager.getCurrent().getPermissions().eupAccess, is(true));
        assertThat(UserContextManager.getCurrent().getPermissions().indirectUser, is(true));
    }

    @Test
    public void shouldNotFailIfPathContainsStatic() throws Exception {
        expectingPath("/rsqe/static");
        systemUnderTest.filter(requestContext);
    }

    @Test
    public void shouldNotFailIfPathContainsInlife() throws Exception {
        expectingPath("/monitoring/information");
        systemUnderTest.filter(requestContext);
    }

    @Test
    public void shouldNotFailIfPathContainsFavicon() throws Exception {
        expectingPath("favicon.ico");
        systemUnderTest.filter(requestContext);
    }

    @Test
    public void shouldNotFailIfPathContainsWebMetrics() throws Exception {
        expectingPath("/rsqe/web-metrics");
        systemUnderTest.filter(requestContext);
    }

    private void expectingPath(final String path) {
        when(this.uriInfo.getPath()).thenReturn(path);
    }

    class PathSegmentImpl implements PathSegment  {

        private String segment;

        PathSegmentImpl(String segment) {
            this.segment = segment;
        }

        @Override
        public String getPath() {
            return segment;
        }

        @Override
        public MultivaluedMap<String, String> getMatrixParameters() {
            throw new UnsupportedOperationException();
        }
    }
}
