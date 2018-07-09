package com.bt.rsqe.inlife.web;

import com.bt.rsqe.customerinventory.client.ProductInstanceClient;
import com.bt.rsqe.customerinventory.parameter.LineItemId;
import com.bt.rsqe.domain.AssetKey;
import com.bt.rsqe.domain.product.ContributedChanges;
import com.bt.rsqe.domain.product.InstanceCharacteristicChange;
import com.bt.rsqe.domain.project.ProductInstance;
import com.bt.rsqe.projectengine.ProjectEngineClientResources;
import com.bt.rsqe.projectengine.QuoteOptionItemResource;
import com.bt.rsqe.projectengine.QuoteOptionResource;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.hamcrest.core.Is;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.Response;

import static com.bt.rsqe.domain.product.AssetChangeResponse.assetChangeResponse;
import static com.bt.rsqe.domain.product.DefaultProductInstanceFixture.aProductInstance;
import static com.bt.rsqe.domain.product.InstanceCharacteristicChange.newInstanceCharacteristicChange;
import static com.bt.rsqe.projectengine.QuoteOptionItemDTO.fromId;
import static com.google.common.collect.Lists.newArrayList;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class DataUpliftHandlerTest {


    private DataUpliftHandler dataUpliftHandler;
    private QuoteOptionResource quoteOptionResource;
    private ProductInstanceClient instanceClient;

    @Before
    public void setup() {

        ProductInstance grandChildInstance = aProductInstance().withProductInstanceId("grandChildAssetId").build();
        ProductInstance childInstance = aProductInstance().withProductInstanceId("childAssetId").withChildProductInstance(grandChildInstance, "relationToGrandChild").build();
        ProductInstance rootInstance = aProductInstance().withProductInstanceId("rootAssetId").withChildProductInstance(childInstance, "relationToChild").build();

        instanceClient = mock(ProductInstanceClient.class);
        when(instanceClient.refreshProductInstance(rootInstance)).thenReturn(assetChangeResponse().withInstanceCharacteristicChanges(newArrayList(
            characteristicChange("rootAssetId", "AttA", "oldValueForAttA", "newValueForAttA", 1),
            characteristicChange("rootAssetId", "AttB", "oldValueForAttB", "newValueForAttB", 1)
        )).withContributedChanges(new ContributedChanges(
            characteristicChange("relAsset1", "AttA1", "oldValueForAttA1", "newValueForAttA1", 2),
            characteristicChange("relAsset2", "AttA11", "oldValueForAttA11", "newValueForAttA1", 3),
            characteristicChange("relAsset3", "AttA2", "oldValueForAttA2", "newValueForAttA2", 2)
        )).build());
        when(instanceClient.refreshProductInstance(childInstance)).thenReturn(assetChangeResponse().withInstanceCharacteristicChanges(newArrayList(
            characteristicChange("childAssetId", "AttP", "oldValueForAttP", "newValueForAttP", 1),
            characteristicChange("childAssetId", "AttQ", "oldValueForAttQ", "newValueForAttQ", 1)
        )).build());
        when(instanceClient.refreshProductInstance(grandChildInstance)).thenReturn(assetChangeResponse().withInstanceCharacteristicChanges(newArrayList(
            characteristicChange("grandChildAsestId", "AttX", "oldValueForAttX", "newValueForAttX", 1),
            characteristicChange("grandChildAsestId", "AttY", "oldValueForAttY", "newValueForAttY", 1)
        )).build());

        ProjectEngineClientResources projectEngineClientResources = mock(ProjectEngineClientResources.class);
        dataUpliftHandler = new DataUpliftHandler(null, instanceClient, projectEngineClientResources);

        when(instanceClient.get(new LineItemId("lineItemId"))).thenReturn(rootInstance);
        when(instanceClient.get(new LineItemId("lineItemId2"))).thenReturn(rootInstance);
        when(instanceClient.getByAssetKey(new AssetKey("rootAssetId", 1L))).thenReturn(rootInstance);
        when(instanceClient.getByAssetKey(new AssetKey("childAssetId", 1L))).thenReturn(childInstance);
        when(instanceClient.getByAssetKey(new AssetKey("grandChildAssetId", 1L))).thenReturn(grandChildInstance);

        quoteOptionResource = mock(QuoteOptionResource.class);
        QuoteOptionItemResource quoteOptionItemResource = mock(QuoteOptionItemResource.class);
        when(projectEngineClientResources.quoteOptionResource("projectId")).thenReturn(quoteOptionResource);
        when(quoteOptionResource.quoteOptionItemResource("quoteOptionId")).thenReturn(quoteOptionItemResource);
        when(quoteOptionItemResource.get()).thenReturn(newArrayList(fromId("lineItemId"), fromId("lineItemId2")));
    }

    private InstanceCharacteristicChange characteristicChange(String assetId, String attName, String oldValue, String newValue, int depth) {
        return newInstanceCharacteristicChange()
            .forAttribute(attName)
            .forAssetKey(new AssetKey(assetId, 1L))
            .withOldValue(oldValue)
            .withNewValue(newValue)
            .withDepth(depth)
            .build();
    }

    @Test
    public void shouldUpliftLineItem() {
        Response response = dataUpliftHandler.upliftLineItem("lineItemId");
        JsonObject json = new JsonParser().parse(response.getEntity().toString()).getAsJsonObject();
        assertTrue(json.has("upliftResult"));
        assertThat(json.getAsJsonArray("upliftResult").size(), Is.is(3));
    }

    @Test
    public void shouldUpliftQuoteOption() {
        Response response = dataUpliftHandler.upliftQuoteOption("projectId", "quoteOptionId");
        JsonObject json = new JsonParser().parse(response.getEntity().toString()).getAsJsonObject();

        verify(quoteOptionResource, times(1)).quoteOptionItemResource("quoteOptionId");
        verify(instanceClient, times(2)).get(any(LineItemId.class));
        assertThat(json.getAsJsonArray("upliftResult").size(), Is.is(6));
    }

}
