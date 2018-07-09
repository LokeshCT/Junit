package com.bt.rsqe.inlife.web;


import com.bt.rsqe.logging.Log;
import com.bt.rsqe.logging.LogFactory;
import com.bt.rsqe.logging.LogLevel;
import com.bt.rsqe.mis.client.TransactionTargetResource;
import com.bt.rsqe.mis.client.dto.TransactionTargetDTO;
import com.bt.rsqe.projectengine.web.view.PageView;
import com.bt.rsqe.rest.ResponseBuilder;
import com.bt.rsqe.utils.GsonUtil;
import com.bt.rsqe.web.Presenter;
import com.bt.rsqe.web.ViewFocusedResourceHandler;
import com.google.common.base.Function;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

import static com.bt.rsqe.inlife.web.PageContext.*;
import static javax.ws.rs.core.Response.Status.*;


@Path("/rsqe/inlife/transaction-target")
@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
public class TransactionTargetResourceHandler extends ViewFocusedResourceHandler {

    private static final Logger LOG = LogFactory.createDefaultLogger(Logger.class);

    private TransactionTargetResource transactionTargetResource;
    private Gson gson = new Gson();

    public TransactionTargetResourceHandler(TransactionTargetResource transactionTargetResource, Presenter presenter) {
        super(presenter);
        this.transactionTargetResource = transactionTargetResource;
    }

    @GET
    @Path("home")
    @Produces(MediaType.TEXT_HTML)
    public Response getWebMetricsPage() {
        final PageView view = new PageView("SQE Web Analytics", "Index");
        return responseOk(presenter.render(view("WebMetrics.ftl")
                                               .withContext("view", view)
                                               .withContext("pageContext", pageContext()
                                                   .withNavigationListUri("/rsqe/inlife/web-metrics/navigations")
                                                   .withNavigationWebMetricsUri("/rsqe/inlife/web-metrics?navName={navName}&fromDate={fromDate}&toDate={toDate}")
                                                   .withExportRawDateUri("/rsqe/inlife/web-metrics/export?fromDate={fromDate}&toDate={toDate}"))));
    }

    @GET
    @Path("percentile")
    @Produces(MediaType.TEXT_HTML)
    public Response getWebMetricsPercentilePage() {
        final PageView view = new PageView("SQE Web Analytics", "Index");
        return responseOk(presenter.render(view("WebMetricsPercentile.ftl")
                                               .withContext("view", view)
                                               .withContext("pageContext", pageContext()
                                                   .withLocationListUri("/rsqe/inlife/web-metrics/locations")
                                                   .withCountryWisePercentageUri("/rsqe/inlife/web-metrics/countryWisePercentage")
                                                   .withPercentileWebMetricsUri("/rsqe/inlife/web-metrics/locationBasedPercentile?location={location}&fromDate={fromDate}&toDate={toDate}"))));
    }

    @GET
    @Path("configure")
    @Produces(MediaType.TEXT_HTML)
    public Response getTransactionConfigurationPage() {
        final PageView view = new PageView("SQE Web Analytics", "Index");
        return responseOk(presenter.render(view("KeyTransactionConfiguration.ftl")
                                               .withContext("view", view)
                                               .withContext("pageContext", pageContext()
                                                   .withTransactionTargetsUri("/rsqe/inlife/transaction-target/getTransactionTargets")
                                                   .withKeyTransactionTargetUri("/rsqe/inlife/transaction-target/saveTargets"))));
    }

    @POST
    public Response create(List<TransactionTargetDTO> transactionTargets) {
        try {
            String createdId = null;
            for (TransactionTargetDTO transactionTarget : transactionTargets) {
                createdId = transactionTargetResource.saveTargetsTransactions(transactionTarget);
            }
            return ResponseBuilder.anOKResponse().withStatus(CREATED).withEntity(createdId).build();
        } catch (Exception e) {
            LOG.error(e);
            return ResponseBuilder.notFound().build();
        }
    }


    @GET
    @Path("getTransactionTargets")
    public Response getTransactionTargets() throws ParseException {

        final List<TransactionTargetDTO> webMetrics = transactionTargetResource.getTransactionTargets();

        JsonArray jsonArray = GsonUtil.toJsonArray(webMetrics, new Function<TransactionTargetDTO, JsonElement>() {
            @Override
            public JsonElement apply(TransactionTargetDTO input) {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("id", input.getId());
                jsonObject.addProperty("name", input.getTransactionName());
                jsonObject.addProperty("target", input.getTarget());
                jsonObject.addProperty("createdBy", input.getCreatedBy());
                if(input.getCreatedDate() != null){
                    jsonObject.addProperty("createdDate", simpleDateFormat.format(input.getCreatedDate()));
                }
                return jsonObject;
            }
        });
        return ResponseBuilder.anOKResponse().withEntity(jsonArray.toString()).build();
    }

    @GET
    @Path("saveTargets")
    public Response saveTargets(@QueryParam("data") String targetDTO) throws ParseException {
        Type t = new TypeToken<List<TransactionTargetDTO>>() {
        }.getType();
        gson = (new GsonBuilder()).setDateFormat("yyyy-MM-dd").create();
        List<TransactionTargetDTO> transactionTargetDTOs = gson.fromJson(targetDTO, t);
        for (TransactionTargetDTO transaction : transactionTargetDTOs) {
            transactionTargetResource.saveTargetsTransactions(transaction);
        }
        return ResponseBuilder.anOKResponse().withStatus(Response.Status.CREATED).build();
    }

    interface Logger {
        @Log(level = LogLevel.ERROR, format = "Error : %s")
        void error(Exception e);
    }
}
