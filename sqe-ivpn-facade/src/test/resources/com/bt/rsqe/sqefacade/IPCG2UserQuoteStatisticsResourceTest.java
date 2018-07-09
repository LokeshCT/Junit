package com.bt.rsqe.sqefacade;

import com.bt.rsqe.ContainerUtils;
import com.bt.rsqe.container.Application;
import com.bt.rsqe.container.ApplicationConfig;
import com.bt.rsqe.container.StubApplicationConfig;
import com.bt.rsqe.sqefacade.domain.QuoteStatusSummary;
import com.bt.rsqe.sqefacade.domain.RAGStatus;
import com.bt.rsqe.sqefacade.domain.UserQuoteStatistics;
import com.bt.rsqe.web.rest.RestRequestBuilder;
import org.hamcrest.core.Is;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.Calendar;
import java.util.List;

import static org.junit.Assert.*;

public class IPCG2UserQuoteStatisticsResourceTest {

    private static Application application;
    private static ApplicationConfig config;
    private IPCG2UserQuoteStatisticsResource IPCG2UserQuoteStatisticsResource;


    @BeforeClass
    public static void beforeClass() throws IOException {
        config = StubApplicationConfig.defaultTestConfig();
        application = ContainerUtils.startContainer(config, new TestHandler());
    }

    @Before
    public void setUp() throws Exception {
        RestRequestBuilder restRequestBuilder = new RestRequestBuilder(config);
        IPCG2UserQuoteStatisticsResource = new IPCG2UserQuoteStatisticsResource(restRequestBuilder);
    }


    @Test
    public void testGetRecentQuoteStatus() throws Exception {
        List<QuoteStatusSummary> recentQuoteStatus = IPCG2UserQuoteStatisticsResource.getRecentQuoteStatus("userId");
        assertThat(recentQuoteStatus.size(), Is.is(1));
        assertThat(recentQuoteStatus.get(0).getSiteCount(), Is.is(10));
        assertThat(recentQuoteStatus.get(0).getConfigStatus(), Is.is(RAGStatus.InProgress));
        assertThat(recentQuoteStatus.get(0).getProduct(), Is.is("IPCG2"));
        assertThat(recentQuoteStatus.get(0).getLastAccessedOn().get(Calendar.YEAR), Is.is(2016));
    }


    @Test
    public void testGetQuoteStatus() throws Exception {
        List<QuoteStatusSummary> recentQuoteStatus = IPCG2UserQuoteStatisticsResource.getAllQuotesStatus("userId");
        assertThat(recentQuoteStatus.size(), Is.is(2));
    }


    @Test
    public void testGetUserQuoteStatistics() throws Exception {
        UserQuoteStatistics userQuoteStatistics = IPCG2UserQuoteStatisticsResource.getUserQuoteStatistics("userId");
        assertThat(userQuoteStatistics, Is.is( new UserQuoteStatistics(1, 1, 10, 5)));
    }


    @AfterClass
    public static void afterClass() throws IOException {
        application.stop();
    }


    @Path("/")
    public static class TestHandler {

        @GET
        @Path("{userId}/recent-quotes/status")
        public Response getRecentQuoteStatus(@PathParam("userId") String userId) {
            String response = "{\"message\": \"Successful\", \"data\": [ { \"siteCount\" : 10, \"lastAccessedOn\": \"11 Jan 2016 12:00:00\", \"configStatus\": \"InProgress\"}]}";
            return Response.ok(response).build();
        }

        @GET
        @Path("{userId}/quotes/status")
        public Response getAllUserQuotes(@PathParam("userId") String userId) {
            String response = "{\"message\": \"Successful\", \"success\": true, \"data\": [ { \"quoteName\" : \"Quote 1\" }, { \"quoteName\" : \"Quote 2\" } ] }";
            System.out.println(response);
            return Response.ok(response).build();
        }

        @GET
        @Path("{userId}/quotes/statistics")
        public Response getUserQuoteStatistics(@PathParam("userId") String userId) {
            String response = "{\"success\": true, \"data\": { \"numberOfCustomers\" : 1, \"numberOfQuotes\" : 1, \"numberOfSites\" : 10, \"numberOfOrders\" : 5}}";
            return Response.ok(response).build();
        }

    }
}