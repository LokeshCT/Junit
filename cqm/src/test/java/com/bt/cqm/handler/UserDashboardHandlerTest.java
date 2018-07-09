package com.bt.cqm.handler;

import com.bt.cqm.utils.Constants;
import com.bt.rsqe.ContainerUtils;
import com.bt.rsqe.container.Application;
import com.bt.rsqe.container.ApplicationConfig;
import com.bt.rsqe.container.StubApplicationConfig;
import com.bt.rsqe.sqefacade.UserQuoteStatisticsResource;
import com.bt.rsqe.sqefacade.domain.QuoteStatusSummary;
import com.bt.rsqe.sqefacade.domain.RAGStatus;
import com.bt.rsqe.sqefacade.domain.UserQuoteStatistics;
import com.bt.rsqe.web.rest.RestRequestBuilder;
import com.bt.rsqe.web.rest.RestResponse;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Calendar;

import static com.google.common.collect.Lists.*;
import static org.hamcrest.core.Is.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class UserDashboardHandlerTest {

    private static UserQuoteStatisticsResource userQuoteStatisticsResource = mock(UserQuoteStatisticsResource.class);
    private static Application application;
    private static RestRequestBuilder restRequestBuilder;

    @BeforeClass
    public static void setUp() throws Exception {
        ApplicationConfig config = StubApplicationConfig.defaultTestConfig();
        application = ContainerUtils.startContainer(config, new UserDashboardHandler(userQuoteStatisticsResource));
        restRequestBuilder = new RestRequestBuilder(config);
    }

    @Test
    public void shouldGetRecentQuoteStatusSummary() throws Exception {

        QuoteStatusSummary quoteStatusSummary = QuoteStatusSummary.newBuilder()
                .withQuoteName("Quote 1")
                .withLastAccessedOn(calendar(2016, 0, 21, 19, 57, 10))
                .withSiteCount(10)
                .withConfigStatus(RAGStatus.InProgress)
                .build();

        when(userQuoteStatisticsResource.getRecentQuoteStatus("userId")).thenReturn(newArrayList(quoteStatusSummary));

        RestResponse restResponse = restRequestBuilder.withHeader(Constants.SM_USER, "userId").build("cqm", "user", "recent-quotes", "status").get();

        JSONArray jsonArray = new JSONArray(restResponse.getEntityAsString());
        assertThat(jsonArray.length(), is(1));
        assertThat(jsonArray.getJSONObject(0).getString("quoteName"), is("Quote 1"));
        assertThat(jsonArray.getJSONObject(0).getInt("siteCount"), is(10));
        assertThat(jsonArray.getJSONObject(0).getString("configStatus"), is("InProgress"));
        assertThat(jsonArray.getJSONObject(0).getString("lastAccessedOn"), is("21 Jan 2016 19:57:10"));
    }

    private Calendar calendar(int year, int month, int day, int hour, int min, int sec) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day, hour, min, sec);
        return calendar;
    }


    @Test
    public void shouldGetUserQuoteStatusSummary() throws Exception {
        when(userQuoteStatisticsResource.getUserQuoteStatistics("userId")).thenReturn(new UserQuoteStatistics(2, 10, 100, 50));

        RestResponse restResponse = restRequestBuilder.withHeader(Constants.SM_USER, "userId").build("cqm", "user", "quotes", "statistics").get();
        JSONObject jsonObject = new JSONObject(restResponse.getEntityAsString());
        assertThat(jsonObject.getInt("numberOfCustomers"), is(2));
        assertThat(jsonObject.getInt("numberOfQuotes"), is(10));
        assertThat(jsonObject.getInt("numberOfSites"), is(100));
        assertThat(jsonObject.getInt("numberOfOrders"), is(50));
    }

    @AfterClass
    public static void tearDown() throws Exception {
        application.stop();
    }
}