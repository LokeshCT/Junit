package com.bt.rsqe.customerinventory.service.handlers;

import com.bt.rsqe.customerinventory.dto.AssetDTO;
import com.bt.rsqe.customerinventory.fixtures.AssetDTOFixture;
import com.bt.rsqe.customerinventory.service.providers.AssetCandidateProvider;
import com.bt.rsqe.customerinventory.service.providers.AssetCandidateProviderFactory;
import com.bt.rsqe.domain.AssetKey;
import com.bt.rsqe.domain.product.parameters.RelationshipName;
import org.hamcrest.core.Is;
import org.junit.Test;

import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.Response;
import java.util.List;

import static com.google.common.collect.Lists.*;
import static org.hamcrest.MatcherAssert.*;
import static org.mockito.Mockito.*;

public class AssetCandidateHandlerTest {
    private static final RelationshipName RELATIONSHIP_NAME = RelationshipName.newInstance("aRelationshipName");
    private static final AssetKey ASSET_KEY = new AssetKey("anAssetId", 5);

    @Test
    public void shouldReturnChoosableCandidates() throws Exception {
        AssetCandidateProvider assetCandidateProvider = mock(AssetCandidateProvider.class);
        final List<AssetDTO> candidates = newArrayList(AssetDTOFixture.anAsset().withId("A1").build(),
                                                       AssetDTOFixture.anAsset().withId("A2").build());

        when(assetCandidateProvider.getChoosableCandidates(ASSET_KEY, RELATIONSHIP_NAME)).thenReturn(candidates);

        AssetCandidateProviderFactory assetCandidateProviderFactory = mock(AssetCandidateProviderFactory.class);
        when(assetCandidateProviderFactory.choosableProvider()).thenReturn(assetCandidateProvider);

        AssetCandidateHandler assetCandidateHandler = new AssetCandidateHandler(assetCandidateProviderFactory);

        Response response = assetCandidateHandler.getChoosableCandidates(RELATIONSHIP_NAME.value(), ASSET_KEY);

        assertThat(response.getStatus(), Is.is(200));
        List<AssetDTO> payload = (List<AssetDTO>)response.getEntity();

        assertThat(payload, Is.is(candidates));
    }
}
