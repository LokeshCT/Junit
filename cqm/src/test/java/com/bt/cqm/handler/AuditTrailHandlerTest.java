package com.bt.cqm.handler;

import com.bt.rsqe.expedio.audit.AuditDetailDTO;
import com.bt.rsqe.expedio.audit.AuditSummaryDTO;
import com.bt.rsqe.expedio.audit.AuditTrailResource;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import static org.mockito.Mockito.*;

/**
 * Created with IntelliJ IDEA.
 * User: 608026723
 * Date: 3/30/15
 * Time: 8:36 PM
 * To change this template use File | Settings | File Templates.
 */
public class AuditTrailHandlerTest {

    AuditTrailHandler auditTrailHandler;

    @Mock
    AuditTrailResource auditTrailResourceMock;

    @Before
    public void setUp(){
        MockitoAnnotations.initMocks(this);
        auditTrailHandler = new AuditTrailHandler(auditTrailResourceMock);
    }

    @Test
    public void shouldGetQuoteAuditSummary(){
        List<AuditSummaryDTO> auditSummaryDTOs = new ArrayList<AuditSummaryDTO>();

        when(auditTrailResourceMock.getQuoteAuditSummary(anyString())).thenReturn(auditSummaryDTOs);
        Response response= auditTrailHandler.getQuoteAuditSummary("1001");
        assert (response.getStatus()==Response.Status.OK.getStatusCode());
    }

    @Test
    public void shouldGetQuoteAuditSummaryHandleInvalidInput(){
        List<AuditSummaryDTO> auditSummaryDTOs = new ArrayList<AuditSummaryDTO>();

        when(auditTrailResourceMock.getQuoteAuditSummary(anyString())).thenReturn(auditSummaryDTOs);
        Response response= auditTrailHandler.getQuoteAuditSummary("");
        assert (response.getStatus()==Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void shouldGetOrderAuditSummary(){
        List<AuditSummaryDTO> auditSummaryDTOs = new ArrayList<AuditSummaryDTO>();

        when(auditTrailResourceMock.getQuoteAuditSummary(anyString())).thenReturn(auditSummaryDTOs);
        Response response= auditTrailHandler.getOrderAuditSummary("1001");
        assert (response.getStatus()==Response.Status.OK.getStatusCode());
    }

    @Test
    public void shouldGetOrderAuditSummaryHandleInvalidInput(){
        List<AuditSummaryDTO> auditSummaryDTOs = new ArrayList<AuditSummaryDTO>();

        when(auditTrailResourceMock.getQuoteAuditSummary(anyString())).thenReturn(auditSummaryDTOs);
        Response response= auditTrailHandler.getOrderAuditSummary("");
        assert (response.getStatus()==Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void shouldGetQuoteAuditDetail(){
        List<AuditDetailDTO> quoteDetailList = new ArrayList<AuditDetailDTO>();

        when(auditTrailResourceMock.getOrderAuditDetail(anyString(),anyString())).thenReturn(quoteDetailList);

        Response response = auditTrailHandler.getQuoteAuditDetail("111","222");
        assert (response.getStatus()==Response.Status.OK.getStatusCode());
    }

    @Test
    public void shouldGetQuoteAuditDetailHandleInvalidInput(){
        List<AuditDetailDTO> quoteDetailList = new ArrayList<AuditDetailDTO>();

        when(auditTrailResourceMock.getOrderAuditDetail(anyString(),anyString())).thenReturn(quoteDetailList);

        Response response = auditTrailHandler.getQuoteAuditDetail("","222");
        assert (response.getStatus()==Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void shouldGetOrderAuditDetail(){
        List<AuditDetailDTO> quoteDetailList = new ArrayList<AuditDetailDTO>();

        when(auditTrailResourceMock.getOrderAuditDetail(anyString(),anyString())).thenReturn(quoteDetailList);

        Response response = auditTrailHandler.getOrderAuditDetail("111","222");
        assert (response.getStatus()==Response.Status.OK.getStatusCode());
    }

    @Test
    public void shouldGetOrderAuditDetailHandleInvalidInput(){
        List<AuditDetailDTO> quoteDetailList = new ArrayList<AuditDetailDTO>();

        when(auditTrailResourceMock.getOrderAuditDetail(anyString(),anyString())).thenReturn(quoteDetailList);

        Response response = auditTrailHandler.getOrderAuditDetail("","222");
        assert (response.getStatus()==Response.Status.BAD_REQUEST.getStatusCode());
    }
}
