package com.bt.rsqe.expedio.audit;

import com.bt.rsqe.factory.RestRequestBuilderFactory;
import com.bt.rsqe.web.rest.RestRequestBuilder;
import com.bt.rsqe.web.rest.RestResource;
import com.bt.rsqe.web.rest.RestResponse;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import javax.ws.rs.core.GenericType;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Created with IntelliJ IDEA.
 * User: 608026723
 * Date: 3/30/15
 * Time: 8:24 PM
 * To change this template use File | Settings | File Templates.
 */
public class AuditTrailResourceTest {

    AuditTrailResource auditTrailResource;

    @Mock
    RestResponse clientResponseMock = null;

    @Mock
    RestResource restResourceMock = null;

    @Mock
    RestRequestBuilder restRequestBuilderMock = null;

    @Mock
    RestRequestBuilderFactory restRequestBuilderFactoryMock =null;

    @Before
    public void setUp() throws Exception {

        MockitoAnnotations.initMocks(this);

        Mockito.when(restRequestBuilderFactoryMock.createProxyAwareRestRequestBuilder(any(URI.class))).thenReturn(restRequestBuilderMock);
        when(restRequestBuilderMock.withSecret(anyString())).thenReturn(restRequestBuilderMock);
        auditTrailResource = new AuditTrailResource(new URI("http://localhost:9999"),null,restRequestBuilderFactoryMock);
    }

    @Test
    public void shouldGetOrderAuditSummary(){
        List<AuditSummaryDTO> auditSummaryDTOs = new ArrayList<AuditSummaryDTO>();

        when(restRequestBuilderMock.build(anyString(), anyMap())).thenReturn(restResourceMock);
        when(restResourceMock.get()).thenReturn(clientResponseMock);
        when(clientResponseMock.getEntity(any(GenericType.class))).thenReturn(auditSummaryDTOs);

        List<AuditSummaryDTO> result =auditTrailResource.getOrderAuditSummary("1111");
        assert(result !=null);
    }

    @Test
    public void shouldGetOrderAuditDetail(){
        List<AuditDetailDTO> auditSummaryDTOs = new ArrayList<AuditDetailDTO>();

        when(restRequestBuilderMock.build(anyString(), anyMap())).thenReturn(restResourceMock);
        when(restResourceMock.get()).thenReturn(clientResponseMock);
        when(clientResponseMock.getEntity(any(GenericType.class))).thenReturn(auditSummaryDTOs);

        List<AuditDetailDTO> result =auditTrailResource.getOrderAuditDetail("1111","100");
        assert(result !=null);
    }

    @Test
    public void shouldGetQuoteAuditSummary(){
        List<AuditSummaryDTO> auditSummaryDTOs = new ArrayList<AuditSummaryDTO>();

        when(restRequestBuilderMock.build(anyString(), anyMap())).thenReturn(restResourceMock);
        when(restResourceMock.get()).thenReturn(clientResponseMock);
        when(clientResponseMock.getEntity(any(GenericType.class))).thenReturn(auditSummaryDTOs);

        List<AuditSummaryDTO> result =auditTrailResource.getQuoteAuditSummary("1111");
        assert(result !=null);
    }

    @Test
    public void shouldGetQuoteAuditDetail(){
        List<AuditDetailDTO> auditSummaryDTOs = new ArrayList<AuditDetailDTO>();

        when(restRequestBuilderMock.build(anyString(), anyMap())).thenReturn(restResourceMock);
        when(restResourceMock.get()).thenReturn(clientResponseMock);
        when(clientResponseMock.getEntity(any(GenericType.class))).thenReturn(auditSummaryDTOs);

        List<AuditDetailDTO> result =auditTrailResource.getQuoteAuditDetail("1111","100");
        assert(result !=null);
    }
}
