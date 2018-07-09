package com.bt.rsqe.customerinventory.service.orchestrators;

import com.bt.rsqe.customerinventory.service.client.domain.updates.RestoreAssetRequest;
import com.bt.rsqe.customerinventory.service.client.domain.updates.RestoreAssetResponse;
import com.bt.rsqe.customerinventory.service.client.domain.updates.SpecialBidAttributesCreationRequest;
import com.bt.rsqe.customerinventory.service.client.domain.updates.SpecialBidAttributesCreationResponse;
import com.bt.rsqe.customerinventory.service.client.domain.updates.UpdateRequestSource;
import com.bt.rsqe.customerinventory.service.filters.CIFAssetUpdateRequestFilter;
import com.bt.rsqe.customerinventory.service.client.domain.updates.AutoDefaultRelationshipsRequest;
import com.bt.rsqe.customerinventory.service.client.domain.updates.AutoDefaultRelationshipsResponse;
import com.bt.rsqe.customerinventory.service.client.domain.updates.CIFAssetUpdateRequest;
import com.bt.rsqe.customerinventory.service.client.domain.updates.CIFAssetUpdateResponse;
import com.bt.rsqe.customerinventory.service.client.domain.updates.CancelRelationshipRequest;
import com.bt.rsqe.customerinventory.service.client.domain.updates.CancelRelationshipResponse;
import com.bt.rsqe.customerinventory.service.client.domain.updates.CardinalityImpactChangeRequest;
import com.bt.rsqe.customerinventory.service.client.domain.updates.CardinalityImpactChangeResponse;
import com.bt.rsqe.customerinventory.service.client.domain.updates.CharacteristicChange;
import com.bt.rsqe.customerinventory.service.client.domain.updates.CharacteristicChangeRequest;
import com.bt.rsqe.customerinventory.service.client.domain.updates.CharacteristicChangeResponse;
import com.bt.rsqe.customerinventory.service.client.domain.updates.CharacteristicReloadRequest;
import com.bt.rsqe.customerinventory.service.client.domain.updates.CharacteristicReloadResponse;
import com.bt.rsqe.customerinventory.service.client.domain.updates.ChooseRelationshipRequest;
import com.bt.rsqe.customerinventory.service.client.domain.updates.ChooseRelationshipResponse;
import com.bt.rsqe.customerinventory.service.client.domain.updates.CreateRelationshipRequest;
import com.bt.rsqe.customerinventory.service.client.domain.updates.CreateRelationshipResponse;
import com.bt.rsqe.customerinventory.service.client.domain.updates.InvalidatePriceRequest;
import com.bt.rsqe.customerinventory.service.client.domain.updates.InvalidatePriceResponse;
import com.bt.rsqe.customerinventory.service.client.domain.updates.ReprovideAssetRequest;
import com.bt.rsqe.customerinventory.service.client.domain.updates.ReprovideAssetResponse;
import com.bt.rsqe.customerinventory.service.client.domain.updates.RuleFilterImpactChangeRequest;
import com.bt.rsqe.customerinventory.service.client.domain.updates.RuleFilterImpactChangeResponse;
import com.bt.rsqe.customerinventory.service.client.domain.updates.SpecialBidAttributesReloadRequest;
import com.bt.rsqe.customerinventory.service.client.domain.updates.SpecialBidAttributesReloadResponse;
import com.bt.rsqe.customerinventory.service.client.domain.updates.SpecialBidCharacteristicChangeRequest;
import com.bt.rsqe.customerinventory.service.client.domain.updates.SpecialBidCharacteristicChangeResponse;
import com.bt.rsqe.customerinventory.service.client.domain.updates.UpdateRelationshipRequest;
import com.bt.rsqe.customerinventory.service.client.domain.updates.UpdateRelationshipResponse;
import com.bt.rsqe.customerinventory.service.client.domain.updates.UpdateStencilRequest;
import com.bt.rsqe.customerinventory.service.client.domain.updates.UpdateStencilResponse;
import com.bt.rsqe.customerinventory.service.client.domain.updates.ValidationImpactChangeRequest;
import com.bt.rsqe.customerinventory.service.client.domain.updates.ValidationImpactChangeResponse;
import com.bt.rsqe.customerinventory.service.client.resource.CIFAssetExtension;
import com.bt.rsqe.customerinventory.service.filters.CIFAssetUpdateResponseFilter;
import com.bt.rsqe.customerinventory.service.updates.AutoDefaultRelationshipUpdater;
import com.bt.rsqe.customerinventory.service.updates.CancelRelationshipUpdater;
import com.bt.rsqe.customerinventory.service.updates.CharacteristicReloadUpdater;
import com.bt.rsqe.customerinventory.service.updates.CharacteristicsUpdater;
import com.bt.rsqe.customerinventory.service.updates.ChooseRelationshipUpdater;
import com.bt.rsqe.customerinventory.service.updates.CreateRelationshipUpdater;
import com.bt.rsqe.customerinventory.service.updates.InvalidatePriceUpdater;
import com.bt.rsqe.customerinventory.service.updates.ReprovideAssetUpdater;
import com.bt.rsqe.customerinventory.service.updates.RestoreAssetUpdater;
import com.bt.rsqe.customerinventory.service.updates.SpecialBidCharacteristicsCreationUpdater;
import com.bt.rsqe.customerinventory.service.updates.SpecialBidCharacteristicsReloadUpdater;
import com.bt.rsqe.customerinventory.service.updates.SpecialBidCharacteristicsUpdater;
import com.bt.rsqe.customerinventory.service.updates.UpdateRelationshipUpdater;
import com.bt.rsqe.customerinventory.service.updates.UpdateStencilUpdater;
import com.bt.rsqe.customerinventory.service.updates.UpdationIgnoreRequestUpdater;
import com.bt.rsqe.domain.AssetKey;
import com.bt.rsqe.domain.product.parameters.RelationshipType;
import com.bt.rsqe.util.TestWithRules;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.bt.rsqe.domain.product.ProductOffering.*;
import static com.google.common.collect.Lists.*;
import static org.hamcrest.core.Is.*;
import static org.junit.Assert.*;
import static org.junit.matchers.JUnitMatchers.*;
import static org.mockito.Mockito.*;

public class AssetUpdateOrchestratorTest extends TestWithRules {
    private final CIFAssetOrchestrator cifAssetOrchestrator = mock (CIFAssetOrchestrator.class) ;
    private final CreateRelationshipUpdater createRelationshipUpdater = mock(CreateRelationshipUpdater.class);
    private final CharacteristicsUpdater characteristicsUpdater = mock(CharacteristicsUpdater.class);
    private final SpecialBidCharacteristicsUpdater specialBidCharacteristicsUpdater = mock(SpecialBidCharacteristicsUpdater.class);
    private final UpdateStencilUpdater stencilUpdater = mock(UpdateStencilUpdater.class);
    private final ChooseRelationshipUpdater chooseRelationshipUpdater = mock(ChooseRelationshipUpdater.class);
    private final AutoDefaultRelationshipUpdater autoDefaultRelationshipUpdater = mock(AutoDefaultRelationshipUpdater.class);
    private final CancelRelationshipUpdater cancelRelationshipUpdater = mock(CancelRelationshipUpdater.class);
    private final ReprovideAssetUpdater reprovideAssetUpdater = mock(ReprovideAssetUpdater.class);
    private final InvalidatePriceUpdater invalidatePriceUpdater = mock(InvalidatePriceUpdater.class);
    private final UpdateRelationshipUpdater updateRelationshipUpdater = mock(UpdateRelationshipUpdater.class);
    private final CharacteristicReloadUpdater characteristicReloadUpdater = mock(CharacteristicReloadUpdater.class);
    private final UpdationIgnoreRequestUpdater updationIgnoreRequestUpdater = mock(UpdationIgnoreRequestUpdater.class);
    private final SpecialBidCharacteristicsReloadUpdater specialBidCharacteristicsReloadUpdater = mock(SpecialBidCharacteristicsReloadUpdater.class);
    private final SpecialBidCharacteristicsCreationUpdater specialBidCharacteristicsCreationUpdater = mock(SpecialBidCharacteristicsCreationUpdater.class);
    private final RestoreAssetUpdater restoreAssetUpdater = mock (RestoreAssetUpdater.class);

    private final AssetUpdateOrchestrator assetUpdateOrchestrator = new AssetUpdateOrchestrator(
            createRelationshipUpdater,
            characteristicsUpdater,
            stencilUpdater,
            chooseRelationshipUpdater,
            autoDefaultRelationshipUpdater,
            specialBidCharacteristicsUpdater,
            cancelRelationshipUpdater,
            reprovideAssetUpdater,
            updateRelationshipUpdater,
            updationIgnoreRequestUpdater,
            characteristicReloadUpdater,
            specialBidCharacteristicsReloadUpdater,
            invalidatePriceUpdater,
            new CIFAssetUpdateRequestFilter(),
            new CIFAssetUpdateResponseFilter(),
            specialBidCharacteristicsCreationUpdater,
            restoreAssetUpdater);

    @Before
    public void setUp() throws Exception {
        CreateRelationshipResponse createRelationshipResponse = mock(CreateRelationshipResponse.class);
        CharacteristicChangeResponse characteristicChangeResponse = mock(CharacteristicChangeResponse.class);
        UpdateStencilResponse updateStencilResponse = mock(UpdateStencilResponse.class);
        ChooseRelationshipResponse chooseRelationshipResponse = mock(ChooseRelationshipResponse.class);
        AutoDefaultRelationshipsResponse autoDefaultRelationshipsResponse = mock(AutoDefaultRelationshipsResponse.class);
        CancelRelationshipResponse cancelRelationshipResponse = mock(CancelRelationshipResponse.class);
        ReprovideAssetResponse reprovideAssetResponse = mock(ReprovideAssetResponse.class);
        InvalidatePriceResponse invalidatePriceResponse = mock(InvalidatePriceResponse.class);
        UpdateRelationshipResponse updateRelationshipResponse = mock(UpdateRelationshipResponse.class);
        RestoreAssetResponse restoreAssetResponse = mock (RestoreAssetResponse.class) ;


        when(createRelationshipUpdater.performUpdate(any(CreateRelationshipRequest.class))).thenReturn(createRelationshipResponse);
        when(characteristicsUpdater.performUpdate(any(CharacteristicChangeRequest.class))).thenReturn(characteristicChangeResponse);
        when(stencilUpdater.performUpdate(any(UpdateStencilRequest.class))).thenReturn(updateStencilResponse);
        when(chooseRelationshipUpdater.performUpdate(any(ChooseRelationshipRequest.class))).thenReturn(chooseRelationshipResponse);
        when(autoDefaultRelationshipUpdater.performUpdate(any(AutoDefaultRelationshipsRequest.class))).thenReturn(autoDefaultRelationshipsResponse);
        when(cancelRelationshipUpdater.performUpdate(any(CancelRelationshipRequest.class))).thenReturn(cancelRelationshipResponse);
        when(invalidatePriceUpdater.performUpdate(any(InvalidatePriceRequest.class))).thenReturn(invalidatePriceResponse);
        when(reprovideAssetUpdater.performUpdate(any(ReprovideAssetRequest.class))).thenReturn(reprovideAssetResponse);
        when(updateRelationshipUpdater.performUpdate(any(UpdateRelationshipRequest.class))).thenReturn(updateRelationshipResponse);
        when (restoreAssetUpdater.performUpdate(any(RestoreAssetRequest.class))).thenReturn(restoreAssetResponse) ;
    }

    @Test
    public void shouldNotCallRelationshipCreatorWhenNoUpdatesRequested() {
        assetUpdateOrchestrator.update(new ArrayList<CIFAssetUpdateRequest>());

        verify(createRelationshipUpdater, times(0)).performUpdate(any(CreateRelationshipRequest.class));
    }

    @Test
    public void shouldCallRelationshipCreatorForEachCIFAssetAddRelationshipUpdate() {
        CreateRelationshipRequest createRelationshipUpdate1 =
                new CreateRelationshipRequest(null, new AssetKey ("1", 1L), null, null, null, null, null, null, 1);
        CreateRelationshipRequest createRelationshipUpdate2 =
                new CreateRelationshipRequest(null, new AssetKey ("2", 1L), null, null, null, null, null, null, 1);
        CreateRelationshipRequest createRelationshipUpdate3 =
                new CreateRelationshipRequest(null, new AssetKey ("3", 1L), null, null, null, null, null, null, 1);

        assetUpdateOrchestrator.update(newArrayList((CIFAssetUpdateRequest) createRelationshipUpdate1, createRelationshipUpdate2, createRelationshipUpdate3));

        verify(createRelationshipUpdater).performUpdate(createRelationshipUpdate1);
        verify(createRelationshipUpdater).performUpdate(createRelationshipUpdate2);
        verify(createRelationshipUpdater).performUpdate(createRelationshipUpdate3);
        verify(invalidatePriceUpdater, never()).performUpdate(any(InvalidatePriceRequest.class));
    }

    @Test
    public void shouldCallCharacteristicUpdaterForCharacteristicsUpdate() {
        //Given
        List<CharacteristicChange> characteristicChangeRequest = newArrayList(new CharacteristicChange("AN ATTRIBUTE ONE", "one"));
        CharacteristicChangeRequest requestList = new CharacteristicChangeRequest(new AssetKey("anAsset", 1L),
                                                                                  newArrayList(CIFAssetExtension.values()),
                                                                                  characteristicChangeRequest,
                                                                                  "lineItemId", 62);

        //When
        assetUpdateOrchestrator.update(newArrayList((CIFAssetUpdateRequest) requestList));

        verify(characteristicsUpdater).performUpdate(requestList);
        verify(specialBidCharacteristicsUpdater, never()).performUpdate(any(SpecialBidCharacteristicChangeRequest.class));
        verify(createRelationshipUpdater, never()).performUpdate(any(CreateRelationshipRequest.class));
        verify(updateRelationshipUpdater, never()).performUpdate(any(UpdateRelationshipRequest.class));
        verify(stencilUpdater, never()).performUpdate(any(UpdateStencilRequest.class));
        verify(invalidatePriceUpdater, never()).performUpdate(any(InvalidatePriceRequest.class));
    }

    @Test
    public void shouldCallSpecialBidCharacteristicUpdaterForSpecialBidCharacteristicsUpdate() {
        //Given
        List<CharacteristicChange> characteristicChangeRequest = newArrayList(new CharacteristicChange("SPECIAL BID ATTRIBUTE ONE", "one"));
        SpecialBidCharacteristicChangeRequest requestList = new SpecialBidCharacteristicChangeRequest(new AssetKey("anAsset", 1L),
                                                                                                      newArrayList(CIFAssetExtension.values()),
                                                                                                      characteristicChangeRequest,
                                                                                                      "lineItemId", 1);
        when(specialBidCharacteristicsUpdater.performUpdate(requestList)).thenReturn(new SpecialBidCharacteristicChangeResponse(requestList, requestList.getCharacteristicChanges(),
                                                                                                                                Collections.<CIFAssetUpdateRequest>emptyList()));

        //When
        assetUpdateOrchestrator.update(newArrayList((CIFAssetUpdateRequest) requestList));

        //Then
        verify(specialBidCharacteristicsUpdater).performUpdate(requestList);
        verify(characteristicsUpdater, never()).performUpdate(any(CharacteristicChangeRequest.class));
        verify(createRelationshipUpdater, never()).performUpdate(any(CreateRelationshipRequest.class));
        verify(stencilUpdater, never()).performUpdate(any(UpdateStencilRequest.class));
        verify(updateRelationshipUpdater, never()).performUpdate(any(UpdateRelationshipRequest.class));
        verify(invalidatePriceUpdater, never()).performUpdate(any(InvalidatePriceRequest.class));
    }

    @Test
    public void shouldCallReprovideUpdaterForReprovideAssetUpdate() {
        AssetKey assetKey = new AssetKey("assetId", 1);
        final ReprovideAssetRequest reprovideAssetRequest = new ReprovideAssetRequest(assetKey, assetKey, "", 0);
        assetUpdateOrchestrator.update(newArrayList((CIFAssetUpdateRequest) reprovideAssetRequest));

        //Then
        verify(reprovideAssetUpdater).performUpdate(reprovideAssetRequest);
    }

    @Test
    public void shouldCallStencilUpdaterForUpdateStencilRequest() {
        // Given
        UpdateStencilRequest request = new UpdateStencilRequest(new AssetKey("assetId", 1), "stencilId", "name", "uri", "lineItemId", 62);

        // when
        assetUpdateOrchestrator.update(newArrayList((CIFAssetUpdateRequest) request));

        // Assert
        verify(characteristicsUpdater, never()).performUpdate(any(CharacteristicChangeRequest.class));
        verify(createRelationshipUpdater, never()).performUpdate(any(CreateRelationshipRequest.class));
        verify(updateRelationshipUpdater, never()).performUpdate(any(UpdateRelationshipRequest.class));
        verify(stencilUpdater).performUpdate(request);
        verify(invalidatePriceUpdater, never()).performUpdate(any(InvalidatePriceRequest.class));
    }

    @Test
    public void shouldCallCancelRelationshipUpdaterForCancelRelationshipRequest() {
        // Given
        CancelRelationshipRequest request = new CancelRelationshipRequest(new AssetKey("assetKey", 1), "lineItemId", 1, new AssetKey("cancellingAssetId", 1), "relationshipName", "cancelAssetProductCode", false);

        // when
        assetUpdateOrchestrator.update(newArrayList((CIFAssetUpdateRequest) request));

        // Assert
        verify(characteristicsUpdater, never()).performUpdate(any(CharacteristicChangeRequest.class));
        verify(createRelationshipUpdater, never()).performUpdate(any(CreateRelationshipRequest.class));
        verify(stencilUpdater, never()).performUpdate(any(UpdateStencilRequest.class));
        verify(updateRelationshipUpdater, never()).performUpdate(any(UpdateRelationshipRequest.class));
        verify(cancelRelationshipUpdater).performUpdate(request);
        verify(invalidatePriceUpdater, never()).performUpdate(any(InvalidatePriceRequest.class));
    }

    @Test
    public void shouldCallAutoDefaultRelationshipsUpdaterForAutoDefaultRelationshipsRequest() {
        // Given
        AutoDefaultRelationshipsRequest request = new AutoDefaultRelationshipsRequest(new AssetKey("assetId", 1), "lineItemId", 62, "aProductCode");

        // when
        assetUpdateOrchestrator.update(newArrayList((CIFAssetUpdateRequest) request));

        // Assert
        verify(autoDefaultRelationshipUpdater).performUpdate(request);
        verify(invalidatePriceUpdater, never()).performUpdate(any(InvalidatePriceRequest.class));
    }

    @Test
    public void shouldCallChooseRelationshipUpdaterForUpdateChooseRelationshipRequest() {
        // Given
        ChooseRelationshipRequest request = new ChooseRelationshipRequest(new AssetKey("assetId", 1),
                                                                          new AssetKey("assetId2", 2),
                                                                          "relationshipName",
                                                                          "name", 52, UpdateRequestSource.Client);

        // when
        assetUpdateOrchestrator.update(newArrayList((CIFAssetUpdateRequest) request));

        // Assert
        verify(characteristicsUpdater, never()).performUpdate(any(CharacteristicChangeRequest.class));
        verify(createRelationshipUpdater, never()).performUpdate(any(CreateRelationshipRequest.class));
        verify(stencilUpdater, never()).performUpdate(any(UpdateStencilRequest.class));
        verify(updateRelationshipUpdater, never()).performUpdate(any(UpdateRelationshipRequest.class));
        verify(chooseRelationshipUpdater).performUpdate(request);
        verify(invalidatePriceUpdater, never()).performUpdate(any(InvalidatePriceRequest.class));

    }

    @Test
    public void shouldCallInvalidatePriceUpdaterForInvalidatePriceRequest() {
        // Setup
        InvalidatePriceRequest request = new InvalidatePriceRequest();
        // Execute
        assetUpdateOrchestrator.update(Lists.<CIFAssetUpdateRequest>newArrayList(request));

        // Assert
        verify(invalidatePriceUpdater).performUpdate(request);
    }

    @Test
    public void shouldCallUpdateRelationshipUpdaterForUpdateRelationshipRequest() {
        // Given
        UpdateRelationshipRequest request = new UpdateRelationshipRequest(new AssetKey("assetId", 1),
                                                                          "assetId",
                                                                          1l,
                                                                          "stencilID",
                                                                          "lineItemId", 52);

        // when
        assetUpdateOrchestrator.update(newArrayList((CIFAssetUpdateRequest) request));

        // Assert
        verify(characteristicsUpdater, never()).performUpdate(any(CharacteristicChangeRequest.class));
        verify(createRelationshipUpdater, never()).performUpdate(any(CreateRelationshipRequest.class));
        verify(stencilUpdater, never()).performUpdate(any(UpdateStencilRequest.class));
        verify(chooseRelationshipUpdater, never()).performUpdate(any(ChooseRelationshipRequest.class));
        verify(updateRelationshipUpdater).performUpdate(request);
    }

    @Test
    public void shouldThrowAnExceptionForAnUnknownUpdate() {
        expectException(IllegalArgumentException.class, "Cannot build an updater for update with type ");
        final CIFAssetUpdateRequest cifAssetUpdate = mock(CIFAssetUpdateRequest.class);
        assetUpdateOrchestrator.update(newArrayList(cifAssetUpdate));
    }

    @Test
    public void shouldQueueAndExecuteDependantUpdatesReturnedInResponse() {
        final CharacteristicChangeRequest originalChangeRequest = new CharacteristicChangeRequest(new AssetKey("assetIdOne", 1l),
                                                                                                  new ArrayList<CIFAssetExtension>(),
                                                                                                  new ArrayList<CharacteristicChange>(),
                                                                                                  "lineItemId", 15);
        final CharacteristicChangeRequest dependantRequest = new CharacteristicChangeRequest(new AssetKey("assetIdTwo", 1l),
                                                                                             new ArrayList<CIFAssetExtension>(),
                                                                                             new ArrayList<CharacteristicChange>(),
                                                                                             "lineItemId", 15);
        final CharacteristicChangeResponse originalResponse = new CharacteristicChangeResponse(originalChangeRequest,
                                                                                               new ArrayList<CharacteristicChange>(),
                                                                                               newArrayList((CIFAssetUpdateRequest) dependantRequest));
        final CharacteristicChangeResponse dependantResponse = new CharacteristicChangeResponse(dependantRequest,
                                                                                                new ArrayList<CharacteristicChange>(),
                                                                                                new ArrayList<CIFAssetUpdateRequest>());

        when(characteristicsUpdater.performUpdate(originalChangeRequest)).thenReturn(originalResponse);
        when(characteristicsUpdater.performUpdate(dependantRequest)).thenReturn(dependantResponse);

        final List<CIFAssetUpdateResponse> completedUpdates = assetUpdateOrchestrator.update(
            newArrayList((CIFAssetUpdateRequest) originalChangeRequest));

        assertThat(completedUpdates.size(), is(2));
        assertThat(completedUpdates.get(0), is((CIFAssetUpdateResponse) originalResponse));
        assertThat(completedUpdates.get(1), is((CIFAssetUpdateResponse) dependantResponse));
        verify(characteristicsUpdater, times(1)).performUpdate(originalChangeRequest);
        verify(characteristicsUpdater, times(1)).performUpdate(dependantRequest);
    }

    @Test
    public void shouldCallUpdationIgnoreRequestHandlerForValidationImpactChangeRequest() {
        //Given
        ValidationImpactChangeRequest validationImpactChangeRequest = new ValidationImpactChangeRequest(new AssetKey("anAsset", 1L));
        when(updationIgnoreRequestUpdater.performUpdate(validationImpactChangeRequest)).thenReturn(new ValidationImpactChangeResponse(validationImpactChangeRequest));

        //When
        assetUpdateOrchestrator.update(newArrayList((CIFAssetUpdateRequest) validationImpactChangeRequest));

        //Then
        verify(updationIgnoreRequestUpdater, times(1)).performUpdate(validationImpactChangeRequest);
    }

    @Test
    public void shouldCallUpdationIgnoreRequestHandlerForCardinalityImpactChangeRequest() {
        //Given
        CardinalityImpactChangeRequest cardinalityImpactChangeRequest = new CardinalityImpactChangeRequest(new AssetKey("anAsset", 1L));
        when(updationIgnoreRequestUpdater.performUpdate(cardinalityImpactChangeRequest)).thenReturn(new CardinalityImpactChangeResponse(cardinalityImpactChangeRequest));

        //When
        assetUpdateOrchestrator.update(newArrayList((CIFAssetUpdateRequest) cardinalityImpactChangeRequest));

        //Then
        verify(updationIgnoreRequestUpdater, times(1)).performUpdate(cardinalityImpactChangeRequest);
    }

    @Test
    public void shouldCallUpdationIgnoreRequestHandlerForRuleFilterImpactChangeRequest() {
        //Given
        RuleFilterImpactChangeRequest ruleFilterImpactChangeRequest = new RuleFilterImpactChangeRequest(new AssetKey("anAsset", 1L));
        when(updationIgnoreRequestUpdater.performUpdate(ruleFilterImpactChangeRequest)).thenReturn(new RuleFilterImpactChangeResponse(ruleFilterImpactChangeRequest));

        //When
        assetUpdateOrchestrator.update(newArrayList((CIFAssetUpdateRequest) ruleFilterImpactChangeRequest));

        //Then
        verify(updationIgnoreRequestUpdater, times(1)).performUpdate(ruleFilterImpactChangeRequest);
    }

    @Test
    public void shouldCallCharacteristicReloadUpdaterForCharacteristicReloadRequest() {
        //Given
        CharacteristicReloadRequest characteristicReloadRequest = new CharacteristicReloadRequest(new AssetKey("anAsset", 1L), "anAttribute", 1);
        when(characteristicReloadUpdater.performUpdate(characteristicReloadRequest)).thenReturn(new CharacteristicReloadResponse(characteristicReloadRequest,
                                                                                                                                 "anAttributeOldValue", "anAttributeNewValue", Collections.<CIFAssetUpdateRequest>emptyList()));

        //When
        assetUpdateOrchestrator.update(newArrayList((CIFAssetUpdateRequest) characteristicReloadRequest));

        //Then
        verify(characteristicReloadUpdater, times(1)).performUpdate(characteristicReloadRequest);
    }

    @Test
    public void shouldCallSpecialBidCharacteristicsReloadUpdaterForSpecialBidCharacteristicReloadRequest() {
        //Given
        SpecialBidAttributesReloadRequest specialBidAttributesRefreshRequest = new SpecialBidAttributesReloadRequest(new AssetKey("anAssetId", 1L), SPECIAL_BID_TEMPLATE_RESERVED_NAME, "newTemplateName");
        SpecialBidAttributesReloadResponse specialBidAttributesReloadResponse = new SpecialBidAttributesReloadResponse(
            specialBidAttributesRefreshRequest,
            newArrayList(new CharacteristicChange(CONFIGURATION_CATEGORY_RESERVED_NAME, "newValue", "oldValue")),
            Collections.<CIFAssetUpdateRequest>emptyList()
        );
        when(specialBidCharacteristicsReloadUpdater.performUpdate(specialBidAttributesRefreshRequest)).thenReturn(specialBidAttributesReloadResponse);

        //When
        assetUpdateOrchestrator.update(newArrayList((CIFAssetUpdateRequest) specialBidAttributesRefreshRequest));

        //Then
        verify(specialBidCharacteristicsReloadUpdater, times(1)).performUpdate(specialBidAttributesRefreshRequest);
    }

    @Test
    public void shouldCallSpecialBidCharacteristicsCreationUpdaterForSpecialBidCharacteristicCreationRequest() {
        //Given
        SpecialBidAttributesCreationRequest specialBidAttributesCreationRequest = new SpecialBidAttributesCreationRequest(new AssetKey("anAssetId", 1L));
        SpecialBidAttributesCreationResponse specialBidAttributesCreationResponse = new SpecialBidAttributesCreationResponse(specialBidAttributesCreationRequest);

        when(specialBidCharacteristicsCreationUpdater.performUpdate(specialBidAttributesCreationRequest)).thenReturn(specialBidAttributesCreationResponse);

        //When
        assetUpdateOrchestrator.update(newArrayList((CIFAssetUpdateRequest) specialBidAttributesCreationRequest));

        //Then
        verify(specialBidCharacteristicsCreationUpdater, times(1)).performUpdate(specialBidAttributesCreationRequest);
    }


    @Test
    public void shouldHandleCharacteristicReloadUpdaterForCharacteristicReloadRequestReturningNull() {
        //Given
        CharacteristicReloadRequest characteristicReloadRequest = new CharacteristicReloadRequest(new AssetKey("anAsset", 1L), "anAttribute", 1);
        when(characteristicReloadUpdater.performUpdate(characteristicReloadRequest)).thenReturn(null);

        //When
        List<CIFAssetUpdateResponse> responses = assetUpdateOrchestrator.update(newArrayList((CIFAssetUpdateRequest) characteristicReloadRequest));

        //Then
        assertThat(responses.size(), is(0));
        verify(characteristicReloadUpdater, times(1)).performUpdate(characteristicReloadRequest);

    }

    @Test
    public void shouldNotQueueCharacteristicReloadRequestWhenRespectiveAssetIsCancelled() {
        final CharacteristicChangeRequest originalChangeRequest = new CharacteristicChangeRequest(new AssetKey("assetIdOne", 1l),
                                                                                                  new ArrayList<CIFAssetExtension>(),
                                                                                                  new ArrayList<CharacteristicChange>(),
                                                                                                  "lineItemId", 15);

        final CancelRelationshipRequest cancelRelationshipRequest = new CancelRelationshipRequest(new AssetKey("assetIdTwo", 1l), "lineItemId", 15,
                                                                                                  new AssetKey("cancellingId", 1), "aRelationshipName", "S123", true);

        final CharacteristicReloadRequest characteristicReloadRequest = new CharacteristicReloadRequest(new AssetKey("cancellingId", 1), "anAttribute", 1);

        final CharacteristicChangeResponse originalResponse = new CharacteristicChangeResponse(originalChangeRequest, new ArrayList<CharacteristicChange>(),
                                                                                               newArrayList((CIFAssetUpdateRequest)characteristicReloadRequest));
        when(characteristicsUpdater.performUpdate(originalChangeRequest)).thenReturn(originalResponse);

        final List<CIFAssetUpdateResponse> completedUpdates = assetUpdateOrchestrator.update(newArrayList(originalChangeRequest, cancelRelationshipRequest));

        assertThat(completedUpdates.size(), is(2));
        verify(cancelRelationshipUpdater, times(1)).performUpdate(cancelRelationshipRequest);
        verify(characteristicReloadUpdater, times(0)).performUpdate(characteristicReloadRequest);

    }

    @Test
    public void shouldFilterValidationImpactResponseWhenRespectiveAssetIsCancelled() {
        //Given
        final CharacteristicChangeRequest originalChangeRequest = new CharacteristicChangeRequest(new AssetKey("assetIdOne", 1l),
                                                                                                  new ArrayList<CIFAssetExtension>(),
                                                                                                  new ArrayList<CharacteristicChange>(),
                                                                                                  "lineItemId", 15);

        final CancelRelationshipRequest cancelRelationshipRequest = new CancelRelationshipRequest(new AssetKey("assetIdTwo", 1l), "lineItemId", 15,
                                                                                                  new AssetKey("cancellingId", 1), "aRelationshipName", "S123", true);

        final CharacteristicReloadRequest characteristicReloadRequest = new CharacteristicReloadRequest(new AssetKey("cancellingId", 1), "anAttribute", 1);
        final ValidationImpactChangeRequest validationImpactChangeRequest = new ValidationImpactChangeRequest(new AssetKey("cancellingId", 1));

        final CharacteristicChangeResponse originalResponse = new CharacteristicChangeResponse(originalChangeRequest, new ArrayList<CharacteristicChange>(),
                                                                                               newArrayList(characteristicReloadRequest, validationImpactChangeRequest));
        when(characteristicsUpdater.performUpdate(originalChangeRequest)).thenReturn(originalResponse);
        ValidationImpactChangeResponse validationImpactChangeResponse = new ValidationImpactChangeResponse(validationImpactChangeRequest);
        when(updationIgnoreRequestUpdater.performUpdate(validationImpactChangeRequest)).thenReturn(validationImpactChangeResponse);
        when(cancelRelationshipUpdater.performUpdate(cancelRelationshipRequest)).thenReturn(new CancelRelationshipResponse(cancelRelationshipRequest, RelationshipType.RelatedTo, Collections.<CIFAssetUpdateRequest>emptyList()));

        //When
        final List<CIFAssetUpdateResponse> completedUpdates = assetUpdateOrchestrator.update(newArrayList(originalChangeRequest, cancelRelationshipRequest));

        //Then
        assertThat(completedUpdates.size(), is(2));
        assertThat(inList(completedUpdates, validationImpactChangeResponse), is(false));
        verify(cancelRelationshipUpdater, times(1)).performUpdate(cancelRelationshipRequest);
        verify(characteristicReloadUpdater, times(0)).performUpdate(characteristicReloadRequest);

    }

    @Test
    public void shouldNotFilterValidationImpactResponseWhenRespectiveAssetIsNotCancelled() {
        final CharacteristicChangeRequest originalChangeRequest = new CharacteristicChangeRequest(new AssetKey("assetIdOne", 1l),
                                                                                                  new ArrayList<CIFAssetExtension>(),
                                                                                                  new ArrayList<CharacteristicChange>(),
                                                                                                  "lineItemId", 15);

        final CancelRelationshipRequest cancelRelationshipRequest = new CancelRelationshipRequest(new AssetKey("assetIdTwo", 1l), "lineItemId", 15,
                                                                                                  new AssetKey("cancellingId", 1), "aRelationshipName", "S123", true);

        final CharacteristicReloadRequest characteristicReloadRequest = new CharacteristicReloadRequest(new AssetKey("cancellingId", 1), "anAttribute", 1);
        final ValidationImpactChangeRequest validationImpactChangeRequest = new ValidationImpactChangeRequest(new AssetKey("assetIdOne", 1));

        final CharacteristicChangeResponse originalResponse = new CharacteristicChangeResponse(originalChangeRequest, new ArrayList<CharacteristicChange>(),
                                                                                               newArrayList(characteristicReloadRequest, validationImpactChangeRequest));
        when(characteristicsUpdater.performUpdate(originalChangeRequest)).thenReturn(originalResponse);
        ValidationImpactChangeResponse validationImpactChangeResponse = new ValidationImpactChangeResponse(validationImpactChangeRequest);
        when(updationIgnoreRequestUpdater.performUpdate(validationImpactChangeRequest)).thenReturn(validationImpactChangeResponse);
        when(cancelRelationshipUpdater.performUpdate(cancelRelationshipRequest)).thenReturn(new CancelRelationshipResponse(cancelRelationshipRequest, RelationshipType.RelatedTo, Collections.<CIFAssetUpdateRequest>emptyList()));

        final List<CIFAssetUpdateResponse> completedUpdates = assetUpdateOrchestrator.update(newArrayList(originalChangeRequest, cancelRelationshipRequest));

        assertThat(completedUpdates.size(), is(3));
        assertThat(completedUpdates, hasItem((CIFAssetUpdateResponse)validationImpactChangeResponse));
        verify(cancelRelationshipUpdater, times(1)).performUpdate(cancelRelationshipRequest);
        verify(characteristicReloadUpdater, times(0)).performUpdate(characteristicReloadRequest);

    }

    private boolean inList(List<CIFAssetUpdateResponse> updateResponses, final CIFAssetUpdateResponse assetUpdateResponse) {
        return Iterables.tryFind(updateResponses, new Predicate<CIFAssetUpdateResponse>() {
            @Override
            public boolean apply(CIFAssetUpdateResponse input) {
                return input.equals(assetUpdateResponse);
            }
        }).isPresent();
    }


    @Test
    public void shouldCallRestoreAssetUpdaterForRestoreAssetRequest() throws Exception {
        RestoreAssetRequest request = new RestoreAssetRequest(new AssetKey("assetId", 1), "lineItemId", 2) ;

        final List<CIFAssetUpdateResponse> completedUpdates = assetUpdateOrchestrator.update(Lists.<CIFAssetUpdateRequest>newArrayList(request));

        verify (restoreAssetUpdater, times(1)).performUpdate(request) ;

        verify (createRelationshipUpdater, never()).performUpdate(any(CreateRelationshipRequest.class)) ;
        verify (characteristicsUpdater, never()).performUpdate(any(CharacteristicChangeRequest.class)) ;
        verify (stencilUpdater, never()).performUpdate(any(UpdateStencilRequest.class)) ;
        verify (chooseRelationshipUpdater, never()).performUpdate(any(ChooseRelationshipRequest.class)) ;
        verify (autoDefaultRelationshipUpdater, never()).performUpdate(any(AutoDefaultRelationshipsRequest.class)) ;
        verify (specialBidCharacteristicsUpdater, never()).performUpdate(any(SpecialBidCharacteristicChangeRequest.class)) ;
        verify (cancelRelationshipUpdater, never()).performUpdate(any(CancelRelationshipRequest.class)) ;
        verify (reprovideAssetUpdater, never()).performUpdate(any(ReprovideAssetRequest.class)) ;
        verify (updateRelationshipUpdater, never()).performUpdate(any(UpdateRelationshipRequest.class)) ;
        verify (updationIgnoreRequestUpdater, never()).performUpdate(any(CIFAssetUpdateRequest.class)) ;
        verify (characteristicReloadUpdater, never()).performUpdate(any(CharacteristicReloadRequest.class)) ;
        verify (specialBidCharacteristicsReloadUpdater, never()).performUpdate(any(SpecialBidAttributesReloadRequest.class)) ;
        verify (invalidatePriceUpdater, never()).performUpdate(any(InvalidatePriceRequest.class)) ;
        verify (specialBidCharacteristicsCreationUpdater, never()).performUpdate(any(SpecialBidAttributesCreationRequest.class)) ;

    }
}