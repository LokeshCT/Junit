package com.bt.rsqe.projectengine.web;

import com.bt.rsqe.customerinventory.client.ProductInstanceClient;
import com.bt.rsqe.customerinventory.client.ScopePricing;
import com.bt.rsqe.customerinventory.client.ScopePricingItem;
import com.bt.rsqe.customerinventory.client.ScopePricingItemError;
import com.bt.rsqe.customerinventory.client.ScopePricingStatus;
import com.bt.rsqe.security.UserContextManager;
import com.bt.rsqe.utils.GsonUtil;
import com.bt.rsqe.web.rest.exception.ResourceNotFoundException;
import com.google.common.base.Function;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.codehaus.jettison.json.JSONException;

import javax.annotation.Nullable;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

@Path("/rsqe/customers/{customerId}/contracts/{contractId}/projects/{projectId}/quote-options/{quoteOptionId}/line-item-prices")
public class PriceHandler {

    private final ProductInstanceClient futureProductInstanceClient;
    private final PriceHandlerProcessor priceHandlerProcessor;

    public PriceHandler(ProductInstanceClient futureProductInstanceClient, PriceHandlerProcessor priceHandlerProcessor) {
        this.futureProductInstanceClient = futureProductInstanceClient;
        this.priceHandlerProcessor = priceHandlerProcessor;
    }

    @GET
    @Path("{lineItems}")
    public Response getPrices(@PathParam("customerId") String customerId,
                              @PathParam("contractId") String contractId,
                              @PathParam("projectId") String projectId,
                              @PathParam("quoteOptionId") String quoteOptionId,
                              @PathParam("lineItems") String lineItems) throws JSONException {

        ScopePricing scopePricing;
        try {
            scopePricing = futureProductInstanceClient.getScopePricing(lineItems);
        } catch (ResourceNotFoundException ex) {
            futureProductInstanceClient.createScopePricing(lineItems);
            scopePricing = futureProductInstanceClient.getScopePricing(lineItems);
            priceHandlerProcessor.startPricing(lineItems, customerId, projectId, quoteOptionId,
                                                                   UserContextManager.getCurrent().getPermissions().indirectUser,
                                                                   UserContextManager.getCurrent().getRsqeToken());
        }
        JsonObject responseJson = new JsonObject();
        if (scopePricing.getStatus().equals(ScopePricingStatus.PROCESSING)) {
            responseJson.addProperty("done", false);
            return Response.ok(responseJson.toString()).build();
        } else if (scopePricing.getStatus().equals(ScopePricingStatus.ERROR)) {
            futureProductInstanceClient.deleteScopePricing(lineItems);
            return Response.serverError().entity(scopePricing.getError()).build();
        } else {
            futureProductInstanceClient.deleteScopePricing(lineItems);
            responseJson.addProperty("done", true);
            JsonArray response = GsonUtil.toJsonArray(scopePricing.getItems(), new Function<ScopePricingItem, JsonElement>() {
                @Override
                public JsonElement apply(@Nullable ScopePricingItem input) {
                    JsonObject jsonObject = new JsonObject();
                    jsonObject.addProperty("lineItemId", input.getLineItemId());
                    jsonObject.addProperty("status", input.getStatus());
                    JsonArray lineItemErrors = GsonUtil.toJsonArray(input.getErrors(), new Function<ScopePricingItemError, JsonElement>() {
                        @Override
                        public JsonElement apply(ScopePricingItemError input) {
                            JsonObject errorJson = new JsonObject();
                            errorJson.addProperty("lineItemId", input.getLineItemId());
                            errorJson.addProperty("error", input.getError());
                            return errorJson;
                        }
                    });
                    jsonObject.add("errors", lineItemErrors);
                    return jsonObject;
                }
            });

            responseJson.add("response", response);
            return Response.ok(responseJson.toString()).build();
        }
    }

}