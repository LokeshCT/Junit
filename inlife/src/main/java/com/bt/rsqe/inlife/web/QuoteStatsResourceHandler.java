package com.bt.rsqe.inlife.web;

import com.bt.rsqe.mis.client.QuoteItemStatsResource;
import com.bt.rsqe.mis.client.QuoteStatsResource;
import com.bt.rsqe.mis.client.TimeRange;
import com.bt.rsqe.mis.client.dto.QuoteItemStatsDTO;
import com.bt.rsqe.mis.client.dto.QuoteStatsSummaryDTO;
import com.bt.rsqe.utils.GsonUtil;
import com.google.common.base.Function;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

import static com.bt.rsqe.mis.client.TimeRange.*;

@Path("/rsqe/inlife/stats")
@Produces({MediaType.APPLICATION_JSON})
public class QuoteStatsResourceHandler {

    private QuoteStatsResource quoteStatsResource;
    private QuoteItemStatsResource quoteItemStatsResource;

    public QuoteStatsResourceHandler(QuoteStatsResource quoteStatsResource, QuoteItemStatsResource quoteItemStatsResource) {
        this.quoteStatsResource = quoteStatsResource;
        this.quoteItemStatsResource = quoteItemStatsResource;
    }

    @GET
    @Path("product/quote-stats-summary")
    public Response getQuoteStatsSummaryByProduct() {

        QuoteStatsSummaryDTO todayStats = quoteStatsResource.quoteStatsSummaryByProduct().today().get();
        QuoteStatsSummaryDTO yesterdayStats = quoteStatsResource.quoteStatsSummaryByProduct().yesterday().get();
        QuoteStatsSummaryDTO last7DayStats = quoteStatsResource.quoteStatsSummaryByProduct().lastWeek().get();
        QuoteStatsSummaryDTO last30DayStats = quoteStatsResource.quoteStatsSummaryByProduct().last30Days().get();
        QuoteStatsSummaryDTO last90DayStats = quoteStatsResource.quoteStatsSummaryByProduct().last90Days().get();
        QuoteStatsSummaryDTO total = quoteStatsResource.quoteStatsSummaryByProduct().get();

        JsonObject summary = new JsonObject();
        summary.add(Today.name(), todayStats.asJson());
        summary.add(Yesterday.name(), yesterdayStats.asJson());
        summary.add(Last7Days.name(), last7DayStats.asJson());
        summary.add(Last30Days.name(), last30DayStats.asJson());
        summary.add(Last90Days.name(), last90DayStats.asJson());
        summary.add(Total.name(), total.asJson());

        return Response.ok(summary.toString()).build();
    }

    @GET
    @Path("quote-item-stats")
    public Response getQuoteItemStats(@QueryParam("product") String product, @QueryParam("dateRange") TimeRange timeRange) {

        List<QuoteItemStatsDTO> quoteItemStats = quoteItemStatsResource.quoteItemStatus(product).forPeriod(timeRange).get();
        JsonArray jsonArray = GsonUtil.toJsonArray(quoteItemStats, new Function<QuoteItemStatsDTO, JsonElement>() {
            @Override
            public JsonElement apply(QuoteItemStatsDTO input) {
                return input.asJson();
            }
        });
        return Response.ok(jsonArray.toString()).build();
    }


}
