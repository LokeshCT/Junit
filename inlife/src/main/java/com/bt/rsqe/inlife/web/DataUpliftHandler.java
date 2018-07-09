package com.bt.rsqe.inlife.web;

import com.bt.rsqe.customerinventory.client.ProductInstanceClient;
import com.bt.rsqe.customerinventory.parameter.LineItemId;
import com.bt.rsqe.customerinventory.parameter.ProjectId;
import com.bt.rsqe.customerinventory.parameter.QuoteOptionId;
import com.bt.rsqe.domain.AssetKey;
import com.bt.rsqe.domain.product.AssetChangeResponse;
import com.bt.rsqe.domain.product.InstanceCharacteristicChange;
import com.bt.rsqe.domain.product.ProductSCode;
import com.bt.rsqe.domain.project.ProductInstance;
import com.bt.rsqe.enums.AssetVersionStatus;
import com.bt.rsqe.logging.Log;
import com.bt.rsqe.logging.LogFactory;
import com.bt.rsqe.logging.LogLevel;
import com.bt.rsqe.projectengine.ProjectEngineClientResources;
import com.bt.rsqe.projectengine.QuoteOptionItemDTO;
import com.bt.rsqe.projectengine.web.view.PageView;
import com.bt.rsqe.utils.GsonUtil;
import com.bt.rsqe.web.Presenter;
import com.bt.rsqe.web.ViewFocusedResourceHandler;
import com.google.common.base.Function;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.joda.time.DateTime;

import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Date;
import java.util.List;

import static com.google.common.collect.Lists.*;
import static org.apache.commons.lang.StringUtils.*;

@Path("/rsqe/inlife/uplift")
public class DataUpliftHandler extends ViewFocusedResourceHandler {

    private static final Logger LOG = LogFactory.createDefaultLogger(Logger.class);

    private ProductInstanceClient instanceClient;
    private ProjectEngineClientResources projectEngineClientResources;

    public DataUpliftHandler(Presenter presenter, ProductInstanceClient instanceClient, ProjectEngineClientResources projectEngineClientResources) {
        super(presenter);
        this.instanceClient = instanceClient;
        this.projectEngineClientResources = projectEngineClientResources;
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response get() {
        final PageView view = new PageView("Data Uplift", "Data Uplift");
        String page = presenter.render(view("DataUplift.ftl")
                                           .withContext("view", view)
                                           .withContext("assetUpliftUrl", "/rsqe/inlife/uplift/assets/{assetId}/assetVersions/{assetVersion}")
                                           .withContext("lineItemUpliftUrl", "/rsqe/inlife/uplift/lineItems/{lineItemId}")
                                           .withContext("quoteOptionUpliftUrl", "/rsqe/inlife/uplift/projects/{projectId}/quoteOptions/{quoteOptionId}")
                                           .withContext("productAttributeUpliftUrl", "/rsqe/inlife/uplift/sCode/{sCode}?AttributeName={attName}")
        );
        return Response.ok(page).build();
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Path("assets/{assetId}/assetVersions/{assetVersion}")
    public Response upliftAsset(@PathParam("assetId") String assetId,
                                @PathParam("assetVersion") String assetVersion) {
        if (isEmpty(assetId)) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        LOG.upliftRequestReceived(assetId, assetVersion);
        ProductInstance productInstance = instanceClient.getByAssetKey(AssetKey.newInstance(assetId, Long.parseLong(assetVersion)));
        return Response.ok(toJson(upliftAsset(productInstance))).build();
    }


    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Path("lineItems/{lineItemId}")
    public Response upliftLineItem(@PathParam("lineItemId") String lineItemId) {
        if (isEmpty(lineItemId)) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        LOG.upliftRequestReceived(lineItemId);
        ProductInstance productInstance = instanceClient.get(new LineItemId(lineItemId));
        return Response.ok(toJson(upliftAsset(productInstance))).build();
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/projects/{projectId}/quoteOptions/{quoteOptionId}")
    public Response upliftQuoteOption(@PathParam("projectId") String projectId,
                                      @PathParam("quoteOptionId") String quoteOptionId) {
        if (isEmpty(quoteOptionId)) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        LOG.upliftRequestReceived(new ProjectId(projectId), new QuoteOptionId(quoteOptionId));
        List<QuoteOptionItemDTO> lineItems = projectEngineClientResources.quoteOptionResource(projectId)
                                                                         .quoteOptionItemResource(quoteOptionId)
                                                                         .get();

        List<AssetChangeResponse> assetChangeResponseList = newArrayList();
        for (QuoteOptionItemDTO lineItem : lineItems) {
            ProductInstance productInstance = instanceClient.get(new LineItemId(lineItem.id));
            assetChangeResponseList.addAll( upliftAsset(productInstance));
        }
        return Response.ok(toJson(assetChangeResponseList)).build();
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Path("sCode/{sCode}")
    public Response upliftProductAttribute(@PathParam("sCode") String sCode,
                                      @QueryParam("AttributeName") String attributeName) {
        if (isEmpty(sCode) || isEmpty(attributeName)) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        Date aMonthAgo = new DateTime().minusDays(30).toDate();
        List<AssetKey> assets = instanceClient.getAssetKeysByProduct(ProductSCode.newInstance(sCode), aMonthAgo, AssetVersionStatus.DRAFT.name());

        List<AssetChangeResponse> assetChangeResponseList = newArrayList();
        for (AssetKey assetKey : assets) {
            ProductInstance productInstance = instanceClient.getByAssetKey(assetKey);
            assetChangeResponseList.addAll( upliftAsset(productInstance));
        }
        return Response.ok(toJson(assetChangeResponseList)).build();
    }


    private List<AssetChangeResponse> upliftAsset(ProductInstance productInstance) {

        List<AssetChangeResponse> assetChangeResponseList = newArrayList();
        assetChangeResponseList.add(
            instanceClient.refreshProductInstance(productInstance)
        );
        for (ProductInstance childInstance : productInstance.getChildren()) {
            childInstance = instanceClient.getByAssetKey(childInstance.getKey());
            assetChangeResponseList.addAll(upliftAsset(childInstance));
        }
        return assetChangeResponseList;
    }

    private String toJson(List<AssetChangeResponse> assetChangeResponseList) {
        JsonArray jsonArray = GsonUtil.toJsonArray(assetChangeResponseList, new Function<AssetChangeResponse, JsonElement>() {
            @Override
            public JsonElement apply(AssetChangeResponse input) {
                return toJson(input);
            }
        });
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("upliftResult", jsonArray);
        String response = jsonObject.toString();
        LOG.upliftResponse(response);
        return response;
    }

    private JsonObject toJson(AssetChangeResponse assetChangeResponse) {
        JsonArray characteristicChanges = GsonUtil.toJsonArray(assetChangeResponse.getInstanceCharacteristicChanges(), toJson());
        JsonArray contributedChanges = GsonUtil.toJsonArray(assetChangeResponse.getContributedChanges().filterCharacteristicChanges(), toJson());
        JsonObject result = new JsonObject();

        result.add("attributes", characteristicChanges);
        result.add("contributesTo", contributedChanges);
        return result;
    }

    private Function<InstanceCharacteristicChange, JsonElement> toJson() {
        return new Function<InstanceCharacteristicChange, JsonElement>() {
            @Override
            public JsonElement apply(InstanceCharacteristicChange input) {
                return input.toJson();
            }
        };
    }

    private interface Logger {

        @Log(level = LogLevel.INFO, format = "Uplift Request for Line Item : %s")
        void upliftRequestReceived(String lineItemId);

        @Log(level = LogLevel.INFO, format = "Uplift Request for assetId[%s] version[%s]")
        void upliftRequestReceived(String assetId, String assetVerion);

        @Log(level = LogLevel.INFO, format = "Uplift Response : %s")
        void upliftResponse(String response);

        @Log(level = LogLevel.INFO, format = "Uplift Request for Project [%s] Quote Option [%s]")
        void upliftRequestReceived(ProjectId projectId, QuoteOptionId quoteOptionId);
    }
}
