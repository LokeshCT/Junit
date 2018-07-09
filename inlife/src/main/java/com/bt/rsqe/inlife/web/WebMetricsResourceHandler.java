package com.bt.rsqe.inlife.web;

import com.bt.rsqe.inlife.geo.locator.GeoLocator;
import com.bt.rsqe.inlife.web.export.WebMetricToExcelConverter;
import com.bt.rsqe.logging.Log;
import com.bt.rsqe.logging.LogFactory;
import com.bt.rsqe.logging.LogLevel;
import com.bt.rsqe.mis.client.WebMetricsResource;
import com.bt.rsqe.mis.client.dto.LocationName;
import com.bt.rsqe.mis.client.dto.NavigationMetricsDTO;
import com.bt.rsqe.mis.client.dto.NavigationName;
import com.bt.rsqe.mis.client.dto.NavigationPercentageMetricsDTO;
import com.bt.rsqe.mis.client.dto.PercentileDTO;
import com.bt.rsqe.monitoring.WebMetricsDTO;
import com.bt.rsqe.projectengine.web.quoteoptionorders.rfosheet.ExcelWorkbook;
import com.bt.rsqe.projectengine.web.view.PageView;
import com.bt.rsqe.rest.ResponseBuilder;
import com.bt.rsqe.utils.GsonUtil;
import com.bt.rsqe.web.Presenter;
import com.bt.rsqe.web.ViewFocusedResourceHandler;
import com.google.common.base.Function;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.IOException;
import java.io.OutputStream;
import java.text.ParseException;
import java.util.List;

import static com.bt.rsqe.inlife.web.PageContext.*;
import static com.bt.rsqe.mis.client.WebMetricsResource.*;
import static javax.ws.rs.core.Response.Status.*;
import static org.apache.commons.lang.StringUtils.*;


@Path("/rsqe/inlife/web-metrics")
@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
public class WebMetricsResourceHandler extends ViewFocusedResourceHandler {

    private static final Logger LOG = LogFactory.createDefaultLogger(Logger.class);
    private static final String FROM_DATE = "fromDate";
    private static final String TO_DATE = "toDate";
    private static final String LOCATION = "location";
    private static final String NAVIGATION = "navigation";

    private WebMetricsResource webMetricsResource;
    private Gson gson = new Gson();
    private GeoLocator geoLocator;

    public WebMetricsResourceHandler(WebMetricsResource webMetricsResource, Presenter presenter, GeoLocator geoLocator) {
        super(presenter);
        this.webMetricsResource = webMetricsResource;
        this.geoLocator = geoLocator;
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
                                                   .withNavigationListUri("/rsqe/inlife/web-metrics/navigations")
                                                   .withCountryWisePercentageUri("/rsqe/inlife/web-metrics/countryWisePercentage?location={location}&navigation={navigation}&fromDate={fromDate}&toDate={toDate}")
                                                   .withPercentileWebMetricsUri("/rsqe/inlife/web-metrics/locationBasedPercentile?location={location}&navigation={navigation}&fromDate={fromDate}&toDate={toDate}"))));
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
    public Response create(WebMetricsDTO webMetrics) {
        try {
            if (isEmpty(webMetrics.getClientDetail().getLocation())) {
                String clientLocation = isNotEmpty(webMetrics.getEin()) ? geoLocator.locateCountry(webMetrics.getEin()) : GeoLocator.UNKNOWN_COUNTRY;
                webMetrics.getClientDetail().setLocation(clientLocation);
            }
            String createdId = webMetricsResource.post(webMetrics);
            return Response.status(CREATED).entity(createdId).build();
        } catch (Exception e) {
            LOG.error(e);
            return Response.status(INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    @GET
    @Path("navigations")
    public Response getNavigations() {

        JsonArray jsonArray = GsonUtil.toJsonArray(webMetricsResource.getNavigationList(), new Function<NavigationName, JsonElement>() {
            @Override
            public JsonElement apply(NavigationName input) {
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("name", input.getName());
                return jsonObject;
            }
        });
        return Response.ok(jsonArray.toString()).build();
    }

    @GET
    public Response getLocationBasedMetrics(@QueryParam("navName") String navName,
                                            @QueryParam(FROM_DATE) String fromDate,
                                            @QueryParam(TO_DATE) String toDate) {
        if (isEmpty(navName) || isEmpty(fromDate) || isEmpty(toDate)) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        try {
            NavigationMetricsDTO locationBasedMetrics = webMetricsResource.getLocationBasedMetrics(navName, DATE_FORMAT.parse(fromDate), DATE_FORMAT.parse(toDate));

            return Response.ok(
                gson.toJson(locationBasedMetrics)
            ).build();

        } catch (ParseException e) {
            LOG.error(e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }


    @GET
    @Path("export")
    @Produces({"application/vnd.ms-excel"})
    public Response getMetricsList(@QueryParam(FROM_DATE) String fromDate,
                                   @QueryParam(TO_DATE) String toDate) throws ParseException {

        if (isEmpty(fromDate) || isEmpty(toDate)) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        final List<WebMetricsDTO> webMetrics = webMetricsResource.getWebMetrics(DATE_FORMAT.parse(fromDate), DATE_FORMAT.parse(toDate));
        final ExcelWorkbook workbook = new WebMetricToExcelConverter().convert(fromDate, toDate, webMetrics);

        final StreamingOutput streamingOutput = new StreamingOutput() {
            @Override
            public void write(OutputStream output) throws IOException, WebApplicationException {
                workbook.getFile().write(output);
            }
        };
        return Response.ok(streamingOutput).header("Content-Disposition", "attachment; filename=" + workbook.getName()).build();
    }

    @GET
    @Path("locations")
    public Response getLocations() {
        List<LocationName> locationsWithName = webMetricsResource.getLocationList();
        GenericEntity<List<LocationName>> locationEntity = new GenericEntity<List<LocationName>>(locationsWithName) {
        };
        return ResponseBuilder.anOKResponse().withEntity(locationEntity).build();
    }

    @GET
    @Path("locationBasedPercentile")
    public Response getLocationBasedPercentileMetrics(@QueryParam(LOCATION) String location,
                                                      @QueryParam(NAVIGATION) String navigation,
                                                      @QueryParam(FROM_DATE) String fromDate,
                                                      @QueryParam(TO_DATE) String toDate) {
        if (isEmpty(fromDate) || isEmpty(toDate)) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        try {
            PercentileDTO locationPercentile = webMetricsResource.getLocationBasedPercentile(location, navigation, DATE_FORMAT.parse(fromDate), DATE_FORMAT.parse(toDate));
            GenericEntity<PercentileDTO> percentileEntity = new GenericEntity<PercentileDTO>(locationPercentile) {
            };
            return ResponseBuilder.anOKResponse().withEntity(percentileEntity).build();
        } catch (Exception e) {
            LOG.error(e);
            return ResponseBuilder.notFound().build();
        }
    }

    @GET
    @Path("countryWisePercentage")
    public Response getCountryWisePercentageMetrics(@QueryParam(LOCATION) String location,
                                                    @QueryParam(NAVIGATION) String navigation,
                                                    @QueryParam(FROM_DATE) String fromDate,
                                                    @QueryParam(TO_DATE) String toDate) {
        if (isEmpty(fromDate) || isEmpty(toDate)) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        try {
            List<NavigationPercentageMetricsDTO> percentageMetricsList = webMetricsResource.getLocationBasedPercentage(location, navigation, DATE_FORMAT.parse(fromDate), DATE_FORMAT.parse(toDate));
            GenericEntity<List<NavigationPercentageMetricsDTO>> percentageEntity = new GenericEntity<List<NavigationPercentageMetricsDTO>>(percentageMetricsList) {
            };
            return ResponseBuilder.anOKResponse().withEntity(percentageEntity).build();

        } catch (Exception e) {
            LOG.error(e);
            return ResponseBuilder.notFound().build();
        }
    }


    interface Logger {
        @Log(level = LogLevel.ERROR, format = "Error : %s")
        void error(Exception e);
    }
}
