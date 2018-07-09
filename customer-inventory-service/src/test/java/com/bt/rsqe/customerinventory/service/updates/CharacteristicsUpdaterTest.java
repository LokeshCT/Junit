package com.bt.rsqe.customerinventory.service.updates;


import com.bt.rsqe.customerinventory.service.client.domain.CIFAsset;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetAttributeDetail;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetCharacteristic;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetKey;
import com.bt.rsqe.customerinventory.service.client.domain.updates.CIFAssetUpdateRequest;
import com.bt.rsqe.customerinventory.service.client.domain.updates.CharacteristicChange;
import com.bt.rsqe.customerinventory.service.client.domain.updates.CharacteristicChangeRequest;
import com.bt.rsqe.customerinventory.service.client.domain.updates.CharacteristicChangeResponse;
import com.bt.rsqe.customerinventory.service.client.domain.updates.CharacteristicReloadRequest;
import com.bt.rsqe.customerinventory.service.client.domain.updates.SpecialBidAttributesCreationRequest;
import com.bt.rsqe.customerinventory.service.client.domain.updates.SpecialBidAttributesReloadRequest;
import com.bt.rsqe.customerinventory.service.client.domain.updates.ValidationImpactChangeRequest;
import com.bt.rsqe.customerinventory.service.client.fixtures.CIFAssetFixture;
import com.bt.rsqe.customerinventory.service.client.resource.CIFAssetExtension;
import com.bt.rsqe.customerinventory.service.orchestrators.CIFAssetOrchestrator;
import com.bt.rsqe.domain.AssetKey;
import com.bt.rsqe.domain.product.ProductOffering;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static com.bt.rsqe.domain.product.AttributeDataType.*;
import static com.bt.rsqe.domain.product.AttributeOwner.*;
import static com.bt.rsqe.domain.product.ProductOffering.*;
import static com.google.common.collect.Lists.*;
import static com.google.common.collect.Sets.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.junit.matchers.JUnitMatchers.*;
import static org.mockito.Mockito.*;

public class CharacteristicsUpdaterTest {

    private CIFAssetOrchestrator cifAssetOrchestrator = mock(CIFAssetOrchestrator.class);
    private ContributesToChangeRequestBuilder contributesToChangeRequestBuilder = mock(ContributesToChangeRequestBuilder.class);
    private InvalidatePriceRequestBuilder invalidatePriceRequestBuilder = mock (InvalidatePriceRequestBuilder.class);
    DependentUpdateBuilderFactory dependentUpdateBuilderFactory = new DependentUpdateBuilderFactoryBuilder().with(contributesToChangeRequestBuilder).with(invalidatePriceRequestBuilder).build();;
    private CharacteristicsUpdater characteristicsUpdater = new CharacteristicsUpdater(cifAssetOrchestrator, dependentUpdateBuilderFactory);

    @Test
    public void shouldUpdateCharacteristicsInAsset() {
        //Given
        AssetKey assetKey = new AssetKey("anAssetId", 1L);
        CharacteristicChangeRequest requestList = new CharacteristicChangeRequest(assetKey,
                CIFAssetExtension.allExtensions(),
                newArrayList(new CharacteristicChange("A", "new_1"),
                        new CharacteristicChange("B", "new_2")),
                "lineItemId", 3);

        when(cifAssetOrchestrator.getAsset(new CIFAssetKey(assetKey, newArrayList(CIFAssetExtension.Relationships, CIFAssetExtension.AttributeDetails))))
                .thenReturn(CIFAssetFixture.aCIFAsset().withCharacteristic("A", "old_1").withCharacteristic("B", "old_2").build());

        //When
        CharacteristicChangeResponse responseList = characteristicsUpdater.performUpdate(requestList);

        //Then
        List<CharacteristicChange> characteristicChangeResponses = newArrayList(new CharacteristicChange("A", "new_1", "old_1"),
                new CharacteristicChange("B", "new_2", "old_2"));
        assertThat(responseList, is(new CharacteristicChangeResponse(requestList, characteristicChangeResponses, new ArrayList<CIFAssetUpdateRequest>())));

        ArgumentCaptor<CIFAsset> argumentCaptor = ArgumentCaptor.forClass(CIFAsset.class);
        verify(cifAssetOrchestrator, times(1)).saveAsset(argumentCaptor.capture());
        CIFAsset cifAsset = argumentCaptor.getValue();

        CIFAssetCharacteristic cifAssetCharacteristic_A = cifAsset.getCharacteristic("A");
        assertThat(cifAssetCharacteristic_A.getValue(), is("new_1"));

        CIFAssetCharacteristic cifAssetCharacteristic_B = cifAsset.getCharacteristic("B");
        assertThat(cifAssetCharacteristic_B.getValue(), is("new_2"));

    }

    @Test
    public void shouldUpdateCharacteristicsAndBuildDependantContributesToRequest() { //When a characteristic get updated, contributesTo requests should be built,
                                                                                     // so that source attributes of this characteristic change will be re-executed.
        //Given
        AssetKey assetKey = new AssetKey("anAssetId", 1L);
        CharacteristicChangeRequest requestList = new CharacteristicChangeRequest(assetKey, CIFAssetExtension.allExtensions(),
                newArrayList(new CharacteristicChange("A", "new_1"), new CharacteristicChange("B", "new_2")), "lineItemId", 1);

        CIFAsset asset = CIFAssetFixture.aCIFAsset().withID("anAssetId").withVersion(1L).withProductIdentifier("S123", "A.1").withCharacteristic("A", "old_1").withCharacteristic("B", "old_2").build();
        when(cifAssetOrchestrator.getAsset(new CIFAssetKey(assetKey, newArrayList(CIFAssetExtension.Relationships, CIFAssetExtension.AttributeDetails)))).thenReturn(asset);

        CharacteristicReloadRequest characteristicReloadRequest = new CharacteristicReloadRequest();
        ValidationImpactChangeRequest validationImpactChangeRequest = new ValidationImpactChangeRequest();
        Set<CIFAssetUpdateRequest> updateRequests = newHashSet(characteristicReloadRequest, validationImpactChangeRequest);
        when(contributesToChangeRequestBuilder.buildRequests(assetKey, "S123", "A", 1)).thenReturn(updateRequests);
        when(contributesToChangeRequestBuilder.buildRequests(assetKey, "S123", "B", 1)).thenReturn(Collections.<CIFAssetUpdateRequest>emptySet());

        //When
        CharacteristicChangeResponse responseList = characteristicsUpdater.performUpdate(requestList);

        //Then
        assertThat(responseList.getDependantUpdates().size(), is(2));
        assertThat(responseList.getDependantUpdates(), hasItems(characteristicReloadRequest, validationImpactChangeRequest));

    }

    @Test
    public void shouldChangeDateFormatAndUpdateValue() {
        //Given
        AssetKey assetKey = new AssetKey("anAssetId", 1L);
        CharacteristicChangeRequest requestList = new CharacteristicChangeRequest(assetKey,
                CIFAssetExtension.allExtensions(), newArrayList(new CharacteristicChange("aDateAttribute", "2015-Jun-10"),
                new CharacteristicChange("anotherDateAttribute", "2015-Jun-11")), "lineItemId", 3);

        when(cifAssetOrchestrator.getAsset(new CIFAssetKey(assetKey, newArrayList(CIFAssetExtension.Relationships, CIFAssetExtension.AttributeDetails))))
                .thenReturn(CIFAssetFixture.aCIFAsset().withCharacteristic("aDateAttribute", null, new CIFAssetAttributeDetail(false, false, Offering, DATE, true, "aDateAttribute", false, null))
                        .withCharacteristic("anotherDateAttribute", null, new CIFAssetAttributeDetail(false, false, Offering, DATE, true, "anotherDateAttribute", false, null)).build());

        //When
        CharacteristicChangeResponse responseList = characteristicsUpdater.performUpdate(requestList);

        //Then
        List<CharacteristicChange> characteristicChangeResponses = newArrayList(new CharacteristicChange("aDateAttribute", "2015-Jun-10", null),
                new CharacteristicChange("anotherDateAttribute", "2015-Jun-11", null));

        assertThat(responseList, is(new CharacteristicChangeResponse(requestList, characteristicChangeResponses, new ArrayList<CIFAssetUpdateRequest>())));

        ArgumentCaptor<CIFAsset> argumentCaptor = ArgumentCaptor.forClass(CIFAsset.class);
        verify(cifAssetOrchestrator, times(1)).saveAsset(argumentCaptor.capture());
        CIFAsset cifAsset = argumentCaptor.getValue();

        CIFAssetCharacteristic cifAssetCharacteristic = cifAsset.getCharacteristic("aDateAttribute");
        assertThat(cifAssetCharacteristic.getValue(), is("2015/06/10 00:00"));

        CIFAssetCharacteristic anotherCifAssetCharacteristic = cifAsset.getCharacteristic("anotherDateAttribute");
        assertThat(anotherCifAssetCharacteristic.getValue(), is("2015/06/11 00:00"));
    }

    @Test
    public void shouldUpdateSpecialBidCharacteristicAndBuildSpecialBidDependantRequest() {
        //Given
        AssetKey assetKey = new AssetKey("anAssetId", 1L);
        CharacteristicChangeRequest requestList = new CharacteristicChangeRequest(assetKey,
                CIFAssetExtension.allExtensions(),
                newArrayList(new CharacteristicChange(SPECIAL_BID_TEMPLATE_RESERVED_NAME, "newTemplateName")),
                "lineItemId", 3);

        when(cifAssetOrchestrator.getAsset(new CIFAssetKey(assetKey, newArrayList(CIFAssetExtension.Relationships, CIFAssetExtension.AttributeDetails))))
                .thenReturn(CIFAssetFixture.aCIFAsset().withCharacteristic(SPECIAL_BID_TEMPLATE_RESERVED_NAME, "oldTemplateName").build());

        //When
        CharacteristicChangeResponse responseList = characteristicsUpdater.performUpdate(requestList);

        //Then
        List<CharacteristicChange> characteristicChangeResponses = newArrayList(new CharacteristicChange(SPECIAL_BID_TEMPLATE_RESERVED_NAME, "newTemplateName", "oldTemplateName"));
        CIFAssetUpdateRequest attributesRefreshRequest = new SpecialBidAttributesReloadRequest(assetKey, SPECIAL_BID_TEMPLATE_RESERVED_NAME, "newTemplateName");
        CIFAssetUpdateRequest specialBidAttributesCreationRequest = new SpecialBidAttributesCreationRequest(assetKey);

        assertThat(responseList.getRequest(), is(requestList));
        assertThat(responseList.getCharacteristicChanges(), is(characteristicChangeResponses));
        assertThat(responseList.getDependantUpdates().size(), is(2));
        assertThat(responseList.getDependantUpdates(), hasItems(attributesRefreshRequest, specialBidAttributesCreationRequest));
    }


    @Test
    public void shouldUpdateCharacteristicsAndBuildSpecialBidCharacteristicsCreationRequestWhenNonStandardFlagChanged() {
        //Given
        AssetKey assetKey = new AssetKey("anAssetId", 1L);
        CharacteristicChangeRequest requestList = new CharacteristicChangeRequest(assetKey, CIFAssetExtension.allExtensions(),
                newArrayList(new CharacteristicChange(ProductOffering.SPECIAL_BID_ATTRIBUTE_INDICATOR, "Yes")), "lineItemId", 1);

        CIFAsset asset = CIFAssetFixture.aCIFAsset().withID("anAssetId").withVersion(1L).withProductIdentifier("S123", "A.1").withCharacteristic(ProductOffering.SPECIAL_BID_ATTRIBUTE_INDICATOR, "No").build();
        when(cifAssetOrchestrator.getAsset(new CIFAssetKey(assetKey, newArrayList(CIFAssetExtension.Relationships, CIFAssetExtension.AttributeDetails)))).thenReturn(asset);

        //When
        CharacteristicChangeResponse responseList = characteristicsUpdater.performUpdate(requestList);

        //Then
        assertThat(responseList.getDependantUpdates().size(), is(1));
        CIFAssetUpdateRequest specialBidAttributesCreationRequest = new SpecialBidAttributesCreationRequest(assetKey);
        assertThat(responseList.getDependantUpdates(), hasItems(specialBidAttributesCreationRequest));

    }

    @Test
    public void shouldUpdateCharacteristicsAndBuildInvalidatePriceRequest() {
        //Given
        AssetKey assetKey = new AssetKey("anAssetId", 1L);
        CharacteristicChangeRequest requestList = new CharacteristicChangeRequest(assetKey,
                CIFAssetExtension.allExtensions(),
                newArrayList(new CharacteristicChange("A", "new_1"),
                        new CharacteristicChange("B", "new_2")),
                "lineItemId", 3);

        when(cifAssetOrchestrator.getAsset(new CIFAssetKey(assetKey, newArrayList(CIFAssetExtension.Relationships, CIFAssetExtension.AttributeDetails))))
                .thenReturn(CIFAssetFixture.aCIFAsset().withCharacteristic("A", "old_1").withCharacteristic("B", "old_2").build());

        //When
        CharacteristicChangeResponse responseList = characteristicsUpdater.performUpdate(requestList);

        //Then
        List<CharacteristicChange> characteristicChangeResponses = newArrayList(new CharacteristicChange("A", "new_1", "old_1"),
                new CharacteristicChange("B", "new_2", "old_2"));
        assertThat(responseList, is(new CharacteristicChangeResponse(requestList, characteristicChangeResponses, new ArrayList<CIFAssetUpdateRequest>())));

        ArgumentCaptor<CIFAsset> argumentCaptor = ArgumentCaptor.forClass(CIFAsset.class);
        verify(cifAssetOrchestrator, times(1)).saveAsset(argumentCaptor.capture());
        CIFAsset cifAsset = argumentCaptor.getValue();

        CIFAssetCharacteristic cifAssetCharacteristic_A = cifAsset.getCharacteristic("A");
        assertThat(cifAssetCharacteristic_A.getValue(), is("new_1"));

        CIFAssetCharacteristic cifAssetCharacteristic_B = cifAsset.getCharacteristic("B");
        assertThat(cifAssetCharacteristic_B.getValue(), is("new_2"));


        verify(invalidatePriceRequestBuilder, times(1)).invalidatePriceForCharacteristicChanges(org.mockito.Matchers.any(CIFAsset.class), eq(newArrayList("A", "B")));

    }

}
