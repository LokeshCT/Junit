package com.bt.rsqe.sqefacade;

import com.bt.rsqe.dto.QuoteStatus;
import com.bt.rsqe.sqefacade.domain.Ipcg2QuoteStatusSummary;
import com.bt.rsqe.sqefacade.domain.QuoteStatusSummary;
import com.bt.rsqe.sqefacade.domain.QuoteStatusSummaryResponse;
import com.bt.rsqe.sqefacade.domain.UserQuoteStatistics;
import com.bt.rsqe.sqefacade.domain.UserQuoteStatisticsResponse;
import com.bt.rsqe.web.rest.RestRequestBuilder;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

public class IPCG2UserQuoteStatisticsResource implements UserQuoteStatisticsResource {

    private final RestRequestBuilder restRequestBuilder;

    public IPCG2UserQuoteStatisticsResource(SqeIvpnFacadeConfig config) {
        this(new RestRequestBuilder(URI.create(config.getServiceEndPointConfig(SqeIvpnFacadeConfig.USER_QUOTE_STATISTICS).getUri())));
    }

    public IPCG2UserQuoteStatisticsResource(RestRequestBuilder restRequestBuilder) {
        this.restRequestBuilder = restRequestBuilder;
    }

    public List<QuoteStatusSummary> getRecentQuoteStatus(String userId) {
        List<Ipcg2QuoteStatusSummary> recentQuotes = restRequestBuilder.build(userId, "recent-quotes", "status").get().getEntity(QuoteStatusSummaryResponse.class).getData();
        QuoteStatusSummary[] quoteStatusSummaries = recentQuotes.toArray(new QuoteStatusSummary[recentQuotes.size()]);
        return Arrays.asList(quoteStatusSummaries);
    }


    public List<QuoteStatusSummary> getAllQuotesStatus(String userId) {
        List<Ipcg2QuoteStatusSummary> quotes = restRequestBuilder.build(userId, "quotes", "status").get().getEntity(QuoteStatusSummaryResponse.class).getData();
        QuoteStatusSummary[] quoteStatusSummaries = quotes.toArray(new QuoteStatusSummary[quotes.size()]);
        return Arrays.asList(quoteStatusSummaries);
    }


    public UserQuoteStatistics getUserQuoteStatistics(String userId) {
        return restRequestBuilder.build(userId, "quotes", "statistics").get().getEntity(UserQuoteStatisticsResponse.class).getData();
    }
}
