package com.bt.rsqe.sqefacade;

import com.bt.rsqe.sqefacade.domain.QuoteStatusSummary;
import com.bt.rsqe.sqefacade.domain.UserQuoteStatistics;

import java.util.List;

public interface UserQuoteStatisticsResource {

    List<QuoteStatusSummary> getRecentQuoteStatus(String userId);

    List<QuoteStatusSummary> getAllQuotesStatus(String userId);

    UserQuoteStatistics getUserQuoteStatistics(String userId);
}
