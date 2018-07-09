package com.bt.cqm.web;

import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;

/**
 * Created with IntelliJ IDEA.
 * User: 608026723
 * Date: 3/3/15
 * Time: 7:53 PM
 * To change this template use File | Settings | File Templates.
 */
public class CQMAuthenticationRequestFilterTest extends TestCase {
    @Mock
    private ContainerRequestContext containerRequestContextMock ;

    @Mock
    UriInfo uriInfoMock;

    @Before
    public void setUp(){
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testFilterWithDomainName() throws Exception {
        MultivaluedMap<String, String> headers = new MultivaluedHashMap<String, String>() ;
        List<String> headerVals = new ArrayList<String>();
        headerVals.add("608026723");
        headers.put("SM_USER",headerVals);

        when(containerRequestContextMock.getHeaderString(anyString())).thenReturn("IUSER/TRANF_USER");
        when(containerRequestContextMock.getUriInfo()).thenReturn(uriInfoMock);
        when(uriInfoMock.getAbsolutePath()).thenReturn(new URI(""));
        when(containerRequestContextMock.getHeaders()).thenReturn(headers);
        CQMAuthenticationRequestFilter cqmAuthenticationRequestFilter = new CQMAuthenticationRequestFilter();
        cqmAuthenticationRequestFilter.filter(containerRequestContextMock);

        assert(((List)headers.get("SM_USER")).get(0).equals("TRANF_USER"));
        verify(containerRequestContextMock,times(1)).getHeaders();
    }

    @Test
    public void testFilterWithRegularUser() throws Exception {

        when(containerRequestContextMock.getHeaderString(anyString())).thenReturn("TRANF_USER");
        when(containerRequestContextMock.getUriInfo()).thenReturn(uriInfoMock);
        when(uriInfoMock.getAbsolutePath()).thenReturn(new URI(""));
        CQMAuthenticationRequestFilter cqmAuthenticationRequestFilter = new CQMAuthenticationRequestFilter();
        cqmAuthenticationRequestFilter.filter(containerRequestContextMock);

        verify(containerRequestContextMock,times(0)).getHeaders();
    }

    @Test
    public void testFilterLogOff() throws Exception {
        MultivaluedMap<String, String> headers = new MultivaluedHashMap<String, String>() ;
        List<String> headerVals = new ArrayList<String>();
        headerVals.add("608026723");
        headers.put("SM_USER",headerVals);

        when(containerRequestContextMock.getHeaderString(anyString())).thenReturn("TRANF_USER");
        when(containerRequestContextMock.getUriInfo()).thenReturn(uriInfoMock);
        when(uriInfoMock.getAbsolutePath()).thenReturn(new URI("/cqm/logout"));
        when(containerRequestContextMock.getHeaders()).thenReturn(headers);
        CQMAuthenticationRequestFilter cqmAuthenticationRequestFilter = new CQMAuthenticationRequestFilter();
        cqmAuthenticationRequestFilter.filter(containerRequestContextMock);

        verify(containerRequestContextMock,times(5)).getHeaders();
    }
}
