package com.bt.rsqe.inlife.handler;

import com.bt.rsqe.inlife.web.QuoteStatsResourceHandler;
import com.bt.rsqe.mis.client.DateFilter;
import com.bt.rsqe.mis.client.QuoteStatsResource;
import com.bt.rsqe.mis.client.dto.QuoteStatsSummaryDTO;
import com.google.common.collect.Lists;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.hamcrest.core.Is;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.Response;

import java.util.Calendar;
import java.util.Date;

import static com.google.common.collect.Lists.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class QuoteStatsResourceHandlerTest {

    private QuoteStatsResource quoteStatsResource = mock(QuoteStatsResource.class);
    private DateFilter<QuoteStatsSummaryDTO> dateFilter = mock(DateFilter.class);
    private DateFilter<QuoteStatsSummaryDTO> todayFilter = mock(DateFilter.class);
    private DateFilter<QuoteStatsSummaryDTO> yesterdayResult = mock(DateFilter.class);
    private DateFilter<QuoteStatsSummaryDTO> last7dayResult = mock(DateFilter.class);
    private DateFilter<QuoteStatsSummaryDTO> last30dayResult = mock(DateFilter.class);
    private DateFilter<QuoteStatsSummaryDTO> last90dayResult = mock(DateFilter.class);
    private Date today = Calendar.getInstance().getTime();
    private Date yesterday = new DateTime(today).minusDays(1).toDate();
    private Date weekAgo = new DateTime(yesterday).minusDays(7).toDate();
    private Date monthAgo = new DateTime(yesterday).minusDays(30).toDate();
    private Date threeMonthsAgo = new DateTime(yesterday).minusDays(90).toDate();

    QuoteStatsSummaryDTO todayStats = new QuoteStatsSummaryDTO(null, today, Lists.<QuoteStatsSummaryDTO.GroupedStats>newArrayList());
    QuoteStatsSummaryDTO yesterdayStats = new QuoteStatsSummaryDTO(null, yesterday, Lists.<QuoteStatsSummaryDTO.GroupedStats>newArrayList());
    QuoteStatsSummaryDTO last7dayStats = new QuoteStatsSummaryDTO(weekAgo, yesterday, newArrayList(new QuoteStatsSummaryDTO.GroupedStats("s1", 1, 2)));
    QuoteStatsSummaryDTO last30dayStats = new QuoteStatsSummaryDTO(monthAgo, yesterday, newArrayList(new QuoteStatsSummaryDTO.GroupedStats("s1", 1, 2), new QuoteStatsSummaryDTO.GroupedStats("s2", 5, 10)));
    QuoteStatsSummaryDTO last90dayStats = new QuoteStatsSummaryDTO(threeMonthsAgo, yesterday, newArrayList(new QuoteStatsSummaryDTO.GroupedStats("s1", 1, 2), new QuoteStatsSummaryDTO.GroupedStats("s2", 5, 10)));
    QuoteStatsSummaryDTO totalStats = new QuoteStatsSummaryDTO(newArrayList(new QuoteStatsSummaryDTO.GroupedStats("s1", 1, 2),
                                                                            new QuoteStatsSummaryDTO.GroupedStats("s2", 5, 10),
                                                                            new QuoteStatsSummaryDTO.GroupedStats("s3", 20, 100)));

    @Before
    public void setup() {

        when(quoteStatsResource.quoteStatsSummaryByProduct()).thenReturn(dateFilter);
        when(dateFilter.today()).thenReturn(todayFilter);
        when(dateFilter.yesterday()).thenReturn(yesterdayResult);
        when(dateFilter.lastWeek()).thenReturn(last7dayResult);
        when(dateFilter.last30Days()).thenReturn(last30dayResult);
        when(dateFilter.last90Days()).thenReturn(last90dayResult);

        when(todayFilter.get()).thenReturn(todayStats);
        when(yesterdayResult.get()).thenReturn(yesterdayStats);
        when(last7dayResult.get()).thenReturn(last7dayStats);
        when(last30dayResult.get()).thenReturn(last30dayStats);
        when(last90dayResult.get()).thenReturn(last90dayStats);
        when(dateFilter.get()).thenReturn(totalStats);
    }

    @Test
    public void shouldGetQuoteSummaryByProduct() {

        Response response = new QuoteStatsResourceHandler(quoteStatsResource, null).getQuoteStatsSummaryByProduct();
        String responseString = response.getEntity().toString();
        JsonObject jsonObject = new JsonParser().parse(responseString).getAsJsonObject();

        assertThat(jsonObject.getAsJsonObject("Today").getAsJsonArray("stats").size(), Is.is(0));
        assertThat(jsonObject.getAsJsonObject("Yesterday").getAsJsonArray("stats").size(), Is.is(0));
        assertThat(jsonObject.getAsJsonObject("Last7Days").getAsJsonArray("stats").size(), Is.is(1));
        assertThat(jsonObject.getAsJsonObject("Last30Days").getAsJsonArray("stats").size(), Is.is(2));
        assertThat(jsonObject.getAsJsonObject("Last90Days").getAsJsonArray("stats").size(), Is.is(2));
        assertThat(jsonObject.getAsJsonObject("Total").getAsJsonArray("stats").size(), Is.is(3));
    }


}
