package com.bt.cqm.handler;


import com.bt.rsqe.sqefacade.UserQuoteStatisticsResource;
import com.bt.rsqe.sqefacade.domain.QuoteStatusSummary;
import com.bt.rsqe.sqefacade.domain.UserQuoteStatistics;
import com.bt.rsqe.web.staticresources.UnableToLoadResourceException;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

import static com.bt.cqm.utils.Constants.*;

@Path("/cqm/user")
@Produces(MediaType.APPLICATION_JSON)
public class UserDashboardHandler {

    private UserQuoteStatisticsResource userQuoteStatisticsResource;

    public UserDashboardHandler(UserQuoteStatisticsResource userQuoteStatisticsResource) {
        this.userQuoteStatisticsResource = userQuoteStatisticsResource;
    }

    @GET
    @Path("recent-quotes/status")
    public Response getRecentQuoteStatusSummary(@HeaderParam(SM_USER) String userId) throws UnableToLoadResourceException {

        List<QuoteStatusSummary> recentQuoteStatus = userQuoteStatisticsResource.getRecentQuoteStatus(userId);
        return Response.ok().entity(recentQuoteStatus).build();
    }


    @GET
    @Path("quotes/status")
    public Response getAllQuotesStatusSummary(@HeaderParam(SM_USER) String userId) throws UnableToLoadResourceException {

        List<QuoteStatusSummary> userQuotesStatus = userQuoteStatisticsResource.getAllQuotesStatus(userId);
        return Response.ok().entity(userQuotesStatus).build();
    }


    @GET
    @Path("quotes/statistics")
    public Response getUserQuoteStatusSummary(@HeaderParam(SM_USER) String userId) throws UnableToLoadResourceException {

        UserQuoteStatistics userQuoteStatistics = userQuoteStatisticsResource.getUserQuoteStatistics(userId);
        return Response.ok(userQuoteStatistics).build();
    }
}
