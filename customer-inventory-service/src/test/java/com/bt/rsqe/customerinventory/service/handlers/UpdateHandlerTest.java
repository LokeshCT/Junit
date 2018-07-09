package com.bt.rsqe.customerinventory.service.handlers;

import com.bt.rsqe.customerinventory.parameter.ProductInstanceState;
import com.bt.rsqe.customerinventory.service.AssetSaveExceptionManager;
import com.bt.rsqe.customerinventory.service.client.domain.updates.CIFAssetUpdateRequest;
import com.bt.rsqe.customerinventory.service.client.domain.updates.CIFAssetUpdateRequestList;
import com.bt.rsqe.customerinventory.service.client.domain.updates.CIFAssetUpdateResponse;
import com.bt.rsqe.customerinventory.service.client.domain.updates.CreateRelationshipRequest;
import com.bt.rsqe.customerinventory.service.client.domain.updates.CreateRelationshipResponse;
import com.bt.rsqe.customerinventory.service.client.domain.updates.UpdateRequests;
import com.bt.rsqe.customerinventory.service.client.domain.updates.UserDetails;
import com.bt.rsqe.customerinventory.service.externals.QuoteEngineHelper;
import com.bt.rsqe.customerinventory.service.orchestrators.AssetUpdateOrchestrator;
import com.bt.rsqe.domain.AssetKey;
import com.bt.rsqe.domain.product.parameters.RelationshipType;
import org.hamcrest.core.Is;
import org.junit.Test;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class UpdateHandlerTest {
    @Test
    public void shouldCallOrchestratorAndWrapResponse() {
        AssetKey assetKey = new AssetKey("assetId", 2);
        CreateRelationshipRequest update1 = new CreateRelationshipRequest("c1", assetKey, "rel1", "pCode", "",
                                                               "siteId", "alternateCity", "lineItemId", 2);
        CreateRelationshipRequest update2 = new CreateRelationshipRequest("c1", assetKey, "rel1", "pCode", "",
                                                               "siteId", "alternateCity", "lineItemId", 2);
        CIFAssetUpdateResponse update3 = new CreateRelationshipResponse(update1, "relationshipName", RelationshipType.Child,
                                                                        ProductInstanceState.LIVE, new AssetKey("id", 1l),
                                                                        new ArrayList<CIFAssetUpdateRequest>(), "");
        CIFAssetUpdateResponse update4 = new CreateRelationshipResponse(update2, "relationshipName", RelationshipType.Child,
                                                                        ProductInstanceState.LIVE, new AssetKey("id", 1l),
                                                                        new ArrayList<CIFAssetUpdateRequest>(), "");

        CIFAssetUpdateRequestList sentUpdates = CIFAssetUpdateRequestList.fromList(newArrayList((CIFAssetUpdateRequest) update1, update2));
        List<CIFAssetUpdateResponse> expectedUpdates = newArrayList(update3, update4);

        AssetUpdateOrchestrator assetUpdateOrchestrator = mock(AssetUpdateOrchestrator.class);
        when(assetUpdateOrchestrator.update(sentUpdates)).thenReturn(expectedUpdates);
        QuoteEngineHelper quoteEngineHelper = mock(QuoteEngineHelper.class);

        final Response response = new UpdateHandler(assetUpdateOrchestrator, new AssetSaveExceptionManager(quoteEngineHelper)).performUpdates(new UpdateRequests(sentUpdates, new UserDetails(), "aProjectId", "aQuoteOptionId"));
        assertThat(response.getStatus(), Is.is(Response.Status.OK.getStatusCode()));
        @SuppressWarnings("unchecked")
        List<CIFAssetUpdateResponse> responseEntity = (List<CIFAssetUpdateResponse>)response.getEntity();
        assertThat(responseEntity, Is.is(expectedUpdates));
    }
}