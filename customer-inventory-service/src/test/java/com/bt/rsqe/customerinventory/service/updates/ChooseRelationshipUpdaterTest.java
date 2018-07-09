package com.bt.rsqe.customerinventory.service.updates;

import com.bt.rsqe.customerinventory.dto.AssetDTO;
import com.bt.rsqe.customerinventory.dto.ExternalIdentifierDTO;
import com.bt.rsqe.customerinventory.fixtures.AssetDTOFixture;
import com.bt.rsqe.customerinventory.parameter.ProductInstanceState;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAsset;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetKey;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetRelationship;
import com.bt.rsqe.customerinventory.service.client.domain.updates.CIFAssetUpdateRequest;
import com.bt.rsqe.customerinventory.service.client.domain.updates.CharacteristicReloadRequest;
import com.bt.rsqe.customerinventory.service.client.domain.updates.ChooseRelationshipRequest;
import com.bt.rsqe.customerinventory.service.client.domain.updates.ChooseRelationshipResponse;
import com.bt.rsqe.customerinventory.service.client.domain.updates.UpdateRequestSource;
import com.bt.rsqe.customerinventory.service.client.fixtures.CIFAssetFixture;
import com.bt.rsqe.customerinventory.service.client.resource.CIFAssetExtension;
import com.bt.rsqe.customerinventory.service.externals.PmrHelper;
import com.bt.rsqe.customerinventory.service.orchestrators.CIFAssetOrchestrator;
import com.bt.rsqe.customerinventory.service.providers.AssetCandidateProvider;
import com.bt.rsqe.domain.AssetKey;
import com.bt.rsqe.domain.bom.fixtures.ProductOfferingFixture;
import com.bt.rsqe.domain.product.parameters.ProductIdentifier;
import com.bt.rsqe.domain.product.parameters.RelationshipName;
import com.bt.rsqe.domain.product.parameters.RelationshipType;
import com.bt.rsqe.enums.AssetType;
import com.bt.rsqe.util.TestWithRules;
import com.google.common.collect.Sets;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.ArrayList;
import java.util.Collections;

import static com.bt.rsqe.customerinventory.service.client.domain.updates.UpdateRequestSource.*;
import static com.bt.rsqe.customerinventory.service.client.resource.CIFAssetExtension.*;
import static com.bt.rsqe.domain.product.parameters.RelationshipName.*;
import static com.bt.rsqe.enums.IdentifierType.*;
import static com.google.common.collect.Lists.*;
import static org.hamcrest.core.Is.*;
import static org.junit.Assert.*;
import static org.junit.matchers.JUnitMatchers.*;
import static org.mockito.Mockito.*;

public class ChooseRelationshipUpdaterTest extends TestWithRules {
    private static final AssetKey OWNER_ASSET_KEY = new AssetKey("assetId", 2);
    private static final AssetKey RELATED_ASSET_KEY = new AssetKey("assetId2", 3);
    public static final String RELATED_TO_RELATIONSHIP_NAME = "relatedToRelationshipName";
    public static final String CHILD_RELATIONSHIP_NAME = "childRelationshipName";
    public static final String OWNER_SITE_ID = "20";
    private InvalidatePriceRequestBuilder invalidatePriceRequestBuilder ;
    private DependentUpdateBuilderFactory dependentUpdateBuilderFactory ;
    private ContributesToChangeRequestBuilder contributesToChangeRequestBuilder ;
    private CIFAssetOrchestrator assetOrchestrator;
    public static final String LINE_ITEM_ID = "lineItemId";
    public static final int LOCK_VERSION = 3;
    private CIFAssetRelationship expectedRelatedToRelationship;
    private CIFAssetRelationship expectedChildRelationship;
    private AssetCandidateProvider assetCandidateProvider;
    private PmrHelper pmrHelper;
    private CIFAsset relatedAsset;


    @Before
    public void setUp() throws Exception {
        invalidatePriceRequestBuilder = mock(InvalidatePriceRequestBuilder.class) ;
        contributesToChangeRequestBuilder = mock(ContributesToChangeRequestBuilder.class) ;
        dependentUpdateBuilderFactory = new DependentUpdateBuilderFactoryBuilder().with(invalidatePriceRequestBuilder).with(contributesToChangeRequestBuilder).build();

        assetOrchestrator = mock(CIFAssetOrchestrator.class);
        assetCandidateProvider = mock(AssetCandidateProvider.class);
        pmrHelper = mock(PmrHelper.class);

        CIFAsset ownerAsset = CIFAssetFixture.aCIFAsset()
                                             .withID("assetId")
                                             .withSiteId(OWNER_SITE_ID)
                                             .build();
        relatedAsset = CIFAssetFixture.aCIFAsset()
                                               .withID("assetId2")
                                               .withSiteId(OWNER_SITE_ID)
                                               .build();
        expectedRelatedToRelationship = new CIFAssetRelationship(relatedAsset, RELATED_TO_RELATIONSHIP_NAME, RelationshipType.RelatedTo, ProductInstanceState.LIVE);
        expectedChildRelationship = new CIFAssetRelationship(relatedAsset, CHILD_RELATIONSHIP_NAME, RelationshipType.Child, ProductInstanceState.LIVE);
        CIFAssetRelationship expectedNullRelationship = new CIFAssetRelationship(relatedAsset, null, RelationshipType.Child, ProductInstanceState.LIVE);

        when(assetOrchestrator.getAsset(new CIFAssetKey(OWNER_ASSET_KEY, newArrayList(CIFAssetExtension.ProductOfferingRelationshipDetail,
                                                                                CIFAssetExtension.QuoteOptionItemDetail))))
            .thenReturn(ownerAsset);
        when(assetOrchestrator.getAsset(new CIFAssetKey(RELATED_ASSET_KEY, newArrayList(CIFAssetExtension.ProductOfferingRelationshipDetail,
                CIFAssetExtension.QuoteOptionItemDetail))))
            .thenReturn(relatedAsset);


        when(assetOrchestrator.relateAssets(ownerAsset, relatedAsset, RELATED_TO_RELATIONSHIP_NAME)).thenReturn(expectedRelatedToRelationship);
        when(assetOrchestrator.relateAssets(ownerAsset, relatedAsset, CHILD_RELATIONSHIP_NAME)).thenReturn(expectedChildRelationship);
        when(assetOrchestrator.relateAssets(ownerAsset, relatedAsset, null)).thenReturn(expectedNullRelationship);

        when(contributesToChangeRequestBuilder.buildRequests(relatedAsset.getAssetKey(), relatedAsset.getProductCode(), Collections.<String>emptyList(), 1))
                .thenReturn(Collections.<CIFAssetUpdateRequest>emptySet());
    }

    @Test
    public void shouldReturnChooseRelationshipResponseFromAssetOrchestratorResponseForRelatedToRelationship() {
        ChooseRelationshipRequest chooseRelationshipRequest =  new ChooseRelationshipRequest(OWNER_ASSET_KEY,
                                                                                             RELATED_ASSET_KEY,
                                                                                             RELATED_TO_RELATIONSHIP_NAME,
                                                                                             LINE_ITEM_ID,
                                                                                             LOCK_VERSION, UpdateRequestSource.RelateTo);
        ChooseRelationshipResponse expectedResponse =  new ChooseRelationshipResponse(chooseRelationshipRequest,
                                                                                      expectedRelatedToRelationship,
                                                                                      new ArrayList<CIFAssetUpdateRequest>());

        final ChooseRelationshipUpdater chooseRelationshipUpdater = new ChooseRelationshipUpdater(assetOrchestrator, dependentUpdateBuilderFactory, assetCandidateProvider, pmrHelper);
        final ChooseRelationshipResponse chooseRelationshipResponse = chooseRelationshipUpdater.performUpdate(chooseRelationshipRequest);

        assertThat(chooseRelationshipResponse, is(expectedResponse));
    }

    @Test
    public void shouldReturnChooseRelationshipResponseFromAssetOrchestratorResponseForChildRelationship() {
        ChooseRelationshipRequest chooseRelationshipRequest =  new ChooseRelationshipRequest(OWNER_ASSET_KEY,
                                                                                             RELATED_ASSET_KEY,
                                                                                             CHILD_RELATIONSHIP_NAME,
                                                                                             LINE_ITEM_ID,
                                                                                             LOCK_VERSION, UpdateRequestSource.RelateTo);
        ChooseRelationshipResponse expectedResponse =  new ChooseRelationshipResponse(chooseRelationshipRequest,
                                                                                      expectedChildRelationship,
                                                                                      new ArrayList<CIFAssetUpdateRequest>());

        final ChooseRelationshipUpdater chooseRelationshipUpdater = new ChooseRelationshipUpdater(assetOrchestrator, dependentUpdateBuilderFactory, assetCandidateProvider, pmrHelper);
        final ChooseRelationshipResponse chooseRelationshipResponse = chooseRelationshipUpdater.performUpdate(chooseRelationshipRequest);

        assertThat(chooseRelationshipResponse, is(expectedResponse));
    }


    @Test
    public void shouldFetchChoosableCandidatesAndMatchRelatedAssetIdAndEstablishRelationship() {

        //Given
        ChooseRelationshipRequest chooseRelationshipRequest = new ChooseRelationshipRequest(OWNER_ASSET_KEY, "assetId2", "assetId2", RELATED_TO_RELATIONSHIP_NAME, Client, "aRelatedProductCode");
        ChooseRelationshipResponse expectedResponse =  new ChooseRelationshipResponse(chooseRelationshipRequest,
                expectedRelatedToRelationship,
                new ArrayList<CIFAssetUpdateRequest>());

        AssetDTO matchingAsset = AssetDTOFixture.anAsset().withId("assetId2").withAssetVersion(3L).build();
        AssetDTO nonMatchingAsset = AssetDTOFixture.anAsset().withId("someOtherAssetId2").build();
        when(assetCandidateProvider.getChoosableCandidates(OWNER_ASSET_KEY, newInstance(RELATED_TO_RELATIONSHIP_NAME))).thenReturn(newArrayList(matchingAsset, nonMatchingAsset));
        when(assetOrchestrator.getAsset(new CIFAssetKey(new AssetKey(matchingAsset.getId(), matchingAsset.getVersion()), newArrayList(ProductOfferingRelationshipDetail, QuoteOptionItemDetail)))).thenReturn(relatedAsset);

        //When
        final ChooseRelationshipUpdater chooseRelationshipUpdater = new ChooseRelationshipUpdater(assetOrchestrator, dependentUpdateBuilderFactory, assetCandidateProvider, pmrHelper);
        final ChooseRelationshipResponse chooseRelationshipResponse = chooseRelationshipUpdater.performUpdate(chooseRelationshipRequest);

        //Then
        assertThat(chooseRelationshipResponse, is(expectedResponse));
    }

    @Test
    public void shouldFetchChoosableCandidatesAndCreateStubAssetIdAndEstablishRelationship() {

        //Given
        ChooseRelationshipRequest chooseRelationshipRequest = new ChooseRelationshipRequest(OWNER_ASSET_KEY, "someVPNID", "assetId2", RELATED_TO_RELATIONSHIP_NAME, Client, "aRelatedProductCode");

        AssetDTO matchingAsset = AssetDTOFixture.anAsset().withId("").withProductVersion(null).withAssetVersion(100L).withAssetType(AssetType.STUB).withExternalIdentifier(new ExternalIdentifierDTO(VPNID, "someVPNID")).build();
        AssetDTO nonMatchingAsset = AssetDTOFixture.anAsset().withId("someOtherAssetId2").withAssetType(AssetType.STUB).withExternalIdentifier(new ExternalIdentifierDTO(VPNID, "anotherVpnId")).build();
        when(assetCandidateProvider.getChoosableCandidates(OWNER_ASSET_KEY, newInstance(RELATED_TO_RELATIONSHIP_NAME))).thenReturn(newArrayList(matchingAsset, nonMatchingAsset));
        when(assetOrchestrator.getAsset(new CIFAssetKey(any(AssetKey.class), newArrayList(ProductOfferingRelationshipDetail, QuoteOptionItemDetail)))).thenReturn(null, relatedAsset);

        when(pmrHelper.getProductOffering(matchingAsset.getProductCode())).thenReturn(ProductOfferingFixture.aProductOffering().withProductIdentifier(new ProductIdentifier(matchingAsset.getProductCode(), "A.22")).build());
        //When
        final ChooseRelationshipUpdater chooseRelationshipUpdater = new ChooseRelationshipUpdater(assetOrchestrator, dependentUpdateBuilderFactory, assetCandidateProvider, pmrHelper);
        chooseRelationshipUpdater.performUpdate(chooseRelationshipRequest);

        //Then
        ArgumentCaptor<AssetDTO> cifAssetArgumentCaptor = ArgumentCaptor.forClass(AssetDTO.class);
        verify(assetCandidateProvider, times(1)).putAsset(cifAssetArgumentCaptor.capture());

        AssetDTO dto = cifAssetArgumentCaptor.getValue();
        assertThat(dto.getExternalIdentifier(VPNID), is("someVPNID"));
    }

    @Test
    public void shouldMatchRelatedAssetAndEstablishRelationshipAndCreateDependantRequests() {

        //Given
        CIFAssetUpdateRequest characteristicReloadRequest = new CharacteristicReloadRequest(new AssetKey("anAssociatedAssetId", 1L), "associatedAttribute", 1);
        ChooseRelationshipRequest chooseRelationshipRequest = new ChooseRelationshipRequest(OWNER_ASSET_KEY, "assetId2", "assetId2", RELATED_TO_RELATIONSHIP_NAME, Client, "aRelatedProductCode");

        AssetDTO matchingAsset = AssetDTOFixture.anAsset().withId("assetId2").withAssetVersion(3L).build();
        AssetDTO nonMatchingAsset = AssetDTOFixture.anAsset().withId("someOtherAssetId2").build();

        relatedAsset = CIFAssetFixture.aCIFAsset()
                .withID("assetId2")
                .withCharacteristic("A", "aValue")
                .withSiteId(OWNER_SITE_ID)
                .build();

        when(contributesToChangeRequestBuilder.buildRequests(relatedAsset.getAssetKey(), relatedAsset.getProductCode(), RelationshipName.newInstance(RELATED_TO_RELATIONSHIP_NAME), 1))
                .thenReturn(Sets.newHashSet(characteristicReloadRequest));

        when(assetCandidateProvider.getChoosableCandidates(OWNER_ASSET_KEY, newInstance(RELATED_TO_RELATIONSHIP_NAME))).thenReturn(newArrayList(matchingAsset, nonMatchingAsset));
        when(assetOrchestrator.getAsset(new CIFAssetKey(new AssetKey(matchingAsset.getId(), matchingAsset.getVersion()), newArrayList(ProductOfferingRelationshipDetail, QuoteOptionItemDetail)))).thenReturn(relatedAsset);

        //When
        final ChooseRelationshipUpdater chooseRelationshipUpdater = new ChooseRelationshipUpdater(assetOrchestrator, dependentUpdateBuilderFactory, assetCandidateProvider, pmrHelper);
        final ChooseRelationshipResponse chooseRelationshipResponse = chooseRelationshipUpdater.performUpdate(chooseRelationshipRequest);

        //Then
        assertThat(chooseRelationshipResponse.getDependantUpdates(), hasItem(characteristicReloadRequest));
    }


    @Test(expected = RuntimeException.class)
    public void shouldThrowExceptionWhenNoMatchingChoosableAssetFound() {

        //Given
        ChooseRelationshipRequest chooseRelationshipRequest = new ChooseRelationshipRequest(OWNER_ASSET_KEY, "assetId2", "assetId2", RELATED_TO_RELATIONSHIP_NAME, Client, "aRelatedProductCode");
        when(assetCandidateProvider.getChoosableCandidates(OWNER_ASSET_KEY, newInstance(RELATED_TO_RELATIONSHIP_NAME))).thenReturn(Collections.<AssetDTO>emptyList());

        //When
        final ChooseRelationshipUpdater chooseRelationshipUpdater = new ChooseRelationshipUpdater(assetOrchestrator, dependentUpdateBuilderFactory, assetCandidateProvider, pmrHelper);
        chooseRelationshipUpdater.performUpdate(chooseRelationshipRequest);
    }

}